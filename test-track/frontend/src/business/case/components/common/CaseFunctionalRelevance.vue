<template>
  <test-case-relevance-base
    @clearSelect="clearSelect"
    @setProject="setProject"
    @save="saveCaseRelevance"
    :flag="isTestPlan"
    :multiple-project="multipleProject"
    :is-saving="isSaving"
    ref="baseRelevance"
  >
    <template v-slot:aside>
      <TestCaseNodeTree
        :showTrashBtn="false"
        :show-public-btn="false"
        :hide-node-operator="true"
        class="node-tree"
        :case-condition="page.condition"
        :scroll="true"
        v-loading="nodeResult.loading"
        local-suffix="test_case"
        default-label="未规划用例"
        @nodeSelectEvent="nodeChange"
        :tree-nodes="treeNodes"
        ref="nodeTree"
      ></TestCaseNodeTree>
      <!-- <node-tree
        class="node-tree"
        :scroll="true"
        v-loading="nodeResult.loading"
        local-suffix="test_case"
        default-label="未规划用例"
        @nodeSelectEvent="nodeChange"
        :tree-nodes="treeNodes"
        ref="nodeTree"
      /> -->
    </template>

    <!-- 表格头部 搜索栏 -->
    <div class="side-content-box">
      <div class="search-wrap">
        <div class="search-lable-row">
          <div class="label">{{ $t("case.all_case") }} (</div>
          <div class="total-count">{{ page.total }})</div>
        </div>
        <div class="search-opt-row">
          <div class="simple-search-row">
            <!-- 简单搜索框 -->
            <ms-new-ui-search
              :condition.sync="page.condition"
              @search="search"
            />
          </div>

          <div class="version-select-row" v-xpack>
            <!-- 版本切换组件 -->
            <version-select
              :project-id="projectId"
              @changeVersion="changeVersion"
            />
          </div>

          <div class="adv-search-row">
            <!-- 高级搜索框  -->
            <ms-table-adv-search
              :condition.sync="page.condition"
              @search="search"
              ref="advanceSearch"
            />
          </div>
        </div>
      </div>
      <div class="table-data-wrap">
        <div class="table-data-row">
          <ms-table
            v-loading="page.loading"
            :data="page.data"
            :condition="page.condition"
            :total="page.total"
            :page-size.sync="page.pageSize"
            :screen-height="screenHeight"
            @handlePageChange="getTestCases"
            @selectCountChange="setSelectCounts"
            @order="getTestCases"
            @filter="search"
            ref="table"
          >
            <ms-table-column
              v-if="!customNum"
              prop="num"
              sortable
              :label="$t('commons.id')"
            >
            </ms-table-column>
            <ms-table-column
              v-if="customNum"
              prop="customNum"
              sortable
              :label="$t('commons.id')"
            >
            </ms-table-column>

            <ms-table-column prop="name" :label="$t('commons.name')" />

            <ms-table-column
              v-if="versionEnable && versionFilters"
              prop="versionId"
              :filters="versionFilters"
              :label="$t('commons.version')"
              show-overflow-tooltip
            >
              <template v-slot:default="scope">
                <span>{{ scope.row.versionName }}</span>
              </template>
            </ms-table-column>

            <ms-table-column
              prop="priority"
              :filters="priorityFilters"
              sortable
              :label="$t('test_track.case.priority')"
              width="120px"
            >
              <template v-slot:default="scope">
                <priority-table-item :value="scope.row.priority" />
              </template>
            </ms-table-column>

            <test-plan-case-status-table-item
              sortable
              prop="lastExecuteResult"
            />

            <test-case-review-status-table-item sortable />

            <ms-table-column
              prop="tags"
              :label="$t('commons.tag')"
              width="90px"
            >
              <template v-slot:default="scope">
                <ms-tag
                  v-for="(itemName, index) in scope.row.tags"
                  :key="index"
                  type="success"
                  effect="plain"
                  :content="itemName"
                  style="margin-left: 0px; margin-right: 2px"
                />
                <span />
              </template>
            </ms-table-column>

            <ms-update-time-column />
            <ms-create-time-column />
          </ms-table>
        </div>
        <!-- <ms-table-pagination
      :change="getTestCases"
      :current-page.sync="page.currentPage"
      :page-size.sync="page.pageSize"
      :total="page.total"
    /> -->

        <div class="pagenation-wrap">
          <home-pagination
            v-if="page.data.length > 0"
            :change="getTestCases"
            :current-page.sync="page.currentPage"
            :page-size.sync="page.pageSize"
            :total="page.total"
            layout="total, prev, pager, next, sizes, jumper"
          />
        </div>
      </div>
    </div>
  </test-case-relevance-base>
