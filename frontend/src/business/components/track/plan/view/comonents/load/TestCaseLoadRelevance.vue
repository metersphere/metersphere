<template>
  <test-case-relevance-base
    @setProject="setProject"
    @save="saveCaseRelevance"
    :plan-id="planId"
    ref="baseRelevance">

    <template v-slot:aside>
      <node-tree class="node-tree"
                 v-loading="result.loading"
                 @nodeSelectEvent="nodeChange"
                 :tree-nodes="treeNodes"
                 ref="nodeTree"/>
    </template>

    <el-card>
      <el-input :placeholder="$t('api_test.definition.request.select_case')" @blur="getTestCases"
                @keyup.enter.native="getTestCases" class="search-input" size="small" v-model="condition.name"/>
      <ms-table-adv-search-bar :condition.sync="condition" class="adv-search-bar"
                               v-if="condition.components !== undefined && condition.components.length > 0"
                               @search="getTestCases"/>
      <version-select v-xpack :project-id="projectId" @changeVersion="changeVersion" margin-right="20" class="search-input"/>

      <el-table
        v-loading="result.loading"
        :data="testCases"
        row-key="id"
        @select-all="handleSelectAll"
        @select="handleSelectionChange"
        @filter-change="filter"
        height="50vh"
        ref="table">
        <el-table-column
          type="selection"/>
        <el-table-column
          prop="num"
          label="ID"
          width="100px"
          sortable>
        </el-table-column>
        <el-table-column
          prop="name"
          :label="$t('commons.name')"
          show-overflow-tooltip>
        </el-table-column>

        <el-table-column
          v-if="versionEnable"
          prop="versionId"
          :column-key="'versionId'"
          :filters="versionFilters"
          :label="$t('commons.version')"
          min-width="120px">
          <template v-slot:default="scope">
            <span>{{ scope.row.versionName }}</span>
          </template>
        </el-table-column>

        <el-table-column
          prop="status"
          column-key="status"
          :filters="statusFilters"
          :label="$t('commons.status')">
          <template v-slot:default="{row}">
            <ms-performance-test-status :row="row"/>
          </template>
        </el-table-column>
        <el-table-column
          sortable
          prop="createTime"
          :label="$t('commons.create_time')">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column
          sortable
          prop="updateTime"
          :label="$t('commons.update_time')">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
      </el-table>

    </el-card>
    <ms-table-pagination :change="getTestCases" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>
  </test-case-relevance-base>
</template>

<script>

import TestCaseRelevanceBase from "@/business/components/track/plan/view/comonents/base/TestCaseRelevanceBase";
import NodeTree from "@/business/components/track/common/NodeTree";
import PriorityTableItem from "@/business/components/track/common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "@/business/components/track/common/tableItems/planview/TypeTableItem";
import MsTableSearchBar from "@/business/components/common/components/MsTableSearchBar";
import MsTableAdvSearchBar from "@/business/components/common/components/search/MsTableAdvSearchBar";
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import MsPerformanceTestStatus from "@/business/components/performance/test/PerformanceTestStatus";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import {_filter} from "@/common/js/tableUtils";
import {TEST_PLAN_RELEVANCE_LOAD_CASE} from "@/business/components/common/components/search/search-components";
import {hasLicense, getCurrentProjectID} from "@/common/js/utils";
const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const VersionSelect = requireComponent.keys().length > 0 ? requireComponent("./version/VersionSelect.vue") : {};

