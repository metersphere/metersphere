<template>
  <a-spin class="pass-rate-content" :loading="props.loading">
    <div class="relative flex items-center justify-center">
      <a-tooltip
        v-if="props.tooltipText"
        :mouse-enter-delay="500"
        :content="t(props.tooltipText || '')"
        position="bottom"
      >
        <div class="tooltip-rate h-[50px] w-[50px]"></div>
      </a-tooltip>
      <MsChart height="92px" width="92px" :options="props.options" />
    </div>
    <div class="pass-rate-title flex-1">
      <div v-for="(item, index) of props.valueList" :key="item.label" class="flex-1">
        <slot :ele="item" :index="index">
          <div class="one-line-text mb-[8px] text-[var(--color-text-4)]">{{ item.label }}</div>
          <div class="pass-rate-count text-[rgb(var(--primary-4))]">
            <div @click="goNavigation(item)">
              {{ hasPermission ? addCommasToNumber(item.value as number) : '-' }}
            </div>
          </div>
        </slot>
      </div>
    </div>
  </a-spin>
</template>

<script setup lang="ts">
  import MsChart from '@/components/pure/chart/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import { addCommasToNumber } from '@/utils';

  const { openNewPage } = useOpenNewPage();

  const { t } = useI18n();

  const props = defineProps<{
    options: Record<string, any>;
    projectId: string;
    tooltipText?: string;
    hasPermission: boolean;
    loading?: boolean;
    valueList: {
      label: string;
      value: number | string;
      status?: string;
      route?: string;
      archivedPassed?: number;
    }[];
  }>();

  function goNavigation(item: { label: string; value: number | string; status?: string; route?: string }) {
    openNewPage(item.route, {
      pId: props.projectId,
      home: item.status,
    });
  }
</script>

<style scoped lang="less">
  .pass-rate-content {
    padding: 0 8px 0 4px;
    width: 100%;
    height: 92px;
    border-radius: 6px;
    background: var(--color-text-n9);
    @apply flex items-center justify-center;
    .pass-rate-title {
      @apply flex items-center gap-2;
      .pass-rate-count {
        font-size: 20px;
        gap: 8px;
        @apply flex cursor-pointer  items-center justify-start font-medium;
      }
    }
  }
  .tooltip-rate {
    position: absolute;
    z-index: 9;
    border-radius: 50%;
  }
</style>
