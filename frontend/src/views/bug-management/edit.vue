<template>
  <MsCard
    :special-height="-54"
    no-content-padding
    divider-has-p-x
    has-breadcrumb
    :title="title"
    :loading="loading"
    :is-edit="isEdit"
    @save="saveHandler(false)"
    @save-and-continue="saveHandler(true)"
  >
    <template #headerRight>
      <a-select
        v-if="!isEdit"
        v-model="form.templateId"
        class="w-[240px]"
        :options="templateOption"
        allow-search
        :placeholder="t('bugManagement.edit.defaultSystemTemplate')"
        @change="templateChange"
      />
    </template>
    <a-form ref="formRef" :model="form" layout="vertical">
      <div class="flex flex-row" style="height: calc(100vh - 224px)">
        <div class="left mt-[16px] min-w-[732px] grow pl-[24px]">
          <a-form-item
            field="title"
            :label="t('bugManagement.bugName')"
            :rules="[{ required: true, message: t('bugManagement.edit.nameIsRequired') }]"
            :placeholder="t('bugManagement.edit.pleaseInputBugName')"
          >
            <a-input v-model="form.title" :max-length="255" />
          </a-form-item>
          <a-form-item field="description" :label="t('bugManagement.edit.content')">
            <MsRichText
              v-model:raw="form.description"
              v-model:filed-ids="richTextFileIds"
              :upload-image="handleUploadImage"
            />
          </a-form-item>
          <a-form-item field="attachment" :label="t('bugManagement.edit.file')">
            <div class="flex flex-col">
              <div class="mb-1">
                <a-dropdown position="tr" trigger="hover">
                  <a-button type="outline">
                    <template #icon> <icon-plus class="text-[14px]" /> </template>
                    {{ t('bugManagement.edit.uploadFile') }}
                  </a-button>
                  <template #content>
                    <a-upload
                      ref="uploadRef"
                      v-model:file-list="fileList"
                      :auto-upload="false"
                      :show-file-list="false"
                      :before-upload="beforeUpload"
                      @change="handleChange"
                    >
                      <template #upload-button>
                        <a-button type="text" class="!text-[var(--color-text-1)]">
                          <icon-upload />{{ t('bugManagement.edit.localUpload') }}</a-button
                        >
                      </template>
                    </a-upload>
                    <a-button type="text" class="!text-[var(--color-text-1)]" @click="associatedFile">
                      <MsIcon type="icon-icon_link-copy_outlined" size="16" />{{
                        t('bugManagement.edit.linkFile')
                      }}</a-button
                    >
                  </template>
                </a-dropdown>
              </div>
            </div>
          </a-form-item>
          <div class="mb-[8px] mt-[2px] text-[var(--color-text-4)]">{{ t('bugManagement.edit.fileExtra') }}</div>
          <FileList mode="static" :file-list="fileList">
            <template #actions="{ item }">
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
                <MsButton
                  v-if="item.status !== 'init'"
                  type="button"
                  status="primary"
                  class="!mr-[4px]"
                  @click="transferFile"
                >
                  {{ t('caseManagement.featureCase.storage') }}
                </MsButton>
                <TransferModal
                  v-model:visible="transferVisible"
                  :request-fun="transferFileRequest"
                  :params="{
                    projectId: currentProjectId,
                    bugId: bugId as string,
                    fileId: item.uid,
                    associated: !item.local,
                  }"
                  @success="getDetailInfo()"
                />
                <MsButton
                  v-if="item.status !== 'init'"
                  type="button"
                  status="primary"
                  class="!mr-[4px]"
                  @click="downloadFile(item)"
                >
                  {{ t('common.download') }}
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
                <MsButton v-if="bugId" type="button" status="primary" class="!mr-[4px]" @click="downloadFile(item)">
                  {{ t('common.download') }}
                </MsButton>
                <MsButton
                  v-if="bugId && item.isUpdateFlag"
                  type="button"
                  status="primary"
                  @click="handleUpdateFile(item)"
                >
                  {{ t('common.update') }}
                </MsButton>
              </div>
            </template>
            <template #title="{ item }">
              <span v-if="item.isUpdateFlag" class="ml-4 flex items-center font-normal text-[rgb(var(--warning-6))]"
                ><icon-exclamation-circle-fill /> <span>{{ t('caseManagement.featureCase.fileIsUpdated') }}</span>
              </span>
            </template>
          </FileList>
        </div>
        <a-divider class="ml-[16px]" direction="vertical" />
        <div class="right mt-[16px] max-w-[433px] grow pr-[24px]">
          <MsFormCreate ref="formCreateRef" v-model:formItem="formItem" v-model:api="fApi" :form-rule="formRules" />
          <a-form-item field="tag" :label="t('bugManagement.tag')">
            <MsTagsInput
              v-model:model-value="form.tags"
              :placeholder="t('bugManagement.edit.tagPlaceholder')"
              allow-clear
            />
          </a-form-item>
        </div>
      </div>
    </a-form>
  </MsCard>
  <div>
    <MsUpload
      v-model:file-list="fileList"
      accept="none"
      :auto-upload="false"
      :sub-text="acceptType === 'jar' ? '' : t('project.fileManagement.normalFileSubText', { size: 50 })"
      multiple
      draggable
      size-unit="MB"
      :max-size="50"
      :is-all-screen="true"
      class="mb-[16px]"
      @change="handleChange"
    />
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
  import { useRoute } from 'vue-router';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsFormCreate from '@/components/pure/ms-form-create/ms-form-create.vue';
  import { FormItem, FormRuleItem } from '@/components/pure/ms-form-create/types';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import FileList from '@/components/pure/ms-upload/fileList.vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';
  import { MsFileItem } from '@/components/pure/ms-upload/types';
  import RelateFileDrawer from '@/components/business/ms-link-file/associatedFileDrawer.vue';
  import TransferModal from '@/views/case-management/caseManagementFeature/components/tabContent/transferModal.vue';

  import {
    checkFileIsUpdateRequest,
    createOrUpdateBug,
    downloadFileRequest,
    editorUploadFile,
    getAssociatedFileList,
    getBugDetail,
    getTemplateById,
    getTemplateOption,
    previewFile,
    transferFileRequest,
    updateFile,
  } from '@/api/modules/bug-management';
  import { getModules, getModulesCount } from '@/api/modules/project-management/fileManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useVisit from '@/hooks/useVisit';
  import router from '@/router';
  import { useAppStore } from '@/store';
  import { downloadByteFile } from '@/utils';
  import { scrollIntoView } from '@/utils/dom';

  import { BugEditCustomField, BugEditCustomFieldItem, BugEditFormObject } from '@/models/bug-management';
  import { AssociatedList, AttachFileInfo } from '@/models/caseManagement/featureCase';
  import { TableQueryParams } from '@/models/common';
  import { SelectValue } from '@/models/projectManagement/menuManagement';
  import { BugManagementRouteEnum } from '@/enums/routeEnum';

  import { convertToFileByBug } from './utils';

  defineOptions({ name: 'BugEditPage' });

  const { t } = useI18n();

  interface TemplateOption {
    label: string;
    value: string;
  }

  const appStore = useAppStore();
  const route = useRoute();
  const templateOption = ref<TemplateOption[]>([]);
  const form = ref<BugEditFormObject>({
    projectId: appStore.currentProjectId,
    title: '',
    description: '',
    templateId: '',
    tags: [],
    deleteLocalFileIds: [],
    unLinkRefIds: [],
    linkFileIds: [],
  });

  const getListFunParams = ref<TableQueryParams>({
    combine: {
      hiddenIds: [],
    },
  });
  const formRef = ref();
  const formCreateRef = ref();

  const fileList = ref<MsFileItem[]>([]);
  const formRules = ref<FormItem[]>([]);
  const formItem = ref<FormRuleItem[]>([]);
  const fApi = ref<any>(null);
  const currentProjectId = computed(() => appStore.currentProjectId);
  const associatedDrawer = ref(false);
  const loading = ref(false);
  const acceptType = ref('none'); // 模块-上传文件类型

  const isEdit = computed(() => !!route.query.id && route.params.mode === 'edit');
  const bugId = computed(() => route.query.id || '');
  const isEditOrCopy = computed(() => !!bugId.value);
  const isCopy = computed(() => route.params.mode === 'copy');
  const imageUrl = ref('');
  const previewVisible = ref<boolean>(false);
  const richTextFileIds = ref<string[]>([]);
  const visitedKey = 'doNotNextTipCreateBug';
  const { getIsVisited } = useVisit(visitedKey);

  const title = computed(() => {
    return isEdit.value ? t('bugManagement.editBug') : t('bugManagement.createBug');
  });

  const attachmentsList = ref<AttachFileInfo[]>([]);

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

  // 后台传过来的local文件的item列表
  const oldLocalFileList = computed(() => {
    return attachmentsList.value.filter((item) => item.local).map((item: any) => item.uid);
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

  const transferVisible = ref<boolean>(false);
  // 转存
  function transferFile() {
    transferVisible.value = true;
  }

  // 处理表单格式
  const getFormRules = (arr: BugEditCustomField[]) => {
    formRules.value = [];
    if (Array.isArray(arr) && arr.length) {
      formRules.value = arr.map((item) => {
        return {
          type: item.type,
          name: item.fieldId,
          label: item.fieldName,
          value: item.value,
          options: item.platformOptionJson ? JSON.parse(item.platformOptionJson) : item.options,
          required: item.required as boolean,
          props: {
            modelValue: item.value,
            options: item.platformOptionJson ? JSON.parse(item.platformOptionJson) : item.options,
          },
        };
      });
    }
  };

  const templateChange = async (v: SelectValue) => {
    if (v) {
      try {
        const res = await getTemplateById({ projectId: appStore.currentProjectId, id: v });
        getFormRules(res.customFields);
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    }
  };

  const getTemplateOptions = async () => {
    try {
      const res = await getTemplateOption(appStore.currentProjectId);
      templateOption.value = res.map((item) => {
        if (item.enableDefault && !isEdit.value) {
          // 当创建时 选中默认模板
          form.value.templateId = item.id;
          templateChange(item.id);
        }
        return {
          label: item.name,
          value: item.id,
        };
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

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

  // 更新文件
  async function handleUpdateFile(item: MsFileItem) {
    try {
      await updateFile(currentProjectId.value, item.associationId);
      Message.success(t('common.updateSuccess'));
    } catch (error) {
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

  function associatedFile() {
    associatedDrawer.value = true;
  }

  function handleChange(_fileList: MsFileItem[]) {
    fileList.value = _fileList.map((e) => {
      return {
        ...e,
        enable: true, // 是否启用
        local: true, // 是否本地文件
      };
    });
  }

  // 处理关联文件
  function saveSelectAssociatedFile(fileData: AssociatedList[]) {
    const fileResultList = fileData.map((fileInfo) => convertToFileByBug(fileInfo));
    fileList.value.push(...fileResultList);
  }

  // 保存
  const saveHandler = async (isContinue = false) => {
    formRef.value.validate((error: any) => {
      if (!error) {
        fApi.value.validate(async (valid: any) => {
          if (valid === true) {
            try {
              loading.value = true;
              const customFields: BugEditCustomFieldItem[] = [];
              if (formItem.value && formItem.value.length) {
                formItem.value.forEach((item: FormRuleItem) => {
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
                customFields,
              };
              if (isCopy.value) {
                delete tmpObj.id;
              }
              // 执行保存操作
              const res = await createOrUpdateBug({ request: tmpObj, fileList: fileList.value as unknown as File[] });
              if (isEdit.value) {
                Message.success(t('common.updateSuccess'));
                router.push({
                  name: BugManagementRouteEnum.BUG_MANAGEMENT_INDEX,
                });
              } else {
                Message.success(t('common.createSuccess'));
                if (isContinue) {
                  // 如果是保存并继续创建
                  const { templateId } = form.value;
                  // 用当前模板初始化自定义字段
                  await templateChange(templateId);
                  form.value = {
                    projectId: appStore.currentProjectId, // 取当前项目id
                    title: '',
                    description: '',
                    templateId,
                    tags: [],
                  };
                  // 清空文件列表
                  fileList.value = [];
                } else {
                  // 否则跳转到成功页
                  if (getIsVisited()) {
                    router.push({
                      name: BugManagementRouteEnum.BUG_MANAGEMENT_INDEX,
                    });
                    return;
                  }
                  router.push({
                    name: BugManagementRouteEnum.BUG_MANAGEMENT_CREATE_SUCCESS,
                    query: {
                      ...route.query,
                      id: res.data.id,
                    },
                  });
                }
              }
            } catch (err) {
              // eslint-disable-next-line no-console
              console.log(err);
            } finally {
              loading.value = false;
            }
          }
        });
      }
    });
    scrollIntoView(document.querySelector('.arco-form-item-message'), { block: 'center' });
  };
  // 获取详情
  const getDetailInfo = async () => {
    const id = route.query.id as string;
    if (!id) return;
    const res = await getBugDetail(id);
    const { customFields, templateId, attachments } = res;
    // 根据模板ID 初始化自定义字段
    await templateChange(templateId);
    if (attachments && attachments.length) {
      attachmentsList.value = attachments;
      // 检查文件是否有更新
      const checkUpdateFileIds = await checkFileIsUpdateRequest(attachments.map((item: any) => item.fileId));
      // 处理文件列表
      fileList.value = attachments
        .map((fileInfo: any) => {
          return {
            ...fileInfo,
            name: fileInfo.fileName,
            isUpdateFlag: checkUpdateFileIds.includes(fileInfo.id),
          };
        })
        .map((fileInfo: any) => {
          return convertToFileByBug(fileInfo);
        });
    }

    const tmpObj = {};
    if (customFields && Array.isArray(customFields)) {
      customFields.forEach((item) => {
        tmpObj[item.id] = item.value;
      });
    }
    // 自定义字段赋值
    fApi.value.setValue(tmpObj);
    // 表单赋值
    form.value = {
      id: res.id,
      title: res.title,
      description: res.description,
      templateId: res.templateId,
      tags: res.tags,
      projectId: res.projectId,
    };
  };

  const initDefaultFields = async () => {
    await getTemplateOptions();
  };

  // 监视文件列表处理关联和本地文件
  watch(
    () => fileList.value,
    (val) => {
      if (val) {
        getListFunParams.value.combine.hiddenIds = fileList.value.filter((item) => !item.local).map((item) => item.uid);
        if (isEdit.value) {
          getFilesParams();
        }
      }
    },
    { deep: true }
  );

  async function handleUploadImage(file: File) {
    const { data } = await editorUploadFile({
      fileList: [file],
    });
    return data;
  }

  onMounted(async () => {
    await initDefaultFields();
    if (isEditOrCopy.value) {
      // 详情
      await getDetailInfo();
    }
  });
</script>

<style lang="less" scoped>
  :deep(.arco-form-item-extra) {
    font-size: 14px;
    color: var(--color-text-4);
  }
</style>
