/*
 *    Copyright 2024 lazycece<lazycece@gmail.com>
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

import java.util.concurrent.TimeUnit;

/**
 * @author lazycece
 * @date 2024/9/9
 */
public interface DLock {

    /**
     * try lock using default wait time
     *
     * @param leaseTime     lease time.
     * @param leaseTimeUnit time unit
     */
    boolean tryLock(long leaseTime, TimeUnit leaseTimeUnit);

    /**
     * try lock
     *
     * @param waitMillisTime wait time while trying.
     * @param leaseTime      lease time
     * @param leaseTimeUnit  lease time unit
     * @return lock result
     */
    boolean tryLock(long waitMillisTime, long leaseTime, TimeUnit leaseTimeUnit);

    /**
     * Releases the lock.
     */
    void unlock();

    /**
     * locked or not
     *
     * @return result
     */
    boolean isLocked();

    /**
     * Number of holds on this lock by the current.
     *
     * @return result
     */
    int getHoldCount();

}
