<template>
  <!-- TODO: 待联调 -->
  <div class="review-history-list">
    <div v-for="item of executeHistoryList" :key="item.id" class="review-history-list-item">
      <div class="flex items-center">
        <MsAvatar :avatar="item.userLogo" />
        <div class="ml-[8px] flex items-center">
          <a-tooltip :content="item.userName" :mouse-enter-delay="300">
            <div class="one-line-text max-w-[300px] font-medium text-[var(--color-text-1)]">{{ item.userName }}</div>
          </a-tooltip>
          <a-divider direction="vertical" margin="8px"></a-divider>
          <div v-if="item.status === 'PASS'" class="flex items-center">
            <MsIcon type="icon-icon_succeed_filled" class="mr-[4px] text-[rgb(var(--success-6))]" />
            {{ t('caseManagement.caseReview.pass') }}
          </div>
          <div v-else-if="item.status === 'UN_PASS'" class="flex items-center">
            <MsIcon type="icon-icon_close_filled" class="mr-[4px] text-[rgb(var(--danger-6))]" />
            {{ t('caseManagement.caseReview.fail') }}
          </div>
          <div v-else-if="item.status === 'UNDER_REVIEWED'" class="flex items-center">
            <MsIcon type="icon-icon_warning_filled" class="mr-[4px] text-[rgb(var(--warning-6))]" />
            {{ t('caseManagement.caseReview.suggestion') }}
          </div>
          <div v-else-if="item.status === 'RE_REVIEWED'" class="flex items-center">
            <MsIcon type="icon-icon_resubmit_filled" class="mr-[4px] text-[rgb(var(--warning-6))]" />
            {{ t('caseManagement.caseReview.reReview') }}
          </div>
          <div v-if="item.status === 'PASSED'" class="flex items-center">
            <MsIcon type="icon-icon_succeed_filled" class="mr-[4px] text-[rgb(var(--success-6))]" />
            {{ t('caseManagement.featureCase.execute.success') }}
          </div>
          <div v-if="item.status === 'BLOCKED'" class="flex items-center">
            <MsIcon type="icon-icon_succeed_filled" class="mr-[4px] text-[rgb(var(--success-6))]" />
            {{ t('caseManagement.featureCase.execute.blocked') }}
          </div>
          <div v-if="item.status === 'FAILED'" class="flex items-center">
            <MsIcon type="icon-icon_succeed_filled" class="mr-[4px] text-[rgb(var(--success-6))]" />
            {{ t('caseManagement.featureCase.execute.failed') }}
          </div>
        </div>
      </div>
      <div class="markdown-body" style="margin-left: 48px" v-html="item.contentText"></div>
      <div class="ml-[48px] mt-[8px] flex text-[var(--color-text-4)]">
        {{ dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        <div>
          <a-tooltip :content="item.reviewName" :mouse-enter-delay="300">
            <span v-if="item.deleted" class="one-line-text ml-[16px] max-w-[300px] break-words break-all">
              {{ characterLimit(item.reviewName) }}
            </span>

            <span
              v-else
              class="one-line-text ml-[16px] max-w-[300px] cursor-pointer break-words break-all text-[rgb(var(--primary-5))]"
            >
              {{ characterLimit(item.reviewName) }}
            </span>
          </a-tooltip>
        </div>
      </div>
    </div>
    <MsEmpty v-if="executeHistoryList.length === 0" />
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import dayjs from 'dayjs';

  import MsAvatar from '@/components/pure/ms-avatar/index.vue';
  import MsEmpty from '@/components/pure/ms-empty/index.vue';
  import { CommentItem, CommentParams } from '@/components/business/ms-comment/types';

  import { useI18n } from '@/hooks/useI18n';
  import { characterLimit } from '@/utils';

  const { t } = useI18n();
  const props = defineProps<{
    caseId: string;
  }>();

  const executeHistoryList = ref<CommentItem[]>([]);
</script>

<style scoped></style>
