<template>
  <div>
    <el-table
      :data="tableData"
      border
      style="width: 100%"
      :default-sort = "{prop: 'elementLabel'}"
    >
      <el-table-column
        prop="elementLabel"
        label="Element Label"
        fixed
        width="150"
        sortable>
      </el-table-column>
      <el-table-column
        prop="samples"
        label="Samples"
        width="150"
        sortable>
      </el-table-column>
      <el-table-column
        prop="avgResponseTime"
        label="Avg Response Time(ms)"
        width="220"
        sortable>
      </el-table-column>
      <el-table-column
        prop="avgHits"
        label="Avg Hits/s"
        width="150"
        sortable>
      </el-table-column>
      <el-table-column
        prop="90line"
        label="90% line(ms)"
        width="150"
        sortable>
      </el-table-column>
      <el-table-column
        prop="95line"
        label="95% line(ms)"
        width="150"
        sortable>
      </el-table-column>
      <el-table-column
        prop="99line"
        label="99% line(ms)"
        width="150"
        sortable>
      </el-table-column>
      <el-table-column
        prop="minResponseTime"
        label="Min Response Time(ms)"
        width="220"
        sortable>
      </el-table-column>
      <el-table-column
        prop="maxResponseTime"
        label="Max Response Time(ms)"
        width="220"
        sortable>
      </el-table-column>
      <el-table-column
        prop="avgBandwidth"
        label="Avg Bandwidth(KBytes/s)"
        width="220"
        sortable>
      </el-table-column>
      <el-table-column
        prop="errorPercentage"
        label="Error Percentage"
        width="180"
        fixed="right"
        sortable>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
  export default {
    name: "RequestStatistics",
    data() {
      return {
        tableData: [{},{},{},{},{}]
      }
    },
    methods: {

    }
  }
</script>

<style scoped>

</style>
