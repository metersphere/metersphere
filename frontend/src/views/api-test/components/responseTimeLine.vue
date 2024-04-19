<template>
  <div class="flex w-full gap-[8px] rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] p-[12px]">
    <div class="text-item-wrapper">
      <div class="light-item">{{ t('apiTestDebug.responseStage') }}</div>
      <div class="normal-item">{{ t('apiTestDebug.dnsQuery') }}</div>
      <div class="normal-item">{{ t('apiTestDebug.tcpHandshake') }}</div>
      <div class="normal-item">{{ t('apiTestDebug.sslHandshake') }}</div>
      <div class="normal-item">{{ t('apiTestDebug.socketInit') }}</div>
      <!-- <div class="normal-item">{{ t('apiTestDebug.waitingTTFB') }}</div> -->
      <div class="normal-item">{{ t('apiTestDebug.downloadContent') }}</div>
      <div class="light-item">{{ t('apiTestDebug.deal') }}</div>
      <div class="total-item">{{ t('apiTestDebug.total') }}</div>
    </div>
    <a-divider direction="vertical" margin="0" />
    <div class="flex flex-1 flex-col gap-[8px]">
      <div class="h-[16px]"></div>
      <div v-for="line of timingLines" :key="line.key" class="flex items-center bg-transparent py-[2px]">
        <div
          class="h-[12px] rounded-[var(--border-radius-mini)] bg-[rgb(var(--success-7))]"
          :style="{
            width: line.width,
            marginLeft: line.left,
          }"
        ></div>
      </div>
      <div class="h-[16px]"></div>
    </div>
    <a-divider direction="vertical" margin="0" />
    <div class="text-item-wrapper--right">
      <div class="light-item">{{ t('apiTestDebug.time') }}</div>
      <div class="normal-item">{{ props.responseTiming.dnsLookupTime }} ms</div>
      <div class="normal-item">{{ props.responseTiming.tcpHandshakeTime }} ms</div>
      <div class="normal-item">{{ props.responseTiming.sslHandshakeTime }} ms</div>
      <div class="normal-item">{{ props.responseTiming.socketInitTime }} ms</div>
      <!-- <div class="normal-item">{{ props.responseTiming.latency }} ms</div> -->
      <div class="normal-item">{{ props.responseTiming.downloadTime }} ms</div>
      <div class="light-item">{{ props.responseTiming.transferStartTime }} ms</div>
      <div class="total-item">{{ props.responseTiming.responseTime }} ms</div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  import { ResponseTiming } from '@/models/apiTest/common';

  const props = defineProps<{
    responseTiming: ResponseTiming;
  }>();

  const { t } = useI18n();

  const timingLines = computed(() => {
    const arr: { key: string; width: string; left: string }[] = [];
    const keys = Object.keys(props.responseTiming).filter((key) => key !== 'total');
    let preLinesTotalLeft = 0;
    keys.forEach((key, index) => {
      if (key !== 'responseTime' && key !== 'latency') {
        // 总耗时就是 100%，不需要绘制
        const itemWidth = (props.responseTiming[key as keyof ResponseTiming] / props.responseTiming.responseTime) * 100;
        arr.push({
          key,
          width: `${itemWidth}%`,
          left: index !== 0 ? `${preLinesTotalLeft}%` : '',
        });
        preLinesTotalLeft += itemWidth;
      }
    });
    return arr;
  });
</script>

<style lang="less" scoped>
  .text-item-wrapper,
  .text-item-wrapper--right {
    display: flex;
    flex-direction: column;
    gap: 8px;
    font-size: 12px;
    .light-item {
      color: var(--color-text-4);
      line-height: 16px;
    }
    .normal-item {
      color: var(--color-text-1);
      line-height: 16px;
    }
    .total-item {
      font-weight: 600;
      color: rgb(var(--success-7));
      line-height: 16px;
    }
  }
  .text-item-wrapper--right {
    align-items: flex-end;
  }
</style>
