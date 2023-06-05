<template>
  <div class="relate-box">
    <div class="relate-header">
      <div class="menu-left-row">
        <el-dropdown placement="bottom" :disabled="readOnly">
          <div style="line-height: 32px; color: #1F2329; cursor: pointer">
            <i class="el-icon-connection" style="margin-right: 4.3px"></i
            >{{ $t("case.associated_defect") }}
          </div>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item>
              <div
                class="add-btn"
                v-permission="['PROJECT_TRACK_PLAN:READ+RELEVANCE_OR_CANCEL']"
                :disabled="readOnly"
                type="primary"
                size="mini"
                @click="addIssue"
              >
                {{ $t("case.create_defect") }}
              </div>
            </el-dropdown-item
            >
            <el-dropdown-item
            >
              <div
                class="add-btn"
                v-permission="['PROJECT_TRACK_PLAN:READ+RELEVANCE_OR_CANCEL']"
                :disabled="readOnly"
                @click="relateIssue"
              >
                {{ $t("case.associate_existing_defects") }}
              </div>
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
      <div class="search-right-row">
        <ms-new-ui-search
          :condition.sync="condition"
          @search="search"
          :baseSearchTip="$t('case.search_by_id')"
        />
      </div>
    </div>
    <div class="table-data">
      <ms-table
        v-loading="page.result.loading"
        :show-select-all="false"
        :data="page.data"
        :fields.sync="fields"
        :operators="operators"
        :enable-selection="false"
        :enable-max-height="false"
        :max-height="'calc(100vh)'"
        ref="table"
        @refresh="getIssues"
      >
        <span v-for="item in fields" :key="item.key">
          <ms-table-column
            :label="$t('test_track.issue.id')"
            :field="item"
            prop="id"
            v-if="false"
          >
          </ms-table-column>
          <ms-table-column
            :field="item"
            :label="$t('ID')"
            :sortable="true"
            prop="num"
          >
          </ms-table-column>

          <ms-table-column
            :field="item"
            :sortable="true"
            :label="$t('test_track.issue.title')"
            prop="title"
          >
          </ms-table-column>

          <ms-table-column
            :label="$t('test_track.issue.platform_status')"
            :field="item"
            v-if="isThirdPart"
            prop="platformStatus"
          >
            <template v-slot="scope">
              <span v-if="item.id === 'platformStatus'">
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
              </span>
            </template>
          </ms-table-column>

          <span v-for="field in issueTemplate.customFields" :key="field.id">
            <ms-table-column
              :field="item"
              :label="field.name"
              :prop="field.name"
              v-if="field.name === '状态'"
            >
              <template v-slot="scope">
                <el-dropdown
                  class="test-case-status"
                  @command="statusChange"
                  placement="bottom"
                  trigger="click"
                >
                  <span class="el-dropdown-link">
                    {{
                      getCustomFieldValue(scope.row, field)
                        ? getCustomFieldValue(scope.row, field)
                        : issueStatusMap[scope.row.status]
                    }}
                  </span>
                  <el-dropdown-menu slot="dropdown" chang>
                    <span v-for="(item, index) in status" :key="index">
                      <el-dropdown-item
                        :command="{ id: scope.row.id, status: item.value }"
                      >
                        {{ item.system ? $t(item.text) : item.text }}
                      </el-dropdown-item>
                    </span>
                  </el-dropdown-menu>
                </el-dropdown>
              </template>
            </ms-table-column>
          </span>

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

          <issue-description-table-item :field="item"/>
        </span>
      </ms-table>
    </div>

    <test-plan-issue-edit
      :plan-case-id="planCaseId"
      :plan-id="planId"
      :case-id="caseId"
      @refresh="getIssues"
      ref="issueEdit"
    />

    <IssueRelateList
      :plan-case-id="planCaseId"
      :case-id="caseId"
      :not-in-ids="notInIds"
      @refresh="getIssues"
      ref="issueRelate"
    />
  </div>
</template>

<script>
import TestPlanIssueEdit from "./CasePlanIssueEdit";
import MsTable from "metersphere-frontend/src/components/new-ui/MsTable";
import HomePagination from "@/business/home/components/pagination/HomePagination";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import IssueDescriptionTableItem from "@/business/issue/IssueDescriptionTableItem";
import {ISSUE_STATUS_MAP, TAPD_ISSUE_STATUS_MAP} from "metersphere-frontend/src/utils/table-constants";
import IssueRelateList from "./CaseIssueRelateList";
import {
  buildIssues,
  closeIssue,
  deleteIssueRelate,
  getIssuePartTemplateWithProject,
  getIssuesByCaseIdWithSearch,
  getPlatformStatus,
  issueStatusChange,
  like,
  parseFields,
} from "@/api/issue";
import {getCustomFieldValue, getTableHeaderWithCustomFields,} from "metersphere-frontend/src/utils/tableUtils";
import {LOCAL} from "metersphere-frontend/src/utils/constants";
import {getCurrentProjectID, getCurrentWorkspaceId,} from "metersphere-frontend/src/utils/token";
import MsNewUiSearch from "metersphere-frontend/src/components/new-ui/MsSearch";

