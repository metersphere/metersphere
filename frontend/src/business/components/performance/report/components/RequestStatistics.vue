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

<!--      <el-table-column-->
<!--        prop="avgHits"-->
<!--        label="Avg Hits/s"-->
<!--        width="150"-->
<!--      />-->

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
      }
    },
    methods: {
      initTableData() {
        this.$get("/report/content/" + this.id, res => {
          this.tableData = res.data;
        })
      },
      getSummaries (param) {
        const { data } = param
        const sums = []
        let allSamples = data.reduce(function (total, currentValue) {
          return total + currentValue.samples;
        }, 0);
        let failSize = data.reduce(function (total, currentValue) {
          return total + currentValue.ko;
        }, 0);
        let allAverageTime = data.reduce(function (total, currentValue) {
          return total + parseFloat(currentValue.average) * currentValue.samples;
        }, 0);
        sums[0] = 'Total'
        sums[1] = allSamples;
        sums[2] = (Math.round(failSize / allSamples * 10000) / 100) + '%';
        sums[3] = (allAverageTime / allSamples).toFixed(2);
        sums[4] = Math.min.apply(Math, data.map(function(o) {return parseFloat(o.min)}));
        sums[5] = Math.max.apply(Math, data.map(function(o) {return parseFloat(o.max)}));
        return sums
      }
    },
    created() {
      this.initTableData()
    },
    props: ['id'],
    watch: {
      '$route'(to) {
        if (to.name === "perReportView") {
          let reportId = to.path.split('/')[4];
          if(reportId){
            this.$get("/report/content/" + reportId, res => {
              this.tableData = res.data;
            })
          }
        }
      }
    }
  }
</script>

<style scoped>

</style>
