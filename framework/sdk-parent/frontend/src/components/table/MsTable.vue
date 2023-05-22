<template>
  <div>
    <el-table
      :id="msTableKey"
      v-if="tableActive"
      border
      class="test-content adjust-table ms-table"
      v-loading="tableIsLoading"
      :data="data"
      :default-sort="defaultSort"
      :class="{
        'ms-select-all-fixed': showSelectAll && !hidePopover,
        'row-click': rowClickStyle,
      }"
      :height="screenHeight"
      :row-key="rowKey"
      :row-class-name="tableRowClassName"
      :row-style="rowStyle"
      :cell-class-name="addPaddingColClass"
      :highlight-current-row="highlightCurrentRow"
      @sort-change="sort"
      @filter-change="filter"
      @select-all="handleSelectAll"
      @select="handleSelect"
      @header-dragend="headerDragend"
      @cell-mouse-enter="showPopover"
      @row-click="handleRowClick"
      ref="table"
    >
      <el-table-column
        v-if="enableSelection && !reserveOption"
        width="50"
        type="selection"
      />

      <el-table-column
        v-if="enableSelection && reserveOption"
        width="50"
        type="selection"
        reserve-selection
      />

      <ms-table-header-select-popover
        v-if="enableSelection && showSelectAll && !hidePopover"
        :page-size="pageSize > total ? total : pageSize"
        :table-data-count-in-page="data.length"
        :total="total"
        :select-type="condition.selectAll"
        @selectPageAll="isSelectDataAll(false)"
        @selectAll="isSelectDataAll(true)"
        ref="selectPopover"
      />

      <el-table-column
        v-if="enableSelection && batchOperators && batchOperators.length > 0"
        width="15"
        fixed="left"
        class-name="padding-col"
        align="center"
        :resizable="false"
      >
        <template v-slot:default="scope">
          <!-- 选中记录后浮现的按钮，提供对记录的批量操作 -->
          <show-more-btn
            :has-showed="hasBatchTipShow"
            :is-show="scope.row.showMore"
            :buttons="batchOperators"
            :size="selectDataCounts"
          />
        </template>
      </el-table-column>

      <el-table-column width="1">
        <template v-slot:header>
          <span class="table-column-mark">&nbsp;</span>
        </template>
      </el-table-column>

      <!--   拖拽排序   -->
      <el-table-column
        v-if="enableOrderDrag"
        width="20"
        column-key="tableRowDropCol"
      >
        <template v-slot:default="scope">
          <div class="table-row-drop-bar">
            <i class="el-icon-more ms-icon-more" />
            <i class="el-icon-more ms-icon-more" />
          </div>
        </template>
      </el-table-column>

      <slot></slot>

      <el-table-column
        v-if="operators && operators.length > 0"
        :fixed="operatorFixed"
        :min-width="operatorWidth"
        :label="$t('commons.operating')"
      >
        <template slot="header">
          <header-label-operate
            v-if="fieldKey"
            :disable-header-config="disableHeaderConfig"
            @exec="openCustomHeader"
          />
        </template>
        <template v-slot:default="scope">
          <div>
            <slot name="opt-before" :row="scope.row"></slot>
            <ms-table-operators
              :buttons="operators"
              :row="scope.row"
              :index="scope.$index"
            />
            <slot name="opt-behind" :row="scope.row"></slot>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <ms-custom-table-header
      v-if="fieldKey"
      :type="fieldKey"
      :custom-fields="customFields"
      @reload="resetHeader"
      ref="customTableHeader"
    />
  </div>
</template>

