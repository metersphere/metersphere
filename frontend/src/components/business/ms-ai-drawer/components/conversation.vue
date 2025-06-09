<template>
  <a-spin :loading="loading" class="flex h-full flex-col p-[24px]">
    <template v-if="title">
      <a-input
        v-if="editTitle"
        ref="titleInputRef"
        v-model:model-value="title"
        size="large"
        class="conversation-title"
        @keydown.enter="editTitle = false"
      />
      <div
        v-else
        class="one-line-text mb-[16px] w-full cursor-pointer py-[8px] text-center text-[16px] font-medium leading-[24px]"
        @click="handleEditTitle"
      >
        {{ title }}
      </div>
    </template>
    <div class="flex-1 overflow-hidden">
      <BubbleList :list="conversationItems" :btn-loading="answering" max-height="100%">
        <!-- 自定义 loading -->
        <template #loading>
          <div class="loading-wrapper">
            <a-spin dot />
          </div>
        </template>
        <!-- 自定义头像 -->
        <template #avatar="{ item }">
          <div class="avatar-wrapper">
            <svg-icon v-if="item.type === AiChatContentRoleTypeEnum.ASSISTANT" width="40px" height="40px" name="ai" />
            <img v-else :src="avatar" alt="avatar" />
          </div>
        </template>

        <!-- 自定义气泡内容 -->
        <template #content="{ item }">
          <!-- <div v-if="item.isEdit" class="flex w-full flex-col gap-[8px]">
            <a-textarea v-model:model-value="tempInput"></a-textarea>
            <div class="flex items-center justify-end gap-[8px]">
              <a-button type="secondary" @click="cancelInput(item)">{{ t('common.cancel') }}</a-button>
              <a-button type="primary" @click="sendInput(item)">{{ t('ms.ai.send') }}</a-button>
            </div>
          </div> -->
          <a-trigger position="left" auto-fit-position :popup-translate="[-8, 0]">
            <div class="content-wrapper">
              <Bubble
                class="content-text"
                :content="item.content"
                :class="item.type === AiChatContentRoleTypeEnum.ASSISTANT ? '!rounded-none !bg-transparent' : ''"
                typing
                :is-markdown="item.type === AiChatContentRoleTypeEnum.ASSISTANT"
              />
            </div>
            <template v-if="item.type === AiChatContentRoleTypeEnum.USER" #content>
              <div class="flex items-center gap-[8px]">
                <a-tooltip :mouse-enter-delay="300">
                  <MsButton type="icon" status="default" class="!mr-0" @click="handleCopy(item)">
                    <MsIcon type="icon-icon_copy_outlined" class="text-[var(--color-text-4)]" />
                  </MsButton>
                  <template #content>
                    <span>{{ t('common.copy') }}</span>
                  </template>
                </a-tooltip>
                <a-tooltip :mouse-enter-delay="300">
                  <MsButton type="icon" status="default" class="!mr-0" @click="handleEdit(item)">
                    <MsIcon type="icon-icon_edit_outlined" class="text-[var(--color-text-4)]" />
                  </MsButton>
                  <template #content>
                    <span>{{ t('common.edit') }}</span>
                  </template>
                </a-tooltip>
              </div>
            </template>
          </a-trigger>
        </template>

        <!-- 自定义底部 -->
        <template #footer="{ item }">
          <div v-if="!answering && item.type === AiChatContentRoleTypeEnum.ASSISTANT" class="footer-wrapper">
            <div class="footer-container">
              <a-tooltip :mouse-enter-delay="300">
                <MsButton type="icon" status="default" class="!mr-0" @click="handleCopy(item)">
                  <MsIcon type="icon-icon_copy_outlined" class="text-[var(--color-text-4)]" />
                </MsButton>
                <template #content>
                  <span>{{ t('common.copy') }}</span>
                </template>
              </a-tooltip>
              <a-tooltip :mouse-enter-delay="300">
                <MsButton type="icon" status="default" class="!mr-0" @click="handleReset(item)">
                  <MsIcon type="icon-icon_reset_outlined" class="text-[var(--color-text-4)]" />
                </MsButton>
                <template #content>
                  <span>{{ t('ms.ai.regenerate') }}</span>
                </template>
              </a-tooltip>
              <a-tooltip :mouse-enter-delay="300">
                <MsButton type="icon" status="default" class="!mr-0" @click="handleSync(item)">
                  <MsIcon type="icon-icon_synchronous" class="text-[var(--color-text-4)]" />
                </MsButton>
                <template #content>
                  <span>{{ t('ms.ai.caseSync') }}</span>
                </template>
              </a-tooltip>
            </div>
          </div>
        </template>
      </BubbleList>
    </div>
    <div class="flex items-center gap-[12px] py-[16px]">
      <MsAiButton v-if="props.type === 'case'" :text="t('ms.ai.generateFeatureCase')" no-icon @click="jump('case')" />
      <MsAiButton v-if="props.type === 'api'" :text="t('ms.ai.generateApiCase')" no-icon @click="jump('api')" />
      <MsAiButton :text="t('ms.ai.openNewConversation')" @click="handleOpenNewConversation" />
    </div>
    <Sender
      ref="senderRef"
      v-model="senderValue"
      variant="updown"
      :auto-size="{ minRows: 2, maxRows: 5 }"
      clearable
      allow-speech
      :placeholder="t('ms.ai.casePlaceholder')"
      @submit="handleSubmit"
    >
      <template #action-list>
        <div class="flex items-center gap-[16px]">
          <MsSelect v-model:model-value="model" :options="models" size="mini" class="!w-[100px]"></MsSelect>
          <MsButton
            v-if="props.type !== 'chat'"
            type="text"
            status="default"
            class="!mr-0 text-[var(--color-text-n2)]"
            @click="configModalVisible = true"
          >
            <MsIcon type="icon-icon-setting" :size="20" />
          </MsButton>
          <MsButton
            v-if="answering"
            type="text"
            class="hover:text-[rgb(var(--primary-4))] active:text-[rgb(var(--primary-7))]"
            @click="handleSendClick"
          >
            <MsIcon type="icon-icon_stop_outlined" :size="22" />
          </MsButton>
          <MsButton
            v-else
            type="text"
            class="hover:text-[rgb(var(--primary-4))] active:text-[rgb(var(--primary-7))]"
            :disabled="!senderValue"
            @click="handleSendClick"
          >
            <MsIcon type="icon-icon_release" :size="22" />
          </MsButton>
        </div>
      </template>
    </Sender>
  </a-spin>
  <caseConfigModal v-if="props.type === 'case'" v-model:visible="configModalVisible" />
  <apiConfigModal v-if="props.type === 'api'" v-model:visible="configModalVisible" />
  <apiSelectModal v-if="props.type === 'api'" v-model:visible="apiSelectModalVisible" />
