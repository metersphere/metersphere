<template>
  <el-card>

    <template v-slot:header>
      <ms-table-header :condition.sync="condition" @search="initTableData" @create="handleCreate"
                       :create-tip="$t('custom_field.template_create')" :title="$t('custom_field.case_template')"/>
    </template>

    <ms-table
      v-loading="result.loading"
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
          <span v-if="scope.row.system">{{ scope.row.name }}({{ $t('custom_field.default_template') }})</span>
          <span v-else>{{ scope.row.name }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        :label="$t('api_test.home_page.failed_case_list.table_coloum.case_type')"
        :fields="fields"
        :filters="caseTypeFilters"
        prop="type">
        <template v-slot="scope">
          <span>{{ caseTypeMap[scope.row.type] }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        :label="$t('custom_field.system_template')"
        :fields="fields"
        prop="system">
        <template v-slot="scope">
          <span v-if="scope.row.system">
            {{$t('commons.yes')}}
          </span>
          <span v-else>
            {{$t('commons.no')}}
          </span>
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
          <span>{{ scope.row.createTime | timestampFormatDate }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        sortable
        :label="$t('commons.update_time')"
        :fields="fields"
        prop="updateTime">
        <template v-slot="scope">
          <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
        </template>
      </ms-table-column>
    </ms-table>

    <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize" :total="total"/>

    <test-case-template-edit ref="templateEdit" @refresh="initTableData"/>

  </el-card>
</template>

<script>
import {CUSTOM_FIELD_LIST} from "@/common/js/default-table-header";
import {CASE_TYPE_OPTION} from "@/common/js/table-constants";
import {getCurrentWorkspaceId, getDefaultTableHeight} from "@/common/js/utils";
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import MsTableButton from "@/business/components/common/components/MsTableButton";
import MsTableOperators from "@/business/components/common/components/MsTableOperators";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsTable from "@/business/components/common/components/table/MsTable";
import TestCaseReportTemplate from "@/business/components/settings/workspace/template/TestCaseReportTemplate";
import TestCaseTemplateEdit from "@/business/components/settings/workspace/template/TestCaseTemplateEdit";

export default {
  name: "TestCaseTemplateList",
  components: {
    TestCaseTemplateEdit,
    TestCaseReportTemplate,
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
      caseTypeMap:{
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
  activated() {
    this.initTableData();
  },
  computed: {
    fields() {
      return CUSTOM_FIELD_LIST;
    },
    caseTypeFilters() {
      return CASE_TYPE_OPTION;
    },
    tableHeight() {
      return getDefaultTableHeight();
    }
  },
  methods: {
    initTableData() {
      this.condition.workspaceId = getCurrentWorkspaceId();
      this.result = this.$post('field/template/case/list/' + this.currentPage + '/' + this.pageSize,
        this.condition, (response) => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
          this.$refs.table.reloadTable();
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
      copyData.name = data.name + '_copy';
      this.$refs.templateEdit.open(copyData, true);
    },
    handleDelete(data) {
      this.result = this.$get('field/template/case/delete/' + data.id,  () => {
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
