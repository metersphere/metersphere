<template>
  <configDetail :is-group="isGroup" :detail-info="detail" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';

  import configDetail from './component/config.vue';

  import { getPlanPassRate } from '@/api/modules/test-plan/testPlan';

  import type { detailCountKey, PlanReportDetail } from '@/models/testPlan/testPlanReport';

  const route = useRoute();

  const detail = ref<PlanReportDetail>({
    id: '',
    name: '',
    startTime: 0,
    createTime: 0, // 报告执行开始时间
    endTime: 0,
    summary: '',
    passThreshold: 100, // 通过阈值
    passRate: 100, // 通过率
    executeRate: 100, // 执行完成率
    bugCount: 10,
    caseTotal: 0,
    functionalTotal: 0,
    apiCaseTotal: 0,
    apiScenarioTotal: 0,
    executeCount: {
      success: 0,
      error: 0,
      fakeError: 0,
      block: 0,
      pending: 0,
    },
    functionalCount: {
      success: 0,
      error: 0,
      fakeError: 0,
      block: 0,
      pending: 0,
    },
    apiCaseCount: {
      success: 0,
      error: 0,
      fakeError: 0,
      block: 0,
      pending: 0,
    },
    apiScenarioCount: {
      success: 0,
      error: 0,
      fakeError: 0,
      block: 0,
      pending: 0,
    },
    planCount: 10,
    passCountOfPlan: 0,
    failCountOfPlan: 0,
    functionalBugCount: 0,
    apiBugCount: 0,
    scenarioBugCount: 0,
    testPlanName: '',
    defaultLayout: false,
  });

  const isGroup = computed(() => route.query.type === 'GROUP');

  async function getStatistics() {
    detail.value.caseTotal = 0;
    try {
      const selectedPlanIds = [route.query.id];
      const result = await getPlanPassRate(selectedPlanIds as string[]);
      const [countDetail] = result;

      const { apiCaseCount, apiScenarioCount, functionalCaseCount } = countDetail;

      const totalCase: { key: detailCountKey; count: number }[] = [
        { key: 'apiCaseCount', count: apiCaseCount },
        { key: 'apiScenarioCount', count: apiScenarioCount },
        { key: 'functionalCount', count: functionalCaseCount },
      ];

      totalCase.forEach((item: { key: detailCountKey; count: number }) => {
        if (item.count > 0) {
          detail.value.caseTotal += 10;
          detail.value.executeCount.success += 10;
          detail.value[item.key].success = 10;
        }
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  onBeforeMount(() => {
    getStatistics();
  });
</script>

<style scoped lang="less"></style>
