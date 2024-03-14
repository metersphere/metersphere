<template>
  <div>
    <div class="mb-[8px] flex items-center gap-[8px]">
      <a-input
        v-model:model-value="moduleKeyword"
        :placeholder="t('apiScenario.tree.selectorPlaceholder')"
        allow-clear
      />
      <a-button v-permission="['[PROJECT_API_SCENARIO:READ+ADD]']" type="primary" value="newScenario">{{
        t('apiScenario.createScenario')
      }}</a-button>
    </div>
    <div class="folder" @click="setActiveFolder('all')">
      <div :class="allFolderClass">
        <MsIcon type="icon-icon_folder_filled1" class="folder-icon" />
        <div class="folder-name">{{ t('apiScenario.tree.folder.allScenario') }}</div>
        <div class="folder-count">({{ allScenarioCount }})</div>
      </div>
      <div class="ml-auto flex items-center">
        <a-tooltip :content="isExpandAll ? t('common.collapseAll') : t('common.expandAll')">
          <MsButton type="icon" status="secondary" class="!mr-0 p-[4px]" @click="changeExpand">
            <MsIcon :type="isExpandAll ? 'icon-icon_folder_collapse1' : 'icon-icon_folder_expansion1'" />
          </MsButton>
        </a-tooltip>
        <template v-if="!props.readOnly">
          <a-dropdown @select="handleSelect">
            <MsButton v-permission="['[PROJECT_API_SCENARIO:READ+ADD]']" type="icon" class="!mr-0 p-[2px]">
              <MsIcon
                type="icon-icon_create_planarity"
                size="18"
                class="text-[rgb(var(--primary-5))] hover:text-[rgb(var(--primary-4))]"
              />
            </MsButton>
            <template #content>
              <a-doption value="newScenario">{{ t('apiScenario.createScenario') + '（暂未实现）' }}</a-doption>
              <a-doption value="addModule">{{ t('apiScenario.createSubModule') }}</a-doption>
            </template>
          </a-dropdown>
          <popConfirm
            mode="add"
            :all-names="rootModulesName"
            parent-id="NONE"
            :add-module-api="addModule"
            @add-finish="initModules"
          >
            <span id="addModulePopSpan"></span>
          </popConfirm>
        </template>
      </div>
    </div>
    <a-divider class="my-[8px]" />

    <a-spin class="w-full" :loading="loading">
      <MsTree
        v-model:focus-node-key="focusNodeKey"
        v-model:selected-keys="selectedKeys"
        :data="folderTree"
        :keyword="moduleKeyword"
        :node-more-actions="folderMoreActions"
        :default-expand-all="isExpandAll"
        :expand-all="isExpandAll"
        :empty-text="t('apiScenario.tree.noMatchModule')"
        :virtual-list-props="virtualListProps"
        :field-names="{
          title: 'name',
          key: 'id',
          children: 'children',
          count: 'count',
        }"
        :draggable="!props.readOnly && hasAnyPermission(['PROJECT_API_SCENARIO:READ+UPDATE'])"
        block-node
        title-tooltip-position="left"
        @select="folderNodeSelect"
        @more-action-select="handleFolderMoreSelect"
        @more-actions-close="moreActionsClose"
        @drop="handleDrop"
      >
        <template #title="nodeData">
          <div :id="nodeData.id" class="inline-flex w-full">
            <div class="one-line-text w-[calc(100%-32px)] text-[var(--color-text-1)]">{{ nodeData.name }}</div>
            <div class="ml-[4px] text-[var(--color-text-4)]">({{ nodeData.count || 0 }})</div>
          </div>
        </template>
        <template v-if="!props.readOnly" #extra="nodeData">
          <!-- 默认模块的 id 是root，默认模块不可编辑、不可添加子模块 -->
          <popConfirm
            v-if="nodeData.id !== 'root' && nodeData.type === 'MODULE'"
            mode="add"
            :all-names="(nodeData.children || []).map((e: ModuleTreeNode) => e.name || '')"
            :parent-id="nodeData.id"
            :add-module-api="addModule"
            @close="resetFocusNodeKey"
            @add-finish="() => initModules()"
          >
            <MsButton
              v-permission="['PROJECT_API_SCENARIO:READ+ADD']"
              type="icon"
              size="mini"
              class="ms-tree-node-extra__btn !mr-0"
              @click="setFocusNodeKey(nodeData)"
            >
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
            :all-names="(nodeData.children || []).map((e: ModuleTreeNode) => e.name || '')"
            :update-module-api="updateModule"
            @close="resetFocusNodeKey"
            @rename-finish="initModules"
          >
            <span :id="`renameSpan${nodeData.id}`" class="relative"></span>
          </popConfirm>
        </template>
      </MsTree>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
  import { computed, onBeforeMount, ref, watch } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import popConfirm from '@/views/api-test/components/popConfirm.vue';

  import {
    addModule,
    deleteModule,
    getModuleCount,
    getModuleTree,
    moveModule,
    updateModule,
  } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useAppStore from '@/store/modules/app';
  import { mapTree } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { ApiScenarioGetModuleParams } from '@/models/apiTest/scenario';
  import { ModuleTreeNode } from '@/models/common';

  const props = withDefaults(
    defineProps<{
      isExpandAll?: boolean; // 是否展开所有节点
      isShowScenario?: boolean; // 是否显示挂载的场景
      activeModule?: string | number; // 选中的节点 key
      readOnly?: boolean; // 是否是只读模式
      activeNodeId?: string | number; // 当前选中节点 id
    }>(),
    {
      activeModule: 'all',
      readOnly: false,
    }
  );
  const emit = defineEmits(['init', 'newScenario', 'import', 'folderNodeSelect', 'clickScenario', 'changeProtocol']);

  const appStore = useAppStore();
  const { t } = useI18n();
  const { openModal } = useModal();

  function handleSelect(value: string | number | Record<string, any> | undefined) {
    switch (value) {
      case 'newScenario':
        emit('newScenario');
        break;
      case 'import':
        emit('import');
        break;
      case 'addModule':
        document.querySelector('#addModulePopSpan')?.dispatchEvent(new Event('click'));
        break;
      default:
        break;
    }
  }

  const virtualListProps = computed(() => {
    if (props.readOnly) {
      return {
        height: 'calc(60vh - 325px)',
        threshold: 200,
        fixedSize: true,
        buffer: 15, // 缓冲区默认 10 的时候，虚拟滚动的底部 padding 计算有问题
      };
    }
    return {
      height: 'calc(100vh - 325px)',
      threshold: 200,
      fixedSize: true,
      buffer: 15, // 缓冲区默认 10 的时候，虚拟滚动的底部 padding 计算有问题
    };
  });

  const moduleKeyword = ref('');
  const folderTree = ref<ModuleTreeNode[]>([]);
  const focusNodeKey = ref<string | number>('');
  const selectedKeys = ref<Array<string | number>>([props.activeModule]);
  const allFolderClass = computed(() =>
    selectedKeys.value[0] === 'all' ? 'folder-text folder-text--active' : 'folder-text'
  );
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
      permission: ['PROJECT_API_SCENARIO:READ+UPDATE'],
    },
    {
      isDivider: true,
    },
    {
      label: 'common.delete',
      eventTag: 'delete',
      permission: ['PROJECT_API_SCENARIO:READ+DELETE'],
      danger: true,
    },
  ];

  const modulesCount = ref<Record<string, number>>({});
  const allScenarioCount = computed(() => modulesCount.value.all || 0);
  const isExpandAll = ref(props.isExpandAll);
  const rootModulesName = ref<string[]>([]); // 根模块名称列表

  /**
   * 初始化模块树
   * @param isSetDefaultKey 是否设置第一个节点为选中节点
   */
  async function initModules(isSetDefaultKey = false) {
    try {
      loading.value = true;
      const res = await getModuleTree({
        keyword: moduleKeyword.value,
        projectId: appStore.currentProjectId,
        moduleIds: [],
      });
      const nodePathObj: Record<string, any> = {};
      if (props.readOnly) {
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
      } else {
        folderTree.value = mapTree<ModuleTreeNode>(res, (e, fullPath) => {
          // 拼接当前节点的完整路径
          nodePathObj[e.id] = {
            path: e.path,
            fullPath,
          };
          return {
            ...e,
            hideMoreAction: e.id === 'root',
          };
        });
      }
      if (isSetDefaultKey) {
        selectedKeys.value = [folderTree.value[0].id];
      }
      emit('init', folderTree.value, nodePathObj);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  watch(
    () => props.isExpandAll,
    (val) => {
      isExpandAll.value = val;
    }
  );

  function changeExpand() {
    isExpandAll.value = !isExpandAll.value;
  }

  /**
   * 处理文件夹树节点选中事件
   */
  function folderNodeSelect(_selectedKeys: (string | number)[], node: MsTreeNodeData) {
    const offspringIds: string[] = [];
    mapTree(node.children || [], (e) => {
      offspringIds.push(e.id);
      return e;
    });
    emit('folderNodeSelect', _selectedKeys, offspringIds);
  }

  /**
   * 删除文件夹
   * @param node 节点信息
   */
  function deleteFolder(node: MsTreeNodeData) {
    openModal({
      type: 'error',
      title: t('apiScenario.module.deleteTipTitle', { name: node.name }),
      content: t('apiScenario.module.deleteTipContent'),
      okText: t('apiScenario.deleteConfirm'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await deleteModule(node.id);
          Message.success(t('apiScenario.deleteSuccess'));
          await initModules();
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
      default:
        break;
    }
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

      await moveModule({
        dragNodeId: dragNode.id as string,
        dropNodeId: dropNode.id || '',
        dropPosition,
      });
      Message.success(t('apiScenario.moveSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
      await initModules();
    }
  }

  function moreActionsClose() {
    if (!renamePopVisible.value) {
      // 当下拉菜单关闭时，若不是触发重命名气泡显示，则清空聚焦节点 key
      resetFocusNodeKey();
    }
  }

  async function refresh() {
    await initModules();
  }

  async function initModuleCount(params: ApiScenarioGetModuleParams) {
    try {
      const res = await getModuleCount(params);
      modulesCount.value = res;
      folderTree.value = mapTree<ModuleTreeNode>(folderTree.value, (node) => {
        return {
          ...node,
          count: res[node.id] || 0,
          draggable: !props.readOnly,
          disabled: props.readOnly ? node.id === selectedKeys.value[0] : false,
        };
      });
    } catch (error) {
      console.log(error);
    }
  }
  onBeforeMount(async () => {
    await initModules();
    await initModuleCount({
      projectId: appStore.currentProjectId,
    });
  });
  defineExpose({
    refresh,
    initModuleCount,
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
