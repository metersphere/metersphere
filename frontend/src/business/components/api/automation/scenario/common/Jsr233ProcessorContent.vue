<template>
    <div>
      <el-row type="flex" :gutter="10">
        <el-col :span="codeSpan" class="script-content">
          <ms-code-edit v-if="isCodeEditAlive" :mode="codeEditModeMap[jsr223ProcessorData.scriptLanguage]"
                        :read-only="isReadOnly"
                        :data.sync="jsr223ProcessorData.script" theme="eclipse" :modes="['java','python']"
                        ref="codeEdit"/>
        </el-col>
        <div style="width: 14px;margin-right: 5px;">
          <div style="height: 12px;width: 12px; line-height:12px;">
            <i :class="showMenu ? 'el-icon-remove-outline' : 'el-icon-circle-plus-outline'"
               class="show-menu"
               @click="switchMenu"></i>
          </div>
        </div>
        <el-col :span="menuSpan" class="script-index">
          <ms-dropdown :default-command="jsr223ProcessorData.scriptLanguage" :commands="languages" style="margin-bottom: 5px;margin-left: 15px;"
                       @command="languageChange"/>
          <script-nav-menu ref="scriptNavMenu" :language="jsr223ProcessorData.scriptLanguage" :menus="codeTemplates"
                           @handleCode="handleCodeTemplate"/>
        </el-col>
      </el-row>
    </div>
</template>

<script>
    import MsCodeEdit from "../../../definition/components/MsCodeEdit";
    import MsDropdown from "../../../../common/components/MsDropdown";
    import CustomFunctionRelate from "@/business/components/project/menu/function/CustomFunctionRelate";
    import ApiFuncRelevance from "@/business/components/project/menu/function/ApiFuncRelevance";
    import ScriptNavMenu from "@/business/components/project/menu/function/ScriptNavMenu";
    export default {
        name: "Jsr233ProcessorContent",
      components: {MsDropdown, MsCodeEdit, CustomFunctionRelate, ApiFuncRelevance, ScriptNavMenu},
      data() {
        return {
          jsr223ProcessorData: {},
          codeTemplates: [
            {
              title: this.$t('project.code_segment.api_test'),
              children: [
                {
                  title: this.$t('project.code_segment.import_api_test'),
                  command: "api_definition",
                },
                {
                  title: this.$t('project.code_segment.new_api_test'),
                  command: "new_api_request",
                }
              ]
            },
            {
              title: this.$t('project.code_segment.custom_value'),
              children: [
                {
                  title: this.$t('api_test.request.processor.code_template_get_variable'),
                  value: 'vars.get("variable_name")',
                },
                {
                  title: this.$t('api_test.request.processor.code_template_set_variable'),
                  value: 'vars.put("variable_name", "variable_value")',
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
              ]
            },
            {
              title: this.$t('project.code_segment.project_env'),
              children: [
                {
                  title: this.$t('api_test.request.processor.param_environment_set_global_variable'),
                  value: 'vars.put(${__metersphere_env_id}+"key","value");\n'+'vars.put("key","value")',
                },
              ]
            },
            {
              title: this.$t('project.code_segment.code_segment'),
              children: [
                {
                  title: this.$t('project.code_segment.insert_segment'),
                  command: "custom_function",
                }
              ]
            },
            {
              title: this.$t('project.code_segment.exception_handle'),
              children: [
                {
                  title: this.$t('project.code_segment.stop_test'),
                  value: 'ctx.getEngine().stopThreadNow(ctx.getThread().getThreadName())'
                },
              ]
            },
            {
              title: this.$t('project.code_segment.report_handle'),
              children: [
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
              ]
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
          },
          codeSpan: 20,
          menuSpan: 4,
          showMenu: true,
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
        switchMenu() {
          this.showMenu = !this.showMenu;
          if (this.showMenu) {
            this.codeSpan = 20;
            this.menuSpan = 4;
          } else {
            this.codeSpan = 24;
            this.menuSpan = 0;
          }
        },
        handleCodeTemplate(code) {
          if (!this.jsr223ProcessorData.script) {
            this.jsr223ProcessorData.script = code;
          } else {
            this.jsr223ProcessorData.script = this.jsr223ProcessorData.script + '\n' + code;
          }
          this.reload();
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
    min-height: 440px;
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

  .show-menu {
    text-align:center;
    font-weight: bold;
    color:#935aa1;
    font-size: 18px;
    cursor: pointer;
  }
  .show-menu:hover {
    color:#935aa1;
  }

</style>
