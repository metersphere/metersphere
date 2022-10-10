<template>
  <el-card class="table-card">

    <template v-slot:header>
      <ms-table-header :condition.sync="condition" :create-tip="$t('custom_field.template_create')"
                       @create="handleCreate"
                       @search="initTableData"/>
    </template>

    <ms-table
      ref="table"
      v-loading="result.loading"
      :condition="condition"
      :data="tableData"
      :enable-selection="false"
      :operators="operators"
      :page-size.sync="pageSize"
      :screen-height="tableHeight"
      :total="total"
      @handlePageChange="initTableData"
      @refresh="initTableData">

      <ms-table-column
        :fields="fields"
        :label="$t('commons.name')"
        prop="name">
        <template v-slot="scope">
          <span v-if="scope.row.system">{{ scope.row.name }}({{ $t('custom_field.default_template') }})</span>
          <span v-else>{{ scope.row.name }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        :fields="fields"
        :label="$t('custom_field.system_template')"
        prop="system">
        <template v-slot="scope">
          <span v-if="scope.row.system">
            {{ $t('commons.yes') }}
          </span>
          <span v-else>
            {{ $t('commons.no') }}
          </span>
        </template>
      </ms-table-column>

      <ms-table-column
        :fields="fields"
        :label="$t('commons.description')"
        prop="description">
      </ms-table-column>

      <ms-table-column
        :fields="fields"
        :label="$t('commons.create_time')"
        prop="createTime"
        sortable>
        <template v-slot="scope">
          <span>{{ scope.row.createTime | datetimeFormat }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        :fields="fields"
        :label="$t('commons.update_time')"
        prop="updateTime"
        sortable>
        <template v-slot="scope">
          <span>{{ scope.row.updateTime | datetimeFormat }}</span>
        </template>
      </ms-table-column>
    </ms-table>

    <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>

    <api-field-template-edit ref="templateEdit" @refresh="initTableData"/>
    <ms-delete-confirm ref="deleteConfirm" :title="$t('commons.template_delete')" @delete="_handleDelete"/>
  </el-card>
</template>

<script>
import {CUSTOM_FIELD_LIST} from "metersphere-frontend/src/utils/default-table-header";
import {CASE_TYPE_OPTION} from "metersphere-frontend/src/utils/table-constants";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsTableButton from "metersphere-frontend/src/components/MsTableButton";
import MsTableOperators from "metersphere-frontend/src/components/MsTableOperators";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import ApiFieldTemplateEdit from "./ApiFieldTemplateEdit";
import MsDeleteConfirm from "metersphere-frontend/src/components/MsDeleteConfirm";
import {deleteApiFieldTemplateById, getApiFieldTemplatePages} from "../../../api/template";

export default {
  name: "ApiTemplateList",
  components: {
    MsTableHeader,
    MsTablePagination,
    MsTableButton,
    MsTableOperators,
    MsTableColumn,
    MsTable,
    ApiFieldTemplateEdit,
    MsDeleteConfirm
  },
  data() {
    return {
      tableData: [],
      condition: {},
      total: 0,
      pageSize: 10,
      currentPage: 1,
      result: {},
      caseTypeMap: {
        functional: this.$t('api_test.home_page.failed_case_list.table_value.case_type.functional')
      },
      operators: [
        {
          tip: this.$t('commons.edit'), icon: "el-icon-edit",
          exec: this.handleEdit
        }, {
          tip: this.$t('commons.copy'), icon: "el-icon-copy-document", type: "success",
          exec: this.handleCopy,
          isDisable: this.systemDisable
        }, {
          tip: this.$t('commons.delete'), icon: "el-icon-delete", type: "danger",
          exec: this.handleDelete,
          isDisable: this.systemDisable
        }
      ],
    };
  },
  created() {
    this.initTableData();
  },
  activated() {
    this.initTableData();
  },
  computed: {
    fields() {
      return CUSTOM_FIELD_LIST;
    },
    caseTypeFilters() {
      return new CASE_TYPE_OPTION();
    },
    tableHeight() {
      return document.documentElement.clientHeight - 200;
    }
  },
  methods: {
    initTableData() {
      this.condition.projectId = getCurrentProjectID();
      this.loading = getApiFieldTemplatePages(this.currentPage, this.pageSize, this.condition)
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
      this.$refs.templateEdit.openEdit(data);
    },
    handleCreate() {
      this.$refs.templateEdit.openEdit();
    },
    handleCopy(data) {
      let copyData = {};
      Object.assign(copyData, data);
      copyData.name = data.name + '_copy';
      this.$refs.templateEdit.openEdit(copyData, true);
    },
    handleDelete(data) {
      this.$refs.deleteConfirm.open(data);
    },
    _handleDelete(data) {
      this.loading = deleteApiFieldTemplateById(data.id).then(() => {
        this.$success(this.$t('commons.delete_success'));
        this.initTableData();
      });
    },
    systemDisable(row) {
      if (row.system) {
        return true;
      }
      return false;
    }
  }
};
</script>

<style scoped>
</style>
