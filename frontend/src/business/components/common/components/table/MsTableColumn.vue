<template>
  <el-table-column class-name="msTableColumn"
      v-if="active && (!field || field.id === prop)"
      :min-width="minWidth"
      :width="fieldsWidth ? fieldsWidth[prop] : width"
      :fixed="fixed"
      :filters="filters"
      :prop="prop"
      :column-key="columnKey ? columnKey : prop"
      :label="label"
      :sortable="sortable"
      :filter-method="filterMethod"
      :show-overflow-tooltip="showOverflowTooltip">
    <template v-slot:default="scope">
      <slot :row="scope.row" :$index="scope.$index" v-if="!editable">
        <span @click="$emit('click', scope.row)">{{ scope.row[prop] }}</span>
      </slot>
      <slot :row="scope.row" :$index="scope.$index" v-if="editable">
        <span style="cursor: pointer;" @click="$emit('click', scope.row)">{{ scope.row[prop] }}</span>
        <el-tooltip :content="editContent ? editContent : $t('commons.edit')"
                    @click.native.stop="$emit('editColumn', scope.row)">
          <a style="cursor: pointer">
            <i style="cursor:pointer" class="el-input__icon el-icon-edit pointer"></i>
          </a>
        </el-tooltip>
      </slot>
    </template>
  </el-table-column>
</template>

<script>
export default {
  name: "Ms-table-column",
  data() {
    return {
      active: false
    }
  },
  props: {
    prop: String,
    label: String,
    width: [String, Number],
    minWidth: [String, Number],
    filterMethod: Function,
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
    },
    editable: {
      type: Boolean,
      default: false
    },
    editContent: {
      type: String,
      default: null
    },
  },
  mounted() {
    this.active = true;
  }
};
</script>

<style scoped>

</style>
