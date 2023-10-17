<template>
  <a-input
    v-model:model-value="moduleKeyword"
    :placeholder="t('project.fileManagement.folderSearchPlaceholder')"
    allow-clear
    class="mb-[16px]"
  ></a-input>
  <MsTree
    v-model:focus-node-key="focusNodeKey"
    :selected-keys="props.selectedKeys"
    :data="folderTree"
    :keyword="moduleKeyword"
    :node-more-actions="folderMoreActions"
    :expand-all="props.isExpandAll"
    :empty-text="t('project.fileManagement.noFolder')"
    :draggable="!props.isModal"
    :virtual-list-props="virtualListProps"
    block-node
    @select="folderNodeSelect"
    @more-action-select="handleFolderMoreSelect"
    @more-actions-close="moreActionsClose"
  >
    <template #title="nodeData">
      <span class="text-[var(--color-text-1)]">{{ nodeData.title }}</span>
      <span v-if="!props.isModal" class="ml-[4px] text-[var(--color-text-4)]">({{ nodeData.count }})</span>
    </template>
    <template v-if="!props.isModal" #extra="nodeData">
      <popConfirm mode="add" :all-names="[]" @close="resetFocusNodeKey">
        <MsButton type="icon" size="mini" class="ms-tree-node-extra__btn !mr-0" @click="setFocusNodeKe(nodeData)">
          <MsIcon type="icon-icon_add_outlined" size="14" class="text-[var(--color-text-4)]" />
        </MsButton>
      </popConfirm>
      <popConfirm mode="rename" :field-config="{ field: renameFolderTitle }" :all-names="[]" @close="resetFocusNodeKey">
        <span :id="`renameSpan${nodeData.key}`" class="relative"></span>
      </popConfirm>
    </template>
  </MsTree>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import popConfirm from './popConfirm.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';

  const props = defineProps<{
    isExpandAll: boolean;
    selectedKeys?: Array<string | number>; // 选中的节点 key
    isModal?: boolean; // 是否是弹窗模式
  }>();
  const emit = defineEmits(['update:selectedKeys', 'folderNodeSelect']);

  const { t } = useI18n();
  const { openModal } = useModal();

  const virtualListProps = computed(() => {
    if (props.isModal) {
      return {
        height: 'calc(60vh - 190px)',
      };
    }
    return {
      height: 'calc(100vh - 320px)',
    };
  });
  const moduleKeyword = ref('');
  const folderTree = ref([
    {
      title: 'Trunk',
      key: 'node1',
      count: 18,
      children: [
        {
          title: 'Leaf',
          key: 'node2',
          count: 28,
        },
        {
          title: 'Leaf',
          key: 'node4',
          count: 138,
        },
        {
          title: 'Leaf',
          key: 'node5',
          count: 108,
        },
        {
          title: 'Leaf',
          key: 'node4',
          count: 138,
        },
        {
          title: 'Leaf',
          key: 'node5',
          count: 108,
        },
        {
          title: 'Leaf',
          key: 'node4',
          count: 138,
        },
        {
          title: 'Leaf',
          key: 'node5',
          count: 108,
        },
      ],
    },
    {
      title: 'Trunk',
      key: 'node3',
      count: 180,
      children: [
        {
          title: 'Leaf',
          key: 'node4',
          count: 138,
        },
        {
          title: 'Leaf',
          key: 'node5',
          count: 108,
        },
        {
          title: 'Leaf',
          key: 'node4',
          count: 138,
        },
        {
          title: 'Leaf',
          key: 'node5',
          count: 108,
        },
        {
          title: 'Leaf',
          key: 'node4',
          count: 138,
        },
        {
          title: 'Leaf',
          key: 'node5',
          count: 108,
        },
        {
          title: 'Leaf',
          key: 'node4',
          count: 138,
        },
        {
          title: 'Leaf',
          key: 'node5',
          count: 108,
        },
      ],
    },
    {
      title: 'Trunk',
      key: 'node6',
      children: [],
      count: 0,
    },
  ]);
  const focusNodeKey = ref<string | number>('');

  function setFocusNodeKe(node: MsTreeNodeData) {
    focusNodeKey.value = node.key || '';
  }

  const folderMoreActions: ActionsItem[] = [
    {
      label: 'project.fileManagement.rename',
      eventTag: 'rename',
    },
    {
      label: 'project.fileManagement.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];
  const renamePopVisible = ref(false);

  /**
   * 删除文件夹
   * @param node 节点信息
   */
  function deleteFolder(node: MsTreeNodeData) {
    openModal({
      type: 'error',
      title: t('project.fileManagement.deleteFolderTipTitle', { name: node.title }),
      content: t('project.fileManagement.deleteFolderTipContent'),
      okText: t('project.fileManagement.deleteConfirm'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          Message.success(t('project.fileManagement.deleteSuccess'));
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  const renameFolderTitle = ref(''); // 重命名的文件夹名称

  function resetFocusNodeKey() {
    focusNodeKey.value = '';
    renamePopVisible.value = false;
    renameFolderTitle.value = '';
  }

  /**
   * 处理文件夹树节点选中事件
   */
  function folderNodeSelect(selectedKeys: (string | number)[]) {
    emit('folderNodeSelect', selectedKeys);
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
        renameFolderTitle.value = node.title || '';
        renamePopVisible.value = true;
        document.querySelector(`#renameSpan${node.key}`)?.dispatchEvent(new Event('click'));
        break;
      default:
        break;
    }
  }

  function moreActionsClose() {
    if (!renamePopVisible.value) {
      // 当下拉菜单关闭时，若不是触发重命名气泡显示，则清空聚焦节点 key
      resetFocusNodeKey();
    }
  }

  const selectedKeys = ref(props.selectedKeys || []);

  watch(
    () => props.selectedKeys,
    (val) => {
      selectedKeys.value = val || [];
    }
  );

  watch(
    () => selectedKeys.value,
    (val) => {
      emit('update:selectedKeys', val);
    }
  );
</script>

<style lang="less" scoped></style>
