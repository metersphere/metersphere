<template>
    <div class="scheduler-config">
      <div>
        <span class="cron-ico">
          <i class="el-icon-date" size="small"></i>
          <span class="character" @click="schedulerEdit">SCHEDULER</span>
        </span>
        <el-switch v-model="isActive"/>
<!--        <el-button class="add-button" type="primary" size="mini" icon="el-icon-plus" plain @click="schedulerEdit"/>-->
        <ms-scheduler-edit ref="schedulerEdit"/>
      </div>
      <div>
        <span :class="{'disable-character': !isActive}"> 下次执行时间：2020-06-19 12:07:09 </span>
      </div>
    </div>
</template>

<script>
    import MsSchedulerEdit from "./MsSchedulerEdit";
    export default {
      name: "MsSchedulerConfig",
      components: {MsSchedulerEdit},
      data() {
        return {
          isActive: false
        }
      },
      methods: {
        schedulerEdit() {
          this.$refs.schedulerEdit.open();
        }
      }
    }
</script>

<style scoped>

  .scheduler-config {
    float: right;
    width: 250px;
    height: 15px;
    line-height: 25px;
  }

  .el-icon-date {
    font-size: 20px;
    margin-left: 5px;
  }

  .character {
    font-weight: bold;
    margin: 0 5px;
  }

  .disable-character {
    color: #cccccc;
  }

  /*.scheduler-config >>> .el-switch__core::after {*/
  /*  height: 12px;*/
  /*  width: 12px;*/
  /*}*/

  /*.scheduler-config >>> .el-switch__core {*/
  /*  height: 15px;*/
  /*}*/

  /*.scheduler-config >>> .el-switch.is-checked .el-switch__core::after {*/
  /*  margin-left: -13px;*/
  /*}*/

  .el-switch {
    margin: 0 5px;
  }

  .cron-ico {
    cursor: pointer;
  }

</style>
