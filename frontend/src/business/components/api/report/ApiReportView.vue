<template>
  <div class="container" v-loading="result.loading">
    <div class="main-content">
      <el-card>
        <section class="report-container">
          <header class="report-header" v-if="this.report.testId">
            <span>{{report.projectName}} / </span>
            <router-link :to="path">{{report.testName}}</router-link>
          </header>
          <main>
            <div class="scenario-chart">
              <ms-metric-chart :content="content"></ms-metric-chart>
            </div>
            <el-card>
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
              <ms-scenario-result v-for="(scenario, index) in content.scenarios" :key="index" :scenario="scenario"/>
            </el-card>
          </main>
        </section>
      </el-card>
    </div>
  </div>
</template>

<script>

  import MsRequestResult from "./components/RequestResult";
  import MsScenarioResult from "./components/ScenarioResult";
  import MsMetricChart from "./components/MetricChart";

  export default {
    name: "MsApiReportView",
    components: {MsMetricChart, MsScenarioResult, MsRequestResult},
    data() {
      return {
        content: {},
        report: {},
        result: {},
      }
    },

    methods: {
      getReport() {
        if (this.reportId) {
          let url = "/api/report/get/" + this.reportId;
          this.result = this.$get(url, response => {
            this.report = response.data || {};
            this.content = JSON.parse(this.report.content);
          });
        }
      }
    },

    watch: {
      '$route': 'getReport',
    },

    created() {
      this.getReport();
    },

    computed: {
      reportId: function () {
        return this.$route.params.reportId;
      },
      path() {
        return "/api/test/edit?id=" + this.report.testId;
      }
    }
  }
</script>

<style scoped>
  .report-container {
    height: calc(100vh - 150px);
    min-height: 600px;
    overflow-y: auto;
  }

  .report-header {
    font-size: 15px;
  }

  .report-header a {
    text-decoration: none;
  }

  .scenario-header {
    border: 1px solid #EBEEF5;
    background-color: #F9FCFF;
    border-left: 0;
    border-right: 0;
    padding: 5px 0;
  }
</style>
