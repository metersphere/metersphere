<template>
  <div
    class="tiled-wrap"
    :class="{
      'border border-solid border-[var(--color-text-n8)]': props.showType === 'API',
    }"
  >
    <a-scrollbar
      :style="{
        overflow: 'auto',
        height: 'calc(100vh - 424px)',
        width: '100%',
      }"
    >
      <ScenarioItem
        v-if="tiledList.length > 0"
        :show-type="props.showType"
        :list="tiledList"
        :show-border="true"
        :active-type="props.activeType"
        :console="props.reportDetail.console"
        :environment-name="props.reportDetail.environmentName"
        @detail="showDetail"
      />
      <MsEmpty v-else />
    </a-scrollbar>
    <StepDrawer
      v-model:visible="showStepDrawer"
      :step-id="activeDetailId"
      :active-step-index="activeStepIndex"
      :scenario-detail="scenarioDetail"
      :show-type="props.showType"
      :console="props.reportDetail.console"
      :environment-name="props.reportDetail.environmentName"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { cloneDeep } from 'lodash-es';

  import MsEmpty from '@/components/pure/ms-empty/index.vue';
  import ScenarioItem from './scenarioItem.vue';
  import StepDrawer from './step/stepDrawer.vue';

  import { addLevelToTree } from '@/utils';

  import type { ReportDetail, ScenarioDetailItem, ScenarioItemType } from '@/models/apiTest/report';

  import { addFoldField } from '../utils';

  const props = defineProps<{
    reportDetail: ReportDetail;
    activeType: string; // 平铺模式|tab模式
    showType: 'API' | 'CASE'; // 接口场景|用例
  }>();

  const tiledList = ref<ScenarioItemType[]>([]);
  watchEffect(() => {
    if (props.reportDetail && props.reportDetail.children) {
      tiledList.value = props.reportDetail.children || [];
      tiledList.value = addLevelToTree<ScenarioItemType>(tiledList.value) as ScenarioItemType[];
      tiledList.value.forEach((item) => {
        addFoldField(item);
      });
    }
  });

  const showStepDrawer = ref<boolean>(false);
  const activeDetailId = ref<string>('');
  const activeStepIndex = ref<number>(0);
  const scenarioDetail = ref<ScenarioDetailItem>({});
  function showDetail(item: ScenarioItemType) {
    showStepDrawer.value = true;
    scenarioDetail.value = cloneDeep(item);
    activeDetailId.value = item.stepId;
    activeStepIndex.value = item.sort;
  }

  onMounted(() => {
    tiledList.value = addLevelToTree<ScenarioItemType>(tiledList.value) as ScenarioItemType[];
  });
</script>

<style scoped lang="less">
  .tiled-wrap {
    height: calc(100vh - 424px);
    border-radius: 4px;
  }
</style>
