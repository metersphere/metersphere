<template>
  <div v-loading="result.loading">
    <el-card class="setting-card">
      <div class="setting-div">
        <el-card style="height: 100%;width: 100%">
          <div slot="header" style="padding: 5px 0">
            <span style="font-size: 18px;font-weight: bold;">系统数据</span>
          </div>
          <div style="position: absolute; top: 50%; left: 50%;transform: translate(-50%, -50%);">
            <div class="div-item">
              <i class="el-icon-user-solid icon-color"></i>
              系统用户 <span class="number"> {{statistic.userSize}} </span> 人
            </div>
            <div class="div-item">
              <i class="el-icon-s-platform icon-color"></i>
              工作空间 <span class="number"> {{statistic.workspaceSize}} </span> 个
            </div>
            <div class="div-item">
              <i class="el-icon-s-cooperation icon-color"></i>
              项目 <span class="number"> {{statistic.projectSize}} </span> 个
            </div>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>

</template>

<script>
export default {
  name: "SettingHome",
  computed: {
    color: function () {
      return `var(--primary_color)`
    }
  },
  data() {
    return {
      statistic: {
        userSize: 0,
        workspaceSize: 0,
        projectSize: 0
      },
      result: {}
    }
  },
  created() {
    this.result = this.$get("/system/statistics/data", res => {
      this.statistic = res.data;
    })
  }
}
</script>

<style scoped>
.setting-div {
  width: 60vh;
  height: 60vh;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
.setting-card {
  height: calc(100vh - 55px);
  position: relative;
}

.icon-color {
  font-size: 30px;
  color: var(--primary_color);
  margin-right: 6px;
}

.div-item {
  width: 100%;
  padding: 20px;
}
.number {
  font-size: 20px;
  color: var(--primary_color);
}
</style>
