<template>
  <div class="card-wrapper card-min-height">
    <div class="flex items-center justify-between">
      <div class="title">
        {{ t('workbench.homePage.numberOfTestPlan') }}
      </div>
      <div>
        <MsSelect
          v-model:model-value="projectId"
          :options="appStore.projectList"
          allow-search
          value-key="id"
          label-key="name"
          :search-keys="['name']"
          class="!w-[240px]"
          :prefix="t('workbench.homePage.project')"
          @change="changeProject"
        >
        </MsSelect>
      </div>
    </div>
    <div class="mt-[16px]">
      <TabCard :content-tab-list="testPlanTabList" not-has-padding hidden-border min-width="270px">
        <template #item="{ item: tabItem }">
          <div class="w-full">
            <PassRatePie
              :has-permission="hasPermission"
              :options="tabItem.options"
              :size="60"
              :value-list="tabItem.valueList"
            />
          </div>
        </template>
      </TabCard>
      <div class="h-[148px]">
        <MsChart :options="testPlanCountOptions" />
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

  const executionOptions = ref<Record<string, any>>({});
  const passOptions = ref<Record<string, any>>({});
  const completeOptions = ref<Record<string, any>>({});

  // 执行率
  const executionValueList = ref<{ value: number | string; label: string; name: string }[]>([]);
  // 通过率
  const passValueList = ref<{ value: number | string; label: string; name: string }[]>([]);

  // 完成率
  const completeValueList = ref<{ value: number | string; label: string; name: string }[]>([]);

  const testPlanTabList = computed(() => {
    return [
      {
        label: '',
        value: 'execution',
        valueList: executionValueList.value,
        options: { ...executionOptions.value },
      },
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
  async function initTestPlanCount() {
    try {
      const { startTime, endTime, dayNumber } = timeForm.value;
      const params: WorkTestPlanDetail = {
        startTime: dayNumber ? null : startTime,
        endTime: dayNumber ? null : endTime,
        dayNumber: dayNumber ?? null,
        projectId: innerProjectIds.value[0],
      };
      const detail: WorkTestPlanRageDetail = await workTestPlanRage(params);
      const { unExecute, executed, passed, notPassed, finished, running, prepared, archived, errorCode } = detail;
      hasPermission.value = errorCode !== 109001;

      const executeRate =
        executed + unExecute > 0 ? parseFloat(((executed / (executed + unExecute)) * 100).toFixed(2)) : 0;
      const executeData: {
        name: string;
        count: number;
      }[] = [
        {
          name: t('workbench.homePage.executeRate'),
          count: executeRate,
        },
        {
          name: t('common.unExecute'),
          count: unExecute,
        },
        {
          name: t('common.executed'),
          count: executed,
        },
      ];

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

      const completeRate = total > 0 ? parseFloat(((finished / total) * 100).toFixed(2)) : 0;

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
      ];

      testPlanCountOptions.value = handlePieData(props.item.key, hasPermission.value, listStatusPercentList);

      // 执行率
      const { options: executedOptions, valueList: executedList } = handleUpdateTabPie(
        executeData,
        hasPermission.value,
        `${props.item.key}-execute`
      );

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

      executionValueList.value = executedList;
      passValueList.value = passList;
      completeValueList.value = completeList;

      executionOptions.value = executedOptions;
      passOptions.value = passedOptions;
      completeOptions.value = comOptions;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function changeProject() {
    nextTick(() => {
      initTestPlanCount();
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
    }
  );

  watch(
    () => innerProjectIds.value,
    (val) => {
      if (val) {
        const [newProjectId] = val;
        projectId.value = newProjectId;
      }
    }
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
</script>

<style scoped lang="less">
  :deep(.arco-tabs-tab) {
    padding: 0 !important;
  }
</style>
