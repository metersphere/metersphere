<template>
  <el-row type="flex" justify="end">
    <div class="home-table-page">
      <el-pagination
        background
        :pager-count="5"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="pageSizes"
        :page-size="pageSize"
        :layout="layout"
        :total="total"
      >
      </el-pagination>
    </div>
  </el-row>
</template>

<script>
export default {
  name: "MsTablePagination",
  props: {
    page: Object,
    currentPage: {
      type: Number,
      default: 1,
    },
    pageSize: {
      type: Number,
      default: 5,
    },
    layout: {
      type: String,
      default() {
        return "total, sizes, prev, pager, next, jumper";
      },
    },
    pageSizes: {
      type: Array,
      default: function () {
        return [5, 10, 20, 50];
      },
    },
    total: {
      type: Number,
      default: 0,
    },
    change: Function,
  },
  methods: {
    handleSizeChange: function (size) {
      this.$emit("update:pageSize", size);
      this.change();
    },
    handleCurrentChange(current) {
      this.$emit("update:currentPage", current);
      this.change();
    },
  },
};
</script>

<style scoped>
.home-table-page {
  padding-top: 10px;
}

.home-table-page :deep(.el-pagination.is-background .el-pager li) {
  background-color: #ffffff;
  color: #1f2329;
  border: 1px solid #bbbfc4;
  width: 28px;
  height: 28px;
  box-sizing: border-box;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 400;
  line-height: 26px;
}

.home-table-page
  :deep(.el-pagination.is-background .el-pager li:not(.disabled).active) {
  background-color: #ffffff;
  color: #783887;
  border: 1px solid #783887;
}

.home-table-page :deep(.el-pagination.is-background .btn-next, ) {
  background-color: #ffffff;
  border: 1px solid #bbbfc4;
  width: 28px;
  height: 28px;
  box-sizing: border-box;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 400;
  line-height: 26px;
}

.home-table-page :deep(.el-pagination.is-background .btn-prev) {
  background-color: #ffffff;
  border: 1px solid #bbbfc4;
  width: 28px;
  height: 28px;
  box-sizing: border-box;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 400;
  line-height: 26px;
}
</style>
