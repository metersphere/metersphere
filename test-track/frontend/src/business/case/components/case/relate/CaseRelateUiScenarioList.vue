<template>
  <div>
    <case-side-dialog-content
      :total-count="total"
      :label="$t('case.all_scenes')"
    >
      <template
        v-slot:simpleSearch
        v-if="
          condition.components !== undefined && condition.components.length > 0
        "
      >
        <!-- 简单搜索框 -->
        <ms-new-ui-search :condition.sync="condition" @search="initTable" />
      </template>
      <template v-slot:versionSelect>
        <version-select
          v-xpack
          :project-id="projectId"
          @changeVersion="changeVersion"
          class="version-select"
        />
      </template>
      <template
        v-slot:advSearch
        v-if="
          condition.components !== undefined && condition.components.length > 0
        "
      >
        <!-- 高级搜索框  -->
        <ms-table-adv-search :condition.sync="condition" @search="initTable" />
      </template>
      <template v-slot:tableRow>
        <ms-table
          v-loading="result.loading"
          :data="tableData"
          :select-node-ids="selectNodeIds"
          :condition="condition"
          :page-size="pageSize"
          :total="total"
          :showSelectAll="false"
          :screenHeight="screenHeight"
          :refresh-by-search.sync="refreshBySearch"
          @selectCountChange="selectCountChange"
          @refresh="initTable"
          ref="table"
        >
          <ms-table-column prop="num" label="ID" width="100px" sortable="true">
          </ms-table-column>

          <ms-table-column
            prop="name"
            :label="$t('api_test.automation.scenario_name')"
          />

          <ms-table-column
            prop="level"
            sortable
            min-width="130px"
            :label="$t('api_test.automation.case_level')"
          >
            <template v-slot:default="scope">
              <priority-table-item :value="scope.row.level" />
            </template>
          </ms-table-column>

          <ms-table-column
            prop="status"
            :label="$t('test_track.plan.plan_status')"
            sortable
            min-width="120px"
          >
            <template v-slot:default="scope">
              <status-table-item :value="scope.row.status" />
            </template>
          </ms-table-column>

          <ms-table-column prop="tags" :show-overflow-tooltip="false" min-width="180" :label="$t('commons.tag')">
            <template v-slot:default="scope">
              <el-tooltip class="item" effect="dark" placement="top">
                <div v-html="getTagToolTips(scope.row.tags)" slot="content"></div>
                <div class="oneLine">
                  <ms-single-tag
                    v-for="(itemName, index) in parseColumnTag(scope.row.tags)"
                    :key="index"
                    type="success"
                    effect="plain"
                    :show-tooltip="scope.row.tags.length === 1 && itemName.length * 12 <= 100"
                    :content="itemName"
                    style="margin-left: 0; margin-right: 2px"/>
                </div>
              </el-tooltip>
              <span/>
            </template>
          </ms-table-column>
        </ms-table>
      </template>
      <template v-slot:pagination>
        <home-pagination
          :change="initTable"
          :current-page.sync="currentPage"
          :page-size.sync="pageSize"
          :total="total"
          layout="total, prev, pager, next, sizes, jumper"
        />
      </template>
    </case-side-dialog-content>
  </div>
</template>

<script>
import CaseSideDialogContent from "../../common/CaseSideDialogContent";
import MsNewUiSearch from "metersphere-frontend/src/components/new-ui/MsSearch";
import MsTableAdvSearch from "metersphere-frontend/src/components/new-ui/MsTableAdvSearch";
import HomePagination from "@/business/home/components/pagination/HomePagination";
import MsTable from "metersphere-frontend/src/components/new-ui/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import PriorityTableItem from "@/business/common/tableItems/planview/PriorityTableItem";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import StatusTableItem from "@/business/common/tableItems/planview/StatusTableItem";
import MsTableAdvSearchBar from "metersphere-frontend/src/components/search/MsTableAdvSearchBar";
import MsSingleTag from "metersphere-frontend/src/components/new-ui/MsSingleTag";
import { TEST_CASE_RELEVANCE_API_CASE_CONFIGS } from "metersphere-frontend/src/components/search/search-components";
import { getVersionFilters } from "@/business/utils/sdk-utils";
import MxVersionSelect from "metersphere-frontend/src/components/version/MxVersionSelect";
import { getTestCaseRelevanceUiScenarioList } from "@/api/testCase";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {getTagToolTips, parseColumnTag} from "@/business/case/test-case";

