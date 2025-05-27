<template>
  <MsDrawer v-model:visible="visible" :title="t('settings.navbar.ai')" width="80vw" :footer="false" no-content-padding>
    <MsSplitBox v-model:size="splitSize">
      <template #first>
        <div class="flex h-full flex-col overflow-hidden py-[24px] pl-[16px] pr-[8px]">
          <conversationList ref="conversationListRef" v-model:value="activeConversation" />
        </div>
      </template>
      <template #second>
        <conversation :id="activeConversation" :type="props.type" @open-new-conversation="openNewConversation" />
      </template>
    </MsSplitBox>
  </MsDrawer>
</template>

<script setup lang="ts">
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import conversation from './components/conversation.vue';
  import conversationList from './components/conversationList.vue';

  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    type: 'chat' | 'case' | 'api';
  }>();

  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', {
    default: false,
  });

  const splitSize = ref(300);
  const activeConversation = ref('');
  const conversationListRef = ref<InstanceType<typeof conversationList>>();

  function openNewConversation() {
    conversationListRef.value?.openNewConversation();
  }
</script>

<style lang="less" scoped></style>
