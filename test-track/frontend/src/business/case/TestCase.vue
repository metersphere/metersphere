<template>
  <ms-container v-if="renderComponent" v-loading="loading">
    <!-- operate-button  -->
    <div class="top-btn-group-layout" v-if="!showPublicNode && !showTrashNode" style="margin-bottom: 16px">
      <el-button size="small" v-permission="['PROJECT_TRACK_CASE:READ+CREATE']" @click="handleCreateCase" type="primary">
        <svg-icon icon-class="icon_add_outlined_white"/>
        {{$t('test_track.case.create_case')}}
      </el-button>
      <el-dropdown @command="handleImportCommand" placement="bottom-start" style="margin-left: 12px" class="btn-dropdown">
        <el-button size="small" v-permission="['PROJECT_TRACK_CASE:READ+IMPORT']">
          <svg-icon icon-class="icon_upload_outlined"/>
          {{$t('commons.import')}}
        </el-button>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="excel">
            <span class="export-model">{{$t('test_track.case.import.import_by_excel')}}</span>
            <span class="export-tips">{{$t('test_track.case.export.export_to_excel_tips1')}}</span>
          </el-dropdown-item>
          <el-dropdown-item style="margin-top: 10px" command="xmind" divided>
            <span class="export-model">{{$t('test_track.case.import.import_by_xmind')}}</span>
            <span class="export-tips">{{$t('test_track.case.export.export_to_xmind_tips')}}</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>

    <!-- public, trash back header  -->
    <div v-show="showPublicNode || showTrashNode" class="back-layout">
      <i class="el-icon-back" style="float: left;position: relative;top: 15px;left: 21px;" @click="activeName = 'default'"/>
      <span class="back-content">{{showPublicNode? $t('project.case_public') : $t('commons.trash')}}</span>
    </div>

    <div style="display: flex; height: calc(100vh - 130px)" class = "test-case-aside-layout">
      <!-- case-aside-container  -->
      <ms-aside-container v-show="isAsideHidden"
                          page-key="TEST_CASE_LIST"
                          :enable-remember-width="true"
                          :min-width="'0'"
                          :enable-aside-hidden.sync="enableAsideHidden">
        <test-case-node-tree
          :type="'edit'"
          :total='total'
          :show-operator="false"
          :public-total="publicTotal"
          :case-condition="condition"
          :is-minder-mode="isMinderMode"
          @refreshTable="refresh"
          @setTreeNodes="setTreeNodes"
          @refreshAll="refreshAll"
          @enableTrash="enableTrash"
          @enablePublic="enablePublic"
          @toPublic="toPublic"
          @importRefresh="importRefresh"
          @importChangeConfirm="importChangeConfirm"
          @createCase="handleCaseSimpleCreate($event, 'add')"
          @nodeSelectEvent="handleCaseNodeSelect"
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
            :default-version="currentVersion"
            :version-enable.sync="versionEnable"
            @refreshTable="refresh"
            @getTrashList="getTrashList"
            @getPublicList="getPublicList"
            @refresh="refresh"
            @refreshAll="refreshAll"
            @setCondition="setCondition"
            @decrease="decrease"
            @search="refreshTreeByCaseFilter"
            ref="testCaseList">
          </test-case-list>
          <test-case-minder
            v-if="isMinderMode"
            :default-version="currentVersion"
            :tree-nodes="treeNodes"
            :project-id="projectId"
            :condition="condition"
            :active-name="activeName"
            @versionChange="changeVersion"
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
            @refresh="refreshTrashNode"
            @refreshAll="refreshAll"
            @setCondition="setTrashCondition"
            @search="refreshTreeByCaseFilter"
            ref="testCaseTrashList">
          </test-case-list>
        </el-card>
      </ms-main-container>
    </div>

    <!-- import case -->
    <test-case-common-import-new ref="caseImport" @refreshAll="refreshAll"/>
  </ms-container>
</template>

