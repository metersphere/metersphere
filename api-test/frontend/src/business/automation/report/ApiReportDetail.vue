<template>
  <div>
    <ms-container v-loading="loading || reportExportVisible">
      <ms-main-container>
        <el-card>
          <section class="report-container" v-if="this.report.testId">
            <!-- header -->
            <ms-api-report-view-header
              :show-cancel-button="showCancel"
              :show-rerun-button="showRerunButton"
              :is-plan="isPlan"
              :is-template="isTemplate"
              :debug="debug"
              :report="report"
              :project-env-map="projectEnvMap"
              :mode="mode"
              :pool-name="poolName"
              :is-share="isShare"
              @reportExport="handleExport"
              @reportSave="handleSave" />
            <!-- content -->
            <main v-if="isNotRunning">
              <!-- content header chart -->
              <ms-metric-chart :content="content" :totalTime="totalTime" :report="report" />

              <el-tabs v-model="activeName" @tab-click="handleClick" style="min-width: 1200px">
                <!-- all step-->
                <el-tab-pane label="All" name="total">
                  <ms-infinite-scroll-scenario-results
                    :treeData="fullTreeNodes"
                    :console="content.console"
                    :report="report"
                    :is-share="isShare"
                    :share-id="shareId"
                    v-on:requestResult="requestResult"
                    ref="resultsTree" />
                </el-tab-pane>
                <!-- fail step -->
                <el-tab-pane name="fail">
                  <template slot="label"> Error</template>
                  <ms-infinite-scroll-scenario-results
                    v-on:requestResult="requestResult"
                    :console="content.console"
                    :report="report"
                    :is-share="isShare"
                    :share-id="shareId"
                    :treeData="errorTreeNodes"
                    ref="failsTree"
                    :errorReport="content.error" />
                </el-tab-pane>
                <!--error step -->
                <el-tab-pane name="errorReport" v-if="content.errorCode > 0">
                  <template slot="label">
                    <span class="fail" style="color: #f6972a"> FakeError </span>
                  </template>
                  <ms-infinite-scroll-scenario-results
                    v-on:requestResult="requestResult"
                    :report="report"
                    :is-share="isShare"
                    :share-id="shareId"
                    :console="content.console"
                    :treeData="fakeErrorTreeNodes"
                    ref="errorReportTree" />
                </el-tab-pane>
                <!-- Not performed step -->
                <el-tab-pane name="unExecute" v-if="content.unExecute > 0">
                  <template slot="label">
                    <span class="fail" style="color: #9c9b9a"> Pending </span>
                  </template>
                  <ms-infinite-scroll-scenario-results
                    v-on:requestResult="requestResult"
                    :report="report"
                    :is-share="isShare"
                    :share-id="shareId"
                    :console="content.console"
                    :treeData="unExecuteTreeNodes"
                    ref="unExecuteTree" />
                </el-tab-pane>
                <!-- console -->
                <el-tab-pane name="console">
                  <template slot="label">
                    <span class="console">Console</span>
                  </template>
                  <ms-code-edit
                    :mode="'text'"
                    :read-only="true"
                    :data.sync="content.console"
                    height="calc(100vh - 500px)" />
                </el-tab-pane>
              </el-tabs>
            </main>
          </section>
        </el-card>
      </ms-main-container>
    </ms-container>

    <!--export report-->
    <ms-api-report-export
      v-if="reportExportVisible"
      id="apiTestReport"
      :project-env-map="projectEnvMap"
      :title="report.name"
      :content="content"
      :mode="mode"
      :pool-name="poolName"
      :report="report"
      :total-time="totalTime" />
  </div>
</template>

