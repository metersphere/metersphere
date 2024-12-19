<template>
  <MsList
    v-model:active-item-key="activeItem.id"
    v-model:focus-item-key="focusItemKey"
    v-model:data="list"
    mode="static"
    item-key-field="id"
    :disabled="props.disabled"
    :item-border="false"
    class="h-full overflow-hidden rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] p-[12px]"
    item-class="mb-[4px] bg-[var(--color-text-fff)] !p-[4px_8px]"
    :item-more-actions="moreActions"
    active-item-class="!bg-[rgb(var(--primary-1))] text-[rgb(var(--primary-5))]"
    draggable
    @item-click="handleItemClick"
    @more-action-select="handleMoreActionSelect"
    @more-actions-close="focusItemKey = ''"
  >
    <template #title="{ item, index }">
      <div class="flex items-center gap-[4px]">
        <div
          :class="`flex h-[16px] w-[16px] flex-shrink-0 items-center justify-center rounded-full ${
            activeItem.id === item.id ? ' bg-[var(--color-text-fff)]' : 'bg-[var(--color-text-n8)]'
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
        <div v-else class="flex min-w-[42px] items-center justify-between">
          <div class="one-line-text">
            {{ t(conditionTypeNameMap[item.processorType as keyof typeof conditionTypeNameMap]) }}</div
          >
          <a-badge
            v-if="item.processorType === RequestConditionProcessor.REQUEST_SCRIPT"
            class="ml-1 mt-[2px] min-w-[48px]"
            :text="
              item.beforeStepScript
                ? t('project.environmental.preOrPost.pre')
                : t('project.environmental.preOrPost.post')
            "
          />
        </div>
      </div>
    </template>
    <template #itemRight="{ item }">
      <a-switch
        v-model:model-value="item.enable"
        :disabled="props.disabled"
        size="small"
        type="line"
        @change="() => emit('change')"
      />
    </template>
  </MsList>
</template>

<script setup lang="ts">
  import { cloneDeep } from 'lodash-es';

  import MsList from '@/components/pure/ms-list/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  import { conditionTypeNameMap } from '@/config/apiTest';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { characterLimit, getGenerateId } from '@/utils';

  import { ExecuteConditionProcessor } from '@/models/apiTest/common';
  import { RequestConditionProcessor } from '@/enums/apiEnum';

  const props = defineProps<{
    activeId?: string | number;
    showAssociatedScene?: boolean;
    disabled?: boolean;
    showPrePostRequest?: boolean; // 是否展示前后置请求忽略选项
  }>();
  const emit = defineEmits<{
    (e: 'activeChange', item: ExecuteConditionProcessor): void;
    (e: 'change'): void;
  }>();

  const { t } = useI18n();
  const list = defineModel<ExecuteConditionProcessor[]>('list', {
    required: true,
  });
  const { openModal } = useModal();

  // 当前聚焦的列表项
  const focusItemKey = ref<any>('');
  // 当前选中的列表项
  const activeItem = ref<ExecuteConditionProcessor>({} as ExecuteConditionProcessor);

  const hasPreAndPost = computed(() => {
    if (props.showPrePostRequest) {
      const hasPre =
        list.value.filter(
          (item) => item.beforeStepScript && item.processorType === RequestConditionProcessor.REQUEST_SCRIPT
        ).length > 0;
      const hasPost =
        list.value.filter(
          (item) => !item.beforeStepScript && item.processorType === RequestConditionProcessor.REQUEST_SCRIPT
        ).length > 0;
      if (hasPre && hasPost) {
        return true;
      }
      return false;
    }
    return false;
  });

  const hasEXTRACT = computed(() => {
    return list.value.filter((item: any) => item.processorType === RequestConditionProcessor.EXTRACT).length > 0;
  });

  const hasSql = computed(
    () =>
      list.value.filter((item: any) => item.processorType === RequestConditionProcessor.SQL).length > 0 &&
      props.showPrePostRequest
  );

  const itemMoreActions: ActionsItem[] = [
    {
      label: 'common.copy',
      eventTag: 'copy',
      disabled: hasPreAndPost.value,
    },
    {
      label: 'common.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];

  let moreActions: ActionsItem[] = [...itemMoreActions];

  watchEffect(() => {
    activeItem.value = list.value.find((item) => item.id === props.activeId) || list.value[0] || {};
    emit('activeChange', activeItem.value);
    if (hasPreAndPost.value || hasEXTRACT.value || hasSql.value) {
      moreActions = itemMoreActions.slice(-1);
    } else {
      moreActions = itemMoreActions;
    }
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
    let copyItem = {
      ...cloneDeep(item),
      id: getGenerateId(),
    };
    const isExistPre = list.value.filter(
      (current) => current.beforeStepScript && current.processorType === RequestConditionProcessor.REQUEST_SCRIPT
    ).length;
    const isExistPost = list.value.filter(
      (current) => !current.beforeStepScript && current.processorType === RequestConditionProcessor.REQUEST_SCRIPT
    ).length;
    // 如果是场景或者是请求类型的 需要限制前后脚本类型只能为一前一后

    if (isExistPre && isExistPost && props.showPrePostRequest) {
      return;
    }

    copyItem = {
      ...cloneDeep(item),
      beforeStepScript: !isExistPre,
      id: getGenerateId(),
    };

    const copyIndex = list.value.findIndex((e: ExecuteConditionProcessor) => e.id === item.id);
    if (copyIndex > -1) {
      list.value.splice(copyIndex, 0, copyItem);
      activeItem.value = copyItem;
      emit('activeChange', activeItem.value);
    }
  }

  /**
   * 删除列表项
   * @param item 列表项
   */
  function deleteListItem(item: ExecuteConditionProcessor) {
    list.value = list.value.filter((precondition) => precondition.id !== item.id);
    if (activeItem.value.id === item.id) {
      [activeItem.value] = list.value;
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
      openModal({
        type: 'error',
        title: t('system.orgTemplate.deleteTemplateTitle', { name: characterLimit(item.name) }),
        content: t('script.delete.confirm'),
        okText: t('system.userGroup.confirmDelete'),
        cancelText: t('system.userGroup.cancel'),
        okButtonProps: {
          status: 'danger',
        },
        onBeforeOk: async () => {
          try {
            deleteListItem(item);
          } catch (error) {
            // eslint-disable-next-line no-console
            console.log(error);
          }
        },
        hideCancel: false,
      });
    }
  }
</script>

<style lang="less" scoped>
  :deep(.arco-badge-text) {
    font-size: 12px;
    color: var(--color-text-4) !important;
    background: var(--color-text-fff) !important;
    box-shadow: 0 0 0 1px var(--color-text-n8);
  }
</style>
