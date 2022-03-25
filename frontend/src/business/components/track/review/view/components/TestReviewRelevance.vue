<template>
  <div>
    <el-dialog :title="$t('test_track.review_view.relevance_case')" :visible.sync="dialogFormVisible" @close="close"
               width="75%"
               :close-on-click-modal="false"
               top="50px" :destroy-on-close="true"
               append-to-body>

      <template slot="title" :slot-scope="$t('test_track.review_view.relevance_case')" v-if="!$slots.headerBtn">
        <ms-dialog-header :title="$t('test_track.review_view.relevance_case')" @cancel="dialogFormVisible = false"
                          @confirm="saveReviewRelevance"/>
      </template>

      <el-container class="main-content">
        <el-aside class="tree-aside" width="270px">
          <select-menu
            :data="projects"
            width="173px"
            :current-data="currentProject"
            :title="$t('test_track.switch_project')"
            @dataChange="changeProject"/>
          <node-tree class="node-tree"
                     :is-display="openType"
                     :all-label="$t('commons.all_label.review')"
                     v-loading="result.loading"
                     @nodeSelectEvent="nodeChange"
                     :tree-nodes="treeNodes"
                     ref="nodeTree"/>
        </el-aside>

        <el-container>
          <el-main class="case-content">
            <ms-table-header :condition.sync="condition" @search="search" title="" :show-create="false">
              <template v-slot:searchBarBefore>
                <version-select v-xpack :project-id="projectId" @changeVersion="changeVersion" margin-right="20"/>
              </template>
            </ms-table-header>
            <ms-table :data="testReviews"
                      @filter-change="filter" row-key="id"
                      v-loading="result.loading"
                      :total="total"
                      :page-size.sync="pageSize"
                      @handlePageChange="getReviews"
                      @refresh="getReviews"
                      :condition="condition"
                      ref="table">

              <el-table-column
                prop="name"
                :label="$t('test_track.case.name')"
                style="width: 100%">
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
                show-overflow-tooltip>
                <template v-slot:default="scope">
                  <priority-table-item :value="scope.row.priority"/>
                </template>
              </el-table-column>

              <el-table-column
                prop="type"
                :filters="typeFilters"
                column-key="type"
                :label="$t('test_track.case.type')"
                show-overflow-tooltip>
                <template v-slot:default="scope">
                  <type-table-item :value="scope.row.type"/>
                </template>
              </el-table-column>

              <el-table-column
                :filters="statusFilters"
                column-key="reviewStatus"
                :label="$t('test_track.case.status')"
                show-overflow-tooltip>
                <template v-slot:default="scope">
                  <review-status :value="scope.row.reviewStatus"/>
                </template>
              </el-table-column>

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

import NodeTree from "../../../common/NodeTree";
import MsDialogFooter from "../../../../common/components/MsDialogFooter";
import PriorityTableItem from "../../../common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "../../../common/tableItems/planview/TypeTableItem";
import MsTableSearchBar from "../../../../common/components/MsTableSearchBar";
import MsTableAdvSearchBar from "../../../../common/components/search/MsTableAdvSearchBar";
import MsTableHeader from "../../../../common/components/MsTableHeader";
import SwitchProject from "../../../case/components/SwitchProject";
import {TEST_CASE_CONFIGS} from "../../../../common/components/search/search-components";
import ReviewStatus from "@/business/components/track/case/components/ReviewStatus";
import elTableInfiniteScroll from 'el-table-infinite-scroll';
import SelectMenu from "../../../common/SelectMenu";
import {_filter, initCondition} from "@/common/js/tableUtils";
import {getCurrentProjectID, getCurrentUserId, getCurrentWorkspaceId, hasLicense} from "@/common/js/utils";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import MsDialogHeader from "@/business/components/common/components/MsDialogHeader";
import MsTable from "@/business/components/common/components/table/MsTable";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const VersionSelect = requireComponent.keys().length > 0 ? requireComponent("./version/VersionSelect.vue") : {};


