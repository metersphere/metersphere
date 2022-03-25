<template>
  <report-chart v-if="!needReloading" :read-only="true" :need-full-screen="false" :chart-type="dataOption.chartType"
                ref="analysisChart" :load-option="dataOption.loadOption" :pie-option="dataOption.pieOption"/>
</template>

<script>
import ReportChart from "@/template/enterprise/share/ReportChart";
import {getShareId} from "@/common/js/utils";

export default {
  name: "ShareEnterpriseReportTemplate",
  components: {ReportChart},
  data() {
    return {
      needReloading: false,
      shareId: '',
      dataOption: {
        chartType: '',
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
      },
    }
  },
  created() {
    this.initEchartData();
  },
  methods: {
    initEchartData() {
      this.shareId = getShareId();
      let paramObj = {
        id: this.shareId
      };
      this.resetOptions();
      this.$post('/share/info/selectHistoryReportById', paramObj, response => {
        let reportData = response.data;
        if (reportData) {
          let selectOption = JSON.parse(reportData.selectOption);
          let data = JSON.parse(reportData.dataOption);
          data.selectOption = selectOption;
          this.dataOption = data;
          this.reloadChart();
        }
      }, (error) => {
        this.$error(this.$t('查找报告失败!'));
        return false;
      });
    },
    initPic(loadOptionParam, tableData) {
      this.loading = true;
      if (loadOptionParam) {
        this.loadOption.legend = loadOptionParam.legend;
        this.loadOption.xAxis = loadOptionParam.xaxis;
        this.loadOption.series = loadOptionParam.series;
        this.loadOption.grid = {
          bottom: '75px',//距离下边距
        }
        this.loadOption.series.forEach(item => {
          item.type = this.$refs.analysisChart.chartType;
        })
      }
      if (tableData) {
        this.tableData = tableData;
      }
      this.loading = false;
    },
    reloadChart() {
      console.info("load data over, reload compnents.");
      this.$refs.analysisChart.reload();
    },
    resetOptions() {
      this.dataOption = {
        chartType: '',
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
      };
    },
  }
}
</script>

<style scoped>
</style>
