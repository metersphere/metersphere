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
            tooltip-text="workbench.homePage.caseReviewCoverRateTooltip"
            :size="60"
            :value-list="coverValueList"
            :has-permission="hasPermission"
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

  import MsChart from '@/components/pure/chart/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import PassRatePie from './passRatePie.vue';

  import { workCaseReviewDetail } from '@/api/modules/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { PassRateDataType, SelectedCardItem, TimeFormParams } from '@/models/workbench/homePage';

  import { handlePieData, handleUpdateTabPie } from '../utils';

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
  async function initApiCount() {
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
    try {
      const detail: PassRateDataType = await workCaseReviewDetail(params);

      hasPermission.value = detail.errorCode !== 109001;

      const { statusStatisticsMap, statusPercentList } = detail;
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
    }
  }

  function changeProject() {
    initApiCount();
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

<style scoped lang="less"></style>
