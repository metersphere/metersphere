<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :title="detail.name"
    :width="1200"
    :footer="false"
    class="report-drawer"
    unmount-on-close
    no-content-padding
    show-full-screen
  >
    <template #tbutton>
      <PlanDetailHeaderRight share-id="" :detail="detail" />
    </template>
    <ViewReport v-model:card-list="cardItemList" :detail-info="detail" :is-group="false" is-preview is-drawer />
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { cloneDeep } from 'lodash-es';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import PlanDetailHeaderRight from '@/views/test-plan/report/detail/component/system-card/planDetailHeaderRight.vue';
  import ViewReport from '@/views/test-plan/report/detail/component/viewReport.vue';

  import { getReportDetail } from '@/api/modules/test-plan/report';
  import { defaultReportDetail } from '@/config/testPlan';

  import type { configItem, PlanReportDetail } from '@/models/testPlan/testPlanReport';

  const props = defineProps<{
    reportId: string;
  }>();

  const innerVisible = defineModel<boolean>('visible', {
    required: true,
  });

  const route = useRoute();

  const cardItemList = ref<configItem[]>([]);

  const shareId = ref<string>(route.query.shareId as string);

  const detail = ref<PlanReportDetail>(cloneDeep(defaultReportDetail));
  async function getDetail() {
    try {
      detail.value = await getReportDetail(props.reportId, shareId.value);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  watch(
    () => innerVisible.value,
    async (val) => {
      if (val) {
        await getDetail();
      }
    }
  );
</script>

<style lang="less">
  .report-drawer {
    .ms-drawer-body {
      padding: 16px;
      background: var(--color-text-n9);
    }
    .right-operation-button-icon {
      margin-left: 4px;
    }
  }
</style>
