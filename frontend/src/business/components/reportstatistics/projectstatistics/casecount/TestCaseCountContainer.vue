<template>
  <div>

    <el-container v-loading="loading" id="reportAnalysis" :style="{ 'max-height': (h-50) + 'px', 'overflow': 'auto'}">
      <el-aside v-if="!isHide" :width="!isHide ?'235px':'0px'" :style="{  'margin-left': '5px'}">
        <history-report-data report-type="TEST_CASE_COUNT"
                             @selectReport="selectReport" @removeHistoryReportId="removeHistoryReportId"
                             ref="historyReport"/>
      </el-aside>
      <el-main class="ms-main" style="padding: 0px 5px 0px">
        <div>
          <test-case-count-chart @hidePage="hidePage" @orderCharts="orderCharts"
                                 ref="analysisChart" @updateChartType="updateChartType"
                                 :chart-width="chartWidth" :load-option="loadOption" :pie-option="pieOption"/>
        </div>
        <div class="ms-row" v-if="!isHide">
          <test-case-count-table :group-name="getGroupNameStr(options.xaxis)" :show-coloums="options.yaxis"
                                 :tableData="tableData"/>
        </div>
      </el-main>
      <el-aside v-if="!isHide" style="height: 100%" :width="!isHide ?'485px':'0px'">
        <test-case-count-filter @filterCharts="filterCharts" ref="countFilter"/>
      </el-aside>
    </el-container>
  </div>
</template>

<script>
import TestCaseCountChart from "./chart/TestCaseCountChart";
import TestCaseCountTable
  from "@/business/components/reportstatistics/projectstatistics/casecount/table/TestCaseCountTable";
import TestCaseCountFilter from "./filter/TestCaseCountFilter";
import {exportPdf, getCurrentProjectID} from "@/common/js/utils";
import html2canvas from 'html2canvas';
import HistoryReportData from "../../base/HistoryReportData";

