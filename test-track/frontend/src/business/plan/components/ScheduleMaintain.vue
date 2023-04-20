<template>
  <el-dialog
    v-loading="result.loading"
    :close-on-click-modal="false"
    width="60%"
    class="schedule-edit"
    :visible.sync="dialogVisible"
    :append-to-body="true"
    @close="close"
  >
    <template>
      <div>
        <el-tabs v-model="activeName">
          <el-tab-pane :label="$t('schedule.task_config')" name="first">
            <div class="el-step__icon is-text" style="margin-right: 10px">
              <div class="el-step__icon-inner">1</div>
            </div>
            <span>{{ $t("schedule.edit_timer_task") }}</span>
            <el-form
              :model="form"
              :rules="rules"
              ref="from"
              style="padding-top: 10px; margin-left: 20px"
              class="ms-el-form-item__error"
            >
              <el-form-item
                :label="$t('commons.schedule_cron_title')"
                prop="cronValue"
                style="height: 50px"
              >
                <el-row :gutter="20">
                  <el-col :span="16">
                    <el-input
                      :disabled="isReadOnly"
                      v-model="form.cronValue"
                      class="inp"
                      :placeholder="$t('schedule.please_input_cron_expression')"
                      size="mini"
                    >
                      <a
                        :disabled="isReadOnly"
                        type="primary"
                        @click="showCronDialog"
                        slot="suffix"
                        class="head"
                      >
                        {{ $t("schedule.generate_expression") }}
                      </a>
                    </el-input>

                    <span>{{ this.$t("commons.schedule_switch") }}</span>
                    <el-tooltip
                      effect="dark"
                      placement="bottom"
                      :content="
                        schedule.enable
                          ? $t('commons.close_schedule')
                          : $t('commons.open_schedule')
                      "
                    >
                      <el-switch
                        v-model="schedule.enable"
                        style="margin-left: 20px"
                      ></el-switch>
                    </el-tooltip>
                  </el-col>
                  <el-col :span="2">
                    <el-button
                      :disabled="isReadOnly"
                      type="primary"
                      @click="saveCron"
                      size="mini"
                      >{{ $t("commons.save") }}
                    </el-button>
                  </el-col>
                </el-row>
              </el-form-item>
              <crontab-result :ex="form.cronValue" ref="crontabResult" />
            </el-form>

            <div class="el-step__icon is-text" style="margin-right: 10px">
              <div class="el-step__icon-inner">2</div>
            </div>
            <span>{{ $t("load_test.runtime_config") }}</span>
            <div class="ms-mode-div">
              <span class="ms-mode-span">{{ $t("run_mode.title") }}：</span>
              <el-radio-group v-model="runConfig.mode" @change="changeMode">
                <el-radio label="serial">{{ $t("run_mode.serial") }}</el-radio>
                <el-radio label="parallel"
                  >{{ $t("run_mode.parallel") }}
                </el-radio>
              </el-radio-group>
            </div>
            <div style="margin-top: 10px">
              <span class="ms-mode-span">{{ $t("浏览器") }}：</span>
              <el-select
                size="mini"
                v-model="runConfig.browser"
                style="margin-right: 30px; width: 100px"
              >
                <el-option
                  v-for="b in browsers"
                  :key="b.value"
                  :value="b.value"
                  :label="b.label"
                ></el-option>
              </el-select>
            </div>
            <div class="ms-mode-div" v-if="runConfig.mode === 'serial'">
              <el-row>
                <el-col :span="3">
                  <span class="ms-mode-span"
                    >{{ $t("run_mode.other_config") }}：</span
                  >
                </el-col>
                <el-col :span="18">
                  <div v-if="testType === 'API'">
                    <el-checkbox
                      v-model="runConfig.runWithinResourcePool"
                      style="padding-right: 10px"
                      :disabled="runMode === 'POOL'"
                    >
                      {{ $t("run_mode.run_with_resource_pool") }}
                    </el-checkbox>
                    <el-select
                      :disabled="!runConfig.runWithinResourcePool"
                      v-model="runConfig.resourcePoolId"
                      size="mini"
                    >
                      <el-option
                        v-for="item in resourcePools"
                        :key="item.id"
                        :label="item.name"
                        :value="item.id"
                      >
                      </el-option>
                    </el-select>
                  </div>
                </el-col>
              </el-row>
            </div>
            <div class="ms-mode-div" v-if="runConfig.mode === 'parallel'">
              <el-row>
                <el-col :span="3">
                  <span class="ms-mode-span"
                    >{{ $t("run_mode.other_config") }}：</span
                  >
                </el-col>
                <el-col :span="18">
                  <div v-if="testType === 'API'">
                    <el-checkbox
                      v-model="runConfig.runWithinResourcePool"
                      style="padding-right: 10px"
                      :disabled="runMode === 'POOL'"
                    >
                      {{ $t("run_mode.run_with_resource_pool") }}
                    </el-checkbox>
                    <el-select
                      :disabled="!runConfig.runWithinResourcePool"
                      v-model="runConfig.resourcePoolId"
                      size="mini"
                    >
                      <el-option
                        v-for="item in resourcePools"
                        :key="item.id"
                        :label="item.name"
                        :disabled="!item.api"
                        :value="item.id"
                      >
                      </el-option>
                    </el-select>
                  </div>
                </el-col>
              </el-row>
            </div>

            <!-- 失败重试 -->
            <div class="ms-mode-div">
              <el-row>
                <el-col :span="3">
                  <span class="ms-mode-span">&nbsp;</span>
                </el-col>
                <el-col :span="18">
                  <el-checkbox
                    v-model="runConfig.retryEnable"
                    class="ms-failure-div-right"
                  >
                    {{ $t("run_mode.retry_on_failure") }}
                  </el-checkbox>
                  <span v-if="runConfig.retryEnable">
                    <el-tooltip placement="top">
                      <div slot="content">
                        {{ $t("run_mode.retry_message") }}
                      </div>
                      <i class="el-icon-question" style="cursor: pointer" />
                    </el-tooltip>
                    <span>
                      {{ $t("run_mode.retry") }}
                      <el-input-number
                        :value="runConfig.retryNum"
                        v-model="runConfig.retryNum"
                        :min="1"
                        :max="10000000"
                        size="mini"
                      />
                      &nbsp;
                      {{ $t("run_mode.retry_frequency") }}
                    </span>
                  </span>
                </el-col>
              </el-row>
            </div>
            <div class="ms-failure-div" v-if="runConfig.mode === 'serial'">
              <el-row>
                <el-col :span="18" :offset="3">
                  <div>
                    <el-checkbox v-model="runConfig.onSampleError"
                      >{{ $t("api_test.fail_to_stop") }}
                    </el-checkbox>
                  </div>
                </el-col>
              </el-row>
            </div>
            <div>
              <el-row>
                <el-col :span="3"> &nbsp;</el-col>
                <el-col :span="18">
                  <div style="margin-top: 10px">
                    <el-checkbox v-model="runConfig.headlessEnabled">
                      {{ $t("性能模式") }}
                    </el-checkbox>
                  </div>
                </el-col>
              </el-row>
            </div>

            <el-dialog
              width="60%"
              :title="$t('schedule.generate_expression')"
              :visible.sync="showCron"
              :modal="false"
            >
              <crontab
                @hide="showCron = false"
                @fill="crontabFill"
                :expression="schedule.value"
                ref="crontab"
              />
            </el-dialog>
          </el-tab-pane>
          <el-tab-pane :label="$t('schedule.task_notification')" name="second">
            <ms-schedule-notification
              :test-id="testId"
              :schedule-receiver-options="scheduleReceiverOptions"
            />
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import {
  getCurrentProjectID,
  getCurrentUser,
  getCurrentWorkspaceId,
} from "metersphere-frontend/src/utils/token";
import {
  listenGoBack,
  removeGoBackListener,
} from "metersphere-frontend/src/utils";
import Crontab from "metersphere-frontend/src/components/cron/Crontab";
import CrontabResult from "metersphere-frontend/src/components/cron/CrontabResult";
import { cronValidate } from "metersphere-frontend/src/utils/cron";
import MsScheduleNotification from "./ScheduleNotification";
import ScheduleSwitch from "./ScheduleSwitch";
import { ENV_TYPE } from "metersphere-frontend/src/utils/constants";
import MxNotification from "metersphere-frontend/src/components/MxNoticeTemplate";
import {
  createSchedule,
  getPlanSchedule,
  updateSchedule,
  updateScheduleEnableByPrimyKey,
} from "@/api/remote/plan/test-plan";
import { saveNotice } from "@/api/notice";
import { getProjectMember } from "@/api/user";
import { getQuotaValidResourcePools } from "@/api/remote/resource-pool";
import { getProjectConfig } from "@/api/project";
import { getSystemBaseSetting } from "metersphere-frontend/src/api/system";

