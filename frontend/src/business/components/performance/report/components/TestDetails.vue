<template>
  <div>
    <el-row>
      <el-col :span="6">
        <div style="padding-bottom: 5px;">
          <el-link type="primary" @click="resetDefault()">{{ $t('load_test.report.set_default') }}</el-link>
        </div>
        <el-collapse v-model="activeNames" class="test-detail">
          <el-collapse-item name="users">
            <template v-slot:title>
              <div style="width: 100%">
                <span>{{ $t('load_test.report.ActiveThreadsChart') }}</span>
                <span style="float:right;">
                  <el-link type="primary" @click="selectAll( 'ActiveThreadsChart', $event)">
                    {{ $t('load_test.report.select_all') }}
                  </el-link>
                  /
                  <el-link type="default" @click="unselectAll('ActiveThreadsChart', $event)">
                     {{ $t('load_test.report.unselect_all') }}
                  </el-link>
                </span>
              </div>
            </template>
            <el-checkbox-group v-model="checkList['ActiveThreadsChart']"
                               @change="handleChecked('ActiveThreadsChart')">
              <div v-for="name in checkOptions['ActiveThreadsChart']" :key="name">
                <el-tooltip class="item" effect="dark"
                            :content="name"
                            :disabled="name.length < minLength"
                            placement="top">
                  <el-checkbox :label="name"/>
                </el-tooltip>
              </div>
            </el-checkbox-group>
          </el-collapse-item>
          <el-collapse-item name="transactions">
            <template v-slot:title>
              <div style="width: 100%">
                <span>{{ $t('load_test.report.TransactionsChart') }}</span>
                <span style="float:right;">
                  <el-link type="primary" @click="selectAll( 'TransactionsChart', $event)">
                    {{ $t('load_test.report.select_all') }}
                  </el-link>
                  /
                  <el-link type="default" @click="unselectAll('TransactionsChart', $event)">
                     {{ $t('load_test.report.unselect_all') }}
                  </el-link>
                </span>
              </div>
            </template>
            <el-checkbox-group v-model="checkList['TransactionsChart']" @change="handleChecked('TransactionsChart')">
              <div v-for="name in checkOptions['TransactionsChart']"
                   :key="name">
                <el-tooltip class="item" effect="dark"

                            :content="name"
                            :disabled="name.length < minLength"
                            placement="top">
                  <el-checkbox :label="name"/>
                </el-tooltip>
              </div>
            </el-checkbox-group>
          </el-collapse-item>
          <el-collapse-item name="responseTime">
            <template v-slot:title>
              <div style="width: 100%">
                <span>{{ $t('load_test.report.ResponseTimeChart') }}</span>
                <span style="float:right;">
                  <el-link type="primary" @click="selectAll( 'ResponseTimeChart', $event)">
                    {{ $t('load_test.report.select_all') }}
                  </el-link>
                  /
                  <el-link type="default" @click="unselectAll('ResponseTimeChart', $event)">
                     {{ $t('load_test.report.unselect_all') }}
                  </el-link>
                </span>
              </div>
            </template>
            <el-checkbox-group v-model="checkList['ResponseTimeChart']" @change="handleChecked('ResponseTimeChart')">
              <div v-for="name in checkOptions['ResponseTimeChart']"
                   :key="name">
                <el-tooltip class="item" effect="dark"
                            :content="name"
                            :disabled="name.length < minLength"
                            placement="top">
                  <el-checkbox :label="name"/>
                </el-tooltip>
              </div>
            </el-checkbox-group>
          </el-collapse-item>
          <el-collapse-item name="responseTimePercentiles">
            <template v-slot:title>
              <div style="width: 100%">
                <span>{{ $t('load_test.report.ResponseTimePercentilesChart') }}</span>
                <span style="float:right;">
                  <el-link type="primary" @click="selectAll( 'ResponseTimePercentilesChart', $event)">
                    {{ $t('load_test.report.select_all') }}
                  </el-link>
                  /
                  <el-link type="default" @click="unselectAll('ResponseTimePercentilesChart', $event)">
                     {{ $t('load_test.report.unselect_all') }}
                  </el-link>
                </span>
              </div>
            </template>
            <el-checkbox-group v-model="checkList['ResponseTimePercentilesChart']"
                               @change="handleChecked('ResponseTimePercentilesChart')">
              <div v-for="name in checkOptions['ResponseTimePercentilesChart']"
                   :key="name">
                <el-tooltip class="item" effect="dark"
                            :content="name"
                            :disabled="name.length < minLength"
                            placement="top">
                  <el-checkbox :label="name"/>
                </el-tooltip>
              </div>
            </el-checkbox-group>
          </el-collapse-item>
          <el-collapse-item :title="$t('load_test.report.ResponseCodeChart')" name="responseCode">
            <template v-slot:title>
              <div style="width: 100%">
                <span>{{ $t('load_test.report.ResponseCodeChart') }}</span>
                <span style="float:right;">
                  <el-link type="primary" @click="selectAll( 'ResponseCodeChart', $event)">
                    {{ $t('load_test.report.select_all') }}
                  </el-link>
                  /
                  <el-link type="default" @click="unselectAll('ResponseCodeChart', $event)">
                     {{ $t('load_test.report.unselect_all') }}
                  </el-link>
                </span>
              </div>
            </template>
            <el-checkbox-group v-model="checkList['ResponseCodeChart']" @change="handleChecked('ResponseCodeChart')">
              <div v-for="name in checkOptions['ResponseCodeChart']"
                   :key="name">
                <el-tooltip class="item" effect="dark"
                            :content="name"
                            :disabled="name.length < minLength"
                            placement="top">
                  <el-checkbox :label="name"/>
                </el-tooltip>
              </div>
            </el-checkbox-group>
          </el-collapse-item>
          <el-collapse-item :title="$t('load_test.report.LatencyChart')" name="latency">
            <template v-slot:title>
              <div style="width: 100%">
                <span>{{ $t('load_test.report.LatencyChart') }}</span>
                <span style="float:right;">
                  <el-link type="primary" @click="selectAll( 'LatencyChart', $event)">
                    {{ $t('load_test.report.select_all') }}
                  </el-link>
                  /
                  <el-link type="default" @click="unselectAll('LatencyChart', $event)">
                     {{ $t('load_test.report.unselect_all') }}
                  </el-link>
                </span>
              </div>
            </template>
            <el-checkbox-group v-model="checkList['LatencyChart']" @change="handleChecked('LatencyChart')">
              <div v-for="name in checkOptions['LatencyChart']"
                   :key="name">
                <el-tooltip class="item" effect="dark"
                            :content="name"
                            :disabled="name.length < minLength"
                            placement="top">
                  <el-checkbox :label="name"/>
                </el-tooltip>
              </div>
            </el-checkbox-group>
          </el-collapse-item>
          <el-collapse-item :title="$t('load_test.report.BytesThroughputChart')" name="bytes">
            <template v-slot:title>
              <div style="width: 100%">
                <span>{{ $t('load_test.report.BytesThroughputChart') }}</span>
                <span style="float:right;">
                  <el-link type="primary" @click="selectAll( 'BytesThroughputChart', $event)">
                    {{ $t('load_test.report.select_all') }}
                  </el-link>
                  /
                  <el-link type="default" @click="unselectAll('BytesThroughputChart', $event)">
                     {{ $t('load_test.report.unselect_all') }}
                  </el-link>
                </span>
              </div>
            </template>
            <el-checkbox-group v-model="checkList['BytesThroughputChart']"
                               @change="handleChecked('BytesThroughputChart')">
              <div v-for="name in checkOptions['BytesThroughputChart']"
                   :key="name">
                <el-tooltip class="item" effect="dark"
                            :content="name"
                            :disabled="name.length < minLength"
                            placement="top">
                  <el-checkbox :label="name"/>
                </el-tooltip>
              </div>
            </el-checkbox-group>
          </el-collapse-item>
          <el-collapse-item :title="$t('load_test.report.ErrorsChart')" name="errors">
            <template v-slot:title>
              <div style="width: 100%">
                <span>{{ $t('load_test.report.ErrorsChart') }}</span>
                <span style="float:right;">
                  <el-link type="primary" @click="selectAll( 'ErrorsChart', $event)">
                    {{ $t('load_test.report.select_all') }}
                  </el-link>
                  /
                  <el-link type="default" @click="unselectAll('ErrorsChart', $event)">
                     {{ $t('load_test.report.unselect_all') }}
                  </el-link>
                </span>
              </div>
            </template>
            <el-checkbox-group v-model="checkList['ErrorsChart']" @change="handleChecked('ErrorsChart')">
              <div v-for="name in checkOptions['ErrorsChart']"
                   :key="name">
                <el-tooltip class="item" effect="dark"
                            :content="name"
                            :disabled="name.length < minLength"
                            placement="top">
                  <el-checkbox :label="name"/>
                </el-tooltip>
              </div>
            </el-checkbox-group>
          </el-collapse-item>
        </el-collapse>
      </el-col>
      <el-col :span="18" v-loading="result.loading">
        <el-row>
          <el-col :span="24">
            <ms-chart ref="chart2"
                      class="chart-config"
                      :options="totalOption"
                      @datazoom="changeDataZoom"
                      :autoresize="true"/>
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

