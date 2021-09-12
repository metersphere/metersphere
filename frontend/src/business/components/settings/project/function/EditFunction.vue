<template>
  <el-dialog :close-on-click-modal="false" :title="dialogTitle" :visible.sync="visible" :destroy-on-close="true"
             @close="close" width="65%" top="5vh" v-loading="result.loading">
    <div style="height: 61vh; overflow-y: auto; overflow-x: hidden">
      <el-form :model="form" label-position="right" label-width="80px" size="small" :rules="rules" v-if="isFormAlive">
        <el-row type="flex" :gutter="20">
          <el-col>
            <el-form-item :label="$t('commons.name')" prop="name">
              <el-input size="small" v-model="form.name" :maxlength="200" show-word-limit/>
            </el-form-item>
          </el-col>
          <el-col style="margin-right: 10px;">
            <el-form-item :label="$t('api_test.automation.tag')" prop="tags">
              <ms-input-tag :currentScenario="form" ref="tag"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row style="margin-right: 10px;">
          <el-col>
            <el-form-item :label="$t('commons.description')" prop="description">
              <el-input class="ms-http-textarea"
                        v-model="form.description"
                        type="textarea"
                        :show-word-limit="true"
                        :maxlength="500"
                        :autosize="{ minRows: 2, maxRows: 10}"
                        :rows="3" size="small"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-form-item :label="'参数列表'" prop="">
            <function-params :items="form.params"/>
          </el-form-item>
        </el-row>
        <el-row style="margin-right: 10px;">
          <el-col :span="20">
            <el-form-item>
              <template v-slot>
                <el-tabs v-model="activeName">
                  <el-tab-pane :label="'函数编辑'" name="code">
                    <ms-code-edit
                      v-if="isCodeEditAlive"
                      :mode="codeEditModeMap[form.type]"
                      height="330px"
                      :data.sync="form.script"
                      theme="eclipse"
                      :modes="modes"
                      ref="codeEdit"/>
                  </el-tab-pane>
                  <el-tab-pane :label="'执行结果'" name="result">
                    <div v-loading="runResult.loading">
                      <ms-code-edit :mode="'text'" :data.sync="console" v-if="isResultAlive" height="330px" ref="funcResult"/>
                    </div>
                  </el-tab-pane>
                </el-tabs>
              </template>
            </el-form-item>
          </el-col>
          <el-col :span="4" class="script-index">
            <div style="margin-top: -25px; margin-left: 10px;">
              <div style="margin-bottom: 10px;">
                <el-button type="primary" size="mini" style="width: 70px;" @click="handleTest" :disabled="runResult.loading">测试</el-button>
              </div>
              <ms-dropdown :default-command="form.type" :commands="languages" @command="languageChange"/>
              <div class="template-title">{{ $t('api_test.request.processor.code_template') }}</div>
              <div v-for="(template, index) in codeTemplates" :key="index" class="code-template">
                <el-link :disabled="template.disabled" @click="addTemplate(template)">{{ template.title }}</el-link>
              </div>
              <el-link href="https://jmeter.apache.org/usermanual/component_reference.html#BeanShell_PostProcessor"
                       target="componentReferenceDoc" style="margin-top: 10px"
                       type="primary">{{ $t('commons.reference_documentation') }}
              </el-link>
            </div>
          </el-col>
        </el-row>
      </el-form>
      <!-- 执行组件 -->
      <function-run :report-id="reportId" :run-data="runData" @runRefresh="runRefresh" @errorRefresh="errorRefresh"/>
    </div>
    <template v-slot:footer>
      <el-button @click="close" size="medium">{{ $t('commons.cancel') }}</el-button>
      <el-button type="primary" @click="submit" size="medium" style="margin-left: 10px;">
        {{ $t('commons.confirm') }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script>
import MsInputTag from "@/business/components/api/automation/scenario/MsInputTag";
import FunctionParams from "@/business/components/settings/project/function/FunctionParams";
import MsCodeEdit from "@/business/components/common/components/MsCodeEdit";
import MsDropdown from "@/business/components/common/components/MsDropdown";
import {splicingCustomFunc} from "@/business/components/settings/project/function/custom_function";
import MsRun from "@/business/components/api/automation/scenario/DebugRun";
import {getUUID} from "@/common/js/utils";
import {JSR223Processor} from "@/business/components/api/definition/model/ApiTestModel";
import FunctionRun from "@/business/components/settings/project/function/FunctionRun";

export default {
  name: "EditFunction",
  components: {
    FunctionRun,
    MsCodeEdit,
    FunctionParams,
    MsInputTag,
    MsDropdown,
    MsRun
  },
  props: {},
  data() {
    return {
      visible: false,
      result: {},
      runResult: {
        loading: false
      },
      reportId: "",
      runData: [],
      isStop: false,
      dialogCreateTitle: "创建函数",
      dialogUpdateTitle: "更新函数",
      activeName: 'code',
      dialogTitle: "",
      isCodeEditAlive: true,
      isResultAlive: true,
      isFormAlive: true,
      form: {
        params: [],
        script: "",
        type: "beanshell",
      },
      rules: {
        name: [
          {required: true, message: this.$t('commons.input_name'), trigger: 'blur'},
          {max: 300, message: this.$t('commons.input_limit', [0, 300]), trigger: 'blur'}
        ]
      },
      modes: ['java', 'python'],
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
          value: 'vars.put(${__metersphere_env_id}+"key","value");\n' + 'vars.put("key","value")',
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
      response: {},
      request: {},
      debug: true,
      console: "无执行结果"
    }
  },
  watch: {
    'form.name'() {
      this.splicingFunc();
    },
    'form.params': {
      handler() {
        this.splicingFunc();
      },
      deep: true
    }
  },
  methods: {
    _parseFuncParam(funcObj) {
      let params = undefined;
      if (funcObj.params) {
        params = funcObj.params.map(p => p.name);
        if (params.length > 0) {
          params = params.filter(p => {
            return p !== undefined
          });
          params.join(",\s");
        }
      }
      // todo 参数拼接问题 删除参数 逗号未去除
      return params;
    },
    splicingFunc() {
      let funcObj = this.form;
      let funcName = funcObj.name;
      let funcLanguage = this.form.type || "beanshell";
      let funcParams = this._parseFuncParam(funcObj);
      this.form.script = splicingCustomFunc(funcLanguage, this.form.script, funcName, funcParams);
      this.reloadCodeEdit();
    },
    open(data) {
      this.activeName = "code";
      this.visible = true;
      this.form.type = "beanshell";
      if (data && data.id) {
        // 重新加载自定义函数对象，列表中没有查询大字段数据
        this.initFunc(data.id);
        this.dialogTitle = this.dialogUpdateTitle;
      } else {
        this.form.tags = [];
        this.form.params = [{}];
        this.dialogTitle = this.dialogCreateTitle;
      }
    },
    initFunc(id) {
      this.result = this.$get("/custom/func/get/" + id, res => {
        this.form = res.data;
        if (!this.form.tags) {
          this.form.tags = [];
        } else {
          this.form.tags = JSON.parse(this.form.tags);
        }
        if (!this.form.params) {
          this.form.params = [];
        } else {
          this.form.params = JSON.parse(this.form.params);
        }
        this.reload();
      })
    },
    close() {
      this.form = {
        type: "beanshell",
        params: [{}]
      };
      this.console = "无执行结果";
      this.visible = false;
    },
    languageChange(language) {
      this.form.type = language;
      this.$emit("languageChange");
    },
    addTemplate(template) {
      if (!this.form.script) {
        this.form.script = "";
      }
      this.form.script += template.value;
      if (this.form.type === 'beanshell') {
        this.form.script += ';';
      }
      this.reloadCodeEdit();
    },
    reload() {
      this.isFormAlive = false;
      this.$nextTick(() => {
        this.isFormAlive = true;
      })
    },
    reloadCodeEdit() {
      this.isCodeEditAlive = false;
      this.$nextTick(() => (this.isCodeEditAlive = true));
    },
    reloadResult() {
      this.isResultAlive = false;
      this.$nextTick(() => (this.isResultAlive = true));
    },
    submit() {
      let param = Object.assign({}, this.form);
      param.params = JSON.stringify(this.form.params);
      param.tags = JSON.stringify(this.form.tags);
      if (this.form.id) {
        this.update(param);
      } else {
        this.create(param);
      }
    },
    create(obj) {
      this.result = this.$post("/custom/func/save", obj, res => {
        this.$emit("refresh");
        if (res.data) {
          this.form.id = res.data.id;
        }
        this.$success(this.$t('commons.save_success'));
      })
    },
    update(obj) {
      this.result = this.$post("/custom/func/update", obj, () => {
        this.$emit("refresh");
        this.$success(this.$t('commons.modify_success'));
      })
    },
    handleTest() {
      this.activeName = "result";
      this.console = "无执行结果";
      this.reloadResult();
      this.runResult.loading = true;

      let jSR223Processor = new JSR223Processor({
        script: this.form.script
      });
      jSR223Processor.id = getUUID().substring(0, 8);
      this.runData = [];
      this.runData.push(jSR223Processor);
      this.reportId = getUUID().substring(0, 8);
    },
    runRefresh(data) {
      this.response = data;
      this.console = this.response.responseResult.console;
      this.runResult.loading = false;
      this.reloadResult();
    },
    errorRefresh() {
      this.runResult.loading = false;
    }
  }
}
</script>

<style scoped>
.template-title {
  margin-bottom: 5px;
  font-weight: bold;
  font-size: 15px;
  margin-top: 1px;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
  position: fixed;
}

::-webkit-scrollbar-thumb {
  border-radius: 1em;
  background-color: rgba(50, 50, 50, .3);
  position: fixed;
}

::-webkit-scrollbar-track {
  border-radius: 1em;
  background-color: transparent;
  position: fixed;
}

.script-index {
  margin-top: 35px;
}
</style>
