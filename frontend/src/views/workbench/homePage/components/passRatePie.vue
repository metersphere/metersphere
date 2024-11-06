<template>
  <div class="pass-rate-content">
    <MsChart :height="`${props.size}px`" :width="`${props.size}px`" :options="options" />
    <div class="pass-rate-title flex-1">
      <div v-for="item of props.valueList" :key="item.label" class="flex-1">
        <div class="mb-[8px] text-[var(--color-text-4)]">{{ item.label }}</div>
        <div class="pass-rate-count">{{ addCommasToNumber(item.value) }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsChart from '@/components/pure/chart/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { addCommasToNumber } from '@/utils';

  const { t } = useI18n();

  const props = defineProps<{
    options: Record<string, any>;
    size: number;
    valueList: {
      label: string;
      value: number;
    }[];
  }>();
</script>

<style scoped lang="less">
  .pass-rate-content {
    padding: 16px;
    width: 100%;
    border-radius: 6px;
    background: var(--color-text-n9);
    @apply flex items-center;
    .pass-rate-title {
      @apply ml-4 flex items-center gap-4;
      .pass-rate-count {
        font-size: 20px;
        color: rgb(var(--primary-4));
        @apply font-medium;
      }
    }
  }
</style>