function defaultCustomValidate() {
  return { pass: true };
}

export default {
  name: "MsTestPlanScheduleMaintain",
  components: {
    CrontabResult,
    ScheduleSwitch,
    Crontab,
    MsScheduleNotification,
    NoticeTemplate: MxNotification,
  },

  props: {
    customValidate: {
      type: Function,
      default: defaultCustomValidate,
    },
    isReadOnly: {
      type: Boolean,
      default: false,
    },
    planCaseIds: [],
    type: String,
    //是否含有ui场景 有 ui 场景就要展示 浏览器选项，性能模式
    haveUICase: {
      type: Boolean,
      default: false,
    },
  },

  watch: {
    "schedule.value"() {
      this.form.cronValue = this.schedule.value;
    },
    "runConfig.runWithinResourcePool"() {
      if (!this.runConfig.runWithinResourcePool) {
        this.runConfig.resourcePoolId = null;
      }
    },
  },
  data() {
    const validateCron = (rule, cronValue, callback) => {
      let customValidate = this.customValidate(this.getIntervalTime());
      if (!cronValue) {
        callback(new Error(this.$t("commons.input_content")));
      } else if (!cronValidate(cronValue)) {
        callback(new Error(this.$t("schedule.cron_expression_format_error")));
      } else if (!this.intervalValidate()) {
        callback(new Error(this.$t("schedule.cron_expression_interval_error")));
      } else if (!customValidate.pass) {
        callback(new Error(customValidate.info));
      } else {
        if (!this.schedule.id) {
          this.schedule.enable = true;
        }
        callback();
      }
    };
    return {
      runMode: "",
      result: {},
      scheduleReceiverOptions: [],
      operation: true,
      dialogVisible: false,
      schedule: {
        value: "",
        enable: false,
      },
      testId: String,
      showCron: false,
      form: {
        cronValue: "",
      },
      paramRow: {},
      activeName: "first",
      rules: {
        cronValue: [
          { required: true, validator: validateCron, trigger: "blur" },
        ],
      },
      resourcePools: [],
      runConfig: {
        mode: "serial",
        reportType: "iddReport",
        onSampleError: false,
        runWithinResourcePool: false,
        resourcePoolId: null,
        retryEnable: false,
        retryNum: 1,
        browser: "CHROME",
        headlessEnabled: true,
      },
      projectList: [],
      testType: "API",
      planId: String,
      projectIds: new Set(),
      browsers: [
        {
          label: this.$t("chrome"),
          value: "CHROME",
        },
        {
          label: this.$t("firefox"),
          value: "FIREFOX",
        },
      ],
    };
  },
  methods: {
    query() {
      this.loading = true;
      this.result = getSystemBaseSetting().then((response) => {
        if (!response.data.runMode) {
          response.data.runMode = "LOCAL";
        }
        this.runMode = response.data.runMode;
        if (this.runMode === "POOL") {
          this.runConfig.runWithinResourcePool = true;
          this.getProjectApplication();
        } else {
          this.loading = false;
        }
      });
    },
    getProjectApplication() {
      getProjectConfig(getCurrentProjectID(), "").then((res) => {
        if (res.data && res.data.poolEnable && res.data.resourcePoolId) {
          this.runConfig.resourcePoolId = res.data.resourcePoolId;
        }
        this.loading = false;
      });
    },
    currentUser: () => {
      return getCurrentUser();
    },
    intervalValidate() {
      if (this.getIntervalTime() < 1 * 60 * 1000) {
        return false;
      }
      return true;
    },
    updateTask(param) {
      this.result = updateScheduleEnableByPrimyKey(param).then((response) => {
        this.taskID = this.paramRow.id;
        this.findSchedule(this.taskID);
        this.$emit("refreshTable");
      });
    },
    initUserList() {
      this.result = getProjectMember().then((response) => {
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
      this.query();
      this.planId = row.id;
      //测试计划页面跳转来的
      let paramTestId = row.id;
      this.paramRow = row;
      this.testId = paramTestId;
      this.findSchedule(paramTestId);
      this.initUserList();
      this.dialogVisible = true;
      this.form.cronValue = this.schedule.value;
      listenGoBack(this.close);
      this.activeName = "first";
      this.getResourcePools();
      this.runConfig.environmentType = ENV_TYPE.JSON;
      this.runConfig.retryEnable = false;
      this.runConfig.retryNum = 1;
    },
    findSchedule() {
      let scheduleResourceID = this.testId;
      this.result = getPlanSchedule(scheduleResourceID, "TEST_PLAN_TEST").then(
        (response) => {
          if (response.data != null) {
            this.schedule = response.data;
            if (response.data.config) {
              this.runConfig = JSON.parse(response.data.config);
              if (this.runConfig.environmentType) {
                delete this.runConfig.environmentType;
              }
            }
          } else {
            this.schedule = {
              value: "",
              enable: false,
            };
          }
        }
      );
    },
    crontabFill(value, resultList) {
      //确定后回传的值
      this.form.cronValue = value;
      // 如果是第一次设置定时任务规则，则默认开启定时任务
      if (!this.schedule.id) {
        this.schedule.enable = true;
      }
      this.$refs.crontabResult.resultList = resultList;
      this.$refs["from"].validate();
    },
    showCronDialog() {
      let tmp = this.schedule.value;
      this.schedule.value = "";
      this.$nextTick(() => {
        this.schedule.value = tmp;
        this.showCron = true;
      });
    },
    saveCron() {
      if (
        this.runConfig.runWithinResourcePool &&
        this.runConfig.resourcePoolId == null
      ) {
        this.$warning(
          this.$t("workspace.env_group.please_select_run_within_resource_pool")
        );
        return;
      }
      this.$refs["from"].validate((valid) => {
        if (valid) {
          this.intervalShortValidate();
          let formCronValue = this.form.cronValue;
          this.schedule.value = formCronValue;
          this.saveSchedule();
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
      param.name = this.paramRow.name;
      param.group = "TEST_PLAN_TEST";
      // 兼容问题，数据库里有的projectId为空
      if (!param.projectId) {
        param.projectId = getCurrentProjectID();
      }
      if (!param.workspaceId) {
        param.workspaceId = getCurrentWorkspaceId();
      }
      if (
        this.runConfig.runWithinResourcePool &&
        this.runConfig.resourcePoolId == null
      ) {
        this.$warning(
          this.$t("workspace.env_group.please_select_run_within_resource_pool")
        );
        return;
      }
      param.config = JSON.stringify(this.runConfig);

      param.scheduleFrom = "testPlan";
      //测试计划页面跳转的创建
      if (param.id) {
        updateSchedule(param).then(() => {
          this.$success(this.$t("commons.save_success"));
          this.$emit("refreshTable");
        });
      } else {
        createSchedule(param).then(() => {
          this.$success(this.$t("commons.save_success"));
          this.$emit("refreshTable");
        });
      }
      this.dialogVisible = false;
    },
    checkScheduleEdit() {
      if (this.create) {
        this.$message(this.$t("api_test.environment.please_save_test"));
        return false;
      }
      return true;
    },
    saveNotice() {
      let param = this.buildParam();
      saveNotice(param).then(() => {
        this.$success(this.$t("commons.save_success"));
      });
    },
    close() {
      this.dialogVisible = false;
      this.form.cronValue = "";
      this.$refs["from"].resetFields();
      if (!this.schedule.value) {
        this.$refs.crontabResult.resultList = [];
      }
      removeGoBackListener(this.close);
    },
    intervalShortValidate() {
      if (this.schedule.enable && this.getIntervalTime() < 3 * 60 * 1000) {
        // return false;
        this.$info(this.$t("schedule.cron_expression_interval_short_error"));
      }
      return true;
    },
    resultListChange() {
      this.$refs["from"].validate();
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
      this.result = getQuotaValidResourcePools().then((response) => {
        this.resourcePools = response.data;
      });
    },
    changeMode() {
      this.runConfig.onSampleError = false;
      this.runConfig.runWithinResourcePool = false;
      this.runConfig.resourcePoolId = null;
    },
  },
  computed: {
    isTesterPermission() {
      return true;
    },
  },
};
</script>

<style scoped>
.inp {
  width: 40%;
  margin-right: 20px;
}

.el-form-item {
  margin-bottom: 10px;
}

.ms-mode-span {
  margin-right: 10px;
  margin-left: 10px;
}

.ms-mode-div {
  margin-top: 10px;
}

:deep(.el-form-item__error) {
  margin-left: 148px;
}

.head {
  border-bottom: 1px solid var(--primary_color);
  color: var(--primary_color);
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB",
    Arial, sans-serif;
  font-size: 13px;
  cursor: pointer;
}

:deep(.el-link) {
  /* display: -webkit-inline-box; */
  /* display: inline-flex; */
}

.ms-el-form-item__error :deep(.el-form-item__error) {
  left: -42px;
  padding-top: 0px;
}

.ms-failure-div-right {
  padding-right: 10px;
}

.ms-failure-div {
  margin-top: 10px;
}
</style>