<script>
import {
  _filter,
  _handleSelect,
  _handleSelectAll,
  _sort,
  checkTableRowIsSelect,
  clearShareDragParam,
  deleteTableRow,
  getCustomTableHeader,
  getSelectDataCounts,
  handleRowDrop,
  saveCustomTableWidth,
  saveLastTableSortField,
  setUnSelectIds,
} from "../../utils/tableUtils";
import MsTableHeaderSelectPopover from "./MsTableHeaderSelectPopover";
import MsTablePagination from "../pagination/TablePagination";
import ShowMoreBtn from "./ShowMoreBtn";
import MsTableColumn from "./MsTableColumn";
import MsTableOperators from "../MsTableOperators";
import HeaderLabelOperate from "../head/HeaderLabelOperate";
import HeaderCustom from "../head/HeaderCustom";
import MsCustomTableHeader from "./MsCustomTableHeader";
import { getUUID, lineToHump } from "../../utils";

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
    MsTableOperators,
    MsTableColumn,
    ShowMoreBtn,
    MsTablePagination,
    MsTableHeaderSelectPopover,
    HeaderCustom,
  },
  data() {
    return {
      selectDataCounts: 0,
      selectRows: new Set(),
      selectIds: [],
      hasBatchTipShow: false,
      defaultSort: {},
      tableActive: true,
      msTableKey: "msTableKey_" + getUUID(),
    };
  },
  props: {
    screenHeight: {
      type: [String, Number],
      default: 400,
    },
    hidePopover: {
      type: Boolean,
      default() {
        return false;
      },
    },
    selectNodeIds: {
      type: Array,
      default() {
        return [];
      },
    },
    data: {
      type: Array,
      default() {
        return [];
      },
    },
    condition: {
      type: Object,
      default() {
        return {};
      },
    },
    pageSize: {
      type: Number,
      default() {
        return 10;
      },
    },
    total: {
      type: Number,
      default() {
        return 10;
      },
    },
    // 操作按钮
    operators: {
      type: Array,
      default() {
        return [];
      },
    },
    //批量操作按钮
    batchOperators: {
      type: Array,
      default() {
        return [];
      },
    },
    // 操作列的宽度
    operatorWidth: {
      type: String,
      default() {
        return "150px";
      },
    },
    // 操作列的宽度
    operatorFixed: {
      type: [String, Boolean],
      default() {
        return "right";
      },
    },
    //开启全选
    enableSelection: {
      type: Boolean,
      default() {
        return true;
      },
    },
    //开启保留前一页的选项的功能，注意需要与row-key搭配使用，否则不生效
    reserveOption: {
      type: Boolean,
      default() {
        return false;
      },
    },
    //是否是翻页的刷新，当reserveOption为true时，引用列表页需要在翻页的时候使该属性为true，用以避免clear
    pageRefresh: {
      type: Boolean,
      default() {
        return false;
      },
    },
    //开启全选
    showSelectAll: {
      type: Boolean,
      default() {
        return true;
      },
    },
    // 添加鼠标移入小手样式
    rowClickStyle: {
      type: Boolean,
      default() {
        return false;
      },
    },
    tableIsLoading: {
      type: [Boolean, Promise],
      default() {
        return false;
      },
    },
    disableHeaderConfig: Boolean,
    fields: Array,
    fieldKey: String,
    customFields: Array,
    highlightCurrentRow: Boolean,
    // 是否记住排序
    rememberOrder: Boolean,
    enableOrderDrag: Boolean,
    rowKey: [String, Function],
    // 自定义排序，需要传资源所属的项目id或者测试计划id，并且传排序的方法
    rowOrderGroupId: String,
    rowOrderFunc: Function,
  },
  created() {},
  mounted() {
    this.setDefaultOrders();
  },
  watch: {
    selectNodeIds() {
      this.selectDataCounts = 0;
    },
    enableOrderDrag() {
      if (!this.enableOrderDrag) {
        clearShareDragParam();
      }
    },
    // 刷新列表后做统一处理
    data(newVar, oldVar) {
      // 不知为何，勾选选择框也会进到这里，但是这种情况 newVar === oldVar
      if (newVar !== oldVar) {
        this.$nextTick(() => {
          this.setDefaultOrders();
          if (!this.pageRefresh) {
            this.clear();
          }
          this.doLayout();
          this.checkTableRowIsSelect();
          this.deleteTableRow();
          this.listenRowDrop();
        });
      }
    },
    pageSize() {
      this.clear();
    },
    selectDataCounts(value) {
      this.$emit("selectCountChange", value);
    },
  },
  methods: {
    // 批量操作提示, 第一次勾选提示, 之后不提示
    // 先添加 batch-popper 样式, 全选后再移除样式, 只保留可见框内第一条数据的提示
    removeBatchPopper() {
      let elements = window.document.getElementsByClassName("batch-popper");
      let tableHeader =
        window.document.getElementsByClassName("table-column-mark");
      let columns = window.document.getElementsByClassName("table-more-icon");
      let tableTop = tableHeader[0].getBoundingClientRect().top;
      let index = 0;
      for (let i = 0; i < columns.length; i++) {
        let column = columns[i];
        if (this.isScrollShow(column, tableTop)) {
          index = i;
          break;
        }
      }
      if (elements) {
        for (let i = 0; i < elements.length; i++) {
          if (i == index) {
            elements[i].classList.remove("batch-popper");
            setTimeout(() => {
              this.hasBatchTipShow = true;
            }, 1500);
          }
        }
      }
    },
    // 拖拽排序
    listenRowDrop() {
      if (this.rowOrderGroupId) {
        handleRowDrop(
          this.data,
          (param) => {
            param.groupId = this.rowOrderGroupId;
            if (this.rowOrderFunc) {
              this.rowOrderFunc(param);
            }
          },
          this.msTableKey
        );
      }
    },
    isScrollShow(column, tableTop) {
      //判断元素是否因为超过表头
      let columnTop = column.getBoundingClientRect().top;
      return columnTop - tableTop > 30;
    },
    // 访问页面默认高亮当前的排序
    setDefaultOrders() {
      let orders = this.condition.orders;
      if (orders) {
        orders.forEach((item) => {
          this.defaultSort = {
            prop: lineToHump(item.name),
            order: "descending",
          };
          if (item.type === "asc") {
            this.defaultSort.order = "ascending";
          }
          return;
        });
      }
    },
    handleSelectAll(selection) {
      _handleSelectAll(
        this,
        selection,
        this.data,
        this.selectRows,
        this.condition
      );
      setUnSelectIds(selection, this.condition, this.selectRows);
      this.selectDataCounts = this.selectRows.size
      this.selectIds = Array.from(this.selectRows).map((o) => o.id);
      //有的组件需要回调父组件的函数，做下一步处理
      this.$emit("callBackSelectAll", selection);
      this.$nextTick(function () {
        setTimeout(this.removeBatchPopper, 1);
      });
    },
    handleSelect(selection, row) {
      let selectRowMap = new Map();
      for (let selectRow of this.selectRows) {
        selectRowMap.set(selectRow.id, selectRow);
      }
      _handleSelect(this, selection, row, selectRowMap);
      let selectRow = Array.from(selectRowMap.values());
      this.selectRows = new Set(selectRow);
      setUnSelectIds(this.data, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(
        this.condition,
        this.total,
        this.selectRows
      );
      this.selectIds = Array.from(this.selectRows).map((o) => o.id);
      //有的组件需要回调父组件的函数，做下一步处理
      this.$emit("callBackSelect", selection);
      this.$nextTick(function () {
        setTimeout(this.removeBatchPopper, 1);
      });
    },
    isSelectDataAll(data) {
      this.condition.selectAll = data;
      //显示隐藏菜单
      _handleSelectAll(this, this.data, this.data, this.selectRows);
      //选中行
      this.selectRows.forEach(t => {
        this.$refs.table.toggleRowSelection(t, true);
      })
      this.deleteTableRow();
      //设置未选择ID(更新)
      this.condition.unSelectIds = [];
      //更新统计信息
      this.selectDataCounts = getSelectDataCounts(
        this.condition,
        this.total,
        this.selectRows
      );
      this.selectIds = Array.from(this.selectRows).map((o) => o.id);
    },
    headerDragend(newWidth, oldWidth, column, event) {
      if (column) {
        if (column.minWidth) {
          let minWidth = column.minWidth;
          if (minWidth > newWidth) {
            column.width = minWidth;
            newWidth = minWidth;
          }
        }
      }
      // 保存列宽
      saveCustomTableWidth(this.fieldKey, column.columnKey, newWidth);
    },
    doLayout() {
      if (this.$refs.table) {
        // 表格错位问题，执行三次
        for (let i = 1; i <= 3; i++) {
          setTimeout(this.$refs.table.doLayout, 300 * i);
        }
      }
    },
    showPopover(row, column, cell) {
      if (column.property === "name") {
        this.currentCaseId = row.id;
      }
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.$emit("filter");
      this.handleRefresh();
    },
    sort(column) {
      // 每次只对一个字段排序
      if (this.condition.orders) {
        this.condition.orders = [];
      }
      _sort(column, this.condition);
      if (this.rememberOrder) {
        saveLastTableSortField(
          this.fieldKey,
          JSON.stringify(this.condition.orders)
        );
      }
      this.$emit("order", column);
      this.handleRefresh();
    },
    handleBatchEdit() {
      this.getMaintainerOptions();
      this.$refs.batchEdit.open();
    },
    handleBatchMove() {
      this.$refs.testBatchMove.open(
        this.treeNodes,
        Array.from(this.selectRows).map((row) => row.id),
        this.moduleOptions
      );
    },
    handleRowClick (row, column) {
      // 多选框的列不触发点击行事件
      if (column.type === 'selection') return; 
      this.$emit("handleRowClick", row, column);
    },
    handleRefresh() {
      this.clear();
      this.$emit("refresh");
    },
    handlePageChange() {
      this.$emit("pageChange");
    },
    cancelCurrentRow() {
      this.$refs.table.setCurrentRow(-1);
    },
    clear() {
      // 清除全选
      this.condition.selectAll = false;
      this.condition.unSelectIds = [];
      this.clearSelectRows();
    },
    checkTableRowIsSelect() {
      checkTableRowIsSelect(
        this,
        this.condition,
        this.data,
        this.$refs.table,
        this.selectRows
      );
    },
    deleteTableRow() {
      deleteTableRow(
        this,
        this.condition,
        this.data,
        this.$refs.table,
        this.selectRows
      );
    },
    clearSelection() {
      this.clearSelectRows();
    },
    clearSort() {
      this.defaultSort = {};
    },
    getSelectRows() {
      return this.selectRows;
    },
    clearSelectRows() {
      this.selectRows.clear();
      this.selectIds = [];
      this.selectDataCounts = 0;
      if (this.$refs.table) {
        this.$refs.table.clearSelection();
      }
    },
    openCustomHeader() {
      this.$refs.customTableHeader.open(this.fields);
    },
    resetHeader() {
      this.$emit(
        "update:fields",
        getCustomTableHeader(this.fieldKey, this.customFields)
      );
      this.tableActive = false;
      this.$nextTick(() => {
        this.doLayout();
        this.tableActive = true;
      });
      this.listenRowDrop();
      this.$emit("headChange");
    },
    toggleRowSelection() {
      this.$refs.table.toggleRowSelection();
    },
    reloadTable() {
      this.$nextTick(() => {
        this.doLayout();
      });
    },
    addPaddingColClass({ column }) {
      if (
        column.columnKey === "tableRowDropCol" ||
        column.columnKey === "selectionCol" ||
        column.columnKey === "batchBtnCol"
      ) {
        return "padding-col";
      }
    },
    rowStyle({row}) {
      return row.hidden ? {display: "none"} : {};
    },
    tableRowClassName(row) {
      if (row.row.hidden) {
        return "ms-variable-hidden-row";
      }
      return "";
    },
    //清空Filter条件
    clearFilter() {
      this.$refs.table.clearFilter();
    }
  },
};
</script>

