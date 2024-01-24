<template>
  <MsAdvanceFilter
    :filter-config-list="filterConfigList"
    :custom-fields-config-list="searchCustomFields"
    :row-count="filterRowCount"
    @keyword-search="fetchData"
    @adv-search="handleAdvSearch"
  >
    <template #left>
      <div class="flex w-full justify-between">
        <div class="text-[var(--color-text-1)]"
          >{{ moduleNamePath }}
          <span class="text-[var(--color-text-4)]"> ({{ props.modulesCount[props.activeFolder] || 0 }})</span></div
        >
        <a-radio-group v-model="showType" type="button" class="file-show-type mr-2">
          <a-radio value="all" class="show-type-icon p-[2px]">{{ t('testPlan.testPlanIndex.all') }}</a-radio>
          <a-radio value="testPlan" class="show-type-icon p-[2px]">{{ t('testPlan.testPlanIndex.testPlan') }}</a-radio>
          <a-radio value="testPlanGroup" class="show-type-icon p-[2px]">{{
            t('testPlan.testPlanIndex.testPlanGroup')
          }}</a-radio>
        </a-radio-group>
      </div>
    </template>
  </MsAdvanceFilter>
  <AllTable v-if="showType === 'all'" />
  <TestPlanTable v-if="showType === 'testPlan'" />
  <TestPlanGroupTable v-if="showType === 'testPlanGroup'" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import { MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem } from '@/components/pure/ms-advance-filter/type';
  import AllTable from './allTable.vue';
  import TestPlanGroupTable from './testplanGroup.vue';
  import TestPlanTable from './testplanTable.vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
  const props = defineProps<{
    activeFolder: string;
    activeFolderType: 'folder' | 'module';
    offspringIds: string[]; // 当前选中文件夹的所有子孙节点id
    modulesCount: Record<string, number>; // 模块数量
  }>();

  const emit = defineEmits<{
    (e: 'init', params: any): void;
  }>();

  /** *
   * 高级检索
   */
  const filterConfigList = ref<FilterFormItem[]>([]);
  const searchCustomFields = ref<FilterFormItem[]>([]);
  const filterRowCount = ref(0);
  const moduleNamePath = ref<string>('全部测试计划');

  const showType = ref<string>('all');

  function fetchData() {}

  function handleAdvSearch() {}
</script>

<style scoped></style>