export default {
  name: "TestCaseCountContainer",
  components: {TestCaseCountChart, TestCaseCountTable, TestCaseCountFilter, HistoryReportData},
  data() {
    return {
      isHide: false,
      loading: false,
      options: {},
      chartWidth: 0,
      tableHeight: 300,
      chartType: "bar",
      loadOption: {
        legend: {},
        xAxis: {},
        yAxis: {},
        label: {},
        tooltip: {},
        series: []
      },
      pieOption: {
        legend: {},
        label: {},
        tooltip: {},
        series: [],
        title: [],
      },

      tableData: [],
      h: document.documentElement.clientHeight - 40,
    };
  },
  methods: {
    updateChartType(value) {
      this.chartType = value;
    },
    handleExport() {
      let name = this.$t('commons.report_statistics.test_case_analysis');
      this.$nextTick(function () {
        setTimeout(() => {
          html2canvas(document.getElementById('reportAnalysis'), {
            scale: 2
          }).then(function (canvas) {
            exportPdf(name, [canvas]);
          });
        }, 1000);
      });
    },
    hidePage(isHide) {
      this.isHide = isHide;
    },
    close() {
      this.$emit('closePage');
    },
    init(opt) {
      this.loading = true;
      this.options = opt;
      this.$post(' /report/test/case/count/getReport', opt, response => {
        let data = response.data.barChartDTO;
        let pieData = response.data.pieChartDTO;
        let selectTableData = response.data.tableDTOs;
        this.initPic(data, pieData, selectTableData);
      }, error => {
        this.loading = false;
      });
    },
    initPic(barData, pieData, selectTableData) {
      this.loading = true;
      this.resetOptions();
      if (barData) {
        this.loadOption.legend = barData.legend;
        this.loadOption.xAxis = barData.xaxis;
        this.loadOption.xaxis = barData.xaxis;
        this.loadOption.series = barData.series;
        this.loadOption.grid = {
          bottom: '75px',//距离下边距
        };
        this.loadOption.series.forEach(item => {
          item.type = this.$refs.analysisChart.chartType;
        });
      }
      if (pieData) {
        this.pieOption.legend = pieData.legend;
        this.pieOption.series = pieData.series;
        this.pieOption.title = pieData.title;
        this.pieOption.grid = {
          bottom: '75px',//距离下边距
        };
        if (pieData.width) {
          this.pieOption.width = pieData.width;
          this.chartWidth = pieData.width;
        }
        this.pieOption.series.forEach(item => {
          item.type = this.$refs.analysisChart.chartType;
        });
      }
      if (selectTableData) {
        this.tableData = selectTableData;
      }
      this.$refs.analysisChart.setPieOptionAndBarOption(this.loadOption,this.pieOption);
      this.loading = false;
      this.$refs.analysisChart.generateOption(this.chartType);
    },
    filterCharts(opt) {
      this.init(opt);
    },
    orderCharts(order) {
      this.options.order = order;
      this.filterCharts(this.options);
    },
    saveReport(reportName) {
      let obj = {};
      obj.name = reportName;
      obj.projectId = getCurrentProjectID();
      obj.selectOption = JSON.stringify(this.options);
      let dataOptionObj = {
        loadOption: this.loadOption,
        pieOption: this.pieOption,
        tableData: this.tableData,
        chartType: this.chartType,
      };
      obj.dataOption = JSON.stringify(dataOptionObj);
      obj.reportType = 'TEST_CASE_COUNT';
      this.$post("/history/report/saveReport", obj, response => {
        this.$alert(this.$t('commons.save_success'));
        this.$refs.historyReport.initReportData();
      });
    },
    selectReport(selectId) {
      if (selectId) {
        this.loading = true;
        let paramObj = {
          id: selectId
        }
        this.$post('/history/report/selectById', paramObj, response => {
          let reportData = response.data;
          if (reportData) {
            if (reportData.dataOption) {
              let dataOptionObj = JSON.parse(reportData.dataOption);
              if (dataOptionObj.chartType) {
                this.chartType = dataOptionObj.chartType;
              } else {
                this.chartType = "bar";
              }
              this.initPic(dataOptionObj.loadOption, dataOptionObj.pieOption, dataOptionObj.tableData);
            }
            if (reportData.selectOption) {
              let selectOptionObj = JSON.parse(reportData.selectOption);
              this.$refs.countFilter.initSelectOption(selectOptionObj);
            }
            this.loading = false;
          }
        }, (error) => {
          this.loading = false;
        });
        this.$emit('initHistoryReportId', selectId);
      }
    },
    removeHistoryReportId() {
      this.$emit('initHistoryReportId', "");
    },
    getGroupNameStr(groupName) {
      if (groupName === 'creator') {
        return this.$t('commons.report_statistics.report_filter.select_options.creator');
      } else if (groupName === 'maintainer') {
        return this.$t('commons.report_statistics.report_filter.select_options.maintainer');
      } else if (groupName === 'casetype') {
        return this.$t('commons.report_statistics.report_filter.select_options.case_type');
      } else if (groupName === 'casestatus') {
        return this.$t('commons.report_statistics.report_filter.select_options.case_status');
      } else if (groupName === 'caselevel') {
        return this.$t('commons.report_statistics.report_filter.select_options.case_level');
      } else {
        return "";
      }
    },
    resetOptions() {
      this.loadOption = {
        legend: {},
        xAxis: {},
        yAxis: {},
        label: {},
        tooltip: {},
        series: []
      };
      this.pieOption = {
        legend: {},
        label: {},
        tooltip: {},
        series: [],
        title: [],
      };
      this.tableData = [];
    },
    selectAndSaveReport(reportName) {
      let opt = this.$refs.countFilter.getOption();
      this.options = opt;
      this.saveReport(reportName);
    },
    saveAndSaveAsReport(reportName, saveType) {
      if (saveType === 'save') {
        this.saveReport(reportName);
      } else if (saveType === 'saveAs') {
        this.selectAndSaveReport(reportName);
      }
    }
  },
};
</script>

<style scoped>
.ms-row {
  padding-top: 5px;
}

/deep/ .el-main {
  padding: 0px 20px 0px;
}
</style>
