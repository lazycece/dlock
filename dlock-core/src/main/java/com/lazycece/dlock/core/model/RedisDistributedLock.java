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
    private DLockConfig lockConfig = new DLockConfig();

    private volatile boolean isLocked = false;

    /* init parameter begin */
    private final StringRedisTemplate redisTemplate;
    private final String lockKey;
    private final String token;
    private ScheduledExecutorService renewExecutor;
    /* init parameter end */

    public RedisDistributedLock(StringRedisTemplate redisTemplate, String lockKey, String token) {
        // variable
        this.redisTemplate = redisTemplate;
        this.lockKey = lockKey;
        this.token = token;

        // default renewal
        if (lockConfig.isEnableRenewal()) {
            this.renewExecutor = Executors.newSingleThreadScheduledExecutor();
        }
    }

    public void setLockConfig(DLockConfig lockConfig) {
        this.lockConfig = lockConfig;
    }

    @Override
    public boolean tryLock(long leaseTime, TimeUnit leaseTimeunit) {
        return this.tryLock(lockConfig.getDefaultWaitMillisTime(), leaseTime, leaseTimeunit);
    }

    @Override
    public boolean tryLock(long waitMillisTime, long leaseTime, TimeUnit leaseTimeunit) {
        long expireMillisTime = TimeoutUtils.toMillis(leaseTime, leaseTimeunit);
        long start = System.currentTimeMillis();

        try {
            LockedValue lockedValue = LockedValue.lockedValue(token, 1);
            String lockedValueJson = JSON.toJSONString(lockedValue);

            while (true) {
                Long result = redisTemplate.execute(lockScript, Collections.singletonList(lockKey)
                        , lockedValueJson, String.valueOf(expireMillisTime));

                if (LuaScript.SUCCESS.equals(result)) {
                    isLocked = true;
                    this.startRenewal(leaseTime, leaseTimeunit);
                    return true;
                }

                if (System.currentTimeMillis() - start >= waitMillisTime) {
                    return false;
                }

                // sleepy
                Thread.sleep(lockConfig.getTrySleepMillis());
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
            LockedValue lockedValue = LockedValue.lockedValue(token, 1);
            String lockedValueJson = JSON.toJSONString(lockedValue);

            Long result = redisTemplate.execute(unLockScript, Collections.singletonList(lockKey), lockedValueJson);

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
    private void startRenewal(long leaseTime, TimeUnit leaseTimeunit) {
        if (renewExecutor == null) {
            log.debug("not open lock renewals service ! ");
            return;
        }

        log.debug("lock renewal service start, lockKey = {}", lockKey);
        renewExecutor.scheduleAtFixedRate(() -> {
            // no locks, no renewal required.
            if (!isLocked) {
                log.debug("not owned lock, need not to  renewal, lockKey = {}", lockKey);
                return;
            }

            try {
                String lockedValueJson = redisTemplate.opsForValue().get(lockKey);
                LockedValue lockedValue = JSON.parseObject(lockedValueJson, LockedValue.class);
                if (lockedValue != null && token.equals(lockedValue.getToken())) {
                    // current own, to renew
                    redisTemplate.expire(lockKey, leaseTime, leaseTimeunit);
                    log.debug("lock renewal successful, lockKey = {}", lockKey);
                } else {
                    // lost lock, to stop renew
                    log.debug("not owned lock, stop renewal service, lockKey = {}", lockKey);
                    this.stopRenewal();
                    isLocked = false;
                }
            } catch (Exception e) {
                // renewal failed, print log.
                log.error("lock renewals fail: {}", e.getMessage(), e);
            }
        }, lockConfig.getRenewalPeriod() / 3, lockConfig.getRenewalPeriod() / 3, TimeUnit.MILLISECONDS);
    }

    /**
     * Stop the lock renewals task.
     */
    private void stopRenewal() {
        if (renewExecutor == null) {
            log.debug("not open lock renewals service, need not to stop ! ");
            return;
        }
        renewExecutor.shutdownNow();
    }

}
