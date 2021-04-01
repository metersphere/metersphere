<template>
  <el-card>
    <template v-slot:header>
      <span class="title">Load</span>
    </template>
    <div v-for="(option, index) in loadList" :key="index">
      <ms-chart ref="chart1" :options="option" :autoresize="true"></ms-chart>
    </div>
  </el-card>
</template>

<script>
import MsChart from "@/business/components/common/chart/MsChart";

export default {
  name: "LoadCompareCard",
  components: {MsChart},
  data() {
    return {
      loadList: []
    }
  },
  methods: {
    initCard() {
      this.loadList = [];
      this.reportId = this.$route.path.split('/')[4];
      this.compareReports = JSON.parse(localStorage.getItem("compareReports"));

      this.compareReports.forEach(report => {
        this.initOverview(report);
      })
    },
    initOverview(report) {
      this.$get("/performance/report/content/load_chart/" + report.id).then(res => {
        let data = res.data.data;
        let yAxisList = data.filter(m => m.yAxis2 === -1).map(m => m.yAxis);
        let yAxis2List = data.filter(m => m.yAxis === -1).map(m => m.yAxis2);
        let yAxisListMax = this._getChartMax(yAxisList);
        let yAxis2ListMax = this._getChartMax(yAxis2List);

        let yAxisIndex0List = data.filter(m => m.yAxis2 === -1).map(m => m.groupName);
        yAxisIndex0List = this._unique(yAxisIndex0List);
        let yAxisIndex1List = data.filter(m => m.yAxis === -1).map(m => m.groupName);
        yAxisIndex1List = this._unique(yAxisIndex1List);

        let loadOption = {
          title: {
            text: report.name + " " + this.$options.filters['timestampFormatDate'](report.createTime),
            left: 'center',
            top: 20,
            textStyle: {
              color: '#65A2FF'
            },
          },
          tooltip: {
            show: true,
            trigger: 'axis',
            // extraCssText: 'z-index: 999;',
            confine: true,
          },
          legend: {},
          xAxis: {},
          yAxis: [{
            name: 'User',
            type: 'value',
            min: 0,
            max: yAxisListMax,
            splitNumber: 5,
            interval: yAxisListMax / 5
          },
            {
              name: 'Transactions/s',
              type: 'value',
              splitNumber: 5,
              min: 0,
              max: yAxis2ListMax,
              interval: yAxis2ListMax / 5
            }
          ],
          series: []
        };
        let setting = {
          series: [
            {
              name: 'users',
              color: '#0CA74A',
            },
            {
              name: 'hits',
              yAxisIndex: '1',
              color: '#65A2FF',
            },
            {
              name: 'errors',
              yAxisIndex: '1',
              color: '#E6113C',
            }
          ]
        }
        yAxisIndex0List.forEach(item => {
          setting["series"].splice(0, 0, {name: item, yAxisIndex: '0'})
        })

        yAxisIndex1List.forEach(item => {
          setting["series"].splice(0, 0, {name: item, yAxisIndex: '1'})
        })
        this.loadList.push(this.generateOption(loadOption, data, setting));
      }).catch(() => {
        this.loadList = [];
      })
    },
    generateOption(option, data, setting) {
      let chartData = data;
      let seriesArray = [];
      for (let set in setting) {
        if (set === "series") {
          seriesArray = setting[set];
          continue;
        }
        this.$set(option, set, setting[set]);
      }
      let legend = [], series = {}, xAxis = [], seriesData = [];
      chartData.forEach(item => {
        if (!xAxis.includes(item.xAxis)) {
          xAxis.push(item.xAxis);
        }
        xAxis.sort()
        let name = item.groupName
        if (!legend.includes(name)) {
          legend.push(name)
          series[name] = []
        }
        if (item.yAxis === -1) {
          series[name].splice(xAxis.indexOf(item.xAxis), 0, [item.xAxis, item.yAxis2.toFixed(2)]);
        } else {
          series[name].splice(xAxis.indexOf(item.xAxis), 0, [item.xAxis, item.yAxis.toFixed(2)]);
        }
      })
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
          smooth: true
        };
        let seriesArrayNames = seriesArray.map(m => m.name);
        if (seriesArrayNames.includes(name)) {
          for (let j = 0; j < seriesArray.length; j++) {
            let seriesObj = seriesArray[j];
            if (seriesObj['name'] === name) {
              Object.assign(items, seriesObj);
            }
          }
        }
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
  }
}
</script>

<style scoped>
.echarts {
  width: 100%;
  height: 300px;
}
</style>
