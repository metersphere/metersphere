<template>
  <MsCard
    :title="t('project.messageManagement.editMessage')"
    :sub-title="`(${messageDetail?.robotName}-${messageDetail?.taskTypeName}-${messageDetail?.eventName})`"
    :special-height="11"
    hide-footer
    has-breadcrumb
  >
    <template #headerRight>
      <a-button type="outline" class="arco-btn-outline--secondary" :disabled="saveLoading" @click="back">
        {{ t('common.cancel') }}
      </a-button>
      <a-button type="outline" class="arco-btn-outline--secondary mx-[12px]" :disabled="saveLoading" @click="reset">
        {{ t('common.resetDefault') }}
      </a-button>
      <a-button type="primary" :loading="saveLoading" @click="save">{{ t('common.save') }}</a-button>
    </template>
    <div class="flex h-full flex-nowrap gap-[16px]">
      <div class="flex h-full w-[268px] flex-col">
        <div class="flex items-center">
          <div class="font-medium text-[var(--color-text-1)]">{{ t('project.messageManagement.messageScript') }}</div>
          <a-tooltip :content="t('project.messageManagement.scriptTip')" position="tl" mini>
            <icon-question-circle class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-6))]" />
          </a-tooltip>
        </div>
        <a-select v-model:model-value="fieldType" class="my-[8px]" :options="fieldTypeOptions"></a-select>
        <a-spin class="relative h-[calc(100%-70px)] w-full" :loading="fieldLoading">
          <div :class="`field-out-container ${containerStatusClass}`">
            <div ref="fieldListRef" class="field-container">
              <div v-for="field of filterFields" :key="field.id" class="field-item" @click="addField(field)">
                <a-popover class="max-w-[400px]" position="right">
                  <div class="one-line-text w-[188px] text-[var(--color-text-1)]">{{ field.name }}</div>
                  <template #content>
                    <div class="flex">
                      <div class="mr-[8px] whitespace-nowrap text-[var(--color-text-4)]">
                        {{ t('project.messageManagement.var') }}
                      </div>
                      <div class="text-[var(--color-text-1)]">{{ `${field.id}` }}</div>
                    </div>
                    <div class="flex">
                      <div class="mr-[8px] whitespace-nowrap text-[var(--color-text-4)]">
                        {{ t('common.desc') }}
                      </div>
                      <div class="text-[var(--color-text-1)]">{{ field.name }}</div>
                    </div>
                  </template>
                </a-popover>
                <MsButton type="icon" class="field-plus" @click="addField(field)">
                  <MsIcon type="icon-icon_add_outlined" size="14"></MsIcon>
                </MsButton>
              </div>
              <a-empty v-if="filterFields.length === 0">{{ t('project.messageManagement.noMatchField') }}</a-empty>
            </div>
          </div>
        </a-spin>
      </div>
      <div class="flex flex-1 flex-col">
        <div class="mb-[8px] flex flex-wrap items-center">
          <div class="font-medium text-[var(--color-text-1)]">
            {{ t('project.messageManagement.messageTemplate') }}
          </div>
          <div class="ml-[8px] flex items-center whitespace-nowrap text-[rgb(var(--warning-6))]">
            <icon-exclamation-circle class="mr-[4px] text-[rgb(var(--warning-6))]" />
            {{
              currentLocale === 'zh-CN'
                ? '引用消息变量格式为：${变量名称}'
                : 'The format of the reference message variable is: ${variable name}'
            }}
          </div>
        </div>
        <div class="flex-1 overflow-hidden rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] p-[16px]">
          <div class="mb-[8px] text-[var(--color-text-4)]">{{ t('project.messageManagement.title') }}</div>
          <a-textarea
            ref="subjectInputRef"
            v-model:model-value="subject"
            :placeholder="t('project.messageManagement.titlePlaceholder')"
            :auto-size="{ minRows: 3, maxRows: 3 }"
            :max-length="1000"
            :disabled="saveLoading"
            class="break-keep"
            @focus="focusTarget = 'subject'"
          />
          <div class="mb-[8px] mt-[16px] text-[var(--color-text-4)]">{{ t('project.messageManagement.content') }}</div>
          <div v-if="template.length > 0 || focusTarget === 'template'" class="h-[calc(100%-156px)]">
            <a-textarea
              ref="templateInputRef"
              v-model:model-value="template"
              class="h-full overflow-scroll break-keep"
              :max-length="1000"
              auto-size
              :disabled="saveLoading"
              @focus="focusTarget = 'template'"
              @blur="focusTarget = null"
            />
          </div>
          <div
            v-else
            class="flex h-[calc(100%-156px)] flex-col items-center gap-[16px] bg-[var(--color-text-fff)]"
            @click="focusTemplate"
          >
            <div class="content-empty-img"></div>
            <div class="text-[var(--color-text-4)]">{{ t('project.messageManagement.contentTip') }}</div>
          </div>
        </div>
      </div>
      <div class="w-[400px]">
        <div class="mb-[8px] font-medium text-[var(--color-text-1)]">
          {{ t('project.messageManagement.updatePreview') }}
        </div>
        <div v-if="messageDetail" class="h-[calc(100%-30px)] overflow-hidden">
          <MessagePreview
            :robot="{
              ...messageDetail,
              template,
              subject,
            }"
            :fields="fields"
            function-name=""
            event-name=""
            is-update-preview
          />
        </div>
      </div>
    </div>
  </MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 项目管理-消息管理-编辑消息模板
   */
  import { computed, nextTick, onBeforeMount, Ref, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MessagePreview from './components/messagePreview.vue';

  import {
    getMessageDetail,
    getMessageFields,
    saveMessageConfig,
  } from '@/api/modules/project-management/messageManagement';
  import useContainerShadow from '@/hooks/useContainerShadow';
  import { useI18n } from '@/hooks/useI18n';
  import useLocale from '@/locale/useLocale';
  import useAppStore from '@/store/modules/app';
  import useUserStore from '@/store/modules/user';
  import { sleep } from '@/utils';

  import type { Field, MessageTemplateDetail } from '@/models/projectManagement/message';

  const route = useRoute();
  const router = useRouter();
  const appStore = useAppStore();
  const userStore = useUserStore();
  const { t } = useI18n();
  const { currentLocale } = useLocale();

  const fieldListRef = ref<HTMLElement | null>(null);
  const { isInitListener, containerStatusClass, setContainer, initScrollListener } = useContainerShadow({
    overHeight: 42,
    containerClassName: 'field-out-container',
  });

  function back() {
    router.back();
  }

  const messageDetail = ref<MessageTemplateDetail | null>();
  const subject = ref('');
  const template = ref('');
  const saveLoading = ref(false);

  async function save() {
    try {
      saveLoading.value = true;
      await saveMessageConfig({
        ...(messageDetail.value as MessageTemplateDetail),
        subject: subject.value,
        template: template.value,
        useDefaultSubject: subject.value === messageDetail.value?.defaultSubject,
        useDefaultTemplate: template.value === messageDetail.value?.defaultTemplate,
        taskType: route.query.taskType as string,
        event: route.query.event as string,
        projectId: appStore.currentProjectId,
        receiverIds: messageDetail.value?.receiverIds || [userStore.id || ''],
      });
      Message.success(t('project.messageManagement.saveSuccess'));
      await sleep(300);
      back();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      saveLoading.value = false;
    }
  }

  const fields = ref<Field[]>([]);
  const focusTarget = ref<'subject' | 'template' | null>('subject');
  const subjectInputRef: Ref = ref(null);
  const templateInputRef: Ref = ref(null);

  function addField(field: Field) {
    if (saveLoading.value) {
      return;
    }
    if (focusTarget.value === 'subject') {
      if (`\${${field.id}}`.length + subject.value.length > 64) {
        // 插入的变量超出限制长度，提示
        Message.warning(t('project.messageManagement.titleMax'));
        return;
      }
      // 获取光标位置，插入变量字符串
      const subjectInput = subjectInputRef.value?.$el.querySelector('textarea');
      const selectionStart = subjectInput?.selectionStart;
      subject.value = `${subject.value.slice(0, selectionStart)}\${${field.id}}${subject.value.slice(selectionStart)}`;
    } else {
      if (`\${${field.id}}`.length + template.value.length > 500) {
        // 插入的变量超出限制长度，提示
        Message.warning(t('project.messageManagement.contentMax'));
        return;
      }
      // 获取光标位置，插入变量字符串
      const templateInput = templateInputRef.value?.$el.querySelector('textarea');
      const selectionStart = templateInput?.selectionStart;
      template.value = `${template.value.slice(0, selectionStart)}\${${field.id}}${template.value.slice(
        selectionStart
      )}`;
    }
  }

  const fieldType = ref('all');
  const fieldTypeOptions = ref([
    {
      label: t('common.all'),
      value: 'all',
    },
  ]);
  const fieldLoading = ref(false);
  // 初始化字段列表 TODO:接口错误重试
  async function initFields() {
    try {
      fieldLoading.value = true;
      const res = await getMessageFields(appStore.currentProjectId, route.query.taskType as string);
      fields.value = res.fieldList;
      fieldTypeOptions.value = fieldTypeOptions.value.concat(
        res.fieldSourceList.map((item) => ({
          label: item.name,
          value: item.id,
        }))
      );
      nextTick(() => {
        if (fieldListRef.value && !isInitListener.value) {
          setContainer(fieldListRef.value);
          initScrollListener();
        }
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      fieldLoading.value = false;
    }
  }
  const filterFields = computed(() => {
    return fields.value.filter((item) => {
      if (fieldType.value === 'all') {
        return true;
      }
      return item.fieldSource === fieldType.value;
    });
  });

  const detailLoading = ref(false);
  async function initMessageDetail() {
    try {
      detailLoading.value = true;
      const { taskType, event, id } = route.query;
      const res = await getMessageDetail(appStore.currentProjectId, taskType as string, event as string, id as string);
      subject.value = res.subject;
      template.value = res.template;
      messageDetail.value = res;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      detailLoading.value = false;
    }
  }

  function focusTemplate() {
    focusTarget.value = 'template';
    nextTick(() => {
      templateInputRef.value?.focus();
    });
  }

  onBeforeMount(() => {
    initMessageDetail();
    initFields();
  });

  function reset() {
    subject.value = messageDetail.value?.defaultSubject || '';
    template.value = messageDetail.value?.defaultTemplate || '';
  }
</script>

<style lang="less" scoped>
  .field-out-container {
    @apply h-full;
    .ms-container--shadow-y();

    border-radius: var(--border-radius-small);
    background-color: var(--color-text-n9);
  }
  .field-container {
    @apply h-full overflow-y-auto;
    .ms-scroll-bar();

    padding: 16px 10px 16px 16px;
  }
  .field-item {
    @apply flex cursor-pointer;

    margin-bottom: 4px;
    padding: 8px 12px;
    height: 38px;
    border: 1px solid var(--color-text-n8);
    border-radius: var(--border-radius-small);
    background-color: var(--color-text-fff);
    gap: 8px;
    .field-plus {
      @apply invisible ml-auto;

      color: var(--color-text-4);
    }
    &:hover {
      background-color: rgb(var(--primary-1));
      .field-plus {
        @apply visible;
      }
    }
  }
  .overflow-scroll {
    @apply overflow-y-auto;
    .ms-scroll-bar();
  }
  .content-empty-img {
    margin-top: 100px;
    width: 160px;
    height: 90px;
    background: url('@/assets/images/message_content.svg') no-repeat;
    background-size: cover;
    box-shadow: 0 4px 10px -1px rgb(100 100 102 / 15%);
  }
</style>
