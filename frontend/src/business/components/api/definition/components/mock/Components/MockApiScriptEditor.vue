<template>
  <div>
    <el-row type="flex">
      <el-col :span="codeSpan" class="script-content">
        <ms-code-edit v-if="isCodeEditAlive" :mode="codeEditModeMap[jsr223Processor.scriptLanguage]"
                      :read-only="isReadOnly"
                      height="90%"
                      :data.sync="jsr223Processor.script" theme="eclipse" :modes="['java','python']"
                      ref="codeEdit"/>
      </el-col>
      <div style="width: 14px;margin-right: 5px;">
        <div style="height: 12px;width: 12px; line-height:12px;">
          <i :class="showMenu ? 'el-icon-remove-outline' : 'el-icon-circle-plus-outline'"
             class="show-menu"
             @click="switchMenu"></i>
        </div>
      </div>
      <el-col :span="menuSpan" style="width: 200px" class="script-index">
        <ms-dropdown :default-command.sync="jsr223Processor.scriptLanguage" :commands="languages"
                     style="margin-bottom: 5px;margin-left: 15px;"
                     @command="languageChange"/>
        <mock-script-nav-menu ref="scriptNavMenu" style="width: 90%" :language="jsr223Processor.scriptLanguage"
                              :menus="baseCodeTemplates"
                              @handleCode="handleCodeTemplate"/>
      </el-col>
    </el-row>
  </div>
</template>

<script>

import MsCodeEdit from "@/business/components/api/definition/components/MsCodeEdit";
import MsDropdown from "@/business/components/common/components/MsDropdown";
import CustomFunctionRelate from "@/business/components/project/menu/function/CustomFunctionRelate";
import ApiFuncRelevance from "@/business/components/project/menu/function/ApiFuncRelevance";
import MockScriptNavMenu from "@/business/components/api/definition/components/mock/Components/MockScriptNavMenu";
import i18n from "@/i18n/i18n";

