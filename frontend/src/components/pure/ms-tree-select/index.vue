<template>
  <a-tooltip
    :content="getTreeSelectTooltip()"
    :disabled="!(checkedKeys ?? []).length"
    position="top"
    content-class="tree-select-content"
    :mouse-enter-delay="300"
  >
    <a-trigger
      v-model:popup-visible="viewSelectOptionVisible"
      :trigger="['click']"
      :click-to-close="false"
      :popup-translate="[0, 4]"
      content-class="arco-trigger-menu tree-select-trigger-content"
      :content-style="{ width: `${triggerWidth}px` }"
    >
      <a-tree-select
        ref="treeSelectRef"
        v-model:model-value="checkedKeys"
        v-model:input-value="inputValue"
        :data="treeData"
        :max-tag-count="maxTagCount"
        :multiple="props.multiple"
        allow-search
        disable-filter
        allow-clear
        :tree-props="{
          virtualListProps: virtualListProps,
        }"
        :field-names="props.fieldNames"
        :placeholder="t('common.pleaseSelect')"
        :trigger-props="{ contentClass: 'view-select-trigger' }"
        @popup-visible-change="handlePopupVisibleChange"
        @input-value-change="handleInputValueChange"
        @change="handleChange"
        @keyup="handleKeyup"
        @clear="handleClear"
      >
        <template #label="{ data: slotData }">
          <div class="one-line-text">{{ slotData.label }}</div>
        </template>
      </a-tree-select>
      <template #content>
        <MsTree
          v-model:checked-keys="checkedKeys"
          v-model:halfCheckedKeys="halfCheckedKeys"
          :selectable="false"
          :data="treeData"
          :keyword="inputValue"
          :empty-text="t('common.noData')"
          :virtual-list-props="virtualListProps"
          :field-names="props.fieldNames as MsTreeFieldNames"
          default-expand-all
          block-node
          title-tooltip-position="tr"
          :multiple="props.multiple"
          :checkable="props.treeCheckable"
          :check-strictly="props.treeCheckStrictly"
          v-bind="$attrs"
          @check="checkNode"
        >
          <template #title="nodeData">
            <div
              class="one-line-text w-full cursor-pointer text-[var(--color-text-1)]"
              @click="checkNode(checkedKeys, { checked: !checkedKeys.includes(nodeData.id), node: nodeData })"
            >
              {{ nodeData.name }}
            </div>
          </template>
          <template #extra="nodeData">
            <MsButton
              v-if="nodeData.children && nodeData.children.length"
              @click="selectParent(nodeData, !!checkedKeys.includes(nodeData.id))"
            >
              {{
                checkedKeys.includes(nodeData.id)
                  ? t('ms.case.associate.cancelCurrent')
                  : t('ms.case.associate.selectCurrent')
              }}
            </MsButton>
          </template>
        </MsTree>
      </template>
    </a-trigger>
  </a-tooltip>
</template>

