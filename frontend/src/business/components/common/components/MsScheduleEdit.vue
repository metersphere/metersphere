<template>
  <el-dialog width="35%" class="schedule-edit" :title="$t('schedule.edit_timer_task')" :visible.sync="dialogVisible"  @close="close">
    <div id="app">
      <el-form :model="form" :rules="rules" ref="from">
        <el-form-item
          prop="cronValue">
          <el-input :disabled="isReadOnly" v-model="form.cronValue" class="inp" :placeholder="$t('schedule.please_input_cron_expression')"/>
<!--          <el-button type="primary" @click="showCronDialog">{{$t('schedule.generate_expression')}}</el-button>-->
          <el-button :disabled="isReadOnly" type="primary" @click="saveCron">{{$t('commons.save')}}</el-button>
        </el-form-item>
        <el-form-item>
          <el-link :disabled="isReadOnly" type="primary" @click="showCronDialog">{{$t('schedule.generate_expression')}}</el-link>
        </el-form-item>
        <crontab-result :ex="form.cronValue" ref="crontabResult" />
      </el-form>
      <el-dialog :title="$t('schedule.generate_expression')" :visible.sync="showCron" :modal="false">
        <crontab @hide="showCron=false" @fill="crontabFill" :expression="schedule.value" ref="crontab"/>
      </el-dialog>
    </div>
  </el-dialog>
</template>

<script>

    import Crontab from "../cron/Crontab";
    import CrontabResult from "../cron/CrontabResult";
    import {cronValidate} from "../../../../common/js/cron";
    import {listenGoBack, removeGoBackListener} from "../../../../common/js/utils";

    function defaultCustomValidate() {return {pass: true};}

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
            else if (!customValidate.pass){
              callback(new Error(customValidate.info));
            } else {
              callback();
            }
          };
          return {
            dialogVisible: false,
            showCron: false,
            form: {
              cronValue: ""
            },
            rules: {
              cronValue :[{required: true, validator: validateCron, trigger: 'blur'}],
            }
          }
      },
      methods: {
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
        saveCron () {
          this.$refs['from'].validate((valid) => {
            if (valid) {
              this.intervalShortValidate();
              this.save(this.form.cronValue);
              this.dialogVisible = false;
            } else  {
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
          if (this.getIntervalTime() < 3*60*1000) {
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
