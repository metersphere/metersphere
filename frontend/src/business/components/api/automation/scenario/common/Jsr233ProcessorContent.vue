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
          <el-link href="https://jmeter.apache.org/usermanual/component_reference.html#BeanShell_PostProcessor"
                   target="componentReferenceDoc" style="margin-top: 10px"
                   type="primary">{{ $t('commons.reference_documentation') }}
          </el-link>
        </el-col>
      </el-row>
    </div>
</template>

<script>
    import MsCodeEdit from "../../../definition/components/MsCodeEdit";
    import MsDropdown from "../../../../common/components/MsDropdown";
    export default {
        name: "Jsr233ProcessorContent",
      components: {MsDropdown, MsCodeEdit},
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
