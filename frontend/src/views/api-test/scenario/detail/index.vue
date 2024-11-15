<template>
  <div class="h-full w-full overflow-hidden">
    <div class="px-[24px] pt-[16px]">
      <MsDetailCard :title="`【${scenario.num}】${scenario.name}`" :description="description" class="!py-[8px]">
        <template #titlePrefix>
          <apiStatus :status="scenario.status" size="small" />
          <caseLevel :case-level="scenario.priority as CaseLevel" />
        </template>
        <template #titleAppend>
          <a-tooltip :content="t(scenario.follow ? 'common.forked' : 'common.notForked')">
            <MsIcon
              :loading="followLoading"
              :type="scenario.follow ? 'icon-icon_collect_filled' : 'icon-icon_collection_outlined'"
              :class="`${scenario.follow ? 'text-[rgb(var(--warning-6))]' : 'text-[var(--color-text-4)]'}`"
              class="cursor-pointer"
              :size="16"
              @click="toggleFollowReview"
            />
          </a-tooltip>
          <a-tooltip :content="t('report.detail.api.copyLink')">
            <MsIcon
              type="icon-icon_link-copy_outlined"
              class="cursor-pointer text-[var(--color-text-4)]"
              :size="16"
              @click="share"
            />
          </a-tooltip>
        </template>
      </MsDetailCard>
    </div>
    <div class="h-[calc(100%-104px)]">
      <a-tabs v-model:active-key="activeKey" class="h-full" animation lazy-load>
        <a-tab-pane
          :key="ScenarioDetailComposition.BASE_INFO"
          :title="t('apiScenario.baseInfo')"
          class="scenario-detail-tab-pane base-info-pane"
        >
          <baseInfo
            ref="baseInfoRef"
            is-edit
            :scenario="scenario as ScenarioDetail"
            :module-tree="props.moduleTree"
            class="w-[30%]"
            @change="scenario.unSaved = true"
          />
        </a-tab-pane>
        <a-tab-pane
          :key="ScenarioDetailComposition.STEP"
          :title="t('apiScenario.step')"
          class="scenario-detail-tab-pane"
        >
          <step
            v-if="activeKey === ScenarioDetailComposition.STEP"
            v-model:scenario="scenario"
            @batch-debug="emit('batchDebug', $event)"
          />
        </a-tab-pane>
        <a-tab-pane
          :key="ScenarioDetailComposition.PARAMS"
          :title="t('apiScenario.params')"
          class="scenario-detail-tab-pane"
        >
          <params
            v-if="activeKey === ScenarioDetailComposition.PARAMS"
            v-model:commonVariables="scenario.scenarioConfig.variable.commonVariables"
            v-model:csvVariables="scenario.scenarioConfig.variable.csvVariables"
            @change="scenario.unSaved = true"
          />
        </a-tab-pane>
        <a-tab-pane
          :key="ScenarioDetailComposition.PRE_POST"
          :title="t('apiScenario.prePost')"
          class="scenario-detail-tab-pane"
        >
          <prePost
            v-if="activeKey === ScenarioDetailComposition.PRE_POST"
            v-model:post-processor-config="scenario.scenarioConfig.postProcessorConfig"
            v-model:pre-processor-config="scenario.scenarioConfig.preProcessorConfig"
            @change="changePrePost"
          />
        </a-tab-pane>
        <a-tab-pane
          :key="ScenarioDetailComposition.ASSERTION"
          :title="t('apiScenario.assertion')"
          class="scenario-detail-tab-pane"
        >
          <assertion
            v-if="activeKey === ScenarioDetailComposition.ASSERTION"
            v-model:assertion-config="scenario.scenarioConfig.assertionConfig"
            @change="scenario.unSaved = true"
          />
          <template #title>
            <div class="flex items-center">
              <div> {{ t('apiScenario.assertion') }}</div>
              <a-badge
                v-if="scenario.scenarioConfig.assertionConfig.assertions.length"
                class="-mb-[2px] ml-2"
                :class="activeKey === ScenarioDetailComposition.ASSERTION ? 'active-badge' : ''"
                :max-count="99"
                :text="assertCount"
              >
              </a-badge>
            </div>
          </template>
        </a-tab-pane>
        <a-tab-pane
          :key="ScenarioDetailComposition.EXECUTE_HISTORY"
          :title="t('apiScenario.executeHistory')"
          class="scenario-detail-tab-pane"
        >
          <executeHistory v-if="activeKey === ScenarioDetailComposition.EXECUTE_HISTORY" :scenario-id="scenario.id" />
        </a-tab-pane>
        <a-tab-pane
          :key="ScenarioDetailComposition.CHANGE_HISTORY"
          :title="t('apiScenario.changeHistory')"
          class="scenario-detail-tab-pane"
        >
          <changeHistory v-if="activeKey === ScenarioDetailComposition.CHANGE_HISTORY" :source-id="scenario.id" />
        </a-tab-pane>
        <!-- <a-tab-pane
          :key="ScenarioDetailComposition.DEPENDENCY"
          :title="t('apiScenario.dependency')"
          class="scenario-detail-tab-pane"
        >
          <dependency v-if="activeKey === ScenarioDetailComposition.DEPENDENCY" />
        </a-tab-pane>
        <a-tab-pane :key="ScenarioDetailComposition.QUOTE" :title="t('apiScenario.quote')" class="scenario-detail-tab-pane">
          <quote v-if="activeKey === ScenarioDetailComposition.QUOTE" />
        </a-tab-pane> -->
        <a-tab-pane
          :key="ScenarioDetailComposition.SETTING"
          :title="t('common.setting')"
          class="scenario-detail-tab-pane"
        >
          <setting
            v-if="activeKey === ScenarioDetailComposition.SETTING"
            v-model:other-config="scenario.scenarioConfig.otherConfig"
            @change="scenario.unSaved = true"
          />
        </a-tab-pane>
      </a-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep, debounce } from 'lodash-es';

  import MsDetailCard from '@/components/pure/ms-detail-card/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import type { CaseLevel } from '@/components/business/ms-case-associate/types';
  import baseInfo from '../components/baseInfo.vue';
  import step from '../components/step/index.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';
  import { TabErrorMessage } from '@/views/api-test/components/requestComposition/index.vue';

  import { followScenario } from '@/api/modules/api-test/scenario';

  import { ApiScenarioDebugRequest, Scenario, ScenarioDetail } from '@/models/apiTest/scenario';
  import { ModuleTreeNode } from '@/models/common';
  import { ScenarioDetailComposition } from '@/enums/apiEnum';

  // 组成部分异步导入
  const params = defineAsyncComponent(() => import('../components/params.vue'));
  const prePost = defineAsyncComponent(() => import('../components/prePost.vue'));
  const assertion = defineAsyncComponent(() => import('../components/assertion.vue'));
  const executeHistory = defineAsyncComponent(() => import('../components/executeHistory.vue'));
  const changeHistory = defineAsyncComponent(() => import('../components/changeHistory.vue'));
  // const dependency = defineAsyncComponent(() => import('../components/dependency.vue'));
  // const quote = defineAsyncComponent(() => import('../components/quote.vue'));
  const setting = defineAsyncComponent(() => import('../components/setting.vue'));

  const props = defineProps<{
    moduleTree: ModuleTreeNode[]; // 模块树
  }>();
  const emit = defineEmits<{
    (e: 'batchDebug', data: Pick<ApiScenarioDebugRequest, 'steps' | 'stepDetails' | 'reportId'>): void;
    (e: 'updateFollow'): void;
  }>();

  const { copy, isSupported } = useClipboard({ legacy: true });
  const { t } = useI18n();

  const scenario = defineModel<Scenario>('scenario', {
    required: true,
  });

  const description = computed(() => [
    {
      key: 'tag',
      locale: 'common.tag',
      value: scenario.value.tags,
    },
    {
      key: 'description',
      locale: 'common.desc',
      value: scenario.value.description,
    },
  ]);

  const followLoading = ref(false);
  async function toggleFollowReview() {
    try {
      followLoading.value = true;
      await followScenario(scenario.value.id || '');
      scenario.value.follow = !scenario.value.follow;
      Message.success(scenario.value.follow ? t('common.followSuccess') : t('common.unFollowSuccess'));
      emit('updateFollow');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      followLoading.value = false;
    }
  }

  function share() {
    if (isSupported) {
      const url = window.location.href;
      const dIdParam = `&id=${scenario.value.id}`;
      const copyUrl = url.includes('id') ? url.split('&id')[0] : url;
      copy(`${copyUrl}${dIdParam}`);
      Message.success(t('common.copySuccess'));
    } else {
      Message.error(t('common.copyNotSupport'));
    }
  }

  const assertCount = computed(() => {
    return scenario.value.scenarioConfig.assertionConfig.assertions.length > 99
      ? '99+'
      : `${scenario.value.scenarioConfig.assertionConfig.assertions.length}` || '';
  });

  const activeKey = ref<ScenarioDetailComposition>(ScenarioDetailComposition.STEP);

  // 前置和后置在一个tab里，isChangePre用于判断当前修改的form是前置还是后置
  const isChangePre = ref(true);
  function changePrePost(changePre: boolean) {
    isChangePre.value = changePre;
    scenario.value.unSaved = true;
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
    if (tabKey === ScenarioDetailComposition.PRE_POST) {
      setChildErrorMessage(
        scenario.value.scenarioConfig[isChangePre.value ? 'preProcessorConfig' : 'postProcessorConfig']
          .activeItemId as number,
        {
          value: tabKey,
          label: t('apiScenario.prePost'),
          messageList: formErrorMessageList,
        }
      );
    } else if (tabKey === ScenarioDetailComposition.PARAMS) {
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
      if (key === ScenarioDetailComposition.PRE_POST) {
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

  const baseInfoRef = ref<InstanceType<typeof baseInfo>>();
  function validScenarioForm(cb: () => Promise<void>) {
    // 检查全部的校验信息
    if (getFlattenedMessages()?.length) {
      showMessage();
      return;
    }
    if (!baseInfoRef.value) {
      cb();
      return;
    }
    baseInfoRef.value?.createFormRef?.validate(async (errors) => {
      if (errors) {
        activeKey.value = ScenarioDetailComposition.BASE_INFO;
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
    .scenario-detail-tab-pane {
      padding: 8px 16px;
    }
    .base-info-pane {
      @apply h-full  overflow-auto;
      .ms-scroll-bar();
    }
  }
</style>
