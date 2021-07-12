<template>
  <ms-container>
    <ms-main-container>
      <el-card class="table-card">
        <template v-slot:header>
          <ms-table-header :create-permission="['PROJECT_TRACK_ISSUE:READ+CREATE']" :condition.sync="page.condition" @search="getIssues" @create="handleCreate"
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
          v-loading="page.result.loading"
          :data="page.data"
          :enableSelection="false"
          :condition="page.condition"
          :total="page.total"
          :page-size.sync="page.pageSize"
          :operators="operators"
          :show-select-all="false"
          :screen-height="screenHeight"
          @handlePageChange="getIssues"
          :fields.sync="fields"
          :field-key="tableHeaderKey"
          @saveSortField="saveSortField"
          @refresh="getIssues"
          :custom-fields="issueTemplate.customFields"
          ref="table"
        >
    <span v-for="(item) in fields" :key="item.key">
<!--          <ms-table-column
           :label="$t('test_track.issue.id')"
           prop="id"
           :field="item"
           :fields-width="fieldsWidth"
           v-if="false">
          </ms-table-column>-->
        <ms-table-column width="1">
        </ms-table-column>
          <ms-table-column
            :label="$t('test_track.issue.id')"
            prop="num"
            :field="item"
            sortable
            :fields-width="fieldsWidth">
          </ms-table-column>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('test_track.issue.title')"
            sortable
            prop="title">
          </ms-table-column>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            :filters="platformFilters"
            :label="$t('test_track.issue.platform')"
            prop="platform">
          </ms-table-column>

          <ms-table-column
                  :field="item"
                  :fields-width="fieldsWidth"
                  sortable
                  :label="$t('test_track.issue.platform_status') "
                  prop="platformStatus">
            <template v-slot="scope">
              {{ scope.row.platformStatus ? scope.row.platformStatus : '--'}}
            </template>
          </ms-table-column>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            column-key="creator"
            sortable
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
              <span>{{ scope.row.createTime | timestampFormatDate }}</span>
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
                           :field="item"
                           :fields-width="fieldsWidth"
                           :label="field.name"
                           :prop="field.name">
              <template v-slot="scope">
                <span v-if="field.name === '状态'">
                  {{getCustomFieldValue(scope.row, field) ? getCustomFieldValue(scope.row, field) : issueStatusMap[scope.row.status]}}
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

      </el-card>
    </ms-main-container>
  </ms-container>
</template>

<script>
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsTableOperators from "@/business/components/common/components/MsTableOperators";
import MsTableButton from "@/business/components/common/components/MsTableButton";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import {
  CUSTOM_FIELD_SCENE_OPTION,
  CUSTOM_FIELD_TYPE_OPTION,
  FIELD_TYPE_MAP, ISSUE_PLATFORM_OPTION,
  ISSUE_STATUS_MAP,
  SYSTEM_FIELD_NAME_MAP
} from "@/common/js/table-constants";
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import IssueDescriptionTableItem from "@/business/components/track/issue/IssueDescriptionTableItem";
import IssueEdit from "@/business/components/track/issue/IssueEdit";
import {getIssues, syncIssues} from "@/network/Issue";
import {
  getCustomFieldValue,
  getCustomTableWidth,
  getPageInfo, getTableHeaderWithCustomFields,saveLastTableSortField,getLastTableSortField
} from "@/common/js/tableUtils";
import MsContainer from "@/business/components/common/components/MsContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import {getCurrentProjectID} from "@/common/js/utils";
import {getIssueTemplate} from "@/network/custom-field-template";
import {getProjectMember} from "@/network/user";

export default {
  name: "IssueList",
  components: {
    MsMainContainer,
    MsContainer,
    IssueEdit,
    IssueDescriptionTableItem,
    MsTableHeader,
    MsTablePagination, MsTableButton, MsTableOperators, MsTableColumn, MsTable
  },
  data() {
    return {
      page: getPageInfo(),
      fields: [],
      tableHeaderKey:"ISSUE_LIST",
      fieldsWidth: getCustomTableWidth('ISSUE_LIST'),
      screenHeight: 'calc(100vh - 200px)',
      operators: [
        {
          tip: this.$t('commons.edit'), icon: "el-icon-edit",
          exec: this.handleEdit,
          isDisable: this.btnDisable,
          permissions: ['PROJECT_TRACK_ISSUE:READ+EDIT']
        }, {
          tip: this.$t('commons.copy'), icon: "el-icon-copy-document", type: "success",
          exec: this.handleCopy,
          isDisable: this.btnDisable
        }, {
          tip: this.$t('commons.delete'), icon: "el-icon-delete", type: "danger",
          exec: this.handleDelete,
          isDisable: this.btnDisable,
          permissions: ['PROJECT_TRACK_ISSUE:READ+DELETE']
        }
      ],
      issueTemplate: {},
      members: [],
      isThirdPart: false
    };
  },
  activated() {
    getProjectMember((data) => {
      this.members = data;
    });
    getIssueTemplate()
      .then((template) => {
        this.issueTemplate = template;
        if (this.issueTemplate.platform === 'metersphere') {
          this.isThirdPart = false;
        } else {
          this.isThirdPart = true;
        }
        this.fields = getTableHeaderWithCustomFields('ISSUE_LIST', this.issueTemplate.customFields);
        if (!this.isThirdPart) {
          for (let i = 0; i < this.fields.length; i++) {
            if (this.fields[i].id === 'platformStatus') {
              this.fields.splice(i, 1);
              break;
            }
          }
        }
        this.$refs.table.reloadTable();
      });
    this.getIssues();
  },
  computed: {
    fieldFilters() {
      return CUSTOM_FIELD_TYPE_OPTION;
    },
    platformFilters() {
     return ISSUE_PLATFORM_OPTION;
    },
    sceneFilters() {
      return CUSTOM_FIELD_SCENE_OPTION;
    },
    fieldTypeMap() {
      return FIELD_TYPE_MAP;
    },
    issueStatusMap() {
      return ISSUE_STATUS_MAP;
    },
    systemNameMap() {
      return SYSTEM_FIELD_NAME_MAP;
    },
    projectId() {
      return getCurrentProjectID();
    }
  },
  methods: {
    getCustomFieldValue(row, field) {
      return getCustomFieldValue(row, field, this.members);
    },
    getIssues() {
      this.page.condition.projectId = this.projectId;
      let orderArr = this.getSortField();
      if(orderArr){
        this.page.condition.orders = orderArr;
      }
      this.page.result = getIssues(this.page);
    },
    handleEdit(data) {
      this.$refs.issueEdit.open(data);
    },
    handleCreate() {
      this.$refs.issueEdit.open();
    },
    handleCopy(data) {
      let copyData = {};
      Object.assign(copyData, data);
      copyData.id = null;
      copyData.name = data.name + '_copy';
      this.$refs.issueEdit.open(copyData);
    },
    handleDelete(data) {
      this.page.result = this.$get('issues/delete/' + data.id, () => {
        this.$success(this.$t('commons.delete_success'));
        this.getIssues();
      });
    },
    btnDisable(row) {
      if (row.platform === 'Local') {
        return false;
      }
      return true;
    },
    saveSortField(key,orders){
      saveLastTableSortField(key,JSON.stringify(orders));
    },
    syncIssues() {
      this.page.result = syncIssues(() => {
        this.getIssues();
      });
    },
    getSortField(){
      let orderJsonStr = getLastTableSortField(this.tableHeaderKey);
      let returnObj = null;
      if(orderJsonStr){
        try {
          returnObj = JSON.parse(orderJsonStr);
        }catch (e){
          return null;
        }
      }
      return returnObj;
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
