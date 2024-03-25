<template>
  <div class="flex min-h-[110px] items-center">
    <div class="relative mr-4">
      <div class="absolute bottom-0 left-[30%] top-[35%] text-center">
        <div class="text-[12px] text-[(var(--color-text-4))]">{{ t('report.detail.api.total') }}</div>
        <div class="text-[18px] font-medium">{{ totalCount }}</div>
      </div>
      <MsChart width="110px" height="110px" :options="props.options" />
    </div>
    <div class="chart-legend grid flex-1 gap-y-3">
      <!-- 图例开始 -->
      <div v-for="item of props.legendData" :key="item.value" class="chart-legend-item">
        <div class="chart-flag">
          <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full" :class="item.class"></div>
          <div class="mr-2 text-[var(--color-text-4)]">{{ item.label }}</div>
        </div>
        <div class="count">{{ item.count || 0 }}</div>
        <div class="count">{{ item.rote || 0 }}%</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /**
   * @description 用例报告独立报告
   */
  import { ref } from 'vue';

  import MsChart from '@/components/pure/chart/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { LegendData } from '@/models/apiTest/report';

  const { t } = useI18n();
  const props = defineProps<{
    options: Record<string, any>;
    legendData: LegendData[];
  }>();

  const totalCount = computed(() => {
    return props.legendData.reduce((prev, item) => {
      return prev + item.count;
    }, 0);
  });
</script>

<style scoped lang="less">
  .chart-legend {
    .chart-legend-item {
      @apply grid grid-cols-3;
    }
    .chart-flag {
      @apply flex items-center;
      .count {
        color: var(--color-text-1);
      }
    }
  }
</style>
