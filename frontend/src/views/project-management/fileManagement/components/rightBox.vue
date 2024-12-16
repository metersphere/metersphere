<template>
  <div class="flex h-full flex-col p-[16px]">
    <div class="header">
      <a-button v-permission="['PROJECT_FILE_MANAGEMENT:READ+ADD']" type="primary" @click="handleAddClick">
        {{ t('project.fileManagement.addFile') }}
      </a-button>
      <div class="header-right">
        <a-select v-model="tableFileType" class="w-[240px]" :loading="fileTypeLoading" @change="searchList">
          <template #prefix>
            {{ t('project.fileManagement.type') }}
          </template>
          <a-option key="" value="">{{ t('common.all') }}</a-option>
          <a-option v-for="item of tableFileTypeOptions" :key="item" :value="item">
            {{ item }}
          </a-option>
        </a-select>
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('project.fileManagement.folderSearchPlaceholder')"
          allow-clear
          class="w-[240px]"
          @search="searchList"
          @press-enter="searchList"
          @clear="searchList"
        />
        <a-radio-group
          v-if="props.activeFolderType === 'folder'"
          v-model:model-value="fileType"
          type="button"
          class="file-show-type min-w-[96px]"
          @change="changeFileType"
        >
          <a-radio value="module" class="show-type-icon">{{ t('project.fileManagement.module') }}</a-radio>
          <a-radio value="storage" class="show-type-icon">{{ t('project.fileManagement.storage') }}</a-radio>
        </a-radio-group>
        <a-radio-group v-model:model-value="showType" type="button" class="file-show-type">
          <a-radio value="list" class="show-type-icon p-[2px]"><MsIcon type="icon-icon_view-list_outlined" /></a-radio>
          <a-radio value="card" class="show-type-icon p-[2px]"><MsIcon type="icon-icon_card_outlined" /></a-radio>
        </a-radio-group>
      </div>
    </div>
    <a-spin class="h-[calc(100%-48px)] w-full" :loading="loading">
      <ms-base-table
        v-if="showType === 'list'"
        v-bind="propsRes"
        :action-config="fileType === 'module' ? moduleFileBatchActions : storageFileBatchActions"
        no-disable
        v-on="propsEvent"
        @selected-change="handleTableSelect"
        @batch-action="handleTableBatch"
        @module-change="searchList"
      >
        <template #name="{ record, rowIndex }">
          <div class="flex w-full items-center justify-start overflow-hidden">
            <MsTag
              v-if="record.fileType.toLowerCase() === 'jar'"
              theme="light"
              type="success"
              :self-style="
                record.enable
                  ? {}
                  : {
                      color: 'var(--color-text-4)',
                      backgroundColor: 'var(--color-text-n9)',
                    }
              "
            >
              {{ t(record.enable ? 'common.enable' : 'common.disable') }}
            </MsTag>
            <a-tooltip :content="record.name">
              <a-button
                type="text"
                class="max-w-[calc(100%-60px)] flex-1 justify-start px-0"
                @click="openFileDetail(record.id, rowIndex)"
              >
                <div class="one-line-text">{{ record.name }}</div>
              </a-button>
            </a-tooltip>
          </div>
        </template>
        <template #size="{ record }">
          <span>{{ formatFileSize(record.size) }}</span>
        </template>
        <template #action="{ record }">
          <a-switch
            v-if="record.fileType === 'jar' && hasAnyPermission(['PROJECT_FILE_MANAGEMENT:READ+UPDATE'])"
            v-model:model-value="record.enable"
            size="small"
            :before-change="(val) => handleChangeEnable(val, record)"
          />
          <a-divider
            v-if="record.fileType === 'jar' && hasAnyPermission(['PROJECT_FILE_MANAGEMENT:READ+UPDATE'])"
            direction="vertical"
            :margin="8"
          />
          <MsButton
            v-if="record.fileType !== 'jar' && hasAnyPermission(['PROJECT_FILE_MANAGEMENT:READ+UPDATE'])"
            type="text"
            class="!mr-0"
            @click="handleMove(record)"
          >
            {{ t('project.fileManagement.move') }}
          </MsButton>
          <a-divider
            v-if="record.fileType !== 'jar' && hasAnyPermission(['PROJECT_FILE_MANAGEMENT:READ+UPDATE'])"
            direction="vertical"
            :margin="8"
          />
          <MsButton
            v-if="hasAnyPermission(['PROJECT_FILE_MANAGEMENT:READ+DOWNLOAD'])"
            type="text"
            class="!mr-0"
            @click="handleDownload(record)"
          >
            {{ t('project.fileManagement.download') }}
          </MsButton>
          <a-divider
            v-if="hasAnyPermission(['PROJECT_FILE_MANAGEMENT:READ+DOWNLOAD'])"
            direction="vertical"
            :margin="8"
          />
          <MsTableMoreAction
            :list="record.fileType === 'jar' ? getJarFileActions(record) : normalFileActions"
            @select="handleMoreActionSelect($event, record)"
          />
        </template>
        <template v-if="keyword.trim() === ''" #empty>
          <div class="flex w-full items-center justify-center p-[8px] text-[var(--color-text-4)]">
            {{ t('project.fileManagement.tableNoFile') }}
            <MsButton class="ml-[8px]" @click="handleAddClick">
              {{ t('project.fileManagement.addFile') }}
            </MsButton>
          </div>
        </template>
      </ms-base-table>
      <MsCardList
        v-else-if="showType === 'card'"
        ref="cardListRef"
        mode="remote"
        :remote-func="getFileList"
        :remote-params="{
          projectId: appStore.currentProjectId,
          moduleId: props.activeFolder,
          moduleIds: isMyOrAllFolder ? [] : [props.activeFolder],
          fileType: tableFileType,
          combine,
          keyword,
        }"
        :shadow-limit="50"
        :card-min-width="102"
        class="flex-1"
      >
        <template #item="{ item, index }">
          <MsThumbnailCard
            :type="item.fileType"
            :url="`${CompressImgUrl}/${userStore.id}/${item.id}`"
            :footer-text="item.name"
            :more-actions="item.fileType === 'jar' ? getJarFileActions(item) : normalFileActions"
            @click="openFileDetail(item.id, index)"
            @action-select="handleMoreActionSelect($event, item)"
          />
        </template>
        <template #empty>
          <div class="flex w-full items-center justify-center p-[16px] text-[var(--color-text-4)]">
            {{ t('project.fileManagement.tableNoFile') }}
            <MsButton class="ml-[8px]" @click="handleAddClick">
              {{ t('project.fileManagement.addFile') }}
            </MsButton>
          </div>
        </template>
      </MsCardList>
    </a-spin>
  </div>
  <MsDrawer v-model:visible="uploadDrawerVisible" :title="t('project.fileManagement.addFile')" :width="680">
    <div class="mb-[8px] flex items-center justify-between text-[var(--color-text-1)]">
      {{ t('project.fileManagement.fileType') }}
      <div class="flex items-center text-[12px] leading-[16px] text-[rgb(var(--warning-6))]">
        <icon-exclamation-circle class="mr-[2px]" />
        {{ t('project.fileManagement.fileTypeTip') }}
      </div>
    </div>
    <div class="mb-[24px] grid grid-cols-2 gap-[16px]">
      <a-tooltip :content="t('project.fileManagement.uploadingTip')" :disabled="!isUploading || acceptType === 'none'">
        <div :class="getCardClass('none')" @click="setAcceptType('none')">
          <svg-icon width="64px" height="46px" name="project_setting" class="file-type-card-icon" />
          <div>
            <div class="file-type-card-text">{{ t('project.fileManagement.normalFile') }}</div>
            <div class="file-type-card-desc">{{ t('project.fileManagement.normalFileDesc') }}</div>
          </div>
        </div>
      </a-tooltip>
      <a-tooltip :content="t('project.fileManagement.uploadingTip')" :disabled="!isUploading || acceptType === 'jar'">
        <div :class="getCardClass('jar')" @click="setAcceptType('jar')">
          <svg-icon width="64px" height="46px" name="JAR" class="file-type-card-icon" />
          <div>
            <div class="file-type-card-text">{{ t('project.fileManagement.jarFile') }}</div>
            <div class="file-type-card-desc">{{ t('project.fileManagement.jarFileDesc') }}</div>
          </div>
        </div>
      </a-tooltip>
    </div>
    <MsUpload
      v-show="!isUploading"
      v-model:file-list="fileList"
      :accept="acceptType"
      :auto-upload="false"
      :sub-text="
        acceptType === 'jar' ? '' : t('project.fileManagement.normalFileSubText', { size: appStore.getFileMaxSize })
      "
      multiple
      draggable
      size-unit="MB"
      class="mb-[16px] w-full"
      @change="handleFileChange"
    />
    <MsFileList
      ref="fileListRef"
      v-model:file-list="fileList"
      :upload-func="uploadFile"
      :request-params="{
        projectId: appStore.currentProjectId,
        moduleId: ['my', 'all'].includes(props.activeFolder) ? 'root' : props.activeFolder, // 模块id, my/all为根目录
      }"
      :route="RouteEnum.PROJECT_MANAGEMENT_FILE_MANAGEMENT"
      :route-query="{ position: 'uploadDrawer' }"
      @start="handleUploadStart"
      @finish="uploadFinish"
    >
      <template #tabExtra>
        <div v-if="acceptType === 'jar'" class="flex items-center gap-[4px]">
          <a-switch
            v-model:model-value="allJarIsEnable"
            size="small"
            :disabled="fileList.length === 0"
            type="line"
            @change="enableAllJar"
          ></a-switch>
          {{ t('project.fileManagement.enableAll') }}
          <a-tooltip position="tr" :content="t('project.fileManagement.uploadTip')">
            <MsIcon type="icon-icon-maybe_outlined" class="cursor-pointer hover:text-[rgb(var(--primary-5))]" />
          </a-tooltip>
        </div>
      </template>
      <template #actions="{ item }">
        <a-switch v-if="acceptType === 'jar'" v-model:model-value="item.enable" size="small" type="line"></a-switch>
      </template>
    </MsFileList>
    <template #footer>
      <a-button type="secondary" @click="cancelUpload">
        {{ t('project.fileManagement.cancel') }}
      </a-button>
      <a-button type="secondary" :disabled="noWaitingUpload" @click="backstageUpload">
        {{ t('project.fileManagement.backendUpload') }}
      </a-button>
      <a-button type="primary" :disabled="isUploading || noWaitingUpload" @click="startUpload">
        {{ t('project.fileManagement.startUpload') }}
      </a-button>
    </template>
  </MsDrawer>
  <a-modal
    v-model:visible="storageDialogVisible"
    :title="t('project.fileManagement.addFile')"
    title-align="start"
    class="ms-modal-form ms-modal-medium"
    :mask-closable="false"
    @close="handleStorageModalCancel"
  >
    <a-form ref="storageFormRef" class="rounded-[4px]" :model="storageForm" layout="vertical">
      <a-form-item
        v-if="isMyOrAllFolder"
        field="storage"
        :label="t('project.fileManagement.storage')"
        :rules="[{ required: true, message: t('project.fileManagement.storageNotNull') }]"
        required
        asterisk-position="end"
      >
        <a-select
          v-model:model-value="storageForm.storage"
          :placeholder="t('project.fileManagement.storagePlaceholder')"
          :options="storageList.map((e) => ({ ...e, label: e.name, value: e.id }))"
        />
      </a-form-item>
      <a-form-item
        field="branch"
        :label="t('project.fileManagement.gitBranch')"
        :rules="[{ required: true, message: t('project.fileManagement.gitBranchNotNull') }]"
        required
        asterisk-position="end"
      >
        <a-input
          v-model:model-value="storageForm.branch"
          :placeholder="t('project.fileManagement.gitBranchPlaceholder')"
          :max-length="255"
        ></a-input>
      </a-form-item>
      <a-form-item
        field="path"
        :label="t('project.fileManagement.gitFilePath')"
        :rules="[{ required: true, message: t('project.fileManagement.gitFilePathNotNull') }]"
        required
        asterisk-position="end"
        class="mb-0"
      >
        <a-input
          v-model:model-value="storageForm.path"
          :placeholder="t('project.fileManagement.gitFilePathPlaceholder')"
          :max-length="255"
        ></a-input>
        <MsFormItemSub :text="t('project.fileManagement.gitFilePathSub')" :show-fill-icon="false" />
      </a-form-item>
    </a-form>
    <template #footer>
      <a-button type="secondary" :disabled="storageModalLoading" @click="handleStorageModalCancel">
        {{ t('common.cancel') }}
      </a-button>
      <a-button type="secondary" :loading="storageModalLoading" @click="saveAndContinue">
        {{ t('common.saveAndContinue') }}
      </a-button>
      <a-button type="primary" :loading="storageModalLoading" @click="beforeAddStorageFile">
        {{ t('common.add') }}
      </a-button>
    </template>
  </a-modal>
  <a-modal
    v-model:visible="moveModalVisible"
    title-align="start"
    class="ms-modal-no-padding ms-modal-small"
    :mask-closable="false"
    :ok-text="t('project.fileManagement.batchMoveConfirm')"
    :ok-button-props="{ disabled: selectedModuleKeys.length === 0 }"
    :cancel-button-props="{ disabled: batchMoveFileLoading }"
    :on-before-ok="handleFileMove"
    @close="handleMoveFileModalCancel"
  >
    <template #title>
      <div class="flex items-center">
        {{ isBatchMove ? t('project.fileManagement.batchMoveTitle') : t('project.fileManagement.singleMoveTitle') }}
        <div
          class="one-line-text ml-[4px] max-w-[70%] text-[var(--color-text-4)]"
          :title="
            isBatchMove
              ? t('project.fileManagement.batchMoveTitleSub', { count: tableSelected.length })
              : `(${activeFile?.name})`
          "
        >
          {{
            isBatchMove
              ? t('project.fileManagement.batchMoveTitleSub', { count: tableSelected.length })
              : `(${activeFile?.name})`
          }}
        </div>
      </div>
    </template>
    <FolderTree
      v-if="moveModalVisible"
      v-model:selected-keys="selectedModuleKeys"
      :is-expand-all="true"
      :active-folder="props.activeFolder"
      is-modal
      @folder-node-select="folderNodeSelect"
    />
  </a-modal>
  <FileDetailDrawer
    v-model:visible="showDetailDrawer"
    :file-id="activeFileId"
    :active-file-index="activeFileIndex"
    :table-data="propsRes.data"
    :page-change="propsEvent.pageChange"
    :pagination="propsRes.msPagination!"
  />