export default {
  name: "CaseRelateUiScenarioList",
  components: {
    StatusTableItem,
    MsTablePagination,
    PriorityTableItem,
    MsTable,
    MsTableColumn,
    MsTableAdvSearchBar,
    MsSingleTag,
    VersionSelect: MxVersionSelect,
    MsNewUiSearch,
    MsTableAdvSearch,
    HomePagination,
    CaseSideDialogContent,
  },
  data() {
    return {
      condition: {
        components: TEST_CASE_RELEVANCE_API_CASE_CONFIGS,
      },
      result: {},
      priorityFilters: [
        { text: "P0", value: "P0" },
        { text: "P1", value: "P1" },
        { text: "P2", value: "P2" },
        { text: "P3", value: "P3" },
      ],
      screenHeight: "calc(60vh - 15px)", //屏幕高度
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      versionFilters: [],
      refreshBySearch: false
    };
  },
  props: {
    selectNodeIds: Array,
    projectId: String,
    versionEnable: Boolean,
    notInIds: {
      type: Array,
      default: null,
    },
    testCaseId: String,
  },
  created: function () {
    this.$emit('setCondition', this.condition);
    this.getVersionOptions();
    this.initTable();
  },
  watch: {
    selectNodeIds() {
      this.initTable();
    },
    projectId() {
      this.condition.versionId = null;
      this.initTable();
    },
  },
  computed: {
    selectRows() {
      if (this.$refs.table) {
        return this.$refs.table.getSelectRows();
      } else {
        return new Set();
      }
    },
  },
  methods: {
    selectCountChange(data) {
      this.$emit("selectCountChange", data);
    },
    initTable(projectId) {
      this.refreshBySearch = true;
      this.condition.status = "";
      this.condition.moduleIds = this.selectNodeIds;
      if (projectId != null && typeof projectId === "string" && projectId !== "") {
        this.condition.projectId = projectId;
      } else if (this.projectId != null && this.projectId !== "") {
        this.condition.projectId = this.projectId;
      } else {
        this.condition.projectId = getCurrentProjectID();
      }

      this.condition.notInIds = this.notInIds;
      this.condition.testCaseId = this.testCaseId;
      this.$emit('setCondition', this.condition);
      getTestCaseRelevanceUiScenarioList(
        this.currentPage,
        this.pageSize,
        this.condition
      ).then((response) => {
        this.total = response.data.itemCount;
        this.tableData = response.data.listObject;
        this.tableData.forEach((item) => {
          if (item.tags && item.tags.length > 0) {
            item.tags = JSON.parse(item.tags);
          }
        });
      });
    },
    clear() {
      if (this.$refs.table) {
        this.$refs.table.clear();
      }
    },
    buildPagePath(path) {
      return path + this.currentPage + "/" + this.pageSize;
    },
    getSelectIds() {
      return this.$refs.table.selectIds;
    },
    clearSelection() {
      if (this.$refs.table) {
        this.$refs.table.clearSelectRows();
      }
    },
    getVersionOptions() {
      getVersionFilters(this.projectId).then(
        (r) => (this.versionFilters = r.data)
      );
    },
    changeVersion(currentVersion) {
      this.condition.versionId = currentVersion || null;
      this.initTable();
    },
    getTagToolTips(tags) {
      return getTagToolTips(tags);
    },
    parseColumnTag(tags) {
      return parseColumnTag(tags);
    }
  },
};
</script>

<style scoped>
.oneLine {
  overflow: hidden;
  white-space: nowrap;
}
</style>
