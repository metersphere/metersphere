<template>
  <div class="h-full">
    <MsMinderEditor
      v-model:activeExtraKey="activeExtraKey"
      v-model:extra-visible="extraVisible"
      v-model:loading="loading"
      v-model:import-json="importJson"
      :extract-content-tab-list="extractContentTabList"
      :can-show-float-menu="canShowFloatMenu"
      :can-show-priority-menu="false"
      :can-show-more-menu="canShowFloatMenu"
      :can-show-enter-node="canShowEnterNode"
      :can-show-more-menu-node-operation="false"
      :more-menu-other-operation-list="canShowFloatMenu ? moreMenuOtherOperationList : []"
      disabled
      @node-select="handleNodeSelect"
      @before-exec-command="handleBeforeExecCommand"
    >
      <template #extractMenu>
        <!-- 评审 查看详情 -->
        <a-trigger
          v-if="hasAnyPermission(['CASE_REVIEW:READ+REVIEW']) && isReviewer"
          v-model:popup-visible="reviewVisible"
          trigger="click"
          position="bl"
          :click-outside-to-close="false"
          popup-container=".ms-minder-container"
        >
          <a-tooltip :content="t('caseManagement.caseReview.review')">
            <MsButton
              type="icon"
              :class="[
                'ms-minder-node-float-menu-icon-button',
                `${reviewVisible ? 'ms-minder-node-float-menu-icon-button--focus' : ''}`,
              ]"
            >
              <MsIcon type="icon-icon_audit" class="text-[var(--color-text-4)]" />
            </MsButton>
          </a-tooltip>
          <template #content>
            <div class="w-[440px] rounded bg-white p-[16px] shadow-[0_0_10px_rgba(0,0,0,0.05)]">
              <ReviewSubmit
                :review-pass-rule="reviewPassRule"
                :select-node="selectNode"
                :user-id="props.viewFlag ? userStore.id || '' : ''"
                :review-id="route.query.id as string"
                @done="handleReviewDone"
              />
            </div>
          </template>
        </a-trigger>
        <a-tooltip v-if="canShowDetail" :content="t('common.detail')">
          <MsButton
            type="icon"
            :class="[
              'ms-minder-node-float-menu-icon-button',
              `${extraVisible ? 'ms-minder-node-float-menu-icon-button--focus' : ''}`,
            ]"
            @click="toggleDetail"
          >
            <MsIcon type="icon-icon_describe_outlined" class="text-[var(--color-text-4)]" />
          </MsButton>
        </a-tooltip>
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
          :active-case="activeCaseInfo"
        />
        <div v-else class="pl-[16px]">
          <div v-if="props.reviewPassRule === 'MULTIPLE'" class="mb-[8px] flex items-center justify-between">
            <div class="text-[12px]">
              <span class="text-[var(--color-text-4)]">{{ t('caseManagement.caseReview.progress') }}</span>
              {{ props.reviewProgress }}
            </div>
            <a-trigger v-model:popup-visible="statusVisible" trigger="click" position="br" :popup-translate="[0, 4]">
              <div
                :class="`flex cursor-pointer items-center rounded p-[4px] hover:bg-[var(--color-text-n9)] 
                ${statusVisible ? 'bg-[var(--color-text-n9)]' : ''} `"
              >
                <ReviewResult :status="activeCaseInfo.reviewStatus" class="text-[12px]" :icon-size="12" />
                <MsIcon type="icon-icon_expand-down_filled" size="12" class="ml-[4px] text-[var(--color-text-4)]" />
              </div>
              <template #content>
                <div
                  class="trigger-content w-[150px] rounded-[var(--border-radius-medium)] bg-white p-[6px] shadow-[0_-1px_4px_rgba(2,2,2,0.1)]"
                >
                  <div v-for="item in reviewUserStatusList" :key="item.id" class="my-[4px] flex justify-between">
                    <div class="one-line-text max-w-[80px]">
                      {{ item.userName }}
                    </div>
                    <ReviewResult :status="item.status" class="text-[12px]" :icon-size="12" />
                  </div>
                  <MsEmpty v-if="!reviewUserStatusList.length" />
                </div>
              </template>
            </a-trigger>
          </div>
          <ReviewCommentList
            :review-comment-list="reviewHistoryList"
            active-comment="reviewComment"
            not-show-review-name
          />
        </div>
      </template>
    </MsMinderEditor>
  </div>
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDescription, { Description } from '@/components/pure/ms-description/index.vue';
  import MsEmpty from '@/components/pure/ms-empty/index.vue';
  import MsMinderEditor from '@/components/pure/ms-minder-editor/minderEditor.vue';
  import type { MinderJson, MinderJsonNode, MinderJsonNodeData } from '@/components/pure/ms-minder-editor/props';
  import { MinderEvent } from '@/components/pure/ms-minder-editor/props';
  import { setPriorityView } from '@/components/pure/ms-minder-editor/script/tool/utils';
  import { MsFileItem } from '@/components/pure/ms-upload/types';
  import Attachment from '@/components/business/ms-minders/featureCaseMinder/attachment.vue';
  import ReviewCommentList from '@/views/case-management/caseManagementFeature/components/tabContent/tabComment/reviewCommentList.vue';
  import ReviewResult from '@/views/case-management/caseReview/components/reviewResult.vue';
  import ReviewSubmit from '@/views/case-management/caseReview/components/reviewSubmit.vue';

  import {
    getCaseReviewerList,
    getCaseReviewHistoryList,
    getCaseReviewMinder,
  } from '@/api/modules/case-management/caseReview';
  import { getCaseDetail } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import { useUserStore } from '@/store';
  import useAppStore from '@/store/modules/app';
  import useCaseReviewStore from '@/store/modules/case/caseReview';
  import useMinderStore from '@/store/modules/components/minder-editor/index';
  import { findNodeByKey, mapTree, replaceNodeInTree } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import {
    CaseReviewFunctionalCaseUserItem,
    ReviewHistoryItem,
    ReviewPassRule,
  } from '@/models/caseManagement/caseReview';
  import { ModuleTreeNode } from '@/models/common';
  import { MinderEventName } from '@/enums/minderEnum';

  import { convertToFile, getCustomField } from '@/views/case-management/caseManagementFeature/components/utils';

  const props = defineProps<{
    moduleId: string;
    viewFlag: boolean; // 是否只看我的
    viewStatusFlag: boolean; // 我的评审结果
    reviewProgress: string;
    reviewPassRule: ReviewPassRule; // 评审规则
    moduleTree: ModuleTreeNode[];
  }>();

  const emit = defineEmits<{
    (e: 'operation', type: string, node: MinderJsonNode): void;
    (e: 'handleReviewDone'): void;
  }>();

  const route = useRoute();
  const appStore = useAppStore();
  const { t } = useI18n();
  const minderStore = useMinderStore();
  const userStore = useUserStore();
  const caseReviewStore = useCaseReviewStore();

  const statusTagMap: Record<string, string> = {
    PASS: t('common.pass'),
    UN_PASS: t('common.unPass'),
    UNDER_REVIEWED: t('caseManagement.caseReview.reviewing'),
    RE_REVIEWED: t('caseManagement.caseReview.reReview'),
  };
  const caseTag = t('common.case');
  const moduleTag = t('common.module');
  const importJson = ref<MinderJson>({
    root: {} as MinderJsonNode,
    template: 'default',
    treePath: [],
  });
  const loading = ref(false);

  const modulesCount = computed(() => caseReviewStore.modulesCount);

  /**
   * 初始化用例模块树
   */
  async function initCaseTree() {
    const tree = mapTree<MinderJsonNode>(props.moduleTree, (e) => ({
      ...e,
      data: {
        ...e.data,
        id: e.id || e.data?.id || '',
        text: e.name || e.data?.text || '',
        resource: modulesCount.value[e.id] !== undefined ? [moduleTag] : e.data?.resource,
        expandState: e.level === 0 ? 'expand' : 'collapse',
        count: modulesCount.value[e.id],
        disabled: true,
      },
      children:
        modulesCount.value[e.id] > 0 && !e.children?.length
          ? [
              {
                data: {
                  id: 'fakeNode',
                  text: 'fakeNode',
                  resource: ['fakeNode'],
                },
              },
            ]
          : e.children,
    }));
    importJson.value.root = {
      children: tree,
      data: {
        id: 'NONE',
        text: t('ms.minders.allModule'),
        resource: [moduleTag],
        disabled: true,
      },
    };
    importJson.value.treePath = [];
    window.minder.importJson(importJson.value);
    if (props.moduleId !== 'all') {
      // 携带具体的模块 ID 加载时，进入该模块内
      nextTick(() => {
        minderStore.dispatchEvent(MinderEventName.ENTER_NODE, undefined, undefined, undefined, [
          findNodeByKey(importJson.value.root.children || [], props.moduleId, 'id', 'data') as MinderJsonNode,
        ]);
      });
    } else {
      // 刷新时不需要重新请求数据，进入根节点
      nextTick(() => {
        minderStore.dispatchEvent(MinderEventName.ENTER_NODE, undefined, undefined, undefined, [importJson.value.root]);
      });
    }
  }

  onMounted(() => {
    initCaseTree();
    // 固定标签颜色
    window.minder._resourceColorMapping = {
      [statusTagMap.UNDER_REVIEWED]: 8,
      [statusTagMap.PASS]: 4,
      [statusTagMap.UN_PASS]: 5,
      [statusTagMap.RE_REVIEWED]: 6,
    };
  });

  watch([() => props.moduleId, () => props.viewStatusFlag], () => {
    initCaseTree();
  });

  /**
   * 移除占位的虚拟节点
   * @param node 对应节点
   * @param fakeNodeName 虚拟节点名称
   */
  function removeFakeNode(node: MinderJsonNode, fakeNodeName: string) {
    const fakeNode = node.children?.find((e: MinderJsonNode) => e.data?.id === fakeNodeName);
    if (fakeNode) {
      window.minder.removeNode(fakeNode);
    }
  }

  /**
   * 渲染其子节点
   * @param node 对应节点
   * @param renderNode 需要渲染的子节点
   */
  function handleRenderNode(node: MinderJsonNode, renderNode: MinderJsonNode) {
    if (!node.data) return;
    window.minder.renderNodeBatch(renderNode);
    node.layout();
    node.data.isLoaded = true;
  }

  /**
   * 创建节点
   * @param data 节点数据
   * @param parentNode 父节点
   */
  function createNode(data?: MinderJsonNodeData, parentNode?: MinderJsonNode) {
    return window.minder.createNode(
      {
        ...data,
        expandState: 'collapse',
        disabled: true,
      },
      parentNode
    );
  }

  /**
   * 递归渲染子节点及其子节点
   * @param parentNode - 父节点
   * @param children - 子节点数组
   */
  function renderSubNodes(parentNode: MinderJsonNode, children?: MinderJsonNode[]) {
    return (
      children?.map((item: MinderJsonNode) => {
        const grandChild = createNode(item.data, parentNode);
        const greatGrandChildren = renderSubNodes(grandChild, item.children);
        window.minder.renderNodeBatch(greatGrandChildren);
        return grandChild;
      }) || []
    );
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
      const { list, total } = await getCaseReviewMinder({
        current: (loadMoreCurrent ?? 0) + 1,
        projectId: appStore.currentProjectId,
        moduleId: node.data?.id,
        reviewId: route.query.id as string,
        viewFlag: props.viewFlag,
        viewStatusFlag: props.viewStatusFlag,
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
            resource: [
              ...(statusTagMap[e.data?.status] ? [statusTagMap[e.data?.status]] : []),
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
            text: '...',
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
  const activeExtraKey = ref<'baseInfo' | 'attachment' | 'history'>('history');
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
      value: 'history',
      label: t('caseManagement.caseReview.reviewHistory'),
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
      const res = await getCaseDetail(data?.caseId || activeCaseInfo.value.caseId);
      activeCaseInfo.value = res;
      // 基本信息
      descriptions.value = [
        {
          label: t('caseManagement.caseReview.belongModule'),
          value: res.moduleName || t('common.root'),
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
        {
          label: t('caseManagement.caseReview.creator'),
          value: res.createUserName || '',
        },
        {
          label: t('caseManagement.caseReview.createTime'),
          value: dayjs().format('YYYY-MM-DD HH:mm:ss'),
        },
      ];
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

  // 加载评审历史列表
  const reviewHistoryList = ref<ReviewHistoryItem[]>([]);
  const reviewUserStatusList = ref<ReviewHistoryItem[]>([]); // 每个评审人最后一次评审结果
  const statusVisible = ref(false);
  async function initReviewHistoryList(data: MinderJsonNodeData) {
    try {
      const res = await getCaseReviewHistoryList(route.query.id as string, data?.caseId || activeCaseInfo.value.caseId);
      reviewHistoryList.value = res;
      reviewUserStatusList.value = [];
      const userNamesSet = new Set();
      reviewHistoryList.value.forEach((reviewItem) => {
        if (!userNamesSet.has(reviewItem.userName)) {
          reviewUserStatusList.value.push(reviewItem);
          userNamesSet.add(reviewItem.userName);
        }
      });
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
    if (extraVisible.value && data?.resource?.includes(caseTag)) {
      activeExtraKey.value = 'history';
      initCaseDetail(data);
      initReviewHistoryList(data);
    }
  }

  const canShowFloatMenu = ref(false); // 是否展示浮动菜单
  const isReviewer = ref(false); // 是否是此用例的评审人
  const caseReviewerList = ref<CaseReviewFunctionalCaseUserItem[]>([]);
  const canShowEnterNode = ref(false);
  const canShowDetail = ref(false);
  const moreMenuOtherOperationList = ref();
  function setMoreMenuOtherOperationList(node: MinderJsonNode) {
    moreMenuOtherOperationList.value = [
      {
        value: 'changeReviewer',
        label: t('caseManagement.caseReview.changeReviewer'),
        permission: ['CASE_REVIEW:READ+UPDATE'],
        onClick: () => {
          emit('operation', 'changeReviewer', node);
        },
      },
      {
        value: 'reReview',
        label: t('caseManagement.caseReview.reReview'),
        permission: ['CASE_REVIEW:READ+UPDATE'],
        onClick: () => {
          emit('operation', 'reReview', node);
        },
      },
      {
        value: 'disassociate',
        label: t('caseManagement.caseReview.disassociate'),
        permission: ['CASE_REVIEW:READ+RELEVANCE'],
        onClick: () => {
          emit('operation', 'disassociate', node);
        },
      },
    ];
  }

  const selectNode = ref();
  const reviewVisible = ref(false);
  function handleReviewDone() {
    reviewVisible.value = false;
    emit('handleReviewDone');
  }

  /**
   * 是否是当前用例的评审人
   * @param data 节点信息
   */
  async function setIsReviewer(data?: MinderJsonNodeData) {
    caseReviewerList.value = await getCaseReviewerList(route.query.id as string, data?.caseId);
    isReviewer.value = caseReviewerList.value.some((child) => child.userId === userStore.id);
  }

  /**
   * 处理节点选中
   * @param node 节点
   */
  async function handleNodeSelect(node: MinderJsonNode) {
    const { data } = node;
    // 点击更多节点，加载更多用例
    if (data?.type === 'tmp' && node.parent?.data?.resource?.includes(moduleTag)) {
      canShowFloatMenu.value = false;
      await initNodeCases(node.parent, data.current);
      setPriorityView(true, 'P');
      return;
    }
    selectNode.value = node;

    // 展示浮动菜单: 模块节点有子节点、用例节点
    if (
      node.data?.resource?.includes(caseTag) ||
      (node.data?.resource?.includes(moduleTag) && (node.children || []).length > 0)
    ) {
      canShowFloatMenu.value = true;
      setMoreMenuOtherOperationList(node);
    } else {
      canShowFloatMenu.value = false;
    }

    // 展示进入节点菜单: 模块节点
    if (data?.resource?.includes(moduleTag) && (node.children || []).length > 0 && node.type !== 'root') {
      canShowEnterNode.value = true;
    } else {
      canShowEnterNode.value = false;
    }

    if (data?.resource?.includes(moduleTag) && (node.children || []).length > 0) {
      isReviewer.value = true;
    } else {
      isReviewer.value = false;
    }

    if (data?.resource?.includes(caseTag)) {
      canShowDetail.value = true;
      setIsReviewer(node.data);
      if (extraVisible.value) {
        toggleDetail(true);
      }
    } else if (data?.resource?.includes(moduleTag) && data.count > 0 && data.isLoaded !== true) {
      // 模块节点且有用例且未加载过用例数据
      await initNodeCases(node);
      extraVisible.value = false;
      canShowDetail.value = false;
    } else {
      extraVisible.value = false;
      canShowDetail.value = false;
      resetExtractInfo();
      removeFakeNode(node, 'fakeNode');
    }
    setPriorityView(true, 'P');
  }

  /**
   * 脑图命令执行前拦截
   * @param event 命令执行事件
   */
  function handleBeforeExecCommand(event: MinderEvent) {
    if (['movetoparent', 'arrange'].includes(event.commandName)) {
      event.stopPropagation();
    }
  }

  defineExpose({
    initCaseTree,
  });
</script>

<style lang="less" scoped>
  :deep(.comment-list-item-name) {
    max-width: 200px;
  }
  .trigger-content {
    max-height: 192px;
    @apply overflow-y-auto overflow-x-hidden;
    .ms-scroll-bar();
  }
</style>
