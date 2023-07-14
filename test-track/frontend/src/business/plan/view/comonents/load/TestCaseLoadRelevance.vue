<template>
  <test-case-relevance-base
    @setProject="setProject"
    @save="saveCaseRelevance"
    :plan-id="planId"
    ref="baseRelevance">

    <ms-table-adv-search-bar :condition.sync="condition" class="adv-search-bar"
                             v-if="condition.components !== undefined && condition.components.length > 0"
                             @search="search"/>

    <el-input :placeholder="$t('api_test.definition.request.select_case')" @blur="search"
              @keyup.enter.native="search" class="search-input" size="small" v-model="condition.name"/>

    <mx-version-select v-xpack :project-id="projectId" @changeVersion="changeVersion" style="float: left;"
                       class="search-input"/>

    <ms-table
      v-loading="loading"
      :data="testCases"
      :condition="condition"
      :page-size="pageSize"
      :total="total"
      :remember-order="true"
      row-key="id"
      :reserve-option="true"
      :page-refresh="pageRefresh"
      :row-order-group-id="projectId"
      @order="getTestCases"
      @filter="search"
      :disable-header-config="true"
      @selectCountChange="setSelectCounts"
      ref="table">

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
        v-if="versionEnable && versionFilters"
        prop="versionId"
        :column-key="'versionId'"
        :filters="versionFilters"
        :label="$t('commons.version')">
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
        :label="$t('commons.create_time')"
        min-width="120"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <span>{{ scope.row.createTime | datetimeFormat }}</span>
        </template>
      </el-table-column>
      <el-table-column
        sortable
        prop="updateTime"
        min-width="120"
        :label="$t('commons.update_time')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <span>{{ scope.row.updateTime | datetimeFormat }}</span>
        </template>
      </el-table-column>
    </ms-table>
    <ms-table-pagination :change="pageChange" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>

  </test-case-relevance-base>
</template>

<script>

import TestCaseRelevanceBase from "@/business/plan/view/comonents/base/TestCaseRelevanceBase";
import NodeTree from "metersphere-frontend/src/components/module/MsNodeTree";
import PriorityTableItem from "@/business/common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "@/business/common/tableItems/planview/TypeTableItem";
import MsTableSearchBar from "metersphere-frontend/src/components/MsTableSearchBar";
import MsTableAdvSearchBar from "metersphere-frontend/src/components/search/MsTableAdvSearchBar";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import {_filter, buildBatchParam} from "metersphere-frontend/src/utils/tableUtils";
import {TEST_PLAN_RELEVANCE_LOAD_CASE} from "metersphere-frontend/src/components/search/search-components";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import {getVersionFilters} from "@/business/utils/sdk-utils";
import MxVersionSelect from "metersphere-frontend/src/components/version/MxVersionSelect";
import MsPerformanceTestStatus from "@/business/performance/PerformanceTestStatus";
import {testPlanLoadRelevance, testPlanLoadRelevanceList} from "@/api/remote/plan/test-plan-load-case";
import {loadTestListBatch} from "@/api/remote/load/performance";

export default {
  name: "TestCaseLoadRelevance",
  components: {
    MsPerformanceTestStatus,
    MsTable,
    TestCaseRelevanceBase,
    NodeTree,
    PriorityTableItem,
    TypeTableItem,
    MsTableSearchBar,
    MsTableAdvSearchBar,
    MsTableHeader,
    MsTablePagination,
    MxVersionSelect
  },
  data() {
    return {
      loading: false,
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
      versionFilters: null,
      pageRefresh: false
    };
  },
  props: {
    planId: {
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
    projectId() {
      this.condition.versionId = null;
      this.getVersionOptions();
    }
  },
  mounted() {
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
      let selectRows = this.$refs.table.selectRows;
      if (selectRows.size < 1) {
        this.$warning(this.$t('test_track.plan_view.please_choose_test_case'));
        return;
      }
      let param = buildBatchParam(this, undefined, this.projectId);
      param.ids = Array.from(selectRows).map(row => row.id);
      if (this.planId) {
        this.loading = true;
        loadTestListBatch(param)
          .then((response) => {
            let tests = response.data;
            let condition = {
              caseIds: Array.from(tests).map(row => row.id),
              testPlanId: this.planId,
            };
            testPlanLoadRelevance(condition)
              .then(() => {
                this.loading = false;
                this.$success(this.$t("plan.relevance_case_success"));
                this.$emit('refresh');
                this.search();
              })
              .catch((e) => {
              });
          });
      }
    },
    search() {
      this.currentPage = 1;
      this.testCases = [];
      this.getTestCases(true);
    },
    pageChange() {
      this.getTestCases("page");
    },
    getTestCases(data) {
      this.pageRefresh = data === "page";
      if (this.planId) {
        this.condition.testPlanId = this.planId;
        this.condition.projectId = this.projectId;
        this.loading = true;
        testPlanLoadRelevanceList({pageNum: this.currentPage, pageSize: this.pageSize}, this.condition)
          .then(response => {
            this.loading = false;
            let data = response.data;
            this.total = data.itemCount;
            this.testCases = data.listObject;

            this.selectIds.clear();
          });
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
      getVersionFilters(this.projectId)
        .then(r => this.versionFilters = r.data);
    },
    changeVersion(currentVersion) {
      this.condition.versionId = currentVersion || null;
      this.search();
    },
    setSelectCounts(data) {
      this.$refs.baseRelevance.selectCounts = data;
    }
  }
};
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
