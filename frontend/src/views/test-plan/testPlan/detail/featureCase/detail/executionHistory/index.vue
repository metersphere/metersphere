<template>
  <a-spin :loading="loading" class="w-full">
    <div :class="`${props.height || 'h-full'} execute-history-list`">
      <div v-for="item of props.executeList" :key="item.id" class="execute-history-list-item">
        <div class="flex items-center">
          <MsAvatar :avatar="item.userLogo" />
          <div class="ml-[8px] flex items-center">
            <a-tooltip :content="item.userName" :mouse-enter-delay="300">
              <div class="one-line-text max-w-[300px] font-medium text-[var(--color-text-1)]">{{ item.userName }}</div>
            </a-tooltip>
            <a-divider
              v-if="props.showStepDetailTrigger && item.caseEditType == 'STEP' && item.showResult"
              direction="vertical"
              margin="8px"
            ></a-divider>
            <StepDetailTrigger
              v-if="props.showStepDetailTrigger && item.caseEditType == 'STEP' && item.showResult"
              :steps-text="item.stepsExecResult"
            />
            <a-divider direction="vertical" margin="8px"></a-divider>
            <div v-if="item.status === 'SUCCESS'" class="flex items-center">
              <MsIcon type="icon-icon_succeed_filled" class="mr-[4px] text-[rgb(var(--success-6))]" />
              {{ t('common.success') }}
            </div>
            <div v-if="item.status === 'BLOCKED'" class="flex items-center">
              <MsIcon type="icon-icon_block_filled" class="mr-[4px] text-[var(--color-fill-p-3)]" />
              {{ t('common.block') }}
            </div>
            <div v-if="item.status === 'ERROR'" class="flex items-center">
              <MsIcon type="icon-icon_close_filled" class="mr-[4px] text-[rgb(var(--danger-6))]" />
              {{ t('common.fail') }}
            </div>
          </div>
        </div>
        <div class="markdown-body" style="margin-left: 48px" v-html="item.contentText"></div>
        <div v-if="props.showStepResult" class="ml-[48px] mt-[8px]">
          <StepDetail
            :step-list="getStepData(item.stepsExecResult)"
            is-disabled
            is-preview
            is-test-plan
            :is-disabled-test-plan="false"
          />
        </div>
        <div class="ml-[48px] mt-[8px] flex text-[var(--color-text-4)]">
          {{ dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss') }}
          <div>
            <a-tooltip :content="item.userName" :mouse-enter-delay="300" :disabled="!item.userName">
              <span v-if="item.deleted" class="one-line-text ml-[16px] max-w-[300px] break-words break-all">
                {{ characterLimit(item.userName) }}
              </span>
            </a-tooltip>
          </div>
        </div>
      </div>
      <MsEmpty v-if="props.executeList.length === 0" />
    </div>
  </a-spin>
</template>

<script setup lang="ts">
  import dayjs from 'dayjs';

  import MsAvatar from '@/components/pure/ms-avatar/index.vue';
  import MsEmpty from '@/components/pure/ms-empty/index.vue';
  import StepDetail from '@/views/case-management/caseManagementFeature/components/addStep.vue';
  import StepDetailTrigger from '@/views/case-management/caseManagementFeature/components/stepDetailTrigger.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { characterLimit } from '@/utils';

  import type { ExecuteHistoryItem } from '@/models/testPlan/testPlan';

  const { t } = useI18n();

  const props = defineProps<{
    executeList: ExecuteHistoryItem[];
    loading: boolean;
    showStepResult?: boolean; // 是否展示执行步骤
    height?: string;
    showStepDetailTrigger?: boolean; // 是否展示步骤详情
  }>();

  function getStepData(steps: string) {
    if (steps) {
      return JSON.parse(steps).map((item: any) => {
        return {
          id: item.id,
          step: item.desc,
          expected: item.result,
          actualResult: item.actualResult,
          executeResult: item.executeResult,
        };
      });
    }
    return [];
  }
</script>

<style scoped lang="less">
  .execute-history-list {
    @apply overflow-auto;
    .ms-scroll-bar();
    .execute-history-list-item {
      &:not(:last-child) {
        margin-bottom: 16px;
      }
    }
  }
</style>
