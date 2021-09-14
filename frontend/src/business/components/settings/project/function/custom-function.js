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
  let {requestHeaders, requestBody, requestUrl, requestMethod} = obj;
  let headers = "", body = JSON.stringify(requestBody);
  for (let [k, v] of requestHeaders) {
    headers += `['${k}':'${v}']`;
  }
  return `
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

def http_post() {
    def headers = ${headers} // 请求headers 例：['Content-type':'application/json']
    // json数据
    def data = ${body}
    def conn = new URL("${requestUrl}").openConnection()
    conn.setRequestMethod("${requestMethod}")
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
