<template>
  <ms-container>
    <ms-main-container>
      <el-card class="report-body">
        <section class="report-container">
          <div style="margin-top: 10px">
            <ms-api-report-view-header
              :show-cancel-button="false"
              :debug="debug"
              :export-flag="exportFlag"
              :report="report"
              :project-env-map="projectEnvMap"
              @reportExport="handleExport"
              @reportSave="handleSave" />
          </div>
          <main>
            <ms-metric-chart :content="content" :totalTime="totalTime" :report="report" v-if="!loading" />
            <div>
              <el-tabs v-model="activeName" @tab-click="handleClick">
                <el-tab-pane label="All" name="total">
                  <ms-scenario-results
                    :treeData="fullTreeNodes"
                    :report="report"
                    :default-expand="true"
                    :console="content.console"
                    v-on:requestResult="requestResult" />
                </el-tab-pane>
                <el-tab-pane name="fail">
                  <template slot="label"> Error </template>
                  <ms-scenario-results
                    :console="content.console"
                    :report="report"
                    :treeData="fullTreeNodes"
                    v-on:requestResult="requestResult"
                    ref="failsTree" />
                </el-tab-pane>
                <el-tab-pane name="errorReport" v-if="content.errorCode > 0">
                  <template slot="label">
                    <span class="fail" style="color: #f6972a"> FakeError </span>
                  </template>
                  <ms-scenario-results
                    v-on:requestResult="requestResult"
                    :console="content.console"
                    :treeData="fullTreeNodes"
                    ref="errorReportTree" />
                </el-tab-pane>
                <el-tab-pane name="unExecute" v-if="content.unExecute > 0">
                  <template slot="label">
                    <span class="fail" style="color: #9c9b9a"> Pending </span>
                  </template>
                  <ms-scenario-results
                    v-on:requestResult="requestResult"
                    :report="report"
                    :console="content.console"
                    :treeData="fullTreeNodes"
                    ref="unExecuteTree" />
                </el-tab-pane>
                <el-tab-pane name="console">
                  <template slot="label">
                    <span class="console">Console</span>
                  </template>
                  <pre>{{ content.console }}</pre>
                </el-tab-pane>
              </el-tabs>
            </div>
            <ms-api-report-export
              :title="report.testName"
              :content="content"
              :total-time="totalTime"
              :project-env-map="projectEnvMap"
              id="apiTestReport"
              v-if="reportExportVisible" />
          </main>
        </section>
      </el-card>
    </ms-main-container>
  </ms-container>
</template>

<script>
import {baseSocket} from '@/api/base-network';
import MsRequestResult from './components/RequestResult';
import MsRequestResultTail from './components/RequestResultTail';
import MsScenarioResult from './components/ScenarioResult';
import MsMetricChart from './components/MetricChart';
import MsScenarioResults from './components/ScenarioResults';
import MsContainer from 'metersphere-frontend/src/components/MsContainer';
import MsMainContainer from 'metersphere-frontend/src/components/MsMainContainer';
import MsApiReportExport from './ApiReportExport';
import MsApiReportViewHeader from './ApiReportViewHeader';
import {RequestFactory} from '../../definition/model/ApiTestModel';
import {getCurrentProjectID} from 'metersphere-frontend/src/utils/token';
import {windowPrint} from 'metersphere-frontend/src/utils';
import {STEP} from '../scenario/Setting';
import {getScenarioReport, updateReport} from '@/api/scenario-report';

