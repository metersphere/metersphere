<template>
  <MsDrawer v-model:visible="visible" :width="800" :footer="false">
    <template #title>
      <div class="flex items-center gap-[8px]">
        <a-tag :color="executeResultMap[props.record.result]?.color">
          {{ t(executeResultMap[props.record.result]?.label) }}
        </a-tag>
        <div>{{ detail.name }}</div>
      </div>
      <div class="flex flex-1 justify-end">
        <MsButton type="icon" status="secondary" class="!rounded-[var(--border-radius-small)]" @click="init">
          <MsIcon type="icon-icon_reset_outlined" class="mr-[8px]" size="14" />
          {{ t('common.refresh') }}
        </MsButton>
      </div>
    </template>
    <a-spin :loading="loading" class="block">
      <MsDescription :descriptions="detail.description" :column="3" :line-gap="8" one-line-value>
        <template #value="{ item }">
          <execStatus v-if="item.key === 'status'" :status="props.record.status" size="small" />
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

  import { TaskCenterTaskDetailItem } from '@/models/taskCenter';

  import { executeResultMap, executeStatusMap } from './config';

  const props = defineProps<{
    record: TaskCenterTaskDetailItem;
  }>();

  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', { required: true });
  const loading = ref(false);
  const detail = ref<any>({ description: [] });

  async function init() {
    try {
      loading.value = true;
      const res = await getCaseTaskReport(props.record.id);
      const [caseDetail] = res;
      detail.value = {
        name: caseDetail.requestName,
        description: [
          {
            label: t('ms.taskCenter.executeStatus'),
            key: 'status',
            value: t(executeStatusMap[props.record.status].label),
          },
          {
            label: t('ms.taskCenter.operationUser'),
            value: props.record.executor,
          },
          {
            label: t('ms.taskCenter.taskCreateTime'),
            value: dayjs(props.record.startTime).format('YYYY-MM-DD HH:mm:ss'),
          },
          {
            label: t('ms.taskCenter.taskResource'),
            value: props.record.resourceName,
          },
          {
            label: t('ms.taskCenter.threadID'),
            value: props.record.threadId,
          },
          {
            label: t('ms.taskCenter.taskStartTime'),
            value: dayjs(props.record.startTime).format('YYYY-MM-DD HH:mm:ss'),
          },
          {
            label: t('ms.taskCenter.executeEnvInfo'),
            value: 'DEV 资源池1 10.11.1.1',
            class: '!w-[calc(100%/3*2)]',
          },
          {
            label: t('ms.taskCenter.taskEndTime'),
            value: dayjs(props.record.endTime).format('YYYY-MM-DD HH:mm:ss'),
          },
        ] as Description[],
        ...caseDetail,
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
      if (props.record.id && val) {
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
