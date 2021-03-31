<template>
  <el-card class="table-card">
    <template v-slot:header>
      <span class="title">Overview</span>
    </template>
    <el-table border :data="overviewList" class="adjust-table test-content">
      <el-table-column prop="name" :label="$t('commons.name')"/>
      <el-table-column prop="createTime" :label="$t('commons.create_time')">
        <template v-slot:default="scope">
          <span>{{ scope.row.createTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="maxUsers" label="Max Users"/>
      <el-table-column prop="avgTransactions" label="Avg.Transactions"/>
      <el-table-column prop="errors" label="Errors"/>
      <el-table-column prop="avgResponseTime" label="Avg.Response Time"/>
      <el-table-column prop="responseTime90" label="90% Response Time"/>
      <el-table-column prop="avgBandwidth" label="Avg.Bandwidth"/>
    </el-table>
  </el-card>
</template>

<script>
export default {
  name: "OverviewCompareCard",
  data() {
    return {
      reportId: null,
      compareReports: [],
      overviewList: [],
    };
  },
  methods: {
    initTable() {
      this.overviewList = [];

      this.reportId = this.$route.path.split('/')[4];
      this.compareReports = JSON.parse(localStorage.getItem("compareReports"));

      this.compareReports.forEach(report => {
        this.initOverview(report);
      })
    },
    initOverview(report) {
      this.$get("/performance/report/content/testoverview/" + report.id).then(res => {
        let data = res.data.data;
        this.overviewList.push({
          name: report.name,
          createTime: report.createTime,
          maxUsers: data.maxUsers,
          avgThroughput: data.avgThroughput,
          avgTransactions: data.avgTransactions,
          errors: data.errors,
          avgResponseTime: data.avgResponseTime,
          responseTime90: data.responseTime90,
          avgBandwidth: data.avgBandwidth,
        })
      }).catch(() => {
      })
    }
  }
}
</script>

<style scoped>

</style>
