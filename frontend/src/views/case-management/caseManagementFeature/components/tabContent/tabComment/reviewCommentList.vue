<template>
  <div class="flex flex-1 flex-col overflow-hidden">
    <div class="ms-comment-list">
      <div v-for="item of props.reviewCommentList" :key="item.id" class="ms-comment-list-item">
        <MSAvatar :avatar="item.userLogo" />
        <div class="flex-1">
          <div class="flex items-center">
            <a-tooltip :content="item.userName" :mouse-enter-delay="300">
              <div class="comment-list-item-name one-line-text max-w-[300px] font-medium text-[var(--color-text-1)]">
                {{ item.userName }}
              </div>
            </a-tooltip>
            <a-divider
              v-if="props.showStepDetailTrigger && item.caseEditType == 'STEP' && item.showResult"
              direction="vertical"
              margin="8px"
            ></a-divider>
            <StepDetailTrigger
              v-if="props.showStepDetailTrigger && item.caseEditType == 'STEP' && item.showResult"
              :steps-text="item.stepsText"
            />
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
            <div v-if="item.status === 'SUCCESS'" class="flex items-center">
              <MsIcon type="icon-icon_succeed_filled" class="mr-[4px] text-[rgb(var(--success-6))]" />
              {{ t('common.success') }}
            </div>
            <div v-if="item.status === 'BLOCKED'" class="flex items-center">
              <MsIcon type="icon-icon_succeed_filled" class="mr-[4px] text-[var(--color-fill-p-3)]" />
              {{ t('common.block') }}
            </div>
            <div v-if="item.status === 'ERROR'" class="flex items-center">
              <MsIcon type="icon-icon_succeed_filled" class="mr-[4px] text-[rgb(var(--danger-6))]" />
              {{ t('common.fail') }}
            </div>
          </div>
          <div class="markdown-body mt-[4px]" v-html="item.contentText"></div>
          <div class="mt-[8px] flex text-[12px] leading-[16px] text-[var(--color-text-4)]">
            {{ dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss') }}
            <div v-if="props.activeComment === 'reviewComment'">
              <a-tooltip :content="item.reviewName" :mouse-enter-delay="300">
                <span v-if="item.deleted" class="one-line-text ml-[16px] max-w-[300px] break-words break-all">
                  {{ characterLimit(item.reviewName) }}
                </span>

                <span
                  v-if="!item.deleted && !props.notShowReviewName"
                  class="one-line-text ml-[16px] max-w-[300px] cursor-pointer break-words break-all text-[rgb(var(--primary-5))]"
                  @click="review(item)"
                >
                  {{ characterLimit(item.reviewName) }}
                </span>
              </a-tooltip>
            </div>
            <div v-if="props.activeComment === 'executiveComment'">
              <a-tooltip :content="item.testPlanName" :mouse-enter-delay="300">
                <span v-if="item.deleted" class="one-line-text ml-[16px] max-w-[300px] break-words break-all">
                  {{ characterLimit(item.testPlanName) }}
                </span>

                <span
                  v-if="!item.deleted && !props.notShowReviewName"
                  class="one-line-text ml-[16px] max-w-[300px] cursor-pointer break-words break-all text-[rgb(var(--primary-5))]"
                  @click="toPlan(item)"
                >
                  {{ characterLimit(item.testPlanName) }}
                </span>
              </a-tooltip>
            </div>
          </div>
        </div>
      </div>
      <MsEmpty v-if="reviewCommentList.length === 0" />
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useRoute, useRouter } from 'vue-router';
  import dayjs from 'dayjs';

  import MSAvatar from '@/components/pure/ms-avatar/index.vue';
  import MsEmpty from '@/components/pure/ms-empty/index.vue';
  import { CommentItem } from '@/components/business/ms-comment/types';
  import StepDetailTrigger from '@/views/case-management/caseManagementFeature/components/stepDetailTrigger.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { characterLimit } from '@/utils';

  import { CaseManagementRouteEnum, TestPlanRouteEnum } from '@/enums/routeEnum';

  const props = defineProps<{
    reviewCommentList: any[];
    activeComment: string;
    notShowReviewName?: boolean;
    showStepDetailTrigger?: boolean;
  }>();

  const router = useRouter();
  const route = useRoute();
  const { t } = useI18n();

  // 去用例评审页面
  function review(record: CommentItem) {
    router.push({
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL_CASE_DETAIL,
      query: {
        ...route.query,
        caseId: record.caseId,
        id: record.reviewId,
      },
      state: {
        params: JSON.stringify(record.moduleName),
      },
    });
  }

  // 去测试计划页面
  function toPlan(record: CommentItem) {
    router.push({
      name: TestPlanRouteEnum.TEST_PLAN_INDEX_DETAIL,
      query: {
        ...route.query,
        id: record.testPlanId,
      },
      state: {
        params: JSON.stringify(record.moduleName),
      },
    });
  }
</script>

<style lang="less" scoped></style>
