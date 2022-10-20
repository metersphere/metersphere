<template>
  <ms-container>
    <ms-main-container>

      <el-card class="table-card">
        <template v-slot:header>
          <ms-table-header :create-permission="['PROJECT_TRACK_ISSUE:READ+CREATE']" :condition.sync="page.condition" @search="search" @create="handleCreate"
                           :create-tip="$t('test_track.issue.create_issue')"
                           :tip="$t('commons.search_by_name_or_id')">
            <template v-slot:button>
              <el-tooltip v-if="isThirdPart" :content="$t('test_track.issue.update_third_party_bugs')">
                <ms-table-button icon="el-icon-refresh" v-if="true"
                                 :content="$t('test_track.issue.sync_bugs')" @click="syncIssues"/>
              </el-tooltip>
            </template>
          </ms-table-header>
        </template>

        <ms-table
          v-loading="page.result.loading || loading"
          :data="page.data"
          :enableSelection="false"
          :condition="page.condition"
          :total="page.total"
          :page-size.sync="page.pageSize"
          :operators="operators"
          :show-select-all="false"
          :screen-height="screenHeight"
          :remember-order="true"
          :fields.sync="fields"
          :field-key="tableHeaderKey"
          :custom-fields="issueTemplate.customFields"
          @filter="search"
          @order="getIssues"
          @handlePageChange="getIssues"
          ref="table"
        >
    <span v-for="(item) in fields" :key="item.key">
        <ms-table-column width="1">
        </ms-table-column>
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
            sortable
            min-width="110"
            prop="title">
          </ms-table-column>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            :filters="platformFilters"
            :label="$t('test_track.issue.platform')"
            min-width="80"
            prop="platform">
          </ms-table-column>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            sortable
            min-width="110"
            :label="$t('test_track.issue.platform_status') "
            prop="platformStatus">
            <template v-slot="scope">
              <span v-if="scope.row.platform ==='Zentao'">{{ scope.row.platformStatus ? issueStatusMap[scope.row.platformStatus] : '--'}}</span>
              <span v-else-if="scope.row.platform ==='Tapd'">{{ scope.row.platformStatus ? tapdIssueStatusMap[scope.row.platformStatus] : '--'}}</span>
              <span v-else>{{ scope.row.platformStatus ? scope.row.platformStatus : '--'}}</span>
            </template>
          </ms-table-column>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            column-key="creator"
            :filters="creatorFilters"
            sortable
            min-width="100px"
            :label="$t('custom_field.issue_creator')"
            prop="creatorName">
          </ms-table-column>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('test_track.issue.issue_resource')"
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
        <ms-table-column prop="createTime"
                         :field="item"
                         :fields-width="fieldsWidth"
                         :label="$t('commons.create_time')"
                         sortable
                         min-width="180px">
            <template v-slot:default="scope">
              <span>{{ scope.row.createTime | datetimeFormat }}</span>
            </template>
          </ms-table-column >

          <issue-description-table-item :fields-width="fieldsWidth" :field="item"/>

         <ms-table-column
           :field="item"
           :fields-width="fieldsWidth"
           :label="item.label"
           prop="caseCount">
            <template v-slot="scope">
               <router-link :to="scope.row.caseCount > 0 ? {name: 'testCase', params: { projectId: 'all', ids: scope.row.caseIds }} : {}">
                 {{scope.row.caseCount}}
               </router-link>
            </template>
         </ms-table-column>

          <ms-table-column v-for="field in issueTemplate.customFields" :key="field.id"
                           :filters="field.name === '状态'? i18nCustomStatus(getCustomFieldFilter(field)) : getCustomFieldFilter(field)"
                           sortable="custom"
                           :field="item"
                           :fields-width="fieldsWidth"
                           min-width="120"
                           :label="field.system ? $t(systemNameMap[field.name]) :field.name"
                           :column-key="generateColumnKey(field)"
                           :prop="field.name">
              <template v-slot="scope">
                <span v-if="field.name === '状态'">
                  {{getCustomFieldValue(scope.row, field, issueStatusMap[scope.row.status])}}
                </span>
                <span v-else-if="field.type === 'richText'">
                   <el-popover
                     placement="right"
                     width="500"
                     trigger="hover"
                     popper-class="issues-popover">
                     <ms-mark-down-text prop="value" :data="{value: getCustomFieldValue(scope.row, field)}" :disabled="true"/>
                    <el-button slot="reference" type="text">{{ $t('test_track.issue.preview') }}</el-button>
                  </el-popover>
                </span>
                <span v-else>
                  {{getCustomFieldValue(scope.row, field)}}
                </span>
              </template>
          </ms-table-column>

        </span>
        </ms-table>

        <ms-table-pagination :change="getIssues" :current-page.sync="page.currentPage" :page-size.sync="page.pageSize"
                             :total="page.total"/>

        <issue-edit @refresh="getIssues" ref="issueEdit"/>
        <issue-sync-select @syncConfirm="syncConfirm" ref="issueSyncSelect" />
      </el-card>
    </ms-main-container>
  </ms-container>
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
import IssueDescriptionTableItem from "@/business/issue/IssueDescriptionTableItem";
import IssueEdit from "@/business/issue/IssueEdit";
import IssueSyncSelect from "@/business/issue/IssueSyncSelect";
import {
  checkSyncIssues,
  getIssuePartTemplateWithProject,
  getIssues,
  syncIssues,
  deleteIssue,
  getIssuesById
} from "@/api/issue";
import {
  getCustomFieldValue,
  getCustomTableWidth,
  getPageInfo, getTableHeaderWithCustomFields, getLastTableSortField, getCustomFieldFilter, parseCustomFilesForList
} from "metersphere-frontend/src/utils/tableUtils";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import {getCurrentProjectID, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {getProjectMember, getProjectMemberUserFilter} from "@/api/user";
import {LOCAL} from "metersphere-frontend/src/utils/constants";
import {TEST_TRACK_ISSUE_LIST} from "metersphere-frontend/src/components/search/search-components";
import {
  generateColumnKey,
  getAdvSearchCustomField
} from "metersphere-frontend/src/components/search/custom-component";
import MsMarkDownText from "metersphere-frontend/src/components/MsMarkDownText";

export default {
  name: "IssueList",
  components: {
    MsMarkDownText,
    MsMainContainer,
    MsContainer,
    IssueEdit,
    IssueDescriptionTableItem,
    IssueSyncSelect,
    MsTableHeader,
    MsTablePagination, MsTableButton, MsTableOperators, MsTableColumn, MsTable
  },
  data() {
    return {
      page: getPageInfo({
        components: TEST_TRACK_ISSUE_LIST,
        custom: false,
      }),
      fields: [],
      tableHeaderKey:"ISSUE_LIST",
      fieldsWidth: getCustomTableWidth('ISSUE_LIST'),
      screenHeight: 'calc(100vh - 160px)',
      operators: [
        {
          tip: this.$t('commons.edit'), icon: "el-icon-edit",
          exec: this.handleEdit,
          permissions: ['PROJECT_TRACK_ISSUE:READ+EDIT']
        }, {
          tip: this.$t('commons.copy'), icon: "el-icon-copy-document", type: "success",
          exec: this.handleCopy,
          isDisable: this.btnDisable
        }, {
          tip: this.$t('commons.delete'), icon: "el-icon-delete", type: "danger",
          exec: this.handleDelete,
          permissions: ['PROJECT_TRACK_ISSUE:READ+DELETE']
        }
      ],
      issueTemplate: {},
      members: [],
      userFilter: [],
      isThirdPart: false,
      creatorFilters: [],
      loading: false
    };
  },
  watch: {
    '$route'(to, from) {
      window.removeEventListener("resize", this.tableDoLayout);
    },
  },
  activated() {
    this.page.result.loading = true;
    this.$nextTick(() => {
      // 解决错位问题
      window.addEventListener('resize', this.tableDoLayout);
      getProjectMember()
        .then((response) => {
          this.members = response.data;
          this.userFilter = response.data.map(u => {
            return {text: u.name, value: u.id};
          });
          getIssuePartTemplateWithProject((template) => {
            this.initFields(template);
            this.page.result.loading = false;
          });
        });
      this.getIssues();
    });
  },
  computed: {
    platformFilters() {
      return ISSUE_PLATFORM_OPTION;
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
    workspaceId(){
      return getCurrentWorkspaceId();
    }
  },
  created() {
    this.getMaintainerOptions();
    //跳转
    this.editParam();
  },
  methods: {
    generateColumnKey,
    tableDoLayout() {
      if (this.$refs.table) this.$refs.table.doLayout();
    },
    getCustomFieldValue(row, field, defaultVal) {
      let value = getCustomFieldValue(row, field, this.members);
      return value ? value : defaultVal;
    },
    getCustomFieldFilter(field) {
      return getCustomFieldFilter(field, this.userFilter);
    },
    i18nCustomStatus(options) {
      let i18ns = [];
      if (options) {
        options.forEach(option => {
          option.text = this.$t(option.text);
          i18ns.push(option);
        });
      }
      return i18ns;
    },
    initFields(template) {
      this.issueTemplate = template;
      if (this.issueTemplate.platform === LOCAL) {
        this.isThirdPart = false;
      } else {
        this.isThirdPart = true;
      }
      this.fields = getTableHeaderWithCustomFields('ISSUE_LIST', this.issueTemplate.customFields, this.members);
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
      // 过滤自定义字段
      this.page.condition.components = this.page.condition.components.filter(item => item.custom !== true);
      let comp = getAdvSearchCustomField(this.page.condition, this.issueTemplate.customFields);
      this.page.condition.components.push(...comp);
      if (this.$refs.table) this.$refs.table.reloadTable();
    },
    search() {
      // 添加搜索条件时，当前页设置成第一页
      this.page.currentPage = 1;
      this.getIssues();
    },
    getIssues() {
      this.page.condition.projectId = this.projectId;
      this.page.condition.workspaceId= this.workspaceId;
      this.page.condition.orders = getLastTableSortField(this.tableHeaderKey);
      this.page.result = getIssues(this.page.currentPage, this.page.pageSize, this.page.condition).then((response) => {
        this.page.total = response.data.itemCount;
        this.page.data = response.data.listObject;
        parseCustomFilesForList(this.page.data);
      });
    },
    getMaintainerOptions() {
      getProjectMemberUserFilter((data) => {
        this.creatorFilters = data;
      });
    },
    handleEdit(data) {
      this.$refs.issueEdit.open(data, 'edit');
    },
    handleCreate() {
      this.$refs.issueEdit.open(null, 'add');
    },
    handleCopy(data) {
      let copyData = {};
      Object.assign(copyData, data)
      copyData.copyIssueId = copyData.id
      copyData.id = null;
      copyData.name = data.name + '_copy';
      this.$refs.issueEdit.open(copyData, 'copy');
    },
    handleDelete(data) {
      this.$alert(this.$t('test_track.issue.delete_tip') + ' ' + data.title + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this._handleDelete(data);
          }
        }
      });
    },
    _handleDelete(data) {
      deleteIssue(data.id).then(() => {
        this.$success(this.$t('commons.delete_success'));
        this.getIssues();
      })
    },
    btnDisable(row) {
      if (this.issueTemplate.platform !== row.platform) {
        return true;
      }
      return false;
    },
    syncIssues() {
      this.$refs.issueSyncSelect.open();
    },
    syncConfirm(data) {
      this.loading = true;
      let param = {
        "projectId": getCurrentProjectID(),
        "createTime": data.createTime.getTime(),
        "pre": data.preValue
      }
      syncIssues(param)
        .then((response) => {
          if (response.data === false) {
            checkSyncIssues(this.loading);
          } else {
            this.$success(this.$t('test_track.issue.sync_complete'));
            this.loading = false;
            this.getIssues();
          }
        });
    },
    editParam(){
      let id = this.$route.query.id;
      if (id) {
        getIssuesById(id).then((response) => {
          this.handleEdit(response.data)
        });
      }
    }
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
