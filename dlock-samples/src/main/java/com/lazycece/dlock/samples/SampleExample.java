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

package com.lazycece.dlock.samples;

import com.lazycece.dlock.core.DLock;
import com.lazycece.dlock.core.DLockFactory;
import com.lazycece.dlock.core.DLockUtils;
import com.lazycece.dlock.core.exception.DLockTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author lazycece
 * @date 2025/5/2
 */
public class SampleExample {

    private final static Logger log = LoggerFactory.getLogger(SampleExample.class);

    public static void main(String[] args) {
        // example
        String lockKey = "lock_key";
        DLock lock = DLockFactory.getInstance().produce(lockKey);
        if (lock.tryLock(10, TimeUnit.SECONDS)) {
            try {
                // do something
            } finally {
                try {
                    lock.unlock();
                } catch (Exception e) {
                    log.error("unlock error, lockKey = {}", lockKey, e);
                }
            }
        } else {
            throw new DLockTimeoutException("try lock timeout!");
        }

        // example
        DLockUtils.tryLock(lockKey, 10, TimeUnit.SECONDS, () -> {
            // do something
        });

    }
}
