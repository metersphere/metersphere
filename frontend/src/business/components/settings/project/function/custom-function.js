export const FUNC_TEMPLATE = {
  beanshell: "public void test() {\n\n\n}",
  groovy: "def test() {\n\n\n}",
  python: "def test():\n",
  nashornScript: "function test() {\n\n\n}",
  rhinoScript: "function test() {\n\n\n}"
}

export function getCodeTemplate(language, requestObj) {
  // 拼装代码模版的请求参数
  if (language === "groovy") {
    return groovyCode(requestObj);
  } else {
    return "";
  }
}

function groovyCode(obj) {
  let {requestHeaders = new Map(), requestBody = "", requestUrl = "", requestMethod = ""} = obj;
  let headers = "", body = JSON.stringify(requestBody);
  for (let [k, v] of requestHeaders) {
    // 拼装映射
    headers += `['${k}':'${v}']`;
  }
  headers = headers === "" ? "[]" : headers;
  let params = "";
  params +=  `[
                'url': '${requestUrl}',
                'method': '${requestMethod}',
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
