<template>
  <el-main>
    <div class="api-schedule-form">
      <el-form :model="formData" :rules="rules" v-loading="result.loading" label-width="80px" ref="form">
        <el-row>
          <el-col :span="12">
            <el-form-item :label-width="labelWith" :label="'Swagger URL'" prop="swaggerUrl" class="swagger-url">
              <el-input size="small" v-model="formData.swaggerUrl" clearable/>
            </el-form-item>
            <el-form-item :label-width="labelWith" :label="'Cron表达式'" prop="rule">
              <el-input :disabled="isReadOnly"
                        v-model="formData.rule"
                        size="small"
                        :placeholder="$t('schedule.please_input_cron_expression')"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label-width="labelWith" :label="$t('commons.import_module')" prop="moduleId">
              <select-tree class="select-tree" size="small"
                           :data="moduleOptions"
                           :defaultKey="formData.moduleId"
                           @getValue="setModule"
                           :obj="moduleObj" clearable checkStrictly ref="selectTree"/>
              <!--              <ms-select-tree :disabled="readOnly" :data="treeNodes" :defaultKey="form.module" :obj="moduleObj"-->
              <!--                              @getValue="setModule" clearable checkStrictly size="small"/>-->
            </el-form-item>
            <el-form-item :label-width="labelWith" :label="$t('commons.import_mode')" prop="modeId">
              <el-select size="small" v-model="formData.modeId" clearable>
                <el-option v-for="item in modeOptions" :key="item.id" :label="item.name" :value="item.id"/>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12" style="margin-left: 50px">
            <el-switch v-model="authEnable" :active-text="$t('api_test.api_import.add_request_params')"
                       @change="changeAuthEnable"></el-switch>
          </el-col>

          <el-col :span="19" v-show="authEnable" style="margin-top: 10px; margin-left: 50px" class="request-tabs">
            <!-- 请求头 -->
            <div>
              <span>{{ $t('api_test.request.headers') }}{{ $t('api_test.api_import.optional') }}：</span>
            </div>
            <ms-api-key-value :label="$t('api_test.definition.request.auth_config')"
                              :show-desc="true" :isShowEnable="isShowEnable" :suggestions="headerSuggestions"
                              :items="headers"/>
            <!--query 参数-->
            <div style="margin-top: 10px">
              <span>{{ $t('api_test.definition.request.query_param') }}{{ $t('api_test.api_import.optional') }}：</span>
            </div>
            <ms-api-variable :with-mor-setting="true" :is-read-only="isReadOnly" :isShowEnable="isShowEnable"
                             :parameters="queryArguments"/>
            <!--认证配置-->
            <div style="margin-top: 10px">
              <span>{{ $t('api_test.definition.request.auth_config') }}{{ $t('api_test.api_import.optional') }}：</span>
            </div>
            <ms-api-auth-config :is-read-only="isReadOnly" :request="authConfig" :encryptShow="false"/>
          </el-col>
        </el-row>

        <el-form-item class="expression-link">
          <el-link :disabled="isReadOnly" type="primary" @click="showCronDialog">
            {{ $t('schedule.generate_expression') }}
          </el-link>
        </el-form-item>
        <el-form-item>
          <crontab-result :ex="formData.rule" ref="crontabResult"/>
        </el-form-item>
      </el-form>

      <div style="margin-top: 20px;" class="clearfix">
        <el-row>
          <el-col :span="4">
            <el-button type="primary" style="float: right" size="mini" @click="openSchedule">
              {{ $t('schedule.task_notification') }}
            </el-button>
          </el-col>
          <el-col :span="20">
            <el-button v-if="!formData.id" type="primary" style="float: right" size="mini" @click="saveCron">
              {{ $t('commons.add') }}
            </el-button>
            <div v-else>
              <el-button type="primary" style="float: right;margin-left: 10px" size="mini" @click="clear">
                {{ $t('commons.clear') }}
              </el-button>
              <el-button type="primary" style="float: right" size="mini" @click="saveCron">{{ $t('commons.update') }}
              </el-button>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>

    <div class="task-list">
      <swagger-task-list
        @clear="clear"
        :param="param"
        @rowClick="handleRowClick"
        ref="taskList"/>
    </div>

    <el-dialog width="60%" :title="$t('schedule.generate_expression')" :visible.sync="showCron" :modal="false">
      <crontab @hide="showCron=false" @fill="crontabFill" :expression="formData.value" ref="crontab"/>
    </el-dialog>
    <el-dialog
      :title="$t('schedule.task_notification')"
      :visible.sync="dialogVisible"
      width="60%"
    >
      <swagger-task-notification :api-test-id="formData.id" :scheduleReceiverOptions="scheduleReceiverOptions"
                                 ref="scheduleTaskNotification">

      </swagger-task-notification>
    </el-dialog>


  </el-main>
