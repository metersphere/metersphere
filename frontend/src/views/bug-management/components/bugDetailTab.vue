<template>
  <div class="relative p-[16px] pb-[16px]">
    <div class="header">
      <div class="header-title">{{ t('bugManagement.edit.content') }}</div>
      <div v-permission="['PROJECT_BUG:READ+UPDATE']" class="header-action">
        <a-button type="text" @click="contentEditAble = !contentEditAble">
          <template #icon> <MsIconfont type="icon-icon_edit_outlined" /> </template>
          {{ t('bugManagement.edit.contentEdit') }}
        </a-button>
      </div>
    </div>
    <div class="mt-[16px]" :class="{ 'max-h-[260px]': contentEditAble }">
      <MsRichText
        v-if="contentEditAble"
        v-model:raw="form.description"
        v-model:filed-ids="fileIds"
        :disabled="!contentEditAble"
        :placeholder="t('bugManagement.edit.contentPlaceholder')"
        :upload-image="handleUploadImage"
      />
      <div v-else v-dompurify-html="form?.description || '-'" class="text-[var(--color-text-3)]"></div>
    </div>
    <div v-if="contentEditAble" class="mt-[8px] flex justify-end">
      <a-button type="secondary" @click="handleCancel">{{ t('common.cancel') }}</a-button>
      <a-button class="ml-[12px]" type="primary" :loading="confirmLoading" @click="handleSave">
        {{ t('common.save') }}
      </a-button></div
    >
    <div v-if="props.allowEdit">
      <div class="mt-[16px] font-medium text-[var(--color-text-1)]">
        {{ t('bugManagement.edit.file') }}
      </div>
      <div class="mt-[16px] pb-[4px]">
        <a-dropdown position="tr" trigger="hover">
          <a-button v-permission="['PROJECT_BUG:READ+UPDATE']" type="outline">
            <template #icon> <icon-plus class="text-[14px]" /> </template
            >{{ t('system.orgTemplate.addAttachment') }}</a-button
          >
          <template #content>
            <a-upload
              ref="uploadRef"
              v-model:file-list="fileList"
              :auto-upload="false"
              :show-file-list="false"
              :before-upload="beforeUpload"
            >
              <template #upload-button>
                <a-button type="text" class="!text-[var(--color-text-1)]">
                  <icon-upload />{{ t('caseManagement.featureCase.uploadFile') }}
                </a-button>
              </template>
            </a-upload>
            <a-button type="text" class="!text-[var(--color-text-1)]" @click="associatedFile">
              <MsIcon type="icon-icon_link-copy_outlined" size="16" />
              {{ t('caseManagement.featureCase.associatedFile') }}
            </a-button>
          </template>
        </a-dropdown>
      </div>
    </div>
    <div class="mb-[8px] mt-[2px] text-[var(--color-text-4)]">{{ t('bugManagement.edit.fileExtra') }}</div>
    <MsFileList
      ref="fileListRef"
      v-model:file-list="fileList"
      :show-tab="false"
      :request-params="{
        bugId: bugId,
        projectId: currentProjectId,
      }"
      :upload-func="uploadOrAssociationFile"
      :handle-delete="deleteFileHandler"
      :show-delete="props.allowEdit"
    >
      <template #actions="{ item }">
        <div v-if="props.allowEdit">
          <!-- 本地文件 -->
          <div v-if="item.local || item.status === 'init'" class="flex flex-nowrap">
            <MsButton
              v-if="item.status !== 'init'"
              type="button"
              status="primary"
              class="!mr-[4px]"
              @click="handlePreview(item)"
            >
              {{ t('ms.upload.preview') }}
            </MsButton>
            <MsButton type="button" status="primary" class="!mr-[4px]" @click="transferVisible = true">
              {{ t('caseManagement.featureCase.storage') }}
            </MsButton>
            <TransferModal
              v-model:visible="transferVisible"
              :request-fun="transferFileRequest"
              :params="{
                projectId: currentProjectId,
                bugId: bugId,
                fileId: item.uid,
                local: true,
              }"
              @success="emit('updateSuccess')"
            />
            <MsButton
              v-if="item.status === 'done'"
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
              v-if="item.status !== 'init'"
              type="button"
              status="primary"
              class="!mr-[4px]"
              @click="handlePreview(item)"
            >
              {{ t('ms.upload.preview') }}
            </MsButton>
            <MsButton
              v-if="item.status === 'done'"
              type="button"
              status="primary"
              class="!mr-[4px]"
              @click="downloadFile(item)"
            >
              {{ t('caseManagement.featureCase.download') }}
            </MsButton>
            <MsButton
              v-if="item.isUpdateFlag"
              type="button"
              status="primary"
              class="!mr-[4px]"
              @click="handleUpdateFile(item)"
            >
              {{ t('common.update') }}
            </MsButton>
          </div>
        </div>
      </template>
      <template #title="{ item }">
        <span v-if="item.isUpdateFlag" class="ml-4 flex items-center font-normal text-[rgb(var(--warning-6))]"
          ><icon-exclamation-circle-fill /> <span>{{ t('caseManagement.featureCase.fileIsUpdated') }}</span>
        </span>
      </template>
    </MsFileList>
  </div>
  <RelateFileDrawer
    v-model:visible="associatedDrawer"
    :get-tree-request="getModules"
    :get-count-request="getModulesCount"
    :get-list-request="getAssociatedFileList"
    :get-list-fun-params="getListFunParams"
    @save="saveSelectAssociatedFile"
  />
  <a-image-preview v-model:visible="previewVisible" :src="imageUrl" />
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import { FormRuleItem } from '@/components/pure/ms-form-create/types';
  import MsIconfont from '@/components/pure/ms-icon-font/index.vue';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import MsFileList from '@/components/pure/ms-upload/fileList.vue';
  import { MsFileItem } from '@/components/pure/ms-upload/types';
  import RelateFileDrawer from '@/components/business/ms-link-file/associatedFileDrawer.vue';
  import TransferModal from '@/views/case-management/caseManagementFeature/components/tabContent/transferModal.vue';

  import {
    checkFileIsUpdateRequest,
    createOrUpdateBug,
    deleteFileOrCancelAssociation,
    downloadFileRequest,
    editorUploadFile,
    getAssociatedFileList,
    previewFile,
    transferFileRequest,
    updateFile,
    uploadOrAssociationFile,
  } from '@/api/modules/bug-management';
  import { getModules, getModulesCount } from '@/api/modules/project-management/fileManagement';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { downloadByteFile, sleep } from '@/utils';

  import { BugEditCustomFieldItem, BugEditFormObject } from '@/models/bug-management';
  import { AssociatedList, AttachFileInfo } from '@/models/caseManagement/featureCase';
  import { TableQueryParams } from '@/models/common';

  import { convertToFileByBug } from '@/views/bug-management/utils';
  import { convertToFile } from '@/views/case-management/caseManagementFeature/components/utils';

  const { t } = useI18n();

  const props = defineProps<{
    detailInfo: BugEditFormObject;
    formItem: FormRuleItem[];
    allowEdit?: boolean; // 是否允许编辑
  }>();

  const emit = defineEmits<{
    (e: 'updateSuccess'): void;
  }>();

  const appStore = useAppStore();
  const transferVisible = ref<boolean>(false);
  const previewVisible = ref<boolean>(false);
  // 富文本附件id
  const fileIds = ref<string[]>([]);
  const imageUrl = ref<string>('');
  const associatedDrawer = ref(false);
  const fileListRef = ref<InstanceType<typeof MsFileList>>();
  // 富文本编辑器是否可编辑
  const contentEditAble = ref(false);
  const currentProjectId = computed(() => appStore.currentProjectId);
  const fileList = ref<MsFileItem[]>([]);
  const bugId = computed(() => props.detailInfo.id);
  const attachmentsList = ref<AttachFileInfo[]>([]);
  const getListFunParams = ref<TableQueryParams>({
    combine: {
      hiddenIds: [],
    },
  });
  const form = ref({
    description: '',
    deleteLocalFileIds: [] as string[],
    unLinkRefIds: [] as string[],
    linkFileIds: [] as string[],
  });

  const handleFileFunc = async (attachments: AttachFileInfo[]) => {
    if (attachments && attachments.length) {
      attachmentsList.value = attachments;
      // 检查文件是否有更新
      const checkUpdateFileIds = await checkFileIsUpdateRequest(attachments.map((item: any) => item.fileId));
      // 处理文件列表
      fileList.value =
        attachments
          .map((fileInfo: any) => {
            return {
              ...fileInfo,
              name: fileInfo.fileName,
              isUpdateFlag: checkUpdateFileIds.includes(fileInfo.id),
            };
          })
          .map((fileInfo: any) => {
            return convertToFileByBug(fileInfo);
          }) || [];
    }
  };

  const confirmLoading = ref<boolean>(false);

  const initCurrentDetail = async (detail: BugEditFormObject) => {
    const { attachments, description } = detail;
    form.value.description = description;
    handleFileFunc(attachments);
  };

  function handleCancel() {
    contentEditAble.value = false;
  }

  // 删除本地文件
  async function deleteFileHandler(item: MsFileItem) {
    try {
      const params = {
        refId: item.uid,
        associated: !item.local,
        bugId: bugId.value,
        projectId: currentProjectId.value,
      };
      await deleteFileOrCancelAssociation(params);
      Message.success(
        item.local ? t('caseManagement.featureCase.deleteSuccess') : t('caseManagement.featureCase.cancelLinkSuccess')
      );
      emit('updateSuccess');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function beforeUpload(file: File) {
    const _maxSize = 50 * 1024 * 1024;
    if (file.size > _maxSize) {
      Message.warning(t('ms.upload.overSize'));
      return Promise.resolve(false);
    }
    return Promise.resolve(true);
  }

  // 预览图片
  async function handlePreview(item: MsFileItem) {
    try {
      previewVisible.value = true;
      if (item.status !== 'init') {
        const res = await previewFile({
          projectId: currentProjectId.value,
          bugId: bugId.value as string,
          fileId: item.uid,
          associated: !item.local,
        });
        const blob = new Blob([res], { type: 'image/jpeg' });
        imageUrl.value = URL.createObjectURL(blob);
      } else {
        imageUrl.value = item.url || '';
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  // 下载
  async function downloadFile(item: MsFileItem) {
    try {
      const res = await downloadFileRequest({
        projectId: currentProjectId.value,
        bugId: bugId.value as string,
        fileId: item.uid,
        associated: !item.local,
      });
      downloadByteFile(res, `${item.name}`);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function associatedFile() {
    associatedDrawer.value = true;
  }

  // 处理关联文件
  function saveSelectAssociatedFile(fileData: AssociatedList[]) {
    const fileResultList = fileData.map((fileInfo) => convertToFile(fileInfo));
    fileList.value.push(...fileResultList);
  }

  // 后台传过来的local文件的item列表
  const oldLocalFileList = computed(() => {
    return attachmentsList.value.filter((item) => item.local).map((item: any) => item.uid);
  });

  // 已经关联过的id列表
  const associateFileIds = computed(() => {
    return attachmentsList.value.filter((item: any) => !item.local).map((item: any) => item.id);
  });

  // 当前新增传过来的关联list
  const currentAlreadyAssociateFileList = computed(() => {
    return fileList.value
      .filter((item) => !item.local && !associateFileIds.value.includes(item.uid))
      .map((item: any) => item.uid);
  });

  // 后台已保存本地文件的item列表
  const currentOldLocalFileList = computed(() => {
    return fileList.value.filter((item) => item.local && item.status !== 'init').map((item: any) => item.uid);
  });

  // 新增关联文件ID列表
  const newAssociateFileListIds = computed(() => {
    return fileList.value
      .filter((item: any) => !item.local && !associateFileIds.value.includes(item.uid))
      .map((item: any) => item.uid);
  });

  // 取消关联文件id TODO
  const unLinkFilesIds = computed(() => {
    const deleteAssociateFileIds = fileList.value
      .filter(
        (item: any) =>
          !currentAlreadyAssociateFileList.value.includes(item.uid) && associateFileIds.value.includes(item.uid)
      )
      .map((item) => item.uid);
    return associateFileIds.value.filter(
      (id: string) => !currentAlreadyAssociateFileList.value.includes(id) && !deleteAssociateFileIds.includes(id)
    );
  });

  // 删除本地上传的文件id
  const deleteFileMetaIds = computed(() => {
    return oldLocalFileList.value
      .filter((item: any) => !currentOldLocalFileList.value.includes(item.id))
      .map((item: any) => item.id);
  });

  // 处理关联文件和已关联文件本地文件和已上传文本文件
  function getFilesParams() {
    form.value.deleteLocalFileIds = deleteFileMetaIds.value;
    form.value.unLinkRefIds = unLinkFilesIds.value;
    form.value.linkFileIds = newAssociateFileListIds.value;
  }

  async function startUpload() {
    await sleep(300);
    fileListRef.value?.startUpload();
    emit('updateSuccess');
  }

  // 文件列表单个上传
  watch(
    () => fileList.value,
    async (val) => {
      const isNewFiles = val.filter((item) => item.status === 'init').length;
      if (val && isNewFiles) {
        startUpload();
      }
    }
  );

  // 更新文件
  async function handleUpdateFile(item: MsFileItem) {
    try {
      await updateFile(currentProjectId.value, item.associationId);
      Message.success(t('common.updateSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  async function handleUploadImage(file: File) {
    const { data } = await editorUploadFile({
      fileList: [file],
    });
    return data;
  }

  // 监视文件列表处理关联和本地文件
  watch(
    () => fileList.value,
    (val) => {
      if (val) {
        getListFunParams.value.combine.hiddenIds = fileList.value.filter((item) => !item.local).map((item) => item.uid);
        getFilesParams();
      }
    },
    { deep: true }
  );
  // 保存操作
  async function handleSave() {
    try {
      confirmLoading.value = true;
      const { formItem } = props;
      const customFields: BugEditCustomFieldItem[] = [];
      if (formItem && formItem.length) {
        formItem.forEach((item: FormRuleItem) => {
          customFields.push({
            id: item.field as string,
            name: item.title as string,
            type: item.sourceType as string,
            value: item.value as string,
          });
        });
      }
      const tmpObj: BugEditFormObject = {
        ...form.value,
        id: props.detailInfo.id,
        projectId: currentProjectId.value,
        templateId: props.detailInfo.templateId,
        customFields,
      };
      // 执行保存操作
      const res = await createOrUpdateBug({ request: tmpObj, fileList: fileList.value as unknown as File[] });
      if (res) {
        Message.success(t('common.updateSuccess'));
        handleCancel();
        emit('updateSuccess');
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  }

  watchEffect(() => {
    initCurrentDetail(props.detailInfo);
  });
  defineExpose({
    handleSave,
  });
</script>

<style lang="less" scoped>
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    &-title {
      font-weight: 500;
      color: var(--color-text-1);
    }
    &-action {
      position: absolute;
      top: 0;
      right: 6px;
      color: rgb(var(--primary-7));
    }
  }
</style>