export default {
  name: "CaseIssueRelate",
  components: {
    HomePagination,
    IssueRelateList,
    IssueDescriptionTableItem,
    MsTableColumn,
    MsTable,
    TestPlanIssueEdit,
    MsNewUiSearch
  },
  data() {
    return {
      page: {
        data: [],
        result: {},
      },
      isThirdPart: false,
      issueTemplate: {},
      fields: [],
      operators: [
        {
          tip: this.$t("test_track.case.unlink"),
          isTextButton: true,
          type: "danger",
          exec: this.deleteIssue,
        },
      ],
      status: [],
      issueRelateVisible: false,
      condition: {},
      platformStatus: [],
      platformStatusMap: new Map(),
    };
  },
  props: {
    planId: String,
    caseId: String,
    copyCaseId: String,
    planCaseId: String,
    readOnly: Boolean,
    isCopy: Boolean,
  },
  computed: {
    issueStatusMap() {
      return ISSUE_STATUS_MAP;
    },
    tapdIssueStatusMap() {
      return TAPD_ISSUE_STATUS_MAP;
    },
    notInIds() {
      return this.page.data ? this.page.data.map((i) => i.id) : [];
    },
    projectId() {
      return getCurrentProjectID();
    },
  },
  created() {
    getIssuePartTemplateWithProject((template, project) => {
      this.currentProject = project;
      this.issueTemplate = template;
      if (this.issueTemplate.platform === LOCAL) {
        this.isThirdPart = false;
      } else {
        this.isThirdPart = true;
      }
      if (template) {
        let customFields = template.customFields;
        for (let fields of customFields) {
          if (fields.name === "状态") {
            this.status = fields.options;
            break;
          }
        }
      }
      this.fields = getTableHeaderWithCustomFields(
        "ISSUE_LIST",
        this.issueTemplate.customFields
      );
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
  activated() {
    this.search();
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
  methods: {
    search() {
      this.getIssues();
    },
    statusChange(param) {
      issueStatusChange(param).then(() => {
        this.getIssues();
        this.$success(this.$t("commons.modify_success"), false);
      });
    },
    getCustomFieldValue(row, field) {
      return getCustomFieldValue(row, field, this.members);
    },
    getIssues() {
      if (!this.isCopy) {
        this.page.result = true;
        let result = getIssuesByCaseIdWithSearch(this.planId ? "PLAN_FUNCTIONAL" : "FUNCTIONAL", this.getCaseResourceId())
                      .then((response) => {
                        if(this.condition && this.condition.name && response.data){
                          //过滤
                          this.page.data = response.data.filter((v) => {
                            return (
                              like(condition.name, v.title) ||
                              like(condition.name, v.num)
                            );
                          });
                        } else {
                          this.page.data = response.data;
                        }
                        this.$emit("setCount", this.page.data.length);
                        buildIssues(page);
                        parseFields(page);
                      });
        if (result) {
          this.page.result = result;
        }
      }
    },
    getCaseResourceId() {
      return this.planId ? this.planCaseId : this.caseId;
    },
    addIssue() {
      if (this.readOnly) {
        return;
      }
      if (!this.caseId || this.isCopy) {
        this.$warning(this.$t("api_test.automation.save_case_info"), false);
        return;
      }
      this.$refs.issueEdit.open();
    },
    relateIssue() {
      if (this.readOnly) {
        return;
      }
      if (!this.caseId || this.isCopy) {
        this.$warning(this.$t("api_test.automation.save_case_info"), false);
        return;
      }
      this.$refs.issueRelate.open();
    },
    closeIssue(row) {
      if (row.status === "closed") {
        this.$success(this.$t("test_track.issue.close_success"));
      } else {
        this.page.result.loading = true;
        closeIssue(row.id).then(() => {
          this.page.result.loading = false;
          this.getIssues();
          this.$success(this.$t("test_track.issue.close_success"));
        });
      }
    },
    deleteIssue(row) {
      this.$confirm(this.$t("test_track.issue.delete_warning"), this.$t("case.cancel_relate_case_tips_title"), {
          cancelButtonText: this.$t("commons.cancel"),
          confirmButtonText: this.$t("commons.confirm"),
          callback: action => {
            if (action === "confirm") {
              this.page.result.loading = true;
              deleteIssueRelate({
                id: row.id,
                caseResourceId: this.getCaseResourceId(),
                isPlanEdit: this.planId ? true : false,
                projectId: this.projectId,
                workspaceId: getCurrentWorkspaceId(),
              }).then(() => {
                this.page.result.loading = false;
                this.getIssues();
                this.$success(this.$t("test_track.cancel_relevance_success"), false);
              });
            }
          }
        }
      );
    },
  },
};
</script>

<style scoped lang="scss">
@import "@/business/style/index.scss";

.add-btn {
  display: inline-block;
  margin-right: 5px;
}

.el-dropdown-link {
  cursor: pointer;
  color: #783887;
}

.relate-box {
  padding: 1.3rem 0 0 0;

  .relate-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 1.3rem;

    .menu-left-row {
      width: 98px;
      height: 32px;
      left: 112px;
      top: 251px;
      background: #ffffff;
      border: 1px solid #DCDFE6;
      border-radius: 4px;
      text-align: center;
      color: #783887;
    }

    .search-right-row {
      width: 240px;
    }
  }
}

.relate-box .relate-header .menu-left-row:hover {
  background-color: whitesmoke;
}

.el-dropdown-menu__item:hover {
  background-color: rgba(31, 35, 41, 0.1) !important;
}

.el-dropdown-menu__item {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  color: #1F2329 !important;
}
</style>
