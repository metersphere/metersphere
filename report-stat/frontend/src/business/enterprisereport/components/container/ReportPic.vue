<template>
  <div style="width: 100%">
    <div class="report-detail-title">
      <div v-if="!reportDetail.nameIsEdit" style="line-height: 30px">
        <span style="margin-left: 5px">{{ reportDetail.name }}</span>
        <i class="el-icon-edit" @click="editAttachDataName(true)"></i>
        <i class="el-icon-delete" @click="deleteDetail" style="float: right;margin-top: 6px; margin-right: 10px"></i>
      </div>
      <el-input v-else v-model="reportDetail.name" @blur="editAttachDataName(false)"></el-input>
    </div>
    <div class="ms-row">
      <report-chart :report-id="reportDetail.id" :chart-width="pieOption.width" :read-only="true"
                    :need-full-screen="false" :chart-type="chartType"
                    ref="analysisChart" :load-option="loadOption" :pie-option="pieOption"/>
    </div>
    <div class="ms-row">
      <report-table :full-screen="false" :group-name="options.xaxis" :show-coloums="options.yaxis"
                    :tableData="tableData"/>
    </div>
  </div>

</template>

<script>


import ReportChart from "@/business/enterprisereport/components/chart/ReportChart";
import ReportTable from "@/business/enterprisereport/components/chart/ReportTable";

export default {
  name: "ReportPic",
  components: {ReportChart, ReportTable},
  data() {
    return {

      chartType: "bar",
      loading: false,
      options: {},
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
    };
  },
  props: {
    reportDetail: Object,
    reportId: String,
    readOnly: {
      type: Boolean,
      default() {
        return false;
      }
    },
  },
  created() {
    this.initPic();
  },
  methods: {
    deleteDetail() {
      this.$emit("deleteDetail", this.reportDetail);
    },
    editAttachDataName(comman) {
      this.reportDetail.nameIsEdit = !this.reportDetail.nameIsEdit;
    },
    initPic() {
      let barData = this.reportDetail.reportRecordData.loadOption;
      let pieData = this.reportDetail.reportRecordData.pieOption;
      let selectTableData = this.reportDetail.reportRecordData.tableData;
      let selectOption = this.reportDetail.reportRecordData.selectOption;
      if (this.reportDetail.reportRecordData.chartType) {
        if (this.reportDetail.reportRecordData.chartType === "\"bar\"") {
          this.chartType = "bar";
        } else if (this.reportDetail.reportRecordData.chartType === "\"pie\"") {
          this.chartType = "pie";
        } else {
          this.chartType = this.reportDetail.reportRecordData.chartType;
        }
      }
      if (selectOption) {
        this.options = selectOption;
        if (this.options.xaxis) {
          if (this.options.xaxis === 'creator') {
            this.options.xaxis = this.$t('commons.report_statistics.report_filter.select_options.creator');
          } else if (this.options.xaxis === 'maintainer') {
            this.options.xaxis = this.$t('commons.report_statistics.report_filter.select_options.maintainer');
          } else if (this.options.xaxis === 'casetype') {
            this.options.xaxis = this.$t('commons.report_statistics.report_filter.select_options.case_type');
          } else if (this.options.xaxis === 'casestatus') {
            this.options.xaxis = this.$t('commons.report_statistics.report_filter.select_options.case_status');
          } else if (this.options.xaxis === 'caselevel') {
            this.options.xaxis = this.$t('commons.report_statistics.report_filter.select_options.case_level');
          }
        }
      }
      this.loading = true;
      if (barData) {
        this.formatData(barData);
        barData = JSON.parse(JSON.stringify(barData));
        this.loadOption.legend = barData.legend;
        this.loadOption.xAxis = barData.xaxis;
        this.loadOption.xaxis = barData.xaxis;
        this.loadOption.series = barData.series;
        this.loadOption.grid = {
          bottom: '75px',//距离下边距
        };
        this.loadOption.series.forEach(item => {
          item.type = "bar";
        });
      }
      if (pieData) {
        this.formatData(pieData);
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
          item.type = "pie";
        });
      }
      if (selectTableData) {
        this.tableData = selectTableData;
      }
      this.loading = false;
      if (this.$refs.analysisChart) {
        this.$refs.analysisChart.reload();
      }
    },
    formatData(formatData) {
      if (!formatData) {
        return;
      }
      if (typeof (formatData.legend) === 'string') {
        formatData.legend = JSON.parse(formatData.legend);
      }
      if (typeof (formatData.xaxis) === 'string') {
        formatData.xaxis = JSON.parse(formatData.xaxis);
      }
      if (typeof (formatData.series) === 'string') {
        formatData.series = JSON.parse(formatData.series);
      }
      if (typeof (formatData.title) === 'string') {
        formatData.title = JSON.parse(formatData.title);
      }
    },
    initData() {
      this.reportDetail.recordImageContent = this.$refs.analysisChart.getImages("png");
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
  }
}
</script>

<style scoped>
.report-detail-title {
  background-color: #783887;
  font-size: 16px;
  color: white;
  margin: 5px;
}

.remark-item {
  padding: 0px 15px;
}
</style>