</template>

<script>
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import SwaggerTaskList from "@/business/components/api/definition/components/import/SwaggerTaskList";
import CrontabResult from "@/business/components/common/cron/CrontabResult";
import Crontab from "@/business/components/common/cron/Crontab";
import {cronValidate} from "@/common/js/cron";
import {getCurrentProjectID, getCurrentUser, getCurrentWorkspaceId} from "@/common/js/utils";
import SelectTree from "@/business/components/common/select-tree/SelectTree";
import SwaggerTaskNotification from "@/business/components/api/definition/components/import/SwaggerTaskNotification";
import MsApiKeyValue from "../ApiKeyValue";
import MsApiVariable from "../ApiVariable";
import MsApiAuthConfig from "../auth/ApiAuthConfig";
import {REQUEST_HEADERS} from "@/common/js/constants";
import {KeyValue} from "../../model/ApiTestModel";
import {ELEMENT_TYPE, TYPE_TO_C} from "@/business/components/api/automation/scenario/Setting";

export default {
  name: "ApiSchedule",
  components: {
    SwaggerTaskNotification,
    SelectTree,
    MsFormDivider,
    SwaggerTaskList,
    CrontabResult,
    Crontab,
    MsApiKeyValue,
    MsApiVariable,
    MsApiAuthConfig
  },
  props: {
    customValidate: {
      type: Function,
      default: () => {
        return {pass: true};
      }
    },
    isReadOnly: {
      type: Boolean,
      default: false
    },
    moduleOptions: Array,
    param: Object
  },

  watch: {
    'schedule.value'() {
      this.formData.rule = this.formData.value;
    }
  },
  data() {
    const validateCron = (rule, ruleVal, callback) => {
      let customValidate = this.customValidate(this.getIntervalTime());
      if (!ruleVal) {
        callback(new Error(this.$t('commons.input_content')));
      } else if (!cronValidate(ruleVal)) {
        callback(new Error(this.$t('schedule.cron_expression_format_error')));
      } else if (!customValidate.pass) {
        callback(new Error(customValidate.info));
      } else {
        callback();
      }
    };
    return {
      labelWith: '150px',
      operation: true,
      schedule: {
        value: "",
      },
      scheduleReceiverOptions: [],
      dialogVisible: false,
      showCron: false,
      activeName: 'first',
      swaggerUrl: String,
      projectId: String,
      moduleId: String,
      paramSwaggerUrlId: String,
      modulePath: String,
      modeId: String,
      rules: {
        rule: [{required: true, validator: validateCron, trigger: 'blur'}],
        swaggerUrl: [{required: true, trigger: 'blur', message: this.$t('commons.please_fill_content')}],
      },
      formData: {
        swaggerUrl: '',
        modeId: this.$t('commons.not_cover'),
        moduleId: '',
        rule: ''
      },
      modeOptions: [
        {
          id: 'fullCoverage',
          name: this.$t('commons.cover')
        },
        {
          id: 'incrementalMerge',
          name: this.$t('commons.not_cover')
        }
      ],
      result: {},
      moduleObj: {
        id: 'id',
        label: 'name',
      },
      isShowEnable: true,
      authEnable: false,
      headerSuggestions: REQUEST_HEADERS,
      headers: [],
      queryArguments: [],
      authConfig: {
        hashTree: []
      }
    };
  },

  methods: {
    openSchedule() {
      if (this.formData.id !== null && this.formData.id !== undefined) {
        this.dialogVisible = true;
        this.initUserList();
        this.$nextTick(() => {
          if (this.$refs.scheduleTaskNotification) {
            this.$refs.scheduleTaskNotification.initForm();
          }
        })
      } else {
        this.$warning("请先选择您要添加通知的定时任务");
      }

    },

    initUserList() {
      this.result = this.$post('/user/project/member/list', {projectId: getCurrentProjectID()},response => {
        this.scheduleReceiverOptions = response.data;
      });
    },
    currentUser: () => {
      return getCurrentUser();
    },
    changeAuthEnable() {
      if (!this.authEnable) {
        this.clearAuthInfo();
      }
    },
    clear() {
      this.formData.id = null;
      this.formData.moduleId = null;
      this.$refs['form'].resetFields();
      if (!this.formData.rule) {
        this.$refs.crontabResult.resultList = [];
      }
      this.clearAuthInfo();
      this.$nextTick(() => {
        this.$refs.selectTree.init();
      });
    },
    crontabFill(value, resultList) {
      //确定后回传的值
      this.formData.rule = value;
      this.$refs.crontabResult.resultList = resultList;
      this.$refs['form'].validate();
    },
    showCronDialog() {
      let tmp = this.schedule.value;
      this.schedule.value = '';
      this.$nextTick(() => {
        this.schedule.value = tmp;
        this.showCron = true;
      });
    },
    saveCron() {
      this.$refs['form'].validate((valid) => {
        if (valid) {
          this.intervalShortValidate();
          this.saveSchedule();
        } else {
          return false;
        }
      });
    },
    saveSchedule() {
      this.formData.projectId = getCurrentProjectID();
      this.formData.workspaceId = getCurrentWorkspaceId();
      this.formData.value = this.formData.rule;
      if (this.authEnable) {
        // 设置请求头或 query 参数
        this.formData.headers = this.headers;
        this.formData.arguments = this.queryArguments;
        // 设置 BaseAuth 参数
        if (this.authConfig.authManager != undefined) {
          this.authConfig.authManager.clazzName = TYPE_TO_C.get("AuthManager");
          this.formData.authManager = this.authConfig.authManager;
        }
      } else {
        this.formData.headers = undefined;
        this.formData.arguments = undefined;
        this.formData.authManager = undefined;
      }
      let url = '';
      if (this.formData.id) {
        url = '/api/definition/schedule/update';
      } else {
        this.formData.enable = true;
        url = '/api/definition/schedule/create';
      }
      if (!this.formData.moduleId) {
        if (this.$refs.selectTree.returnDataKeys.length > 0) {
          this.formData.moduleId = this.$refs.selectTree.returnDataKeys
        }
      }
      this.$post(url, this.formData, () => {
        this.$success(this.$t('commons.save_success'));
        this.$refs.taskList.search();
        this.clear();
      });
    },
    intervalShortValidate() {
      if (this.getIntervalTime() < 3 * 60 * 1000) {
        this.$info(this.$t('schedule.cron_expression_interval_short_error'));
      }
      return true;
    },
    resultListChange() {
      this.$refs['form'].validate();
    },
    getIntervalTime() {
      let resultList = this.$refs.crontabResult.resultList;
      let time1 = new Date(resultList[0]);
      let time2 = new Date(resultList[1]);
      return time2 - time1;
    },
    setModule(id, data) {
      this.formData.moduleId = id;
      this.formData.modulePath = data.path;
    },
    handleRowClick(row) {
      // 如果认证信息不为空，进行转化
      if (row.config != null || row.config != undefined) {
        this.authEnable = true;
        let config = JSON.parse(row.config);
        this.headers = config.headers;
        this.queryArguments = config.arguments;
        if (config.authManager != null || config.authManager != undefined) {
          this.authConfig = config;
        } else {
          this.authConfig = {hashTree: [], authManager: {}};
        }
      } else {
        this.clearAuthInfo();
      }
      Object.assign(this.formData, row);
      this.$nextTick(() => {
        this.$refs.selectTree.init();
      });
    },
    clearAuthInfo() {
      this.headers = [];
      this.queryArguments = [];
      this.headers.push(new KeyValue({enable: true}));
      this.queryArguments.push(new KeyValue({enable: true}));
      this.authConfig = {hashTree: [], authManager: {}};
      this.authEnable = false;
    }
  },
  computed: {
    isTesterPermission() {
      return true;
    },
    isSwagger2() {
      return this.selectedPlatformValue === 'Swagger2';
    },
  }
};
</script>

<style scoped>
.select-tree {
  width: 205px;
  display: inline-block;
}

.api-schedule-form, .task-list {
  border: #DCDFE6 solid 1px;
  padding: 10px;
}

.task-list {
  margin-top: 15px;
}

.expression-link {
  margin-bottom: 0;
}
</style>
