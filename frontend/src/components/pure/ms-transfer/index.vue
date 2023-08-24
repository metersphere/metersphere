<template>
  <a-transfer
    v-model="innerTarget"
    :title="props.title || [t('system.user.batchOptional'), t('system.user.batchChosen')]"
    :data="transferData"
    show-search
  >
    <template #source="{ data: tData, selectedKeys, onSelect }">
      <a-tree
        :checkable="true"
        checked-strategy="child"
        :checked-keys="selectedKeys"
        :data="getTreeData(tData)"
        block-node
        @check="onSelect"
      />
    </template>
  </a-transfer>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { useI18n } from '@/hooks/useI18n';

  export interface TreeDataItem {
    key?: string;
    title?: string;
    children?: TreeDataItem[];
    disabled?: boolean;
    [key: string]: any;
  }

  export interface TransferDataItem {
    value: string;
    label: string;
    disabled: boolean;
    [key: string]: any;
  }

  const props = withDefaults(
    defineProps<{
      modelValue: string[];
      title?: string[]; // [left, right]左右标题
      data: TreeDataItem[]; // 树结构数据
      treeFiled?: Record<keyof TreeDataItem, string>; // 自定义树结构字段
    }>(),
    {
      treeFiled: () => ({
        key: 'key',
        title: 'title',
        children: 'children',
        disabled: 'disabled',
      }),
    }
  );
  const emit = defineEmits(['update:modelValue']);

  const { t } = useI18n();

  const innerTarget = ref<string[]>([]);
  const transferData = ref<TransferDataItem[]>([]);

  watch(
    () => props.modelValue,
    (val) => {
      innerTarget.value = val;
    }
  );

  watch(
    () => innerTarget.value,
    (val) => {
      emit('update:modelValue', val);
    }
  );

  /**
   * 获取穿梭框数据，根据树结构获取
   * @param _treeData 树结构
   * @param transferDataSource 穿梭框数组
   */
  const getTransferData = (_treeData: TreeDataItem[], transferDataSource: TransferDataItem[]) => {
    _treeData.forEach((item) => {
      const itemChildren = item[props.treeFiled.children];
      if (Array.isArray(itemChildren) && itemChildren.length > 0) getTransferData(itemChildren, transferDataSource);
      else
        transferDataSource.push({
          label: item[props.treeFiled.title],
          value: item[props.treeFiled.key],
          disabled: item[props.treeFiled.disabled],
        });
    });
    return transferDataSource;
  };

  /**
   * 获取树结构数据，根据穿梭框过滤的数据获取
   */
  const getTreeData = (data: TransferDataItem[]) => {
    const values = data.map((item) => item[props.treeFiled.key]);

    const travel = (_treeData: TreeDataItem[]) => {
      const treeDataSource: TreeDataItem[] = [];
      _treeData.forEach((item) => {
        const itemChildren = item[props.treeFiled.children];
        const itemKey = item[props.treeFiled.key];
        const itemTitle = item[props.treeFiled.title];
        // 需要判断当前父节点下的子节点是否全部选中，若选中则不会 push 进穿梭框数组内，否则会出现空的节点无法选中
        const allSelected =
          innerTarget.value.length > 0 &&
          Array.isArray(itemChildren) &&
          itemChildren.length > 0 &&
          itemChildren?.every((child: TreeDataItem) => innerTarget.value.includes(child[props.treeFiled.key]));
        if (!allSelected && !innerTarget.value.includes(itemKey) && (itemChildren || values.includes(itemKey))) {
          // 非选中父节点时，需要判断每个子节点是否已经在右侧的选中的数组内，不在才渲染到左侧
          treeDataSource.push({
            title: itemTitle,
            key: itemKey,
            children: itemChildren ? travel(itemChildren) : [],
          });
        }
      });
      return treeDataSource;
    };

    return travel(props.data);
  };

  watch(
    () => props.data,
    (arr) => {
      transferData.value = getTransferData(arr, []);
    }
  );
</script>

<style lang="less" scoped></style>
