<template>
  <div class="tiled-wrap">
    <ScenarioItem :item="scenario" :show-border="false" />
    <a-divider :margin="0" class="!mb-4"></a-divider>
    <div class="pl-[32px] pr-4">
      <MsList
        v-model:data="tiledList"
        mode="static"
        item-key-field="stepId"
        :item-border="false"
        class="w-full rounded-[var(--border-radius-small)]"
        :no-more-data="noMoreData"
        :draggable="false"
        :virtual-list-props="{
          height: 'calc(100vh - 438px)',
        }"
      >
        <template #item="{ item }">
          <ScenarioItem :item="item" @click="showDetail(item)" />
        </template>
      </MsList>
    </div>
    <StepDrawer v-model:visible="showStepDrawer" :step-id="activeDetailId" :active-step-index="activeStepIndex" />
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsList from '@/components/pure/ms-list/index.vue';
  import ScenarioItem from './scenarioItem.vue';
  import StepDrawer from './step/stepDrawer.vue';

  import type { ScenarioItemType } from '@/models/apiTest/report';

  const noMoreData = ref<boolean>(false);
  const tiledList = ref<ScenarioItemType[]>([
    {
      stepId: '步骤id',
      reportId: '报告id',
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
    },
    {
      stepId: '步骤id1',
      reportId: '报告id',
      name: '场景名称',
      sort: 0,
      stepType: 'LOOP_CONTROL',
      parentId: 'string',
      status: 'SUCCESS',
      fakeCode: 'string',
      requestName: 'string',
      requestTime: 3000,
      code: '200',
      responseSize: 234543,
      scriptIdentifier: 'string',
    },
    {
      stepId: '步骤id1',
      reportId: '报告id',
      name: '场景名称',
      sort: 0,
      stepType: 'CONDITION_CONTROL',
      parentId: 'string',
      status: 'ERROR',
      fakeCode: 'string',
      requestName: 'string',
      requestTime: 3000,
      code: '200',
      responseSize: 234543,
      scriptIdentifier: 'string',
    },
    {
      stepId: '步骤id1',
      reportId: '报告id',
      name: '场景名称',
      sort: 0,
      stepType: 'ONLY_ONCE_CONTROL',
      parentId: 'string',
      status: 'SUCCESS',
      fakeCode: 'string',
      requestName: 'string',
      requestTime: 3000,
      code: '200',
      responseSize: 234543,
      scriptIdentifier: 'string',
    },
    {
      stepId: '步骤id1',
      reportId: '报告id',
      name: '场景名称',
      sort: 0,
      stepType: 'ONLY_ONCE_CONTROL',
      parentId: 'string',
      status: 'ERROR',
      fakeCode: 'string',
      requestName: 'string',
      requestTime: 3000,
      code: '200',
      responseSize: 234543,
      scriptIdentifier: 'string',
    },
  ]);

  const scenario = ref<ScenarioItemType>({
    stepId: '步骤id1',
    reportId: '报告id',
    name: '场景名称',
    sort: 0,
    stepType: 'ONLY_ONCE_CONTROL',
    parentId: 'string',
    status: 'string',
    fakeCode: 'string',
    requestName: 'string',
    requestTime: 3000,
    code: '200',
    responseSize: 234543,
    scriptIdentifier: 'string',
  });

  const showStepDrawer = ref<boolean>(false);
  const activeDetailId = ref<string>('');
  const activeStepIndex = ref<number>(0);
  function showDetail(item: ScenarioItemType) {
    showStepDrawer.value = true;
    activeDetailId.value = item.stepId;
    activeStepIndex.value = item.sort;
  }
</script>

<style scoped lang="less">
  .tiled-wrap {
    min-height: 300px;
    border: 1px solid var(--color-text-n8);
    border-radius: 4px;
  }
</style>
