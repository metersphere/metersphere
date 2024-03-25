<template>
  <div
    class="tiled-wrap h-[calc(100vh - 374px)] p-4"
    :class="{
      'border border-solid border-[var(--color-text-n8)]': props.showType === 'API',
    }"
  >
    <!-- <a-scrollbar
      :style="{
        overflow: 'auto',
        height: 'calc(100vh - 424px)',
        width: '100%',
      }"
    > -->
    <!-- 步骤树 -->
    <stepTree
      ref="stepTreeRef"
      v-model:steps="tiledList"
      :show-type="props.showType"
      :active-type="props.activeType"
      :expand-all="isExpandAll"
      :console="props.reportDetail.console"
      :environment-name="props.reportDetail.environmentName"
      :report-id="props.reportDetail.id"
      @detail="showDetail"
    />
    <!-- </a-scrollbar> -->
    <!-- 步骤抽屉 -->
    <StepDrawer
      v-model:visible="showStepDrawer"
      :step-id="activeDetailId"
      :active-step-index="activeStepIndex"
      :scenario-detail="scenarioDetail"
      :show-type="props.showType"
      :console="props.reportDetail.console"
      :environment-name="props.reportDetail.environmentName"
      :report-id="props.reportDetail.id"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { cloneDeep } from 'lodash-es';

  import StepDrawer from './step/stepDrawer.vue';
  import StepTree from './step/stepTree.vue';

  import { addLevelToTree } from '@/utils';

  import type { ReportDetail, ScenarioItemType } from '@/models/apiTest/report';

  import { addFoldField } from '../utils';

  const props = defineProps<{
    reportDetail: ReportDetail;
    activeType: 'tiled' | 'tab'; // 平铺模式|tab模式
    showType: 'API' | 'CASE'; // 接口场景|用例
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

  onMounted(() => {
    tiledList.value = addLevelToTree<ScenarioItemType>(tiledList.value) as ScenarioItemType[];
  });

  watchEffect(() => {
    if (props.reportDetail && props.reportDetail.children) {
      tiledList.value = props.reportDetail.children || [];
      tiledList.value = addLevelToTree<ScenarioItemType>(tiledList.value) as ScenarioItemType[];
      tiledList.value.forEach((item) => {
        addFoldField(item);
      });
    }
  });
</script>

<style scoped lang="less">
  .tiled-wrap {
    height: calc(100vh - 424px);
    border-radius: 4px;
  }
</style>
