<template>
  <el-dialog
    :close-on-click-modal="false"
    width="60%"
    class="schedule-edit"
    :visible.sync="dialogVisible"
    :append-to-body="true"
    @close="close">
    <template>
      <div v-loading="loading">
        <el-tabs v-model="activeName">
          <el-tab-pane :label="$t('schedule.task_config')" name="first">
            <div class="el-step__icon is-text" style="margin-right: 10px">
              <div class="el-step__icon-inner">1</div>
            </div>
            <span>{{ $t('schedule.edit_timer_task') }}</span>
            <el-form :model="form" :rules="rules" ref="from" style="margin-top: 10px" class="ms-el-form-item__error">
              <el-form-item :label="$t('commons.schedule_cron_title')" prop="cronValue" style="height: 50px">
                <el-row :gutter="20">
                  <el-col :span="16">
                    <el-input
                      :disabled="isReadOnly"
                      v-model="form.cronValue"
                      class="inp"
                      :placeholder="$t('schedule.please_input_cron_expression')"
                      size="mini">
                      <a :disabled="isReadOnly" @click="showCronDialog" slot="suffix" class="head">
                        {{ $t('schedule.generate_expression') }}
                      </a>
                    </el-input>

                    <span>{{ this.$t('commons.schedule_switch') }}</span>
                    <el-tooltip
                      effect="dark"
                      placement="bottom"
                      :content="schedule.enable ? $t('commons.close_schedule') : $t('commons.open_schedule')">
                      <el-switch v-model="schedule.enable" style="margin-left: 20px"></el-switch>
                    </el-tooltip>
                  </el-col>
                  <el-col :span="2">
                    <el-button :disabled="isReadOnly" type="primary" @click="saveCron" size="mini"
                      >{{ $t('commons.save') }}
                    </el-button>
                  </el-col>
                </el-row>
              </el-form-item>
              <crontab-result :ex="form.cronValue" ref="crontabResult" />
            </el-form>
            <div class="el-step__icon is-text" style="margin-right: 10px">
              <div class="el-step__icon-inner">2</div>
            </div>
            <span>{{ $t('load_test.runtime_config') }}</span>
            <div style="padding-top: 10px">
              <span class="ms-mode-span">{{ $t('commons.environment') }}：</span>
              <el-radio-group v-model="runConfig.defaultEnv" style="margin-right: 20px">
                <el-radio :label="true">{{ $t('automation.default_environment') }}
                  <el-tooltip class="item" effect="dark" :content="$t('automation.default_environment_tips')" placement="right">
                    <i class="el-icon-info"/>
                  </el-tooltip></el-radio>
                <el-radio :label="false">{{ $t('automation.select_new_environment') }}</el-radio>
              </el-radio-group>
              <env-popover
                :project-ids="projectIds"
                :placement="'bottom-start'"
                :project-list="projectList"
                :env-map="projectEnvListMap"
                :environment-type.sync="runConfig.environmentType"
                :group-id="runConfig.environmentGroupId"
                :has-option-group="true"
                @setEnvGroup="setEnvGroup"
                @setProjectEnvMap="setProjectEnvMap"
                @showPopover="showPopover"
                ref="envPopover"
                class="env-popover"
                v-show="this.runConfig.defaultEnv === false" />
            </div>
            <div class="ms-mode-div">
              <span class="ms-mode-span">{{ $t('run_mode.other_config') }}：</span>
              <span>{{ $t('run_mode.run_with_resource_pool') }}:</span>
              <el-select style="margin-left: 10px" v-model="runConfig.resourcePoolId" size="mini">
                <el-option
                  v-for="item in resourcePools"
                  :key="item.id"
                  :label="item.name"
                  :disabled="!item.api"
                  :value="item.id">
                </el-option>
              </el-select>
            </div>

            <el-dialog width="60%" :title="$t('schedule.generate_expression')" :visible.sync="showCron" :modal="false">
              <crontab @hide="showCron = false" @fill="crontabFill" :expression="schedule.value" ref="crontab" />
            </el-dialog>
          </el-tab-pane>
          <el-tab-pane :label="$t('schedule.task_notification')" name="second" v-permission="['PROJECT_MESSAGE:READ']">
            <ms-schedule-notification :test-id="testId" :schedule-receiver-options="scheduleReceiverOptions" />
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import { saveNotice } from '@/api/notice';
import { apiScenarioEnv, createSchedule, updateSchedule } from '@/api/scenario';
import { getScheduleByIdAndType, scheduleUpdate } from '@/api/schedule';
import { getCurrentProjectID, getCurrentUser, getCurrentWorkspaceId } from 'metersphere-frontend/src/utils/token';
import { listenGoBack, objToStrMap, removeGoBackListener, strMapToObj } from 'metersphere-frontend/src/utils';
import Crontab from 'metersphere-frontend/src/components/cron/Crontab';
import CrontabResult from 'metersphere-frontend/src/components/cron/CrontabResult';
import { cronValidate } from 'metersphere-frontend/src/utils/cron';
import MsScheduleNotification from './ScheduleNotification';
import ScheduleSwitch from '@/business/automation/schedule/ScheduleSwitch';
import { ENV_TYPE } from 'metersphere-frontend/src/utils/constants';
import EnvPopover from '@/business/automation/scenario/EnvPopover';
import { getMaintainer, getOwnerProjects, getProjectConfig } from '@/api/project';
import { getTestResourcePools } from '@/api/test-resource-pool';
function defaultCustomValidate() {
  return { pass: true };
}

