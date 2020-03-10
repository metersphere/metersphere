<template>
  <div>
    <el-row :gutter="12">
      <el-col :span="4">
        <el-card shadow="always" class="ms-card-index-1">
          <span class="ms-card-data">
            <span class="ms-card-data-digital">40</span>
            <span class="ms-card-data-unit"> VU</span>
          </span>
          <span class="ms-card-desc">Max Users</span>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="always" class="ms-card-index-2">
          <span class="ms-card-data">
            <span class="ms-card-data-digital">5.4</span>
            <span class="ms-card-data-unit"> Hits/s</span>
          </span>
          <span class="ms-card-desc">Avg.Throughput</span>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="always" class="ms-card-index-3">
          <span class="ms-card-data">
            <span class="ms-card-data-digital">0.41</span>
            <span class="ms-card-data-unit"> %</span>
          </span>
          <span class="ms-card-desc">Errors</span>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="always" class="ms-card-index-4">
          <span class="ms-card-data">
            <span class="ms-card-data-digital">1.28</span>
            <span class="ms-card-data-unit"> s</span>
          </span>
          <span class="ms-card-desc">Avg.Response Time</span>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="always" class="ms-card-index-5">
          <span class="ms-card-data">
            <span class="ms-card-data-digital">1.41</span>
            <span class="ms-card-data-unit"> s</span>
          </span>
          <span class="ms-card-desc">90% Response Time</span>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="always" class="ms-card-index-6">
          <span class="ms-card-data">
            <span class="ms-card-data-digital">817.29</span>
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
        option1: {
          xAxis: {
            type: 'category',
            boundaryGap: false,
            data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
          },
          yAxis: {
            type: 'value'
          },
          series: [{
            data: [820, 932, 901, 934, 1290, 1330, 1320],
            type: 'line',
            areaStyle: {}
          }]
        },
        option2: {
          xAxis: {
            type: 'category',
            data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
          },
          yAxis: {
            type: 'value'
          },
          series: [{
            data: [820, 932, 901, 934, 1290, 1330, 1320],
            type: 'line'
          }]
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
    color: #FFC265;
  }

  .ms-card-index-3 {
    border-left-color: #FFC265;
    border-left-width: 3px;
  }

  .ms-card-index-4 .ms-card-data-digital {
    color: #C265FF;
  }

  .ms-card-index-4 {
    border-left-color: #C265FF;
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
