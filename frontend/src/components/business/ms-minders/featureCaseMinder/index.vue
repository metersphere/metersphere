<template>
  <div class="h-full py-[16px]">
    <MsMinderEditor
      v-model:activeExtraKey="activeExtraKey"
      v-model:extra-visible="extraVisible"
      v-model:loading="loading"
      v-model:import-json="importJson"
      :minder-key="MinderKeyEnum.FEATURE_CASE_MINDER"
      :tags="[]"
      :replaceable-tags="replaceableTags"
      :insert-node="insertNode"
      :priority-disable-check="priorityDisableCheck"
      :after-tag-edit="afterTagEdit"
      :extract-content-tab-list="extractContentTabList"
      :can-show-float-menu="canShowFloatMenu()"
      :can-show-enter-node="canShowEnterNode"
      :insert-sibling-menus="insertSiblingMenus"
      :insert-son-menus="insertSonMenus"
      :can-show-more-menu-node-operation="canShowMoreMenuNodeOperation()"
      :can-show-paste-menu="!stopPaste()"
      :can-show-more-menu="canShowMoreMenu()"
      :can-show-priority-menu="canShowPriorityMenu()"
      :custom-batch-expand="customBatchExpand"
      :can-show-batch-expand="canShowBatchExpand()"
      :can-show-batch-cut="true"
      :can-show-batch-copy="true"
      :can-show-batch-delete="true"
      :priority-tooltip="t('caseManagement.caseReview.caseLevel')"
      :disabled="!hasEditPermission"
      :disabled-extra-tab="!!activeCase.moduleIsNew"
      can-show-more-batch-menu
      single-tag
      tag-enable
      sequence-enable
      @content-change="handleMinderNodeContentChange"
      @node-select="handleNodeSelect"
      @action="handleAction"
      @before-exec-command="handleBeforeExecCommand"
      @save="handleMinderSave"
    >
      <template #extractMenu>
        <a-tooltip v-if="showDetailMenu" :content="t('common.detail')">
          <MsButton
            type="icon"
            class="ms-minder-node-float-menu-icon-button"
            :class="[extraVisible ? 'ms-minder-node-float-menu-icon-button--focus' : '']"
            @click="toggleDetail"
          >
            <MsIcon type="icon-icon_describe_outlined" class="text-[var(--color-text-4)]" />
          </MsButton>
        </a-tooltip>
      </template>
      <template #extractTabContent>
        <baseInfo
          v-if="activeExtraKey === 'baseInfo'"
          ref="baseInfoRef"
          :loading="baseInfoLoading"
          :active-case="activeCase"
          @init-template="(id) => (templateId = id)"
          @cancel="handleBaseInfoCancel"
          @saved="handleBaseInfoSaved"
        />
        <attachment
          v-else-if="activeExtraKey === 'attachment'"
          v-model:model-value="fileList"
          :active-case="activeCase"
          @upload-success="initCaseDetail"
        />
        <caseCommentList v-else-if="activeExtraKey === 'comments'" :active-case="activeCase" />
        <bugList v-else :active-case="activeCase" />
      </template>
      <template #shortCutList>
        <div class="ms-minder-shortcut-trigger-listitem">
          <div>{{ t('ms.minders.createSiblingModule') }}</div>
          <div class="ms-minder-shortcut-trigger-listitem-icon ms-minder-shortcut-trigger-listitem-icon-auto"> M </div>
        </div>
        <div class="ms-minder-shortcut-trigger-listitem">
          <div>{{ t('ms.minders.createSiblingCase') }}</div>
          <div class="ms-minder-shortcut-trigger-listitem-icon ms-minder-shortcut-trigger-listitem-icon-auto"> C </div>
        </div>
        <div class="ms-minder-shortcut-trigger-listitem">
          <div>{{ t('ms.minders.createChildModule') }}</div>
          <div class="ms-minder-shortcut-trigger-listitem-icon ms-minder-shortcut-trigger-listitem-icon-auto">
            Shift + M
          </div>
        </div>
        <div class="ms-minder-shortcut-trigger-listitem">
          <div>{{ t('ms.minders.createChildCase') }}</div>
          <div class="ms-minder-shortcut-trigger-listitem-icon ms-minder-shortcut-trigger-listitem-icon-auto">
            Shift + C
          </div>
        </div>
      </template>
    </MsMinderEditor>
  </div>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import useShortCut from '@/components/pure/ms-minder-editor/hooks/useShortCut';
  import MsMinderEditor from '@/components/pure/ms-minder-editor/minderEditor.vue';
  import type { MinderJson, MinderJsonNode, MinderJsonNodeData } from '@/components/pure/ms-minder-editor/props';
  import {
    clearSelectedNodes,
    expendNodeAndChildren,
    setPriorityView,
  } from '@/components/pure/ms-minder-editor/script/tool/utils';
  import { MsFileItem } from '@/components/pure/ms-upload/types';
  import attachment from './attachment.vue';
  import baseInfo from './basInfo.vue';
  import bugList from './bugList.vue';
  import caseCommentList from './commentList.vue';

  import {
    checkFileIsUpdateRequest,
    getCaseDetail,
    getCaseMinder,
    getCaseMinderTree,
    saveCaseMinder,
  } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import useMinderStore from '@/store/modules/components/minder-editor/index';
  import { MinderCustomEvent } from '@/store/modules/components/minder-editor/types';
  import { filterTree, findNodeByKey, getGenerateId, mapTree, replaceNodeInTree, traverseTree } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import {
    FeatureCaseMinderEditType,
    FeatureCaseMinderStepItem,
    FeatureCaseMinderUpdateParams,
  } from '@/models/caseManagement/featureCase';
  import { MoveMode, TableQueryParams } from '@/models/common';
  import { MinderEventName, MinderKeyEnum } from '@/enums/minderEnum';

  import useMinderBaseApi from './useMinderBaseApi';
  import { convertToFile } from '@/views/case-management/caseManagementFeature/components/utils';

  const props = defineProps<{
    moduleId: string;
    moduleName: string;
    modulesCount: Record<string, number>; // 模块数量
  }>();
  const emit = defineEmits<{
    (e: 'save'): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();
  const minderStore = useMinderStore();

  const hasEditPermission = hasAnyPermission(['FUNCTIONAL_CASE:READ+MINDER']);
  const {
    caseTag,
    moduleTag,
    stepTag,
    textDescTag,
    prerequisiteTag,
    remarkTag,
    caseOffspringTags,
    insertSiblingMenus,
    insertSonMenus,
    insertNode,
    handleBeforeExecCommand,
    stopPaste,
    canShowFloatMenu,
    checkNodeCanShowMenu,
    canShowMoreMenu,
    canShowPriorityMenu,
    handleContentChange,
    replaceableTags,
    priorityDisableCheck,
    canShowMoreMenuNodeOperation,
  } = useMinderBaseApi({ hasEditPermission });
  const importJson = ref<MinderJson>({
    root: {} as MinderJsonNode,
    treePath: [],
  });
  const largeModulesMap = ref<Record<string, MinderJsonNode[]>>({}); // 存储大数据量的模块节点
  const caseTree = ref<MinderJsonNode[]>([]);
  const loading = ref(false);
  const tempMinderParams = ref<FeatureCaseMinderUpdateParams>({
    projectId: appStore.currentProjectId,
    versionId: '',
    updateCaseList: [],
    updateModuleList: [],
    deleteResourceList: [],
    additionalNodeList: [],
  });
  const templateId = ref('');

  /**
   * 初始化用例模块树
   */
  async function initCaseTree() {
    try {
      loading.value = true;
      const res = await getCaseMinderTree({
        projectId: appStore.currentProjectId,
        moduleId: '', // 始终加载全部，然后再进入对应的模块节点
      });
      caseTree.value = mapTree<MinderJsonNode>(res, (e) => {
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
            text: e.name || e.data?.text || '',
            resource: props.modulesCount[e.id] !== undefined ? [moduleTag] : e.data?.resource,
            expandState: e.level === 0 ? 'expand' : 'collapse',
            count: props.modulesCount[e.id],
            isNew: false,
            changed: false,
            disabled: ['NONE', 'root'].includes(e.id || e.data?.id), // 全部模块节点和根节点不可编辑文本
          },
          children:
            props.modulesCount[e.id] > 0 && !e.children?.length
              ? [
                  {
                    data: {
                      id: 'fakeNode',
                      text: t('ms.minders.moreCase'),
                      resource: [],
                      isNew: false,
                      changed: false,
                    },
                  },
                ]
              : showingChildren,
        };
      });
      importJson.value.root = {
        children: caseTree.value,
        data: {
          id: 'NONE',
          text: t('caseManagement.caseReview.allCases'),
          resource: [moduleTag],
          disabled: true,
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
          minderStore.dispatchEvent(MinderEventName.ENTER_NODE, undefined, undefined, undefined, [
            importJson.value.root,
          ]);
        });
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  onMounted(() => {
    initCaseTree();
  });

  const baseInfoRef = ref<InstanceType<typeof baseInfo>>();
  const baseInfoLoading = ref(false);

  const extraVisible = ref<boolean>(false);
  const activeCase = ref<Record<string, any>>({});
  const extractContentTabList = computed(() => {
    const fullTabList = [
      {
        label: t('common.baseInfo'),
        value: 'baseInfo',
      },
      {
        label: t('caseManagement.featureCase.attachment'),
        value: 'attachment',
      },
      {
        value: 'comments',
        label: t('caseManagement.featureCase.comments'),
      },
      {
        value: 'bug',
        label: t('caseManagement.featureCase.bug'),
      },
    ];
    if (!activeCase.value.isNew) {
      return fullTabList;
    }
    return fullTabList.filter((item) => item.value === 'baseInfo');
  });
  const activeExtraKey = ref<'baseInfo' | 'attachment' | 'comments' | 'bug'>('baseInfo');

  const isContentChanging = ref(false);
  function handleMinderNodeContentChange(node?: MinderJsonNode) {
    isContentChanging.value = true;
    if (extraVisible.value) {
      // 已打开用例详情抽屉，更改用例节点文本时同步更新抽屉内的用例名称
      activeCase.value = {
        ...activeCase.value,
        name: node?.data?.text || window.minder.getNodeById(activeCase.value.id)?.data?.text || '',
      };
    }
    handleContentChange(node);
    setTimeout(() => {
      isContentChanging.value = false;
    }, 300); // 300ms 是脑图编辑器的 debounce 时间，300ms 后触发选中事件
  }

  const fileList = ref<MsFileItem[]>([]);
  const checkUpdateFileIds = ref<string[]>([]);

  const getListFunParams = ref<TableQueryParams>({
    combine: {
      hiddenIds: [],
    },
  });

  // 监视文件列表处理关联和本地文件
  watch(
    () => fileList.value,
    (val) => {
      if (val) {
        getListFunParams.value.combine.hiddenIds = fileList.value.filter((item) => !item.local).map((item) => item.uid);
      }
    },
    { deep: true }
  );

  /**
   * 初始化用例详情
   * @param data 节点数据/用例数据
   */
  async function initCaseDetail(data?: MinderJsonNodeData | Record<string, any>) {
    try {
      baseInfoLoading.value = true;
      const res = await getCaseDetail(data?.id || activeCase.value.id);
      activeCase.value = {
        ...res,
        isNew: false,
      };
      const fileIds = (res.attachments || []).map((item: any) => item.id) || [];
      if (fileIds.length) {
        checkUpdateFileIds.value = await checkFileIsUpdateRequest(fileIds);
      }
      if (res.attachments) {
        // 处理文件列表
        fileList.value = res.attachments
          .map((fileInfo: any) => {
            return {
              ...fileInfo,
              name: fileInfo.fileName,
              isUpdateFlag: checkUpdateFileIds.value.includes(fileInfo.id),
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

  function resetExtractInfo() {
    activeCase.value = {};
    fileList.value = [];
  }

  function handleBaseInfoCancel() {
    extraVisible.value = false;
    resetExtractInfo();
  }

  function handleBaseInfoSaved() {
    const node: MinderJsonNode = window.minder.getSelectedNode();
    if (node.data) {
      initCaseDetail(node.data);
    }
  }

  /**
   * 切换用例详情显示
   */
  async function toggleDetail(val?: boolean) {
    extraVisible.value = val !== undefined ? val : !extraVisible.value;
    const node: MinderJsonNode = window.minder.getSelectedNode();
    const { data } = node;
    if (extraVisible.value) {
      if (data?.resource && data.resource.includes(caseTag)) {
        activeExtraKey.value = 'baseInfo';
        resetExtractInfo();
        if (data.isNew === false) {
          // 非新用例节点才能加载详情
          initCaseDetail(data);
        } else {
          activeCase.value = {
            id: data.id,
            name: data.text,
            moduleId: node.parent?.data?.id || '',
            moduleIsNew: !!node.parent?.data?.isNew, // 标记父模块节点是否为新建
            isNew: true,
          };
        }
      }
    }
  }

  /**
   * 初始化模块节点下的用例节点
   * @param node 选中节点
   */
  async function initNodeCases(node: MinderJsonNode) {
    try {
      const { data } = node;
      if (!data) return;
      loading.value = true;
      const { list, total } = await getCaseMinder({
        projectId: appStore.currentProjectId,
        moduleId: data.id,
        current: 1,
      });
      const fakeNode = node.children?.find((e) => e.data?.id === 'fakeNode'); // 移除占位的虚拟节点
      if (fakeNode) {
        window.minder.removeNode(fakeNode);
      }
      if ((!list || list.length === 0) && node.children?.length) {
        // 如果模块下没有用例且有别的模块节点，正常展开
        node.expand();
        window.minder.renderNodeBatch(node.children);
        node.layout();
        data.isLoaded = true;
        return;
      }
      // TODO:递归渲染存在的子节点
      let waitingRenderNodes: MinderJsonNode[] = [];
      list.forEach((e) => {
        // 用例节点
        const child = window.minder.createNode(
          {
            ...e.data,
            text: e.data?.text.replace(/<\/?p\b[^>]*>/gi, '') || '',
            expandState: 'collapse',
            isNew: false,
          },
          node
        );
        waitingRenderNodes.push(child);
        const grandChildren: MinderJsonNode[] = [];
        e.children?.forEach((item) => {
          // 前置/步骤/备注节点
          const grandChild = window.minder.createNode(
            {
              ...item.data,
              text: item.data?.text.replace(/<\/?p\b[^>]*>/gi, '') || '',
              expandState: 'collapse',
              isNew: false,
            },
            child
          );
          grandChildren.push(grandChild);
          const greatGrandChildren: MinderJsonNode[] = [];
          item.children?.forEach((subItem) => {
            // 预期结果节点
            const greatGrandChild = window.minder.createNode(
              {
                ...subItem.data,
                text: subItem.data?.text.replace(/<\/?p\b[^>]*>/gi, '') || '',
                expandState: 'collapse',
                isNew: false,
              },
              grandChild
            );
            greatGrandChildren.push(greatGrandChild);
          });
          window.minder.renderNodeBatch(greatGrandChildren);
        });
        window.minder.renderNodeBatch(grandChildren);
      });
      node.expand();
      // node.renderTree();
      if (node.children && node.children.length > 0) {
        waitingRenderNodes = waitingRenderNodes.concat(node.children);
      }
      if (total > list.length) {
        // 有更多用例
        const moreNode = window.minder.createNode(
          {
            id: `tmp-${data.id}`,
            text: t('ms.minders.moreCase'),
            type: 'tmp',
            expandState: 'collapse',
            current: 1,
          },
          node
        );
        waitingRenderNodes.push(moreNode);
      }
      window.minder.renderNodeBatch(waitingRenderNodes);
      if (node.parent) {
        node.parent?.layout();
      } else {
        node.layout();
      }
      data.isLoaded = true;
      // 加载完用例数据后，更新当前importJson数据
      replaceNodeInTree([importJson.value.root], node.data?.id || '', window.minder.exportNode(node), 'data', 'id');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  /**
   * 加载模块下更多用例
   * @param node 模块节点
   * @param current 当前页码
   */
  async function loadMoreCases(node: MinderJsonNode, current: number) {
    try {
      const { data } = node;
      if (!data) return;
      loading.value = true;
      const { list, total } = await getCaseMinder({
        projectId: appStore.currentProjectId,
        moduleId: data.id,
        current: current + 1,
      });
      const fakeNode = node.children?.find((e) => e.data?.id === `tmp-${data.id}`); // 移除占位的虚拟节点
      if (fakeNode) {
        window.minder.removeNode(fakeNode);
      }
      // TODO:递归渲染存在的子节点
      let waitingRenderNodes: MinderJsonNode[] = [];
      list.forEach((e) => {
        // 用例节点
        const child = window.minder.createNode(
          {
            ...e.data,
            text: e.data?.text.replace(/<\/?p\b[^>]*>/gi, '') || '',
            expandState: 'collapse',
            isNew: false,
          },
          node
        );
        waitingRenderNodes.push(child);
        const grandChildren: MinderJsonNode[] = [];
        e.children?.forEach((item) => {
          // 前置/步骤/备注节点
          const grandChild = window.minder.createNode(
            {
              ...item.data,
              text: item.data?.text.replace(/<\/?p\b[^>]*>/gi, '') || '',
              expandState: 'collapse',
              isNew: false,
            },
            child
          );
          grandChildren.push(grandChild);
          const greatGrandChildren: MinderJsonNode[] = [];
          item.children?.forEach((subItem) => {
            // 预期结果节点
            const greatGrandChild = window.minder.createNode(
              {
                ...subItem.data,
                text: subItem.data?.text.replace(/<\/?p\b[^>]*>/gi, '') || '',
                expandState: 'collapse',
                isNew: false,
              },
              grandChild
            );
            greatGrandChildren.push(greatGrandChild);
          });
          window.minder.renderNodeBatch(greatGrandChildren);
        });
        window.minder.renderNodeBatch(grandChildren);
      });
      node.expand();
      if (node.children && node.children.length > 0) {
        waitingRenderNodes = waitingRenderNodes.concat(node.children);
      }
      if (total > 100 * (current + 1)) {
        // 有更多用例
        const moreNode = window.minder.createNode(
          {
            id: `tmp-${data.id}`,
            text: t('ms.minders.moreCase'),
            type: 'tmp',
            expandState: 'collapse',
            current: current + 1,
          },
          node
        );
        waitingRenderNodes.push(moreNode);
      }
      window.minder.renderNodeBatch(waitingRenderNodes);
      node.layout();
      data.isLoaded = true;
      // 加载完用例数据后，更新当前importJson数据
      replaceNodeInTree([importJson.value.root], node.data?.id || '', window.minder.exportNode(node), 'data', 'id');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
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
            count: props.modulesCount[e.id],
            isNew: false,
            changed: false,
          },
          node
        );
        if (props.modulesCount[e.id] > 0 && !e.children?.length) {
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
            type: 'tmpModule',
            text: t('ms.minders.moreModule'),
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

  const showDetailMenu = ref(false);
  const canShowEnterNode = ref(false);
  /**
   * 处理脑图节点激活/点击
   * @param node 被激活/点击的节点
   */
  async function handleNodeSelect(node: MinderJsonNode) {
    const { data } = node;
    checkNodeCanShowMenu(node);
    if (data?.type === 'tmpModule') {
      // 点击更多节点，加载更多模块
      loadMoreModules(node.parent?.data?.id || '');
      return;
    }
    if (data?.type === 'tmp' && node.parent?.data?.resource?.includes(moduleTag)) {
      // 点击更多节点，加载更多用例
      await loadMoreCases(node.parent, data.current);
      setPriorityView(true, 'P');
      return;
    }
    if (
      data?.resource?.includes(moduleTag) &&
      (node.children || []).length > 0 &&
      node.type !== 'root' &&
      !data.isNew
    ) {
      // 模块节点且有子节点且非根节点且非新建节点，可展示进入节点菜单
      canShowEnterNode.value = true;
    } else {
      canShowEnterNode.value = false;
    }
    if (data?.resource && data.resource.includes(caseTag)) {
      // 用例节点才展示详情按钮
      showDetailMenu.value = true;
      if (extraVisible.value) {
        toggleDetail(true);
      }
      // 用例下面所有节点都展开
      if (!isContentChanging.value) {
        expendNodeAndChildren(node);
        node.layout();
      }
    } else if (data?.resource?.includes(moduleTag) && data.count > 0 && data.isLoaded !== true) {
      // 模块节点且有用例且未加载过用例数据
      await initNodeCases(node);
      showDetailMenu.value = false;
      extraVisible.value = false;
    } else {
      // 文本节点或已加载过用例数据的模块节点
      extraVisible.value = false;
      showDetailMenu.value = false;
      resetExtractInfo();
      const fakeNode = node.children?.find((e) => e.data?.id === 'fakeNode'); // 移除占位的虚拟节点
      if (fakeNode) {
        window.minder.removeNode(fakeNode);
      }
    }
    setPriorityView(true, 'P');
  }

  const { unbindShortcuts } = useShortCut(
    {
      addChildModule: () => {
        const node: MinderJsonNode = window.minder.getSelectedNode();
        if (node.data?.resource?.includes(moduleTag) && node.data?.id !== 'root') {
          // 非未规划模块节点下才能添加子模块
          insertNode(node, 'AppendChildNode', moduleTag);
        }
      },
      addChildCase: () => {
        const node: MinderJsonNode = window.minder.getSelectedNode();
        if (node.data?.resource?.includes(moduleTag) && node.data?.id !== 'NONE') {
          // 模块节点且不是虚拟根模块节点下才能添加用例
          insertNode(node, 'AppendChildNode', caseTag);
        }
      },
      addSiblingModule: () => {
        const node: MinderJsonNode = window.minder.getSelectedNode();
        if (node.parent?.data?.resource?.includes(moduleTag) && node.parent?.data?.id !== 'root') {
          // 父节点是模块节点且不是未规划模块，才能添加兄弟模块节点
          insertNode(node, 'AppendSiblingNode', moduleTag);
        }
      },
      addSiblingCase: () => {
        const node: MinderJsonNode = window.minder.getSelectedNode();
        if (node.parent?.data?.resource?.includes(moduleTag) && node.parent?.data?.id !== 'NONE') {
          // 父节点是模块节点且不是虚拟根模块节点，才能添加兄弟用例节点
          insertNode(node, 'AppendSiblingNode', caseTag);
        }
      },
    },
    {}
  );

  /**
   * 标签编辑后，如果将标签修改为模块，则删除已添加的优先级
   * @param node 选中节点
   * @param tag 更改后的标签
   */
  function afterTagEdit(nodes: MinderJsonNode[], tag: string) {
    nodes.forEach((node, index) => {
      if (tag === moduleTag && node.data) {
        // 排除是从用例节点切换到模块节点的数据
        tempMinderParams.value.updateCaseList = tempMinderParams.value.updateCaseList.filter(
          (e) => e.id !== node.data?.id
        );
        node.data.isNew = true;
        window.minder.execCommand('priority');
        if (index === nodes.length - 1) {
          window.minder.toggleSelect(node);
        }
      } else if (tag === caseTag && node.data) {
        // 排除是从模块节点切换到用例节点的数据
        tempMinderParams.value.updateModuleList = tempMinderParams.value.updateModuleList.filter(
          (e) => e.id !== node.data?.id
        );
        node.data.isNew = true;
        if (index === nodes.length - 1) {
          window.minder.toggleSelect(node);
        }
      } else if (node.data?.resource?.some((e) => caseOffspringTags.includes(e))) {
        // 用例子孙节点更新，标记用例节点变化
        if (node.parent?.data?.resource?.includes(caseTag)) {
          node.parent.data.changed = true;
        } else if (node.parent?.parent?.data?.resource?.includes(caseTag)) {
          // 期望结果是第三层节点
          node.parent.parent.data.changed = true;
        }
      }
    });
  }

  /**
   * 处理脑图节点操作
   * @param event 脑图事件对象
   */
  function handleAction(event: MinderCustomEvent) {
    const { nodes, name } = event;
    if (nodes && nodes.length > 0) {
      switch (name) {
        case MinderEventName.DELETE_NODE:
        case MinderEventName.CUT_NODE:
          // TODO:循环优化
          nodes.forEach((node) => {
            if (!node.data?.resource || node.data?.resource.length === 0) {
              // 删除文本节点
              tempMinderParams.value.deleteResourceList.push({
                id: node.data?.id || '',
                type: 'NONE',
              });
            } else if (caseOffspringTags.some((e) => node.data?.resource?.includes(e))) {
              // 用例下的子孙节点的移除，标记用例节点变化
              const parentCase = tempMinderParams.value.updateCaseList.find((e) => e.id !== node.data?.id);
              if (!parentCase) {
                if (node.parent?.data?.resource?.includes(caseTag)) {
                  // 第二层子节点
                  node.parent.data.changed = true;
                } else if (node.parent?.parent?.data?.resource?.includes(caseTag)) {
                  // 第三层子节点
                  node.parent.parent.data.changed = true;
                }
              }
            } else if (!caseOffspringTags.some((e) => node.data?.resource?.includes(e))) {
              // 非用例下的子孙节点的移除，才加入删除资源队列
              tempMinderParams.value.deleteResourceList.push({
                id: node.data?.id || '',
                type: node.data?.resource?.[0] || moduleTag,
              });
            }
            if (node.data?.resource?.includes(caseTag)) {
              // 删除用例节点
              tempMinderParams.value.updateCaseList = tempMinderParams.value.updateCaseList.filter(
                (e) => e.id !== node.data?.id
              );
            } else if (node.data?.resource?.includes(moduleTag)) {
              // 删除模块节点
              tempMinderParams.value.updateModuleList = tempMinderParams.value.updateModuleList.filter(
                (e) => e.id !== node.data?.id
              );
            } else if (!node.data?.resource) {
              // 删除文本节点
              tempMinderParams.value.additionalNodeList = tempMinderParams.value.additionalNodeList.filter(
                (e) => e.id !== node.data?.id
              );
            }
          });
          break;
        default:
          break;
      }
    }
  }

  /**
   * 批量展开节点
   */
  function customBatchExpand(node: MinderJsonNode) {
    if (node.data?.resource?.includes(caseTag)) {
      expendNodeAndChildren(node);
      node.layout();
    }
  }

  /**
   * 判断是否显示批量展开按钮
   */
  function canShowBatchExpand() {
    if (window.minder) {
      const nodes: MinderJsonNode[] = window.minder.getSelectedNodes();
      return nodes.some((node) => !!node.data?.resource?.includes(caseTag));
    }
    return false;
  }

  /**
   * 解析用例节点信息
   * @param node 用例节点
   */
  function getCaseNodeInfo(node?: MinderJsonNode) {
    let textStep: MinderJsonNode | undefined; // 文本描述
    let prerequisiteNode: MinderJsonNode | undefined; // 前置操作
    let remarkNode: MinderJsonNode | undefined; // 备注
    const stepNodes: MinderJsonNode[] = []; // 步骤描述
    node?.children?.forEach((item) => {
      if (item.data?.resource?.includes(textDescTag)) {
        textStep = item;
      } else if (item.data?.resource?.includes(stepTag)) {
        stepNodes.push(item);
      } else if (item.data?.resource?.includes(prerequisiteTag)) {
        prerequisiteNode = item;
      } else if (item.data?.resource?.includes(remarkTag)) {
        remarkNode = item;
      }
    });
    const steps: FeatureCaseMinderStepItem[] = stepNodes.map((child, i) => {
      return {
        id: child.data?.id || getGenerateId(),
        num: i,
        desc: child.data?.text || '',
        result: child.children?.[0]?.data?.text || '',
      };
    });
    return {
      prerequisite: prerequisiteNode?.data?.text || '',
      caseEditType: steps.length > 0 ? 'STEP' : ('TEXT' as FeatureCaseMinderEditType),
      steps: JSON.stringify(steps),
      textDescription: textStep?.data?.text || '',
      expectedResult: textStep?.children?.[0]?.data?.text || '',
      description: remarkNode?.data?.text || '',
      priority: node?.data?.priority,
    };
  }

  /**
   * 获取节点的移动信息
   * @param node 节点
   * @param parent 父节点
   */
  function getNodeMoveInfo(nodeIndex: number, parent?: MinderJsonNode): { moveMode: MoveMode; targetId?: string } {
    const moveMode = nodeIndex === 0 ? 'BEFORE' : 'AFTER'; // 除了第一个以外，其他都是在目标节点后面插入
    if (!parent) {
      // 没有父节点的话，说明是根节点下的子节点
      parent = importJson.value.root;
    }

    return {
      moveMode,
      targetId:
        moveMode === 'BEFORE'
          ? parent?.children?.[1]?.data?.id
          : parent?.children?.[(nodeIndex || parent.children.length - 1) - 1]?.data?.id,
    };
  }

  function resetMinderParams(clearDeleteResource = true) {
    tempMinderParams.value = {
      projectId: appStore.currentProjectId,
      versionId: '',
      updateCaseList: [],
      updateModuleList: [],
      deleteResourceList: clearDeleteResource ? [] : tempMinderParams.value.deleteResourceList, // 请求错误的时候，删除的资源不清空，因为此时节点已经被脑图删除
      additionalNodeList: [],
    };
    if (clearDeleteResource) {
      traverseTree(importJson.value.root.children, (node) => {
        const minderNode: MinderJsonNode = window.minder.getNodeById(node.data.id);
        if (minderNode?.data) {
          // 能找到对应的脑图节点，重置 isNew 和 changed 状态
          minderNode.data.isNew = false;
          minderNode.data.changed = false;
        } else {
          // 找不到对应的脑图节点，说明当前是进入了模块层级，之前更改的节点没有渲染，重置源数据 isNew 和 changed 状态
          node.data.isNew = false;
          node.data.changed = false;
        }
        return true;
      });
    }
  }

  /**
   * 生成脑图保存的入参
   */
  function makeMinderParams(fullJson: MinderJson): FeatureCaseMinderUpdateParams {
    filterTree(fullJson.root.children, (node, nodeIndex, parent) => {
      if (node.data?.type !== 'tmp' && (node.data.isNew !== false || node.data.changed === true)) {
        if (node.data.resource?.includes(moduleTag)) {
          // 处理模块节点
          tempMinderParams.value.updateModuleList.push({
            id: node.data.id,
            name: node.data.text,
            parentId: parent?.data.id || 'NONE',
            type: node.data.isNew !== false ? 'ADD' : 'UPDATE',
            ...getNodeMoveInfo(nodeIndex, parent as MinderJsonNode),
          });
        } else if (node.data.resource?.includes(caseTag)) {
          // 处理用例节点
          const caseNodeInfo = getCaseNodeInfo(node as MinderJsonNode);
          let caseBaseInfo;
          if (activeCase.value.id === node.data.id) {
            // 当前用例节点是打开的用例详情，获取用例详情数据
            caseBaseInfo = baseInfoRef.value?.makeParams();
          }
          tempMinderParams.value.updateCaseList.push({
            id: node.data.id,
            moduleId: parent?.data.id || '',
            type: node.data.isNew !== false ? 'ADD' : 'UPDATE',
            templateId: templateId.value,
            tags: caseBaseInfo?.tags || [],
            customFields: caseBaseInfo?.customFields || [],
            name: caseBaseInfo?.name || node.data.text,
            ...getNodeMoveInfo(nodeIndex, parent as MinderJsonNode),
            ...caseNodeInfo,
          });
          return false; // 用例的子孙节点已经处理过，跳过
        } else if (!node.data.resource || node.data.resource.length === 0) {
          // 处理文本节点
          tempMinderParams.value.additionalNodeList.push({
            id: node.data.id,
            parentId: parent?.data.id || 'NONE',
            type: node.data.isNew !== false ? 'ADD' : 'UPDATE',
            name: node.data.text,
            ...getNodeMoveInfo(nodeIndex, parent as MinderJsonNode),
          });
        }
      } else if (node.data.resource?.includes(caseTag)) {
        // 处理用例节点（直接更改用例子孙节点可能没触发用例节点变化）
        let hasChangedSubNode = false;
        traverseTree(node.children, (child) => {
          if (child.data?.changed === true) {
            hasChangedSubNode = true;
            return false;
          }
          return true;
        });
        if (hasChangedSubNode) {
          const caseNodeInfo = getCaseNodeInfo(node as MinderJsonNode);
          let caseBaseInfo;
          if (activeCase.value.id === node.data.id) {
            // 当前用例节点是打开的用例详情，获取用例详情数据
            caseBaseInfo = baseInfoRef.value?.makeParams();
          }
          tempMinderParams.value.updateCaseList.push({
            id: node.data.id,
            moduleId: parent?.data.id || '',
            type: 'UPDATE',
            templateId: templateId.value,
            tags: caseBaseInfo?.tags || [],
            customFields: caseBaseInfo?.customFields || [],
            name: caseBaseInfo?.name || node.data.text,
            ...getNodeMoveInfo(nodeIndex, parent as MinderJsonNode),
            ...caseNodeInfo,
          });
        }
        return false; // 用例的子孙节点已经处理过，跳过
      }
      return true;
    });
    return tempMinderParams.value;
  }

  /**
   * 处理脑图保存
   * @param fullJson 脑图导出的完整数据
   * @param callback 保存成功回调
   */
  async function handleMinderSave(fullJson: MinderJson, callback: (refresh: boolean) => void) {
    try {
      loading.value = true;
      await saveCaseMinder(makeMinderParams(fullJson));
      extraVisible.value = false;
      Message.success(t('common.saveSuccess'));
      resetMinderParams();
      emit('save');
      callback(false);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      resetMinderParams(false);
    } finally {
      loading.value = false;
    }
  }

  watch(
    () => props.moduleId,
    () => {
      initCaseTree();
    },
    {
      deep: true,
    }
  );

  onBeforeUnmount(() => {
    unbindShortcuts();
  });
</script>

<style lang="less" scoped></style>
