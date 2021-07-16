<template>
  <el-dialog
    v-loading="result.loading"
    :close-on-click-modal="false" width="60%" class="schedule-edit" :visible.sync="dialogVisible"
    :append-to-body='true'
    @close="close">
    <template>
      <div>
        <el-tabs v-model="activeName">
          <el-tab-pane :label="$t('schedule.task_config')" name="first">
            <div class="el-step__icon is-text" style="margin-right: 10px;">
              <div class="el-step__icon-inner">1</div>
            </div>
            <span>{{ $t('schedule.edit_timer_task') }}</span>
            <el-form :model="form" :rules="rules" ref="from" style="padding-top: 10px;margin-left: 20px;">
              <el-form-item
                prop="cronValue">
                <el-row>
                  <el-col :span="18">
                    <el-input :disabled="isReadOnly" v-model="form.cronValue" class="inp"
                              :placeholder="$t('schedule.please_input_cron_expression')"/>
                    <el-button :disabled="isReadOnly" type="primary" @click="saveCron">{{
                        $t('commons.save')
                      }}
                    </el-button>
                  </el-col>
                  <el-col :span="6">
                    <schedule-switch :schedule="schedule" :corn-value="form.cronValue"
                                     @resultListChange="getExecuteTimeTemplate"
                                     @scheduleChange="scheduleChange"></schedule-switch>
                  </el-col>
                </el-row>

              </el-form-item>
              <el-form-item>
                <el-link :disabled="isReadOnly" type="primary" @click="showCronDialog">
                  {{ $t('schedule.generate_expression') }}
                </el-link>
              </el-form-item>
              <crontab-result :ex="form.cronValue" ref="crontabResult"/>
            </el-form>

            <div class="el-step__icon is-text" style="margin-right: 10px;">
              <div class="el-step__icon-inner">2</div>
            </div>
            <span>{{ $t('load_test.runtime_config') }}</span>
            <div style="padding-top: 10px;">
              <span class="ms-mode-span">{{ $t("run_mode.title") }}：</span>
              <el-radio-group v-model="runConfig.mode">
                <el-radio label="serial">{{ $t("run_mode.serial") }}</el-radio>
                <el-radio label="parallel">{{ $t("run_mode.parallel") }}</el-radio>
              </el-radio-group>
            </div>
            <div class="ms-mode-div" v-if="runConfig.mode === 'serial'">
              <el-row>
                <el-col :span="3">
                  <span class="ms-mode-span">{{ $t("run_mode.other_config") }}:</span>
                </el-col>
                <el-col :span="18">
                  <div>
                    <el-checkbox v-model="runConfig.onSampleError">失败停止</el-checkbox>
                  </div>
                  <div v-if="scheduleTaskType === 'TEST_PLAN_TEST'" style="padding-top: 10px">
                    <el-checkbox v-model="runConfig.runWithinResourcePool" style="padding-right: 10px;">
                      {{ $t('run_mode.run_with_resource_pool') }}
                    </el-checkbox>
                    <el-select :disabled="!runConfig.runWithinResourcePool" v-model="runConfig.resourcePoolId"
                               size="mini">
                      <el-option
                        v-for="item in resourcePools"
                        :key="item.id"
                        :label="item.name"
                        :disabled="!item.api"
                        :value="item.id">
                      </el-option>
                    </el-select>
                  </div>
                </el-col>
              </el-row>
            </div>
            <div class="ms-mode-div" v-if="runConfig.mode === 'parallel'">
              <el-row>
                <el-col :span="3">
                  <span class="ms-mode-span">{{ $t("run_mode.other_config") }}:</span>
                </el-col>
                <el-col :span="18">
                  <div v-if="scheduleTaskType === 'TEST_PLAN_TEST'" style="padding-top: 10px">
                    <el-checkbox v-model="runConfig.runWithinResourcePool" style="padding-right: 10px;">
                      {{ $t('run_mode.run_with_resource_pool') }}
                    </el-checkbox>
                    <el-select :disabled="!runConfig.runWithinResourcePool" v-model="runConfig.resourcePoolId"
                               size="mini">
                      <el-option
                        v-for="item in resourcePools"
                        :key="item.id"
                        :label="item.name"
                        :disabled="!item.api"
                        :value="item.id">
                      </el-option>
                    </el-select>
                  </div>
                </el-col>
              </el-row>
            </div>
            <el-dialog width="60%" :title="$t('schedule.generate_expression')" :visible.sync="showCron"
                       :modal="false">
              <crontab @hide="showCron=false" @fill="crontabFill" :expression="schedule.value"
                       ref="crontab"/>
            </el-dialog>
          </el-tab-pane>
          <el-tab-pane :label="$t('schedule.task_notification')" name="second">
            <ms-schedule-notification :test-id="testId"
                                      :schedule-receiver-options="scheduleReceiverOptions"/>
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import {
  getCurrentOrganizationId,
  getCurrentProjectID,
  getCurrentUser, getCurrentWorkspaceId,
  listenGoBack,
  removeGoBackListener
} from "@/common/js/utils";
import Crontab from "@/business/components/common/cron/Crontab";
import CrontabResult from "@/business/components/common/cron/CrontabResult";
import {cronValidate} from "@/common/js/cron";
import MsScheduleNotification from "@/business/components/api/automation/schedule/ScheduleNotification";
import ScheduleSwitch from "@/business/components/api/automation/schedule/ScheduleSwitch";

