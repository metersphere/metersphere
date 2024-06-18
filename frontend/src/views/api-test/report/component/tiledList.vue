<template>
  <div
    class="tiled-wrap p-4"
    :class="{
      'border border-solid border-[var(--color-text-n8)]': props.showType === 'API',
    }"
  >
    <!-- 步骤树 -->
    <StepTree
      ref="stepTreeRef"
      v-model:steps="tiledList"
      v-model:expandedKeys="expandedKeys"
      :show-type="props.showType"
      :active-type="props.activeType"
      :expand-all="isExpandAll"
      :console="props.reportDetail.console"
      :report-id="props.reportDetail.id"
      :get-report-step-detail="props.getReportStepDetail"
      @detail="showDetail"
    />
    <!-- 步骤抽屉 -->
    <StepDrawer
      v-model:visible="showStepDrawer"
      :step-id="activeDetailId"
      :active-step-index="activeStepIndex"
      :scenario-detail="scenarioDetail"
      :show-type="props.showType"
      :console="props.reportDetail.console"
      :report-id="props.reportDetail.id"
      :get-report-step-detail="props.getReportStepDetail"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { cloneDeep, debounce } from 'lodash-es';

  import StepDrawer from './step/stepDrawer.vue';
  import StepTree from './step/stepTree.vue';

  import type { ReportDetail, ScenarioItemType } from '@/models/apiTest/report';
  import { ScenarioStepType } from '@/enums/apiEnum';

  import { addFoldField } from '../utils';

  const props = defineProps<{
    reportDetail: ReportDetail;
    activeType: 'tiled' | 'tab'; // 平铺模式|tab模式
    showType: 'API' | 'CASE'; // 接口场景|用例
    keyWords: string;
    getReportStepDetail?: (...args: any) => Promise<any>; // 获取步骤的详情内容接口
  }>();

  const tiledList = ref<ScenarioItemType[]>([]);

  const isExpandAll = ref(false); // 是否展开全部

  const showStepDrawer = ref<boolean>(false);
  const activeDetailId = ref<string>('');
  const activeStepIndex = ref<number>(0);
  const scenarioDetail = ref<ScenarioItemType>();

  /**
   * 步骤详情
   */
  function showDetail(item: ScenarioItemType) {
    showStepDrawer.value = true;
    scenarioDetail.value = cloneDeep(item);
    activeDetailId.value = item.stepId;
    activeStepIndex.value = item.sort;
  }

  const expandedKeys = ref<(string | number)[]>([]);
  const originTreeData = ref<ScenarioItemType[]>([]);

  function initStepTree() {
    tiledList.value = cloneDeep(props.reportDetail.children) || [];
    tiledList.value.forEach((item) => {
      addFoldField(item);
    });
    originTreeData.value = cloneDeep(tiledList.value);
  }

  watch(
    () => props.reportDetail,
    (val) => {
      if (val && val.children) {
        initStepTree();
      }
    },
    { deep: true, immediate: true }
  );
  const showApiType = ref<string[]>([
    ScenarioStepType.API,
    ScenarioStepType.API_CASE,
    ScenarioStepType.CUSTOM_REQUEST,
    ScenarioStepType.SCRIPT,
  ]);
  function searchStep() {
    const splitLevel = props.keyWords.split('-');
    const stepTypeStatus = splitLevel[1];
    const stepType = splitLevel[0] === 'CUSTOM_REQUEST' ? ['API', 'API_CASE', 'CUSTOM_REQUEST'] : splitLevel[0];

    const search = (_data: ScenarioItemType[]) => {
      const result: ScenarioItemType[] = [];

      _data.forEach((item) => {
        const isStepChildren = item.children && item?.children.length && showApiType.value.includes(item.stepType);
        if (
          stepType.includes(item.stepType) &&
          ((item.status && item.status === stepTypeStatus && stepTypeStatus !== 'scriptIdentifier') ||
            (stepTypeStatus.includes('scriptIdentifier') && item.scriptIdentifier))
        ) {
          const resItem = {
            ...item,
            expanded: false,
            stepChildren: isStepChildren ? cloneDeep(item.children) : [],
            children: isStepChildren ? [] : item.children,
          };
          result.push(resItem);
        } else if (item.children) {
          const filterData = search(item.children);
          if (filterData.length) {
            const filterItem = {
              ...item,
              expanded: false,
              children: filterData,
            };

            if (isStepChildren) {
              filterItem.stepChildren = cloneDeep(item.children);

              filterItem.children = [];
            }
            result.push(filterItem);
          }
        }
      });

      return result;
    };

    return search(originTreeData.value);
  }

  // 防抖搜索
  const updateDebouncedSearch = debounce(() => {
    if (props.keyWords) {
      tiledList.value = searchStep();
    }
  }, 300);

  watch(
    () => props.keyWords,
    (val) => {
      if (!val) {
        initStepTree();
      } else {
        updateDebouncedSearch();
      }
    }
  );
</script>

<style scoped lang="less">
  .tiled-wrap {
    min-height: calc(100vh - 424px);
    border-radius: 4px;
  }
</style>
