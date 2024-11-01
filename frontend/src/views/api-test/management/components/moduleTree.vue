<template>
  <div>
    <template v-if="!props.isModal">
      <div v-if="!props.readOnly && !props.trash && !props.docShareId" class="mb-[8px] flex items-center gap-[8px]">
        <a-button
          v-permission="['PROJECT_API_DEFINITION:READ+ADD']"
          type="primary"
          long
          @click="handleSelect('newApi')"
        >
          {{ t('apiTestManagement.newApi') }}
        </a-button>
        <a-button
          v-permission="['PROJECT_API_DEFINITION:READ+IMPORT']"
          type="outline"
          long
          @click="handleSelect('import')"
        >
          {{ t('apiTestManagement.importApi') }}
        </a-button>
      </div>
      <a-input
        v-model:model-value="moduleKeyword"
        :placeholder="props.isModal ? t('apiTestManagement.moveSearchTip') : t('apiTestManagement.searchTip')"
        class="mb-[8px]"
        allow-clear
      />
      <TreeFolderAll
        v-if="!props.readOnly"
        ref="treeFolderAllRef"
        v-model:isExpandApi="isExpandApi"
        v-model:isExpandAll="isExpandAll"
        :protocol-key="ProtocolKeyEnum.API_MODULE_TREE_PROTOCOL"
        :folder-name="t('apiTestManagement.allApi')"
        :all-count="allFileCount"
        :active-folder="selectedKeys[0] as string"
        :show-expand-api="!props.readOnly && !props.trash && !props.docShareId"
        :not-show-operation="!!props.docShareId"
        @set-active-folder="setActiveFolder"
        @change-api-expand="changeApiExpand"
        @selected-protocols-change="selectedProtocolsChange"
      >
        <template #expandRight>
          <popConfirm
            v-if="
              hasAnyPermission(['PROJECT_API_DEFINITION:READ+ADD']) &&
              !props.readOnly &&
              !props.trash &&
              !props.docShareId
            "
            mode="add"
            :all-names="rootModulesName"
            parent-id="NONE"
            :add-module-api="addModule"
            @add-finish="handleAddFinish"
          >
            <MsButton v-if="!props.docShareId" type="icon" class="!mr-0 p-[2px]">
              <MsIcon
                type="icon-icon_create_planarity"
                size="18"
                class="text-[rgb(var(--primary-5))] hover:text-[rgb(var(--primary-4))]"
              />
            </MsButton>
          </popConfirm>
          <a-tooltip v-if="props.docShareId && shareDetailInfo.allowExport" :content="t('common.export')">
            <MsButton type="icon" status="secondary" class="!mr-[4px] p-[4px]" @click="changeApiExpand">
              <MsIcon type="icon-icon_top-align_outlined" :size="16" @click="exportShare" />
            </MsButton>
          </a-tooltip>
        </template>
      </TreeFolderAll>
    </template>
    <a-input
      v-else
      v-model:model-value="moduleKeyword"
      :placeholder="props.isModal ? t('apiTestManagement.moveSearchTip') : t('apiTestManagement.searchTip')"
      class="mb-[16px]"
      allow-clear
    />
    <a-spin class="w-full" :loading="loading">
      <MsTree
        v-model:focus-node-key="focusNodeKey"
        v-model:selected-keys="selectedKeys"
        :data="folderTree"
        :keyword="moduleKeyword"
        :node-more-actions="folderMoreActions"
        :default-expand-all="isExpandAll"
        :expand-all="isExpandAll"
        :empty-text="props.isModal ? t('apiTestManagement.noMatchModule') : t('apiTestManagement.noMatchModuleAndApi')"
        :draggable="!props.readOnly && !props.isModal && hasAnyPermission(['PROJECT_API_DEFINITION:READ+UPDATE'])"
        :virtual-list-props="virtualListProps"
        :field-names="{
          title: 'name',
          key: 'id',
          children: 'children',
          count: 'count',
        }"
        :filter-more-action-func="filterMoreActionFunc"
        :allow-drop="allowDrop"
        block-node
        @select="folderNodeSelect"
        @more-action-select="handleFolderMoreSelect"
        @more-actions-close="moreActionsClose"
        @drop="handleDrop"
      >
        <template #title="nodeData">
          <div v-if="nodeData.type === 'API'" class="inline-flex w-full cursor-pointer gap-[4px]">
            <apiMethodName :method="nodeData.attachInfo?.method || nodeData.attachInfo?.protocol" />
            <div class="one-line-text w-full text-[var(--color-text-1)]">{{ nodeData.name }}</div>
          </div>
          <div v-else :id="nodeData.id" class="inline-flex w-full gap-[8px]">
            <div class="one-line-text w-full text-[var(--color-text-1)]">{{ nodeData.name }}</div>
            <div v-if="!props.isModal" class="ms-tree-node-count ml-[4px] text-[var(--color-text-brand)]">
              {{ modulesCount[nodeData.id] || 0 }}
            </div>
          </div>
        </template>
        <template v-if="!props.readOnly && !props.isModal" #extra="nodeData">
          <!-- 默认模块的 id 是root，默认模块不可编辑、不可添加子模块 -->
          <popConfirm
            v-if="nodeData.id !== 'root' && nodeData.type === 'MODULE'"
            mode="add"
            :all-names="(nodeData.children || []).map((e: ModuleTreeNode) => e.name || '')"
            :parent-id="nodeData.id"
            :add-module-api="addModule"
            @close="resetFocusNodeKey"
            @add-finish="handleAddFinish"
          >
            <MsButton type="icon" size="mini" class="ms-tree-node-extra__btn !mr-0" @click="setFocusNodeKey(nodeData)">
              <MsIcon type="icon-icon_add_outlined" size="14" class="text-[var(--color-text-4)]" />
            </MsButton>
          </popConfirm>
          <popConfirm
            v-if="nodeData.id !== 'root'"
            mode="rename"
            :node-type="nodeData.type"
            :parent-id="nodeData.id"
            :node-id="nodeData.id"
            :field-config="{ field: renameFolderTitle }"
            :all-names="(nodeData.parent? nodeData.parent.children || [] : folderTree).map((e: ModuleTreeNode) => e.name || '')"
            :update-module-api="updateModule"
            :update-api-node-api="updateDefinition"
            @close="resetFocusNodeKey"
            @rename-finish="handleRenameFinish"
          >
            <div :id="`renameSpan${nodeData.id}`" class="relative h-full"></div>
          </popConfirm>
        </template>
      </MsTree>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
  import { computed, onBeforeMount, ref, watch } from 'vue';
  import { useClipboard } from '@vueuse/core';
  import { Message, SelectOptionData } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import popConfirm from '@/views/api-test/components/popConfirm.vue';
  import TreeFolderAll from '@/views/api-test/components/treeFolderAll.vue';

  import { getProtocolList } from '@/api/modules/api-test/common';
  import {
    addModule,
    deleteDefinition,
    deleteModule,
    getModuleCount,
    getModuleTree,
    getModuleTreeOnlyModules,
    getShareModuleCount,
    getShareModuleTree,
    getTrashModuleCount,
    getTrashModuleTree,
    moveModule,
    sortDefinition,
    updateDefinition,
    updateModule,
  } from '@/api/modules/api-test/management';
  import { dropPositionMap } from '@/config/common';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useAppStore from '@/store/modules/app';
  import { characterLimit, mapTree, TreeNode } from '@/utils';
  import { getLocalStorage } from '@/utils/local-storage';
  import { hasAnyPermission } from '@/utils/permission';

  import { ApiDefinitionGetModuleParams, ShareDetailType } from '@/models/apiTest/management';
  import { ModuleTreeNode } from '@/models/common';
  import { ProtocolKeyEnum } from '@/enums/apiEnum';

  const props = withDefaults(
    defineProps<{
      isExpandAll?: boolean; // 是否展开所有节点
      activeModule?: string | number; // 选中的节点 key
      readOnly?: boolean; // 是否是只读模式
      activeNodeId?: string | number; // 当前选中节点 id
      isModal?: boolean; // 是否弹窗模式，只读且只可见模块树
      trash?: boolean; // 是否是回收站
      docShareId?: string; // 是否分享文档
    }>(),
    {
      activeModule: 'all',
      readOnly: false,
      isModal: false,
      trash: false,
    }
  );
  const emit = defineEmits([
    'init',
    'newApi',
    'import',
    'folderNodeSelect',
    'clickApiNode',
    'changeProtocol',
    'updateApiNode',
    'deleteNode',
    'execute',
    'openCurrentNode',
    'exportShare',
  ]);

  const appStore = useAppStore();
  const { t } = useI18n();
  const { openModal } = useModal();
  const { copy, isSupported } = useClipboard({ legacy: true });

  const treeFolderAllRef = ref<InstanceType<typeof TreeFolderAll>>();
  const selectedProtocols = computed<string[]>(() => treeFolderAllRef.value?.selectedProtocols ?? []);
  const moduleProtocolOptions = ref<SelectOptionData[]>([]);
  const protocolLoading = ref(false);

  async function initProtocolList() {
    try {
      protocolLoading.value = true;
      const res = await getProtocolList(appStore.currentOrgId);
      moduleProtocolOptions.value = res.map((e) => ({
        label: e.protocol,
        value: e.protocol,
        polymorphicName: e.polymorphicName,
        pluginId: e.pluginId,
      }));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      protocolLoading.value = false;
    }
  }

  function handleSelect(value: string | number | Record<string, any> | undefined) {
    switch (value) {
      case 'newApi':
        emit('newApi');
        break;
      case 'import':
        emit('import');
        break;
      default:
        break;
    }
  }

  const virtualListProps = computed(() => {
    if (props.readOnly || props.isModal) {
      return {
        height: 'calc(60vh - 190px)',
        threshold: 200,
        fixedSize: true,
        buffer: 15, // 缓冲区默认 10 的时候，虚拟滚动的底部 padding 计算有问题
      };
    }
    return {
      height: props.docShareId ? 'calc(100vh - 193px)' : 'calc(100vh - 273px)',
      threshold: 200,
      fixedSize: true,
      buffer: 15, // 缓冲区默认 10 的时候，虚拟滚动的底部 padding 计算有问题
    };
  });

  const moduleKeyword = ref(''); // 只用于前端过滤树节点，不传入后台查询！！！
  const folderTree = ref<TreeNode<ModuleTreeNode>[]>([]);
  const focusNodeKey = ref<string | number>('');
  const selectedKeys = ref<Array<string | number>>([props.activeModule]);
  const loading = ref(false);

  function setActiveFolder(id: string) {
    selectedKeys.value = [id];
    emit('folderNodeSelect', selectedKeys.value, []);
  }

  watch(
    () => props.activeNodeId,
    (val) => {
      if (val) {
        selectedKeys.value = [val];
      }
    }
  );

  function setFocusNodeKey(node: MsTreeNodeData) {
    focusNodeKey.value = node.id || '';
  }

  const folderMoreActions: ActionsItem[] = [
    {
      label: 'common.rename',
      eventTag: 'rename',
    },
    {
      label: 'apiTestManagement.execute',
      eventTag: 'execute',
    },
    {
      label: 'apiTestManagement.share',
      eventTag: 'share',
    },
    // {
    //   label: 'apiTestManagement.shareModule',
    //   eventTag: 'shareModule',
    // }, // TODO:第一版没有文档，不需要分享模块
    {
      isDivider: true,
    },
    {
      label: 'common.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];
  const moduleActions = folderMoreActions.filter(
    (action) => action.eventTag === undefined || !['execute', 'share'].includes(action.eventTag)
  );
  const apiActions = folderMoreActions.filter((action) => action.eventTag !== 'shareModule');

  function filterMoreActionFunc(actions: ActionsItem[], node: MsTreeNodeData) {
    if (node.type === 'MODULE') {
      return moduleActions;
    }
    return apiActions;
  }

  const modulesCount = ref<Record<string, number>>({});
  const allFileCount = computed(() => modulesCount.value.all || 0);
  const isExpandAll = ref(props.isExpandAll);
  const rootModulesName = ref<string[]>([]); // 根模块名称列表
  const isExpandApi = ref(getLocalStorage('isExpandApi') === 'true');
  const lastModuleCountParam = ref<ApiDefinitionGetModuleParams>({
    projectId: appStore.currentProjectId,
    keyword: '',
    protocols: selectedProtocols.value,
    moduleIds: [],
  });
  // 分享详情
  const shareDetailInfo = inject<Ref<ShareDetailType>>(
    'shareDetailInfo',
    ref({
      invalid: false,
      allowExport: false,
      isPrivate: false,
    })
  );

  const apiNodes = ref<TreeNode<ModuleTreeNode>[]>([]);
  const currentNode = ref<TreeNode<ModuleTreeNode> | null>(null);
  // 设置当前节点
  const setCurrentNode = (id: string, isSelectedNode = false) => {
    currentNode.value = apiNodes.value.find((node) => node.id === id) || null;
    selectedKeys.value = [id];
    emit('openCurrentNode', currentNode.value, apiNodes.value, isSelectedNode);
  };

  const getTreeNodeList = (nodes: TreeNode<ModuleTreeNode>[]) => {
    nodes.forEach((node: TreeNode<ModuleTreeNode>) => {
      if (node.type === 'API') {
        apiNodes.value.push(node);
      }
      if (node.children) {
        getTreeNodeList(node.children);
      }
    });
  };

  async function initModuleCount(params: ApiDefinitionGetModuleParams) {
    try {
      lastModuleCountParam.value = params;
      lastModuleCountParam.value.protocols = selectedProtocols.value;
      let res;
      if (props.trash) {
        res = await getTrashModuleCount(params);
      } else {
        res = await getModuleCount(params);
      }
      modulesCount.value = res;
      folderTree.value = mapTree<ModuleTreeNode>(folderTree.value, (node) => {
        return {
          ...node,
          count: res[node.id] || 0,
          draggable: node.id !== 'root' && !(props.readOnly || props.isModal),
          disabled: props.readOnly || props.isModal ? node.id === selectedKeys.value[0] : false,
        };
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  /**
   * 处理文件夹树节点选中事件
   */
  function folderNodeSelect(_selectedKeys: (string | number)[], node: MsTreeNodeData) {
    if (node.type === 'MODULE') {
      const offspringIds: string[] = [];
      mapTree(node.children || [], (e) => {
        offspringIds.push(e.id);
        return e;
      });
      emit('folderNodeSelect', _selectedKeys, offspringIds);
    } else if (node.type === 'API') {
      emit('clickApiNode', node);
      if (props.docShareId) {
        setCurrentNode(node.id, true);
      }
    }
  }
  // 分享模块count
  async function initShareModuleCount(params: ApiDefinitionGetModuleParams) {
    try {
      modulesCount.value = await getShareModuleCount({
        ...params,
        shareId: props.docShareId,
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
  // 分享模块树
  async function initShareModuleTree() {
    await initShareModuleCount({ ...lastModuleCountParam.value, orgId: appStore.currentOrgId, protocols: [] });
    let res;
    res = await getShareModuleTree({
      keyword: '',
      protocols: [],
      projectId: appStore.currentProjectId,
      moduleIds: [],
      orgId: appStore.currentOrgId,
      shareId: props.docShareId,
    });
    res = mapTree<ModuleTreeNode>(res, (node) => {
      const mappedNode = {
        ...node,
        count: modulesCount.value[node.id] || 0,
        draggable: false,
        disabled: false,
        hideMoreAction: true,
      };
      return modulesCount.value[node.id] === 0 && node.type === 'MODULE' ? null : mappedNode;
    });
    return res;
  }

  /**
   * 初始化模块树
   * @param isSetDefaultKey 是否设置第一个节点为选中节点
   */
  async function initModules(isSetDefaultKey = false) {
    try {
      loading.value = true;
      let res;
      if (props.docShareId) {
        res = await initShareModuleTree();
        folderTree.value = res;
        getTreeNodeList(folderTree.value);
        if (apiNodes.value.length) {
          setCurrentNode(apiNodes.value[0].id);
        }
      } else if (props.trash) {
        res = await getTrashModuleTree({
          // 回收站下的模块
          keyword: '',
          protocols: selectedProtocols.value,
          projectId: appStore.currentProjectId,
          moduleIds: [],
        });
      } else if (isExpandApi.value && !props.readOnly) {
        // 查看模块及模块下的请求
        res = await getModuleTree({
          keyword: '',
          protocols: selectedProtocols.value,
          projectId: appStore.currentProjectId,
          moduleIds: [],
        });
      } else {
        res = await getModuleTreeOnlyModules({
          // 只查看模块
          keyword: '',
          protocols: selectedProtocols.value,
          projectId: appStore.currentProjectId,
          moduleIds: [],
        });
      }
      const nodePathObj: Record<string, any> = {};
      if (props.readOnly || props.isModal) {
        folderTree.value = mapTree<ModuleTreeNode>(res, (e, fullPath) => {
          // 拼接当前节点的完整路径
          nodePathObj[e.id] = {
            path: e.path,
            fullPath,
          };
          return {
            ...e,
            hideMoreAction: true,
            draggable: false,
            disabled: e.id === selectedKeys.value[0],
          };
        });
      } else if (!props.docShareId) {
        folderTree.value = mapTree<ModuleTreeNode>(res, (e, fullPath) => {
          // 拼接当前节点的完整路径
          nodePathObj[e.id] = {
            path: e.path,
            fullPath,
          };
          return {
            ...e,
            hideMoreAction: e.id === 'root',
            draggable: e.id !== 'root',
          };
        });
      }
      if (isSetDefaultKey) {
        selectedKeys.value = [folderTree.value[0].id];
      }
      emit('init', folderTree.value, selectedProtocols.value, nodePathObj);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
      if (!props.docShareId) {
        initModuleCount(lastModuleCountParam.value);
      }
    }
  }

  function selectedProtocolsChange() {
    emit('changeProtocol', selectedProtocols.value);
    lastModuleCountParam.value.protocols = selectedProtocols.value;
    initModules();
  }

  // 获取上一条ID
  const getPreviousApiId = () => {
    if (!currentNode.value) return null;
    const index = apiNodes.value.indexOf(currentNode.value);
    return index > 0 ? apiNodes.value[index - 1].id : null;
  };

  // 获取下一条ID
  const getNextApiId = () => {
    if (!currentNode.value) return null;
    const index = apiNodes.value.indexOf(currentNode.value);
    return index < apiNodes.value.length - 1 ? apiNodes.value[index + 1].id : null;
  };

  // 上一条
  const previousApi = () => {
    const previousId = getPreviousApiId();
    if (previousId) {
      setCurrentNode(previousId);
    }
  };

  // 下一条
  const nextApi = () => {
    const nextId = getNextApiId();
    if (nextId) {
      setCurrentNode(nextId);
    }
  };

  watch(
    () => props.isExpandAll,
    (val) => {
      isExpandAll.value = val;
    }
  );

  function changeApiExpand() {
    initModules();
  }

  /**
   * 删除文件夹
   * @param node 节点信息
   */
  function deleteFolder(node: MsTreeNodeData) {
    openModal({
      type: 'error',
      title:
        node.type === 'API'
          ? t('apiTestDebug.deleteDebugTipTitle', { name: characterLimit(node.name) })
          : t('apiTestDebug.deleteFolderTipTitle', { name: characterLimit(node.name) }),
      content: node.type === 'API' ? t('apiTestDebug.deleteDebugTipContent') : t('apiTestDebug.deleteFolderTipContent'),
      okText: t('apiTestDebug.deleteConfirm'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          if (node.type === 'API') {
            await deleteDefinition(node.id);
          } else {
            await deleteModule(node.id);
          }
          Message.success(t('apiTestDebug.deleteSuccess'));
          emit('deleteNode', node.id, node.type === 'MODULE');
          initModules();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  const renamePopVisible = ref(false);
  const renameFolderTitle = ref(''); // 重命名的文件夹名称

  function resetFocusNodeKey() {
    focusNodeKey.value = '';
    renamePopVisible.value = false;
    renameFolderTitle.value = '';
  }

  function share(id: string) {
    if (isSupported) {
      const url = window.location.href;
      const dIdParam = `&dId=${id}`;
      const copyUrl = url.includes('dId') ? url.split('&dId')[0] : url;
      copy(`${copyUrl}${dIdParam}`);
      Message.success(t('apiTestManagement.shareUrlCopied'));
    } else {
      Message.error(t('common.copyNotSupport'));
    }
  }

  /**
   * 处理树节点更多按钮事件
   * @param item
   */
  function handleFolderMoreSelect(item: ActionsItem, node: MsTreeNodeData) {
    switch (item.eventTag) {
      case 'delete':
        deleteFolder(node);
        resetFocusNodeKey();
        break;
      case 'rename':
        renameFolderTitle.value = node.name || '';
        renamePopVisible.value = true;
        document.querySelector(`#renameSpan${node.id}`)?.dispatchEvent(new Event('click'));
        break;
      case 'share':
        share(node.id);
        break;
      case 'execute':
        emit('execute', node.id);
        break;
      default:
        break;
    }
  }

  function allowDrop(dropNode: MsTreeNodeData, dropPosition: number, dragNode?: MsTreeNodeData | null) {
    if (dropNode.type === 'API' && dropPosition === 0) {
      // API节点不可添加子节点
      return false;
    }
    if (dropNode.type === 'MODULE' && dragNode?.type === 'API' && dropPosition !== 0) {
      // API节点不移动到模块的前后位置
      document.querySelector('.arco-tree-node-title-draggable::before')?.setAttribute('style', 'display: none');
      return false;
    }
    return true;
  }

  /**
   * 处理文件夹树节点拖拽事件
   * @param tree 树数据
   * @param dragNode 拖拽节点
   * @param dropNode 释放节点
   * @param dropPosition 释放位置
   */
  async function handleDrop(
    tree: MsTreeNodeData[],
    dragNode: MsTreeNodeData,
    dropNode: MsTreeNodeData,
    dropPosition: number
  ) {
    if (dragNode.id === 'root' || (dragNode.type === 'MODULE' && dropNode.id === 'root')) {
      // 根节点不可拖拽；模块不可拖拽到根节点
      return;
    }
    try {
      loading.value = true;
      if (dragNode.type === 'MODULE') {
        await moveModule({
          dragNodeId: dragNode.id as string,
          dropNodeId: dropNode.id || '',
          dropPosition,
        });
      } else {
        await sortDefinition({
          projectId: appStore.currentProjectId,
          moveMode: dropPositionMap[dropPosition],
          moveId: dragNode.id,
          targetId: dropNode.type === 'MODULE' ? dragNode.id : dropNode.id, // 释放节点是模块，则传入当前拖动的 API 的id；释放节点是 API 节点的话就传入释放节点的 id
          moduleId: dropNode.type === 'API' ? dropNode.parentId : dropNode.id, // 释放节点是 API，则传入它所属模块id；模块的话直接是模块id
        });
        emit('updateApiNode', { ...dragNode, moduleId: dropNode.type === 'API' ? dropNode.parentId : dropNode.id });
      }
      Message.success(t('apiTestDebug.moduleMoveSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
      await initModules();
    }
  }

  function handleAddFinish() {
    initModules();
  }

  function handleRenameFinish(newName: string, id: string) {
    emit('updateApiNode', { name: newName, id });
    initModules();
  }

  function moreActionsClose() {
    if (!renamePopVisible.value) {
      // 当下拉菜单关闭时，若不是触发重命名气泡显示，则清空聚焦节点 key
      resetFocusNodeKey();
    }
  }

  // 导出分享
  function exportShare() {
    emit('exportShare', true);
  }

  onBeforeMount(() => {
    if (!props.docShareId) {
      initProtocolList();
    }
  });

  async function refresh() {
    await initModules();
  }

  watch(
    () => props.docShareId,
    (val) => {
      if (val) {
        initModules();
      }
    },
    {
      immediate: true,
    }
  );

  defineExpose({
    refresh,
    initModuleCount,
    setActiveFolder,
    setCurrentNode,
    previousApi,
    nextApi,
  });
</script>

<style lang="less" scoped>
  .folder {
    @apply flex cursor-pointer items-center justify-between;

    padding: 8px 4px;
    border-radius: var(--border-radius-small);
    &:hover {
      background-color: rgb(var(--primary-1));
    }
    .folder-text {
      @apply flex flex-1 cursor-pointer items-center;
      .folder-icon {
        margin-right: 4px;
        color: var(--color-text-4);
      }
      .folder-name {
        color: var(--color-text-1);
      }
      .folder-count {
        margin-left: 4px;
        color: var(--color-text-4);
      }
    }
    .folder-text--active {
      .folder-icon,
      .folder-name,
      .folder-count {
        color: rgb(var(--primary-5));
      }
    }
  }
  :deep(#root ~ .arco-tree-node-drag-icon) {
    @apply hidden;
  }
</style>
