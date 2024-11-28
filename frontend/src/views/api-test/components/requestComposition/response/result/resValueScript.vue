<template>
  <div class="h-full overflow-hidden rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] p-[12px]">
    <pre class="response-header-pre">{{ getResponsePreContent(props.activeTab) }}</pre>
  </div>
</template>

<script setup lang="ts">
  /**
   * @description (共用) 请求头 || 提取 || 真实请求
   */
  import { useI18n } from '@/hooks/useI18n';

  import { RequestResult } from '@/models/apiTest/common';
  import { ResponseComposition } from '@/enums/apiEnum';

  const { t } = useI18n();

  const props = defineProps<{
    requestResult?: RequestResult;
    activeTab: keyof typeof ResponseComposition;
  }>();

  function getResponsePreContent(type: keyof typeof ResponseComposition) {
    switch (type) {
      case ResponseComposition.HEADER:
        return props.requestResult?.responseResult?.headers.trim();
      case ResponseComposition.REAL_REQUEST:
        return props.requestResult?.body
          ? `${t('apiTestDebug.requestUrl')}:\n${props.requestResult.url}\n${t('apiTestDebug.header')}:\n${
              props.requestResult.headers
            }\nBody:\n${props.requestResult.body.trim()}`
          : '';
      default:
        return '';
    }
  }
</script>

<style lang="less" scoped>
  .response-header-pre {
    @apply h-full overflow-auto;
    .ms-scroll-bar();

    padding: 8px 12px;
    border-radius: var(--border-radius-small);
    background-color: var(--color-text-fff);
  }
</style>
