<template>
  <a-trigger
    v-model:popup-visible="statusVisible"
    content-class="review-result-trigger-content"
    trigger="click"
    position="br"
    :popup-translate="[0, 4]"
  >
    <div
      :class="`overall-review-result flex cursor-pointer items-center rounded p-[4px] hover:bg-[var(--color-text-n9)] 
      ${statusVisible ? 'bg-[var(--color-text-n9)]' : ''} `"
    >
      <ReviewResult :status="reviewHistoryStatus" :class="[`text-[${props.size}px]`]" :icon-size="props.size" />
      <MsIcon type="icon-icon_expand-down_filled" :size="props.size" class="ml-[4px] text-[var(--color-text-4)]" />
    </div>
    <template #content>
      <div v-for="item in reviewUserStatusList" :key="item.id" class="my-[4px] flex justify-between">
        <div class="one-line-text max-w-[72px]">
          {{ item.id }}
        </div>
        <ReviewResult
          :status="item.name as ReviewResultStatus"
          is-part
          :class="[`text-[${props.size}px]`]"
          :icon-size="props.size"
        />
      </div>
      <MsEmpty v-if="!reviewUserStatusList.length" />
    </template>
  </a-trigger>
</template>

<script setup lang="ts">
  import MsEmpty from '@/components/pure/ms-empty/index.vue';
  import ReviewResult from '@/views/case-management/caseReview/components/reviewResult.vue';

  import { getReviewerAndStatus } from '@/api/modules/case-management/caseReview';
  import { OptionItem } from '@/api/modules/message/index';

  import { ReviewResult as ReviewResultStatus } from '@/models/caseManagement/caseReview';

  const props = defineProps<{
    size?: number;
  }>();

  const statusVisible = ref(false);
  const reviewHistoryStatus = ref<ReviewResultStatus>();
  const reviewUserStatusList = ref<OptionItem[]>([]); // 每个评审人最后一次评审结果

  async function initReviewerAndStatus(reviewId: string, caseId: string) {
    try {
      const res = await getReviewerAndStatus(reviewId, caseId);
      reviewUserStatusList.value = res.reviewerStatus;
      reviewHistoryStatus.value = res.status as ReviewResultStatus;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  defineExpose({
    initReviewerAndStatus,
  });
</script>

<style lang="less">
  .review-result-trigger-content {
    padding: 6px;
    width: 150px;
    max-height: 192px;
    border-radius: var(--border-radius-medium);
    background-color: var(--color-text-fff);
    box-shadow: 0 -1px 4px rgb(2 2 2 / 10%);
    @apply overflow-y-auto overflow-x-hidden;
    .ms-scroll-bar();
  }
</style>
