<template>
  <div>
    <ms-container v-if="renderComponent">
      <ms-aside-container v-show="isAsideHidden">
        <ms-api-module
          :show-operator="true"
          :default-protocol="defaultProtocol"
          :select-default-protocol="isSelectDefaultProtocol"
          @nodeSelectEvent="nodeChange"
          @protocolChange="handleProtocolChange"
          @refreshTable="refresh"
          @exportAPI="exportAPI"
          @debug="debug"
          @saveAsEdit="editApi"
          @setModuleOptions="setModuleOptions"
          @setNodeTree="setNodeTree"
          @enableTrash="enableTrash"
          @schedule="handleTabsEdit($t('api_test.api_import.timing_synchronization'), 'SCHEDULE')"
          :type="'edit'"
          page-source="definition"
          :is-trash-data="trashEnable"
          :total="total"
          :current-version="currentVersion"
          ref="nodeTree" />
      </ms-aside-container>

      <ms-main-container>
        <ms-environment-select
          :project-id="projectId"
          :is-read-only="false"
          :useEnvironment="useEnvironment"
          @setEnvironment="setEnvironment"
          class="ms-api-button"
          ref="environmentSelect" />
        <!-- 主框架列表 -->
        <el-tabs
          v-model="apiDefaultTab"
          @edit="closeConfirm"
          @tab-click="addTab"
          @tab-remove="removeTab"
          ref="mainTabs">
          <el-tab-pane name="trash" :closable="true" :label="$t('commons.trash')" v-if="trashEnable">
            <ms-tab-button
              v-if="this.trashTabInfo.type === 'list'"
              :active-dom.sync="trashActiveDom"
              :left-tip="$t('api_test.definition.api_title')"
              :right-tip="$t('api_test.definition.case_title')"
              :middle-button-enable="false"
              left-content="API"
              right-content="CASE">
              <template v-slot:version>
                <mx-version-select
                  v-xpack
                  :project-id="projectId"
                  :version-id="trashVersion"
                  @changeVersion="changeVersion" />
              </template>
              <!-- 列表集合 -->
              <ms-api-list
                v-if="trashActiveDom === 'left'"
                @runTest="runTest"
                @refreshTree="refreshTree"
                @getTrashApi="getTrashApi"
                :module-tree="nodeTree"
                :module-options="moduleOptions"
                :current-protocol="currentProtocol"
                :current-version="currentVersion"
                :visible="visible"
                :currentRow="currentRow"
                :select-node-ids="selectNodeIds"
                :trash-enable="true"
                :selectDataRange="selectDataRange"
                :is-read-only="isReadOnly"
                @changeSelectDataRangeAll="changeSelectDataRangeAll"
                @editApi="editApi"
                @handleCase="handleCase"
                @showExecResult="showExecResult"
                @refreshTable="refresh"
                :init-api-table-opretion="initApiTableOpretion"
                @updateInitApiTableOpretion="updateInitApiTableOpr"
                ref="trashApiList" />
              <!--测试用例列表-->
              <api-case-simple-list
                v-if="trashActiveDom === 'right'"
                :current-protocol="currentProtocol"
                :current-version="currentVersion"
                :visible="visible"
                :currentRow="currentRow"
                :select-node-ids="selectNodeIds"
                :trash-enable="true"
                :is-read-only="isReadOnly"
                @changeSelectDataRangeAll="changeSelectDataRangeAll"
                @handleCase="handleCase"
                @refreshTable="refresh"
                @showExecResult="showExecResult"
                ref="trashCaseList" />
            </ms-tab-button>
          </el-tab-pane>

          <el-tab-pane
            v-for="(item, index) in apiTabs"
            :key="item.name"
            :label="item.title"
            :closable="item.closable"
            :name="item.name">
            <el-tooltip
              v-if="index > 0"
              slot="label"
              effect="dark"
              :content="item.title"
              placement="bottom-start"
              class="ms-tab-name-width">
              <span>{{ item.title }}</span>
            </el-tooltip>
            <ms-tab-button
              v-if="item.type === 'list'"
              :active-dom.sync="activeDom"
              :left-tip="$t('api_test.definition.api_title')"
              :right-tip="$t('api_test.definition.doc_title')"
              :middle-tip="$t('api_test.definition.case_title')"
              left-content="API"
              middle-content="CASE"
              :right-content="$t('api_test.definition.doc_title')"
              :right-button-enable="currentProtocol === 'HTTP'"
              @changeTab="changeTab">
              <template v-slot:version>
                <mx-version-select v-xpack :project-id="projectId" @changeVersion="changeVersion" />
              </template>
              <!-- 列表集合 -->
              <ms-api-list
                v-if="activeDom === 'left' && !trashEnable"
                @getTrashApi="getTrashApi"
                :module-tree="nodeTree"
                :module-options="moduleOptions"
                :current-protocol="currentProtocol"
                :current-version="currentVersion"
                :visible="visible"
                :currentRow="currentRow"
                :select-node-ids="selectNodeIds"
                :trash-enable="false"
                :selectDataRange="selectDataRange"
                :is-read-only="isReadOnly"
                @runTest="runTest"
                @handleTestCase="handleTestCase"
                @refreshTree="refreshTree"
                @changeSelectDataRangeAll="changeSelectDataRangeAll"
                @editApiModule="editApiModule"
                @copyApi="copyApi"
                @handleCase="handleCase"
                @showExecResult="showExecResult"
                @refreshTable="refresh"
                :init-api-table-opretion="initApiTableOpretion"
                @updateInitApiTableOpretion="updateInitApiTableOpr"
                ref="apiDefList" />
              <!--测试用例列表-->
              <api-case-simple-list
                v-if="activeDom === 'middle'"
                :current-protocol="currentProtocol"
                :current-version="currentVersion"
                :visible="visible"
                :currentRow="currentRow"
                :select-node-ids="selectNodeIds"
                :trash-enable="false"
                :is-read-only="isReadOnly"
                @changeSelectDataRangeAll="changeSelectDataRangeAll"
                @handleCase="handleCase"
                @refreshTable="refresh"
                @showExecResult="showExecResult"
                ref="caseList" />
              <api-documents-page
                class="api-doc-page"
                v-if="activeDom === 'right' && currentProtocol === 'HTTP'"
                :project-id="projectId"
                :trash-enable="trashEnable"
                :version-id="currentVersion"
                :module-ids="selectNodeIds"
                ref="documentsPage" />
            </ms-tab-button>
            <!-- 添加/编辑测试窗口-->
            <div v-if="item.type === 'ADD' || item.type === 'TEST'" class="ms-api-div">
              <ms-edit-complete-container
                :syncTabs="syncTabs"
                @runTest="runTest"
                @saveApi="saveApi"
                @createRootModel="createRootModel"
                @editApi="editApi"
                @refresh="refresh"
                :current-api="item.api"
                :project-id="projectId"
                :currentProtocol="currentProtocol"
                :moduleOptions="moduleOptions"
                :activeDom="activeTab"
                @changeSelectDataRangeAll="changeSelectDataRangeAll"
                @handleCase="handleCase"
                @showExecResult="showExecResult"
                ref="apiConfig" />
            </div>
            <!-- 快捷调试 -->
            <div v-else-if="item.type === 'debug'" class="ms-api-schedule-div">
              <ms-debug-http-page
                :currentProtocol="currentProtocol"
                :testCase="item.api"
                @saveAs="editApi"
                @refreshModule="refreshModule"
                v-if="currentProtocol === 'HTTP'" />
              <ms-debug-jdbc-page
                :currentProtocol="currentProtocol"
                :testCase="item.api"
                @saveAs="editApi"
                @refreshModule="refreshModule"
                v-if="currentProtocol === 'SQL'" />
              <ms-debug-tcp-page
                :currentProtocol="currentProtocol"
                :testCase="item.api"
                :scenario="false"
                @saveAs="editApi"
                @refreshModule="refreshModule"
                v-if="currentProtocol === 'TCP'" />
              <ms-debug-dubbo-page
                :currentProtocol="currentProtocol"
                :testCase="item.api"
                @saveAs="editApi"
                @refreshModule="refreshModule"
                v-if="currentProtocol === 'DUBBO'" />
            </div>

            <!-- 定时任务 -->
            <div v-if="item.type === 'SCHEDULE'" class="ms-api-schedule-div">
              <api-schedule :param="param" :module-options="nodeTree" ref="apiSchedules" />
            </div>
          </el-tab-pane>

          <el-tab-pane name="add" v-if="hasPermission('PROJECT_API_DEFINITION:READ+CREATE_API')">
            <template v-slot:label>
              <el-dropdown @command="handleCommand">
                <el-button type="primary" plain icon="el-icon-plus" size="mini" />
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="debug" v-permission="['PROJECT_API_DEFINITION:READ+DEBUG']">
                    {{ $t('api_test.definition.request.fast_debug') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="ADD" v-permission="['PROJECT_API_DEFINITION:READ+CREATE_API']">
                    {{ $t('api_test.definition.request.title') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="CLOSE_ALL"
                    >{{ $t('api_test.definition.request.close_all_label') }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </template>
          </el-tab-pane>
        </el-tabs>
      </ms-main-container>
    </ms-container>
    <mock-edit-drawer
      :is-tcp="isTcp"
      :api-id="mockConfigData.apiId"
      :api-params="apiParams"
      @refreshMockInfo="refreshMockInfo"
      :mock-config-id="mockConfigData.id"
      ref="mockEditDrawer" />
  </div>
</template>
<script>
import { createDefinitionEnv, getDefinitionById, getDefinitionEnv } from '@/api/definition';
import { getProjectConfig } from '@/api/project';
import { getApiModuleByProjectIdAndProtocol, trashCount } from '@/api/definition-module';
import MsApiList from './components/list/ApiList';
import MsContainer from 'metersphere-frontend/src/components/MsContainer';
import MsMainContainer from 'metersphere-frontend/src/components/MsMainContainer';
import MsAsideContainer from 'metersphere-frontend/src/components/MsAsideContainer';
import MsDebugHttpPage from './components/debug/DebugHttpPage';
import MsDebugJdbcPage from './components/debug/DebugJdbcPage';
import MsDebugTcpPage from './components/debug/DebugTcpPage';
import MsDebugDubboPage from './components/debug/DebugDubboPage';

import MsRunTestHttpPage from './components/runtest/RunTestHTTPPage';
import MsRunTestTcpPage from './components/runtest/RunTestTCPPage';
import MsRunTestSqlPage from './components/runtest/RunTestSQLPage';
import MsRunTestDubboPage from './components/runtest/RunTestDubboPage';
import { getCurrentProjectID, getCurrentUser, getCurrentUserId } from 'metersphere-frontend/src/utils/token';
import { hasPermission } from 'metersphere-frontend/src/utils/permission';
import { getUUID } from 'metersphere-frontend/src/utils';
import MsApiModule from './components/module/ApiModule';
import ApiCaseSimpleList from './components/list/ApiCaseSimpleList';
import ApiDocumentsPage from '@/business/definition/components/list/ApiDocumentsPage';
import MsTableButton from 'metersphere-frontend/src/components/MsTableButton';
import MsTabButton from '@/business/commons/MsTabs';
import ApiSchedule from '@/business/definition/components/import/ApiSchedule';
import MsEditCompleteContainer from './components/EditCompleteContainer';
import MsEnvironmentSelect from './components/case/MsEnvironmentSelect';
import { PROJECT_ID, WORKSPACE_ID } from 'metersphere-frontend/src/utils/constants';
import { useApiStore } from '@/store';
import { buildTree } from 'metersphere-frontend/src/model/NodeTree';
import { createMockConfig, getMockApiParams, mockExpectConfig } from '@/api/api-mock';
import MockEditDrawer from '@/business/definition/components/mock/MockEditDrawer';
import {getUserDefaultApiType} from "metersphere-frontend/src/api/environment";

const store = useApiStore();
export default {
  name: 'ApiDefinition',
  computed: {
    isReadOnly() {
      return false;
    },
    projectId() {
      return getCurrentProjectID();
    },
    isSelectDefaultProtocol() {
      let selectDefaultProtocol = true;
      let routeParamObj = this.$route.params;
      if (routeParamObj) {
        let dataRange = routeParamObj.dataSelectRange;
        let dataType = routeParamObj.dataType;
        if (dataRange && typeof dataRange === 'string') {
          let selectParamArr = dataRange.split('edit:');
          if (selectParamArr.length === 2) {
            if (dataType === 'api') {
              selectDefaultProtocol = false;
            }
          }
        }
      } else {
        let dataRange = this.$route.params.dataSelectRange;
        let dataType = this.$route.params.dataType;
        if (dataRange && typeof dataRange === 'string') {
          let selectParamArr = dataRange.split('edit:');
          if (selectParamArr.length === 2) {
            if (dataType === 'api') {
              selectDefaultProtocol = false;
            }
          }
        }
      }
      return selectDefaultProtocol;
    },
  },
  components: {
    ApiSchedule,
    MsTabButton,
    MsTableButton,
    ApiCaseSimpleList,
    MsApiModule,
    MsApiList,
    MsMainContainer,
    MsContainer,
    MsAsideContainer,
    MsDebugHttpPage,
    MsRunTestHttpPage,
    MsDebugJdbcPage,
    MsDebugTcpPage,
    MsDebugDubboPage,
    MsRunTestTcpPage,
    MsRunTestSqlPage,
    MsRunTestDubboPage,
    ApiDocumentsPage,
    MsEditCompleteContainer,
    MsEnvironmentSelect,
    MockEditDrawer,
    MxVersionSelect: () => import('metersphere-frontend/src/components/version/MxVersionSelect'),
  },
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    currentRow: {
      type: Object,
    },
  },
  data() {
    return {
      redirectID: '',
      total: 0,
      renderComponent: true,
      selectDataRange: 'all',
      showCasePage: true,
      apiDefaultTab: 'default',
      currentProtocol: '',
      currentModule: null,
      selectNodeIds: [],
      currentApi: {},
      moduleOptions: [],
      defaultProtocol: null,
      trashEnable: false,
      apiTabs: [
        {
          title: this.$t('api_test.definition.api_title'),
          name: 'default',
          type: 'list',
          closable: false,
        },
      ],
      trashTabInfo: {
        title: this.$t('api_test.definition.api_title'),
        name: 'default',
        type: 'list',
        closable: false,
      },
      activeDom: 'left',
      trashActiveDom: 'left',
      syncTabs: [],
      nodeTree: [],
      currentModulePath: '',
      //影响API表格刷新的操作。 为了防止高频率刷新模块列表用。如果是模块更新而造成的表格刷新，则不回调模块刷新方法
      initApiTableOpretion: 'init',
      param: {},
      useEnvironment: String,
      activeTab: 'api',
      currentVersion: null,
      trashVersion: null,
      isAsideHidden: true,
      mockConfigData: {},
      apiParams: {},
      isTcp: false,
    };
  },
  activated() {
    this.$nextTick(() => {
      let routeParamObj = this.$route.params;
      if (routeParamObj) {
        if (routeParamObj.dataType === 'swagger') {
          this.openSwaggerScheduleTab();
        } else {
          let dataRange = routeParamObj.dataSelectRange;
          if (dataRange && dataRange.length > 0) {
            this.activeDom = 'middle';
          }
          let dataType = routeParamObj.dataType;
          if (dataType) {
            if (dataType === 'api') {
              this.activeDom = 'left';
            } else {
              this.activeDom = 'middle';
            }
          }
          this.selectNodeIds = [];
        }
      } else {
        let dataType = this.$route.params.dataType;
        if (dataType) {
          if (dataType === 'swagger') {
            this.openSwaggerScheduleTab();
          } else if (dataType === 'api') {
            this.activeDom = 'left';
          } else {
            this.activeDom = 'middle';
          }
        }
      }
    });
  },
  watch: {
    trashEnable() {
      this.selectNodeIds = [];
    },
    currentProtocol() {
      if (this.activeDom === 'right') {
        this.activeDom = 'left';
      }
      this.handleCommand('CLOSE_ALL');
    },
    selectNodeIds() {},
    redirectID() {
      this.renderComponent = false;
      this.$nextTick(() => {
        // 在 DOM 中添加 my-component 组件
        this.renderComponent = true;
      });
    },
    $route(to, from) {
      //  路由改变时，把接口定义界面中的 ctrl s 保存快捷键监听移除
      if (to.path.indexOf('/api/definition') === -1) {
        if (this.$refs && this.$refs.apiConfig) {
          this.$refs.apiConfig.forEach((item) => {
            item.removeListener();
          });
        }
      }
    },
    apiDefaultTab() {
      this.isAsideHidden = this.apiDefaultTab === 'default' || this.apiDefaultTab === 'trash';
    },
  },
  beforeCreate() {
    getUserDefaultApiType().then(response => {
      if (!this.currentProtocol) {
        this.currentProtocol = response.data;
      }
    })
  },
  created() {
    let routeParamObj = this.$route.params;
    if (routeParamObj) {
      let workspaceId = routeParamObj.workspaceId;
      if (workspaceId) {
        sessionStorage.setItem(WORKSPACE_ID, workspaceId);
      } else {
        if (this.$route.query.workspaceId) {
          workspaceId = this.$route.query.workspaceId;
          sessionStorage.setItem(WORKSPACE_ID, workspaceId);
        }
      }
      let projectId = routeParamObj.projectId;
      if (projectId) {
        sessionStorage.setItem(PROJECT_ID, projectId);
      } else {
        if (this.$route.query.projectId) {
          projectId = this.$route.query.projectId;
          sessionStorage.setItem(PROJECT_ID, projectId);
        }
      }
      // 通知过来的数据跳转到编辑
      if (routeParamObj.caseId) {
        this.activeDom = 'middle';
      }
    }
    this.getEnv();
  },
  mounted() {
    // 从场景路由进编辑页面
    this.initForwardData();
    // 通知过来的数据跳转到编辑
    if (this.$route.query.caseId) {
      this.activeDom = 'middle';
    }
    if (this.$route.query.mockId) {
      if (this.currentProtocol === 'TCP') {
        this.isTcp = true;
      }
      mockExpectConfig(this.$route.query.mockId).then((res) => {
        this.mockExpectConfigData = res.data;
        if (this.mockExpectConfigData.mockConfigId) {
          let mockParam = {};
          mockParam.id = this.mockExpectConfigData.mockConfigId;
          createMockConfig(mockParam).then((response) => {
            this.mockConfigData = response.data;
            this.searchApiParams(this.mockConfigData.apiId);
            this.$refs.mockEditDrawer.open(this.mockExpectConfigData);
          });
        }
      });
    }
  },
  methods: {
    removeTab(name) {
      if (name === 'trash') {
        this.selectNodeIds = [];
        this.trashEnable = false;
      }
    },
    openSwaggerScheduleTab() {
      //检查是否有开启的定时任务配置页，如果有的话直接跳转，不用再开启
      let scheduleTabName = '';
      if (this.apiTabs) {
        this.apiTabs.forEach((tab) => {
          if (tab.type === 'SCHEDULE') {
            scheduleTabName = tab.name;
          }
        });
      }
      if (scheduleTabName === '') {
        this.handleTabsEdit(this.$t('api_test.api_import.timing_synchronization'), 'SCHEDULE');
      } else {
        this.apiDefaultTab = scheduleTabName;
      }
    },
    setEnvironment(data) {
      if (data) {
        this.useEnvironment = data.id;
        store.useEnvironment = data.id;
        this.addEnv(data.id);
      }
    },
    addEnv(envId) {
      createDefinitionEnv({ userId: getCurrentUserId(), envId: envId }).then((response) => {});
    },
    getTrashApi() {
      trashCount(this.projectId, this.currentProtocol).then((response) => {
        this.total = response.data;
      });
    },
    getEnv() {
      getDefinitionEnv(getCurrentUserId(), getCurrentProjectID()).then((response) => {
        let env = response.data;
        if (env) {
          store.useEnvironment = env.envId;
          this.useEnvironment = env.envId;
        } else {
          store.useEnvironment = '';
          this.useEnvironment = '';
        }
      });
    },
    hasPermission,
    getPath(id, arr) {
      if (id === null) {
        return null;
      }
      if (arr) {
        arr.forEach((item) => {
          if (item.id === id) {
            this.currentModulePath = item.path;
          }
          if (item.children && item.children.length > 0) {
            this.getPath(id, item.children);
          }
        });
      }
    },
    changeRedirectParam(redirectIDParam) {
      this.redirectID = redirectIDParam;
    },
    addTab(tab) {
      this.trashEnable = tab.name === 'trash';
      if (tab.name === 'add') {
        getProjectConfig(this.projectId, '/API_QUICK_MENU').then((res) => {
          let projectData = res.data;
          if (projectData && projectData.apiQuickMenu === 'api') {
            this.handleTabAdd('ADD');
          } else {
            this.handleTabsEdit(this.$t('api_test.definition.request.fast_debug'), 'debug');
          }
        });
      } else if (tab.name === 'trash') {
        if (this.$refs.trashApiList) {
          this.$refs.trashApiList.initTable();
        }
        if (this.$refs.trashCaseList) {
          this.$refs.trashCaseList.initTable();
        }
      }
      if (this.$refs.apiConfig) {
        this.$refs.apiConfig.forEach((item) => {
          if (item) {
            item.removeListener();
          }
        }); //  删除所有tab的 ctrl + s 监听
        let tabs = this.apiTabs;
        let index = tabs.findIndex((item) => item.name === tab.name); //  找到当前选中tab的index
        if (index !== -1 && this.$refs.apiConfig[index - 1]) {
          this.$refs.apiConfig[index - 1].addListener(); //  为选中tab添加 ctrl + s 监听（index-1的原因是要除去第一个固有tab）
        }
      }
    },
    handleCommand(e) {
      switch (e) {
        case 'ADD':
          this.handleTabAdd(e);
          break;
        case 'TEST':
          this.handleTabsEdit(this.$t('commons.api'), e);
          break;
        case 'CLOSE_ALL':
          this.handleTabClose();
          break;
        default:
          this.handleTabsEdit(this.$t('api_test.definition.request.fast_debug'), 'debug');
          break;
      }
    },
    handleTabAdd(e) {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      let api = {
        name: '',
        status: 'Underway',
        method: 'GET',
        userId: this._getCurrentUserId(),
        url: '',
        protocol: this.currentProtocol,
        environmentId: '',
        remark: '',
        moduleId: 'default-module',
        modulePath: '/' + this.$t('commons.module_title'),
      };
      this.currentModulePath = '';
      if (this.nodeTree && this.nodeTree.length > 0) {
        api.moduleId = this.nodeTree[0].id;
        this.getPath(this.nodeTree[0].id, this.moduleOptions);
        api.modulePath = this.currentModulePath;
      }

      if (this.selectNodeIds && this.selectNodeIds.length > 0) {
        api.moduleId = this.selectNodeIds[0];
        this.getPath(this.selectNodeIds[0], this.moduleOptions);
        api.modulePath = this.currentModulePath;
      }
      this.handleTabsEdit(this.$t('api_test.definition.request.title'), e, api);
    },
    _getCurrentUserId() {
      const {id, userGroups} = getCurrentUser();
      if (userGroups) {
        // 是否是当前项目下的成员
        let index = userGroups.findIndex(ug => ug.sourceId === getCurrentProjectID());
        if (index !== -1) {
          return id;
        }
      }
      return '';
    },
    handleTabClose() {
      let tabs = this.apiTabs[0];
      let message = '';
      let tab = this.apiTabs;
      tab.forEach((t) => {
        if (
          t.type === 'ADD' &&
          t.api &&
          store.apiMap.has(t.api.id) &&
          (store.apiMap.get(t.api.id).get('responseChange') === true ||
            store.apiMap.get(t.api.id).get('requestChange') === true ||
            store.apiMap.get(t.api.id).get('fromChange') === true)
        ) {
          message += t.api.name + '，';
        } else if (t.type === 'ADD' && t.title === this.$t('api_test.definition.request.title')) {
          message += this.$t('api_test.definition.request.title') + '，';
        }
        if (t.type === 'ADD' && t.isCopy) {
          message += t.api.name + '，';
        }
      });
      if (message !== '') {
        this.$alert(
          this.$t('commons.api') +
            ' [ ' +
            message.substr(0, message.length - 1) +
            ' ] ' +
            this.$t('commons.confirm_info'),
          '',
          {
            confirmButtonText: this.$t('commons.confirm'),
            cancelButtonText: this.$t('commons.cancel'),
            callback: (action) => {
              if (action === 'confirm') {
                store.apiMap.clear();
                this.apiTabs = [];
                this.trashEnable = false;
                this.apiDefaultTab = tabs.name;
                this.apiTabs.push(tabs);
              }
            },
          }
        );
      } else {
        this.apiTabs = [];
        this.trashEnable = false;
        this.apiDefaultTab = tabs.name;
        this.apiTabs.push(tabs);
      }
    },
    closeConfirm(targetName) {
      let message = '';
      let tab = this.apiTabs;
      let id;
      tab.forEach((t) => {
        if (t.name === targetName) {
          if (t.api && store.apiMap.size > 0 && store.apiMap.has(t.api.id)) {
            id = t.api.id;
            if (
              store.apiMap.get(t.api.id).get('responseChange') === true ||
              store.apiMap.get(t.api.id).get('requestChange') === true ||
              store.apiMap.get(t.api.id).get('fromChange') === true ||
              store.apiMap.get(t.api.id).get('customFormChange') === true
            ) {
              message += t.api.name;
              id = t.api.id;
            }
          } else if (t.type === 'ADD' && t.title === this.$t('api_test.definition.request.title')) {
            message += this.$t('api_test.definition.request.title');
            id = t.api.id;
          }
          if (t.type === 'ADD' && t.isCopy) {
            message += t.api.name;
            id = t.api.id;
          }
        }
      });
      if (message !== '') {
        this.$alert(this.$t('commons.api') + ' [ ' + message + ' ] ' + this.$t('commons.confirm_info'), '', {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          callback: (action) => {
            if (action === 'confirm') {
              store.apiMap.delete(id);
              this.handleTabRemove(targetName);
            }
          },
        });
      } else {
        if (id) {
          store.apiMap.delete(id);
          store.saveMap.delete(id);
        }
        this.handleTabRemove(targetName);
      }
    },
    handleTabRemove(targetName) {
      let tabs = this.apiTabs;
      let activeName = this.apiDefaultTab;
      if (activeName === targetName) {
        tabs.forEach((tab, index) => {
          if (tab.name === targetName) {
            let nextTab = tabs[index + 1] || tabs[index - 1];
            if (nextTab) {
              tab.api = undefined;
              activeName = nextTab.name;
            }
          }
        });
      }
      this.apiDefaultTab = activeName;
      this.apiTabs = tabs.filter((tab) => tab.name !== targetName);
      this.$refs.mainTabs.$children = [];
      if (this.$refs.apiDefList[0]) {
        this.$refs.apiDefList[0].refreshTable();
      }
    },
    //创建左侧树的根目录模块
    createRootModel() {
      this.$refs.nodeTree.createRootModel();
    },
    handleMockTabsConfig(targetName, action, param) {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      if (targetName === undefined || targetName === null) {
        targetName = this.$t('api_test.definition.request.title');
      }
      let newTabName = getUUID();
      this.apiTabs.push({
        title: targetName,
        name: newTabName,
        closable: true,
        type: action,
        mock: param,
      });
      this.apiDefaultTab = newTabName;
    },
    handleTabsEdit(targetName, action, api) {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      if (targetName === undefined || targetName === null) {
        targetName = this.$t('api_test.definition.request.title');
      }
      let newTabName = getUUID();
      this.apiTabs.push({
        title: targetName,
        name: newTabName,
        closable: true,
        type: action,
        api: api,
        isCopy: api ? api.isCopy : false,
      });
      if (action === 'ADD') {
        this.activeTab = 'api';
      }
      this.apiDefaultTab = newTabName;
    },
    debug(id) {
      this.handleTabsEdit(this.$t('api_test.definition.request.fast_debug'), 'debug', id);
    },
    initForwardData() {
      let dataRange = this.$route.params.dataSelectRange;
      let dataType = this.$route.params.dataType;
      if (this.$route && this.$route.params && this.$route.params.type) {
        this.currentProtocol = this.$route.params.type;
      }
      if (dataRange && typeof dataRange === 'string' && dataType === 'api') {
        let selectParamArr = dataRange.split('edit:');
        if (selectParamArr.length === 2) {
          let scenarioId = selectParamArr[1];
          getDefinitionById(scenarioId).then((response) => {
            this.defaultProtocol = response.data.protocol;
            this.editApiModule(response.data);
          });
        }
      }
    },
    editApiModule(row) {
      getApiModuleByProjectIdAndProtocol(row.projectId, row.protocol).then((response) => {
        if (response.data) {
          response.data.forEach((node) => {
            node.name = node.name === '未规划接口' ? this.$t('api_test.definition.unplanned_api') : node.name;
            buildTree(node, { path: '' });
          });
          this.moduleOptions = response.data;
        }
        this.editApi(row);
      });
    },
    editApi(row) {
      const index = this.apiTabs.find((p) => p.api && p.api.id === row.id);
      if (!index) {
        let name = '';
        if (row.isCopy) {
          name = 'copy' + '_' + row.name;
          row.name = 'copy' + '_' + row.name;
        } else {
          if (row.name) {
            name = this.$t('api_test.definition.request.edit_api') + '-' + row.name;
          } else {
            name = this.$t('api_test.definition.request.title');
          }
        }
        this.activeTab = 'api';
        if (row != null && row.tags != 'null' && row.tags != '' && row.tags != undefined) {
          if (
            Object.prototype.toString
              .call(row.tags)
              .match(/\[object (\w+)\]/)[1]
              .toLowerCase() !== 'object' &&
            Object.prototype.toString
              .call(row.tags)
              .match(/\[object (\w+)\]/)[1]
              .toLowerCase() !== 'array'
          ) {
            row.tags = JSON.parse(row.tags);
          }
        }
        this.handleTabsEdit(name, 'ADD', row);
      } else {
        this.apiDefaultTab = index.name;
      }
    },
    copyApi(row) {
      let name = '';
      if (row.isCopy) {
        name = 'copy' + '_' + row.name;
        row.name = 'copy' + '_' + row.name;
      }
      this.activeTab = 'api';
      if (row != null && row.tags != 'null' && row.tags != '' && row.tags != undefined) {
        if (
          Object.prototype.toString
            .call(row.tags)
            .match(/\[object (\w+)\]/)[1]
            .toLowerCase() !== 'object' &&
          Object.prototype.toString
            .call(row.tags)
            .match(/\[object (\w+)\]/)[1]
            .toLowerCase() !== 'array'
        ) {
          row.tags = JSON.parse(row.tags);
        }
      }
      this.handleTabsEdit(name, 'ADD', row);
    },
    handleCase(api) {
      this.currentApi = api;
      this.showCasePage = false;
    },
    apiCaseClose() {
      this.showCasePage = true;
    },
    exportAPI(type, nodeTree) {
      if (this.activeDom !== 'left') {
        this.$warning('用例列表暂不支持导出，请切换成接口列表');
        return;
      }
      this.$refs.apiDefList[0].exportApi(type, nodeTree);
    },
    refreshModule() {
      this.$refs.nodeTree.list();
    },
    refresh(data) {
      if (this.$refs.caseList && this.$refs.caseList[0]) {
        this.$refs.caseList[0].initTable();
      }
      if (this.$refs.trashApiList) {
        this.$refs.trashApiList.initTable();
      }
      if (this.$refs.trashCaseList) {
        this.$refs.trashCaseList.initTable();
      }
      if (this.$refs.nodeTree) {
        this.$refs.nodeTree.list();
      }
      if (this.$refs.apiDefList && this.$refs.apiDefList[0]) {
        this.$refs.apiDefList[0].initTable();
      }
      if (this.$refs.documentsPage && this.$refs.documentsPage[0]) {
        this.$refs.documentsPage[0].initApiDocSimpleList();
      }
      //this.$refs.nodeTree.list();
    },
    refreshTree() {
      this.$refs.nodeTree.list();
    },
    setTabTitle(data) {
      for (let index in this.apiTabs) {
        let tab = this.apiTabs[index];
        if (tab.name === this.apiDefaultTab) {
          tab.title = this.$t('api_test.definition.request.edit_api') + '-' + data.name;
          break;
        }
      }
    },
    runTest(data) {
      this.activeTab = 'test';
      this.handleTabsEdit(this.$t('commons.api'), 'TEST', data);
      this.setTabTitle(data);
    },
    handleTestCase(row) {
      this.activeTab = 'testCase';
      let name = '';
      if (row.name) {
        name = this.$t('api_test.definition.request.edit_api') + '-' + row.name;
      } else {
        name = this.$t('api_test.definition.request.title');
      }
      if (row != null && row.tags != 'null' && row.tags != '' && row.tags != undefined) {
        if (
          Object.prototype.toString
            .call(row.tags)
            .match(/\[object (\w+)\]/)[1]
            .toLowerCase() !== 'object' &&
          Object.prototype.toString
            .call(row.tags)
            .match(/\[object (\w+)\]/)[1]
            .toLowerCase() !== 'array'
        ) {
          row.tags = JSON.parse(row.tags);
        }
      }
      this.handleTabsEdit(name, 'TEST', row);
    },
    mockConfig(data) {
      let targetName = this.$t('commons.mock') + '-' + data.apiName;
      this.handleMockTabsConfig(targetName, 'MOCK', data);
    },
    refreshMockInfo(mockConfigId) {
      let mockParam = {};
      mockParam.id = mockConfigId;
      createMockConfig(mockParam).then((response) => {
        this.mockConfigData = response.data;
      });
    },
    searchApiParams(apiId) {
      getMockApiParams(apiId).then((response) => {
        this.apiParams = response.data;
        if (!this.apiParams.query) {
          this.apiParams.query = [];
        }
        if (!this.apiParams.rest) {
          this.apiParams.rest = [];
        }
        if (!this.apiParams.form) {
          this.apiParams.form = [];
        }
      });
    },
    saveApi(data) {
      this.setTabTitle(data);
      this.refresh(data);
      this.apiTabs.forEach((t) => {
        if (t.api && t.api.id === data.id) {
          t.isCopy = false;
        }
      });
      store.apiStatus.set('fromChange', false);
      store.apiStatus.set('requestChange', false);
      store.apiStatus.set('responseChange', false);
      store.apiStatus.set('customFormChange', false);
      store.apiMap.set(data.id, store.apiStatus);
      // 保存后将保存状态置为true
      store.saveMap.set(data.id, true);
    },

    showExecResult(row) {
      this.debug(row);
    },
    nodeChange(node, nodeIds, pNodes) {
      this.initApiTableOpretion = 'selectNodeIds';
      this.selectNodeIds = nodeIds;
    },
    handleProtocolChange(protocol) {
      this.initApiTableOpretion = 'currentProtocol';
      this.selectNodeIds = [];
      this.currentProtocol = protocol;
    },
    setModuleOptions(data) {
      this.moduleOptions = data;
    },
    setNodeTree(data) {
      this.nodeTree = data;
    },
    changeSelectDataRangeAll(tableType) {
      if (this.$route.params.paramObj) {
        this.$route.params.paramObj.dataSelectRange = 'all';
      }
    },
    enableTrash(data) {
      this.initApiTableOpretion = 'trashEnable';
      this.trashEnable = data;
      this.trashVersion = this.currentVersion;
      if (data) {
        this.apiDefaultTab = 'trash';
      } else {
        this.apiDefaultTab = 'default';
      }
    },
    updateInitApiTableOpr(param) {
      this.initApiTableOpretion = param;
    },
    changeVersion(currentVersion) {
      this.trashVersion = null;
      this.currentVersion = currentVersion || null;
    },
    changeTab(active) {
      this.activeDom = active;
      this.$EventBus.$emit('apiConditionBus', {});
      this.$refs.nodeTree.list();
    },
  },
};
</script>

<style lang="scss" scoped>
.ms-api-div {
  overflow-y: hidden;
  height: calc(100vh - 100px);
}

.ms-api-schedule-div {
  overflow-y: auto;
  height: calc(100vh - 100px);
}

:deep(.el-main) {
  overflow: auto;
}

:deep(.el-tabs__header) {
  margin: 0 0 0px;
}

:deep(.el-card) {
  /*border: 1px solid #EBEEF5;*/
  /*border-style: none;*/
  border-top: none;
}

:deep(.api-component) {
  margin-top: 10px;
}

:deep(.el-tabs__nav-wrap) {
  width: calc(100% - 250px);
}

.ms-api-button {
  position: absolute;
  right: 10px;
  padding: 0;
  background: 0 0;
  border: none;
  outline: 0;
  cursor: pointer;
  margin-right: 10px;
  font-size: 16px;
  z-index: 1;
}

:deep(.el-table__empty-block) {
  width: 100%;
  min-width: 100%;
  max-width: 100%;
  padding-right: 100%;
}

.version-select {
  padding-left: 10px;
}

.ms-tab-name-width {
  display: inline-block;
  overflow-x: hidden;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  max-width: 200px;
}
</style>
