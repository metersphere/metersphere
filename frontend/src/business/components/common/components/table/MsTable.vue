<template>
  <div class="card-container" >

    <el-table
      border
      :data="data"
      @sort-change="sort"
      @filter-change="filter"
      @select-all="handleSelectAll"
      @select="handleSelect"
      @header-dragend="headerDragend"
      @cell-mouse-enter="showPopover"
      row-key="id"
      class="test-content adjust-table ms-select-all-fixed"
      ref="table" @row-click="handleRowClick">

      <el-table-column v-if="enableSelection" width="50" type="selection"/>

      <ms-table-header-select-popover v-if="enableSelection" v-show="total > 0"
                                      :page-size="pageSize > total ? total : pageSize"
                                      :total="total"
                                      @selectPageAll="isSelectDataAll(false)"
                                      @selectAll="isSelectDataAll(true)"/>

      <el-table-column v-if="enableSelection && buttons && buttons.length > 0" width="40" :resizable="false" align="center">
        <template v-slot:default="scope">
          <show-more-btn :is-show="scope.row.showMore" :buttons="buttons" :size="selectDataCounts"/>
        </template>
      </el-table-column>

      <slot></slot>

      <ms-table-column
        v-if="operators && operators.length > 0"
        :width="operatorWidth"
        fixed="right"
        :label="$t('commons.operating')">
        <template slot="header">
          <header-label-operate @exec="openCustomHeader"/>
        </template>
        <template v-slot:default="scope">
          <ms-table-operators :buttons="operators" :row="scope.row" :index="scope.$index"/>
        </template>
      </ms-table-column>

    </el-table>

  </div>
</template>

<script>
import {
  _filter,
  _handleSelect,
  _handleSelectAll, _sort, getLabel,
  getSelectDataCounts,
  setUnSelectIds,
  toggleAllSelection
} from "@/common/js/tableUtils";
import MsTableHeaderSelectPopover from "@/business/components/common/components/table/MsTableHeaderSelectPopover";
import {TEST_CASE_LIST} from "@/common/js/constants";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import ShowMoreBtn from "@/business/components/api/automation/scenario/TableMoreBtn";
import MsTableColumn from "@/business/components/common/components/table/Ms-table-column";
import MsTableOperators from "@/business/components/common/components/MsTableOperators";

export default {
  name: "MsTable",
  components: {MsTableOperators, MsTableColumn, ShowMoreBtn, MsTablePagination, MsTableHeaderSelectPopover},
  data() {
    return {
      buttons: [],
      selectDataCounts: 0,
      selectRows: new Set(),
    }
  },
  props: {
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
    // 操作列的宽度
    operatorWidth: {
      type: String,
      default() {
        return '150';
      }
    },
    //开启全选
    enableSelection: {
      type: Boolean,
      default() {
        return true;
      }
    }
  },
  mounted() {
    getLabel(this, TEST_CASE_LIST);
  },
  computed: {
    selectIds() {
      return Array.from(this.selectRows).map(o => o.id);
    }
  },
  methods: {
    openCustomHeader() {
      this.$refs.headerCustom.open(this.tableLabel)
    },
    handleSelectAll(selection) {
      _handleSelectAll(this, selection, this.data, this.selectRows);
      setUnSelectIds(this.data, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
    },
    handleSelect(selection, row) {
      _handleSelect(this, selection, row, this.selectRows);
      setUnSelectIds(this.data, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
    },
    isSelectDataAll(data) {
      this.condition.selectAll = data;
      setUnSelectIds(this.data, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
      toggleAllSelection(this.$refs.table, this.data, this.selectRows);
    },
    headerDragend(newWidth, oldWidth, column, event) {
      // let finalWidth = newWidth;
      // if (column.minWidth > finalWidth) {
      //   finalWidth = column.minWidth;
      // }
      // column.width = finalWidth;
      // column.realWidth = finalWidth;
    },
    showPopover(row, column, cell) {
      if (column.property === 'name') {
        this.currentCaseId = row.id;
      }
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
      this.handleRefresh();
    },
    handleBatchEdit() {
      this.getMaintainerOptions();
      this.$refs.batchEdit.open();
    },
    handleBatchMove() {
      this.$refs.testBatchMove.open(this.treeNodes, Array.from(this.selectRows).map(row => row.id), this.moduleOptions);
    },
    handleRowClick() {

    },
    handleRefresh() {
      this.selectRows.clear();
      this.$emit('refresh')
    },
    handlePageChange() {
      this.$emit('pageChange');
    }
  }
}
</script>

<style scoped>

</style>
