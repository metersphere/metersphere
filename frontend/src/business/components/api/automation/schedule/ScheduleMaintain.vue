<template>
  <el-dialog :close-on-click-modal="false" width="60%" class="schedule-edit" :visible.sync="dialogVisible"
             :append-to-body='true'
             @close="close">
    <template>
      <div v-loading="request.loading">
        <el-tabs v-model="activeName">
          <el-tab-pane :label="$t('schedule.task_config')" name="first">
            <div class="el-step__icon is-text" style="margin-right: 10px;">
              <div class="el-step__icon-inner">1</div>
            </div>
            <span>{{ $t('schedule.edit_timer_task') }}</span>
            <el-form :model="form" :rules="rules" ref="from" style="margin-top: 10px;" class="ms-el-form-item__error">

              <el-form-item :label="$t('commons.schedule_cron_title')"
                            prop="cronValue" style="height: 50px">
                <el-row :gutter="20">
                  <el-col :span="16">
                    <el-input :disabled="isReadOnly" v-model="form.cronValue" class="inp"
                              :placeholder="$t('schedule.please_input_cron_expression')" size="mini">
                      <a :disabled="isReadOnly" @click="showCronDialog" slot="suffix" class="head">
                        {{ $t('schedule.generate_expression') }}
                      </a>
                    </el-input>

                    <span>{{ this.$t('commons.schedule_switch') }}</span>
                    <el-tooltip effect="dark" placement="bottom"
                                :content="schedule.enable ? $t('commons.close_schedule') : $t('commons.open_schedule')">
                      <el-switch v-model="schedule.enable" style="margin-left: 20px"></el-switch>
                    </el-tooltip>
                  </el-col>
                  <el-col :span="2">
                    <el-button :disabled="isReadOnly" type="primary" @click="saveCron" size="mini">{{
                        $t('commons.save')
                      }}
                    </el-button>
                  </el-col>
                </el-row>

              </el-form-item>
              <crontab-result :ex="form.cronValue" ref="crontabResult"/>
            </el-form>
            <div class="el-step__icon is-text" style="margin-right: 10px;">
              <div class="el-step__icon-inner">2</div>
            </div>
            <span>{{ $t('load_test.runtime_config') }}</span>
            <div style="padding-top: 10px;">
              <span class="ms-mode-span">{{ $t("commons.environment") }}：</span>
              <env-popover :project-ids="projectIds"
                           :placement="'bottom-start'"
                           :project-list="projectList"
                           :env-map="projectEnvListMap"
                           :environment-type.sync="runConfig.environmentType"
                           :group-id="runConfig.environmentGroupId"
                           :has-option-group="true"
                           @setEnvGroup="setEnvGroup"
                           @setProjectEnvMap="setProjectEnvMap"
                           @showPopover="showPopover"
                           ref="envPopover" class="env-popover"/>
            </div>
            <div class="ms-mode-div">
                  <span class="ms-mode-span">{{ $t("run_mode.other_config") }}：</span>
              <span> {{ $t('run_mode.run_with_resource_pool') }}：</span>
              <el-select style="margin-left: 10px"
                         v-model="runConfig.resourcePoolId"
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
  getCurrentProjectID,
  getCurrentUser, getCurrentWorkspaceId, hasLicense,
  listenGoBack, objToStrMap,
  removeGoBackListener, strMapToObj
} from "@/common/js/utils";
import Crontab from "@/business/components/common/cron/Crontab";
import CrontabResult from "@/business/components/common/cron/CrontabResult";
import {cronValidate} from "@/common/js/cron";
import MsScheduleNotification from "./ScheduleNotification";
import ScheduleSwitch from "@/business/components/api/automation/schedule/ScheduleSwitch";
import {ENV_TYPE} from "@/common/js/constants";
import EnvPopover from "@/business/components/api/automation/scenario/EnvPopover";

function defaultCustomValidate() {
  return {pass: true};
}

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const noticeTemplate = requireComponent.keys().length > 0 ? requireComponent("./notice/NoticeTemplate.vue") : {};


