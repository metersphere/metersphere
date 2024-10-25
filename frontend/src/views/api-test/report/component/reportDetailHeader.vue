<template>
  <div class="report-header flex items-center justify-between">
    <div class="flex items-center">
      <div v-if="route.query.shareId" class="flex items-center font-medium"
        >{{ t('report.name') }}
        <a-tooltip :content="props.detail.name" :mouse-enter-delay="300"
          ><div class="one-line-text max-w-[300px]">【{{ props.detail.name }}</div
          >】
        </a-tooltip>
        <a-divider direction="vertical" :margin="4" class="!mx-2"></a-divider
      ></div>

      <a-popover position="left" content-class="response-popover-content">
        <div class="one-line-text max-w-[150px]">
          {{ props.detail.environmentName || t('report.detail.api.defaultEnv') }}</div
        >
        <a-divider direction="vertical" :margin="4" class="!mx-2"></a-divider>
        <template #content>
          <div class="max-w-[400px] items-center gap-[8px] text-[14px]">
            <div class="flex-shrink-0 text-[var(--color-text-4)]">{{ t('report.detail.api.executeEnv') }}</div>
            <div>
              {{
                props.detail.environmentName
                  ? props.detail.environmentName
                  : props.showType === 'CASE'
                  ? t('report.detail.api.caseSaveEnv')
                  : t('report.detail.api.scenarioSavedEnv')
              }}
            </div>
          </div>
        </template>
      </a-popover>
      <a-popover position="bottom" content-class="response-popover-content">
        <div class="one-line-text max-w-[150px]"> {{ props.detail.poolName || '-' }}</div>
        <a-divider direction="vertical" :margin="4" class="!mx-2"></a-divider>
        <template #content>
          <div class="max-w-[400px] items-center gap-[8px] text-[14px]">
            <div class="mb-[4px] text-[var(--color-text-4)]">{{ t('report.detail.api.resourcePool') }}</div>
            <div> {{ props.detail.poolName || '-' }}</div>
          </div>
        </template>
      </a-popover>

      <a-popover position="left" content-class="response-popover-content">
        <span v-if="!props.detail.integrated && props.showType === 'API' && props.detail.waitingTime">
          {{ props.detail.waitingTime ? formatDuration(props.detail.waitingTime).split('-')[0] : '-' }}
          <span>{{ props.detail.waitingTime ? formatDuration(props.detail.waitingTime).split('-')[1] : 'ms' }}</span>
          <a-divider direction="vertical" :margin="4" class="!mx-2"></a-divider>
        </span>
        <template #content>
          <div class="flex items-center gap-[8px] text-[14px]">
            <div class="text-[var(--color-text-4)]">{{ t('report.detail.api.globalWaitingTime') }}</div>
            {{ props.detail.waitingTime ? formatDuration(props.detail.waitingTime).split('-')[0] : '-' }}
            <span class="mx-1">{{
              props.detail.waitingTime ? formatDuration(props.detail.waitingTime).split('-')[1] : 'ms'
            }}</span>
          </div>
        </template>
      </a-popover>
      <a-popover position="bottom" content-class="response-popover-content">
        <div class="one-line-text max-w-[150px]"> {{ props.detail.creatUserName || '-' }}</div>
        <template #content>
          <div class="max-w-[400px] items-center gap-[8px] text-[14px]">
            <div class="text-[var(--color-text-4)]">{{ t('report.detail.api.reportCreator') }}</div>
            <div class="mt-1"> {{ props.detail.creatUserName || '-' }}</div>
          </div>
        </template>
      </a-popover>
    </div>
    <div>
      <span class="text-[var(--color-text-4)]">{{ t('report.detail.api.executionTime') }}</span>
      {{ props.detail.startTime ? dayjs(props.detail.startTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
      <span class="text-[var(--color-text-4)]">{{ t('report.detail.api.executionTimeTo') }}</span>
      {{ props.detail.endTime ? dayjs(props.detail.endTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import dayjs from 'dayjs';

  import { useI18n } from '@/hooks/useI18n';
  import { formatDuration } from '@/utils';

  import type { ReportDetail } from '@/models/apiTest/report';

  const { t } = useI18n();
  const route = useRoute();
  const props = defineProps<{
    detail: ReportDetail;
    showType: 'API' | 'CASE';
  }>();
</script>

<style scoped></style>
