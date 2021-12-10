<template>
  <div>
    <span class="table-title">Errors</span>
    <el-table
      :data="tableData"
      border
      stripe
      style="width: 100%"
      :default-sort="{prop: 'elementLabel'}"
    >
      <el-table-column
        prop="errorType"
        label="Type of error"
        sortable>
      </el-table-column>
      <el-table-column
        width="200"
        prop="errorNumber"
        label="Number of errors"
        sortable>
      </el-table-column>
      <el-table-column
        width="200"
        prop="percentOfErrors"
        label="% in errors"
        sortable>
      </el-table-column>
      <el-table-column
        width="200"
        prop="percentOfAllSamples"
        label="% in all samples"
        sortable>
      </el-table-column>
    </el-table>

    <span class="table-title">Top 5 Errors</span>
    <el-table
      :data="errorSummary"
      border
      stripe
      style="width: 100%"
      show-summary
    >
      <el-table-column prop="sample" label="Sample"/>
      <el-table-column prop="samples" label="#Samples"/>
      <el-table-column prop="errorsAllSize" label="All Errors"/>
    </el-table>

    <span class="table-title">#1 Error</span>
    <el-table
      :data="errorTop1"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column prop="sample" label="Sample"/>
      <el-table-column prop="error1" label="#1 Error"/>
      <el-table-column prop="error1Size" label="#1 Errors Count" width="200"/>
    </el-table>

    <span class="table-title">#2 Error</span>
    <el-table
      :data="errorTop2"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column prop="sample" label="Sample"/>
      <el-table-column prop="error2" label="#2 Error"/>
      <el-table-column prop="error2Size" label="#2 Errors Count" width="200"/>
    </el-table>

    <span class="table-title">#3 Error</span>
    <el-table
      :data="errorTop3"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column prop="sample" label="Sample"/>
      <el-table-column prop="error3" label="#3 Error"/>
      <el-table-column prop="error3Size" label="#3 Errors Count" width="200"/>
    </el-table>

    <span class="table-title">#4 Error</span>
    <el-table
      :data="errorTop4"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column prop="sample" label="Sample"/>
      <el-table-column prop="error4" label="#4 Error"/>
      <el-table-column prop="error4Size" label="#4 Errors Count" width="200"/>
    </el-table>

    <span class="table-title">#5 Error</span>
    <el-table
      :data="errorTop5"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column prop="sample" label="Sample"/>
      <el-table-column prop="error5" label="#5 Error"/>
      <el-table-column prop="error5Size" label="#5 Errors Count" width="200"/>
    </el-table>
  </div>
</template>

<script>
import {
  getPerformanceReportErrorsContent,
  getPerformanceReportErrorsTop5,
  getSharePerformanceReportErrorsContent,
  getSharePerformanceReportErrorsTop5
} from "@/network/load-test";

export default {
  name: "ErrorLog",
  data() {
    return {
      tableData: [],
      errorSummary: [],
      errorTop1: [],
      errorTop2: [],
      errorTop3: [],
      errorTop4: [],
      errorTop5: [],
      id: ''
    };
  },
  props: ['report', 'isShare', 'shareId', 'planReportTemplate'],
  methods: {
    initTableData() {
      if (this.planReportTemplate) {
        this.tableData = this.planReportTemplate.reportErrors;
        this.handleGetTop5(this.planReportTemplate.reportErrorsTop5);
      } else if (this.isShare) {
        getSharePerformanceReportErrorsContent(this.shareId, this.id).then(res => {
          this.tableData = res.data.data;
        });

        getSharePerformanceReportErrorsTop5(this.shareId, this.id).then(res => {
          this.handleGetTop5(res.data.data);
        });
      } else {
        getPerformanceReportErrorsContent(this.id).then(res => {
          this.tableData = res.data.data;
        });

        getPerformanceReportErrorsTop5(this.id).then(res => {
          this.handleGetTop5(res.data.data);
        });
      }
    },
    handleGetTop5(data) {
      if (!data) {
        return;
      }
      this.errorTop1 = data
        .map(e => {
          return {sample: e.sample, error1: e.error1, error1Size: e.error1Size};
        })
        .filter(e => e.error1Size > 0);

      this.errorTop2 = data
        .map(e => {
          return {sample: e.sample, error2: e.error2, error2Size: e.error2Size};
        })
        .filter(e => e.error2Size > 0);

      this.errorTop3 = data
        .map(e => {
          return {sample: e.sample, error3: e.error3, error3Size: e.error3Size};
        })
        .filter(e => e.error3Size > 0);

      this.errorTop4 = data
        .map(e => {
          return {sample: e.sample, error4: e.error4, error4Size: e.error4Size};
        })
        .filter(e => e.error4Size > 0);

      this.errorTop5 = data
        .map(e => {
          return {sample: e.sample, error5: e.error5, error5Size: e.error5Size};
        })
        .filter(e => e.error5Size > 0);

      this.errorSummary = data.map(e => {
        return {sample: e.sample, samples: e.samples, errorsAllSize: e.errorsAllSize};
      });
    },
    initData() {
      this.tableData = [];
      this.errorTop1 = [];
      this.errorTop2 = [];
      this.errorTop3 = [];
      this.errorTop4 = [];
      this.errorTop5 = [];
      this.errorSummary = [];
    }
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
          this.errorTop1 = [];
          this.errorTop2 = [];
          this.errorTop3 = [];
          this.errorTop4 = [];
          this.errorTop5 = [];
          this.errorSummary = [];
        }
      },
      deep: true
    },
    planReportTemplate: {
      handler() {
        if (this.planReportTemplate) {
          this.initTableData();
        }
      },
      deep: true
    }
  },
};
</script>

<style scoped>
.table-title {
  font-size: 20px;
  color: #8492a6;
  display: block;
  text-align: center;
  margin-bottom: 8px;
  margin-top: 40px;
}
</style>
