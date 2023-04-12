<template>
  <ms-drawer-component
    :title="$t('关联现有缺陷')"
    @confirm="save"
    @clearSelect="clearSelection"
    ref="relevanceDialog"
  >
    <div slot="header">
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
      <ms-table
        :screen-height="screenHeight"
        v-loading="page.loading"
        :data="page.data"
        :condition="page.condition"
        :total="page.total"
        :page-size.sync="page.pageSize"
        :show-select-all="false"
        @handlePageChange="getIssues"
        @selectCountChange="setSelectCounts"
        :max-height="maxHeight"
        @refresh="getIssues"
        class="relate-issue-table"
        ref="table"
      >
        <span v-for="item in fields" :key="item.key">
          <ms-table-column
            :field="item"
            :label="$t('test_track.issue.id')"
            prop="id"
            v-if="false"
          >
          </ms-table-column>
          <ms-table-column
            :field="item"
            :label="'ID'"
            prop="num"
            :sortable="true">
          </ms-table-column>

          <ms-table-column
            :field="item"
            :label="$t('test_track.issue.title')"
            prop="title"
            :sortable="true"
          >
          </ms-table-column>

          <!-- 自定义状态字段  -->
          <span v-for="field in issueTemplate.customFields" :key="field.id">
            <ms-table-column
              :field="item"
              :label="field.name"
              :prop="field.name"
              v-if="field.name === '状态'"
              :sortable="true"
              type="select"
              :filters="item.filters"
              :column-key="item.columnKey"
            >
              <template v-slot="scope">
                <span>
                    {{ getCustomFieldValue(scope.row, field) ? getCustomFieldValue(scope.row, field) : issueStatusMap[scope.row.status]}}
                </span>
              </template>
            </ms-table-column>
          </span>

          <ms-table-column
            :field="item"
            :label="$t('test_track.issue.platform_status')"
            v-if="isThirdPart"
            prop="platformStatus"
            :sortable="true"
            type="select"
            :filters="getPlatformStatusFilte"
          >
            <template v-slot="scope">
              <span v-if="scope.row.platform === 'Tapd'">
                  {{ scope.row.platformStatus ? tapdIssueStatusMap[scope.row.platformStatus] : '--' }}
                </span>
              <span v-else-if="scope.row.platform ==='Local'">
                  {{ '--' }}
                </span>
              <span v-else-if="platformStatusMap && platformStatusMap.get(scope.row.platformStatus)">
                  {{ platformStatusMap.get(scope.row.platformStatus) }}
              </span>
              <span v-else>
                  {{ scope.row.platformStatus ? scope.row.platformStatus : '--' }}
                </span>
            </template>
          </ms-table-column>

          <ms-table-column
            :field="item"
            :label="$t('test_track.issue.platform')"
            prop="platform"
          >
          </ms-table-column>

          <ms-table-column
            :field="item"
            :label="$t('test_track.review.creator')"
            prop="creatorName"
          >
          </ms-table-column>

          <issue-description-table-item :field="item" />
        </span>

      </ms-table>

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
</template>
<style scoped>
.search-input {
  float: right;
  width: 300px;
}
</style>
<script>
import { LOCAL } from "metersphere-frontend/src/utils/constants";
import MsNewUiSearch from "metersphere-frontend/src/components/new-ui/MsSearch";
import MsEditDialog from "metersphere-frontend/src/components/MsEditDialog";
import HomePagination from "@/business/home/components/pagination/HomePagination";
import MsTable from "metersphere-frontend/src/components/new-ui/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import {
  getIssuePartTemplateWithProject,
  getPlatformOption, getPlatformStatus,
  getRelateIssues,
  isThirdPartEnable,
  testCaseIssueRelate,
} from "@/api/issue";
import IssueDescriptionTableItem from "@/business/issue/IssueDescriptionTableItem";
import { ISSUE_STATUS_MAP, TAPD_ISSUE_STATUS_MAP } from "metersphere-frontend/src/utils/table-constants";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import { getPageInfo } from "metersphere-frontend/src/utils/tableUtils";
import { getCurrentProjectID, getCurrentWorkspaceId } from "metersphere-frontend/src/utils/token";
import { TEST_CASE_RELEVANCE_ISSUE_LIST } from "@/business/utils/sdk-utils";
import MsSearch from "metersphere-frontend/src/components/search/MsSearch";
import MsDrawerComponent from "../common/MsDrawerComponent";
import MsTableAdvSearch from "metersphere-frontend/src/components/new-ui/MsTableAdvSearch";
import {setIssuePlatformComponent} from "@/business/issue/issue";
import {getCustomFieldValue, getTableHeaderWithCustomFields} from "metersphere-frontend/src/utils/tableUtils";

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
      issueTemplate: {},
      fields: [],
      isThirdPart: false,
      selectCounts: null,
      screenHeight: 'calc(100vh - 185px)',
      maxHeight: 'calc(100vh - 287px)',
      platformStatus: [],
      platformStatusMap: new Map(),
    };
  },
  computed: {
    issueStatusMap() {
      return ISSUE_STATUS_MAP;
    },
    tapdIssueStatusMap() {
      return TAPD_ISSUE_STATUS_MAP;
    },
    projectId() {
      return getCurrentProjectID();
    },
    getPlatformStatusFilte() {
      return this.getPlatformStatusFiltes();
    },
  },
  props: {
    caseId: String,
    planCaseId: String,
    notInIds: Array,
  },
  created() {
    this.getIssueTemplate();
  },
  methods: {
    getIssueTemplate() {
      getIssuePartTemplateWithProject((template, project) => {
        this.currentProject = project;
        this.issueTemplate = template;
        if (this.issueTemplate.platform === LOCAL) {
          this.isThirdPart = false;
        } else {
          this.isThirdPart = true;
        }
        this.fields = getTableHeaderWithCustomFields("ISSUE_LIST", this.issueTemplate.customFields);
        if (!this.isThirdPart) {
          for (let i = 0; i < this.fields.length; i++) {
            if (this.fields[i].id === "platformStatus") {
              this.fields.splice(i, 1);
              break;
            }
          }
        }
        if (this.$refs.table) {
          this.$refs.table.reloadTable();
        }
      });
    },
    getCustomFieldValue(row, field) {
      return getCustomFieldValue(row, field, this.members);
    },
    clearSelection() {
      if (this.$refs.table) {
        this.$refs.table.clearSelectRows();
      }
    },
    setSelectCounts(data) {
      this.$refs.relevanceDialog.selectCounts = data;
    },
    open() {

      // 清空查询条件
      this.page = getPageInfo({
        components: TEST_CASE_RELEVANCE_ISSUE_LIST,
      });

      this.getIssueTemplate();
      this.getIssues();
      this.visible = true;
      this.$refs.relevanceDialog.open();

      getPlatformOption()
        .then((r) => {
          setIssuePlatformComponent(r.data, this.page.condition.components);
        });
      getPlatformStatus({
        projectId: getCurrentProjectID(),
        workspaceId: getCurrentWorkspaceId()
      }).then((r) => {
        this.platformStatus = r.data;
        this.platformStatusMap = new Map();
        if (this.platformStatus) {
          this.platformStatus.forEach(item => {
            this.platformStatusMap.set(item.value, item.label);
          });
        }
      });
    },
    getIssues() {
      this.page.condition.projectId = this.projectId;
      this.page.condition.notInIds = this.notInIds;
      this.page.loading = true;
      getRelateIssues(this.page);
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
    getPlatformStatusFiltes() {
      let options = [];
      getPlatformStatus({
        projectId: getCurrentProjectID(),
        workspaceId: getCurrentWorkspaceId()
      }).then((r) => {
        this.platformStatus = r.data;
        if (this.platformStatus) {
          this.platformStatus.forEach(item => {
            options.push({"text":item.label,"value":item.value,"system": false});
          });
          return options;
        }
        return options;
      });
      return options;
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
