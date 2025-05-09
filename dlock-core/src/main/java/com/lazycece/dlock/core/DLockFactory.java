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

package com.lazycece.dlock.core;

import com.lazycece.dlock.core.config.DLockConfig;
import com.lazycece.dlock.core.model.RedisDistributedLock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * @author lazycece
 * @date 2025/4/30
 */
public class DLockFactory {

    private static DLockFactory instance;
    private final StringRedisTemplate redisTemplate;
    private final ThreadLocal<String> dlockToken = new ThreadLocal<>();

    private DLockConfig lockConfig = new DLockConfig();

    public DLockFactory(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        instance = this;
    }

    public static DLockFactory getInstance() {
        return instance;
    }

    public DLock produce(String lockKey) {
        // thread reentrant
        String token = dlockToken.get();
        if (!StringUtils.hasText(token)) {
            token = UUID.randomUUID().toString();
            dlockToken.set(token);
        }

        RedisDistributedLock lock = new RedisDistributedLock(redisTemplate, lockKey, token);
        lock.setLockConfig(lockConfig);
        return lock;
    }

    public void setLockConfig(DLockConfig lockConfig) {
        this.lockConfig = lockConfig;
    }
}