export default {
  name: "TestReviewRelevance",
  components: {
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
    'VersionSelect': VersionSelect.default,
    MsTablePagination,
    MsDialogHeader,
    MsTable
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
      condition: {
        components: TEST_CASE_CONFIGS
      },
      priorityFilters: [
        {text: 'P0', value: 'P0'},
        {text: 'P1', value: 'P1'},
        {text: 'P2', value: 'P2'},
        {text: 'P3', value: 'P3'}
      ],
      typeFilters: [
        {text: this.$t('commons.functional'), value: 'functional'},
        {text: this.$t('commons.performance'), value: 'performance'},
        {text: this.$t('commons.api'), value: 'api'}
      ],
      statusFilters: [
        {text: this.$t('test_track.review.prepare'), value: 'Prepare'},
        {text: this.$t('test_track.review.pass'), value: 'Pass'},
        {text: this.$t('test_track.review.un_pass'), value: 'UnPass'},
      ],
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
        this.search();
      }
    },
    projectId() {
      this.condition.projectId = this.projectId;
      this.getProjectNode();
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
    openTestReviewRelevanceDialog() {
      this.getProject();
      this.dialogFormVisible = true;
      this.getProjectNode(this.projectId);
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
      this.result = this.$post('/test/case/review/relevance', param, () => {
        this.selectIds.clear();
        this.$success(this.$t('commons.save_success'));
        this.dialogFormVisible = false;
        this.$emit('refresh');
      });
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    getReviews(flag) {
      if (this.reviewId) {
        this.condition.reviewId = this.reviewId;
      }
      if (this.selectNodeIds && this.selectNodeIds.length > 0) {
        this.condition.nodeIds = this.selectNodeIds;
      } else {
        this.condition.nodeIds = [];
      }
      initCondition(this.condition, this.condition.selectAll);
      if (this.projectId) {
        this.condition.projectId = this.projectId;
        this.result = this.$post(this.buildPagePath('/test/case/reviews/case'), this.condition, response => {
          let data = response.data;
          this.total = data.itemCount;
          this.testReviews = data.listObject;
        });
      }

    },

    nodeChange(node, nodeIds, nodeNames) {
      this.selectNodeIds = nodeIds;
      this.selectNodeNames = nodeNames;
    },
    refresh() {
      this.close();
    },
    getAllNodeTreeByPlanId() {
      if (this.reviewId) {
        let param = {
          reviewId: this.reviewId,
          projectId: this.projectId
        };
        this.result = this.$post("/case/node/list/all/review", param, response => {
          this.treeNodes = response.data;
        });
      }
    },
    close() {
      this.lineStatus = false;
      this.selectIds.clear();
      this.selectNodeIds = [];
      this.selectNodeNames = [];
      this.dialogFormVisible = false;
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
        this.$post("/project/list/related", {userId: getCurrentUserId(), workspaceId: getCurrentWorkspaceId()}, res => {
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
        })
      }
    },
    switchProject() {
      this.$refs.switchProject.open({id: this.reviewId, url: '/test/case/review/project/', type: 'review'});
    },
    search() {
      this.currentPage = 1;
      this.testReviews = [];
      this.getReviews(true);
    },
    changeProject(project) {
      this.projectId = project.id;
    },

    getProjectNode(projectId) {
      const index = this.projects.findIndex(project => project.id === projectId);
      if (index !== -1) {
        this.projectName = this.projects[index].name;
        this.currentProject = this.projects[index];
      }
      if (projectId) {
        this.projectId = projectId;
      }
      this.result = this.$post("/case/node/list/all/review",
        {reviewId: this.reviewId, projectId: this.projectId}, response => {
          this.treeNodes = response.data;
        });
      this.selectNodeIds = [];
    },
    getVersionOptions() {
      if (hasLicense()) {
        this.$get('/project/version/get-project-versions/' + getCurrentProjectID(), response => {
          this.versionFilters = response.data.map(u => {
            return {text: u.name, value: u.id};
          });
        });
      }
    },
    changeVersion(version) {
      this.condition.versionId = version || null;
      this.search();
    }
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
