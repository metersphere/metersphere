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
        prop="percentOfErrors"
        label="% in errors"
        sortable>
      </el-table-column>
      <el-table-column
        prop="percentOfAllSamples"
        label="% in all samples"
        sortable>
      </el-table-column>
    </el-table>

    <div style="margin-top: 40px;"></div>

    <span class="table-title">Top 5 Errors by sampler </span>
    <el-table
      :data="errorTop5"
      border
      stripe
      style="width: 100%"
      show-summary
    >
      <el-table-column
        prop="sample"
        label="Sample"
        width="400"
      >
      </el-table-column>
      <el-table-column
        prop="samples"
        label="#Samples"
        width="120"
      >
      </el-table-column>
      <el-table-column
        prop="errorsAllSize"
        label="#Errors"
        width="100"
      >
      </el-table-column>

      <el-table-column
        prop="error1"
        label="Error"
        width="400"
      >
      </el-table-column>
      <el-table-column
        prop="error1Size"
        label="#Errors"
        width="100"
      >
      </el-table-column>
      <el-table-column
        prop="error2"
        label="Error"
        width="400"
      >
      </el-table-column>
      <el-table-column
        prop="error2Size"
        label="#Errors"
        width="100"
      >
      </el-table-column>
      <el-table-column
        prop="error3"
        label="Error"
        width="400"
      >
      </el-table-column>
      <el-table-column
        prop="error3Size"
        label="#Errors"
        width="100"
      >
      </el-table-column>
      <el-table-column
        prop="error4"
        label="Error"
        width="400"
      >
      </el-table-column>
      <el-table-column
        prop="error4Size"
        label="#Errors"
        width="100"
      >
      </el-table-column>

      <el-table-column
        prop="error5"
        label="Error"
        width="400"
      >
      </el-table-column>

      <el-table-column
        prop="error5Size"
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
        tableData: [],
        errorTop5: [],
        id: ''
      }
    },
    methods: {
      initTableData() {
        this.$get("/performance/report/content/errors/" + this.id).then(res => {
          this.tableData = res.data.data;
        }).catch(() => {
          this.tableData = [];
        })
        this.$get("/performance/report/content/errors_top5/" + this.id).then(res => {
          this.errorTop5 = res.data.data;
        }).catch(() => {
          this.errorTop5 = [];
        })
      }
    },
    watch: {
      report: {
        handler(val){
          let status = val.status;
          this.id = val.id;
          if (status === "Completed") {
            this.initTableData();
          } else {
            this.tableData = [];
            this.errorTop5 = [];
          }
        },
        deep:true
      }
    },
    props: ['report']
  }
</script>

<style scoped>
  .table-title {
    font-size: 20px;
    color: #8492a6;
    display: block;
    text-align: center;
    margin-bottom: 8px;
  }
</style>
