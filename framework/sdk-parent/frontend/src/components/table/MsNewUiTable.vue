<template>
  <div>
    <el-table
      :id="msTableKey"
      v-if="tableActive"
      class="test-content adjust-table ms-table"
      header-cell-class-name="ms-table-header-cell"
      v-loading="tableIsLoading"
      :data="data"
      :default-sort="defaultSort"
      :class="{'ms-select-all-fixed': (showSelectAll && !hidePopover), 'row-click': rowClickStyle}"
      :height="screenHeight"
      :row-key="rowKey"
      :row-class-name="tableRowClassName"
      :row-style='rowStyle'
      :cell-class-name="addPaddingColClass"
      :highlight-current-row="highlightCurrentRow"
      @sort-change="sort"
      @filter-change="filter"
      @select-all="handleSelectAll"
      @select="handleSelect"
      @header-dragend="headerDragend"
      @cell-mouse-enter="showPopover"
      @row-click="handleRowClick"
      :max-height="466"
      ref="table">

      <el-table-column
        v-if="enableSelection"
        width="50"
        type="selection"/>

      <ms-table-header-select-popover v-if="enableSelection && showSelectAll && !hidePopover"
                                      :page-size="pageSize > total ? total : pageSize"
                                      :table-data-count-in-page="data.length"
                                      :total="total"
                                      :select-type="condition.selectAll"
                                      @selectPageAll="isSelectDataAll(false)"
                                      @selectAll="isSelectDataAll(true)"
                                      ref="selectPopover"/>

      <el-table-column v-if="enableSelection && batchOperators && batchOperators.length > 0"
                       width="15"
                       fixed="left"
                       column-key="batchBtnCol"
                       align="center"
                       :resizable="false">

        <template v-slot:default="scope">
          <!-- 选中记录后浮现的按钮，提供对记录的批量操作 -->
          <show-more-btn :has-showed="hasBatchTipShow"
                         :is-show="scope.row.showMore"
                         :buttons="batchOperators"
                         :size="selectDataCounts"/>
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
        column-key="tableRowDropCol">
        <template v-slot:default="scope">
          <div class="table-row-drop-bar">
            <i class="el-icon-more ms-icon-more"/>
            <i class="el-icon-more ms-icon-more"/>
          </div>
        </template>
      </el-table-column>

      <slot></slot>

      <el-table-column
        v-if="operators && operators.length > 0"
        :fixed="operatorFixed"
        :min-width="operatorWidth"
        :label="$t('commons.operating')">
        <template slot="header">
          <header-label-operate
            v-if="fieldKey"
            :disable-header-config="disableHeaderConfig"
            @exec="openCustomHeader"/>
        </template>
        <template
          v-slot:default="scope">
          <div style="padding-left: 2px;">
            <slot
              name="opt-before"
              :row="scope.row">
            </slot>
            <ms-table-operators
              :buttons="operators"
              :row="scope.row"
              :index="scope.$index"/>
            <slot
              name="opt-behind"
              :row="scope.row">
            </slot>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <ms-custom-table-header
      v-if="fieldKey"
      :type="fieldKey"
      :custom-fields="customFields"
      @reload="resetHeader"
      ref="customTableHeader"/>

  </div>
</template>

