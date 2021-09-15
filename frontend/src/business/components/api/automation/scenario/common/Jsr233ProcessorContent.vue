<template>
    <div>
      <el-row>
        <el-col :span="20" class="script-content">
          <ms-code-edit v-if="isCodeEditAlive" :mode="codeEditModeMap[jsr223ProcessorData.scriptLanguage]"
                        :read-only="isReadOnly"
                        :data.sync="jsr223ProcessorData.script" theme="eclipse" :modes="['java','python']"
                        ref="codeEdit"/>
        </el-col>
        <el-col :span="4" class="script-index">
          <ms-dropdown :default-command="jsr223ProcessorData.scriptLanguage" :commands="languages"
                       @command="languageChange"/>
          <div class="template-title">{{ $t('api_test.request.processor.code_template') }}</div>
          <div v-for="(template, index) in codeTemplates" :key="index" class="code-template">
            <el-link :disabled="template.disabled" @click="addTemplate(template)">{{ template.title }}</el-link>
          </div>
          <div v-for="funcLink in funcLinks" :key="funcLink.index" class="code-template">
            <el-link :disabled="funcLink.disabled" @click="doFuncLink(funcLink)">{{ funcLink.title }}</el-link>
          </div>
          <el-link href="https://jmeter.apache.org/usermanual/component_reference.html#BeanShell_PostProcessor"
                   target="componentReferenceDoc" style="margin-top: 10px"
                   type="primary">{{ $t('commons.reference_documentation') }}
          </el-link>

          <custom-function-relate ref="customFunctionRelate" @addCustomFuncScript="addCustomFuncScript"/>
          <!--接口列表-->
          <api-func-relevance @save="apiSave" @close="apiClose" ref="apiFuncRelevance"/>
        </el-col>
      </el-row>
    </div>
</template>

