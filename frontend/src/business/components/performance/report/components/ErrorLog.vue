<template>
  <div>
    <span class="table-title">Errors</span>
    <el-table
      :data="tableData"
      border
      stripe
      style="width: 100%"
      :default-sort = "{prop: 'elementLabel'}"
    >
      <el-table-column
        prop="errorType"
        label="Type of Error"
        sortable>
      </el-table-column>
      <el-table-column
        prop="errorNumber"
        label="Number of errors"
        sortable>
      </el-table-column>
      <el-table-column
        prop="precentOfErrors"
        label="% in errors"
        sortable>
      </el-table-column>
      <el-table-column
        prop="precentOfAllSamples"
        label="% in all samples"
        sortable>
      </el-table-column>
    </el-table>

    <div style="margin-top: 40px;"></div>

    <span class="table-title">Top 5 Errors by sampler </span>
    <el-table
      :data="tableData"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column
        prop="errorType"
        label="Sample"
        width="400"
      >
      </el-table-column>
      <el-table-column
        prop="errorNumber"
        label="#Samples"
        width="120"
      >
      </el-table-column>
      <el-table-column
        prop="#Errors"
        label="#Errors"
        width="100"
      >
      </el-table-column>
      <el-table-column
        prop="Error"
        label="Error"
        width="400"
      >
      </el-table-column>
      <el-table-column
        prop="#Errors"
        label="#Errors"
        width="100"
      >
      </el-table-column>
      <el-table-column
        prop="Error"
        label="Error"
        width="400"
      >
      </el-table-column>
      <el-table-column
        prop="#Errors"
        label="#Errors"
        width="100"
      >
      </el-table-column>
      <el-table-column
        prop="Error"
        label="Error"
        width="400"
      >
      </el-table-column>
      <el-table-column
        prop="#Errors"
        label="#Errors"
        width="100"
      >
      </el-table-column>
      <el-table-column
        prop="Error"
        label="Error"
        width="400"
      >
      </el-table-column>
      <el-table-column
        prop="#Errors"
        label="#Errors"
        width="100"
      >
      </el-table-column>
      <el-table-column
        prop="Error"
        label="Error"
        width="400"
      >
      </el-table-column>
      <el-table-column
        prop="#Errors"
        label="#Errors"
        width="100"
      >
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
  export default {
    name: "ErrorLog",
    data() {
      return {
        tableData: [{},{},{},{},{}]
      }
    },
    methods: {
      initTableData() {
        this.$get("/report/content/errors/" + this.id, res => {
          this.tableData = res.data;
        })
      }
    },
    created() {
      this.initTableData();
    },
    props: ['id'],
    watch: {
      '$route'(to) {
        if (to.name === "perReportView") {
          let reportId = to.path.split('/')[4];
          if(reportId){
            this.$get("/report/content/errors/" + this.id, res => {
              this.tableData = res.data;
            })
          }
        }
      }
    }
  }
</script>

<style scoped>
  .table-title {
    font-size: 20px;
    color: #8492a6;
    display: block;
    margin-bottom: 8px;
  }
</style>