<script lang="ts" setup>
  import { isEqual } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import useTreeSelection from '@/components/business/ms-associate-case/useTreeSelection';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeFieldNames, MsTreeNodeData } from '@/components/business/ms-tree/types';

  import { useI18n } from '@/hooks/useI18n';
  import useSelect from '@/hooks/useSelect';
  import { findNodeByKey } from '@/utils';

  import type { TreeFieldNames, TreeNodeData } from '@arco-design/web-vue';

  const props = withDefaults(
    defineProps<{
      data: TreeNodeData[];
      fieldNames?: TreeFieldNames | MsTreeFieldNames;
      multiple?: boolean;
      shouldCalculateMaxTag?: boolean;
      treeCheckStrictly?: boolean;
      treeCheckable?: boolean;
    }>(),
    {
      shouldCalculateMaxTag: true,
    }
  );

  const { t } = useI18n();

  const viewSelectOptionVisible = ref(false);

  const selectValue = defineModel<Array<string | number>>('modelValue', { required: true, default: [] });
  const treeData = defineModel<TreeNodeData[]>('data', { required: true });
  const selectedModuleProps = ref({
    modulesTree: treeData.value,
    moduleCount: {},
  });
  const { selectedModulesMaps, checkedKeys, halfCheckedKeys, selectParent, checkNode, clearSelector } =
    useTreeSelection(selectedModuleProps.value);

  const skipSelectValueWatch = ref(false);
  watch(
    () => selectValue.value,
    (newValue) => {
      if (!skipSelectValueWatch.value && !isEqual(checkedKeys.value, newValue)) {
        clearSelector();
        (newValue ?? []).forEach((id) => {
          selectedModulesMaps.value[id] = {
            selectAll: true,
            selectIds: new Set(),
            excludeIds: new Set(),
            count: 0,
          };
        });
      }
      skipSelectValueWatch.value = false;
    },
    {
      immediate: true,
    }
  );

  const treeSelectRef = ref();
  const { maxTagCount, calculateMaxTag } = useSelect({
    selectRef: treeSelectRef,
    selectVal: checkedKeys,
  });
  watch(
    () => checkedKeys.value,
    (newValue) => {
      if (!isEqual(selectValue.value, newValue)) {
        // 赋值selectValue，但是不触发watch selectValue
        skipSelectValueWatch.value = true;
        selectValue.value = [...newValue];
      }
      if (props.shouldCalculateMaxTag !== false && props.multiple) {
        calculateMaxTag();
      }
    },
    {
      immediate: true,
    }
  );
  watch(
    () => props.data,
    () => {
      if (props.shouldCalculateMaxTag !== false && props.multiple) {
        calculateMaxTag();
      }
    }
  );

  const getTreeSelectTooltip = computed(() => {
    return () => {
      let treeSelectTooltip = '';
      const values = Array.isArray(checkedKeys.value) ? checkedKeys.value : [checkedKeys.value];
      if (props.data) {
        treeSelectTooltip = values
          ?.map((valueItem: string | number) => {
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

  const inputValue = ref('');
  const tempInputValue = ref('');

  const isPopupVisibleChanging = ref(false);
  function handlePopupVisibleChange() {
    isPopupVisibleChanging.value = true;
    setTimeout(() => {
      isPopupVisibleChanging.value = false;
    }, 0);
  }

  /**
   * 处理输入框搜索值变化
   * @param val 搜索值
   */
  async function handleInputValueChange(val: string) {
    // 搜索后选中一个节点，treeSelect组件会触发popupVisibleChange，进而清空inputValue，这时需要手动将inputValue还原
    if (isPopupVisibleChanging.value) {
      inputValue.value = tempInputValue.value;
    } else {
      inputValue.value = val;
      if (val !== '') {
        // 只存储有值的搜索值
        tempInputValue.value = val;
      }
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
    clearSelector();
  }

  const triggerWidth = ref<number>(280); // 下拉框的宽度
  function updateTriggerWidth() {
    const treeSelectInput = treeSelectRef.value?.$el.nextElementSibling as HTMLElement;
    if (treeSelectInput) {
      triggerWidth.value = treeSelectInput.offsetWidth; // 设置成输入框的宽度
    }
  }

  watch(
    () => viewSelectOptionVisible.value,
    (val: boolean) => {
      if (!val) {
        inputValue.value = '';
        tempInputValue.value = '';
      } else {
        updateTriggerWidth();
      }
    }
  );

  const virtualListProps = computed(() => {
    return {
      threshold: 300,
      height: 280,
      fixedSize: true,
      buffer: 15, // 缓冲区默认 10 的时候，虚拟滚动的底部 padding 计算有问题
    };
  });
</script>

<style lang="less">
  .tree-select-content {
    overflow-y: auto;
    max-height: 150px;
    .ms-scroll-bar();
  }
  .tree-select-trigger-content {
    max-height: 300px;
    .ms-scroll-bar();
  }
</style>
