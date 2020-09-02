<template>
  <ms-container v-loading="loading" :element-loading-text="$t('api_report.running')">
    <ms-main-container>
      <el-card>
        <section class="report-container" v-if="this.report.testId">
          <header class="report-header">
            <el-row>
              <el-col>
                <span>{{ report.projectName }} / </span>
                <router-link :to="path">{{ report.testName }}</router-link>
                <span class="time">{{ report.createTime | timestampFormatDate }}</span>
                <!--<el-button plain type="primary" size="mini" @click="handleExport(report.name)"
                           style="margin-left: 1200px">
                  {{$t('test_track.plan_view.export_report')}}
                </el-button>-->
              </el-col>
            </el-row>
          </header>
          <main v-if="this.isNotRunning">
            <ms-metric-chart :content="content" :totalTime="totalTime"/>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-tabs v-model="activeName" @tab-click="handleClick">
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
    </ms-main-container>
  </ms-container>
</template>

<script>

import MsRequestResult from "./components/RequestResult";
import MsRequestResultTail from "./components/RequestResultTail";
import MsScenarioResult from "./components/ScenarioResult";
import MsMetricChart from "./components/MetricChart";
import MsScenarioResults from "./components/ScenarioResults";
import MsContainer from "@/business/components/common/components/MsContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";

export default {
  name: "MsApiReportView",
  components: {
    MsMainContainer,
    MsContainer, MsScenarioResults, MsRequestResultTail, MsMetricChart, MsScenarioResult, MsRequestResult
  },
  data() {
    return {
      activeName: "total",
      content: {},
      report: {},
      loading: true,
      fails: [],
      totalTime: 0,
      isRequestResult: false,
      request: {},
      scenarioName: null,
    }
  },
  activated() {
    this.isRequestResult = false
  },
  methods: {
    init() {
      this.loading = true;
      this.report = {};
      this.content = {};
      this.fails = [];
    },
    handleClick(tab, event) {
      this.isRequestResult = false
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
        this.totalTime = 0
        this.content.scenarios.forEach((scenario) => {
          this.totalTime = this.totalTime + Number(scenario.responseTime)
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
      this.isRequestResult = false;
      this.$nextTick(function () {
        this.isRequestResult = true;
        this.request = requestResult.request;
        this.scenarioName = requestResult.scenarioName;
      });
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
  height: calc(100vh - 155px);
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
