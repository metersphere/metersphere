<template>
  <ms-container>
    <ms-main-container>
      <el-card class="table-card">
        <template v-slot:header>
          <ms-table-header :create-permission="['PROJECT_TRACK_ISSUE:READ+CREATE']" :condition.sync="page.condition"
                           @search="search" @create="handleCreate"
                           :create-tip="$t('test_track.issue.create_issue')"
                           :tip="$t('commons.search_by_name_or_id')">
            <template v-slot:button>

              <span v-if="isThirdPart && hasPermission('PROJECT_TRACK_ISSUE:READ+CREATE')">
                <ms-table-button
                  v-if="hasLicense"
                  icon="el-icon-refresh"
                  :content="$t('test_track.issue.sync_bugs')"
                  @click="syncAllIssues"/>
                <ms-table-button
                  v-if="!hasLicense"
                  icon="el-icon-refresh"
                  :content="$t('test_track.issue.sync_bugs')"
                  @click="syncIssues"/>
              </span>


              <ms-table-button icon="el-icon-upload2" :content="$t('commons.import')"
                               v-if="hasPermission('PROJECT_TRACK_ISSUE:READ+CREATE')" @click="handleImport"/>
              <ms-table-button icon="el-icon-download" :content="$t('commons.export')"
                               v-if="hasPermission('PROJECT_TRACK_ISSUE:READ')" @click="handleExport"/>
            </template>
          </ms-table-header>
        </template>

        <ms-table
          v-loading="loading"
          row-key="id"
          :data="page.data"
          :condition="page.condition"
          :total="page.total"
          :page-size.sync="page.pageSize"
          :operators="operators"
          :batch-operators="batchButtons"
          :screen-height="screenHeight"
          :remember-order="true"
          :fields.sync="fields"
          :field-key="tableHeaderKey"
          :custom-fields="issueTemplate.customFields"
          @headChange="handleHeadChange"
          @filter="search"
          @order="getIssues"
          @handlePageChange="getIssues"
          ref="table">

          <ms-table-column
            v-for="(item) in fields" :key="item.key"
            :label="item.label"
            :prop="item.id"
            :field="item"
            :sortable="item.sortable"
            :min-width="item.minWidth"
            :column-key="item.columnKey"
            :width="item.width"
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

              <span v-else-if="item.id === 'updateTime'">
                 {{ scope.row.updateTime | datetimeFormat }}
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
        <issue-edit @refresh="getIssues" ref="issueEdit"/>
        <issue-sync-select @syncConfirm="syncConfirm" ref="issueSyncSelect"/>
        <issue-import @refresh="getIssues" ref="issueImport"/>
        <issue-export @export="exportIssue" ref="issueExport"/>
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
import IssueImport from "@/business/issue/components/import/IssueImport";
import IssueExport from "@/business/issue/components/export/IssueExport";
import {
  checkSyncIssues,
  getIssuePartTemplateWithProject,
  getIssues,
  syncIssues,
  deleteIssue,
  getIssuesById, batchDeleteIssue, getPlatformOption, syncAllIssues, getPlatformStatus
} from "@/api/issue";
import {
  getCustomFieldValue,
  getCustomTableWidth,
  getPageInfo, getTableHeaderWithCustomFields, getLastTableSortField, getCustomFieldFilter, parseCustomFilesForList
} from "metersphere-frontend/src/utils/tableUtils";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import {getCurrentProjectID, getCurrentWorkspaceId, getCurrentUserId} from "metersphere-frontend/src/utils/token";
import {hasPermission} from "metersphere-frontend/src/utils/permission";
import {getProjectMember, getProjectMemberUserFilter} from "@/api/user";
import {LOCAL} from "metersphere-frontend/src/utils/constants";
import {TEST_TRACK_ISSUE_LIST} from "metersphere-frontend/src/components/search/search-components";
import {
  generateColumnKey,
  getAdvSearchCustomField
} from "metersphere-frontend/src/components/search/custom-component";
import MsMarkDownText from "metersphere-frontend/src/components/MsMarkDownText";
import {hasLicense} from "metersphere-frontend/src/utils/permission";
import MsReviewTableItem from "@/business/issue/MsReviewTableItem";
import {setIssuePlatformComponent} from "@/business/issue/issue";

