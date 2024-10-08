<template>
  <a-popover
    v-if="props.isSimple"
    class="ms-mini-setting-popover"
    unmount-on-close
    position="rt"
    trigger="click"
    @hide="handleCancel"
  >
    <icon-settings :class="`setting-icon ${props.isHiddenSetting ? 'invisible' : 'visible'}`" />
    <template #content>
      <div class="flex items-center justify-between p-[16px]">
        <div class="text-[16px] font-medium text-[var(--color-text-1)]">{{ t('msTable.columnSetting.display') }}</div>
      </div>
      <a-divider orientation="center" class="!m-0" />
      <div class="p-[16px]">
        <template v-if="props.showPagination">
          <div class="font-medium text-[var(--color-text-4)]">{{ t('msTable.columnSetting.pageSize') }} </div>
          <PageSizeSelector
            v-model:model-value="pageSize"
            class="mt-[8px]"
            @page-size-change="(v: number) => emit('pageSizeChange',v)"
          />
        </template>
        <a-divider v-if="!props.onlyPageSize" orientation="center" class="!my-[16px]" />
        <div v-if="!props.onlyPageSize" class="mb-2 flex items-center justify-between">
          <div class="font-medium text-[var(--color-text-4)]">{{ t('msTable.columnSetting.header') }}</div>
          <MsButton v-if="!props.onlyPageSize" size="mini" :disabled="!hasChange" @click="handleReset">
            {{ t('msTable.columnSetting.resetDefault') }}
          </MsButton>
        </div>
        <template v-if="!props.onlyPageSize">
          <div class="mt-[16px] flex-col pl-[14px]">
            <div v-for="item in nonSortColumn" :key="item.dataIndex" class="column-item">
              <div>{{ t((item.title || item.columnTitle) as string) }}</div>
              <a-switch
                v-if="item.slotName !== SpecialColumnEnum.OPERATION"
                v-model="item.showInTable"
                :disabled="item.columnSelectorDisabled"
                size="small"
                type="line"
                @change="handleSwitchChange"
              />
            </div>
          </div>
          <a-divider v-if="nonSortColumn.length" orientation="center" class="non-sort">
            <span class="one-line-text text-[12px] text-[var(--color-text-4)]">
              {{ t('msTable.columnSetting.nonSort') }}
            </span>
          </a-divider>
          <VueDraggable
            v-model="couldSortColumn"
            handle=".sort-handle"
            ghost-class="ghost"
            @change="handleSwitchChange"
          >
            <div v-for="element in couldSortColumn" :key="element.dataIndex" class="column-drag-item">
              <div class="flex w-[90%] items-center">
                <MsIcon type="icon-icon_drag" class="sort-handle cursor-move text-[16px] text-[var(--color-text-4)]" />
                <span class="one-line-text ml-[8px] max-w-[85%]">
                  {{ t((element.title || element.columnTitle) as string) }}
                </span>
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
      </div>
    </template>
  </a-popover>
  <icon-settings
    v-else
    :class="`setting-icon ${props.isHiddenSetting ? 'invisible' : 'visible'}`"
    @click="handleShowSetting"
  />
</template>

<script setup lang="ts">
  import { VueDraggable } from 'vue-draggable-plus';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import PageSizeSelector from './comp/pageSizeSelector.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { useTableStore } from '@/store';

  import { SpecialColumnEnum, TableKeyEnum } from '@/enums/tableEnum';

  import { MsTableColumn } from './type';

  // 不能拖拽的列
  const nonSortColumn = ref<MsTableColumn>([]);
  // 可以拖拽的列
  const couldSortColumn = ref<MsTableColumn>([]);

  const tableStore = useTableStore();
  const { t } = useI18n();

  // 是否有改动
  const hasChange = ref(false);
  const pageSize = ref();

  const handleSwitchChange = () => {
    hasChange.value = true;
  };

  const props = defineProps<{
    tableKey: TableKeyEnum;
    isSimple: boolean;
    onlyPageSize?: boolean;
    showPagination?: boolean;
    isHiddenSetting?: boolean; // 是否隐藏设置
  }>();

  const emit = defineEmits<{
    (e: 'showSetting'): void; //  数据发生变化
    (e: 'initData'): void;
    (e: 'pageSizeChange', value: number): void;
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

  const loadColumn = (key: TableKeyEnum) => {
    tableStore.getColumns(key, true).then((res) => {
      const { nonSort, couldSort } = res;
      nonSortColumn.value = nonSort;
      couldSortColumn.value = couldSort;
    });
  };

  const handleReset = () => {
    loadColumn(props.tableKey);
    hasChange.value = false;
  };

  onBeforeMount(() => {
    if (props.tableKey) {
      tableStore.getPageSize(props.tableKey).then((res) => {
        pageSize.value = res;
      });
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
    padding: 8px;
    flex-flow: row nowrap;
    @apply flex flex-nowrap items-center justify-between;
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

<style lang="less">
  .ms-mini-setting-popover {
    .arco-popover-popup-content {
      padding: 0;
    }
  }
</style>
