<template>
  <div v-if="tableActive">
    <el-table
      border
      :data="data"
      @sort-change="sort"
      @filter-change="filter"
      @select-all="handleSelectAll"
      @select="handleSelect"
      @header-dragend="headerDragend"
      @cell-mouse-enter="showPopover"
      class="test-content adjust-table ms-table"
      :class="{'ms-select-all-fixed': showSelectAll}"
      :height="screenHeight"
      v-loading="tableIsLoading"
      ref="table" @row-click="handleRowClick">

      <el-table-column v-if="enableSelection" width="50" type="selection"/>

      <ms-table-header-select-popover v-if="enableSelection && showSelectAll" ref="selectPopover"
                                      :page-size="pageSize > total ? total : pageSize"
                                      :table-data-count-in-page="data.length"
                                      :total="total"
                                      @selectPageAll="isSelectDataAll(false)"
                                      @selectAll="isSelectDataAll(true)"/>

      <el-table-column v-if="enableSelection && batchOperators && batchOperators.length > 0" width="30"
                       fixed="left"
                       :resizable="false" align="center">
        <template v-slot:default="scope">
          <!-- 选中记录后浮现的按钮，提供对记录的批量操作 -->
          <show-more-btn :is-show-tool="scope.row.showTool" :is-show="scope.row.showMore" :buttons="batchOperators"
                         :size="selectDataCounts"/>
        </template>
      </el-table-column>

      <el-table-column width="1"/>
      <slot></slot>

      <el-table-column
        v-if="operators && operators.length > 0"
        :min-width="operatorWidth"
        fixed="right"
        :label="$t('commons.operating')">
        <template slot="header">
          <header-label-operate v-if="fieldKey" @exec="openCustomHeader"/>
        </template>
        <template v-slot:default="scope">
          <div>
            <slot name="opt-before" :row="scope.row"></slot>
            <ms-table-operators :buttons="operators" :row="scope.row" :index="scope.$index"/>
            <slot name="opt-behind" :row="scope.row"></slot>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <ms-custom-table-header
      v-if="fieldKey"
      @reload="resetHeader"
      :type="fieldKey"
      :custom-fields="customFields"
      ref="customTableHeader"/>

  </div>
</template>

<script>
import {
  _filter,
  _handleSelect,
  _handleSelectAll, _sort, getLabel,
  getSelectDataCounts,
  setUnSelectIds,
  toggleAllSelection,
  checkTableRowIsSelect, getCustomTableHeader, saveCustomTableWidth,
} from "@/common/js/tableUtils";
import MsTableHeaderSelectPopover from "@/business/components/common/components/table/MsTableHeaderSelectPopover";
import {TEST_CASE_LIST} from "@/common/js/constants";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import ShowMoreBtn from "@/business/components/track/case/components/ShowMoreBtn";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import MsTableOperators from "@/business/components/common/components/MsTableOperators";
import HeaderLabelOperate from "@/business/components/common/head/HeaderLabelOperate";
import HeaderCustom from "@/business/components/common/head/HeaderCustom";
import MsCustomTableHeader from "@/business/components/common/components/table/MsCustomTableHeader";

/**
 * 参考 ApiList
 *
 * 添加自定义表头步骤：
 *   设置 fieldKey,
 *   设置 fields,
 *   ms-table-column 外层加循环，并设置 field
 *   （操作按钮使用 Mstable 里的操作逻辑，要自定义可以使用插画槽 opt-before，opt-behind）
 * 记住列宽步骤：
 *   设置 fieldKey,
 *   ms-table-column 设置 fields-width
 */
