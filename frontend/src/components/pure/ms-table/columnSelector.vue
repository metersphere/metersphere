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
      <div class="mb-2 flex items-center">
        <span class="text-[var(--color-text-4)]">{{ t('msTable.columnSetting.mode') }}</span>
        <a-tooltip :content="t('msTable.columnSetting.tooltipContent')">
          <template #content>
            <span>{{ t('msTable.columnSetting.tooltipContentDrawer') }}</span
            ><br />
            <span>{{ t('msTable.columnSetting.tooltipContentWindow') }}</span>
          </template>
          <span class="inline-block align-middle"
            ><icon-question-circle
              class="ml-[4px] mt-[3px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
          /></span>
        </a-tooltip>
      </div>
      <a-radio-group :model-value="currentMode" type="button" @change="handleModeChange">
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
      <div class="mb-2 flex items-center justify-between">
        <div class="text-[var(--color-text-4)]">{{ t('msTable.columnSetting.header') }}</div>
        <MsButton :disabled="!hasChange" @click="handleReset">{{ t('msTable.columnSetting.resetDefault') }}</MsButton>
      </div>
      <div class="flex-col">
        <div v-for="item in nonSortColumn" :key="item.dataIndex" class="column-item">
          <div>{{ t(item.title as string) }}</div>
          <a-switch
            v-model="item.showInTable"
            size="small"
            :disabled="item.dataIndex === 'name' || item.dataIndex === 'operation'"
          />
        </div>
      </div>
      <a-divider orientation="center" class="non-sort"
        ><span class="one-line-text text-xs text-[var(--color-text-4)]">{{
          t('msTable.columnSetting.nonSort')
        }}</span></a-divider
      >
      <Draggable tag="div" :list="couldSortColumn" ghost-class="ghost" item-key="dateIndex">
        <template #item="{ element }">
          <div class="column-drag-item">
            <div class="flex w-[90%] items-center">
              <MsIcon type="icon-icon_drag" class="text-[16px] text-[var(--color-text-4)]" />
              <span class="ml-[8px]">{{ t(element.title as string) }}</span>
            </div>
            <a-switch v-model="element.showInTable" size="small" />
          </div>
        </template>
      </Draggable>
    </div>
  </MsDrawer>
</template>

<script lang="ts" setup>
  import { onBeforeMount, ref } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { useTableStore } from '@/store';
  import { TableOpenDetailMode } from '@/store/modules/ms-table/types';

  import { MsTableColumn } from './type';
  import Draggable from 'vuedraggable';

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
  :deep(.arco-divider-horizontal) {
    margin: 16px 0;
    border-bottom-color: var(--color-text-n8);
  }
  :deep(.arco-divider-text) {
    padding: 0 8px;
  }
  .icon {
    margin-left: 16px;
    color: var(--color-text-4);
    background-color: var(--color-text-10);
    cursor: pointer;
    &:hover {
      color: rgba(var(--primary-5));
    }
  }
  .mode-button {
    display: flex;
    flex-flow: row nowrap;
    align-items: center;
    .active-color {
      color: rgba(var(--primary-5));
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
    &:hover {
      border-radius: 6px;
      background: var(--color-text-n9);
    }
  }
  .column-drag-item {
    display: flex;
    flex-flow: row nowrap;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px;
    cursor: move;
    &:hover {
      border-radius: 6px;
      background-color: var(--color-text-n9);
    }
  }
  .ghost {
    border: 1px dashed rgba(var(--primary-5));
    background-color: rgba(var(--primary-1));
  }
  .non-sort {
    font-size: 12px;
  }
</style>
