export const FUNC_TEMPLATE = {
  beanshell: "",
  groovy: "",
  python: "",
  nashornScript: "",
  rhinoScript: ""
}

export function getCodeTemplate(language, requestObj) {
  switch (language) {
    case "groovy":
      return groovyCode(requestObj);
    case "python":
      return pythonCode(requestObj);
    case "beanshell":
      return javaCode(requestObj);
    case "nashornScript":
      return jsCode(requestObj);
    case "rhinoScript":
      return jsCode(requestObj);
    default:
      return "";
  }
}

function groovyCode(requestObj) {
  let {requestHeaders = new Map(), requestBody = "", requestPath = "", requestMethod = "", host = "", protocol = ""} = requestObj;
  let requestUrl = "";
  if (protocol && host && requestPath) {
    requestUrl = protocol + "://" + host + requestPath;
  }
  let headers = "", body = JSON.stringify(requestBody);
  for (let [k, v] of requestHeaders) {
    // 拼装映射
    headers += `['${k}':'${v}']`;
  }
  headers = headers === "" ? "[]" : headers;
  let params = "";
  params +=  `[
                'url': '${requestUrl}',
                'method': '${requestMethod}', // POST/GET
                'headers': ${headers}, // 请求headers 例：['Content-type':'application/json']
                'data': ${body} // 参数
                ]`
  return `import groovy.json.JsonOutput
import groovy.json.JsonSlurper

def http_post() {
  def params = ${params}

  def headers = params['headers']
  // json数据
  def data = params['data']
  def conn = new URL(params['url']).openConnection()
  conn.setRequestMethod(params['method'])
  if (data) {
      headers.each {
        k,v -> conn.setRequestProperty(k, v);
      }
      // 输出请求参数
      log.info(data)
      conn.doOutput = true
      def writer = new OutputStreamWriter(conn.outputStream)
      writer.write(data)
      writer.flush()
      writer.close()
  }
  log.info(conn.content.text)
}
http_post()
`;
}

function pythonCode(requestObj) {
  let {requestHeaders = new Map(), requestBody = "", requestPath = "", requestMethod = "", host = "", protocol = "http"} = requestObj;
  let connType = "HTTPConnection";
  if (protocol === 'https') {
    connType = "HTTPSConnection";
  }
  let headers = "";
  for (let [k, v] of requestHeaders) {
    // 拼装
    headers += `{'${k}':'${v}'}`;
  }
  headers = headers === "" ? "{}" : headers;
  requestBody = requestBody ? requestBody : "{}";
  return `import httplib
params = ${requestBody} #例 {'username':'test'}
headers = ${headers} #例 {'Content-Type':'application/json'}
host = '${host}'
path = '${requestPath}'
method = '${requestMethod}' # POST/GET

conn = httplib.${connType}(host)
conn.request(method, path, params, headers)
res = conn.getresponse()
data = unicode(res.read(), 'utf-8')
log.info(data)
  `
}

function javaCode(requestObj) {
  return ``;
}

function jsCode(requestObj) {
  return ``;
}
