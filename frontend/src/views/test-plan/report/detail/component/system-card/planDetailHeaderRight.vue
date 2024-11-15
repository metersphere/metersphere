<template>
  <div class="flex items-center">
    <a-popover position="bottom" content-class="response-popover-content">
      <div>
        <span class="text-[var(--color-text-4)]">{{ t('report.detail.api.executionTime') }}</span>
        {{ props.detail.startTime ? dayjs(props.detail.startTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
        <span class="text-[var(--color-text-4)]">{{ t('report.detail.api.executionTimeTo') }}</span>
        {{ props.detail.endTime ? dayjs(props.detail.endTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
      </div>
      <template #content>
        <div class="max-w-[400px] items-center gap-[8px] text-[14px]">
          <div class="flex-shrink-0 text-[var(--color-text-4)]">{{ t('report.detail.api.createTime') }}</div>
          <div class="mt-2">
            {{ dayjs(props.detail.createTime).format('YYYY-MM-DD HH:mm:ss') }}
          </div>
        </div>
      </template>
    </a-popover>
    <MsButton
      v-if="hasAnyPermission(['PROJECT_TEST_PLAN_REPORT:READ+SHARE']) && !props.shareId"
      type="icon"
      status="secondary"
      class="!mr-0 ml-4 !rounded-[var(--border-radius-small)]"
      :loading="shareLoading"
      @click="shareHandler"
    >
      <MsIcon type="icon-icon_link-copy_outlined" class="mr-2 font-[16px]" />
      {{ t('common.share') }}
    </MsButton>
    <MsButton
      v-permission="['PROJECT_TEST_PLAN_REPORT:READ+EXPORT']"
      type="icon"
      status="secondary"
      class="ml-4 !rounded-[var(--border-radius-small)]"
      @click="exportPdf"
    >
      <MsIcon type="icon-icon_into-item_outlined" class="mr-2 font-[16px]" />
      {{ t('report.detail.exportPdf') }}
    </MsButton>
  </div>
</template>

<script setup lang="ts">
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';

  import { planReportShare } from '@/api/modules/test-plan/report';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import useAppStore from '@/store/modules/app';
  import { hasAnyPermission } from '@/utils/permission';

  import type { PlanReportDetail } from '@/models/testPlan/testPlanReport';
  import { FullPageEnum, RouteEnum } from '@/enums/routeEnum';

  const props = defineProps<{
    detail: PlanReportDetail;
    shareId?: string;
    isGroup?: boolean;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();
  const { copy, isSupported } = useClipboard({ legacy: true });
  const { openNewPage } = useOpenNewPage();

  const shareLink = ref<string>('');
  const shareLoading = ref<boolean>(false);
  async function shareHandler() {
    try {
      const res = await planReportShare({
        reportId: props.detail.id,
        projectId: appStore.currentProjectId,
      });
      const { origin } = window.location;
      shareLink.value = `${origin}/#/${RouteEnum.SHARE}/${RouteEnum.SHARE_REPORT_TEST_PLAN}${res.shareUrl}&type=${
        props.isGroup ? 'GROUP' : 'TEST_PLAN'
      }`;
      if (isSupported) {
        copy(shareLink.value);
        Message.info(t('bugManagement.detail.shareTip'));
      } else {
        Message.error(t('common.copyNotSupport'));
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function exportPdf() {
    openNewPage(FullPageEnum.FULL_PAGE_TEST_PLAN_EXPORT_PDF, {
      id: props.detail.id,
      type: props.isGroup ? 'GROUP' : 'TEST_PLAN',
    });
  }
</script>
