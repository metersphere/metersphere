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
        <crontab-result :ex="schedule.value" ref="crontabResult"/>
      </el-form>
      <el-dialog title="生成 cron" :visible.sync="showCron" :modal="false">
        <crontab @hide="showCron=false" @fill="crontabFill" :expression="schedule.value"/>
      </el-dialog>
    </div>
  </el-dialog>
</template>

<script>

    import Crontab from "../cron/Crontab";
    import CrontabResult from "../cron/CrontabResult";
    import {cronValidate} from "../../../../common/js/cron";

    export default {
      name: "MsScheduleEdit",
      components: {CrontabResult, Crontab},
      props: {
        save: Function,
        schedule: {},
      },
      watch: {
        'schedule.value'() {
          this.form.cronValue = this.schedule.value;
        }
      },
      data() {
          const validateCron = (rule, cronValue, callback) => {
            if (!cronValidate(cronValue)) {
              callback(new Error('Cron 表达式格式错误'));
            } else {
              this.schedule.value = cronValue;
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
        },
        crontabFill(value) {
          //确定后回传的值
          this.schedule.value = value;
          this.form.cronValue = value;
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
          this.$refs['from'].resetFields();
          this.$refs.crontabResult.resultList = [];
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
