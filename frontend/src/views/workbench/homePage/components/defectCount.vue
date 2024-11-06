<template>
  <CountOverview :value-list="valueList" :legend-data="legendData" :title="title" />
</template>

<script setup lang="ts">
  /** *
   * @desc 待我处理的缺陷&缺陷数量
   */
  import { ref } from 'vue';

  import CountOverview from './countOverview.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { LegendData } from '@/models/apiTest/report';
  import { WorkCardEnum } from '@/enums/workbenchEnum';

  const { t } = useI18n();
  const props = defineProps<{
    type: WorkCardEnum;
  }>();

  const title = computed(() => {
    switch (props.type) {
      case WorkCardEnum.HANDLE_BUG_BY_ME:
        return t('workbench.homePage.pendingDefect');
      case WorkCardEnum.CREATE_BUG_BY_ME:
        return t('workbench.homePage.createdBugByMe');
      case WorkCardEnum.PLAN_LEGACY_BUG:
        return t('workbench.homePage.remainingBugOfPlan');
      default:
        return t('workbench.homePage.bugCount');
    }
  });

  const valueList = ref<
    {
      label: string;
      value: number;
    }[]
  >([
    {
      label: t('workbench.homePage.defectTotal'),
      value: 10000,
    },
    {
      label: t('workbench.homePage.legacyDefectsNumber'),
      value: 2000,
    },
  ]);

  const legendData = ref<LegendData[]>([
    {
      label: t('common.notStarted'),
      value: 'notStarted',
      rote: 30,
      count: 3,
      class: 'bg-[rgb(var(--primary-4))] ml-[24px]',
    },
    {
      label: t('common.inProgress'),
      value: 'inProgress',
      rote: 30,
      count: 3,
      class: 'bg-[rgb(var(--link-6))] ml-[24px]',
    },
    {
      label: t('common.completed'),
      value: 'completed',
      rote: 30,
      count: 3,
      class: 'bg-[rgb(var(--success-6))] ml-[24px]',
    },
    {
      label: t('common.archived'),
      value: 'archived',
      rote: 30,
      count: 3,
      class: 'bg-[var(--color-text-input-border)] ml-[24px]',
    },
  ]);
</script>

<style scoped lang="less"></style>
