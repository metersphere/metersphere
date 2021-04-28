<template>
  <el-table-column
      v-if="fields.has(prop) || fields.size < 1"
      :min-width="minWidth"
      :width="width"
      :fixed="fixed"
      :filters="filters"
      :prop="prop"
      :column-key="prop"
      :label="label"
      :sortable="sortable"
      :show-overflow-tooltip="showOverflowTooltip">
    <template v-slot:default="scope">
      <slot :row="scope.row" :$index="scope.$index">
        {{scope.row[prop]}}
      </slot>
    </template>
  </el-table-column>
</template>

<script>
export default {
  name: "Ms-table-column",
  props: {
    prop: String,
    label: String,
    width: String,
    minWidth: String,
    fixed: String,
    // 排序列， 后端mapper处理filters
    filters: Array,
    showOverflowTooltip: {
      type: Boolean,
      default() {
        return true;
      }
    },
    // 开启排序，后端mapper添加ExtBaseMapper.orders
    sortable: {
      type: [Boolean, String],
      default() {
        return false;
      }
    },
    fields: {
      type: Set,
      default() {
        return new Set();
      }
    },
  }
};
</script>

<style scoped>

</style>
