<template>
  <ms-container>
    <ms-main-container>
      <el-card>
        <section class="report-container">
          <div style="margin-top: 10px">
            <ms-api-report-view-header
              :show-cancel-button="false"
              :debug="debug"
              :export-flag="exportFlag"
              :report="report"
              @reportExport="handleExport"
              @reportSave="handleSave"/>
          </div>
          <main>
            <ms-metric-chart
              :content="content"
              :totalTime="totalTime"
              :report="report"
              v-if="!loading"/>
            <div>
              <el-tabs v-model="activeName" @tab-click="handleClick">
                <el-tab-pane :label="$t('api_report.total')" name="total">
                  <ms-scenario-results
                    :treeData="fullTreeNodes"
                    :default-expand="true"
                    :console="content.console"
                    v-on:requestResult="requestResult"
                  />
                </el-tab-pane>
                <el-tab-pane name="fail">
                  <template slot="label">
                    <span class="fail">
                      {{ $t('api_report.fail') }}
                    </span>
                  </template>
                  <ms-scenario-results
                    :console="content.console"
                    :treeData="fullTreeNodes"
                    v-on:requestResult="requestResult"
                    ref="failsTree"
                  />
                </el-tab-pane>
                <el-tab-pane name="errorReport" v-if="content.errorCode > 0">
                  <template slot="label">
                    <span class="fail">{{ $t('error_report_library.option.name') }}</span>
                  </template>
                  <ms-scenario-results v-on:requestResult="requestResult" :console="content.console"
                                       :treeData="fullTreeNodes" ref="errorReportTree"/>
                </el-tab-pane>
                <el-tab-pane name="unExecute" v-if="content.unExecute > 0">
                  <template slot="label">
                    <span class="fail">{{ $t('api_test.home_page.detail_card.unexecute') }}</span>
                  </template>
                  <ms-scenario-results v-on:requestResult="requestResult" :console="content.console"
                                       :treeData="fullTreeNodes" ref="unExecuteTree"/>
                </el-tab-pane>
                <el-tab-pane name="console">
                  <template slot="label">
                    <span class="console">{{ $t('api_test.definition.request.console') }}</span>
                  </template>
                  <pre>{{ content.console }}</pre>
                </el-tab-pane>
              </el-tabs>
            </div>
            <ms-api-report-export
              :title="report.testName"
              :content="content"
              :total-time="totalTime"
              id="apiTestReport"
              v-if="reportExportVisible"
            />
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
import MsApiReportExport from "./ApiReportExport";
import MsApiReportViewHeader from "./ApiReportViewHeader";
import {RequestFactory} from "../../definition/model/ApiTestModel";
import {windowPrint, getCurrentProjectID, getUUID} from "@/common/js/utils";
import {STEP} from "../scenario/Setting";

