<template>
  <MsCard
    :special-height="-54"
    no-content-padding
    divider-has-p-x
    has-breadcrumb
    :title="title"
    :loading="loading"
    @save="saveHandler"
    @save-and-continue="saveHandler"
  >
    <template #headerRight>
      <a-select
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
            <a-input v-model="form.title" :max-length="255" show-word-limit />
          </a-form-item>
          <a-form-item field="description" :label="t('bugManagement.edit.content')">
            <MsRichText v-model="form.description" />
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
          <FileList
            :show-tab="false"
            :file-list="fileList"
            :upload-func="uploadFile"
            @delete-file="deleteFile"
            @reupload="reupload"
            @handle-preview="handlePreview"
          >
          </FileList>
        </div>
        <a-divider class="ml-[16px]" direction="vertical" />
        <div class="right mt-[16px] max-w-[433px] grow pr-[24px]">
          <!-- <a-form-item
            :label="t('bugManagement.handleMan')"
            field="handleMan"
            :rules="[{ required: true, message: t('bugManagement.edit.handleManIsRequired') }]"
          >
            <MsUserSelector
              v-model:model-value="form.handleMan"
              :type="UserRequestTypeEnum.PROJECT_PERMISSION_MEMBER"
              :load-option-params="{ projectId: appStore.currentProjectId }"
              placeholder="bugManagement.edit.handleManPlaceholder"
            />
          </a-form-item> -->
          <MsFormCreate
            v-if="formRules.length"
            ref="formCreateRef"
            v-model:formItem="formItem"
            v-model:api="fApi"
            :form-rule="formRules"
          />
          <a-form-item field="tag" :label="t('bugManagement.tag')">
            <a-input-tag
              v-model:model-value="form.tag"
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
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router';
  import { FileItem, Message } from '@arco-design/web-vue';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsFormCreate from '@/components/pure/ms-form-create/ms-form-create.vue';
  import { FormItem, FormRuleItem } from '@/components/pure/ms-form-create/types';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import FileList from '@/components/pure/ms-upload/fileList.vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';
  import { MsFileItem } from '@/components/pure/ms-upload/types';
  import RelateFileDrawer from '@/components/business/ms-link-file/associatedFileDrawer.vue';

  // import { MsUserSelector } from '@/components/business/ms-user-selector';
  // import { UserRequestTypeEnum } from '@/components/business/ms-user-selector/utils';
  import {
    createBug,
    getAssociatedFileList,
    getBugDetail,
    getTemplageOption,
    getTemplateById,
  } from '@/api/modules/bug-management';
  import { getModules, getModulesCount } from '@/api/modules/project-management/fileManagement';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { scrollIntoView } from '@/utils/dom';

  import { BugEditCustomField, BugEditCustomFieldItem, BugEditFormObject } from '@/models/bug-management';
  import { AssociatedList, AttachFileInfo } from '@/models/caseManagement/featureCase';
  import { TableQueryParams } from '@/models/common';
  import { SelectValue } from '@/models/projectManagement/menuManagement';

  import { convertToFile } from '../case-management/caseManagementFeature/components/utils';

  const { t } = useI18n();

  interface TemplateOption {
    label: string;
    value: string;
  }

  const appStore = useAppStore();
  // const formCreateStore = useFormCreateStore();

  const route = useRoute();
  const templateOption = ref<TemplateOption[]>([]);
  const form = ref<BugEditFormObject>({
    projectId: appStore.currentProjectId,
    title: '',
    description: '',
    templateId: '',
    tag: [],
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
  const fApi = ref({});
  const associatedDrawer = ref(false);
  const loading = ref(false);
  const acceptType = ref('none'); // 模块-上传文件类型

  const isEdit = computed(() => !!route.query.id);

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

  // 删除本地上传的文件id
  const deleteFileMetaIds = computed(() => {
    return oldLocalFileList.value
      .filter((item: any) => !currentOldLocalFileList.value.includes(item.id))
      .map((item: any) => item.id);
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
          options: item.platformOptionJson ? JSON.parse(item.platformOptionJson) : [],
          required: item.required as boolean,
          props: {
            modelValue: item.value,
            options: item.platformOptionJson ? JSON.parse(item.platformOptionJson) : [],
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
      const res = await getTemplageOption({ projectId: appStore.currentProjectId });
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

  const handlePreview = (item: FileItem) => {
    const { url } = item;
    window.open(url);
  };

  const deleteFile = (item: FileItem) => {
    fileList.value = fileList.value.filter((e) => e.uid !== item.uid);
  };

  const reupload = (item: FileItem) => {
    fileList.value = fileList.value.map((e) => {
      if (e.uid === item.uid) {
        return {
          ...e,
          status: 'init',
        };
      }
      return e;
    });
  };

  const uploadFile = (file: File) => {
    const fileItem: FileItem = {
      uid: `${Date.now()}`,
      name: file.name,
      status: 'init',
      file,
    };
    fileList.value.push(fileItem);
    return Promise.resolve(fileItem);
  };

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
    const fileResultList = fileData.map((fileInfo) => convertToFile(fileInfo));
    fileList.value.push(...fileResultList);
  }

  // 保存
  const saveHandler = async () => {
    formRef.value.validate((error: any) => {
      if (!error) {
        formCreateRef.value.formApi.validate(async (valid: any) => {
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
              const tmpObj = {
                ...form.value,
                tag: form.value.tag.join(',') || '',
                customFields,
              };
              await createBug({ request: tmpObj, fileList: fileList.value as unknown as File[] });
              Message.success(t('common.createSuccess'));
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

  const getDetailInfo = async () => {
    const id = route.query.id as string;
    // TODO: 等后端接口
    const res = await getBugDetail(id);
    const { customFields, file } = res;
    formRules.value = customFields;
    fileList.value = file;
  };

  const initDefaultFields = () => {
    getTemplateOptions();
  };

  // 处理关联文件和已关联文件本地文件和已上传文本文件
  function getFilesParams() {
    form.value.deleteFileMetaIds = deleteFileMetaIds.value;
    form.value.unLinkFilesIds = unLinkFilesIds.value;
    form.value.relateFileMetaIds = newAssociateFileListIds.value;
  }

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

  onBeforeMount(() => {
    if (isEdit.value) {
      // 详情
      getDetailInfo();
    } else {
      initDefaultFields();
    }
  });
</script>

<style lang="less" scoped>
  :deep(.arco-form-item-extra) {
    font-size: 14px;
    color: var(--color-text-4);
  }
</style>
