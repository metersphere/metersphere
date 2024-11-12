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
        >
        </MsSelect>
      </div>
    </div>
    <div class="mt-[16px]">
      <div class="case-count-wrapper">
        <div class="case-count-item">
          <div v-for="(ele, index) of executionTimeValue" :key="index" class="case-count-item-content">
            <div class="case-count-item-title">{{ ele.name }}</div>
            <div class="case-count-item-number">{{ addCommasToNumber(ele.count) }}</div>
          </div>
        </div>
        <div class="case-count-item">
          <div v-for="(ele, index) of apiCountValue" :key="index" class="case-count-item-content">
            <div class="case-count-item-title">{{ ele.name }}</div>
            <div class="case-count-item-number">{{ addCommasToNumber(ele.count) }}</div>
          </div>
        </div>
      </div>
      <div class="case-ratio-wrapper mt-[16px]">
        <div class="case-ratio-item">
          <RatioPie :data="coverData" :rate-config="coverTitleConfig" />
        </div>
        <div class="case-ratio-item">
          <RatioPie :data="caseExecuteData" :rate-config="executeTitleConfig" />
        </div>
        <div class="case-ratio-item">
          <RatioPie :data="casePassData" :rate-config="casePassTitleConfig" />
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

  const innerProjectIds = defineModel<string[]>('projectIds', {
    required: true,
  });

  const projectId = ref<string>(innerProjectIds.value[0]);

  const executionTimeValue = ref<{ name: string; count: number }[]>([
    {
      name: '执行次数',
      count: 100,
    },
  ]);

  const apiCountValue = ref<{ name: string; count: number }[]>([
    {
      name:
        props.item.key === WorkCardEnum.API_CASE_COUNT
          ? t('workbench.homePage.apiUseCasesNumber')
          : t('workbench.homePage.scenarioUseCasesNumber'),
      count: 100,
    },
    {
      name: t('workbench.homePage.misstatementCount'),
      count: 100,
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
      count: '80%',
      color: ['#EDEDF1', '#00C261'],
    };
  });

  const executeTitleConfig = computed(() => {
    return props.item.key === WorkCardEnum.API_CASE_COUNT
      ? {
          name: t('workbench.homePage.caseExecutionRate'),
          count: '80%',
          color: ['#EDEDF1', '#00C261'],
        }
      : {
          name: t('workbench.homePage.sceneExecutionRate'),
          count: '80%',
          color: ['#EDEDF1', '#00C261'],
        };
  });

  const casePassTitleConfig = computed(() => {
    return props.item.key === WorkCardEnum.API_CASE_COUNT
      ? {
          name: t('workbench.homePage.casePassedRate'),
          count: '80%',
          color: ['#00C261', '#ED0303'],
        }
      : {
          name: t('workbench.homePage.executionRate'),
          count: '80%',
          color: ['#00C261', '#ED0303'],
        };
  });

  function initApiOrScenarioCount() {}

  watch(
    () => innerProjectIds.value,
    (val) => {
      if (val) {
        const [newProjectId] = val;
        projectId.value = newProjectId;
        initApiOrScenarioCount();
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
