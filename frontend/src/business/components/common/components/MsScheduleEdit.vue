<template>
  <el-dialog :close-on-click-modal="false" width="50%" class="schedule-edit" :visible.sync="dialogVisible"
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
                <el-button :disabled="isReadOnly" type="primary" @click="saveCron">{{$t('commons.save')}}</el-button>
              </el-form-item>
              <el-form-item>
                <el-link :disabled="isReadOnly" type="primary" @click="showCronDialog">
                  {{$t('schedule.generate_expression')}}
                </el-link>
              </el-form-item>
              <crontab-result :ex="form.cronValue" ref="crontabResult"/>
            </el-form>
            <el-dialog :title="$t('schedule.generate_expression')" :visible.sync="showCron" :modal="false">
              <crontab @hide="showCron=false" @fill="crontabFill" :expression="schedule.value" ref="crontab"/>
            </el-dialog>
          </el-tab-pane>
          <el-tab-pane :label="$t('schedule.task_notification')" name="second">
            <template>
              <el-select v-model="value" :placeholder="$t('commons.please_select')">
                <el-option
                  v-for="item in options"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
              <el-table
                :data="tableData"
                style="width: 100%">
                <el-table-column
                  prop="receiver"
                  :label="$t('schedule.receiver')"
                >
                  <template v-slot:default="{row}">
                    <el-input
                      size="mini"
                      type="textarea"
                      :rows="1"
                      class="edit-input"
                      v-model="row.receiver"
                      :placeholder="$t('schedule.receiver')"
                      clearable>
                    </el-input>
                  </template>
                </el-table-column>
                <el-table-column
                  prop="email"
                  :label="$t('schedule.receiving_mode')"
                  width="200"
                  >
                  <template v-slot:default="{row}">
                    <el-input
                      size="mini"
                      type="textarea"
                      :rows="1"
                      class="edit-input"
                      v-model="row.email"
                      :placeholder="$t('schedule.input_email')"
                      clearable>
                    </el-input>
                  </template>
                </el-table-column>
                <el-table-column
                  align="center"
                  prop="enable"
                  :label="$t('test_resource_pool.enable_disable')"
                  >
                  <template slot-scope="scope">
                    <el-switch
                      v-model="scope.row.enable"
                      :active-value="true"
                      :inactive-value="false"
                      active-color="#13ce66"
                      inactive-color="#ff4949"
                    />
                  </template>
                </el-table-column>
                <el-table-column :label="$t('schedule.operation')">
                  <template v-slot:default="scope">
                    <el-button
                      type="primary"
                      icon="el-icon-plus"
                      circle size="mini"
                      @click="handleAddStep(scope.$index)"></el-button>
                    <el-button
                      type="danger"
                      icon="el-icon-delete"
                      circle size="mini"
                      @click="handleDeleteStep(scope.$index)"></el-button>
                  </template>
                </el-table-column>
              </el-table>
                <el-button  type="primary" @click="saveNotice">{{$t('commons.save')}}</el-button>
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
      save: Function,
      schedule: {},
      customValidate: {
        type: Function,
        default: defaultCustomValidate
      },
      isReadOnly: {
        type: Boolean,
        default: false
      }
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
        options: [{
          value: 'success',
          label: '执行成功通知'
        }, {
          value: 'fail',
          label: '执行失败通知'
        }, {
          value: 'all',
          label: '全部通知'
        }],
        value: '',
        tableData: [
          {
            receiver: "",
            email: "",
            enable: false
          }
        ],
        enable: false,
        email: "",
        activeName: 'first',
        rules: {
          cronValue: [{required: true, validator: validateCron, trigger: 'blur'}],
        }
      }
    },
    methods: {
      handleClick() {

      },
      saveNotice(){
        let param = this.buildParam();
        /* this.result=this.$post("notice/save",param,()=>{

          })*/

      },
      buildParam() {
        let param = {};
        param.form = this.tableData
        param.testId = ""
        param.event = this.value
        return param;
      },
      handleAddStep(index) {
        let form = {}
        form.receiver = null;
        form.email = null;
        form.enable = null
        this.tableData.splice(index + 1, 0, form);
      },
      handleDeleteStep(index) {
        this.tableData.splice(index, 1);
      },
      open() {
        this.dialogVisible = true;
        this.form.cronValue = this.schedule.value;
        listenGoBack(this.close);
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

</style>