</template>

<script setup lang="ts">
  import { computed, onBeforeMount, ref, watch } from 'vue';
  import { useRoute } from 'vue-router';
  import { Message, ValidatedError } from '@arco-design/web-vue';
  import { debounce } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import MsFileList from '@/components/pure/ms-upload/fileList.vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';
  import type { MsFileItem, UploadType } from '@/components/pure/ms-upload/types';
  import MsCardList from '@/components/business/ms-card-list/index.vue';
  import MsFormItemSub from '@/components/business/ms-form-item-sub/index.vue';
  import MsThumbnailCard from '@/components/business/ms-thumbnail-card/index.vue';
  import FileDetailDrawer from './fileDetailDrawer.vue';
  import FolderTree from './folderTree.vue';

  import {
    addRepositoryFile,
    batchDownloadFile,
    batchMoveFile,
    deleteFile,
    downloadFile,
    getFileList,
    getFileTypes,
    getRepositories,
    getRepositoryFileTypes,
    toggleJarFileStatus,
    uploadFile,
  } from '@/api/modules/project-management/fileManagement';
  import { CompressImgUrl } from '@/api/requrls/project-management/fileManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';
  import useAsyncTaskStore from '@/store/modules/app/asyncTask';
  import useUserStore from '@/store/modules/user';
  import { characterLimit, downloadByteFile, formatFileSize } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import type { FileItem, FileListQueryParams, Repository } from '@/models/projectManagement/file';
  import { RouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { UploadStatus } from '@/enums/uploadEnum';

  import type { FormInstance } from '@arco-design/web-vue';

  const props = defineProps<{
    activeFolder: string;
    activeFolderType: 'folder' | 'module' | 'storage';
    offspringIds: string[]; // 当前选中文件夹的所有子孙节点id
  }>();
  const emit = defineEmits<{
    (e: 'init', params: FileListQueryParams): void;
  }>();

  const route = useRoute();
  const { t } = useI18n();
  const appStore = useAppStore();
  const userStore = useUserStore();
  const asyncTaskStore = useAsyncTaskStore();
  const { openModal } = useModal();

  const fileType = ref('module'); // 当前查看的文件类型，模块/存储库
  const acceptType = ref<UploadType>('none'); // 模块-上传文件类型
  const isUploading = ref(false);
  const keyword = ref('');
  const loading = ref(false);

  const tableFileType = ref(''); // 文件格式筛选
  const tableFileTypeOptions = ref<string[]>([]);
  const fileTypeLoading = ref(false);

  /**
   * 初始化文件类型筛选选项
   */
  async function initFileTypes() {
    try {
      fileTypeLoading.value = true;
      let res: string[] = [];
      if (fileType.value === 'storage') {
        res = await getRepositoryFileTypes(appStore.currentProjectId);
      } else {
        res = await getFileTypes(appStore.currentProjectId);
      }
      tableFileType.value = '';
      tableFileTypeOptions.value = res;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      fileTypeLoading.value = false;
    }
  }

  watch(
    () => props.activeFolderType,
    () => {
      initFileTypes();
    },
    {
      immediate: true,
    }
  );

  const showType = ref<'list' | 'card'>('list'); // 文件列表展示形式

  function getCardClass(type: 'none' | 'jar') {
    if (acceptType.value !== type && isUploading.value) {
      return 'file-type-card file-type-card--disabled';
    }
    if (acceptType.value === type) {
      return 'file-type-card file-type-card--active';
    }
    return 'file-type-card';
  }

  const normalFileActions = computed(() => {
    if (fileType.value === 'storage') {
      return [
        {
          label: 'project.fileManagement.delete',
          eventTag: 'delete',
          danger: true,
          permission: ['PROJECT_FILE_MANAGEMENT:READ+DELETE'],
        },
      ];
    }
    const normalActions = [
      {
        label: 'project.fileManagement.move',
        eventTag: 'move',
        permission: ['PROJECT_FILE_MANAGEMENT:READ+UPDATE'],
      },
      {
        isDivider: true,
      },
      {
        label: 'project.fileManagement.delete',
        eventTag: 'delete',
        permission: ['PROJECT_FILE_MANAGEMENT:READ+DELETE'],
        danger: true,
      },
    ];
    if (showType.value === 'card') {
      return [
        {
          label: 'project.fileManagement.download',
          eventTag: 'download',
          permission: ['PROJECT_FILE_MANAGEMENT:READ+DOWNLOAD'],
        },
        ...normalActions,
      ];
    }
    return normalActions;
  });

  function getJarFileActions(record: FileItem) {
    const moveFileActions: ActionsItem[] = [
      {
        label: 'project.fileManagement.move',
        eventTag: 'move',
        permission: ['PROJECT_FILE_MANAGEMENT:READ+UPDATE'],
      },
    ];
    let enableFileActions = [
      {
        label: 'common.enable',
        eventTag: 'toggle',
        permission: ['PROJECT_FILE_MANAGEMENT:READ+UPDATE'],
      },
      {
        label: 'common.disable',
        eventTag: 'toggle',
        permission: ['PROJECT_FILE_MANAGEMENT:READ+UPDATE'],
      },
    ];

    if (record.enable) {
      enableFileActions = enableFileActions.filter((e) => e.label !== 'common.enable');
    } else if (record.enable === false) {
      enableFileActions = enableFileActions.filter((e) => e.label !== 'common.disable');
    }

    const deleteFileActions: ActionsItem[] = [
      {
        isDivider: true,
      },
      {
        label: 'project.fileManagement.delete',
        eventTag: 'delete',
        permission: ['PROJECT_FILE_MANAGEMENT:READ+DELETE'],
        danger: true,
      },
    ];
    if (showType.value === 'card') {
      return [
        {
          label: 'project.fileManagement.download',
          eventTag: 'download',
          permission: ['PROJECT_FILE_MANAGEMENT:READ+DOWNLOAD'],
        },
        ...moveFileActions,
        ...enableFileActions,
        ...deleteFileActions,
      ];
    }

    if (record.storage === 'GIT') {
      enableFileActions = showType.value === 'list' ? [] : enableFileActions;

      return [...enableFileActions, ...deleteFileActions];
    }

    return [...moveFileActions, ...deleteFileActions];
  }
  const hasOperationPermission = computed(() =>
    hasAnyPermission([
      'PROJECT_FILE_MANAGEMENT:READ+UPDATE',
      'PROJECT_FILE_MANAGEMENT:READ+DELETE',
      'PROJECT_FILE_MANAGEMENT:READ+DOWNLOAD',
    ])
  );

  const columns: MsTableColumn = [
    {
      title: 'project.fileManagement.name',
      slotName: 'name',
      dataIndex: 'name',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 270,
    },
    {
      title: 'project.fileManagement.type',
      slotName: 'fileType',
      dataIndex: 'fileType',
      width: 100,
    },
    {
      title: 'project.fileManagement.size',
      dataIndex: 'size',
      slotName: 'size',
      width: 120,
    },
    {
      title: 'project.fileManagement.tag',
      dataIndex: 'tags',
      isTag: true,
    },
    {
      title: 'project.fileManagement.creator',
      dataIndex: 'createUser',
      showTooltip: true,
      width: 120,
    },
    {
      title: 'project.fileManagement.updater',
      dataIndex: 'updateUser',
      showTooltip: true,
      width: 120,
    },
    {
      title: 'project.fileManagement.updateTime',
      dataIndex: 'updateTime',
      width: 180,
    },
    {
      title: 'project.fileManagement.createTime',
      dataIndex: 'createTime',
      width: 180,
    },
    {
      title: hasOperationPermission.value ? 'common.operation' : '',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: hasOperationPermission.value ? 180 : 50,
    },
  ];
  const tableStore = useTableStore();
  await tableStore.initColumn(TableKeyEnum.FILE_MANAGEMENT_FILE, columns, 'drawer', true);
  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector, resetPagination } = useTable(
    getFileList,
    {
      tableKey: TableKeyEnum.FILE_MANAGEMENT_FILE,
      showSetting: true,
      selectable: !!hasAnyPermission([
        'PROJECT_FILE_MANAGEMENT:READ+DOWNLOAD',
        'PROJECT_FILE_MANAGEMENT:READ+UPDATE',
        'PROJECT_FILE_MANAGEMENT:READ+DELETE',
      ]),
      heightUsed: 242,
      showSelectAll: true,
      showSubdirectory: true,
    },
    (item) => {
      return {
        ...item,
        tags: item.tags?.map((e: string) => ({ id: e, name: e })) || [],
      };
    }
  );
  const moduleFileBatchActions = {
    baseAction: [
      {
        label: 'project.fileManagement.download',
        eventTag: 'download',
        permission: ['PROJECT_FILE_MANAGEMENT:READ+DOWNLOAD'],
      },
      {
        label: 'project.fileManagement.move',
        eventTag: 'move',
        permission: ['PROJECT_FILE_MANAGEMENT:READ+UPDATE'],
      },
      {
        label: 'project.fileManagement.delete',
        eventTag: 'delete',
        danger: true,
        permission: ['PROJECT_FILE_MANAGEMENT:READ+DELETE'],
      },
    ],
  };
  const storageFileBatchActions = {
    baseAction: [
      {
        label: 'project.fileManagement.download',
        eventTag: 'download',
        permission: ['PROJECT_FILE_MANAGEMENT:READ+DOWNLOAD'],
      },
      {
        label: 'project.fileManagement.delete',
        eventTag: 'delete',
        danger: true,
        permission: ['PROJECT_FILE_MANAGEMENT:READ+DELETE'],
      },
    ],
  };
  const tableSelected = ref<(string | number)[]>([]);
  const batchParams = ref<BatchActionQueryParams>({
    selectedIds: [],
    selectAll: false,
    excludeIds: [],
    currentSelectCount: 0,
  });
  const combine = ref<Record<string, any>>({});
  const isMyOrAllFolder = computed(() => ['my', 'all'].includes(props.activeFolder)); // 是否是我的文件/全部文件

  /**
   * 处理表格选中
   */
  function handleTableSelect(arr: (string | number)[]) {
    tableSelected.value = arr;
  }

  /**
   * 批量下载文件
   */
  async function batchDownload() {
    try {
      loading.value = true;
      const res = await batchDownloadFile({
        selectIds: batchParams.value?.selectedIds || [],
        selectAll: !!batchParams.value?.selectAll,
        excludeIds: batchParams.value?.excludeIds || [],
        condition: { keyword: keyword.value, combine: combine.value },
        projectId: appStore.currentProjectId,
        fileType: tableFileType.value,
        moduleIds: isMyOrAllFolder.value ? [] : [props.activeFolder],
      });
      downloadByteFile(res, 'files.zip');
      resetSelector();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  function emitTableParams() {
    emit('init', {
      keyword: keyword.value,
      fileType: tableFileType.value,
      moduleIds: [],
      projectId: appStore.currentProjectId,
      current: propsRes.value.msPagination?.current,
      pageSize: propsRes.value.msPagination?.pageSize,
      combine: combine.value,
    });
  }

  const cardListRef = ref<InstanceType<typeof MsCardList>>();

  /**
   * 删除文件
   */
  function delFile(record: FileItem | null, isBatch: boolean) {
    let title = t('project.fileManagement.deleteFileTipTitle', { name: characterLimit(record?.name) });
    let selectIds = [record?.id || ''];
    if (isBatch) {
      title = t('project.fileManagement.batchDeleteFileTipTitle', {
        count: batchParams.value?.currentSelectCount || batchParams.value?.selectedIds?.length,
      });
      selectIds = batchParams.value?.selectedIds || [];
    }
    openModal({
      type: 'error',
      title,
      content: t('project.fileManagement.deleteFileTipContent'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await deleteFile({
            selectIds,
            selectAll: !!batchParams.value?.selectAll,
            excludeIds: batchParams.value?.excludeIds || [],
            condition: { keyword: keyword.value, combine: combine.value },
            projectId: appStore.currentProjectId,
            fileType: tableFileType.value,
            moduleIds: isMyOrAllFolder.value ? [] : [props.activeFolder],
          });
          Message.success(t('common.deleteSuccess'));
          if (showType.value === 'card') {
            cardListRef.value?.reload();
          } else {
            loadList();
            resetSelector();
          }
          emitTableParams();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  const moveModalVisible = ref(false); // 移动文件弹窗
  const selectedModuleKeys = ref<(string | number)[]>([]); // 移动文件选中节点
  const isBatchMove = ref(false); // 是否批量移动文件
  const activeFile = ref<FileItem | null>(null); // 当前查看的文件信息

  /**
   * 处理文件夹树节点选中事件
   */
  function folderNodeSelect(keys: (string | number)[]) {
    selectedModuleKeys.value = keys;
  }

  /**
   * 处理表格选中后批量操作
   * @param event 批量操作事件对象
   */
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    tableSelected.value = params?.selectedIds || [];
    batchParams.value = params;
    switch (event.eventTag) {
      case 'download':
        batchDownload();
        break;
      case 'move':
        moveModalVisible.value = true;
        isBatchMove.value = true;
        break;
      case 'delete':
        delFile(null, true);
        break;
      default:
        break;
    }
  }

  const batchMoveFileLoading = ref(false);
  /**
   * 单个/批量移动文件
   */
  async function handleFileMove() {
    try {
      batchMoveFileLoading.value = true;
      await batchMoveFile({
        selectIds: isBatchMove.value ? batchParams.value?.selectedIds || [] : [activeFile.value?.id || ''],
        selectAll: !!batchParams.value?.selectAll,
        excludeIds: batchParams.value?.excludeIds || [],
        condition: { keyword: keyword.value, combine: combine.value },
        projectId: appStore.currentProjectId,
        fileType: tableFileType.value,
        moduleIds: isMyOrAllFolder.value ? [] : [props.activeFolder],
        moveModuleId: selectedModuleKeys.value[0],
      });
      Message.success(t('project.fileManagement.batchMoveSuccess'));
      if (isBatchMove.value) {
        tableSelected.value = [];
        isBatchMove.value = false;
      } else {
        activeFile.value = null;
      }
      if (showType.value === 'card') {
        cardListRef.value?.reload();
      } else {
        loadList();
        resetSelector();
      }
      emitTableParams();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      batchMoveFileLoading.value = false;
    }
  }

  function handleMoveFileModalCancel() {
    moveModalVisible.value = false;
    selectedModuleKeys.value = [];
  }

  async function getModuleIds() {
    let moduleIds = [props.activeFolder];
    const getAllChildren = await tableStore.getSubShow(TableKeyEnum.FILE_MANAGEMENT_FILE);
    if (getAllChildren) {
      moduleIds = [props.activeFolder, ...props.offspringIds];
    }
    return moduleIds;
  }

  async function setTableParams() {
    if (props.activeFolder === 'my') {
      combine.value.createUser = userStore.id;
    } else {
      combine.value.createUser = '';
    }
    if (fileType.value === 'storage') {
      combine.value.storage = 'git';
    } else {
      combine.value.storage = 'minio';
    }
    let moduleIds: string[];
    if (isMyOrAllFolder.value) {
      moduleIds = [];
    } else {
      moduleIds = await getModuleIds();
    }
    setLoadListParams({
      keyword: keyword.value,
      fileType: tableFileType.value,
      moduleIds,
      projectId: appStore.currentProjectId,
      combine: combine.value,
    });
  }

  /**
   * 更改文件展示类型：模块/存储库
   */
  async function changeFileType() {
    await initFileTypes();
    resetSelector();
    await setTableParams();
    if (showType.value === 'card') {
      cardListRef.value?.reload();
    } else {
      loadList();
    }
  }

  watch(
    () => showType.value,
    (val) => {
      if (val === 'list') {
        loadList();
      }
    }
  );

  watch(
    () => props.activeFolderType,
    (val) => {
      if (val === 'folder') {
        fileType.value = 'module';
      } else {
        fileType.value = val;
      }
      setTableParams();
    }
  );

  const searchList = debounce(async () => {
    await setTableParams();
    if (showType.value === 'card') {
      cardListRef.value?.reload();
    } else {
      loadList();
    }
    emitTableParams();
  }, 300);

  watch(
    () => props.activeFolder,
    () => {
      keyword.value = '';
      resetPagination();
      searchList();
      resetSelector();
    },
    { immediate: true }
  );

  /**
   * 下载单个文件
   * @param record 表格数据项
   */
  async function handleDownload(record: FileItem) {
    try {
      loading.value = true;
      const res = await downloadFile(record.id);
      downloadByteFile(res, `${record.name}.${record.fileType}`);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  /**
   * 启用/禁用 jar 文件
   */
  async function toggleJarFile(record: FileItem) {
    if (record.enable) {
      // 禁用
      openModal({
        type: 'warning',
        title: t('project.fileManagement.disabledFileTipTitle', { name: characterLimit(record.name) }),
        content: t('project.fileManagement.disabledFileTipContent'),
        okText: t('common.confirmDisable'),
        cancelText: t('common.cancel'),
        maskClosable: false,
        onBeforeOk: async () => {
          try {
            await toggleJarFileStatus(record.id, !record.enable);
            Message.success(t('common.disableSuccess'));
            if (showType.value === 'card') {
              cardListRef.value?.reload();
            } else {
              loadList();
            }
          } catch (error) {
            // eslint-disable-next-line no-console
            console.log(error);
          }
        },
        hideCancel: false,
      });
    } else {
      try {
        await toggleJarFileStatus(record.id, !record.enable);
        Message.success(t('common.enableSuccess'));
        if (showType.value === 'card') {
          cardListRef.value?.reload();
        } else {
          loadList();
        }
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    }
  }

  // 移动
  function handleMove(record: FileItem) {
    isBatchMove.value = false;
    activeFile.value = record;
    moveModalVisible.value = true;
  }

  /**
   * 处理表格更多按钮事件
   * @param item
   */
  function handleMoreActionSelect(item: ActionsItem, record: FileItem) {
    switch (item.eventTag) {
      case 'move':
        handleMove(record);
        break;
      case 'delete':
        delFile(record, false);
        break;
      case 'toggle':
        toggleJarFile(record);
        break;
      case 'download':
        handleDownload(record);
        break;
      default:
        break;
    }
  }

  const showDetailDrawer = ref(false);
  const activeFileId = ref<string>('');
  const activeFileIndex = ref(0);

  async function openFileDetail(id: string, index: number) {
    activeFileId.value = id;
    activeFileIndex.value = index;
    await nextTick(() => {
      showDetailDrawer.value = true;
    });
  }

  watch(
    () => showDetailDrawer.value,
    (val) => {
      if (!val) {
        loadList();
      }
    }
  );

  const uploadDrawerVisible = ref(false); // 模块-上传文件抽屉
  const allJarIsEnable = ref(false);
  const fileList = ref<MsFileItem[]>(asyncTaskStore.uploadFileTask.fileList);
  // 是否非上传中状态
  const noWaitingUpload = computed(
    () =>
      fileList.value.filter((e) => e.status && (e.status === UploadStatus.init || e.status === UploadStatus.uploading))
        .length === 0
  );

  /**
   * 设置上传文件类型
   * @param type 文件类型
   */
  function setAcceptType(type: UploadType) {
    if (isUploading.value || acceptType.value === type) return;
    acceptType.value = type;
    fileList.value = [];
    allJarIsEnable.value = false;
  }

  /**
   * 选择文件时，初始化文件参数
   * @param files 文件列表
   */
  function handleFileChange(files: MsFileItem[]) {
    fileList.value = files.map((e) => {
      e.enable = acceptType.value === 'jar' ? allJarIsEnable.value : false; // 是否启用
      return e; // 不能解构 e，否则会丢失响应性
    });
  }

  /**
   * 开启/关闭所有jar包
   * @param val 是否启用
   */
  function enableAllJar(val: string | number | boolean) {
    for (let i = 0; i < fileList.value.length; i++) {
      fileList.value[i].enable = !!val;
    }
  }

  const fileListRef = ref<InstanceType<typeof MsFileList>>();

  function handleUploadStart() {
    isUploading.value = true;
  }

  /**
   * 后台上传
   */
  function backstageUpload() {
    fileListRef.value?.backstageUpload();
    uploadDrawerVisible.value = false;
  }

  function startUpload() {
    fileListRef.value?.startUpload();
  }

  function uploadFinish() {
    isUploading.value = false;
  }

  /**
   * 取消上传二次确认
   */
  function cancelUpload() {
    if (asyncTaskStore.uploadFileTask.eachTaskQueue.length > 0 && asyncTaskStore.eachUploadTaskProgress !== 100) {
      openModal({
        type: 'error',
        title: t('project.fileManagement.cancelTipTitle'),
        content: t('project.fileManagement.cancelTipContent'),
        okText: t('project.fileManagement.cancelConfirm'),
        cancelText: t('project.fileManagement.cancel'),
        onBeforeOk: async () => {
          try {
            asyncTaskStore.cancelUpload();
            uploadDrawerVisible.value = false;
            fileList.value = [];
            isUploading.value = false;
          } catch (error) {
            // eslint-disable-next-line no-console
            console.log(error);
          }
        },
        hideCancel: false,
      });
    } else {
      uploadDrawerVisible.value = false;
    }
  }

  type RouteQueryPosition = 'uploadDrawer' | null;

  onBeforeMount(() => {
    if (route.query.position) {
      switch (
        route.query.position as RouteQueryPosition // 定位到上传文件抽屉，自动打开
      ) {
        case 'uploadDrawer':
          uploadDrawerVisible.value = true;
          break;

        default:
          break;
      }
    }
  });

  // 在第一次后台上传时从查看详情进来该页面，然后再次进行后台上传，在本页面点击查看详情时，由于路由地址一样所以不会触发onBeforeMount，所以这里需要检测是否后台下载的状态，以在当前页面判断是否进行查看详情操作
  watch(
    () => asyncTaskStore.uploadFileTask.isBackstageUpload,
    (val) => {
      if (!val && route.query.position === 'uploadDrawer') {
        uploadDrawerVisible.value = true;
      }
    }
  );

  watch(
    () => asyncTaskStore.uploadFileTask.finishedTime,
    () => {
      if (asyncTaskStore.uploadFileTask.finishedTime) {
        // 上传任务完成后刷新文件列表
        if (showType.value === 'card') {
          cardListRef.value?.reload();
        } else {
          loadList();
        }
        emitTableParams();
      }
    }
  );

  const storageDialogVisible = ref(false); // 存储库-上传文件弹窗
  const storageForm = ref({
    storage: '',
    branch: '',
    path: '',
  }); // 存储库-上传文件表单
  const storageFormRef = ref<FormInstance>(); // 存储库-上传文件表单ref
  const storageModalLoading = ref(false); // 存储库-上传文件弹窗loading
  const storageList = ref<Repository[]>([]); // 存储库列表
  const storageListLoading = ref(false); // 存储库列表loading

  async function initStorageList() {
    try {
      storageListLoading.value = true;
      const res = await getRepositories(appStore.currentProjectId);
      storageList.value = res;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      storageListLoading.value = false;
    }
  }

  /**
   * 处理添加文件按钮点击事件，根据当前查看的文件类型，打开不同的弹窗
   */
  function handleAddClick() {
    if (fileType.value === 'module') {
      uploadDrawerVisible.value = true;
    } else if (fileType.value === 'storage') {
      storageDialogVisible.value = true;
      if (isMyOrAllFolder.value) {
        // 如果是我的/全部文件夹，需要先获取存储库列表，因为此时添加存储库需要指定存储库id
        initStorageList();
      }
    }
  }

  function handleStorageModalCancel() {
    storageFormRef.value?.resetFields();
    storageDialogVisible.value = false;
  }

  /**
   * 存储库-添加文件
   * @param isContinue 是否继续添加
   */
  async function addStorageFile(isContinue?: boolean) {
    const params = {
      moduleId: isMyOrAllFolder.value ? storageForm.value.storage : props.activeFolder,
      branch: storageForm.value.branch,
      filePath: storageForm.value.path,
      enable: false,
    };
    await addRepositoryFile(params);
    Message.success(t('common.addSuccess'));
    emitTableParams();
    if (!isContinue) {
      storageDialogVisible.value = false;
    }
    if (showType.value === 'card') {
      cardListRef.value?.reload();
    } else {
      loadList();
    }
  }

  /**
   * 存储库-触发添加文件表单校验
   * @param cb 校验通过后执行回调
   */
  function storageFormValidate(cb: () => Promise<any>) {
    storageFormRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (!errors) {
        try {
          storageModalLoading.value = true;
          await cb();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          storageModalLoading.value = false;
        }
      }
    });
  }

  function saveAndContinue() {
    storageFormValidate(async () => {
      await addStorageFile(true);
      storageFormRef.value?.resetFields();
    });
  }

  function beforeAddStorageFile() {
    storageFormValidate(async () => {
      await addStorageFile();
      handleStorageModalCancel();
    });
  }

  // 开启|禁用
  function handleChangeEnable(newValue: string | number | boolean, record: FileItem) {
    toggleJarFile(record);
    return false;
  }

  await tableStore.initColumn(TableKeyEnum.FILE_MANAGEMENT_FILE, columns, 'drawer');
</script>

<style lang="less" scoped>
  .header {
    @apply flex items-center justify-between;

    margin-bottom: 16px;
    .header-right {
      @apply ml-auto flex items-center justify-end;

      width: 70%;
      gap: 8px;
      .show-type-icon {
        :deep(.arco-radio-button-content) {
          @apply flex;

          padding: 4px;
          line-height: 20px;
        }
      }
    }
  }
  .file-type-card {
    @apply flex cursor-pointer;

    padding: 16px;
    border: 1px solid var(--color-text-n8);
    border-radius: var(--border-radius-small);
    &:hover {
      box-shadow: 0 4px 10px rgb(100 100 102 / 15%);
    }
    .file-type-card-icon {
      margin-right: 12px;
    }
    .file-type-card-text {
      margin-bottom: 8px;
      color: var(--color-text-1);
    }
    .file-type-card-desc {
      font-size: 12px;
      color: var(--color-text-4);
      line-height: 16px;
    }
  }
  .file-type-card--active {
    border-color: rgb(var(--primary-5));
  }
  .file-type-card--disabled {
    @apply cursor-not-allowed hover:shadow-none;

    background-color: var(--color-text-n9);
    .file-type-card-icon {
      opacity: 0.6;
    }
    .file-type-card-text,
    .file-type-card-desc {
      color: var(--color-text-4);
    }
  }
  .card-list {
    @apply grid flex-1 overflow-auto;
    .ms-scroll-bar();
    .ms-container--shadow-y();

    gap: 24px;
    grid-template-columns: repeat(auto-fill, minmax(102px, 1fr));
    aspect-ratio: 1/1;
  }
</style>
