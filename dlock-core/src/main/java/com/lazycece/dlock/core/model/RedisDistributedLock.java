/*
 *    Copyright 2025 lazycece<lazycece@gmail.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.lazycece.dlock.core.model;

import com.alibaba.fastjson2.JSON;
import com.lazycece.dlock.core.DLock;
import com.lazycece.dlock.core.config.DLockConfig;
import com.lazycece.dlock.core.exception.DLockException;
import com.lazycece.dlock.core.script.LuaScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author lazycece
 * @date 2025/4/20
 */
public class RedisDistributedLock implements DLock {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /* redis script begin */
    private final static RedisScript<Long> lockScript = RedisScript.of(LuaScript.LOCK_SCRIPT, Long.class);
    private final static RedisScript<Long> unLockScript = RedisScript.of(LuaScript.UNLOCK_SCRIPT, Long.class);
    /* redis script end */

    /**
     * lock config, given default information. Can custom using <code>{@code setLockConfig}</code>
     *
     * @see DLockConfig
     * @see RedisDistributedLock#setLockConfig
     */
    private DLockConfig dLockConfig = new DLockConfig();

    private volatile boolean isLocked = false;

    /* init parameter begin */
    private final StringRedisTemplate redisTemplate;
    private final String lockKey;
    private final String threadId;
    private final ScheduledExecutorService renewExecutor;
    /* init parameter end */

    public RedisDistributedLock(StringRedisTemplate redisTemplate, String lockKey, String threadId) {
        // variable
        this.redisTemplate = redisTemplate;
        this.lockKey = lockKey;
        this.threadId = threadId;

        // default renewal
        this.renewExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    public void setLockConfig(DLockConfig dLockConfig) {
        this.dLockConfig = dLockConfig;
    }

    @Override
    public void lock(long leaseTime, TimeUnit unit) {
        this.tryLock(0, leaseTime, unit);
    }

    @Override
    public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) {
        long waitMillisTime = TimeoutUtils.toMillis(waitTime, unit);
        long expireMillisTime = TimeoutUtils.toMillis(leaseTime, unit);
        long start = System.currentTimeMillis();

        try {
            LockedValue lockedValue = LockedValue.lockedValue(threadId, 1);
            String lockedValueJson = JSON.toJSONString(lockedValue);

            while (true) {
                Long result = redisTemplate.execute(lockScript, Collections.singletonList(lockKey)
                        , Collections.singletonList(lockedValueJson), String.valueOf(expireMillisTime));

                if (LuaScript.SUCCESS.equals(result)) {
                    isLocked = true;
                    this.startRenewal(leaseTime, unit);
                    return true;
                }

                if (System.currentTimeMillis() - start >= waitMillisTime) {
                    return false;
                }

                // sleepy
                Thread.sleep(dLockConfig.getTrySleepMillis());
            }
        } catch (Exception e) {
            throw new DLockException("lock fail !", e);
        }
    }

    @Override
    public void unlock() {
        if (!isLocked) {
            return;
        }

        try {
            LockedValue lockedValue = LockedValue.lockedValue(threadId, 1);
            String lockedValueJson = JSON.toJSONString(lockedValue);

            Long result = redisTemplate.execute(unLockScript, Collections.singletonList(lockKey)
                    , Collections.singletonList(lockedValueJson));

            if (LuaScript.SUCCESS.equals(result)) {
                this.stopRenewal();
                isLocked = false;
            } else {
                throw new DLockException("try to release a lock that is not owned.");
            }
        } catch (Exception e) {
            throw new DLockException("unlock fail !", e);
        }
    }

    @Override
    public boolean isLocked() {
        return this.isLocked;
    }

    @Override
    public int getHoldCount() {
        try {
            String lockedValueJson = redisTemplate.opsForValue().get(lockKey);
            LockedValue lockedValue = JSON.parseObject(lockedValueJson, LockedValue.class);
            if (lockedValue == null) {
                return 0;
            } else {
                return lockedValue.getCount() == null ? 0 : lockedValue.getCount();
            }
        } catch (Exception e) {
            throw new DLockException("Get hold count fail.", e);
        }
    }

    /**
     * Start the lock renewals task.
     */
    private void startRenewal(long leaseTime, TimeUnit unit) {
        renewExecutor.scheduleAtFixedRate(() -> {
            // no locks, no renewal required.
            if (!isLocked) {
                return;
            }

            try {
                String lockedValueJson = redisTemplate.opsForValue().get(lockKey);
                LockedValue lockedValue = JSON.parseObject(lockedValueJson, LockedValue.class);
                if (lockedValue != null && threadId.equals(lockedValue.getThreadId())) {
                    // current own, to renew
                    redisTemplate.expire(lockKey, leaseTime, unit);
                } else {
                    // lost lock, to stop renew
                    stopRenewal();
                    isLocked = false;
                }
            } catch (Exception e) {
                // renewal failed, print log.
                log.error("lock renewals fail: {}", e.getMessage(), e);
            }
        }, dLockConfig.getRenewalPeriod() / 3, dLockConfig.getRenewalPeriod() / 3, TimeUnit.MILLISECONDS);
    }

    /**
     * Stop the lock renewals task.
     */
    private void stopRenewal() {
        renewExecutor.shutdownNow();
    }

}