export default {
  name: 'SyncApiReportDetail',
  components: {
    MsApiReportViewHeader,
    MsApiReportExport,
    MsMainContainer,
    MsContainer,
    MsScenarioResults,
    MsRequestResultTail,
    MsMetricChart,
    MsScenarioResult,
    MsRequestResult,
  },
  data() {
    return {
      activeName: 'total',
      content: { total: 0, scenarioTotal: 1 },
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
      debugResult: new Map(),
      scenarioMap: new Map(),
      exportFlag: false,
      messageWebSocket: {},
      websocket: {},
      stepFilter: new STEP(),
      tempResult: [],
      projectEnvMap: {},
    };
  },
  activated() {
    this.isRequestResult = false;
  },
  created() {},
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
    infoDb: Boolean,
    debug: Boolean,
    scenario: {},
    scenarioId: String,
    isUi: {
      type: Boolean,
      default: false,
    },
  },
  methods: {
    initTree() {
      this.fullTreeNodes = [];
      let obj = {
        resId: 'root',
        index: 1,
        label: this.scenario.name,
        value: {
          responseResult: {},
          unexecute: true,
          testing: false,
          status: 'PENDING',
        },
        children: [],
        unsolicited: true,
      };
      this.formatContent(this.scenario.scenarioDefinition.hashTree, obj, '', 'root');
      this.fullTreeNodes.push(obj);
    },
    compare() {
      return function (a, b) {
        let v1 = a.value.sort;
        let v2 = b.value.sort;
        return v1 - v2;
      };
    },
    getType(type) {
      switch (type) {
        case 'LoopController':
          return this.$t('api_test.automation.loop_controller');
        case 'TransactionController':
          return this.$t('api_test.automation.transaction_controller');
        case 'ConstantTimer':
          return this.$t('api_test.automation.wait_controller');
        case 'IfController':
          return this.$t('api_test.automation.if_controller');
      }
      return type;
    },
    margeTransaction(item, arr) {
      arr.forEach((subItem) => {
        if (item.resId === subItem.resourceId) {
          item.value = subItem;
          item.testing = false;
          item.debug = true;
        }
        if (subItem.subRequestResults && subItem.subRequestResults.length > 0) {
          this.margeTransaction(item, subItem.subRequestResults);
        }
      });
    },
    formatContent(hashTree, tree, fullPath, pid) {
      if (hashTree) {
        hashTree.forEach((item) => {
          if (item.enable) {
            item.parentIndex = fullPath ? fullPath + '_' + item.index : item.index;
            let name = item.name ? item.name : this.getType(item.type);
            let id = item.type === 'JSR223Processor' || !item.id ? item.resourceId : item.id;
            let obj = {
              pid: pid,
              resId: id + '_' + item.parentIndex,
              index: Number(item.index),
              label: name,
              value: {
                name: name,
                responseResult: {},
                unexecute: true,
                testing: false,
                status: 'PENDING',
              },
              children: [],
              unsolicited: true,
            };
            tree.children.push(obj);
            if (this.stepFilter.get('AllSamplerProxy').indexOf(item.type) !== -1) {
              obj.unsolicited = false;
              obj.type = item.type;
            } else if (item.type === 'scenario') {
              this.content.scenarioTotal += 1;
            }
            if (
              item.hashTree &&
              item.hashTree.length > 0 &&
              this.stepFilter &&
              this.stepFilter.get('AllSamplerProxy').indexOf(item.type) === -1
            ) {
              this.formatContent(item.hashTree, obj, item.parentIndex, obj.resId);
            }
          }
        });
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
      if (index === '1') {
        this.$refs.failsTree.filter(index);
      } else if (this.activeName === 'errorReport') {
        this.$refs.errorReportTree.filter('FAKE_ERROR');
      } else if (this.activeName === 'unExecute') {
        this.$refs.unExecuteTree.filter('PENDING');
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
      let url = '/api/scenario/report/update';
      this.result = updateReport(this.report).then(
        (response) => {
          this.$success(this.$t('commons.save_success'));
          this.loading = false;
          this.$emit('refresh');
        },
        (error) => {
          this.loading = false;
        }
      );
    },

    initMessageSocket() {
      this.messageWebSocket = baseSocket(this.reportId);
      this.messageWebSocket.onmessage = this.onMessage;
      this.messageWebSocket.onerror = this.cleanHeartBeat;
    },
    getReport() {
      getScenarioReport(this.reportId).then((response) => {
        this.report = response.data || {};
        if (response.data) {
          if (response.data.status === 'RUNNING') {
            setTimeout(this.getReport, 2000);
          } else {
            this.content = JSON.parse(response.data.content);
            if (!this.content) {
              this.content = { scenarios: [] };
            }
            if (this.content.projectEnvMap) {
              this.projectEnvMap = this.content.projectEnvMap;
            }
            this.content.error = this.content ? this.content.error : '';

            this.content.success =
              this.content.total - this.content.error - this.content.errorCode - this.content.unExecute;
            this.totalTime = this.content.totalTime;
            this.fullTreeNodes = this.content.steps;
            this.recursiveSorting(this.fullTreeNodes);
            this.reload();
          }
          if ('Running' !== this.report.status) {
            this.$emit('finish');
          }
        }
      });
    },
    checkOrder(origin) {
      if (!origin) {
        return;
      }
      if (Array.isArray(origin)) {
        this.sortChildren(origin);
        origin.forEach((v) => {
          if (v.children) {
            this.checkOrder(v.children);
          }
        });
      }
    },
    sortChildren(source) {
      if (!source) {
        return;
      }
      source.forEach((item) => {
        let children = item.children;
        if (children && children.length > 0) {
          let tempArr = new Array(children.length);
          let tempMap = new Map();

          for (let i = 0; i < children.length; i++) {
            if (!children[i].value || !children[i].value.startTime || children[i].value.startTime === 0) {
              //若没有value或未执行的，则step留在当前位置
              tempArr[i] = children[i];
              //进行标识
              tempMap.set(children[i].stepId, children[i]);
            }
          }

          //过滤出还没有指定好位置的step
          let arr = children
            .filter((m) => {
              return !tempMap.get(m.stepId);
            })
            .sort((m, n) => {
              //按时间排序
              return m.value.startTime - n.value.startTime;
            });

          //找出arr(已经有序，从头取即可)中时间最小的插入 tempArr 可用位置
          for (let j = 0, i = 0; j < tempArr.length; j++) {
            if (!tempArr[j]) {
              //占位
              tempArr[j] = arr[i];
              i++;
            }
            //重新排序
            tempArr[j].index = j + 1;
          }

          //赋值
          item.children = tempArr;
        }
      });
    },
    runningNodeChild(arr, resourceId) {
      arr.forEach((item) => {
        if (resourceId === item.resId) {
          item.value.testing = true;
        } else if (resourceId && resourceId.startsWith('result_')) {
          let data = JSON.parse(resourceId.substring(7));
          if (!data.status && data.error > 0) {
            data.status = 'ERROR';
          }
          if (!data.status && data.success) {
            data.status = 'SUCCESS';
          }
          if (data.method === 'Request' && data.subRequestResults && data.subRequestResults.length > 0) {
            this.margeTransaction(item, data.subRequestResults);
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
      });
    },
    runningEvaluation(resourceId) {
      if (this.fullTreeNodes) {
        this.fullTreeNodes.forEach((item) => {
          if (resourceId === item.resId) {
            item.value.testing = true;
          } else if (resourceId && resourceId.startsWith('result_')) {
            let data = JSON.parse(resourceId.substring(7));
            if (!data.status && data.error > 0) {
              data.status = 'ERROR';
            }
            if (!data.status && data.success) {
              data.status = 'SUCCESS';
            }
            if (data.method === 'Request' && data.subRequestResults && data.subRequestResults.length > 0) {
              data.subRequestResults.forEach((subItem) => {
                if (item.resId === subItem.resourceId) {
                  item.value = subItem;
                  item.testing = false;
                  item.debug = true;
                }
              });
            } else if (item.resId === data.resourceId) {
              item.value = data;
              item.testing = false;
              item.debug = true;
            }
          }
          if (item.children && item.children.length > 0) {
            this.runningNodeChild(item.children, resourceId);
          }
        });
      }
    },
    onMessage(e) {
      if (e.data) {
        this.runningEvaluation(e.data);
        this.sort(this.fullTreeNodes);
      }
      if (e && e.data === 'DEBUG_ERROR') {
        this.$error(this.$t('api_definition.debug_pool_warning'));
        this.messageWebSocket.close();
        this.cleanHeartBeat();
        this.$EventBus.$emit('hide', this.scenarioId);
        this.$emit('refresh', this.debugResult);
      }

      if (e.data && e.data.indexOf('MS_TEST_END') !== -1) {
        setTimeout(this.getReport, 3000);
        this.messageWebSocket.close();
        this.cleanHeartBeat();
        this.$EventBus.$emit('hide', this.scenarioId);
        this.$emit('refresh', this.debugResult);
      }
    },
    sort(stepArray) {
      for (let i in stepArray) {
        stepArray[i].index = Number(i) + 1;
        if (stepArray[i].children && stepArray[i].children.length > 0) {
          this.sort(stepArray[i].children);
        }
      }
    },

    cleanHeartBeat() {
      this.$emit('refresh', this.debugResult);
    },
    heartBeat() {
      let msg = {
        reportId: this.reportId,
        content: "i'm alive",
      };
      this.messageWebSocket.send(JSON.stringify(msg));
    },
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
};
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
  color: #f56c6c;
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

.report-body {
  min-width: 750px !important;
}
</style>
