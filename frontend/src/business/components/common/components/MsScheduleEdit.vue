<template>
  <el-dialog :close-on-click-modal="false" width="60%" class="schedule-edit" :visible.sync="dialogVisible"
             @close="close">
    <template>
      <div>
        <el-tabs v-model="activeName" @tab-click="handleClick">

          <el-tab-pane :label="$t('schedule.edit_timer_task')" name="first">
            <el-form :model="form" :rules="rules" ref="from">
              <el-form-item
                prop="cronValue">
                <el-input :disabled="isReadOnly" v-model="form.cronValue" class="inp"
                          :placeholder="$t('schedule.please_input_cron_expression')"/>
                <!--          <el-button type="primary" @click="showCronDialog">{{$t('schedule.generate_expression')}}</el-button>-->
                <el-button :disabled="isReadOnly" type="primary" @click="saveCron">{{
                    $t('commons.save')
                  }}
                </el-button>
              </el-form-item>
              <el-form-item>
                <el-link :disabled="isReadOnly" type="primary" @click="showCronDialog">
                  {{ $t('schedule.generate_expression') }}
                </el-link>
              </el-form-item>
              <crontab-result :ex="form.cronValue" ref="crontabResult"/>
            </el-form>
            <el-dialog width="60%" :title="$t('schedule.generate_expression')" :visible.sync="showCron"
                       :modal="false">
              <crontab @hide="showCron=false" @fill="crontabFill" :expression="schedule.value"
                       ref="crontab"/>
            </el-dialog>
          </el-tab-pane>
          <el-tab-pane :label="$t('schedule.task_notification')" name="second">
            <template>
              <el-table
                :data="tableData"
                style="width: 100%">
                <el-table-column
                  prop="event"
                  :label="$t('schedule.event')">
                  <template v-slot:default="{row}">
                    <span v-if="row.event === 'EXECUTE_SUCCESSFUL'"> {{ $t('schedule.event_success') }}</span>
                    <span v-else-if="row.event === 'EXECUTE_FAILED'"> {{ $t('schedule.event_failed') }}</span>
                    <span v-else>{{ row.event }}</span>
                  </template>
                </el-table-column>
                <el-table-column
                  prop="name"
                  :label="$t('schedule.receiver')"
                  width="240"
                >
                  <template v-slot:default="{row}">
                    <el-select v-model="row.userIds" filterable multiple
                               :placeholder="$t('commons.please_select')"
                               @click.native="userList()" style="width: 100%;">
                      <el-option
                        v-for="item in options"
                        :key="item.id"
                        :label="item.name"
                        :value="item.id">
                      </el-option>
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column
                  prop="type"
                  :label="$t('schedule.receiving_mode')"
                >
                </el-table-column>
                <el-table-column
                  :label="$t('test_resource_pool.enable_disable')"
                  prop="enable"
                >
                  <template v-slot:default="{row}">
                    <el-switch
                      v-model="row.enable"
                      active-value="true"
                      inactive-value="false"
                      inactive-color="#DCDFE6"
                    />
                  </template>
                </el-table-column>
              </el-table>
              <div style="padding-top: 20px;">
                <el-button type="primary" @click="saveNotice">{{ $t('commons.save') }}</el-button>
              </div>
            </template>
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>
  </el-dialog>
</template>

<script>

import Crontab from "../cron/Crontab";
import CrontabResult from "../cron/CrontabResult";
import {cronValidate} from "@/common/js/cron";
import {listenGoBack, removeGoBackListener} from "@/common/js/utils";

function defaultCustomValidate() {
  return {pass: true};
}

export default {
  name: "MsScheduleEdit",
  components: {CrontabResult, Crontab},
  props: {
    testId: String,
    save: Function,
    schedule: {},
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
      }
        // else if(!this.intervalShortValidate()) {
        //   callback(new Error(this.$t('schedule.cron_expression_interval_short_error')));
      // }
      else if (!customValidate.pass) {
        callback(new Error(customValidate.info));
      } else {
        callback();
      }
    };
    return {
      operation: true,
      dialogVisible: false,
      showCron: false,
      form: {
        cronValue: ""
      },
      tableData: [
        {
          event: "EXECUTE_SUCCESSFUL",
          type: "EMAIL",
          userIds: [],
          enable: false
        },
        {
          event: "EXECUTE_FAILED",
          type: "EMAIL",
          userIds: [],
          enable: false
        }
      ],
      options: [{}],
      enable: true,
      type: "",
      activeName: 'first',
      rules: {
        cronValue: [{required: true, validator: validateCron, trigger: 'blur'}],
      }
    }
  },
  methods: {
    userList() {
      this.result = this.$get('user/list', response => {
        this.options = response.data
      })
    },
    handleClick() {
      if (this.activeName === "second") {
        this.result = this.$get('notice/query/' + this.testId, response => {
          if (response.data.length > 0) {
            this.tableData = response.data

            this.tableData[0].event = "EXECUTE_SUCCESSFUL"
            this.tableData[0].type = "EMAIL"
            this.tableData[1].event = "EXECUTE_FAILED"
            this.tableData[1].type = "EMAIL"
          } else {
            this.tableData[0].userIds = []
            this.tableData[1].userIds = []
          }
        })
      }
    },
    buildParam() {
      let param = {};
      param.notices = this.tableData
      param.testId = this.testId
      return param;
    },
    open() {
      this.dialogVisible = true;
      this.form.cronValue = this.schedule.value;
      listenGoBack(this.close);
      this.handleClick()
      this.activeName = 'first'
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
          this.save(this.form.cronValue);
          this.dialogVisible = false;
        } else {
          return false;
        }
      });
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
    }
  }
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

/deep/ .el-select__tags {
  flex-wrap: unset;
  overflow: auto;
}


</style>
