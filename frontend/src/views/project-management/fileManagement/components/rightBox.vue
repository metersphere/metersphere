<template>
  <div class="p-[24px]">
    <div class="header">
      <a-button type="primary" @click="uploadDrawerVisible = true">{{ t('project.fileManagement.addFile') }}</a-button>
      <div class="header-right">
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('project.fileManagement.folderSearchPlaceholder')"
          allow-clear
          class="w-[240px]"
        ></a-input-search>
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
    <ms-base-table v-bind="propsRes" no-disable v-on="propsEvent">
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
          @select="handleSelect($event, record)"
        ></MsTableMoreAction>
      </template>
      <template v-if="keyword.trim() === ''" #empty>
        <div class="flex items-center justify-center p-[8px] text-[var(--color-text-4)]">
          {{ t('project.fileManagement.tableNoFile') }}
          <MsButton class="ml-[8px]" @click="uploadDrawerVisible = true">
            {{ t('project.fileManagement.addFile') }}
          </MsButton>
        </div>
      </template>
    </ms-base-table>
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
  <fileDetailDrawerVue
    v-model:visible="showDetailDrawer"
    :file-id="activeFileId"
    :is-first="activeFileIsFirst"
    :is-last="activeFileIsLast"
    @prev-file="openPrevFile"
    @next-file="openNextFile"
  />
</template>

<script setup lang="ts">
  import { computed, onBeforeMount, ref, watch } from 'vue';
  import { useRoute } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
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
  import fileDetailDrawerVue from './fileDetailDrawer.vue';

  import type { MsTableColumn } from '@/components/pure/ms-table/type';
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

  const keyword = ref('');
  const fileType = ref('module');
  const acceptType = ref<UploadType>('none');
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

  function changeFileType() {
    console.log(fileType.value);
  }

  const showType = ref('list');

  function changeShowType() {
    console.log(showType.value);
  }

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
      label: 'project.fileManagement.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];

  const jarFileActions: ActionsItem[] = [
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
    },
    {
      title: 'project.fileManagement.updater',
      dataIndex: 'updater',
    },
    {
      title: 'project.fileManagement.updateTime',
      dataIndex: 'updateTime',
      width: 170,
    },
    {
      title: 'project.fileManagement.createTime',
      dataIndex: 'createTime',
      width: 170,
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
  const { propsRes, propsEvent, loadList } = useTable(getFileList, {
    tableKey: TableKeyEnum.FILE_MANAGEMENT_FILE,
    columns,
    showSetting: true,
  });

  watch(
    () => props.activeFolder,
    () => {
      keyword.value = '';
      debounce(loadList, 200)();
    },
    { immediate: true }
  );

  function downloadFile(url: string, name: string) {
    downloadUrlFile(url, name);
  }

  /**
   * 删除文件
   */
  function delFile(record: any) {
    openModal({
      type: 'error',
      title: t('project.fileManagement.deleteFileTipTitle', { name: characterLimit(record.name) }),
      content: t('project.fileManagement.deleteFileTipContent'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          Message.success(t('common.deleteSuccess'));
          loadList();
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
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
  function handleSelect(item: ActionsItem, record: any) {
    switch (item.eventTag) {
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
  // 当前查看的文件是否是总数据的第一条数据，用当前查看数据的下标是否等于0，且当前页码是否等于1
  const activeFileIsFirst = computed(() => activeFileIndex.value === 0 && propsRes.value.msPagination?.current === 1);
  const activeFileIsLast = computed(
    // 当前查看的文件是否是总数据的最后一条数据，用当前页码*每页条数+当前查看的条数下标，是否等于总条数
    () =>
      activeFileIndex.value === propsRes.value.data.length - 1 &&
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      propsRes.value.msPagination!.current * propsRes.value.msPagination!.pageSize + activeFileIndex.value >=
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        propsRes.value.msPagination!.total
  );

  async function openFileDetail(id: string, index: number) {
    showDetailDrawer.value = true;
    activeFileId.value = id;
    activeFileIndex.value = index;
  }

  async function openPrevFile() {
    if (!activeFileIsFirst.value) {
      // 当前不是第一条，则往前查看
      if (activeFileIndex.value === 0 && propsRes.value.msPagination) {
        // 当前查看的是当前页的第一条数据，则需要加载上一页的数据
        await propsEvent.value.pageChange(propsRes.value.msPagination.current - 1);
        activeFileId.value = propsRes.value.data[propsRes.value.data.length - 1].id;
        activeFileIndex.value = propsRes.value.data.length - 1;
      } else {
        // 当前查看的不是当前页的第一条数据，则直接查看上一条数据
        activeFileId.value = propsRes.value.data[activeFileIndex.value - 1].id;
        activeFileIndex.value -= 1;
      }
    }
  }

  async function openNextFile() {
    if (!activeFileIsLast.value) {
      // 当前不是最后一条，则往后查看
      if (activeFileIndex.value === propsRes.value.data.length - 1 && propsRes.value.msPagination) {
        // 当前查看的是当前页的最后一条数据，则需要加载下一页的数据
        await propsEvent.value.pageChange(propsRes.value.msPagination.current + 1);
        activeFileId.value = propsRes.value.data[0].id;
        activeFileIndex.value = 0;
      } else {
        // 当前查看的不是当前页的最后一条数据，则直接查看下一条数据
        activeFileId.value = propsRes.value.data[activeFileIndex.value + 1].id;
        activeFileIndex.value += 1;
      }
    }
  }

  const uploadDrawerVisible = ref(false);
  const fileList = ref<MsFileItem[]>(asyncTaskStore.uploadFileTask.fileList);

  const noWaitingUpload = computed(
    () =>
      fileList.value.filter((e) => e.status && (e.status === UploadStatus.init || e.status === UploadStatus.uploading))
        .length === 0
  );

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
</style>
