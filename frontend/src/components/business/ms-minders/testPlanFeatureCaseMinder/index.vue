<template>
  <div class="h-full">
    <MsMinderEditor
      v-model:activeExtraKey="activeExtraKey"
      v-model:extra-visible="extraVisible"
      v-model:loading="loading"
      v-model:import-json="importJson"
      :minder-key="MinderKeyEnum.TEST_PLAN_FEATURE_CASE_MINDER"
      :extract-content-tab-list="extractContentTabList"
      :can-show-float-menu="canShowFloatMenu"
      :can-show-priority-menu="false"
      :can-show-more-menu="canShowMoreMenu"
      :can-show-enter-node="canShowEnterNode"
      :can-show-more-menu-node-operation="false"
      :more-menu-other-operation-list="canShowFloatMenu && hasOperationPermission ? moreMenuOtherOperationList : []"
      :shortcut-list="['expand', 'enter']"
      disabled
      @content-change="handleMinderNodeContentChange"
      @node-batch-select="handleNodeBatchSelect"
      @node-select="handleNodeSelect"
      @node-unselect="handleNodeUnselect"
    >
      <template #extractMenu>
        <!-- 缺陷 -->
        <a-dropdown trigger="hover" position="bl">
          <MsButton
            v-if="
              props.canEdit &&
              showAssociateBugMenu &&
              hasAllPermission(['PROJECT_BUG:READ', 'PROJECT_TEST_PLAN:READ+EXECUTE'])
            "
            type="icon"
            class="ms-minder-node-float-menu-icon-button"
          >
            <MsIcon type="icon-icon_bug" class="text-[var(--color-text-4)]" />
          </MsButton>
          <template #content>
            <a-doption v-permission="['PROJECT_BUG:READ+ADD']" value="new" @click="showAddDefectDrawer = true">
              {{ t('testPlan.featureCase.noBugDataNewBug') }}
            </a-doption>
            <a-doption v-permission="['PROJECT_BUG:READ']" value="link" @click="showLinkDefectDrawer = true">
              {{ t('caseManagement.featureCase.linkDefect') }}
            </a-doption>
          </template>
        </a-dropdown>
        <!-- 执行 -->
        <a-trigger
          v-model:popup-visible="executeVisible"
          trigger="click"
          position="bl"
          :click-outside-to-close="false"
          popup-container="body"
        >
          <MsButton
            v-if="props.canEdit && hasAnyPermission(['PROJECT_TEST_PLAN:READ+EXECUTE'])"
            type="icon"
            :class="[
              'ms-minder-node-float-menu-icon-button',
              `${executeVisible ? 'ms-minder-node-float-menu-icon-button--focus' : ''}`,
            ]"
          >
            <MsIcon type="icon-icon_play-round_filled" class="text-[var(--color-text-4)]" />
          </MsButton>
          <template #content>
            <div class="w-[440px] rounded bg-[var(--color-text-fff)] p-[16px] shadow-[0_0_10px_rgba(0,0,0,0.05)]">
              <ExecuteSubmit
                :select-node="selectNode"
                :tree-type="props.treeType"
                :test-plan-id="props.planId"
                is-default-activate
                @done="handleExecuteDone"
              />
            </div>
          </template>
        </a-trigger>
        <!-- 查看详情 -->
        <MsButton
          v-if="canShowDetail"
          type="icon"
          :class="[
            'ms-minder-node-float-menu-icon-button',
            `${extraVisible ? 'ms-minder-node-float-menu-icon-button--focus' : ''}`,
          ]"
          @click="toggleDetail"
        >
          <MsIcon type="icon-icon_describe_outlined" class="text-[var(--color-text-4)]" />
        </MsButton>
      </template>
      <template #extractTabContent>
        <MsDescription
          v-if="activeExtraKey === 'baseInfo'"
          :loading="baseInfoLoading"
          :descriptions="descriptions"
          label-width="90px"
          class="pl-[16px]"
        />
        <Attachment
          v-else-if="activeExtraKey === 'attachment'"
          v-model:model-value="fileList"
          not-show-add-button
          disabled
          :active-case="activeCaseInfo"
        />
        <BugList
          v-else-if="activeExtraKey === 'bug'"
          ref="bugListRef"
          :active-case="activeCaseInfo"
          :test-plan-case-id="selectNode?.data?.id"
          :show-disassociate-button="props.canEdit && hasAnyPermission(['PROJECT_TEST_PLAN:READ+EXECUTE'])"
          @disassociate-bug-done="emit('refreshPlan')"
        />
        <ReviewCommentList
          v-else
          class="pl-[16px]"
          :review-comment-list="executeHistoryList"
          active-comment="executiveComment"
          not-show-review-name
          show-step-detail-trigger
        />
      </template>
      <template v-if="canShowBatchAddDefect" #batchMenu>
        <a-dropdown trigger="hover" position="bl">
          <MsButton
            v-if="props.canEdit && hasAllPermission(['PROJECT_BUG:READ', 'PROJECT_TEST_PLAN:READ+EXECUTE'])"
            type="icon"
            class="ms-minder-node-float-menu-icon-button"
          >
            <MsIcon type="icon-icon_bug" class="text-[rgb(var(--danger-6))]" />
          </MsButton>
          <template #content>
            <a-doption v-permission="['PROJECT_BUG:READ+ADD']" value="new" @click="showBatchAddDefect">
              {{ t('testPlan.featureCase.noBugDataNewBug') }}
            </a-doption>
            <a-doption v-permission="['PROJECT_BUG:READ']" value="link" @click="showBatchLinkDefect">
              {{ t('caseManagement.featureCase.linkDefect') }}
            </a-doption>
          </template>
        </a-dropdown>
      </template>
      <template #shortCutList>
        <div class="ms-minder-shortcut-trigger-listitem">
          <div>{{ t('common.success') }}</div>
          <div class="ms-minder-shortcut-trigger-listitem-icon ms-minder-shortcut-trigger-listitem-icon-auto"> S </div>
        </div>
        <div class="ms-minder-shortcut-trigger-listitem">
          <div>{{ t('common.fail') }}</div>
          <div class="ms-minder-shortcut-trigger-listitem-icon ms-minder-shortcut-trigger-listitem-icon-auto"> E </div>
        </div>
        <div class="ms-minder-shortcut-trigger-listitem">
          <div>{{ t('common.block') }}</div>
          <div class="ms-minder-shortcut-trigger-listitem-icon ms-minder-shortcut-trigger-listitem-icon-auto"> B </div>
        </div>
      </template>
    </MsMinderEditor>
    <LinkDefectDrawer
      v-if="isMinderOperation"
      v-model:visible="showLinkDefectDrawer"
      :case-id="selectNode?.data?.caseId ?? ''"
      :drawer-loading="linkDrawerLoading"
      :load-api="AssociatedBugApiTypeEnum.BUG_TOTAL_LIST"
      :show-selector-all="false"
      @save="associateSuccessHandler"
    />
    <LinkDefectDrawer
      v-else
      v-model:visible="showLinkDefectDrawer"
      :case-id="selectNode?.data?.caseId ?? ''"
      :drawer-loading="linkDrawerLoading"
      :load-api="AssociatedBugApiTypeEnum.TEST_PLAN_BUG_LIST"
      :show-selector-all="false"
      @save="associateSuccessHandler"
    />
    <AddDefectDrawer
      v-model:visible="showAddDefectDrawer"
      :case-id="selectNode?.data?.id ?? ''"
      :extra-params="{
        testPlanCaseId: selectNode?.data?.id,
        caseId: selectNode?.data?.caseId,
        testPlanId: props.planId,
        selectAll:
          batchMinderParams.minderModuleIds.includes('NONE') || batchMinderParams.minderCollectionIds.includes('NONE'),
        ...batchMinderParams,
      }"
      :is-minder-batch="isMinderOperation"
      @success="handleAddBugDone"
    />
    <a-modal
      v-model:visible="stepExecuteModelVisible"
      :title="t('common.executionResult')"
      class="p-[4px]"
      title-align="start"
      body-class="p-0"
      :width="800"
      :cancel-button-props="{ disabled: submitStepExecuteLoading }"
      :ok-loading="submitStepExecuteLoading"
      :ok-text="t('caseManagement.caseReview.commitResult')"
      @before-ok="submitStepExecute"
      @cancel="cancelStepExecute"
    >
      <AddStep
        v-model:step-list="stepData"
        is-scroll-y
        is-test-plan
        :scroll-y="190"
        :is-disabled-test-plan="false"
        is-disabled
      />
      <ExecuteForm v-model:form="executeForm" class="mt-[24px]" rich-text-max-height="150px" not-rich-auto-focus />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDescription, { Description } from '@/components/pure/ms-description/index.vue';
  import useShortCut from '@/components/pure/ms-minder-editor/hooks/useShortCut';
  import MsMinderEditor from '@/components/pure/ms-minder-editor/minderEditor.vue';
  import type { MinderJson, MinderJsonNode, MinderJsonNodeData } from '@/components/pure/ms-minder-editor/props';
  import {
    clearNodeChildren,
    clearSelectedNodes,
    createNode,
    expendNodeAndChildren,
    handleRenderNode,
    removeFakeNode,
    renderSubModules,
    renderSubNodes,
    setPriorityView,
  } from '@/components/pure/ms-minder-editor/script/tool/utils';
  import { MsFileItem } from '@/components/pure/ms-upload/types';
  import {
    getMinderOperationParams,
    isModuleOrCollection,
  } from '@/components/business/ms-minders/caseReviewMinder/utils';
  import Attachment from '@/components/business/ms-minders/featureCaseMinder/attachment.vue';
  import useMinderBaseApi from '@/components/business/ms-minders/featureCaseMinder/useMinderBaseApi';
  import BugList from './bugList.vue';
  import AddStep from '@/views/case-management/caseManagementFeature/components/addStep.vue';
  import ReviewCommentList from '@/views/case-management/caseManagementFeature/components/tabContent/tabComment/reviewCommentList.vue';
  import AddDefectDrawer from '@/views/case-management/components/addDefectDrawer/index.vue';
  import LinkDefectDrawer from '@/views/case-management/components/linkDefectDrawer.vue';
  import ExecuteForm from '@/views/test-plan/testPlan/detail/featureCase/components/executeForm.vue';
  import ExecuteSubmit from '@/views/test-plan/testPlan/detail/featureCase/detail/executeSubmit.vue';

  import { getCasePlanMinder } from '@/api/modules/case-management/caseReview';
  import {
    associateBugToPlan,
    batchAssociatedBugToMinderCase,
    batchExecuteCase,
    executeHistory,
    getCaseDetail,
    runFeatureCase,
  } from '@/api/modules/test-plan/testPlan';
  import { defaultExecuteForm } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import useMinderStore from '@/store/modules/components/minder-editor/index';
  import useTestPlanFeatureCaseStore from '@/store/modules/testPlan/testPlanFeatureCase';
  import { findNodeByKey, getGenerateId, mapTree, replaceNodeInTree } from '@/utils';
  import { hasAllPermission, hasAnyPermission } from '@/utils/permission';

  import type { StepList } from '@/models/caseManagement/featureCase';
  import type { TableQueryParams } from '@/models/common';
  import { ModuleTreeNode } from '@/models/common';
  import type {
    BatchExecuteFeatureCaseParams,
    ExecuteFeatureCaseFormParams,
    ExecuteHistoryItem,
  } from '@/models/testPlan/testPlan';
  import { AssociatedBugApiTypeEnum } from '@/enums/associateBugEnum';
  import { LastExecuteResults } from '@/enums/caseEnum';
  import { MinderEventName, MinderKeyEnum } from '@/enums/minderEnum';

  import {
    convertToFile,
    executionResultMap,
    getCustomField,
  } from '@/views/case-management/caseManagementFeature/components/utils';

  const props = defineProps<{
    activeModule: string;
    moduleTree: ModuleTreeNode[];
    planId: string;
    canEdit: boolean; // 已归档的测试计划不能操作
    treeType: 'MODULE' | 'COLLECTION';
    tableSorter: Record<string, any>;
  }>();

  const emit = defineEmits<{
    (e: 'operation', type: string, node: MinderJsonNode): void;
    (e: 'refreshPlan'): void;
  }>();

  const { t } = useI18n();
  const appStore = useAppStore();
  const minderStore = useMinderStore();
  const testPlanFeatureCaseStore = useTestPlanFeatureCaseStore();

  const { moduleTag, stepTag, stepExpectTag } = useMinderBaseApi({});
  const actualResultTag = t('system.orgTemplate.actualResult');
  const importJson = ref<MinderJson>({
    root: {} as MinderJsonNode,
    treePath: [],
  });
  const loading = ref(false);
  const modulesCount = computed(() => testPlanFeatureCaseStore.modulesCount);

  /**
   * 找到最顶层的父节点id
   * @param node 选中节点
   */
  function getMinderNodeParentId(node: MinderJsonNode): string {
    while (node?.parent && node.parent.data?.id !== 'NONE') {
      node = node.parent;
    }
    return node.id ?? '';
  }

  /**
   * 初始化用例模块树
   */
  async function initCaseTree() {
    const tree = mapTree<MinderJsonNode>(props.moduleTree, (e) => ({
      ...e,
      data: {
        ...e.data,
        type: e.type || e.data?.type,
        id: e.id || e.data?.id || '',
        text: e.name || e.data?.text.replace(/<\/?p\b[^>]*>/gi, '') || '',
        resource:
          modulesCount.value[e.id] !== undefined && props.treeType === 'MODULE' ? [moduleTag] : e.data?.resource,
        expandState: e.level === 0 ? 'expand' : 'collapse',
        count: modulesCount.value[e.id],
        disabled: true,
        projectId: props.treeType === 'MODULE' ? getMinderNodeParentId(e) : appStore.currentProjectId,
        isModuleOrCollection: true, // 模块或测试点
      },
      children:
        modulesCount.value[e.id] > 0 && !e.children?.length
          ? [
              {
                data: {
                  id: 'fakeNode',
                  text: t('ms.minders.moreCase'),
                  resource: [''],
                },
              },
            ]
          : e.children,
    }));
    importJson.value.root = {
      children: tree,
      data: {
        id: 'NONE',
        text: t('testPlan.testPlanIndex.functionalUseCase'),
        resource: props.treeType === 'MODULE' ? [moduleTag] : [],
        disabled: true,
        count: modulesCount.value.all,
        isModuleOrCollection: true, // 模块或测试点
      },
    };
    importJson.value.treePath = [];
    clearSelectedNodes();
    if (props.activeModule !== 'all') {
      // 携带具体的模块 ID 加载时，进入该模块内
      nextTick(() => {
        minderStore.dispatchEvent(MinderEventName.ENTER_NODE, undefined, undefined, undefined, [
          findNodeByKey(importJson.value.root.children || [], props.activeModule, 'id', 'data') as MinderJsonNode,
        ]);
      });
    } else {
      // 刷新时不需要重新请求数据，进入根节点
      nextTick(() => {
        minderStore.dispatchEvent(MinderEventName.ENTER_NODE, undefined, undefined, undefined, [importJson.value.root]);
      });
    }
  }

  watch([() => props.activeModule, () => props.treeType], () => {
    initCaseTree();
  });

  function isCaseTag(data?: MinderJsonNodeData) {
    return data?.caseId?.length;
  }

  /**
   * 加载模块节点下的用例节点
   * @param node 选中节点
   * @param loadMoreCurrent 加载模块下更多用例时的当前页码
   */
  async function initNodeCases(node: MinderJsonNode, loadMoreCurrent?: number) {
    try {
      loading.value = true;
      if (!node?.data) return;
      const { list, total } = await getCasePlanMinder(props.treeType, {
        current: (loadMoreCurrent ?? 0) + 1,
        ...(props.treeType === 'COLLECTION' ? { collectionId: node.data?.id } : { moduleId: node.data?.id }),
        projectId: node.data?.projectId,
        planId: props.planId,
        sort: props.tableSorter,
      });
      // 移除占位的虚拟节点
      removeFakeNode(node, loadMoreCurrent ? `tmp-${node.data?.id}` : 'fakeNode');
      // 如果模块下没有用例且有别的模块节点，正常展开
      if ((!list || list.length === 0) && node.children?.length && !loadMoreCurrent) {
        node.expand();
        handleRenderNode(node, node.children);
        return;
      }

      // 渲染节点
      let waitingRenderNodes: MinderJsonNode[] = [];
      list.forEach((e: MinderJsonNode) => {
        // 用例节点
        const child = createNode(
          {
            ...(e.data as MinderJsonNodeData),
            text: e.data?.text.replace(/<\/?p\b[^>]*>/gi, '') || '',
            resource: [
              ...(executionResultMap[e.data?.status]?.statusText
                ? [executionResultMap[e.data?.status].statusText]
                : []),
              ...(e.data?.resource ?? []),
            ],
          },
          node
        );
        waitingRenderNodes.push(child);
        // 前置/步骤/备注/预期结果节点
        const grandChildren = renderSubNodes(child, e.children);
        window.minder.renderNodeBatch(grandChildren);
      });

      node.expand();
      if (node.children && node.children.length > 0) {
        waitingRenderNodes = waitingRenderNodes.concat(node.children);
      }
      // 更多用例节点
      if (total > 100 * ((loadMoreCurrent ?? 0) + 1)) {
        const moreNode = window.minder.createNode(
          {
            id: `tmp-${node.data?.id}`,
            text: t('ms.minders.moreCase'),
            type: 'tmp',
            expandState: 'collapse',
            current: (loadMoreCurrent ?? 0) + 1,
            disabled: true,
          },
          node
        );
        waitingRenderNodes.push(moreNode);
      }
      handleRenderNode(node, waitingRenderNodes);
      // 加载完用例数据后，更新当前importJson数据
      replaceNodeInTree([importJson.value.root], node.data?.id || '', window.minder.exportNode(node), 'data', 'id');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  const extraVisible = ref<boolean>(false);
  const activeExtraKey = ref<'baseInfo' | 'attachment' | 'history' | 'bug'>('history');
  const baseInfoLoading = ref(false);
  const activeCaseInfo = ref<Record<string, any>>({});
  const descriptions = ref<Description[]>([]);
  const fileList = ref<MsFileItem[]>([]);
  const extractContentTabList = [
    {
      value: 'baseInfo',
      label: t('common.baseInfo'),
    },
    {
      value: 'attachment',
      label: t('caseManagement.featureCase.attachment'),
    },
    {
      value: 'bug',
      label: t('testPlan.featureCase.bug'),
    },
    {
      value: 'history',
      label: t('testPlan.featureCase.executionHistory'),
    },
  ];
  function resetExtractInfo() {
    activeCaseInfo.value = {};
    fileList.value = [];
  }

  /**
   * 初始化用例详情
   * @param data 节点数据
   */
  async function initCaseDetail(data: MinderJsonNodeData) {
    try {
      baseInfoLoading.value = true;
      const res = await getCaseDetail(data?.id || activeCaseInfo.value.id);
      activeCaseInfo.value = res;
      // 基本信息
      descriptions.value = [
        {
          label: t('caseManagement.caseReview.caseName'),
          value: res.name,
        },
        {
          label: t('common.tag'),
          value: res.tags,
          isTag: true,
        },
        // 解析用例模板的自定义字段
        ...res.customFields.map((e: Record<string, any>) => {
          try {
            return {
              label: e.fieldName,
              value: getCustomField(e),
            };
          } catch (error) {
            return {
              label: e.fieldName,
              value: e.defaultValue,
            };
          }
        }),
      ].map((item) => ({ ...item, tooltipPosition: 'tr' }));
      // 附件文件
      if (activeCaseInfo.value.attachments) {
        fileList.value = activeCaseInfo.value.attachments
          .map((fileInfo: any) => {
            return {
              ...fileInfo,
              name: fileInfo.fileName,
            };
          })
          .map((fileInfo: any) => {
            return convertToFile(fileInfo);
          });
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      baseInfoLoading.value = false;
    }
  }
  // 执行历史
  const executeHistoryList = ref<ExecuteHistoryItem[]>([]);
  async function initExecuteHistory(data: MinderJsonNodeData) {
    try {
      const res = await executeHistory({
        caseId: data?.caseId,
        id: data.id,
        testPlanId: props.planId,
      });
      executeHistoryList.value = res.map((item) => ({ ...item, stepsText: item.stepsExecResult }));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  /**
   * 切换用例详情显示
   */
  async function toggleDetail(val?: boolean) {
    extraVisible.value = val !== undefined ? val : !extraVisible.value;
    const node: MinderJsonNode = window.minder.getSelectedNode();
    const { data } = node;
    if (extraVisible.value && data && isCaseTag(data)) {
      activeExtraKey.value = 'history';
      initExecuteHistory(data);
      initCaseDetail(data);
    }
  }

  const selectNode = ref();

  // 添加缺陷
  const showLinkDefectDrawer = ref(false);
  const showAddDefectDrawer = ref(false);
  const linkDrawerLoading = ref(false);
  const bugListRef = ref<InstanceType<typeof BugList>>();
  const batchMinderParams = ref({
    minderModuleIds: [] as string[],
    minderCaseIds: [] as string[],
    minderCollectionIds: [] as string[],
    minderProjectIds: [] as string[],
  });
  const isMinderOperation = ref(false);

  function handleAddBugDone() {
    if (extraVisible.value && activeExtraKey.value === 'bug') {
      bugListRef.value?.loadBugList();
    }
    emit('refreshPlan');
  }

  async function associateSuccessHandler(params: TableQueryParams) {
    try {
      linkDrawerLoading.value = true;
      if (isMinderOperation.value) {
        await batchAssociatedBugToMinderCase({
          ...params,
          testPlanCaseId: '',
          caseId: '',
          testPlanId: props.planId,
          bugIds: params.selectIds,
          selectAll:
            batchMinderParams.value.minderModuleIds.includes('NONE') ||
            batchMinderParams.value.minderCollectionIds.includes('NONE'),
          ...batchMinderParams.value,
        });
      } else {
        await associateBugToPlan({
          ...params,
          testPlanCaseId: selectNode.value.data?.id,
          caseId: selectNode.value.data?.caseId,
          testPlanId: props.planId,
        });
      }
      Message.success(t('caseManagement.featureCase.associatedSuccess'));
      linkDrawerLoading.value = false;
      handleAddBugDone();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      linkDrawerLoading.value = false;
    }
  }

  // 执行
  const executeVisible = ref(false);
  // 更新用例的实际结果节点
  function updateCaseActualResultNode(node: MinderJsonNode, content: string) {
    let actualResultNode = node.children?.find((item: MinderJsonNode) =>
      item.data?.resource?.includes(actualResultTag)
    );
    if (actualResultNode) {
      if (content.length) {
        actualResultNode.setData('text', content.replace(/<\/?p\b[^>]*>/gi, '')).render();
      } else {
        // 删除实际结果节点
        window.minder.removeNode(actualResultNode);
      }
    } else if (content.length) {
      actualResultNode = createNode(
        {
          resource: [actualResultTag],
          text: content.replace(/<\/?p\b[^>]*>/gi, ''),
          id: `actualResult-${node.data?.id}`,
        },
        node
      );
      handleRenderNode(node, [actualResultNode]);
    }
  }

  function isActualResultNode(node: MinderJsonNode) {
    return node.data?.resource?.includes(actualResultTag) && isCaseTag(node.parent?.data);
  }

  // 点击模块/用例/用例的实际结果执行
  async function handleExecuteDone(status: LastExecuteResults, content: string) {
    const curSelectNode = window.minder.getSelectedNode();
    const node = isActualResultNode(curSelectNode) ? curSelectNode.parent : curSelectNode;
    executeVisible.value = false;
    if (isCaseTag(node.data)) {
      //  用例添加标签
      node.setData('resource', [executionResultMap[status].statusText]).render();
      // 更新用例的实际结果节点
      updateCaseActualResultNode(node, content);
      // 更新执行历史
      if (extraVisible.value && activeExtraKey.value === 'history') {
        initExecuteHistory(node.data);
      }
      window.minder.refresh();
    } else if (node.data.id === 'NONE') {
      // 处理根节点，重新渲染整个用例树
      await initCaseTree();
    } else if (isModuleOrCollection(node.data)) {
      // TODO 想一个更好的处理方案
      // 处理模块节点
      // 删除模块下所有子节点
      clearNodeChildren(node);
      // 重新渲染子模块，子模块都收起
      renderSubModules(node, importJson.value.root, modulesCount.value);
      // 重新渲染用例
      await initNodeCases(node);
    }
    setPriorityView(true, 'P');
    emit('refreshPlan');
  }

  async function handleShortCutExecute(status: LastExecuteResults) {
    const selectedNodes: MinderJsonNode = window.minder.getSelectedNode();
    if (!isCaseTag(selectedNodes?.data)) return;
    try {
      await batchExecuteCase({
        projectId: appStore.currentProjectId,
        testPlanId: props.planId,
        lastExecResult: status,
        content: '',
        ...getMinderOperationParams(selectedNodes, props.treeType === 'COLLECTION'),
      } as BatchExecuteFeatureCaseParams);
      // 更新
      handleExecuteDone(status, '');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const stepExecuteModelVisible = ref(false);
  const caseNodeAboveSelectStep = ref(); // 选中的步骤节点对应的用例节点信息
  const submitStepExecuteLoading = ref(false);
  const executeForm = ref<ExecuteFeatureCaseFormParams>({ ...defaultExecuteForm });
  const stepData = ref<StepList[]>([
    {
      id: getGenerateId(),
      step: '',
      expected: '',
      showStep: false,
      showExpected: false,
    },
  ]);
  async function getStepData(id: string) {
    try {
      const res = await getCaseDetail(id);
      if (res.steps) {
        stepData.value = JSON.parse(res.steps).map((item: any) => {
          return {
            id: item.id,
            step: item.desc,
            expected: item.result,
            actualResult: item.actualResult,
            executeResult: item.executeResult,
          };
        });
      } else {
        stepData.value = [];
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
  watch(
    () => stepData.value,
    () => {
      const executionResultList = stepData.value?.map((item) => item.executeResult);
      if (executionResultList?.includes(LastExecuteResults.ERROR)) {
        executeForm.value.lastExecResult = LastExecuteResults.ERROR;
      } else if (executionResultList?.includes(LastExecuteResults.BLOCKED)) {
        executeForm.value.lastExecResult = LastExecuteResults.BLOCKED;
      } else {
        executeForm.value.lastExecResult = LastExecuteResults.SUCCESS;
      }
    },
    { deep: true }
  );
  function cancelStepExecute() {
    executeForm.value = { ...defaultExecuteForm };
  }
  /**
   * 步骤/用例执行后 更新脑图数据
   * @param status 用例执行状态
   * @param content 用例实际结果内容
   */
  function submitStepExecuteDone(status: string, content: string) {
    // 用例更新标签
    caseNodeAboveSelectStep.value.setData('resource', [executionResultMap[status].statusText]).render();
    // 更新用例的实际结果节点
    updateCaseActualResultNode(toRaw(caseNodeAboveSelectStep.value), content);
    // 更新步骤数据：标签和实际结果
    caseNodeAboveSelectStep.value.children.forEach((child: MinderJsonNode) => {
      const step = stepData.value.find((item) => item.id === child.data?.id);
      if (step?.executeResult?.length) {
        child.setData('resource', [executionResultMap[step?.executeResult].statusText, stepTag]).render();
      }
      if (step?.actualResult?.length) {
        child.children?.[0].children?.[0].setData('text', step?.actualResult).render();
      }
    });
    caseNodeAboveSelectStep.value.layout();
    setPriorityView(true, 'P');
  }
  /**
   * 步骤/用例执行
   */
  async function submitStepExecute() {
    try {
      submitStepExecuteLoading.value = true;
      const params = {
        projectId: appStore.currentProjectId,
        testPlanId: props.planId,
        caseId: caseNodeAboveSelectStep.value.data.caseId,
        id: caseNodeAboveSelectStep.value.data.id,
        ...executeForm.value,
        notifier: executeForm.value?.commentIds?.join(';'),
        stepsExecResult: JSON.stringify(
          stepData.value.map((item, index) => {
            return {
              id: item.id,
              num: index,
              desc: item.step,
              result: item.expected,
              actualResult: item.actualResult,
              executeResult: item.executeResult,
            };
          })
        ),
      };
      await runFeatureCase(params);
      stepExecuteModelVisible.value = false;
      Message.success(t('common.updateSuccess'));
      cancelStepExecute();
      emit('refreshPlan');
      submitStepExecuteDone(params.lastExecResult, params.content ?? '');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      submitStepExecuteLoading.value = false;
    }
  }

  // 菜单显隐
  const hasOperationPermission = computed(
    () => hasAnyPermission(['PROJECT_TEST_PLAN:READ+UPDATE', 'PROJECT_TEST_PLAN:READ+ASSOCIATION']) && props.canEdit
  );
  const canShowFloatMenu = ref(false); // 是否展示浮动菜单
  const canShowMoreMenu = ref(false); // 更多
  const canShowEnterNode = ref(false);
  const showAssociateBugMenu = ref(false);
  const canShowDetail = ref(false);
  const moreMenuOtherOperationList = ref();
  function setMoreMenuOtherOperationList(node: MinderJsonNode) {
    moreMenuOtherOperationList.value = [
      {
        value: 'changeExecutor',
        label: t('testPlan.featureCase.changeExecutor'),
        permission: ['PROJECT_TEST_PLAN:READ+UPDATE'],
        onClick: () => {
          emit('operation', 'changeExecutor', node);
        },
      },
      {
        value: 'disassociate',
        label: t('caseManagement.caseReview.disassociateCase'),
        permission: ['PROJECT_TEST_PLAN:READ+ASSOCIATION'],
        onClick: () => {
          emit('operation', 'disassociate', node);
        },
      },
    ];
  }

  /**
   * 获取步骤节点对应的用例节点
   * @param node 选中节点
   * @param resource 标签
   */
  function getCaseNodeWithResource(node: MinderJsonNode, resource: string) {
    while (node.parent) {
      if (node?.data?.resource?.includes(resource)) {
        return node.parent;
      }
      if (isCaseTag(node.data)) {
        return null;
      }
      node = node.parent;
    }
    return null;
  }

  function setBatchMinderParams() {
    batchMinderParams.value = {
      minderCollectionIds: [],
      minderModuleIds: [],
      minderCaseIds: [],
      minderProjectIds: [],
    };
    const selectedNodes: MinderJsonNode[] = window.minder.getSelectedNodes();
    selectedNodes.forEach((node) => {
      if (isCaseTag(node?.data)) {
        batchMinderParams.value.minderCaseIds.push(node.data?.id || '');
      } else if (node.data?.type === 'PROJECT') {
        batchMinderParams.value.minderProjectIds.push(node.data?.id || '');
      } else if (isModuleOrCollection(node.data)) {
        if (props.treeType === 'COLLECTION') {
          batchMinderParams.value.minderCollectionIds.push(node.data?.id || '');
        } else {
          batchMinderParams.value.minderModuleIds.push(node.data?.id || '');
        }
      }
    });
  }

  const isContentChanging = ref(false);
  function handleMinderNodeContentChange() {
    isContentChanging.value = true;
    setTimeout(() => {
      isContentChanging.value = false;
    }, 300); // 300ms 是脑图编辑器的 debounce 时间，300ms 后触发选中事件
  }

  // 选中节点
  async function handleNodeSelect(node: MinderJsonNode) {
    const { data } = node;
    if (isModuleOrCollection(node.data)) {
      isMinderOperation.value = true; // 批量操作/脑图模块节点操作
      setBatchMinderParams();
    } else {
      isMinderOperation.value = false;
    }
    // 点击更多节点，加载更多用例
    if (data?.type === 'tmp' && node.parent && isModuleOrCollection(node.parent?.data)) {
      canShowFloatMenu.value = false;
      await initNodeCases(node.parent, data.current);
      setPriorityView(true, 'P');
      return;
    }
    selectNode.value = isActualResultNode(node) ? node.parent : node;

    // 展示浮动菜单: 模块节点且有子节点且不是没权限的根结点、用例节点、用例节点下的实际结果
    if (
      isCaseTag(node?.data) ||
      (isModuleOrCollection(node.data) &&
        (node.children || []).length > 0 &&
        !(!hasOperationPermission.value && node.type === 'root'))
    ) {
      canShowFloatMenu.value = true;
      setMoreMenuOtherOperationList(node);
    } else if (isActualResultNode(node) && props.canEdit && hasAnyPermission(['PROJECT_TEST_PLAN:READ+EXECUTE'])) {
      canShowFloatMenu.value = true;
    } else {
      canShowFloatMenu.value = false;
    }

    // 点步骤描述下的【步骤描述/预期结果/实际结果】标签
    if (
      [actualResultTag, stepTag, stepExpectTag].some((item) => node.data?.resource?.includes(item)) &&
      props.canEdit &&
      hasAnyPermission(['PROJECT_TEST_PLAN:READ+EXECUTE'])
    ) {
      caseNodeAboveSelectStep.value = getCaseNodeWithResource(node, stepTag);
      if (caseNodeAboveSelectStep.value?.data?.id) {
        getStepData(caseNodeAboveSelectStep.value.data.id);
        stepExecuteModelVisible.value = true;
        return;
      }
    }

    // 不展示更多：没操作权限的用例、用例节点下的实际结果
    if ((isCaseTag(node?.data) && !hasOperationPermission.value) || isActualResultNode(node)) {
      canShowMoreMenu.value = false;
    } else {
      canShowMoreMenu.value = true;
    }

    // 展示进入节点菜单: 模块节点
    if (isModuleOrCollection(data) && (node.children || []).length > 0 && node.type !== 'root') {
      canShowEnterNode.value = true;
    } else {
      canShowEnterNode.value = false;
    }

    executeVisible.value = false;

    if (isCaseTag(data)) {
      canShowDetail.value = true;
      showAssociateBugMenu.value = true;
      if (extraVisible.value) {
        toggleDetail(true);
      }
      // 点击展开折叠，会先触发contentChange，再触发select，isContentChanging用来判断是点击展开折叠触发的选中还是点击节点触发的选中
      if (!isContentChanging.value) {
        // 用例下面所有节点都展开
        expendNodeAndChildren(node);
        node.layout();
      }
    } else if (data && isModuleOrCollection(data)) {
      // 模块节点且有用例且未加载过用例数据
      if (data.id !== 'NONE' && data.count > 0 && data.isLoaded !== true) {
        await initNodeCases(node);
      }
      extraVisible.value = false;
      canShowDetail.value = false;
      showAssociateBugMenu.value = true;
    } else {
      extraVisible.value = false;
      canShowDetail.value = false;
      showAssociateBugMenu.value = false;
      resetExtractInfo();
      removeFakeNode(node, 'fakeNode');
    }
    setPriorityView(true, 'P');
  }

  const canShowBatchAddDefect = ref(false);
  function handleNodeBatchSelect(nodes: MinderJsonNode[]) {
    isMinderOperation.value = true;
    if (nodes.every((node) => isModuleOrCollection(node.data) || node.data?.caseId)) {
      canShowBatchAddDefect.value = true;
    } else {
      canShowBatchAddDefect.value = false;
    }
  }

  function showBatchAddDefect() {
    showAddDefectDrawer.value = true;
    setBatchMinderParams();
  }

  function showBatchLinkDefect() {
    showLinkDefectDrawer.value = true;
    setBatchMinderParams();
  }

  function handleNodeUnselect() {
    extraVisible.value = false;
    isMinderOperation.value = false;
  }

  const { unbindShortcuts } = useShortCut(
    {
      executeToError: () => {
        handleShortCutExecute(LastExecuteResults.ERROR);
      },
      executeToBlocked: () => {
        handleShortCutExecute(LastExecuteResults.BLOCKED);
      },
      executeToSuccess: () => {
        handleShortCutExecute(LastExecuteResults.SUCCESS);
      },
    },
    {}
  );

  onMounted(() => {
    initCaseTree();
    // 固定标签颜色
    window.minder._resourceColorMapping = {
      [executionResultMap.SUCCESS.statusText]: 4,
      [executionResultMap.ERROR.statusText]: 5,
      [executionResultMap.BLOCKED.statusText]: 6,
    };
  });

  onBeforeUnmount(() => {
    unbindShortcuts();
  });

  defineExpose({
    initCaseTree,
  });
</script>

<style lang="less" scoped>
  :deep(.comment-list-item-name) {
    max-width: 130px;
  }
  :deep(.ms-list) {
    margin: 0;
    height: 100%;
  }
  :deep(.execute-form) .rich-wrapper .halo-rich-text-editor .editor-content {
    max-height: 54px !important;
    .ProseMirror {
      min-height: 38px;
    }
  }
</style>
