<template>
  <MsDrawer
    v-model:visible="showDrawer"
    :mask="false"
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
      <template #left>
        <div class="p-[16px] pt-0">
          <div class="folder">
            <div class="folder-text">
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
            </div>
          </div>
          <a-divider class="my-[8px]" />
          <a-radio-group v-model:model-value="showType" type="button" class="file-show-type" @change="changeShowType">
            <a-radio value="Module">{{ t('project.fileManagement.module') }}</a-radio>
            <a-radio value="Storage">{{ t('project.fileManagement.storage') }}</a-radio>
          </a-radio-group>
          <div v-show="showType === 'Module'">
            <FileTree
              ref="folderTreeRef"
              v-model:selected-keys="selectedKeys"
              v-model:active-folder="activeFolder"
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
      <template #right>
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
          @init="handleModuleTableInit"
        />
      </template>
    </MsSplitBox>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import FileTree from './fileTree.vue';
  import LinkFileTable from './linkFileTable.vue';
  import StorageList from './storageList.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { AssociatedList } from '@/models/caseManagement/featureCase';
  import type { CommonList, TableQueryParams } from '@/models/common';
  import { FileListQueryParams, ModuleTreeNode, Repository } from '@/models/projectManagement/file';

  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
    getTreeRequest: (params: any) => Promise<ModuleTreeNode[]>; // 获取左侧树请求
    getCountRequest: (params: any) => Promise<Record<string, any>>; // 获取左侧树模块数量请求
    getListRequest: (params: TableQueryParams) => Promise<CommonList<AssociatedList>>; // 获取表格请求
  }>();

  const emit = defineEmits<{
    (e: 'save', val: AssociatedList[]): void;
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

  const activeFolderType = ref<'folder' | 'module' | 'storage'>('module');

  const activeFolder = ref<string>('root');
  const selectedKeys = computed({
    get: () => [activeFolder.value],
    set: (val) => val,
  });
  const offspringIds = ref<string[]>([]);

  const modulesCount = ref<Record<string, number>>({});
  const myFileCount = ref(0);
  const allFileCount = ref(0);

  const isExpandAll = ref(false);

  function changeExpand() {
    isExpandAll.value = !isExpandAll.value;
  }

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
  async function initModulesCount(params: FileListQueryParams) {
    try {
      modulesCount.value = await props.getCountRequest(params);
      myFileCount.value = modulesCount.value.my || 0;
      allFileCount.value = modulesCount.value.all || 0;
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
    if (val === 'Storage') {
      initModulesCount({
        ...tableFilterParams.value,
        combine: {
          ...tableFilterParams.value.combine,
          storage: 'git',
        },
      });
    } else {
      initModulesCount(tableFilterParams.value);
    }
  }

  /**
   * 右侧表格数据刷新后，若当前展示的是模块，则刷新模块树的统计数量
   */
  function handleModuleTableInit(params: FileListQueryParams) {
    initModulesCount(params);
    tableFilterParams.value = { ...params };
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

  const selectFile = ref<AssociatedList[]>([]);

  function handleDrawerConfirm() {
    emit('save', selectFile.value);
    showDrawer.value = false;
  }

  function handleDrawerCancel() {
    showDrawer.value = false;
  }
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
</style>