<script>
import {
  _filter,
  _handleSelect,
  _handleSelectAll,
  _sort,
  getSelectDataCounts,
  setUnSelectIds,
  toggleAllSelection,
  checkTableRowIsSelect,
  getCustomTableHeader,
  saveCustomTableWidth,
  saveLastTableSortField,
  handleRowDrop,
  clearShareDragParam,
} from "../../utils/tableUtils";
import MsTableHeaderSelectPopover from "./MsTableHeaderSelectPopover";
import MsTablePagination from "../pagination/TablePagination";
import ShowMoreBtn from "./ShowMoreBtn";
import MsTableColumn from "./MsTableColumn";
import MsTableOperators from "../MsTableOperators";
import HeaderLabelOperate from "../head/HeaderLabelOperate";
import HeaderCustom from "../head/HeaderCustom";
import MsCustomTableHeader from "./MsCustomTableHeader";
import {lineToHump} from "../../utils";
import {getUUID} from "../../utils";

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
      }
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
        return "130px";
      }
    },
    // 操作列的宽度
    operatorFixed: {
      type: [String, Boolean],
      default() {
        return "right";
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
    // 添加鼠标移入小手样式
    rowClickStyle: {
      type: Boolean,
      default() {
        return false;
      }
    },
    tableIsLoading: {
      type: [Boolean, Promise],
      default() {
        return false;
      }
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
    rowOrderFunc: Function
  },
  created() {
  },
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
          this.clear();
          this.doLayout();
          this.checkTableRowIsSelect();
          this.listenRowDrop();
        });
      }
    },
    selectDataCounts(value) {
      this.$emit("selectCountChange", value);
    }
  },
  methods: {
    // 批量操作提示, 第一次勾选提示, 之后不提示
    // 先添加 batch-popper 样式, 全选后再移除样式, 只保留可见框内第一条数据的提示
    removeBatchPopper() {
      let elements = window.document.getElementsByClassName('batch-popper');
      let tableHeader = window.document.getElementsByClassName('table-column-mark');
      let columns = window.document.getElementsByClassName('table-more-icon');
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
            elements[i].classList.remove('batch-popper');
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
        handleRowDrop(this.data, (param) => {
          param.groupId = this.rowOrderGroupId;
          if (this.rowOrderFunc) {
            this.rowOrderFunc(param);
          }
        }, this.msTableKey);
      }
    },
    isScrollShow(column, tableTop) {  //判断元素是否因为超过表头
      let columnTop = column.getBoundingClientRect().top;
      return columnTop - tableTop > 30;
    },
    // 访问页面默认高亮当前的排序
    setDefaultOrders() {
      let orders = this.condition.orders;
      if (orders) {
        orders.forEach(item => {
          this.defaultSort = {
            prop: lineToHump(item.name),
            order: 'descending'
          };
          if (item.type === 'asc') {
            this.defaultSort.order = 'ascending';
          }
          return;
        });
      }
    },
    handleSelectAll(selection) {
      _handleSelectAll(this, selection, this.data, this.selectRows, this.condition);
      setUnSelectIds(this.data, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
      this.selectIds = Array.from(this.selectRows).map(o => o.id);
      //有的组件需要回调父组件的函数，做下一步处理
      this.$emit('callBackSelectAll', selection);
      this.$nextTick(function () {
        setTimeout(this.removeBatchPopper, 1);
      });
    },
    handleSelect(selection, row) {
      _handleSelect(this, selection, row, this.selectRows);
      setUnSelectIds(this.data, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
      this.selectIds = Array.from(this.selectRows).map(o => o.id);
      //有的组件需要回调父组件的函数，做下一步处理
      this.$emit('callBackSelect', selection);
      this.$nextTick(function () {
        setTimeout(this.removeBatchPopper, 1);
      });
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
    showPopover(row, column, cell) {
      if (column.property === 'name') {
        this.currentCaseId = row.id;
      }
    },
    doLayout() {
      if (this.$refs.table) {
        // 表格错位问题，执行三次
        for (let i = 1; i <= 3; i++) {
          setTimeout(this.$refs.table.doLayout, 300 * i);
        }
      }
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.$emit('filter');
      this.handleRefresh();
    },
    sort(column) {
      // 每次只对一个字段排序
      if (this.condition.orders) {
        this.condition.orders = [];
      }
      _sort(column, this.condition);
      if (this.rememberOrder) {
        saveLastTableSortField(this.fieldKey, JSON.stringify(this.condition.orders));
      }
      this.$emit('order', column);
      this.handleRefresh();
    },
    handleBatchEdit() {
      this.getMaintainerOptions();
      this.$refs.batchEdit.open();
    },
    handleBatchMove() {
      this.$refs.testBatchMove.open(this.treeNodes, Array.from(this.selectRows).map(row => row.id), this.moduleOptions);
    },
    handleRowClick(row, column) {
      this.$emit("handleRowClick", row, column);
    },
    handleRefresh() {
      this.clear();
      this.$emit('refresh');
    },
    handlePageChange() {
      this.$emit('pageChange');
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
      this.tableActive = false;
      this.$nextTick(() => {
        this.doLayout();
        this.tableActive = true;
      });
      this.listenRowDrop();
    },
    toggleRowSelection() {
      this.$refs.table.toggleRowSelection();
    },
    reloadTable() {
      this.$nextTick(() => {
        this.doLayout();
      });
    },
    addPaddingColClass({column}) {
      if (column.columnKey === 'tableRowDropCol'
        || column.columnKey === 'selectionCol'
        || column.columnKey === 'batchBtnCol') {
        return 'padding-col';
      }
    },
    rowStyle({row}) {
      return row.hidden ? {"display": "none"} : {};
    },
    tableRowClassName(row) {
      if (row.row.hidden) {
        return 'ms-variable-hidden-row';
      }
      return '';
    },
  }
};
</script>

<style scoped>
.batch-popper {
  top: 300px;
  color: #1FDD02;
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
.ms-table :deep(.el-table__body) tr.hover-row.el-table__row--striped.current-row > td,
.ms-table :deep(.el-table__body) tr.hover-row.el-table__row--striped > td,
.ms-table :deep(.el-table__body) tr.hover-row > td {
  background-color: #ffffff;
}

/* 解决拖拽排序后hover阴影错乱问题 */
.ms-table :deep(.el-table__body) tr:hover > td {
  background-color: #F5F7FA;
}

.disable-hover :deep(tr:hover>td) {
  background-color: #ffffff !important;
}

.row-click {
  cursor: pointer;
}

:deep(.ms-table-header-cell) {
  height: 46px;
  background-color: #F5F6F7;
  font-size: 14px;
  font-weight: 500;
  border: 1px solid rgba(31, 35, 41, 0.15);
  border-right-width: 0;
  border-left-width: 0;
  color: #646A73;
  line-height: 22px;
  padding: 0px;
}

:deep(.ms-select-all-fixed th.el-table-column--selection.is-leaf.ms-table-header-cell.el-table__cell) {
  border-radius: 0;
  border: 0;
  display: inline-block;
  width: 50px;
  border-top: 1px solid rgba(31, 35, 41, 0.15);
}

:deep(.ms-select-all-fixed th.el-table-column--selection.is-leaf.ms-table-header-cell.el-table__cell .el-checkbox) {
  margin-left: 8px;
  margin-top: 5px;
}

:deep(.ms-select-all-fixed th:first-child.el-table-column--selection) {
  margin-top: 0px;
}

:deep(.ms-select-all-fixed th:nth-child(2) .table-select-icon) {
  position: absolute;
  display: inline-block;
  top: -8px;
  left: -25px;
  width: 30px;
}

:deep(.el-table th, .el-table tr) {
  height: 46px;
}

/*:deep(.el-table--scrollable-x .el-table__body-wrapper) {*/
/*    overflow-x: hidden;*/
/*}*/

/*:deep(.el-table--scrollable-x .el-table__body-wrapper:hover) {*/
/*  overflow-x: auto;*/
/*}*/

/*:deep(.el-table--scrollable-y .el-table__body-wrapper) {*/
/*  overflow-y: hidden;*/
/*}*/

/*:deep(.el-table--scrollable-y .el-table__body-wrapper:hover) {*/
/*  overflow-y: auto;*/
/*}*/
</style>
