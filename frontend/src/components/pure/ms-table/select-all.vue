<template>
  <div class="ms-table-select-all">
    <a-checkbox
      v-model:model-value="checked"
      :disabled="props.disabled"
      class="text-base"
      :indeterminate="indeterminate"
      @change="handleCheckChange"
    />
    <a-dropdown v-if="props.showSelectAll" :disable="props.disabled" position="bl" @select="handleSelect">
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
  import MsIcon from '../ms-icon-font/index.vue';

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
    }>(),
    {
      current: 0,
      total: 0,
      showSelectAll: true,
      disabled: false,
    }
  );

  const checked = computed({
    get: () => {
      return props.selectedKeys.size === props.total;
    },
    set: (value) => {
      return value;
    },
  });
  const indeterminate = computed(() => {
    // 已选中的数量大于 0 且小于总数时是半选状态
    return props.selectedKeys.size > 0 && props.selectedKeys.size < props.total;
  });

  const handleSelect = (v: string | number | Record<string, any> | undefined) => {
    emit('change', v as SelectAllEnum);
  };

  const handleCheckChange = () => {
    if (props.currentData.some((item) => !props.selectedKeys.has(item.id))) {
      // 当前页有数据没有勾选上，此时点击全选按钮代表全部选中
      handleSelect(SelectAllEnum.CURRENT);
    } else {
      // 否则是当前页全部数据已勾选，此时点击全选按钮代表取消当前页面数据勾选
      handleSelect(SelectAllEnum.NONE);
    }
  };
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