</template>

<script setup lang="ts">
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import { Bubble, BubbleList, Sender } from 'vue-element-plus-x';

  import MsAiButton from '@/components/pure/ms-ai-button/index.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSelect from '@/components/business/ms-select';

  import { AxiosCanceler } from '@/api/http/axiosCancel';
  import { addAiChat, aiChat, getAiChatDetail } from '@/api/modules/ai';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useAppStore from '@/store/modules/app';
  import useAIStore from '@/store/modules/setting/ai';
  import { getGenerateId } from '@/utils';

  import { AiChatContentItem, AiChatListItem } from '@/models/ai';
  import { AiChatContentRoleTypeEnum } from '@/enums/aiEnums';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';

  const apiConfigModal = defineAsyncComponent(() => import('./apiConfigModal.vue'));
  const apiSelectModal = defineAsyncComponent(() => import('./apiSelectModal.vue'));
  const caseConfigModal = defineAsyncComponent(() => import('./caseConfigModal.vue'));

  const props = withDefaults(
    defineProps<{
      type: 'case' | 'api' | 'chat';
    }>(),
    {
      type: 'chat',
    }
  );
  const emit = defineEmits<{
    (e: 'openNewConversation'): void;
    (e: 'addSuccess'): void;
  }>();

  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();
  const { copy, isSupported } = useClipboard({ legacy: true });
  const appStore = useAppStore();
  const aiStore = useAIStore();

  const activeConversation = defineModel<AiChatListItem | undefined>('value', {
    required: true,
  });

  const loading = ref(false);
  const title = computed(() => activeConversation.value?.title || '');
  const editTitle = ref(false);
  const titleInputRef = ref<InstanceType<typeof HTMLInputElement>>();

  function handleEditTitle() {
    editTitle.value = true;
    nextTick(() => {
      titleInputRef.value?.focus();
    });
  }

  const senderRef = ref<InstanceType<typeof Sender>>();
  const senderValue = ref('');
  const answering = ref(false);
  const model = ref(aiStore.aiSourceNameList[0]?.id || ''); // 默认选择第一个模型
  const models = computed(() =>
    aiStore.aiSourceNameList.map((item) => ({
      label: item.name,
      value: item.id,
    }))
  );
  const configModalVisible = ref(false);

  const conversationItems = ref<AiChatContentItem[]>([]);
  const avatar = ref('https://avatars.githubusercontent.com/u/76239030?v=4');

  function handleCopy(item: AiChatContentItem) {
    if (isSupported) {
      copy(item.content || '');
      Message.success(t('common.copySuccess'));
    } else {
      Message.error(t('common.copyNotSupport'));
    }
  }

  const apiSelectModalVisible = ref(false);
  function jump(type: 'case' | 'api') {
    if (type === 'api') {
      apiSelectModalVisible.value = true;
    } else {
      openNewPage(CaseManagementRouteEnum.CASE_MANAGEMENT, {
        openAi: 'Y',
      });
    }
  }

  function handleOpenNewConversation() {
    conversationItems.value = [];
    emit('openNewConversation');
  }

  function handleEdit(item: AiChatContentItem) {
    // item.isEdit = true;
    senderValue.value = item.content || '';
    senderRef.value?.focus();
  }

  async function handleSubmit() {
    if (!senderValue.value || answering.value) return;
    conversationItems.value.push({
      id: getGenerateId(),
      conversationId: activeConversation.value?.id || '',
      type: AiChatContentRoleTypeEnum.USER,
      isEdit: false,
      timestamp: new Date().getTime(),
      content: senderValue.value,
      placement: 'end',
    });
    conversationItems.value.push({
      id: getGenerateId(),
      conversationId: activeConversation.value?.id || '',
      type: AiChatContentRoleTypeEnum.ASSISTANT,
      isEdit: false,
      timestamp: new Date().getTime(),
      content: t('ms.ai.thinking'),
      loading: true,
      placement: 'start',
    });
    const prompt = senderValue.value.trim();
    senderValue.value = '';
    try {
      answering.value = true;
      let res = '';
      const newId = getGenerateId();
      if (!activeConversation.value || activeConversation.value?.isNew) {
        const addRes = await addAiChat({
          prompt,
          chatModelId: model.value,
          conversationId: activeConversation.value?.id || newId,
          organizationId: appStore.currentOrgId || '',
        });
        activeConversation.value = {
          id: addRes.id,
          title: addRes.title,
          isNew: false,
          createTime: addRes.createTime,
          createUser: addRes.createUser,
        };
        emit('addSuccess');
      }
      res = await aiChat({
        prompt,
        chatModelId: model.value,
        conversationId: activeConversation.value?.id || newId,
        organizationId: appStore.currentOrgId || '',
      });
      conversationItems.value[conversationItems.value.length - 1] = {
        ...conversationItems.value[conversationItems.value.length - 1],
        content: res,
        loading: false,
        isMarkdown: true,
      };
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log('Error submitting:', error);
      if ((error as any).code === 'ERR_CANCELED') {
        conversationItems.value[conversationItems.value.length - 1] = {
          ...conversationItems.value[conversationItems.value.length - 1],
          content: t('ms.ai.hasStopped'),
          loading: false,
          isMarkdown: false,
        };
      } else {
        conversationItems.value[conversationItems.value.length - 1] = {
          ...conversationItems.value[conversationItems.value.length - 1],
          content: t('ms.ai.failed'),
          loading: false,
          isMarkdown: false,
        };
      }
    } finally {
      answering.value = false;
    }
  }

  function handleReset(item: AiChatContentItem) {
    const lastUserAsk = conversationItems.value[conversationItems.value.findIndex((e) => e.id === item.id) - 1];

    if (lastUserAsk) {
      senderValue.value = lastUserAsk.content || '';
      handleSubmit();
    }
  }
  function handleSync(item: AiChatContentItem) {
    console.log('同步', item);
  }

  function handleSendClick() {
    if (answering.value) {
      answering.value = false; // 停止回答
      const axiosCanceler = new AxiosCanceler();
      axiosCanceler.removeAllPending();
      return;
    }
    if (senderValue.value) {
      handleSubmit();
    }
  }

  async function initDetail() {
    if (!activeConversation.value || activeConversation.value?.isNew) {
      conversationItems.value = [];
      return;
    }
    if (activeConversation.value) {
      try {
        loading.value = true;
        const res = await getAiChatDetail(activeConversation.value.id);
        conversationItems.value = res.map((e) => ({
          ...e,
          isMarkdown: true,
          placement: e.type === AiChatContentRoleTypeEnum.ASSISTANT ? 'start' : 'end',
        }));
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log('Error initializing conversation detail:', error);
      } finally {
        loading.value = false;
      }
    }
  }

  watch(
    () => activeConversation.value?.id,
    () => {
      if (!answering.value) {
        initDetail();
      }
    },
    {
      immediate: true,
    }
  );
