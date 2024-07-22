<template>
  <MsDrawer
    v-model:visible="showDrawer"
    :title="t('caseManagement.featureCase.associatedFile')"
    :ok-text="t('caseManagement.featureCase.associated')"
    :ok-loading="drawerLoading"
    :ok-disabled="selectFile.length < 1"
    :width="1200"
    unmount-on-close
    :show-continue="false"
    @confirm="handleDrawerConfirm"
    @cancel="handleDrawerCancel"
  >
    <MsSplitBox>
      <template #first>
        <div class="p-[16px] pt-0">
          <div class="folder mb-[8px]" @click="setActiveFolder('all')">
            <div class="folder-text" :class="getFolderClass('all')">
              <MsIcon type="icon-icon_folder_filled1" class="folder-icon" />
              <div class="folder-name">{{ t('project.fileManagement.allFile') }}</div>
              <div class="folder-count">({{ allFileCount }})</div>
            </div>
          </div>
          <a-radio-group v-model:model-value="showType" type="button" class="file-show-type" @change="changeShowType">
            <a-radio value="Module">{{ t('project.fileManagement.module') }}</a-radio>
            <a-radio value="Storage">{{ t('project.fileManagement.storage') }}</a-radio>
          </a-radio-group>
          <div v-show="showType === 'Module'" class="mb-[8px] flex items-center gap-[8px]">
            <a-input
              v-model:model-value="moduleKeyword"
              :placeholder="t('project.fileManagement.folderSearchPlaceholder')"
              allow-clear
              :max-length="255"
            ></a-input>
            <a-tooltip :content="isExpandAll ? t('common.collapseAllSubModule') : t('common.expandAllSubModule')">
              <a-button
                type="outline"
                class="expand-btn arco-btn-outline--secondary"
                @click="() => (isExpandAll = !isExpandAll)"
              >
                <MsIcon v-if="isExpandAll" type="icon-icon_comment_collapse_text_input" />
                <MsIcon v-else type="icon-icon_comment_expand_text_input" />
              </a-button>
            </a-tooltip>
          </div>
          <div v-show="showType === 'Module'">
            <FileTree
              ref="folderTreeRef"
              v-model:selected-keys="selectedKeys"
              v-model:active-folder="activeFolder"
              v-model:group-keyword="moduleKeyword"
              :is-expand-all="isExpandAll"
              :modules-count="modulesCount"
              :show-type="showType"
              :get-tree-request="props.getTreeRequest"
              @init="setRootModules"
              @folder-node-select="folderNodeSelect"
            />
          </div>
          <div v-show="showType === 'Storage'">
            <StorageList
              v-model:drawer-visible="storageDrawerVisible"
              v-model:active-folder="activeFolder"
              :modules-count="modulesCount"
              :show-type="showType"
              @item-click="storageItemSelect"
            />
          </div>
        </div>
      </template>
      <template #second>
        <LinkFileTable
          v-model:selectFile="selectFile"
          :active-folder="activeFolder"
          :active-folder-type="activeFolderType"
          :offspring-ids="offspringIds"
          :modules-count="modulesCount"
          :folder-tree="folderTree"
          :storage-list="storageList"
          :show-type="showType"
          :get-list-request="props.getListRequest"
          :get-list-fun-params="props.getListFunParams"
          :selector-type="props.selectorType"
          :file-all-count-by-storage="fileAllCountByStorage"
          :filetype="props.filetype"
          @init="handleModuleTableInit"
          @update-file-ids="updateFiles"
        />
      </template>
    </MsSplitBox>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import FileTree from './fileTree.vue';
  import LinkFileTable from './linkFileTable.vue';
  import StorageList from './storageList.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { AssociatedList } from '@/models/caseManagement/featureCase';
  import type { CommonList, ModuleTreeNode, TableQueryParams } from '@/models/common';
  import { FileListQueryParams, Repository } from '@/models/projectManagement/file';

  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
    getTreeRequest: (params: any) => Promise<ModuleTreeNode[]>; // 获取左侧树请求
    getCountRequest: (params: any) => Promise<Record<string, any>>; // 获取左侧树模块数量请求
    getListRequest: (params: TableQueryParams) => Promise<CommonList<AssociatedList>>; // 获取表格请求
    getListFunParams: TableQueryParams; // 关联表去重id
    selectorType?: 'none' | 'checkbox' | 'radio';
    filetype?: string;
  }>();

  const emit = defineEmits<{
    (e: 'save', val: AssociatedList[], selectIds?: string[]): void;
    (e: 'update:visible', val: boolean): void;
  }>();
  const showDrawer = computed({
    get() {
      return props.visible;
    },
    set(val) {
      emit('update:visible', val);
    },
  });

  const drawerLoading = ref<boolean>(false);
  const activeFolder = ref<string>('all');
  function getFolderClass(id: string) {
    return activeFolder.value === id ? 'folder-text folder-text--active' : 'folder-text';
  }

  const activeFolderType = ref<'folder' | 'module' | 'storage'>('module');
  const moduleKeyword = ref<string>('');
  const selectedKeys = computed({
    get: () => [activeFolder.value],
    set: (val) => val,
  });
  const offspringIds = ref<string[]>([]);

  const modulesCount = ref<Record<string, number>>({});
  const myFileCount = ref(0);
  const allFileCount = ref(0);
  const fileAllCountByStorage = ref<number>(0);

  const isExpandAll = ref(false);

  type FileShowType = 'Module' | 'Storage';
  const showType = ref<FileShowType>('Module');

  /**
   * 处理文件夹树节点选中事件
   */
  function folderNodeSelect(keys: string[], _offspringIds: string[]) {
    [activeFolder.value] = keys;
    activeFolderType.value = 'module';
    offspringIds.value = [..._offspringIds];
  }

  /**
   * 设置根模块名称列表
   * @param names 根模块名称列表
   */

  const folderTree = ref<ModuleTreeNode[]>([]);
  const rootModulesName = ref<string[]>([]); // 根模块名称列表
  function setRootModules(treeNode: ModuleTreeNode[]) {
    folderTree.value = treeNode;
    rootModulesName.value = treeNode.map((e) => e.name);
  }

  /*
   * 初始化模块文件数量
   */
  const storageCount = ref<number>(0);
  async function initModulesCount(params: FileListQueryParams) {
    try {
      modulesCount.value = await props.getCountRequest(params);
      myFileCount.value = modulesCount.value.my || 0;
      allFileCount.value = modulesCount.value.all;
      if (showType.value === 'Storage') {
        fileAllCountByStorage.value = modulesCount.value.git;
      } else {
        fileAllCountByStorage.value = modulesCount.value.minio;
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const tableFilterParams = ref<FileListQueryParams>({
    moduleIds: [],
    fileType: '',
    projectId: '',
  });

  function changeShowType(val: string | number | boolean) {
    showType.value = val as FileShowType;
    activeFolder.value = 'all';
  }

  /**
   * 右侧表格数据刷新后，若当前展示的是模块，则刷新模块树的统计数量
   */
  function handleModuleTableInit(params: FileListQueryParams) {
    initModulesCount(params);
    tableFilterParams.value = { ...params };
    storageCount.value = params.storageItemCount || 0;
  }

  const storageDrawerVisible = ref(false);

  /**
   * 处理存储库列表项选中事件
   */
  const storageList = ref<Repository[]>([]);
  function storageItemSelect(key: string, storages: Repository[]) {
    storageList.value = storages;
    activeFolder.value = key;
    activeFolderType.value = 'storage';
  }

  function setActiveFolder(id: string) {
    activeFolder.value = id;
    if (['my', 'all'].includes(id)) {
      activeFolderType.value = 'folder';
    } else {
      activeFolderType.value = 'storage';
    }
  }

  const selectFile = ref<AssociatedList[]>([]);
  const selectIds = ref<string[]>([]);

  function updateFiles(fileIds: string[]) {
    selectIds.value = fileIds;
  }

  function handleDrawerConfirm() {
    emit('save', selectFile.value, selectIds.value);
    showDrawer.value = false;
  }

  function handleDrawerCancel() {
    showDrawer.value = false;
  }

  watch(
    () => showDrawer.value,
    (val) => {
      if (!val) {
        selectFile.value = [];
      }
    }
  );
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
  :deep(.arco-drawer-body) {
    padding: 0 16px !important;
  }
  // TODO 这个后边可以提取一个全局样式
  .expand-btn {
    padding: 8px;
    .arco-icon {
      color: var(--color-text-4);
    }
    &:hover {
      border-color: rgb(var(--primary-5)) !important;
      background-color: rgb(var(--primary-1)) !important;
      .arco-icon {
        color: rgb(var(--primary-5));
      }
    }
  }
</style>
