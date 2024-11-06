<template>
  <div :class="`min-h-[${props.size || '120px'}] flex items-center`">
    <div class="relative mr-4">
      <div class="charts">
        <div class="text-[12px] text-[(var(--color-text-4))]">{{ t('report.detail.api.total') }}</div>
        <a-popover position="bottom" content-class="report-popover-content">
          <div class="one-line-text w-full max-w-[90%] text-center text-[18px] font-medium">
            {{ getIndicators(requestTotal) }}
          </div>
          <template #content>
            <div class="min-w-[176px] max-w-[400px] text-[14px]">
              <div class="text-[12px] font-medium text-[var(--color-text-4)]">{{ t('report.detail.api.total') }}</div>
              <div class="mt-2 text-[18px] font-medium text-[var(--color-text-1)]">
                {{ getIndicators(addCommasToNumber(requestTotal)) }}
              </div>
            </div>
          </template>
        </a-popover>
      </div>
      <!-- TODO 汇总图例暂时不要了 -->
      <!-- <a-popover position="bottom" content-class="report-popover-content"> -->
      <div> <MsChart :width="props.size || '120px'" :height="props.size || '120px'" :options="props.options" /></div>
      <!-- <template #content>
          <div class="min-w-[176px] max-w-[400px] p-4">
            <div v-for="item of legendData" :key="item.value" class="mb-2 flex flex-nowrap justify-between">
              <div class="chart-flag flex flex-nowrap items-center">
                <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full" :class="item.class"></div>
                <div class="mr-2 text-[var(--color-text-4)]">{{ item.label }}</div>
              </div>
              <div class="count font-medium">{{ item.count || 0 }}</div>
            </div>
          </div>
        </template> -->
      <!-- </a-popover> -->
    </div>

    <div :class="`chart-legend flex flex-col gap-[${props.gap || 8}px]`">
      <!-- 图例开始 -->
      <div v-for="item of legendData" :key="item.value" class="chart-legend-item">
        <div class="flex flex-1 flex-nowrap items-center">
          <div class="mb-[2px] mr-[4px] h-[6px] w-[6px] rounded-full" :class="item.class"></div>
          <div class="text-[var(--color-text-4)]">{{ item.label }}</div>
        </div>
        <div class="flex-1 text-center font-medium text-[var(--color-text-1)]">{{ item.count || 0 }}</div>
        <div class="flex-1 text-end font-medium text-[var(--color-text-1)]">
          {{ item.rote || 0 }}
          <span v-if="String(item.rote) !== 'Calculating'"> </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /**
   * @description 用例报告独立报告
   */
  import MsChart from '@/components/pure/chart/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { addCommasToNumber } from '@/utils';

  import type { LegendData } from '@/models/apiTest/report';

  import { getIndicators } from '../../utils';

  const { t } = useI18n();
  const props = defineProps<{
    options: Record<string, any>;
    legendData: LegendData[];
    requestTotal: number;
    size?: string;
    gap?: string;
  }>();
</script>

<style scoped lang="less">
  .chart-legend {
    width: 100%;
    height: 100%;
    .chart-legend-item {
      width: 100%;
      @apply flex items-center justify-between;
    }
    .chart-flag {
      .count {
        color: var(--color-text-1);
        @apply flex-1;
      }
    }
  }
  .charts {
    z-index: 99;
    margin: auto;
    width: 66%;
    height: 66%;
    border-radius: 50%;
    @apply absolute bottom-0 left-0 right-0 top-0 flex flex-col items-center justify-center;
  }
</style>