function defaultCustomValidate() {
  return {pass: true};
}

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const noticeTemplate = requireComponent.keys().length > 0 ? requireComponent("./notice/NoticeTemplate.vue") : {};


export default {
  name: "MsTestPlanScheduleMaintain",
  components: {
    CrontabResult,
    ScheduleSwitch,
    Crontab,
    MsScheduleNotification,
    "NoticeTemplate": noticeTemplate.default
  },

  props: {
    customValidate: {
      type: Function,
      default: defaultCustomValidate
    },
    isReadOnly: {
      type: Boolean,
      default: false
    },
  },


  watch: {
    'schedule.value'() {
      this.form.cronValue = this.schedule.value;
    }
  },
  data() {
    const validateCron = (rule, cronValue, callback) => {
      let customValidate = this.customValidate(this.getIntervalTime());
      if (!cronValue) {
        callback(new Error(this.$t('commons.input_content')));
      } else if (!cronValidate(cronValue)) {
        callback(new Error(this.$t('schedule.cron_expression_format_error')));
      } else if (!customValidate.pass) {
        callback(new Error(customValidate.info));
      } else {
        callback();
      }
    };
    return {
      result: {},
      scheduleReceiverOptions: [],
      operation: true,
      dialogVisible: false,
      schedule: {
        value: "",
      },
      scheduleTaskType: "",
      testId: String,
      showCron: false,
      form: {
        cronValue: ""
      },
      paramRow: {},
      activeName: 'first',
      rules: {
        cronValue: [{required: true, validator: validateCron, trigger: 'blur'}],
      },
      resourcePools: [],
      runConfig: {
        mode: "serial",
        reportType: "iddReport",
        onSampleError: false,
        runWithinResourcePool: false,
        resourcePoolId: null,
      },
    };
  },
  methods: {
    currentUser: () => {
      return getCurrentUser();
    },
    scheduleChange() {
      let flag = this.schedule.enable;
      let param = {};
      param.taskID = this.schedule.id;
      param.enable = flag;
      let that = this;
      if (flag === false) {
        this.$confirm(this.$t('api_test.home_page.running_task_list.confirm.close_title'), this.$t('commons.prompt'), {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning',
          beforeClose(action, instance, done) {
            if (action === 'cancel') {  //  否则在 messageBox 点击取消后，switch 按钮仍然会被关闭
              that.schedule.enable = param.enable = true;
            }
            done(); //  done 是关闭 messageBox 的行为
          },
        }).then(() => {
          this.updateTask(param);
        }).catch(() => {
        });
      } else {
        this.updateTask(param);
      }
    },
    updateTask(param) {
      this.result = this.$post('/api/schedule/updateEnableByPrimyKey', param, response => {
        let paramTestId = "";
        if (this.paramRow.redirectFrom === 'testPlan') {
          paramTestId = this.paramRow.id;
          this.scheduleTaskType = "TEST_PLAN_TEST";
        } else {
          paramTestId = this.paramRow.id;
          this.scheduleTaskType = "API_SCENARIO_TEST";
        }
        this.taskID = paramTestId;
        this.findSchedule(paramTestId);
      });
    },
    initUserList() {
      let param = {
        name: '',
        organizationId: getCurrentOrganizationId()
      };

      this.result = this.$post('user/org/member/list/all', param, response => {
        this.scheduleReceiverOptions = response.data;
      });

    },
    buildParam() {
      let param = {};
      param.notices = this.tableData;
      param.testId = this.testId;
      return param;
    },
    open(row) {
      //测试计划页面跳转来的
      let paramTestId = "";
      this.paramRow = row;
      if (row.redirectFrom === 'testPlan') {
        paramTestId = row.id;
        this.scheduleTaskType = "TEST_PLAN_TEST";
      } else {
        paramTestId = row.id;
        this.scheduleTaskType = "API_SCENARIO_TEST";
      }
      this.testId = paramTestId;
      this.findSchedule(paramTestId);
      this.initUserList();
      this.dialogVisible = true;
      this.form.cronValue = this.schedule.value;
      listenGoBack(this.close);
      this.activeName = 'first';
      this.getResourcePools();
    },
    findSchedule() {
      var scheduleResourceID = this.testId;
      var taskType = this.scheduleTaskType;
      this.result = this.$get("/schedule/findOne/" + scheduleResourceID + "/" + taskType, response => {
        if (response.data != null) {
          this.schedule = response.data;
          if(response.data.config){
            this.runConfig = JSON.parse(response.data.config);
          }
        } else {
          this.schedule = {};
        }
      });
    },
    crontabFill(value, resultList) {
      //确定后回传的值
      this.form.cronValue = value;
      this.$refs.crontabResult.resultList = resultList;
      this.$refs['from'].validate();
    },
    showCronDialog() {
      this.showCron = true;
    },
    saveCron() {
      this.$refs['from'].validate((valid) => {
        if (valid) {
          this.intervalShortValidate();
          let formCronValue = this.form.cronValue;
          this.schedule.enable = true;
          this.schedule.value = formCronValue;
          this.saveSchedule();
          this.dialogVisible = false;
        } else {
          return false;
        }
      });
    },
    saveSchedule() {
      this.checkScheduleEdit();
      let param = {};
      param = this.schedule;
      param.resourceId = this.testId;
      // 兼容问题，数据库里有的projectId为空
      if (!param.projectId) {
        param.projectId = getCurrentProjectID();
      }
      if (!param.workspaceId) {
        param.workspaceId = getCurrentWorkspaceId();
      }
      param.config = JSON.stringify(this.runConfig);
      let url = '/api/automation/schedule/create';
      if (this.scheduleTaskType === "TEST_PLAN_TEST") {
        param.scheduleFrom = "testPlan";
        //测试计划页面跳转的创建
        url = '/schedule/create';
        if (param.id) {
          url = '/schedule/update';
        }
      } else {
        param.scheduleFrom = "scenario";
        if (param.id) {
          url = '/api/automation/schedule/update';
        }
      }

      this.$post(url, param, () => {
        this.$success(this.$t('commons.save_success'));
        this.$emit("refreshTable");
      });
    },
    checkScheduleEdit() {
      if (this.create) {
        this.$message(this.$t('api_test.environment.please_save_test'));
        return false;
      }
      return true;
    },
    saveNotice() {
      let param = this.buildParam();
      this.result = this.$post("notice/save", param, () => {
        this.$success(this.$t('commons.save_success'));
      });
    },
    close() {
      this.dialogVisible = false;
      this.form.cronValue = '';
      this.$refs['from'].resetFields();
      if (!this.schedule.value) {
        this.$refs.crontabResult.resultList = [];
      }
      removeGoBackListener(this.close);
    },
    intervalShortValidate() {
      if (this.getIntervalTime() < 3 * 60 * 1000) {
        // return false;
        this.$info(this.$t('schedule.cron_expression_interval_short_error'));
      }
      return true;
    },
    resultListChange() {
      this.$refs['from'].validate();
    },
    getIntervalTime() {
      let resultList = this.$refs.crontabResult.resultList;
      let time1 = new Date(resultList[0]);
      let time2 = new Date(resultList[1]);
      return time2 - time1;
    },
    getExecuteTimeTemplate(executeTileArr) {
      alert(executeTileArr);
    },
    getResourcePools() {
      this.result = this.$get('/testresourcepool/list/quota/valid', response => {
        this.resourcePools = response.data;
      });
    },
  },
  computed: {
    isTesterPermission() {
      return true;
    }
  }
};
</script>

<style scoped>

.inp {
  width: 50%;
  margin-right: 20px;
}

.el-form-item {
  margin-bottom: 10px;
}

.ms-mode-span {
  margin-right: 10px;
  margin-left: 20px;
}

.ms-mode-div {
  margin-top: 10px;
}
</style>
