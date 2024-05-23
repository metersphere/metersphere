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
        <a-doption :value="SelectAllEnum.ALL">{{ t('msTable.all') }}</a-doption>
      </template>
    </a-dropdown>
  </div>
</template>

<script lang="ts" setup>
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { SelectAllEnum } from '@/enums/tableEnum';

  import { MsTableDataItem } from './type';

  const { t } = useI18n();

  const emit = defineEmits<{
    (e: 'change', value: SelectAllEnum): void;
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
    }>(),
    {
      current: 0,
      total: 0,
      showSelectAll: true,
      disabled: false,
      rowKey: 'id',
    }
  );

  const selectAllStatus = ref<SelectAllEnum>(SelectAllEnum.NONE);
  const checked = computed({
    get: () => {
      // 如果是选中所有页则是全选状态（选中所有页分两种情况：一是直接通过下拉选项选中所有页；二是当前已选的数量等于表格总数）
      return (
        (props.selectedKeys.size > 0 && selectAllStatus.value === SelectAllEnum.ALL) ||
        (props.selectedKeys.size > 0 && props.selectedKeys.size === props.total)
      );
    },
    set: (value) => {
      return value;
    },
  });
  const indeterminate = computed(() => {
    // 有无勾选的 key且是全选所有页，或非全选所有页且已选中的数量大于 0 且小于总数时是半选状态
    return (
      (props.excludeKeys.length > 0 && selectAllStatus.value === SelectAllEnum.ALL) ||
      (selectAllStatus.value !== SelectAllEnum.ALL &&
        props.selectedKeys.size > 0 &&
        props.selectedKeys.size < props.total)
    );
  });

  const handleSelect = (v: SelectAllEnum) => {
    if (
      (selectAllStatus.value === SelectAllEnum.ALL &&
        v === SelectAllEnum.NONE &&
        props.excludeKeys.length < props.total) ||
      (selectAllStatus.value === SelectAllEnum.ALL && v === SelectAllEnum.CURRENT)
    ) {
      // 如果当前是全选所有页状态，且是取消选中当前页操作，且排除项小于总数，则保持跨页全选状态
      // 如果当前是全选所有页状态，且是选中当前页操作，则保持跨页全选状态
      selectAllStatus.value = SelectAllEnum.ALL;
    } else {
      selectAllStatus.value = v;
    }
    emit('change', v);
  };

  function hasUnselectedChildren(
    data: MsTableDataItem<Record<string, any>>[],
    selectedKeys: Set<string>,
    rowKey: string
  ): boolean {
    return data.some((item: any) => {
      if (item.children && item.children.length > 0) {
        return hasUnselectedChildren(item.children, selectedKeys, rowKey);
      }
      return !selectedKeys.has(item[rowKey]);
    });
  }

  const handleCheckChange = () => {
    if (hasUnselectedChildren(props.currentData, props.selectedKeys, props.rowKey)) {
      // 当前页有数据没有勾选上，此时点击全选按钮代表全部选中
      handleSelect(SelectAllEnum.CURRENT);
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
