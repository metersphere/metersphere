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
        <div class="case-count-wrapper mb-[16px]">
          <div class="case-count-item">
            <PassRatePie
              :options="options"
              :project-id="projectId"
              tooltip-text="workbench.homePage.caseReviewCoverRateTooltip"
              :value-list="coverValueList"
              :has-permission="hasPermission"
            />
          </div>
        </div>
        <div class="h-[148px]">
          <LegendPieChart
            v-model:currentPage="currentPage"
            :has-permission="hasPermission"
            :data="statusPercentValue"
            :options="caseReviewCountOptions"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /** *
   * @desc 用例评审数量
   */
  import { ref } from 'vue';

  import MsSelect from '@/components/business/ms-select';
  import CardSkeleton from './cardSkeleton.vue';
  import LegendPieChart, { legendDataType } from './legendPieChart.vue';
  import PassRatePie from './passRatePie.vue';

  import { workCaseReviewDetail } from '@/api/modules/workbench';
  import getVisualThemeColor from '@/config/chartTheme';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { PassRateDataType, SelectedCardItem, TimeFormParams } from '@/models/workbench/homePage';

  import { colorMapConfig, handlePieData, handleUpdateTabPie } from '../utils';

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
  const currentPage = ref(1);

  const projectId = ref<string>(innerProjectIds.value[0]);

  const timeForm = inject<Ref<TimeFormParams>>(
    'timeForm',
    ref({
      dayNumber: 3,
      startTime: 0,
      endTime: 0,
    })
  );

  const options = ref<Record<string, any>>({});

  const coverValueList = ref<{ value: number | string; label: string; name: string }[]>([
    {
      label: t('workbench.homePage.covered'),
      value: '-',
      name: '',
    },
    {
      label: t('workbench.homePage.notCover'),
      value: '-',
      name: '',
    },
  ]);

  const caseReviewCountOptions = ref<Record<string, any>>({});

  const hasPermission = ref<boolean>(false);
  const showSkeleton = ref(false);
  const statusPercentValue = ref<legendDataType[]>([]);

  async function initReviewCount() {
    try {
      showSkeleton.value = true;
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
      const detail: PassRateDataType = await workCaseReviewDetail(params);

      hasPermission.value = detail.errorCode !== 109001;

      const { statusStatisticsMap, statusPercentList } = detail;
      statusPercentValue.value = (statusPercentList || []).map((item, index) => {
        return {
          ...item,
          selected: true,
          color: `${
            colorMapConfig[props.item.key][index] !== 'initItemStyleColor'
              ? colorMapConfig[props.item.key][index]
              : getVisualThemeColor('initItemStyleColor')
          }`,
        };
      });

      caseReviewCountOptions.value = handlePieData(props.item.key, hasPermission.value, statusPercentList);
      const { options: coverOptions, valueList } = handleUpdateTabPie(
        statusStatisticsMap?.cover || [],
        hasPermission.value,
        `${props.item.key}-cover`
      );
      coverValueList.value = valueList;
      options.value = coverOptions;
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
    initReviewCount();
  });

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
    }
  );

  watch(
    () => timeForm.value,
    (val) => {
      if (val) {
        initReviewCount();
      }
    },
    {
      deep: true,
    }
  );

  watch([() => props.refreshKey, () => projectId.value], async () => {
    await nextTick();
    initReviewCount();
  });
</script>

<style scoped lang="less"></style>
