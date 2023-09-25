<template>
  <div class="flex h-[calc(100vh-88px)] flex-col overflow-hidden p-[24px]">
    <div class="header">
      <a-button type="primary" @click="handleAddClick">{{ t('project.fileManagement.addFile') }}</a-button>
      <div class="header-right">
        <a-select v-model="tableFileType" class="w-[240px]" @change="searchList">
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
        />
        <a-radio-group
          v-if="props.activeFolderType === 'folder'"
          v-model:model-value="fileType"
          type="button"
          class="file-show-type min-w-[92px]"
          @change="changeFileType"
        >
          <a-radio value="module" class="show-type-icon">{{ t('project.fileManagement.module') }}</a-radio>
          <a-radio value="storage" class="show-type-icon">{{ t('project.fileManagement.storage') }}</a-radio>
        </a-radio-group>
        <a-radio-group v-model:model-value="showType" type="button" class="file-show-type" @change="changeShowType">
          <a-radio value="list" class="show-type-icon p-[2px]"><MsIcon type="icon-icon_view-list_outlined" /></a-radio>
          <a-radio value="card" class="show-type-icon p-[2px]"><MsIcon type="icon-icon_card_outlined" /></a-radio>
        </a-radio-group>
      </div>
    </div>
    <ms-base-table
      v-if="showType === 'list'"
      v-bind="propsRes"
      :action-config="fileType === 'module' ? moduleFileBatchActions : storageFileBatchActions"
      no-disable
      v-on="propsEvent"
      @selected-change="handleTableSelect"
      @batch-action="handleTableBatch"
    >
      <template #name="{ record, rowIndex }">
        <a-button type="text" class="px-0" @click="openFileDetail(record.id, rowIndex)">{{ record.name }}</a-button>
      </template>
      <template #tag="{ record }">
        <MsTagGroup theme="outline" :tag-list="record.tag" is-string-tag />
      </template>
      <template #action="{ record }">
        <MsButton type="text" class="mr-[8px]" @click="downloadFile(record.url, record.name)">
          {{ t('project.fileManagement.download') }}
        </MsButton>
        <MsTableMoreAction
          :list="record.type === 'JAR' ? jarFileActions : normalFileActions"
          @select="handleMoreActionSelect($event, record)"
        />
      </template>
      <template v-if="keyword.trim() === ''" #empty>
        <div class="flex items-center justify-center p-[8px] text-[var(--color-text-4)]">
          {{ t('project.fileManagement.tableNoFile') }}
          <MsButton class="ml-[8px]" @click="handleAddClick">
            {{ t('project.fileManagement.addFile') }}
          </MsButton>
        </div>
      </template>
    </ms-base-table>
    <MsCardList
      v-else-if="showType === 'card'"
      mode="remote"
      :remote-func="getFileList"
      :shadow-limit="50"
      :card-min-width="102"
      class="flex-1"
    >
      <template #item="{ item, index }">
        <MsThumbnailCard
          :type="item.type"
          :url="item.url"
          :footer-text="item.name"
          :more-actions="item.type === 'JAR' ? jarFileActions : normalFileActions"
          @click="openFileDetail(item.id, index)"
          @action-select="handleMoreActionSelect($event, item)"
        />
      </template>
    </MsCardList>
  </div>
  <MsDrawer v-model:visible="uploadDrawerVisible" :title="t('project.fileManagement.addFile')" :width="680">
    <div class="mb-[8px] flex items-center justify-between text-[var(--color-text-1)]">
      {{ t('project.fileManagement.fileType') }}
      <div class="flex items-center text-[12px] text-[rgb(var(--warning-6))]">
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
      :sub-text="acceptType === 'jar' ? '' : t('project.fileManagement.normalFileSubText', { size: 50 })"
      multiple
      size-unit="MB"
      class="mb-[16px] w-full"
      @change="handleFileChange"
    />
    <MsFileList
      ref="fileListRef"
      v-model:file-list="fileList"
      :route="RouteEnum.PROJECT_MANAGEMENT_FILE_MANAGEMENT"
      :route-query="{ position: 'uploadDrawer' }"
      @start="handleUploadStart"
      @finish="uploadFinish"
    >
      <template #tabExtra>
        <div v-if="acceptType === 'jar'" class="flex items-center gap-[4px]">
          <a-switch size="small" :disabled="fileList.length === 0" @change="enableAllJar"></a-switch>
          {{ t('project.fileManagement.enableAll') }}
          <a-tooltip :content="t('project.fileManagement.uploadTip')">
            <MsIcon type="icon-icon-maybe_outlined" class="cursor-pointer hover:text-[rgb(var(--primary-5))]" />
          </a-tooltip>
        </div>
      </template>
      <template #actions="{ item }">
        <a-switch v-if="acceptType === 'jar'" v-model:model-value="item.enable" size="small"></a-switch>
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
        field="branch"
        :label="t('project.fileManagement.gitBranch')"
        :rules="[{ required: true, message: t('project.fileManagement.gitBranchNotNull') }]"
        required
        asterisk-position="end"
      >
        <a-input
          v-model:model-value="storageForm.branch"
          :placeholder="t('project.fileManagement.gitBranchPlaceholder')"
          :max-length="250"
        ></a-input>
      </a-form-item>
      <a-form-item
        field="path"
        :label="t('project.fileManagement.gitFilePath')"
        :rules="[{ required: true, message: t('project.fileManagement.gitFilePathNotNull') }]"
        required
        asterisk-position="end"
      >
        <a-input
          v-model:model-value="storageForm.path"
          :placeholder="t('project.fileManagement.gitFilePathPlaceholder')"
          :max-length="250"
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
    :on-before-ok="batchMoveFile"
    @close="handleMoveFileModalCancel"
  >
    <template #title>
      <div class="flex items-center">
        {{ isBatchMove ? t('project.fileManagement.batchMoveTitle') : t('project.fileManagement.singleMoveTitle') }}
        <div class="ml-[4px] text-[var(--color-text-4)]">
          {{
            isBatchMove
              ? t('project.fileManagement.batchMoveTitleSub', { count: tableSelected.length })
              : `(${activeFile?.name})`
          }}
        </div>
      </div>
    </template>
    <folderTree
      v-if="moveModalVisible"
      v-model:selected-keys="selectedModuleKeys"
      :is-expand-all="true"
      is-modal
      @folder-node-select="folderNodeSelect"
    />
  </a-modal>
  <fileDetailDrawerVue
    v-model:visible="showDetailDrawer"
    :file-id="activeFileId"
    :active-file-index="activeFileIndex"
    :table-data="propsRes.data"
    :page-change="propsEvent.pageChange"
    :pagination="propsRes.msPagination"
  />
