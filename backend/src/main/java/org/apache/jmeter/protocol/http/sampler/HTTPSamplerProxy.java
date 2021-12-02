/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.protocol.http.sampler;

import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.samplers.Interruptible;

import java.net.URL;

/**
 * Proxy class that dispatches to the appropriate HTTP sampler.
 * <p>
 * This class is stored in the test plan, and holds all the configuration settings.
 * The actual implementation is created at run-time, and is passed a reference to this class
 * so it can get access to all the settings stored by HTTPSamplerProxy.
 */
public final class HTTPSamplerProxy extends HTTPSamplerBase implements Interruptible {

    private static final long serialVersionUID = 1L;

    private transient HTTPAbstractImpl impl;

    public HTTPSamplerProxy() {
        super();
    }

    /**
     * Convenience method used to initialise the implementation.
     *
     * @param impl the implementation to use.
     */
    public HTTPSamplerProxy(String impl) {
        super();
        setImplementation(impl);
    }

    protected String toExternalForm(URL u) {
        int len = u.getProtocol().length() + 1;
        if (u.getAuthority() != null && u.getAuthority().length() > 0)
            len += 2 + u.getAuthority().length();
        if (u.getPath() != null) {
            len += u.getPath().length();
        }
        if (u.getQuery() != null) {
            len += 1 + u.getQuery().length();
        }
        if (u.getRef() != null)
            len += 1 + u.getRef().length();

        StringBuffer result = new StringBuffer(len);
        result.append(u.getProtocol());
        result.append(":");
        if (u.getAuthority() != null && u.getAuthority().length() > 0) {
            result.append("//");
            result.append(u.getAuthority());
        }
        if (StringUtils.isNotEmpty(u.getPath())) {
            int index = 0;
            for (int i = 0; i < u.getPath().length(); i++) {
                char ch = u.getPath().charAt(i);
                if (String.valueOf(ch).equals("/")) {
                    index++;
                } else {
                    break;
                }
            }
            result.append("/" + u.getPath().substring(index));
        }
        if (u.getQuery() != null) {
            result.append('?');
            result.append(u.getQuery());
        }
        if (u.getRef() != null) {
            result.append("#");
            result.append(u.getRef());
        }
        return result.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HTTPSampleResult sample(URL u, String method, boolean areFollowingRedirect, int depth) {
        // When Retrieve Embedded resources + Concurrent Pool is used
        // as the instance of Proxy is cloned, we end up with impl being null
        // testIterationStart will not be executed but it's not a problem for 51380 as it's download of resources
        // so SSL context is to be reused
        if (impl == null) { // Not called from multiple threads, so this is OK
            try {
                impl = HTTPSamplerFactory.getImplementation(getImplementation(), this);
            } catch (Exception ex) {
                return errorResult(ex, new HTTPSampleResult());
            }
        }
        try {
            String url = toExternalForm(u);
            if (StringUtils.isNotEmpty(url) && url.startsWith("http:/http")) {
                url = url.substring(6);
            }
            u = new URL(url);
        } catch (Exception ex) {
            LogUtil.error(ex);
        }
        return impl.sample(u, method, areFollowingRedirect, depth);
    }

    // N.B. It's not possible to forward threadStarted() to the implementation class.
    // This is because Config items are not processed until later, and HTTPDefaults may define the implementation

    @Override
    public void threadFinished() {
        if (impl != null) {
            impl.threadFinished(); // Forward to sampler
        }
    }

    @Override
    public boolean interrupt() {
        if (impl != null) {
            return impl.interrupt(); // Forward to sampler
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase#testIterationStart(org.apache.jmeter.engine.event.LoopIterationEvent)
     */
    @Override
    public void testIterationStart(LoopIterationEvent event) {
        if (impl != null) {
            impl.notifyFirstSampleAfterLoopRestart();
        }
    }
}
