<template>
  <div class="flex h-full flex-col gap-[8px]">
    <a-spin class="h-full w-full" :loading="loading">
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
            <div class="text-[rgb(var(--primary-5))]">{{ topLevelStepCount }}</div>
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
              <div class="text-[var(--color-text-1)]">{{ scenario.executeTime }}</div>
            </div>
            <a-divider direction="vertical" :margin="8"></a-divider>
            <div class="action-group">
              <div class="text-[var(--color-text-4)]">{{ t('apiScenario.executeResult') }}</div>
              <div class="flex items-center gap-[4px]">
                <div class="text-[var(--color-text-1)]">{{ t('common.success') }}</div>
                <div class="text-[rgb(var(--success-6))]">{{ scenario.executeSuccessCount }}</div>
              </div>
              <div class="flex items-center gap-[4px]">
                <div class="text-[var(--color-text-1)]">{{ t('common.fail') }}</div>
                <div class="text-[rgb(var(--danger-6))]">{{ scenario.executeFailCount }}</div>
              </div>
              <div class="flex items-center gap-[4px]">
                <div class="text-[var(--color-text-1)]">{{ t('common.fakeError') }}</div>
                <div class="text-[rgb(var(--warning-5))]">{{ scenario.executeFakeErrorCount }}</div>
              </div>
              <MsButton
                v-if="
                  scenario.executeType !== 'localExec' &&
                  scenario.isDebug === false &&
                  !scenario.executeLoading &&
                  !scenario.isNew
                "
                type="text"
                class="ml-[8px]"
                @click="checkReport"
              >
                <icon-eye class="mr-[4px] text-[rgb(var(--primary-5))]" />
                {{ t('apiScenario.checkReport') }}
              </MsButton>
            </div>
          </template>
          <div v-if="!checkedAll && !indeterminate" class="action-group ml-auto">
            <!-- <a-input
              v-model:model-value="keyword"
              :placeholder="t('apiScenario.searchByName')"
              allow-clear
              class="w-[200px]"
            /> -->
            <a-tooltip v-if="!scenario.isNew" position="left" :content="t('apiScenario.refreshRefScenario')">
              <a-button type="outline" class="arco-btn-outline--secondary !mr-0 !p-[8px]" @click="refreshStepInfo">
                <template #icon>
                  <icon-refresh class="text-[var(--color-text-4)]" />
                </template>
              </a-button>
            </a-tooltip>
          </div>
        </div>
      </div>
      <div class="h-[calc(100%-30px)]">
        <stepTree
          ref="stepTreeRef"
          v-model:selected-keys="selectedKeys"
          v-model:steps="scenario.steps"
          v-model:checked-keys="checkedKeys"
          v-model:stepKeyword="keyword"
          v-model:scenario="scenario"
          :expand-all="isExpandAll"
          :step-details="scenario.stepDetails"
          @step-add="handleAddStepDone"
          @update-resource="handleUpdateResource"
        />
      </div>
    </a-spin>
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
  <!-- 场景报告抽屉 -->
  <caseAndScenarioReportDrawer
    v-model:visible="showScenarioReportVisible"
    is-scenario
    :report-id="scenario.reportId as string ?? ''"
  />
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import stepTree from './stepTree.vue';
  import caseAndScenarioReportDrawer from '@/views/api-test/components/caseAndScenarioReportDrawer.vue';

  import { getScenarioDetail } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { deleteNodes, filterTree, getGenerateId, mapTree, traverseTree } from '@/utils';
  import { countNodes } from '@/utils/tree';

  import { ApiScenarioDebugRequest, Scenario, ScenarioStepDetails, ScenarioStepItem } from '@/models/apiTest/scenario';
  import { ScenarioExecuteStatus, ScenarioStepRefType, ScenarioStepType } from '@/enums/apiEnum';

  const emit = defineEmits<{
    (e: 'batchDebug', data: Pick<ApiScenarioDebugRequest, 'steps' | 'stepDetails' | 'reportId'>): void;
  }>();

  const { t } = useI18n();
  const { openModal } = useModal();

  const scenario = defineModel<Scenario>('scenario', {
    required: true,
  });
  const scenarioExecuteLoading = inject<Ref<boolean>>('scenarioExecuteLoading');
  const loading = ref(false);

  const checkedAll = ref(false); // 是否全选
  const indeterminate = ref(false); // 是否半选
  const isExpandAll = ref(false); // 是否展开全部
  const checkedKeys = ref<(string | number)[]>([]); // 选中的key
  const selectedKeys = ref<(string | number)[]>([]); // 没啥用，现在用来展示选中样式
  const stepTreeRef = ref<InstanceType<typeof stepTree>>();
  const keyword = ref('');

  const topLevelStepCount = computed(() => scenario.value.steps.length);
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

  function handleAddStepDone() {
    checkedKeys.value = [];
    checkedAll.value = false;
    indeterminate.value = false;
  }

  watch(
    () => scenario.value.id,
    () => {
      checkedKeys.value = [];
      selectedKeys.value = [];
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
          if (ids.has(item.uniqueId)) {
            item.enable = isBatchEnable.value;
          }
          return item;
        });
      } else {
        scenario.value.steps = mapTree(scenario.value.steps, (node) => {
          if (ids.has(node.uniqueId) && node.isRefScenarioStep !== true) {
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
    openModal({
      type: 'error',
      title: t('common.tip'),
      content: t('apiScenario.deleteStepConfirmWithChildren'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        const deleteResult = deleteNodes(
          scenario.value.steps,
          checkedKeys.value,
          (node) => !node.isQuoteScenarioStep,
          (node) => {
            delete scenario.value.stepDetails[node.id];
          },
          'uniqueId'
        );
        if (deleteResult) {
          Message.success(t('common.deleteSuccess'));
          checkedKeys.value = [];
          if (scenario.value.steps.length === 0) {
            checkedAll.value = false;
            indeterminate.value = false;
          }
        } else {
          Message.warning(t('apiScenario.quoteScenarioStepNotAllowDelete'));
        }
      },
      hideCancel: false,
    });
  }

  const showScenarioReportVisible = ref(false);
  function checkReport() {
    showScenarioReportVisible.value = true;
  }

  /**
   * 刷新引用场景的步骤数据
   */
  async function refreshStepInfo() {
    try {
      loading.value = true;
      if (scenario.value.id) {
        const res = await getScenarioDetail(scenario.value.id);
        const refScenarioMap = new Map<string, ScenarioStepItem>();
        traverseTree(
          res.steps,
          (node) => {
            if (
              node.stepType === ScenarioStepType.API_SCENARIO &&
              [ScenarioStepRefType.REF, ScenarioStepRefType.PARTIAL_REF].includes(node.refType)
            ) {
              // 是引用的场景就存储起来
              refScenarioMap.set(node.id, node as ScenarioStepItem);
            }
          },
          (node) => {
            // 是引用的场景就没必要再递归子孙节点了
            return (
              node.stepType !== ScenarioStepType.API_SCENARIO &&
              [ScenarioStepRefType.REF, ScenarioStepRefType.PARTIAL_REF].includes(node.refType)
            );
          }
        );
        scenario.value.steps = mapTree(scenario.value.steps, (node) => {
          const newStep = refScenarioMap.get(node.id);
          if (newStep) {
            node = {
              ...cloneDeep(node), // 避免前端初始化的东西被丢弃
              ...newStep,
              uniqueId: node.uniqueId, // 保留原有的唯一标识
            };
            node.children = mapTree(newStep.children || [], (child) => {
              if (
                node.stepType === ScenarioStepType.API_SCENARIO &&
                [ScenarioStepRefType.REF, ScenarioStepRefType.PARTIAL_REF].includes(node.refType)
              ) {
                // 如果根节点是引用场景
                child.isQuoteScenarioStep = true; // 标记为引用场景下的子步骤
                child.isRefScenarioStep = node.refType === ScenarioStepRefType.REF; // 标记为完全引用场景
                child.draggable = false; // 引用场景下的任何步骤不可拖拽
              } else if (child.parent) {
                // 如果有父节点
                child.isQuoteScenarioStep = child.parent.isQuoteScenarioStep; // 复用父节点的引用场景标记
                child.isRefScenarioStep = child.parent.isRefScenarioStep; // 复用父节点的是否完全引用场景标记
                child.draggable = !node.parent.isQuoteScenarioStep; // 引用场景下的任何步骤不可拖拽
              }
              if (selectedKeys.value.includes(node.uniqueId) && !selectedKeys.value.includes(child.uniqueId)) {
                // 如果有新增的子步骤，且当前步骤被选中，则这个新增的子步骤也要选中
                selectedKeys.value.push(child.uniqueId);
              }
              child.uniqueId = getGenerateId();
              return child;
            }) as ScenarioStepItem[];
          }
          return node;
        });
        Message.success(t('apiScenario.updateRefScenarioSuccess'));
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  function batchDebug() {
    scenario.value.executeLoading = true;
    const checkedKeysSet = new Set(checkedKeys.value);
    const waitTingDebugSteps = filterTree(scenario.value.steps, (node) => {
      if (checkedKeysSet.has(node.uniqueId)) {
        if (!node.enable) {
          // 如果步骤未开启，则删除已选 uniqueId，方便下面waitingDebugStepDetails详情判断是否携带
          checkedKeysSet.delete(node.uniqueId);
          node.executeStatus = ScenarioExecuteStatus.UN_EXECUTE; // 已选中但未开启的步骤显示未执行状态
        } else {
          node.executeStatus = ScenarioExecuteStatus.EXECUTING; // 已选中且已开启的步骤显示执行中状态
        }
        return !!node.enable;
      }
      node.executeStatus = undefined; // 未选中的步骤不显示执行状态
      if (node.children && node.children.length > 0) {
        return true; // 因为存在选中了子步骤但是没有选中父步骤，所以需要递归完整树
      }
      return false;
    });
    const waitingDebugStepDetails: Record<string, ScenarioStepDetails> = {};
    Object.keys(scenario.value.stepDetails).forEach((key) => {
      if (checkedKeysSet.has(key)) {
        waitingDebugStepDetails[key] = scenario.value.stepDetails[key];
      }
    });
    emit('batchDebug', {
      steps: waitTingDebugSteps as ScenarioStepItem[],
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

    margin-bottom: 8px;
    height: 32px;
    gap: 16px;
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
