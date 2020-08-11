<template>
  <div class="container" v-loading="loading" :element-loading-text="$t('api_report.running')">
    <div class="main-content">
      <el-card>
        <section class="report-container" v-if="this.report.testId">
          <header class="report-header">
            <span>{{ report.projectName }} / </span>
            <router-link :to="path">{{ report.testName }}</router-link>
            <span class="time">{{ report.createTime | timestampFormatDate }}</span>
          </header>
          <main v-if="this.isNotRunning">
            <ms-metric-chart :content="content" :totalTime="totalTime"/>
            <!--<el-container>
              <el-aside width="900px">
                <el-tabs v-model="activeName">
                  <el-tab-pane :label="$t('api_report.total')" name="total">
                    <ms-scenario-results :scenarios="content.scenarios"/>
                  </el-tab-pane>
                  <el-tab-pane name="fail">
                    <template slot="label">
                      <span class="fail">{{ $t('api_report.fail') }}</span>
                    </template>
                    <ms-scenario-results :scenarios="fails"/>
                  </el-tab-pane>
                </el-tabs>
              </el-aside>
              <el-main style="margin-top: 20px">

              </el-main>
            </el-container>-->
            <el-row :gutter="20">
              <el-col :span="8">
                <el-tabs v-model="activeName">
                  <el-tab-pane :label="$t('api_report.total')" name="total">
                    <ms-scenario-results :scenarios="content.scenarios" v-on:requestResult="requestResult"/>
                  </el-tab-pane>
                  <el-tab-pane name="fail">
                    <template slot="label">
                      <span class="fail">{{ $t('api_report.fail') }}</span>
                    </template>
                    <ms-scenario-results v-on:requestResult="requestResult" :scenarios="fails"/>
                  </el-tab-pane>
                </el-tabs>
              </el-col>
              <el-col :span="16" style="margin-top: 40px;">
                <ms-request-result-tail v-if="isRequestResult" :request="request" :scenario-name="scenarioName"/>
              </el-col>
            </el-row>
          </main>
        </section>
      </el-card>
    </div>
  </div>
</template>

<script>

  import MsRequestResult from "./components/RequestResult";
  import MsRequestResultTail from "./components/RequestResultTail";
  import MsScenarioResult from "./components/ScenarioResult";
  import MsMetricChart from "./components/MetricChart";
  import MsScenarioResults from "./components/ScenarioResults";

  export default {
    name: "MsApiReportView",
    components: {MsScenarioResults, MsRequestResultTail, MsMetricChart, MsScenarioResult, MsRequestResult},
    data() {
      return {
        activeName: "total",
        content: {},
        report: {},
        loading: true,
        fails: [],
        totalTime: "",
        isRequestResult: false,
        request: {},
        scenarioName: null,
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
            if (response.data) {
              if (this.isNotRunning) {
                try {
                  this.content = JSON.parse(this.report.content);
                } catch (e) {
                  console.log(this.report.content)
                  throw e;
                }
                this.getFails();
                this.loading = false;
              } else {
                setTimeout(this.getReport, 2000)
              }
            } else {
              this.loading = false;
              this.$error(this.$t('api_report.not_exist'));
            }
          });
        }
      },
      getFails() {
        if (this.isNotRunning) {
          this.fails = [];
          this.content.scenarios.forEach((scenario) => {
            this.totalTime = scenario.responseTime + scenario.responseTime
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
      },
      requestResult(requestResult) {
        this.isRequestResult = true;
        this.request = requestResult.request;
        this.scenarioName = requestResult.scenarioName;
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
