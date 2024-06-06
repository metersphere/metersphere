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
            v-model:commonVariables="scenario.scenarioConfig.variable.commonVariables"
            v-model:csvVariables="scenario.scenarioConfig.variable.csvVariables"
            :scenario-id="scenario.id"
            @change="() => (scenario.unSaved = true)"
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
            @change="changePrePost"
          />
        </a-tab-pane>
        <a-tab-pane :key="ScenarioCreateComposition.ASSERTION" class="scenario-create-tab-pane">
          <assertion
            v-if="activeKey === ScenarioCreateComposition.ASSERTION"
            v-model:assertion-config="scenario.scenarioConfig.assertionConfig"
            @change="() => (scenario.unSaved = true)"
          />
          <template #title>
            <div class="flex items-center">
              <div> {{ t('apiScenario.assertion') }}</div>
              <a-badge
                v-if="scenario.scenarioConfig.assertionConfig.assertions.length"
                class="-mb-[2px] ml-2"
                :class="activeKey === ScenarioCreateComposition.ASSERTION ? 'active-badge' : ''"
                :max-count="99"
                :text="assertCount"
              >
              </a-badge>
            </div>
          </template>
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
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep, debounce } from 'lodash-es';

  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import baseInfo from '../components/baseInfo.vue';
  import { TabErrorMessage } from '@/views/api-test/components/requestComposition/index.vue';

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

  // 前置和后置在一个tab里，isChangePre用于判断当前修改的form是前置还是后置
  const isChangePre = ref(true);
  function changePrePost(changePre: boolean) {
    isChangePre.value = changePre;
  }

  // TODO: 优化，拆出来
  function initErrorMessageInfoItem(key: string) {
    if (scenario.value.errorMessageInfo && !scenario.value.errorMessageInfo[key]) {
      scenario.value.errorMessageInfo[key] = {};
    }
  }

  function setChildErrorMessage(key: number | string, listItem: TabErrorMessage) {
    if (!scenario.value.errorMessageInfo) return;
    scenario.value.errorMessageInfo[activeKey.value][key] = cloneDeep(listItem);
  }

  function changeTabErrorMessageList(tabKey: string, formErrorMessageList: string[]) {
    if (!scenario.value.errorMessageInfo) return;
    initErrorMessageInfoItem(tabKey);
    if (tabKey === ScenarioCreateComposition.PRE_POST) {
      setChildErrorMessage(
        scenario.value.scenarioConfig[isChangePre.value ? 'preProcessorConfig' : 'postProcessorConfig']
          .activeItemId as number,
        {
          value: tabKey,
          label: t('apiScenario.prePost'),
          messageList: formErrorMessageList,
        }
      );
    } else if (tabKey === ScenarioCreateComposition.PARAMS) {
      scenario.value.errorMessageInfo[tabKey] = {
        value: tabKey,
        label: t('apiScenario.params'),
        messageList: formErrorMessageList,
      };
    }
  }

  const setErrorMessageList = debounce((list: string[]) => {
    changeTabErrorMessageList(activeKey.value, list);
  }, 300);
  provide('setErrorMessageList', setErrorMessageList);

  // 需要最终提示的信息
  function getFlattenedMessages() {
    if (!scenario.value.errorMessageInfo) return;
    const flattenedMessages: { label: string; messageList: string[] }[] = [];
    const { errorMessageInfo } = scenario.value;
    Object.entries(errorMessageInfo).forEach(([key, item]) => {
      const label = item.label || Object.values(item)[0]?.label;
      // 处理前后置已删除的
      if (key === ScenarioCreateComposition.PRE_POST) {
        // 前后置一共的id
        const processorIds = [
          ...scenario.value.scenarioConfig.preProcessorConfig.processors,
          ...scenario.value.scenarioConfig.postProcessorConfig.processors,
        ].map((processorItem) => String(processorItem.id));

        Object.entries(item).forEach(([childKey, childItem]) => {
          if (!processorIds.includes(childKey)) {
            childItem.messageList = [];
          }
        });
      }
      const messageList: string[] =
        item.messageList || [...new Set(Object.values(item).flatMap((child) => child.messageList))] || [];
      if (messageList.length) {
        flattenedMessages.push({ label, messageList: [...new Set(messageList)] });
      }
    });
    return flattenedMessages;
  }

  function showMessage() {
    getFlattenedMessages()?.forEach(({ label, messageList }) => {
      messageList?.forEach((message) => {
        Message.error(`${label}${message}`);
      });
    });
  }

  function validScenarioForm(cb: () => Promise<void>) {
    // 检查全部的校验信息
    if (getFlattenedMessages()?.length) {
      showMessage();
      return;
    }
    baseInfoRef.value?.createFormRef?.validate(async (errors) => {
      if (errors) {
        splitBoxRef.value?.expand();
      } else {
        cb();
      }
    });
  }

  const assertCount = computed(() => {
    return scenario.value.scenarioConfig.assertionConfig.assertions.length > 99
      ? '99+'
      : `${scenario.value.scenarioConfig.assertionConfig.assertions.length}` || '';
  });

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
