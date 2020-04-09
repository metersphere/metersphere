<template>
  <div>
    <el-table
      :data="tableData"
      stripe
      border
      style="width: 100%"
      show-summary
      :summary-method="getSummaries"
      :default-sort = "{prop: 'samples', order: 'descending'}"
    >
      <el-table-column label="Requests" fixed width="450" align="center">
        <el-table-column
          prop="requestLabel"
          label="Label"
          width="450"/>
      </el-table-column>

      <el-table-column label="Executions" align="center">
        <el-table-column
          prop="samples"
          label="Samples"
          sortable
          width="110"
        />

        <el-table-column
          prop="errors"
          label="Error%"
          align="center"
        />
      </el-table-column>

      <el-table-column label="Response Times(ms)" align="center">
        <el-table-column
          prop="average"
          label="Average"
        />
        <el-table-column
          prop="min"
          label="Min"
        />
        <el-table-column
          prop="max"
          label="Max"
        />
        <el-table-column
          prop="tp90"
          label="90% line"
        />
        <el-table-column
          prop="tp95"
          label="95% line"
        />
        <el-table-column
          prop="tp99"
          label="99% line"
        />
      </el-table-column>

      <el-table-column
        prop="avgHits"
        label="Avg Hits/s"
        width="100"
      />

      <el-table-column
        prop="kbPerSec"
        label="Avg Bandwidth(KBytes/s)"
        align="center"
        width="200"
      />
    </el-table>
  </div>
</template>

<script>
  export default {
    name: "RequestStatistics",
    data() {
      return {
        tableData: [{},{},{},{},{}],
        totalInfo: {
          totalLabel: '',
          totalSamples: '',
          totalErrors: '',
          totalAverage: '',
          totalMin: '',
          totalMax: '',
          totalTP90: '',
          totalTP95: '',
          totalTP99: '',
          totalAvgHits: '',
          totalAvgBandwidth: ''
        }
      }
    },
    methods: {
      initTableData() {
        this.$get("/performance/report/content/" + this.id, res => {
          this.tableData = res.data.requestStatisticsList;
          this.totalInfo = res.data;
        })
      },
      getSummaries () {
        const sums = []
        sums[0] = this.totalInfo.totalLabel;
        sums[1] = this.totalInfo.totalSamples;
        sums[2] = this.totalInfo.totalErrors;
        sums[3] = this.totalInfo.totalAverage;
        sums[4] = this.totalInfo.totalMin;
        sums[5] = this.totalInfo.totalMax;
        sums[6] = this.totalInfo.totalTP90;
        sums[7] = this.totalInfo.totalTP95;
        sums[8] = this.totalInfo.totalTP99;
        sums[9] = this.totalInfo.totalAvgHits;
        sums[10] = this.totalInfo.totalAvgBandwidth;
        return sums;
      }
    },
    watch: {
      status() {
        if ("Completed" === this.status) {
          this.initTableData()
          this.getSummaries()
        }
      }
    },
    props: ['id','status']
  }
</script>

<style scoped>

</style>
