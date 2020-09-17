<template>
  <ms-container>
    <ms-main-container>
      <span v-if="!reportId">{{$t('commons.not_performed_yet')}}</span>
      <el-card v-if="reportId">
        <section class="report-container" v-loading="loading">
          <header class="report-header">
            <span>{{report.projectName}} / </span>
            <span class="time">{{report.createTime | timestampFormatDate}}</span>
          </header>
          <main>
            <ms-metric-chart v-if="content" :content="content"/>
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
    </ms-main-container>
  </ms-container>
</template>

<script>

import MsScenarioResult from "../../../../../api/report/components/ScenarioResult";
import MsMetricChart from "../../../../../api/report/components/MetricChart";
import MsScenarioResults from "../../../../../api/report/components/ScenarioResults";
import MsRequestResult from "../../../../../api/report/components/RequestResult";
import MsContainer from "../../../../../common/components/MsContainer";
import MsMainContainer from "../../../../../common/components/MsMainContainer";

export default {
  name: "ApiTestResult",
  components: {MsMainContainer, MsContainer, MsRequestResult, MsScenarioResults, MsMetricChart, MsScenarioResult},
  data() {
    return {
      activeName: "total",
      content: {},
      report: {},
      loading: true,
      fails: [],
    }
    },
    props:['reportId'],
    watch: {
      reportId() {
        this.init();
      }
    },
    mounted() {
      this.init();
    },
    methods: {
      init() {
        this.loading = true;
        this.report = {};
        this.content = {};
        this.fails = [];
        this.getReport();
      },
      getReport() {
        if (this.reportId) {
          let url = "/api/report/get/" + this.reportId;
          this.$get(url, response => {
            this.report = response.data || {};
            if (this.report.status == 'Completed' || this.report.status == 'Success' || this.report.status == 'Error') {
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
            });
          }
        });
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
