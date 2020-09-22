<template>
  <el-card>
    <div class="report-title title">接口测试报告</div>
    <ms-metric-chart :content="content" :totalTime="totalTime"/>
    <div class="scenario-result" v-for="(scenario, index) in content.scenarios" :key="index" :scenario="scenario">
        <div>
          <el-card >
            <template v-slot:header>
              {{$t('api_report.scenario_name')}}：{{scenario.name}}
            </template>
            <div class="ms-border" v-for="(request, index) in scenario.requestResults" :key="index" :request="request">

              <div class="request-left">
                <api-report-reqest-header-item :title="request.name">
                  <span class="url"> {{request.url}}</span>
                </api-report-reqest-header-item>
              </div>


              <div class="request-right">

                <api-report-reqest-header-item :title="$t('api_test.request.method')">
                  <span class="method"> {{request.method}}</span>
                </api-report-reqest-header-item>

                <api-report-reqest-header-item :title="$t('api_report.response_time')">
                  {{request.responseResult.responseTime}}
                </api-report-reqest-header-item>

                <api-report-reqest-header-item :title="$t('api_report.latency')">
                  {{request.responseResult.latency}} ms
                </api-report-reqest-header-item>

                <api-report-reqest-header-item :title="$t('api_report.request_size')">
                  {{request.requestSize}} bytes
                </api-report-reqest-header-item>

                <api-report-reqest-header-item :title="$t('api_report.response_size')">
                  {{request.responseResult.latency}} ms
                </api-report-reqest-header-item>

                <api-report-reqest-header-item :title="$t('api_report.error')">
                  {{request.responseResult.responseSize}} bytes
                </api-report-reqest-header-item>

                <api-report-reqest-header-item :title="$t('api_report.assertions')">
                  {{request.passAssertions + " / " + request.totalAssertions}}
                </api-report-reqest-header-item>

                <api-report-reqest-header-item :title="$t('api_report.response_code')">
                  {{request.responseResult.responseCode}}
                </api-report-reqest-header-item>

                <api-report-reqest-header-item :title="$t('api_report.result')">
                  <el-tag size="mini" type="success" v-if="request.success">
                    {{$t('api_report.success')}}
                  </el-tag>
                  <el-tag size="mini" type="danger" v-else>
                    {{$t('api_report.fail')}}
                  </el-tag>
                </api-report-reqest-header-item>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </el-card>
</template>

<script>
    import MsScenarioResult from "./components/ScenarioResult";
    import MsRequestResultTail from "./components/RequestResultTail";
    import ApiReportReqestHeaderItem from "./ApiReportReqestHeaderItem";
    import MsMetricChart from "./components/MetricChart";
    export default {
      name: "MsApiReportExport",
      components: {MsMetricChart, ApiReportReqestHeaderItem, MsRequestResultTail, MsScenarioResult},
      props: {
        content: Object,
        totalTime: Number
      },
      data() {
        return {
        }
      },
    }
</script>

<style scoped>

  .scenario-result {
    margin-top: 20px;
    margin-bottom: 20px;
  }

  .method {
    color: #1E90FF;
    font-size: 14px;
    font-weight: 500;
  }

  .request-right {
    float: right;
  }

  .request-left {
    float: left;
    margin-left: 20px;
  }

  .ms-border {
    height: 60px;
  }

  .report-title {
    font-size: 30px;
    font-weight: bold;
    height: 50px;
    text-align: center;
    margin-bottom: 20px;
  }

  .url {
    color: #409EFF;
    font-size: 14px;
    font-weight: bold;
    font-style: italic;
  }

  .el-card {
    padding: 10px;
    padding: 30px;
    border-style: none;
  }

</style>
