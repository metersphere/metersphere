<template>
  <a-popover
    v-if="props.isSimple"
    unmount-on-close
    content-class="w-[240px]"
    position="rt"
    trigger="click"
    @hide="handleCancel"
  >
    <icon-settings class="setting-icon" />
    <template #content>
      <div class="mb-2 flex items-center justify-between">
        <div class="font-medium text-[var(--color-text-1)]">{{ t('msTable.columnSetting.display') }}</div>
        <MsButton :disabled="!hasChange" @click="handleReset">{{ t('msTable.columnSetting.resetDefault') }}</MsButton>
      </div>
      <div class="flex-col">
        <div v-for="item in nonSortColumn" :key="item.dataIndex" class="column-item">
          <div>{{ t((item.title || item.columnTitle) as string) }}</div>
          <a-switch
            v-if="item.slotName !== SpecialColumnEnum.OPERATION"
            v-model="item.showInTable"
            size="small"
            type="line"
            @change="handleSwitchChange"
          />
        </div>
      </div>
      <a-divider v-if="nonSortColumn.length" orientation="center" class="non-sort"
        ><span class="one-line-text text-xs text-[var(--color-text-4)]">{{
          t('msTable.columnSetting.nonSort')
        }}</span></a-divider
      >
      <VueDraggable v-model="couldSortColumn" handle=".sort-handle" ghost-class="ghost" @change="handleSwitchChange">
        <div v-for="element in couldSortColumn" :key="element.dataIndex" class="column-drag-item">
          <div class="flex w-[90%] items-center">
            <MsIcon type="icon-icon_drag" class="sort-handle cursor-move text-[16px] text-[var(--color-text-4)]" />
            <span class="one-line-text ml-[8px] max-w-[85%]">{{
              t((element.title || element.columnTitle) as string)
            }}</span>
          </div>
          <a-switch
            v-model="element.showInTable"
            :disabled="element.columnSelectorDisabled"
            size="small"
            type="line"
            @change="handleSwitchChange"
          />
        </div>
      </VueDraggable>
    </template>
  </a-popover>
  <icon-settings v-else class="setting-icon" @click="handleShowSetting" />
</template>

<script setup lang="ts">
  import { VueDraggable } from 'vue-draggable-plus';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { useTableStore } from '@/store';

  import { SpecialColumnEnum } from '@/enums/tableEnum';

  import { MsTableColumn } from './type';

  // 不能拖拽的列
  const nonSortColumn = ref<MsTableColumn>([]);
  // 可以拖拽的列
  const couldSortColumn = ref<MsTableColumn>([]);

  const tableStore = useTableStore();
  const { t } = useI18n();

  // 是否有改动
  const hasChange = ref(false);

  const handleSwitchChange = () => {
    hasChange.value = true;
  };

  const props = defineProps<{
    tableKey: string;
    isSimple: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'showSetting'): void; //  数据发生变化
    (e: 'initData'): void;
  }>();

  const handleCancel = async () => {
    await tableStore.setColumns(
      props.tableKey,
      [...nonSortColumn.value, ...couldSortColumn.value],
      undefined,
      undefined,
      true
    );
    hasChange.value = false;
    emit('initData');
  };

  const loadColumn = (key: string) => {
    tableStore.getColumns(key, true).then((res) => {
      const { nonSort, couldSort } = res;
      nonSortColumn.value = nonSort;
      couldSortColumn.value = couldSort;
    });
  };

  const handleReset = () => {
    loadColumn(props.tableKey);
  };
  onBeforeMount(() => {
    if (props.tableKey) {
      loadColumn(props.tableKey);
    }
  });
  const handleShowSetting = () => {
    emit('showSetting');
  };
</script>

<style lang="scss" scoped>
  .setting-icon {
    color: var(--color-text-4);
    background-color: var(--color-text-10);
    cursor: pointer;
    &:hover {
      color: rgba(var(--primary-5));
    }
  }
  .column-item {
    display: flex;
    flex-flow: row nowrap;
    justify-content: space-between;
    align-items: center;
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
    padding: 8px 0;
    color: var(--color-text-1);
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
