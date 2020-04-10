<template>
  <div>
    <el-row :gutter="12">
      <el-col :span="4">
        <el-card shadow="always" class="ms-card-index-1">
          <span class="ms-card-data">
            <span class="ms-card-data-digital">{{maxUsers}}</span>
            <span class="ms-card-data-unit"> VU</span>
          </span>
          <span class="ms-card-desc">Max Users</span>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="always" class="ms-card-index-2">
          <span class="ms-card-data">
            <span class="ms-card-data-digital">{{avgThroughput}}</span>
            <span class="ms-card-data-unit"> Hits/s</span>
          </span>
          <span class="ms-card-desc">Avg.Throughput</span>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="always" class="ms-card-index-3">
          <span class="ms-card-data">
            <span class="ms-card-data-digital">{{errors}}</span>
            <span class="ms-card-data-unit"> %</span>
          </span>
          <span class="ms-card-desc">Errors</span>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="always" class="ms-card-index-4">
          <span class="ms-card-data">
            <span class="ms-card-data-digital">{{avgResponseTime}}</span>
            <span class="ms-card-data-unit"> s</span>
          </span>
          <span class="ms-card-desc">Avg.Response Time</span>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="always" class="ms-card-index-5">
          <span class="ms-card-data">
            <span class="ms-card-data-digital">{{responseTime90}}</span>
            <span class="ms-card-data-unit"> s</span>
          </span>
          <span class="ms-card-desc">90% Response Time</span>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="always" class="ms-card-index-6">
          <span class="ms-card-data">
            <span class="ms-card-data-digital">{{avgBandwidth}}</span>
            <span class="ms-card-data-unit"> KiB/s</span>
          </span>
          <span class="ms-card-desc">Avg.Bandwidth</span>
        </el-card>
      </el-col>
    </el-row>

    <el-row>
      <el-col :span="12">
        <chart ref="chart1" :options="loadOption" class="chart-config" :autoresize="true"></chart>
      </el-col>
      <el-col :span="12">
        <chart ref="chart2" :options="resOption" class="chart-config" :autoresize="true"></chart>
      </el-col>
    </el-row>
  </div>
</template>

<script>
  export default {
    name: "TestOverview",
    data() {
      return {
        maxUsers: "0",
        avgThroughput: "0",
        errors: "0",
        avgResponseTime: "0",
        responseTime90: "0",
        avgBandwidth: "0",
        loadOption: {},
        resOption: {}
      }
    },
    methods: {
      initTableData() {
        this.$get("/performance/report/content/testoverview/" + this.id, res => {
          let data = res.data;
          this.maxUsers = data.maxUsers;
          this.avgThroughput = data.avgThroughput;
          this.errors = data.errors;
          this.avgResponseTime = data.avgResponseTime;
          this.responseTime90 = data.responseTime90;
          this.avgBandwidth = data.avgBandwidth;
        })
        this.$get("/performance/report/content/load_chart/" + this.id, res => {
          let data = res.data;
          let loadOption = {
            title: {
              text: 'Load',
              left: 'center',
              top: 20,
              textStyle: {
                color: '#65A2FF'
              },
            },
            legend: {},
            xAxis: {},
            yAxis: [{
              name: 'User',
              type: 'value',
              min: 0,
              splitNumber: 5,
              // interval: 10 / 5
            },
              {
                name: 'Hits/s',
                type: 'value',
                splitNumber: 5,
                min: 0,
                // max: 5,
                // interval: 5 / 5
              }
            ],
            series: []
          }
          this.loadOption = this.generateOption(loadOption, data);
        })
        this.$get("/performance/report/content/res_chart/" + this.id, res => {
          let data = res.data;
          let resOption = {
            title: {
              text: 'Response Time',
              left: 'center',
              top: 20,
              textStyle: {
                color: '#99743C'
              },
            },
            legend: {},
            xAxis: {},
            yAxis: [{
              name: 'User',
              type: 'value',
              splitNumber: 5,
              min: 0
            },
              {
                name: 'Response Time',
                type: 'value',
                splitNumber: 5,
                min: 0
              }
            ],
            series: []
          }
          this.resOption = this.generateOption(resOption, data);
        })
      },
      generateOption(option, data) {
        let chartData = data;
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
          series[name].splice(xAxis.indexOf(item.xAxis), 0, item.yAxis.toFixed(2));
        })
        this.$set(option.legend, "data", legend);
        this.$set(option.legend, "bottom", 10);
        this.$set(option.xAxis, "data", xAxis);
        for (let name in series) {
          let data = series[name];
          let items = {
            name: name,
            type: 'line',
            data: data
          };
          seriesData.push(items);
        }
        this.$set(option, "series", seriesData);
        return option;
      },
    },
    watch: {
      status() {
        if ("Completed" === this.status) {
          this.initTableData()
        }
      }
    },
    props: ['id', 'status']
  }
</script>

<style scoped>

  .ms-card-data {
    text-align: left;
    display: block;
    margin-bottom: 5px;
  }

  .ms-card-desc {
    display: block;
    text-align: left;
  }

  .ms-card-data-digital {
    font-size: 21px;
  }

  .ms-card-data-unit {
    color: #8492a6;
    font-size: 15px;
  }

  .ms-card-index-1 .ms-card-data-digital {
    color: #44b349;
  }

  .ms-card-index-1 {
    border-left-color: #44b349;
    border-left-width: 3px;
  }

  .ms-card-index-2 .ms-card-data-digital {
    color: #65A2FF;
  }

  .ms-card-index-2 {
    border-left-color: #65A2FF;
    border-left-width: 3px;
  }

  .ms-card-index-3 .ms-card-data-digital {
    color: #E6113C;
  }

  .ms-card-index-3 {
    border-left-color: #E6113C;
    border-left-width: 3px;
  }

  .ms-card-index-4 .ms-card-data-digital {
    color: #99743C;
  }

  .ms-card-index-4 {
    border-left-color: #99743C;
    border-left-width: 3px;
  }

  .ms-card-index-5 .ms-card-data-digital {
    color: #99743C;
  }

  .ms-card-index-5 {
    border-left-color: #99743C;
    border-left-width: 3px;
  }

  .ms-card-index-6 .ms-card-data-digital {
    color: #3C9899;
  }

  .ms-card-index-6 {
    border-left-color: #3C9899;
    border-left-width: 3px;
  }

  .chart-config {
    width: 100%;
  }

</style>
