<template>
  <div class="card-wrapper card-min-height">
    <CardSkeleton v-if="showSkeleton" :show-skeleton="showSkeleton" />
    <div v-else>
      <div class="flex items-center justify-between">
        <a-tooltip :content="t(props.item.label)" position="tl">
          <div class="title one-line-text"> {{ t(props.item.label) }} </div>
        </a-tooltip>
        <div>
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
        </div>
      </div>
      <div class="mt-[16px]">
        <div class="flex gap-[16px]">
          <div v-for="tabItem of testPlanTabList" :key="tabItem.label" class="flex-1">
            <PassRatePie
              :has-permission="hasPermission"
              :options="tabItem.options"
              :size="60"
              :value-list="tabItem.valueList"
            />
          </div>
        </div>
        <div class="h-[148px]">
          <MsChart :options="testPlanCountOptions" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /** *
   * @desc 测试计划数量
   */
  import { ref } from 'vue';

  import MsChart from '@/components/pure/chart/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import CardSkeleton from './cardSkeleton.vue';
  import PassRatePie from './passRatePie.vue';
  import TabCard from './tabCard.vue';

  import { workTestPlanRage } from '@/api/modules/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type {
    SelectedCardItem,
    TimeFormParams,
    WorkTestPlanDetail,
    WorkTestPlanRageDetail,
  } from '@/models/workbench/homePage';

  import { handlePieData, handleUpdateTabPie } from '../utils';

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

  const timeForm = inject<Ref<TimeFormParams>>(
    'timeForm',
    ref({
      dayNumber: 3,
      startTime: 0,
      endTime: 0,
    })
  );

  const passOptions = ref<Record<string, any>>({});
  const completeOptions = ref<Record<string, any>>({});

  // 通过率
  const passValueList = ref<{ value: number | string; label: string; name: string }[]>([]);

  // 完成率
  const completeValueList = ref<{ value: number | string; label: string; name: string }[]>([]);

  const testPlanTabList = computed(() => {
    return [
      {
        label: '',
        value: 'pass',
        valueList: passValueList.value,
        options: { ...passOptions.value },
      },
      {
        label: '',
        value: 'complete',
        valueList: completeValueList.value,
        options: { ...completeOptions.value },
      },
    ];
  });

  const testPlanCountOptions = ref({});
  // 测试计划权限
  const hasPermission = ref<boolean>(false);
  const showSkeleton = ref(false);

  async function initTestPlanCount() {
    try {
      showSkeleton.value = true;

      const { startTime, endTime, dayNumber } = timeForm.value;
      const params: WorkTestPlanDetail = {
        startTime: dayNumber ? null : startTime,
        endTime: dayNumber ? null : endTime,
        dayNumber: dayNumber ?? null,
        projectId: innerProjectIds.value[0],
      };
      const detail: WorkTestPlanRageDetail = await workTestPlanRage(params);
      const { passed, notPassed, finished, running, prepared, archived, errorCode } = detail;
      hasPermission.value = errorCode !== 109001;

      const passRate = passed + notPassed > 0 ? parseFloat(((passed / (passed + notPassed)) * 100).toFixed(2)) : 0;

      const passData = [
        {
          name: t('workbench.homePage.passRate'),
          count: passRate,
        },
        {
          name: t('workbench.homePage.havePassed'),
          count: passed,
        },
        {
          name: t('workbench.homePage.notPass'),
          count: notPassed,
        },
      ];

      const statusPercentList = [
        { status: t('common.notStarted'), count: prepared, percentValue: '0%' },
        { status: t('common.inProgress'), count: running, percentValue: '0%' },
        { status: t('common.completed'), count: finished, percentValue: '0%' },
        { status: t('common.archived'), count: archived, percentValue: '0%' },
      ];

      const total = statusPercentList.reduce((sum, item) => sum + item.count, 0);

      const listStatusPercentList = statusPercentList.map((item) => ({
        ...item,
        percentValue: total > 0 ? `${((item.count / total) * 100).toFixed(2)}%` : '0%',
      }));

      const completeRate = total > 0 ? parseFloat((((finished + archived) / total) * 100).toFixed(2)) : 0;

      const completeData = [
        {
          name: t('workbench.homePage.completeRate'),
          count: completeRate,
        },
        {
          name: t('common.completed'),
          count: finished,
        },
        {
          name: t('common.inProgress'),
          count: running,
        },
        {
          name: t('common.notStarted'),
          count: prepared,
        },
        {
          name: t('common.archived'),
          count: archived,
        },
      ];

      testPlanCountOptions.value = handlePieData(props.item.key, hasPermission.value, listStatusPercentList);

      // 通过率
      const { options: passedOptions, valueList: passList } = handleUpdateTabPie(
        passData,
        hasPermission.value,
        `${props.item.key}-pass`
      );

      // 完成率
      const { options: comOptions, valueList: completeList } = handleUpdateTabPie(
        completeData,
        hasPermission.value,
        `${props.item.key}-complete`
      );

      passValueList.value = passList;
      completeValueList.value = completeList;

      passOptions.value = passedOptions;
      completeOptions.value = comOptions;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      showSkeleton.value = false;
    }
  }

  function changeProject() {
    nextTick(() => {
      emit('change');
    });
  }

  onMounted(() => {
    initTestPlanCount();
  });

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
    () => timeForm.value,
    (val) => {
      if (val) {
        initTestPlanCount();
      }
    },
    {
      deep: true,
    }
  );

  watch([() => props.refreshKey, () => projectId.value], async () => {
    await nextTick();
    initTestPlanCount();
  });
</script>

<style scoped lang="less">
  :deep(.arco-tabs-tab) {
    padding: 0 !important;
  }
</style>
