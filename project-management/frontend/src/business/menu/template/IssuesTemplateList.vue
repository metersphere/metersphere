<template>
  <el-card class="table-card">

    <template v-slot:header>
      <ms-table-header
        :condition.sync="condition"
        @search="initTableData"
        @create="handleCreate"
        :create-tip="$t('custom_field.create_issue_template')"/>
    </template>

    <ms-table
      v-loading="loading"
      :data="tableData"
      :condition="condition"
      :total="total"
      :page-size.sync="pageSize"
      :operators="operators"
      :screen-height="tableHeight"
      :enable-selection="false"
      @handlePageChange="initTableData"
      @refresh="initTableData"
      ref="table">

      <ms-table-column
        :label="$t('commons.name')"
        :fields="fields"
        prop="name">
        <template v-slot="scope">
          <span v-if="scope.row.system">{{ scope.row.name }}(默认模板)</span>
          <span v-else>{{ scope.row.name }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        :label="$t('custom_field.issue_platform')"
        :fields="fields"
        :filters="platformFilters"
        prop="platform">
        <template v-slot="scope">
          <span>{{ issuePlatformMap[scope.row.platform] }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        :label="$t('commons.description')"
        :fields="fields"
        prop="description">
      </ms-table-column>

      <ms-table-column
        sortable
        :label="$t('commons.create_time')"
        :fields="fields"
        prop="createTime">
        <template v-slot="scope">
          <span>{{ scope.row.createTime | datetimeFormat }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        sortable
        :label="$t('commons.update_time')"
        :fields="fields"
        prop="updateTime">
        <template v-slot="scope">
          <span>{{ scope.row.updateTime | datetimeFormat }}</span>
        </template>
      </ms-table-column>
    </ms-table>

    <ms-table-pagination
      :change="initTableData"
      :current-page.sync="currentPage"
      :page-size.sync="pageSize"
      :total="total"/>

    <issue-template-copy ref="templateCopy" @refresh="initTableData"/>
    <issue-template-edit :platform-option="platformFilters" ref="templateEdit" @refresh="initTableData"/>
    <ms-delete-confirm :title="$t('commons.template_delete')" @delete="_handleDelete" ref="deleteConfirm"/>
  </el-card>
</template>

<script>
import {ISSUE_TEMPLATE_LIST} from "metersphere-frontend/src/utils/default-table-header";
import {ISSUE_PLATFORM_OPTION} from "metersphere-frontend/src/utils/table-constants";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsTableButton from "metersphere-frontend/src/components/MsTableButton";
import MsTableOperators from "metersphere-frontend/src/components/MsTableOperators";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import IssueTemplateEdit from "./IssueTemplateEdit";
import {deleteIssueFieldTemplateById, getIssueFieldTemplatePages} from "../../../api/template";
import MsDeleteConfirm from "metersphere-frontend/src/components/MsDeleteConfirm";
import IssueTemplateCopy from "./IssueTemplateCopy";
import {getPlatformOption} from "@/api/platform-plugin";

export default {
  name: "IssuesTemplateList",
  components: {
    IssueTemplateEdit,
    IssueTemplateCopy,
    MsTableHeader,
    MsTablePagination, MsTableButton, MsTableOperators, MsTableColumn, MsTable, MsDeleteConfirm
  },
  data() {
    return {
      tableData: [],
      condition: {},
      total: 0,
      pageSize: 10,
      currentPage: 1,
      result: {},
      loading: false,
      platformOptions: [],
      issuePlatformMap: {
        Local: 'Metersphere',
        Tapd: 'Tapd',
        Zentao: '禅道',
        AzureDevops: 'Azure Devops',
      },
      operators: [
        {
          tip: this.$t('commons.edit'), icon: "el-icon-edit",
          exec: this.handleEdit
        }, {
          tip: this.$t('commons.copy'), icon: "el-icon-copy-document", type: "success",
          exec: this.handleCopy
        }, {
          tip: this.$t('commons.delete'), icon: "el-icon-delete", type: "danger",
          exec: this.handleDelete
        }
      ],
    };
  },
  created() {
    getPlatformOption()
      .then((r) => {
        this.platformOptions = r.data;
      });
    this.initTableData();
  },
  computed: {
    fields() {
      return ISSUE_TEMPLATE_LIST;
    },
    platformFilters() {
      this.platformOptions.forEach(item => {
        this.issuePlatformMap[item.value] = item.text;
      });
      let options = [...ISSUE_PLATFORM_OPTION];
      options.push(...this.platformOptions);
      return options;
    },
    tableHeight() {
      return document.documentElement.clientHeight - 200;
    }
  },
  methods: {
    initTableData() {
      this.condition.projectId = getCurrentProjectID();
      this.loading = getIssueFieldTemplatePages(this.currentPage, this.pageSize, this.condition)
        .then((response) => {
          let {itemCount, listObject} = response.data;
          this.total = itemCount;
          this.tableData = listObject;
          if (this.$refs.table) {
            this.$refs.table.reloadTable();
          }
        });
    },
    handleEdit(data) {
      this.$refs.templateEdit.open(data);
    },
    handleCreate() {
      this.$refs.templateEdit.open();
    },
    handleCopy(data) {
      let copyData = {};
      Object.assign(copyData, data);
      this.$refs.templateCopy.open(copyData);
    },
    handleDelete(data) {
      this.$refs.deleteConfirm.open(data);
    },
    _handleDelete(data) {
      this.loading = deleteIssueFieldTemplateById(data.id).then(() => {
        this.$success(this.$t('commons.delete_success'));
        this.initTableData();
      });
    },
    systemDisable(row) {
      return !!row.system;
    }
  }
};
</script>

<style scoped>
</style>
