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

    <span class="table-title">Top 5 Errors</span>
    <el-table
      :data="errorSummary"
      border
      stripe
      style="width: 100%"
      show-summary
    >
      <el-table-column
        prop="sample"
        label="Sample"
      >
      </el-table-column>
      <el-table-column
        prop="samples"
        label="#Samples"
      >
      </el-table-column>
      <el-table-column
        prop="errorsAllSize"
        label="All Errors"
      >
      </el-table-column>
    </el-table>

    <span class="table-title">#1 Error</span>
    <el-table
      :data="errorTop1"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column
        prop="error1"
        label="#1 Error"
      >
      </el-table-column>
      <el-table-column
        prop="error1Size"
        label="#1 Errors Count"
      >
      </el-table-column>
    </el-table>

    <span class="table-title">#2 Error</span>
    <el-table
      :data="errorTop2"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column
        prop="error2"
        label="#2 Error"
      >
      </el-table-column>
      <el-table-column
        prop="error2Size"
        label="#2 Errors Count"
      >
      </el-table-column>
    </el-table>

    <span class="table-title">#3 Error</span>
    <el-table
      :data="errorTop3"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column
        prop="error3"
        label="#3 Error"
      >
      </el-table-column>
      <el-table-column
        prop="error3Size"
        label="#3 Errors Count"
      >
      </el-table-column>
    </el-table>

    <span class="table-title">#4 Error</span>
    <el-table
      :data="errorTop4"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column
        prop="error4"
        label="#4 Error"
      >
      </el-table-column>
      <el-table-column
        prop="error4Size"
        label="#4 Errors Count"
      >
      </el-table-column>
    </el-table>

    <span class="table-title">#5 Error</span>
    <el-table
      :data="errorTop5"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column
        prop="error5"
        label="#5 Error"
      >
      </el-table-column>
      <el-table-column
        prop="error5Size"
        label="#5 Errors Count"
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
      errorSummary: [],
      errorTop1: [],
      errorTop2: [],
      errorTop3: [],
      errorTop4: [],
      errorTop5: [],
      id: ''
    };
  },
  methods: {
    initTableData() {
      this.$get("/performance/report/content/errors/" + this.id).then(res => {
        this.tableData = res.data.data;
      }).catch(() => {
        this.tableData = [];
      });
      this.$get("/performance/report/content/errors_top5/" + this.id).then(res => {
        this.errorTop1 = res.data.data.map(e => {
          return {error1: e.error1, error1Size: e.error1Size};
        });
        this.errorTop2 = res.data.data.map(e => {
          return {error2: e.error2, error2Size: e.error2Size};
        });
        this.errorTop3 = res.data.data.map(e => {
          return {error3: e.error3, error3Size: e.error3Size};
        });
        this.errorTop4 = res.data.data.map(e => {
          return {error4: e.error4, error4Size: e.error4Size};
        });
        this.errorTop5 = res.data.data.map(e => {
          return {error5: e.error5, error5Size: e.error5Size};
        });
        this.errorSummary = res.data.data.map(e => {
          return {sample: e.sample, samples: e.samples, errorsAllSize: e.errorsAllSize};
        });
      }).catch(() => {
        this.errorTop1 = [];
        this.errorTop2 = [];
        this.errorTop3 = [];
        this.errorTop4 = [];
        this.errorTop5 = [];
        this.errorSummary = [];
      });
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
    }
  },
  props: ['report']
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
