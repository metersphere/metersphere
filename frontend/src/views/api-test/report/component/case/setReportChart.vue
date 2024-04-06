<template>
  <div class="flex min-h-[110px] items-center">
    <div class="relative mr-4">
      <div class="charts absolute text-center">
        <div class="text-[12px] text-[(var(--color-text-4))]">{{ t('report.detail.api.total') }}</div>
        <a-popover position="bottom" content-class="response-popover-content">
          <div class="flex justify-center text-[18px] font-medium">
            <div class="one-line-text max-w-[60px]">{{ getIndicators(requestTotal) }}</div>
          </div>
          <template #content>
            <div class="min-w-[176px] max-w-[400px] p-4 text-[14px]">
              <div class="text-[12px] font-medium text-[var(--color-text-4)]">{{ t('report.detail.api.total') }}</div>
              <div class="mt-2 text-[18px] font-medium text-[var(--color-text-1)]">{{
                getIndicators(addCommasToNumber(requestTotal))
              }}</div>
            </div>
          </template>
        </a-popover>
      </div>
      <a-popover position="bottom" content-class="response-popover-content">
        <div> <MsChart width="110px" height="110px" :options="props.options" /></div>
        <template #content>
          <div class="min-w-[176px] max-w-[400px] p-4">
            <div v-for="item of legendData" :key="item.value" class="mb-2 flex justify-between">
              <div class="chart-flag flex items-center">
                <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full" :class="item.class"></div>
                <div class="mr-2 text-[var(--color-text-4)]">{{ item.label }}</div>
              </div>
              <div class="count font-medium">{{ item.count || 0 }}</div>
            </div>
          </div>
        </template>
      </a-popover>
    </div>

    <div class="chart-legend grid flex-1 gap-y-3">
      <!-- 图例开始 -->
      <div v-for="item of legendData" :key="item.value" class="chart-legend-item">
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
  import { addCommasToNumber, formatDuration } from '@/utils';

  import type { LegendData } from '@/models/apiTest/report';

  import { getIndicators } from '../../utils';

  const { t } = useI18n();
  const props = defineProps<{
    options: Record<string, any>;
    legendData: LegendData[];
    requestTotal: number;
  }>();
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
  .charts {
    top: 30%;
    right: 0;
    bottom: 0;
    left: 0;
    z-index: 99;
    margin: auto;
  }
</style>
