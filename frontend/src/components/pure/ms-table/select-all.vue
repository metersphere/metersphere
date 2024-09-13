<template>
  <div class="ms-table-select-all">
    <a-checkbox
      v-model:model-value="checked"
      :disabled="props.disabled"
      class="text-base"
      :indeterminate="indeterminate"
      @change="handleCheckChange"
    />
    <a-dropdown
      v-if="props.showSelectAll"
      :disable="props.disabled"
      position="bl"
      @select="(v) => handleSelect(v as SelectAllEnum)"
    >
      <div>
        <MsIcon type="icon-icon_down_outlined" class="dropdown-icon" />
      </div>
      <template #content>
        <a-doption :value="SelectAllEnum.CURRENT">{{ t('msTable.current') }}</a-doption>
        <a-doption v-if="selectAllStatus === SelectAllEnum.ALL" :value="SelectAllEnum.CANCEL_ALL">
          {{ t('msTable.cancelAll') }}
        </a-doption>
        <a-doption v-else :value="SelectAllEnum.ALL">{{ t('msTable.all') }}</a-doption>
      </template>
    </a-dropdown>
  </div>
</template>

<script lang="ts" setup>
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { SelectAllEnum } from '@/enums/tableEnum';

  import { MsTableDataItem, MsTableRowSelectionDisabledConfig } from './type';

  const { t } = useI18n();

  const emit = defineEmits<{
    (e: 'change', value: SelectAllEnum, onlyCurrent: boolean): void;
  }>();

  const props = withDefaults(
    defineProps<{
      selectedKeys: Set<string>;
      total: number;
      currentData: MsTableDataItem<Record<string, any>>[];
      showSelectAll: boolean;
      disabled: boolean;
      excludeKeys: string[];
      rowKey?: string;
      rowSelectionDisabledConfig?: MsTableRowSelectionDisabledConfig;
    }>(),
    {
      current: 0,
      total: 0,
      showSelectAll: true,
      disabled: false,
      rowKey: 'id',
    }
  );

  // 是否具备子级children
  const isHasChildren = computed(() => props.currentData.some((item) => item.children));

  // 获取数据第一层级的ids，用来判断全选或者半选
  const firstLevelAllIds = computed(() => {
    if (isHasChildren.value) {
      return props.currentData.map((item) => item[props.rowKey]);
    }
    return [];
  });

  const selectAllStatus = ref<SelectAllEnum>(SelectAllEnum.NONE);
  const checked = computed({
    get: () => {
      // 如果是选中所有页则是全选状态（选中所有页分两种情况：一是直接通过下拉选项选中所有页；二是当前已选的数量等于表格总数）
      // 非子级全选条件
      if (!isHasChildren.value) {
        return (
          (props.selectedKeys.size > 0 && selectAllStatus.value === SelectAllEnum.ALL) ||
          (props.selectedKeys.size > 0 && props.selectedKeys.size === props.total) ||
          (props.selectedKeys.size > 0 &&
            props.selectedKeys.size > props.total &&
            props.currentData.length === props.total &&
            props.currentData.every((e) => props.selectedKeys.has(e[props.rowKey])))
        );
      }
      // 含有子级 children全选条件
      return firstLevelAllIds.value.every((item) => props.selectedKeys.has(item));
    },
    set: (value) => {
      return value;
    },
  });
  const indeterminate = computed(() => {
    // 有无勾选的 key且是全选所有页，或非全选所有页且已选中的数量大于 0 且小于总数时是半选状态
    // 非子级半选条件
    if (!isHasChildren.value) {
      return (
        (props.excludeKeys.length > 0 && selectAllStatus.value === SelectAllEnum.ALL) ||
        (selectAllStatus.value !== SelectAllEnum.ALL &&
          props.selectedKeys.size > 0 &&
          props.selectedKeys.size < props.total)
      );
    }
    // 包含子级半选条件
    const isSomeSelected = firstLevelAllIds.value.some((key) => props.selectedKeys.has(key));
    const isEverySelected = firstLevelAllIds.value.every((key) => props.selectedKeys.has(key));

    if (isSomeSelected && !isEverySelected) {
      return true;
    }
    return false;
  });

  const handleSelect = (v: SelectAllEnum, onlyCurrent = true) => {
    if (
      (selectAllStatus.value === SelectAllEnum.ALL &&
        v === SelectAllEnum.NONE &&
        props.excludeKeys.length < props.total) ||
      (selectAllStatus.value === SelectAllEnum.ALL && v === SelectAllEnum.CURRENT && !onlyCurrent)
    ) {
      // 如果当前是全选所有页状态，且是取消选中当前页操作，且排除项小于总数，则保持跨页全选状态
      // 如果当前是全选所有页状态，且是选中当前页操作(是点击全选的多选框，非下拉菜单全选当前页)，则保持跨页全选状态
      selectAllStatus.value = SelectAllEnum.ALL;
    } else {
      selectAllStatus.value = v;
    }
    emit('change', v, onlyCurrent);
  };

  function hasUnselectedChildren(
    data: MsTableDataItem<Record<string, any>>[],
    selectedKeys: Set<string>,
    rowKey: string
  ): boolean {
    return data.some((item: any) => {
      if (item.children && item.children.length > 0 && !props.rowSelectionDisabledConfig?.disabledChildren) {
        return hasUnselectedChildren(item.children, selectedKeys, rowKey);
      }
      // 有数据没有勾选上，且该数据没有被禁用
      return (
        !selectedKeys.has(item[rowKey]) &&
        !(props?.rowSelectionDisabledConfig?.disabledKey && item[props?.rowSelectionDisabledConfig?.disabledKey])
      );
    });
  }

  const handleCheckChange = () => {
    if (hasUnselectedChildren(props.currentData, props.selectedKeys, props.rowKey)) {
      // 当前页有数据没有勾选上，且该数据没有被禁用，此时点击全选按钮代表全部选中
      handleSelect(SelectAllEnum.CURRENT, false);
    } else {
      // 否则是当前页全部数据已勾选，此时点击全选按钮代表取消当前页面数据勾选
      handleSelect(SelectAllEnum.NONE);
    }
  };

  watchEffect(() => {
    // 表格清空已选时，判断排除项也为空则是重置全选状态
    if (props.excludeKeys.length === 0 && props.selectedKeys.size === 0) {
      selectAllStatus.value = SelectAllEnum.NONE;
    }
  });
</script>

<style lang="less" scoped>
  .ms-table-select-all {
    display: flex;
    flex-flow: row nowrap;
    align-items: center;
    .dropdown-icon {
      margin-left: 4px;
      font-size: 12px;
      border-radius: 50%;
      color: var(--color-text-4);
      line-height: 16px;
    }
    .dropdown-icon:hover {
      color: rgb(var(--primary-5));
    }
  }
</style>
