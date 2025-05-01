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

    /**
     * The default timeout waiting time while trying lock, default value is 50 milliseconds.
     */
    private long defaultWaitMillisTime = 50;

    /**
     * thread sleep time while trying lock.
     */
    private long trySleepMillis = 100;

    /**
     * Whether to enable the renewal capability.
     */
    private boolean enableRenewal = true;

    /**
     * The renewal threshold, default the lease time is greater than or equal to 3000 millisecond.
     */
    private long renewalThreshold = 3000;

    public long getDefaultWaitMillisTime() {
        return defaultWaitMillisTime;
    }

    public void setDefaultWaitMillisTime(long defaultWaitMillisTime) {
        this.defaultWaitMillisTime = defaultWaitMillisTime;
    }

    public long getTrySleepMillis() {
        return trySleepMillis;
    }

    public void setTrySleepMillis(long trySleepMillis) {
        this.trySleepMillis = trySleepMillis;
    }

    public boolean isEnableRenewal() {
        return enableRenewal;
    }

    public void setEnableRenewal(boolean enableRenewal) {
        this.enableRenewal = enableRenewal;
    }

    public long getRenewalThreshold() {
        return renewalThreshold;
    }

    public void setRenewalThreshold(long renewalThreshold) {
        this.renewalThreshold = renewalThreshold;
    }
}
