<template>
  <MsCard simple no-content-padding>
    <a-tabs v-model:active-key="activeTab" class="no-content">
      <a-tab-pane v-for="item of realTabList" :key="item.value" :title="item.label" />
    </a-tabs>
    <a-divider margin="0" class="!mb-[8px]"></a-divider>
    <!-- 报告列表-->
    <ReportList :name="listName" :module-type="activeTab" />
  </MsCard>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import ReportList from './component/reportList.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ReportEnum } from '@/enums/reportEnum';

  const { t } = useI18n();
  const activeTab = ref<keyof typeof ReportEnum>(ReportEnum.API_SCENARIO_REPORT);

  const realTabList = ref([
    {
      value: ReportEnum.API_SCENARIO_REPORT,
      label: t('report.api.scenario'),
    },
    {
      value: ReportEnum.API_REPORT,
      label: t('report.api.case'),
    },
  ]);

  const rightTabList = computed(() => {
    return realTabList.value;
  });

  const listName = computed(() => {
    return rightTabList.value.find((item) => item.value === activeTab.value)?.label || '';
  });
</script>

<style scoped lang="less">
  :deep(.arco-tabs-content) {
    padding-top: 0;
  }
</style>
