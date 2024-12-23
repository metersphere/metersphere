<template>
  <MsDrawer v-model:visible="visible" :width="1200" :footer="false">
    <template #title>
      <div class="flex flex-1 items-center gap-[8px] overflow-hidden">
        <a-tag :color="executeResultMap[detail.result]?.color">
          {{ t(executeResultMap[detail.result]?.label || '-') }}
        </a-tag>
        <div class="one-line-text flex-1">{{ detail.name }}</div>
      </div>
      <div class="flex justify-end">
        <MsButton type="icon" status="secondary" class="!rounded-[var(--border-radius-small)]" @click="init">
          <MsIcon type="icon-icon_reset_outlined" class="mr-[8px]" size="14" />
          {{ t('common.refresh') }}
        </MsButton>
      </div>
    </template>
    <a-spin :loading="loading" class="block min-h-[200px]">
      <MsDescription :descriptions="detail.description" :column="3" :line-gap="8" one-line-value>
        <template #value="{ item }">
          <execStatus v-if="item.key === 'status'" :status="detail.status" size="small" />
          <a-tooltip
            v-else
            :content="`${item.value}`"
            :disabled="item.value === undefined || item.value === null || item.value?.toString() === ''"
            :position="item.tooltipPosition ?? 'tl'"
          >
            <div class="w-[fit-content]">
              {{ item.value === undefined || item.value === null || item.value?.toString() === '' ? '-' : item.value }}
            </div>
          </a-tooltip>
        </template>
      </MsDescription>
      <div class="mt-[8px]">
        <StepDetailContent
          v-if="visible && detail.content"
          mode="tiled"
          show-type="CASE"
          static
          :static-info="detail"
          :is-definition="true"
          :get-report-step-detail="getCaseTaskReport"
          :report-id="detail.reportId"
        />
      </div>
    </a-spin>
  </MsDrawer>
</template>

<script setup lang="ts">
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDescription, { Description } from '@/components/pure/ms-description/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import execStatus from './execStatus.vue';
  import StepDetailContent from '@/views/api-test/components/requestComposition/response/result/index.vue';

  import { getCaseTaskReport } from '@/api/modules/api-test/report';
  import { useI18n } from '@/hooks/useI18n';

  import { executeResultMap, executeStatusMap } from './config';

  const props = defineProps<{
    id: string;
    userName: string;
    resourceName: string;
  }>();

  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', { required: true });
  const loading = ref(false);
  const detail = ref<any>({ description: [] });

  async function init() {
    try {
      loading.value = true;
      const res = await getCaseTaskReport(props.id);
      const { apiReportDetailDTOList } = res;
      const [caseDetail] = apiReportDetailDTOList;
      detail.value = {
        name: caseDetail?.requestName || props.resourceName,
        description: [
          {
            label: t('ms.taskCenter.executeStatus'),
            key: 'status',
            value: t(executeStatusMap[res.status].label),
          },
          {
            label: t('ms.taskCenter.operationUser'),
            value: props.userName,
          },
          {
            label: t('ms.taskCenter.taskCreateTime'),
            value: res.createTime ? dayjs(res.createTime).format('YYYY-MM-DD HH:mm:ss') : '-',
          },
          {
            label: t('ms.taskCenter.executeEnvInfo'),
            value: `${res.environmentName || t('ms.taskCenter.defaultResourcePool')} ${res.resourcePoolName} ${
              res.resourcePoolNode
            }`,
          },
          {
            label: t('ms.taskCenter.threadID'),
            value: res.threadId,
          },
          {
            label: t('ms.taskCenter.taskStartTime'),
            value: res.startTime ? dayjs(res.startTime).format('YYYY-MM-DD HH:mm:ss') : '-',
          },
          res.taskOriginName
            ? {
                label: t('ms.taskCenter.taskBelongTestPlan'),
                value: res.taskOriginName,
                class: '!w-[calc(100%/3*2)]',
              }
            : {
                label: '',
                value: ' ',
                class: '!w-[calc(100%/3*2)]',
              },
          {
            label: t('ms.taskCenter.taskEndTime'),
            value: res.endTime ? dayjs(res.endTime).format('YYYY-MM-DD HH:mm:ss') : '-',
          },
        ] as Description[],
        ...caseDetail,
        result: res.result,
        status: res.status,
      };
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  watch(
    () => visible.value,
    (val) => {
      if (props.id && val) {
        init();
      }
    },
    { immediate: true }
  );
</script>

<style lang="less" scoped>
  :deep(.ms-description-item) {
    @apply items-center;

    margin-bottom: 8px;
    font-size: 12px;
    line-height: 16px;
  }
</style>
