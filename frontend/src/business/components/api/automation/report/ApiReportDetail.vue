<template>
  <ms-container v-loading="loading">
    <ms-main-container>
      <el-card>
        <section class="report-container" v-if="this.report.testId">
          <ms-api-report-view-header :debug="debug" :report="report" @reportExport="handleExport" @reportSave="handleSave"/>
          <main v-if="isNotRunning">
            <ms-metric-chart :content="content" :totalTime="totalTime"/>
            <div>
              <el-tabs v-model="activeName" @tab-click="handleClick">
                <el-tab-pane :label="$t('api_report.total')" name="total">
                  <ms-scenario-results :treeData="fullTreeNodes" :console="content.console" v-on:requestResult="requestResult"/>
                </el-tab-pane>
                <el-tab-pane name="fail">
                  <template slot="label">
                    <span class="fail">{{ $t('api_report.fail') }}</span>
                  </template>
                  <ms-scenario-results v-on:requestResult="requestResult" :console="content.console" :treeData="failsTreeNodes"/>
                </el-tab-pane>
              </el-tabs>
            </div>
            <ms-api-report-export v-if="reportExportVisible" id="apiTestReport" :title="report.testName"
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

export default {
  name: "MsApiReport",
  components: {
    MsApiReportViewHeader,
    MsApiReportExport,
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
      failsTreeNodes: [],
      totalTime: 0,
      isRequestResult: false,
      request: {},
      isActive: false,
      scenarioName: null,
      reportExportVisible: false,
      requestType: undefined,
      fullTreeNodes: [],
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
  },
  watch: {
    reportId() {
      this.getReport();
    }
  },
  methods: {
    init() {
      this.loading = true;
      this.report = {};
      this.content = {};
      this.fails = [];
      this.report = {};
      this.fullTreeNodes = [];
      this.failsTreeNodes = [];
      this.isRequestResult = false;
    },
    handleClick(tab, event) {
      this.isRequestResult = false
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
          if (i !== nodeArray.length) {
            node.children = [];
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
      if (this.reportId) {
        let url = "/api/scenario/report/get/" + this.reportId;
        this.$get(url, response => {
          this.report = response.data || {};
          if (response.data) {
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
        });
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
        let startTime = 99991611737506593;
        let endTime = 0;
        this.content.scenarios.forEach((scenario) => {
          scenario.requestResults.forEach((request) => {
            if (request.startTime && Number(request.startTime) < startTime) {
              startTime = request.startTime;
            }
            if (request.endTime && Number(request.endTime) > endTime) {
              endTime = request.endTime;
            }
          })
        })
        if (startTime < endTime) {
          this.totalTime = endTime - startTime + 100;
        }
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
    handleExport() {
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

.export-button {
  float: right;
}

.scenario-result .icon.is-active {
  transform: rotate(90deg);
}

</style>
