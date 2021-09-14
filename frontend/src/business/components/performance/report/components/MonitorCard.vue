<template>
  <div v-loading="result.loading">
    <el-row>
      <el-col :span="4">
        <div>
          <el-select v-model="currentInstance" placeholder="" size="small" style="width: 100%"
                     @change="handleChecked(currentInstance)">
            <el-option
                v-for="item in instances"
                :key="item"
                :label="item"
                :value="item">
            </el-option>
          </el-select>
        </div>

        <div style="padding-top: 10px">
          <el-checkbox-group v-model="checkList"
                             @change="handleChecked(currentInstance)">
            <div v-for="op in checkOptions"
                 :key="op.key"
                 :content="op.label">
              <el-checkbox :label="op.label"/>
            </div>
          </el-checkbox-group>
        </div>
      </el-col>
      <el-col :span="20">
        <ms-chart ref="chart2" class="chart-config" :options="totalOption" :autoresize="true"></ms-chart>
      </el-col>
    </el-row>
  </div>
</template>

<script>

import MsChart from "@/business/components/common/chart/MsChart";
import {
  getPerformanceMetricQuery,
  getPerformanceMetricQueryResource,
  getSharePerformanceMetricQuery,
  getSharePerformanceMetricQueryResource,
} from "@/network/load-test";

const color = ['#60acfc', '#32d3eb', '#5bc49f', '#feb64d', '#ff7c7c', '#9287e7', '#ca8622', '#bda29a', '#6e7074', '#546570', '#c4ccd3'];

export default {
  name: "MonitorCard",
  props: ['report', 'export', 'isShare', 'shareId', 'planReportTemplate'],
  components: {MsChart},
  data() {
    return {
      activeNames: '0',
      result: {},
      id: '',
      init: false,
      loading: false,
      currentInstance: '',
      instances: [],
      data: [],
      checkList: ['CPU', 'Memory', 'Disk', 'Network In', 'Network Out'],
      checkOptions: [
        {key: 'cpu', label: 'CPU'},
        {key: 'memory', label: 'Memory'},
        {key: 'disk', label: 'Disk'},
        {key: 'netIn', label: 'Network In'},
        {key: 'netOut', label: 'Network Out'}
      ],
      baseOption: {
        color: color,
        grid: {
          // right: '35%' // 动态改这个值
        },
        title: {},
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          },
        },
        legend: {
          y: 'top'
        },
        xAxis: {type: 'category'},
        yAxis: [{
          name: 'Usage(%)',
          type: 'value',
          min: 0,
          max: 100,
        }, {
          type: 'value',
          name: 'kb/s',
          min: 0,
        }],
        dataZoom: [
          {
            type: 'inside',
            start: 0,
            end: 100
          },
          {
            start: 0,
            end: 20
          }
        ],
        series: []
      },
      totalOption: {}
    };
  },
  created() {
    this.data = [];
    this.instances = [];
    this.id = this.$route.path.split('/')[4];
    this.getResource();
  },
  methods: {
    getResource() {
      // this.init = true;
      if (this.planReportTemplate) {
        this.instances = this.planReportTemplate.reportResource;
        this.currentInstance = this.instances[0];
        this.data = this.planReportTemplate.metricData;
        this.totalOption = this.getOption(this.instances[0]);
      } else if (this.isShare) {
        getSharePerformanceMetricQueryResource(this.shareId, this.id).then(response => {
          this.instances = response.data.data;
          this.currentInstance = this.instances[0];
          getSharePerformanceMetricQuery(this.shareId, this.id).then(result => {
            if (result) {
              this.data = result.data.data;
              this.totalOption = this.getOption(this.instances[0]);
            }
          });
        });
      } else {
        getPerformanceMetricQueryResource(this.id).then(response => {
          this.instances = response.data.data;
          this.currentInstance = this.instances[0];
          getPerformanceMetricQuery(this.id).then(result => {
            if (result) {
              this.data = result.data.data;
              this.$nextTick(() => {
                this.totalOption = this.getOption(this.instances[0]);
              });
            }
          });
        });
      }
    },
    handleChecked(id) {
      this.totalOption = {};
      this.$nextTick(() => {
        this.totalOption = this.getOption(id);
      });
    },
    getOption(id) {
      let legend = [];
      let series = [];

      for (const name of this.checkList) {
        let check = this.checkOptions.filter(op => op.label === name)[0].key;
        let yAxisIndex = 1;
        if (check === 'cpu' || check === 'memory' || check === 'disk') {
          yAxisIndex = 0;
        }
        this.data.forEach(d => {
          if (d.instance === id && d.seriesName === check) {
            if (legend.indexOf(name) > -1) {
              return;
            }

            this.baseOption.xAxis.data = d.timestamps;

            let yAxis = d.values.map(v => v.toFixed(2));
            legend.push(name);
            series.push({
              name: name,
              data: yAxis,
              type: 'line',
              yAxisIndex: yAxisIndex,
              smooth: true,
              sampling: 'lttb',
            });
          }
        });
      }
      this.baseOption.legend.data = legend;
      this.baseOption.series = series;
      return this.baseOption;
    },
  },
  watch: {
    '$route'(to) {
      if (to.name === "perReportView") {
        this.id = to.path.split('/')[4];
        this.init = false;
        this.getResource();
      }
    },
    report: {
      handler(val) {
        if (!val.status || !val.id) {
          return;
        }
        let status = val.status;
        this.id = val.id;
        if (this.init) {
          return;
        }
        if (status === "Completed" || status === "Running") {
          this.getResource();
        } else {
          this.instances = [];
        }
      },
      deep: true
    },
    planReportTemplate: {
      handler() {
        if (this.planReportTemplate) {
          this.getResource();
        }
      },
      deep: true
    }
  },
};
</script>

<style scoped>
.chart-config {
  width: 100%;
}

.monitor-detail {
  height: calc(100vh - 375px);
  overflow: auto;
}

/deep/ .el-checkbox__label {
  font-size: 10px !important;
}
</style>
