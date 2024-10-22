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
          <execStatus v-if="item.key === 'status'" :status="item.value as ReportExecStatus" size="small" />
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
        <reportInfoHeader
          v-model:keywordName="keywordName"
          v-model:keyword="cascaderKeywords"
          v-model:active-tab="activeTab"
          show-type="API"
          @search="searchHandler"
          @reset="resetHandler"
        />
        <TiledList
          ref="tiledListRef"
          v-model:keyword-name="keywordName"
          :key-words="cascaderKeywords"
          show-type="API"
          :get-report-step-detail="getScenarioTaskReportStep"
          :active-type="activeTab"
          :report-detail="detail || []"
          class="p-[16px]"
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
  import reportInfoHeader from '@/views/api-test/report/component/step/reportInfoHeaders.vue';
  import TiledList from '@/views/api-test/report/component/tiledList.vue';

  import { getScenarioTaskReport, getScenarioTaskReportStep } from '@/api/modules/api-test/report';
  import { useI18n } from '@/hooks/useI18n';

  import { TaskCenterTaskDetailItem } from '@/models/taskCenter';
  import { ReportExecStatus } from '@/enums/apiEnum';

  import { executeResultMap, executeStatusMap } from './config';

  const props = defineProps<{
    record: TaskCenterTaskDetailItem;
  }>();

  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', { required: true });
  const loading = ref(false);
  const detail = ref<any>({ description: [], children: [] });

  async function init() {
    try {
      loading.value = true;
      const res = await getScenarioTaskReport(props.record.id);
      detail.value = {
        description: [
          {
            label: t('ms.taskCenter.executeStatus'),
            key: 'status',
            value: t(executeStatusMap[props.record.status].label),
          },
          {
            label: t('ms.taskCenter.operationUser'),
            value: res.creatUserName,
          },
          {
            label: t('ms.taskCenter.taskCreateTime'),
            value: dayjs(res.startTime).format('YYYY-MM-DD HH:mm:ss'),
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
            value: dayjs(res.startTime).format('YYYY-MM-DD HH:mm:ss'),
          },
          {
            label: t('ms.taskCenter.executeEnvInfo'),
            value: 'DEV 资源池1 10.11.1.1',
            class: '!w-[calc(100%/3*2)]',
          },
          {
            label: t('ms.taskCenter.taskEndTime'),
            value: res.endTime ? dayjs(res.endTime).format('YYYY-MM-DD HH:mm:ss') : '-',
          },
        ] as Description[],
        ...res,
      };
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  watch(
    () => props.record.id,
    () => {
      if (props.record.id) {
        init();
      }
    },
    { immediate: true }
  );

  const cascaderKeywords = ref<string>('');
  const keywordName = ref<string>('');
  const activeTab = ref<'tiled' | 'tab'>('tiled');
  const tiledListRef = ref<InstanceType<typeof TiledList>>();

  function searchHandler() {
    if (keywordName.value) {
      tiledListRef.value?.updateDebouncedSearch();
    } else {
      tiledListRef.value?.initStepTree();
    }
  }

  function resetHandler() {
    tiledListRef.value?.initStepTree();
  }
</script>

<style lang="less" scoped>
  :deep(.ms-description-item) {
    @apply items-center;

    margin-bottom: 8px;
    font-size: 12px;
    line-height: 16px;
  }
</style>