export default {
  name: "IssueList",
  components: {
    MsReviewTableItem,
    MsMarkDownText,
    MsMainContainer,
    MsContainer,
    IssueEdit,
    IssueDescriptionTableItem,
    IssueSyncSelect,
    IssueImport,
    IssueExport,
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
      customFields: [], // 通过表头过滤后的自定义字段列表
      tableHeaderKey: "ISSUE_LIST",
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
          isDisable: this.btnDisable,
          permissions: ['PROJECT_TRACK_ISSUE:READ+DELETE']
        }, {
          tip: this.$t('commons.delete'), icon: "el-icon-delete", type: "danger",
          exec: this.handleDelete,
          permissions: ['PROJECT_TRACK_ISSUE:READ+DELETE']
        }
      ],
      batchButtons: [
        {
          name: this.$t('test_track.issue.batch_delete_issue'),
          handleClick: this.handleBatchDelete,
          permissions: ['PROJECT_TRACK_ISSUE:READ+DELETE']
        }
      ],
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
          minWidth: 100
        },
        title: {
          sortable: true,
          minWidth: 120,
        },
        platform: {
          minWidth: 80,
          filters: this.platformFilters
        },
        platformStatus: {
          sortable: true,
          minWidth: 110,
          type: 'select',
          filters: this.getPlatformStatusFiltes(),
        },
        creatorName: {
          columnKey: 'creator',
          minWidth: 100,
          filters: this.creatorFilters
        },
        resourceName: {},
        createTime: {
          sortable: true,
          minWidth: 180
        },
        updateTime: {
          sortable: true,
          minWidth: 180
        },
        caseCount: {}
      }
    };
  },
  watch: {
    '$route'(to, from) {
      window.removeEventListener("resize", this.tableDoLayout);
    }
  },
  activated() {
    if (this.$route.params.dataSelectRange) {
      this.dataSelectRange = this.$route.params.dataSelectRange;
    }
    this.loading = true;
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
          }, () => {
            this.loading = false;
          });
        });
      this.getIssues();
    });

    getPlatformOption()
      .then((r) => {
        this.platformOptions = r.data;
        setIssuePlatformComponent(this.platformOptions, this.page.condition.components);
      });

    this.hasLicense = hasLicense();

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
        this.page.condition.components.forEach(item => {
          if (item.key === 'platformStatus') {
            item.options =[];
            this.platformStatus.forEach(option => {
              item.options.push({label: option.label, value: option.value});
            });
          }
        });
      }
    });
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
    workspaceId() {
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
    hasPermission,
    tableDoLayout() {
      if (this.$refs.table) this.$refs.table.doLayout();
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
      let fields = getTableHeaderWithCustomFields('ISSUE_LIST', template.customFields, this.members);
      if (!this.isThirdPart) {
        for (let i = 0; i < fields.length; i++) {
          if (fields[i].id === 'platformStatus') {
            fields.splice(i, 1);
            break;
          }
        }
        // 如果不是三方平台则移除备选字段中的平台状态
        let removeField = {id: 'platformStatus', name: 'platformStatus', remove: true};
        template.customFields.push(removeField);
        for (let i = 0; i < this.page.condition.components.length; i++) {
          if (this.page.condition.components[i].key === 'platformStatus') {
            this.page.condition.components.splice(i, 1);
            break;
          }
        }

      }
      this.issueTemplate = template;
      fields.forEach(item => {
        if (this.columns[item.id]) {
          Object.assign(item, this.columns[item.id]);
          if (this.columns[item.id].filters) {
            item.filters = this.columns[item.id].filters;
          }
        }
      });

      this.fields = fields;

      // 过滤自定义字段
      this.page.condition.components = this.page.condition.components.filter(item => item.custom !== true);
      let comp = getAdvSearchCustomField(this.page.condition, template.customFields);
      this.page.condition.components.push(...comp);

      this.initCustomFieldValue();

      if (this.$refs.table) this.$refs.table.reloadTable();
    },
    search() {
      // 添加搜索条件时，当前页设置成第一页
      this.page.currentPage = 1;
      this.getIssues();
    },
    handleHeadChange() {
      this.initFields(this.issueTemplate);
    },
    getIssues() {
      this.loading = true;
      if (this.dataSelectRange === 'thisWeekUnClosedIssue') {
        this.page.condition.thisWeekUnClosedTestPlanIssue = true;
      } else if (this.dataSelectRange === 'unClosedRelatedTestPlan') {
        this.page.condition.unClosedTestPlanIssue = true;
      } else if (this.dataSelectRange === 'AllRelatedTestPlan') {
        this.page.condition.allTestPlanIssue = true;
      }
      this.page.condition.projectId = this.projectId;
      this.page.condition.workspaceId = this.workspaceId;
      this.page.condition.orders = getLastTableSortField(this.tableHeaderKey);
      getIssues(this.page.currentPage, this.page.pageSize, this.page.condition)
        .then((response) => {
          this.page.total = response.data.itemCount;
          this.page.data = response.data.listObject;
          parseCustomFilesForList(this.page.data);
          this.initCustomFieldValue();
        });
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
    handleBatchDelete() {
      this.$alert(this.$t('test_track.issue.batch_delete_tip') + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this._handleBatchDelete();
          }
        }
      });
    },
    _handleBatchDelete() {
      let selectIds = this.$refs.table.selectIds;
      if (selectIds.length == 0) {
        this.$warning(this.$t("test_track.issue.check_select"));
        return;
      }
      batchDeleteIssue({"batchDeleteIds": selectIds, "batchDeleteAll": this.page.condition.selectAll})
        .then(() => {
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
    syncAllIssues() {
      this.$refs.issueSyncSelect.open();
    },
    handleImport() {
      this.$refs.issueImport.open();
    },
    handleExport() {
      let exportIds = this.$refs.table.selectIds;
      if (exportIds.length == 0) {
        this.$warning(this.$t("test_track.issue.check_select"));
        return;
      }
      this.$refs.issueExport.open();
    },
    exportIssue(data) {
      let param = {
        "projectId": getCurrentProjectID(),
        "workspaceId": getCurrentWorkspaceId(),
        "userId": getCurrentUserId(),
        "isSelectAll": this.page.condition.selectAll,
        "exportIds": this.$refs.table.selectIds,
        "exportFields": data,
        "orders": getLastTableSortField(this.tableHeaderKey),
        "combine": this.page.condition.combine,
        "name": this.page.condition.name
      }
      this.$fileDownloadPost("/issues/export", param);
    },
    syncConfirm(data) {
      this.loading = true;
      let param = {
        "projectId": getCurrentProjectID(),
        "createTime": data.createTime.getTime(),
        "pre": data.preValue
      }
      syncAllIssues(param)
        .then((response) => {
          if (response.data === false) {
            checkSyncIssues(this.loading);
          } else {
            this.$success(this.$t('test_track.issue.sync_complete'));

            this.getIssues();
          }
        })
      .catch(() => {
        this.loading = false;
      });
    },
    syncIssues() {
      this.loading = true;
      syncIssues()
        .then((response) => {
          if (response.data === false) {
            checkSyncIssues(this.loading);
          } else {
            this.$success(this.$t('test_track.issue.sync_complete'));
            this.loading = false;
            this.getIssues();
          }
        }).catch(() => {
          this.loading = false;
        });
    },
    editParam() {
      let id = this.$route.query.id;
      if (id) {
        getIssuesById(id).then((response) => {
          this.handleEdit(response.data)
        });
      } else {
        let type = this.$route.query.type;
        if (type === 'create') {
          this.$nextTick(() => {
            this.handleCreate()
          });
        }
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

:deep(.el-table) {
  overflow: auto;
}

span.operate-button button {
  margin-left: 10px;
}
</style>
