<template>
  <div class="mb-[16px] flex justify-center">
    <MsAiButton
      size="large"
      icon-size="18px"
      :text="t('ms.ai.openNewConversation')"
      :disabled="props.answering"
      @click="openNewConversation"
    />
  </div>
  <div class="flex-1 overflow-y-hidden">
    <a-list
      max-height="100%"
      :split="false"
      :bordered="false"
      :loading="loading"
      class="ms-ai-drawer-conversation-list"
      scrollbar
    >
      <template #empty><div /></template>
      <a-list-item
        v-for="item of conversationList"
        :id="`ai-item-${item.id}`"
        :key="item.id"
        class="ms-ai-drawer-conversation-item"
        :class="activeConversation?.id === item.id ? 'ms-ai-drawer-conversation-item--active' : ''"
        @click="handleClick(item)"
      >
        <a-input
          v-if="item.isEditing"
          :id="`ai-title-${item.id}`"
          v-model:model-value="tempInputVal"
          :max-length="255"
          @blur="handleEditConfirm(item)"
          @keydown.enter="handleEditConfirm(item)"
          @keydown.esc="handleEditCancel(item)"
        ></a-input>
        <div v-else class="flex items-center justify-between gap-[8px] overflow-hidden">
          <a-tooltip :mouse-enter-delay="300">
            <div class="one-line-text">{{ item.title }}</div>
            <template #content>
              <div>{{ item.title }}</div>
            </template>
          </a-tooltip>
          <MsTableMoreAction
            :list="getItemMoreAction(item)"
            trigger="click"
            @select="handleMoreActionSelect($event, item)"
          />
        </div>
      </a-list-item>
    </a-list>
  </div>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import MsAiButton from '@/components/pure/ms-ai-button/index.vue';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  import { deleteAiChat, getAiChatList, updateAiChatTitle } from '@/api/modules/ai';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { characterLimit, getGenerateId } from '@/utils';

  import { AiChatListItem } from '@/models/ai';

  const props = defineProps<{
    answering?: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'stopAnswer'): void;
  }>();

  const { t } = useI18n();
  const { openModal } = useModal();

  const activeConversation = defineModel<AiChatListItem | undefined>('value', {
    required: true,
  });

  const loading = ref<boolean>(false);
  const conversationList = ref<AiChatListItem[]>([]);

  const itemMoreActions: ActionsItem[] = [
    {
      label: 'common.rename',
      eventTag: 'rename',
    },
    {
      label: 'project.fileManagement.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];

  function getItemMoreAction(item: AiChatListItem) {
    if (item.isNew) {
      return itemMoreActions.filter((action) => action.eventTag !== 'rename');
    }
    return itemMoreActions;
  }

  function handleClick(item: AiChatListItem) {
    if (!props.answering) {
      // 如果正在回答，则不允许切换对话
      activeConversation.value = item;
    } else {
      Message.warning(t('ms.ai.answeringNotAllowedChange'));
    }
  }

  function openNewConversation() {
    if (!conversationList.value.some((item: AiChatListItem) => item.isNew)) {
      conversationList.value.unshift({
        id: getGenerateId(),
        title: t('ms.ai.newConversation'),
        isNew: true,
        createTime: new Date().getTime(),
        createUser: '',
      });
      [activeConversation.value] = conversationList.value;
    } else {
      [activeConversation.value] = conversationList.value.filter((item: AiChatListItem) => item.isNew);
    }
    nextTick(() => {
      const itemElement = document.querySelector(`#ai-item-${activeConversation.value?.id}`) as HTMLInputElement;
      if (itemElement) {
        itemElement.scrollIntoView({
          behavior: 'smooth',
        });
      }
    });
  }

  const tempInputVal = ref<string>('');

  function handleEditConfirm(item: AiChatListItem) {
    // setTimeout 解决中文输入法输入英文回车时英文未被填充的问题
    setTimeout(async () => {
      if (!tempInputVal.value.trim()) {
        item.isEditing = false;
        return;
      }
      try {
        await updateAiChatTitle({
          id: item.id,
          title: tempInputVal.value.trim(),
        });
        item.title = tempInputVal.value.trim();
        tempInputVal.value = '';
        item.isEditing = false;
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    });
  }

  function handleEditCancel(item: AiChatListItem) {
    item.isEditing = false;
    tempInputVal.value = '';
  }

  const handleMoreActionSelect = (event: ActionsItem, item: AiChatListItem) => {
    if (event.eventTag === 'rename') {
      tempInputVal.value = item.title;
      item.isEditing = true;
      nextTick(() => {
        const inputElement = document.querySelector(`#ai-title-${item.id}`) as HTMLInputElement;
        if (inputElement) {
          (inputElement.querySelector('.arco-input') as HTMLInputElement)?.focus();
        }
      });
    } else if (event.eventTag === 'delete') {
      if (props.answering && activeConversation.value?.id === item.id) {
        // 如果正在回答当前对话，则不允许删除
        Message.warning(t('ms.ai.answeringNotAllowedDelete'));
        return;
      }
      openModal({
        type: 'error',
        title: t('common.deleteConfirmTitle', { name: characterLimit(item.title) }),
        content: t('ms.ai.deleteConversationTip'),
        okText: t('common.confirmDelete'),
        cancelText: t('common.cancel'),
        okButtonProps: {
          status: 'danger',
        },
        onBeforeOk: async () => {
          try {
            if (!item.isNew) {
              await deleteAiChat(item.id);
            }
            conversationList.value = conversationList.value.filter((i: AiChatListItem) => i.id !== item.id);
            if (activeConversation.value?.id === item.id) {
              [activeConversation.value] = conversationList.value;
              // 停止被删除的对话的回复
              emit('stopAnswer');
            }
            Message.success(t('common.deleteSuccess'));
          } catch (error) {
            // eslint-disable-next-line no-console
            console.log(error);
          }
        },
        hideCancel: false,
      });
    }
  };

  async function initList(setDefaultActive = true) {
    try {
      loading.value = true;
      conversationList.value = await getAiChatList();
      const localConversation = JSON.parse(localStorage.getItem('activeAiConversation') || '""');
      if (typeof localConversation === 'object' && localConversation?.id) {
        const found = conversationList.value.find((item: AiChatListItem) => item.id === localConversation.id);
        if (found) {
          activeConversation.value = found;
        } else {
          activeConversation.value = undefined;
        }
      } else if (conversationList.value.length > 0 && setDefaultActive) {
        [activeConversation.value] = conversationList.value;
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  onBeforeMount(() => {
    initList();
  });

  defineExpose({
    openNewConversation,
    initList,
  });
</script>

<style lang="less" scoped>
  .arco-list-wrapper {
    @apply h-full;
    :deep(.arco-scrollbar) {
      @apply h-full;
      .arco-list-content-wrapper {
        padding-right: 12px;
        .arco-list-content {
          @apply flex flex-col;

          gap: 4px;
          padding: 1px 0 0 1px;
        }
      }
      .arco-scrollbar-track-direction-vertical {
        right: 0;
      }
    }
  }
  .ms-ai-drawer-conversation-item {
    @apply flex cursor-pointer items-center;

    padding: 14px 16px !important;
    border: 1px solid transparent;
    border-radius: 4px;
    &:hover {
      border: 1px solid rgb(var(--primary-7));
    }
    &--active {
      color: rgb(var(--primary-5));
      background: rgb(var(--primary-1));
    }
    :deep(.arco-list-item-main) {
      @apply overflow-hidden;
    }
    :deep(.ms-more-action-trigger-content) {
      @apply invisible;
    }
    &:hover {
      :deep(.ms-more-action-trigger-content) {
        @apply visible;
      }
    }
  }
</style>
