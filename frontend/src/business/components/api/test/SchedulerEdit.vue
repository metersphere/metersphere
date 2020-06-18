<template>
  <el-dialog :title="'编辑定时任务'" :visible.sync="dialogVisible">
    <div id="app">
      <div class="box">
        <el-input v-model="input" placeholder class="inp"></el-input>
        <el-button type="primary" @click="showDialog">生成 cron</el-button>
      </div>
      <el-dialog title="生成 cron" :visible.sync="showCron">
<!--        <vcrontab @hide="showCron=false" @fill="crontabFill" :expression="expression"/>-->
      </el-dialog>
    </div>
  </el-dialog>
</template>

<script>
  // import vcrontab from "../../common/cron/Crontab";

    export default {
      name: "SchedulerEdit",
      // comments: {vcrontab},
      data() {
          return {
            dialogVisible: false,
            input: "",
            expression: "",
            showCron: false
          }
      },
      methods: {
        open() {
          this.dialogVisible = true;
        },
        crontabFill(value) {
          //确定后回传的值
          this.input = value;
        },
        showDialog() {
          this.expression = this.input;//传入的 cron 表达式，可以反解析到 UI 上
          this.showCron = true;
        }
      }
    }
</script>

<style scoped>

</style>
