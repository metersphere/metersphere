<template>
  <el-card class="table-card">
    <ms-table
      v-loading="loading"
      row-key="id"
      :data="page.data"
      :enableSelection="false"
      :condition="page.condition"
      :total="page.total"
      :page-size.sync="page.pageSize"
      :show-select-all="false"
      :screen-height="screenHeight"
      :remember-order="true"
      :fields.sync="fields"
      :field-key="tableHeaderKey"
      :custom-fields="issueTemplate.customFields"
      @headChange="handleHeadChange"
      @handleRowClick="handleEdit"
      @filter="search"
      @order="getIssues"
      @handlePageChange="getIssues"
      ref="table"
    >
      <ms-table-column
        v-for="item in fields"
        :key="item.key"
        :label="item.label"
        :prop="item.id"
        :field="item"
        :sortable="item.sortable"
        :min-width="item.minWidth"
        :column-key="item.columnKey"
        :fields-width="fieldsWidth"
        :filters="item.filters"
      >
        <template v-slot="scope">
          <span v-if="item.id === 'platformStatus'">
            <span v-if="scope.row.platform === 'Tapd'">
              {{
                scope.row.platformStatus
                  ? tapdIssueStatusMap[scope.row.platformStatus]
                  : "--"
              }}
            </span>
            <span v-else-if="scope.row.platform === 'Local'">
              {{
                scope.row.platformStatus
                  ? tapdIssueStatusMap[scope.row.platformStatus]
                  : "--"
              }}
            </span>
            <span
              v-else-if="
                platformStatusMap &&
                platformStatusMap.get(scope.row.platformStatus)
              "
            >
              {{ platformStatusMap.get(scope.row.platformStatus) }}
            </span>
            <span v-else>
              {{ scope.row.platformStatus ? scope.row.platformStatus : "--" }}
            </span>
          </span>

          <ms-review-table-item
            v-else-if="item.id === 'description'"
            :data="scope.row"
            prop="description"
          />

          <span v-else-if="item.id === 'resourceName'">
            <el-link
              v-if="scope.row.resourceName"
              @click="$router.push('/track/plan/view/' + scope.row.resourceId)"
            >
              {{ scope.row.resourceName }}
            </el-link>
            <span v-else> -- </span>
          </span>

          <span v-else-if="item.id === 'createTime'">
            {{ scope.row.createTime | datetimeFormat }}
          </span>

          <span v-else-if="item.id === 'updateTime'">
            {{ scope.row.updateTime | datetimeFormat }}
          </span>

          <span v-else-if="item.id === 'caseCount'">
            <router-link
              :to="
                scope.row.caseCount > 0
                  ? {
                      name: 'testCase',
                      params: { projectId: 'all', ids: scope.row.caseIds },
                    }
                  : {}
              "
            >
              {{ scope.row.caseCount }}
            </router-link>
          </span>

          <!-- 自定义字段 -->
          <span v-else-if="item.isCustom">
            <span
              v-if="
                item.type === 'richText' && scope.row.displayValueMap[item.id]
              "
            >
              <ms-review-table-item
                :data="scope.row.displayValueMap"
                :prop="item.id"
              />
            </span>
            <span v-else>
              {{ scope.row.displayValueMap[item.id] }}
            </span>
          </span>

          <span v-else>
            {{ scope.row[item.id] }}
          </span>
        </template>
      </ms-table-column>
    </ms-table>

    <ms-table-pagination
      :change="getIssues"
      :current-page.sync="page.currentPage"
      :page-size.sync="page.pageSize"
      :total="page.total"
    />
  </el-card>
</template>

<script>
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsTableOperators from "metersphere-frontend/src/components/MsTableOperators";
import MsTableButton from "metersphere-frontend/src/components/MsTableButton";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import {
  ISSUE_PLATFORM_OPTION,
  ISSUE_STATUS_MAP,
  TAPD_ISSUE_STATUS_MAP,
} from "metersphere-frontend/src/utils/table-constants";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";

