<template>
  <a-spin :loading="loading" class="flex h-full flex-col overflow-hidden p-[24px]">
    <template v-if="title">
      <a-input
        v-if="editTitle"
        ref="titleInputRef"
        v-model:model-value="tempTitle"
        size="large"
        class="conversation-title"
        :max-length="255"
        @keydown.enter="handleSaveTitle"
        @keydown.esc="editTitle = false"
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
      <a-checkbox-group v-model:model-value="checkedCases" class="flex max-h-full">
        <BubbleList
          ref="bubbleListRef"
          :list="conversationItems"
          :btn-loading="answering"
          max-height="100%"
          class="w-full"
        >
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
              <MsAvatar v-else is-user />
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
            <a-trigger position="left" auto-fit-position :popup-translate="[-8, 0]" popup-container=".content-wrapper">
              <div class="content-wrapper">
                <Bubble
                  class="content-text"
                  :class="item.type === AiChatContentRoleTypeEnum.ASSISTANT ? '!rounded-none !bg-transparent' : ''"
                >
                  <template #content>
                    <a-collapse
                      v-if="
                        item.type === AiChatContentRoleTypeEnum.ASSISTANT &&
                        (item.content.includes('featureCaseStart') || item.content.includes('apiCaseStart'))
                      "
                      :default-active-key="[item.id]"
                      expand-icon-position="right"
                      show-expand-icon
                      :bordered="false"
                      class="flex w-full flex-col"
                    >
                      <template #expand-icon="{ active }">
                        <icon-up v-if="active" />
                        <icon-down v-else />
                      </template>
                      <div
                        v-for="(caseItem, idx) in item.content
                          .split(/featureCaseEnd|apiCaseEnd/)
                          .filter((e) => e.trim())"
                        :key="caseItem + idx"
                        class="case-collapse-item flex w-full border-b border-[var(--color-text-n8)] pb-[16px]"
                      >
                        <a-checkbox
                          v-if="
                            (props.type === 'case' && item.content.includes('featureCaseStart')) ||
                            (props.type === 'api' && item.content.includes('apiCaseStart'))
                          "
                          :value="caseItem"
                          class="mt-[4px] h-[16px] items-start"
                        />
                        <a-collapse-item
                          :key="
                            item.content.split(/featureCaseEnd|apiCaseEnd/).filter((e) => e.trim()).length > 1
                              ? caseItem + idx
                              : item.id
                          "
                          class="flex-1 overflow-hidden"
                          :style="{
                            background: 'transparent',
                          }"
                        >
                          <template #header>
                            <h3 class="text-[1.25em] font-semibold">
                              {{ caseItem.split(/caseExpand/)[0].replace(/featureCaseStart|apiCaseStart|[#\s]/g, '') }}
                            </h3>
                          </template>
                          <div class="flex">
                            <Typewriter
                              :content="
                                caseItem
                                  .split(/caseExpand/)[1]
                                  ?.replace(/featureCaseStart|featureCaseEnd|apiCaseStart|apiCaseEnd/g, '')
                              "
                              :is-markdown="true"
                            />
                          </div>
                        </a-collapse-item>
                      </div>
                    </a-collapse>
                    <Typewriter
                      v-else
                      :content="item.content"
                      :is-markdown="item.type === AiChatContentRoleTypeEnum.ASSISTANT"
                    />
                  </template>
                </Bubble>
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
              </div>
            </div>
          </template>
        </BubbleList>
      </a-checkbox-group>
    </div>
    <div class="flex items-center justify-between gap-[12px] py-[16px]">
      <div class="flex items-center gap-[12px]">
        <template v-if="props.type === 'chat'">
          <MsAiButton
            v-permission="['FUNCTIONAL_CASE:READ+ADD']"
            :text="t('ms.ai.generateFeatureCase')"
            no-icon
            @click="jump('case')"
          />
          <MsAiButton
            v-permission="['PROJECT_API_DEFINITION_CASE:READ+ADD']"
            :text="t('ms.ai.generateApiCase')"
            no-icon
            @click="jump('api')"
          />
        </template>
        <a-button v-if="checkedCases.length > 0" type="outline" @click="handleSync()">
          <template #icon>
            <MsIcon type="icon-icon_synchronous" />
          </template>
          {{ t('ms.ai.caseSync') }}
        </a-button>
      </div>
      <MsAiButton :text="t('ms.ai.openNewConversation')" :disabled="answering" @click="handleOpenNewConversation" />
    </div>
    <Sender
      ref="senderRef"
      v-model="senderValue"
      variant="updown"
      :auto-size="{ minRows: 2, maxRows: 5 }"
      clearable
      allow-speech
      :placeholder="sendPlaceholder"
      @submit="handleSubmit"
    >
      <template #action-list>
        <div class="flex items-center gap-[16px]">
          <MsSelect
            v-model:model-value="model"
            :options="models"
            option-tooltip-position="left"
            full-tooltip-position="left"
            size="mini"
            class="!w-[100px]"
          ></MsSelect>
          <MsButton
            v-if="props.type !== 'chat'"
            type="text"
            status="default"
            class="!mr-0 text-[var(--color-text-n2)]"
            @click="configModalVisible = true"
          >
            <MsIcon type="icon-icon-setting" :size="20" />
          </MsButton>
          <a-tooltip v-if="answering" :content="t('ms.ai.stop')" position="tr" :disabled="!!senderValue">
            <MsButton
              type="text"
              class="hover:text-[rgb(var(--primary-4))] active:text-[rgb(var(--primary-7))]"
              @click="handleSendClick"
            >
              <MsIcon type="icon-icon_stop_outlined" :size="22" />
            </MsButton>
          </a-tooltip>
          <a-tooltip v-else :content="sendPlaceholder" position="tr" :disabled="!!senderValue">
            <MsButton
              type="text"
              class="hover:text-[rgb(var(--primary-4))] active:text-[rgb(var(--primary-7))]"
              :disabled="!senderValue"
              @click="handleSendClick"
            >
              <MsIcon type="icon-icon_release" :size="22" />
            </MsButton>
          </a-tooltip>
        </div>
      </template>
    </Sender>
  </a-spin>
  <caseConfigModal v-if="props.type === 'case'" v-model:visible="configModalVisible" />
  <caseModuleSelectModal
    v-if="props.type === 'case'"
    v-model:visible="caseModuleSelectModalVisible"
    :prompt="checkedCases.join('')"
    :conversation-id="activeConversation?.id || ''"
    :model="model"
    :template-id="props.templateId || ''"
    @sync-success="emit('syncSuccess')"
  />
  <apiConfigModal v-if="props.type === 'api'" v-model:visible="configModalVisible" />
  <apiSelectModal v-if="props.type === 'chat'" v-model:visible="apiSelectModalVisible" />
</template>

<script setup lang="ts">
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import { Bubble, BubbleList, Sender, Typewriter } from 'vue-element-plus-x';

  import MsAiButton from '@/components/pure/ms-ai-button/index.vue';
  import MsAvatar from '@/components/pure/ms-avatar/index.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSelect from '@/components/business/ms-select';

  import { AxiosCanceler } from '@/api/http/axiosCancel';
  import { addAiChat, aiChat, getAiChatDetail, updateAiChatTitle } from '@/api/modules/ai';
  import { apiAiCaseBatchSave, apiAiChat, apiAiTransform } from '@/api/modules/api-test/management';
  import { caseAiChat, caseAiTransform } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useAppStore from '@/store/modules/app';
  import useAIStore from '@/store/modules/setting/ai';
  import { getGenerateId } from '@/utils';

  import { AiCaseTransformResult, AiChatContentItem, AiChatListItem } from '@/models/ai';
  import { ApiCaseDetail } from '@/models/apiTest/management';
  import { AiChatContentRoleTypeEnum } from '@/enums/aiEnums';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';

  const apiConfigModal = defineAsyncComponent(() => import('./apiConfigModal.vue'));
  const apiSelectModal = defineAsyncComponent(() => import('./apiSelectModal.vue'));
  const caseConfigModal = defineAsyncComponent(() => import('./caseConfigModal.vue'));
  const caseModuleSelectModal = defineAsyncComponent(() => import('./caseModuleSelect.vue'));

  const props = withDefaults(
    defineProps<{
      type: 'case' | 'api' | 'chat';
      apiDefinitionId?: string | number;
      moduleId?: string | number; // 用例保存时的模块
      templateId?: string | number; // 功能用例保存时需要的模板 id
    }>(),
    {
      type: 'chat',
    }
  );
  const emit = defineEmits<{
    (e: 'openNewConversation'): void;
    (e: 'addSuccess'): void;
    (e: 'syncSuccess'): void;
    (e: 'syncApiCase', detail: ApiCaseDetail): void;
    (e: 'syncFeatureCase', detail: AiCaseTransformResult): void;
  }>();

  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();
  const { copy, isSupported } = useClipboard({ legacy: true });
  const appStore = useAppStore();
  const aiStore = useAIStore();

  const activeConversation = defineModel<AiChatListItem | undefined>('value', {
    required: true,
  });
  const answering = defineModel<boolean>('answering', {
    default: false,
  });

  const loading = ref(false);
  const title = computed(() => activeConversation.value?.title || '');
  const editTitle = ref(false);
  const titleInputRef = ref<InstanceType<typeof HTMLInputElement>>();
  const tempTitle = ref('');

  function handleEditTitle() {
    if (activeConversation.value?.isNew) {
      return;
    }
    tempTitle.value = title.value;
    editTitle.value = true;
    nextTick(() => {
      titleInputRef.value?.focus();
    });
  }

  async function handleSaveTitle() {
    try {
      if (!tempTitle.value.trim()) {
        editTitle.value = false;
        return;
      }
      await updateAiChatTitle({
        id: activeConversation.value?.id || '',
        title: tempTitle.value.trim(),
      });
      if (activeConversation.value) {
        activeConversation.value.title = tempTitle.value.trim();
      }
      editTitle.value = false;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log('Error saving title:', error);
    }
  }

  const senderRef = ref<InstanceType<typeof Sender>>();
  const senderValue = ref('');
  const model = ref(aiStore.aiSourceNameList[0]?.id || ''); // 默认选择第一个模型
  const models = computed(() =>
    aiStore.aiSourceNameList.map((item) => ({
      label: item.name,
      value: item.id,
    }))
  );

  watch(
    () => models.value,
    (vals) => {
      model.value =
        vals.find((item) => item.value === localStorage.getItem('aiChatModel'))?.value || vals[0]?.value || '';
    },
    { immediate: true }
  );

  watch(model, (newModel) => {
    localStorage.setItem('aiChatModel', newModel);
  });

  const configModalVisible = ref(false);
  const bubbleListRef = ref<any>();
  const conversationItems = ref<AiChatContentItem[]>([]);
  const checkedCases = ref<string[]>([]);
  const sendPlaceholder = computed(() => {
    if (props.type === 'case') {
      return t('ms.ai.casePlaceholder');
    }
    if (props.type === 'api') {
      return t('ms.ai.apiCasePlaceholder');
    }
    return t('ms.ai.sendTip');
  });

  function handleCopy(item: AiChatContentItem) {
    if (isSupported) {
      copy(item.content || '');
      Message.success(t('common.copySuccess'));
    } else {
      Message.error(t('common.copyNotSupport'));
    }
  }

  const apiSelectModalVisible = ref(false);
  const caseModuleSelectModalVisible = ref(false);
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
    if (!answering.value) {
      conversationItems.value = [];
      emit('openNewConversation');
    }
  }

  function handleEdit(item: AiChatContentItem) {
    // item.isEdit = true;
    senderValue.value = item.content || '';
    senderRef.value?.focus();
  }

  async function addChat(prompt: string, newId: string) {
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
      checkedCases: [],
    });
    const prompt = senderValue.value.trim();
    senderValue.value = '';
    nextTick(() => {
      bubbleListRef.value?.scrollToBottom();
    });
    try {
      answering.value = true;
      let res = '';
      const newId = getGenerateId();
      if (!activeConversation.value || activeConversation.value?.isNew) {
        addChat(prompt, newId);
      }
      if (props.type === 'case') {
        res = await caseAiChat({
          prompt,
          chatModelId: model.value,
          conversationId: activeConversation.value?.id || newId,
          organizationId: appStore.currentOrgId || '',
        });
      } else if (props.type === 'api') {
        res = await apiAiChat({
          prompt,
          chatModelId: model.value,
          conversationId: activeConversation.value?.id || newId,
          organizationId: appStore.currentOrgId || '',
          apiDefinitionId: props.apiDefinitionId || '',
        });
      } else {
        res = await aiChat({
          prompt,
          chatModelId: model.value,
          conversationId: activeConversation.value?.id || newId,
          organizationId: appStore.currentOrgId || '',
        });
      }

      conversationItems.value[conversationItems.value.length - 1] = {
        ...conversationItems.value[conversationItems.value.length - 1],
        content: res,
        loading: false,
      };
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log('Error submitting:', error);
      if ((error as any).code === 'ERR_CANCELED') {
        conversationItems.value[conversationItems.value.length - 1] = {
          ...conversationItems.value[conversationItems.value.length - 1],
          content: t('ms.ai.hasStopped'),
          loading: false,
        };
      } else {
        conversationItems.value[conversationItems.value.length - 1] = {
          ...conversationItems.value[conversationItems.value.length - 1],
          content: t('ms.ai.failed'),
          loading: false,
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

  async function syncSingleApiCase() {
    try {
      loading.value = true;
      const res = await apiAiTransform({
        prompt: checkedCases.value[0],
        chatModelId: model.value,
        conversationId: activeConversation.value?.id || '',
        organizationId: appStore.currentOrgId || '',
        apiDefinitionId: props.apiDefinitionId || '',
      });
      emit('syncApiCase', res);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log('Error syncing single API case:', error);
    } finally {
      loading.value = false;
    }
  }

  async function syncSingleFeatureCase() {
    try {
      loading.value = true;
      const res = await caseAiTransform({
        prompt: checkedCases.value[0],
        chatModelId: model.value,
        conversationId: activeConversation.value?.id || '',
        organizationId: appStore.currentOrgId || '',
      });
      emit('syncFeatureCase', res);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log('Error syncing single feature case:', error);
    } finally {
      loading.value = false;
    }
  }

  async function batchSaveAiCase() {
    try {
      loading.value = true;
      if (props.type === 'api') {
        const res = await apiAiCaseBatchSave({
          prompt: checkedCases.value.join('apiCaseEnd'),
          chatModelId: model.value,
          conversationId: activeConversation.value?.id || '',
          organizationId: appStore.currentOrgId || '',
          apiDefinitionId: props.apiDefinitionId || '',
          projectId: appStore.currentProjectId || '',
        });
        if (res.errorCount > 0) {
          Message.success(
            t('ms.ai.errorSyncTip', {
              successCount: res.successCount,
              errorCount: res.errorCount,
              errorDetail: res.errorDetail,
            })
          );
        } else {
          Message.success(t('ms.ai.caseSyncSuccess'));
        }
        checkedCases.value = [];
      } else if (props.type === 'case') {
        caseModuleSelectModalVisible.value = true;
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log('Error batch saving AI case:', error);
    } finally {
      loading.value = false;
    }
  }

  function handleSync() {
    if (checkedCases.value.length === 1) {
      if (props.type === 'api') {
        syncSingleApiCase();
      } else if (props.type === 'case') {
        syncSingleFeatureCase();
      }
    } else {
      batchSaveAiCase();
    }
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
      checkedCases.value = [];
      return;
    }
    if (activeConversation.value) {
      try {
        loading.value = true;
        const res = await getAiChatDetail(activeConversation.value.id);
        conversationItems.value = res.map((e) => ({
          ...e,
          checkedCases: [],
          placement: e.type === AiChatContentRoleTypeEnum.ASSISTANT ? 'start' : 'end',
        }));
        checkedCases.value = [];
        editTitle.value = false;
        nextTick(() => {
          bubbleListRef.value?.scrollToBottom();
        });
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log('Error initializing conversation detail:', error);
        conversationItems.value = [];
        checkedCases.value = [];
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

  function stopAnswer() {
    answering.value = false;
    const axiosCanceler = new AxiosCanceler();
    axiosCanceler.removeAllPending();
  }

  defineExpose({
    stopAnswer,
  });
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
      background: var(--color-text-fff);
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
    @apply relative flex;

    max-width: 80%;
    .content-text {
      @apply w-full;

      padding: 12px;
      min-height: 48px;
      font-size: 14px;
      border-radius: 12px 0 12px 12px;
      color: var(--color-text-1);
      background: rgb(var(--link-1));
      :deep(.typer-content) {
        color: var(--color-text-1);
        background: rgb(var(--link-1));
      }
      .case-collapse-item {
        &:not(:last-child) {
          margin-bottom: 24px;
        }
      }
      :deep(.arco-collapse-item-icon-right) {
        right: 0;
      }
      :deep(.arco-collapse-item-header-title) {
        width: calc(100% - 20px);
      }
      :deep(.arco-collapse-item-content) {
        margin-top: 16px;
        padding: 0;
      }
    }
    :deep(.markdown-body) {
      color: var(--color-text-1) !important;
    }
  }
  :deep(.el-bubble-loading-wrap) {
    @apply !justify-start;
  }
  .loading-wrapper {
    padding: 8px;
    min-height: 48px;
    font-size: 12px;
    color: var(--color-text-1);
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
