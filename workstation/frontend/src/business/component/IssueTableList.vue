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
      <span v-for="(item) in fields" :key="item.key">
        <ms-table-column
          :label="$t('test_track.issue.id')"
          prop="num"
          :field="item"
          sortable
          min-width="100"
          :fields-width="fieldsWidth">
        </ms-table-column>

        <ms-table-column
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.issue.title')"
          min-width="100"
          prop="title">
        </ms-table-column>

        <ms-table-column
          :field="item"
          :fields-width="fieldsWidth"
          sortable
          min-width="110"
          :label="$t('test_track.issue.platform_status') "
          prop="platformStatus">
          <template v-slot="scope">
            <span
              v-if="scope.row.platform ==='Zentao'">{{ scope.row.platformStatus ? issueStatusMap[scope.row.platformStatus] : '--' }}</span>
            <span v-else>{{ scope.row.platformStatus ? scope.row.platformStatus : '--' }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
          :field="item"
          :fields-width="fieldsWidth"
          :filters="platformFilters"
          :label="$t('test_track.issue.platform')"
          min-width="100"
          prop="platform">
        </ms-table-column>

        <ms-table-column
          prop="createTime"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('commons.create_time')"
          sortable
          min-width="140px">
            <template v-slot:default="scope">
              <span>{{ scope.row.createTime | datetimeFormat }}</span>
            </template>
        </ms-table-column>

        <ms-table-column
          prop="projectName"
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('test_track.issue.issue_project')"
          min-width="80">
          <template v-slot="scope">
            {{ scope.row.projectName ? scope.row.projectName : '--' }}
          </template>
        </ms-table-column>
        <ms-table-column
          :field="item"
          v-if="isShowAllColumn"
          :fields-width="fieldsWidth"
          column-key="creator"
          min-width="100"
          :label="$t('custom_field.issue_creator')"
          prop="creatorName">
        </ms-table-column>

        <ms-table-column
          :field="item"
          v-if="isShowAllColumn"
          :fields-width="fieldsWidth"
          :label="$t('test_track.issue.issue_resource')"
          min-width="120"
          prop="resourceName">
          <template v-slot="scope">
            <el-link v-if="scope.row.resourceName" @click="$router.push('/track/plan/view/' + scope.row.resourceId)">
              {{ scope.row.resourceName }}
            </el-link>
            <span v-else>
            --
            </span>
          </template>
        </ms-table-column>

        <issue-description-table-item :fields-width="fieldsWidth" :field="item" v-if="isShowAllColumn"/>

        <ms-table-column
          :field="item"
          v-if="isShowAllColumn"
          :fields-width="fieldsWidth"
          :label="item.label"
          prop="caseCount">
            <template v-slot="scope">
               <router-link
                 :to="scope.row.caseCount > 0 ? {name: 'testCase', params: { projectId: 'all', ids: scope.row.caseIds }} : {}">
                 {{ scope.row.caseCount }}
               </router-link>
            </template>
          </ms-table-column>

          <div v-if="isShowAllColumn">
            <ms-table-column v-for="field in issueTemplate.customFields" :key="field.id"
                             :field="item"
                             min-width="120"
                             :fields-width="fieldsWidth"
                             :label="field.system ? $t(systemNameMap[field.name]) :field.name"
                             :prop="field.name">
              <template v-slot="scope">
                <span v-if="field.name === '状态'">
                  {{ getCustomFieldValue(scope.row, field) ? getCustomFieldValue(scope.row, field) : issueStatusMap[scope.row.status] }}
                </span>
                <span v-else>
                  {{ getCustomFieldValue(scope.row, field) }}
                </span>
              </template>
          </ms-table-column>
          </div>

      </span>
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
import {ISSUE_PLATFORM_OPTION, ISSUE_STATUS_MAP, SYSTEM_FIELD_NAME_MAP} from "metersphere-frontend/src/utils/table-constants";
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
import IssueDescriptionTableItem from "@/business/component/IssueDescriptionTableItem";
import {getUUID} from "metersphere-frontend/src/utils";

export default {
  name: "IssueTableList",
  components: {
    MsMainContainer,
    MsContainer,
    IssueDescriptionTableItem,
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
        });
      } else {
        this.page.result.loading = getIssues(this.page).then((response)=>{
          let data = response.data;
          this.page.total = data.itemCount;
          this.page.data = data.listObject;
          parseCustomFilesForList(this.page.data);
        });
      }
    },
    handleEdit(resource) {
      let issueData = this.$router.resolve({path:'/track/issue',query:{id:resource.id}});
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

