package io.metersphere.proxy;

import io.metersphere.commons.utils.LogUtil;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.AbortableHttpRequest;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.HeaderGroup;
import org.apache.http.util.EntityUtils;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

/**
 * Http代理
 * 获取 zalenium 录像等资源
 */
public class ProxyServlet extends HttpServlet {

  public static final String PRESERVEHOST = "preserveHost";

  public static final String PRESERVECOOKIES = "preserveCookies";

  public static final String HANDLEREDIRECTS = "http.protocol.handle-redirects";

  public static final String CONNECTTIMEOUT = "http.socket.timeout";

  public static final String READTIMEOUT = "http.read.timeout";
  
  public static final String USESYSTEMPROPERTIES = "useSystemProperties";

  public static final String FORWARDEDFOR = "forwardip";

  protected boolean doPreserveHost = false;
  protected boolean doForwardIP = true;
  protected boolean doPreserveCookies = false;
  protected boolean doHandleRedirects = false;
  protected boolean useSystemProperties = false;
  protected int connectTimeout = -1;
  protected int readTimeout = -1;


  protected String targetUri;
  protected URI targetUriObj;
  protected HttpHost targetHost;

  private HttpClient proxyClient;


  protected String getConfigParam(String key) {
    return getServletConfig().getInitParameter(key);
  }

  @Override
  public void init() {

    String doForwardIPString = getConfigParam(FORWARDEDFOR);
    if (doForwardIPString != null) {
      this.doForwardIP = Boolean.parseBoolean(doForwardIPString);
    }

    String preserveHostString = getConfigParam(PRESERVEHOST);
    if (preserveHostString != null) {
      this.doPreserveHost = Boolean.parseBoolean(preserveHostString);
    }

    String preserveCookiesString = getConfigParam(PRESERVECOOKIES);
    if (preserveCookiesString != null) {
      this.doPreserveCookies = Boolean.parseBoolean(preserveCookiesString);
    }

    String handleRedirectsString = getConfigParam(HANDLEREDIRECTS);
    if (handleRedirectsString != null) {
      this.doHandleRedirects = Boolean.parseBoolean(handleRedirectsString);
    }

    String connectTimeoutString = getConfigParam(CONNECTTIMEOUT);
    if (connectTimeoutString != null) {
      this.connectTimeout = Integer.parseInt(connectTimeoutString);
    }
    
    String readTimeoutString = getConfigParam(READTIMEOUT);
    if (readTimeoutString != null) {
      this.readTimeout = Integer.parseInt(readTimeoutString);
    }

    String useSystemPropertiesString = getConfigParam(USESYSTEMPROPERTIES);
    if (useSystemPropertiesString != null) {
      this.useSystemProperties = Boolean.parseBoolean(useSystemPropertiesString);
    }
    proxyClient = createHttpClient(buildRequestConfig());
  }

  protected RequestConfig buildRequestConfig() {
    return RequestConfig.custom()
            .setRedirectsEnabled(doHandleRedirects)
            .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
            .setConnectTimeout(connectTimeout)
            .setSocketTimeout(readTimeout)
            .build();
  }

