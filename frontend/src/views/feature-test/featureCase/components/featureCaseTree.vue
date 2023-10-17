<template>
  <MsTree
    v-model:focus-node-key="focusNodeKey"
    :selected-keys="props.selectedKeys"
    :data="caseTree"
    :keyword="groupKeyword"
    :node-more-actions="caseMoreActions"
    :expand-all="props.isExpandAll"
    :empty-text="t('featureTest.featureCase.caseEmptyContent')"
    draggable
    :virtual-list-props="virtualListProps"
    block-node
    @select="caseNodeSelect"
    @more-action-select="handleCaseMoreSelect"
    @more-actions-close="moreActionsClose"
  >
    <template #title="nodeData">
      <span class="text-[var(--color-text-1)]">{{ nodeData.title }}</span>
      <span class="ml-[4px] text-[var(--color-text-4)]">({{ nodeData.count }})</span>
    </template>
    <template #extra="nodeData">
      <MsPopConfirm
        :is-delete="false"
        :all-names="[]"
        :title="t('featureTest.featureCase.addSubModule')"
        @cancel="resetFocusNodeKey"
      >
        <MsButton type="icon" size="mini" class="ms-tree-node-extra__btn !mr-0" @click="setFocusKey(nodeData)">
          <MsIcon type="icon-icon_add_outlined" size="14" class="text-[var(--color-text-4)]" />
        </MsButton>
      </MsPopConfirm>
      <MsPopConfirm
        :title="t('featureTest.featureCase.rename')"
        :all-names="[]"
        :is-delete="false"
        :field-config="{ field: renameCaseName }"
        @cancel="resetFocusNodeKey"
      >
        <span :id="`renameSpan${nodeData.key}`" class="relative"></span>
      </MsPopConfirm>
    </template>
  </MsTree>
  <div class="recycle w-[88%]">
    <a-divider class="mb-[16px]" />
    <div class="recycle-bin pt-2">
      <MsIcon type="icon-icon_delete-trash_outlined" size="16" class="mx-[10px] text-[var(--color-text-4)]" />
      <div class="text-[var(--color-text-1)]">{{ t('featureTest.featureCase.recycle') }}</div>
      <div class="recycle-count">({{ recycleCount }})</div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsPopConfirm from '@/components/pure/ms-popconfirm/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';

  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';

  const { t } = useI18n();
  const { openModal } = useModal();

  const focusNodeKey = ref<string | number>('');

  const props = defineProps<{
    selectedKeys?: Array<string | number>; // 选中的节点 key
    isExpandAll: boolean; // 是否展开用例节点
  }>();

  const emits = defineEmits(['update:selectedKeys', 'caseNodeSelect']);

  const groupKeyword = ref<string>('');

  const caseTree = ref([
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

  const caseMoreActions: ActionsItem[] = [
    {
      label: 'featureTest.featureCase.rename',
      eventTag: 'rename',
    },
    {
      label: 'featureTest.featureCase.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];

  const renameCaseName = ref('');

  const selectedNodeKeys = ref(props.selectedKeys || []);

  const renamePopVisible = ref(false);

  // 用例树节点选中事件
  const caseNodeSelect = (selectedKeys: (string | number)[]) => {
    emits('caseNodeSelect', selectedKeys);
  };

  // 删除节点
  const deleteHandler = (node: MsTreeNodeData) => {
    openModal({
      type: 'error',
      title: t('featureTest.featureCase.deleteTipTitle', { name: node.title }),
      content: t('featureTest.featureCase.deleteCaseTipContent'),
      okText: t('featureTest.featureCase.deleteConfirm'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          Message.success(t('featureTest.featureCase.deleteSuccess'));
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  };

  function resetFocusNodeKey() {
    focusNodeKey.value = '';
    renamePopVisible.value = false;
    renameCaseName.value = '';
  }

  // 用例树节点更多事件
  const handleCaseMoreSelect = (item: ActionsItem, node: MsTreeNodeData) => {
    switch (item.eventTag) {
      case 'delete':
        deleteHandler(node);
        resetFocusNodeKey();
        break;
      case 'rename':
        renameCaseName.value = node.title || '';
        renamePopVisible.value = true;
        document.querySelector(`#renameSpan${node.key}`)?.dispatchEvent(new Event('click'));
        break;
      default:
        break;
    }
  };

  const moreActionsClose = () => {
    if (!renamePopVisible.value) {
      resetFocusNodeKey();
    }
  };

  const setFocusKey = (node: MsTreeNodeData) => {
    focusNodeKey.value = node.key || '';
  };

  const recycleCount = ref<number>(100);

  const virtualListProps = computed(() => {
    return {
      height: 'calc(100vh - 376px)',
    };
  });

  watch(
    () => props.selectedKeys,
    (val) => {
      selectedNodeKeys.value = val || [];
    }
  );

  watch(
    () => selectedNodeKeys.value,
    (val) => {
      emits('update:selectedKeys', val);
    }
  );
</script>

<style scoped lang="less">
  .recycle {
    @apply absolute bottom-0 bg-white  pb-4;
    :deep(.arco-divider-horizontal) {
      margin: 8px 0;
    }
    .recycle-bin {
      @apply bottom-0 flex items-center bg-white;
      .recycle-count {
        margin-left: 4px;
        color: var(--color-text-4);
      }
    }
  }
</style>
