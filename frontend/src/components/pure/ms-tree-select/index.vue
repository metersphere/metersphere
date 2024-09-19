<template>
  <a-tooltip
    :content="getTreeSelectTooltip()"
    :disabled="!(modelValue ?? []).length"
    position="top"
    content-class="tree-select-content"
    :mouse-enter-delay="300"
  >
    <a-tree-select
      v-model:model-value="selectValue"
      :data="props.data"
      :disabled="props.disabled"
      :field-names="props.fieldNames"
      allow-search
      :filter-tree-node="filterTreeNode"
      :tree-props="{
        virtualListProps: {
          height: 200,
          threshold: 200,
        },
      }"
      v-bind="$attrs"
    >
      <template #label="{ data: slotData }">
        <div class="one-line-text">{{ slotData.label }}</div>
      </template>
      <template #tree-slot-title="node">
        <a-tooltip :content="`${node[props?.fieldNames?.title || 'title']}`" position="tr">
          <div class="one-line-text max-w-[170px]">{{ node[props?.fieldNames?.title || 'title'] }}</div>
        </a-tooltip>
      </template>
    </a-tree-select>
  </a-tooltip>
</template>

<script lang="ts" setup>
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';

  import { filterTreeNode, findNodeByKey } from '@/utils';

  import type { TreeFieldNames, TreeNodeData } from '@arco-design/web-vue';

  const props = defineProps<{
    data: TreeNodeData[];
    fieldNames?: TreeFieldNames;
    disabled?: boolean;
  }>();
  const selectValue = defineModel<any>('modelValue', { required: true });

  const getTreeSelectTooltip = computed(() => {
    return () => {
      let treeSelectTooltip = '';
      const values = Array.isArray(selectValue.value) ? selectValue.value : [selectValue.value];
      if (props.data) {
        treeSelectTooltip = values
          ?.map((valueItem: string) => {
            const optItem = findNodeByKey<MsTreeNodeData>(
              props.data as MsTreeNodeData[],
              valueItem,
              props?.fieldNames?.key
            );
            return optItem ? optItem[props?.fieldNames?.title || 'title'] : '';
          })
          .filter(Boolean) // 过滤掉假值
          .join('，');
      }

      return treeSelectTooltip;
    };
  });
</script>

<style lang="less">
  .tree-select-content {
    overflow-y: auto;
    max-height: 150px;
    .ms-scroll-bar();
  }
</style>
