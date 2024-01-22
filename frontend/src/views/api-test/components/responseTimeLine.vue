<template>
  <div class="flex w-full gap-[8px] rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] p-[12px]">
    <div class="text-item-wrapper">
      <div class="light-item">{{ t('apiTestDebug.responseStage') }}</div>
      <div class="light-item">{{ t('apiTestDebug.ready') }}</div>
      <div class="normal-item">{{ t('apiTestDebug.socketInit') }}</div>
      <div class="normal-item">{{ t('apiTestDebug.dnsQuery') }}</div>
      <div class="normal-item">{{ t('apiTestDebug.tcpHandshake') }}</div>
      <div class="normal-item">{{ t('apiTestDebug.sslHandshake') }}</div>
      <div class="normal-item">{{ t('apiTestDebug.waitingTTFB') }}</div>
      <div class="normal-item">{{ t('apiTestDebug.downloadContent') }}</div>
      <div class="light-item">{{ t('apiTestDebug.deal') }}</div>
      <div class="total-item">{{ t('apiTestDebug.total') }}</div>
    </div>
    <a-divider direction="vertical" margin="0" />
    <div class="flex flex-1 flex-col">
      <div class="h-full"></div>
      <div v-for="line of timingLines" :key="line.key" class="flex h-full items-center bg-transparent">
        <div
          class="h-[12px] rounded-[var(--border-radius-mini)] bg-[rgb(var(--success-7))]"
          :style="{
            width: line.width,
            marginLeft: line.left,
          }"
        ></div>
      </div>
      <div class="h-full"></div>
    </div>
    <a-divider direction="vertical" margin="0" />
    <div class="text-item-wrapper--right">
      <div class="light-item">{{ t('apiTestDebug.time') }}</div>
      <div class="light-item">{{ props.responseTiming.ready }} ms</div>
      <div class="normal-item">{{ props.responseTiming.socketInit }} ms</div>
      <div class="normal-item">{{ props.responseTiming.dnsQuery }} ms</div>
      <div class="normal-item">{{ props.responseTiming.tcpHandshake }} ms</div>
      <div class="normal-item">{{ props.responseTiming.sslHandshake }} ms</div>
      <div class="normal-item">{{ props.responseTiming.waitingTTFB }} ms</div>
      <div class="normal-item">{{ props.responseTiming.downloadContent }} ms</div>
      <div class="light-item">{{ props.responseTiming.deal }} ms</div>
      <div class="total-item">{{ props.responseTiming.total }} ms</div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  import { ResponseTiming } from '@/models/apiTest/debug';

  const props = defineProps<{
    responseTiming: ResponseTiming;
  }>();

  const { t } = useI18n();

  const timingLines = computed(() => {
    const arr: { key: string; width: string; left: string }[] = [];
    const keys = Object.keys(props.responseTiming).filter((key) => key !== 'total');
    let preLinesTotalLeft = 0;
    keys.forEach((key, index) => {
      const itemWidth = (props.responseTiming[key] / props.responseTiming.total) * 100;
      arr.push({
        key,
        width: `${itemWidth}%`,
        left: index !== 0 ? `${preLinesTotalLeft}%` : '',
      });
      preLinesTotalLeft += itemWidth;
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
