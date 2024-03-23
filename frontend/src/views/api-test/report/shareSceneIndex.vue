<template>
  <ShareCom :detail-info="detail" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';

  import ShareCom from './component/scenarioCom.vue';

  import { getShareReportInfo, reportScenarioDetail } from '@/api/modules/api-test/report';

  import type { ReportDetail } from '@/models/apiTest/report';

  const detail = ref<ReportDetail>();

  const route = useRoute();

  async function getDetail() {
    try {
      const res = await getShareReportInfo(route.query.shareId as string);
      detail.value = await reportScenarioDetail(res.reportId, route.query.shareId as string);
    } catch (error) {
      console.log(error);
    }
  }

  watchEffect(() => {
    if (route.query.shareId) {
      getDetail();
    }
  });
</script>

<style scoped></style>
