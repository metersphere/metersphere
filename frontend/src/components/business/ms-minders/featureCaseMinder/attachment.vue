<template>
  <a-spin :loading="attachmentLoading" class="block h-full pl-[16px]">
    <MsAddAttachment
      v-if="!props.notShowAddButton"
      v-model:file-list="fileList"
      :disabled="!hasEditPermission"
      multiple
      only-button
      @change="(files, file) => handleFileChange(file ? [file] : [])"
      @link-file="() => (showLinkFileDrawer = true)"
    />
    <MsFileList
      v-if="fileList.length > 0"
      ref="fileListRef"
      v-model:file-list="fileList"
      mode="static"
      :init-file-save-tips="t('ms.upload.waiting_save')"
      :show-upload-type-desc="true"
      :handle-delete="deleteFileHandler"
      :show-delete="!props.disabled"
      button-in-title
    >
      <template #title="{ item }">
        <span v-if="item.isUpdateFlag" class="ml-4 flex items-center font-normal text-[rgb(var(--warning-6))]">
          <icon-exclamation-circle-fill />
          <span>{{ t('caseManagement.featureCase.fileIsUpdated') }}</span>
        </span>
      </template>
      <template #titleAction="{ item }">
        <!-- 本地文件 -->
        <div v-if="item.local || item.status === 'init'" class="flex flex-nowrap">
          <MsButton
            v-if="item.file.type.includes('/image')"
            type="button"
            status="primary"
            class="!mr-[4px]"
            @click="handlePreview(item)"
          >
            {{ t('ms.upload.preview') }}
          </MsButton>
          <MsButton v-if="!props.disabled" type="button" status="primary" class="!mr-[4px]" @click="transferFile(item)">
            {{ t('caseManagement.featureCase.storage') }}
          </MsButton>
          <SaveAsFilePopover
            v-if="item.status !== 'init'"
            :saving-file="activeTransferFileParams"
            :file-save-as-source-id="activeCase.id || ''"
            :file-save-as-api="transferFileRequest"
            :file-module-options-api="getTransferFileTree"
            source-id-key="caseId"
            @finish="emit('uploadSuccess')"
          >
            <span :id="item.uid"></span>
          </SaveAsFilePopover>
          <MsButton
            v-if="item.status !== 'init'"
            type="button"
            status="primary"
            class="!mr-[4px]"
            @click="downloadFile(item)"
          >
            {{ t('caseManagement.featureCase.download') }}
          </MsButton>
        </div>
        <!-- 关联文件 -->
        <div v-else class="flex flex-nowrap">
          <MsButton
            v-if="item.file.type.includes('/image')"
            type="button"
            status="primary"
            class="!mr-[4px]"
            @click="handlePreview(item)"
          >
            {{ t('ms.upload.preview') }}
          </MsButton>
          <MsButton v-if="activeCase.id" type="button" status="primary" class="!mr-[4px]" @click="downloadFile(item)">
            {{ t('caseManagement.featureCase.download') }}
          </MsButton>
          <MsButton
            v-if="activeCase.id && item.isUpdateFlag"
            type="button"
            status="primary"
            :disabled="!hasEditPermission"
            @click="handleUpdateFile(item)"
          >
            {{ t('common.update') }}
          </MsButton>
        </div>
      </template>
    </MsFileList>
    <MsEmpty v-if="!fileList.length" />
  </a-spin>
  <LinkFileDrawer
    v-model:visible="showLinkFileDrawer"
    :get-tree-request="getModules"
    :get-count-request="getModulesCount"
    :get-list-request="getAssociatedFileListUrl"
    :get-list-fun-params="getListFunParams"
    @save="saveSelectAssociatedFile"
  />
  <a-image-preview v-model:visible="previewVisible" :src="imageUrl" />
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsEmpty from '@/components/pure/ms-empty/index.vue';
  import MsFileList from '@/components/pure/ms-upload/fileList.vue';
  import { MsFileItem } from '@/components/pure/ms-upload/types';
  import MsAddAttachment from '@/components/business/ms-add-attachment/index.vue';
  import SaveAsFilePopover from '@/components/business/ms-add-attachment/saveAsFilePopover.vue';
  import LinkFileDrawer from '@/components/business/ms-link-file/associatedFileDrawer.vue';

  import {
    deleteFileOrCancelAssociation,
    downloadFileRequest,
    getAssociatedFileListUrl,
    getTransferFileTree,
    previewFile,
    transferFileRequest,
    updateFile,
    uploadOrAssociationFile,
  } from '@/api/modules/case-management/featureCase';
  import { getModules, getModulesCount } from '@/api/modules/project-management/fileManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useAppStore from '@/store/modules/app';
  import { downloadByteFile } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { AssociatedList } from '@/models/caseManagement/featureCase';
  import { TableQueryParams } from '@/models/common';

  import { convertToFile } from '@/views/case-management/caseManagementFeature/components/utils';

  const props = defineProps<{
    activeCase: Record<string, any>;
    notShowAddButton?: boolean;
    disabled?: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'uploadSuccess'): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();
  const { openModal } = useModal();

  const fileList = defineModel<MsFileItem[]>({
    required: true,
  });

  const getListFunParams = ref<TableQueryParams>({
    combine: {
      hiddenIds: [],
    },
  });
  const hasEditPermission = hasAnyPermission(['FUNCTIONAL_CASE:READ+UPDATE']);

  // 监视文件列表处理关联和本地文件
  watch(
    () => fileList.value,
    (val) => {
      if (val) {
        getListFunParams.value.combine.hiddenIds = fileList.value.filter((item) => !item.local).map((item) => item.uid);
      }
    },
    { deep: true }
  );

  const showLinkFileDrawer = ref(false);
  const attachmentLoading = ref(false);

  /**
   * 处理文件更改
   * @param _fileList 文件列表
   * @param isAssociated 是否是关联文件
   */
  async function handleFileChange(_fileList: MsFileItem[], isAssociated = false) {
    try {
      attachmentLoading.value = true;
      const params = {
        request: {
          caseId: props.activeCase.id,
          projectId: appStore.currentProjectId,
          fileIds: isAssociated ? _fileList.map((item) => item.uid) : [],
          enable: true,
        },
        file: isAssociated ? _fileList.map((item) => item.file) : _fileList[0].file,
      };
      await uploadOrAssociationFile(params);
      if (isAssociated) {
        fileList.value.unshift(..._fileList);
      } else {
        fileList.value = fileList.value.map((item) => {
          if (item.status === 'init') {
            return { ...item, status: 'done', local: true };
          }
          return item;
        });
      }
      Message.success(t('common.linkSuccess'));
      emit('uploadSuccess');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      fileList.value = fileList.value.map((item) => ({ ...item, status: 'error' }));
    } finally {
      attachmentLoading.value = false;
    }
  }

  /**
   * 处理关联文件
   * @param fileData 文件信息集合
   */
  function saveSelectAssociatedFile(fileData: AssociatedList[]) {
    const fileResultList = fileData.map((fileInfo) => convertToFile(fileInfo));
    handleFileChange(fileResultList, true);
  }

  const imageUrl = ref('');
  const previewVisible = ref<boolean>(false);

  // 预览图片
  async function handlePreview(item: MsFileItem) {
    try {
      if (item.status !== 'init') {
        const res = await previewFile({
          projectId: appStore.currentProjectId,
          caseId: props.activeCase.id,
          fileId: item.uid,
          local: item.local,
        });
        const blob = new Blob([res], { type: 'image/jpeg' });
        imageUrl.value = URL.createObjectURL(blob);
      } else {
        imageUrl.value = item.url || '';
      }
      previewVisible.value = true;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const activeTransferFileParams = ref<MsFileItem>();

  // 转存
  function transferFile(item: MsFileItem) {
    activeTransferFileParams.value = { ...item };
    document.getElementById(item.uid)?.click();
  }

  // 删除本地文件
  async function deleteFileHandler(item: MsFileItem) {
    if (!item.local) {
      try {
        const params = {
          id: item.uid,
          local: item.local,
          caseId: props.activeCase.id,
          projectId: appStore.currentProjectId,
        };
        await deleteFileOrCancelAssociation(params);
        Message.success(
          item.local ? t('caseManagement.featureCase.deleteSuccess') : t('caseManagement.featureCase.cancelLinkSuccess')
        );
        fileList.value = fileList.value.filter((e) => e.uid !== item.uid);
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    } else {
      openModal({
        type: 'error',
        title: t('caseManagement.featureCase.deleteFile', { name: item?.name }),
        content: t('caseManagement.featureCase.deleteFileTip'),
        okText: t('common.confirmDelete'),
        cancelText: t('common.cancel'),
        okButtonProps: {
          status: 'danger',
        },
        onBeforeOk: async () => {
          try {
            const params = {
              id: item.uid,
              local: item.local,
              caseId: props.activeCase.id,
              projectId: appStore.currentProjectId,
            };
            await deleteFileOrCancelAssociation(params);
            Message.success(
              item.local
                ? t('caseManagement.featureCase.deleteSuccess')
                : t('caseManagement.featureCase.cancelLinkSuccess')
            );
            fileList.value = fileList.value.filter((e) => e.uid !== item.uid);
          } catch (error) {
            // eslint-disable-next-line no-console
            console.log(error);
          }
        },
        hideCancel: false,
      });
    }
  }

  // 下载
  async function downloadFile(item: MsFileItem) {
    try {
      const res = await downloadFileRequest({
        projectId: appStore.currentProjectId,
        caseId: props.activeCase.id,
        fileId: item.uid,
        local: item.local,
      });
      downloadByteFile(res, `${item.name}`);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  // 更新文件
  async function handleUpdateFile(item: MsFileItem) {
    try {
      await updateFile(appStore.currentProjectId, item.associationId);
      Message.success(t('common.updateSuccess'));
      emit('uploadSuccess');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
</script>

<style lang="less" scoped>
  :deep(.file-name-first) {
    max-width: 170px;
  }
</style>
