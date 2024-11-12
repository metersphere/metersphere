<template>
  <div class="card-wrapper card-min-height">
    <div class="flex items-center justify-between">
      <div class="title"> {{ t('workbench.homePage.useCasesNumber') }} </div>
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
        >
        </MsSelect>
      </div>
    </div>
    <div class="mt-[16px]">
      <TabCard :content-tab-list="caseCountTabList" not-has-padding hidden-border min-width="270px">
        <template #item="{ item: tabItem }">
          <div class="w-full">
            <PassRatePie
              :options="tabItem.options"
              :tooltip-text="tabItem.tooltip"
              :size="60"
              :value-list="tabItem.valueList"
            />
          </div>
        </template>
      </TabCard>
      <div class="h-[148px]">
        <MsChart :options="caseCountOptions" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /** *
   * @desc 用例数量
   */
  import { ref } from 'vue';
  import { cloneDeep } from 'lodash-es';

  import MsChart from '@/components/pure/chart/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import PassRatePie from './passRatePie.vue';
  import TabCard from './tabCard.vue';

  import { workCaseCountDetail } from '@/api/modules/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { SelectedCardItem, TimeFormParams } from '@/models/workbench/homePage';
  import { StatusStatisticsMapType } from '@/models/workbench/homePage';

  import { commonRatePieOptions, handlePieData } from '../utils';

  const appStore = useAppStore();
  const { t } = useI18n();

  const props = defineProps<{
    item: SelectedCardItem;
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

  const options = ref(cloneDeep(commonRatePieOptions));

  function handlePassRatePercent(data: { name: string; count: number }[]) {
    return data.slice(1).map((item) => {
      return {
        value: item.count,
        label: item.name,
        name: item.name,
      };
    });
  }

  const reviewValueList = ref([
    {
      label: t('workbench.homePage.reviewed'),
      value: 10000,
    },
    {
      label: t('workbench.homePage.unReviewed'),
      value: 2000,
    },
  ]);

  const passValueList = ref([
    {
      label: t('workbench.homePage.havePassed'),
      value: 10000,
    },
    {
      label: t('workbench.homePage.notPass'),
      value: 2000,
    },
  ]);

  const reviewOptions = ref<Record<string, any>>(cloneDeep(options.value));
  const passOptions = ref<Record<string, any>>(cloneDeep(options.value));
  const caseCountTabList = computed(() => {
    return [
      {
        label: '',
        value: 'execution',
        valueList: reviewValueList.value,
        options: { ...reviewOptions.value },
        tooltip: 'workbench.homePage.reviewRateTooltip',
      },
      {
        label: '',
        value: 'pass',
        valueList: passValueList.value,
        options: { ...passOptions.value },
        tooltip: 'workbench.homePage.reviewPassRateTooltip',
      },
    ];
  });

  // 处理X率饼图数据
  function handleRatePieData(statusStatisticsMap: StatusStatisticsMapType) {
    const { review, pass } = statusStatisticsMap;
    reviewValueList.value = handlePassRatePercent(review);
    passValueList.value = handlePassRatePercent(pass);

    reviewOptions.value.series.data = handlePassRatePercent(review);
    passOptions.value.series.data = handlePassRatePercent(pass);

    reviewOptions.value.title.text = review[0].name ?? '';
    reviewOptions.value.title.subtext = `${review[0].count ?? 0}%`;
    passOptions.value.title.text = pass[0].name ?? '';
    passOptions.value.title.subtext = `${pass[0].count ?? 0}%`;
    reviewOptions.value.series.color = ['#00C261', '#D4D4D8'];
    passOptions.value.series.color = ['#00C261', '#ED0303'];
  }

  const caseCountOptions = ref<Record<string, any>>({});
  async function initCaseCount() {
    try {
      const { startTime, endTime, dayNumber } = timeForm.value;
      const params = {
        current: 1,
        pageSize: 5,
        startTime: dayNumber ? null : startTime,
        endTime: dayNumber ? null : endTime,
        dayNumber: dayNumber ?? null,
        projectIds: innerProjectIds.value,
        organizationId: appStore.currentOrgId,
        handleUsers: [],
      };
      const detail = await workCaseCountDetail(params);
      const { statusStatisticsMap, statusPercentList } = detail;
      caseCountOptions.value = handlePieData(props.item.key, statusPercentList);
      handleRatePieData(statusStatisticsMap);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  onMounted(() => {
    initCaseCount();
  });

  watch(
    () => innerProjectIds.value,
    (val) => {
      if (val) {
        const [newProjectId] = val;
        projectId.value = newProjectId;
        initCaseCount();
      }
    }
  );

  watch(
    () => projectId.value,
    (val) => {
      if (val) {
        innerProjectIds.value = [val];
        initCaseCount();
      }
    }
  );

  watch(
    () => timeForm.value,
    (val) => {
      if (val) {
        initCaseCount();
      }
    },
    {
      deep: true,
    }
  );
</script>

<style scoped lang="less"></style>