export default {
  name: "SysnApiReportDetail",
  components: {
    MsApiReportViewHeader,
    MsApiReportExport,
    MsMainContainer,
    MsContainer, MsScenarioResults, MsRequestResultTail, MsMetricChart, MsScenarioResult, MsRequestResult
  },
  data() {
    return {
      activeName: "total",
      content: {total: 0, scenarioTotal: 1},
      report: {},
      loading: false,
      totalTime: 0,
      isRequestResult: false,
      startTime: 99991611737506593,
      endTime: 0,
      request: {},
      isActive: false,
      scenarioName: null,
      reportExportVisible: false,
      requestType: undefined,
      fullTreeNodes: [],
      debugResult: new Map,
      scenarioMap: new Map,
      exportFlag: false,
      messageWebSocket: {},
      websocket: {},
      stepFilter: new STEP,
    }
  },
  activated() {
    this.isRequestResult = false;
  },
  created() {

  },
  mounted() {
    this.$nextTick(() => {
      if (this.scenario && this.scenario.scenarioDefinition) {
        this.content.scenarioStepTotal = this.scenario.scenarioDefinition.hashTree.length;
        this.initTree();
        this.initMessageSocket();
        this.clearDebug();
      }
    });
  },
  props: {
    reportId: String,
    currentProjectId: String,
    infoDb: Boolean,
    debug: Boolean,
    scenario: {},
    scenarioId: String
  },
  methods: {
    initTree() {
      this.fullTreeNodes = [];
      let obj = {resId: "root", index: 1, label: this.scenario.name, value: {responseResult: {}, unexecute: true, testing: false}, children: [], unsolicited: true};
      this.formatContent(this.scenario.scenarioDefinition.hashTree, obj, "", "root");
      this.fullTreeNodes.push(obj);
    },
    compare() {
      return function (a, b) {
        let v1 = a.value.sort;
        let v2 = b.value.sort;
        return v1 - v2;
      }
    },
    getType(type) {
      switch (type) {
        case "LoopController":
          return "循环控制器";
        case "TransactionController":
          return "事物控制器";
        case "ConstantTimer":
          return "等待控制器";
        case "IfController":
          return "条件控制器";
      }
      return type;
    },
    formatContent(hashTree, tree, fullPath, pid) {
      if (hashTree) {
        hashTree.forEach(item => {
          if (item.enable) {
            item.parentIndex = fullPath ? fullPath + "_" + item.index : item.index;
            let name = item.name ? item.name : this.getType(item.type);
            let id = item.type === 'JSR223Processor' || !item.id ? item.resourceId : item.id
            let obj = {
              pid: pid, resId: id + "_" + item.parentIndex, index: Number(item.index), label: name,
              value: {name: name, responseResult: {}, unexecute: true, testing: false}, children: [], unsolicited: true
            };
            tree.children.push(obj);
            if (this.stepFilter.get("AllSamplerProxy").indexOf(item.type) !== -1) {
              obj.unsolicited = false;
              obj.type = item.type;
            } else if (item.type === 'scenario') {
              this.content.scenarioTotal += 1;
            }
            if (item.hashTree && item.hashTree.length > 0 && this.stepFilter && this.stepFilter.get("AllSamplerProxy").indexOf(item.type) === -1) {
              this.formatContent(item.hashTree, obj, item.parentIndex, obj.resId);
            }
          }
        })
      }
    },
    handleExport() {
      this.reportExportVisible = true;
      let reset = this.exportReportReset;
      this.$nextTick(() => {
        windowPrint('apiTestReport', 0.57);
        reset();
      });
    },
    filter(index) {
      if (index === "1") {
        this.$refs.failsTree.filter(index);
      } else if (this.activeName === "errorReport") {
        this.$refs.errorReportTree.filter("errorReport");
      } else if (this.activeName === "unExecute") {
        this.$refs.unExecuteTree.filter("unexecute");
      }
    },
    handleClick(tab, event) {
      this.isRequestResult = false;
      this.filter(tab.index);
    },
    exportReportReset() {
      this.$router.go(0);
    },
    requestResult(requestResult) {
      this.active();
      this.isRequestResult = false;
      this.requestType = undefined;
      if (requestResult.request.body.indexOf('[Callable Statement]') > -1) {
        this.requestType = RequestFactory.TYPES.SQL;
      }
      this.$nextTick(function () {
        this.isRequestResult = true;
        this.request = requestResult.request;
        this.scenarioName = requestResult.scenarioName;
      });
    },

    clearDebug() {
      this.totalTime = 0;
      this.content.total = 0;
      this.content.error = 0;
      this.content.success = 0;
      this.content.passAssertions = 0;
      this.content.totalAssertions = 0;
      this.content.scenarioSuccess = 0;
      this.content.scenarioError = 0;
    },
    reload() {
      this.loading = true;
      this.$nextTick(() => {
        this.loading = false;
      });
    },
    recursiveSorting(arr) {
      for (let i in arr) {
        let step = arr[i];
        if (step) {
          step.index = Number(i) + 1;
          if (step.value) {
            step.value.testing = false;
          }
          if (step.children && step.children.length > 0) {
            this.recursiveSorting(step.children);
          }
        }
      }
    },
    handleSave() {
      if (!this.report.name) {
        this.$warning(this.$t('api_test.automation.report_name_info'));
        return;
      }
      this.loading = true;
      this.report.projectId = this.projectId;
      let url = "/api/scenario/report/update";
      this.result = this.$post(url, this.report, response => {
        this.$success(this.$t('commons.save_success'));
        this.loading = false;
        this.$emit('refresh');
      }, error => {
        this.loading = false;
      });
    },

    initMessageSocket() {
      let protocol = "ws://";
      if (window.location.protocol === 'https:') {
        protocol = "wss://";
      }
      const uri = protocol + window.location.host + "/ws/" + this.reportId;
      this.messageWebSocket = new WebSocket(uri);
      this.messageWebSocket.onmessage = this.onMessage;
    },
    getReport() {
      let url = "/api/scenario/report/get/" + this.reportId;
      this.$get(url, response => {
        this.report = response.data || {};
        if (response.data) {
          this.content = JSON.parse(response.data.content);
          if (!this.content) {
            this.content = {scenarios: []};
          }
          this.content.error = this.content.error;
          this.content.success = (this.content.total - this.content.error - this.content.errorCode);
          this.totalTime = this.content.totalTime;
          this.fullTreeNodes = this.content.steps;
          this.recursiveSorting(this.fullTreeNodes);
          this.reload();
        }
      });
    },
    runningNodeChild(arr, resourceId) {
      arr.forEach(item => {
        if (resourceId === item.resId) {
          item.value.testing = true;
        } else if (resourceId && resourceId.startsWith("result_")) {
          let data = JSON.parse(resourceId.substring(7));
          if (data.method === 'Request' && data.subRequestResults && data.subRequestResults.length > 0) {
            data.subRequestResults.forEach(subItem => {
              if (item.resId === subItem.resourceId) {
                item.value = subItem;
                item.testing = false;
                item.debug = true;
              }
            })
          } else if (item.resId === data.resourceId) {
            if (item.value && item.value.id && !item.mark) {
              let newItem = JSON.parse(JSON.stringify(item));
              newItem.value = data;
              newItem.mark = true;
              newItem.testing = false;
              newItem.index = item.index + 1;
              newItem.debug = true;
              arr.push(newItem);
            } else {
              item.value = data;
              item.testing = false;
              item.debug = true;
            }
          }
        }
        if (item.children && item.children.length > 0) {
          this.runningNodeChild(item.children, resourceId);
        }
      })
    },
    runningEvaluation(resourceId) {
      if (this.fullTreeNodes) {
        this.fullTreeNodes.forEach(item => {
          if (resourceId === item.resId) {
            item.value.testing = true;
          } else if (resourceId && resourceId.startsWith("result_")) {
            let data = JSON.parse(resourceId.substring(7));
            if (data.method === 'Request' && data.subRequestResults && data.subRequestResults.length > 0) {
              data.subRequestResults.forEach(subItem => {
                if (item.resId === subItem.resourceId) {
                  item.value = subItem;
                  item.testing = false;
                  item.debug = true;
                }
              })
            } else if (item.resId === data.resourceId) {
              item.value = data;
              item.testing = false;
              item.debug = true;
            }
          }
          if (item.children && item.children.length > 0) {
            this.runningNodeChild(item.children, resourceId);
          }
        })
      }
    },
    onMessage(e) {
      if (e.data) {
        this.runningEvaluation(e.data);
      }
      if (e.data && e.data.indexOf("MS_TEST_END") !== -1) {
        this.getReport();
        this.messageWebSocket.close();
        this.$EventBus.$emit('hide', this.scenarioId);
        this.$emit('refresh', this.debugResult);
      }
    },
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
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

.export-button {
  float: right;
}

.scenario-result .icon.is-active {
  transform: rotate(90deg);
}

</style>