<style scoped>
.batch-popper {
  top: 300px;
  color: #1fdd02;
}

.el-table :deep(.padding-col) .cell {
  padding: 0px !important;
}

.table-row-drop-bar {
  /*visibility: hidden;*/
  text-align: center;
  height: 28px;
}

.table-row-drop-bar:hover {
  /*visibility: visible;*/
  /*font-size: 15px;*/
}

.ms-icon-more {
  transform: rotate(90deg);
  width: 9px;
  color: #cccccc;
}

.ms-icon-more:first-child {
  margin-right: -5px;
}

.ms-table :deep(.el-table__body) tr.hover-row.current-row > td,
.ms-table
  :deep(.el-table__body)
  tr.hover-row.el-table__row--striped.current-row
  > td,
.ms-table :deep(.el-table__body) tr.hover-row.el-table__row--striped > td,
.ms-table :deep(.el-table__body) tr.hover-row > td {
  background-color: #ffffff;
}

/* 解决拖拽排序后hover阴影错乱问题 */
.ms-table :deep(.el-table__body) tr:hover > td {
  background-color: #f5f7fa;
}

.ms-table :deep(.el-table__body .el-checkbox__inner) {
  width: 16px;
  height: 16px;
}

.ms-table :deep(.el-table__body .el-checkbox__inner::after) {
  height: 9px;
  left: 5px;
}

.disable-hover :deep(tr:hover > td) {
  background-color: #ffffff !important;
}

.row-click {
  cursor: pointer;
}
</style>
