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
      v-model:input-value="inputValue"
      :data="props.data"
      :disabled="props.disabled"
      :multiple="props.multiple"
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
      @input-value-change="handleInputValueChange"
      @popup-visible-change="handlePopupVisibleChange"
      @change="handleChange"
      @keyup="handleKeyup"
      @clear="handleClear"
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
    multiple?: boolean;
  }>();
  const selectValue = defineModel<any>('modelValue', { required: true });

  const inputValue = ref('');
  const tempInputValue = ref('');

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

  /**
   * 处理输入框搜索值变化
   * @param val 搜索值
   */
  function handleInputValueChange(val: string) {
    inputValue.value = val;
    if (val !== '') {
      // 只存储有值的搜索值，因为当搜索完选中一个选项后，arco-tree-select 会自动清空输入框，这里需要过滤掉
      tempInputValue.value = val;
    }
  }
  function handlePopupVisibleChange(val: boolean) {
    if (!val) {
      tempInputValue.value = '';
    }
  }
  function handleChange() {
    if (props.multiple) {
      nextTick(() => {
        inputValue.value = tempInputValue.value;
      });
    }
  }
  function handleKeyup(e: KeyboardEvent) {
    if (e.code === 'Backspace' && inputValue.value === '') {
      tempInputValue.value = '';
    }
  }
  function handleClear() {
    tempInputValue.value = '';
  }
</script>

<style lang="less">
  .tree-select-content {
    overflow-y: auto;
    max-height: 150px;
    .ms-scroll-bar();
  }
</style>
