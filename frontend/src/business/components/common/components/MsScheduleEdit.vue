<template>
  <el-dialog width="30%" class="schedule-edit" :title="'编辑定时任务'" :visible.sync="dialogVisible"  @close="close">
    <div id="app">
      <el-form :model="form" :rules="rules" ref="from">
        <el-form-item
          :placeholder="'请输入 Cron 表达式'"
          prop="cronValue">
          <el-input v-model="form.cronValue" placeholder class="inp"/>
          <el-button type="primary" @click="showCronDialog">生成 Cron</el-button>
          <el-button type="primary" @click="saveCron">保存</el-button>
        </el-form-item>
        <crontab-result :ex="form.cronValue" ref="crontabResult" />
      </el-form>
      <el-dialog title="生成 cron" :visible.sync="showCron" :modal="false">
        <crontab @hide="showCron=false" @fill="crontabFill" :expression="schedule.value" ref="crontab"/>
      </el-dialog>
    </div>
  </el-dialog>
</template>

<script>

    import Crontab from "../cron/Crontab";
    import CrontabResult from "../cron/CrontabResult";
    import {cronValidate} from "../../../../common/js/cron";

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
      },
      watch: {
        'schedule.value'() {
          this.form.cronValue = this.schedule.value;
        }
      },
      data() {
          const validateCron = (rule, cronValue, callback) => {
            let customValidate = this.customValidate(this.getIntervalTime());
            if (!cronValidate(cronValue)) {
              callback(new Error('Cron 表达式格式错误'));
            } else if(!this.intervalShortValidate()) {
              callback(new Error('间隔时间请大于 5 分钟'));
            } else if (!customValidate.pass){
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
        },
        intervalShortValidate() {
          if (this.getIntervalTime() < 5*60*1000) {
            return false;
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

</style>
