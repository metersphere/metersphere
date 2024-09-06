<template>
  <div class="mb-[8px] flex items-center justify-between">
    <div class="font-medium"> {{ props.label }} </div>
    <div v-if="props.isPreview" class="flex items-center">
      <a-switch v-model:model-value="enabledTestSet" size="small" />
      <span class="mx-[8px]"> {{ t('ms.case.associate.testSet') }}</span>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('common.searchByIdName')"
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
    v-if="enabledTestSet"
    ref="testSetTableRef"
    v-model:keyword="keyword"
    :active-type="props.activeType"
    :report-id="props.reportId"
    :share-id="props.shareId"
    :is-preview="props.isPreview"
    :is-group="props.isGroup"
    :enabled-test-set="enabledTestSet"
  />
  <!-- 功能用例明细 -->
  <FeatureCaseTable
    v-else-if="props.activeType === ReportCardTypeEnum.FUNCTIONAL_DETAIL && !enabledTestSet"
    ref="featureCaseTableRef"
    v-model:keyword="keyword"
    :report-id="props.reportId"
    :share-id="props.shareId"
    :is-preview="props.isPreview"
    :is-group="props.isGroup"
    :enabled-test-set="enabledTestSet"
  />
  <!-- 接口用例明细和场景用例明细 -->
  <ApiAndScenarioTable
    v-else-if="
      (props.activeType === ReportCardTypeEnum.API_CASE_DETAIL ||
        props.activeType === ReportCardTypeEnum.SCENARIO_CASE_DETAIL) &&
      !enabledTestSet
    "
    ref="apiAndScenarioTableRef"
    :active-type="props.activeType"
    :report-id="props.reportId"
    :share-id="props.shareId"
    :is-preview="props.isPreview"
    :is-group="props.isGroup"
    :keyword="keyword"
    :enabled-test-set="enabledTestSet"
  />
</template>

<script setup lang="ts">
  import TestSetTable from './testSetTable.vue';
  import ApiAndScenarioTable from '@/views/test-plan/report/detail/component/system-card/apiAndScenarioTable.vue';
  import FeatureCaseTable from '@/views/test-plan/report/detail/component/system-card/featureCaseTable.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ReportCardTypeEnum } from '@/enums/testPlanReportEnum';

  const props = defineProps<{
    reportId: string;
    activeType: ReportCardTypeEnum; // 卡片类型
    label: string;
    shareId?: string;
    isPreview?: boolean;
    isGroup?: boolean;
  }>();
  const { t } = useI18n();

  const enabledTestSet = ref<boolean>(false);

  const keyword = ref<string>('');

  const testSetTableRef = ref<InstanceType<typeof TestSetTable>>();
  const featureCaseTableRef = ref<InstanceType<typeof TestSetTable>>();
  const apiAndScenarioTableRef = ref<InstanceType<typeof TestSetTable>>();

  function searchList() {
    if (enabledTestSet.value) {
      testSetTableRef.value?.loadCaseList();
    } else {
      featureCaseTableRef.value?.loadCaseList();
      apiAndScenarioTableRef.value?.loadCaseList();
    }
  }

  function clearHandler() {
    keyword.value = '';
    searchList();
  }
</script>

<style lang="less" scoped>
  .test-set-wrapper {
    padding-bottom: 16px;
    border-radius: 12px;
    @apply overflow-y-auto bg-white;
    .ms-scroll-bar();
  }
  .test-set-cell {
    :deep(.arco-table-cell) {
      padding: 8px 16px !important;
    }
  }
</style>
