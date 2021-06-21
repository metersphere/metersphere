<template>
  <el-card class="table-card" v-loading="result.loading" body-style="padding:10px;">
    <div slot="header">
      <span class="title">
        {{ $t('test_track.home.case_maintenance') }}
      </span>
    </div>
    <el-container>
      <ms-chart ref="chart1" :options="caseOption" :autoresize="true" style="width: 100%;height: 340px"></ms-chart>
    </el-container>
  </el-card>
</template>

<script>
import MsChart from "@/business/components/common/chart/MsChart";

export default {
  name: "CaseMaintenance",
  components: {MsChart},
  props: {
    caseOption: {}
  },
  data() {
    return {
      result: {}
    }
  }
}
</script>

<style scoped>
.no-shadow-card{
  -webkit-box-shadow: 0 0px 0px 0 rgba(0,0,0,.1);
  box-shadow: 0 0px 0px 0 rgba(0,0,0,.1);
}

.el-card /deep/ .el-card__header {
  border-bottom: 0px solid #EBEEF5;
  padding-bottom: 0px;
}
</style>
