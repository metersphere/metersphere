<template>
  <div >
    <el-row>
      <el-col :span="20" class="script-content">
        <ms-code-edit v-if="isCodeEditAlive" mode="java" :read-only="isReadOnly" :data.sync="beanShellProcessor.script" theme="eclipse" :modes="['java']" ref="codeEdit"/>
      </el-col>
      <el-col :span="4" class="script-index">
        <div class="template-title">{{$t('api_test.request.processor.code_template')}}</div>
        <div v-for="(template, index) in codeTemplates" :key="index" class="code-template">
          <el-link :disabled="template.disabled" @click="addTemplate(template)">{{template.title}}</el-link>
        </div>
        <div class="document-url">
          <el-link href="https://jmeter.apache.org/usermanual/component_reference.html#BeanShell_PostProcessor" type="primary">{{$t('commons.reference_documentation')}}</el-link>
          <ms-instructions-icon :content="$t('api_test.request.processor.bean_shell_processor_tip')"/>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
    import MsCodeEdit from "../../../../common/components/MsCodeEdit";
    import MsInstructionsIcon from "../../../../common/components/MsInstructionsIcon";
    export default {
      name: "MsBeanShellProcessor",
      components: {MsInstructionsIcon, MsCodeEdit},
      data() {
        return {
          codeTemplates: [
            {
              title: this.$t('api_test.request.processor.code_template_get_variable'),
              value: 'vars.get("variable_name");',
            },
            {
              title: this.$t('api_test.request.processor.code_template_set_variable'),
              value: 'vars.put("variable_name", "variable_value");',
            },
            {
              title: this.$t('api_test.request.processor.code_template_get_response_header'),
              value: 'prev.getResponseHeaders();',
              disabled: this.isPreProcessor
            },
            {
              title: this.$t('api_test.request.processor.code_template_get_response_code'),
              value: 'prev.getResponseCode();',
              disabled: this.isPreProcessor
            },
            {
              title: this.$t('api_test.request.processor.code_template_get_response_result'),
              value: 'prev.getResponseDataAsString();',
              disabled: this.isPreProcessor
            }
          ],
          isCodeEditAlive: true
        }
      },
      props: {
        type: {
          type: String,
        },
        isReadOnly: {
          type: Boolean,
          default: false
        },
        beanShellProcessor: {
          type: Object,
        },
        isPreProcessor: {
          type: Boolean,
          default: false
        }
      },
      methods: {
        addTemplate(template) {
          if (!this.beanShellProcessor.script) {
            this.beanShellProcessor.script = "";
          }
          this.beanShellProcessor.script += template.value;
          this.reload();
        },
        reload() {
          this.isCodeEditAlive = false;
          this.$nextTick(() => (this.isCodeEditAlive = true));
        }
      }
    }
</script>

<style scoped>

  .ace_editor {
    border-radius: 5px;
  }

  .script-content {
    height: calc(100vh - 570px);
  }

  .script-index {
   padding: 0 20px;
  }

  .script-index div:first-child {
    font-weight: bold;
    font-size: 15px;
  }

  .template-title {
    margin-bottom: 5px;
  }

  .document-url {
    margin-top: 10px;
  }

  .instructions-icon {
    margin-left: 5px;
  }

</style>
