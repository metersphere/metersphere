<template>
  <el-card class="table-card">
    <ms-table
      :table-is-loading="page.result.loading"
      :data="page.data"
      :enableSelection="false"
      :condition="page.condition"
      :total="page.total"
      :page-size.sync="page.pageSize"
      :show-select-all="false"
      :screen-height="screenHeight"
      :remember-order="true"
      @handlePageChange="getIssues"
      @handleRowClick="handleEdit"
      :fields.sync="fields"
      :field-key="tableHeaderKey"
      @refresh="getIssues"
      ref="table"
    >
      <ms-table-column
        v-for="(item) in fields" :key="item.key"
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
                  {{ scope.row.platformStatus ? tapdIssueStatusMap[scope.row.platformStatus] : '--' }}
                </span>
                <span v-else-if="scope.row.platform ==='Local'">
                  {{ scope.row.platformStatus ? tapdIssueStatusMap[scope.row.platformStatus] : '--' }}
                </span>
                <span v-else-if="platformStatusMap && platformStatusMap.get(scope.row.platformStatus)">
                  {{ platformStatusMap.get(scope.row.platformStatus) }}
                </span>
                <span v-else>
                  {{ scope.row.platformStatus ? scope.row.platformStatus : '--' }}
                </span>
              </span>

          <ms-review-table-item
            v-else-if="item.id === 'description'"
            :data="scope.row"
            prop="description"/>

          <span v-else-if="item.id === 'resourceName'">
                 <el-link v-if="scope.row.resourceName"
                          @click="$router.push('/track/plan/view/' + scope.row.resourceId)">
                  {{ scope.row.resourceName }}
                </el-link>
                <span v-else>
                  --
                 </span>
              </span>

          <span v-else-if="item.id === 'createTime'">
                 {{ scope.row.createTime | datetimeFormat }}
              </span>

          <span v-else-if="item.id === 'caseCount'">
                 <router-link
                   :to="scope.row.caseCount > 0 ? {name: 'testCase', params: { projectId: 'all', ids: scope.row.caseIds }} : {}">
                   {{ scope.row.caseCount }}
                 </router-link>
              </span>

          <!-- 自定义字段 -->
          <span v-else-if="item.isCustom">
                <span v-if="item.type === 'richText' && scope.row.displayValueMap[item.id]">
                     <ms-review-table-item
                       :data="scope.row.displayValueMap" :prop="item.id"/>
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

    <ms-table-pagination :change="getIssues" :current-page.sync="page.currentPage" :page-size.sync="page.pageSize"
                         :total="page.total"/>
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
  SYSTEM_FIELD_NAME_MAP,
  TAPD_ISSUE_STATUS_MAP
} from "metersphere-frontend/src/utils/table-constants";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import {getDashboardIssues, getIssuePartTemplateWithProject, getIssues, getPlatformOption} from "@/api/issue";
import {
  getCustomFieldValue,
  getCustomTableWidth,
  getLastTableSortField,
  getPageDate,
  getPageInfo, parseCustomFilesForList
} from "metersphere-frontend/src/utils/tableUtils";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import {getCurrentProjectID, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {getProjectMember} from "@/api/user";
import {getTableHeaderWithCustomFieldsByXpack} from "@/business/component/js/table-head-util";
import {LOCAL} from "metersphere-frontend/src/utils/constants";
import MsReviewTableItem from "@/business/component/MsReviewTableItem";
import {getUUID} from "metersphere-frontend/src/utils";

export default {
  name: "IssueTableList",
  components: {
    MsMainContainer,
    MsReviewTableItem,
    MsContainer,
    MsTableHeader,
    MsTablePagination, MsTableButton, MsTableOperators, MsTableColumn, MsTable
  },
  data() {
    return {
      page: getPageInfo(),
      fields: [],
      tableHeaderKey: "ISSUE_LIST",
      fieldsWidth: getCustomTableWidth('ISSUE_LIST'),
      issueTemplate: {},
      members: [],
      isThirdPart: false,
      platformOptions: [],
    };
  },
  activated() {
    getPlatformOption()
      .then((r) => {
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
        return 'calc(100vh - 160px)';
      }
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
    systemNameMap() {
      return SYSTEM_FIELD_NAME_MAP;
    },
    projectId() {
      return getCurrentProjectID();
    },

  },
  methods: {
    getCustomFieldValue(row, field) {
      let value = getCustomFieldValue(row, field, this.members);
      if (!value) {
        if (field.name === '处理人') {
          return row.maintainerName;
        }
      }
      return value;
    },
    initFields(template) {
      this.issueTemplate = template;
      if (this.issueTemplate.platform === LOCAL) {
        this.isThirdPart = false;
      } else {
        this.isThirdPart = true;
      }
      this.fields = getTableHeaderWithCustomFieldsByXpack('ISSUE_LIST_HEAD', this.issueTemplate.customFields);
      if (!this.isThirdPart) {
        for (let i = 0; i < this.fields.length; i++) {
          if (this.fields[i].id === 'platformStatus') {
            this.fields.splice(i, 1);
            break;
          }
        }
        // 如果不是三方平台则移除备选字段中的平台状态
        let removeField = {id: 'platformStatus', name: 'platformStatus', remove: true};
        this.issueTemplate.customFields.push(removeField);
      }
      this.$nextTick(() => {
        if (this.$refs.table) {
          this.$refs.table.reloadTable();
        }
      });
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
          }
        }
      } else if (this.isCreation) {
        if (this.page.condition.filters) {
          delete this.condition.filters['user_id']
        }
        this.page.condition.combine = {
          creator: {
            operator: "current user",
            value: "current user",
          }

        }
      } else {
        if (this.page.condition.filters) {
          this.page.condition.filters.status = ["new"];
        } else {
          this.page.condition.filters = {status: ["new"]};
        }
        this.page.condition.combine = {
          creator: {
            operator: "current user",
            value: "current user",
          }
        }
      }
      this.page.condition.workspaceId = getCurrentWorkspaceId();
      this.page.condition.orders = getLastTableSortField(this.tableHeaderKey);
      if (this.isDashboard) {
        this.page.result.loading = getDashboardIssues(this.page).then((response)=>{
          let data = response.data;
          this.page.total = data.itemCount;
          this.page.data = data.listObject;
          parseCustomFilesForList(this.page.data);
          this.initCustomFieldValue();
        });
      } else {
        this.page.result.loading = getIssues(this.page).then((response) => {
          let data = response.data;
          this.page.total = data.itemCount;
          this.page.data = data.listObject;
          parseCustomFilesForList(this.page.data);
          this.initCustomFieldValue();
        });
      }
    },
    initCustomFieldValue() {
      if (this.fields.length <= 0) {
        return;
      }
      this.page.data.forEach(item => {
        let displayValueMap = {};
        let fieldIdSet = new Set(this.fields.map(i => i.id));
        this.issueTemplate.customFields.forEach(field => {
          let displayValue;
          if (!fieldIdSet.has(field.name)) {
            return;
          }
          if (field.name === '状态') {
            displayValue = this.getCustomFieldValue(item, field, this.issueStatusMap[item.status]);
          } else {
            displayValue = this.getCustomFieldValue(item, field);
          }
          displayValueMap[field.name] = displayValue;
        });
        item.displayValueMap = displayValueMap;
      });
      this.loading = false;
    },

    handleEdit(resource) {
      let issueData = this.$router.resolve({path: '/track/issue', query: {id: resource.id}});
      window.open(issueData.href, '_blank');
    },
  },
  created() {
    this.page.result.loading = true;
    this.$nextTick(() => {
      getProjectMember((data) => {
        this.members = data;
      });
      getIssuePartTemplateWithProject((template) => {
        this.initFields(template);
        this.page.result.loading = false;
      });
      this.getIssues();
    });
  }
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

