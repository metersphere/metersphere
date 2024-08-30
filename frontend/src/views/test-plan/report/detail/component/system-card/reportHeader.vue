<template>
  <MsCard v-if="!props.isDrawer" class="mb-[16px]" hide-back hide-footer auto-height no-content-padding hide-divider>
    <template #headerLeft>
      <div class="flex items-center font-medium">
        <a-tooltip :content="detail.name" :mouse-enter-delay="300"
          ><div class="one-line-text max-w-[300px]">{{ detail.name }}</div>
        </a-tooltip>
      </div>
    </template>
    <template #headerRight>
      <PlanDetailHeaderRight :is-group="props.isGroup" :share-id="shareId" :detail="detail" />
    </template>
  </MsCard>
</template>

<script setup lang="ts">
  import MsCard from '@/components/pure/ms-card/index.vue';
  import PlanDetailHeaderRight from '@/views/test-plan/report/detail/component/system-card/planDetailHeaderRight.vue';

  import type { PlanReportDetail } from '@/models/testPlan/testPlanReport';

  const props = defineProps<{
    detail: PlanReportDetail;
    shareId?: string;
    isDrawer?: boolean;
    isGroup: boolean;
  }>();
</script>

<style scoped></style>
