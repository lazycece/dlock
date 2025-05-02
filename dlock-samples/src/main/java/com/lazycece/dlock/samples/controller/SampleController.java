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

package com.lazycece.dlock.samples.controller;

import com.lazycece.dlock.core.DLock;
import com.lazycece.dlock.core.DLockFactory;
import com.lazycece.dlock.core.DLockUtils;
import com.lazycece.dlock.core.exception.DLockTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author lazycece
 * @date 2025/5/1
 */
@RestController
public class SampleController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/sample")
    public String sample() {
        log.info("================== sample begin");

        try {
            DLockUtils.tryLock("sample", 60, TimeUnit.SECONDS, () -> {
                log.info("================== sample handle something");
                try {
                    Thread.sleep(15 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } catch (DLockTimeoutException e) {
            return e.getMessage();
        }
        log.info("================== sample end");
        return "sample end";
    }

    @GetMapping("/renewals")
    public String renewals() {
        log.info("================== renewals begin");

        try {
            DLockUtils.tryLock("renewals", 30, TimeUnit.SECONDS, () -> {
                log.info("================== renewals handle something");
                try {
                    Thread.sleep(29 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } catch (DLockTimeoutException e) {
            return e.getMessage();
        }
        log.info("================== renewals end");
        return "renewals end";
    }

    @GetMapping("/reentrant")
    public String reentrant() throws InterruptedException {
        log.info("================== reentrant begin");

        DLock lock = DLockFactory.getInstance().produce("reentrant");
        if (lock.tryLock(60, TimeUnit.SECONDS)) {
            try {
                if (lock.tryLock(60, TimeUnit.SECONDS)) {
                    try {
                        Thread.sleep(20 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            lock.unlock();
                        } catch (Exception e) {
                            log.error("unlock error, lockKey = {}", "reentrant", e);
                        }
                    }
                } else {
                    throw new DLockTimeoutException("try lock timeout!");
                }
            } finally {
                try {
                    lock.unlock();
                } catch (Exception e) {
                    log.error("unlock error, lockKey = {}", "reentrant", e);
                }
            }
        } else {
            throw new DLockTimeoutException("try lock timeout!");
        }

        log.info("================== reentrant end");
        return "reentrant end";
    }


}