<script>
import MsRequestResult from './components/RequestResult';
import MsRequestResultTail from './components/RequestResultTail';
import MsScenarioResult from './components/ScenarioResult';
import MsMetricChart from './components/MetricChart';
import MsScenarioResults from './components/ScenarioResults';
import MsContainer from 'metersphere-frontend/src/components/MsContainer';
import MsMainContainer from 'metersphere-frontend/src/components/MsMainContainer';
import MsApiReportExport from './ApiReportExport';
import MsApiReportViewHeader from './ApiReportViewHeader';
import MsInfiniteScrollScenarioResults from '@/business/automation/report/components/InfiniteScrollScenarioResults.vue';
import { RequestFactory } from '../../definition/model/ApiTestModel';
import { getCurrentProjectID } from 'metersphere-frontend/src/utils/token';
import { getUUID } from 'metersphere-frontend/src/utils';
import {
  getScenarioReport,
  getScenarioReportDetail,
  getSharePlanScenarioReport,
  getShareScenarioReport,
  reportReName,
} from '../../../api/scenario-report';
import { STEP } from '../../automation/scenario/Setting';
import MsCodeEdit from 'metersphere-frontend/src/components/MsCodeEdit';
import print from 'print-js';

export default {
  name: 'MsApiReport',
  components: {
    MsApiReportViewHeader,
    MsApiReportExport,
    MsMainContainer,
    MsCodeEdit,
    MsContainer,
    MsScenarioResults,
    MsRequestResultTail,
    MsMetricChart,
    MsScenarioResult,
    MsRequestResult,
    MsInfiniteScrollScenarioResults,
  },
  data() {
    return {
      activeName: 'total',
      content: {},
      report: {},
      loading: true,
      fails: [],
      failsTreeNodes: [],
      totalTime: 0,
      isRequestResult: false,
      descFlag: false,
      request: {},
      isActive: false,
      scenarioName: null,
      reportExportVisible: false,
      requestType: undefined,
      fullTreeNodes: [],
      errorTreeNodes: [],
      unExecuteTreeNodes: [],
      fakeErrorTreeNodes: [],
      showRerunButton: false,
      stepFilter: new STEP(),
      exportReportIsOk: false,
      tempResult: [],
      projectEnvMap: {},
      showCancel: false,
      poolName: '',
      mode: '',
    };
  },
  activated() {
    this.isRequestResult = false;
    this.descFlag = false;
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
    isPlan: {
      type: Boolean,
      default: false,
    },
    showCancelButton: {
      type: Boolean,
      default: false,
    },
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
    },
  },
  methods: {
    filter(index) {
      if (index === '1') {
        //查询失败的步骤
        this.initFilterTreeNodes('ERROR');
      } else if (this.activeName === 'errorReport') {
        this.initFilterTreeNodes('FAKE_ERROR');
      } else if (this.activeName === 'unExecute') {
        this.initFilterTreeNodes('UN_EXECUTE');
      }
    },
    initFilterTreeNodes(status) {
      if (this.fullTreeNodes.length > 0) {
        let filteredTreeNodeArr = [];
        for (let i = 0; i < this.fullTreeNodes.length; i++) {
          let node = this.filterNodes(this.fullTreeNodes[i], status);
          if (node) {
            filteredTreeNodeArr.push(node);
          }
        }
        if (status === 'ERROR') {
          this.errorTreeNodes = filteredTreeNodeArr;
        } else if (status === 'FAKE_ERROR') {
          this.fakeErrorTreeNodes = filteredTreeNodeArr;
        } else if (status === 'UN_EXECUTE') {
          this.unExecuteTreeNodes = filteredTreeNodeArr;
        }
      }
    },
    filterNodes(node, status) {
      if (status === 'ERROR' || status === 'FAKE_ERROR' || status === 'UN_EXECUTE') {
        let data = { ...node };
        if (!data.value && (!data.children || data.children.length === 0)) {
          return null;
        }
        if (data.children.length > 0) {
          let filteredChildren = [];
          for (let i = 0; i < data.children.length; i++) {
            let filteredNode = this.filterNodes(data.children[i], status);
            if (filteredNode) {
              filteredChildren.push(filteredNode);
            }
          }
          data.children = filteredChildren;
        }
        if (data.children.length > 0) {
          return data;
        } else {
          if (status === 'FAKE_ERROR') {
            if (data.errorCode && data.errorCode !== '' && data.value.status === 'FAKE_ERROR') {
              return data;
            }
          } else if (status === 'UN_EXECUTE') {
            if (data.value && data.value.status === 'PENDING') {
              return data;
            }
          } else if (status === 'ERROR') {
            if (data.totalStatus !== 'FAKE_ERROR' && data.value && data.value.error > 0) {
              return data;
            }
          }
        }
      }
      return null;
    },
    init() {
      this.loading = true;
      this.projectEnvMap = {};
      this.content = {};
      this.fails = [];
      this.report = {};
      this.fullTreeNodes = [];
      this.failsTreeNodes = [];
      this.isRequestResult = false;
      this.activeName = 'total';
      this.showRerunButton = false;
      if (
        this.$route &&
        this.$route.path.startsWith('/api/automation/report') &&
        this.$route.query &&
        this.$route.query.list
      ) {
        this.$nextTick(() => {
          this.showCancel = true;
        });
      }
    },
    rerunVerify() {
      if (this.fullTreeNodes && this.fullTreeNodes.length > 0 && !this.isShare) {
        this.fullTreeNodes.forEach((item) => {
          item.redirect = true;
          if (
            item.totalStatus === 'FAIL' ||
            item.totalStatus === 'ERROR' ||
            item.unExecuteTotal > 0 ||
            (item.type === 'API' && item.totalStatus === 'PENDING')
          ) {
            this.showRerunButton = true;
          }
        });
      }
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
      let resMap = new Map();
      let array = [];
      if (res && res.scenarios) {
        res.scenarios.forEach((item) => {
          if (item && item.requestResults) {
            item.requestResults.forEach((req) => {
              req.responseResult.console = res.console;
              resMap.set(req.id + req.name, req);
              req.name = item.name + '^@~@^' + req.name + 'UUID=' + getUUID();
              array.push(req);
            });
          }
        });
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
        let scenarioId = '';
        let scenarioName = '';
        if (item.scenario) {
          let scenarioArr = JSON.parse(item.scenario);
          if (scenarioArr.length > 1) {
            let scenarioIdArr = scenarioArr[0].split('_');
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
          if (i !== nodeArray.length - 1) {
            node.children = [];
          } else {
            if (item.subRequestResults && item.subRequestResults.length > 0) {
              let itemChildren = this.deepFormatTreeNode(item.subRequestResults);
              node.children = itemChildren;
              if (node.label.indexOf('UUID=')) {
                node.label = node.label.split('UUID=')[0];
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
                let childId = '';
                let childName = '';
                if (children[j].value && children[j].value.scenario) {
                  let scenarioArr = JSON.parse(children[j].value.scenario);
                  if (scenarioArr.length > 1) {
                    let childArr = scenarioArr[0].split('_');
                    childId = childArr[0];
                    if (childArr.length > 1) {
                      childName = childArr[1];
                    }
                  }
                }
                if (scenarioId === '') {
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
                children = i === nodeArray.length - 1 ? children : children[j].children;
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
            children = i === nodeArray.length - 1 ? children : children[children.length - 1].children;
          }
        }
      });
    },

    deepFormatTreeNode(array) {
      let returnChildren = [];
      array.map((item) => {
        let children = [];
        let key = item.name.split('^@~@^')[0];
        let nodeArray = key.split('<->');
        //运行场景中如果连续将1个场景引入多次，会出现运行结果合并的情况。
        //为了解决这种问题，在转hashTree的时候给场景放了个新ID，前台加载解析的时候也要做处理
        let scenarioId = '';
        let scenarioName = '';
        if (item.scenario) {
          let scenarioArr = JSON.parse(item.scenario);
          if (scenarioArr.length > 1) {
            let scenarioIdArr = scenarioArr[0].split('_');
            scenarioId = scenarioIdArr[0];
            scenarioName = scenarioIdArr[1];
          }
        }
        // 循环构建子节点
        let node = {
          label: nodeArray[0],
          value: item,
          children: [],
        };
        if (item.subRequestResults && item.subRequestResults.length > 0) {
          let itemChildren = this.deepFormatTreeNode(item.subRequestResults);
          node.children = itemChildren;
        }
        children.push(node);
        children.forEach((itemNode) => {
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
    getReportByExport() {
      if (this.exportReportIsOk) {
        this.startExport();
      } else {
        getScenarioReportDetail(this.reportId).then((res) => {
          let data = res.data;
          if (data && data.content) {
            let report = JSON.parse(data.content);
            if (report.projectEnvMap) {
              this.projectEnvMap = report.projectEnvMap;
            }
            this.content = report;
            this.fullTreeNodes = report.steps;
            this.content.console = report.console;
            this.content.error = report.error;
            let successCount = report.total - report.error - report.errorCode - report.unExecute;
            this.content.success = successCount;
            this.totalTime = report.totalTime;
          }
          this.exportReportIsOk = true;
          setTimeout(this.startExport, 500);
        });
      }
    },
    getReport() {
      this.init();
      if (this.isTemplate) {
        // 测试计划报告导出
        if (this.templateReport) {
          this.handleGetScenarioReport(this.templateReport);
        } else {
          this.report = this.templateReport;
          this.buildReport();
        }
      } else if (this.isShare) {
        if (this.isPlan) {
          getSharePlanScenarioReport(this.shareId, this.reportId).then((res) => {
            let data = res.data;
            if (data) {
              this.checkReport(data);
              this.handleGetScenarioReport(data);
            }
          });
        } else if (this.reportId) {
          getShareScenarioReport(this.shareId, this.reportId).then((res) => {
            let data = res.data;
            if (data) {
              this.checkReport(data);
              this.handleGetScenarioReport(data);
            }
          });
        }
      } else if (this.reportId) {
        getScenarioReport(this.reportId).then((res) => {
          let data = res.data;
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
          if ('RUNNING' === data.status && !this.descFlag) {
            setTimeout(this.getReport, 2000);
          } else {
            if (data.content) {
              let report = JSON.parse(data.content);
              this.content = report;
              if (report.projectEnvMap) {
                this.projectEnvMap = report.projectEnvMap;
              }
              if (report.poolName) {
                this.poolName = report.poolName;
              }
              this.mode = report.mode;
              this.fullTreeNodes = report.steps;
              this.content.console = report.console;
              this.content.error = report.error;
              let successCount = report.total - report.error - report.errorCode - report.unExecute;
              this.content.success = successCount;
              this.totalTime = report.totalTime;
            }
            // 增加失败重跑校验
            if (
              (this.report && this.report.reportType === 'SCENARIO_INTEGRATED') ||
              this.report.reportType === 'API_INTEGRATED'
            ) {
              this.rerunVerify();
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
    buildReport() {
      if (this.report) {
        if (this.isNotRunning) {
          this.content = JSON.parse(this.report.content);
          if (!this.content) {
            this.content = { scenarios: [] };
          }
          this.formatResult(this.content);
          this.getFails();
          this.computeTotalTime();
          this.loading = false;
        } else {
          setTimeout(this.getReport, 2000);
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
        this.totalTime = 0;
        if (this.content.scenarios) {
          this.content.scenarios.forEach((scenario) => {
            this.totalTime = this.totalTime + Number(scenario.responseTime);
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
              });
            }
          });
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
              resTime = 0;
            } else {
              resTime = endTime - startTime;
            }
            requestTime = requestTime + resTime;
          });
        });
        this.totalTime = requestTime;
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
      array.forEach((item) => {
        if (this.stepFilter && this.stepFilter.get('AllSamplerProxy').indexOf(item.type) !== -1) {
          if (item.errorCode) {
            item.value.errorCode = item.errorCode;
          }
          scenario.requestResults.push(item.value);
        }
        if (item.children && item.children.length > 0) {
          this.formatExportApi(item.children, scenario);
        }
      });
    },
    handleExport() {
      this.getReportByExport();
    },
    startExport() {
      if (this.report.reportVersion && this.report.reportVersion > 1) {
        if (this.report.reportType === 'API_INTEGRATED' || this.report.reportType === 'UI_INTEGRATED') {
          let scenario = { name: '', requestResults: [] };
          this.content.scenarios = [scenario];
          this.formatExportApi(this.fullTreeNodes, scenario);
        } else {
          if (this.fullTreeNodes) {
            this.fullTreeNodes.forEach((item) => {
              if (item.type === 'scenario') {
                let scenario = { name: item.label, requestResults: [] };
                if (this.content.scenarios && this.content.scenarios.length > 0) {
                  this.content.scenarios.push(scenario);
                } else {
                  this.content.scenarios = [scenario];
                }
                this.formatExportApi(item.children, scenario);
              }
            });
          }
        }
      }
      if (this.content.scenarios && this.content.scenarios[0].requestResults) {
        this.content.scenarios[0].requestResults.push({ responseResult: {} });
      }
      this.reportExportVisible = true;
      let reset = this.exportReportReset;
      let name = this.report.name;
      name = this.encodeSearchKey(name);
      this.$nextTick(() => {
        setTimeout(() => {
          this.printHTML();
          reset();
        }, 5000);
      });
    },
    printHTML() {
      function closeFuc() {
        location.reload();
      }

      print({
        printable: 'apiTestReport',
        type: 'html',
        maxWidth: 1200,
        documentTitle: '',
        scanStyles: true,
        header: '',
        targetStyles: ['*'], // 打印内容使用所有HTML样式，没有设置这个属性/值，设置分页打印没有效果
        onPrintDialogClose: closeFuc,
      });
    },
    // 对报告名称中的特殊字符进行编码
    encodeSearchKey(key) {
      const encodeArr = [
        {
          code: '%',
          encode: '%25',
        },
        {
          code: '?',
          encode: '%3F',
        },
        {
          code: '#',
          encode: '%23',
        },
        {
          code: '&',
          encode: '%26',
        },
        {
          code: '=',
          encode: '%3D',
        },
      ];
      return key.replace(/[%?#&=]/g, ($, index, str) => {
        for (const k of encodeArr) {
          if (k.code === $) {
            return k.encode;
          }
        }
      });
    },
    handleSave() {
      if (!this.report.name) {
        this.$warning(this.$t('api_test.automation.report_name_info'));
        return;
      }
      this.loading = true;
      reportReName({
        id: this.report.id,
        name: this.report.name,
        reportType: this.report.reportType,
      }).then(
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
    exportReportReset() {
      this.reportExportVisible = false;
      this.loading = false;
    },
    handleProjectChange() {
      this.$router.push('/api/automation/report');
    },
  },

  created() {
    this.descFlag = false;
    this.showCancel = this.showCancelButton;
    this.getReport();
    if (this.$EventBus) {
      this.$EventBus.$on('projectChange', this.handleProjectChange);
    }
  },
  destroyed() {
    this.descFlag = true;
    if (this.$EventBus) {
      this.$EventBus.$off('projectChange', this.handleProjectChange);
    }
  },
  computed: {
    path() {
      return '/api/test/edit?id=' + this.report.testId;
    },
    isNotRunning() {
      return 'RUNNING' !== this.report.status;
    },
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
  height: calc(100vh - 70px);
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

.report-body {
  min-width: 750px !important;
}

.target-node-item {
  background: #ffffff;
}
</style>
