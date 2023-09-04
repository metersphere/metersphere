<template>
  <div class="page">
    <MsSplitBox>
      <template #left>
        <div class="p-[24px]">
          <div class="folder" @click="setActiveFolder('my')">
            <div :class="getFolderClass('my')">
              <MsIcon type="icon-icon_folder_filled1" class="folder-icon" />
              <div class="folder-name">{{ t('project.fileManagement.myFile') }}</div>
              <div class="folder-count">({{ myFileCount }})</div>
            </div>
          </div>
          <div class="folder">
            <div :class="getFolderClass('all')" @click="setActiveFolder('all')">
              <MsIcon type="icon-icon_folder_filled1" class="folder-icon" />
              <div class="folder-name">{{ t('project.fileManagement.allFile') }}</div>
              <div class="folder-count">({{ allFileCount }})</div>
            </div>
            <div class="ml-auto flex items-center">
              <a-tooltip
                :content="isExpandAll ? t('project.fileManagement.collapseAll') : t('project.fileManagement.expandAll')"
              >
                <a-button type="text" size="mini" class="p-[4px]" @click="changeExpand">
                  <MsIcon :type="isExpandAll ? 'icon-icon_folder_collapse1' : 'icon-icon_folder_expansion'" />
                </a-button>
              </a-tooltip>
              <popConfirm mode="add" :all-names="[]">
                <a-tooltip :content="t('project.fileManagement.addSubModule')">
                  <a-button type="text" size="mini" class="p-[2px]">
                    <MsIcon type="icon-icon_create_planarity" size="18" />
                  </a-button>
                </a-tooltip>
              </popConfirm>
            </div>
          </div>
          <a-divider class="my-[8px]" />
          <a-radio-group v-model:model-value="showType" type="button" class="file-show-type" @change="changeShowType">
            <a-radio value="Module">{{ t('project.fileManagement.module') }}</a-radio>
            <a-radio value="Storage">{{ t('project.fileManagement.storage') }}</a-radio>
          </a-radio-group>
          <div v-show="showType === 'Module'">
            <a-input
              v-model:model-value="moduleKeyword"
              :placeholder="t('project.fileManagement.folderSearchPlaceholder')"
              allow-clear
              class="mb-[8px]"
            ></a-input>
            <MsTree
              v-model:focus-node-key="focusNodeKey"
              :data="folderTree"
              :keyword="moduleKeyword"
              :node-more-actions="folderMoreActions"
              :expand-all="isExpandAll"
              :empty-text="t('project.fileManagement.noFolder')"
              draggable
              block-node
              @select="folderNodeSelect"
              @more-action-select="handleFolderMoreSelect"
              @more-actions-close="moreActionsClose"
            >
              <template #title="nodeData">
                <span class="text-[var(--color-text-1)]">{{ nodeData.title }}</span>
                <span class="ml-[4px] text-[var(--color-text-4)]">({{ nodeData.count }})</span>
              </template>
              <template #extra="nodeData">
                <popConfirm mode="add" :all-names="[]" @close="resetFocusNodeKey">
                  <MsButton
                    type="text"
                    size="mini"
                    class="ms-tree-node-extra__btn !mr-0"
                    @click="setFocusNodeKe(nodeData)"
                  >
                    <MsIcon type="icon-icon_add_outlined" size="14" class="text-[var(--color-text-4)]" />
                  </MsButton>
                </popConfirm>
                <popConfirm mode="rename" :title="renameFolderTitle" :all-names="[]" @close="resetFocusNodeKey">
                  <span :id="`renameSpan${nodeData.key}`" class="relative"></span>
                </popConfirm>
              </template>
            </MsTree>
          </div>
          <div v-show="showType === 'Storage'">
            <a-input
              v-model:model-value="storageKeyword"
              :placeholder="t('project.fileManagement.folderSearchPlaceholder')"
              allow-clear
              class="mb-[8px]"
            ></a-input>
            <a-list
              :virtual-list-props="{
                height: 'calc(100vh - 310px)',
              }"
              :data="storageList"
              :bordered="false"
              :split="false"
            >
              <template #item="{ item, index }">
                <div
                  :key="index"
                  :class="['folder', focusNodeKey === item.key ? 'ms-tree-node-extra--focus' : '']"
                  @click="setActiveFolder(item.key)"
                >
                  <div :class="getFolderClass(item.key)">
                    <MsIcon type="icon-icon_git" class="folder-icon" />
                    <div class="folder-name">{{ item.title }}</div>
                    <div class="folder-count">({{ item.count }})</div>
                  </div>
                  <itemActions
                    :item="item"
                    @click.stop
                    @add="setFocusNodeKe"
                    @close="resetFocusNodeKey"
                    @actions-close="moreActionsClose"
                    @click-more="setFocusNodeKe"
                  />
                </div>
              </template>
              <template #empty>
                <div
                  class="rounded-[var(--border-radius-small)] bg-[var(--color-fill-1)] p-[8px] text-[12px] text-[var(--color-text-4)]"
                >
                  {{ t('project.fileManagement.noStorage') }}
                </div>
              </template>
            </a-list>
          </div>
        </div>
      </template>
      <template #right>
        <rightBox />
      </template>
    </MsSplitBox>
  </div>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import { debounce } from 'lodash-es';
  import { useI18n } from '@/hooks/useI18n';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import useModal from '@/hooks/useModal';
  import popConfirm from './components/popConfirm.vue';
  import rightBox from './components/rightBox.vue';
  import itemActions from './components/itemActions.vue';

  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  const { t } = useI18n();
  const { openModal } = useModal();

  const myFileCount = ref(0);
  const allFileCount = ref(0);
  const isExpandAll = ref(false);
  const activeFolderType = ref<'folder' | 'module' | 'storage'>('folder');

  function changeExpand() {
    isExpandAll.value = !isExpandAll.value;
  }

  const activeFolder = ref<string | number>('all');

  function setActiveFolder(id: string) {
    activeFolder.value = id;
    if (['my', 'all'].includes(id)) {
      activeFolderType.value = 'folder';
    } else {
      activeFolderType.value = 'storage';
    }
  }

  function getFolderClass(id: string) {
    return activeFolder.value === id ? 'folder-text folder-text--active' : 'folder-text';
  }

  type FileShowType = 'Module' | 'Storage';
  const showType = ref<FileShowType>('Module');

  function changeShowType(val: string | number | boolean) {
    showType.value = val as FileShowType;
  }

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
      title: t('project.fileManagement.deleteTipTitle', { name: node.title }),
      content: t('project.fileManagement.deleteTipContent'),
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
    [activeFolder.value] = selectedKeys;
    activeFolderType.value = 'module';
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

  const storageKeyword = ref('');
  const originStorageList = ref([
    {
      title: 'storage1',
      key: '1',
      count: 129,
    },
    {
      title: 'storage2',
      key: '2',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage1',
      key: '1',
      count: 129,
    },
    {
      title: 'storage2',
      key: '2',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage1',
      key: '1',
      count: 129,
    },
    {
      title: 'storage2',
      key: '2',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
  ]);
  const storageList = ref(originStorageList.value);

  const searchStorage = debounce(() => {
    storageList.value = originStorageList.value.filter((item) => item.title.includes(storageKeyword.value));
  }, 300);

  watch(
    () => storageKeyword.value,
    () => {
      if (storageKeyword.value === '') {
        storageList.value = originStorageList.value;
      }
      searchStorage();
    }
  );
</script>

<style lang="less" scoped>
  .page {
    @apply bg-white;

    height: calc(100vh - 88px);
    border-radius: var(--border-radius-large);
    .folder {
      @apply flex cursor-pointer items-center justify-between;

      padding: 8px 4px;
      border-radius: var(--border-radius-small);
      &:hover {
        background-color: rgb(var(--primary-1));
      }
      .folder-text {
        @apply flex cursor-pointer items-center;
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
    .file-show-type {
      @apply grid grid-cols-2;

      margin-bottom: 8px;
      :deep(.arco-radio-button-content) {
        @apply text-center;
      }
    }
  }
</style>
