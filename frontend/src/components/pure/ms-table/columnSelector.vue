<template>
  <icon-settings class="icon" @click="handleClick" />
  <a-drawer :width="480" :visible="visible" unmount-on-close :footer="false" @cancel="handleCancel">
    <template #title> {{ t('msTable.columnSetting.display') }} </template>
    <div class="ms-table-column-seletor">
      <div>
        <span>{{ t('msTable.columnSetting.mode') }}</span>
        <icon-question-circle class="ml-1" />
      </div>
      <a-radio-group :model-value="currentMode" class="ml-[14px]" type="button" @change="handleModeChange">
        <a-radio value="drawer">{{ t('msTable.columnSetting.drawer') }}</a-radio>
        <a-radio value="new_window">{{ t('msTable.columnSetting.newWindow') }}</a-radio>
      </a-radio-group>
      <a-divider />
      <div class="flex items-center justify-between">
        <div>{{ t('msTable.columnSetting.header') }}</div>
        <MsButton :disabled="!hasChange" @click="handleReset">{{ t('msTable.columnSetting.resetDefault') }}</MsButton>
      </div>
      <div class="flex-col">
        <div v-for="(item, idx) in firstColumns" :key="item.dataIndex" class="column-item">
          <div>{{ t(item.title as string) }}</div>
          <a-switch size="small" :model-value="item.showInTable" @change="handleFisrtColumnChange(idx)" />
        </div>
      </div>
      <a-divider orientation="center" class="non-sort">{{ t('msTable.columnSetting.nonSort') }}</a-divider>
      <Draggable tag="div" :list="secondColumns" class="list-group" handle=".handle" item-key="dateIndex"> </Draggable>
    </div>
  </a-drawer>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import { onMounted, ref } from 'vue';
  import { useTableStore } from '@/store';
  import { MsTableColumn } from './type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import { TableOpenDetailMode } from '@/store/modules/ms-table/types';
  import Draggable from 'vuedraggable';

  const tableStore = useTableStore();
  const { t } = useI18n();
  const currentMode = ref('');
  // 不能拖拽的列
  const firstColumns = ref<MsTableColumn>([]);
  // 可以拖拽的列
  const secondColumns = ref<MsTableColumn>([]);
  // 是否有改动
  const hasChange = ref(false);

  const props = defineProps<{
    tableKey: string;
  }>();

  const visible = ref(false);

  const handleClick = () => {
    visible.value = true;
  };

  const handleCancel = () => {
    visible.value = false;
  };

  const loadColumn = (key: string) => {
    const { nonSortableColumns: noSort, couldSortableColumns: couldSort } = tableStore.getColumns(key);
    firstColumns.value = noSort;
    secondColumns.value = couldSort;
  };

  const handleReset = () => {
    loadColumn(props.tableKey);
  };

  const handleFisrtColumnChange = (idx: number) => {
    const item = firstColumns.value[idx];
    item.showInTable = !item.showInTable;
    hasChange.value = true;
  };

  const handleModeChange = (value: string | number | boolean) => {
    currentMode.value = value as string;
    tableStore.setMode(props.tableKey, value as TableOpenDetailMode);
  };

  onMounted(() => {
    if (props.tableKey) {
      currentMode.value = tableStore.getMode(props.tableKey);
      loadColumn(props.tableKey);
    }
  });
</script>

<style lang="less" scoped>
  .icon {
    margin-left: 16px;
    color: var(--color-text-4);
    background-color: var(--color-text-10);
  }
  .column-item {
    display: flex;
    flex-flow: row nowrap;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px 8px 36px;
  }
  :deep(.arco-divider-text) {
    padding: 0 8px;
    color: var(--color-text-4);
    background: var(--color-bg-3);
  }
  .non-sort {
    font-size: 12px;
  }
</style>
