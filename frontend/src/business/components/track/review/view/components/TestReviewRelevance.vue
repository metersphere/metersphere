<template>

  <div>

    <el-dialog :title="$t('test_track.review_view.relevance_case')"
               :visible.sync="dialogFormVisible"
               @close="close"
               width="60%" v-loading="result.loading"
               :close-on-click-modal="false"
               top="50px">

      <el-container class="main-content">
        <el-aside class="tree-aside" width="250px">
          <el-link type="primary" class="project-link" @click="switchProject">{{projectName ? projectName : $t('test_track.switch_project') }}</el-link>
          <node-tree class="node-tree"
                     @nodeSelectEvent="nodeChange"
                     @refresh="refresh"
                     :tree-nodes="treeNodes"
                     ref="nodeTree"/>
        </el-aside>

        <el-container>
          <el-main class="case-content">
            <ms-table-header :condition.sync="condition" @search="getReviews" title="" :show-create="false"/>
            <el-table
              :data="testReviews"
              @filter-change="filter"
              row-key="id"
              @select-all="handleSelectAll"
              @select="handleSelectionChange"
              height="50vh"
              ref="table">

              <el-table-column
                type="selection"/>

              <el-table-column
                prop="name"
                :label="$t('test_track.case.name')"
                style="width: 100%">
                <template v-slot:default="scope">
                  {{scope.row.name}}
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
                column-key="status"
                :label="$t('test_track.case.status')"
                show-overflow-tooltip>
                <template v-slot:default="scope">
                  <status-table-item :value="scope.row.reviewStatus"/>
                </template>
              </el-table-column>
            </el-table>
            <div style="text-align: center">共 {{testReviews.length}} 条</div>
          </el-main>
        </el-container>
      </el-container>

      <template v-slot:footer>
        <ms-dialog-footer @cancel="dialogFormVisible = false" @confirm="saveReviewRelevance"/>
      </template>

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
import {_filter} from "../../../../../../common/js/utils";
import StatusTableItem from "@/business/components/track/common/tableItems/planview/StatusTableItem";

export default {
  name: "TestReviewRelevance",
  components: {
    NodeTree,
    MsDialogFooter,
    PriorityTableItem,
    TypeTableItem,
    MsTableSearchBar,
    MsTableAdvSearchBar,
    MsTableHeader,
    SwitchProject,
    StatusTableItem
  },
  data() {
    return {
      result: {},
      dialogFormVisible: false,
      isCheckAll: false,
      testReviews: [],
      selectIds: new Set(),
      treeNodes: [],
      selectNodeIds: [],
      selectNodeNames: [],
      projectId: '',
      projectName: '',
      projects: [],
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
        {text: this.$t('test_track.case.status_prepare'), value: 'Prepare'},
        {text: this.$t('test_track.case.status_pass'), value: 'Pass'},
        {text: this.$t('test_track.case.status_un_pass'), value: 'UnPass'},
      ],
    };
  },
  props: {
    reviewId: {
      type: String
    }
  },
  watch: {
    reviewId() {
      this.initData();
    },
    selectNodeIds() {
      this.getReviews();
    },
    projectId() {
      this.getProjectNode();
    }
  },
  updated() {
    this.toggleSelection(this.testReviews);
  },
  methods: {
    openTestReviewRelevanceDialog() {
      this.getProject();
      this.initData();
      this.dialogFormVisible = true;
    },
    saveReviewRelevance() {
      let param = {};
      param.reviewId = this.reviewId;
      param.testCaseIds = [...this.selectIds];
      this.result = this.$post('/test/case/review/relevance', param, () => {
        this.selectIds.clear();
        this.$success(this.$t('commons.save_success'));
        this.dialogFormVisible = false;
        this.$emit('refresh');
      });
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

      if (this.projectId) {
        this.condition.projectId = this.projectId;
        this.result = this.$post('/test/case/reviews/case', this.condition, response => {
          this.testReviews = response.data;
          this.testReviews.forEach(item => {
            item.checked = false;
          });
        });
      }

    },
    handleSelectAll(selection) {
      if (selection.length > 0) {
        this.testReviews.forEach(item => {
          this.selectIds.add(item.id);
        });
      } else {
        // this.selectIds.clear();
        this.testReviews.forEach(item => {
          if (this.selectIds.has(item.id)) {
            this.selectIds.delete(item.id);
          }
        });
      }
    },
    handleSelectionChange(selection, row) {
      if (this.selectIds.has(row.id)) {
        this.selectIds.delete(row.id);
      } else {
        this.selectIds.add(row.id);
      }
    },
    nodeChange(nodeIds, nodeNames) {
      this.selectNodeIds = nodeIds;
      this.selectNodeNames = nodeNames;
    },
    initData() {
      this.getReviews();
      this.getAllNodeTreeByPlanId();
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
        this.result = this.$post("/case/node/list/all/review", param , response => {
          this.treeNodes = response.data;
        });
      }
    },
    close() {
      this.selectIds.clear();
      this.selectNodeIds = [];
      this.selectNodeNames = [];
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.initData();
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
        this.$post("/test/case/review/projects", {reviewId: this.reviewId},res => {
          let data = res.data;
          if (data) {
            this.projects = data;
            this.projectId = data[0].id;
            this.projectName = data[0].name;
          }
        })
      }
    },
    switchProject() {
      this.$refs.switchProject.open({id: this.reviewId, url : '/test/case/review/project/', type: 'review'});
    },
    getProjectNode(projectId) {
      const index = this.projects.findIndex(project => project.id === projectId);
      if (index !== -1) {
        this.projectName = this.projects[index].name;
      }
      if (projectId) {
        this.projectId = projectId;
      }
      this.result = this.$post("/case/node/list/all/review",
        {reviewId: this.reviewId, projectId: this.projectId} , response => {
          this.treeNodes = response.data;
        });

      this.selectNodeIds = [];
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
