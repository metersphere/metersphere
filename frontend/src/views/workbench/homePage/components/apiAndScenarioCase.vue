<template>
  <div class="card-wrapper">
    <div class="flex items-center justify-between">
      <div class="title">
        {{
          props.type === WorkCardEnum.API_CASE_COUNT
            ? t('workbench.homePage.apiUseCasesNumber')
            : t('workbench.homePage.scenarioUseCasesNumber')
        }}
      </div>
      <div>
        <MsSelect
          v-model:model-value="projectIds"
          :options="projectOptions"
          :allow-search="false"
          allow-clear
          class="!w-[240px]"
          :prefix="t('workbench.homePage.project')"
          :has-all-select="true"
          :default-all-select="true"
        >
        </MsSelect>
      </div>
    </div>
    <div class="mt-[16px]">
      <div class="case-count-wrapper">
        <div class="case-count-item">
          <div class="case-count-item-title">{{ t('workbench.homePage.executionTimes') }}</div>
          <div class="case-count-item-number">{{ addCommasToNumber(executionTimes) }}</div>
        </div>
        <div class="case-count-item flex">
          <div class="case-count-item-count">
            <div class="case-count-item-title">
              {{
                props.type === WorkCardEnum.API_CASE_COUNT
                  ? t('workbench.homePage.apiUseCasesNumber')
                  : t('workbench.homePage.scenarioUseCasesNumber')
              }}
            </div>
            <div class="case-count-item-number">{{ addCommasToNumber(executionTimes) }}</div>
          </div>
          <div class="case-count-item-count">
            <div class="case-count-item-title">{{ t('workbench.homePage.misstatementCount') }}</div>
            <div class="case-count-item-number">{{ addCommasToNumber(executionTimes) }}</div>
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
  import { addCommasToNumber } from '@/utils';

  import { WorkCardEnum } from '@/enums/workbenchEnum';

  import type { SelectOptionData } from '@arco-design/web-vue';

  const projectIds = ref('');
  const projectOptions = ref<SelectOptionData[]>([]);
  const { t } = useI18n();

  const props = defineProps<{
    type: WorkCardEnum;
  }>();

  const executionTimes = ref(100000);

  // 接口覆盖
  const coverData = ref([
    {
      value: 0,
      name: t('workbench.homePage.notCover'),
      itemStyle: {
        color: '#D4D4D8',
      },
    },
    {
      value: 0,
      name: t('workbench.homePage.covered'),
      itemStyle: {
        color: '#00C261',
      },
    },
  ]);
  const caseExecuteData = ref([
    {
      value: 0,
      name: t('common.unExecute'),
      itemStyle: {
        color: '#D4D4D8',
      },
    },
    {
      value: 0,
      name: t('common.executed'),
      itemStyle: {
        color: '#00C261',
      },
    },
  ]);
  const casePassData = ref([
    {
      value: 0,
      name: t('workbench.homePage.notPass'),
      itemStyle: {
        color: '#ED0303',
      },
    },
    {
      value: 0,
      name: t('workbench.homePage.havePassed'),
      itemStyle: {
        color: '#00C261',
      },
    },
  ]);

  const coverTitleConfig = computed(() => {
    return {
      name: t('workbench.homePage.apiCoverage'),
      count: '80%',
    };
  });
  const executeTitleConfig = computed(() => {
    return props.type === WorkCardEnum.API_CASE_COUNT
      ? {
          name: t('workbench.homePage.caseExecutionRate'),
          count: '80%',
        }
      : {
          name: t('workbench.homePage.sceneExecutionRate'),
          count: '80%',
        };
  });
  const casePassTitleConfig = computed(() => {
    return props.type === WorkCardEnum.API_CASE_COUNT
      ? {
          name: t('workbench.homePage.casePassedRate'),
          count: '80%',
        }
      : {
          name: t('workbench.homePage.executionRate'),
          count: '80%',
        };
  });
</script>

<style scoped lang="less">
  .case-count-wrapper {
    @apply flex items-center gap-4;
    .case-count-item {
      padding: 16px;
      border-radius: 6px;
      background: var(--color-text-n9);
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
  .case-ratio-wrapper {
    @apply flex;
    .case-ratio-item {
      @apply flex flex-1 items-center justify-center;
    }
  }
</style>
