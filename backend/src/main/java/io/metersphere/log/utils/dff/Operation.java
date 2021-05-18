/*
 * Copyright 2016 flipkart.com zjsonpatch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package io.metersphere.log.utils.dff;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * User: gopi.vishwakarma
 * Date: 30/07/14
 */
public enum Operation {
    ADD("添加"),
    REMOVE("移除"),
    REPLACE("修改"),
    MOVE("移动"),
    COPY("复制"),
    TEST("测试");

    private final static Map<String, Operation> OPS = createImmutableMap();

    private static Map<String, Operation> createImmutableMap() {
        Map<String, Operation> map = new HashMap<String, Operation>();
        map.put(ADD.rfcName, ADD);
        map.put(REMOVE.rfcName, REMOVE);
        map.put(REPLACE.rfcName, REPLACE);
        map.put(MOVE.rfcName, MOVE);
        map.put(COPY.rfcName, COPY);
        map.put(TEST.rfcName, TEST);
        return Collections.unmodifiableMap(map);
    }

    private String rfcName;

    Operation(String rfcName) {
        this.rfcName = rfcName;
    }

    public static Operation fromRfcName(String rfcName) throws InvalidJsonPatchException {
        if (rfcName == null) throw new InvalidJsonPatchException("rfcName cannot be null");
        Operation op = OPS.get(rfcName.toLowerCase());
        if (op == null) throw new InvalidJsonPatchException("unknown / unsupported operation " + rfcName);
        return op;
    }

    public String rfcName() {
        return this.rfcName;
    }


}
