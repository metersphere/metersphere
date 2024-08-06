<template>
  <div
    v-if="props.requestResult?.responseResult?.responseCode"
    class="flex items-center justify-between gap-[16px] text-[14px]"
  >
    <a-tooltip :content="props.requestResult.fakeErrorCode" :disabled="!props.requestResult.fakeErrorCode">
      <executeStatus :status="finalStatus" size="small" class="ml-[4px]" />
    </a-tooltip>
    <a-popover position="left" content-class="response-popover-content">
      <div class="one-line-text max-w-[200px]" :style="{ color: statusCodeColor }">
        {{ props.requestResult.responseResult.responseCode }}
      </div>
      <template #content>
        <div class="flex items-center gap-[8px] text-[14px]">
          <div class="text-[var(--color-text-4)]">{{ t('apiTestDebug.statusCode') }}</div>
          <div :style="{ color: statusCodeColor }">
            {{ props.requestResult.responseResult.responseCode }}
          </div>
        </div>
      </template>
    </a-popover>
    <a-popover position="left" content-class="w-[400px]">
      <div class="one-line-text text-[rgb(var(--success-7))]"> {{ timingInfo?.responseTime }} ms </div>
      <template #content>
        <div class="mb-[8px] flex items-center gap-[8px] text-[14px]">
          <div class="text-[var(--color-text-4)]">{{ t('apiTestDebug.responseTime') }}</div>
          <div class="text-[rgb(var(--success-7))]"> {{ timingInfo?.responseTime }} ms </div>
        </div>
        <responseTimeLine v-if="timingInfo" :response-timing="timingInfo" />
      </template>
    </a-popover>
    <a-popover position="left" content-class="response-popover-content">
      <div class="one-line-text text-[rgb(var(--success-7))]">
        {{ props.requestResult.responseResult.responseSize }} bytes
      </div>
      <template #content>
        <div class="flex items-center gap-[8px] text-[14px]">
          <div class="text-[var(--color-text-4)]">{{ t('apiTestDebug.responseSize') }}</div>
          <div class="one-line-text text-[rgb(var(--success-7))]">
            {{ props.requestResult.responseResult.responseSize }} bytes
          </div>
        </div>
      </template>
    </a-popover>
  </div>
</template>

<script setup lang="ts">
  import responseTimeLine from '@/views/api-test/components/responseTimeLine.vue';
  import executeStatus from '@/views/api-test/scenario/components/common/executeStatus.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { RequestResult } from '@/models/apiTest/common';
  import { ScenarioExecuteStatus } from '@/enums/apiEnum';

  const props = defineProps<{
    requestResult?: RequestResult;
  }>();

  const { t } = useI18n();

  // 响应时间信息
  const timingInfo = computed(() => {
    if (props.requestResult) {
      const {
        dnsLookupTime,
        downloadTime,
        latency,
        responseTime,
        socketInitTime,
        sslHandshakeTime,
        tcpHandshakeTime,
        transferStartTime,
      } = props.requestResult.responseResult;
      return {
        dnsLookupTime,
        tcpHandshakeTime,
        sslHandshakeTime,
        socketInitTime,
        latency,
        downloadTime,
        transferStartTime,
        responseTime,
      };
    }
    return null;
  });
  // 响应状态码对应颜色
  const statusCodeColor = computed(() => {
    if (props.requestResult) {
      const code = props.requestResult.responseResult.responseCode;
      if (code >= 200 && code < 300) {
        return 'rgb(var(--success-7)';
      }
      if (code >= 300 && code < 400) {
        return 'rgb(var(--warning-7)';
      }
      return 'rgb(var(--danger-7)';
    }
    return '';
  });
  const finalStatus = computed(() => {
    if (props.requestResult?.fakeErrorCode) {
      return ScenarioExecuteStatus.FAKE_ERROR;
    }
    return props.requestResult?.isSuccessful ? ScenarioExecuteStatus.SUCCESS : ScenarioExecuteStatus.FAILED;
  });
</script>

<style lang="less">
  .response-popover-content {
    padding: 4px 8px;
    .arco-popover-content {
      @apply mt-0;

      font-size: 14px;
      line-height: 22px;
    }
  }
</style>
