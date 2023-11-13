<template>
  <el-row type="flex" justify="end">
    <div class="table-page">
      <el-pagination
        class="home-pagination"
        background
        :pager-count="5"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="pageSizes"
        :page-size="pageSize"
        :layout="layout"
        :total="total">
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
      default: 1
    },
    pageSize: {
      type: Number,
      default: 5
    },
    pageSizes: {
      type: Array,
      default: function () {
        return [5, 10, 20, 50]
      }
    },
    total: {
      type: Number,
      default: 0
    },
    layout: {
      type: String,
      default() {
        return 'total, sizes, prev, pager, next, jumper';
      }
    },
    change: Function
  },
  data() {
    return {
      timer: null
    };
  },
  methods: {
    handleSizeChange: function (size) {
      this.$emit('update:pageSize', size);
      if (this.timer === null) {
        this.timer = setTimeout(() => {
          this.change();
          this.timer = null;
        }, 0);
      }
    },
    handleCurrentChange(current) {
      this.$emit('update:currentPage', current);
      if (this.timer === null) {
        this.timer = setTimeout(() => {
          this.change();
          this.timer = null;
        }, 0);
      }
    }
  }
}
</script>

<style scoped>
.table-page {
  padding-top: 10px;
}

.table-page {
  padding-top: 10px;
}

:deep(.home-pagination.el-pagination.is-background .el-pager li) {
  background-color: #FFFFFF;
  color: #1F2329;
  border: 1px solid #BBBFC4;
  height: 28px;
  box-sizing: border-box;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 400;
  line-height: 26px;
}

:deep(.home-pagination.el-pagination.is-background .el-pager li:not(.disabled).active) {
  background-color: #FFFFFF;
  color: #783887;
  border: 1px solid #783887;
}
:deep(.home-pagination.el-pagination.is-background .btn-next,) {
  background-color: #FFFFFF;
  border: 1px solid #BBBFC4;
  width: 28px;
  height: 28px;
  box-sizing: border-box;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 400;
  line-height: 26px;
}
:deep(.home-pagination.el-pagination.is-background .btn-prev) {
  background-color: #FFFFFF;
  border: 1px solid #BBBFC4;
  width: 28px;
  height: 28px;
  box-sizing: border-box;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 400;
  line-height: 26px;
}
</style>
