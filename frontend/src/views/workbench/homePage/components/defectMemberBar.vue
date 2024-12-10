<template>
  <div class="card-wrapper">
    <CardSkeleton v-if="showSkeleton" :show-skeleton="showSkeleton" />
    <div v-else>
      <div class="flex items-center justify-between">
        <a-tooltip :content="t(props.item.label)" position="tl">
          <div class="title one-line-text"> {{ t(props.item.label) }} </div>
        </a-tooltip>
        <div class="flex items-center gap-[8px]">
          <MsSelect
            v-model:model-value="projectId"
            :options="appStore.projectList"
            allow-search
            value-key="id"
            label-key="name"
            :search-keys="['name']"
            class="!w-[200px]"
            :prefix="t('workbench.homePage.project')"
            @change="changeProject"
          >
          </MsSelect>
          <MsSelect
            v-model:model-value="innerHandleUsers"
            :options="memberOptions"
            allow-search
            :search-keys="['label']"
            class="!w-[220px]"
            :prefix="t('workbench.homePage.staff')"
            :multiple="true"
            :has-all-select="true"
            @popup-visible-change="popupVisibleChange"
          >
          </MsSelect>
        </div>
      </div>
      <div class="mt-[16px]">
        <MsChart ref="chartRef" height="260px" :options="options" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /**
   * @desc 缺陷处理人数量
   */
  import { ref } from 'vue';

  import MsChart from '@/components/pure/chart/index.vue';
  import bindDataZoomEvent from '@/components/pure/chart/utils';
  import MsSelect from '@/components/business/ms-select';
  import CardSkeleton from './cardSkeleton.vue';

  import { workBugHandlerDetail, workHandleUserOptions } from '@/api/modules/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { OverViewOfProject, SelectedCardItem, TimeFormParams } from '@/models/workbench/homePage';

  import { createCustomTooltip, getColorScheme, getSeriesData } from '../utils';

  const { t } = useI18n();
  const appStore = useAppStore();
  const props = defineProps<{
    item: SelectedCardItem;
    refreshKey: number;
  }>();

  const emit = defineEmits<{
    (e: 'change'): void;
  }>();

  const innerProjectIds = defineModel<string[]>('projectIds', {
    required: true,
  });

  const projectId = ref<string>(innerProjectIds.value[0]);

  const innerHandleUsers = defineModel<string[]>('handleUsers', {
    required: true,
  });

  const timeForm = inject<Ref<TimeFormParams>>(
    'timeForm',
    ref({
      dayNumber: 3,
      startTime: 0,
      endTime: 0,
    })
  );

  const memberOptions = ref<{ label: string; value: string }[]>([]);

  const options = ref<Record<string, any>>({});

  const defectStatusColor = ['#811FA3', '#FFA200', '#3370FF', '#F24F4F'];
  const hasPermission = ref<boolean>(false);

  function handleData(detail: OverViewOfProject) {
    const data = detail.projectCountList.map((e) => {
      return {
        value: '',
        label: e.name,
      };
    });

    options.value = getSeriesData(
      data,
      detail,
      [...defectStatusColor, ...getColorScheme(13)],
      false,
      true,
      props.item.fullScreen
    );
  }
  const showSkeleton = ref(false);

  async function getDefectMemberDetail() {
    try {
      showSkeleton.value = true;
      const { startTime, endTime, dayNumber } = timeForm.value;
      const detail = await workBugHandlerDetail({
        current: 1,
        pageSize: 5,
        startTime: dayNumber ? null : startTime,
        endTime: dayNumber ? null : endTime,
        dayNumber: dayNumber ?? null,
        projectIds: innerProjectIds.value,
        organizationId: appStore.currentOrgId,
        handleUsers: innerHandleUsers.value,
      });
      hasPermission.value = detail.errorCode !== 109001;

      handleData(detail);
    } catch (error) {
      console.log(error);
    } finally {
      showSkeleton.value = false;
    }
  }

  async function getMemberOptions() {
    const [newProjectId] = innerProjectIds.value;
    const res = await workHandleUserOptions(newProjectId);
    memberOptions.value = res.map((e: any) => ({
      label: e.text,
      value: e.value,
    }));
  }
  const chartRef = ref<InstanceType<typeof MsChart>>();

  async function handleProjectChange(isRefreshKey: boolean = false, setAll = false) {
    await nextTick();
    if (!isRefreshKey) {
      await getMemberOptions();
      if (setAll) {
        innerHandleUsers.value = [...memberOptions.value.map((e) => e.value)];
      } else {
        innerHandleUsers.value = innerHandleUsers.value.filter((id: string) =>
          memberOptions.value.some((member) => member.value === id)
        );
      }
    }
    await nextTick();
    await getDefectMemberDetail();
    const chartDom = chartRef.value?.chartRef;

    if (chartDom && chartDom.chart) {
      createCustomTooltip(chartDom);
      bindDataZoomEvent(chartRef, options);
    }
  }

  async function changeProject() {
    await handleProjectChange(false, true);
    emit('change');
  }

  function popupVisibleChange(val: boolean) {
    if (!val) {
      nextTick(() => {
        getDefectMemberDetail();
        emit('change');
      });
    }
  }

  watch(
    () => props.item.projectIds,
    (val) => {
      if (val) {
        const [newProjectId] = val;
        projectId.value = newProjectId;
      }
    },
    { immediate: true }
  );

  watch(
    () => projectId.value,
    (val) => {
      if (val) {
        innerProjectIds.value = [val];
      }
    },
    { immediate: true }
  );

  watch(
    () => timeForm.value,
    (val) => {
      if (val) {
        getDefectMemberDetail();
      }
    },
    {
      deep: true,
    }
  );

  watch(
    () => props.refreshKey,
    (refreshKey) => {
      handleProjectChange(!!refreshKey);
    }
  );

  onMounted(() => {
    handleProjectChange(false);
  });
</script>

<style scoped lang="less">
  :deep(.arco-select-view-multiple.arco-select-view-size-medium .arco-select-view-tag) {
    margin-top: 1px;
    margin-bottom: 1px;
    max-width: 80px;
    height: auto;
    min-height: 24px;
    line-height: 22px;
    vertical-align: middle;
  }
</style>