export default {
  name: 'MsScheduleMaintain',
  components: {
    CrontabResult,
    ScheduleSwitch,
    Crontab,
    MsScheduleNotification,
    EnvPopover,
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
    request: {},
  },

  watch: {
    'schedule.value'() {
      this.form.cronValue = this.schedule.value;
    },
  },
  data() {
    const validateCron = (rule, cronValue, callback) => {
      let customValidate = this.customValidate(this.getIntervalTime());
      if (!cronValue) {
        callback(new Error(this.$t('commons.input_content')));
      } else if (!cronValidate(cronValue)) {
        callback(new Error(this.$t('schedule.cron_expression_format_error')));
      } else if (!this.intervalValidate()) {
        callback(new Error(this.$t('schedule.cron_expression_interval_error')));
      } else if (!customValidate.pass) {
        callback(new Error(customValidate.info));
      } else {
        callback();
      }
    };
    return {
      loading: false,
      scheduleReceiverOptions: [],
      operation: true,
      dialogVisible: false,
      schedule: {
        value: '',
        enable: false,
      },
      scheduleTaskType: '',
      testId: String,
      showCron: false,
      form: {
        cronValue: '',
      },
      paramRow: {},
      activeName: 'first',
      rules: {
        cronValue: [{ required: true, validator: validateCron, trigger: 'blur' }],
      },
      resourcePools: [],
      runConfig: {
        reportName: '',
        mode: 'serial',
        reportType: 'iddReport',
        onSampleError: false,
        resourcePoolId: null,
        envMap: {},
        environmentGroupId: '',
        environmentType: ENV_TYPE.JSON,
        defaultEnv: true,
      },
      projectList: [],
      projectIds: new Set(),
      projectEnvListMap: new Map(),
    };
  },
  methods: {
    async checkPool() {
      let hasPool = false;
      this.resourcePools.forEach((item) => {
        if (item.id === this.runConfig.resourcePoolId) {
          hasPool = true;
        }
      });
      return hasPool;
    },
    async getProjectApplication() {
      let hasPool = await this.checkPool();
      if (!hasPool) {
        this.runConfig.resourcePoolId = null;
        getProjectConfig(getCurrentProjectID(), '').then(async (res) => {
          if (res.data && res.data.poolEnable && res.data.resourcePoolId) {
            this.runConfig.resourcePoolId = res.data.resourcePoolId;
          }
          hasPool = await this.checkPool();
          if (!hasPool) {
            this.runConfig.resourcePoolId = undefined;
          }
        });
      }
    },
    intervalValidate() {
      if (this.getIntervalTime() < 1 * 60 * 1000) {
        return false;
      }
      return true;
    },
    changeMode() {
      this.runConfig.reportType = 'iddReport';
      this.runConfig.reportName = '';
    },
    setEnvGroup(id) {
      this.runConfig.environmentGroupId = id;
    },
    setProjectEnvMap(projectEnvMap) {
      this.runConfig.envMap = strMapToObj(projectEnvMap);
    },
    showPopover() {
      this.projectIds.clear();
      apiScenarioEnv(this.request).then((res) => {
        let data = res.data;
        if (data) {
          for (let d in data) {
            this.projectIds.add(data[d]);
          }
        }
        this.$refs.envPopover.openEnvSelect();
      });
    },
    getWsProjects() {
      getOwnerProjects().then((res) => {
        this.projectList = res.data;
      });
    },
    getResourcePools() {
      this.result = getTestResourcePools().then((response) => {
        this.resourcePools = response.data;
        this.getProjectApplication();
      });
    },
    updateTask(param) {
      this.result = scheduleUpdate(param).then((response) => {
        let paramTestId = '';
        if (this.paramRow.redirectFrom == 'testPlan') {
          paramTestId = this.paramRow.id;
          this.scheduleTaskType = 'TEST_PLAN_TEST';
        } else if (this.paramRow.redirectFrom == 'enterpriseReport') {
          paramTestId = this.paramRow.id;
          this.scheduleTaskType = 'ENTERPRISE_REPORT';
        } else {
          paramTestId = this.paramRow.id;
          this.scheduleTaskType = 'API_SCENARIO_TEST';
        }
        this.taskID = paramTestId;
        this.findSchedule(paramTestId);
      });
    },
    initUserList() {
      this.result = getMaintainer().then((response) => {
        this.scheduleReceiverOptions = response.data;
      });
    },
    buildParam() {
      let param = {};
      param.notices = this.tableData;
      param.testId = this.testId;
      return param;
    },
    async open(row) {
      //测试计划页面跳转来的
      let paramTestId = '';
      this.paramRow = row;
      if (row.redirectFrom == 'testPlan') {
        paramTestId = row.id;
        this.scheduleTaskType = 'TEST_PLAN_TEST';
      } else if (row.redirectFrom == 'enterpriseReport') {
        paramTestId = row.id;
        this.scheduleTaskType = 'ENTERPRISE_REPORT';
      } else {
        paramTestId = row.id;
        this.scheduleTaskType = 'API_SCENARIO_TEST';
      }
      this.testId = paramTestId;
      await this.findSchedule(paramTestId);
      this.initUserList();
      this.dialogVisible = true;
      this.form.cronValue = this.schedule.value;
      listenGoBack(this.close);
      this.activeName = 'first';
      this.getResourcePools();
      this.getWsProjects();
      this.runConfig.environmentType = ENV_TYPE.JSON;
    },
    async findSchedule() {
      let scheduleResourceID = this.testId;
      let taskType = this.scheduleTaskType;
      this.result = getScheduleByIdAndType(scheduleResourceID, taskType).then((response) => {
        if (response.data != null) {
          this.schedule = response.data;
          if (response.data.config) {
            this.runConfig = JSON.parse(response.data.config);
            // 兼容历史数据
            if (this.runConfig.defaultEnv === null) {
              this.runConfig.defaultEnv = false;
            }
            if (this.runConfig.envMap) {
              this.projectEnvListMap = objToStrMap(this.runConfig.envMap);
            } else {
              this.projectEnvListMap = new Map();
            }
          }
        } else {
          this.schedule = {
            value: '',
            enable: false,
          };
        }
      });
    },
    crontabFill(value, resultList) {
      //确定后回传的值
      this.form.cronValue = value;
      // 如果是第一次设置定时任务规则，则默认开启定时任务
      if (!this.schedule.id) {
        this.schedule.enable = true;
      }
      this.$refs.crontabResult.resultList = resultList;
      this.$refs['from'].validate();
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
      this.$refs['from'].validate((valid) => {
        if (valid) {
          this.intervalShortValidate();
          let formCronValue = this.form.cronValue;
          this.schedule.value = formCronValue;
          setTimeout(() => {
            this.saveSchedule();
          }, 10);
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
      param.group = this.scheduleTaskType;
      // 兼容问题，数据库里有的projectId为空
      if (!param.projectId) {
        param.projectId = getCurrentProjectID();
      }
      if (!param.workspaceId) {
        param.workspaceId = getCurrentWorkspaceId();
      }
      if (this.schedule.enable) {
        if (
          (!this.runConfig.defaultEnv &&
            this.runConfig.environmentType === 'JSON' &&
            Object.keys(this.runConfig.envMap).length === 0) ||
          (this.runConfig.environmentType === 'GROUP' && !this.runConfig.environmentGroupId)
        )
          if (this.runConfig.resourcePoolId == null) {
            this.$warning(this.$t('workspace.env_group.please_select_run_within_resource_pool'));
            return;
          }
      }
      param.config = JSON.stringify(this.runConfig);
      param.scheduleFrom = 'scenario';
      if (param.id) {
        updateSchedule(param).then(() => {
          this.$success(this.$t('commons.save_success'));
          this.$emit('refreshTable');
        });
      } else {
        createSchedule(param).then(() => {
          this.$success(this.$t('commons.save_success'));
          this.$emit('refreshTable');
        });
      }
      this.dialogVisible = false;
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
      this.result = saveNotice(param).then(() => {
        this.$success(this.$t('commons.save_success'));
      });
    },
    close() {
      this.dialogVisible = false;
      this.form.cronValue = '';
      if (this.$refs['from']) {
        this.$refs['from'].resetFields();
      }
      if (!this.schedule.value) {
        this.$refs.crontabResult.resultList = [];
      }
      removeGoBackListener(this.close);
    },
    intervalShortValidate() {
      if (this.schedule.enable && this.getIntervalTime() < 3 * 60 * 1000) {
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
  },
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

:deep(.el-form-item__error) {
  margin-left: 148px;
}

.ms-mode-span {
  margin-right: 10px;
}

.head {
  border-bottom: 1px solid var(--primary_color);
  color: var(--primary_color);
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', Arial, sans-serif;
  font-size: 13px;
  cursor: pointer;
}

:deep(.el-link) {
  /* display: -webkit-inline-box; */
  /* display: inline-flex; */
}

.ms-mode-div {
  margin-top: 10px;
}

.ms-mode-span-label:before {
  content: '*';
  color: #f56c6c;
  margin-right: 4px;
  margin-left: 10px;
}

.ms-el-form-item__error :deep(.el-form-item__error) {
  left: -42px;
  padding-top: 0px;
}
</style>
