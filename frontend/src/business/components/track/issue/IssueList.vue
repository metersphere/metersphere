<template>
  <ms-container>
    <ms-main-container>
      <el-card class="table-card">
        <template v-slot:header>
          <ms-table-header :create-permission="['PROJECT_TRACK_ISSUE:READ+CREATE']" :condition.sync="page.condition" @search="getIssues" @create="handleCreate"
                           :create-tip="$t('test_track.issue.create_issue')" :title="$t('test_track.issue.issue_list')"
                           :tip="$t('issue.search_name')" :have-search="false"/>
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
          field-key="ISSUE_LIST"
          @refresh="getIssues"
        >
    <span v-for="(item) in fields" :key="item.key">
<!--          <ms-table-column
           :label="$t('test_track.issue.id')"
           prop="id"
           :field="item"
           :fields-width="fieldsWidth"
           v-if="false">
          </ms-table-column>-->
          <ms-table-column
            :label="$t('test_track.issue.id')"
            prop="num"
            :field="item"
            :fields-width="fieldsWidth">
          </ms-table-column>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('test_track.issue.title')"
            prop="title">
          </ms-table-column>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('test_track.issue.status')"
            prop="status">
            <template v-slot="scope">
              <span>{{ issueStatusMap[scope.row.status] ? issueStatusMap[scope.row.status] : scope.row.status }}</span>
            </template>
          </ms-table-column>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            :label="$t('test_track.issue.platform')"
            prop="platform">
          </ms-table-column>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
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

          <issue-description-table-item :fields-width="fieldsWidth" :field="item"/>
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
import MsTableColumn from "@/business/components/common/components/table/Ms-table-column";
import MsTableOperators from "@/business/components/common/components/MsTableOperators";
import MsTableButton from "@/business/components/common/components/MsTableButton";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import {
  CUSTOM_FIELD_SCENE_OPTION,
  CUSTOM_FIELD_TYPE_OPTION,
  FIELD_TYPE_MAP,
  ISSUE_STATUS_MAP,
  SYSTEM_FIELD_NAME_MAP
} from "@/common/js/table-constants";
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import IssueDescriptionTableItem from "@/business/components/track/issue/IssueDescriptionTableItem";
import IssueEdit from "@/business/components/track/issue/IssueEdit";
import {getIssues} from "@/network/Issue";
import {getCustomTableHeader, getCustomTableWidth, getPageInfo} from "@/common/js/tableUtils";
import MsContainer from "@/business/components/common/components/MsContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import {getCurrentProjectID} from "@/common/js/utils";

export default {
  name: "CustomFieldList",
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
      fields: getCustomTableHeader('ISSUE_LIST'),
      fieldsWidth: getCustomTableWidth('ISSUE_LIST'),
      screenHeight: 'calc(100vh - 290px)',
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
    };
  },
  activated() {
    this.getIssues();
  },
  computed: {
    fieldFilters() {
      return CUSTOM_FIELD_TYPE_OPTION;
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
    getIssues() {
      this.page.condition.projectId = this.projectId;
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
