/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jmeter.util;

import org.apache.jmeter.testbeans.TestBean;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.util.*;
import java.util.Map.Entry;

/**
 *  解决JSR233加载 ScriptEngineFactory 空指针问题
 */
public abstract class JSR223BeanInfoSupport extends ScriptingBeanInfoSupport {

    private static final String[] LANGUAGE_TAGS;

    /**
     * Will be removed in next version following 3.2
     * @deprecated use {@link JSR223BeanInfoSupport#getLanguageNames()}
     */
    @Deprecated
    public static final String[][] LANGUAGE_NAMES; // NOSONAR Kept for backward compatibility

    private static final String[][] CONSTANT_LANGUAGE_NAMES;

    static {
        Map<String, ScriptEngineFactory> nameMap = new HashMap<>();
        ScriptEngineManager sem = new ScriptEngineManager();
        final List<ScriptEngineFactory> engineFactories = sem.getEngineFactories();
        for(ScriptEngineFactory fact : engineFactories){
            List<String> names = fact.getNames();
            for(String shortName : names) {
                if (shortName != null) {
                    nameMap.put(shortName.toLowerCase(Locale.ENGLISH), fact);
                }
            }
        }
        LANGUAGE_TAGS = nameMap.keySet().toArray(new String[nameMap.size()]);
        Arrays.sort(LANGUAGE_TAGS);
        CONSTANT_LANGUAGE_NAMES = new String[nameMap.size()][2];
        int i = 0;
        for(Entry<String, ScriptEngineFactory> me : nameMap.entrySet()) {
            final String key = me.getKey();
            CONSTANT_LANGUAGE_NAMES[i][0] = key;
            final ScriptEngineFactory fact = me.getValue();
            CONSTANT_LANGUAGE_NAMES[i++][1] = key +
                    "     (" // $NON-NLS-1$
                    + fact.getLanguageName() + " " + fact.getLanguageVersion()  // $NON-NLS-1$
                    + " / "  // $NON-NLS-1$
                    + fact.getEngineName() + " " + fact.getEngineVersion() // $NON-NLS-1$
                    + ")";   // $NON-NLS-1$
        }

        LANGUAGE_NAMES = getLanguageNames(); // NOSONAR Kept for backward compatibility
    }

    private static final ResourceBundle NAME_BUNDLE = new ListResourceBundle() {
        @Override
        protected Object[][] getContents() {
            return CONSTANT_LANGUAGE_NAMES;
        }
    };

    protected JSR223BeanInfoSupport(Class<? extends TestBean> beanClass) {
        super(beanClass, LANGUAGE_TAGS, NAME_BUNDLE);
    }

    /**
     * @return String array of 2 columns array containing Script engine short name / Script Language details
     */
    public static final String[][] getLanguageNames() {
        return CONSTANT_LANGUAGE_NAMES.clone();
    }

}
