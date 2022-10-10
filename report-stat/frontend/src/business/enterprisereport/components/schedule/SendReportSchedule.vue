<template>
  <el-dialog :close-on-click-modal="false" width="60%" class="schedule-edit" :visible.sync="dialogVisible"
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
              <el-form-item :label="$t('commons.schedule_cron_title')"
                            prop="cronValue">
                <el-row :gutter="20">
                  <el-col :span="16">
                    <el-input :disabled="isReadOnly" v-model="form.cronValue" class="inp"
                              :placeholder="$t('schedule.please_input_cron_expression')" size="mini">
                      <a :disabled="isReadOnly" type="primary" @click="showCronDialog" slot="suffix" class="head">
                        {{ $t('schedule.generate_expression') }}
                      </a>
                    </el-input>
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
            <el-dialog width="60%" :title="$t('schedule.generate_expression')" :visible.sync="showCron"
                       :modal="false">
              <crontab @hide="showCron=false" @fill="crontabFill" :expression="schedule.value"
                       ref="crontab"/>
            </el-dialog>
          </el-tab-pane>

        </el-tabs>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import {getCurrentProjectID, getCurrentUser, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {listenGoBack, operationConfirm, removeGoBackListener} from "metersphere-frontend/src/utils"
import Crontab from "metersphere-frontend/src/components/cron/Crontab";
import CrontabResult from "metersphere-frontend/src/components/cron/CrontabResult";
import {cronValidate} from "metersphere-frontend/src/utils/cron";
import MsScheduleNotification from "@/business/compnent/schedule/ScheduleNotification";
import ScheduleSwitch from "@/business/compnent/schedule/ScheduleSwitch";
import {saveNotice} from "@/api/notice";
import {createScheduleTask, selectScheduleTask, updateScheduleTask} from "@/api/enterprise-report";
import {selectUserProjectMember} from "@/api/resource";

function defaultCustomValidate() {
  return {pass: true};
}


export default {
  name: "SendReportSchedule",
  components: {
    CrontabResult,
    ScheduleSwitch,
    Crontab,
    MsScheduleNotification,
    "NoticeTemplate": () => import("metersphere-frontend/src/components/MxNoticeTemplate")
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
    },
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
      }
    }
  },
  methods: {
    currentUser: () => {
      return getCurrentUser();
    },
    scheduleChange() {
      let param = {};
      param.id = this.schedule.id;
      param.enable = this.schedule.enable;
      this.schedule.enable = !this.schedule.enable;
      operationConfirm(this, this.$t('api_test.home_page.running_task_list.confirm.close_title'), () => {
        this.schedule.enable = !this.schedule.enable;
        this.updateTask(param);
      });
    },
    updateTask(param) {
      this.result = this.updateScheduleTask(param).then(() => {
        let paramTestId = this.paramRow.id;
        this.scheduleTaskType = "SCHEDULE_SEND_REPORT";
        this.taskID = paramTestId;
        if (!param.enable) {
          this.close();
          this.$emit("refreshTable");
        }
        this.findSchedule(paramTestId);
      });
    },
    initUserList() {
      this.result = selectUserProjectMember().then(response => {
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
      paramTestId = row.id;
      this.scheduleTaskType = "SCHEDULE_SEND_REPORT";
      this.testId = paramTestId;
      this.findSchedule(paramTestId);
      this.initUserList();
      this.dialogVisible = true;
      this.form.cronValue = this.schedule.value;
      listenGoBack(this.close);
      this.activeName = 'first';
    },
    findSchedule() {
      this.result = selectScheduleTask(this.testId, this.scheduleTaskType).then(response => {
        if (response.data != null) {
          this.schedule = response.data;
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
      param.scheduleFrom = "SCHEDULE_SEND_REPORT";

      if (this.schedule.id) {
        param.id = this.schedule.id;
        updateScheduleTask(param).then(() => {
          this.$success(this.$t('commons.save_success'));
          this.$emit("refreshTable");
        });
      } else {
        createScheduleTask(param).then(() => {
          this.$success(this.$t('commons.save_success'));
          this.$emit("refreshTable");
        })
      }
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
  },
}
</script>

<style scoped>

.inp {
  width: 50%;
  margin-right: 20px;
}

.head {
  border-bottom: 1px solid #7C3985;
  color: #7C3985;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", Arial, sans-serif;
  font-size: 13px;
  cursor: pointer;
}

</style>
