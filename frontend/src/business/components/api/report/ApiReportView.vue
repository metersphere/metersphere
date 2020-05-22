<template>
  <div class="container" v-loading="loading">
    <div class="main-content">
      <el-card>
        <section class="report-container" v-if="this.report.testId">
          <header class="report-header">
            <span>{{report.projectName}} / </span>
            <router-link :to="path">{{report.testName}}</router-link>
            <span class="time">{{report.createTime | timestampFormatDate}}</span>
          </header>
          <main v-if="this.isCompleted">
            <ms-metric-chart :content="content"/>
            <el-tabs v-model="activeName">
              <el-tab-pane :label="$t('api_report.total')" name="total">
                <ms-scenario-results :scenarios="content.scenarios"/>
              </el-tab-pane>
              <el-tab-pane name="fail">
                <template slot="label">
                  <span class="fail">{{$t('api_report.fail')}}</span>
                </template>
                <ms-scenario-results :scenarios="fails"/>
              </el-tab-pane>
            </el-tabs>
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
  import MsScenarioResults from "./components/ScenarioResults";

  export default {
    name: "MsApiReportView",
    components: {MsScenarioResults, MsMetricChart, MsScenarioResult, MsRequestResult},
    data() {
      return {
        activeName: "total",
        content: {},
        report: {},
        loading: true,
        fails: []
      }
    },

    methods: {
      init() {
        this.loading = true;
        this.report = {};
        this.content = {};
        this.fails = [];
      },
      getReport() {
        this.init();

        if (this.reportId) {
          let url = "/api/report/get/" + this.reportId;
          this.$get(url, response => {
            this.report = response.data || {};
            if (this.isNotRunning) {
              this.content = JSON.parse(this.report.content);
              this.getFails();
              this.loading = false;
            } else {
              setTimeout(this.getReport, 2000)
            }
          });
        }
      },
      getFails() {
        if (this.isNotRunning) {
          this.fails = [];
          this.content.scenarios.forEach((scenario) => {
            let failScenario = Object.assign({}, scenario);
            if (scenario.error > 0) {
              this.fails.push(failScenario);
              failScenario.requestResults = [];
              scenario.requestResults.forEach((request) => {
                if (!request.success) {
                  let failRequest = Object.assign({}, request);
                  failScenario.requestResults.push(failRequest);
                }
              })

            }
          })
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
      },
      isNotRunning() {
        return "Running" !== this.report.status;
      }
    }
  }
</script>
<style>
  .report-container .el-tabs__header {
    margin-bottom: 1px;
  }
</style>

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

  .report-header .time {
    color: #909399;
    margin-left: 10px;
  }

  .report-container .fail {
    color: #F56C6C;
  }

  .report-container .is-active .fail {
    color: inherit;
  }
</style>
