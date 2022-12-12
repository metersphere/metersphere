<template>
  <ms-container v-if="renderComponent" v-loading="loading">
    <!-- operate-button  -->
    <div class="top-btn-group-layout" v-if="!showPublicNode && !showTrashNode && !editable" style="margin-bottom: 16px">
      <el-button size="small" icon="el-icon-plus" v-permission="['PROJECT_TRACK_CASE:READ+BATCH_EDIT']" @click="handleCreateCase" class="iconBtn" type="primary">
        {{$t('test_track.case.create_case')}}
      </el-button>
      <el-dropdown @command="handleImportCommand" placement="bottom-start" style="margin-left: 12px">
        <el-button size="small" v-permission="['PROJECT_TRACK_CASE:READ+IMPORT']" class="btn-dropdown">
          {{$t('commons.import')}}
        </el-button>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="excel">
            <span class="export-model">{{$t('test_track.case.import.import_by_excel')}}</span>
            <span class="export-tips">{{$t('test_track.case.export.export_to_excel_tips1')}}</span>
          </el-dropdown-item>
          <el-dropdown-item style="margin-top: 10px" command="xmind">
            <span class="export-model">{{$t('test_track.case.import.import_by_xmind')}}</span>
            <span class="export-tips">{{$t('test_track.case.export.export_to_xmind_tips')}}</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
      <el-dropdown @command="handleExportCommand" placement="bottom-start" style="margin-left: 12px" class="btn-dropdown">
        <el-button size="small" v-permission="['PROJECT_TRACK_CASE:READ+EXPORT']">
          {{$t('commons.export')}}
        </el-button>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="excel">
            <span class="export-model">{{$t('test_track.case.export.export_to_excel')}}</span>
            <span class="export-tips">{{$t('test_track.case.export.export_to_excel_tips')}}</span>
          </el-dropdown-item>
          <el-dropdown-item style="margin-top: 10px" command="xmind">
            <span class="export-model">{{$t('test_track.case.export.export_to_xmind')}}</span>
            <span class="export-tips">{{$t('test_track.case.export.export_to_xmind_tips')}}</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>

    <!-- public, trash back header  -->
    <div v-if="showPublicNode || showTrashNode" class="back-layout">
      <i class="el-icon-back" style="float: left;position: relative;top: 15px;left: 21px;" @click="activeName = 'default'"/>
      <span class="back-content">{{showPublicNode? $t('project.case_public') : $t('commons.trash')}}</span>
    </div>

    <div style="display: flex; height: calc(100vh - 130px)" v-if="!editable" class = "test-case-aside-layout">
      <!-- case-aside-container  -->
      <ms-aside-container v-show="isAsideHidden" :min-width="'0'" :enable-aside-hidden.sync="enableAsideHidden">
        <test-case-node-tree
          :type="'edit'"
          :total='total'
          :show-operator="false"
          :public-total="publicTotal"
          :case-condition="condition"
          @handleExportCheck="handleExportCheck"
          @refreshTable="refresh"
          @setTreeNodes="setTreeNodes"
          @exportTestCase="exportTestCase"
          @refreshAll="refreshAll"
          @enableTrash="enableTrash"
          @enablePublic="enablePublic"
          @toPublic="toPublic"
          @importRefresh="importRefresh"
          @importChangeConfirm="importChangeConfirm"
          @createCase="handleCaseSimpleCreate($event, 'add')"
          ref="nodeTree"/>
      </ms-aside-container>

      <!-- public-case-aside-container  -->
      <ms-aside-container v-if="showPublicNode">
        <test-case-public-node-tree
          :show-operator="false"
          :case-condition="publicCondition"
          @nodeSelectEvent="publicNodeChange"
          ref="publicNodeTree"/>
      </ms-aside-container>

      <!-- trash-case-aside-container  -->
      <ms-aside-container v-if="showTrashNode">
        <test-case-trash-node-tree
          :show-operator="false"
          :case-condition="trashCondition"
          @nodeSelectEvent="trashNodeChange"
          ref="trashNodeTree"/>
      </ms-aside-container>

      <!-- case-main-container  -->
      <ms-main-container v-if="!showPublicNode && !showTrashNode">
        <ms-tab-button
          :active-dom="activeDom"
          @update:activeDom="updateActiveDom"
          :left-tip="$t('test_track.case.list')"
          :left-icon-class="'icon_view-list_outlined'"
          :left-icon-active-class="'icon_view-list_outlined_active'"
          :right-tip="$t('test_track.case.minder')"
          :right-icon-class="'icon_mindnote_outlined'"
          :right-icon-active-class="'icon_mindnote_outlined_active'"
          :middle-button-enable="false">
          <test-case-list
            v-if="activeDom === 'left'"
            :isRedirectEdit="isRedirectEdit"
            :tree-nodes="treeNodes"
            :trash-enable="false"
            :public-enable="false"
            :current-version="currentVersion"
            :version-enable.sync="versionEnable"
            @closeExport="closeExport"
            @refreshTable="refresh"
            @getTrashList="getTrashList"
            @getPublicList="getPublicList"
            @refresh="refresh"
            @refreshAll="refreshAll"
            @setCondition="setCondition"
            @decrease="decrease"
            @search="refreshTreeByCaseFilter"
            @openExcelExport="openExportDialog"
            ref="testCaseList">
          </test-case-list>
          <test-case-minder
            :current-version="currentVersion"
            :tree-nodes="treeNodes"
            :project-id="projectId"
            :condition="condition"
            :active-name="activeName"
            v-if="activeDom === 'right'"
            @refresh="minderSaveRefresh"
            @toggleMinderFullScreen="toggleMinderFullScreen"
            ref="minder"/>
        </ms-tab-button>

        <is-change-confirm
          @confirm="changeConfirm"
          ref="isChangeConfirm"/>
      </ms-main-container>

      <!-- public-main-container  -->
      <ms-main-container v-if="showPublicNode">
        <el-card class="card-content">
          <public-test-case-list
            :tree-nodes="treeNodes"
            :version-enable="versionEnable"
            @refreshTable="refresh"
            @testCaseCopy="copyTestCase"
            @refresh="refresh"
            @refreshAll="refreshAll"
            @refreshPublic="refreshPublic"
            @setCondition="setPublicCondition"
            @search="refreshTreeByCaseFilter"
            ref="testCasePublicList"/>
        </el-card>
      </ms-main-container>

      <!-- trash-main-container  -->
      <ms-main-container v-if="showTrashNode">
        <el-card class="card-content">
          <test-case-list
            :isRedirectEdit="isRedirectEdit"
            :tree-nodes="treeNodes"
            :trash-enable="true"
            :current-version="currentTrashVersion"
            :version-enable="versionEnable"
            @testCaseCopy="copyTestCase"
            @refresh="refreshTrashNode"
            @refreshAll="refreshAll"
            @setCondition="setTrashCondition"
            @search="refreshTreeByCaseFilter"
            ref="testCaseTrashList">
          </test-case-list>
        </el-card>
      </ms-main-container>
    </div>
    <!-- since v2.6 创建用例流程变更 -->
    <ms-container v-if="editable" class = "edit-layout">
      <div v-for="item in tabs" :key="item.name">
        <test-case-edit
          :currentTestCaseInfo="item.testCaseInfo"
          :version-enable="versionEnable"
          @refresh="refreshAll"
          @caseEdit="handleCaseCreateOrEdit($event, 'edit')"
          @caseCreate="handleCaseCreateOrEdit($event, 'add')"
          @checkout="checkout($event, item)"
          :is-public="item.isPublic"
          :read-only="testCaseReadOnly"
          :tree-nodes="treeNodes"
          :select-node="selectNode"
          :select-condition="item.isPublic ? publicCondition : condition"
          :public-enable="item.isPublic"
          :case-type="type"
          @addTab="addTab"
          @closeTab="closeTab"
          :editable="item.edit"
          ref="testCaseEdit"
        >
        </test-case-edit>
      </div>
    </ms-container>

    <!--  dialog  -->
    <!-- export case -->
    <test-case-export-to-excel @exportTestCase="exportTestCase" ref="exportExcel" class="export-case-layout"/>
    <!-- import case -->
    <test-case-common-import-new ref="caseImport" @refreshAll="refreshAll"/>
  </ms-container>
