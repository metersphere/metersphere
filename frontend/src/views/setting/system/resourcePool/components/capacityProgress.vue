<template>
  <!-- TODO 等待联调 -->
  <div class="progress-capacity">
    <div class="capacity-attr">
      <span>{{ props.name }}</span>
      <span class="capacity-value"> {{ props.percent * 100 }} %</span>
    </div>
    <a-progress :percent="props.percent" :show-text="false" :color="props.color" />
  </div>
</template>

<script setup lang="ts">
  const props = defineProps<{
    name: string;
    percent: number;
    color: string;
  }>();
</script>

<style scoped lang="less">
  .progress-capacity {
    padding: 4px 8px;
    min-width: 116px;
    height: 32px;
    border-radius: 4px;
    background-color: var(--color-text-n9) !important;
    @apply flex flex-col;
    .capacity-attr {
      font-size: 12px;
      color: var(--color-text-4);
      @apply flex items-center justify-between;
      .capacity-value {
        color: var(--color-text-1);
        @apply font-medium;
      }
    }
  }
</style>
