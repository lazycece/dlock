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

package com.lazycece.dlock.springboot;

import com.lazycece.dlock.core.DLock;
import com.lazycece.dlock.core.config.DLockConfig;
import com.lazycece.dlock.core.model.RedisDistributedLock;
import com.lazycece.dlock.springboot.autoconfigure.DLockProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author lazycece
 * @date 2025/4/30
 */
@Component
public class DLockFactory {

    private static DLockFactory instance;
    private final DLockProperties lockProperties;
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public DLockFactory(DLockProperties lockProperties, StringRedisTemplate redisTemplate) {
        this.lockProperties = lockProperties;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        instance = this;
    }

    public static DLockFactory getInstance() {
        return instance;
    }

    public DLock produce(String lockKey) {
        String lockValue = UUID.randomUUID().toString();
        if (lockProperties.isReentrancy()) {
            // TODO: 2025/5/1  怎么解决主子线程的问题
            lockValue = String.format("[%s]-[%s-%s]", lockProperties.getMachineInstanceId(),
                    Thread.currentThread().getName(), Thread.currentThread().getId());
        }

        RedisDistributedLock lock = new RedisDistributedLock(redisTemplate, lockKey, lockValue);
        lock.setLockConfig(this.getLockConfig());
        return lock;
    }

    private DLockConfig getLockConfig() {
        DLockConfig config = new DLockConfig();
        config.setDefaultWaitMillisTime(lockProperties.getDefaultWaitMillisTime());
        config.setTrySleepMillis(lockProperties.getTrySleepMillis());
        config.setEnableRenewal(lockProperties.isEnableRenewal());
        config.setRenewalPeriod(lockProperties.getRenewalPeriod());
        return config;
    }
}