</template>

<script>
import TestCaseExportToExcel from "@/business/case/components/export/TestCaseExportToExcel";
import TestCaseCommonImportNew from "@/business/case/components/import/TestCaseCommonImportNew";
import TestCaseEdit from "./components/TestCaseEdit";
import TestCaseList from "./components/TestCaseList";
import SelectMenu from "../common/SelectMenu";
import MsContainer from "metersphere-frontend/src/components/new-ui/MsContainer";
import MsAsideContainer from "metersphere-frontend/src/components/new-ui/MsAsideContainer";
import MsMainContainer from "metersphere-frontend/src/components/new-ui/MsMainContainer";
import MsMainButtonGroup from "metersphere-frontend/src/components/new-ui/MsMainButtonGroup";
import {getCurrentProjectID, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {hasLicense, hasPermission} from "metersphere-frontend/src/utils/permission";
import {getUUID} from "metersphere-frontend/src/utils";
import TestCaseNodeTree from "@/business/module/TestCaseNodeTree";
import MsTabButton from "metersphere-frontend/src/components/new-ui/MsTabButton";
import TestCaseMinder from "../common/minder/TestCaseMinder";
import IsChangeConfirm from "metersphere-frontend/src/components/IsChangeConfirm";
import {openMinderConfirm} from "../common/minder/minderUtils";
import TestCaseEditShow from "./components/TestCaseEditShow";
import {PROJECT_ID} from "metersphere-frontend/src/utils/constants";
import MxVersionSelect from "metersphere-frontend/src/components/version/MxVersionSelect";
import {useStore} from "@/store";
import {testCaseNodePublicCount, testCaseNodeTrashCount} from "@/api/test-case-node";
import {getTestCase} from "@/api/testCase";
import {getProjectApplicationConfig} from "@/api/project-application";
import {versionEnableByProjectId} from "@/api/project";
import TestCasePublicNodeTree from "@/business/module/TestCasePublicNodeTree";
import TestCaseTrashNodeTree from "@/business/module/TestCaseTrashNodeTree";
import PublicTestCaseList from "@/business/case/components/public/PublicTestCaseList";
import {openCaseEdit} from "@/business/case/test-case";

const store = useStore();
export default {
  name: "TestCase",
  components: {
    PublicTestCaseList, TestCaseTrashNodeTree, TestCasePublicNodeTree, IsChangeConfirm, TestCaseMinder, MsTabButton, TestCaseNodeTree,
    MsMainContainer, MsAsideContainer, MsContainer, TestCaseList, TestCaseEdit, SelectMenu, TestCaseEditShow, 'VersionSelect': MxVersionSelect,
    MsMainButtonGroup, TestCaseExportToExcel, TestCaseCommonImportNew
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
      trashCondition: {},
      publicCondition: {},
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
      ignoreTreeNodes: false,
      hasRefreshDefault: true,
      enableAsideHidden: false
    };
  },
  created() {
    let projectId = this.$route.query.projectId;
    if (projectId) {
      this.ignoreTreeNodes = true;
      if (projectId !== getCurrentProjectID() && projectId !== 'all') {
        sessionStorage.setItem(PROJECT_ID, projectId);
      }
    }
  },
  mounted() {
    this.getProject();
    this.checkVersionEnable();
  },
  beforeRouteLeave(to, from, next) {
    if (store.isTestCaseMinderChanged) {
      this.$refs.isChangeConfirm.open();
      this.tmpPath = to.path;
    } else {
      next();
    }
  },
  watch: {
    activeName(newVal, oldVal) {
      this.isAsideHidden = this.activeName === 'default';
      if (oldVal !== 'default' && newVal === 'default' && this.$refs.minder) {
        this.$refs.minder.refresh();
      }
      if (oldVal === 'trash' && newVal === 'default') {
        this.condition.filters.status = [];
        // 在回收站恢复后，切到列表页面刷新
        if (!this.hasRefreshDefault) {
          this.refreshAll();
          this.hasRefreshDefault = true;
        } else {
          this.refresh();
        }
      } else if (newVal === 'default') {
        this.refresh();
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
        this.publicEnable = false;
        this.$nextTick(() => {
          this.$refs.trashNodeTree.list();
        });
      }
    },
    publicEnable() {
      if (this.publicEnable) {
        this.activeName = 'public';
        this.$nextTick(() => {
          this.$refs.publicNodeTree.list();
        });
        this.trashEnable = false;
      }
    },
    '$store.state.temWorkspaceId'() {
      if (this.$store.state.temWorkspaceId) {
        this.$refs.isChangeConfirm.open(null, this.$store.state.temWorkspaceId);
      }
    }
  },
  computed: {
    isRedirectEdit: function () {
      return this.$route.params.dataSelectRange;
    },
    showPublicNode() {
      return this.activeName === 'public';
    },
    showTrashNode() {
      return this.activeName === 'trash';
    },
    projectId() {
      return getCurrentProjectID();
    },
    selectNodeIds() {
      return store.testCaseSelectNodeIds;
    },
    selectNode() {
      return store.testCaseSelectNode;
    },
    moduleOptions() {
      return store.testCaseModuleOptions;
    },
    editable() {
      return this.tabs.length > 0;
    },
  },
  methods: {
    hasPermission,
    handleCreateCase(){
      let TestCaseData = this.$router.resolve({path: "/track/case/create",});
      window.open(TestCaseData.href, "_blank");
    },
    closeTab(){
      this.handleTabClose();
    },
    handleCommand(e) {
      switch (e) {
        case "ADD":
          this.addTab({name: 'add'});
          break;
      }
    },
    addTab(tab) {
      this.showPublic = false
      if (tab.name === 'edit' || tab.name === 'show') {
        let label = this.$t('test_track.case.create');
        let name = getUUID().substring(0, 8);
        if (this.activeName === 'public') {
          this.currentActiveName = 'public'
        } else {
          this.currentActiveName = 'default'
        }
        this.activeName = name;
        label = tab.testCaseInfo.name;
        this.tabs = [];
        this.tabs.push({ edit: false, label: label, name: name, testCaseInfo: tab.testCaseInfo, isPublic: tab.isPublic});
      }

      if (tab.name === 'public') {
        this.publicEnable = false;
        this.$nextTick(() => {
          this.publicEnable = true;
        })
      } else if (tab.name === 'trash') {
        this.trashEnable = false;
        this.$nextTick(() => {
          this.trashEnable = true;
        })
      }

      this.setCurTabId(tab, 'testCaseEdit');
    },
    handleImportCommand(e) {
      switch (e) {
        case "excel":
          this.$refs.caseImport.open("excel");
          break;
        case "xmind":
          this.$refs.caseImport.open("xmind");
          break;
      }
    },
    handleExportCommand(e) {
      switch (e) {
        case "excel":
          this.openExportDialog(0, true)
          break;
        case "xmind":
          this.exportTestCase(e, {exportAll: true})
          break;
      }
    },
    openExportDialog(size, isExportAll) {
      this.$refs.exportExcel.open(size, isExportAll);
    },
    getTrashList() {
      testCaseNodeTrashCount(this.projectId)
        .then(response => {
          this.total = response.data;
        });
    },
    getPublicList() {
      testCaseNodePublicCount(getCurrentWorkspaceId())
        .then(response => {
          this.publicTotal = response.data;
        });
    },
    setCurTabId(tab, ref) {
      this.$nextTick(() => {
        if (this.$refs && this.$refs[ref]) {
          let index = tab.index ? Number.parseInt(tab.index) : this.tabs.length;
          let cutEditTab = this.$refs[ref][index - 1];
          let curTabId = cutEditTab ? cutEditTab.tabId : null;
          useStore().curTabId = curTabId;
        }
      });
    },
    updateActiveDom(activeDom) {
      openMinderConfirm(this, activeDom);
    },
    importChangeConfirm(isSave) {
      store.isTestCaseMinderChanged = false;
      if (isSave) {
        this.$refs.minder.save(() => {
          this.$refs.nodeTree.handleImport();
        });
      } else {
        this.$refs.nodeTree.handleImport();
      }
    },
    changeConfirm(isSave, temWorkspaceId) {
      if (isSave) {
        this.$refs.minder.save(() => {
          // 保存成功之后再切换tab
          this.activeDom = this.tmpActiveDom;
          this.tmpActiveDom = null;
        });
      } else {
        this.activeDom = this.tmpActiveDom;
        this.tmpActiveDom = null;
      }
      store.isTestCaseMinderChanged = false;
      this.$nextTick(() => {
        if (this.tmpPath) {
          this.$router.push({
            path: this.tmpPath
          });
          this.tmpPath = null;
        }
      });

      if (temWorkspaceId) {
        // 如果是切换工作空间提示的保存，则保存完后跳转到对应的工作空间
        this.$EventBus.$emit('changeWs', temWorkspaceId);
      }
    },
    validateProjectId() {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return false;
      }
      return true;
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
        this.tabs.push({ edit: false, label: label, name: name, testCaseInfo: tab.testCaseInfo});
      }
      this.setCurTabId(this, tab, 'testCaseEditShow');
    },
    handleTabClose() {
      let message = "";
      this.tabs.forEach(t => {
        if (t && store.testCaseMap.has(t.testCaseInfo.id) && store.testCaseMap.get(t.testCaseInfo.id) > 1) {
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
              store.testCaseMap.clear();
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
      if (targetName === 'trash') {
        this.activeName = 'default';
        this.trashEnable = false;
      } else {
        this.closeTabWithSave(targetName);
      }
    },
    closeTabWithSave(targetName) {
      let t = this.tabs.filter(tab => tab.name === targetName);
      let message = "";
      if (t && store.testCaseMap.has(t[0].testCaseInfo.id) && store.testCaseMap.get(t[0].testCaseInfo.id) > 0) {
        message += t[0].testCaseInfo.name;
      }
      if (t[0].label === this.$t('test_track.case.create')) {
        message += this.$t('test_track.case.create');
      }
      if (t[0].testCaseInfo.isCopy) {
        message += t[0].testCaseInfo.name;
      }
      if (message !== "") {
        this.$alert(this.$t('commons.track') + " [ " + message + " ] " + this.$t('commons.confirm_info'), '', {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          callback: (action) => {
            if (action === 'confirm') {
              store.testCaseMap.delete(t[0].testCaseInfo.id);
              this.removeTab(targetName);
            }
          }
        });
      } else {
        store.testCaseMap.delete(t[0].testCaseInfo.id);
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
    handleExportCheck() {
      if (this.$refs.testCaseList.checkSelected()) {
        this.$refs.nodeTree.openExport();
      }
    },
    exportTestCase(type, param) {
      if (this.activeDom !== 'left') {
        this.$warning(this.$t('test_track.case.export.xmind_export_tip'), false);
        return;
      }
      this.$refs.testCaseList.exportTestCase(type, param);
    },
    closeExport() {
      this.$refs.nodeTree.closeExport();
    },
    publicNodeChange(node, nodeIds, pNodes) {
      if (this.$refs.testCasePublicList) {
        this.$refs.testCasePublicList.initTableData(nodeIds);
      }
    },
    trashNodeChange(node, nodeIds, pNodes) {
      if (this.$refs.testCaseTrashList) {
        this.$refs.testCaseTrashList.initTableData(nodeIds);
      }
    },
    increase(id) {
      this.$refs.nodeTree.increase(id);
    },
    decrease(id) {
      this.$refs.nodeTree.decrease(id);
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
    refresh(data) {
      if (this.selectNodeIds && this.selectNodeIds.length > 0) {
        store.testCaseSelectNode = {};
        store.testCaseSelectNodeIds = [];
      }
      this.refreshAll(data);
    },
    refreshTrashNode() {
      this.$refs.trashNodeTree.list();
      this.hasRefreshDefault = false;
    },
    refreshTreeByCaseFilter() {
      if (this.publicEnable) {
        this.$refs.publicNodeTree.list();
      } else if (this.trashEnable) {
        this.$refs.trashNodeTree.list();
      } else {
        this.$refs.nodeTree.list();
      }
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
    refreshAll(data) {
      if (this.$refs.testCaseList) {
        this.$refs.testCaseList.initTableData();
      }
      if(this.$refs.nodeTree){
        this.$refs.nodeTree.list();
      }
      this.setTable(data);
    },
    importRefresh() {
      this.refreshAll();
      if (this.$refs.testCaseEdit && this.$refs.testCaseEdit.length > 0) {
        setTimeout(() => {
          this.$info(this.$t('test_track.case.import.import_refresh_tips'));
        }, 3000)
      }
    },
    minderSaveRefresh() {
      if (this.$refs.testCaseList) {
        this.$refs.testCaseList.initTableData();
      }
      this.$refs.nodeTree.list();
    },
    toggleMinderFullScreen(isFullScreen) {
      this.enableAsideHidden = isFullScreen;
    },
    refreshPublic() {
      if (this.$refs.testCasePublicList) {
        this.$refs.testCasePublicList.initTableData([]);
      }
      this.$refs.publicNodeTree.list();
    },
    setTreeNodes(data) {
      this.treeNodes = data;
    },
    setPublicCondition(data) {
      this.publicCondition = data;
    },
    setTrashCondition(data) {
      this.trashCondition = data;
    },
    setCondition(data) {
      this.condition = data;
    },
    getProject() {
      getProjectApplicationConfig('CASE_CUSTOM_NUM')
        .then(result => {
          let data = result.data;
          if (data && data.typeValue === 'true') {
            store.currentProjectIsCustomNum = true;
          } else {
            store.currentProjectIsCustomNum = false;
          }
        });
    },
    enableTrash(data) {
      this.trashEnable = !data;
      this.$nextTick(() => {
        this.trashEnable = data;
      })
    },
    enablePublic(data) {
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
        versionEnableByProjectId(this.projectId)
          .then(response => {
            this.versionEnable = response.data;
          });
      }
    },
  }
};
</script>

<style scoped>
:deep(.el-card__body) {
  padding: 24px;
}

.el-main {
  padding: 5px 10px;
}

:deep(.el-tabs__header) {
  margin: 0 0 0px;
  /*width: calc(100% - 90px);*/
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

.svg:hover {
  -webkit-filter: drop-shadow(0px 0px 0px #783887);
}

.iconBtn {
  width: 98px;
}

:deep(.iconBtn i){
  position: relative;
  top: -5px;
  width: 12px;
  height: 12px;
}

:deep(.iconBtn span) {
  position: relative;
  left: -7px;
}

.export-model {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  display: flex;
  align-items: center;
  color: #1F2329;
  flex: none;
  order: 0;
  align-self: stretch;
  flex-grow: 0;
}

.export-tips {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 12px;
  line-height: 20px;
  display: block;
  align-items: center;
  color: #8F959E;
  flex: none;
  order: 1;
  align-self: stretch;
  flex-grow: 0;
}

/* 创建用例按钮样式 */
.el-button--small {
    height: 32px;
    border-radius: 4px;
}

.back-layout {
  height: 48px;
  background-color: #FFFFFF;;
  border-bottom: 1px solid rgba(31, 35, 41, 0.15);
  border-radius: 4px 4px 0 0;
}

.back-content {
  position: relative;
  top: 12px;
  left: 35px;
  width: 80px;
  height: 24px;
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 500;
  font-size: 16px;
  line-height: 24px;
  color: #1F2329;
  flex: none;
  order: 1;
  flex-grow: 0;
}

.el-icon-back:before {
  font-size: 20px;
}

:deep(i.el-icon-back:hover) {
  color: #783887;
  cursor: pointer;
}

/* 作用域处理 */
.test-case-aside-layout :deep(.el-button--small span),
.back-layout :deep(.el-button--small span),
.top-btn-group-layout :deep(.el-button--small span),
.export-case-layout :deep(.el-button--small span) {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  position: relative;
  top: -5px;
}

.edit-layout :deep(.el-button--small span){
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  text-align: center;
  /* color:#783887; */
}

.el-button--small {
  min-width: 80px;
  height: 32px;
  border-radius: 4px;
  font-size: 14px;
}

.el-dropdown-menu__item:hover {
  background-color: rgba(31, 35, 41, 0.1)!important;
}
</style>
