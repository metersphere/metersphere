<template>
  <div class="tiled-wrap">
    <ScenarioItem :list="tiledList" :show-border="true" :active-type="props.activeType" @detail="showDetail" />
    <StepDrawer
      v-model:visible="showStepDrawer"
      :step-id="activeDetailId"
      :active-step-index="activeStepIndex"
      :scenario-detail="scenarioDetail"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { cloneDeep } from 'lodash-es';

  import ScenarioItem from './scenarioItem.vue';
  import StepDrawer from './step/stepDrawer.vue';

  import { addLevelToTree } from '@/utils';

  import type { ReportDetail, ScenarioDetailItem, ScenarioItemType } from '@/models/apiTest/report';

  const props = defineProps<{
    reportDetail: ReportDetail;
    activeType: string;
  }>();

  // TODO 虚拟数据
  // const tiledList = ref<ScenarioItemType[]>([]);
  // watchEffect(() => {
  //   if (props.reportDetail && props.reportDetail.children) {
  //     tiledList.value = props.reportDetail.children || [];
  //   }
  // });
  const tiledList = ref<ScenarioItemType[]>([
    {
      stepId: '1001',
      reportId: '12345657687',
      name: '场景名称',
      sort: 0,
      stepType: 'QUOTE_API',
      parentId: 'string',
      status: 'SUCCESS',
      fakeCode: 'string',
      requestName: 'string',
      requestTime: 3000,
      code: '200',
      responseSize: 234543,
      scriptIdentifier: 'string',
      fold: true,
      children: [
        {
          stepId: '1001102',
          reportId: '12345657687',
          name: '场景名称1-1',
          sort: 0,
          stepType: 'LOOP_CONTROLLER',
          parentId: 'string',
          status: 'SUCCESS',
          fakeCode: 'string',
          requestName: 'string',
          requestTime: 3000,
          code: '200',
          responseSize: 234543,
          scriptIdentifier: 'string',
          fold: true,
          children: [
            {
              stepId: '100103',
              reportId: '12345657687',
              name: '场景名称1-1-1',
              sort: 0,
              stepType: 'CUSTOM_API',
              parentId: 'string',
              status: 'SUCCESS',
              fakeCode: 'string',
              requestName: 'string',
              requestTime: 3000,
              code: '200',
              responseSize: 234543,
              scriptIdentifier: 'string',
              fold: true,
              children: [
                {
                  stepId: '100104',
                  reportId: '12345657687',
                  name: '场景名称1-1-1-1',
                  sort: 0,
                  stepType: 'LOOP_CONTROLLER',
                  parentId: 'string',
                  status: 'SUCCESS',
                  fakeCode: 'string',
                  requestName: 'string',
                  requestTime: 3000,
                  code: '200',
                  responseSize: 234543,
                  scriptIdentifier: 'string',
                  fold: true,
                  children: [],
                },
              ],
            },
          ],
        },
        {
          stepId: '步骤id',
          reportId: '12345657687',
          name: '场景名称1-1-1',
          sort: 0,
          stepType: 'QUOTE_API',
          parentId: 'string',
          status: 'SUCCESS',
          fakeCode: 'string',
          requestName: 'string',
          requestTime: 3000,
          code: '200',
          responseSize: 234543,
          scriptIdentifier: 'string',
          fold: true,
          children: [],
        },
      ],
    },
  ]);

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
    min-height: 300px;
    border: 1px solid var(--color-text-n8);
    border-radius: 4px;
  }
</style>
