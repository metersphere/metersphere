<template>
  <div class="mb-[16px] flex justify-center">
    <MsAiButton size="large" icon-size="18px" :text="t('ms.ai.openNewConversation')" @click="openNewConversation" />
  </div>
  <div class="flex-1 overflow-y-hidden">
    <a-list
      max-height="100%"
      :split="false"
      :bordered="false"
      :bottom-offset="24"
      class="ms-ai-drawer-conversation-list"
      scrollbar
      @reach-bottom="fetchData"
    >
      <template #scroll-loading>
        <div v-if="bottom"></div>
        <a-spin v-else />
      </template>
      <a-list-item
        v-for="item of conversationList"
        :key="item.id"
        class="ms-ai-drawer-conversation-item"
        :class="activeConversation === item.id ? 'ms-ai-drawer-conversation-item--active' : ''"
        @click="activeConversation = item.id"
      >
        <div class="flex items-center justify-between gap-[8px]">
          <div>{{ item.name }}</div>
          <MsTableMoreAction :list="itemMoreActions" trigger="click" @select="handleMoreActionSelect($event, item)" />
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

  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { characterLimit } from '@/utils';

  const { t } = useI18n();
  const { openModal } = useModal();

  const activeConversation = defineModel<string>('value', {
    required: true,
  });

  const conversationList = ref<Record<string, any>>([]);

  const current = ref(1);
  const bottom = ref(false);
  const fetchData = () => {
    bottom.value = true;
  };

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

  function openNewConversation() {
    conversationList.value.push({
      id: Date.now().toString(),
      name: t('ms.ai.newConversation'),
    });
    activeConversation.value = conversationList.value[conversationList.value.length - 1].id;
  }

  const handleMoreActionSelect = (event: ActionsItem, item: Record<string, any>) => {
    if (event.eventTag === 'rename') {
      // Handle copy action
      console.log('rename action for item:', item);
    } else if (event.eventTag === 'delete') {
      openModal({
        type: 'error',
        title: t('common.deleteConfirmTitle', { name: characterLimit(item.name) }),
        content: t('ms.ai.deleteConversationTip'),
        okText: t('common.confirmDelete'),
        cancelText: t('common.cancel'),
        okButtonProps: {
          status: 'danger',
        },
        onBeforeOk: async () => {
          try {
            conversationList.value = conversationList.value.filter((i: Record<string, any>) => i.id !== item.id);
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

  defineExpose({
    openNewConversation,
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
  }
</style>
