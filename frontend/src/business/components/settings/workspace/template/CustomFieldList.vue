<template>
  <el-card>

    <template v-slot:header>
      <ms-table-header :condition.sync="condition" @search="getCustomFields" @create="handleCreate"
                       :create-tip="$t('custom_field.create')" :title="$t('custom_field.name')"/>
    </template>

    <ms-table
      v-loading="result.loading"
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
          :label="$t('commons.name')"
          prop="name">
          <template v-slot="scope">
            <span v-if="scope.row.system">
              {{$t(systemNameMap[scope.row.name])}}
            </span>
            <span v-else>
              {{scope.row.name}}
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
            <span>{{ sceneMap[scope.row.scene] }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
          :field="item"
          :label="$t('custom_field.attribute_type')"
          :filters="fieldFilters"
          :fields-width="fieldsWidth"
          prop="type">
          <template v-slot="scope">
            <span>{{ fieldTypeMap[scope.row.type] }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
          :label="$t('custom_field.system_field')"
          :field="item"
          :fields-width="fieldsWidth"
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
          :label="$t('commons.remark')"
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
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
          sortable
          :label="$t('commons.update_time')"
          :field="item"
          :fields-width="fieldsWidth"
          prop="updateTime">
          <template v-slot="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </ms-table-column>

      </div>

    </ms-table>

    <ms-table-pagination :change="getCustomFields" :current-page.sync="currentPage" :page-size.sync="pageSize" :total="total"/>

    <custom-field-edit
      @refresh="getCustomFields"
      ref="customFieldEdit">

    </custom-field-edit>
  </el-card>
</template>

<script>
import MsTable from "@/business/components/common/components/table/MsTable";
import {getCurrentWorkspaceId, getDefaultTableHeight} from "@/common/js/utils";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsTableOperators from "@/business/components/common/components/MsTableOperators";
import MsTableButton from "@/business/components/common/components/MsTableButton";
import CustomFieldEdit from "@/business/components/settings/workspace/template/CustomFieldEdit";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import {
  CUSTOM_FIELD_SCENE_OPTION,
  CUSTOM_FIELD_TYPE_OPTION,
  FIELD_TYPE_MAP,
  SCENE_MAP, SYSTEM_FIELD_NAME_MAP
} from "@/common/js/table-constants";
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import HeaderCustom from "@/business/components/common/head/HeaderCustom";
import {getCustomTableHeader, getCustomTableWidth} from "@/common/js/tableUtils";
import MsCustomTableHeader from "@/business/components/common/components/table/MsCustomTableHeader";

export default {
  name: "CustomFieldList",
  components: {
    MsCustomTableHeader,
    HeaderCustom,
    MsTableHeader,
    MsTablePagination, CustomFieldEdit, MsTableButton, MsTableOperators, MsTableColumn, MsTable},
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
    };
  },
  activated() {
    this.getCustomFields();
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
    sceneMap() {
      return SCENE_MAP;
    },
    systemNameMap() {
      return SYSTEM_FIELD_NAME_MAP;
    },
    tableHeight() {
      return getDefaultTableHeight();
    }
  },
  methods: {
    getCustomFields() {
      this.condition.workspaceId = getCurrentWorkspaceId();
      this.result = this.$post('custom/field/list/' + this.currentPage + '/' + this.pageSize,
        this.condition, (response) => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
        this.$refs.table.reloadTable();
      });
    },
    handleEdit(data) {
      this.$refs.customFieldEdit.open(data);
    },
    handleCreate() {
      this.$refs.customFieldEdit.open();
    },
    handleCopy(data) {
      let copyData = {};
      Object.assign(copyData, data);
      copyData.id = null;
      copyData.name = data.name + '_copy';
      this.$refs.customFieldEdit.open(copyData);
    },
    handleDelete(data) {
      this.result = this.$get('custom/field/delete/' + data.id,  () => {
        this.$success(this.$t('commons.delete_success'));
        this.getCustomFields();
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
