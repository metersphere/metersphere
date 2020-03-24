<template>
  <div>
    <el-table
      :data="tableData"
      border
      style="width: 100%"
      :default-sort = "{prop: 'samples'}"
    >
      <el-table-column
        prop="requestLabel"
        label="Element Label"
        fixed
        width="450"
      >
      </el-table-column>
      <el-table-column
        prop="samples"
        label="Samples"
        width="150"
        sortable>
      </el-table-column>
      <el-table-column
        prop="average"
        label="Avg Response Time(ms)"
        width="220"
      >
      </el-table-column>
      <el-table-column
        prop="avgHits"
        label="Avg Hits/s"
        width="150"
      >
      </el-table-column>
      <el-table-column
        prop="tp90"
        label="90% line(ms)"
        width="150"
      >
      </el-table-column>
      <el-table-column
        prop="tp95"
        label="95% line(ms)"
        width="150"
      >
      </el-table-column>
      <el-table-column
        prop="tp99"
        label="99% line(ms)"
        width="150"
      >
      </el-table-column>
      <el-table-column
        prop="min"
        label="Min Response Time(ms)"
        width="220"
      >
      </el-table-column>
      <el-table-column
        prop="max"
        label="Max Response Time(ms)"
        width="220"
      >
      </el-table-column>
      <el-table-column
        prop="kbPerSec"
        label="Avg Bandwidth(KBytes/s)"
        width="220"
      >
      </el-table-column>
      <el-table-column
        prop="errors"
        label="Error Percentage"
        width="180"
        fixed="right"
      >
      </el-table-column>
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
      }
    },
    created() {
      this.initTableData()
    },
    props: ['id'],
    watch: {
      '$route'(to) {
        let reportId = to.path.split('/')[4];
        if(reportId){
          this.$get("/report/content/" + reportId, res => {
            this.tableData = res.data;
          })
        }
      }
    }
  }
</script>

<style scoped>

</style>
