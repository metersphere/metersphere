<template>
  <ms-edit-dialog
    appendToBody
    width="70%"
    :visible.sync="visible"
    @confirm="save"
    :title="$t('custom_field.add_field')"
    ref="msEditDialog">

    <template v-slot:header>
      <ms-table-header
        :condition.sync="condition"
        @search="search"
        :show-create="false"/>
    </template>

    <ms-table
      v-loading="loading"
      :data="tableData"
      :condition="condition"
      :total="total"
      :show-select-all="false"
      :page-size.sync="pageSize"
      @handlePageChange="initTableData"
      @refresh="initTableData"
      ref="table">

      <ms-table-column
        :label="$t('commons.name')"
        :fields="fields"
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
        :label="$t('custom_field.attribute_type')"
        :fields="fields"
        :filters="fieldFilters"
        prop="type">
        <template v-slot="scope">
          <span>{{ $t(fieldTypeMap[scope.row.type]) }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        :label="$t('custom_field.system_field')"
        :fields="fields"
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
        :label="$t('commons.remark')"
        :fields="fields"
        prop="remark">
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

  </ms-edit-dialog>
</template>

<script>
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import {CUSTOM_FIELD_LIST} from "metersphere-frontend/src/utils/default-table-header";
import MsTableButton from "metersphere-frontend/src/components/MsTableButton";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import {
  CUSTOM_FIELD_TYPE_FILTERS,
  FIELD_TYPE_MAP,
  SYSTEM_FIELD_NAME_MAP
} from "metersphere-frontend/src/utils/table-constants";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsEditDialog from "../common/MsEditDialog";
import {getCustomFieldRelatePages} from "../../../api/template";

export default {
  name: "CustomFieldRelateList",
  components: {
    MsEditDialog,
    MsTableHeader,
    MsTablePagination, MsTableButton, MsTableColumn, MsTable
  },
  data() {
    return {
      tableData: [],
      condition: {},
      visible: false,
      total: 0,
      pageSize: 10,
      currentPage: 1,
      result: {},
      loading: false,
    };
  },
  props: [
    'scene',
    'templateId',
    'templateContainIds',
  ],
  computed: {
    fields() {
      return CUSTOM_FIELD_LIST;
    },
    fieldFilters() {
      return CUSTOM_FIELD_TYPE_FILTERS(this);
    },
    fieldTypeMap() {
      return FIELD_TYPE_MAP;
    },
    systemNameMap() {
      return SYSTEM_FIELD_NAME_MAP;
    }
  },
  methods: {
    search() {
      if (!(this.condition.name.trim())) {
        this.currentPage = 1;
      }
      this.initTableData();
    },
    initTableData() {
      this.condition.projectId = getCurrentProjectID();
      this.condition.templateId = this.templateId;
      this.condition.templateContainIds = Array.from(this.templateContainIds);
      let filters = this.condition.filters;
      if (filters) {
        filters.scene = [this.scene];
      } else {
        this.condition.filters = {scene: [this.scene]};
      }
      if (this.scene) {
        this.loading = getCustomFieldRelatePages(this.currentPage, this.pageSize, this.condition).then((response) => {
          let {itemCount, listObject} = response.data;
          this.total = itemCount;
          this.tableData = listObject;
        });
      }
    },
    open() {
      this.initTableData();
      this.visible = true;
    },
    save() {
      if (this.$refs.table.selectIds.length > 0) {
        this.$emit('save', this.$refs.table.selectIds);
      }
      this.visible = false;
    },
  }
};
</script>

<style scoped>

</style>
