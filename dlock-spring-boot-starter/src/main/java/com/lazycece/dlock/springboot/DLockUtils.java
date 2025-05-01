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
import com.lazycece.dlock.core.exception.DLockTimeoutException;
import com.lazycece.dlock.springboot.function.Answer;
import com.lazycece.dlock.springboot.function.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author lazycece
 * @date 2025/4/30
 */
public class DLockUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DLockUtils.class);

    public static void tryLock(String lockKey, long leaseTime, TimeUnit unit, Handler handler) {
        DLock lock = DLockFactory.getInstance().produce(lockKey);
        if (lock.tryLock(leaseTime, unit)) {
            try {
                handler.handle();
            } finally {
                try {
                    lock.unlock();
                } catch (Exception e) {
                    LOGGER.error("unlock error, lockKey = {}", lockKey, e);
                }
            }
        } else {
            throw new DLockTimeoutException("try lock timeout!");
        }
    }

    public static void tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Handler handler) {
        DLock lock = DLockFactory.getInstance().produce(lockKey);
        if (lock.tryLock(waitTime, leaseTime, unit)) {
            try {
                handler.handle();
            } finally {
                try {
                    lock.unlock();
                } catch (Exception e) {
                    LOGGER.error("unlock error, lockKey = {}", lockKey, e);
                }
            }
        } else {
            throw new DLockTimeoutException("try lock timeout!");
        }
    }

    public static <T> T tryLock(String lockKey, long leaseTime, TimeUnit unit, Answer<T> answer) {
        DLock lock = DLockFactory.getInstance().produce(lockKey);
        if (lock.tryLock(leaseTime, unit)) {
            try {
                return answer.reply();
            } finally {
                try {
                    lock.unlock();
                } catch (Exception e) {
                    LOGGER.error("unlock error, lockKey = {}", lockKey, e);
                }
            }
        } else {
            throw new DLockTimeoutException("try lock timeout!");
        }
    }

    public static <T> T tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Answer<T> answer) {
        DLock lock = DLockFactory.getInstance().produce(lockKey);
        if (lock.tryLock(waitTime, leaseTime, unit)) {
            try {
                return answer.reply();
            } finally {
                try {
                    lock.unlock();
                } catch (Exception e) {
                    LOGGER.error("unlock error, lockKey = {}", lockKey, e);
                }
            }
        } else {
            throw new DLockTimeoutException("try lock timeout!");
        }
    }

}
