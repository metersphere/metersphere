<template>
  <div v-show="props.requestResult?.responseResult.responseCode" class="h-full">
    <a-tabs v-model:active-key="activeTab" class="no-content border-b border-[var(--color-text-n8)]">
      <a-tab-pane v-for="item of responseCompositionTabList" :key="item.value" :title="item.label" />
    </a-tabs>
    <div class="response-container">
      <ResBody v-if="activeTab === ResponseComposition.BODY" :request-result="props.requestResult" @copy="copyScript" />
      <ResConsole v-else-if="activeTab === ResponseComposition.CONSOLE" :console="props.console?.trim()" />
      <ResValueScript
        v-else-if="
          activeTab === ResponseComposition.HEADER ||
          activeTab === ResponseComposition.REAL_REQUEST ||
          activeTab === ResponseComposition.EXTRACT
        "
        :active-tab="activeTab"
        :request-result="props.requestResult"
      />
      <ResAssertion v-else-if="activeTab === 'ASSERTION'" :request-result="props.requestResult" />
    </div>
  </div>
  <a-empty
    v-if="props.showEmpty"
    v-show="!props.requestResult?.responseResult.responseCode"
    class="flex h-[150px] items-center gap-[16px] p-[16px]"
  >
    <template #image>
      <img :src="noDataSvg" class="!h-[60px] w-[78px]" />
    </template>
    <div class="flex items-center gap-[8px]">
      <div>{{ t('apiTestManagement.click') }}</div>
      <MsButton
        class="!mr-0"
        type="text"
        :disabled="props.isHttpProtocol && !props.requestUrl"
        @click="emit('execute')"
      >
        {{ props.isPriorityLocalExec ? t('apiTestDebug.localExec') : t('apiTestDebug.serverExec') }}
      </MsButton>
      <div>{{ t('apiTestManagement.getResponse') }}</div>
    </div>
  </a-empty>
</template>

<script setup lang="ts">
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import ResAssertion from './result/assertionTable.vue';
  import ResBody from './result/body.vue';
  import ResConsole from './result/console.vue';
  import ResValueScript from './result/resValueScript.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { RequestResult } from '@/models/apiTest/common';
  import { ResponseComposition } from '@/enums/apiEnum';

  const props = withDefaults(
    defineProps<{
      requestResult?: RequestResult;
      console?: string;
      isPriorityLocalExec: boolean;
      requestUrl?: string;
      isHttpProtocol?: boolean;
      isDefinition?: boolean;
      showEmpty?: boolean;
    }>(),
    {
      showEmpty: true,
    }
  );
  const emit = defineEmits(['execute']);

  const { t } = useI18n();

  const noDataSvg = `${import.meta.env.BASE_URL}images/noResponse.svg`;
  const responseCompositionTabList = [
    {
      label: t('apiTestDebug.responseBody'),
      value: ResponseComposition.BODY,
    },
    {
      label: t('apiTestDebug.responseHeader'),
      value: ResponseComposition.HEADER,
    },
    {
      label: t('apiTestDebug.realRequest'),
      value: ResponseComposition.REAL_REQUEST,
    },
    {
      label: t('apiTestDebug.console'),
      value: ResponseComposition.CONSOLE,
    },
    ...(props.isDefinition
      ? [
          {
            label: t('apiTestDebug.extract'),
            value: ResponseComposition.EXTRACT,
          },
          {
            label: t('apiTestDebug.assertion'),
            value: ResponseComposition.ASSERTION,
          },
        ]
      : []),
  ];

  const activeTab = defineModel<ResponseComposition>('activeTab', {
    required: true,
    default: ResponseComposition.BODY,
  });

  const { copy, isSupported } = useClipboard({ legacy: true });

  function copyScript() {
    if (isSupported) {
      copy(props.requestResult?.responseResult.body || '');
      Message.success(t('common.copySuccess'));
    } else {
      Message.warning(t('apiTestDebug.copyNotSupport'));
    }
  }
</script>

<style lang="less" scoped>
  .response-container {
    margin-top: 8px;
    height: calc(100% - 48px);
  }
  :deep(.arco-table-th) {
    background-color: var(--color-text-n9);
  }
  :deep(.arco-tabs-tab) {
    @apply leading-none;
  }
  .no-content :deep(.arco-tabs-content) {
    display: none;
  }
</style>