export default {
  name: "TestCaseLoadRelevance",
  components: {
    TestCaseRelevanceBase,
    NodeTree,
    PriorityTableItem,
    TypeTableItem,
    MsTableSearchBar,
    MsTableAdvSearchBar,
    MsTableHeader,
    MsPerformanceTestStatus,
    MsTablePagination,
    'VersionSelect': VersionSelect.default,
  },
  data() {
    return {
      result: {},
      testCases: [],
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
      condition: {
        components: TEST_PLAN_RELEVANCE_LOAD_CASE
      },
      statusFilters: [
        {text: 'Saved', value: 'Saved'},
        {text: 'Starting', value: 'Starting'},
        {text: 'Running', value: 'Running'},
        {text: 'Reporting', value: 'Reporting'},
        {text: 'Completed', value: 'Completed'},
        {text: 'Error', value: 'Error'}
      ],
      versionFilters: [],
    };
  },
  props: {
    planId: {
      type: String
    },
    reviewId: {
      type: String
    },
    versionEnable: {
      type: Boolean
    },
  },
  watch: {
    planId() {
      this.condition.planId = this.planId;
    },
    reviewId() {
      this.condition.reviewId = this.reviewId;
    },
  },
  mounted(){
    this.getVersionOptions();
  },
  methods: {
    filter(filters) {
      _filter(filters, this.condition);
      this.search();
    },
    open() {
      this.$refs.baseRelevance.open();
    },
    setProject(projectId) {
      this.projectId = projectId;
      this.condition.projectId = this.projectId;
      this.getProjectNode();
      this.search();
    },
    saveCaseRelevance() {
      let param = {};
      param.caseIds = [...this.selectIds];
      if (this.planId) {
        param.testPlanId = this.planId;
        this.result = this.$post('/test/plan/load/case/relevance', param, () => {
          this.selectIds.clear();
          this.$success(this.$t('commons.save_success'));

          this.$refs.baseRelevance.close();

          this.$emit('refresh');
        });
      }
      if (this.reviewId) {
        param.testCaseReviewId = this.reviewId;
        this.result = this.$post('/test/review/load/case/relevance', param, () => {
          this.selectIds.clear();
          this.$success(this.$t('commons.save_success'));

          this.$refs.baseRelevance.close();

          this.$emit('refresh');
        });
      }

    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    search() {
      this.currentPage = 1;
      this.testCases = [];
      this.getTestCases(true);
    },
    getTestCases() {
      if (this.planId) {
        this.condition.testPlanId = this.planId;
        this.condition.projectId = this.projectId;
        this.result = this.$post(this.buildPagePath('/test/plan/load/case/relevance/list'), this.condition, response => {
          let data = response.data;
          this.total = data.itemCount;
          this.testCases = data.listObject;

          this.selectIds.clear();
          if (this.$refs.table) {
            this.$refs.table.clearSelection();
          }
        });
      }
      if (this.reviewId) {
        this.condition.testCaseReviewId = this.reviewId;
        if (this.projectId) {
          this.condition.projectId = this.projectId;
          this.result = this.$post(this.buildPagePath('/test/review/load/case/relevance/list'), this.condition, response => {
            let data = response.data;
            this.total = data.itemCount;
            this.testCases = data.listObject;
          });
        }
      }
    },
    handleSelectAll(selection) {
      if (selection.length > 0) {
        this.testCases.forEach(item => {
          this.selectIds.add(item.id);
        });
      } else {
        this.testCases.forEach(item => {
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
    nodeChange(node, nodeIds, nodeNames) {
      this.selectNodeIds = nodeIds;
      this.selectNodeNames = nodeNames;
      this.search();
    },
    refresh() {
      this.close();
    },
    getAllNodeTreeByPlanId() {
      if (this.planId) {
        let param = {
          testPlanId: this.planId,
          projectId: this.projectId
        };
        this.result = this.$post("/case/node/list/all/plan", param, response => {
          this.treeNodes = response.data;
        });
      }
    },
    close() {
      this.selectIds.clear();
      this.selectNodeIds = [];
      this.selectNodeNames = [];
    },
    getProjectNode(projectId) {
      const index = this.projects.findIndex(project => project.id === projectId);
      if (index !== -1) {
        this.projectName = this.projects[index].name;
      }
      if (projectId) {
        this.projectId = projectId;
      }
      this.treeNodes = [];
      this.selectNodeIds = [];
    },
    getVersionOptions() {
      if (hasLicense()) {
        this.$get('/project/version/get-project-versions/' + getCurrentProjectID(), response => {
          this.versionOptions = response.data;
          this.versionFilters = response.data.map(u => {
            return {text: u.name, value: u.id};
          });
        });
      }
    },
    changeVersion(currentVersion){
      this.condition.versionId = currentVersion || null;
      this.search();
    }
  }
}
</script>

<style scoped>
.search-input {
  float: right;
  width: 250px;
  margin-top: 10px;
  margin-bottom: 20px;
  margin-right: 20px;
}

.adv-search-bar {
  float: right;
  margin-top: 15px;
  margin-right: 10px;
}
</style>
