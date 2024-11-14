<template>
  <div class="card-wrapper api-count-wrapper card-min-height">
    <div class="flex items-center justify-between">
      <div class="title"> {{ t('workbench.homePage.apiCount') }} </div>
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
    <div class="my-[16px]">
      <TabCard :content-tab-list="apiCountTabList" not-has-padding hidden-border min-width="270px">
        <template #item="{ item: tabItem }">
          <div class="w-full">
            <PassRatePie
              :tooltip-text="tabItem.tooltip"
              :options="tabItem.options"
              :size="60"
              :has-permission="hasPermission"
              :value-list="tabItem.valueList"
            />
          </div>
        </template>
      </TabCard>
      <div class="h-[148px]">
        <MsChart :options="apiCountOptions" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /** *
   * @desc 接口数量
   */
  import { ref } from 'vue';

  import MsChart from '@/components/pure/chart/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import PassRatePie from './passRatePie.vue';
  import TabCard from './tabCard.vue';

  import { workApiCountDetail } from '@/api/modules/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { SelectedCardItem, TimeFormParams } from '@/models/workbench/homePage';

  import { handlePieData, handleUpdateTabPie } from '../utils';

  const { t } = useI18n();
  const appStore = useAppStore();

  const props = defineProps<{
    item: SelectedCardItem;
    projectIds: string[];
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

  const coverValueList = ref<{ value: string | number; label: string; name: string }[]>([]);

  const completeValueList = ref<{ value: string | number; label: string; name: string }[]>([]);
  const coverOptions = ref<Record<string, any>>({});
  const completeOptions = ref<Record<string, any>>({});
  const apiCountTabList = computed(() => {
    return [
      {
        label: '',
        value: 'execution',
        valueList: coverValueList.value,
        options: { ...coverOptions.value },
        tooltip: 'workbench.homePage.apiCountCoverRateTooltip',
      },
      {
        label: '',
        value: 'pass',
        valueList: completeValueList.value,
        options: { ...completeOptions.value },
        tooltip: 'workbench.homePage.apiCountCompleteRateTooltip',
      },
    ];
  });

  const apiCountOptions = ref({});

  const hasPermission = ref<boolean>(false);
  async function initApiCount() {
    try {
      const { startTime, endTime, dayNumber } = timeForm.value;
      const detail = await workApiCountDetail({
        current: 1,
        pageSize: 5,
        startTime: dayNumber ? null : startTime,
        endTime: dayNumber ? null : endTime,
        dayNumber: dayNumber ?? null,
        projectIds: innerProjectIds.value,
        organizationId: appStore.currentOrgId,
        handleUsers: [],
      });
      const { statusStatisticsMap, statusPercentList, errorCode } = detail;

      hasPermission.value = errorCode !== 109001;

      apiCountOptions.value = handlePieData(props.item.key, hasPermission.value, statusPercentList);

      // 覆盖率 TODO 等接口
      // const { options: covOptions, valueList: coverList } = handleUpdateTabPie(
      //   statusStatisticsMap?.cover || [],
      //   hasPermission.value,
      //   `${props.item.key}-cover`
      // );
      // coverValueList.value = coverList;
      // coverOptions.value = covOptions;

      // 完成率
      const { options: comOptions, valueList: completedList } = handleUpdateTabPie(
        statusStatisticsMap?.completionRate || [],
        hasPermission.value,
        `${props.item.key}-complete`
      );
      completeValueList.value = completedList;
      completeOptions.value = comOptions;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function changeProject() {
    nextTick(() => {
      initApiCount();
      emit('change');
    });
  }

  onMounted(() => {
    initApiCount();
    emit('change');
  });

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
        initApiCount();
      }
    },
    {
      deep: true,
    }
  );
</script>

<style scoped lang="less">
  .card-wrapper.api-count-wrapper {
    padding-bottom: 4px;
  }
</style>
