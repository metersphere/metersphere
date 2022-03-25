<template>
  <ms-container v-loading="loading">
    <ms-main-container>
      <el-card>
        <section class="report-container" v-if="this.report.testId">
          <ms-api-report-view-header :show-cancel-button="showCancelButton" :is-plan="isPlan" :is-template="isTemplate"
                                     :debug="debug" :report="report" @reportExport="handleExport"
                                     @reportSave="handleSave"/>
          <main v-if="isNotRunning">
            <ms-metric-chart :content="content" :totalTime="totalTime" :report="report"/>
            <div>
              <el-tabs v-model="activeName" @tab-click="handleClick">
                <el-tab-pane :label="$t('api_report.total')" name="total">
                  <ms-scenario-results :treeData="fullTreeNodes" :console="content.console"
                                       v-on:requestResult="requestResult" ref="resultsTree"/>
                </el-tab-pane>
                <el-tab-pane name="fail">
                  <template slot="label">
                    <span class="fail">{{ $t('api_report.fail') }}</span>
                  </template>
                  <ms-scenario-results v-on:requestResult="requestResult" :console="content.console"
                                       :treeData="fullTreeNodes" ref="failsTree"
                                       :errorReport="content.error"/>
                </el-tab-pane>
                <el-tab-pane name="errorReport" v-if="content.errorCode > 0">
                  <template slot="label">
                    <span class="fail" style="color: #F6972A">{{ $t('error_report_library.option.name') }}</span>
                  </template>
                  <ms-scenario-results v-on:requestResult="requestResult" :console="content.console"
                                       :treeData="fullTreeNodes" ref="errorReportTree"/>
                </el-tab-pane>
                <el-tab-pane name="unExecute" v-if="content.unExecute > 0">
                  <template slot="label">
                    <span class="fail" style="color: #9C9B9A">{{ $t('api_test.home_page.detail_card.unexecute') }}</span>
                  </template>
                  <ms-scenario-results v-on:requestResult="requestResult" :console="content.console"
                                       :treeData="fullTreeNodes" ref="unExecuteTree"/>
                </el-tab-pane>
                <el-tab-pane name="console">
                  <template slot="label">
                    <span class="console">{{ $t('api_test.definition.request.console') }}</span>
                  </template>
                  <ms-code-edit :mode="'text'" :read-only="true" :data.sync="content.console"
                                height="calc(100vh - 500px)"/>
                </el-tab-pane>

              </el-tabs>
            </div>
            <ms-api-report-export v-if="reportExportVisible" id="apiTestReport" :title="report.name"
                                  :content="content" :total-time="totalTime"/>
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
import {windowPrint, getUUID, getCurrentProjectID} from "@/common/js/utils";
import {getScenarioReport, getShareScenarioReport} from "@/network/api";
import {STEP} from "@/business/components/api/automation/scenario/Setting";
import MsCodeEdit from "@/business/components/common/components/MsCodeEdit";

