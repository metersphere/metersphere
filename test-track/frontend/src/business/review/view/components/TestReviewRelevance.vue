<template>
  <div>
    <el-dialog :title="$t('test_track.review_view.relevance_case')" :visible.sync="dialogFormVisible" @close="close"
               width="75%"
               :close-on-click-modal="false"
               :fullscreen="isFullScreen"
               top="50px" :destroy-on-close="true"
               append-to-body>

      <template slot="title" :slot-scope="$t('test_track.review_view.relevance_case')" v-if="!$slots.headerBtn">
        <ms-dialog-header :title="$t('test_track.review_view.relevance_case')" @cancel="dialogFormVisible = false"
                          @confirm="saveReviewRelevance"
                          @fullScreen="fullScreen"
                          :enable-full-screen="false"
                          :is-full-screen.sync="isFullScreen">
          <template #other>
            <table-select-count-bar :count="selectCounts" style="float: left; margin: 5px;"/>
          </template>
        </ms-dialog-header>
      </template>

      <el-container class="main-content">
        <ms-aside-container
            :min-width="'350'"
            :max-width="'600'"
            :enable-aside-hidden="false"
            :default-hidden-bottom-top="200"
            :enable-auto-height="true"
        >
          <select-menu
              :data="projects"
              width="173px"
              :current-data="currentProject"
              :title="$t('test_track.switch_project')"
              @dataChange="changeProject"/>
          <node-tree class="node-tree"
                     :all-label="$t('commons.all_label.review')"
                     v-loading="result.loading"
                     local-suffix="test_case"
                     default-label="未规划用例"
                     @nodeSelectEvent="nodeChange"
                     :tree-nodes="treeNodes"
                     ref="nodeTree"/>
        </ms-aside-container>

        <el-container>
          <el-main class="case-content">
            <ms-table-header :tip="$t('commons.search_by_name_or_id')" :condition.sync="condition" @search="search"
                             title="" :show-create="false">
              <template v-slot:searchBarBefore>
                <version-select v-xpack :project-id="projectId" @changeVersion="changeVersion" margin-right="20"/>
              </template>
            </ms-table-header>
            <ms-table :data="testReviews"
                      @filter-change="filter" row-key="id"
                      v-loading="result.loading"
                      :total="total"
                      :page-size.sync="pageSize"
                      :screen-height="screenHeight"
                      @handlePageChange="getReviews"
                      @refresh="getReviews"
                      @selectCountChange="setSelectCounts"
                      :condition="condition"
                      ref="table">

              <el-table-column
                  v-if="!customNum"
                  prop="num"
                  min-width="120"
                  sortable
                  :label="$t('commons.id')">
              </el-table-column>

              <el-table-column
                  v-if="customNum"
                  prop="customNum"
                  min-width="120"
                  sortable
                  :label="$t('commons.id')">
              </el-table-column>

              <el-table-column
                  prop="name"
                  :label="$t('test_track.case.name')"
                  min-width="120"
                  show-overflow-tooltip>
                <template v-slot:default="scope">
                  {{ scope.row.name }}
                </template>
              </el-table-column>

              <el-table-column
                  v-if="versionEnable"
                  prop="versionName"
                  :label="$t('test_track.case.version')"
                  column-key="versionId"
                  :filters="versionFilters"
                  style="width: 100%">
                <template v-slot:default="scope">
                  {{ scope.row.versionName }}
                </template>
              </el-table-column>

              <el-table-column
                  prop="priority"
                  :filters="priorityFilters"
                  column-key="priority"
                  :label="$t('test_track.case.priority')"
                  min-width="120"
                  show-overflow-tooltip>
                <template v-slot:default="scope">
                  <priority-table-item :value="scope.row.priority" :priority-options="priorityFilters"/>
                </template>
              </el-table-column>

              <el-table-column
                  :filters="statusFilters"
                  column-key="reviewStatus"
                  :label="$t('test_track.case.status')"
                  min-width="120"
                  show-overflow-tooltip>
                <template v-slot:default="scope">
                  <review-status :value="scope.row.reviewStatus"/>
                </template>
              </el-table-column>

              <ms-update-time-column/>
              <ms-create-time-column/>

            </ms-table>
            <ms-table-pagination :change="getReviews" :current-page.sync="currentPage" :page-size.sync="pageSize"
                                 :total="total"/>

          </el-main>
        </el-container>
      </el-container>


    </el-dialog>

    <switch-project ref="switchProject" @getProjectNode="getProjectNode"/>
  </div>

