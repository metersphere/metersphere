<template>
  <div class="flex items-center">
    <a-popover position="bottom" content-class="response-popover-content">
      <div>
        <span class="text-[var(--color-text-4)]">{{ t('report.detail.api.executionTime') }}</span>
        {{ props.detail.executeTime ? dayjs(props.detail.executeTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
        <span class="text-[var(--color-text-4)]">{{ t('report.detail.api.executionTimeTo') }}</span>
        {{ props.detail.endTime ? dayjs(props.detail.endTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
      </div>
      <template #content>
        <div class="max-w-[400px] items-center gap-[8px] text-[14px]">
          <div class="flex-shrink-0 text-[var(--color-text-4)]">{{ t('report.detail.api.executionTime') }}</div>
          <div class="mt-2">
            {{ dayjs(props.detail.executeTime).format('YYYY-MM-DD HH:mm:ss') }}
          </div>
        </div>
      </template>
    </a-popover>
    <MsButton
      v-if="hasAnyPermission(['PROJECT_TEST_PLAN_REPORT:READ+SHARE']) && !props.shareId"
      type="icon"
      status="secondary"
      class="ml-4 !rounded-[var(--border-radius-small)]"
      :loading="shareLoading"
      @click="shareHandler"
    >
      <MsIcon type="icon-icon_share1" class="mr-2 font-[16px]" />
      {{ t('common.share') }}
    </MsButton>
  </div>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';

  import { planReportShare } from '@/api/modules/test-plan/report';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { hasAnyPermission } from '@/utils/permission';

  import type { PlanReportDetail } from '@/models/testPlan/testPlanReport';
  import { RouteEnum } from '@/enums/routeEnum';

  const props = defineProps<{
    detail: PlanReportDetail;
    shareId?: string;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  const shareLink = ref<string>('');
  const shareLoading = ref<boolean>(false);
  async function shareHandler() {
    try {
      const res = await planReportShare({
        reportId: props.detail.id,
        projectId: appStore.currentProjectId,
      });
      const { origin } = window.location;
      shareLink.value = `${origin}/#/${RouteEnum.SHARE}/${RouteEnum.SHARE_REPORT_TEST_PLAN}${res.shareUrl}`;
      if (navigator.clipboard) {
        navigator.clipboard.writeText(shareLink.value).then(
          () => {
            Message.info(t('bugManagement.detail.shareTip'));
          },
          (e) => {
            Message.error(e);
          }
        );
      } else {
        const input = document.createElement('input');
        input.value = shareLink.value;
        document.body.appendChild(input);
        input.select();
        document.execCommand('copy');
        document.body.removeChild(input);
        Message.info(t('bugManagement.detail.shareTip'));
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
</script>
