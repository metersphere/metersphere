<template>
  <div
    class="tiled-wrap"
    :class="{
      'border border-solid border-[var(--color-text-n8)]': props.showType === 'API',
      '!max-h-max': props.isExport,
    }"
  >
    <div v-if="isFailedRetry" class="mb-[8px]">
      <MsTab
        v-model:activeKey="controlCurrent"
        :content-tab-list="controlTotalList"
        mode="button"
        button-size="small"
      />
    </div>
    <!-- 步骤树 -->
    <ReadOnlyStepTree
      v-if="props.isExport"
      v-model:steps="currentTiledList"
      v-model:expandedKeys="expandedKeys"
      :show-type="props.showType"
      :active-type="props.activeType"
      :expand-all="isExpandAll"
      :console="props.reportDetail.console"
      :report-id="props.reportDetail.id"
      :get-report-step-detail="props.getReportStepDetail"
    />
    <StepTree
      v-else
      ref="stepTreeRef"
      v-model:steps="currentTiledList"
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

  import MsTab from '@/components/pure/ms-tab/index.vue';
  import ReadOnlyStepTree from './step/readOnlyTree.vue';
  import StepDrawer from './step/stepDrawer.vue';
  import StepTree from './step/stepTree.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { ReportDetail, ScenarioItemType } from '@/models/apiTest/report';
  import { ScenarioStepType } from '@/enums/apiEnum';

  import { addFoldField } from '../utils';

  const props = defineProps<{
    reportDetail: ReportDetail;
    activeType: 'tiled' | 'tab'; // 平铺模式|tab模式
    showType: 'API' | 'CASE'; // 接口场景|用例
    keyWords: string;
    getReportStepDetail?: (...args: any) => Promise<any>; // 获取步骤的详情内容接口
    isExport?: boolean; // 是否是导出pdf预览
    isFilterStep?: boolean; // 是否打开抽屉之前过滤用例步骤
    caseId?: string; // 用例id
    caseName?: string; // 用例名称
  }>();

  const { t } = useI18n();

  const tiledList = ref<ScenarioItemType[]>([]);

  const innerKeyword = defineModel<string>('keywordName', {
    default: '',
  });

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

  const controlCurrent = ref(0);
  const isFailedRetry = computed(() => {
    // 所有步骤 id 相同且带有重试前缀，说明是单个用例的重试结果
    return (
      props.reportDetail.children?.every((item) => item.stepId === props.reportDetail.children[0].stepId) &&
      props.reportDetail.children?.some((item) => item.requestName?.includes('MsRetry_'))
    );
  });
  const currentTiledList = computed(() => {
    if (isFailedRetry.value === false) {
      // 不是失败重试结果
      return tiledList.value;
    }
    // 失败重试的结果
    return tiledList.value[controlCurrent.value] ? [tiledList.value[controlCurrent.value]] : [];
  });
  const controlTotalList = computed(() => {
    return Array.from({ length: props.reportDetail.children.length }, (v, k) => {
      if (k === 0) {
        return {
          value: k,
          label: t('apiTestDebug.first'),
        };
      }
      return {
        value: k,
        label: `${t('apiTestDebug.retry')} ${k}`,
      };
    });
  });

  const showApiType = ref<string[]>([
    ScenarioStepType.API,
    ScenarioStepType.API_CASE,
    ScenarioStepType.CUSTOM_REQUEST,
    ScenarioStepType.SCRIPT,
  ]);

  function searchStep() {
    const splitLevel = props.keyWords.split('-');
    const stepTypeStatus = splitLevel[1] || '';
    const stepType =
      splitLevel[0] === 'CUSTOM_REQUEST' ? ['API', 'API_CASE', 'CUSTOM_REQUEST'] : Object.values(ScenarioStepType);
    const nameSearch = innerKeyword.value?.toLowerCase(); // 传入的 name 检索关键字
    const search = (_data: ScenarioItemType[]) => {
      const result: ScenarioItemType[] = [];
      _data.forEach((item) => {
        const isStepChildren = item.children && item?.children.length && showApiType.value.includes(item.stepType);

        const isFilterCaseStep =
          props.isFilterStep && props.reportDetail.integrated && innerKeyword.value === props.caseName;

        // 匹配步骤类型
        const matchStepType = stepType.includes(item.stepType);

        // 匹配步骤状态
        const matchStepStatus =
          (item.status && item.status === stepTypeStatus && stepTypeStatus !== 'scriptIdentifier') ||
          (stepTypeStatus.includes('scriptIdentifier') && item.scriptIdentifier);

        // 条件匹配逻辑
        let matchesStepCondition;
        // 如果开启用例过滤且为集合报告过滤用例步骤
        if (isFilterCaseStep) {
          const caseStepCondition = item.name?.toLowerCase().includes(nameSearch) && item.stepId === props.caseId;

          matchesStepCondition = stepTypeStatus
            ? caseStepCondition && matchStepType && matchStepStatus
            : caseStepCondition;
        }
        // 如果传入了 name 且有状态
        else if (nameSearch && stepTypeStatus) {
          matchesStepCondition = matchStepType && matchStepStatus && item.name?.toLowerCase().includes(nameSearch);
        }
        // 仅传入了 name 没有状态或类型
        else if (nameSearch) {
          matchesStepCondition = item.name?.toLowerCase().includes(nameSearch);
        }
        // 没有传入 name 只按状态和类型检索
        else {
          matchesStepCondition = matchStepType && matchStepStatus;
        }

        if (matchesStepCondition) {
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
    if (props.keyWords || innerKeyword.value) {
      tiledList.value = searchStep();
    }
  }, 300);

  function initStepTree() {
    tiledList.value = cloneDeep(props.reportDetail.children) || [];
    tiledList.value.forEach((item) => {
      addFoldField(item);
    });
    originTreeData.value = cloneDeep(tiledList.value);

    // 过滤当前用例步骤
    if (props.isFilterStep && props.reportDetail.integrated) {
      updateDebouncedSearch();
    }
  }

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

  watch(
    () => props.reportDetail,
    (val) => {
      if (val && val.children) {
        initStepTree();
      }
    },
    { deep: true, immediate: true }
  );

  defineExpose({
    updateDebouncedSearch,
    initStepTree,
  });
</script>

<style scoped lang="less">
  .tiled-wrap {
    overflow: auto;
    max-height: calc(100vh - 162px);
    border-radius: 4px;
    .ms-scroll-bar();
  }
</style>