<script>
    import MsCodeEdit from "../../../definition/components/MsCodeEdit";
    import MsDropdown from "../../../../common/components/MsDropdown";
    import CustomFunctionRelate from "@/business/components/settings/project/function/CustomFunctionRelate";
    import {getCodeTemplate} from "@/business/components/settings/project/function/custom-function";
    import ApiFuncRelevance from "@/business/components/settings/project/function/ApiFuncRelevance";
    export default {
        name: "Jsr233ProcessorContent",
      components: {MsDropdown, MsCodeEdit, CustomFunctionRelate, ApiFuncRelevance},
      data() {
        return {
          jsr223ProcessorData: {},
          codeTemplates: [
            {
              title: this.$t('api_test.request.processor.code_template_get_variable'),
              value: 'vars.get("variable_name")',
            },
            {
              title: this.$t('api_test.request.processor.code_template_set_variable'),
              value: 'vars.put("variable_name", "variable_value")',
            },
            {
              title: this.$t('api_test.request.processor.param_environment_set_global_variable'),
              value: 'vars.put(${__metersphere_env_id}+"key","value");\n'+'vars.put("key","value")',
            },
            {
              title: this.$t('api_test.request.processor.code_add_report_length'),
              value: 'String report = ctx.getCurrentSampler().getRequestData();\n' +
                'if(report!=null){\n' +
                '    //补足8位长度，前置补0\n' +
                '    String reportlengthStr = String.format("%08d",report.length());\n' +
                '    report = reportlengthStr+report;\n' +
                '    ctx.getCurrentSampler().setRequestData(report);\n' +
                '}',
            },
            {
              title: this.$t('api_test.request.processor.code_template_get_response_header'),
              value: 'prev.getResponseHeaders()',
              disabled: this.isPreProcessor
            },
            {
              title: this.$t('api_test.request.processor.code_template_get_response_code'),
              value: 'prev.getResponseCode()',
              disabled: this.isPreProcessor
            },
            {
              title: this.$t('api_test.request.processor.code_template_get_response_result'),
              value: 'prev.getResponseDataAsString()',
              disabled: this.isPreProcessor
            },
            {
              title: this.$t('api_test.request.processor.code_hide_report_length'),
              value: '//Get response data\n' +
                'String returnData = prev.getResponseDataAsString();\n' +
                'if(returnData!=null&&returnData.length()>8){\n' +
                '//remove 8 report length \n' +
                '    String subStringData = returnData.substring(8,returnData.length());\n' +
                '    if(subStringData.startsWith("<")){\n' +
                '        returnData = subStringData;\n' +
                '        prev.setResponseData(returnData);\n' +
                '    }\n' +
                '}',
              disabled: this.isPreProcessor
            },
            {
              title: "终止测试",
              value: 'ctx.getEngine().stopThreadNow(ctx.getThread().getThreadName())'
            }
          ],
          funcLinks: [
            {
              title: "插入自定义函数",
              command: "custom_function",
              index: "custom_function"
            },
            {
              title: "从API定义导入",
              command: "api_definition",
              index: "api_definition"
            },
            {
              title: "新API测试[JSON]",
              command: "new_api_request",
              index: "new_api_request"
            }
          ],
          isCodeEditAlive: true,
          languages: [
            'beanshell', "python", "groovy", "nashornScript", "rhinoScript"
          ],
          codeEditModeMap: {
            beanshell: 'java',
            python: 'python',
            groovy: 'java',
            nashornScript: 'javascript',
            rhinoScript: 'javascript',
          }
        }
      },
      created() {
        this.jsr223ProcessorData = this.jsr223Processor;
      },
      props: {
        isReadOnly: {
          type: Boolean,
          default:
            false
        },
        jsr223Processor: {
          type: Object,
        },
        isPreProcessor: {
          type: Boolean,
          default:
            false
        },
        node: {},
      },
      watch: {
        jsr223Processor() {
          this.reload();
        }
      }
      ,
      methods: {
        addTemplate(template) {
          if (!this.jsr223ProcessorData.script) {
            this.jsr223ProcessorData.script = "";
          }
          this.jsr223ProcessorData.script += template.value;
          if (this.jsr223ProcessorData.scriptLanguage === 'beanshell') {
            this.jsr223ProcessorData.script += ';';
          }
          this.reload();
        },
        reload() {
          this.isCodeEditAlive = false;
          this.$nextTick(() => (this.isCodeEditAlive = true));
        },
        languageChange(language) {
          this.jsr223ProcessorData.scriptLanguage = language;
          this.$emit("languageChange");
        },
        addCustomFuncScript(script) {
          this.jsr223ProcessorData.script = this.jsr223ProcessorData.script ?
            this.jsr223ProcessorData.script + '\n\n' + script : script;
          this.reload();
        },
        doFuncLink(funcLink) {
          if (funcLink.command === 'custom_function') {
            this.$refs.customFunctionRelate.open(this.jsr223ProcessorData.scriptLanguage);
          } else if (funcLink.command === 'api_definition') {
            this.$refs.apiFuncRelevance.open();
          } else if (funcLink.command === 'new_api_request') {
            // requestObj为空则生产默认模版
            let headers = new Map();
            headers.set('Content-type', 'application/json');
            let code = getCodeTemplate(this.jsr223ProcessorData.scriptLanguage, {requestHeaders: headers});
            let codeStr = this.jsr223ProcessorData.script + "\n\n" + code;
            this.jsr223ProcessorData.script = this.jsr223ProcessorData.script ? codeStr : code;
            this.reload();
          }
        },
        apiSave(data, env) {
          // data：选中的多个接口定义或多个接口用例; env: 关联页面选中的环境
          let condition = env.config.httpConfig.conditions || [];
          let requestUrl = "";
          if (condition && condition.length > 0) {
            // 如果有多个环境，取第一个
            let protocol = condition[0].protocol ? condition[0].protocol : "http";
            requestUrl = protocol + "://" + condition[0].socket;
          }
          // todo
          if (data.length > 5) {
            this.$warning("最多可以选择5个接口！");
            return;
          }
          let code = "";
          if (data.length > 0) {
            data.forEach(dt => {
              let param = this.parseRequestObj(dt, requestUrl);
              code += '\n' + getCodeTemplate(this.jsr223ProcessorData.scriptLanguage, param);
            })
          }
          if (code) {
            let codeStr = this.jsr223ProcessorData.script + code;
            this.jsr223ProcessorData.script = this.jsr223ProcessorData.script ? codeStr : code;
            this.reload();
          } else {
            //todo
            this.$warning("无对应语言模版");
          }
          this.$refs.apiFuncRelevance.close();
        },
        parseRequestObj(data, requestUrl) {
          let requestHeaders = new Map();
          let requestMethod = "";
          let requestBody = "";
          let request = JSON.parse(data.request);
          // 拼接发送请求需要的参数
          requestUrl = requestUrl + request.path;
          requestMethod = request.method;
          let headers = request.headers;
          if (headers && headers.length > 0) {
            headers.forEach(header => {
              if (header.name) {
                requestHeaders.set(header.name, header.value);
              }
            })
          }
          let body = request.body;
          if (body.json) {
            requestBody = body.raw;
          }
          return {requestUrl, requestHeaders, requestMethod, requestBody}
        },
        apiClose() {

        },
      }
    }
</script>

<style scoped>
  .ace_editor {
    border-radius: 5px;
  }

  .script-content {
    height: calc(100vh - 570px);
    min-height: 300px;
  }

  .script-index {
    padding: 0 20px;
  }

  .template-title {
    margin-bottom: 5px;
    font-weight: bold;
    font-size: 15px;
  }

  .document-url {
    margin-top: 10px;
  }

  .instructions-icon {
    margin-left: 5px;
  }

  .ms-dropdown {
    margin-bottom: 20px;
  }

</style>
