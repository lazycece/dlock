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

package com.lazycece.dlock.core.config;

/**
 * @author lazycece
 * @date 2025/4/20
 */
public class DLockConfig {

    private long trySleepMillis = 100;

    /**
     * The renewal period, default 10 * 1000 milliseconds.
     */
    private long renewalPeriod = 10 * 1000;


    public long getTrySleepMillis() {
        return trySleepMillis;
    }

    public void setTrySleepMillis(long trySleepMillis) {
        this.trySleepMillis = trySleepMillis;
    }

    public long getRenewalPeriod() {
        return renewalPeriod;
    }

    public void setRenewalPeriod(long renewalPeriod) {
        this.renewalPeriod = renewalPeriod;
    }
}
