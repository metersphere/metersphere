<template>
  <ms-edit-dialog
    appendToBody
    width="70%"
    :visible.sync="visible"
    @confirm="save"
    :title="'添加字段'"
    ref="msEditDialog">

      <template v-slot:header>
        <ms-table-header :is-tester-permission="true" :condition.sync="condition" @search="initTableData"
                         :show-create="false"/>
      </template>

      <ms-table
        v-loading="result.loading"
        :data="tableData"
        :condition="condition"
        :total="total"
        :page-size.sync="pageSize"
        @handlePageChange="initTableData"
        @refresh="initTableData"
        ref="table">

          <ms-table-column
            :label="'名称'"
            :fields="fields"
            prop="name">
          </ms-table-column>

          <ms-table-column
            :label="'属性类型'"
            :fields="fields"
            :filters="fieldFilters"
            prop="type">
            <template v-slot="scope">
              <span>{{ fieldTypeMap[scope.row.type] }}</span>
            </template>
          </ms-table-column>

          <ms-table-column
            :label="'备注'"
            :fields="fields"
            prop="remark">
          </ms-table-column>

          <ms-table-column
            sortable
            :label="'创建时间'"
            :fields="fields"
            prop="createTime">
            <template v-slot="scope">
              <span>{{ scope.row.createTime | timestampFormatDate }}</span>
            </template>
          </ms-table-column>

          <ms-table-column
            sortable
            :label="'更新时间'"
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
import MsTableColumn from "@/business/components/common/components/table/Ms-table-column";
import {CUSTOM_FIELD_LIST} from "@/common/js/default-table-header";
import MsTableButton from "@/business/components/common/components/MsTableButton";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import {CUSTOM_FIELD_TYPE_OPTION} from "@/common/js/table-constants";
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
      fieldTypeMap:{
        input: '输入框',
        textarea: '文本框',
        select: '单选下拉列表',
        multipleSelect: '多选下拉列表',
        radio: '单选框',
        checkbox: '多选框',
        member: '单选成员',
        multipleMember: '多选成员',
        data: '日期',
        int: '整型',
        float: '浮点型'
      },
      sceneMap: {
        issues: '缺陷模板',
        testCase: '用例模板'
      },
    }
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
    }
  },
  methods: {
    initTableData() {
      this.condition.workspaceId = getCurrentWorkspaceId();
      this.condition.templateId = this.templateId;
      this.condition.templateContainIds = this.templateContainIds;
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
          });
      }
    },
    open() {
      this.initTableData();
      this.visible = true;
    },
    save() {
      if (this.condition.selectAll) {
        if (this.scene) {
          // this.result = this.$post('custom/field/list/ids' + this.currentPage + '/' + this.pageSize,
          //   this.condition, (response) => {
          //     this.$emit('save', response.data);
          //   });
        }
      } else {
        this.$emit('save', this.$refs.table.selectIds);
      }
      this.visible = false;
    },
  }
}
</script>

<style scoped>

</style>
