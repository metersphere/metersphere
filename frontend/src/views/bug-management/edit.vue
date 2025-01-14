<template>
  <a-form ref="formRef" class="h-full" :model="form" layout="vertical">
    <div class="flex h-full">
      <div :class="`${props.isDrawer ? 'w-[calc(100%-332px)]' : 'w-[calc(100%-428px)]'} left grow`">
        <!-- 平台默认模板不展示缺陷名称, 描述 -->
        <a-form-item
          v-if="!isPlatformDefaultTemplate"
          field="title"
          :label="t('bugManagement.bugName')"
          :rules="[{ required: true, message: t('bugManagement.edit.nameIsRequired') }]"
        >
          <a-input v-model="form.title" :placeholder="t('bugManagement.edit.pleaseInputBugName')" :max-length="255" />
        </a-form-item>
        <a-form-item v-if="!isPlatformDefaultTemplate" field="description" :label="t('bugManagement.edit.content')">
          <MsRichText
            v-model:raw="form.description"
            v-model:filed-ids="descriptionFileIds"
            :upload-image="handleUploadImage"
            :preview-url="`${EditorPreviewFileUrl}/${appStore.currentProjectId}`"
          />
        </a-form-item>
        <!-- 平台默认模板展示字段, 暂时支持输入框, 富文本类型 -->
        <div v-if="isPlatformDefaultTemplate">
          <a-form-item
            v-for="(value, key) in form.platformSystemFields"
            :key="key"
            :field="'platformSystemFields.' + key"
            :label="platformSystemFieldMap[key].fieldName"
            :rules="[
              {
                required: platformSystemFieldMap[key].required,
                message: `${platformSystemFieldMap[key].fieldName}` + t('bugManagement.edit.cannotBeNull'),
              },
            ]"
          >
            <a-input
              v-if="platformSystemFieldMap[key].type === 'INPUT'"
              v-model="form.platformSystemFields[key]"
              :max-length="255"
            />
            <MsRichText
              v-if="platformSystemFieldMap[key].type === 'RICH_TEXT'"
              v-model:raw="form.platformSystemFields[key]"
              v-model:filed-ids="descriptionFileIdMap[key]"
              :upload-image="handleUploadImage"
              :preview-url="`${EditorPreviewFileUrl}/${appStore.currentProjectId}`"
            />
          </a-form-item>
        </div>
        <a-form-item field="attachment">
          <div class="flex flex-col">
            <div class="mb-1">
              <AddAttachment v-model:file-list="fileList" @change="handleChange" @link-file="associatedFile" />
            </div>
          </div>
        </a-form-item>
        <MsFileList
          ref="fileListRef"
          v-model:file-list="fileList"
          :init-file-save-tips="t('ms.upload.waiting_save')"
          mode="static"
          :show-delete="false"
          :file-name-max-width="props.fileNameMaxWidth"
        >
          <template #actions="{ item }">
            <!-- 本地文件 -->
            <div v-if="item.local || item.status === 'init'" class="flex items-center font-normal">
              <MsButton
                v-if="item.status !== 'init' && item.file.type.includes('image')"
                type="button"
                status="primary"
                class="!mx-0"
                @click="handlePreview(item)"
              >
                {{ t('ms.upload.preview') }}
              </MsButton>
              <a-divider v-if="item.status !== 'init' && item.file.type.includes('image')" direction="vertical" />
              <MsButton
                v-if="item.status !== 'init'"
                type="button"
                status="primary"
                class="!mx-0"
                @click="transferFile(item)"
              >
                {{ t('caseManagement.featureCase.storage') }}
              </MsButton>
              <SaveAsFilePopover
                v-model:visible="transferVisible"
                :saving-file="activeTransferFileParams"
                :file-save-as-source-id="(form.id as string)"
                :file-save-as-api="transferFileRequest"
                :file-module-options-api="getTransferFileTree"
                source-id-key="bugId"
                @finish="getDetailInfo()"
              />
              <a-divider v-if="item.status !== 'init'" direction="vertical" />
              <MsButton
                v-if="item.status !== 'init'"
                type="button"
                status="primary"
                class="!mx-0"
                @click="downloadFile(item)"
              >
                {{ t('common.download') }}
              </MsButton>
              <a-divider v-if="item.status !== 'init'" direction="vertical" />
              <MsButton
                v-if="item.status !== 'uploading'"
                type="button"
                :status="item.deleteContent ? 'primary' : 'danger'"
                class="!mx-0"
                @click="deleteFile(item)"
              >
                {{ t(item.deleteContent) || t('ms.upload.delete') }}
              </MsButton>
            </div>
            <!-- 关联文件 -->
            <div v-else class="flex items-center font-normal">
              <MsButton
                v-if="item.status !== 'init' && item.file.type.includes('image')"
                type="button"
                status="primary"
                class="!mx-0"
                @click="handlePreview(item)"
              >
                {{ t('ms.upload.preview') }}
              </MsButton>
              <a-divider v-if="item.status !== 'init' && item.file.type.includes('image')" direction="vertical" />
              <MsButton v-if="bugId" type="button" status="primary" class="!mx-0" @click="downloadFile(item)">
                {{ t('common.download') }}
              </MsButton>
              <a-divider v-if="bugId" direction="vertical" />
              <MsButton
                v-if="bugId && item.isUpdateFlag"
                type="button"
                class="!mx-0"
                status="primary"
                @click="handleUpdateFile(item)"
              >
                {{ t('common.update') }}
              </MsButton>
              <a-divider v-if="bugId && item.isUpdateFlag" direction="vertical" />
              <MsButton
                v-if="item.status !== 'uploading'"
                type="button"
                :status="item.deleteContent ? 'primary' : 'danger'"
                class="!mx-0"
                @click="deleteFile(item)"
              >
                {{ t(item.deleteContent) }}
              </MsButton>
            </div>
          </template>
          <template #title="{ item }">
            <span v-if="item.isUpdateFlag" class="ml-4 flex items-center font-normal text-[rgb(var(--warning-6))]">
              <icon-exclamation-circle-fill /> <span>{{ t('caseManagement.featureCase.fileIsUpdated') }}</span>
            </span>
          </template>
        </MsFileList>
      </div>
      <a-divider class="ml-[16px]" direction="vertical" />
      <div :class="`${props.isDrawer ? 'w-[332px]' : 'w-[428px]'} right grow`">
        <div :class="`min-w-[250px] overflow-x-hidden ${props.isDrawer ? 'max-w-[316px]' : 'max-w-[412px]'}`">
          <a-skeleton v-if="isLoading" :loading="isLoading" :animation="true">
            <a-space direction="vertical" class="w-full" size="large">
              <a-skeleton-line :rows="12" :line-height="30" :line-spacing="30" />
            </a-space>
          </a-skeleton>
          <a-form v-else :model="form" layout="vertical">
            <div style="display: inline-block; width: 100%; word-wrap: break-word">
              <MsFormCreate ref="formCreateRef" v-model:formItem="formItem" v-model:api="fApi" :form-rule="formRules" />
            </div>

            <a-form-item v-if="!isPlatformDefaultTemplate" field="tag" :label="t('bugManagement.tag')">
              <MsTagsInput
                v-model:model-value="form.tags"
                :placeholder="t('bugManagement.edit.tagPlaceholder')"
                allow-clear
              />
            </a-form-item>
          </a-form>
        </div>
      </div>
    </div>
  </a-form>
  <div>
    <MsUpload
      v-model:file-list="fileList"
      accept="none"
      :auto-upload="false"
      :sub-text="
        acceptType === 'jar' ? '' : t('project.fileManagement.normalFileSubText', { size: appStore.getFileMaxSize })
      "
      multiple
      draggable
      size-unit="MB"
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
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsFormCreate from '@/components/pure/ms-form-create/ms-form-create.vue';
  import { FormItem, FormRuleItem } from '@/components/pure/ms-form-create/types';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import MsFileList from '@/components/pure/ms-upload/fileList.vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';
  import { MsFileItem } from '@/components/pure/ms-upload/types';
  import AddAttachment from '@/components/business/ms-add-attachment/index.vue';
  import SaveAsFilePopover from '@/components/business/ms-add-attachment/saveAsFilePopover.vue';
  import RelateFileDrawer from '@/components/business/ms-link-file/associatedFileDrawer.vue';

  import {
    checkFileIsUpdateRequest,
    downloadFileRequest,
    editorUploadFile,
    getAssociatedFileList,
    getBugDetail,
    getTemplateById,
    previewFile,
    transferFileRequest,
    updateFile,
  } from '@/api/modules/bug-management';
  import { getTransferFileTree } from '@/api/modules/case-management/featureCase';
  import { getModules, getModulesCount } from '@/api/modules/project-management/fileManagement';
  import { EditorPreviewFileUrl } from '@/api/requrls/bug-management';
  import { useI18n } from '@/hooks/useI18n';
  import useShortcutSave from '@/hooks/useShortcutSave';
  import { useAppStore } from '@/store';
  import { downloadByteFile } from '@/utils';
  import { scrollIntoView } from '@/utils/dom';
  import { findParents, Option } from '@/utils/recursion';

  import type { CustomFieldItem } from '@/models/bug-management';
  import {
    BugEditCustomField,
    BugEditCustomFieldItem,
    BugEditFormObject,
    BugTemplateRequest,
  } from '@/models/bug-management';
  import { AssociatedList, AttachFileInfo } from '@/models/caseManagement/featureCase';
  import { TableQueryParams } from '@/models/common';
  import { SelectValue } from '@/models/projectManagement/menuManagement';
  import type { CustomField, DetailCustomField, FieldOptions } from '@/models/setting/template';
  import { CaseLinkEnum } from '@/enums/caseEnum';

  import { convertToFile } from '../case-management/caseManagementFeature/components/utils';
  import { convertToFileByBug, getCurrentText, getDefaultMemberValue } from './utils';
  import { getCaseTemplateContent } from '@/views/case-management/components/addDefectDrawer/utils';

  const props = defineProps<{
    templateId: string; // 缺陷模板id
    isCopyBug?: boolean; // 是否复制
    bugId?: string; // 缺陷id，不传递为创建
    isDrawer?: boolean; // 是否是弹窗模式
    caseType?: CaseLinkEnum; // 用例类型
    fileNameMaxWidth?: string; // 文件名称最大宽度
    fillConfig?: {
      isQuickFillContent: boolean; // 是否快速填充内容
      detailId: string; // 快填详情id
      name: string; // 用例明细名称
    };
  }>();

  const emit = defineEmits<{
    (e: 'saveParams', isContinue: boolean, params: { request: BugEditFormObject; fileList: File[] }): void;
  }>();

  const innerTemplateId = defineModel<string>('templateId', {
    required: true,
  });

  defineOptions({ name: 'BugEditPage' });

  const { t } = useI18n();

  const appStore = useAppStore();
  const form = ref<BugEditFormObject>({
    projectId: appStore.currentProjectId,
    title: '',
    description: '',
    templateId: '',
    tags: [],
    deleteLocalFileIds: [],
    unLinkRefIds: [],
    linkFileIds: [],
    // 平台默认模板系统字段
    platformSystemFields: {},
  });

  // 平台默认模板系统字段
  const platformSystemFieldMap: Record<string, any> = {};

  const getListFunParams = ref<TableQueryParams>({
    combine: {
      hiddenIds: [],
    },
  });
  const formRef = ref();
  const formCreateRef = ref();
  // 保存后端传进来的文件(初始状态)
  const attachmentsList = ref<AttachFileInfo[]>([]);
  // 前端展示的文件(当前状态)
  const fileList = ref<MsFileItem[]>([]);
  const formRules = ref<FormItem[]>([]);
  const formItem = ref<FormRuleItem[]>([]);
  const fApi = ref<any>(null);
  const currentProjectId = computed(() => appStore.currentProjectId);
  const associatedDrawer = ref(false);
  const loading = ref(false);
  const acceptType = ref('none'); // 模块-上传文件类型
  const isEdit = computed(() => props.bugId && !props.isCopyBug);
  const bugId = ref<string | undefined>(props.bugId);
  const isEditOrCopy = computed(() => !!bugId.value);
  const isPlatformDefaultTemplate = ref(false);
  const imageUrl = ref('');
  const previewVisible = ref<boolean>(false);
  // 内容/富文本临时附件ID
  const descriptionFileIds = ref<string[]>([]);
  // 描述-环境/富文本临时附件ID
  const descriptionFileIdMap = ref<Record<string, string[]>>({});

  const isLoading = ref<boolean>(false);
  const rowLength = ref<number>(0);

  // 处理文件参数
  function getFilesParams() {
    // link file
    const associateFileIds = attachmentsList.value.filter((item) => !item.local).map((item) => item.fileId);
    form.value.linkFileIds = fileList.value
      .filter((item) => !item.local && !item.isCopyFlag && !associateFileIds.includes(item.uid))
      .map((item) => item.uid);
    // unlink file
    const remainLinkFileIds = fileList.value.filter((item) => !item.local && !item.isCopyFlag).map((item) => item.uid);
    form.value.unLinkRefIds = attachmentsList.value
      .filter((item) => !item.local && !remainLinkFileIds.includes(item.fileId))
      .map((item) => item.refId);
    // delete local file
    const remainLocalFileIds = fileList.value
      .filter((item) => item.local && !item.isCopyFlag && item.status !== 'init')
      .map((item) => item.uid);
    form.value.deleteLocalFileIds = attachmentsList.value
      .filter((item) => item.local && !remainLocalFileIds.includes(item.fileId))
      .map((item) => item.fileId);
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
  const activeTransferFileParams = ref<MsFileItem>();
  // 转存
  function transferFile(item: MsFileItem) {
    activeTransferFileParams.value = {
      ...item,
    };
    transferVisible.value = true;
  }

  // 获取模板初始值
  function getInitValue(item: DetailCustomField, initOptions: FieldOptions[]) {
    const memberType = ['MEMBER', 'MULTIPLE_MEMBER'];
    const multipleType = ['MULTIPLE_SELECT', 'CHECKBOX'];
    const numberType = ['INT', 'FLOAT'];
    if (isEditOrCopy.value) return null;

    const initValue = item.defaultValue;
    // 成员类型
    if (memberType.includes(item.type)) {
      return getDefaultMemberValue(item, initOptions);
    }
    // 多选类型
    if (multipleType.includes(item.type)) {
      if (Array.isArray(item.defaultValue) && item.defaultValue.length > 0) {
        return item.defaultValue;
      }
      if (typeof item.defaultValue === 'string') {
        return JSON.parse(item.defaultValue || '[]');
      }
    }
    // 数字和浮点格式
    if (numberType.includes(item.type)) {
      return Number(initValue);
    }

    return initValue;
  }

  // 处理表单格式
  const getFormRules = (arr: BugEditCustomField[]) => {
    formRules.value = [];
    if (Array.isArray(arr) && arr.length) {
      formRules.value = arr.map((item: any) => {
        const initOptions = item.options || JSON.parse(item.platformOptionJson || '[]');
        const initValue = getInitValue(item, initOptions);
        return {
          type: item.type,
          name: item.fieldId,
          label: item.fieldName,
          value: initValue,
          options: initOptions,
          required: item.required as boolean,
          platformPlaceHolder: item.platformPlaceHolder,
          props: {
            modelValue: initValue,
            options: initOptions,
          },
        };
      });
    }
  };

  // 设置填充内容
  async function setFillContent() {
    if (props.fillConfig) {
      const { isQuickFillContent, detailId, name } = props.fillConfig;
      if (isQuickFillContent && props.caseType) {
        const fillContent = await getCaseTemplateContent(props.caseType, detailId);
        form.value.description = fillContent || '';
        form.value.title = name;
      }
    }
  }
  const currentCustomFields = ref<CustomFieldItem[]>([]);
  // TODO:: Record<string, any>
  const templateChange = async (v: SelectValue, request?: BugTemplateRequest) => {
    if (v) {
      try {
        isLoading.value = true;
        let param = { projectId: appStore.currentProjectId, id: v };
        if (request) {
          param = { ...param, ...request };
        }
        const res = await getTemplateById(param);
        currentCustomFields.value = res.customFields || [];
        await getFormRules(res.customFields);
        isLoading.value = false;
        isPlatformDefaultTemplate.value = res.platformDefault;
        if (isPlatformDefaultTemplate.value) {
          const systemFields = res.customFields.filter((field: Record<string, any>) => field.platformSystemField);
          systemFields.forEach((field: Record<string, any>) => {
            form.value.platformSystemFields[field.fieldId] = field.defaultValue;
            platformSystemFieldMap[field.fieldId] = field;
          });
        }
        getFormRules(res.customFields.filter((field: Record<string, any>) => !field.platformSystemField));
        // 回显默认系统模板字段
        const systemFieldsDetail = res.systemFields || [];
        systemFieldsDetail.forEach((item: CustomField) => {
          form.value[item.fieldId] = item.defaultValue;
        });

        setFillContent();
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      } finally {
        loading.value = false;
      }
    }
  };

  // 预览图片
  async function handlePreview(item: MsFileItem) {
    try {
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
      previewVisible.value = true;
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
      const params = {
        refId: item.associateId,
        associated: !item.local,
        bugId: bugId.value,
        projectId: currentProjectId.value,
      };
      await updateFile(params);
      Message.success(t('common.updateSuccess'));
    } catch (error) {
      console.log(error);
    }
  }

  function associatedFile() {
    associatedDrawer.value = true;
  }

  function handleChange(_fileList: MsFileItem[]) {
    fileList.value = _fileList.map((e) => {
      return {
        ...e,
        enable: true, // 是否启用
      };
    });
  }

  // 处理关联文件
  function saveSelectAssociatedFile(fileData: AssociatedList[]) {
    const fileResultList = fileData.map((fileInfo) => convertToFile(fileInfo));
    fileList.value.push(...fileResultList);
  }

  function getDescriptionFileId() {
    const fileIds = [] as string[];
    Object.keys(descriptionFileIdMap.value).forEach((key) => {
      if (descriptionFileIdMap.value[key].length > 0) {
        fileIds.push(...descriptionFileIdMap.value[key]);
      }
    });
    return fileIds;
  }

  function makeParams() {
    // 自定义字段参数
    const customFields: BugEditCustomFieldItem[] = [];
    if (formItem.value && formItem.value.length) {
      formItem.value.forEach((item: FormRuleItem) => {
        if (item.sourceType === 'CASCADER') {
          item.value = findParents(item.options as Option[], item.value as string, []) || '';
        }
        customFields.push({
          id: item.field as string,
          name: item.title as string,
          type: item.sourceType as string,
          value: Array.isArray(item.value) ? JSON.stringify(item.value) : (item.value as string),
          text: getCurrentText(item, currentCustomFields.value),
        });
      });
    }
    if (isPlatformDefaultTemplate.value && form.value.platformSystemFields) {
      Object.keys(form.value.platformSystemFields).forEach((key) => {
        customFields.push({
          id: platformSystemFieldMap[key].fieldId,
          name: platformSystemFieldMap[key].fieldName,
          type: platformSystemFieldMap[key].type,
          value: form.value.platformSystemFields[key],
        });
      });
      // delete form.value.platformSystemFields;
      // 平台默认模板不传递名称, 描述, 标签等参数
      delete form.value.title;
      delete form.value.description;
      delete form.value.tags;
    }
    // 过滤出复制的附件
    const copyFileList = fileList.value.filter((item) => item.isCopyFlag);
    let copyFiles: { refId: string; fileId: string; local: boolean }[] = [];
    if (copyFileList.length > 0) {
      copyFiles = copyFileList.map((file) => {
        return {
          refId: file.associateId,
          fileId: file.uid,
          local: file.local,
          bugId: bugId.value as string,
          fileName: file.name,
        };
      });
    }
    const tmpObj: BugEditFormObject = {
      ...form.value,
      customFields,
      copyFiles,
      richTextTmpFileIds: isPlatformDefaultTemplate.value ? getDescriptionFileId() : descriptionFileIds.value,
    };
    if (props.isCopyBug) {
      delete tmpObj.id;
      delete tmpObj.richTextTmpFileIds;
    }
    // 过滤出本地保存的文件
    const localFiles = fileList.value.filter((item) => item.local && item.status === 'init');

    return {
      request: tmpObj,
      fileList: localFiles as unknown as File[],
    };
  }

  async function resetForm() {
    // 如果是保存并继续创建
    const { templateId } = form.value;
    // 用当前模板初始化自定义字段
    form.value = {
      projectId: appStore.currentProjectId, // 取当前项目id
      title: '',
      description: '',
      templateId,
      tags: [],
      platformSystemFields: {},
    };
    await templateChange(templateId);
    // 清空文件列表
    fileList.value = [];
  }

  // 保存
  const saveHandler = async (isContinue = false) => {
    formRef.value.validate((error: any) => {
      if (!error) {
        fApi.value.validate(async (valid: any) => {
          if (valid === true) {
            emit('saveParams', isContinue, makeParams());
          }
        });
      }
    });
    scrollIntoView(document.querySelector('.arco-form-item-message'), { block: 'center' });
  };

  const getOptionFromTemplate = (field: CustomFieldItem | undefined) => {
    if (field) {
      return field.options ? field.options : JSON.parse(field.platformOptionJson);
    }
    return [];
  };

  // 设置模板值
  async function setTemplateValue(bugDetail: BugEditFormObject) {
    const { status, templateId, platformBugId } = bugDetail;
    try {
      if (props.isCopyBug) {
        // 复制, 只需返回初始状态
        await templateChange(templateId);
      } else {
        await templateChange(templateId, { fromStatusId: status, platformBugKey: platformBugId });
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  // 处理文件
  async function handleFile(attachments: AttachFileInfo[]) {
    if (!attachments.length) {
      return;
    }
    // 非Copy时, 附件列表赋值
    if (!props.isCopyBug) {
      attachmentsList.value = attachments;
    }
    // 检查文件是否有更新
    const checkUpdateFileIds = await checkFileIsUpdateRequest(attachments.map((item: any) => item.fileId));
    // 处理文件列表
    fileList.value = attachments
      .map((fileInfo: any) => {
        return {
          ...fileInfo,
          name: fileInfo.fileName,
          isUpdateFlag: checkUpdateFileIds.includes(fileInfo.fileId),
          isCopyFlag: props.isCopyBug,
        };
      })
      .map((fileInfo: any) => {
        return convertToFileByBug(fileInfo);
      });
  }

  function makeCustomValue(bugDetail: BugEditFormObject) {
    const { customFields, status } = bugDetail;
    let tmpObj: Record<string, any> = {};

    if (isEdit.value) {
      tmpObj = { status };
    }

    if (customFields && Array.isArray(customFields)) {
      const MULTIPLE_TYPE = ['MULTIPLE_SELECT', 'MULTIPLE_INPUT', 'CHECKBOX', 'MULTIPLE_MEMBER'];
      const SINGRADIO_TYPE = ['RADIO', 'SELECT', 'MEMBER'];
      customFields.forEach((item) => {
        if (item.id === 'status' && props.isCopyBug) {
          // 复制时, 状态赋值为空
          tmpObj[item.id] = '';
          // 多选类型需要过滤选项
        } else if (MULTIPLE_TYPE.includes(item.type)) {
          if (!item.value) {
            tmpObj[item.id] = [];
            return;
          }
          const multipleOptions = getOptionFromTemplate(
            currentCustomFields.value.find((filed: any) => item.id === filed.fieldId)
          );
          // 如果该值在选项中已经被删除掉
          const optionsIds = (multipleOptions || []).map((e: any) => e.value);
          if (item.type !== 'MULTIPLE_INPUT') {
            const currentDefaultValue = optionsIds.filter((e: any) => JSON.parse(item.value).includes(e));
            tmpObj[item.id] = currentDefaultValue;
          } else {
            tmpObj[item.id] = JSON.parse(item.value);
          }
        } else if (item.type === 'INT' || item.type === 'FLOAT') {
          tmpObj[item.id] = Number(item.value);
        } else if (item.type === 'CASCADER') {
          if (item.value) {
            const arr = JSON.parse(item.value);
            if (arr && arr instanceof Array && arr.length > 0) {
              tmpObj[item.id] = arr[arr.length - 1];
            }
          }
        } else if (SINGRADIO_TYPE.includes(item.type)) {
          const multipleOptions = getOptionFromTemplate(
            currentCustomFields.value.find((filed: any) => item.id === filed.fieldId)
          );
          // 如果该值在选项中已经被删除掉
          const optionsIds = (multipleOptions || []).map((e: any) => e.value);
          const currentDefaultValue = optionsIds.find((e: any) => item.value === e) || '';
          tmpObj[item.id] = currentDefaultValue;
        } else {
          tmpObj[item.id] = item.value;
        }
      });
    }
    return tmpObj;
  }

  // 获取详情
  const getDetailInfo = async () => {
    try {
      loading.value = true;
      const res = await getBugDetail(bugId.value as string);
      const { templateId, attachments } = res;

      if (templateId) {
        await setTemplateValue(res);
      }
      // 处理附件
      handleFile(attachments);

      // 处理自定义字段以及三方字段
      const tmpObj = await makeCustomValue(res);
      fApi.value.setValue(tmpObj);

      // 平台默认模板系统字段单独处理
      if (isPlatformDefaultTemplate.value && form.value.platformSystemFields) {
        Object.keys(form.value.platformSystemFields).forEach((key) => {
          form.value.platformSystemFields[key] = tmpObj[key];
        });
      }
      const { platformSystemFields } = form.value;

      let copyName = '';
      if (props.isCopyBug) {
        copyName = `copy_${res.title}`;
        if (copyName.length > 255) {
          form.value.title = copyName.slice(0, 255);
        }
      }

      // 表单赋值
      form.value = {
        id: res.id,
        title: props.isCopyBug ? copyName : res.title,
        description: res.description,
        templateId: res.templateId,
        tags: res.tags || [],
        projectId: res.projectId,
        platformSystemFields,
      };
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  };

  function deleteFile(item: MsFileItem) {
    const index = fileList.value.findIndex((e) => e.uid === item.uid);
    if (index !== -1) {
      fileList.value.splice(index, 1);
    }
  }

  // 监视自定义字段改变处理formCreate
  watch(
    () => formRules.value,
    () => {
      rowLength.value = formRules.value.length + 2;
    },
    { deep: true }
  );

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

  const originTemId = ref<string>('');
  watch(
    () => innerTemplateId.value,
    (val) => {
      if (val) {
        form.value.templateId = val;
        if (props.isCopyBug && originTemId.value === val) {
          getDetailInfo();
        } else {
          templateChange(val);
        }
      }
    }
  );

  const { registerCatchSaveShortcut, removeCatchSaveShortcut } = useShortcutSave(() => {
    saveHandler();
  });

  onMounted(async () => {
    if (isEditOrCopy.value) {
      // 获取详情
      await getDetailInfo();
      if (props.isCopyBug) {
        originTemId.value = form.value.templateId;
        innerTemplateId.value = form.value.templateId;
      }
    }
    registerCatchSaveShortcut();
  });

  onBeforeUnmount(() => {
    removeCatchSaveShortcut();
  });

  defineExpose({
    saveHandler,
    resetForm,
  });
</script>

<style lang="less" scoped>
  :deep(.arco-form-item-extra) {
    font-size: 14px;
    color: var(--color-text-4);
  }
  :deep(.arco-form-item-content) {
    overflow-wrap: anywhere;
  }
</style>
