<template>
  <div class="flex h-full flex-col gap-[8px]">
    <div class="action-line">
      <div class="action-group">
        <a-checkbox
          v-show="scenario.steps.length > 0"
          v-model:model-value="checkedAll"
          :indeterminate="indeterminate"
          :disabled="scenarioExecuteLoading"
          @change="handleChangeAll"
        />
        <div class="flex items-center gap-[4px]">
          {{ t('apiScenario.sum') }}
          <div class="text-[rgb(var(--primary-5))]">{{ totalStepCount }}</div>
          {{ t('apiScenario.steps') }}
        </div>
      </div>
      <div class="action-group">
        <a-tooltip :content="isExpandAll ? t('apiScenario.collapseAllStep') : t('apiScenario.expandAllStep')">
          <a-button
            v-show="scenario.steps.length > 0"
            type="outline"
            class="expand-step-btn arco-btn-outline--secondary"
            size="mini"
            @click="expandAllStep"
          >
            <MsIcon v-if="isExpandAll" type="icon-icon_comment_collapse_text_input" />
            <MsIcon v-else type="icon-icon_comment_expand_text_input" />
          </a-button>
        </a-tooltip>
        <template v-if="checkedAll || indeterminate">
          <a-button type="outline" size="mini" :disabled="scenarioExecuteLoading" @click="batchEnable">
            {{ t('common.batchEnable') }}
          </a-button>
          <a-button type="outline" size="mini" :disabled="scenarioExecuteLoading" @click="batchDisable">
            {{ t('common.batchDisable') }}
          </a-button>
          <a-button type="outline" size="mini" :disabled="scenarioExecuteLoading" @click="batchDebug">
            {{ t('common.batchDebug') }}
          </a-button>
          <a-button type="outline" size="mini" :disabled="scenarioExecuteLoading" @click="batchDelete">
            {{ t('common.batchDelete') }}
          </a-button>
        </template>
      </div>
      <div class="action-group ml-auto">
        <template v-if="scenario.executeTime">
          <div class="action-group">
            <div class="text-[var(--color-text-4)]">{{ t('apiScenario.executeTime') }}</div>
            <div class="text-[var(--color-text-4)]">{{ scenario.executeTime }}</div>
          </div>
          <div class="action-group">
            <div class="text-[var(--color-text-4)]">{{ t('apiScenario.executeResult') }}</div>
            <div class="flex items-center gap-[4px]">
              <div class="text-[var(--color-text-1)]">{{ t('common.success') }}</div>
              <div class="text-[rgb(var(--success-6))]">{{ scenario.executeSuccessCount }}</div>
            </div>
            <div class="flex items-center gap-[4px]">
              <div class="text-[var(--color-text-1)]">{{ t('common.fail') }}</div>
              <div class="text-[rgb(var(--success-6))]">{{ scenario.executeFailCount }}</div>
            </div>
            <MsButton v-if="scenario.isDebug === false" type="text" @click="checkReport">
              {{ t('apiScenario.checkReport') }}
            </MsButton>
          </div>
        </template>
        <div v-if="!checkedAll && !indeterminate" class="action-group ml-auto">
          <a-input
            v-model:model-value="keyword"
            :placeholder="t('apiScenario.searchByName')"
            allow-clear
            class="w-[200px]"
          />
          <a-button
            v-if="!props.isNew"
            type="outline"
            class="arco-btn-outline--secondary !mr-0 !p-[8px]"
            @click="refreshStepInfo"
          >
            <template #icon>
              <icon-refresh class="text-[var(--color-text-4)]" />
            </template>
          </a-button>
        </div>
      </div>
    </div>
    <div class="h-[calc(100%-30px)]">
      <stepTree
        ref="stepTreeRef"
        v-model:steps="scenario.steps"
        v-model:checked-keys="checkedKeys"
        v-model:stepKeyword="keyword"
        v-model:scenario="scenario"
        :expand-all="isExpandAll"
        :step-details="scenario.stepDetails"
        @update-resource="handleUpdateResource"
      />
    </div>
  </div>
  <a-modal
    v-model:visible="batchToggleVisible"
    :title="isBatchEnable ? t('common.batchEnable') : t('common.batchDisable')"
    :width="480"
    :ok-text="isBatchEnable ? t('common.enable') : t('common.disable')"
    class="ms-modal-form"
    title-align="start"
    body-class="!p-0"
    @close="resetBatchToggle"
    @before-ok="handleBeforeBatchToggle"
  >
    <div class="mb-[8px] text-[var(--color-text-1)]">{{ t('apiScenario.range') }}</div>
    <a-radio-group v-model:model-value="batchToggleRange">
      <a-radio value="top">{{ t('apiScenario.topStep') }}</a-radio>
      <a-radio value="all">{{ t('apiScenario.allStep') }}</a-radio>
    </a-radio-group>
  </a-modal>
