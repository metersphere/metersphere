<template>
  <a-popover
    v-model:popup-visible="visible"
    trigger="hover"
    position="bottom"
    :disabled="record.caseTotal === 0 || record.status === ExecuteStatusEnum.PENDING"
    @popup-visible-change="($event) => handleExecuteRatePopVisibleChange($event)"
  >
    <div :class="props.class">{{ record.executeRate || '0.00' }}%</div>
    <template #content>
      <a-spin :loading="record.loading" class="flex w-[130px] flex-col gap-[8px]">
        <div class="ms-taskCenter-execute-rate-item">
          <div class="ms-taskCenter-execute-rate-item-label">
            {{ t('ms.taskCenter.executeFinishedRate') }}
          </div>
          <div class="ms-taskCenter-execute-rate-item-value">
            {{ `${record.executeRate}%` }}
          </div>
        </div>
        <div class="ms-taskCenter-execute-rate-item">
          <div class="ms-taskCenter-execute-rate-item-label">
            <div
              :class="`ms-taskCenter-execute-rate-item-label-point bg-[${executeFinishedRateMap.UN_EXECUTE.color}]`"
            ></div>
            {{ t(executeFinishedRateMap.UN_EXECUTE.label) }}
          </div>
          <div class="ms-taskCenter-execute-rate-item-value">{{ record.pendingCount }}</div>
        </div>
        <div class="ms-taskCenter-execute-rate-item">
          <div class="ms-taskCenter-execute-rate-item-label">
            <div
              :class="`ms-taskCenter-execute-rate-item-label-point bg-[${executeFinishedRateMap.SUCCESS.color}]`"
            ></div>
            {{ t(executeFinishedRateMap.SUCCESS.label) }}
          </div>
          <div class="ms-taskCenter-execute-rate-item-value">{{ record.successCount }}</div>
        </div>
        <div class="ms-taskCenter-execute-rate-item">
          <div class="ms-taskCenter-execute-rate-item-label">
            <div
              :class="`ms-taskCenter-execute-rate-item-label-point bg-[${executeFinishedRateMap.FAKE_ERROR.color}]`"
            ></div>
            {{ t(executeFinishedRateMap.FAKE_ERROR.label) }}
          </div>
          <div class="ms-taskCenter-execute-rate-item-value">{{ record.fakeErrorCount }}</div>
        </div>
        <div class="ms-taskCenter-execute-rate-item">
          <div class="ms-taskCenter-execute-rate-item-label">
            <div
              :class="`ms-taskCenter-execute-rate-item-label-point bg-[${executeFinishedRateMap.ERROR.color}]`"
            ></div>
            {{ t(executeFinishedRateMap.ERROR.label) }}
          </div>
          <div class="ms-taskCenter-execute-rate-item-value">{{ record.errorCount }}</div>
        </div>
      </a-spin>
    </template>
  </a-popover>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  import { TaskCenterStatisticsItem, TaskCenterTaskItem } from '@/models/taskCenter';
  import { PlanExecuteResultExecuteCaseCount } from '@/models/testPlan/testPlan';
  import { ExecuteStatusEnum } from '@/enums/taskCenter';

  import { executeFinishedRateMap } from './config';

  const props = defineProps<{
    class?: string;
    executeCaseCount?: PlanExecuteResultExecuteCaseCount;
    executeTaskStatisticsRequest?: (ids: string[]) => Promise<TaskCenterStatisticsItem[]>;
  }>();

  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', {
    required: true,
  });
  const record = defineModel<TaskCenterTaskItem>('record', {
    required: true,
  });

  async function handleExecuteRatePopVisibleChange(_visible: boolean) {
    if (_visible && props.executeTaskStatisticsRequest) {
      try {
        record.value.loading = true;
        const res = await props.executeTaskStatisticsRequest([record.value.id]);
        record.value.executeRate = res[0].executeRate;
        record.value.pendingCount = res[0].pendingCount;
        record.value.successCount = res[0].successCount;
        record.value.fakeErrorCount = res[0].fakeErrorCount;
        record.value.errorCount = res[0].errorCount;
        record.value.caseTotal = res[0].caseTotal;
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      } finally {
        record.value.loading = false;
      }
    } else if (props.executeCaseCount) {
      record.value.pendingCount = props.executeCaseCount.pending;
      record.value.successCount = props.executeCaseCount.success;
      record.value.fakeErrorCount = props.executeCaseCount.fakeError;
      record.value.errorCount = props.executeCaseCount.error;
    }
  }
</script>

<style lang="less">
  .ms-taskCenter-execute-rate-item {
    @apply flex items-center justify-between;
    .ms-taskCenter-execute-rate-item-label {
      @apply flex items-center;

      gap: 4px;
      color: var(--color-text-4);
      .ms-taskCenter-execute-rate-item-label-point {
        width: 6px;
        height: 6px;
        border-radius: 100%;
      }
    }
    .ms-taskCenter-execute-rate-item-value {
      font-weight: 500;
      color: var(--color-text-1);
    }
  }
</style>
