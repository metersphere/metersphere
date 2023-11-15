<template>
  <el-row type="flex" justify="end">
    <div class="table-page">
      <el-pagination
        background
        :pager-count="5"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="pageSizes"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
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
    change: Function,
    changeSize: Function,
    changeCurrent: Function
  },
  methods: {
    handleSizeChange: function (size) {
      this.$emit('update:pageSize', size)
      if (this.change) {
        this.change();
      }
      if (this.changeSize) {
        this.changeSize();
      }
    },
    handleCurrentChange(current) {
      this.$emit('update:currentPage', current)
      if (this.change) {
        this.change();
      }
      if (this.changeCurrent) {
        this.changeCurrent();
      }
    }
  }
}
</script>

<style scoped>
.table-page {
  padding-top: 10px;
}
</style>
