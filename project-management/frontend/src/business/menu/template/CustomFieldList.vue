<template>
  <el-card class="table-card">

    <template v-slot:header>
      <ms-table-header :condition.sync="condition" @search="getCustomFields" @create="handleCreate"
                       :create-tip="$t('custom_field.create')"/>
    </template>

    <ms-table
      v-loading="loading"
      :data="tableData"
      :condition="condition"
      :total="total"
      :page-size.sync="pageSize"
      :operators="operators"
      :fields.sync="fields"
      :enable-selection="false"
      field-key="CUSTOM_FIELD"
      :screen-height="tableHeight"
      @handlePageChange="getCustomFields"
      @refresh="getCustomFields"
      ref="table">

      <div v-for="(item) in fields" :key="item.key">

        <ms-table-column
          :field="item"
          :fields-width="fieldsWidth"
          :label="$t('custom_field.field_name')"
          prop="name">
          <template v-slot="scope">
            <span v-if="scope.row.system">
              {{ $t(systemNameMap[scope.row.name]) }}
            </span>
            <span v-else>
              {{ scope.row.name }}
            </span>
          </template>
        </ms-table-column>

        <ms-table-column
          :label="$t('custom_field.scene')"
          :field="item"
          :filters="sceneFilters"
          :fields-width="fieldsWidth"
          prop="scene">
          <template v-slot="scope">
            <span>{{ $t(sceneMap[scope.row.scene]) }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
          :field="item"
          :label="$t('custom_field.field_type')"
          :filters="fieldFilters"
          :fields-width="fieldsWidth"
          prop="type">
          <template v-slot="scope">
            <span>{{ $t(fieldTypeMap[scope.row.type]) }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
          :label="$t('custom_field.system_field')"
          :field="item"
          :fields-width="fieldsWidth"
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
          :label="$t('custom_field.field_remark')"
          :field="item"
          :fields-width="fieldsWidth"
          prop="remark">
        </ms-table-column>

        <ms-table-column
          sortable
          :label="$t('commons.create_time')"
          :field="item"
          :fields-width="fieldsWidth"
          prop="createTime">
          <template v-slot="scope">
            <span>{{ scope.row.createTime | datetimeFormat }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
          sortable
          :label="$t('commons.update_time')"
          :field="item"
          :fields-width="fieldsWidth"
          prop="updateTime">
          <template v-slot="scope">
            <span>{{ scope.row.updateTime | datetimeFormat }}</span>
          </template>
        </ms-table-column>

      </div>

    </ms-table>

    <ms-table-pagination :change="getCustomFields" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>

    <custom-field-edit
      @refresh="getCustomFields"
      ref="customFieldEdit">
    </custom-field-edit>

    <ms-delete-confirm :title="$t('pj_custom_field.delete')" @delete="_handleDelete" ref="deleteConfirm" :with-tip="true">
      <i style="font-size: 13px">{{ $t('pj_custom_field.delete_tips') }}</i>
    </ms-delete-confirm>

  </el-card>
</template>

<script>
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {getTranslateOptions} from "metersphere-frontend/src/utils";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsTableOperators from "metersphere-frontend/src/components/MsTableOperators";
import MsTableButton from "metersphere-frontend/src/components/MsTableButton";
import CustomFieldEdit from "./CustomFieldEdit";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import {
  CUSTOM_FIELD_SCENE_OPTION,
  CUSTOM_FIELD_TYPE_OPTION,
  FIELD_TYPE_MAP,
  SCENE_MAP,
  SYSTEM_FIELD_NAME_MAP
} from "metersphere-frontend/src/utils/table-constants";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import HeaderCustom from "metersphere-frontend/src/components/head/HeaderCustom";
import {getCustomTableHeader, getCustomTableWidth} from "metersphere-frontend/src/utils/tableUtils";
import MsCustomTableHeader from "metersphere-frontend/src/components/table/MsCustomTableHeader";
import {deleteCustomField, getCustomFieldPages} from "../../../api/custom-field";
import MsDeleteConfirm from "metersphere-frontend/src/components/MsDeleteConfirm";

export default {
  name: "CustomFieldList",
  components: {
    MsCustomTableHeader,
    HeaderCustom,
    MsTableHeader,
    MsTablePagination,
    CustomFieldEdit,
    MsTableButton,
    MsTableOperators,
    MsTableColumn,
    MsTable,
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
      fields: getCustomTableHeader('CUSTOM_FIELD'),
      fieldsWidth: getCustomTableWidth('CUSTOM_FIELD'),
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
      fieldFilters: [],
      sceneFilters: [],
      loading: false,
    };
  },
  created() {
    this.getCustomFields();
    this.fieldFilters = getTranslateOptions(CUSTOM_FIELD_TYPE_OPTION);
    this.sceneFilters = getTranslateOptions(CUSTOM_FIELD_SCENE_OPTION);
  },
  computed: {
    fieldTypeMap() {
      return FIELD_TYPE_MAP;
    },
    sceneMap() {
      return SCENE_MAP;
    },
    systemNameMap() {
      return SYSTEM_FIELD_NAME_MAP;
    },
    tableHeight() {
      return document.documentElement.clientHeight - 200;
    }
  },
  methods: {
    getCustomFields() {
      this.condition.projectId = getCurrentProjectID();
      this.loading = getCustomFieldPages(this.currentPage, this.pageSize, this.condition).then(res => {
        let {itemCount, listObject} = res.data;
        this.total = itemCount;
        this.tableData = listObject;
      });
    },
    handleEdit(data) {
      this.$refs.customFieldEdit.open(data, this.$t('custom_field.update'));
    },
    handleCreate() {
      this.$refs.customFieldEdit.open(null, this.$t('custom_field.create'));
    },
    handleCopy(data) {
      let copyData = {};
      Object.assign(copyData, data);
      copyData.id = null;
      copyData.name = data.name + '_copy';
      this.$refs.customFieldEdit.open(copyData, this.$t('pj_custom_field.copy'));
    },
    handleDelete(data) {
      this.$refs.deleteConfirm.open(data);
    },
    _handleDelete(data) {
      this.loading = deleteCustomField(data.id).then(() => {
        this.$success(this.$t('commons.delete_success'));
        this.getCustomFields();
      })
    },
    systemDisable(row) {
      return !!row.system;
    }
  }
};
</script>

<style scoped>
</style>
