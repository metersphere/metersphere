<template>
  <MsList
    v-model:active-item-key="activeItem.id"
    v-model:focus-item-key="focusItemKey"
    v-model:data="data"
    mode="static"
    item-key-field="id"
    :item-border="false"
    class="h-full rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] p-[12px]"
    item-class="mb-[4px] bg-white !p-[4px_8px]"
    :item-more-actions="itemMoreActions"
    active-item-class="!bg-[rgb(var(--primary-1))] text-[rgb(var(--primary-5))]"
    draggable
    @item-click="handleItemClick"
    @more-action-select="handleMoreActionSelect"
    @more-actions-close="focusItemKey = ''"
  >
    <template #title="{ item, index }">
      <div class="flex items-center gap-[4px]">
        <div
          :class="`flex h-[16px] w-[16px] items-center justify-center rounded-full ${
            activeItem.id === item.id ? ' bg-white' : 'bg-[var(--color-text-n8)]'
          }`"
        >
          {{ index + 1 }}
        </div>
        <div
          v-if="item.processorType === RequestConditionProcessor.TIME_WAITING"
          :title="`${t('apiTestDebug.wait')}${item.delay}ms`"
          class="one-line-text"
        >
          {{ `${t('apiTestDebug.wait')}${item.delay}` }} ms
        </div>
        <div v-else>
          {{ t(conditionTypeNameMap[item.processorType]) }}
        </div>
      </div>
    </template>
    <template #itemRight="{ item }">
      <a-switch v-model:model-value="item.enable" size="small" type="line" @change="() => emit('change')" />
    </template>
  </MsList>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import MsList from '@/components/pure/ms-list/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  import { conditionTypeNameMap } from '@/config/apiTest';
  import { useI18n } from '@/hooks/useI18n';

  import { ExecuteConditionProcessor } from '@/models/apiTest/common';
  import { RequestConditionProcessor } from '@/enums/apiEnum';

  const props = defineProps<{
    list: ExecuteConditionProcessor[];
    activeId?: string | number;
  }>();
  const emit = defineEmits<{
    (e: 'update:list', list: ExecuteConditionProcessor[]): void;
    (e: 'activeChange', item: ExecuteConditionProcessor): void;
    (e: 'change'): void;
  }>();

  const { t } = useI18n();
  const data = useVModel(props, 'list', emit);

  // 当前聚焦的列表项
  const focusItemKey = ref<any>('');
  // 当前选中的列表项
  const activeItem = ref<ExecuteConditionProcessor>({} as ExecuteConditionProcessor);
  const itemMoreActions: ActionsItem[] = [
    {
      label: 'common.copy',
      eventTag: 'copy',
    },
    {
      label: 'common.delete',
      eventTag: 'delete',
    },
  ];

  watchEffect(() => {
    activeItem.value = data.value.find((item) => item.id === props.activeId) || data.value[0] || {};
    emit('activeChange', activeItem.value);
  });

  function handleItemClick(item: ExecuteConditionProcessor) {
    activeItem.value = item;
    emit('activeChange', item);
  }

  /**
   * 复制列表项
   * @param item 列表项
   */
  function copyListItem(item: ExecuteConditionProcessor) {
    const copyItem = {
      ...item,
      id: new Date().getTime(),
    };
    data.value.push(copyItem);
    activeItem.value = copyItem;
    emit('activeChange', activeItem.value);
  }

  /**
   * 删除列表项
   * @param item 列表项
   */
  function deleteListItem(item: ExecuteConditionProcessor) {
    data.value = data.value.filter((precondition) => precondition.id !== item.id);
    if (activeItem.value.id === item.id) {
      [activeItem.value] = data.value;
    }
    emit('activeChange', activeItem.value);
  }

  /**
   * 列表项-选择更多操作项
   * @param event
   * @param item
   */
  function handleMoreActionSelect(event: ActionsItem, item: ExecuteConditionProcessor) {
    if (event.eventTag === 'copy') {
      copyListItem(item);
    } else if (event.eventTag === 'delete') {
      deleteListItem(item);
    }
  }
</script>

<style lang="less" scoped></style>