  protected HttpClient createHttpClient(final RequestConfig requestConfig) {
    HttpClientBuilder clientBuilder = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig);
    if (useSystemProperties){
        clientBuilder = clientBuilder.useSystemProperties();
    }
    return clientBuilder.build();
  }


  @Override
  public void destroy() {
    if (proxyClient instanceof Closeable) {
      try {
        ((Closeable) proxyClient).close();
      } catch (IOException e) {
        LogUtil.error(e.getMessage(),e);
      }
    } else {
      if (proxyClient != null){
          proxyClient.getConnectionManager().shutdown();
      }
    }
    super.destroy();
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse servletResponse)
      throws ServletException, IOException {

    String method = request.getMethod();

    String targetUri = getTargetUri(request);

    HttpRequest targetRequest;
    if (request.getHeader(HttpHeaders.CONTENT_LENGTH) != null ||
            request.getHeader(HttpHeaders.TRANSFER_ENCODING) != null) {
        targetRequest = getTargetRequestWithEntity(method, targetUri, request);
    } else {
        targetRequest = new BasicHttpRequest(method, targetUri);
    }

    copyRequestHeaders(request, targetRequest);

    setXForwardedForHeader(request, targetRequest);

    HttpResponse proxyResponse = null;
    try {
      proxyResponse = proxyClient.execute(targetHost, targetRequest);;
      int statusCode = proxyResponse.getStatusLine().getStatusCode();
      servletResponse.setStatus(statusCode, proxyResponse.getStatusLine().getReasonPhrase());
      copyResponseHeaders(proxyResponse, request, servletResponse);

      if (statusCode == HttpServletResponse.SC_NOT_MODIFIED) {
        servletResponse.setIntHeader(HttpHeaders.CONTENT_LENGTH, 0);
      } else {
        copyResponseEntity(proxyResponse, servletResponse);
      }

    } catch (Exception e) {
      handleRequestException(targetRequest, e);
    } finally {
      if (proxyResponse != null){
          EntityUtils.consumeQuietly(proxyResponse.getEntity());
      }
    }
  }

  protected void handleRequestException(HttpRequest proxyRequest, Exception e) {
    if (proxyRequest instanceof AbortableHttpRequest) {
      AbortableHttpRequest abortableHttpRequest = (AbortableHttpRequest) proxyRequest;
      abortableHttpRequest.abort();
    }
    throw new RuntimeException(e.getMessage(), e);
  }

  protected HttpRequest getTargetRequestWithEntity(String method, String zaleniumUri,
                                                HttpServletRequest servletRequest) throws IOException {
    HttpEntityEnclosingRequest targetRequest = new BasicHttpEntityEnclosingRequest(method, zaleniumUri);
    targetRequest.setEntity(new InputStreamEntity(servletRequest.getInputStream(),
            getContentLength(servletRequest)));
    return targetRequest;
  }

  private long getContentLength(HttpServletRequest request) {
    String contentLengthHeader = request.getHeader("Content-Length");
    if (contentLengthHeader != null) {
      return Long.parseLong(contentLengthHeader);
    }
    return -1L;
  }


  protected static final HeaderGroup hopByHopHeaders;
  static {
    hopByHopHeaders = new HeaderGroup();
    String[] headers = new String[] {
        "Connection", "Keep-Alive", "Proxy-Authenticate", "Proxy-Authorization",
        "TE", "Trailers", "Transfer-Encoding", "Upgrade" };
    for (String header : headers) {
      hopByHopHeaders.addHeader(new BasicHeader(header, null));
    }
  }


  protected void copyRequestHeaders(HttpServletRequest servletRequest, HttpRequest proxyRequest) {
    Enumeration<String> enumerationOfHeaderNames = servletRequest.getHeaderNames();
    while (enumerationOfHeaderNames.hasMoreElements()) {
      String headerName = enumerationOfHeaderNames.nextElement();
      copyRequestHeader(servletRequest, proxyRequest, headerName);
    }
  }

  protected void copyRequestHeader(HttpServletRequest servletRequest, HttpRequest proxyRequest,
                                   String headerName) {
    if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)){
        return;
    }
    if (hopByHopHeaders.containsHeader(headerName)){
        return;
    }

    Enumeration<String> headers = servletRequest.getHeaders(headerName);
    while (headers.hasMoreElements()) {
      String headerValue = headers.nextElement();
      if (!doPreserveHost && headerName.equalsIgnoreCase(HttpHeaders.HOST)) {
        HttpHost host = targetHost;
        headerValue = host.getHostName();
        if (host.getPort() != -1){
            headerValue += ":"+host.getPort();
        }
      } else if (!doPreserveCookies && headerName.equalsIgnoreCase(org.apache.http.cookie.SM.COOKIE)) {
        headerValue = getRealCookie(headerValue);
      }
      proxyRequest.addHeader(headerName, headerValue);
    }
  }

  private void setXForwardedForHeader(HttpServletRequest servletRequest,
                                      HttpRequest proxyRequest) {
    if (doForwardIP) {
      String forHeaderName = "X-Forwarded-For";
      String forHeader = servletRequest.getRemoteAddr();
      String existingForHeader = servletRequest.getHeader(forHeaderName);
      if (existingForHeader != null) {
        forHeader = existingForHeader + ", " + forHeader;
      }
      proxyRequest.setHeader(forHeaderName, forHeader);

      String protoHeaderName = "X-Forwarded-Proto";
      String protoHeader = servletRequest.getScheme();
      proxyRequest.setHeader(protoHeaderName, protoHeader);
    }
  }

  protected void copyResponseHeaders(HttpResponse proxyResponse, HttpServletRequest servletRequest,
                                     HttpServletResponse servletResponse) {
    for (Header header : proxyResponse.getAllHeaders()) {
      copyResponseHeader(servletRequest, servletResponse, header);
    }
  }

  protected void copyResponseHeader(HttpServletRequest servletRequest,
                                  HttpServletResponse servletResponse, Header header) {
    String headerName = header.getName();
    if (hopByHopHeaders.containsHeader(headerName)){
        return;
    }
    String headerValue = header.getValue();
    if (headerName.equalsIgnoreCase(org.apache.http.cookie.SM.SET_COOKIE) ||
            headerName.equalsIgnoreCase(org.apache.http.cookie.SM.SET_COOKIE2)) {
      copyProxyCookie(servletRequest, servletResponse, headerValue);
    } else if (headerName.equalsIgnoreCase(HttpHeaders.LOCATION)) {
      servletResponse.addHeader(headerName, rewriteUrlFromResponse(servletRequest, headerValue));
    } else {
      servletResponse.addHeader(headerName, headerValue);
    }
  }

  protected void copyProxyCookie(HttpServletRequest servletRequest,
                                 HttpServletResponse servletResponse, String headerValue) {
    String path = servletRequest.getContextPath();
    path += servletRequest.getServletPath();
    if(path.isEmpty()){
      path = "/";
    }

    for (HttpCookie cookie : HttpCookie.parse(headerValue)) {
      String proxyCookieName = doPreserveCookies ? cookie.getName() : getCookieNamePrefix(cookie.getName()) + cookie.getName();
      Cookie servletCookie = new Cookie(proxyCookieName, cookie.getValue());
      servletCookie.setComment(cookie.getComment());
      servletCookie.setMaxAge((int) cookie.getMaxAge());
      servletCookie.setPath(path);
      servletCookie.setSecure(cookie.getSecure());
      servletCookie.setVersion(cookie.getVersion());
      servletResponse.addCookie(servletCookie);
    }
  }

  protected String getRealCookie(String cookieValue) {
    StringBuilder escapedCookie = new StringBuilder();
    String cookies[] = cookieValue.split("[;,]");
    for (String cookie : cookies) {
      String cookieSplit[] = cookie.split("=");
      if (cookieSplit.length == 2) {
        String cookieName = cookieSplit[0].trim();
        if (cookieName.startsWith(getCookieNamePrefix(cookieName))) {
          cookieName = cookieName.substring(getCookieNamePrefix(cookieName).length());
          if (escapedCookie.length() > 0) {
            escapedCookie.append("; ");
          }
          escapedCookie.append(cookieName).append("=").append(cookieSplit[1].trim());
        }
      }
    }
    return escapedCookie.toString();
  }

  protected String getCookieNamePrefix(String name) {
    return "!Proxy!" + getServletConfig().getServletName();
  }

  protected void copyResponseEntity(HttpResponse proxyResponse, HttpServletResponse servletResponse)
          throws IOException {
    HttpEntity entity = proxyResponse.getEntity();
    if (entity != null) {
      OutputStream servletOutputStream = servletResponse.getOutputStream();
      entity.writeTo(servletOutputStream);
    }
  }

  protected String getTargetUri(HttpServletRequest servletRequest) {
    String pathInfo = servletRequest.getPathInfo();
    targetUri =  pathInfo.split("/")[1];
    String host = targetUri.split(":")[0];
    String port = targetUri.split(":")[1];
      try {
          targetUriObj = new URI(targetUri);
      } catch (URISyntaxException e) {
          e.printStackTrace();
          throw new RuntimeException(e.getMessage(),e);
      }
      targetHost = new HttpHost(host, Integer.valueOf(port));
    return "http:/" + pathInfo;
  }


  protected String rewriteUrlFromResponse(HttpServletRequest servletRequest, String theUrl) {
    if (theUrl.startsWith(targetUri)) {
      StringBuffer curUrl = servletRequest.getRequestURL();
      int pos;
      if ((pos = curUrl.indexOf("://"))>=0) {
        if ((pos = curUrl.indexOf("/", pos + 3)) >=0) {
          curUrl.setLength(pos);
        }
      }
      curUrl.append(servletRequest.getContextPath());
      curUrl.append(servletRequest.getServletPath());
      curUrl.append(theUrl, targetUri.length(), theUrl.length());
      return curUrl.toString();
    }
    return theUrl;
  }

}