export default {
  name: "MsTable",
  components: {
    MsCustomTableHeader,
    HeaderLabelOperate,
    MsTableOperators, MsTableColumn, ShowMoreBtn, MsTablePagination, MsTableHeaderSelectPopover, HeaderCustom
  },
  data() {
    return {
      selectDataCounts: 0,
      selectRows: new Set(),
      selectIds: [],
      tableActive: true
    };
  },
  props: {
    screenHeight: {
      type: [String, Number],
      default: 400,
    },
    selectNodeIds: {
      type: Array,
      default() {
        return [];
      }
    },
    data: {
      type: Array,
      default() {
        return [];
      }
    },
    condition: {
      type: Object,
      default() {
        return {};
      }
    },
    pageSize: {
      type: Number,
      default() {
        return 10;
      }
    },
    total: {
      type: Number,
      default() {
        return 10;
      }
    },
    // 操作按钮
    operators: {
      type: Array,
      default() {
        return [];
      }
    },
    //批量操作按钮
    batchOperators: {
      type: Array,
      default() {
        return [];
      }
    },
    // 操作列的宽度
    operatorWidth: {
      type: String,
      default() {
        return "150px";
      }
    },
    //开启全选
    enableSelection: {
      type: Boolean,
      default() {
        return true;
      }
    }, //开启全选
    showSelectAll: {
      type: Boolean,
      default() {
        return true;
      }
    },
    tableIsLoading:{
      type:Boolean,
      default() {
        return false;
      }
    },
    fields: Array,
    fieldKey: String,
    customFields: Array
  },
  mounted() {
    getLabel(this, TEST_CASE_LIST);
  },
  watch: {
    selectNodeIds() {
      this.selectDataCounts = 0;
    },
  },
  methods: {
    handleSelectAll(selection) {
      _handleSelectAll(this, selection, this.data, this.selectRows, this.condition);
      setUnSelectIds(this.data, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
      this.selectIds = Array.from(this.selectRows).map(o => o.id);
      //有的组件需要回调父组件的函数，做下一步处理
      this.$emit('callBackSelectAll',selection);
    },
    handleSelect(selection, row) {
      _handleSelect(this, selection, row, this.selectRows);
      setUnSelectIds(this.data, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
      this.selectIds = Array.from(this.selectRows).map(o => o.id);
      //有的组件需要回调父组件的函数，做下一步处理
      this.$emit('callBackSelect',selection);
    },
    isSelectDataAll(data) {
      this.condition.selectAll = data;
      //设置勾选
      toggleAllSelection(this.$refs.table, this.data, this.selectRows);
      //显示隐藏菜单
      _handleSelectAll(this, this.data, this.data, this.selectRows);
      //设置未选择ID(更新)
      this.condition.unSelectIds = [];
      //更新统计信息
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
      this.selectIds = Array.from(this.selectRows).map(o => o.id);
    },
    headerDragend(newWidth, oldWidth, column, event) {
      // 保存列宽
      saveCustomTableWidth(this.fieldKey, column.columnKey, newWidth);
    },
    showPopover(row, column, cell) {
      if (column.property === 'name') {
        this.currentCaseId = row.id;
      }
    },
    doLayout() {
      setTimeout(this.$refs.table.doLayout(), 200);
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.handleRefresh();
    },
    sort(column) {
      // 每次只对一个字段排序
      if (this.condition.orders) {
        this.condition.orders = [];
      }
      _sort(column, this.condition);
      this.$emit("saveSortField", this.fieldKey,this.condition.orders);
      this.handleRefresh();
    },
    handleBatchEdit() {
      this.getMaintainerOptions();
      this.$refs.batchEdit.open();
    },
    handleBatchMove() {
      this.$refs.testBatchMove.open(this.treeNodes, Array.from(this.selectRows).map(row => row.id), this.moduleOptions);
    },
    handleRowClick(row) {
      this.$emit("handleRowClick", row);
    },
    handleRefresh() {
      this.clear();
      this.$emit('refresh');
    },
    handlePageChange() {
      this.$emit('pageChange');
    },
    clear() {
      this.clearSelectRows();
    },
    checkTableRowIsSelect() {
      checkTableRowIsSelect(this, this.condition, this.data, this.$refs.table, this.selectRows);
    },
    clearSelection() {
      this.clearSelectRows();
    },
    getSelectRows() {
      return this.selectRows;
    },
    clearSelectRows() {
      this.selectRows.clear();
      this.selectIds = [];
      this.condition.selectAll = false;
      this.condition.unSelectIds = [];
      this.selectDataCounts = 0;
      if (this.$refs.table) {
        this.$refs.table.clearSelection();
      }
    },
    openCustomHeader() {
      this.$refs.customTableHeader.open(this.fields);
    },
    resetHeader() {
      this.$emit('update:fields', getCustomTableHeader(this.fieldKey, this.customFields));
      this.reloadTable();
    },
    reloadTable() {
      this.tableActive = false;
      this.$nextTick(() => {
        this.tableActive = true;
      });
    }
  }
};
</script>

<style scoped>

</style>