import {
  getDashboardIssues,
  getIssuePartTemplateWithProject,
  getIssues,
  getPlatformOption,
} from "@/api/issue";
import {
  getCustomFieldFilter,
  getCustomFieldValue,
  getCustomTableWidth,
  getLastTableSortField,
  getPageInfo,
  getTableHeaderWithCustomFields,
  parseCustomFilesForList,
} from "metersphere-frontend/src/utils/tableUtils";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import {
  getCurrentProjectID,
  getCurrentWorkspaceId,
} from "metersphere-frontend/src/utils/token";
import { getProjectMember, getProjectMemberUserFilter } from "@/api/user";
import { LOCAL } from "metersphere-frontend/src/utils/constants";
import { TEST_TRACK_ISSUE_LIST } from "metersphere-frontend/src/components/search/search-components";
import { getAdvSearchCustomField } from "metersphere-frontend/src/components/search/custom-component";
import MsMarkDownText from "metersphere-frontend/src/components/MsMarkDownText";
import MsReviewTableItem from "@/business/component/MsReviewTableItem";
import IssueDescriptionTableItem from "@/business/component/IssueDescriptionTableItem";

export default {
  name: "IssueList",
  components: {
    MsReviewTableItem,
    MsMarkDownText,
    MsMainContainer,
    MsContainer,
    IssueDescriptionTableItem,
    MsTableHeader,
    MsTablePagination,
    MsTableButton,
    MsTableOperators,
    MsTableColumn,
    MsTable,
  },
  data() {
    return {
      page: getPageInfo({
        components: TEST_TRACK_ISSUE_LIST,
        custom: false,
      }),
      fields: [],
      customFields: [], // 通过表头过滤后的自定义字段列表
      tableHeaderKey: "ISSUE_LIST",
      fieldsWidth: getCustomTableWidth("ISSUE_LIST"),
      issueTemplate: {},
      members: [],
      userFilter: [],
      isThirdPart: false,
      creatorFilters: [],
      loading: false,
      dataSelectRange: "",
      platformOptions: [],
      platformStatus: [],
      platformStatusMap: new Map(),
      hasLicense: false,
      columns: {
        num: {
          sortable: true,
          minWidth: 100,
        },
        title: {
          sortable: true,
          minWidth: 120,
        },
        platform: {
          minWidth: 80,
          filters: this.platformFilters,
        },
        platformStatus: {
          minWidth: 110,
        },
        creatorName: {
          columnKey: "creator",
          minWidth: 100,
          filters: this.creatorFilters,
        },
        resourceName: {},
        createTime: {
          sortable: true,
          minWidth: 180,
        },
        updateTime: {
          sortable: true,
          minWidth: 180,
        },
        caseCount: {},
      },
    };
  },
  activated() {
    getPlatformOption().then((r) => {
      this.platformOptions = r.data;
    });
  },
  props: {
    isFocus: {
      type: Boolean,
      default: false,
    },
    isShowAllColumn: {
      type: Boolean,
      default: true,
    },
    isSelectAll: {
      type: Boolean,
      default: false,
    },
    isCreation: {
      type: Boolean,
      default: false,
    },
    isDashboard: {
      type: Boolean,
      default: false,
    },
    screenHeight: {
      type: [Number, String],
      default() {
        return "calc(100vh - 160px)";
      },
    }, //屏幕高度
  },
  computed: {
    platformFilters() {
      let options = [...ISSUE_PLATFORM_OPTION];
      options.push(...this.platformOptions);
      return options;
    },
    issueStatusMap() {
      return ISSUE_STATUS_MAP;
    },
    tapdIssueStatusMap() {
      return TAPD_ISSUE_STATUS_MAP;
    },
    projectId() {
      return getCurrentProjectID();
    },
    workspaceId() {
      return getCurrentWorkspaceId();
    },
    isToDo() {
      return !this.isFocus && !this.isCreation;
    },
  },
  created() {
    this.getMaintainerOptions();
    this.loading = true;
    this.$nextTick(() => {
      getProjectMember((data) => {
        this.members = data;
      });
      getIssuePartTemplateWithProject((template) => {
        this.initFields(template);
        this.getIssues();
      });
    });
  },
  methods: {
    getCustomFieldValue(row, field, defaultVal) {
      let value = getCustomFieldValue(row, field, this.members);
      return value ? value : defaultVal;
    },
    getCustomFieldFilter(field) {
      return getCustomFieldFilter(field, this.userFilter);
    },
    initFields(template) {
      if (template.platform === LOCAL) {
        this.isThirdPart = false;
      } else {
        this.isThirdPart = true;
      }
      let fields = getTableHeaderWithCustomFields(
        "ISSUE_LIST",
        template.customFields,
        this.members
      );
      if (!this.isThirdPart) {
        for (let i = 0; i < fields.length; i++) {
          if (fields[i].id === "platformStatus") {
            fields.splice(i, 1);
            break;
          }
        }
        // 如果不是三方平台则移除备选字段中的平台状态
        let removeField = {
          id: "platformStatus",
          name: "platformStatus",
          remove: true,
        };
        template.customFields.push(removeField);
      }
      this.issueTemplate = template;
      fields.forEach((item) => {
        if (this.columns[item.id]) {
          Object.assign(item, this.columns[item.id]);
          if (this.columns[item.id].filters) {
            item.filters = this.columns[item.id].filters;
          }
        }
        if (this.isToDo && item.id === "状态") {
          item.filters = item.filters.filter(
            (i) =>
              i.value !== "closed" &&
              i.value !== "已关闭" &&
              i.value !== "resolved" &&
              i.value !== "Done" &&
              i.value !== "verified"
          );
        }
      });

      this.fields = fields;

      // 过滤自定义字段
      this.page.condition.components = this.page.condition.components.filter(
        (item) => item.custom !== true
      );
      let comp = getAdvSearchCustomField(
        this.page.condition,
        template.customFields
      );
      this.page.condition.components.push(...comp);

      this.initCustomFieldValue();

      if (this.$refs.table) {
        this.$refs.table.reloadTable();
      }
      this.loading = false;
    },
    search() {
      // 添加搜索条件时，当前页设置成第一页
      this.page.currentPage = 1;
      this.getIssues();
    },
    handleHeadChange() {
      this.initFields(this.issueTemplate);
    },
    handleEdit(resource) {
      let issueData = this.$router.resolve({
        path: "/track/issue",
        query: { id: resource.id },
      });
      window.open(issueData.href, "_blank");
    },
    getIssues() {
      if (this.isSelectAll === false) {
        this.page.condition.projectId = this.projectId;
      }
      if (this.isFocus) {
        this.page.condition.combine = {
          followPeople: {
            operator: "current user",
            value: "current user",
          },
        };
      } else if (this.isCreation) {
        if (this.page.condition.filters) {
          delete this.condition.filters["user_id"];
        }
        this.page.condition.combine = {
          creator: {
            operator: "current user",
            value: "current user",
          },
        };
      } else {
        this.addDefaultStatusFilter();
      }
      this.page.condition.workspaceId = getCurrentWorkspaceId();
      this.page.condition.orders = getLastTableSortField(this.tableHeaderKey);
      if (this.isDashboard) {
        this.loading = true;
        this.page.result.loading = getDashboardIssues(this.page).then(
          (response) => {
            let data = response.data;
            this.page.total = data.itemCount;
            this.page.data = data.listObject;
            parseCustomFilesForList(this.page.data);
            this.initCustomFieldValue();
            this.loading = false;
          }
        );
      } else {
        this.loading = true;
        this.page.result.loading = getIssues(this.page).then((response) => {
          let data = response.data;
          this.page.total = data.itemCount;
          this.page.data = data.listObject;
          parseCustomFilesForList(this.page.data);
          this.initCustomFieldValue();
          this.loading = false;
        });
      }
    },
    addDefaultStatusFilter() {
      this.page.condition.combine = {
        creator: {
          operator: "current user",
          value: "current user",
        },
      };
      if (this.isToDo) {
        this.page.condition.combine.doneStatus = {
          operator: "not in",
          value: [
            "closed",
            "已关闭",
            "已完成",
            "完成",
            "拒绝",
            "已拒绝",
            "rejected",
            "delete",
            "resolved",
            "Done",
            "verified",
            "已验证",
          ],
        };
      }
    },
    initCustomFieldValue() {
      if (this.fields.length <= 0) {
        return;
      }
      this.page.data.forEach((item) => {
        let displayValueMap = {};
        let fieldIdSet = new Set(this.fields.map((i) => i.id));
        this.issueTemplate.customFields.forEach((field) => {
          let displayValue;
          if (!fieldIdSet.has(field.name)) {
            return;
          }
          if (field.name === "状态") {
            displayValue = this.getCustomFieldValue(
              item,
              field,
              this.issueStatusMap[item.status]
            );
          } else {
            displayValue = this.getCustomFieldValue(item, field);
          }
          displayValueMap[field.name] = displayValue;
        });
        item.displayValueMap = displayValueMap;
      });
      this.loading = false;
    },
    getMaintainerOptions() {
      getProjectMemberUserFilter((data) => {
        this.creatorFilters = data;
      });
    },
  },
};
</script>

<style scoped>
.table-page {
  padding-top: 20px;
  margin-right: -9px;
  float: right;
}

.el-table {
  cursor: pointer;
}
</style>
