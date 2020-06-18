<template>
    <div class="scheduler-config">
      <i class="el-icon-date" size="small"></i>
      <span class="character">SCHEDULER</span>
      <el-button type="primary" size="mini" icon="el-icon-plus" plain @click="schedulerEdit"/>
      <scheduler-edit ref="schedulerEdit"/>
    </div>
</template>

<script>
    import SchedulerEdit from "./SchedulerEdit";
    export default {
      name: "SchedulerConfig",
      components: {SchedulerEdit},
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
    background: #44b349;
    width: 180px;
    height: 28px;
    line-height: 28px;
  }

  .el-col {
    line-height: 28px;
    font-weight: bold;
  }

  .el-icon-date {
    font-size: 20px;
    margin: 5px;
  }

  .character {
    font-weight: bold;
    margin: 0 5px;
  }

</style>
