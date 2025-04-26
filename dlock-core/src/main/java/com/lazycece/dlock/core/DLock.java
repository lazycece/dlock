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
     * lock
     *
     * @param leaseTime lease time.
     * @param unit      time unit
     */
    void lock(long leaseTime, TimeUnit unit);

    /**
     * try lock
     *
     * @param waitTime  wait time while trying.
     * @param leaseTime lease time
     * @param unit      time unit
     * @return lock result
     */
    boolean tryLock(long waitTime, long leaseTime, TimeUnit unit);

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
