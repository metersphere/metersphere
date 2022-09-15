<template>
  <ms-table
      ref="table"
      v-loading="result.loading"
      :data="tableData"
      :enable-selection="false"
      :operators="operators"
      :screen-height="null"
      @refresh="refreshTable">

    <ms-table-column
        :label="$t('commons.name')"
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
        :label="$t('commons.default')"
        :show-overflow-tooltip="false"
        min-width="200" prop="type">
      <template v-slot="scope">
        <el-scrollbar>
          <custom-filed-component :data="scope.row" :is-template-edit="true" class="default-value-item"
                                  prop="defaultValue"/>
        </el-scrollbar>
      </template>
    </ms-table-column>

    <field-custom-data-table-item :scene="scene"/>

    <ms-table-column
        :label="$t('api_test.definition.document.table_coloum.is_required')"
        prop="type"
        width="80">
      <template v-slot="scope">
        <el-checkbox v-model="scope.row.required"/>
      </template>
    </ms-table-column>

    <ms-table-column
        :label="$t('custom_field.system_field')"
        prop="system"
        width="80">
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
        :label="$t('commons.remark')"
        prop="remark">
    </ms-table-column>

  </ms-table>

</template>

<script>
import MsTableOperatorButton from "@/business/components/common/components/MsTableOperatorButton";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import FieldCustomDataTableItem from "@/business/components/project/template/FieldCustomDataTableItem";
import CustomFiledComponent from "@/business/components/project/template/CustomFiledComponent";
import {SYSTEM_FIELD_NAME_MAP} from "@/common/js/table-constants";

export default {
  name: "CustomFieldFormList",
  components: {
    CustomFiledComponent,
    FieldCustomDataTableItem, MsTableColumn, MsTable, MsTableOperatorButton
  },
  data() {
    return {
      result: {},
      operators: [
        {
          tip: this.$t('commons.delete'), icon: "el-icon-delete", type: "danger",
          exec: this.handleDelete,
          isDisable: (row) => {
            if (row.name === '用例等级') {
              return true;
            }
            return false;
          }
        }
      ],
    };
  },
  props: {
    tableData: {
      type: Array,
      default() {
        return [];
      },
    },
    scene: String,
    platform: String,
    templateContainIds: Set
  },
  watch: {
    'customFieldIds.length'() {
      this.initTableData();
    }
  },
  computed: {
    systemNameMap() {
      return SYSTEM_FIELD_NAME_MAP;
    }
  },
  methods: {
    handleDelete(item, index) {
      this.templateContainIds.delete(item.fieldId);
      this.tableData.splice(index, 1);
    },
    refreshTable() {
      this.$refs.table.reloadTable();
    },
    appendData(customFieldIds) {
      let condition = {};
      condition.ids = customFieldIds;
      this.result = this.$post('custom/field/list',
          condition, (response) => {
            let data = response.data;
            data.forEach(item => {
              if (item.id) {
                this.templateContainIds.add(item.id);
              }
              item.fieldId = item.id;
              item.id = null;
              item.options = JSON.parse(item.options);
              if (item.type === 'checkbox') {
                item.defaultValue = [];
              }
            });
            this.tableData.push(...data);
          });
    }
  }
};
</script>

<style scoped>

/deep/ .el-table--border, .el-table--group {
  border: 0px;
}
</style>