export default {
  name: "MsApiReport",
  components: {
    MsApiReportViewHeader,
    MsApiReportExport,
    MsMainContainer,
    MsCodeEdit,
    MsContainer, MsScenarioResults, MsRequestResultTail, MsMetricChart, MsScenarioResult, MsRequestResult
  },
  data() {
    return {
      activeName: "total",
      content: {},
      report: {},
      loading: true,
      fails: [],
      failsTreeNodes: [],
      totalTime: 0,
      isRequestResult: false,
      request: {},
      isActive: false,
      scenarioName: null,
      reportExportVisible: false,
      requestType: undefined,
      fullTreeNodes: [],
      stepFilter: new STEP,
    }
  },
  activated() {
    this.isRequestResult = false;
  },
  props: {
    reportId: String,
    currentProjectId: String,
    infoDb: Boolean,
    debug: Boolean,
    isTemplate: Boolean,
    templateReport: Object,
    isShare: Boolean,
    shareId: String,
    isPlan: Boolean,
    showCancelButton: {
      type: Boolean,
      default: true
    }
  },
  watch: {
    reportId() {
      if (!this.isTemplate) {
        this.getReport();
      }
    },
    templateReport() {
      if (this.isTemplate) {
        this.getReport();
      }
    }
  },
  methods: {
    filter(index) {
      if (index === "1") {
        this.$refs.failsTree.filter(index);
      } else if (this.activeName === "errorReport") {
        this.$refs.errorReportTree.filter("errorReport");
      } else if(this.activeName === "unExecute"){
        this.$refs.unExecuteTree.filter("unexecute");
      }
    },
    init() {
      this.loading = true;
      this.report = {};
      this.content = {};
      this.fails = [];
      this.report = {};
      this.fullTreeNodes = [];
      this.failsTreeNodes = [];
      this.isRequestResult = false;
      this.activeName = "total";
    },
    handleClick(tab, event) {
      this.isRequestResult = false;
      if (this.report && this.report.reportVersion && this.report.reportVersion > 1) {
        this.filter(tab.index);
      }
    },
    active() {
      this.isActive = !this.isActive;
    },
    formatResult(res) {
      let resMap = new Map;
      let array = [];
      if (res && res.scenarios) {
        res.scenarios.forEach(item => {
          if (item && item.requestResults) {
            item.requestResults.forEach(req => {
              req.responseResult.console = res.console;
              resMap.set(req.id + req.name, req);
              req.name = item.name + "^@~@^" + req.name + "UUID=" + getUUID();
              array.push(req);
            })
          }
        })
      }
      this.formatTree(array, this.fullTreeNodes);
      this.sort(this.fullTreeNodes);
      this.$emit('refresh', resMap);
    },
    formatTree(array, tree) {
      array.map((item) => {
        let key = item.name;
        let nodeArray = key.split('^@~@^');
        let children = tree;
        //运行场景中如果连续将1个场景引入多次，会出现运行结果合并的情况。
        //为了解决这种问题，在转hashTree的时候给场景放了个新ID，前台加载解析的时候也要做处理
        let scenarioId = "";
        let scenarioName = "";
        if (item.scenario) {
          let scenarioArr = JSON.parse(item.scenario);
          if (scenarioArr.length > 1) {
            let scenarioIdArr = scenarioArr[0].split("_");
            scenarioId = scenarioIdArr[0];
            scenarioName = scenarioIdArr[1];
          }
        }
        // 循环构建子节点
        for (let i = 0; i < nodeArray.length; i++) {
          if (!nodeArray[i]) {
            continue;
          }
          let node = {
            label: nodeArray[i],
            value: item,
          };
          if (i !== (nodeArray.length - 1)) {
            node.children = [];
          } else {
            if (item.subRequestResults && item.subRequestResults.length > 0) {
              let itemChildren = this.deepFormatTreeNode(item.subRequestResults);
              node.children = itemChildren;
              if (node.label.indexOf("UUID=")) {
                node.label = node.label.split("UUID=")[0];
              }
            }
          }
          if (children.length === 0) {
            children.push(node);
          }

          let isExist = false;
          for (let j in children) {
            if (children[j].label === node.label) {

              let idIsPath = true;
              //判断ID是否匹配  目前发现问题的只有重复场景，而重复场景是在第二个节点开始合并的。所以这里暂时只判断第二个场景问题。
              //如果出现了其他问题，则需要检查其他问题的数据结构。暂时采用具体问题具体分析的策略
              if (i === nodeArray.length - 2) {
                idIsPath = false;
                let childId = "";
                let childName = "";
                if (children[j].value && children[j].value.scenario) {
                  let scenarioArr = JSON.parse(children[j].value.scenario);
                  if (scenarioArr.length > 1) {
                    let childArr = scenarioArr[0].split("_");
                    childId = childArr[0];
                    if (childArr.length > 1) {
                      childName = childArr[1];
                    }
                  }
                }
                if (scenarioId === "") {
                  idIsPath = true;
                } else if (scenarioId === childId) {
                  idIsPath = true;
                } else if (scenarioName !== childName) {
                  //如果两个名字不匹配则默认通过，不匹配ID
                  idIsPath = true;
                }
              }
              if (idIsPath) {
                if (i !== nodeArray.length - 1 && !children[j].children) {
                  children[j].children = [];
                }
                children = (i === nodeArray.length - 1 ? children : children[j].children);
                isExist = true;
                break;
              }
            }
          }
          if (!isExist) {
            children.push(node);
            if (i !== nodeArray.length - 1 && !children[children.length - 1].children) {
              children[children.length - 1].children = [];
            }
            children = (i === nodeArray.length - 1 ? children : children[children.length - 1].children);
          }
        }
      })
    },

    deepFormatTreeNode(array) {
      let returnChildren = [];
      array.map((item) => {
        let children = [];
        let key = item.name.split('^@~@^')[0];
        let nodeArray = key.split('<->');
        //运行场景中如果连续将1个场景引入多次，会出现运行结果合并的情况。
        //为了解决这种问题，在转hashTree的时候给场景放了个新ID，前台加载解析的时候也要做处理
        let scenarioId = "";
        let scenarioName = "";
        if (item.scenario) {
          let scenarioArr = JSON.parse(item.scenario);
          if (scenarioArr.length > 1) {
            let scenarioIdArr = scenarioArr[0].split("_");
            scenarioId = scenarioIdArr[0];
            scenarioName = scenarioIdArr[1];
          }
        }
        // 循环构建子节点
        let node = {
          label: nodeArray[0],
          value: item,
          children: []
        };
        if (item.subRequestResults && item.subRequestResults.length > 0) {
          let itemChildren = this.deepFormatTreeNode(item.subRequestResults);
          node.children = itemChildren;
        }
        children.push(node);
        children.forEach(itemNode => {
          returnChildren.push(itemNode);
        });

      });
      return returnChildren;
    },
    recursiveSorting(arr) {
      for (let i in arr) {
        if (arr[i]) {
          arr[i].index = Number(i) + 1;
          if (arr[i].children && arr[i].children.length > 0) {
            this.recursiveSorting(arr[i].children);
          }
        }
      }
    },
    sort(scenarioDefinition) {
      for (let i in scenarioDefinition) {
        // 排序
        if (scenarioDefinition[i]) {
          scenarioDefinition[i].index = Number(i) + 1;
          if (scenarioDefinition[i].children && scenarioDefinition[i].children.length > 0) {
            this.recursiveSorting(scenarioDefinition[i].children);
          }
        }
      }
    },
    getReport() {
      this.init();
      if (this.isTemplate) {
        // 测试计划报告导出
        this.report = this.templateReport;
        this.buildReport();
      } else if (this.isShare) {
        getShareScenarioReport(this.shareId, this.reportId, (data) => {
          this.checkReport(data);
          this.handleGetScenarioReport(data);
        });
      } else {
        getScenarioReport(this.reportId, (data) => {
          this.checkReport(data);
          this.handleGetScenarioReport(data);
        });
      }
    },
    checkReport(data) {
      if (!data) {
        this.$emit('reportNotExist');
      }
    },
    handleGetScenarioReport(data) {
      if (data) {
        this.report = data;
        if (this.report.reportVersion && this.report.reportVersion > 1) {
          this.report.status = data.status;
          if (!this.isNotRunning) {
            setTimeout(this.getReport, 2000)
          } else {
            if (data.content) {
              let report = JSON.parse(data.content);
              this.content = report;
              this.fullTreeNodes = report.steps;
              this.content.console = report.console;
              this.content.error = report.error;
              this.content.success = (report.total - report.error - report.errorCode);
              this.totalTime = report.totalTime;
            }
            this.loading = false;
          }
        } else {
          this.buildReport();
        }
      } else {
        this.$emit('invisible');
        this.$warning(this.$t('commons.report_delete'));
      }
    },
    buildReport() {
      if (this.report) {
        if (this.isNotRunning) {
          try {
            this.content = JSON.parse(this.report.content);
            if (!this.content) {
              this.content = {scenarios: []};
            }
            this.formatResult(this.content);
          } catch (e) {
            throw e;
          }
          this.getFails();
          this.computeTotalTime();
          this.loading = false;
        } else {
          setTimeout(this.getReport, 2000)
        }
      } else {
        this.loading = false;
        this.$error(this.$t('api_report.not_exist'));
      }
    },
    getFails() {
      if (this.isNotRunning) {
        this.fails = [];
        let array = [];
        this.totalTime = 0
        if (this.content.scenarios) {
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
                  array.push(request);
                }
              })
            }
          })
        }
        this.formatTree(array, this.failsTreeNodes);
        this.sort(this.failsTreeNodes);
      }
    },
    computeTotalTime() {
      if (this.content.scenarios) {
        let startTime = 0;
        let endTime = 0;
        let requestTime = 0;
        this.content.scenarios.forEach((scenario) => {
          scenario.requestResults.forEach((request) => {
            if (request.startTime && Number(request.startTime)) {
              startTime = request.startTime;
            }
            if (request.endTime && Number(request.endTime)) {
              endTime = request.endTime;
            }
            let resTime;
            if (startTime === 0 || endTime === 0) {
              resTime = 0
            } else {
              resTime = endTime - startTime
            }
            requestTime = requestTime + resTime;
          })
        })
        this.totalTime = requestTime
      }
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
    formatExportApi(array, scenario) {
      array.forEach(item => {
        if (this.stepFilter && this.stepFilter.get("AllSamplerProxy").indexOf(item.type) !== -1) {
          if(item.errorCode){
            item.value.errorCode = item.errorCode;
          }
          scenario.requestResults.push(item.value);
        }
        if (item.children && item.children.length > 0) {
          this.formatExportApi(item.children, scenario);
        }
      })
    },
    handleExport() {
      if (this.report.reportVersion && this.report.reportVersion > 1) {
        if (this.report.reportType === 'API_INTEGRATED') {
          let scenario = {name: "", requestResults: []};
          this.content.scenarios = [scenario];
          this.formatExportApi(this.fullTreeNodes, scenario);
        } else {
          if (this.fullTreeNodes) {
            this.fullTreeNodes.forEach(item => {
              if (item.type === "scenario") {
                let scenario = {name: item.label, requestResults: []};
                if (this.content.scenarios && this.content.scenarios.length > 0) {
                  this.content.scenarios.push(scenario);
                } else {
                  this.content.scenarios = [scenario];
                }
                this.formatExportApi(item.children, scenario);
              }
            })
          }
        }
      }
      this.reportExportVisible = true;
      let reset = this.exportReportReset;
      this.$nextTick(() => {
        windowPrint('apiTestReport', 0.57);
        reset();
      });
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
    exportReportReset() {
      this.$router.go(0);
    }
  },

  created() {
    this.getReport();
  },

  computed: {
    path() {
      return "/api/test/edit?id=" + this.report.testId;
    },
    isNotRunning() {
      return "Running" !== this.report.status;
    },
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

.report-console {
  height: calc(100vh - 270px);
  overflow-y: auto;
}

.export-button {
  float: right;
}

.scenario-result .icon.is-active {
  transform: rotate(90deg);
}
</style>
