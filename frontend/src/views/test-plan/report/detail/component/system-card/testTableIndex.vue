<template>
  <div class="mb-[8px] flex items-center justify-between">
    <div class="font-medium"> {{ props.label }} </div>
    <div v-if="props.isPreview" class="flex items-center">
      <a-switch v-model:model-value="enabledTestSet" size="small" @change="(val)=>changeHandler(val as boolean)" />
      <span class="mx-[16px]"> {{ t('ms.case.associate.testSet') }}</span>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('report.detail.caseDetailSearchPlaceholder')"
        allow-clear
        class="w-[240px]"
        @search="searchList"
        @press-enter="searchList"
        @clear="clearHandler"
      />
    </div>
  </div>
  <!-- 开启测试集 -->
  <TestSetTable
    v-if="showSetTable"
    ref="testSetTableRef"
    v-model:keyword="keyword"
    v-model:expandedKeys="expandedKeys"
    :active-type="props.activeType"
    :report-id="props.reportId"
    :share-id="props.shareId"
    :is-preview="props.isPreview"
    :enabled-test-set="enabledTestSet"
    :table-key="tableKey"
    @init-column="handleInitColumn"
  />
  <!-- 功能用例明细 -->
  <FeatureCaseTable
    v-if="showFeatureTable"
    ref="featureCaseTableRef"
    v-model:keyword="keyword"
    :report-id="props.reportId"
    :share-id="props.shareId"
    :is-preview="props.isPreview"
    :enabled-test-set="enabledTestSet"
  />
  <!-- 接口用例明细和场景用例明细 -->
  <ApiAndScenarioTable
    v-if="showApiAndScenario"
    ref="apiAndScenarioTableRef"
    v-model:keyword="keyword"
    :active-type="props.activeType"
    :report-id="props.reportId"
    :share-id="props.shareId"
    :is-preview="props.isPreview"
    :enabled-test-set="enabledTestSet"
  />
</template>

<script setup lang="ts">
  import TestSetTable from './testSetTable.vue';
  import ApiAndScenarioTable from '@/views/test-plan/report/detail/component/system-card/apiAndScenarioTable.vue';
  import FeatureCaseTable from '@/views/test-plan/report/detail/component/system-card/featureCaseTable.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useTableStore from '@/hooks/useTableStore';
  import useTestPlanReportStore from '@/store/modules/testPlan/testPlanReport';

  import { TableKeyEnum } from '@/enums/tableEnum';
  import { ReportCardTypeEnum } from '@/enums/testPlanReportEnum';

  const props = defineProps<{
    reportId: string;
    activeType: ReportCardTypeEnum; // 卡片类型
    label: string;
    shareId?: string;
    isPreview?: boolean;
  }>();

  const { t } = useI18n();
  const testPlanReportStore = useTestPlanReportStore();
  const tableStore = useTableStore();

  const keyword = ref<string>('');
  const expandedKeys = ref<string[]>([]);

  const testSetTableRef = ref<InstanceType<typeof TestSetTable>>();
  const featureCaseTableRef = ref<InstanceType<typeof FeatureCaseTable>>();
  const apiAndScenarioTableRef = ref<InstanceType<typeof ApiAndScenarioTable>>();
  const isGroup = inject<Ref<boolean>>('isPlanGroup', ref(false));

  const enabledTestSet = computed({
    set: (val) => {
      testPlanReportStore.setTestStatus(isGroup.value, val, props.activeType);
    },
    get: () => {
      return testPlanReportStore.getTestStatus(isGroup.value, props.activeType);
    },
  });

  function searchList() {
    if (enabledTestSet.value) {
      testSetTableRef.value?.loadCaseList();
    } else {
      featureCaseTableRef.value?.loadCaseList();
      apiAndScenarioTableRef.value?.loadCaseList();
    }
  }

  const tableKeyMap: Record<string, Record<string, TableKeyEnum>> = {
    [ReportCardTypeEnum.FUNCTIONAL_DETAIL]: {
      GROUP: TableKeyEnum.TEST_PLAN_REPORT_FUNCTIONAL_TABLE_GROUP,
      TEST_PLAN: TableKeyEnum.TEST_PLAN_REPORT_FUNCTIONAL_TABLE,
      NOT_PREVIEW: TableKeyEnum.TEST_PLAN_REPORT_FUNCTIONAL_TABLE_NOT_PREVIEW,
    },
    [ReportCardTypeEnum.API_CASE_DETAIL]: {
      GROUP: TableKeyEnum.TEST_PLAN_REPORT_API_TABLE_GROUP,
      TEST_PLAN: TableKeyEnum.TEST_PLAN_REPORT_API_TABLE,
      NOT_PREVIEW: TableKeyEnum.TEST_PLAN_REPORT_API_TABLE_NOT_PREVIEW,
    },
    [ReportCardTypeEnum.SCENARIO_CASE_DETAIL]: {
      GROUP: TableKeyEnum.TEST_PLAN_REPORT_SCENARIO_TABLE_GROUP,
      TEST_PLAN: TableKeyEnum.TEST_PLAN_REPORT_SCENARIO_TABLE,
      NOT_PREVIEW: TableKeyEnum.TEST_PLAN_REPORT_API_TABLE_NOT_PREVIEW,
    },
  };

  const tableKey = computed(() => {
    if (props.isPreview) {
      const groupKey = isGroup.value ? 'GROUP' : 'TEST_PLAN';
      return tableKeyMap[props.activeType][groupKey];
    }
    return tableKeyMap[props.activeType].NOT_PREVIEW;
  });

  function clearHandler() {
    keyword.value = '';
    searchList();
  }

  const showFeatureTable = computed(() => {
    if (props.isPreview) {
      return props.activeType === ReportCardTypeEnum.FUNCTIONAL_DETAIL && !enabledTestSet.value;
    }
    return props.activeType === ReportCardTypeEnum.FUNCTIONAL_DETAIL;
  });

  const showApiAndScenario = computed(() => {
    if (props.isPreview) {
      return (
        (props.activeType === ReportCardTypeEnum.API_CASE_DETAIL ||
          props.activeType === ReportCardTypeEnum.SCENARIO_CASE_DETAIL) &&
        !enabledTestSet.value
      );
    }
    return (
      props.activeType === ReportCardTypeEnum.API_CASE_DETAIL ||
      props.activeType === ReportCardTypeEnum.SCENARIO_CASE_DETAIL
    );
  });

  const showSetTable = computed(() => (props.isPreview ? enabledTestSet.value : false));

  function changeHandler(value: boolean) {
    keyword.value = '';
    testPlanReportStore.setTestStatus(isGroup.value, value, props.activeType);
  }

  // 列配置改变
  async function handleInitColumn() {
    if (enabledTestSet.value) {
      expandedKeys.value = [];
    }
  }
</script>

<style lang="less" scoped>
  .test-set-wrapper {
    padding-bottom: 16px;
    border-radius: 12px;
    background-color: var(--color-text-fff);
    @apply overflow-y-auto;
    .ms-scroll-bar();
  }
</style>
