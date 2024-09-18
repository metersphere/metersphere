<template>
  <MsDrawer
    :visible="props.visible"
    :width="480"
    unmount-on-close
    :footer="false"
    :title="t('msTable.columnSetting.display')"
    class="column-drawer"
    @cancel="handleCancel"
  >
    <div class="ms-table-column-selector">
      <template v-if="showJumpMethod">
        <div class="mb-2 flex items-center">
          <span class="font-medium text-[var(--color-text-4)]">{{ t('msTable.columnSetting.mode') }}</span>
          <a-tooltip position="right">
            <template #content>
              <span>{{ t('msTable.columnSetting.tooltipContentDrawer') }}</span>
              <br />
              <span>{{ t('msTable.columnSetting.tooltipContentWindow') }}</span>
            </template>
            <span class="inline-block align-middle">
              <icon-question-circle
                class="ml-[4px] mt-[3px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
              />
            </span>
          </a-tooltip>
        </div>
        <a-radio-group class="mb-2" :model-value="currentMode" type="button" @change="handleModeChange">
          <a-radio value="drawer">
            <div class="mode-button">
              <MsIcon :class="{ 'active-color': currentMode === 'drawer' }" type="icon-icon_drawer" />
              <span class="mode-button-title">{{ t('msTable.columnSetting.drawer') }}</span>
            </div>
          </a-radio>
          <!-- TODO 这版本不上新窗口 -->
          <!-- <a-radio value="new_window">
            <div class="mode-button">
              <MsIcon :class="{ 'active-color': currentMode === 'new_window' }" type="icon-icon_into-item_outlined" />
              <span class="mode-button-title">{{ t('msTable.columnSetting.newWindow') }}</span>
            </div>
          </a-radio> -->
        </a-radio-group>
      </template>
      <template v-if="props.showPagination">
        <div class="font-medium text-[var(--color-text-4)]">{{ t('msTable.columnSetting.pageSize') }} </div>
        <PageSizeSelector
          v-model:model-value="pageSize"
          class="mt-[8px]"
          @page-size-change="(v: number) => emit('pageSizeChange',v)"
        />
      </template>
      <template v-if="props.showSubdirectory">
        <div class="mt-[24px] flex items-center">
          <a-switch v-model="subdirectoryVal" size="small" @change="handleSubSwitch" />
          <span class="mx-[4px] font-medium text-[var(--color-text-4)]">
            {{ t('msTable.columnSetting.showSubdirectoryTips') }}
          </span>
          <a-tooltip position="rt">
            <icon-question-circle class="text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]" size="16" />
            <template #content>
              <div class="w-[250px]"> {{ t('msTable.columnSetting.showSubdirectoryTips1') }} </div>
              <br />
              <div class="w-[250px]"> {{ t('msTable.columnSetting.showSubdirectoryTips2') }} </div>
            </template>
          </a-tooltip>
        </div>
      </template>
      <a-divider v-if="showJumpMethod || props.showPagination || props.showSubdirectory" class="!mt-[24px]" />
      <div class="mb-[4px] flex items-center justify-between">
        <div class="font-medium text-[var(--color-text-4)]">{{ t('msTable.columnSetting.header') }}</div>
        <a-tooltip :content="t('msTable.columnSetting.default')" :disabled="hasChange">
          <MsButton :disabled="!hasChange" @click="handleReset">{{ t('msTable.columnSetting.resetDefault') }}</MsButton>
        </a-tooltip>
      </div>
      <div class="flex flex-col gap-[4px]">
        <div
          v-for="item in nonSortColumn"
          v-show="item.dataIndex !== 'operation'"
          :key="item.dataIndex"
          class="column-item"
        >
          <div>{{ t((item.title || item.columnTitle) as string) }}</div>
          <a-switch
            v-model="item.showInTable"
            size="small"
            :disabled="item.columnSelectorDisabled"
            @change="handleSwitchChange"
          />
        </div>
      </div>
      <a-divider orientation="center">
        <span class="one-line-text text-[12px] font-normal text-[var(--color-text-4)]">
          {{ t('msTable.columnSetting.nonSort') }}
        </span>
      </a-divider>
      <VueDraggable v-model="couldSortColumn" handle=".sort-handle" ghost-class="ghost" @change="handleSwitchChange">
        <div v-for="element in couldSortColumn" :key="element.dataIndex" class="column-drag-item">
          <div class="flex w-[60%] items-center">
            <MsIcon type="icon-icon_drag" class="sort-handle cursor-move text-[16px] text-[var(--color-text-4)]" />
            <span class="one-line-text ml-[8px] max-w-[85%]">
              {{ t((element.title || element.columnTitle) as string) }}
            </span>
          </div>
          <a-switch v-model="element.showInTable" size="small" @change="handleSwitchChange" />
        </div>
      </VueDraggable>
    </div>
  </MsDrawer>