</template>

<script setup lang="ts">
  // import dayjs from 'dayjs';

  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import stepTree from './stepTree.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import { deleteNodes, filterTree, getGenerateId, mapTree } from '@/utils';
  import { countNodes } from '@/utils/tree';

  import { ApiScenarioDebugRequest, Scenario } from '@/models/apiTest/scenario';
  import { ScenarioExecuteStatus, ScenarioStepType } from '@/enums/apiEnum';
  import { ApiTestRouteEnum } from '@/enums/routeEnum';

  const props = defineProps<{
    isNew?: boolean; // 是否新建
  }>();
  const emit = defineEmits<{
    (e: 'batchDebug', data: Pick<ApiScenarioDebugRequest, 'steps' | 'stepDetails' | 'reportId'>): void;
  }>();

  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();

  const scenario = defineModel<Scenario>('scenario', {
    required: true,
  });
  const scenarioExecuteLoading = inject<Ref<boolean>>('scenarioExecuteLoading');

  const checkedAll = ref(false); // 是否全选
  const indeterminate = ref(false); // 是否半选
  const isExpandAll = ref(false); // 是否展开全部
  const checkedKeys = ref<(string | number)[]>([]); // 选中的key
  const stepTreeRef = ref<InstanceType<typeof stepTree>>();
  const keyword = ref('');

  const totalStepCount = computed(() => countNodes(scenario.value.steps));

  function handleChangeAll(value: boolean | (string | number | boolean)[]) {
    indeterminate.value = false;
    if (value) {
      checkedAll.value = true;
    } else {
      checkedAll.value = false;
    }
    stepTreeRef.value?.checkAll(checkedAll.value);
  }

  watch(
    () => checkedKeys.value,
    (val) => {
      if (val.length === 0) {
        checkedAll.value = false;
        indeterminate.value = false;
      } else if (val.length === totalStepCount.value) {
        checkedAll.value = true;
        indeterminate.value = false;
      } else {
        checkedAll.value = false;
        indeterminate.value = true;
      }
    }
  );

  watch(
    () => scenario.value.steps.length,
    () => {
      checkedKeys.value = [];
      checkedAll.value = false;
      indeterminate.value = false;
    }
  );

  function expandAllStep() {
    isExpandAll.value = !isExpandAll.value;
  }

  // 批量启用/禁用
  const isBatchEnable = ref(false);
  const batchToggleVisible = ref(false);
  const batchToggleRange = ref('top');
  function batchEnable() {
    batchToggleVisible.value = true;
    isBatchEnable.value = true;
  }

  function batchDisable() {
    batchToggleVisible.value = true;
    isBatchEnable.value = false;
  }

  function resetBatchToggle() {
    batchToggleVisible.value = false;
    batchToggleRange.value = 'top';
    isBatchEnable.value = false;
  }

  async function handleBeforeBatchToggle(done: (closed: boolean) => void) {
    try {
      const ids = new Set(checkedKeys.value);
      if (batchToggleRange.value === 'top') {
        scenario.value.steps = scenario.value.steps.map((item) => {
          if (ids.has(item.id)) {
            item.enable = isBatchEnable.value;
          }
          return item;
        });
      } else {
        scenario.value.steps = mapTree(scenario.value.steps, (node) => {
          if (ids.has(node.id) && node.config.isRefScenarioStep !== true) {
            // 如果是完全引用的场景下的子孙步骤，则不允许操作启用禁用
            node.enable = isBatchEnable.value;
          }
          return node;
        });
      }
      done(true);
      Message.success(isBatchEnable.value ? t('common.enableSuccess') : t('common.disableSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function batchDelete() {
    deleteNodes(scenario.value.steps, checkedKeys.value, 'id');
    Message.success(t('common.deleteSuccess'));
    if (scenario.value.steps.length === 0) {
      checkedAll.value = false;
      indeterminate.value = false;
    }
  }

  function checkReport() {
    openNewPage(ApiTestRouteEnum.API_TEST_REPORT, {
      reportId: scenario.value.reportId,
    });
  }

  function refreshStepInfo() {
    console.log('刷新步骤信息');
  }

  function batchDebug() {
    scenario.value.executeLoading = true;
    const checkedKeysSet = new Set(checkedKeys.value);
    const waitTingDebugSteps = filterTree(scenario.value.steps, (node) => {
      if (checkedKeysSet.has(node.id)) {
        if (!node.enable) {
          // 如果步骤未开启，则删除已选 id，方便下面waitingDebugStepDetails详情判断是否携带
          checkedKeysSet.delete(node.id);
          node.executeStatus = undefined;
        } else if (
          [ScenarioStepType.API, ScenarioStepType.API_CASE, ScenarioStepType.CUSTOM_REQUEST].includes(node.stepType)
        ) {
          // 请求和场景类型才直接显示执行中，其他控制器需要等待执行完毕才结算执行结果
          node.executeStatus = ScenarioExecuteStatus.EXECUTING;
        } else {
          // 其他类型步骤不显示执行状态
          node.executeStatus = undefined;
        }
        return !!node.enable;
      }
      node.executeStatus = undefined; // 未选中的步骤不显示执行状态
      return false;
    });
    const waitingDebugStepDetails = {};
    Object.keys(scenario.value.stepDetails).forEach((key) => {
      if (checkedKeysSet.has(key)) {
        waitingDebugStepDetails[key] = scenario.value.stepDetails[key];
      }
    });
    emit('batchDebug', {
      steps: waitTingDebugSteps,
      stepDetails: waitingDebugStepDetails,
      reportId: getGenerateId(),
    });
  }

  function handleUpdateResource(uploadFileIds?: string[], linkFileIds?: string[]) {
    const uploadFileIdsSet = new Set(scenario.value.uploadFileIds);
    const linkFileIdsSet = new Set(scenario.value.linkFileIds);
    uploadFileIds?.forEach((id) => {
      uploadFileIdsSet.add(id);
    });
    linkFileIds?.forEach((id) => {
      linkFileIdsSet.add(id);
    });
    // scenario.value.uploadFileIds = Array.from(uploadFileIdsSet);
    // scenario.value.linkFileIds = Array.from(linkFileIdsSet);
  }
</script>

<style lang="less">
  .scenario-action-dropdown {
    .arco-dropdown-list-wrapper {
      width: 200px;
      max-height: 100%;
      .arco-dropdown-option-content {
        @apply flex w-full;
      }
    }
  }
</style>

<style lang="less" scoped>
  .action-line {
    @apply flex items-center;

    gap: 16px;
    height: 32px;
    .action-group {
      @apply flex items-center;

      gap: 8px;
      .expand-step-btn {
        padding: 4px;
        .arco-icon {
          color: var(--color-text-4);
        }
        &:hover {
          border-color: rgb(var(--primary-5)) !important;
          background-color: rgb(var(--primary-1)) !important;
          .arco-icon {
            color: rgb(var(--primary-5));
          }
        }
      }
    }
  }
</style>