</template>

<script setup lang="ts">
  import { computed, onBeforeMount, ref, watch } from 'vue';
  import { useRoute } from 'vue-router';
  import { Message, ValidatedError } from '@arco-design/web-vue';
  import { debounce } from 'lodash-es';
  import { useI18n } from '@/hooks/useI18n';
  import useTableStore from '@/store/modules/ms-table';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { UploadStatus } from '@/enums/uploadEnum';
  import { RouteEnum } from '@/enums/routeEnum';
  import useAsyncTaskStore from '@/store/modules/app/asyncTask';
  import useModal from '@/hooks/useModal';
  import { characterLimit, downloadUrlFile } from '@/utils';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import useTable from '@/components/pure/ms-table/useTable';
  import { getFileList } from '@/api/modules/project-management/fileManagement';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';
  import MsFileList from '@/components/pure/ms-upload/fileList.vue';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import MsTagGroup from '@/components/pure/ms-tag/ms-tag-group.vue';
  import MsFormItemSub from '@/components/business/ms-form-item-sub/index.vue';
  import MsThumbnailCard from '@/components/business/ms-thumbnail-card/index.vue';
  import MsCardList from '@/components/business/ms-card-list/index.vue';
  import fileDetailDrawerVue from './fileDetailDrawer.vue';
  import folderTree from './folderTree.vue';

  import type { FormInstance } from '@arco-design/web-vue';
  import type { BatchActionParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import type { MsFileItem, UploadType } from '@/components/pure/ms-upload/types';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  const props = defineProps<{
    activeFolder: string | number;
    activeFolderType: 'folder' | 'module' | 'storage';
  }>();

  const route = useRoute();
  const { t } = useI18n();
  const asyncTaskStore = useAsyncTaskStore();
  const { openModal } = useModal();

  const fileType = ref('module'); // 当前查看的文件类型，模块/存储库
  const acceptType = ref<UploadType>('none'); // 模块-上传文件类型
  const isUploading = ref(false);

  watch(
    () => props.activeFolderType,
    (val) => {
      if (val === 'folder') {
        fileType.value = 'module';
      } else {
        fileType.value = val;
      }
    }
  );

  function changeFileType() {}

  const showType = ref<'list' | 'card'>('list'); // 文件列表展示形式

  function changeShowType() {}

  function getCardClass(type: 'none' | 'jar') {
    if (acceptType.value !== type && isUploading.value) {
      return 'file-type-card file-type-card--disabled';
    }
    if (acceptType.value === type) {
      return 'file-type-card file-type-card--active';
    }
    return 'file-type-card';
  }

  const normalFileActions: ActionsItem[] = [
    {
      label: 'project.fileManagement.move',
      eventTag: 'move',
    },
    {
      isDivider: true,
    },
    {
      label: 'project.fileManagement.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];

  const jarFileActions: ActionsItem[] = [
    {
      label: 'project.fileManagement.move',
      eventTag: 'move',
    },
    {
      label: 'common.disable',
      eventTag: 'disabled',
    },
    {
      isDivider: true,
    },
    {
      label: 'project.fileManagement.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];

  const columns: MsTableColumn = [
    {
      title: 'project.fileManagement.name',
      slotName: 'name',
      dataIndex: 'name',
      showTooltip: true,
    },
    {
      title: 'project.fileManagement.type',
      dataIndex: 'type',
      width: 90,
    },
    {
      title: 'project.fileManagement.tag',
      dataIndex: 'tag',
      slotName: 'tag',
      isTag: true,
    },
    {
      title: 'project.fileManagement.creator',
      dataIndex: 'creator',
      showTooltip: true,
    },
    {
      title: 'project.fileManagement.updater',
      dataIndex: 'updater',
      showTooltip: true,
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
      title: 'common.operation',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: 120,
    },
  ];
  const tableStore = useTableStore();
  tableStore.initColumn(TableKeyEnum.FILE_MANAGEMENT_FILE, columns, 'drawer');
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getFileList, {
    tableKey: TableKeyEnum.FILE_MANAGEMENT_FILE,
    columns,
    showSetting: true,
    selectable: true,
    showSelectAll: true,
  });
  const moduleFileBatchActions = {
    baseAction: [
      {
        label: 'project.fileManagement.download',
        eventTag: 'download',
      },
      {
        label: 'project.fileManagement.move',
        eventTag: 'move',
      },
      {
        label: 'project.fileManagement.delete',
        eventTag: 'delete',
        danger: true,
      },
    ],
  };
  const storageFileBatchActions = {
    baseAction: [
      {
        label: 'project.fileManagement.download',
        eventTag: 'download',
      },
      {
        label: 'project.fileManagement.delete',
        eventTag: 'delete',
        danger: true,
      },
    ],
  };
  const tableSelected = ref<(string | number)[]>([]);

  /**
   * 处理表格选中
   */
  function handleTableSelect(arr: (string | number)[]) {
    tableSelected.value = arr;
  }

  function batchDownload() {}

  /**
   * 删除文件
   */
  function delFile(record: any, isBatch?: boolean) {
    let title = t('project.fileManagement.deleteFileTipTitle', { name: characterLimit(record?.name) });
    let selectIds = [record?.id];
    if (isBatch) {
      title = t('project.fileManagement.batchDeleteFileTipTitle', { count: tableSelected.value.length });
      selectIds = tableSelected.value as string[];
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
          console.log(selectIds);

          Message.success(t('common.deleteSuccess'));
          loadList();
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  const moveModalVisible = ref(false); // 移动文件弹窗
  const selectedModuleKeys = ref<(string | number)[]>([]); // 移动文件搜索关键字
  const isBatchMove = ref(false); // 是否批量移动文件
  const activeFile = ref<any>(null); // 当前查看的文件信息

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
  function handleTableBatch(event: BatchActionParams) {
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
  async function batchMoveFile() {
    try {
      batchMoveFileLoading.value = true;
      await new Promise((resolve) => {
        setTimeout(() => resolve(true), 2000);
      });
      Message.success(t('project.fileManagement.batchMoveSuccess'));
      if (isBatchMove.value) {
        tableSelected.value = [];
        isBatchMove.value = false;
      } else {
        activeFile.value = null;
      }
      loadList();
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

  const keyword = ref('');
  const tableFileType = ref('');
  const tableFileTypeOptions = ref(['JPG', 'PNG']);

  const searchList = debounce(() => {
    setLoadListParams({
      fileType: tableFileType.value,
      keyword: keyword.value,
    });
    loadList();
  }, 300);

  watch(
    () => props.activeFolder,
    () => {
      keyword.value = '';
      searchList();
    },
    { immediate: true }
  );

  watch(
    () => keyword.value,
    () => {
      searchList();
    }
  );

  function downloadFile(url: string, name: string) {
    downloadUrlFile(url, name);
  }

  /**
   * 禁用 jar 文件
   */
  function disabledFile(record: any) {
    openModal({
      type: 'warning',
      title: t('project.fileManagement.disabledFileTipTitle', { name: characterLimit(record.name) }),
      content: t('project.fileManagement.disabledFileTipContent'),
      okText: t('common.confirmDisable'),
      cancelText: t('common.cancel'),
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          Message.success(t('common.disableSuccess'));
          loadList();
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  /**
   * 处理表格更多按钮事件
   * @param item
   */
  function handleMoreActionSelect(item: ActionsItem, record: any) {
    switch (item.eventTag) {
      case 'move':
        isBatchMove.value = false;
        activeFile.value = record;
        moveModalVisible.value = true;
        break;
      case 'delete':
        delFile(record);
        break;
      case 'disabled':
        disabledFile(record);
        break;
      default:
        break;
    }
  }

  const showDetailDrawer = ref(false);
  const activeFileId = ref<string | number>('');
  const activeFileIndex = ref(0);

  async function openFileDetail(id: string | number, index: number) {
    showDetailDrawer.value = true;
    activeFileId.value = id;
    activeFileIndex.value = index;
  }

  const uploadDrawerVisible = ref(false); // 模块-上传文件抽屉
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
    if (isUploading.value) return;
    acceptType.value = type;
    fileList.value = [];
  }

  /**
   * 选择文件时，初始化文件参数
   * @param files 文件列表
   */
  function handleFileChange(files: MsFileItem[]) {
    fileList.value = files.map((e) => {
      if (e.enable !== undefined) {
        return e;
      }
      return {
        ...e,
        enable: false, // 是否启用
      };
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

  const storageDialogVisible = ref(false); // 存储库-上传文件弹窗
  const storageForm = ref({
    branch: '',
    path: '',
  }); // 存储库-上传文件表单
  const storageFormRef = ref<FormInstance>(); // 存储库-上传文件表单ref
  const storageModalLoading = ref(false); // 存储库-上传文件弹窗loading

  /**
   * 处理添加文件按钮点击事件，根据当前查看的文件类型，打开不同的弹窗
   */
  function handleAddClick() {
    if (fileType.value === 'module') {
      uploadDrawerVisible.value = true;
    } else if (fileType.value === 'storage') {
      storageDialogVisible.value = true;
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
      branch: storageForm.value.branch,
      path: storageForm.value.path,
    };
    // await batchCreateUser(params);
    Message.success(t('common.addSuccess'));
    if (!isContinue) {
      storageDialogVisible.value = false;
    }
    loadList();
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
    .ms-container--shadow();

    gap: 24px;
    grid-template-columns: repeat(auto-fill, minmax(102px, 1fr));
    aspect-ratio: 1/1;
  }
</style>