<script>
import TestCaseCommonImportNew from "@/business/case/components/import/TestCaseCommonImportNew";
import TestCaseList from "./components/TestCaseList";
import SelectMenu from "../common/SelectMenu";
import MsContainer from "metersphere-frontend/src/components/new-ui/MsContainer";
import MsAsideContainer from "metersphere-frontend/src/components/new-ui/MsAsideContainer";
import MsMainContainer from "metersphere-frontend/src/components/new-ui/MsMainContainer";
import MsMainButtonGroup from "metersphere-frontend/src/components/new-ui/MsMainButtonGroup";
import {getCurrentProjectID, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {hasLicense, hasPermission} from "metersphere-frontend/src/utils/permission";
import TestCaseNodeTree from "@/business/module/TestCaseNodeTree";
import MsTabButton from "metersphere-frontend/src/components/new-ui/MsTabButton";
import TestCaseMinder from "../common/minder/TestCaseMinder";
import IsChangeConfirm from "metersphere-frontend/src/components/IsChangeConfirm";
import {openMinderConfirm} from "../common/minder/minderUtils";
import {PROJECT_ID} from "metersphere-frontend/src/utils/constants";
import MxVersionSelect from "metersphere-frontend/src/components/version/MxVersionSelect";
import {useStore} from "@/store";
import {testCaseNodePublicCount, testCaseNodeTrashCount} from "@/api/test-case-node";
import {getProjectApplicationConfig} from "@/api/project-application";
import {versionEnableByProjectId} from "@/api/project";
import TestCasePublicNodeTree from "@/business/module/TestCasePublicNodeTree";
import TestCaseTrashNodeTree from "@/business/module/TestCaseTrashNodeTree";
import PublicTestCaseList from "@/business/case/components/public/PublicTestCaseList";
import {openCaseCreate} from "@/business/case/test-case";
import merge from 'webpack-merge';

const store = useStore();
export default {
  name: "TestCase",
  components: {
    PublicTestCaseList, TestCaseTrashNodeTree, TestCasePublicNodeTree, IsChangeConfirm, TestCaseMinder, MsTabButton, TestCaseNodeTree,
    MsMainContainer, MsAsideContainer, MsContainer, TestCaseList, SelectMenu, 'VersionSelect': MxVersionSelect,
    MsMainButtonGroup, TestCaseCommonImportNew
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
    let projectId = this.$route.params.projectId;
    if (projectId) {
      this.ignoreTreeNodes = true;
      if (projectId !== getCurrentProjectID() && projectId !== 'all') {
        sessionStorage.setItem(PROJECT_ID, projectId);
      }
    }
  },
  mounted() {
    this.currentVersion = this.defaultVersion || null;
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
    },
    routeModuleId() {
      return this.$route.query.moduleId;
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
    isMinderMode() {
      return this.activeDom === 'right';
    }
  },
  methods: {
    hasPermission,
    handleCreateCase(){
      openCaseCreate({
        projectId: this.projectId,
        createNodeId: this.selectNode.data && this.selectNode.data.id !== 'root' ? this.selectNode.data.id : ""
      }, this);
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
    updateActiveDom(activeDom) {
      openMinderConfirm(this, activeDom, 'PROJECT_TRACK_CASE:READ');
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
    handleCaseNodeSelect(node, nodeIds, pNodes) {
      if (node.data.id !== this.routeModuleId) {
        this.$router.push({
          query: merge(this.$route.query, {'moduleId': node.data.id})
        });
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
    refreshTreeByCaseFilter(currentVersion) {
      this.condition.versionId = currentVersion || null;
      this.currentVersion = this.condition.versionId;
      if (this.publicEnable) {
        this.$refs.publicNodeTree.list();
      } else if (this.trashEnable) {
        this.$refs.trashNodeTree.list();
      } else {
        this.$refs.nodeTree.list();
      }
    },
    refreshAll() {
      if (this.$refs.testCaseList) {
        this.$refs.testCaseList.initTableData();
      }
      if(this.$refs.nodeTree){
        this.$refs.nodeTree.list();
      }
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
      this.$refs.nodeTree.list({ isForceSetCurrentKey: true });
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

:deep(.svg-icon) {
  position: relative;
  top: 2px;
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
