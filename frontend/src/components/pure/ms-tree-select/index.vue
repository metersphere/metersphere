<template>
  <div class="w-full">
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
            v-model:focus-node-key="focusNodeKey"
            :data="treeData"
            :selectable="false"
            :keyword="inputValue"
            :empty-text="t('common.noData')"
            :virtual-list-props="virtualListProps"
            :field-names="props.fieldNames as MsTreeFieldNames"
            :node-disabled-tip="t('project.environmental.http.nodeDisabledTip')"
            default-expand-all
            block-node
            title-tooltip-position="tr"
            :multiple="props.multiple"
            :checkable="props.treeCheckable"
            :check-strictly="props.treeCheckStrictly"
            v-bind="$attrs"
            @check="handleCheck"
          >
            <template #title="nodeData">
              <div
                class="one-line-text w-full cursor-pointer text-[var(--color-text-1)]"
                @click="handleCheck(checkedKeys, { checked: !checkedKeys.includes(nodeData.id), node: nodeData })"
              >
                {{ nodeData.name }}
              </div>
            </template>
            <template #extra="nodeData">
              <MsButton
                v-if="nodeData.children && nodeData.children.length && !nodeData.disabled"
                class="!mr-[8px]"
                @click="handleSelectCurrent(nodeData)"
              >
                {{
                  checkedKeys.includes(nodeData.id)
                    ? t('ms.case.associate.cancelCurrent')
                    : t('ms.case.associate.selectCurrent')
                }}
              </MsButton>
              <MoreMenuDropdown
                v-if="props.showContainChildModule && !nodeData.disabled && nodeData.id !== 'root'"
                v-model:contain-child-module="nodeData.containChildModule"
                @handle-contain-child-module="
                  (containChildModule) => handleContainChildModule(nodeData, containChildModule)
                "
                @close="resetFocusNodeKey"
                @open="setFocusKey(nodeData)"
              />
            </template>
          </MsTree>
        </template>
      </a-trigger>
    </a-tooltip>
  </div>
</template>

<script lang="ts" setup>
  import { isEqual } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import useTreeSelection from '@/components/business/ms-associate-case/useTreeSelection';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeFieldNames, MsTreeNodeData } from '@/components/business/ms-tree/types';
  import MoreMenuDropdown from './moreMenuDropdown.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useSelect from '@/hooks/useSelect';
  import { findNodeByKey, mapTree } from '@/utils';

  import type { TreeFieldNames, TreeNodeData } from '@arco-design/web-vue';
  import { LabelValue } from '@arco-design/web-vue/es/tree-select/interface';

  const props = withDefaults(
    defineProps<{
      fieldNames?: TreeFieldNames | MsTreeFieldNames;
      multiple?: boolean;
      shouldCalculateMaxTag?: boolean;
      treeCheckStrictly?: boolean;
      treeCheckable?: boolean;
      showContainChildModule?: boolean;
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

  // 设置子节点的 containChildModule 和 disabled 属性值
  function updateChildNodesState(node: MsTreeNodeData, state: boolean) {
    if (node.children) {
      node.children.forEach((child: MsTreeNodeData) => {
        child.containChildModule = state;
        child.disabled = state;
        updateChildNodesState(child, state);
      });
    }
  }

  function handleCheck(_checkedKeys: Array<string | number>, checkedNodes: MsTreeNodeData) {
    if (props.showContainChildModule) {
      if (checkedNodes.node.disabled) return;
      const realNode = findNodeByKey<MsTreeNodeData>(treeData.value, checkedNodes.node.id, 'id');
      if (!realNode) return;
      if (checkedNodes.checked) {
        // 父级勾选，且父级“包含新增子模块”勾选，那么下面所有子级：禁用和勾选“包含新增子模块”
        if (realNode.containChildModule) {
          updateChildNodesState(realNode, true);
        }
      } else {
        // 父级取消勾选，父级和所有子级“包含新增子模块”取消勾选，所有子级取消禁用
        realNode.containChildModule = false;
        updateChildNodesState(realNode, false);
      }
    }
    checkNode(_checkedKeys, checkedNodes);
  }

  // 当前节点“包含新增子模块”取消勾选，下面一层的子级取消禁用
  function updateNodeState(nodeId: string | number) {
    const realNode = findNodeByKey<MsTreeNodeData>(treeData.value, nodeId, 'id');
    if (!realNode) return;
    realNode.containChildModule = false;
    realNode.children?.forEach((child) => {
      child.disabled = false;
    });
  }

  function handleSelectCurrent(nodeData: MsTreeNodeData) {
    if (props.showContainChildModule && checkedKeys.value.includes(nodeData.id)) {
      // 取消当前
      updateNodeState(nodeData.id);
    }
    selectParent(nodeData, !!checkedKeys.value.includes(nodeData.id));
  }

  function handleContainChildModule(nodeData: MsTreeNodeData, containChildModule: boolean) {
    const realNode = findNodeByKey<MsTreeNodeData>(treeData.value, nodeData.id, 'id');
    if (!realNode) return;
    realNode.containChildModule = containChildModule;
    if (containChildModule) {
      handleCheck(checkedKeys.value, { checked: true, node: realNode });
    } else {
      realNode.children?.forEach((child) => {
        child.disabled = false;
      });
    }
  }

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
    () => treeData.value,
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
      if (treeData.value) {
        treeSelectTooltip = values
          ?.map((valueItem: string | number) => {
            const optItem = findNodeByKey<MsTreeNodeData>(
              treeData.value as MsTreeNodeData[],
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
  function handleChange(val: string | number | LabelValue | Array<string | number> | LabelValue[] | undefined) {
    if (props.multiple) {
      nextTick(() => {
        inputValue.value = tempInputValue.value;
      });
      if (props.showContainChildModule) {
        const deletedIds = selectValue.value.filter(
          (item: string | number) => !(val as (string | number)[]).includes(item)
        );
        // 如果是输入框删除一个
        if (deletedIds.length === 1) {
          updateNodeState(deletedIds[0]);
        }
      }
    }
  }
  function handleKeyup(e: KeyboardEvent) {
    if (e.code === 'Backspace' && inputValue.value === '') {
      tempInputValue.value = '';
    }
  }
  function handleClear() {
    if (props.showContainChildModule) {
      treeData.value = mapTree<TreeNodeData>(treeData.value, (node) => {
        return {
          ...node,
          containChildModule: false,
          disabled: false,
        };
      });
    }
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

  const focusNodeKey = ref<string | number>('');
  function setFocusKey(node: MsTreeNodeData) {
    focusNodeKey.value = node.id || '';
  }
  function resetFocusNodeKey() {
    focusNodeKey.value = '';
  }
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
