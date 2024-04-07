<template>
  <MsSplitBox ref="splitBoxRef" :size="0.7" :max="0.9" :min="0.7" direction="horizontal" expand-direction="right">
    <template #first>
      <a-tabs v-model:active-key="activeKey" class="h-full" animation lazy-load>
        <a-tab-pane
          :key="ScenarioCreateComposition.STEP"
          :title="t('apiScenario.step')"
          class="scenario-create-tab-pane"
        >
          <step
            v-if="activeKey === ScenarioCreateComposition.STEP"
            v-model:scenario="scenario"
            is-new
            @batch-debug="emit('batchDebug', $event)"
          />
        </a-tab-pane>
        <a-tab-pane
          :key="ScenarioCreateComposition.PARAMS"
          :title="t('apiScenario.params')"
          class="scenario-create-tab-pane"
        >
          <params
            v-if="activeKey === ScenarioCreateComposition.PARAMS"
            v-model:params="scenario.scenarioConfig.variable.commonVariables"
          />
        </a-tab-pane>
        <a-tab-pane
          :key="ScenarioCreateComposition.PRE_POST"
          :title="t('apiScenario.prePost')"
          class="scenario-create-tab-pane"
        >
          <prePost
            v-if="activeKey === ScenarioCreateComposition.PRE_POST"
            v-model:post-processor-config="scenario.scenarioConfig.postProcessorConfig"
            v-model:pre-processor-config="scenario.scenarioConfig.preProcessorConfig"
          />
        </a-tab-pane>
        <a-tab-pane
          :key="ScenarioCreateComposition.ASSERTION"
          :title="t('apiScenario.assertion')"
          class="scenario-create-tab-pane"
        >
          <assertion
            v-if="activeKey === ScenarioCreateComposition.ASSERTION"
            v-model:assertion-config="scenario.scenarioConfig.assertionConfig"
          />
        </a-tab-pane>
        <a-tab-pane
          :key="ScenarioCreateComposition.SETTING"
          :title="t('common.setting')"
          class="scenario-create-tab-pane"
        >
          <setting
            v-if="activeKey === ScenarioCreateComposition.SETTING"
            v-model:other-config="scenario.scenarioConfig.otherConfig"
          />
        </a-tab-pane>
      </a-tabs>
    </template>
    <template #second>
      <div class="p-[16px]">
        <!-- TODO:第一版没有模板 -->
        <!-- <MsFormCreate v-model:api="fApi" :rule="currentApiTemplateRules" :option="options" /> -->
        <baseInfo
          ref="baseInfoRef"
          :scenario="scenario as Scenario"
          :module-tree="props.moduleTree"
          @change="scenario.unSaved = true"
        />
        <!-- TODO:第一版先不做依赖 -->
        <!-- <div class="mb-[8px] flex items-center">
                  <div class="text-[var(--color-text-2)]">
                    {{ t('apiTestManagement.addDependency') }}
                  </div>
                  <a-divider margin="4px" direction="vertical" />
                  <MsButton
                    type="text"
                    class="font-medium"
                    :disabled="scenario.preDependency.length === 0 && scenario.postDependency.length === 0"
                    @click="clearAllDependency"
                  >
                    {{ t('apiTestManagement.clearSelected') }}
                  </MsButton>
                </div>
                <div class="rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] p-[12px]">
                  <div class="flex items-center">
                    <div class="flex items-center gap-[4px] text-[var(--color-text-2)]">
                      {{ t('apiTestManagement.preDependency') }}
                      <div class="text-[rgb(var(--primary-5))]">
                        {{ scenario.preDependency.length }}
                      </div>
                      {{ t('apiTestManagement.dependencyUnit') }}
                    </div>
                    <a-divider margin="8px" direction="vertical" />
                    <MsButton type="text" class="font-medium" @click="handleDddDependency('pre')">
                      {{ t('apiTestManagement.addPreDependency') }}
                    </MsButton>
                  </div>
                  <div class="mt-[8px] flex items-center">
                    <div class="flex items-center gap-[4px] text-[var(--color-text-2)]">
                      {{ t('apiTestManagement.postDependency') }}
                      <div class="text-[rgb(var(--primary-5))]">
                        {{ scenario.postDependency.length }}
                      </div>
                      {{ t('apiTestManagement.dependencyUnit') }}
                    </div>
                    <a-divider margin="8px" direction="vertical" />
                    <MsButton type="text" class="font-medium" @click="handleDddDependency('post')">
                      {{ t('apiTestManagement.addPostDependency') }}
                    </MsButton>
                  </div>
                </div> -->
      </div>
    </template>
  </MsSplitBox>
</template>

<script setup lang="ts">
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import baseInfo from '../components/baseInfo.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ApiScenarioDebugRequest, Scenario } from '@/models/apiTest/scenario';
  import { ModuleTreeNode } from '@/models/common';
  import { ScenarioCreateComposition } from '@/enums/apiEnum';

  // 组成部分异步导入
  const step = defineAsyncComponent(() => import('../components/step/index.vue'));
  const params = defineAsyncComponent(() => import('../components/params.vue'));
  const prePost = defineAsyncComponent(() => import('../components/prePost.vue'));
  const assertion = defineAsyncComponent(() => import('../components/assertion.vue'));
  const setting = defineAsyncComponent(() => import('../components/setting.vue'));

  const props = defineProps<{
    moduleTree: ModuleTreeNode[]; // 模块树
  }>();
  const emit = defineEmits<{
    (e: 'batchDebug', data: Pick<ApiScenarioDebugRequest, 'steps' | 'stepDetails' | 'reportId'>): void;
  }>();

  const { t } = useI18n();

  const activeKey = ref<ScenarioCreateComposition>(ScenarioCreateComposition.STEP);
  const scenario = defineModel<Scenario>('scenario', {
    required: true,
  });

  const splitBoxRef = ref<InstanceType<typeof MsSplitBox>>();
  const baseInfoRef = ref<InstanceType<typeof baseInfo>>();

  function validScenarioForm(cb: () => Promise<void>) {
    baseInfoRef.value?.createFormRef?.validate(async (errors) => {
      if (errors) {
        splitBoxRef.value?.expand();
      } else {
        cb();
      }
    });
  }

  defineExpose({
    validScenarioForm,
  });
</script>

<style lang="less" scoped>
  :deep(.arco-tabs-nav) {
    @apply border-b;
  }
  :deep(.arco-tabs-content) {
    @apply pt-0;

    height: calc(100% - 49px);
    .arco-tabs-content-list {
      @apply h-full;
      .arco-tabs-pane {
        @apply h-full;
      }
    }
    .scenario-create-tab-pane {
      padding: 8px 16px;
    }
  }
</style>
