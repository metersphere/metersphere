<template>
  <div class="card-wrapper card-min-height">
    <div class="flex items-center justify-between">
      <div class="title"> {{ t(props.item.label) }} </div>
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
      <div class="case-count-wrapper">
        <div class="case-count-item mb-[16px]">
          <PassRatePie
            :has-permission="hasPermission"
            :tooltip-text="tooltip"
            :options="legacyOptions"
            :size="60"
            :value-list="legacyValueList"
          />
        </div>
      </div>
      <div class="h-[148px]">
        <MsChart :options="countOptions" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /** *
   * @desc 用于缺陷数量，待我处理的缺陷数量组件
   */
  import { ref } from 'vue';

  import MsChart from '@/components/pure/chart/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import PassRatePie from './passRatePie.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { PassRateDataType, SelectedCardItem, TimeFormParams } from '@/models/workbench/homePage';
  import { WorkCardEnum } from '@/enums/workbenchEnum';

  import { handlePieData, handleUpdateTabPie } from '../utils';

  const appStore = useAppStore();

  const { t } = useI18n();

  const props = defineProps<{
    item: SelectedCardItem;
  }>();

  const innerProjectIds = defineModel<string[]>('projectIds', {
    required: true,
  });

  const projectId = ref<string>(innerProjectIds.value[0]);

  const legacyValueList = ref<{ value: number | string; label: string; name: string }[]>([]);

  const timeForm = inject<Ref<TimeFormParams>>(
    'timeForm',
    ref({
      dayNumber: 3,
      startTime: 0,
      endTime: 0,
    })
  );

  const legacyOptions = ref<Record<string, any>>({});

  // TODO 假数据
  const detail = ref<PassRateDataType>({
    statusStatisticsMap: {
      legacy: [
        { name: '遗留率', count: 10 },
        { name: '缺陷总数', count: 2 },
        { name: '遗留缺陷数', count: 1 },
      ],
    },
    statusPercentList: [
      { status: 'AAA', count: 1, percentValue: '10%' },
      { status: 'BBB', count: 3, percentValue: '0%' },
      { status: 'CCC', count: 6, percentValue: '0%' },
    ],
    errorCode: 0,
  });

  const countOptions = ref<Record<string, any>>({});

  const hasPermission = ref<boolean>(false);
  async function initCount() {
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
      const { statusStatisticsMap, statusPercentList, errorCode } = detail.value;
      hasPermission.value = errorCode !== 109001;

      countOptions.value = handlePieData(props.item.key, hasPermission.value, statusPercentList);

      const { options, valueList } = handleUpdateTabPie(
        statusStatisticsMap?.legacy || [],
        hasPermission.value,
        `${props.item.key}-legacy`
      );
      legacyValueList.value = valueList;
      legacyOptions.value = options;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const tooltip = computed(() => {
    return props.item.key === WorkCardEnum.PLAN_LEGACY_BUG ? 'workbench.homePage.planCaseCountLegacyRateTooltip' : '';
  });

  function changeProject() {
    initCount();
  }

  onMounted(() => {
    initCount();
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
        initCount();
      }
    },
    {
      deep: true,
    }
  );
</script>

<style scoped lang="less"></style>
