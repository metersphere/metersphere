<template>
  <el-main>
    <el-card>

      <template v-slot:header>
        <ms-table-header :is-tester-permission="true" :condition.sync="condition" @search="getIssues" @create="handleCreate"
                         :create-tip="$t('test_track.issue.create_issue')" :title="$t('test_track.issue.issue_list')"  :tip="$t('issue.search_name')" :have-search="false"/>
      </template>

      <ms-table
        v-loading="result.loading"
        :data="tableData"
        :condition="condition"
        :total="total"
        :page-size.sync="pageSize"
        :operators="operators"
        :show-select-all="false"
        @handlePageChange="getIssues"
        @refresh="getIssues">

        <ms-table-column
          :label="$t('test_track.issue.id')"
          prop="id">
        </ms-table-column>

        <ms-table-column
          :label="$t('test_track.issue.title')"
          prop="title">
        </ms-table-column>

        <ms-table-column
          :label="$t('test_track.issue.status')"
          prop="status">
          <template v-slot="scope">
            <span>{{ issueStatusMap[scope.row.status] ? issueStatusMap[scope.row.status] : scope.row.status }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
          :label="$t('test_track.issue.platform')"
          prop="platform">
        </ms-table-column>

        <ms-table-column
          :label="$t('custom_field.issue_creator')"
          prop="creatorName">
        </ms-table-column>

        <issue-description-table-item/>

      </ms-table>

      <ms-table-pagination :change="getIssues" :current-page.sync="currentPage" :page-size.sync="pageSize" :total="total"/>

      <issue-edit @refresh="getIssues" ref="issueEdit"/>

    </el-card>
  </el-main>

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
  FIELD_TYPE_MAP, ISSUE_STATUS_MAP,
  SYSTEM_FIELD_NAME_MAP
} from "@/common/js/table-constants";
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import IssueDescriptionTableItem from "@/business/components/track/issue/IssueDescriptionTableItem";
import IssueEdit from "@/business/components/track/issue/IssueEdit";
export default {
  name: "CustomFieldList",
  components: {
    IssueEdit,
    IssueDescriptionTableItem,
    MsTableHeader,
    MsTablePagination, MsTableButton, MsTableOperators, MsTableColumn, MsTable},
  data() {
    return {
      tableData: [],
      condition: {},
      total: 0,
      pageSize: 10,
      currentPage: 1,
      result: {},
      operators: [
        {
          tip: this.$t('commons.edit'), icon: "el-icon-edit",
          exec: this.handleEdit,
          isDisable: this.btnDisable
        }, {
          tip: this.$t('commons.copy'), icon: "el-icon-copy-document", type: "success",
          exec: this.handleCopy,
          isDisable: this.btnDisable
        }, {
          tip: this.$t('commons.delete'), icon: "el-icon-delete", type: "danger",
          exec: this.handleDelete,
          isDisable: this.btnDisable
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
      return this.$store.state.projectId;
    }
  },
  methods: {
    getIssues() {
      this.condition.projectId = this.projectId;
      this.result = this.$post('issues/list/' + this.currentPage + '/' + this.pageSize,
        this.condition, (response) => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
        for (let i = 0; i < this.tableData.length; i++) {
          if (this.tableData[i]) {
            if (this.tableData[i].platform !== 'Local') {
              this.$post("issues/get/platform/issue", this.tableData[i]).then(response => {
                let issues = response.data.data;
                if (issues) {
                  this.$set(this.tableData[i], "title", issues.title ? issues.title : "--");
                  this.$set(this.tableData[i], "description", issues.description ? issues.description : "--");
                  this.$set(this.tableData[i], "status", issues.status ? issues.status : 'delete');
                }
              }).catch(() => {
                this.$set(this.tableData[i], "title", "--");
                this.$set(this.tableData[i], "description", "--");
                this.$set(this.tableData[i], "status", "--");
              });
            }
          }
        }
      });
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
      this.result = this.$get('issues/delete/' + data.id,  () => {
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

</style>
