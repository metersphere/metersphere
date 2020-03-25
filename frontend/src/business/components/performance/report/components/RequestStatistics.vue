<template>
  <div>
    <el-table
      :data="tableData"
      stripe
      border
      style="width: 100%"
      show-summary
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
          fixed="right"/>
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
