<template>
  <el-table-column
      v-if="!field || field.id === prop"
      :min-width="minWidth"
      :width="fieldsWidth ? fieldsWidth[prop] : width"
      :fixed="fixed"
      :filters="filters"
      :prop="prop"
      :column-key="columnKey ? columnKey : prop"
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
    minWidth: [String, Number],
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
    // 判断是否显示自定义列
    field: {
      type: Object,
      default() {
        return null;
      }
    },
    // 保存自定义列宽
    fieldsWidth: Object,
    columnKey: {
      type: String,
      default() {
        return null;
      }
    }
  }
};
</script>

<style scoped>

</style>
