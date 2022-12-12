<template>
  <ms-drawer-component
    :title="$t('关联现有缺陷')"
    @confirm="save"
    @clearSelect="clearSelection"
    ref="relevanceDialog"
  >
    <div slot="header" v-if="page.data.length > 0">
      <div class="header-search-row">
        <div class="simple-row">
          <ms-new-ui-search
            :condition.sync="page.condition"
            @search="getIssues"
            :base-search-tip="$t('commons.search_by_name_or_id')"
          />
        </div>
        <div class="adv-row">
          <!-- 高级搜索框  -->
          <ms-table-adv-search
            :condition.sync="page.condition"
            @search="getIssues"
            ref="advanceSearch"
          />
        </div>
      </div>
    </div>
    <div slot="content">
      <div v-if="page.data.length === 0" class="none-data-tip">
        <img src="/assets/module/figma/icon_none.svg" style="height: 100px;width: 100px;margin-bottom: 8px"/>
        <span class="none-data-content">{{$t('test_track.issue.list_none_tips')}}</span>
      </div>
      <ms-table
        v-if="page.data.length > 0"
        :screen-height="screenHeight"
        v-loading="page.result.status"
        :data="page.data"
        :condition="page.condition"
        :total="page.total"
        :page-size.sync="page.pageSize"
        :show-select-all="false"
        @handlePageChange="getIssues"
        @selectCountChange="setSelectCounts"
        @refresh="getIssues"
        class="relate-issue-table"
        ref="table"
      >
        <ms-table-column
          :label="$t('test_track.issue.id')"
          prop="id"
          v-if="false"
        >
        </ms-table-column>
        <ms-table-column :label="$t('test_track.issue.id')" prop="num">
        </ms-table-column>

        <ms-table-column
          :label="$t('test_track.issue.title')"
          prop="title"
          min-width="200px"
        >
        </ms-table-column>

        <ms-table-column
          :label="$t('test_track.issue.platform_status')"
          v-if="isThirdPart"
          prop="platformStatus"
        >
          <template v-slot="scope">
            {{ scope.row.platformStatus ? scope.row.platformStatus : "--" }}
          </template>
        </ms-table-column>

        <ms-table-column
          v-else
          :label="$t('test_track.issue.status')"
          prop="status"
        >
          <template v-slot="scope">
            <span>{{
              issueStatusMap[scope.row.status]
                ? issueStatusMap[scope.row.status]
                : scope.row.status
            }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
          :label="$t('test_track.issue.platform')"
          prop="platform"
        >
        </ms-table-column>

        <issue-description-table-item />
      </ms-table>

      <!-- <ms-table-pagination
        :change="getIssues"
        :current-page.sync="page.currentPage"
        :page-size.sync="page.pageSize"
        :total="page.total"
      /> -->
    </div>
    <div slot="pagination" v-if="page.data.length > 0">
      <home-pagination
        :change="getIssues"
        :current-page.sync="page.currentPage"
        :page-size.sync="page.pageSize"
        :total="page.total"
        layout="total, prev, pager, next, sizes, jumper"
      />
    </div>
  </ms-drawer-component>
  <!-- <ms-edit-dialog
    :append-to-body="true"
    :visible.sync="visible"
    :title="$t('test_track.case.relate_issue')"
    @confirm="save"
    ref="relevanceDialog"
  >
  </ms-edit-dialog> -->
</template>
<style scoped>
.search-input {
  float: right;
  width: 300px;
}
</style>
<script>
import MsNewUiSearch from "metersphere-frontend/src/components/new-ui/MsSearch";
import MsEditDialog from "metersphere-frontend/src/components/MsEditDialog";
import HomePagination from "@/business/home/components/pagination/HomePagination";
import MsTable from "metersphere-frontend/src/components/new-ui/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import {
  getRelateIssues,
  isThirdPartEnable,
  testCaseIssueRelate,
} from "@/api/issue";
import IssueDescriptionTableItem from "@/business/issue/IssueDescriptionTableItem";
import { ISSUE_STATUS_MAP } from "metersphere-frontend/src/utils/table-constants";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import { getPageInfo } from "metersphere-frontend/src/utils/tableUtils";
import { getCurrentProjectID } from "metersphere-frontend/src/utils/token";
import { TEST_CASE_RELEVANCE_ISSUE_LIST } from "@/business/utils/sdk-utils";
import MsSearch from "metersphere-frontend/src/components/search/MsSearch";
import MsDrawerComponent from "../common/MsDrawerComponent";
import MsTableAdvSearch from "metersphere-frontend/src/components/new-ui/MsTableAdvSearch";

export default {
  name: "CaseIssueRelateList",
  components: {
    MsTablePagination,
    IssueDescriptionTableItem,
    MsTableColumn,
    MsTable,
    MsEditDialog,
    MsSearch,
    MsDrawerComponent,
    HomePagination,
    MsTableAdvSearch,
    MsNewUiSearch,
  },
  data() {
    return {
      page: getPageInfo({
        components: TEST_CASE_RELEVANCE_ISSUE_LIST,
      }),
      visible: false,
      isThirdPart: false,
      selectCounts: null,
      screenHeight: 'calc(100vh - 185px)',
    };
  },
  computed: {
    issueStatusMap() {
      return ISSUE_STATUS_MAP;
    },
    projectId() {
      return getCurrentProjectID();
    },
  },
  props: {
    caseId: String,
    planCaseId: String,
    notInIds: Array,
  },
  created() {
    isThirdPartEnable((data) => {
      this.isThirdPart = data;
    });
  },
  methods: {
    clearSelection() {
      if (this.$refs.table) {
        this.$refs.table.clearSelectRows();
      }
    },
    setSelectCounts(data) {
      this.$refs.relevanceDialog.selectCounts = data;
    },
    open() {
      this.getIssues();
      this.visible = true;
      this.$refs.relevanceDialog.open();
    },
    getIssues() {
      this.page.condition.projectId = this.projectId;
      this.page.condition.notInIds = this.notInIds;
      getRelateIssues(this.page, this.page.result);
    },
    getCaseResourceId() {
      return this.planCaseId ? this.planCaseId : this.caseId;
    },
    save() {
      let param = {};
      param.issueIds = Array.from(this.$refs.table.selectRows).map((i) => i.id);
      param.caseResourceId = this.getCaseResourceId();
      param.isPlanEdit = !!this.planCaseId;
      param.refId = this.planCaseId ? this.caseId : null;
      testCaseIssueRelate(param).then(() => {
        this.visible = false;
        this.$refs.relevanceDialog.close();
        this.$emit("refresh", this.$refs.table.selectRows);
        this.$success(this.$t('commons.relate_success'), false)
      });
    },
  },
};
</script>

<style scoped lang="scss">
@import "@/business/style/index.scss";
.header-search-row {
  display: flex;
  margin: 0 px2rem(24);
  .simple-row {
    width: px2rem(1200);
    margin-right: px2rem(12);
    :deep(.el-input--small) {
      width: 100% !important;
    }
  }
  .simple-row > div {
    width: 100%;
  }
  .adv-row {
    width: px2rem(52);
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

.relate-issue-table {
  width: 95%;
  margin-left: px2rem(24);
}

.none-data-tip {
  text-align: center;
  margin-top: px2rem(150);
  .none-data-content {
    display: block;
    font-weight: 400;
    color: #646A73;
  }
}
</style>