</template>

<script>

import MsAsideContainer from "metersphere-frontend/src/components/MsAsideContainer";
import NodeTree from "metersphere-frontend/src/components/module/MsNodeTree";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import PriorityTableItem from "@/business/common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "@/business/common/tableItems/planview/TypeTableItem";
import MsTableSearchBar from "metersphere-frontend/src/components/MsTableSearchBar";
import MsTableAdvSearchBar from "metersphere-frontend/src/components/search/MsTableAdvSearchBar";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import SwitchProject from "../../../case/components/SwitchProject";
import {TEST_REVIEW_RELEVANCE_CASE_CONFIGS} from "metersphere-frontend/src/components/search/search-components";
import ReviewStatus from "@/business/case/components/ReviewStatus";
import elTableInfiniteScroll from 'el-table-infinite-scroll';
import SelectMenu from "@/business/common/SelectMenu";
import {hasLicense} from "metersphere-frontend/src/utils/permission";
import {_filter, initCondition} from "metersphere-frontend/src/utils/tableUtils";
import {getCurrentProjectID, getCurrentUserId, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsDialogHeader from "metersphere-frontend/src/components/MsDialogHeader";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import TableSelectCountBar from "metersphere-frontend/src/components/table/TableSelectCountBar";
import VersionSelect from "metersphere-frontend/src/components/version/MxVersionSelect";
import {getTestCaseReviewRelevance, getTestCaseReviewsCasePage} from "@/api/test-review";
import {testCaseNodeListReviewRelate} from "@/api/test-case-node";
import {getVersionFilters} from "@/business/utils/sdk-utils";
import {projectRelated} from "@/api/project";
import {getTestTemplate} from "@/api/custom-field-template";
import {initTestCaseConditionComponents} from "@/business/case/test-case";
import MsCreateTimeColumn from "metersphere-frontend/src/components/table/MsCreateTimeColumn";
import MsUpdateTimeColumn from "metersphere-frontend/src/components/table/MsUpdateTimeColumn";
import {getProjectApplicationConfig} from "@/api/project-application";

export default {
  name: "TestReviewRelevance",
  components: {
    MsAsideContainer,
    TableSelectCountBar,
    SelectMenu,
    NodeTree,
    MsDialogFooter,
    PriorityTableItem,
    TypeTableItem,
    MsTableSearchBar,
    MsTableAdvSearchBar,
    MsTableHeader,
    SwitchProject,
    ReviewStatus,
    'VersionSelect': VersionSelect,
    MsTablePagination,
    MsDialogHeader,
    MsTable,
    MsUpdateTimeColumn,
    MsCreateTimeColumn,
  },
  directives: {
    'el-table-infinite-scroll': elTableInfiniteScroll
  },
  data() {
    return {
      openType: 'relevance',
      checked: true,
      result: {},
      currentProject: {},
      dialogFormVisible: false,
      isCheckAll: false,
      testReviews: [],
      versionFilters: [],
      selectIds: new Set(),
      treeNodes: [],
      selectNodeIds: [],
      selectNodeNames: [],
      projectId: '',
      projectName: '',
      projects: [],
      pageSize: 10,
      currentPage: 1,
      total: 0,
      lineStatus: true,
      customNum: false,
      condition: {
        components: TEST_REVIEW_RELEVANCE_CASE_CONFIGS
      },
      priorityFilters: [],
      statusFilters: [
        {text: this.$t('test_track.review.prepare'), value: 'Prepare'},
        {text: this.$t('test_track.review.pass'), value: 'Pass'},
        {text: this.$t('test_track.review.un_pass'), value: 'UnPass'},
        {text: this.$t('test_track.review.again'), value: 'Again'},
        {text: this.$t('test_track.review.underway'), value: 'Underway'},
      ],
      selectCounts: null,
      isFullScreen: false,
      screenHeight: 'calc(100vh - 420px)'
    };
  },
  props: {
    reviewId: {
      type: String
    },
    versionEnable: {
      type: Boolean,
      default: false
    }
  },
  watch: {
    reviewId() {
      this.condition.reviewId = this.reviewId;
    },
    selectNodeIds() {
      if (this.dialogFormVisible) {
        this.getReviews();
      }
    },
    projectId() {
      this.setConditionModuleIdParam();
      this.condition.projectId = this.projectId;
      this.condition.versionId = null;
      this.getVersionOptions();
      this.getProjectNode();
      this.getCustomNum();
    }
  },
  mounted() {
    if (hasLicense()) {
      this.getVersionOptions();
    }
  },
  updated() {
    this.toggleSelection(this.testReviews);
  },
  methods: {
    loadConditionComponents() {
      getTestTemplate(this.projectId).then((template) => {
        this.initPriorityFilters(template);
        this.condition.components = initTestCaseConditionComponents(this.condition, template.customFields, false);
      });
    },
    initPriorityFilters(template) {
      template.customFields.forEach(field => {
        if (field.name === '用例等级') {
          this.priorityFilters = field.options;
        }
      })
    },
    fullScreen() {
      this.isFullScreen = !this.isFullScreen;
      this.screenHeight = this.isFullScreen ? 'calc(100vh - 180px)' : 'calc(100vh - 420px)'
    },
    setConditionModuleIdParam() {
      this.condition.components.forEach(component => {
        if (component.key === 'moduleIds') {
          component.options.params = {"projectId": this.projectId};
        }
      });
    },
    async openTestReviewRelevanceDialog() {
      // 高级搜索基础字段
      this.condition = {components: TEST_REVIEW_RELEVANCE_CASE_CONFIGS};
      this.getProject();
      this.dialogFormVisible = true;
      await this.getProjectNode(this.projectId);
      this.getReviews();
      // 高级搜索自定义字段
      this.loadConditionComponents();
    },
    saveReviewRelevance() {
      let param = {};
      param.reviewId = this.reviewId;
      param.testCaseIds = this.$refs.table.selectIds;
      param.request = this.condition;
      /*
              param.checked = this.checked;
      */
      // 选择全选则全部加入到评审，无论是否加载完全部
      if (this.condition.selectAll) {
        param.testCaseIds = ['all'];
      }
      getTestCaseReviewRelevance(param)
          .then(() => {
            this.selectIds.clear();
            this.selectCounts = 0;
            this.$success(this.$t('commons.save_success'));
            this.dialogFormVisible = false;
            this.$emit('refresh');
          });
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    getReviews() {
      if (this.reviewId) {
        this.condition.reviewId = this.reviewId;
      }
      if (this.selectNodeIds && this.selectNodeIds.length > 0) {
        this.condition.nodeIds = this.selectNodeIds;
      } else {
        this.condition.nodeIds = [];
      }
      this.result.loading = true;
      initCondition(this.condition, this.condition.selectAll);
      this.condition.projectId = this.projectId || getCurrentProjectID();
      getTestCaseReviewsCasePage(this.currentPage, this.pageSize, this.condition)
          .then((response) => {
            let data = response.data;
            this.total = data.itemCount;
            this.testReviews = data.listObject;
            this.result.loading = false;
          });
    },
    setSelectCounts(data) {
      this.selectCounts = data;
    },
    nodeChange(node, nodeIds, nodeNames) {
      this.selectNodeIds = nodeIds;
      this.selectNodeNames = nodeNames;
    },
    refresh() {
      this.close();
    },
    close() {
      this.testReviews = [];
      this.treeNodes = [];
      this.lineStatus = false;
      this.selectIds.clear();
      this.selectNodeIds = [];
      this.selectNodeNames = [];
      this.dialogFormVisible = false;
      this.condition.filters = {};
      this.condition.combine = {};
      if (this.condition.projectId) {
        delete this.condition.projectId;
      }
      this.projectId = '';
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.search();
    },
    toggleSelection(rows) {
      rows.forEach(row => {
        this.selectIds.forEach(id => {
          if (row.id === id) {
            // true 是为选中
            this.$refs.table.toggleRowSelection(row, true)
          }
        })
      })
    },
    getProject() {
      if (this.reviewId) {
        projectRelated({userId: getCurrentUserId(), workspaceId: getCurrentWorkspaceId()})
            .then((res) => {
              let data = res.data;
              if (data) {
                this.projects = data;
                const index = data.findIndex(d => d.id === getCurrentProjectID());
                if (index !== -1) {
                  this.projectId = data[index].id;
                  this.projectName = data[index].name;
                  this.currentProject = data[index];
                } else {
                  this.projectId = data[0].id;
                  this.projectName = data[0].name;
                  this.currentProject = data[0];
                }
              }
            });
      }
    },
    switchProject() {
      this.$refs.switchProject.open({id: this.reviewId, url: '/test/case/review/project/', type: 'review'});
    },
    search() {
      this.currentPage = 1;
      this.testReviews = [];
      this.getReviews();
      this.getProjectNode(this.projectId, this.condition);
    },
    changeProject(project) {
      this.projectId = project.id;
      this.loadConditionComponents();
    },
    getProjectNode(projectId, condition) {
      return new Promise((resolve) => {
        const index = this.projects.findIndex(project => project.id === projectId);
        if (index !== -1) {
          this.projectName = this.projects[index].name;
          this.currentProject = this.projects[index];
        }
        if (projectId) {
          this.projectId = projectId;
        }
        testCaseNodeListReviewRelate({reviewId: this.reviewId, projectId: this.projectId, ...condition})
            .then((response) => {
              this.treeNodes = response.data;
              resolve();
            });
        this.selectNodeIds = [];
      });
    },
    getVersionOptions() {
      getVersionFilters(this.projectId)
          .then(r => this.versionFilters = r.data);
    },
    changeVersion(version) {
      this.condition.versionId = version || null;
      this.search();
    },
    getCustomNum() {
      getProjectApplicationConfig('CASE_CUSTOM_NUM')
          .then(result => {
            let data = result.data;
            if (data && data.typeValue === 'true') {
              this.customNum = true;
            } else {
              this.customNum = false;
            }
          });
    },
  }
}
</script>

<style scoped>

.tb-edit .el-input {
  display: none;
  color: black;
}

.tb-edit .current-row .el-input {
  display: block;

}

.tb-edit .current-row .el-input + span {
  display: none;

}

.node-tree {
  margin-right: 10px;
  height: 52vh;
  overflow-y: auto;
  padding-right: 4px;
}

.el-header {
  background-color: darkgrey;
  color: #333;
  line-height: 60px;
}

.case-content {
  padding: 0px 20px;
  height: 100%;
  /*border: 1px solid #EBEEF5;*/
}

.tree-aside {
  min-height: 300px;
  max-height: 100%;
}

.main-content {
  min-height: 300px;
  height: 100%;
  /*border: 1px solid #EBEEF5;*/
}

.project-link {
  float: right;
  margin-right: 12px;
  margin-bottom: 10px;
}

</style>
