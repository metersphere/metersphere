<template>
  <ms-edit-dialog
    appendToBody
    width="70%"
    :visible.sync="visible"
    @confirm="save"
    :title="$t('custom_field.add_field')"
    ref="msEditDialog">

      <template v-slot:header>
        <ms-table-header :condition.sync="condition" @search="initTableData"
                         :show-create="false"/>
      </template>

      <ms-table
        v-loading="result.loading"
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
                {{$t(systemNameMap[scope.row.name])}}
              </span>
                    <span v-else>
                {{scope.row.name}}
              </span>
            </template>
          </ms-table-column>

          <ms-table-column
            :label="$t('custom_field.attribute_type')"
            :fields="fields"
            :filters="fieldFilters"
            prop="type">
            <template v-slot="scope">
              <span>{{ fieldTypeMap[scope.row.type] }}</span>
            </template>
          </ms-table-column>

          <ms-table-column
          :label="$t('custom_field.system_field')"
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

  </ms-edit-dialog>
</template>

<script>
import MsTable from "@/business/components/common/components/table/MsTable";
import {getCurrentWorkspaceId} from "@/common/js/utils";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import {CUSTOM_FIELD_LIST} from "@/common/js/default-table-header";
import MsTableButton from "@/business/components/common/components/MsTableButton";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import {CUSTOM_FIELD_TYPE_OPTION, FIELD_TYPE_MAP, SCENE_MAP, SYSTEM_FIELD_NAME_MAP} from "@/common/js/table-constants";
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import MsEditDialog from "@/business/components/common/components/MsEditDialog";
export default {
  name: "CustomFieldRelateList",
  components: {
    MsEditDialog,
    MsTableHeader,
    MsTablePagination, MsTableButton, MsTableColumn, MsTable},
  data() {
    return {
      tableData: [],
      condition: {},
      visible: false,
      total: 0,
      pageSize: 10,
      currentPage: 1,
      result: {},
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
      return CUSTOM_FIELD_TYPE_OPTION;
    },
    fieldTypeMap() {
      return FIELD_TYPE_MAP;
    },
    sceneMap() {
      return SCENE_MAP;
    },
    systemNameMap() {
      return SYSTEM_FIELD_NAME_MAP;
    }
  },
  methods: {
    initTableData() {
      this.condition.workspaceId = getCurrentWorkspaceId();
      this.condition.templateId = this.templateId;
      this.condition.templateContainIds = Array.from(this.templateContainIds);
      let filters = this.condition.filters;
      if (filters) {
        filters.scene = [this.scene];
      } else {
        this.condition.filters = {scene: [this.scene]};
      }
      if (this.scene) {
        this.result = this.$post('custom/field/list/relate/' + this.currentPage + '/' + this.pageSize,
          this.condition, (response) => {
            let data = response.data;
            this.total = data.itemCount;
            this.tableData = data.listObject;
            this.$refs.table.reloadTable();
          });
      }
    },
    open() {
      this.initTableData();
      this.visible = true;
    },
    save() {
      this.$emit('save', this.$refs.table.selectIds);
      this.visible = false;
    },
  }
};
</script>

<style scoped>

</style>
