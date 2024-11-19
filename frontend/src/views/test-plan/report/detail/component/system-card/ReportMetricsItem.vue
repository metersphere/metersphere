<template>
  <div class="report-analysis-item">
    <div class="report-analysis-item-icon">
      <svg-icon class="mr-2" width="24px" height="24px" :name="props.itemInfo.icon" />
      <span>{{ props.itemInfo.name }}</span>
      <a-tooltip :content="props.itemInfo.tip" position="top">
        <MsIcon
          v-if="props.itemInfo.tip?.length"
          type="icon-icon-maybe_outlined"
          class="ml-[8px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
        />
      </a-tooltip>
      <MsTag
        v-if="props.itemInfo.runMode?.length"
        class="ml-[8px]"
        size="small"
        theme="outline"
        :self-style="{
          border: `1px solid rgb(var(--link-6))`,
          color: `rgb(var(--link-6))`,
          padding: '0 4px',
        }"
      >
        {{ props.itemInfo.runMode }}
      </MsTag>
    </div>
    <div>
      <span class="report-analysis-item-number">{{ props.itemInfo.value }}</span>
      <span class="report-analysis-item-unit">({{ props.itemInfo.unit }})</span>
    </div>
  </div>
</template>

<script setup lang="ts">
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import type { ReportMetricsItemModel } from '@/models/testPlan/testPlanReport';

  const props = defineProps<{
    itemInfo: ReportMetricsItemModel;
  }>();
</script>

<style scoped lang="less">
  .report-analysis-item {
    padding: 4px 8px;
    border-radius: 4px;
    background-color: var(--color-text-n9);
    @apply flex items-center justify-between;
    &:not(:last-child) {
      margin-bottom: 12px;
    }
    .report-analysis-item-icon {
      @apply flex items-center;
    }
    .report-analysis-item-number {
      font-size: 16px;
      @apply font-medium;
    }
    .report-analysis-item-unit {
      font-size: 12px;
      color: var(--color-text-4);
      @apply ml-1 font-medium;
    }
  }
</style>
