<template>
  <div class="card-wrapper card-min-height">
    <div class="flex items-center justify-between">
      <div class="title"> {{ t('workbench.homePage.numberOfCaseReviews') }} </div>
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
      <div class="case-count-wrapper mb-[16px]">
        <div class="case-count-item">
          <PassRatePie
            :options="options"
            tooltip-text="workbench.homePage.caseReviewCoverRateTooltip"
            :size="60"
            :value-list="coverValueList"
          />
        </div>
      </div>
      <div class="h-[148px]">
        <MsChart :options="caseReviewCountOptions" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /** *
   * @desc 用例评审数量
   */
  import { ref } from 'vue';
  import { cloneDeep } from 'lodash-es';

  import MsChart from '@/components/pure/chart/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import PassRatePie from './passRatePie.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { PassRateDataType, SelectedCardItem, TimeFormParams } from '@/models/workbench/homePage';

  import { commonRatePieOptions, handlePieData } from '../utils';

  const { t } = useI18n();
  const appStore = useAppStore();

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

  const options = ref<Record<string, any>>(cloneDeep(commonRatePieOptions));

  const coverValueList = ref([
    {
      label: t('workbench.homePage.covered'),
      value: 10000,
    },
    {
      label: t('workbench.homePage.notCover'),
      value: 2000,
    },
  ]);

  // TODO 假数据
  const detail = ref<PassRateDataType>({
    statusStatisticsMap: {
      cover: [
        { name: '覆盖率', count: 10 },
        { name: '已覆盖', count: 2 },
        { name: '未覆盖', count: 1 },
      ],
    },
    statusPercentList: [
      { status: '未开始', count: 1, percentValue: '10%' },
      { status: '进行中', count: 3, percentValue: '0%' },
      { status: '已完成', count: 6, percentValue: '0%' },
      { status: '已归档', count: 7, percentValue: '0%' },
    ],
  });

  const caseReviewCountOptions = ref<Record<string, any>>({});
  function initApiCount() {
    const { statusStatisticsMap, statusPercentList } = detail.value;
    caseReviewCountOptions.value = handlePieData(props.item.key, statusPercentList);
    const { cover } = statusStatisticsMap;

    coverValueList.value = cover.slice(1).map((item) => {
      return {
        value: item.count,
        label: item.name,
        name: item.name,
      };
    });

    options.value.series.data = coverValueList.value;
    options.value.title.text = cover[0].name ?? '';
    options.value.title.subtext = `${cover[0].count ?? 0}%`;
    options.value.series.color = ['#00C261', '#D4D4D8'];
  }

  onMounted(() => {
    initApiCount();
  });

  watch(
    () => innerProjectIds.value,
    (val) => {
      if (val) {
        const [newProjectId] = val;
        projectId.value = newProjectId;
        initApiCount();
      }
    }
  );

  watch(
    () => projectId.value,
    (val) => {
      if (val) {
        innerProjectIds.value = [val];
        initApiCount();
      }
    }
  );

  watch(
    () => timeForm.value,
    (val) => {
      if (val) {
        initApiCount();
      }
    },
    {
      deep: true,
    }
  );
</script>

<style scoped lang="less"></style>
