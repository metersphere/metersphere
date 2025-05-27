<template>
  <div class="flex h-full flex-col p-[24px]">
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
      class="mb-[16px] w-full cursor-pointer py-[8px] text-center text-[16px] font-medium leading-[24px]"
      @click="handleEditTitle"
    >
      {{ title }}
    </div>
    <div class="flex-1 overflow-hidden">
      <BubbleList :list="bubbleItems" max-height="100%">
        <!-- 自定义头像 -->
        <template #avatar="{ item }">
          <div class="avatar-wrapper">
            <svg-icon v-if="item.role === 'ai'" width="40px" height="40px" name="ai" />
            <img v-else :src="avatar" alt="avatar" />
          </div>
        </template>

        <!-- 自定义气泡内容 -->
        <template #content="{ item }">
          <div v-if="item.isEdit" class="flex w-full flex-col gap-[8px]">
            <a-textarea v-model:model-value="tempInput"></a-textarea>
            <div class="flex items-center justify-end gap-[8px]">
              <a-button type="secondary" @click="cancelInput(item)">{{ t('common.cancel') }}</a-button>
              <a-button type="primary" @click="sendInput(item)">{{ t('ms.ai.send') }}</a-button>
            </div>
          </div>
          <a-trigger v-else position="left" auto-fit-position :popup-translate="[-8, 0]">
            <div class="content-wrapper">
              <div class="content-text" :class="item.role === 'ai' ? '!rounded-none !bg-transparent' : ''">
                {{ item.content }}
              </div>
            </div>
            <template v-if="item.role === 'user'" #content>
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
          <div v-if="item.role === 'ai'" class="footer-wrapper">
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
      <MsAiButton :text="t('ms.ai.generateFeatureCase')" no-icon @click="jump('case')" />
      <MsAiButton :text="t('ms.ai.generateApiCase')" no-icon @click="jump('api')" />
      <MsAiButton :text="t('ms.ai.openNewConversation')" @click="emit('openNewConversation')" />
    </div>
    <Sender
      v-model="senderValue"
      variant="updown"
      :auto-size="{ minRows: 2, maxRows: 5 }"
      clearable
      allow-speech
      :placeholder="t('ms.ai.casePlaceholder')"
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
            type="text"
            class="hover:text-[rgb(var(--primary-4))] active:text-[rgb(var(--primary-7))]"
            :disabled="!senderValue"
          >
            <MsIcon type="icon-icon_release" :size="22" />
          </MsButton>
        </div>
      </template>
    </Sender>
  </div>
  <caseConfigModal v-if="props.type === 'case'" v-model:visible="configModalVisible" />
  <apiConfigModal v-if="props.type === 'api'" v-model:visible="configModalVisible" />
</template>

<script setup lang="ts">
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import { BubbleList, Sender } from 'vue-element-plus-x';
  // eslint-disable-next-line import/no-unresolved
  import { BubbleListItemProps, BubbleListProps } from 'vue-element-plus-x/types/components/BubbleList/types';

  import MsAiButton from '@/components/pure/ms-ai-button/index.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import apiConfigModal from './apiConfigModal.vue';
  import caseConfigModal from './caseConfigModal.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';

  import { ApiTestRouteEnum, CaseManagementRouteEnum } from '@/enums/routeEnum';

  type listType = BubbleListItemProps & {
    key: number;
    role: 'user' | 'ai';
    isEdit?: boolean;
  };

  const props = withDefaults(
    defineProps<{
      id: string;
      type: 'case' | 'api' | 'chat';
    }>(),
    {
      type: 'chat',
    }
  );
  const emit = defineEmits<{
    (e: 'openNewConversation'): void;
  }>();

  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();
  const { copy, isSupported } = useClipboard({ legacy: true });

  const title = ref(t('ms.ai.newConversation'));
  const editTitle = ref(false);
  const titleInputRef = ref<InstanceType<typeof HTMLInputElement>>();

  function handleEditTitle() {
    editTitle.value = true;
    nextTick(() => {
      titleInputRef.value?.focus();
    });
  }

  const senderValue = ref('');
  const model = ref('gpt-3.5-turbo');
  const models = ref([
    { label: 'GPT-3.5', value: 'gpt-3.5-turbo' },
    { label: 'GPT-4', value: 'gpt-4' },
  ]);
  const configModalVisible = ref(false);

  function generateFakeItems(count: number): listType[] {
    const messages: listType[] = [];
    for (let i = 0; i < count; i++) {
      const role = i % 2 === 0 ? 'ai' : 'user';
      const placement = role === 'ai' ? 'start' : 'end';
      const key = i + 1;
      messages.push({
        key,
        role,
        placement,
        isEdit: false,
        content: role === 'ai' ? `AI Response ${key}` : `User Message ${key}`,
      });
    }
    return messages;
  }

  const bubbleItems = ref<BubbleListProps<listType>['list']>(generateFakeItems(10));
  const avatar = ref('https://avatars.githubusercontent.com/u/76239030?v=4');

  function handleCopy(item: listType) {
    if (isSupported) {
      copy(item.content || '');
      Message.success(t('common.copySuccess'));
    } else {
      Message.error(t('common.copyNotSupport'));
    }
  }
  function handleReset(item: listType) {
    console.log('重置', item);
  }
  function handleSync(item: listType) {
    console.log('同步', item);
  }

  function jump(type: 'case' | 'api') {
    if (type === 'api') {
      openNewPage(ApiTestRouteEnum.API_TEST_MANAGEMENT, {
        openAi: 'Y',
      });
    } else {
      openNewPage(CaseManagementRouteEnum.CASE_MANAGEMENT, {
        openAi: 'Y',
      });
    }
  }

  const tempInput = ref('');

  function handleEdit(item: listType) {
    item.isEdit = true;
    tempInput.value = item.content || '';
  }

  function cancelInput(item: listType) {
    item.isEdit = false;
    tempInput.value = '';
  }

  function sendInput(item: listType) {
    if (tempInput.value.trim()) {
      bubbleItems.value.push({
        key: Date.now(),
        role: 'user',
        isEdit: false,
      });
      item.isEdit = false;
      tempInput.value = '';
      senderValue.value = '';
    }
  }
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
    max-width: 80%;
    .content-text {
      padding: 12px;
      font-size: 14px;
      border-radius: 12px 0 12px 12px;
      color: #333333;
      background: rgb(var(--link-1));
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
