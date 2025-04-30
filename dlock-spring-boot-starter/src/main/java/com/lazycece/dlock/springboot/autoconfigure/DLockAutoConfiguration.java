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

package com.lazycece.dlock.springboot.autoconfigure;

import com.alibaba.fastjson2.JSON;
import com.lazycece.dlock.springboot.DLockFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author lazycece
 * @date 2025/4/27
 */
@Configuration
@EnableConfigurationProperties({DLockProperties.class})
@ComponentScan(basePackages = "com.lazycece.dlock.*")
@ConditionalOnBean(DLockFactory.class)
public class DLockAutoConfiguration implements InitializingBean {

    private final Logger log = LoggerFactory.getLogger(DLockAutoConfiguration.class);
    private final DLockProperties lockProperties;

    @Autowired
    public DLockAutoConfiguration(DLockProperties lockProperties) {
        this.lockProperties = lockProperties;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("dlock loading completed, lock config is: {}", JSON.toJSONString(lockProperties));
    }
}