const color = ['#60acfc', '#32d3eb', '#5bc49f', '#feb64d', '#ff7c7c', '#9287e7', '#ca8622', '#bda29a', '#6e7074', '#546570', '#c4ccd3'];

const groupBy = function (xs, key) {
  return xs.reduce(function (rv, x) {
    (rv[x[key]] = rv[x[key]] || []).push(x);
    return rv;
  }, {});
};

const CHART_MAP = [
  'ActiveThreadsChart',
  'TransactionsChart',
  'ResponseTimeChart',
  'ResponseTimePercentilesChart',
  'ResponseCodeChart',
  'ErrorsChart',
  'LatencyChart',
  'BytesThroughputChart',
];

export default {
  name: "TestDetails",
  components: {MsChart},
  props: ['report', 'export'],
  data() {
    return {
      result: {},
      activeNames: 'users',
      minLength: 35,
      loadOption: {},
      resOption: {},
      totalOption: {},
      responseCodes: [],
      checkList: CHART_MAP.reduce((result, curr) => {
        result[curr] = [];
        return result;
      }, {}),
      checkOptions: {},
      defaultProps: {
        children: 'children',
        label: 'label'
      },
      init: false,
      tableData: [],
      baseOption: {
        color: color,
        grid: {
          // right: '35%' // 动态改这个值
        },
        title: {},
        tooltip: {
          show: true,
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          },
          confine: true,
          formatter: function (params, ticket, callback) {
            let result = "";
            let name = params[0].name;
            result += name + "<br/>";
            for (let i = 0; i < params.length; i++) {
              let seriesName = params[i].seriesName;
              if (seriesName.length > 100) {
                seriesName = seriesName.substring(0, 100);
              }
              let value = params[i].value;
              let marker = params[i].marker;
              result += marker + seriesName + ": " + value[1] + "<br/>";
            }

            return result;
          }
        },
        legend: {
          y: 'top',
        },
        xAxis: {boundaryGap: false},
        yAxis: [],
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
      seriesData: [],
      legend: [],
    };
  },
  methods: {
    resetDefault() {

      this.checkList['ActiveThreadsChart'] = ['ALL'];
      this.checkList['TransactionsChart'] = ['ALL'];
      this.checkList['ResponseTimeChart'] = ['ALL'];
      //
      this.checkList['ResponseTimePercentilesChart'] = [];
      this.checkList['ErrorsChart'] = [];
      this.checkList['LatencyChart'] = [];
      this.checkList['BytesThroughputChart'] = [];

      this.getTotalChart();
    },
    selectAll(name, e) {
      if (e) {
        e.stopPropagation(); // 阻止冒泡
      }
      this.seriesData = [];
      this.totalOption = {};
      this.baseOption.yAxis = [];
      this.legend = [];
      this.checkList[name] = this.checkOptions[name];

      this.getTotalChart();
    },
    unselectAll(name, e) {
      if (e) {
        e.stopPropagation(); // 阻止冒泡
      }
      this.seriesData = [];
      this.totalOption = {};
      this.baseOption.yAxis = [];
      this.legend = [];
      if (name) {
        this.checkList[name] = [];
        this.getTotalChart();
        return;
      }
      for (const name in this.checkList) {
        this.checkList[name] = [];
      }
    },
    handleChecked(name) {
      // let minus = this.checkOptions[name].filter((v) => {
      //   return this.checkList[name].indexOf(v) === -1;
      // })
      // let groupName = this.$t('load_test.report.' + name) + ': ';
      // for (const m of minus) {
      //   this.chartData = this.chartData.filter(c => c.groupName !== groupName + m);
      // }
      // this.totalOption = this.generateOption(this.baseOption, this.chartData);
      // this.getChart(name, this.checkList[name]);
      this.getTotalChart();
    },
    initTableData() {
      this.init = true;

      for (const name of CHART_MAP) {
        this.getCheckOptions(name);
      }

      this.resetDefault();
    },
    getCheckOptions(reportKey) {
      this.$get("/performance/report/content/" + reportKey + "/" + this.id)
        .then(res => {
          let data = res.data.data;
          if (!data || data.length === 0) {
            this.init = false;
            return;
          }
          let yAxisIndex0List = data.filter(m => m.yAxis2 === -1).map(m => m.groupName);
          yAxisIndex0List = this._unique(yAxisIndex0List);
          this.checkOptions[reportKey] = ['ALL'].concat(yAxisIndex0List);
        });
    },
    getTotalChart() {
      this.result.loading = true;

      this.totalOption = {};
      this.seriesData = [];
      this.baseOption.yAxis = [];
      this.legend = [];
      let promises = [];
      for (let name in this.checkList) {
        promises.push(this.getChart(name, this.checkList[name]));
      }
      Promise.all(promises).then((res) => {
        res = res.filter(v => !!v);
        // console.log(res);
        for (let i = 0; i < res.length; i++) {
          if (i === 0) {
            this.baseOption.yAxis.push({
              name: this.$t('load_test.report.' + res[i].reportKey),
              type: 'value',
              min: 0,
              position: 'left',
              boundaryGap: [0, '100%']
            });
          } else {
            this.baseOption.yAxis.push({
              name: this.$t('load_test.report.' + res[i].reportKey),
              type: 'value',
              min: 0,
              position: 'right',
              nameRotate: 20,
              offset: (i - 1) * 50,
              boundaryGap: [0, '100%']
            });
          }
          this.totalOption = this.generateOption(this.baseOption, res[i].data, i);
        }
        this.totalOption.grid.right = (res.length - 1) * 5 + '%';
        this.changeDataZoom({start: 0, end: 100});
        this.result.loading = false;
      }).catch(() => {
        this.result.loading = false;
      });
    },
    getChart(reportKey, checkList) {
      if (!checkList || checkList.length === 0) {
        return;
      }
      return this.$get("/performance/report/content/" + reportKey + "/" + this.id)
        .then(res => {
          let data = res.data.data;
          let allData = [];
          let checkAllOption = checkList.indexOf('ALL') > -1;
          if (checkAllOption) {
            let avgOpt = [
              'ResponseTimeChart',
              'ResponseTimePercentilesChart',
              'LatencyChart',
            ];
            let result = groupBy(data, 'xAxis');
            for (const xAxis in result) {
              let yAxis = result[xAxis].map(a => a.yAxis).reduce((a, b) => a + b, 0);
              if (avgOpt.indexOf(reportKey) > -1) {
                yAxis = yAxis / result[xAxis].length;
              }
              allData.push({
                groupName: 'ALL',
                xAxis: xAxis,
                yAxis: yAxis
              });
            }
          }

          //
          data = data.filter(item => {
            if (checkList.indexOf(item.groupName) > -1) {
              return true;
            }
          });

          // 选中了all
          data = data.concat(allData);


          // prefix
          data.forEach(item => {
            item.groupName = this.$t('load_test.report.' + reportKey) + ': ' + item.groupName;
          });
          return {data, reportKey};
          // if (this.baseOption.yAxis.length === 0) {
          //   this.baseOption.yAxis.push({
          //     name: this.$t('load_test.report.' + reportKey),
          //     type: 'value',
          //     min: 0,
          //     position: 'left',
          //     boundaryGap: [0, '100%']
          //   });
          // } else {
          //   this.baseOption.yAxis.push({
          //     name: this.$t('load_test.report.' + reportKey),
          //     type: 'value',
          //     min: 0,
          //     position: 'right',
          //     nameRotate: 20,
          //     offset: (this.baseOption.yAxis.length - 1) * 50,
          //     boundaryGap: [0, '100%']
          //   });
          //   this.baseOption.grid.right = (this.baseOption.yAxis.length - 1) * 5 + '%';
          // }
          // let yAxisIndex = this.baseOption.yAxis.length - 1;
          // this.totalOption = this.generateOption(this.baseOption, data, yAxisIndex);
        })
        .catch(() => {
          this.totalOption = {};
        });
    },
    generateOption(option, data, yAxisIndex) {
      let chartData = data;
      let series = {}, xAxis = [];
      chartData.forEach(item => {
        if (!xAxis.includes(item.xAxis)) {
          xAxis.push(item.xAxis);
        }
        xAxis.sort();
        let name = item.groupName;
        if (!this.legend.includes(name)) {
          this.legend.push(name);
          series[name] = [];
        }
        series[name].splice(xAxis.indexOf(item.xAxis), 0, [item.xAxis, item.yAxis.toFixed(2)]);
      });
      this.$set(option.legend, "data", this.legend);
      this.$set(option.legend, "type", "scroll");
      this.$set(option.legend, "bottom", "10px");
      this.$set(option.xAxis, "data", xAxis);
      for (let name in series) {
        let d = series[name];
        d.sort((a, b) => a[0].localeCompare(b[0]));
        let items = {
          name: name,
          type: 'line',
          data: d,
          yAxisIndex: yAxisIndex,
          smooth: true,
          sampling: 'lttb',
          animation: !this.export,
        };
        this.seriesData.push(items);
      }
      this.$set(option, "series", this.seriesData);
      return option;
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
    _getChartMax(arr) {
      const max = Math.max(...arr);
      return Math.ceil(max / 4.5) * 5;
    },
    _unique(arr) {
      return Array.from(new Set(arr));
    }
  },
  created() {
    this.id = this.$route.path.split('/')[4];
    this.initTableData();
  },
  watch: {
    '$route'(to) {
      if (to.name === "perReportView") {
        this.id = to.path.split('/')[4];
        this.init = false;
        this.initTableData();
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
        if (status === "Running") {
          this.getTotalChart();
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
  height: 445px;
}

.test-detail {
  height: calc(100vh - 345px);
  overflow: auto;
}

/deep/ .el-checkbox__label {
  font-size: 10px !important;
}
</style>
