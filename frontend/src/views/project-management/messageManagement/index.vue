<template>
  <MsTabCard v-model:active-tab="activeTab" :title="t('project.messageManagement')" :tab-list="tabList" />
  <MessageList v-if="activeTab === 'config'" @create-robot="createRobot" />
  <RobotList v-else-if="activeTab === 'botList'" ref="robotListRef" :active-tab="activeTab" />
</template>

<script setup lang="ts">
  /**
   * @description 项目管理-消息管理
   */
  import { nextTick, ref } from 'vue';
  import { useRoute } from 'vue-router';

  import MsTabCard from '@/components/pure/ms-tab-card/index.vue';
  import MessageList from './components/messageList.vue';
  import RobotList from './components/robotList.vue';

  import { useI18n } from '@/hooks/useI18n';

  const route = useRoute();
  const { t } = useI18n();

  const activeTab = ref((route.query.tab as string) || 'config');
  const tabList = [
    { key: 'config', title: t('project.messageManagement.config') },
    { key: 'botList', title: t('project.messageManagement.botList') },
  ];

  const robotListRef = ref();

  function createRobot() {
    activeTab.value = 'botList';
    nextTick(() => {
      robotListRef.value?.createRobot();
    });
  }
</script>

<style lang="less" scoped></style>
