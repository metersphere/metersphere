<template>
  <div>
    <case-side-dialog-content :tableCount="total" :label="$t('case.all_case')">
      <template v-slot:simpleSearch>
        <!-- 简单搜索框 -->
        <ms-new-ui-search :condition.sync="condition" @search="initTable" />
      </template>
      <template v-slot:versionSelect>
        <version-select
          v-xpack
          :project-id="projectId"
          @changeVersion="changeVersion"
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
          :condition="condition"
          :page-size="pageSize"
          :total="total"
          :showSelectAll="false"
          @selectCountChange="selectCountChange"
          @refresh="initTable"
          ref="table"
        >
          <ms-table-column prop="num" label="ID" width="100px" sortable="true">
          </ms-table-column>

          <ms-table-column prop="name" :label="$t('commons.name')" />

          <ms-table-column
            v-if="versionEnable"
            :label="$t('project.version.name')"
            :filters="versionFilters"
            min-width="100px"
            prop="versionId"
          >
            <template v-slot:default="scope">
              <span>{{ scope.row.versionName }}</span>
            </template>
          </ms-table-column>

          <ms-table-column
            prop="status"
            :label="$t('commons.status')"
            min-width="80"
          >
            <template v-slot:default="{ row }">
              <ms-performance-test-status :row="row" />
            </template>
          </ms-table-column>
          <ms-table-column
            prop="updateTime"
            :label="$t('commons.update_time')"
            min-width="150px"
          >
            <template v-slot:default="scope">
              <span>{{ scope.row.updateTime | datetimeFormat }}</span>
            </template>
          </ms-table-column>
          <ms-table-column
            prop="createTime"
            :label="$t('commons.create_time')"
            min-width="150px"
          >
            <template v-slot:default="scope">
              <span>{{ scope.row.createTime | datetimeFormat }}</span>
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
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsTableAdvSearchBar from "metersphere-frontend/src/components/search/MsTableAdvSearchBar";
import { TEST_CASE_RELEVANCE_LOAD_CASE } from "metersphere-frontend/src/components/search/search-components";
import MxVersionSelect from "metersphere-frontend/src/components/version/MxVersionSelect";
import { getVersionFilters } from "@/business/utils/sdk-utils";
import { getTestCaseRelevanceLoadList } from "@/api/testCase";

export default {
  name: "CaseRelateLoadList",
  components: {
    MsTablePagination,
    MsTable,
    MsTableColumn,
    MsTableAdvSearchBar,
    VersionSelect: MxVersionSelect,
    MsNewUiSearch,
    MsTableAdvSearch,
    HomePagination,
    CaseSideDialogContent,
  },
  data() {
    return {
      condition: {
        components: TEST_CASE_RELEVANCE_LOAD_CASE,
      },
      result: {},
      screenHeight: "100vh - 400px", //屏幕高度
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      versionFilters: [],
    };
  },
  props: {
    projectId: String,
    versionEnable: Boolean,
    notInIds: {
      type: Array,
      default: null,
    },
    testCaseId: String,
  },
  created: function () {
    this.initTable();
    this.getVersionOptions();
  },
  watch: {
    projectId() {
      this.condition.versionId = null;
      this.getVersionOptions();
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
      this.condition.status = "";
      if (projectId != null && typeof projectId === "string") {
        this.condition.projectId = projectId;
      } else if (this.projectId != null) {
        this.condition.projectId = this.projectId;
      }
      this.condition.notInIds = this.notInIds;
      this.condition.testCaseId = this.testCaseId;
      getTestCaseRelevanceLoadList(
        this.currentPage,
        this.pageSize,
        this.condition
      ).then((response) => {
        this.total = response.data.itemCount;
        this.tableData = response.data.listObject;
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
  },
};
</script>
