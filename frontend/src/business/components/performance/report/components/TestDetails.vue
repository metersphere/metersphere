<template>
  <div>
    <el-row>
      <el-col :span="6">
        <div style="padding-bottom: 5px;">
          <el-link type="primary" @click="resetDefault()">{{ $t('load_test.report.set_default') }}</el-link>
          &nbsp;/&nbsp;
          <el-link type="danger" @click="unselectAll()">{{ $t('load_test.report.unselect_all') }}</el-link>
        </div>
        <el-collapse v-model="activeNames" class="test-detail">
          <el-collapse-item :title="$t('load_test.report.ActiveThreadsChart')" name="users">
            <el-checkbox-group v-model="checkList['ActiveThreadsChart']"
                               @change="handleChecked('ActiveThreadsChart')">
              <el-tooltip class="item" effect="dark"
                          v-for="name in checkOptions['ActiveThreadsChart']"
                          :key="name"
                          :content="name"
                          :disabled="name.length < minLength"
                          placement="top">
                <el-checkbox :label="name"/>
              </el-tooltip>
            </el-checkbox-group>
          </el-collapse-item>
          <el-collapse-item :title="$t('load_test.report.TransactionsChart')" name="transactions">
            <el-checkbox-group v-model="checkList['TransactionsChart']" @change="handleChecked('TransactionsChart')">
              <el-tooltip class="item" effect="dark"
                          v-for="name in checkOptions['TransactionsChart']"
                          :key="name"
                          :content="name"
                          :disabled="name.length < minLength"
                          placement="top">
                <el-checkbox :label="name"/>
              </el-tooltip>
            </el-checkbox-group>
          </el-collapse-item>
          <el-collapse-item :title="$t('load_test.report.ResponseTimeChart')" name="responseTime">
            <el-checkbox-group v-model="checkList['ResponseTimeChart']" @change="handleChecked('ResponseTimeChart')">
              <el-tooltip class="item" effect="dark"
                          v-for="name in checkOptions['ResponseTimeChart']"
                          :key="name"
                          :content="name"
                          :disabled="name.length < minLength"
                          placement="top">
                <el-checkbox :label="name"/>
              </el-tooltip>
            </el-checkbox-group>
          </el-collapse-item>
          <el-collapse-item :title="$t('load_test.report.ResponseTimePercentilesChart')" name="responseTimePercentiles">
            <el-checkbox-group v-model="checkList['ResponseTimePercentilesChart']"
                               @change="handleChecked('ResponseTimePercentilesChart')">
              <el-tooltip class="item" effect="dark"
                          v-for="name in checkOptions['ResponseTimePercentilesChart']"
                          :key="name"
                          :content="name"
                          :disabled="name.length < minLength"
                          placement="top">
                <el-checkbox :label="name"/>
              </el-tooltip>
            </el-checkbox-group>
          </el-collapse-item>
          <el-collapse-item :title="$t('load_test.report.ResponseCodeChart')" name="responseCode">
            <el-checkbox-group v-model="checkList['ResponseCodeChart']" @change="handleChecked('ResponseCodeChart')">
              <el-tooltip class="item" effect="dark"
                          v-for="name in checkOptions['ResponseCodeChart']"
                          :key="name"
                          :content="name"
                          :disabled="name.length < minLength"
                          placement="top">
                <el-checkbox :label="name"/>
              </el-tooltip>
            </el-checkbox-group>
          </el-collapse-item>
          <el-collapse-item :title="$t('load_test.report.LatencyChart')" name="latency">
            <el-checkbox-group v-model="checkList['LatencyChart']" @change="handleChecked('LatencyChart')">
              <el-tooltip class="item" effect="dark"
                          v-for="name in checkOptions['LatencyChart']"
                          :key="name"
                          :content="name"
                          :disabled="name.length < minLength"
                          placement="top">
                <el-checkbox :label="name"/>
              </el-tooltip>
            </el-checkbox-group>
          </el-collapse-item>
          <el-collapse-item :title="$t('load_test.report.BytesThroughputChart')" name="bytes">
            <el-checkbox-group v-model="checkList['BytesThroughputChart']"
                               @change="handleChecked('BytesThroughputChart')">
              <el-tooltip class="item" effect="dark"
                          v-for="name in checkOptions['BytesThroughputChart']"
                          :key="name"
                          :content="name"
                          :disabled="name.length < minLength"
                          placement="top">
                <el-checkbox :label="name"/>
              </el-tooltip>
            </el-checkbox-group>
          </el-collapse-item>
          <el-collapse-item :title="$t('load_test.report.ErrorsChart')" name="errors">
            <el-checkbox-group v-model="checkList['ErrorsChart']" @change="handleChecked('ErrorsChart')">
              <el-tooltip class="item" effect="dark"
                          v-for="name in checkOptions['ErrorsChart']"
                          :key="name"
                          :content="name"
                          :disabled="name.length < minLength"
                          placement="top">
                <el-checkbox :label="name"/>
              </el-tooltip>
            </el-checkbox-group>
          </el-collapse-item>
        </el-collapse>
      </el-col>
      <el-col :span="18">
        <ms-chart ref="chart2" :options="totalOption" class="chart-config" :autoresize="true"></ms-chart>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import MsChart from "@/business/components/common/chart/MsChart";

