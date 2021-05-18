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

import java.util.EnumSet;

/**
 * Created by tomerga on 04/09/2016.
 */
public enum CompatibilityFlags {
    MISSING_VALUES_AS_NULLS,
    REMOVE_NONE_EXISTING_ARRAY_ELEMENT,
    ALLOW_MISSING_TARGET_OBJECT_ON_REPLACE;

    public static EnumSet<CompatibilityFlags> defaults() {
        return EnumSet.noneOf(CompatibilityFlags.class);
    }
}
