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
import com.lazycece.dlock.springboot.function.Handler;

import java.util.concurrent.TimeUnit;

/**
 * @author lazycece
 * @date 2025/4/30
 */
public class DLockUtils {

    /**
     * try lock handle
     *
     * @param lockKey   lock key
     * @param leaseTime lease time
     * @param unit      time unit
     * @param handler   handler
     */
    public static void tryLock(String lockKey, long leaseTime, TimeUnit unit, Handler handler) {
        DLock lock = DLockFactory.getInstance().produce(lockKey);
        if (lock.tryLock(leaseTime, unit)) {
            handler.handle();
        } else {
            throw new DLockTimeoutException("try lock timeout!");
        }
    }

    /**
     * try lock handle
     *
     * @param lockKey   lock key
     * @param waitTime  wait time while trying.
     * @param leaseTime lease time
     * @param unit      time unit
     * @param handler   handler
     */
    public static void tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Handler handler) {
        DLock lock = DLockFactory.getInstance().produce(lockKey);
        if (lock.tryLock(waitTime, leaseTime, unit)) {
            handler.handle();
        } else {
            throw new DLockTimeoutException("try lock timeout!");
        }
    }

}
