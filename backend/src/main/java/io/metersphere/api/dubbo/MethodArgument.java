/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.metersphere.api.dubbo;


import io.metersphere.api.dubbo.utils.JsonUtils;
import io.metersphere.api.dubbo.utils.StringUtils;

import java.io.Serializable;


/**
 * MethodArgument
 */
public class MethodArgument implements Serializable {

    private static final long serialVersionUID = -2567457932227227262L;
    private String paramType;
    private String paramValue;

    public MethodArgument(String paramType, String paramValue) {
        setParamType(paramType);
        setParamValue(paramValue);
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = (paramType == null ? null : StringUtils.trimAllWhitespace(paramType));
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = (paramValue == null ? null : StringUtils.trimWhitespace(paramValue));
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((paramType == null) ? 0 : paramType.hashCode());
        result = prime * result + ((paramValue == null) ? 0 : paramValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MethodArgument other = (MethodArgument) obj;
        if (paramType == null) {
            if (other.paramType != null)
                return false;
        } else if (!paramType.equals(other.paramType))
            return false;
        if (paramValue == null) {
            if (other.paramValue != null)
                return false;
        } else if (!paramValue.equals(other.paramValue))
            return false;
        return true;
    }

}
