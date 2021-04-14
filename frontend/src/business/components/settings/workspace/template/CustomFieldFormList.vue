<template>
  <ms-table
    v-loading="result.loading"
    :enable-selection="false"
    :operators="operators"
    :data="tableData"
    @refresh="refreshTable"
    ref="table">

    <ms-table-column
      :label="'名称'"
      prop="name">
    </ms-table-column>

    <ms-table-column
      :label="'默认值'"
      prop="type">
      <template v-slot="scope">
        <default-value-table-item class="default-value-item" :data="scope.row"/>
      </template>
    </ms-table-column>

    <field-custom-data-table-item :scene="scene"/>

    <ms-table-column
      :label="'是否必填'"
      prop="type">
      <template v-slot="scope">
        <el-checkbox v-model="scope.row.required"/>
      </template>
    </ms-table-column>

    <ms-table-column
      :label="'备注'"
      prop="remark">
    </ms-table-column>

  </ms-table>

</template>

<script>
import MsTableOperatorButton from "@/business/components/common/components/MsTableOperatorButton";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/Ms-table-column";
import DefaultValueTableItem from "@/business/components/settings/workspace/template/DefaultValueTableItem";
import FieldCustomDataTableItem from "@/business/components/settings/workspace/template/FieldCustomDataTableItem";
export default {
  name: "CustomFieldFormList",
  components: {FieldCustomDataTableItem, DefaultValueTableItem, MsTableColumn, MsTable, MsTableOperatorButton},
  data() {
    return {
      result: {},
     operators: [
       {
         tip: this.$t('commons.delete'), icon: "el-icon-delete", type: "danger",
         exec: this.handleDelete
       }
     ],
    }
  },
  props: {
    tableData: {
      type: Array,
      default() {
        return []
      },
    },
    scene: String
  },
  watch: {
    'customFieldIds.length'() {
      this.initTableData();
    }
  },
  methods: {
    handleDelete(item, index) {
      this.tableData.splice(index, 1);
    },
    refreshTable() {
    },
    appendData(customFieldIds) {
      let condition = {};
      condition.ids = customFieldIds;
      this.result = this.$post('custom/field/list',
        condition, (response) => {
          let data = response.data;
          data.forEach(item => {
            item.fieldId = item.id;
            item.id = null;
            item.options = JSON.parse(item.options);
          });
          this.tableData.push(...data);
        });
    }
  }
}
</script>

<style scoped>
.default-value-item >>> .custom-with {
  width: 207px;
}
</style>
