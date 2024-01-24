<!-- eslint-disable vue/no-v-html -->
<template>
  <div class="caseDetailWrapper ml-1">
    <a-form ref="caseFormRef" class="rounded-[4px]" :model="detailForm" layout="vertical">
      <a-form-item
        class="relative"
        field="precondition"
        :label="t('system.orgTemplate.precondition')"
        asterisk-position="end"
      >
        <span class="absolute right-[6px] top-0">
          <a-button v-if="props.allowEdit" type="text" class="px-0" @click="prepositionEdit">
            <MsIcon type="icon-icon_edit_outlined" class="mr-1 font-[16px] text-[rgb(var(--primary-5))]" />{{
              t('caseManagement.featureCase.contentEdit')
            }}</a-button
          ></span
        >
        <MsRichText
          v-if="isEditPreposition"
          v-model:raw="detailForm.prerequisite"
          v-model:filed-ids="prerequisiteFileIds"
          :upload-image="handleUploadImage"
          class="mt-2"
        />
        <div v-else v-dompurify-html="detailForm?.prerequisite || '-'" class="text-[var(--color-text-3)]"></div>
      </a-form-item>
      <a-form-item
        field="step"
        :label="
          detailForm.caseEditType === 'STEP'
            ? t('system.orgTemplate.stepDescription')
            : t('system.orgTemplate.textDescription')
        "
        class="relative"
      >
        <div class="absolute left-16 top-0 font-normal">
          <a-divider direction="vertical" />
          <a-dropdown :popup-max-height="false" @select="handleSelectType">
            <span class="changeType cursor-pointer text-[var(--color-text-3)]"
              >{{ t('system.orgTemplate.changeType') }} <icon-down
            /></span>
            <template #content>
              <a-doption value="STEP" :class="getSelectTypeClass('STEP')">
                {{ t('system.orgTemplate.stepDescription') }}</a-doption
              >
              <a-doption value="TEXT" :class="getSelectTypeClass('TEXT')">{{
                t('system.orgTemplate.textDescription')
              }}</a-doption>
            </template>
          </a-dropdown>
        </div>
        <!-- 步骤描述 -->
        <div v-if="detailForm.caseEditType === 'STEP'" class="w-full">
          <AddStep v-model:step-list="stepData" :is-disabled="!isEditPreposition" />
        </div>
        <!-- 文本描述 -->
        <MsRichText
          v-if="detailForm.caseEditType === 'TEXT' && isEditPreposition"
          v-model:raw="detailForm.textDescription"
          v-model:filed-ids="textDescriptionFileIds"
          :upload-image="handleUploadImage"
        />
        <div v-if="detailForm.caseEditType === 'TEXT' && !isEditPreposition">{{
          detailForm.textDescription || '-'
        }}</div>
      </a-form-item>
      <a-form-item
        v-if="detailForm.caseEditType === 'TEXT'"
        field="remark"
        :label="t('caseManagement.featureCase.expectedResult')"
      >
        <MsRichText
          v-if="detailForm.caseEditType === 'TEXT' && isEditPreposition"
          v-model:raw="detailForm.expectedResult"
          v-model:filed-ids="expectedResultFileIds"
          :upload-image="handleUploadImage"
        />
        <div v-else class="text-[var(--color-text-3)]" v-html="detailForm.description || '-'"></div>
      </a-form-item>
      <a-form-item field="description" :label="t('caseManagement.featureCase.remark')">
        <MsRichText
          v-if="isEditPreposition"
          v-model:filed-ids="descriptionFileIds"
          v-model:raw="detailForm.description"
          :upload-image="handleUploadImage"
        />
        <div v-else v-dompurify-html="detailForm.description || '-'" class="text-[var(--color-text-3)]"></div>
      </a-form-item>
      <div v-if="isEditPreposition" class="flex justify-end">
        <a-button type="secondary" @click="handleCancel">{{ t('common.cancel') }}</a-button>
        <a-button class="ml-[12px]" type="primary" :loading="confirmLoading" @click="handleOK">
          {{ t('common.save') }}
        </a-button></div
      >
      <a-form-item
        field="attachment"
        :label="props.allowEdit ? t('caseManagement.featureCase.attachment') : '附件列表'"
      >
        <div v-if="props.allowEdit" class="flex flex-col">
          <div class="mb-1">
            <a-dropdown position="tr" trigger="hover">
              <a-button type="outline">
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
                      <icon-upload />{{ t('caseManagement.featureCase.uploadFile') }}</a-button
                    >
                  </template>
                </a-upload>
                <a-button type="text" class="!text-[var(--color-text-1)]" @click="associatedFile">
                  <MsIcon type="icon-icon_link-copy_outlined" size="16" />{{
                    t('caseManagement.featureCase.associatedFile')
                  }}</a-button
                >
              </template>
            </a-dropdown>
          </div>
          <div class="!hover:bg-[rgb(var(--primary-1))] !text-[var(--color-text-4)]">{{
            t('system.orgTemplate.addAttachmentTip')
          }}</div>
        </div>
      </a-form-item>
    </a-form>
    <!-- 文件列表开始 -->
    <div class="w-[90%]">
      <MsFileList
        ref="fileListRef"
        v-model:file-list="fileList"
        :show-tab="false"
        :request-params="{
          caseId: detailForm.id,
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
                  caseId: detailForm.id,
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
    <LinkFileDrawer
      v-model:visible="showDrawer"
      :get-tree-request="getModules"
      :get-count-request="getModulesCount"
      :get-list-request="getAssociatedFileListUrl"
      :get-list-fun-params="getListFunParams"
      @save="saveSelectAssociatedFile"
    />
  </div>
  <a-image-preview v-model:visible="previewVisible" :src="imageUrl" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { FormInstance, Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import type { FormRuleItem } from '@/components/pure/ms-form-create/types';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import MsFileList from '@/components/pure/ms-upload/fileList.vue';
  import type { MsFileItem } from '@/components/pure/ms-upload/types';
  import LinkFileDrawer from '@/components/business/ms-link-file/associatedFileDrawer.vue';
  import AddStep from '../addStep.vue';
  import TransferModal from './transferModal.vue';

  import {
    checkFileIsUpdateRequest,
    deleteFileOrCancelAssociation,
    downloadFileRequest,
    editorUploadFile,
    getAssociatedFileListUrl,
    previewFile,
    transferFileRequest,
    updateCaseRequest,
    updateFile,
    uploadOrAssociationFile,
  } from '@/api/modules/case-management/featureCase';
  import { getModules, getModulesCount } from '@/api/modules/project-management/fileManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { downloadByteFile, getGenerateId } from '@/utils';
  import { scrollIntoView } from '@/utils/dom';

  import type { AssociatedList, DetailCase, StepList } from '@/models/caseManagement/featureCase';
  import type { TableQueryParams } from '@/models/common';

  import { convertToFile } from '../utils';

  const caseFormRef = ref<FormInstance>();

  const appStore = useAppStore();
  const currentProjectId = computed(() => appStore.currentProjectId);

  const { t } = useI18n();

  const props = withDefaults(
    defineProps<{
      form: DetailCase;
      allowEdit?: boolean; // 是否允许编辑
      formRules?: FormRuleItem[]; // 编辑表单
    }>(),
    {
      allowEdit: true, // 是否允许编辑
    }
  );

  const emit = defineEmits<{
    (e: 'updateSuccess'): void;
  }>();

  const detailForm = ref<Record<string, any>>({
    projectId: currentProjectId.value,
    templateId: '',
    name: '',
    prerequisite: '',
    caseEditType: 'STEP',
    steps: '',
    textDescription: '',
    expectedResult: '',
    description: '',
    publicCase: false,
    moduleId: '',
    versionId: '',
    tags: [],
    customFields: [],
    relateFileMetaIds: [],
  });

  // 步骤描述
  const stepData = ref<StepList[]>([
    {
      id: getGenerateId(),
      step: '',
      expected: '',
      showStep: false,
      showExpected: false,
    },
  ]);

  const isEditPreposition = ref<boolean>(false); // 非编辑状态

  // 更改类型
  const handleSelectType = (value: string | number | Record<string, any> | undefined) => {
    detailForm.value.caseEditType = value as string;
  };

  // 获取类型样式
  function getSelectTypeClass(type: string) {
    return detailForm.value.caseEditType === type
      ? ['bg-[rgb(var(--primary-1))]', '!text-[rgb(var(--primary-5))]']
      : [];
  }

  // 编辑前置条件
  function prepositionEdit() {
    isEditPreposition.value = !isEditPreposition.value;
  }

  const fileList = ref<MsFileItem[]>([]);
  const acceptType = ref('none'); // 模块-上传文件类型
  const getListFunParams = ref<TableQueryParams>({
    combine: {
      hiddenIds: [],
    },
  });

  const showDrawer = ref<boolean>(false);
  function associatedFile() {
    showDrawer.value = true;
  }

  const attachmentsList = ref([]);

  // 后台传过来的local文件的item列表
  const oldLocalFileList = computed(() => {
    return attachmentsList.value.filter((item: any) => item.local);
  });

  // 后台已保存本地文件
  const currentOldLocalFileList = computed(() => {
    return fileList.value.filter((item) => item.local && item.status !== 'init').map((item: any) => item.uid);
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

  // 新增关联文件ID列表
  const newAssociateFileListIds = computed(() => {
    return fileList.value
      .filter((item: any) => !item.local && !associateFileIds.value.includes(item.uid))
      .map((item: any) => item.uid);
  });

  // 删除本地上传的文件id
  const deleteFileMetaIds = computed(() => {
    return oldLocalFileList.value
      .filter((item: any) => !currentOldLocalFileList.value.includes(item.id))
      .map((item: any) => item.id);
  });

  // 取消关联文件id
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

  // 前置条件附件id
  const prerequisiteFileIds = ref<string[]>([]);
  // 文本描述附件id
  const textDescriptionFileIds = ref<string[]>([]);
  // 预期结果附件id
  const expectedResultFileIds = ref<string[]>([]);
  // 描述附件id
  const descriptionFileIds = ref<string[]>([]);

  // 所有附近文件id
  const allAttachmentsFileIds = computed(() => {
    return [
      ...prerequisiteFileIds.value,
      ...textDescriptionFileIds.value,
      ...expectedResultFileIds.value,
      ...descriptionFileIds.value,
    ];
  });

  // 处理编辑详情参数
  function getParams() {
    const steps = stepData.value.map((item, index) => {
      return {
        num: index,
        desc: item.step,
        result: item.expected,
      };
    });

    const customFieldsArr = props.formRules?.map((item: any) => {
      return {
        fieldId: item.field,
        value: Array.isArray(item.value) ? JSON.stringify(item.value) : item.value,
      };
    });

    return {
      request: {
        ...detailForm.value,
        steps: JSON.stringify(steps),
        deleteFileMetaIds: deleteFileMetaIds.value,
        unLinkFilesIds: unLinkFilesIds.value,
        newAssociateFileListIds: newAssociateFileListIds.value,
        customFields: customFieldsArr,
        caseDetailFileIds: allAttachmentsFileIds.value,
      },
      fileList: fileList.value.filter((item: any) => item.status === 'init'), // 总文件列表
    };
  }

  const confirmLoading = ref<boolean>(false);

  function handleOK() {
    caseFormRef.value?.validate().then(async (res: any) => {
      if (!res) {
        try {
          confirmLoading.value = true;
          await updateCaseRequest(getParams());
          Message.success(t('caseManagement.featureCase.editSuccess'));
          isEditPreposition.value = false;
          emit('updateSuccess');
        } catch (error) {
          console.log(error);
        } finally {
          confirmLoading.value = false;
        }
      }
      return scrollIntoView(document.querySelector('.arco-form-item-message'), { block: 'center' });
    });
  }

  function handleCancel() {
    isEditPreposition.value = false;
  }

  const fileListRef = ref<InstanceType<typeof MsFileList>>();
  async function startUpload() {
    await fileListRef.value?.startUpload();
    emit('updateSuccess');
  }

  function beforeUpload(file: File) {
    const _maxSize = 50 * 1024 * 1024;
    if (file.size > _maxSize) {
      Message.warning(t('ms.upload.overSize'));
      return Promise.resolve(false);
    }
    return Promise.resolve(true);
  }

  // 删除本地文件
  async function deleteFileHandler(item: MsFileItem) {
    try {
      const params = {
        id: item.uid,
        local: item.local,
        caseId: detailForm.value.id,
        projectId: currentProjectId.value,
      };
      await deleteFileOrCancelAssociation(params);
      Message.success(
        item.local ? t('caseManagement.featureCase.deleteSuccess') : t('caseManagement.featureCase.cancelLinkSuccess')
      );
      emit('updateSuccess');
    } catch (error) {
      console.log(error);
    }
  }
  const transferVisible = ref<boolean>(false);

  // 下载文件
  async function downloadFile(item: MsFileItem) {
    try {
      const prams = {
        projectId: currentProjectId.value,
        caseId: detailForm.value.id,
        fileId: item.uid,
        local: true,
      };
      const res = await downloadFileRequest(prams);
      downloadByteFile(res, `${item.name}`);
    } catch (error) {
      console.log(error);
    }
  }
  const checkUpdateFileIds = ref<string[]>([]);

  // 检测更新文件
  async function getCheckFileIds(fileIds: string[]) {
    try {
      checkUpdateFileIds.value = await checkFileIsUpdateRequest(fileIds);
    } catch (error) {
      console.log(error);
    }
  }

  // 获取详情
  async function getDetails() {
    const { steps, attachments } = detailForm.value;
    if (steps) {
      stepData.value = JSON.parse(steps).map((item: any) => {
        return {
          step: item.desc,
          expected: item.result,
        };
      });
    }
    const fileIds = (attachments || []).map((item: any) => item.id);
    if (fileIds.length) {
      await getCheckFileIds(fileIds);
    }

    attachmentsList.value = attachments || [];
    // 处理文件列表
    fileList.value = (attachments || [])
      .map((fileInfo: any) => {
        return {
          ...fileInfo,
          name: fileInfo.fileName,
          isUpdateFlag: checkUpdateFileIds.value.includes(fileInfo.id),
        };
      })
      .map((fileInfo: any) => {
        return convertToFile(fileInfo);
      });
  }

  const imageUrl = ref('');
  const previewVisible = ref<boolean>(false);
  // 预览
  async function handlePreview(item: MsFileItem) {
    try {
      previewVisible.value = true;
      const res = await previewFile({
        projectId: currentProjectId.value,
        caseId: detailForm.value.id,
        fileId: item.uid,
        local: item.local,
      });
      const blob = new Blob([res], { type: 'image/jpeg' });
      imageUrl.value = URL.createObjectURL(blob);
    } catch (error) {
      console.log(error);
    }
  }

  watch(
    () => props.form,
    (val) => {
      detailForm.value = { ...val };
      getDetails();
    },
    {
      deep: true,
    }
  );

  // 文件列表单个上传
  watch(
    () => fileList.value,
    (val) => {
      if (val) {
        if (val.filter((item) => item.status === 'init').length) {
          setTimeout(() => {
            startUpload();
          }, 30);
        }
      }
    }
  );

  // 处理关联文件
  function saveSelectAssociatedFile(fileData: AssociatedList[]) {
    const fileResultList = fileData.map((fileInfo) => convertToFile(fileInfo));
    fileList.value.push(...fileResultList);
  }

  // 更新文件
  async function handleUpdateFile(item: MsFileItem) {
    try {
      await updateFile(currentProjectId.value, item.associationId);
      Message.success(t('common.updateSuccess'));
    } catch (error) {
      console.log(error);
    }
  }

  async function handleUploadImage(file: File) {
    const { data } = await editorUploadFile({
      fileList: [file],
    });
    return data;
  }

  onMounted(() => {
    detailForm.value = { ...props.form };
    getDetails();
  });

  defineExpose({
    handleOK,
    getParams,
  });
</script>

<style scoped lang="less">
  :deep(.arco-form-item-label) {
    font-weight: bold !important;
  }
</style>
