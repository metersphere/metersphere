<template>
  <div class="progress-capacity">
    <div class="capacity-attr">
      <span>{{ props.name }}</span>
      <span class="capacity-value"> {{ Number.isNaN(props.percent) ? 0 : Number(props.percent.toFixed(1)) }} %</span>
    </div>
    <a-progress :percent="percentValue" :show-text="false" :color="props.color" />
  </div>
</template>

<script setup lang="ts">
  const props = defineProps<{
    name: string;
    percent: number;
    color: string;
  }>();

  const percentValue = computed(() => {
    return Number(props.percent / 100);
  });
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
