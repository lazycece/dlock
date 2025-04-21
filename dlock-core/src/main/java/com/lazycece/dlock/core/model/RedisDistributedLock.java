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
import com.alibaba.fastjson2.TypeReference;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author lazycece
 * @date 2025/4/20
 */
public class RedisDistributedLock implements DLock {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private DLockConfig dLockConfig = new DLockConfig();
    private volatile boolean isLocked = false;

    /* init parameter begin */
    private final StringRedisTemplate redisTemplate;
    private final String lockKey;
    private final String threadId;

    private final ScheduledExecutorService renewExecutor;
    private final RedisScript<Long> lockScript;
    private final RedisScript<Long> unLockScript;
    /* init parameter end */


    public RedisDistributedLock(StringRedisTemplate redisTemplate, String lockKey, String threadId) {

        this.redisTemplate = redisTemplate;
        this.lockKey = lockKey;
        this.threadId = threadId;

        this.renewExecutor = Executors.newSingleThreadScheduledExecutor();
        this.lockScript = RedisScript.of(LuaScript.LOCK_SCRIPT, Long.class);
        this.unLockScript = RedisScript.of(LuaScript.UNLOCK_SCRIPT, Long.class);
    }

    @Override
    public void lock() {
        // TODO: 2025/4/21  lazycece
    }

    @Override
    public void lock(long leaseTime, TimeUnit unit) {
        // TODO: 2025/4/21  lazycece
    }

    @Override
    public boolean tryLock() {
        // TODO: 2025/4/21  lazycece
        return false;
    }

    @Override
    public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) {

        long expireMillisTime = TimeoutUtils.toMillis(leaseTime, unit);
        long start = System.currentTimeMillis();

        try {
            Map<String, Object> threadInfo = new HashMap<>();
            threadInfo.put("threadId", threadId);
            threadInfo.put("count", 1);
            String threadInfoJson = JSON.toJSONString(threadInfo);

            while (true) {
                Long result = redisTemplate.execute(lockScript, Collections.singletonList(lockKey)
                        , Collections.singletonList(threadInfoJson), String.valueOf(expireMillisTime));

                if (LuaScript.SUCCESS.equals(result)) {
                    isLocked = true;
                    this.startRenewal(leaseTime, unit);
                    return true;
                }

                if (System.currentTimeMillis() - start >= waitTime) {
                    return false;
                }

                // 避免CPU空转
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
            Map<String, Object> threadInfo = new HashMap<>();
            threadInfo.put("threadId", threadId);
            threadInfo.put("count", 1);
            String threadInfoJson = JSON.toJSONString(threadInfo);

            Long result = redisTemplate.execute(unLockScript, Collections.singletonList(lockKey)
                    , Collections.singletonList(threadInfoJson));

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

    /**
     * Start the lock renewals task.
     */
    private void startRenewal(long leaseTime, TimeUnit unit) {
        renewExecutor.scheduleAtFixedRate(() -> {
            if (!isLocked) {
                return;
            }

            try {
                // 检查锁是否仍被当前线程持有
                String currentValue = redisTemplate.opsForValue().get(lockKey);
                if (currentValue != null) {
                    Map<String, Object> map = JSON.parseObject(currentValue, new TypeReference<Map<String, Object>>() {
                    });
                    if (threadId.equals(map.get("threadId"))) {
                        // 续期
                        redisTemplate.expire(lockKey, leaseTime, unit);
                    } else {
                        // 锁已被其他线程获取，停止续期
                        stopRenewal();
                        isLocked = false;
                    }
                } else {
                    // 锁已过期，停止续期
                    stopRenewal();
                    isLocked = false;
                }
            } catch (Exception e) {
                // 续期失败，记录日志
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
