<template>
  <div class="h-full">
    <MsMinderEditor
      v-model:activeExtraKey="activeExtraKey"
      v-model:extra-visible="extraVisible"
      v-model:loading="loading"
      v-model:import-json="importJson"
      :minder-key="MinderKeyEnum.CASE_REVIEW_MINDER"
      :extract-content-tab-list="extractContentTabList"
      :can-show-float-menu="canShowFloatMenu"
      :can-show-priority-menu="false"
      :can-show-more-menu="canShowMoreMenu"
      :can-show-enter-node="canShowEnterNode"
      :can-show-more-menu-node-operation="false"
      :more-menu-other-operation-list="canShowFloatMenu ? moreMenuOtherOperationList : []"
      :shortcut-list="['expand', 'enter']"
      disabled
      @content-change="handleMinderNodeContentChange"
      @node-select="handleNodeSelect"
      @node-unselect="handleNodeUnselect"
    >
      <template #extractMenu>
        <!-- 评审 查看详情 -->
        <a-trigger
          v-if="hasAnyPermission(['CASE_REVIEW:READ+REVIEW']) && isReviewer"
          v-model:popup-visible="reviewVisible"
          trigger="click"
          position="bl"
          :click-outside-to-close="false"
          popup-container="body"
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
            <div class="w-[440px] rounded bg-[var(--color-text-fff)] p-[16px] shadow-[0_0_10px_rgba(0,0,0,0.05)]">
              <ReviewSubmit
                :review-pass-rule="reviewPassRule"
                :select-node="selectNode"
                :user-id="props.viewStatusFlag ? userStore.id || '' : ''"
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
          disabled
          :active-case="activeCaseInfo"
        />
        <div v-show="activeExtraKey === 'history'" class="pl-[16px]">
          <div v-if="props.reviewPassRule === 'MULTIPLE'" class="mb-[8px] flex items-center justify-between">
            <div class="text-[12px]">
              <span class="text-[var(--color-text-4)]">{{ t('caseManagement.caseReview.progress') }}</span>
              {{ props.reviewProgress }}
            </div>
            <ReviewStatusTrigger ref="reviewStatusTriggerRef" :size="12" />
          </div>
          <ReviewCommentList
            :review-comment-list="reviewHistoryList"
            active-comment="reviewComment"
            not-show-review-name
          />
        </div>
      </template>
      <!-- <template #shortCutList>
        <div class="ms-minder-shortcut-trigger-listitem">
          <div>{{ t('common.pass') }}</div>
          <div class="ms-minder-shortcut-trigger-listitem-icon ms-minder-shortcut-trigger-listitem-icon-auto"> P </div>
        </div>
        <div class="ms-minder-shortcut-trigger-listitem">
          <div>{{ t('common.unPass') }}</div>
          <div class="ms-minder-shortcut-trigger-listitem-icon ms-minder-shortcut-trigger-listitem-icon-auto"> R </div>
        </div>
      </template> -->
    </MsMinderEditor>
  </div>
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDescription, { Description } from '@/components/pure/ms-description/index.vue';
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
  import Attachment from '@/components/business/ms-minders/featureCaseMinder/attachment.vue';
  import ReviewStatusTrigger from './components/reviewStatusTrigger.vue';
  import ReviewCommentList from '@/views/case-management/caseManagementFeature/components/tabContent/tabComment/reviewCommentList.vue';
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
    ReviewResult,
  } from '@/models/caseManagement/caseReview';
  import { ModuleTreeNode } from '@/models/common';
  import { StartReviewStatus } from '@/enums/caseEnum';
  import { MinderEventName, MinderKeyEnum } from '@/enums/minderEnum';

  import { convertToFile, getCustomField } from '@/views/case-management/caseManagementFeature/components/utils';

  const props = defineProps<{
    moduleId: string;
    viewStatusFlag: boolean; // 我的评审结果
    reviewProgress: string;
    reviewPassRule: ReviewPassRule; // 评审规则
    moduleTree: ModuleTreeNode[];
    tableSorter: Record<string, any>;
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
  const moduleTag = t('common.module');
  const importJson = ref<MinderJson>({
    root: {} as MinderJsonNode,
    treePath: [],
  });
  const loading = ref(false);
  const largeModulesMap = ref<Record<string, MinderJsonNode[]>>({}); // 存储大数据量的模块节点

  const modulesCount = computed(() => caseReviewStore.modulesCount);

  /**
   * 初始化用例模块树
   */
  async function initCaseTree() {
    const tree = mapTree<MinderJsonNode>(props.moduleTree, (e) => {
      if (e.children && e.children.length > 50) {
        // 每层超过 50 个节点，只展示 50 个
        largeModulesMap.value[e.id] = e.children; // 存储大数据量的模块节点
      }
      const showingChildren = largeModulesMap.value[e.id]?.splice(0, 50) || e.children || [];
      if (showingChildren.length === 50) {
        showingChildren.push({
          data: {
            id: `tmp-${e.id}`,
            type: 'tmpModule',
            text: t('ms.minders.moreModule'),
            resource: [],
            isNew: false,
            changed: false,
          },
        });
      }
      return {
        ...e,
        data: {
          ...e.data,
          id: e.id || e.data?.id || '',
          text: e.name || e.data?.text.replace(/<\/?p\b[^>]*>/gi, '') || '',
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
                    text: t('ms.minders.moreCase'),
                    resource: [''],
                  },
                },
              ]
            : showingChildren,
      };
    });
    importJson.value.root = {
      children: tree,
      data: {
        id: 'NONE',
        text: t('caseManagement.caseReview.allCases'),
        resource: [moduleTag],
        disabled: true,
        count: modulesCount.value.all,
      },
    };
    importJson.value.treePath = [];
    clearSelectedNodes();
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
      const { list, total } = await getCaseReviewMinder({
        current: (loadMoreCurrent ?? 0) + 1,
        projectId: appStore.currentProjectId,
        moduleId: node.data?.id,
        reviewId: route.query.id as string,
        viewStatusFlag: props.viewStatusFlag,
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

  const reviewHistoryList = ref<ReviewHistoryItem[]>([]); // 加载评审历史列表
  async function initReviewHistoryList(data: MinderJsonNodeData) {
    try {
      const res = await getCaseReviewHistoryList(route.query.id as string, data?.caseId || activeCaseInfo.value.caseId);
      reviewHistoryList.value = res;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  /**
   * 切换用例详情显示
   */
  const reviewStatusTriggerRef = ref<InstanceType<typeof ReviewStatusTrigger>>();
  async function toggleDetail(val?: boolean) {
    extraVisible.value = val !== undefined ? val : !extraVisible.value;
    const node: MinderJsonNode = window.minder.getSelectedNode();
    const { data } = node;
    if (extraVisible.value && data && isCaseTag(data)) {
      activeExtraKey.value = 'history';
      initCaseDetail(data);
      initReviewHistoryList(data);
      reviewStatusTriggerRef.value?.initReviewerAndStatus(
        route.query.id as string,
        data?.caseId || activeCaseInfo.value.caseId
      );
    }
  }

  const hasOperationPermission = hasAnyPermission(['CASE_REVIEW:READ+UPDATE', 'CASE_REVIEW:READ+RELEVANCE']);
  const canShowFloatMenu = ref(false); // 是否展示浮动菜单
  const canShowMoreMenu = ref(false); // 更多
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
        label: t('caseManagement.caseReview.disassociateCase'),
        permission: ['CASE_REVIEW:READ+RELEVANCE'],
        onClick: () => {
          emit('operation', 'disassociate', node);
        },
      },
    ];
  }

  const selectNode = ref();
  const reviewVisible = ref(false);
  async function handleReviewDone(status: StartReviewStatus | ReviewResult) {
    const node = window.minder.getSelectedNode();
    reviewVisible.value = false;
    // 如果是用例，则status是获取后端的值，可直接替换标签
    if (isCaseTag(node.data)) {
      window.minder.execCommand('resource', [statusTagMap[status]]);
    } else if (node.data.id === 'NONE') {
      // 处理根节点，重新渲染整个用例树
      await initCaseTree();
    } else if (status !== StartReviewStatus.UNDER_REVIEWED && node.data?.resource?.includes(moduleTag)) {
      // TODO 想一个更好的处理方案：只递归处理用例节点，可以保证不折叠原本展开的东西，但多人评审时不知道评审结果标签是啥，调后端又反而增加了负重
      // 处理模块节点
      // 删除模块下所有子节点
      clearNodeChildren(node);
      // 重新渲染子模块，子模块都收起
      renderSubModules(node, importJson.value.root, modulesCount.value);
      // 重新渲染用例
      await initNodeCases(node);
    }
    setPriorityView(true, 'P');
    emit('handleReviewDone');
  }

  // 递归更新子节点的用例标签
  function updateChildResources(status: string, node?: MinderJsonNode) {
    node?.children?.forEach((child: MinderJsonNode) => {
      if (isCaseTag(child.data)) {
        child.setData('resource', [statusTagMap[status]]).render();
      } else if (child.data?.resource?.includes(moduleTag)) {
        updateChildResources(status, child);
      }
    });
  }

  // 更新状态标签
  function updateResource(status: string) {
    if (selectNode.value.data?.resource?.includes(moduleTag)) {
      updateChildResources(status, selectNode.value);
    } else if (isCaseTag(selectNode.value.data)) {
      window.minder.execCommand('resource', [statusTagMap[status]]);
    }
    setPriorityView(true, 'P');
  }

  /**
   * 是否是当前用例的评审人
   * @param data 节点信息
   */
  async function setIsReviewer(data?: MinderJsonNodeData) {
    caseReviewerList.value = await getCaseReviewerList(route.query.id as string, data?.caseId);
    isReviewer.value = caseReviewerList.value.some((child) => child.userId === userStore.id);
  }

  function loadMoreModules(id: string) {
    const node: MinderJsonNode = window.minder.getNodeById(id);
    const children = largeModulesMap.value[id];
    if (children) {
      const fakeNode = node.children?.find((e) => e.data?.id === `tmp-${id}`); // 移除占位的虚拟节点
      if (fakeNode) {
        window.minder.removeNode(fakeNode);
      }
      const waitingRenderNodes: MinderJsonNode[] = [];
      children.splice(0, 50).forEach((e) => {
        const child = window.minder.createNode(
          {
            ...e.data,
            id: e.id || e.data?.id || '',
            text: e.name || e.data?.text || '',
            resource: [moduleTag],
            expandState: 'collapse',
            count: modulesCount.value[e.id],
            isNew: false,
            changed: false,
          },
          node
        );
        if (modulesCount.value[e.id] > 0 && !e.children?.length) {
          const fakeChildNode = window.minder.createNode(
            {
              id: 'fakeNode',
              text: t('ms.minders.moreCase'),
              resource: [],
              isNew: false,
              changed: false,
            },
            child
          );
          waitingRenderNodes.push(fakeChildNode);
        }
        waitingRenderNodes.push(child);
      });
      if (children.length > 0) {
        // 有更多模块
        const moreNode = window.minder.createNode(
          {
            id: `tmp-${id}`,
            text: t('ms.minders.moreModule'),
            type: 'tmpModule',
            resource: [],
            isNew: false,
            changed: false,
          },
          node
        );
        waitingRenderNodes.push(moreNode);
      }
      window.minder.renderNodeBatch(waitingRenderNodes);
      node.layout();
      // 加载完模块数据后，更新当前importJson数据
      replaceNodeInTree([importJson.value.root], node.data?.id || '', window.minder.exportNode(node), 'data', 'id');
    }
  }

  const isContentChanging = ref(false);
  function handleMinderNodeContentChange() {
    isContentChanging.value = true;
    setTimeout(() => {
      isContentChanging.value = false;
    }, 300); // 300ms 是脑图编辑器的 debounce 时间，300ms 后触发选中事件
  }

  /**
   * 处理节点选中
   * @param node 节点
   */
  async function handleNodeSelect(node: MinderJsonNode) {
    const { data } = node;
    if (data?.type === 'tmpModule') {
      // 点击更多节点，加载更多模块
      loadMoreModules(node.parent?.data?.id || '');
      return;
    }
    // 点击更多节点，加载更多用例
    if (data?.type === 'tmp' && node.parent?.data?.resource?.includes(moduleTag)) {
      canShowFloatMenu.value = false;
      await initNodeCases(node.parent, data.current);
      setPriorityView(true, 'P');
      return;
    }
    selectNode.value = node;

    // 展示浮动菜单: 模块节点且有子节点且不是没权限的根结点、用例节点
    if (
      isCaseTag(node.data) ||
      (node.data?.resource?.includes(moduleTag) &&
        (node.children || []).length > 0 &&
        !(!hasOperationPermission && node.type === 'root'))
    ) {
      canShowFloatMenu.value = true;
      setMoreMenuOtherOperationList(node);
    } else {
      canShowFloatMenu.value = false;
    }

    reviewVisible.value = false;

    // 不展示更多：没操作权限的用例
    if (isCaseTag(node.data) && !hasOperationPermission) {
      canShowMoreMenu.value = false;
    } else {
      canShowMoreMenu.value = true;
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

    if (isCaseTag(data)) {
      canShowDetail.value = true;
      setIsReviewer(node.data);
      if (extraVisible.value) {
        toggleDetail(true);
      }
      // 点击展开折叠，会先触发contentChange，再触发select，isContentChanging用来判断是点击展开折叠触发的选中还是点击节点触发的选中
      if (!isContentChanging.value) {
        // 用例下面所有节点都展开
        expendNodeAndChildren(node);
        node.layout();
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

  function handleNodeUnselect() {
    extraVisible.value = false;
  }

  defineExpose({
    initCaseTree,
    updateResource,
  });
</script>

<style lang="less" scoped>
  :deep(.comment-list-item-name) {
    max-width: 200px;
  }
</style>
