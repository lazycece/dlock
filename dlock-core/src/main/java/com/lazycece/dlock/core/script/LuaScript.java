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

package com.lazycece.dlock.core.script;

/**
 * @author lazycece
 * @date 2024/9/14
 */
public class LuaScript {

    public static final Long SUCCESS = 1L;

    // lock lua script
    public static final String LOCK_SCRIPT =
                    "local lockKey = KEYS[1]\n" +
                    "local lockValue = ARGV[1]\n" +
                    "local expireTime = tonumber(ARGV[2])\n" +
                    "local currentValue = redis.call('get', lockKey)\n" +
                    "if currentValue == false then\n" +
                    "    local newValue = cjson.decode(lockValue)\n" +
                    "    newValue.expireTime = redis.call('time')[1] * 1000 + expireTime\n" +
                    "    redis.call('set', lockKey, cjson.encode(newValue), 'PX', expireTime)\n" +
                    "    return 1\n" +
                    "else\n" +
                    "    local map = cjson.decode(currentValue)\n" +
                    "    if map.token == cjson.decode(lockValue).token then\n" +
                    "        map.count = map.count + 1\n" +
                    "        map.expireTime = redis.call('time')[1] * 1000 + expireTime\n" +
                    "        redis.call('set', lockKey, cjson.encode(map), 'PX', expireTime)\n" +
                    "        return 1\n" +
                    "    end\n" +
                    "    return 0\n" +
                    "end";

    // unlock lua script
    public static final String UNLOCK_SCRIPT =
                    "local lockKey = KEYS[1]\n" +
                    "local lockValue = ARGV[1]\n" +
                    "local currentValue = redis.call('get', lockKey)\n" +
                    "if currentValue == false then\n" +
                    "    return 1\n" +
                    "else\n" +
                    "    local map = cjson.decode(currentValue)\n" +
                    "    if map.token == cjson.decode(lockValue).token then\n" +
                    "        if map.count > 1 then\n" +
                    "            map.count = map.count - 1\n" +
                    "            redis.call('set', lockKey, cjson.encode(map), 'PX', map.expireTime - redis.call('time')[1] * 1000)\n" +
                    "            return 1\n" +
                    "        else\n" +
                    "            redis.call('del', lockKey)\n" +
                    "            return 1\n" +
                    "        end\n" +
                    "    end\n" +
                    "    return 0\n" +
                    "end";

}
