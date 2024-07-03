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
      :can-show-more-menu="showCaseMenu"
      :can-show-more-menu-node-operation="false"
      :more-menu-other-operation-list="moreMenuOtherOperationList"
      disabled
      single-tag
      @node-select="handleNodeSelect"
    >
      <template #extractMenu>
        <!-- 进入当前节点 -->
        <template v-if="canShowEnterNode">
          <MsButton type="text" class="!text-[var(--color-text-1)]" @click="handleEnterNode">
            {{ t('minder.hotboxMenu.enterNode') }}
          </MsButton>
        </template>
        <template v-if="showCaseMenu">
          <!-- 评审 查看详情 更多 -->
          <a-tooltip :content="t('caseManagement.caseReview.review')">
            <MsButton type="icon" class="ms-minder-node-float-menu-icon-button">
              <MsIcon type="icon-icon_audit" class="text-[var(--color-text-4)]" />
            </MsButton>
          </a-tooltip>
          <a-tooltip :content="t('common.detail')">
            <MsButton type="icon" class="ms-minder-node-float-menu-icon-button" @click="toggleDetail">
              <MsIcon
                type="icon-icon_describe_outlined"
                class="text-[var(--color-text-4)]"
                :class="[extraVisible ? 'ms-minder-node-float-menu-icon-button--focus' : '']"
              />
            </MsButton>
          </a-tooltip>
        </template>
      </template>
      <template #extractTabContent>
        <MsDescription
          v-if="activeExtraKey === 'baseInfo'"
          :loading="baseInfoLoading"
          :descriptions="descriptions"
          label-width="90px"
        />
        <Attachment
          v-else-if="activeExtraKey === 'attachment'"
          v-model:model-value="fileList"
          not-show-add-button
          :active-case="activeCaseInfo"
        />
        <div v-else>
          <div v-if="props.reviewPassRule === 'MULTIPLE'" class="flex justify-between">
            <div class="text-[12px]">
              <span class="text-[var(--color-text-4)]">{{ t('caseManagement.caseReview.progress') }}</span>
              {{ props.passRate }}
            </div>
            <!-- TODO 下拉 -->
          </div>
          <ReviewCommentList :review-comment-list="reviewHistoryList" active-comment="reviewComment" />
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
  import MsMinderEditor from '@/components/pure/ms-minder-editor/minderEditor.vue';
  import type { MinderJson, MinderJsonNode, MinderJsonNodeData } from '@/components/pure/ms-minder-editor/props';
  import { setPriorityView } from '@/components/pure/ms-minder-editor/script/tool/utils';
  import { MsFileItem } from '@/components/pure/ms-upload/types';
  import Attachment from '@/components/business/ms-minders/featureCaseMinder/attachment.vue';
  import ReviewCommentList from '@/views/case-management/caseManagementFeature/components/tabContent/tabComment/reviewCommentList.vue';

  import { getCaseReviewHistoryList, getCaseReviewMinder } from '@/api/modules/case-management/caseReview';
  import { getCaseDetail } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import useMinderStore from '@/store/modules/components/minder-editor/index';
  import { findNodeByKey, mapTree, replaceNodeInTree } from '@/utils';

  import { ReviewHistoryItem, ReviewPassRule } from '@/models/caseManagement/caseReview';
  import { ModuleTreeNode } from '@/models/common';
  import { MinderEventName } from '@/enums/minderEnum';

  import { convertToFile, getCustomField } from '@/views/case-management/caseManagementFeature/components/utils';

  const props = defineProps<{
    moduleId: string;
    modulesCount: Record<string, number>; // 模块数量
    viewFlag: boolean; // 是否只看我的
    viewStatusFlag: boolean; // 我的评审结果
    passRate: string;
    reviewPassRule: ReviewPassRule; // 评审规则
    moduleTree: ModuleTreeNode[];
  }>();

  const route = useRoute();
  const appStore = useAppStore();
  const { t } = useI18n();
  const minderStore = useMinderStore();

  const caseTag = t('common.case');
  const moduleTag = t('common.module');
  const importJson = ref<MinderJson>({
    root: {} as MinderJsonNode,
    template: 'default',
    treePath: [],
  });
  const loading = ref(false);

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
        resource: props.modulesCount[e.id] !== undefined ? [moduleTag] : e.data?.resource,
        expandState: e.level === 0 ? 'expand' : 'collapse',
        count: props.modulesCount[e.id],
      },
      children:
        props.modulesCount[e.id] > 0 && !e.children?.length
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
  });

  watch(
    () => props.moduleId,
    () => {
      initCaseTree();
    }
  );

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
    const { data } = node;
    if (!data) return;
    window.minder.renderNodeBatch(renderNode);
    node.layout();
    data.isLoaded = true;
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
   * @param selectedNode 选中节点
   * @param loadMoreCurrent 加载模块下更多用例时的当前页码
   */
  async function initNodeCases(selectedNode?: MinderJsonNode, loadMoreCurrent?: number) {
    try {
      loading.value = true;
      const node = selectedNode || window.minder.getSelectedNode();
      const { data } = node;
      if (!data) return;
      const { list, total } = await getCaseReviewMinder({
        current: loadMoreCurrent ? loadMoreCurrent + 1 : 1,
        projectId: appStore.currentProjectId,
        moduleId: data.id,
        reviewId: route.query.id as string,
        viewFlag: props.viewFlag,
        viewStatusFlag: props.viewStatusFlag,
      });
      // 移除占位的虚拟节点
      removeFakeNode(node, loadMoreCurrent ? `tmp-${data.id}` : 'fakeNode');
      // 如果模块下没有用例且有别的模块节点，正常展开
      if ((!list || list.length === 0) && node.children?.length && !loadMoreCurrent) {
        node.expand();
        handleRenderNode(node, node.children);
        return;
      }

      // 渲染节点
      let waitingRenderNodes: MinderJsonNode[] = [];
      list.forEach((e) => {
        // 用例节点
        const child = createNode(e.data, node);
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
      if (total > list.length * (loadMoreCurrent || 1)) {
        const moreNode = window.minder.createNode(
          {
            id: `tmp-${data.id}`,
            text: '...',
            type: 'tmp',
            expandState: 'collapse',
            current: loadMoreCurrent ? loadMoreCurrent + 1 : 1,
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

  watch(
    () => props.viewStatusFlag,
    () => {
      initNodeCases();
    }
  );

  const extraVisible = ref<boolean>(false);
  const activeExtraKey = ref<'baseInfo' | 'attachment' | 'history'>('baseInfo');
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
      const res = await getCaseDetail(data?.id || activeCaseInfo.value.id);
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
  async function initReviewHistoryList(data: MinderJsonNodeData) {
    try {
      const res = await getCaseReviewHistoryList(route.query.id as string, data?.id || activeCaseInfo.value.id);
      reviewHistoryList.value = res;
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
      activeExtraKey.value = 'baseInfo';
      initCaseDetail(data);
      initReviewHistoryList(data);
    }
  }

  const canShowFloatMenu = ref(false); // 是否展示浮动菜单
  const canShowEnterNode = ref(false);
  const showCaseMenu = ref(false);
  const moreMenuOtherOperationList = [
    {
      value: 'changeReviewer',
      label: t('caseManagement.caseReview.changeReviewer'),
      permission: ['CASE_REVIEW:READ+UPDATE'],
      onClick: () => {
        // TODO 操作
        console.log('🤔️ =>', t('caseManagement.caseReview.changeReviewer'));
      },
    },
    {
      value: 'reReview',
      label: t('caseManagement.caseReview.reReview'),
      permission: ['CASE_REVIEW:READ+UPDATE'],
      onClick: () => {
        // TODO 操作
        console.log('🤔️ =>', t('caseManagement.caseReview.reReview'));
      },
    },
    {
      value: 'disassociate',
      label: t('caseManagement.caseReview.disassociate'),
      permission: ['CASE_REVIEW:READ+RELEVANCE'],
      onClick: () => {
        // TODO 操作
        console.log('🤔️ =>', t('caseManagement.caseReview.disassociate'));
      },
    },
  ];

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
    // 展示浮动菜单: 模块节点且非根节点、用例节点
    if (
      node?.data?.resource?.includes(caseTag) ||
      (node?.data?.resource?.includes(moduleTag) && node.type !== 'root')
    ) {
      canShowFloatMenu.value = true;
    } else {
      canShowFloatMenu.value = false;
    }

    // 展示进入节点菜单: 模块节点且有子节点且非根节点
    if (data?.resource?.includes(moduleTag) && (node.children || []).length > 0) {
      canShowEnterNode.value = true;
    } else {
      canShowEnterNode.value = false;
    }

    if (data?.resource?.includes(caseTag)) {
      showCaseMenu.value = true;
      if (extraVisible.value) {
        toggleDetail(true);
      }
    } else if (data?.resource?.includes(moduleTag) && data.count > 0 && data.isLoaded !== true) {
      // 模块节点且有用例且未加载过用例数据
      await initNodeCases(node);
      showCaseMenu.value = false;
      extraVisible.value = false;
    } else {
      showCaseMenu.value = false;
      extraVisible.value = false;
      resetExtractInfo();
      removeFakeNode(node, 'fakeNode');
    }
    setPriorityView(true, 'P');
  }

  /**
   * 进入当前节点
   */
  function handleEnterNode() {
    const selectedNodes: MinderJsonNode[] = window.minder.getSelectedNodes();
    minderStore.dispatchEvent(MinderEventName.ENTER_NODE, undefined, undefined, undefined, [selectedNodes[0]]);
  }

  defineExpose({
    initNodeCases,
  });
</script>
