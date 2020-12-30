<template>
  <div>
    <el-table
      :data="tableData"
      stripe
      border
      style="width: 100%"
    >
      <el-table-column label="Requests" fixed width="450" align="center">
        <el-table-column
          prop="label"
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
          prop="ko"
          label="KO"
          align="center"
        />

        <el-table-column
          prop="error"
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

      <el-table-column label="Throughput">
        <el-table-column
          prop="transactions"
          label="Transactions/s"
          width="150"
        />
      </el-table-column>

      <el-table-column label="NetWork(KB/sec)" align="center">
        <el-table-column
          prop="received"
          label="Received"
          align="center"
          width="150"
        />
        <el-table-column
          prop="sent"
          label="Sent"
          align="center"
          width="150"
        />
      </el-table-column>

    </el-table>
  </div>
</template>

<script>
export default {
  name: "RequestStatistics",
  data() {
    return {
      tableData: [],
      id: ''
    }
  },
  methods: {
    initTableData() {
      this.$get("/performance/report/content/" + this.id).then(res => {
        this.tableData = res.data.data;
      }).catch(() => {
        this.tableData = [];
      })
    },
  },
  watch: {
    report: {
      handler(val) {
        if (!val.status || !val.id) {
          return;
        }
        let status = val.status;
        this.id = val.id;
        if (status === "Completed" || status === "Running") {
          this.initTableData();
        } else {
          this.tableData = [];
        }
      },
      deep: true
    }
  },
  props: ['report']
}
</script>

<style scoped>

</style>