const color = ['#60acfc', '#32d3eb', '#5bc49f', '#feb64d', '#ff7c7c', '#9287e7', '#ca8622', '#bda29a', '#6e7074', '#546570', '#c4ccd3'];

const CHART_MAP = [
  'ActiveThreadsChart',
  'TransactionsChart',
  'ResponseTimePercentilesChart',
  'ResponseTimeChart',
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
      baseOption: {
        color: color,
        title: {
          text: 'Test Details',
          left: 'center',
          top: 20,
          textStyle: {
            color: '#99743C'
          },
        },
        tooltip: {
          show: true,
          trigger: 'axis',
          // extraCssText: 'z-index: 999;',
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
        legend: {},
        xAxis: {},

        series: []
      },
      chartData: [],
    };
  },
  methods: {
    resetDefault() {
      this.chartData = [];
      this.checkList['ActiveThreadsChart'] = ['ALL'];
      this.checkList['TransactionsChart'] = ['ALL'];
      this.checkList['ResponseTimeChart'] = ['ALL'];
      this.getTotalChart();
    },
    unselectAll() {
      this.chartData = [];
      this.totalOption = {};
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
      this.totalOption = {};
      this.chartData = [];
      for (let name in this.checkList) {
        if (this.checkList[name].length > 0) {
          this.getChart(name, this.checkList[name]);
        }
      }
    },
    getChart(reportKey, checkList) {
      if (!checkList || checkList.length === 0) {
        return;
      }
      this.$get("/performance/report/content/" + reportKey + "/" + this.id)
        .then(res => {
          let data = res.data.data;
          if (checkList) {
            data = data.filter(item => {
              if (checkList.indexOf('ALL') > -1) {
                return true;
              }
              if (checkList.indexOf(item.groupName) > -1) {
                return true;
              }
            });
          }

          // prefix
          data.forEach(item => {
            item.groupName = this.$t('load_test.report.' + reportKey) + ': ' + item.groupName;
          });

          this.chartData = this.chartData.concat(data);

          let yAxisList = data.filter(m => m.yAxis2 === -1).map(m => m.yAxis);
          let yAxisListMax = this._getChartMax(yAxisList);

          this.baseOption.yAxis = [
            {
              name: 'Value',
              type: 'value',
              min: 0,
            }
          ];

          this.totalOption = this.generateOption(this.baseOption, this.chartData);
        })
        .catch(() => {
          this.totalOption = {};
        });
    },
    generateOption(option, data) {
      let chartData = data;
      let legend = [], series = {}, xAxis = [], seriesData = [];
      chartData.forEach(item => {
        if (!xAxis.includes(item.xAxis)) {
          xAxis.push(item.xAxis);
        }
        xAxis.sort();
        let name = item.groupName;
        if (!legend.includes(name)) {
          legend.push(name);
          series[name] = [];
        }
        series[name].splice(xAxis.indexOf(item.xAxis), 0, [item.xAxis, item.yAxis.toFixed(2)]);
      });
      this.$set(option.legend, "data", legend);
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
          smooth: true,
          sampling: 'lttb',
          animation: !this.export,
        };
        seriesData.push(items);
      }
      this.$set(option, "series", seriesData);
      return option;
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
        if (status === "Completed" || status === "Running") {
          this.initTableData();
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
</style>