export default {
  name: "MsScheduleMaintain",
  components: {
    CrontabResult,
    ScheduleSwitch,
    Crontab,
    MsScheduleNotification,
    "NoticeTemplate": noticeTemplate.default,
    EnvPopover
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
    request: {}
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
      } else if(!this.intervalValidate()){
        callback(new Error(this.$t('schedule.cron_expression_interval_error')));
      }
      else if (!customValidate.pass) {
        callback(new Error(customValidate.info));
      } else {
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
        enable: false
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
        reportName: "",
        mode: "serial",
        reportType: "iddReport",
        onSampleError: false,
        resourcePoolId: null,
        envMap: {},
        environmentGroupId: "",
        environmentType: ENV_TYPE.JSON
      },
      projectList: [],
      projectIds: new Set(),
      projectEnvListMap: new Map,
    }
  },
  methods: {
    currentUser: () => {
      return getCurrentUser();
    },
    intervalValidate() {
      if (this.getIntervalTime() < 1 * 60 * 1000) {
        return false;
      }
      return true;
    },
    getProjectApplication() {
      let hasPool = false;
      this.resourcePools.forEach(item => {
        if (item.id === this.runConfig.resourcePoolId) {
          hasPool = true;
          return;
        }
      });
      if (!hasPool) {
        this.$get('/project_application/get/config/' + getCurrentProjectID(), res => {
          if (res.data && res.data.poolEnable && res.data.resourcePoolId) {
            this.runConfig.resourcePoolId = res.data.resourcePoolId;
          }
        });
      }
    },
    changeMode() {
      this.runConfig.reportType = "iddReport";
      this.runConfig.reportName = "";
    },
    setEnvGroup(id) {
      this.runConfig.environmentGroupId = id;
    },
    setProjectEnvMap(projectEnvMap) {
      this.runConfig.envMap = strMapToObj(projectEnvMap);
    },
    showPopover() {
      this.projectIds.clear();
      let url = "/api/automation/env";

      this.$post(url, this.request, res => {
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
      this.$get("/project/getOwnerProjects", res => {
        this.projectList = res.data;
      })
    },
    getResourcePools() {
      this.result = this.$get('/testresourcepool/list/quota/valid', response => {
        this.resourcePools = response.data;
        this.getProjectApplication();
      });
    },
    scheduleChange() {
      let flag = this.schedule.enable;
      let param = {};
      param.taskID = this.schedule.id;
      param.enable = flag;
      let that = this;
      if (flag == false) {
        this.$confirm(this.$t('api_test.home_page.running_task_list.confirm.close_title'), this.$t('commons.prompt'), {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning',
          beforeClose(action, instance, done) {
            if (action == 'cancel') {  //  否则在 messageBox 点击取消后，switch 按钮仍然会被关闭
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
        if (this.paramRow.redirectFrom == 'testPlan') {
          paramTestId = this.paramRow.id;
          this.scheduleTaskType = "TEST_PLAN_TEST";
        } else if (this.paramRow.redirectFrom == 'enterpriseReport') {
          paramTestId = this.paramRow.id;
          this.scheduleTaskType = "ENTERPRISE_REPORT";
        } else {
          paramTestId = this.paramRow.id;
          this.scheduleTaskType = "API_SCENARIO_TEST";
        }
        this.taskID = paramTestId;
        this.findSchedule(paramTestId);
      });
    },
    initUserList() {
      this.result = this.$get('/user/project/member/list', response => {
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
      if (row.redirectFrom == 'testPlan') {
        paramTestId = row.id;
        this.scheduleTaskType = "TEST_PLAN_TEST";
      } else if (row.redirectFrom == 'enterpriseReport') {
        paramTestId = row.id;
        this.scheduleTaskType = "ENTERPRISE_REPORT";
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
      this.getWsProjects();
      this.runConfig.environmentType = ENV_TYPE.JSON;
    },
    findSchedule() {
      let scheduleResourceID = this.testId;
      let taskType = this.scheduleTaskType;
      this.result = this.$get("/schedule/findOne/" + scheduleResourceID + "/" + taskType, response => {
        if (response.data != null) {
          this.schedule = response.data;
          if (response.data.config) {
            this.runConfig = JSON.parse(response.data.config);
            if (this.runConfig.envMap) {
              this.projectEnvListMap = objToStrMap(this.runConfig.envMap);
            } else {
              this.projectEnvListMap = new Map;
            }
          }
        } else {
          this.schedule = {
            value: '',
            enable: false
          };
        }
      });
    },
    crontabFill(value, resultList) {
      //确定后回传的值
      this.form.cronValue = value;
      // 如果是第一次设置定时任务规则，则默认开启定时任务
      if (!this.schedule.id){
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
      param.group = this.scheduleTaskType;
      // 兼容问题，数据库里有的projectId为空
      if (!param.projectId) {
        param.projectId = getCurrentProjectID();
      }
      if (!param.workspaceId) {
        param.workspaceId = getCurrentWorkspaceId();
      }
      if (this.schedule.enable) {
        if ((this.runConfig.environmentType === 'JSON' && Object.keys(this.runConfig.envMap).length === 0) || (this.runConfig.environmentType === 'GROUP' && !this.runConfig.environmentGroupId)) {
          this.$warning(this.$t('workspace.env_group.please_select_env_for_current_scenario'));
          return;
        }
        if (this.runConfig.resourcePoolId == null) {
          this.$warning(this.$t('workspace.env_group.please_select_run_within_resource_pool'));
          return;
        }
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
      } else if (this.scheduleTaskType == 'ENTERPRISE_REPORT') {
        param.scheduleFrom = "enterpriseReport";
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
      this.result = this.$post("notice/save", param, () => {
        this.$success(this.$t('commons.save_success'));
      })
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
}
</script>

<style scoped>

.inp {
  width: 50%;
  margin-right: 20px;
}

.el-form-item {
  margin-bottom: 10px;
}

>>> .el-form-item__error {
  margin-left: 148px;
}

.ms-mode-span {
  margin-right: 10px;
}

.head {
  border-bottom: 1px solid #7C3985;
  color: #7C3985;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", Arial, sans-serif;
  font-size: 13px;
  cursor: pointer;
}

/deep/ .el-link {
  /* display: -webkit-inline-box; */
  /* display: inline-flex; */
}

.ms-mode-div {
  margin-top: 10px;
}

.ms-mode-span-label:before {
  content: '*';
  color: #F56C6C;
  margin-right: 4px;
  margin-left: 10px;
}
.ms-el-form-item__error >>> .el-form-item__error{
  left: -42px;
  padding-top: 0px;
}
</style>
