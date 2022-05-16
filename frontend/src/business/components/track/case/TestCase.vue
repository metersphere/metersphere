<template>
  <ms-container v-if="renderComponent" v-loading="loading">

    <ms-aside-container v-show="isAsideHidden">
      <test-case-node-tree
        @nodeSelectEvent="nodeChange"
        @refreshTable="refresh"
        @setTreeNodes="setTreeNodes"
        @exportTestCase="exportTestCase"
        @saveAsEdit="editTestCase"
        :show-operator="true"
        @createCase="handleCaseSimpleCreate($event, 'add')"
        @refreshAll="refreshAll"
        @enableTrash="enableTrash"
        @enablePublic="enablePublic"
        @toPublic="toPublic"
        :type="'edit'"
        :total='total'
        :public-total="publicTotal"
        ref="nodeTree"
      />
    </ms-aside-container>

    <ms-main-container>
      <el-tabs v-model="activeName" @tab-click="addTab" @tab-remove="closeConfirm">
        <el-tab-pane name="trash" v-if="trashEnable" :label="$t('commons.trash')">
          <ms-tab-button
            :isShowChangeButton="false">
            <template v-slot:version>
              <version-select v-xpack :project-id="projectId" @changeVersion="changeTrashVersion" margin-left="-10"/>
            </template>
            <test-case-list
              :checkRedirectID="checkRedirectID"
              :isRedirectEdit="isRedirectEdit"
              :tree-nodes="treeNodes"
              :trash-enable="true"
              :current-version="currentTrashVersion"
              :version-enable="versionEnable"
              @refreshTable="refresh"
              @testCaseEdit="editTestCase"
              @testCaseCopy="copyTestCase"
              @testCaseDetail="showTestCaseDetail"
              @getTrashList="getTrashList"
              @getPublicList="getPublicList"
              @refresh="refresh"
              @refreshAll="refreshAll"
              @setCondition="setCondition"
              ref="testCaseTrashList">
            </test-case-list>
          </ms-tab-button>
        </el-tab-pane>
        <el-tab-pane name="public" v-if="publicEnable" :label="$t('project.case_public')">
          <div style="height: 6px;"></div>
          <test-case-list
            :checkRedirectID="checkRedirectID"
            :isRedirectEdit="isRedirectEdit"
            :tree-nodes="treeNodes"
            :trash-enable="false"
            :public-enable="true"
            :version-enable="versionEnable"
            @refreshTable="refresh"
            @testCaseEdit="editTestCase"
            @testCaseEditShow="editTestCaseShow"
            @testCaseCopy="copyTestCase"
            @testCaseDetail="showTestCaseDetail"
            @getTrashList="getTrashList"
            @getPublicList="getPublicList"
            @refresh="refresh"
            @refreshAll="refreshAll"
            @setCondition="setCondition"
            ref="testCasePublicList">
          </test-case-list>
        </el-tab-pane>
        <el-tab-pane name="default" :label="$t('api_test.definition.case_title')">
          <ms-tab-button
            :active-dom="activeDom"
            @update:activeDom="updateActiveDom"
            :left-tip="$t('test_track.case.list')"
            :left-content="$t('test_track.case.list')"
            :right-tip="$t('test_track.case.minder')"
            :right-content="$t('test_track.case.minder')"
            :middle-button-enable="false">
            <template v-slot:version>
              <version-select v-xpack :project-id="projectId" @changeVersion="changeVersion"/>
            </template>
            <test-case-list
              v-if="activeDom === 'left'"
              :checkRedirectID="checkRedirectID"
              :isRedirectEdit="isRedirectEdit"
              :tree-nodes="treeNodes"
              :trash-enable="false"
              :public-enable="false"
              :current-version="currentVersion"
              :version-enable="versionEnable"
              @refreshTable="refresh"
              @testCaseEdit="editTestCase"
              @testCaseCopy="copyTestCase"
              @testCaseDetail="showTestCaseDetail"
              @getTrashList="getTrashList"
              @getPublicList="getPublicList"
              @refresh="refresh"
              @refreshAll="refreshAll"
              @setCondition="setCondition"
              @decrease="decrease"
              ref="testCaseList">
            </test-case-list>
            <test-case-minder
              :tree-nodes="treeNodes"
              :project-id="projectId"
              :condition="condition"
              v-if="activeDom === 'right'"
              @refresh="refreshTable"
              ref="minder"/>
          </ms-tab-button>
        </el-tab-pane>
        <el-tab-pane
          :key="item.name"
          v-for="(item) in tabs"
          :label="item.label"
          :name="item.name"
          closable>
          <div class="ms-api-scenario-div" v-if="!showPublic">
            <test-case-edit
              :currentTestCaseInfo="item.testCaseInfo"
              :version-enable="versionEnable"
              @refresh="refreshTable"
              @caseEdit="handleCaseCreateOrEdit($event,'edit')"
              @caseCreate="handleCaseCreateOrEdit($event,'add')"
              @checkout="checkout($event, item)"
              :read-only="testCaseReadOnly"
              :tree-nodes="treeNodes"
              :select-node="selectNode"
              :select-condition="condition"
              :public-enable="currentActiveName === 'default' ? false : true"
              :case-type="type"
              @addTab="addTab"
              ref="testCaseEdit">
            </test-case-edit>
          </div>
          <div class="ms-api-scenario-div" v-if="showPublic">
            <test-case-edit-show
              :currentTestCaseInfo="item.testCaseInfo"
              :version-enable="versionEnable"
              @refresh="refreshTable"
              @caseEdit="handleCaseCreateOrEdit($event,'edit')"
              @caseCreate="handleCaseCreateOrEdit($event,'add')"
              :read-only="testCaseReadOnly"
              @checkout="checkoutPublic($event, item)"
              :tree-nodes="treeNodes"
              :select-node="selectNode"
              :select-condition="condition"
              :type="type"
              :public-enable="currentActiveName === 'default' ? false : true"
              @addTab="addTabShow"
              ref="testCaseEditShow">
            </test-case-edit-show>
          </div>
          <template v-slot:version>
            <version-select v-xpack :project-id="projectId" @changeVersion="changeVersion"/>
          </template>
        </el-tab-pane>
        <el-tab-pane name="add" v-if="hasPermission('PROJECT_TRACK_CASE:READ+CREATE')">
          <template v-slot:label>
            <el-dropdown @command="handleCommand" v-permission="['PROJECT_TRACK_CASE:READ+CREATE']">
              <el-button type="primary" plain icon="el-icon-plus" size="mini"/>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="ADD" v-permission="['PROJECT_TRACK_CASE:READ+CREATE']">
                  {{ $t('test_track.case.create') }}
                </el-dropdown-item>
                <el-dropdown-item command="CLOSE_ALL">{{ $t('api_test.definition.request.close_all_label') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
        </el-tab-pane>

      </el-tabs>

      <is-change-confirm
        :title="'请保存脑图'"
        :tip="'脑图未保存，确认保存脑图吗？'"
        @confirm="changeConfirm"
        ref="isChangeConfirm"/>
    </ms-main-container>


  </ms-container>

</template>

<script>

import NodeTree from '../common/NodeTree';
import TestCaseEdit from './components/TestCaseEdit';
import TestCaseList from "./components/TestCaseList";
import SelectMenu from "../common/SelectMenu";
import MsContainer from "../../common/components/MsContainer";
import MsAsideContainer from "../../common/components/MsAsideContainer";
import MsMainContainer from "../../common/components/MsMainContainer";
import {
  hasLicense,
  getCurrentProjectID,
  getCurrentWorkspaceId,
  getUUID,
  hasPermission,
  setCurTabId
} from "@/common/js/utils";
import TestCaseNodeTree from "../common/TestCaseNodeTree";

import MsTabButton from "@/business/components/common/components/MsTabButton";
import TestCaseMinder from "@/business/components/track/common/minder/TestCaseMinder";
import IsChangeConfirm from "@/business/components/common/components/IsChangeConfirm";
import {openMinderConfirm, saveMinderConfirm} from "@/business/components/track/common/minder/minderUtils";
import TestCaseEditShow from "@/business/components/track/case/components/TestCaseEditShow";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const VersionSelect = requireComponent.keys().length > 0 ? requireComponent("./version/VersionSelect.vue") : {};

export default {
  name: "TestCase",
  components: {
    IsChangeConfirm,
    TestCaseMinder,
    MsTabButton,
    TestCaseNodeTree,
    MsMainContainer,
    MsAsideContainer, MsContainer, TestCaseList, NodeTree, TestCaseEdit, SelectMenu, TestCaseEditShow,
    'VersionSelect': VersionSelect.default,
  },
  comments: {},
  data() {
    return {
      result: {},
      projects: [],
      treeNodes: [],
      testCaseReadOnly: true,
      trashEnable: false,
      publicEnable: false,
      showPublic: false,
      condition: {},
      activeName: 'default',
      currentActiveName: '',
      tabs: [],
      renderComponent: true,
      loading: false,
      type: '',
      activeDom: 'left',
      tmpActiveDom: null,
      total: 0,
      publicTotal: 0,
      tmpPath: null,
      currentVersion: null,
      currentTrashVersion: null,
      versionEnable: false,
      isAsideHidden: true,
    };
  },
  mounted() {
    this.getProject();
    let routeTestCase = this.$route.params.testCase;
    if (routeTestCase && routeTestCase.add === true) {
      this.addTab({name: 'add'});
    } else {
      this.init(this.$route);
    }
    this.checkVersionEnable();
  },
  beforeRouteLeave(to, from, next) {
    if (this.$store.state.isTestCaseMinderChanged) {
      this.$refs.isChangeConfirm.open();
      this.tmpPath = to.path;
    } else {
      next();
    }
  },
  watch: {
    redirectID() {
      this.renderComponent = false;
      this.$nextTick(() => {
        // 在 DOM 中添加 my-component 组件
        this.renderComponent = true;
      });
    },
    '$route'(to) {
      this.init(to);
    },
    activeName(newVal, oldVal) {
      this.isAsideHidden = this.activeName === 'default';
      if (oldVal !== 'default' && newVal === 'default' && this.$refs.minder) {
        this.$refs.minder.refresh();
      }
    },
    activeDom(newVal, oldVal) {
      this.$nextTick(() => {
        if (oldVal !== 'left' && newVal === 'left' && this.$refs.testCaseList) {
          this.$refs.testCaseList.getTemplateField();
        }
      });
    },
    trashEnable() {
      if (this.trashEnable) {
        this.activeName = 'trash';
      } else {
        this.activeName = 'default';
      }
    },
    publicEnable() {
      if (this.publicEnable) {
        this.activeName = 'public';
      } else {
        this.activeName = 'default';
      }
    }
  },
  computed: {
    checkRedirectID: function () {
      let redirectIDParam = this.$route.params.redirectID;
      this.changeRedirectParam(redirectIDParam);
      return redirectIDParam;
    },
    isRedirectEdit: function () {
      let redirectParam = this.$route.params.dataSelectRange;
      return redirectParam;
    },

    projectId() {
      return getCurrentProjectID();
    },
    selectNodeIds() {
      return this.$store.state.testCaseSelectNodeIds;
    },
    selectNode() {
      return this.$store.state.testCaseSelectNode;
    },
    moduleOptions() {
      return this.$store.state.testCaseModuleOptions;
    }
  },
  methods: {
    hasPermission,
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
    getTrashList() {
      this.$get("/case/node/trashCount/" + this.projectId, response => {
        this.total = response.data;
      });
    },
    getPublicList() {
      this.$get("/case/node/publicCount/" + getCurrentWorkspaceId(), response => {
        this.publicTotal = response.data;
      });
    },
    updateActiveDom(activeDom) {
      openMinderConfirm(this, activeDom);
    },
    changeConfirm(isSave) {
      saveMinderConfirm(this, isSave);
    },
    changeRedirectParam(redirectIDParam) {
      this.redirectID = redirectIDParam;
      if (redirectIDParam != null) {
        if (this.redirectFlag == "none") {
          this.activeName = "default";
          this.redirectFlag = "redirected";
        }
      } else {
        this.redirectFlag = "none";
      }
    },
    addTab(tab) {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      this.showPublic = false
      if (tab.name === 'add') {
        let label = this.$t('test_track.case.create');
        let name = getUUID().substring(0, 8);
        this.activeName = name;
        this.currentActiveName = 'default'
        this.type = 'add';
        this.tabs.push({label: label, name: name, testCaseInfo: {testCaseModuleId: "", id: getUUID()}});
      }
      if (tab.name === 'edit') {
        let label = this.$t('test_track.case.create');
        let name = getUUID().substring(0, 8);
        if (this.activeName === 'public') {
          this.currentActiveName = 'public'
        } else {
          this.currentActiveName = 'default'
        }
        this.activeName = name;
        label = tab.testCaseInfo.name;
        this.tabs.push({label: label, name: name, testCaseInfo: tab.testCaseInfo});
      }

      if (tab.name === 'public') {
        this.publicEnable = false;
        this.$nextTick(() => {
          this.publicEnable = true;
        })
      }

      setCurTabId(this, tab, 'testCaseEdit');
    },
    addTabShow(tab) {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      if (tab.name === 'show') {
        this.showPublic = true
        let label = this.$t('test_track.case.create');
        let name = getUUID().substring(0, 8);
        this.activeName = name;
        this.currentActiveName = 'public'
        label = tab.testCaseInfo.name;
        this.tabs.push({label: label, name: name, testCaseInfo: tab.testCaseInfo});
      }
      setCurTabId(this, tab, 'testCaseEditShow');
    },
    handleTabClose() {
      let message = "";
      this.tabs.forEach(t => {
        if (t && this.$store.state.testCaseMap.has(t.testCaseInfo.id) && this.$store.state.testCaseMap.get(t.testCaseInfo.id) > 1) {
          message += t.testCaseInfo.name + "，";
        }
        if (t.label === this.$t('test_track.case.create')) {
          message += this.$t('test_track.case.create') + "，";
        }
        if (t.testCaseInfo.isCopy) {
          message += t.testCaseInfo.name + "，";
        }
      })
      if (message !== "") {
        this.$alert(this.$t('commons.track') + " [ " + message.substr(0, message.length - 1) + " ] " + this.$t('commons.confirm_info'), '', {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          callback: (action) => {
            if (action === 'confirm') {
              this.$store.state.testCaseMap.clear();
              this.tabs = [];
              this.activeName = "default";
              this.refresh();
            }
          }
        });
      } else {
        this.tabs = [];
        this.activeName = "default";
        this.refresh();
      }
    },
    closeConfirm(targetName) {
      let t = this.tabs.filter(tab => tab.name === targetName);
      let message = "";
      if (t && this.$store.state.testCaseMap.has(t[0].testCaseInfo.id) && this.$store.state.testCaseMap.get(t[0].testCaseInfo.id) > 0) {
        message += t[0].testCaseInfo.name + "，";
      }
      if (t[0].label === this.$t('test_track.case.create')) {
        message += this.$t('test_track.case.create') + "，";
      }
      if (t[0].testCaseInfo.isCopy) {
        message += t[0].testCaseInfo.name + "，";
      }
      if (message !== "") {
        this.$alert(this.$t('commons.track') + " [ " + message.substr(0, message.length - 1) + " ] " + this.$t('commons.confirm_info'), '', {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          callback: (action) => {
            if (action === 'confirm') {
              this.$store.state.testCaseMap.delete(t[0].testCaseInfo.id);
              this.removeTab(targetName);
            }
          }
        });
      } else {
        this.$store.state.testCaseMap.delete(t[0].testCaseInfo.id);
        this.removeTab(targetName);
      }
    },
    removeTab(targetName) {
      this.tabs = this.tabs.filter(tab => tab.name !== targetName);
      if (this.tabs.length > 0) {
        this.activeName = this.tabs[this.tabs.length - 1].name;
      } else {
        this.activeName = "default";
      }
    },
    exportTestCase(type) {
      if (this.activeDom !== 'left') {
        this.$warning(this.$t('test_track.case.export.export_tip'));
        return;
      }
      this.$refs.testCaseList.exportTestCase(type);
    },
    init(route) {
      let path = route.path;
      if (path.indexOf("/track/case/edit") >= 0 || path.indexOf("/track/case/create") >= 0) {
        this.testCaseReadOnly = false;
        let caseId = this.$route.params.caseId;
        let routeTestCase = this.$route.params.testCase;
        if (!this.projectId) {
          this.$warning(this.$t('commons.check_project_tip'));
          return;
        }
        if (routeTestCase) {
          this.editTestCase(routeTestCase);
        } else if (caseId) {
          this.$get('test/case/get/' + caseId, response => {
            let testCase = response.data;
            this.editTestCase(testCase);
          });
        } else {
          this.addTab({name: 'add'});
        }
        this.$router.push('/track/case/all');
      }
    },
    nodeChange(node) {
      this.condition.trashEnable = false;
      this.trashEnable = false;
      this.condition.publicEnable = false;
      this.publicEnable = false;
      this.activeName = "default";
    },
    refreshTable(data) {
      if (this.$refs.testCaseList) {
        this.$refs.testCaseList.initTableData();
      }
      this.$refs.nodeTree.list();
      this.setTable(data);
    },
    increase(id) {
      this.$refs.nodeTree.increase(id);
    },
    decrease(id) {
      this.$refs.nodeTree.decrease(id);
    },
    editTestCase(testCase) {
      const index = this.tabs.find(p => p.testCaseInfo && p.testCaseInfo.id === testCase.id);
      if (!index) {
        this.type = "edit";
        this.testCaseReadOnly = false;
        if (testCase.label !== "redirect") {
          if (this.treeNodes.length < 1) {
            this.$warning(this.$t('test_track.case.create_module_first'));
            return;
          }
        }
        let hasEditPermission = hasPermission('PROJECT_TRACK_CASE:READ+EDIT');
        this.$set(testCase, 'rowClickHasPermission', hasEditPermission);
        this.addTab({name: 'edit', testCaseInfo: testCase});
      } else {
        this.activeName = index.name;
      }
    },

    editTestCaseShow(testCase) {
      const index = this.tabs.find(p => p.testCaseInfo && p.testCaseInfo.id === testCase.id);
      if (!index) {
        this.type = "edit";
        this.testCaseReadOnly = false;
        if (testCase.label !== "redirect") {
          if (this.treeNodes.length < 1) {
            this.$warning(this.$t('test_track.case.create_module_first'));
            return;
          }
        }
        let hasEditPermission = hasPermission('PROJECT_TRACK_CASE:READ+EDIT');
        this.$set(testCase, 'rowClickHasPermission', hasEditPermission);
        this.addTabShow({name: 'show', testCaseInfo: testCase});
      } else {
        this.activeName = index.name;
      }
    },
    handleCaseCreateOrEdit(data, type) {
      if (this.$refs.minder) {
        this.$refs.minder.addCase(data, type);
      }
    },
    handleCaseSimpleCreate(data, type) {
      if ('default-module' === data.nodeId) {
        for (let i = 0; i < this.moduleOptions.length; i++) {
          let item = this.moduleOptions[i];
          if (item.path.indexOf('未规划用例') > -1) {
            data.nodeId = item.id;
            break;
          }
        }
      }
      this.handleCaseCreateOrEdit(data, type);
      if (this.$refs.minder) {
        this.$refs.minder.refresh();
      }
    },
    copyTestCase(testCase) {
      this.type = "copy";
      this.testCaseReadOnly = false;
      testCase.isCopy = true;
      this.addTab({name: 'edit', testCaseInfo: testCase});
    },
    showTestCaseDetail(testCase) {
      this.testCaseReadOnly = true;
    },
    refresh(data) {
      this.$store.commit('setTestCaseSelectNode', {});
      this.$store.commit('setTestCaseSelectNodeIds', []);
      this.refreshTable(data);
    },
    setTable(data) {
      if (data) {
        for (let index in this.tabs) {
          let tab = this.tabs[index];
          if (tab.name === this.activeName) {
            tab.label = data.name;
            break;
          }
        }
      }
    },
    refreshAll() {
      this.$refs.nodeTree.list();
      this.refresh();
    },
    openRecentTestCaseEditDialog(caseId) {
      if (caseId) {
        // this.getProjectByCaseId(caseId);
        this.$get('/test/case/get/' + caseId, response => {
          if (response.data) {
            /*
                        this.$refs.testCaseEditDialog.open(response.data);
            */
          }
        });
      } else {
        /*
                this.$refs.testCaseEditDialog.open();
        */
      }
    },
    setTreeNodes(data) {
      this.treeNodes = data;
    },
    setCondition(data) {
      this.condition = data;
    },
    getProject() {
      this.$get('/project_application/get/config/' + this.projectId + "/CASE_CUSTOM_NUM", result => {
        let data = result.data;
        if (data) {
          this.$store.commit('setCurrentProjectIsCustomNum', data.caseCustomNum);
        }
      });
    },
    enableTrash(data) {
      this.initApiTableOpretion = "trashEnable";
      this.trashEnable = data;
    },
    enablePublic(data) {
      this.initApiTableOpretion = "publicEnable";
      this.publicEnable = !data;
      this.$nextTick(() => {
        this.publicEnable = data;
      })
    },
    toPublic(data) {
      if (data === 'public') {
        this.activeName = "public"
      } else {
        this.activeName = "trash"
      }

    },
    changeVersion(currentVersion) {
      this.currentVersion = currentVersion || null;
    },
    changeTrashVersion(currentVersion) {
      this.currentTrashVersion = currentVersion || null;
    },
    checkout(testCase, item) {
      Object.assign(item.testCaseInfo, testCase)
      //子组件先变更 copy 状态，再执行初始化操作
      for (let i = 0; i < this.$refs.testCaseEdit.length; i++) {
        this.$refs.testCaseEdit[i].initEdit(item.testCaseInfo, () => {
          this.$nextTick(() => {
            let vh = this.$refs.testCaseEdit[i].$refs.versionHistory;
            vh.getVersionOptionList(vh.handleVersionOptions);
            vh.show = false;
            vh.loading = false;
          });
        });
      }
    },
    checkoutPublic(testCase, item) {
      Object.assign(item.testCaseInfo, testCase)
      //子组件先变更 copy 状态，再执行初始化操作
      this.$refs.testCaseEditShow[0].initEdit(item.testCaseInfo, () => {
        this.$nextTick(() => {
          let vh = this.$refs.testCaseEditShow[0].$refs.versionHistory;
          vh.getVersionOptionList(vh.handleVersionOptions);
          vh.show = false;
          vh.loading = false;
        });
      });
    },
    checkVersionEnable() {
      if (!this.projectId) {
        return;
      }
      if (hasLicense()) {
        this.$get('/project/version/enable/' + this.projectId, response => {
          this.versionEnable = response.data;
        });
      }
    },
  }
};
</script>

<style scoped>

.el-main {
  padding: 5px 10px;
}

/deep/ .el-tabs__header {
  margin: 0 0 0px;
  /*width: calc(100% - 90px);*/
}

/deep/ .el-table__empty-block {
  width: 100%;
  min-width: 100%;
  max-width: 100%;
  padding-right: 100%;
}

.version-select {
  padding-left: 10px;
}

</style>
