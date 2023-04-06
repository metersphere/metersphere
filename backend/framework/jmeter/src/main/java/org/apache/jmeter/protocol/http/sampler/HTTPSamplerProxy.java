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
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.Interruptible;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Proxy class that dispatches to the appropriate HTTP sampler.
 * <p>
 * This class is stored in the test plan, and holds all the configuration settings.
 * The actual implementation is created at run-time, and is passed a reference to this class
 * so it can get access to all the settings stored by HTTPSamplerProxy.
 */
public final class HTTPSamplerProxy extends HTTPSamplerBase implements Interruptible {

    private static final long serialVersionUID = 1L;
    private static final String HTTP_PREFIX = HTTPConstants.PROTOCOL_HTTP+"://"; // $NON-NLS-1$
    private static final String HTTPS_PREFIX = HTTPConstants.PROTOCOL_HTTPS+"://"; // $NON-NLS-1$

    private static final String QRY_PFX = "?"; // $NON-NLS-1$

    private static final String QRY_SEP = "&"; // $NON-NLS-1$

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

    /**
     * 重写getUrl方法， 防止获取的domain是完整的地址
     * 重新切割domain，重新赋值给domain和protocol
     * @return
     * @throws MalformedURLException
     */
    @Override
    public URL getUrl() throws MalformedURLException {
        String path = this.getPath();
        if (path.startsWith("//")) {
            path.replaceFirst("/" ,"");
        }
        // Hack to allow entire URL to be provided in host field
        if (path.startsWith(HTTP_PREFIX)
                || path.startsWith(HTTPS_PREFIX)) {
            return new URL(path);
        }
        String domain = getDomain();
        String protocol = null;
        if (domain.startsWith(HTTP_PREFIX) || domain.startsWith(HTTPS_PREFIX)) {
            try {
                URL url = new URL(domain);
                domain = url.getHost();
                protocol = url.getProtocol();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        protocol = StringUtils.isNotEmpty(protocol) ? protocol : getProtocol();
        String method = getMethod();
        StringBuilder pathAndQuery = new StringBuilder(100);
        if (PROTOCOL_FILE.equalsIgnoreCase(protocol)) {
            domain = null; // allow use of relative file URLs
        } else {
            // HTTP URLs must be absolute, allow file to be relative
            if (!path.startsWith("/")) { // $NON-NLS-1$
                pathAndQuery.append('/'); // $NON-NLS-1$
            }
        }
        pathAndQuery.append(path);

        // Add the query string if it is a HTTP GET or DELETE request
        if (HTTPConstants.GET.equals(method)
                || HTTPConstants.DELETE.equals(method)
                || HTTPConstants.OPTIONS.equals(method)) {
            // Get the query string encoded in specified encoding
            // If no encoding is specified by user, we will get it
            // encoded in UTF-8, which is what the HTTP spec says
            String queryString = getQueryString(getContentEncoding());
            if (queryString.length() > 0) {
                if (path.contains(QRY_PFX)) {// Already contains a prefix
                    pathAndQuery.append(QRY_SEP);
                } else {
                    pathAndQuery.append(QRY_PFX);
                }
                pathAndQuery.append(queryString);
            }
        }
        // If default port for protocol is used, we do not include port in URL
        if (isProtocolDefaultPort()) {
            return new URL(protocol, domain, pathAndQuery.toString());
        }
        return new URL(protocol, domain, getPort(), pathAndQuery.toString());

    }
}
