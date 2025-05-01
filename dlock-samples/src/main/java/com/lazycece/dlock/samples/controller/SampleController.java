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
import com.lazycece.dlock.core.exception.DLockException;
import com.lazycece.dlock.core.model.RedisDistributedLock;
import com.lazycece.dlock.springboot.DLockUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author lazycece
 * @date 2025/5/1
 */
@RestController
public class SampleController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/core-sample")
    public void coreSample() throws InterruptedException {
        log.info("================== core sample begin");

        DLock lock = new RedisDistributedLock(redisTemplate, "coreSample", UUID.randomUUID().toString());
        if (lock.tryLock(5, TimeUnit.MINUTES)) {
            try {
                log.info("================== core sample handle something .");
                Thread.sleep(30 * 1000);
            } finally {
                try {
                    lock.unlock();
                } catch (Exception e) {
                    log.error(" core sample unlock error", e);
                }
            }
        } else {
            throw new DLockException("try lock timeout !");
        }

        log.info("================== core sample end");

    }


    @GetMapping("/spring-sample")
    public void springSample() throws InterruptedException {
        log.info("================== spring sample begin");

        DLockUtils.tryLock("springSample", 5, TimeUnit.MINUTES, () -> {
            log.info("================== spring sample handle something .");
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        log.info("================== spring sample end");
    }
}
