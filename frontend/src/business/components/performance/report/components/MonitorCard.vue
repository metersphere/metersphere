<template>
  <div v-loading="result.loading">
    <el-row>
      <el-col :span="4">
        <div>
          <el-select v-model="currentInstance" placeholder="" size="small" style="width: 100%"
                     @change="getResource(currentInstance)">
            <el-option
              v-for="item in instances"
              :key="item.ip+item.port"
              :value="item.ip+':'+item.port">
              {{ item.ip }} {{ item.name }}
            </el-option>
          </el-select>
        </div>

        <div style="padding-top: 10px">
          <el-checkbox-group v-model="checkList"
                             @change="handleCheckListChange(currentInstance)">
            <div v-for="op in checkOptions"
                 :key="op.key"
                 :content="op.label">
              <el-checkbox :label="op.label"/>
            </div>
          </el-checkbox-group>
        </div>
      </el-col>
      <el-col :span="20">
        <el-row>
          <el-col :span="24">
            <ms-chart v-if="showChart" ref="chart2" class="chart-config" @datazoom="changeDataZoom"
                      :options="totalOption"
                      :autoresize="true"></ms-chart>
          </el-col>
        </el-row>
        <el-row>
          <el-col :offset="2" :span="20">
            <el-table
              :data="tableData"
              stripe
              border
              style="width: 100%">
              <el-table-column label="Label" align="center">
                <el-table-column
                  prop="label"
                  label="Label"
                  sortable>
                </el-table-column>
              </el-table-column>
              <el-table-column label="Aggregate" align="center">
                <el-table-column
                  prop="avg"
                  label="Avg."
                  width="100"
                  sortable
                />
                <el-table-column
                  prop="min"
                  label="Min."
                  width="100"
                  sortable
                />
                <el-table-column
                  prop="max"
                  label="Max."
                  width="100"
                  sortable
                />
              </el-table-column>
              <el-table-column label="Range" align="center">
                <el-table-column
                  prop="startTime"
                  label="Start"
                  width="160"
                />
                <el-table-column
                  prop="endTime"
                  label="End"
                  width="160"
                />
              </el-table-column>
            </el-table>
          </el-col>
        </el-row>
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
const checkList = ['CPU', 'Memory', 'Disk', 'Network In', 'Network Out'];
const checkOptions = [
  {key: 'cpu', label: 'CPU'},
  {key: 'memory', label: 'Memory'},
  {key: 'disk', label: 'Disk'},
  {key: 'netIn', label: 'Network In'},
  {key: 'netOut', label: 'Network Out'}
];

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
      tableData: [],
      checkList: checkList,
      checkOptions: checkOptions,
      showChart: true,
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
      totalOption: {},
      seriesData: [],
    };
  },
  created() {
    this.data = [];
    this.instances = [];
    this.getResource();
  },
  methods: {
    getResource(currentInstance) {
      // this.init = true;
      if (this.planReportTemplate) {
        this.instances = this.planReportTemplate.reportResource;
        this.currentInstance = currentInstance || this.instances[0].ip + ":" + this.instances[0].port;
        this.data = this.planReportTemplate.metricData;
        this.totalOption = this.getOption(this.currentInstance);
      } else if (this.isShare) {
        getSharePerformanceMetricQueryResource(this.shareId, this.id).then(response => {
          this.instances = response.data.data;
          if (currentInstance) {
            this.currentInstance = currentInstance;
          } else {
            this.currentInstance = this.currentInstance || this.instances[0].ip + ":" + this.instances[0].port;
          }
          this.handleChecked(this.currentInstance);
          getSharePerformanceMetricQuery(this.shareId, this.id).then(result => {
            if (result) {
              this.data = result.data.data;
              this.totalOption = this.getOption(this.currentInstance);
              this.changeDataZoom({start: 0, end: 100});
            }
          });
        });
      } else {
        getPerformanceMetricQueryResource(this.id).then(response => {
          this.instances = response.data.data;
          if (currentInstance) {
            this.currentInstance = currentInstance;
            this.showChart = false;
            this.$nextTick(() => {
              this.showChart = true;
            });
          } else {
            this.currentInstance = this.currentInstance || this.instances[0].ip + ":" + this.instances[0].port;
          }
          this.handleChecked(this.currentInstance);
          getPerformanceMetricQuery(this.id).then(result => {
            if (result) {
              this.data = result.data.data;
              this.$nextTick(() => {
                this.totalOption = this.getOption(this.currentInstance);
                this.changeDataZoom({start: 0, end: 100});
              });
            }
          });
        });
      }
    },
    handleChecked(id) {
      let curr = this.instances.filter(instance => id === instance.ip + ":" + instance.port)[0];
      if (curr && curr.monitorConfig) {
        this.checkList = [];
        this.checkOptions = curr.monitorConfig.filter(mc => mc.value && mc.name)
          .map(mc => {
            this.checkList.push(mc.name);
            return {key: mc.name, label: mc.name,};
          });
        if (this.checkList.length === 0) {
          this.checkList = checkList;
          this.checkOptions = checkOptions;
        }
      } else {
        this.checkOptions = checkOptions;
        this.checkList = checkList;
      }
      this.totalOption = {};
      this.$nextTick(() => {
        this.totalOption = this.getOption(id);
        this.changeDataZoom({start: 0, end: 100});
      });
    },
    handleCheckListChange(id) {
      this.totalOption = {};
      this.showChart = false;
      this.$nextTick(() => {
        this.showChart = true;
        this.totalOption = this.getOption(id);
        this.changeDataZoom({start: 0, end: 100});
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
            let data = [];
            for (let i = 0; i < d.timestamps.length; i++) {
              data.push([d.timestamps[i], yAxis[i]]);
            }

            legend.push(name);
            series.push({
              name: name,
              data: data,
              type: 'line',
              yAxisIndex: yAxisIndex,
              smooth: true,
              sampling: 'lttb',
              showSymbol: false,
            });

            this.seriesData = series;
          }
        });
      }
      this.baseOption.legend.data = legend;
      this.baseOption.series = series;
      return this.baseOption;
    },
    changeDataZoom(params) {
      let start = params.start / 100;
      let end = params.end / 100;
      if (params.batch) {
        start = params.batch[0].start / 100;
        end = params.batch[0].end / 100;
      }

      let tableData = [];
      for (let i = 0; i < this.seriesData.length; i++) {
        let sub = this.seriesData[i].data, label = this.seriesData[i].name;
        let len = 0;
        let min, avg, max, sum = 0, startTime, endTime;
        for (let j = 0; j < sub.length; j++) {
          let time = sub[j][0];
          let value = Number.parseFloat(sub[j][1]);
          let index = (j / (sub.length - 1)).toFixed(2);
          if (index < start) {
            continue;
          }
          if (index >= end) {
            endTime = time;
            break;
          }

          if (!startTime) {
            startTime = time;
          }

          if (!min && !max) {
            min = max = value;
          }

          if (min > value) {
            min = value;
          }
          if (max < value) {
            max = value;
          }
          sum += value;

          len++; // 实际 len
        }

        avg = (sum / len).toFixed(2);
        tableData.push({label, min, max, avg, startTime, endTime});
      }
      this.tableData = tableData;
    },
  },
  watch: {
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