</template>

<script>
import MsNewUiSearch from "metersphere-frontend/src/components/new-ui/MsSearch";
import MsTableAdvSearch from "metersphere-frontend/src/components/new-ui/MsTableAdvSearch";
import HomePagination from "@/business/home/components/pagination/HomePagination";
import TestCaseNodeTree from "@/business/module/TestCaseNodeTree";
import NodeTree from "metersphere-frontend/src/components/module/MsNodeTree";
import PriorityTableItem from "@/business/common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "@/business/common/tableItems/planview/TypeTableItem";
import MsTableSearchBar from "metersphere-frontend/src/components/MsTableSearchBar";
import MsTableAdvSearchBar from "metersphere-frontend/src/components/search/MsTableAdvSearchBar";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import TestCaseRelevanceBase from "./CaseRelevanceSideDialog";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsTable from "metersphere-frontend/src/components/new-ui/MsTable";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsTag from "metersphere-frontend/src/components/MsTag";
import MsCreateTimeColumn from "metersphere-frontend/src/components/table/MsCreateTimeColumn";
import MsUpdateTimeColumn from "metersphere-frontend/src/components/table/MsUpdateTimeColumn";
import StatusTableItem from "@/business/common/tableItems/planview/StatusTableItem";
import ReviewStatus from "@/business/case/components/ReviewStatus";
import TestCaseReviewStatusTableItem from "@/business/common/tableItems/TestCaseReviewStatusTableItem";
import TestPlanCaseStatusTableItem from "@/business/common/tableItems/TestPlanCaseStatusTableItem";
import { TEST_CASE_CONFIGS } from "metersphere-frontend/src/components/search/search-components";
import MxVersionSelect from "metersphere-frontend/src/components/version/MxVersionSelect";
import { getProjectApplicationConfig } from "@/api/project-application";
import { getVersionFilters } from "@/business/utils/sdk-utils";
import { getTestTemplate } from "@/api/custom-field-template";
import { initTestCaseConditionComponents } from "@/business/case/test-case";