</script>

<style lang="less" scoped>
  :deep(.el-sender:focus-within) {
    border-color: rgb(var(--primary-6));
    &::after {
      border-width: 1px;
    }
  }
  :deep(.el-bubble) {
    gap: 8px;
    .el-bubble-content-filled {
      padding: 0;
      width: 100%;
      max-width: 100%;
      background: white;
    }
    .el-bubble-footer {
      margin-top: 4px;
    }
  }
  :deep(.el-bubble-end) {
    .el-bubble-content-filled {
      @apply flex justify-end;
    }
  }
  .conversation-title {
    margin-bottom: 16px;
    padding: 4px 8px;
    width: 100%;
    :deep(.arco-input) {
      padding: 3px 0;
      font-size: 16px;
      font-weight: 500;
      text-align: center;
      line-height: 24px;
    }
  }
  .avatar-wrapper {
    @apply overflow-hidden;

    width: 40px;
    height: 40px;
    border-radius: 50%;
    background-color: var(--color-text-n9);
    img {
      width: 100%;
      height: 100%;
    }
  }
  .header-wrapper {
    .header-name {
      font-size: 14px;
      color: #979797;
    }
  }
  .content-wrapper {
    @apply flex;

    max-width: 80%;
    .content-text {
      padding: 12px;
      min-height: 48px;
      font-size: 14px;
      border-radius: 12px 0 12px 12px;
      color: #333333;
      background: rgb(var(--link-1));
      :deep(.typer-content) {
        background: rgb(var(--link-1));
      }
    }
  }
  :deep(.el-bubble-loading-wrap) {
    @apply !justify-start;
  }
  .loading-wrapper {
    padding: 8px;
    min-height: 48px;
    font-size: 12px;
    color: #333333;
    :deep(.arco-dot-loading) {
      width: 48px;
      .arco-dot-loading-item {
        width: 6px;
        height: 6px;
      }
    }
  }
  .footer-wrapper {
    display: flex;
    align-items: center;
  }
  .footer-container {
    @apply flex items-center;

    gap: 8px;
  }
</style>
