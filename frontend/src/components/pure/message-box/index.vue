<template>
  <a-spin style="display: block" :loading="loading">
    <a-tabs v-model:activeKey="messageType" type="rounded" destroy-on-hide>
      <a-tab-pane v-for="item in tabList" :key="item.key">
        <template #title>
          <span> {{ item.title }}{{ formatUnreadLength(item.key) }} </span>
        </template>
        <a-result v-if="!renderList.length" status="404">
          <template #subtitle> {{ $t('messageBox.noContent') }} </template>
        </a-result>
        <List :render-list="renderList" :unread-count="unreadCount" @item-click="handleItemClick" />
      </a-tab-pane>
      <template #extra>
        <a-button type="text" @click="emptyList">
          {{ $t('messageBox.tab.button') }}
        </a-button>
      </template>
    </a-tabs>
  </a-spin>
</template>

<script lang="ts" setup>
  import { computed, reactive, ref, toRefs } from 'vue';

  import List from './list.vue';

  import { MessageListType, MessageRecord, queryMessageList, setMessageStatus } from '@/api/modules/message';
  import { useI18n } from '@/hooks/useI18n';
  import useLoading from '@/hooks/useLoading';

  interface TabItem {
    key: string;
    title: string;
    avatar?: string;
  }
  const { loading, setLoading } = useLoading(true);
  const messageType = ref('message');
  const { t } = useI18n();
  const messageData = reactive<{
    renderList: MessageRecord[];
    messageList: MessageRecord[];
  }>({
    renderList: [],
    messageList: [],
  });
  toRefs(messageData);
  const tabList: TabItem[] = [
    {
      key: 'message',
      title: t('messageBox.tab.title.message'),
    },
    {
      key: 'notice',
      title: t('messageBox.tab.title.notice'),
    },
    {
      key: 'todo',
      title: t('messageBox.tab.title.todo'),
    },
  ];
  async function fetchSourceData() {
    setLoading(true);
    try {
      const data = await queryMessageList();
      messageData.messageList = data;
    } catch (err) {
      // you can report use errorHandler or other
    } finally {
      setLoading(false);
    }
  }
  async function readMessage(data: MessageListType) {
    const ids = data.map((item) => item.id);
    await setMessageStatus({ ids });
    fetchSourceData();
  }
  const renderList = computed(() => {
    return messageData.messageList.filter((item) => messageType.value === item.type);
  });
  const unreadCount = computed(() => {
    return renderList.value.filter((item) => !item.status).length;
  });
  const getUnreadList = (type: string) => {
    const list = messageData.messageList.filter((item) => item.type === type && !item.status);
    return list;
  };
  const formatUnreadLength = (type: string) => {
    const list = getUnreadList(type);
    return list.length ? `(${list.length})` : ``;
  };
  const handleItemClick = (items: MessageListType) => {
    if (renderList.value.length) readMessage([...items]);
  };
  const emptyList = () => {
    messageData.messageList = [];
  };
  fetchSourceData();
</script>

<style scoped lang="less">
  :deep(.arco-popover-popup-content) {
    @apply p-0;
  }
  :deep(.arco-list-item-meta) {
    @apply items-start;
  }
  :deep(.arco-tabs-nav) {
    @apply pr-0;

    padding-top: 14px;
    padding-bottom: 12px;
    padding-left: 16px;
    border-bottom: 1px solid var(--color-neutral-3);
  }
  :deep(.arco-tabs-content) {
    @apply pt-0;
    .arco-result-subtitle {
      color: rgb(var(--gray-6));
    }
  }
</style>
