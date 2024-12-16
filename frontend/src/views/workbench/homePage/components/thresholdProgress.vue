<template>
  <a-trigger position="bottom" trigger="hover" align-point>
    <div class="progress-container">
      <div class="progress-bar-background">
        <div class="progress-bar" :style="{ width: `${props.progress}%`, background: `${props.progressColor}` }"></div>
      </div>
      <div class="threshold-line" :style="{ left: `${props.threshold === 100 ? 99 : props.threshold}%` }"></div>
    </div>
    <template #content>
      <div class="popover-trigger-content flex items-center justify-between">
        <div class="text-[var(--color-text-2)]">
          {{ t('workbench.homePage.passRate') }}
        </div>
        <div>
          {{ `${props.progress}%` }}
        </div>
      </div>
    </template>
  </a-trigger>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const props = defineProps<{
    threshold: number;
    progress: number;
    progressColor: string;
  }>();
</script>

<style scoped lang="less">
  .progress-container {
    position: relative;
    padding: 2px 0;
    width: 100%;
  }
  .progress-bar-background {
    position: relative;
    overflow: hidden;
    width: 100%;
    height: 10px;
    border-radius: 3px;
    background: var(--color-text-n8);
  }
  .progress-bar {
    height: 100%;
    border-radius: 3px;
    transition: width 0.3s ease;
  }
  .threshold-line {
    position: absolute;
    top: 0;
    z-index: 2;
    width: 2px;
    height: 14px;
    border-radius: 2px;
    background: rgb(var(--success-6));
  }
  .popover-trigger-content {
    padding: 8px 16px;
    width: 140px;
    border-radius: var(--border-radius-small);
    background-color: var(--color-text-fff);
    box-shadow: 0 4px 10px -1px rgb(100 100 102 / 15%);
  }
</style>
