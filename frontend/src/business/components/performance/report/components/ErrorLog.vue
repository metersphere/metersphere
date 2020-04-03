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
      :data="errorTop5"
      border
      stripe
      style="width: 100%"
      show-summary
      :summary-method="getSummaries"
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
        prop="error"
        label="Error"
        width="400"
      >
      </el-table-column>
      <el-table-column
        prop="errors"
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
        tableData: [{},{},{},{},{}],
        errorTotal: {
          label: '',
          totalSamples: '',
          totalErrors: '',
          error1: '',
          error1Size: '',
          error2: '',
          error2Size: '',
          error3: '',
          error3Size: '',
          error4: '',
          error4Size: '',
          error5: '',
          error5Size: ''
        },
        errorTop5: []
      }
    },
    methods: {
      initTableData() {
        this.$get("/report/content/errors/" + this.id, res => {
          this.tableData = res.data;
        })
        this.$get("/report/content/errors_top5/" + this.id, res => {
          this.errorTotal = res.data
          this.errorTop5 = res.data.errorsTop5List;
        })
      },
      getSummaries () {
        const sums = []
        sums[0] = this.errorTotal.label;
        sums[1] = this.errorTotal.totalSamples;
        sums[2] = this.errorTotal.totalErrors;
        sums[3] = this.errorTotal.error1;
        sums[4] = this.errorTotal.error1Size;
        sums[5] = this.errorTotal.error2;
        sums[6] = this.errorTotal.error2Size;
        sums[7] = this.errorTotal.error3;
        sums[8] = this.errorTotal.error3Size;
        sums[9] = this.errorTotal.error4;
        sums[10] = this.errorTotal.error4Size;
        sums[11] = this.errorTotal.error5;
        sums[12] = this.errorTotal.error5Size;
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
  .table-title {
    font-size: 20px;
    color: #8492a6;
    display: block;
    margin-bottom: 8px;
  }
</style>
