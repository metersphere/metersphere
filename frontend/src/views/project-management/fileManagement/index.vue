<template>
  <div class="page">
    <MsSplitBox>
      <template #first>
        <div class="p-[16px]">
          <div class="folder" @click="setActiveFolder('my')">
            <div :class="getFolderClass('my')">
              <MsIcon type="icon-icon_folder_filled1" class="folder-icon" />
              <div class="folder-name">{{ t('project.fileManagement.myFile') }}</div>
              <div class="folder-count">({{ myFileCount }})</div>
            </div>
          </div>
          <div class="folder mb-[8px]" @click="setActiveFolder('all')">
            <div :class="getFolderClass('all')">
              <MsIcon type="icon-icon_folder_filled1" class="folder-icon" />
              <div class="folder-name">{{ t('project.fileManagement.allFile') }}</div>
              <div class="folder-count">({{ allFileCount }})</div>
            </div>
            <div class="ml-auto flex items-center">
              <a-tooltip :content="isExpandAll ? t('common.collapseAllSubModule') : t('common.expandAllSubModule')">
                <MsButton type="icon" status="secondary" class="!mr-0 p-[4px]" @click="changeExpand">
                  <MsIcon :type="isExpandAll ? 'icon-icon_folder_collapse1' : 'icon-icon_folder_expansion1'" />
                </MsButton>
              </a-tooltip>
              <a-dropdown
                v-if="hasAnyPermission(['PROJECT_FILE_MANAGEMENT:READ+ADD'])"
                trigger="click"
                @select="handleAddSelect"
              >
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
              <popConfirm
                v-if="hasAnyPermission(['PROJECT_FILE_MANAGEMENT:READ+ADD'])"
                mode="add"
                :all-names="rootModulesName"
                parent-id="none"
                @add-finish="handleAddRootModuleFinish"
              >
                <span id="allPlus" class="invisible"></span>
              </popConfirm>
            </div>
          </div>
          <a-radio-group v-model:model-value="showType" type="button" class="file-show-type" @change="changeShowType">
            <a-radio value="Module">{{ t('project.fileManagement.module') }}</a-radio>
            <a-radio value="Storage">{{ t('project.fileManagement.storage') }}</a-radio>
          </a-radio-group>
          <div v-show="showType === 'Module'">
            <FolderTree
              ref="folderTreeRef"
              v-model:selected-keys="selectedKeys"
              :is-expand-all="isExpandAll"
              :modules-count="modulesCount"
              :show-type="showType"
              @init="setRootModules"
              @folder-node-select="folderNodeSelect"
            />
          </div>
          <div v-show="showType === 'Storage'">
            <StorageList
              v-model:drawer-visible="storageDrawerVisible"
              :active-folder="activeFolder"
              :modules-count="modulesCount"
              :show-type="showType"
              @item-click="storageItemSelect"
            />
          </div>
        </div>
      </template>
      <template #second>
        <rightBox
          :active-folder="activeFolder"
          :active-folder-type="activeFolderType"
          :offspring-ids="offspringIds"
          @init="handleModuleTableInit"
        />
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

  import { getModulesCount } from '@/api/modules/project-management/fileManagement';
  import { useI18n } from '@/hooks/useI18n';
  import { hasAnyPermission } from '@/utils/permission';

  import { FileListQueryParams } from '@/models/projectManagement/file';

  const { t } = useI18n();

  const isExpandAll = ref(false);
  const activeFolderType = ref<'folder' | 'module' | 'storage'>('folder');
  const storageDrawerVisible = ref(false);
  const rootModulesName = ref<string[]>([]); // 根模块名称列表

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

  const activeFolder = ref<string>('all');
  const selectedKeys = computed({
    get: () => [activeFolder.value],
    set: (val) => val,
  });
  const offspringIds = ref<string[]>([]);

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

  /**
   * 处理文件夹树节点选中事件
   */
  function folderNodeSelect(keys: string[], _offspringIds: string[]) {
    [activeFolder.value] = keys;
    activeFolderType.value = 'module';
    offspringIds.value = [..._offspringIds];
  }

  /**
   * 处理存储库列表项选中事件
   */
  function storageItemSelect(key: string) {
    activeFolder.value = key;
    activeFolderType.value = 'storage';
  }

  /**
   * 设置根模块名称列表
   * @param names 根模块名称列表
   */
  function setRootModules(names: string[]) {
    rootModulesName.value = names;
  }

  type FileShowType = 'Module' | 'Storage';
  const showType = ref<FileShowType>('Module');
  const tableFilterParams = ref<FileListQueryParams>({
    moduleIds: [],
    fileType: '',
    projectId: '',
  });

  const folderTreeRef = ref<InstanceType<typeof FolderTree>>();
  /**
   * 添加根模块后，若当前展示的是模块，则刷新模块树
   */
  function handleAddRootModuleFinish() {
    if (showType.value === 'Module') {
      folderTreeRef.value?.initModules();
    }
  }

  const modulesCount = ref<Record<string, number>>({});
  const myFileCount = ref(0);
  const allFileCount = ref(0);

  /**
   * 初始化模块文件数量
   */
  async function initModulesCount(params: FileListQueryParams) {
    try {
      modulesCount.value = await getModulesCount(params);
      myFileCount.value = modulesCount.value.my || 0;
      allFileCount.value = modulesCount.value.all || 0;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

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
      initModulesCount({
        ...tableFilterParams.value,
        combine: {
          ...tableFilterParams.value.combine,
          storage: 'minio',
        },
      });
    }
  }

  /**
   * 右侧表格数据刷新后，若当前展示的是模块，则刷新模块树的统计数量
   */
  function handleModuleTableInit(params: FileListQueryParams) {
    initModulesCount({
      ...params,
      combine: {
        ...params.combine,
        storage: showType.value === 'Module' ? 'minio' : 'git', // 这里因为存在切换我的、全部文件夹时，激活的是存储库列表，所以还要区分一下当前展示的类型是啥
      },
    });
    tableFilterParams.value = { ...params };
  }
</script>

<style lang="less" scoped>
  .page {
    @apply h-full;

    min-width: 1000px;
    border-radius: var(--border-radius-large);
    background-color: var(--color-text-fff);
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
