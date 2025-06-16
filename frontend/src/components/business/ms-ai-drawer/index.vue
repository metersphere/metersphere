<template>
  <MsDrawer
    v-model:visible="visible"
    :title="t('settings.navbar.ai')"
    width="80vw"
    :footer="false"
    :esc-to-close="false"
    :mask-closable="false"
    :closable="false"
    no-content-padding
  >
    <template #tbutton>
      <a-button type="text" class="arco-btn-text--secondary px-0" size="mini" @click="onBeforeClose">
        <icon-close :size="16" />
      </a-button>
    </template>
    <MsSplitBox v-model:size="splitSize">
      <template #first>
        <div class="flex h-full flex-col overflow-hidden py-[24px] pl-[16px] pr-[8px]">
          <conversationList
            v-if="visible"
            ref="conversationListRef"
            v-model:value="activeConversation"
            :answering="answering"
            @stop-answer="handleStopAnswer"
          />
        </div>
      </template>
      <template #second>
        <conversation
          v-if="visible"
          ref="conversationRef"
          v-model:value="activeConversation"
          v-model:answering="answering"
          :type="props.type"
          :api-definition-id="props.apiDefinitionId"
          :module-id="props.moduleId"
          :template-id="props.templateId"
          @open-new-conversation="openNewConversation"
          @add-success="handleAddConversationSuccess"
          @sync-api-case="emit('syncApiCase', $event)"
          @sync-feature-case="emit('syncFeatureCase', $event)"
          @sync-success="handleSyncSuccess"
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
  import useModal from '@/hooks/useModal';

  import { AiCaseTransformResult, AiChatListItem } from '@/models/ai';
  import { ApiCaseDetail } from '@/models/apiTest/management';

  const props = defineProps<{
    type: 'chat' | 'case' | 'api';
    apiDefinitionId?: string | number;
    moduleId?: string | number;
    templateId?: string | number; // 功能用例保存时需要的模板 id
  }>();
  const emit = defineEmits<{
    (e: 'syncApiCase', detail: ApiCaseDetail): void;
    (e: 'syncFeatureCase', detail: AiCaseTransformResult): void;
    (e: 'syncSuccess'): void;
  }>();

  const { t } = useI18n();
  const { openModal } = useModal();

  const visible = defineModel<boolean>('visible', {
    default: false,
  });

  const splitSize = ref(300);
  const activeConversation = ref<AiChatListItem | undefined>();
  const conversationListRef = ref<InstanceType<typeof conversationList>>();
  const conversationRef = ref<InstanceType<typeof conversation>>();
  const answering = ref<boolean>(false);

  function openNewConversation() {
    if (!answering.value) {
      conversationListRef.value?.openNewConversation();
    }
  }

  function handleAddConversationSuccess() {
    conversationListRef.value?.initList(false);
  }

  function handleSyncSuccess() {
    visible.value = false;
    emit('syncSuccess');
  }

  function handleStopAnswer() {
    conversationRef.value?.stopAnswer();
  }

  function onBeforeClose() {
    if (answering.value) {
      openModal({
        type: 'warning',
        title: t('common.tip'),
        content: t('ms.ai.answeringCloseTip'),
        okText: t('common.confirm'),
        cancelText: t('common.cancel'),
        onBeforeOk: async () => {
          answering.value = false;
          visible.value = false;
          return Promise.resolve(); // 允许关闭
        },
        hideCancel: false,
      });
      return;
    }
    answering.value = false;
    visible.value = false;
  }

  watch(
    () => activeConversation.value,
    (newVal) => {
      localStorage.setItem('activeAiConversation', JSON.stringify(newVal));
    }
  );
</script>

<style lang="less" scoped></style>