export default {
  name: "CaseFunctionalRelevance",
  components: {
    TestPlanCaseStatusTableItem,
    TestCaseReviewStatusTableItem,
    ReviewStatus,
    StatusTableItem,
    MsUpdateTimeColumn,
    MsCreateTimeColumn,
    MsTag,
    MsTablePagination,
    MsTable,
    MsTableColumn,
    TestCaseRelevanceBase,
    NodeTree,
    PriorityTableItem,
    TypeTableItem,
    MsTableSearchBar,
    MsTableAdvSearchBar,
    MsTableHeader,
    VersionSelect: MxVersionSelect,
    TestCaseNodeTree,
    HomePagination,
    MsTableAdvSearch,
    MsNewUiSearch,
  },
  mounted() {
    this.getVersionOptions();
  },
  data() {
    return {
      nodeResult: {},
      isSaving: false,
      loading: false,
      treeNodes: [],
      selectNodeIds: [],
      selectNodeNames: [],
      projectId: "",
      projectName: "",
      projects: [],
      customNum: false,
      screenHeight: "calc(100vh - 185px)",
      priorityFilters: [
        { text: "P0", value: "P0" },
        { text: "P1", value: "P1" },
        { text: "P2", value: "P2" },
        { text: "P3", value: "P3" },
      ],
      versionFilters: null,
      testCaseTemplate: {},
      selectCounts: 0,
    };
  },
  props: {
    page: {
      type: Object,
    },
    isTestPlan: {
      type: Boolean,
    },
    getTableData: {
      type: Function,
    },
    getNodeTree: {
      type: Function,
    },
    save: {
      type: Function,
    },
    multipleProject: {
      type: Boolean,
      default: true,
    },
    versionEnable: {
      type: Boolean,
      default: false,
    },
  },
  watch: {
    selectNodeIds() {
      this.getTestCases();
    },
    projectId(val) {
      this.pushCustomFieldToCondition(val);
      this.setConditionModuleIdParam();
      this.page.condition.projectId = this.projectId;
      this.page.condition.versionId = null;
      this.getProjectNode();
      this.getTestCases();
      this.getCustomNum();
      this.getVersionOptions();
    },
  },
  methods: {
    clearSelect() {
      this.$refs.table.clearSelection();
    },
    open() {
      this.page.condition = { custom: false, components: TEST_CASE_CONFIGS };
      this.isSaving = false;
      this.$refs.baseRelevance.open();
      if (this.$refs.table) {
        this.$refs.table.clear();
      }
      if (this.projectId) {
        this.getProjectNode(this.projectId);
      }
    },
    setProject(projectId) {
      this.projectId = projectId;
    },
    setConditionModuleIdParam() {
      this.page.condition.components.forEach((component) => {
        if (component.key === "moduleIds") {
          component.options.params = { projectId: this.projectId };
        }
      });
    },
    getCustomNum() {
      getProjectApplicationConfig("CASE_CUSTOM_NUM").then((result) => {
        let data = result.data;
        if (data && data.typeValue === "true") {
          this.customNum = true;
        } else {
          this.customNum = false;
        }
      });
    },
    search() {
      // 添加搜索条件时，当前页设置成第一页
      this.page.currentPage = 1;
      this.getTestCases();
      this.getProjectNode(this.projectId, this.page.condition);
    },
    getTestCases() {
      let condition = this.page.condition;
      if (this.selectNodeIds && this.selectNodeIds.length > 0) {
        condition.nodeIds = this.selectNodeIds;
      } else {
        condition.nodeIds = [];
      }
      condition.projectId = this.projectId;
      if (this.projectId) {
        this.getTableData();
      }
    },
    saveCaseRelevance(item) {
      this.isSaving = true;
      let param = {};
      param.ids = this.$refs.table.selectIds;
      param.request = this.page.condition;
      param.checked = item;
      this.save(param, this);
    },
    nodeChange(node, nodeIds, nodeNames) {
      this.page.condition.selectAll = false;
      this.$refs.table.condition.selectAll = false;
      this.selectNodeIds = nodeIds;
      this.selectNodeNames = nodeNames;
    },
    close() {
      this.selectNodeIds = [];
      this.selectNodeNames = [];
      this.$refs.table.clear();
    },
    getProjectNode(projectId, condition) {
      const index = this.projects.findIndex(
        (project) => project.id === projectId
      );
      if (index !== -1) {
        this.projectName = this.projects[index].name;
      }
      if (projectId) {
        this.projectId = projectId;
      }
      this.getNodeTree(this, condition);
    },
    getVersionOptions() {
      getVersionFilters(this.projectId).then(
        (r) => (this.versionFilters = r.data)
      );
    },
    changeVersion(currentVersion) {
      this.page.condition.versionId = currentVersion || null;
      this.getTestCases();
      this.getProjectNode(this.projectId, this.page.condition);
    },
    setSelectCounts(data) {
      this.$refs.baseRelevance.selectCounts = data;
    },
    pushCustomFieldToCondition(projectId) {
      getTestTemplate(projectId).then((data) => {
        this.testCaseTemplate = data;
        this.page.condition.components = initTestCaseConditionComponents(
          this.page.condition,
          this.testCaseTemplate.customFields
        );
      });
    },
  },
};
</script>

<style scoped lang="scss">
@import "@/business/style/index.scss";
.side-content-box {
  height: px2rem(72);
  padding-left: px2rem(24);
  padding-right: px2rem(24);
  .search-wrap {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 100%;
    .search-lable-row {
      display: flex;
      height: 26px;
      font-weight: 500;
      font-size: 18px;
      line-height: 26px;
    }
    .search-opt-row {
      display: flex;
      height: 32px;
      align-items: center;
      justify-content: flex-end;
      .simple-search-row {
        width: 200px;
        margin-right: 12px;
      }
      .version-select-row {
        width: 140px;
        margin-right: 12px;
        height: 32px;
      }
      .adv-search-row {
        width: 32px;
        height: 32px;
        :deep(button.el-button.el-button--default.el-button--mini) {
          box-sizing: border-box;
          width: 32px;
          height: 32px;
          background: #ffffff;
          border: 1px solid #bbbfc4;
          border-radius: 4px;
          flex: none;
          order: 5;
          align-self: center;
          flex-grow: 0;
        }
      }
    }
  }
  .table-data-wrap {
    display: block;
    flex-direction: column;
    justify-content: space-between;
    .table-data-row {
      height: px2rem(430);
      :deep(.test-content) {
        overflow: scroll;
      }
      :deep(.el-table__body-wrapper) {
        height: auto!important;
      }
    }
    .pagenation-wrap {
      height: px2rem(68);
      :deep(.home-pagination) {
        margin-top: 6px;
      }
    }
  }
}
</style>
