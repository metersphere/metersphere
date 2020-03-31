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
        <chart ref="chart1" :options="option1" :autoresize="true"></chart>
      </el-col>
      <el-col :span="12">
        <chart ref="chart2" :options="option2" :autoresize="true"></chart>
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
        option1: {
          legend: {
            top: 20,
            data: ['Users', 'Hits/s', 'Error(s)']
          },
          xAxis: {
            type: 'category',
            data: ["10:22:01", "10:22:02", "10:22:04", "10:22:06",
              "10:22:07", "10:22:08", "10:22:09", "10:22:10", "10:22:11", "10:22:12"]
          },
          yAxis: [{
            name: 'User',
            type: 'value',
            min: 0,
            max: 30,
            splitNumber: 5,
            interval: 30 / 5
          },
            {
              name: 'Hits/s',
              type: 'value'
            }
          ],
          series: [
            {
              name: 'Users',
              color: '#0CA74A',
              data: [6,9,10,15,1,10,25,19,15,2],
              type: 'line',
              yAxisIndex: 0
            },
            {
              name: 'Hits/s',
              color: '#65A2FF',
              data: [1,1,1,1,1,1,1,1,1,1],
              type: 'line',
              yAxisIndex: 1
            },
            {
              name: 'Error(s)',
              color: '#E6113C',
              data: [0,0,0,0,1,1,2,0,5,2],
              type: 'line',
              yAxisIndex: 1
            }
          ]
        },
        option2: {
          legend: {
            top: 20,
            data: ['Users', 'Response Time']
          },
          xAxis: {
            type: 'category',
            data: ["2020-03-25 10:22:01", "2020-03-25 10:22:02", "2020-03-25 10:22:04", "2020-03-25 10:22:06",
              "2020-03-25 10:22:07", "2020-03-25 10:22:08", "2020-03-25 10:22:09", "2020-03-25 10:22:10", "2020-03-25 10:22:11", "2020-03-25 10:22:12"]
          },
          yAxis: [{
            name: 'User',
            type: 'value',
            },
            {
              name: 'Response Time',
              type: 'value'
            }
          ],
          series: [
            {
              name: 'Users',
              color: '#0CA74A',
              data: [20, 40, 40, 40, 40, 40, 40],
              type: 'line',
            },
            {
              name: 'Response Time',
              color: '#99743C',
              data: [15, 38, 35, 39, 36, 37, 5],
              type: 'line',
            }
          ]
        }
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
      }
    },
    created() {
      this.initTableData()
    },
    props: ['id'],
    watch: {
      '$route'(to) {
        if (to.name === "perReportView") {
          let reportId = to.path.split('/')[4];
          if(reportId){
            this.$get("/report/content/testoverview/" + reportId, res => {
              let data = res.data;
              this.maxUsers = data.maxUsers;
              this.avgThroughput = data.avgThroughput;
              this.errors = data.errors;
              this.avgResponseTime = data.avgResponseTime;
              this.responseTime90 = data.responseTime90;
              this.avgBandwidth = data.avgBandwidth;
            })
          }
        }
      }
    }
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
