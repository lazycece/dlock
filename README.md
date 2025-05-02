# dlock

[![Maven Central](https://img.shields.io/maven-central/v/com.lazycece.dlock/dlock-spring-boot-starter)](https://search.maven.org/search?q=dlock-spring-boot-starter)
[![License](https://img.shields.io/badge/license-Apache--2.0-green)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![GitHub release](https://img.shields.io/badge/release-download-orange.svg)](https://github.com/lazycece/cell/releases)

![dlock-logo](/document/logo/logo_1.png)

dlock 是一个使用redis实现的分布式锁组件，具备可重入性以及自动续约能力。

## 环境依赖

dlock 环境依赖如下:

|dlock|Java|Spring Boot|
|---|---|---|
|1.x|17+|3.x|

## 快速开始

### Maven Dependency

```xml

<dependency>
    <groupId>com.lazycece.dlock</groupId>
    <artifactId>dlock-spring-boot-starter</artifactId>
    <version>${dlock.version}</version>
</dependency>
```

### Coding

```java
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
```

更多样例详情可查看 [dlock-samples](/dlock-samples)

### 自定义配置

|配置 |默认值 |描述 |
|--- |---   |--- |
|dlock.default-wait-millis-time |50 |尝试获取锁的默认等待时长 |
|dlock.try-sleep-millis |100 |尝试获取锁过程中的线程休眠时长 |
|dlock.enable-renewal |true |是否开启锁自动续约能力 |
|dlock.renewal-threshold |3000 |如果开启锁自动续约能力，那么锁的租约时间需要达到给定的阙值 |

## License

[Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0.html)