export default {
  name: "MockApiScriptEditor",
  components: {MsDropdown, MsCodeEdit, CustomFunctionRelate, ApiFuncRelevance, MockScriptNavMenu},
  data() {
    return {
      baseCodeTemplates: [],
      tcpCodeTemplates: [
        {
          title: this.$t('project.code_segment.code_segment'),
          children: [
            {
              title: this.$t('project.code_segment.insert_segment'),
              command: "custom_function",
            }
          ]
        },
      ],
      isCodeEditAlive: true,
      languages: [
        'beanshell', "python"
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
    if(this.jsr223Processor){
      if (!this.jsr223Processor.scriptLanguage) {
        this.jsr223Processor.scriptLanguage = "beanshell";
      }
    }
    if (this.showApi) {
      this.baseCodeTemplates = this.httpCodeTemplates;
    } else {
      this.baseCodeTemplates = this.tcpCodeTemplates;
    }
  },

  computed: {
    httpCodeTemplates() {
      let returnData = [
        {
          title: "API" + this.$t('api_test.definition.document.request_info'),
          children: [
            {
              title: this.$t('api_test.request.address'),
              value: this.getScript("address"),
            },
            {
              title: "Header " + this.$t('api_test.definition.document.request_param'),
              value: this.getScript("header"),
            },
            {
              title: this.$t('api_test.request.body') + this.$t('api_test.variable'),
              value: this.getScript("body"),
            },
            {
              title: this.$t('api_test.request.body') + this.$t('api_test.variable') + " (Raw)",
              value: this.getScript("bodyRaw"),
            },
            {
              title: "Query " + this.$t('api_test.definition.document.request_param'),
              value: this.getScript("query"),
            },
            {
              title: "Rest " + this.$t('api_test.definition.document.request_param'),
              value: this.getScript("rest"),
            },

          ]
        },
        {
          title: i18n.t('project.code_segment.custom_value'),
          children: [
            {
              title: i18n.t('api_test.request.processor.code_template_get_variable'),
              value: 'vars.get("variable_name");',
            },
            {
              title: i18n.t('api_test.request.processor.code_template_set_variable'),
              value: 'vars.put("variable_name", "variable_value");',
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
      ];
      return returnData;
    }
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
    showApi: {
      type: Boolean,
      default: true,
    },
    node: {},
  },
  watch: {
    jsr223Processor() {
      this.reload();
    },
    'jsr223Processor.scriptLanguage'() {
      if (this.showApi) {
        this.baseCodeTemplates = this.httpCodeTemplates;
      } else {
        this.baseCodeTemplates = this.tcpCodeTemplates;
      }
    }
  }
  ,
  methods: {
    getScript(type) {
      let returnScript = "";
      let laguanges = "beanshell";
      if (this.jsr223Processor) {
        laguanges = this.jsr223Processor.scriptLanguage
      }
      switch (type) {
        case "address":
          if (laguanges === "python") {
            returnScript = 'param=vars["address"]';
          } else {
            returnScript = 'var param=vars.get("address")';
          }
          break;
        case "header":
          if (laguanges === "python") {
            returnScript = 'param=vars["header.${param}"]';
          } else {
            returnScript = 'var param=vars.get("header.${param}")';
          }
          break;
        case "body":
          if (laguanges === "python") {
            returnScript = 'param=vars["body.${param}"]';
          } else {
            returnScript = 'var param=vars.get("body.${param}");\n' +
              '//如果对象是多层JSON，需要引入fastjson协助解析:\n' +
              '// 以"{\"name\":\"user\",\"rows\":[{\"type\":1}]}" 为demo,取rows第1个的type数据:\n' +
              'import com.alibaba.fastjson.JSON;\n' +
              'import com.alibaba.fastjson.JSONArray;\n' +
              'import com.alibaba.fastjson.JSONObject;\n' +
              '\n' +
              'var jsonParam = vars.get("body.json");\n' +
              'JSONObject jsonObject = JSONObject.parseObject(jsonParam);\n' +
              'var value = jsonObject.getJSONArray("rows").getJSONObject(0).getString("type");\n' +
              'vars.put("key1","value");\n';
          }
          break;
        case "bodyRaw":
          if (laguanges === "python") {
            returnScript = 'param=vars["bodyRaw"]';
          } else {
            returnScript = 'var param=vars.get("bodyRaw")';
          }
          break;
        case "query":
          if (laguanges === "python") {
            returnScript = 'param=vars["query.${param}"]';
          } else {
            returnScript = 'var param=vars.get("query.${param}")';
          }
          break;
        case "rest":
          if (laguanges === "python") {
            returnScript = 'param=vars["rest.${param}"]';
          } else {
            returnScript = 'var param=vars.get("rest.${param}")';
          }
          break;
        default:
          break;
      }

      return returnScript;
    },
    addTemplate(template) {
      if (!this.jsr223Processor.script) {
        this.jsr223Processor.script = "";
      }
      this.jsr223Processor.script += template.value;
      if (this.jsr223Processor.scriptLanguage === 'beanshell') {
        this.jsr223Processor.script += ';';
      }
      this.reload();
    },
    reload() {
      this.isCodeEditAlive = false;
      this.$nextTick(() => (this.isCodeEditAlive = true));
    },
    languageChange(language) {
      this.jsr223Processor.scriptLanguage = language;
      this.$emit("languageChange");
    },
    addCustomFuncScript(script) {
      this.jsr223Processor.script = this.jsr223Processor.script ?
        this.jsr223Processor.script + '\n\n' + script : script;
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
      if (!this.jsr223Processor.script) {
        this.jsr223Processor.script = code;
      } else {
        this.jsr223Processor.script = this.jsr223Processor.script + '\n' + code;
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
  /*height: calc(100vh - 570px);*/
  height: 185px;
  min-height: 170px;
}

.script-index {
  padding: 0 20px;
  width: 230px;
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
  text-align: center;
  font-weight: bold;
  color: #935aa1;
  font-size: 18px;
  cursor: pointer;
}

.show-menu:hover {
  color: #935aa1;
}

</style>
