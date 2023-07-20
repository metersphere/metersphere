<template>
  <icon-settings class="icon" @click="handleClick" />
  <MsDrawer
    :width="480"
    :visible="visible"
    unmount-on-close
    :footer="false"
    :title="t('msTable.columnSetting.display')"
    @cancel="handleCancel"
  >
    <div class="ms-table-column-seletor">
      <div>
        <span>{{ t('msTable.columnSetting.mode') }}</span>
        <icon-question-circle class="ml-1" />
      </div>
      <a-radio-group :model-value="currentMode" class="ml-[14px]" type="button" @change="handleModeChange">
        <a-radio value="drawer">
          <div class="mode-button">
            <MsIcon :class="{ 'active-color': currentMode === 'drawer' }" type="icon-icon_drawer" />
            <span class="mode-button-title">{{ t('msTable.columnSetting.drawer') }}</span>
          </div>
        </a-radio>
        <a-radio value="new_window">
          <div class="mode-button">
            <MsIcon :class="{ 'active-color': currentMode === 'new_window' }" type="icon-icon_into-item_outlined" />
            <span class="mode-button-title">{{ t('msTable.columnSetting.newWindow') }}</span>
          </div>
        </a-radio>
      </a-radio-group>
      <a-divider />
      <div class="flex items-center justify-between">
        <div>{{ t('msTable.columnSetting.header') }}</div>
        <MsButton :disabled="!hasChange" @click="handleReset">{{ t('msTable.columnSetting.resetDefault') }}</MsButton>
      </div>
      <div class="flex-col">
        <div v-for="(item, idx) in nonSortColumn" :key="item.dataIndex" class="column-item">
          <div>{{ t(item.title as string) }}</div>
          <a-switch size="small" :model-value="item.showInTable" @change="handleFisrtColumnChange(idx)" />
        </div>
      </div>
      <a-divider orientation="center" class="non-sort">{{ t('msTable.columnSetting.nonSort') }}</a-divider>
      <Draggable tag="div" :list="couldSortColumn" class="list-group" handle=".handle" item-key="dateIndex">
        <template #item="{ element, index }">
          <div class="column-drag-item">
            <div class="flex items-center">
              <icon-drag-dot-vertical class="handle" />
              <div class="ml-[8px]">{{ t(element.title as string) }}</div>
            </div>
            <a-switch size="small" :model-value="element.showInTable" @change="handleSecondColumnChange(index)" />
          </div>
        </template>
      </Draggable>
    </div>
  </MsDrawer>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import { onBeforeMount, ref } from 'vue';
  import { useTableStore } from '@/store';
  import { MsTableColumn } from './type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import { TableOpenDetailMode } from '@/store/modules/ms-table/types';
  import Draggable from 'vuedraggable';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';

  const tableStore = useTableStore();
  const { t } = useI18n();
  const currentMode = ref('');
  // 不能拖拽的列
  const nonSortColumn = ref<MsTableColumn>([]);
  // 可以拖拽的列
  const couldSortColumn = ref<MsTableColumn>([]);
  // 是否有改动
  const hasChange = ref(false);

  const emit = defineEmits<{
    (e: 'close'): void;
  }>();

  const props = defineProps<{
    tableKey: string;
  }>();

  const visible = ref(false);

  const handleClick = () => {
    visible.value = true;
  };

  const handleCancel = () => {
    tableStore.setColumns(
      props.tableKey,
      [...nonSortColumn.value, ...couldSortColumn.value],
      currentMode.value as TableOpenDetailMode
    );
    visible.value = false;
    emit('close');
  };

  const loadColumn = (key: string) => {
    const { nonSort, couldSort } = tableStore.getColumns(key);
    nonSortColumn.value = nonSort;
    couldSortColumn.value = couldSort;
  };

  const handleReset = () => {
    loadColumn(props.tableKey);
  };

  const handleFisrtColumnChange = (idx: number) => {
    const item = nonSortColumn.value[idx];
    item.showInTable = !item.showInTable;
    hasChange.value = true;
  };

  const handleSecondColumnChange = (idx: number) => {
    const item = couldSortColumn.value[idx];
    item.showInTable = !item.showInTable;
    hasChange.value = true;
  };

  const handleModeChange = (value: string | number | boolean) => {
    currentMode.value = value as string;
    tableStore.setMode(props.tableKey, value as TableOpenDetailMode);
  };

  onBeforeMount(() => {
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
  .mode-button {
    display: flex;
    flex-flow: row nowrap;
    align-items: center;
    .active-color {
      color: var(--color-primary-5);
    }
    .mode-button-title {
      margin-left: 4px;
    }
  }
  .column-item {
    display: flex;
    flex-flow: row nowrap;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px 8px 36px;
  }
  .column-drag-item {
    display: flex;
    flex-flow: row nowrap;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px;
  }
  :deep(.arco-divider-text) {
    padding: 0 8px;
    color: var(--color-text-4);
  }
  .non-sort {
    font-size: 12px;
  }
</style>
