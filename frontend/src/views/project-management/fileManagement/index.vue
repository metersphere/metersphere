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
                <MsButton type="icon" status="secondary" class="!mr-0 p-[4px]" @click="changeExpand">
                  <MsIcon :type="isExpandAll ? 'icon-icon_folder_collapse1' : 'icon-icon_folder_expansion1'" />
                </MsButton>
              </a-tooltip>
              <a-dropdown trigger="click" @select="handleAddSelect">
                <MsButton type="icon" class="!mr-0 p-[2px]">
                  <MsIcon
                    type="icon-icon_create_planarity"
                    size="18"
                    class="text-[rgb(var(--primary-5))] hover:text-[rgb(var(--primary-4))]"
                  />
                </MsButton>
                <template #content>
                  <a-doption value="module">{{ t('project.fileManagement.addSubModule') }}</a-doption>
                  <a-doption value="storage">{{ t('project.fileManagement.addStorage') }}</a-doption>
                </template>
              </a-dropdown>
              <popConfirm mode="add" :all-names="[]">
                <span id="allPlus" class="invisible"></span>
              </popConfirm>
            </div>
          </div>
          <a-divider class="my-[8px]" />
          <a-radio-group v-model:model-value="showType" type="button" class="file-show-type" @change="changeShowType">
            <a-radio value="Module">{{ t('project.fileManagement.module') }}</a-radio>
            <a-radio value="Storage">{{ t('project.fileManagement.storage') }}</a-radio>
          </a-radio-group>
          <div v-show="showType === 'Module'">
            <FolderTree
              v-model:selected-keys="selectedKeys"
              :is-expand-all="isExpandAll"
              @folder-node-select="folderNodeSelect"
            />
          </div>
          <div v-show="showType === 'Storage'">
            <StorageList
              v-model:drawer-visible="storageDrawerVisible"
              :active-folder="activeFolder"
              @item-click="storageItemSelect"
            />
          </div>
        </div>
      </template>
      <template #right>
        <rightBox :active-folder="activeFolder" :active-folder-type="activeFolderType" />
      </template>
    </MsSplitBox>
  </div>
</template>

<script setup lang="ts">
  /**
   * @description 项目管理-文件管理
   */
  import { computed, ref } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import type { SelectedValue } from '@/components/pure/ms-table-more-action/types';
  import FolderTree from './components/folderTree.vue';
  import popConfirm from './components/popConfirm.vue';
  import rightBox from './components/rightBox.vue';
  import StorageList from './components/storageList.vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const myFileCount = ref(0);
  const allFileCount = ref(0);
  const isExpandAll = ref(false);
  const activeFolderType = ref<'folder' | 'module' | 'storage'>('folder');
  const storageDrawerVisible = ref(false);

  function changeExpand() {
    isExpandAll.value = !isExpandAll.value;
  }

  /**
   * 处理全部文件夹更多操作选中事件
   * @param val 选中的值
   */
  function handleAddSelect(val: SelectedValue) {
    if (val === 'module') {
      document.querySelector('#allPlus')?.dispatchEvent(new Event('click'));
    } else {
      storageDrawerVisible.value = true;
    }
  }

  const activeFolder = ref<string | number>('all');
  const selectedKeys = computed({
    get: () => [activeFolder.value],
    set: (val) => val,
  });

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

  /**
   * 处理文件夹树节点选中事件
   */
  function folderNodeSelect(keys: (string | number)[]) {
    [activeFolder.value] = keys;
    activeFolderType.value = 'module';
  }

  /**
   * 处理存储库列表项选中事件
   */
  function storageItemSelect(key: string | number) {
    activeFolder.value = key;
    activeFolderType.value = 'storage';
  }
</script>

<style lang="less" scoped>
  .page {
    @apply bg-white;

    min-width: 1000px;
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
