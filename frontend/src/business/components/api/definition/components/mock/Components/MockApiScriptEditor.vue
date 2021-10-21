<template>
  <div>
    <el-row type="flex" :gutter="10">
      <el-col :span="codeSpan" class="script-content">
        <ms-code-edit v-if="isCodeEditAlive" :mode="codeEditModeMap[jsr223ProcessorData.scriptLanguage]"
                      :read-only="isReadOnly"
                      height="90%"
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
        <mock-script-nav-menu ref="scriptNavMenu" :language="jsr223ProcessorData.scriptLanguage" :menus="baseCodeTemplates"
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

export default {
  name: "MockApiScriptEditor",
  components: {MsDropdown, MsCodeEdit, CustomFunctionRelate, ApiFuncRelevance, MockScriptNavMenu},
  data() {
    return {
      jsr223ProcessorData: {},
      baseCodeTemplates: [],
      httpCodeTemplates: [
        {
          title: "API"+this.$t('api_test.definition.document.request_info'),
          children: [
            {
              title: this.$t('api_test.request.address'),
              value: '\nreturnMsg.add(@address)\n',
            },
            {
              title: "Header "+this.$t('api_test.definition.document.request_param'),
              value: '\nreturnMsg.add(@header(${param}))\n',
            },
            {
              title: this.$t('api_test.request.body')+this.$t('api_test.variable'),
              value: '\nreturnMsg.add(@body(${param}))\n',
            },
            {
              title: this.$t('api_test.request.body')+this.$t('api_test.variable')+" (Raw)",
              value: '\nreturnMsg.add(@bodyRaw)\n',
            },
            {
              title: "Query "+this.$t('api_test.definition.document.request_param'),
              value: '\nreturnMsg.add(@query(${param}))\n',
            },
            {
              title: "Rest "+this.$t('api_test.definition.document.request_param'),
              value: '\nreturnMsg.add(@rest(${param}))\n',
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
      ],
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
        'beanshell',"groovy"
        // , "python",  "nashornScript", "rhinoScript"
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
    if(this.showApi){
      this.baseCodeTemplates = this.httpCodeTemplates;
    }else {
      this.baseCodeTemplates = this.tcpCodeTemplates;
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
    showApi:{
      type:Boolean,
      default:true,
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
  /*height: calc(100vh - 570px);*/
  height: 185px;
  min-height: 170px;
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
