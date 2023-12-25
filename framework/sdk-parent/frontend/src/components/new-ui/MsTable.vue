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
      :border="enableHeaderDrag"
      @sort-change="sort"
      @filter-change="filter"
      @select-all="handleSelectAll"
      @select="handleSelect"
      @selection-change="handleSelectionChange"
      @header-dragend="headerDragend"
      @cell-mouse-enter="showPopover"
      @row-click="handleRowClick"
      :max-height="enableMaxHeight ? maxHeight : 'auto'"
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
          <div>
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

      <template #empty>
        <div style="width: 100%;height: 300px;display: flex;flex-direction: column;justify-content: start;align-items: center">
          <img :src="refreshBySearch ? '/assets/module/figma/icon_search_none.svg' : '/assets/module/figma/icon_none.svg'" style="height: 100px;width: 100px;margin-bottom: 0.6rem; margin-top: 3rem"/>
          <span class="addition-info-title">{{ refreshBySearch ? $t("home.dashboard.public.no_search_data") : $t("home.dashboard.public.no_data") }}</span>
        </div>
      </template>
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
  checkTableRowIsSelect,
  clearShareDragParam,
  getCustomTableHeader,
  getSelectDataCounts,
  handleRowDrop,
  saveCustomTableWidth,
  saveLastTableSortField,
  setUnSelectIds,
  toggleAllSelection,
} from "../../utils/tableUtils";
import MsTableHeaderSelectPopover from "./MsTableHeaderSelectPopover";
import MsTablePagination from "../pagination/TablePagination";
import ShowMoreBtn from "../table/ShowMoreBtn";
import MsTableColumn from "../table/MsTableColumn";
import MsTableOperators from "../MsTableOperators";
import HeaderLabelOperate from "../head/HeaderLabelOperate";
import HeaderCustom from "../head/HeaderCustom";
import MsCustomTableHeader from "../table/MsCustomTableHeader";
import {getUUID, lineToHump} from "../../utils";

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
      highlightRowIndexArr: [],
    };
  },
  props: {
    enableMaxHeight: {
      type: Boolean,
      default() {
        return true;
      }
    },
    maxHeight: {
      type: String,
      default() {
        return 'calc(100vh)';
      }
    },
    screenHeight: {
      type: [String, Number],
      // default: 400,
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
        return "150px";
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
    rowOrderFunc: Function,
    refreshBySearch: Boolean, // 是否通过搜索刷新的列表,
    enableHeaderDrag: {
      type: Boolean,
      default() {
        return true;
      }
    }
  },
  created() {
  },
  mounted() {
    this.setDefaultOrders();
    this.preventSortableEventPropagation();
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
    preventSortableEventPropagation() {
      document.body.ondrop = function (event) {
        event.preventDefault();
        event.stopPropagation();
      }
    },
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
      if (this.condition.selectAll && selection && selection.length > 0) {
        this.isSelectDataAll(true)
        return;
      } else if (this.condition.selectAll && selection && selection.length === 0){
        this.$emit("clearTableSelect");
        return;
      }
      _handleSelectAll(this, selection, this.data, this.selectRows, this.condition);
      if (this.condition.selectAll) {
        this.condition.unSelectIds = [];
      } else {
        setUnSelectIds(this.data, this.condition, this.selectRows);
      }
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
      this.selectIds = Array.from(this.selectRows).map(o => o.id);
      //有的组件需要回调父组件的函数，做下一步处理
      this.$emit('callBackSelectAll', selection);
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
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
      this.selectIds = Array.from(this.selectRows).map(o => o.id);
      //有的组件需要回调父组件的函数，做下一步处理
      this.$emit('callBackSelect', selection);
      this.$nextTick(function () {
        setTimeout(this.removeBatchPopper, 1);
      });
    },
    handleSelectionChange(rows) {
      let selectArr = [];
      rows.forEach((row, index) => {
        this.data.forEach((item, i) => {
          if(item.id == row.id){
            selectArr.push(i)
          }
        })
      })
      this.highlightRowIndexArr = selectArr;
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
      this.$emit(data ? 'callBackSelectAll' : 'callBackSelect');
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
    resetHeader(callback) {
      this.$emit('update:fields', getCustomTableHeader(this.fieldKey, this.customFields));
      this.tableActive = false;
      this.$nextTick(() => {
        this.doLayout();
        this.tableActive = true;
        if (callback) {
          callback();
        }
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
    tableRowClassName({row, rowIndex}) {
      let selectIndex = this.highlightRowIndexArr;
      for(let i = 0; i < selectIndex.length; i++){
        if(rowIndex === selectIndex[i]){
          return 'hignlight-row'
        }
      }
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

:deep(.adjust-table) {
  overflow: auto;
  position: relative;
  top: 16px;
  cursor: pointer;
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
  background-color: rgba(31, 35, 41, 0.1)!important;
  cursor: pointer!important;
}

/* focus行背景色 */
.ms-table :deep(.el-table__body) tr.current-row > td {
  background-color: #FFFFFF!important;
}

/* 选中某行时的背景色*/
.ms-table :deep(.el-table__body tr.hignlight-row > td) {
  background-color: rgba(120, 56, 135, 0.1)!important;
}

/* 解决拖拽排序后hover阴影错乱问题 */
.ms-table :deep(.el-table__body) tr:hover > td {
  background-color: #F5F7FA;
}

.disable-hover :deep(tr:hover>td) {
  background-color: #FFFFFF!important;
}

.row-click {
  cursor: pointer;
}

:deep(.ms-table-header-cell) {
  height: 46px;
  background-color: #F5F6F7;
  font-family: 'PingFang SC';
  font-size: 14px;
  font-weight: 500;
  border: 1px solid rgba(31, 35, 41, 0.15);
  border-right-width: 0;
  border-left-width: 0;
  color: #646A73;
  line-height: 22px;
  padding: 0px;
  align-items: center;
  flex: none;
  order: 0;
  flex-grow: 0;
}

:deep(.el-table__body-wrapper) {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #1F2329;
  flex: none;
  flex-grow: 0;
  flex-shrink: 0;
  flex-basis: auto;
  order: 1;
  flex-grow: 0;
}

:deep(.ms-select-all-fixed th.el-table-column--selection.is-leaf.ms-table-header-cell.el-table__cell) {
  border-radius: 0;
  border: 0;
  display: inline-block;
  width: 50px;
  border-top: 1px solid rgba(31, 35, 41, 0.15);
  border-bottom: 1px solid rgba(31, 35, 41, 0.15);
}

:deep(.ms-select-all-fixed th.el-table-column--selection.is-leaf.ms-table-header-cell.el-table__cell .el-checkbox) {
  margin-left: 8px;
  margin-top: 5px;
}

.ms-select-all-fixed :deep(th:first-child.el-table-column--selection) {
  margin-top: 0px;
}
.ms-select-all-fixed :deep(th:first-child.el-table-column--selection > .cell) {
  padding: 5px 9px 5px 2px;
  width: 35px;
}

:deep(.el-table th.is-leaf) {
  border-bottom: 1px solid rgba(31, 35, 41, 0.15);
}

:deep(.el-table th > .cell) {
  word-break: keep-all !important;
  white-space: nowrap !important;
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

:deep(tr.el-table__row) {
  height: 40px;
}

.addition-info-title {
  margin-top: -10px;
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

<style>
.ms-table .el-table__fixed::before {
  height: 0;
}

/*.ms-table .el-table__fixed-right::before {*/
/*  height: 0;*/
/*}*/
</style>
