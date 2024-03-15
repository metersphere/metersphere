<template>
  <MsSplitBox :size="0.7" :max="0.9" :min="0.7" direction="horizontal" expand-direction="right">
    <template #first>
      <a-tabs v-model:active-key="activeKey" class="h-full" animation lazy-load>
        <a-tab-pane :key="ScenarioCreateComposition.STEP" :title="t('apiScenario.step')" class="p-[16px]">
          <step v-if="activeKey === ScenarioCreateComposition.STEP" />
        </a-tab-pane>
        <a-tab-pane :key="ScenarioCreateComposition.PARAMS" :title="t('apiScenario.params')" class="p-[16px]">
          <params v-if="activeKey === ScenarioCreateComposition.PARAMS" v-model:params="scenario.params" />
        </a-tab-pane>
        <a-tab-pane :key="ScenarioCreateComposition.PRE_POST" :title="t('apiScenario.prePost')" class="p-[16px]">
          <prePost v-if="activeKey === ScenarioCreateComposition.PRE_POST" />
        </a-tab-pane>
        <a-tab-pane :key="ScenarioCreateComposition.ASSERTION" :title="t('apiScenario.assertion')" class="p-[16px]">
          <assertion v-if="activeKey === ScenarioCreateComposition.ASSERTION" />
        </a-tab-pane>
        <a-tab-pane :key="ScenarioCreateComposition.SETTING" :title="t('common.setting')" class="p-[16px]">
          <setting v-if="activeKey === ScenarioCreateComposition.SETTING" />
        </a-tab-pane>
      </a-tabs>
    </template>
    <template #second>
      <div class="p-[16px]">
        <!-- TODO:第一版没有模板 -->
        <!-- <MsFormCreate v-model:api="fApi" :rule="currentApiTemplateRules" :option="options" /> -->
        <a-form ref="activeApiTabFormRef" :model="scenario" layout="vertical">
          <a-form-item
            field="name"
            :label="t('apiScenario.name')"
            class="mb-[16px]"
            :rules="[{ required: true, message: t('apiScenario.nameRequired') }]"
          >
            <a-input
              v-model:model-value="scenario.name"
              :max-length="255"
              :placeholder="t('apiScenario.namePlaceholder')"
              allow-clear
            />
          </a-form-item>
          <a-form-item :label="t('apiScenario.belongModule')" class="mb-[16px]">
            <a-tree-select
              v-model:modelValue="scenario.moduleId"
              :data="props.moduleTree"
              :field-names="{ title: 'name', key: 'id', children: 'children' }"
              :tree-props="{
                virtualListProps: {
                  height: 200,
                  threshold: 200,
                },
              }"
              allow-search
            />
          </a-form-item>
          <a-form-item :label="t('apiScenario.status')" class="mb-[16px]">
            <a-select
              v-model:model-value="scenario.status"
              :placeholder="t('common.pleaseSelect')"
              class="param-input w-full"
            >
              <template #label>
                <apiStatus :status="scenario.status" />
              </template>
              <a-option v-for="item of Object.values(ApiScenarioStatus)" :key="item" :value="item">
                <apiStatus :status="item" />
              </a-option>
            </a-select>
          </a-form-item>
          <a-form-item :label="t('common.tag')" class="mb-[16px]">
            <MsTagsInput v-model:model-value="scenario.tags" />
          </a-form-item>
        </a-form>
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
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ModuleTreeNode } from '@/models/common';
  import { ApiScenarioStatus, RequestCaseStatus, ScenarioCreateComposition } from '@/enums/apiEnum';

  // 组成部分异步导入
  const step = defineAsyncComponent(() => import('../components/step/index.vue'));
  const params = defineAsyncComponent(() => import('../components/params.vue'));
  const prePost = defineAsyncComponent(() => import('../components/prePost.vue'));
  const assertion = defineAsyncComponent(() => import('../components/assertion.vue'));
  const setting = defineAsyncComponent(() => import('../components/setting.vue'));

  const props = defineProps<{
    moduleTree: ModuleTreeNode[]; // 模块树
  }>();

  const { t } = useI18n();

  const activeKey = ref<ScenarioCreateComposition>(ScenarioCreateComposition.STEP);
  const scenario = ref<any>({
    name: '',
    moduleId: 'root',
    status: RequestCaseStatus.PROCESSING,
    tags: [],
    params: [],
  });
</script>

<style lang="less" scoped>
  :deep(.arco-tabs-content) {
    @apply pt-0;
  }
</style>
