<template>
  <el-card class="scenario-results">
    <div class="scenario-header">
      <el-row :gutter="10">
        <el-col :span="16">
          {{$t('api_report.scenario_name')}}
        </el-col>
        <el-col :span="2">
          {{$t('api_report.response_time')}}
        </el-col>
        <el-col :span="2">
          {{$t('api_report.error')}}
        </el-col>
        <el-col :span="2">
          {{$t('api_report.assertions')}}
        </el-col>
        <el-col :span="2">
          {{$t('api_report.result')}}
        </el-col>
      </el-row>
    </div>
    <ms-scenario-result v-for="(scenario, index) in scenarios" :key="index" :scenario="scenario"/>
  </el-card>
</template>

<script>
  import MsScenarioResult from "./ScenarioResult";

  export default {
    name: "MsScenarioResults",

    components: {MsScenarioResult},

    props: {
      scenarios: Array
    }

  }
</script>

<style scoped>
  .scenario-header {
    border: 1px solid #EBEEF5;
    background-color: #F9FCFF;
    border-left: 0;
    border-right: 0;
    padding: 5px 0;
  }
</style>
