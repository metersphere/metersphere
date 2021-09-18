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
  let {requestHeaders = new Map(), requestBody = "", requestPath = "",
    requestMethod = "", host = "", protocol = "", requestArguments = new Map()} = requestObj;
  let requestUrl = "";
  requestPath = getRequestPath(requestArguments, requestPath);
  if (protocol && host && requestPath) {
    requestUrl = protocol + "://" + host + requestPath;
  }
  let body = JSON.stringify(requestBody);
  let headers = getHeaders(requestHeaders);
  let params = "";
  params +=  `[
                'url': '${requestUrl}',
                'method': '${requestMethod}', // POST/GET
                'headers': ${headers}, // 请求headers 例：['Content-type':'application/json']
                'data': ${body} // 参数
                ]`
  return `import groovy.json.JsonOutput
import groovy.json.JsonSlurper

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
`;
}

function pythonCode(requestObj) {
  let {requestHeaders = new Map(), requestBody = "", requestPath = "/",
    requestMethod = "", host = "", protocol = "http", requestArguments = new Map()} = requestObj;
  let connType = "HTTPConnection";
  if (protocol === 'https') {
    connType = "HTTPSConnection";
  }
  let headers = getHeaders(requestHeaders);
  requestBody = requestBody ? requestBody : "{}";
  requestPath = getRequestPath(requestArguments, requestPath);
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
  `;
}

function javaCode(requestObj) {
  return ``;
}

function jsCode(requestObj) {
  return ``;
}

function getRequestPath(requestArgs, requestPath) {
  if (requestArgs.size > 0) {
    requestPath = requestPath + "?"
    let index = 1;
    for (let [k, v] of requestArgs) {
      if (index !== 1) {
        requestPath = requestPath + "&";
      }
      requestPath = requestPath + k + "=" + v;
      index++;
    }
  }
  return requestPath;
}

function getHeaders(requestHeaders) {
  let headers = "{";
  let index = 1;
  for (let [k, v] of requestHeaders) {
    if (index !== 1) {
      headers += ",";
    }
    // 拼装
    headers += `'${k}':'${v}'`;
    index ++;
  }
  headers = headers + "}"
  return headers;
}
