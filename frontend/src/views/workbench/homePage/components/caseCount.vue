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
          <div v-for="tabItem of caseCountTabList" :key="tabItem.label" class="flex-1">
            <PassRatePie
              :options="tabItem.options"
              :project-id="projectId"
              :tooltip-text="tabItem.tooltip"
              :value-list="tabItem.valueList"
              :has-permission="hasPermission"
            />
          </div>
        </div>

        <div class="mt-[16px] h-[148px]">
          <LegendPieChart
            v-model:currentPage="currentPage"
            :has-permission="hasPermission"
            :data="statusPercentValue"
            :options="caseCountOptions"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /** *
   * @desc 用例数量
   */
  import { ref } from 'vue';

  import MsSelect from '@/components/business/ms-select';
  import CardSkeleton from './cardSkeleton.vue';
  import LegendPieChart, { legendDataType } from './legendPieChart.vue';
  import PassRatePie from './passRatePie.vue';

  import { workCaseCountDetail } from '@/api/modules/workbench';
  import getVisualThemeColor from '@/config/chartTheme';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { SelectedCardItem, TimeFormParams } from '@/models/workbench/homePage';

  import { colorMapConfig, handlePieData, handleUpdateTabPie } from '../utils';

  const appStore = useAppStore();
  const { t } = useI18n();

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
  const currentPage = ref(1);

  const timeForm = inject<Ref<TimeFormParams>>(
    'timeForm',
    ref({
      dayNumber: 3,
      startTime: 0,
      endTime: 0,
    })
  );

  const reviewValueList = ref<{ value: number | string; label: string; name: string }[]>([]);

  const passValueList = ref<{ value: number | string; label: string; name: string }[]>([]);

  const reviewOptions = ref<Record<string, any>>({});
  const passOptions = ref<Record<string, any>>({});
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

  const hasPermission = ref<boolean>(false);

  const caseCountOptions = ref<Record<string, any>>({});
  const showSkeleton = ref(false);
  const statusPercentValue = ref<legendDataType[]>([]);

  async function initCaseCount() {
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
      const detail = await workCaseCountDetail(params);
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
      hasPermission.value = detail.errorCode !== 109001;
      caseCountOptions.value = handlePieData(props.item.key, hasPermission.value, statusPercentList);

      const { valueList: reviewValue, options: reviewedOptions } = handleUpdateTabPie(
        statusStatisticsMap?.review || [],
        hasPermission.value,
        `${props.item.key}-review`
      );

      reviewOptions.value = reviewedOptions;
      reviewValueList.value = reviewValue;

      const { valueList: passList, options: passOpt } = handleUpdateTabPie(
        statusStatisticsMap?.pass || [],
        hasPermission.value,
        `${props.item.key}-pass`
      );
      passOptions.value = passOpt;
      passValueList.value = passList;
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
    initCaseCount();
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
      innerProjectIds.value = [val];
    },
    {
      immediate: true,
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

  watch([() => props.refreshKey, () => projectId.value], async () => {
    await nextTick();
    initCaseCount();
  });
</script>

<style scoped lang="less"></style>
