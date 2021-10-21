<template>
  <div v-loading="result.loading">
    <el-dialog :close-on-click-modal="false" :title="dialogTitle" :visible.sync="visible" :destroy-on-close="true"
               @close="close" width="75%" top="5vh">
      <div style="height: 62vh; overflow-y: auto; overflow-x: hidden">
        <el-form :model="form" label-position="right" label-width="80px" size="small" :rules="rules" v-if="isFormAlive"
                 ref="form">
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
          <el-row style="margin-right: 10px;" type="flex" :gutter="5">
            <el-col :span="codeSpan">
              <el-form-item>
                <template v-slot>
                  <div style="position: relative;">
                    <el-tabs v-model="activeName">
                      <el-tab-pane :label="$t('project.code_segment.segment')" name="code">
                        <ms-code-edit
                          v-if="isCodeEditAlive"
                          :mode="codeEditModeMap[form.type]"
                          height="440px"
                          :data.sync="form.script"
                          theme="eclipse"
                          :modes="modes"
                          ref="codeEdit"/>
                      </el-tab-pane>
                      <el-tab-pane :label="$t('project.code_segment.result')" name="result">
                        <div v-loading="runResult.loading">
                          <ms-code-edit :mode="'text'" :data.sync="console" v-if="isResultAlive" height="440px"
                                        ref="funcResult"/>
                        </div>
                      </el-tab-pane>
                    </el-tabs>
                    <el-button type="primary" size="mini" style="width: 70px;position: absolute; right: 0;top:10px;" @click="handleTest"
                               :disabled="runResult.loading">{{ $t('project.code_segment.test') }}
                    </el-button>
                  </div>
                </template>
              </el-form-item>
            </el-col>
            <div style="width: 14px;margin-top: 20px;margin-right: 5px;">
              <div style="height: 12px;width: 12px; line-height:12px; margin-top: 20px;">
                <i :class="showMenu ? 'el-icon-remove-outline' : 'el-icon-circle-plus-outline'"
                   class="show-menu"
                   @click="switchMenu"></i>
              </div>
            </div>
            <el-col :span="menuSpan" class="script-index" v-if="showMenu">
              <ms-dropdown :default-command="form.type" :commands="languages" style="margin-bottom: 5px;margin-left: 15px;margin-top: 30px;"
                           @command="languageChange"/>
              <script-nav-menu ref="scriptNavMenu" :language="form.type" @handleCode="handleCodeTemplate"/>
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
  </div>

</template>

<script>
import MsInputTag from "@/business/components/api/automation/scenario/MsInputTag";
import FunctionParams from "@/business/components/project/menu/function/FunctionParams";
import MsCodeEdit from "@/business/components/common/components/MsCodeEdit";
import MsDropdown from "@/business/components/common/components/MsDropdown";
import {FUNC_TEMPLATE} from "@/business/components/project/menu/function/custom-function";
import MsRun from "@/business/components/api/automation/scenario/DebugRun";
import {getCurrentProjectID, getUUID} from "@/common/js/utils";
import {JSR223Processor} from "@/business/components/api/definition/model/ApiTestModel";
import FunctionRun from "@/business/components/project/menu/function/FunctionRun";
import CustomFunctionRelate from "@/business/components/project/menu/function/CustomFunctionRelate";
import ApiFuncRelevance from "@/business/components/project/menu/function/ApiFuncRelevance";
import ScriptNavMenu from "@/business/components/project/menu/function/ScriptNavMenu";

export default {
  name: "EditFunction",
  components: {
    ScriptNavMenu,
    CustomFunctionRelate,
    FunctionRun,
    MsCodeEdit,
    FunctionParams,
    MsInputTag,
    MsDropdown,
    MsRun,
    ApiFuncRelevance
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
      dialogCreateTitle: this.$t('project.code_segment.create'),
      dialogUpdateTitle: this.$t('project.code_segment.update'),
      activeName: 'code',
      dialogTitle: "",
      isCodeEditAlive: true,
      isResultAlive: true,
      isFormAlive: true,
      showMenu: true,
      codeSpan: 20,
      menuSpan: 4,
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
        'beanshell',
        "python",
        "groovy",
        "nashornScript",
        "rhinoScript"
      ],
      codeEditModeMap: {
        beanshell: 'java',
        python: 'python',
        groovy: 'java',
        nashornScript: 'javascript',
        rhinoScript: 'javascript',
      },
      response: {},
      request: {},
      debug: true,
      console: this.$t('project.code_segment.no_result')
    }
  },
  methods: {
    open(data) {
      this.activeName = "code";
      this.visible = true;
      this.form.type = "beanshell";
      if (data && data.id) {
        // 重新加载自定义函数对象，列表中没有查询大字段数据
        this.initFunc(data.id);
        this.dialogTitle = this.dialogUpdateTitle;
      } else {
        this.form.script = FUNC_TEMPLATE[this.form.type];
        this.reloadCodeEdit();
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
        this.reload();
      })
    },
    close() {
      this.form = {
        type: "beanshell",
        params: [{}]
      };
      this.console = this.$t('project.code_segment.no_result');
      this.visible = false;
    },
    languageChange(language) {
      this.form.type = language;
      if (!this.form.script) {
        this.form.script = FUNC_TEMPLATE[language];
        this.reloadCodeEdit();
      }
    },
    handleCodeTemplate(code) {
      if (!this.form.script) {
        this.form.script = code;
      } else {
        this.form.script = this.form.script + '\n' + code;
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
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.id) {
            this.update(param);
          } else {
            this.create(param);
          }
        } else {
          return false;
        }
      })
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
      if (!obj.projectId) {
        obj.projectId = getCurrentProjectID();
      }
      this.result = this.$post("/custom/func/update", obj, () => {
        this.$emit("refresh");
        this.$success(this.$t('commons.modify_success'));
      })
    },
    handleTest() {
      this.activeName = "result";
      this.console = this.$t('project.code_segment.no_result');
      this.reloadResult();
      this.runResult.loading = true;
      let jSR223Processor = new JSR223Processor({
        scriptLanguage: this.form.type,
        script: this.form.script
      });
      jSR223Processor.id = getUUID().substring(0, 8);
      this.runData = [];
      this.runData.push(jSR223Processor);
      this.reportId = getUUID().substring(0, 8);
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
  margin-top: 10px;
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
