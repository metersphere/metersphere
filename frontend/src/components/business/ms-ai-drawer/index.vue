<template>
  <MsDrawer
    v-model:visible="visible"
    :title="t('settings.navbar.ai')"
    width="80vw"
    :footer="false"
    :esc-to-close="false"
    no-content-padding
  >
    <MsSplitBox v-model:size="splitSize">
      <template #first>
        <div class="flex h-full flex-col overflow-hidden py-[24px] pl-[16px] pr-[8px]">
          <conversationList ref="conversationListRef" v-model:value="activeConversation" />
        </div>
      </template>
      <template #second>
        <conversation
          v-model:value="activeConversation"
          :type="props.type"
          :api-definition-id="props.apiDefinitionId"
          @open-new-conversation="openNewConversation"
          @add-success="handleAddConversationSuccess"
          @sync-api-case="emit('syncApiCase', $event)"
          @sync-feature-case="emit('syncFeatureCase', $event)"
        />
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

  import { AiCaseTransformResult, AiChatListItem } from '@/models/ai';
  import { ApiCaseDetail } from '@/models/apiTest/management';

  const props = defineProps<{
    type: 'chat' | 'case' | 'api';
    apiDefinitionId?: string | number;
  }>();
  const emit = defineEmits<{
    (e: 'syncApiCase', detail: ApiCaseDetail): void;
    (e: 'syncFeatureCase', detail: AiCaseTransformResult): void;
  }>();

  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', {
    default: false,
  });

  const splitSize = ref(300);
  const activeConversation = ref<AiChatListItem | undefined>();
  const conversationListRef = ref<InstanceType<typeof conversationList>>();

  function openNewConversation() {
    conversationListRef.value?.openNewConversation();
  }

  function handleAddConversationSuccess() {
    conversationListRef.value?.initList(false);
  }
</script>

<style lang="less" scoped></style>
