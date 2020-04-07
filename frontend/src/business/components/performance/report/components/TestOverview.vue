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
        <chart ref="chart1" :options="loadOption" :autoresize="true"></chart>
      </el-col>
      <el-col :span="12">
        <chart ref="chart2" :options="resOption" :autoresize="true"></chart>
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
        this.$get("/report/content/testoverview/" + this.id, res => {
          let data = res.data;
          this.maxUsers = data.maxUsers;
          this.avgThroughput = data.avgThroughput;
          this.errors = data.errors;
          this.avgResponseTime = data.avgResponseTime;
          this.responseTime90 = data.responseTime90;
          this.avgBandwidth = data.avgBandwidth;
        })
        this.$get("/report/content/load_chart/" + this.id, res => {
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
            legend: {
              bottom: 10,
              data: ['Users', 'Hits/s', 'Error(s)']
            },
            xAxis: {
              type: 'category',
            },
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
            series: [
              {
                name: 'Users',
                color: '#0CA74A',
                type: 'line',
                yAxisIndex: 0
              },
              {
                name: 'Hits/s',
                color: '#65A2FF',
                type: 'line',
                yAxisIndex: 1
              },
              {
                name: 'Error(s)',
                color: '#E6113C',
                type: 'line',
                yAxisIndex: 1
              }
            ]
          }
          this.loadOption = this.generateLoadOption(loadOption, data);
        })
        this.$get("/report/content/res_chart/" + this.id, res => {
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
            legend: {
              bottom: 10,
              data: ['Users', 'Response Time']
            },
            xAxis: {
              type: 'category'
            },
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
            series: [
              {
                name: 'Users',
                color: '#0CA74A',
                type: 'line',
                yAxisIndex: 0
              },
              {
                name: 'Response Time',
                color: '#99743C',
                type: 'line',
                yAxisIndex: 1
              }
            ]
          }
          this.resOption = this.generateResponseOption(resOption, data);
        })
      },
      _objToStrMap(obj){
        let strMap = new Map();
        for (let k of Object.keys(obj)) {
          strMap.set(k,obj[k]);
        }
        return strMap;
      },
      _jsonToMap(jsonStr){
        return this._objToStrMap(JSON.parse(jsonStr));
      },
      generateLoadOption(loadOption, data) {
        let map = this._jsonToMap(data.serices);
        let xAxis = data.xAxis;
        this.$set(loadOption.xAxis, "data", xAxis.split(','));
        let user = map.get("users").slice(0);
        let hit = map.get("hits").slice(0);
        user.sort(function (a,b) {
          return parseInt(a) - parseInt(b);
        })
        hit.sort(function (a,b) {
          return parseFloat(a) - parseFloat(b);
        })
        this.$set(loadOption.yAxis[0], "max",user[user.length-1]);
        this.$set(loadOption.yAxis[0], "interval", user[user.length-1]/5);
        this.$set(loadOption.yAxis[1], "max", hit[hit.length-1]);
        this.$set(loadOption.yAxis[1], "interval", hit[hit.length-1]/5);

        this.$set(loadOption.series[0], "data", map.get("users"));
        this.$set(loadOption.series[1], "data", map.get("hits"));
        this.$set(loadOption.series[2], "data", map.get("errors"));
        return loadOption;
      },
      generateResponseOption(resOption, data) {
        let map = this._jsonToMap(data.serices);
        let user = map.get("users").slice(0);
        let res = map.get("resTime").slice(0);
        user.sort(function (a,b) {
          return parseInt(a) - parseInt(b);
        })
        res.sort(function (a,b) {
          return parseFloat(a) - parseFloat(b);
        })

        this.$set(resOption.yAxis[0], "max",user[user.length-1]);
        this.$set(resOption.yAxis[0], "interval", user[user.length-1]/5);
        this.$set(resOption.yAxis[1], "max", res[res.length-1]);
        this.$set(resOption.yAxis[1], "interval", res[res.length-1]/5);

        let xAxis = data.xAxis;
        this.$set(resOption.xAxis, "data", xAxis.split(','));
        this.$set(resOption.series[0], "data", map.get("users"));
        this.$set(resOption.series[1], "data", map.get("resTime"));
        return resOption;
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

</style>
