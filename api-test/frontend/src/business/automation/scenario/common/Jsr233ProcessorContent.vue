<template>
  <div>
    <el-row type="flex" :gutter="10">
      <el-col :span="codeSpan" class="script-content">
        <ms-code-edit
          v-if="isCodeEditAlive"
          :mode="codeEditModeMap[jsr223ProcessorData.scriptLanguage]"
          :read-only="isReadOnly"
          :data.sync="jsr223ProcessorData.script"
          theme="eclipse"
          :modes="['java', 'python']"
          ref="codeEdit" />
      </el-col>
      <div style="width: 14px; margin-right: 5px">
        <div style="height: 12px; width: 12px; line-height: 12px">
          <i
            :class="showMenu ? 'el-icon-remove-outline' : 'el-icon-circle-plus-outline'"
            class="show-menu"
            @click="switchMenu"></i>
        </div>
      </div>
      <el-col :span="menuSpan" class="script-index" v-if="isReadOnly !== true">
        <ms-dropdown
          :default-command="jsr223ProcessorData.scriptLanguage"
          :commands="languages"
          style="margin-bottom: 5px; margin-left: 15px"
          @command="languageChange" />
        <el-checkbox
          v-model="jsr223ProcessorData.jsrEnable"
          style="padding-left: 10px"
          :disabled="jsr223ProcessorData.scriptLanguage !== 'beanshell'"
          @change="changeEnable">
          JSR223
        </el-checkbox>
        <script-nav-menu
          ref="scriptNavMenu"
          :language="jsr223ProcessorData.scriptLanguage"
          :menus="codeTemplates"
          @handleCode="handleCodeTemplate" />
      </el-col>
    </el-row>
  </div>
</template>

<script>
import MsCodeEdit from '../../../definition/components/MsCodeEdit';
import MsDropdown from '@/business/commons/MsDropdown';
import ScriptNavMenu from './function/ScriptNavMenu';

export default {
  name: 'Jsr233ProcessorContent',
  components: { MsDropdown, MsCodeEdit, ScriptNavMenu },
  data() {
    return {
      jsr223ProcessorData: {},
      codeTemplates: [
        {
          title: this.$t('project.code_segment.api_test'),
          children: [
            {
              title: this.$t('project.code_segment.import_api_test'),
              command: 'api_definition',
            },
            {
              title: this.$t('project.code_segment.new_api_test'),
              command: 'new_api_request',
            },
          ],
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
              disabled: this.isPreProcessor,
            },
            {
              title: this.$t('api_test.request.processor.code_template_get_response_code'),
              value: 'prev.getResponseCode()',
              disabled: this.isPreProcessor,
            },
            {
              title: this.$t('api_test.request.processor.code_template_get_response_result'),
              value: 'prev.getResponseDataAsString()',
              disabled: this.isPreProcessor,
            },
          ],
        },
        {
          title: this.$t('project.code_segment.project_env'),
          children: [
            {
              title: this.$t('api_test.request.processor.param_environment_set_global_variable'),
              value: 'vars.put(${__metersphere_env_id}+"key","value");\n' + 'vars.put("key","value")',
            },
          ],
        },
        {
          title: this.$t('project.code_segment.code_segment'),
          children: [
            {
              title: this.$t('project.code_segment.insert_segment'),
              command: 'custom_function',
            },
          ],
        },
        {
          title: this.$t('project.code_segment.exception_handle'),
          children: [
            {
              title: this.$t('project.code_segment.stop_test'),
              value: 'ctx.getEngine().stopThreadNow(ctx.getThread().getThreadName());',
            },
          ],
        },
      ],
      isCodeEditAlive: true,
      languages: ['beanshell', 'python', 'groovy', 'javascript'],
      codeEditModeMap: {
        beanshell: 'java',
        python: 'python',
        groovy: 'java',
        nashornScript: 'javascript',
        rhinoScript: 'javascript',
        javascript: 'javascript',
      },
      codeSpan: 20,
      menuSpan: 4,
      showMenu: true,
    };
  },
  created() {
    if (this.jsr223Processor.jsrEnable === null || this.jsr223Processor.jsrEnable === undefined) {
      this.$set(this.jsr223Processor, 'jsrEnable', true);
    }
    if (!this.jsr223Processor.scriptLanguage) {
      this.$set(this.jsr223Processor, 'scriptLanguage', 'beanshell');
    }
    this.jsr223ProcessorData = this.jsr223Processor;
  },
  props: {
    isReadOnly: {
      type: Boolean,
      default: false,
    },
    jsr223Processor: {
      type: Object,
    },
    protocol: String,
    isPreProcessor: {
      type: Boolean,
      default: false,
    },
    node: {},
  },
  watch: {
    jsr223Processor() {
      this.reload();
    },
    activeName: {
      handler() {
        setTimeout(() => {
          // 展开动画大概是 300ms 左右，使视觉效果更流畅
          this.$refs.codeEdit?.$el.querySelector('.ace_text-input')?.focus();
        }, 300);
      },
      immediate: true,
    },
  },
  methods: {
    addTemplate(template) {
      if (!this.jsr223ProcessorData.script) {
        this.jsr223ProcessorData.script = '';
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
      if (language !== 'beanshell') {
        this.jsr223ProcessorData.jsrEnable = true;
      }
      this.$emit('languageChange');
    },
    changeEnable() {
      if (this.jsr223ProcessorData.jsrEnable) {
        if (this.jsr223ProcessorData.type === 'JSR223PostProcessor') {
          this.jsr223ProcessorData.name =
            this.jsr223ProcessorData.name !== 'BeanShellPostProcessor'
              ? this.jsr223ProcessorData.name
              : 'JSR223PostProcessor';
        } else {
          this.jsr223ProcessorData.name =
            this.jsr223ProcessorData.name !== 'BeanShellPreProcessor'
              ? this.jsr223ProcessorData.name
              : 'JSR223PreProcessor';
        }
      } else {
        if (this.jsr223ProcessorData.type === 'JSR223PostProcessor') {
          this.jsr223ProcessorData.name =
            this.jsr223ProcessorData.name !== 'JSR223PostProcessor'
              ? this.jsr223ProcessorData.name
              : 'BeanShellPostProcessor';
        } else {
          this.jsr223ProcessorData.name =
            this.jsr223ProcessorData.name !== 'JSR223PreProcessor'
              ? this.jsr223ProcessorData.name
              : 'BeanShellPreProcessor';
        }
      }
    },
    addCustomFuncScript(script) {
      this.jsr223ProcessorData.script = this.jsr223ProcessorData.script
        ? this.jsr223ProcessorData.script + '\n\n' + script
        : script;
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
      if (this.$refs.codeEdit) {
        this.$refs.codeEdit.insert(code);
      }
    },
  },
};
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
