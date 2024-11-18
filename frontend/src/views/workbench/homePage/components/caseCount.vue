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
          @change="changeProject"
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
              :has-permission="hasPermission"
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

  import MsChart from '@/components/pure/chart/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import PassRatePie from './passRatePie.vue';
  import TabCard from './tabCard.vue';

  import { workCaseCountDetail } from '@/api/modules/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { SelectedCardItem, TimeFormParams } from '@/models/workbench/homePage';

  import { handlePieData, handleUpdateTabPie } from '../utils';

  const appStore = useAppStore();
  const { t } = useI18n();

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
    }
  }

  function changeProject() {
    nextTick(() => {
      initCaseCount();
      emit('change');
    });
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
        initCaseCount();
      }
    },
    {
      deep: true,
    }
  );
</script>

<style scoped lang="less"></style>
