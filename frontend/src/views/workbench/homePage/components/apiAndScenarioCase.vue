<template>
  <div class="card-wrapper card-min-height">
    <div class="flex items-center justify-between">
      <div class="title">
        {{ t(props.item.label) }}
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
      <div class="case-count-wrapper">
        <div class="case-count-item">
          <div v-for="(ele, index) of executionTimeValue" :key="index" class="case-count-item-content">
            <div class="case-count-item-title">{{ ele.name }}</div>
            <div class="case-count-item-number">{{ hasPermission ? addCommasToNumber(ele.count as number) : '-' }}</div>
          </div>
        </div>
        <div class="case-count-item">
          <div v-for="(ele, index) of apiCountValue" :key="index" class="case-count-item-content">
            <div class="case-count-item-title">{{ ele.name }}</div>
            <div class="case-count-item-number">{{ hasPermission ? addCommasToNumber(ele.count as number) : '-' }}</div>
          </div>
        </div>
      </div>
      <div class="case-ratio-wrapper mt-[16px]">
        <div class="case-ratio-item">
          <RatioPie :has-permission="hasPermission" :data="coverData" :rate-config="coverTitleConfig" />
        </div>
        <div class="case-ratio-item">
          <RatioPie :has-permission="hasPermission" :data="caseExecuteData" :rate-config="executeTitleConfig" />
        </div>
        <div class="case-ratio-item">
          <RatioPie :has-permission="hasPermission" :data="casePassData" :rate-config="casePassTitleConfig" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /** *
   * @desc 接口用例数量/场景用例数量
   */
  import { ref } from 'vue';

  import MsSelect from '@/components/business/ms-select';
  import RatioPie from './ratioPie.vue';

  import { workApiCaseCountDetail, workScenarioCaseCountDetail } from '@/api/modules/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { addCommasToNumber } from '@/utils';

  import type { SelectedCardItem, TimeFormParams } from '@/models/workbench/homePage';
  import { WorkCardEnum } from '@/enums/workbenchEnum';

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

  const executionTimeValue = ref<{ name: string; count: number | string }[]>([
    {
      name: '执行次数',
      count: '-',
    },
  ]);

  const apiCountValue = ref<{ name: string; count: number | string }[]>([
    {
      name:
        props.item.key === WorkCardEnum.API_CASE_COUNT
          ? t('workbench.homePage.apiUseCasesNumber')
          : t('workbench.homePage.scenarioUseCasesNumber'),
      count: '-',
    },
    {
      name: t('workbench.homePage.misstatementCount'),
      count: '-',
    },
  ]);

  const timeForm = inject<Ref<TimeFormParams>>(
    'timeForm',
    ref({
      dayNumber: 3,
      startTime: 0,
      endTime: 0,
    })
  );

  // 接口覆盖
  const coverData = ref<{ name: string; value: number }[]>([
    {
      value: 0,
      name: t('workbench.homePage.notCover'),
    },
    {
      value: 0,
      name: t('workbench.homePage.covered'),
    },
  ]);
  const caseExecuteData = ref<{ name: string; value: number }[]>([
    {
      value: 0,
      name: t('common.unExecute'),
    },
    {
      value: 0,
      name: t('common.executed'),
    },
  ]);

  const casePassData = ref<{ name: string; value: number }[]>([
    {
      value: 0,
      name: t('workbench.homePage.notPass'),
    },
    {
      value: 0,
      name: t('workbench.homePage.havePassed'),
    },
  ]);

  const coverTitleConfig = computed(() => {
    return {
      name: t('workbench.homePage.apiCoverage'),
      color: ['#EDEDF1', '#00C261'],
      tooltipText:
        props.item.key === WorkCardEnum.API_CASE_COUNT
          ? t('workbench.homePage.apiCaseCountCoverRateTooltip')
          : t('workbench.homePage.scenarioCaseCountCoverRateTooltip'),
    };
  });

  const executeTitleConfig = computed(() => {
    return props.item.key === WorkCardEnum.API_CASE_COUNT
      ? {
          name: t('workbench.homePage.caseExecutionRate'),
          color: ['#00C261', '#EDEDF1'],
          tooltipText: t('workbench.homePage.apiCaseCountExecuteRateTooltip'),
        }
      : {
          name: t('workbench.homePage.sceneExecutionRate'),
          color: ['#EDEDF1', '#00C261'],
          tooltipText: t('workbench.homePage.scenarioCaseCountExecuteRateTooltip'),
        };
  });

  const casePassTitleConfig = computed(() => {
    return props.item.key === WorkCardEnum.API_CASE_COUNT
      ? {
          name: t('workbench.homePage.casePassedRate'),
          color: ['#00C261', '#ED0303'],
          tooltipText: t('workbench.homePage.apiCaseCountPassRateTooltip'),
        }
      : {
          name: t('workbench.homePage.executionRate'),
          color: ['#00C261', '#ED0303'],
          tooltipText: t('workbench.homePage.scenarioCaseCountPassRateTooltip'),
        };
  });

  const hasPermission = ref<boolean>(false);
  async function initApiOrScenarioCount() {
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
      let detail;
      if (props.item.key === WorkCardEnum.API_CASE_COUNT) {
        detail = await workApiCaseCountDetail(params);
      } else {
        detail = await workScenarioCaseCountDetail(params);
      }

      hasPermission.value = detail.errorCode !== 109001;

      caseExecuteData.value = (detail.statusStatisticsMap?.execRate || []).map((e) => {
        return {
          ...e,
          value: e.count,
        };
      });

      casePassData.value = (detail.statusStatisticsMap?.passRate || []).map((e) => {
        return {
          ...e,
          value: e.count,
        };
      });

      if (hasPermission.value) {
        // 执行次数
        executionTimeValue.value = detail.statusStatisticsMap?.execCount || [];
        // 数量
        const valueKey = props.item.key === WorkCardEnum.API_CASE_COUNT ? 'apiCaseCount' : 'apiScenarioCount';
        apiCountValue.value = detail.statusStatisticsMap?.[valueKey] || [];
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function changeProject() {
    nextTick(() => {
      initApiOrScenarioCount();
      emit('change');
    });
  }

  onMounted(() => {
    initApiOrScenarioCount();
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
        initApiOrScenarioCount();
      }
    },
    {
      deep: true,
    }
  );
</script>

<style scoped lang="less">
  .case-count-wrapper {
    @apply flex items-center gap-4;
    .case-count-item {
      padding: 16px;
      border-radius: 6px;
      background: var(--color-text-n9);
      @apply flex items-center;
      .case-count-item-content {
        @apply flex-1;
        .case-count-item-count {
          @apply flex-1;
        }
        .case-count-item-title {
          margin-bottom: 8px;
          color: var(--color-text-4);
        }
        .case-count-item-number {
          font-size: 20px;
          color: var(--color-text-1);
          @apply font-medium;
        }
      }
    }
  }
  .case-ratio-wrapper {
    @apply flex;
    .case-ratio-item {
      @apply flex flex-1 items-center justify-center;
    }
  }
</style>
