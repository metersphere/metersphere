<template>
  <ShareCom :detail-info="detail" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';

  import ShareCom from './component/scenarioCom.vue';

  import { getShareReportInfo, reportScenarioDetail } from '@/api/modules/api-test/report';
  import { NOT_FOUND_RESOURCE } from '@/router/constants';

  import type { ReportDetail } from '@/models/apiTest/report';

  const detail = ref<ReportDetail>();
  const router = useRouter();
  const route = useRoute();

  async function getDetail() {
    try {
      const res = await getShareReportInfo(route.query.shareId as string);
      if (res.deleted) {
        router.push({
          name: NOT_FOUND_RESOURCE,
          query: {
            type: 'DELETE',
          },
        });
      }

      if (res.expired) {
        router.push({
          name: NOT_FOUND_RESOURCE,
          query: {
            type: 'EXPIRED',
          },
        });
        return;
      }

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