</template>

<script lang="ts" setup>
  import { onBeforeMount, ref } from 'vue';
  import { VueDraggable } from 'vue-draggable-plus';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import PageSizeSelector from './comp/pageSizeSelector.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { useTableStore } from '@/store';
  import { TableOpenDetailMode } from '@/store/modules/components/ms-table/types';

  import { TableKeyEnum } from '@/enums/tableEnum';

  import { MsTableColumn } from './type';

  const tableStore = useTableStore();
  const { t } = useI18n();
  const currentMode = ref('');
  const subdirectoryVal = ref(true);
  const pageSize = ref();
  // 不能拖拽的列
  const nonSortColumn = ref<MsTableColumn>([]);
  // 可以拖拽的列
  const couldSortColumn = ref<MsTableColumn>([]);
  // 是否有改动
  const hasChange = ref(false);

  const emit = defineEmits<{
    (e: 'initData'): void;
    (e: 'moduleChange'): void;
    (e: 'pageSizeChange', value: number): void;
    (e: 'update:visible', value: boolean): void;
  }>();

  const props = defineProps<{
    visible: boolean;
    tableKey: TableKeyEnum;
    showJumpMethod: boolean;
    showPagination: boolean;
    showSubdirectory: boolean;
  }>();

  const handleCancel = async () => {
    await tableStore.setColumns(
      props.tableKey,
      [...nonSortColumn.value, ...couldSortColumn.value],
      currentMode.value as TableOpenDetailMode,
      subdirectoryVal.value
    );
    emit('update:visible', false);
    if (hasChange.value) {
      emit('initData');
    }
  };

  const loadColumn = (key: TableKeyEnum) => {
    tableStore.getColumns(key).then((res) => {
      const { nonSort, couldSort } = res;
      nonSortColumn.value = nonSort;
      couldSortColumn.value = couldSort;
    });
  };

  const handleReset = () => {
    loadColumn(props.tableKey);
    hasChange.value = false;
  };

  const handleModeChange = (value: string | number | boolean) => {
    currentMode.value = value as string;
    tableStore.setMode(props.tableKey, value as TableOpenDetailMode);
  };

  const handleSubSwitch = async () => {
    await tableStore.setSubdirectory(props.tableKey, subdirectoryVal.value);
    emit('moduleChange');
  };

  const handleSwitchChange = () => {
    hasChange.value = true;
  };

  onBeforeMount(() => {
    if (props.tableKey) {
      tableStore.getMode(props.tableKey).then((res) => {
        if (res) {
          currentMode.value = res;
        }
      });
      tableStore.getSubShow(props.tableKey).then((res) => {
        subdirectoryVal.value = res === undefined ? true : res;
      });
      tableStore.getPageSize(props.tableKey).then((res) => {
        pageSize.value = res;
      });
      loadColumn(props.tableKey);
    }
  });
  watch(
    () => props.visible,
    (value) => {
      if (value) {
        hasChange.value = false;
      }
    }
  );
</script>

<style lang="less" scoped>
  :deep(.arco-divider-horizontal) {
    margin: 16px 0;
    border-bottom-color: var(--color-text-n8);
  }
  :deep(.arco-divider-text) {
    padding: 0 8px;
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
</style>

<style lang="less">
  .column-drawer {
    .ms-drawer-body {
      min-width: auto !important;
    }
    .arco-drawer-body {
      padding: 24px 16px;
    }
  }
</style>
