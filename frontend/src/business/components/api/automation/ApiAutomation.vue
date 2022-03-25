<template>
  <ms-container v-if="renderComponent" v-loading="loading">
    <ms-aside-container v-show="isAsideHidden">
      <ms-api-scenario-module
        :show-operator="true"
        @nodeSelectEvent="nodeChange"
        @refreshTable="refresh"
        @saveAsEdit="editScenario"
        @setModuleOptions="setModuleOptions"
        @setNodeTree="setNodeTree"
        @enableTrash="enableTrash"
        @exportAPI="exportAPI"
        @exportJmx="exportJmx"
        @refreshAll="refreshAll"
        page-source="scenario"
        :type="'edit'"
        :total='total'
        ref="nodeTree"/>
    </ms-aside-container>

    <ms-main-container style="overflow: hidden">
      <el-tabs v-model="activeName" @tab-click="addTab" @tab-remove="closeConfirm">
        <el-tab-pane
          name="trash"
          :label="$t('commons.trash')" v-if="trashEnable">
          <ms-api-scenario-list
            @getTrashCase="getTrashCase"
            @refreshTree="refreshTree"
            :module-tree="nodeTree"
            :module-options="moduleOptions"
            :select-node-ids="selectNodeIds"
            :trash-enable="true"
            :checkRedirectID="checkRedirectID"
            :isRedirectEdit="isRedirectEdit"
            :is-read-only="isReadOnly"
            @openScenario="editScenario"
            @edit="editScenario"
            @changeSelectDataRangeAll="changeSelectDataRangeAll"
            :custom-num="customNum"
            :init-api-table-opretion="initApiTableOpretion"
            @updateInitApiTableOpretion="updateInitApiTableOpretion"
            ref="apiTrashScenarioList">
            <template v-slot:version>
              <version-select v-xpack :project-id="projectId" @changeVersion="changeVersion"/>
            </template>
          </ms-api-scenario-list>
        </el-tab-pane>
        <el-tab-pane name="default" :label="$t('api_test.automation.scenario_list')">
          <ms-api-scenario-list
            @getTrashCase="getTrashCase"
            @refreshTree="refreshTree"
            :module-tree="nodeTree"
            :module-options="moduleOptions"
            :select-node-ids="selectNodeIds"
            :trash-enable="false"
            :checkRedirectID="checkRedirectID"
            :isRedirectEdit="isRedirectEdit"
            :is-read-only="isReadOnly"
            @openScenario="editScenario"
            @edit="editScenario"
            @changeSelectDataRangeAll="changeSelectDataRangeAll"
            :custom-num="customNum"
            :init-api-table-opretion="initApiTableOpretion"
            @updateInitApiTableOpretion="updateInitApiTableOpretion"
            ref="apiScenarioList">
            <template v-slot:version>
              <version-select v-xpack :project-id="projectId" @changeVersion="changeVersion"/>
            </template>
          </ms-api-scenario-list>
        </el-tab-pane>


        <el-tab-pane
          :key="item.name"
          v-for="(item) in tabs"
          :label="item.label"
          :name="item.name"
          closable>
          <div class="ms-api-scenario-div">
            <ms-edit-api-scenario @refresh="refresh" @openScenario="editScenario" @closePage="closePage"
                                  :currentScenario="item.currentScenario"
                                  :custom-num="customNum" :moduleOptions="moduleOptions" ref="autoScenarioConfig"/>
          </div>
        </el-tab-pane>

        <el-tab-pane name="add" v-if="hasPermission('PROJECT_API_SCENARIO:READ+CREATE')">
          <template v-slot:label>
            <el-dropdown @command="handleCommand">
              <el-button type="primary" plain icon="el-icon-plus" size="mini"/>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="ADD"
                                  v-permission="['PROJECT_API_SCENARIO:READ+CREATE']">
                  {{ $t('api_test.automation.add_scenario') }}
                </el-dropdown-item>
                <el-dropdown-item command="CLOSE_ALL">{{ $t('api_test.definition.request.close_all_label') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
        </el-tab-pane>
      </el-tabs>
    </ms-main-container>
  </ms-container>
</template>

<script>

import {getCurrentProjectID, getCurrentUser, getUUID, hasPermission} from "@/common/js/utils";
import {PROJECT_ID, WORKSPACE_ID} from "@/common/js/constants";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const VersionSelect = requireComponent.keys().length > 0 ? requireComponent("./version/VersionSelect.vue") : {};
const jsondiffpatch = require('jsondiffpatch');

export default {
  name: "ApiAutomation",
  components: {
    'VersionSelect': VersionSelect.default,
    MsApiScenarioModule: () => import("@/business/components/api/automation/scenario/ApiScenarioModule"),
    MsApiScenarioList: () => import("@/business/components/api/automation/scenario/ApiScenarioList"),
    MsMainContainer: () => import("@/business/components/common/components/MsMainContainer"),
    MsAsideContainer: () => import("@/business/components/common/components/MsAsideContainer"),
    MsContainer: () => import("@/business/components/common/components/MsContainer"),
    MsEditApiScenario: () => import("./scenario/EditApiScenario")
  },
  comments: {},
  computed: {
    checkRedirectID: function () {
      let redirectIDParam = this.$route.params.redirectID;
      this.changeRedirectParam(redirectIDParam);
      return redirectIDParam;
    },
    isRedirectEdit: function () {
      let redirectParam = this.$route.params.dataSelectRange;
      this.checkRedirectEditPage(redirectParam);
      return redirectParam;
    },
    isReadOnly() {
      return false;
    },
    projectId() {
      return getCurrentProjectID();
    },
  },
  data() {
    return {
      total: 0,
      redirectID: '',
      renderComponent: true,
      isHide: true,
      activeName: 'default',
      redirectFlag: 'none',
      currentModule: null,
      moduleOptions: [],
      tabs: [],
      loading: false,
      trashEnable: false,
      selectNodeIds: [],
      nodeTree: [],
      currentModulePath: "",
      customNum: false,
      //影响API表格刷新的操作。 为了防止高频率刷新模块列表用。如果是模块更新而造成的表格刷新，则不回调模块刷新方法
      initApiTableOpretion: 'init',
      isSave: false,
      isAsideHidden: true,
    };
  },
  created() {
    let workspaceId = this.$route.params.workspaceId;
    if (workspaceId) {
      sessionStorage.setItem(WORKSPACE_ID, workspaceId);
    }
    let projectId = this.$route.params.projectId;
    if (projectId) {
      sessionStorage.setItem(PROJECT_ID, projectId);
    }
  },
  mounted() {
    this.getProject();
    this.getTrashCase();
    this.init();
  },
  watch: {
    redirectID() {
      this.renderComponent = false;
      this.$nextTick(() => {
        // 在 DOM 中添加 my-component 组件
        this.renderComponent = true;
      });
    },
    '$route'(to, from) {  //  路由改变时，把接口定义界面中的 ctrl s 保存快捷键监听移除
      if (to.path.indexOf('/api/automation') == -1) {
        if (this.$refs && this.$refs.autoScenarioConfig) {
          this.$refs.autoScenarioConfig.forEach(item => {
            item.removeListener();
          });
        }
      }
    },
    selectNodeIds() {
      this.activeName = "default";
    },
    activeName() {
      this.isAsideHidden = this.activeName === 'default';
    }
  },
  methods: {
    hasPermission,
    exportAPI(nodeTree) {
      this.$refs.apiScenarioList.exportApi(nodeTree);
    },
    exportJmx() {
      this.$refs.apiScenarioList.exportJmx();
    },
    checkRedirectEditPage(redirectParam) {
      if (redirectParam != null) {
        let selectParamArr = redirectParam.split("edit:");
        if (selectParamArr.length == 2) {
          let scenarioId = selectParamArr[1];
          //查找单条数据，跳转修改页面
          let url = "/api/automation/list/" + 1 + "/" + 1;
          this.$post(url, {id: scenarioId}, response => {
            let data = response.data;
            if (data != null) {
              //如果树未加载
              if (this.moduleOptions && JSON.stringify(this.moduleOptions) === '{}') {
                this.$refs.nodeTree.list();
              }
              if (data.listObject && data.listObject.length > 0) {
                let row = data.listObject[0];
                if (row != null && row.tags != 'null' && row.tags != '' && row.tags != undefined) {
                  if (Object.prototype.toString.call(row.tags).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'object'
                    && Object.prototype.toString.call(row.tags).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'array') {
                    row.tags = JSON.parse(row.tags);
                  }
                }
                this.editScenario(row);
              }
            }
          });
        }
      }
    },
    changeRedirectParam(redirectIDParam) {
      this.redirectID = redirectIDParam;
      if (redirectIDParam != null) {
        if (this.redirectFlag == "none") {
          this.activeName = "default";
          this.addListener();
          this.redirectFlag = "redirected";
        }
      } else {
        this.redirectFlag = "none";
      }
    },
    getPath(id, arr) {
      if (id === null) {
        return null;
      }
      if (arr) {
        arr.forEach(item => {
          if (item.id === id) {
            this.currentModulePath = item.path;
          }
          if (item.children && item.children.length > 0) {
            this.getPath(id, item.children);
          }
        });
      }
    },
    addTab(tab) {
      if (tab.name === 'default') {
        this.$refs.apiScenarioList.search();
      } else if (tab.name === 'trash') {
        this.$refs.apiTrashScenarioList.search();
      }
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      this.currentModulePath = "";
      if (tab.name === 'add') {
        let label = this.$t('api_test.automation.add_scenario');
        let name = getUUID().substring(0, 8);
        this.activeName = name;
        let currentScenario = {
          status: "Underway", principal: getCurrentUser().id,
          apiScenarioModuleId: "default-module", id: getUUID(),
          modulePath: "/" + this.$t("commons.module_title"),
          level: "P0", type: "add"
        };
        if (this.nodeTree && this.nodeTree.length > 0) {
          currentScenario.apiScenarioModuleId = this.nodeTree[0].id;
          this.getPath(this.nodeTree[0].id, this.moduleOptions);
          currentScenario.modulePath = this.currentModulePath;
        }

        if (this.selectNodeIds && this.selectNodeIds.length > 0) {
          currentScenario.apiScenarioModuleId = this.selectNodeIds[0];
          this.getPath(this.selectNodeIds[0], this.moduleOptions);
          currentScenario.modulePath = this.currentModulePath;
        }
        this.tabs.push({label: label, name: name, currentScenario: currentScenario});
      }
      if (tab.name === 'edit') {
        let label = this.$t('api_test.automation.add_scenario');
        let name = getUUID().substring(0, 8);
        this.activeName = name;
        label = tab.currentScenario.name;
        if (!tab.currentScenario.level) {
          tab.currentScenario.level = "P0";
        }
        this.tabs.push({label: label, name: name, currentScenario: tab.currentScenario});
      }
      if (this.$refs && this.$refs.autoScenarioConfig) {
        this.$refs.autoScenarioConfig.forEach(item => {
          item.removeListener();
        });  //  删除所有tab的 ctrl + s 监听
        this.addListener();
      }
    },
    addListener() {
      let index = this.tabs.findIndex(item => item.name === this.activeName); //  找到当前选中tab的index
      if (index != -1) {   //  为当前选中的tab添加监听
        this.$nextTick(() => {
          this.$refs.autoScenarioConfig[index].addListener();
        });
      }
    },
    handleTabClose() {
      let message = "";
      if (!this.isSave) {
        this.tabs.forEach(t => {
          this.diff(t);
          if (t && this.isSave) {
            message += t.currentScenario.name + "，";
            this.isSave = false;
          }
        });
        if (message !== "") {
          this.$alert(this.$t('commons.scenario') + " [ " + message.substr(0, message.length - 1) + " ] " + this.$t('commons.confirm_info'), '', {
            confirmButtonText: this.$t('commons.confirm'),
            cancelButtonText: this.$t('commons.cancel'),
            callback: (action) => {
              if (action === 'confirm') {
                this.tabs = [];
                this.activeName = "default";
                this.isSave = false;
              } else {
                this.isSave = false;
              }
            }
          });
        } else {
          this.tabs = [];
          this.activeName = "default";
          this.refresh();
          this.isSave = false;
        }
      } else {
        this.tabs = [];
        this.activeName = "default";
        this.refresh();
        this.isSave = false;
      }
      if (this.tabs && this.tabs.length === 0) {
        this.refreshAll();
      }
    },
    handleCommand(e) {
      switch (e) {
        case "ADD":
          this.addTab({name: 'add'});
          break;
        case "CLOSE_ALL":
          this.handleTabClose();
          break;
        default:
          this.addTab({name: 'add'});
          break;
      }
    },
    closePage(targetName) {
      this.tabs = this.tabs.filter(tab => tab.label !== targetName);
      if (this.tabs.length > 0) {
        this.activeName = this.tabs[this.tabs.length - 1].name;
        this.addListener(); //  自动切换当前标签时，也添加监听
      } else {
        this.activeName = "default";
      }
    },
    diff(t) {
      if (t.currentScenario.type !== "add") {
        let v1 = t.currentScenario.scenarioDefinitionOrg;
        let v2 = {
          apiScenarioModuleId: t.currentScenario.apiScenarioModuleId,
          name: t.currentScenario.name,
          status: t.currentScenario.status,
          principal: t.currentScenario.principal,
          level: t.currentScenario.level,
          tags: t.currentScenario.tags,
          description: t.currentScenario.description,
          scenarioDefinition: t.currentScenario.scenarioDefinition
        };
        let v3 = JSON.parse(JSON.stringify(v2));
        if (v1.scenarioDefinition) {
          this.deleteResourceIds(v1.scenarioDefinition);
        }
        if (v3.scenarioDefinition) {
          this.deleteResourceIds(v3.scenarioDefinition);
        }
        let delta = jsondiffpatch.diff(JSON.parse(JSON.stringify(v1)), JSON.parse(JSON.stringify(v3)));
        if (delta) {
          this.isSave = true;
        }
      }
    },
    deleteResourceIds(array) {
      array.forEach(item => {
        if (item.resourceId) {
          delete item.resourceId;
        }
        if (item.method) {
          delete item.method;
        }
        if (item.timeout >= 0) {
          delete item.timeout;
        }
        if (item.ctimeout >= 0) {
          delete item.ctimeout;
        }
        if (item.rest && item.rest.length === 0) {
          delete item.rest;
        }
        if (item.arguments && item.arguments.length === 0) {
          delete item.arguments;
        }
        if (item.id) {
          delete item.id;
        }
        if (!item.checkBox) {
          delete item.checkBox;
        }
        if (!item.isBatchProcess) {
          delete item.isBatchProcess;
        }
        if (!item.isLeaf || item.isLeaf) {
          delete item.isLeaf;
        }
        if (item.maxThreads) {
          delete item.maxThreads;
        }
        if (item.parentIndex) {
          delete item.parentIndex
        }
        if (item.connectTimeout) {
          delete item.connectTimeout;
        }
        if (item.index) {
          delete item.index;
        }
        if (item.postSize >= 0) {
          delete item.postSize;
        }
        if (item.preSize >= 0) {
          delete item.preSize;
        }
        if (item.requestResult) {
          delete item.requestResult;
        }
        if (item.responseTimeout) {
          delete item.responseTimeout;
        }
        if (item.root) {
          delete item.root;
        }
        if (item.ruleSize >= 0) {
          delete item.ruleSize;
        }
        if (item.delay) {
          item.delay = Number(item.delay);
        }
        if (item.body && item.body.kvs) {
          item.body.kvs.forEach(v => {
            if (v.files) {
              delete v.files;
            }
          })
        }
        if (item.body && ((item.body.binary && item.body.binary.length === 0) || (item.body.kvs && item.body.kvs.length === 0))) {
          delete item.body;
        }
        delete item.projectId;
        if (item.hashTree && item.hashTree.length > 0) {
          this.deleteResourceIds(item.hashTree);
        }
      })
    },
    closeConfirm(targetName) {
      let message = "";
      let tab = this.tabs.filter(tab => tab.name === targetName);
      if (!this.isSave) {
        tab.forEach(t => {
          this.diff(t);
          if (t && this.isSave) {
            message += t.currentScenario.name + "，";
          }
        });
        if (message !== "") {
          this.$alert(this.$t('commons.scenario') + " [ " + message.substr(0, message.length - 1) + " ] " + this.$t('commons.confirm_info'), '', {
            confirmButtonText: this.$t('commons.confirm'),
            cancelButtonText: this.$t('commons.cancel'),
            callback: (action) => {
              if (action === 'confirm') {
                this.removeTab(targetName);
                this.isSave = false;
              } else {
                this.isSave = false;
              }
            }
          });
        } else {
          this.isSave = false;
          this.removeTab(targetName);
        }
      } else {
        this.isSave = false;
        this.removeTab(targetName);
      }
      if (tab) {
        tab.splice(0, 1);
        tab = undefined;
      }
      if (this.tabs && this.tabs.length === 0) {
        this.refreshAll();
      }
    },
    removeTab(targetName) {
      this.tabs = this.tabs.filter(tab => tab.name !== targetName);
      if (this.tabs.length > 0) {
        this.activeName = this.tabs[this.tabs.length - 1].name;
        this.addListener(); //  自动切换当前标签时，也添加监听
      } else {
        this.activeName = "default";
      }
      let index = this.tabs.findIndex(item => item.name === targetName);
      if (index !== -1) {
        this.tabs.splice(index, 1);
      }
    },
    setTabLabel(data) {
      for (const tab of this.tabs) {
        if (tab.name === this.activeName) {
          tab.label = data.name;
          break;
        }
      }
    },
    selectModule(data) {
      this.currentModule = data;
    },
    saveScenario(data) {
      this.setTabLabel(data);
      this.$refs.apiScenarioList.search(data);
      if (this.$refs.apiTrashScenarioList) {
        this.$refs.apiTrashScenarioList.search(data);
      }
    },
    refresh(data) {
      if (data) {
        this.setTabTitle(data);
      }
      this.isSave = true;
    },
    refreshTree() {
      if (this.$refs.nodeTree) {
        this.$refs.nodeTree.list();
      }
    },
    refreshAll() {
      this.$refs.nodeTree.list();
      this.$refs.apiScenarioList.search();
      if (this.$refs.apiTrashScenarioList) {
        this.$refs.apiTrashScenarioList.search();
      }
    },
    setTabTitle(data) {
      for (let index in this.tabs) {
        let tab = this.tabs[index];
        if (tab && tab.name === this.activeName) {
          tab.label = data.name;
          break;
        }
      }
    },
    init() {
      let scenarioData = this.$route.params.scenarioData;
      if (scenarioData) {
        this.editScenario(scenarioData);
      }
    },
    editScenario(row) {
      const index = this.tabs.find(p => p.currentScenario.id === row.id && p.currentScenario.copy === row.copy);
      if (!index) {
        this.addTab({name: 'edit', currentScenario: row});
      } else {
        this.activeName = index.name;
      }
    },

    nodeChange(node, nodeIds, pNodes) {
      this.initApiTableOpretion = "nodeChange";
      this.selectNodeIds = nodeIds;
    },
    setModuleOptions(data) {
      this.moduleOptions = data;
    },
    setNodeTree(data) {
      this.nodeTree = data;
    },
    changeSelectDataRangeAll(tableType) {
      this.$route.params.dataSelectRange = 'all';
    },
    enableTrash(data) {
      this.activeName = "default";
      this.initApiTableOpretion = "enableTrash";
      this.trashEnable = data;
      if (data) {
        this.activeName = "trash";
      } else {
        this.activeName = "default";
      }

      this.getTrashCase();
    },
    getTrashCase() {
      let param = {};
      param.projectId = this.projectId;
      this.$post("/api/automation/list/all/trash", param, response => {
        this.total = response.data;
      });
    },
    getProject() {
      this.$get('/project_application/get/config/' + this.projectId + "/SCENARIO_CUSTOM_NUM", result => {
        let data = result.data;
        if (data) {
          this.customNum = data.scenarioCustomNum;
        }
      });
    },
    updateInitApiTableOpretion(param) {
      this.initApiTableOpretion = param;
    },
    changeVersion(currentVersion) {
      if (this.$refs.apiScenarioList) {
        this.$refs.apiScenarioList.condition.versionId = currentVersion || null;
        this.$refs.apiScenarioList.getVersionOptions(currentVersion);
      }
      if (this.$refs.apiTrashScenarioList) {
        this.$refs.apiTrashScenarioList.condition.versionId = currentVersion || null;
        this.$refs.apiTrashScenarioList.getVersionOptions(currentVersion);
      }
      this.refresh();
    }
  }
};
</script>

<style scoped>
/deep/ .el-tabs__header {
  margin: 0 0 0px;
}

/deep/ .el-table__empty-block {
  width: 100%;
  min-width: 100%;
  max-width: 100%;
  padding-right: 100%;
}
</style>
