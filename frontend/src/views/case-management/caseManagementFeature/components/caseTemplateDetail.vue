<template>
  <div class="wrapper-preview">
    <div class="preview-left pr-4">
      <a-form ref="caseFormRef" class="rounded-[4px]" :model="form" layout="vertical">
        <a-form-item
          field="name"
          :label="t('system.orgTemplate.caseName')"
          :rules="[{ required: true, message: t('system.orgTemplate.caseNamePlaceholder') }]"
          required
          asterisk-position="end"
        >
          <a-input
            v-model="form.name"
            :max-length="255"
            :placeholder="t('system.orgTemplate.caseNamePlaceholder')"
            allow-clear
          ></a-input>
        </a-form-item>
        <a-form-item field="precondition" :label="t('system.orgTemplate.precondition')" asterisk-position="end">
          <MsRichText
            v-model:raw="form.prerequisite"
            v-model:filed-ids="prerequisiteFileIds"
            :upload-image="handleUploadImage"
            :preview-url="PreviewEditorImageUrl"
          />
        </a-form-item>
        <a-form-item
          field="step"
          :label="
            form.caseEditType === 'STEP'
              ? t('system.orgTemplate.stepDescription')
              : t('system.orgTemplate.textDescription')
          "
          class="relative"
        >
          <div class="absolute left-16 top-0">
            <a-divider direction="vertical" />
            <a-dropdown :popup-max-height="false" @select="handleSelectType">
              <span class="changeType">{{ t('system.orgTemplate.changeType') }} <icon-down /></span>
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
          <div v-if="form.caseEditType === 'STEP'" class="w-full">
            <AddStep v-model:step-list="stepData" :is-disabled="false" />
          </div>
          <!-- 文本描述 -->
          <MsRichText
            v-else
            v-model:raw="form.textDescription"
            v-model:filed-ids="textDescriptionFileIds"
            :upload-image="handleUploadImage"
            :preview-url="PreviewEditorImageUrl"
          />
        </a-form-item>
        <a-form-item
          v-if="form.caseEditType === 'TEXT'"
          field="remark"
          :label="t('caseManagement.featureCase.expectedResult')"
        >
          <MsRichText
            v-model:raw="form.expectedResult"
            v-model:filed-ids="expectedResultFileIds"
            :upload-image="handleUploadImage"
            :preview-url="PreviewEditorImageUrl"
          />
        </a-form-item>
        <a-form-item field="description" :label="t('caseManagement.featureCase.remark')">
          <MsRichText
            v-model:raw="form.description"
            v-model:filed-ids="descriptionFileIds"
            :upload-image="handleUploadImage"
            :preview-url="PreviewEditorImageUrl"
          />
        </a-form-item>
        <AddAttachment v-model:file-list="fileList" multiple @change="handleChange" @link-file="associatedFile" />
      </a-form>
      <!-- 文件列表开始 -->
      <div class="w-[90%]">
        <MsFileList ref="fileListRef" v-model:file-list="fileList" mode="static" :show-upload-type-desc="true">
          <template #actions="{ item }">
            <!-- 本地文件 -->
            <div v-if="item.local || item.status === 'init'" class="flex flex-nowrap">
              <MsButton
                v-if="item.status !== 'init' && item.file.type.includes('image/')"
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
                  caseId: props.caseId,
                  fileId: item.uid,
                  local: true,
                }"
                @success="getCaseInfo()"
              />
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
                v-if="item.status !== 'init'"
                type="button"
                status="primary"
                class="!mr-[4px]"
                @click="handlePreview(item)"
              >
                {{ t('ms.upload.preview') }}
              </MsButton>
              <MsButton
                v-if="props.caseId"
                type="button"
                status="primary"
                class="!mr-[4px]"
                @click="downloadFile(item)"
              >
                {{ t('caseManagement.featureCase.download') }}
              </MsButton>
              <MsButton
                v-if="props.caseId && item.isUpdateFlag"
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
        </MsFileList>
      </div>
      <!-- 文件列表结束 -->
    </div>
    <!-- 自定义字段开始 -->
    <div class="preview-right px-4">
      <div>
        <a-skeleton v-if="isLoading" :loading="isLoading" :animation="true">
          <a-space direction="vertical" class="w-full" size="large">
            <a-skeleton-line :rows="rowLength" :line-height="30" :line-spacing="30" />
          </a-space>
        </a-skeleton>
        <a-form v-else ref="formRef" class="rounded-[4px]" :model="form" layout="vertical">
          <a-form-item
            field="moduleId"
            asterisk-position="end"
            :label="t('caseManagement.featureCase.ModuleOwned')"
            :rules="[{ required: true, message: t('system.orgTemplate.moduleRuleTip') }]"
            @change="changeSelectModule"
          >
            <a-tree-select
              v-model="form.moduleId"
              :allow-search="true"
              :data="caseTree"
              :field-names="{
                title: 'name',
                key: 'id',
                children: 'children',
              }"
              :draggable="false"
              :tree-props="{
                virtualListProps: {
                  height: 200,
                },
              }"
            >
              <template #tree-slot-title="node">
                <a-tooltip :content="`${node.name}`" position="tl">
                  <div class="one-line-text w-[300px] text-[var(--color-text-1)]">{{ node.name }}</div>
                </a-tooltip>
              </template>
            </a-tree-select>
          </a-form-item>
          <MsFormCreate
            v-if="formRules.length"
            ref="formCreateRef"
            v-model:api="fApi"
            v-model:form-item="formItem"
            :form-rule="formRules"
          />
          <a-form-item field="tags" :label="t('system.orgTemplate.tags')">
            <MsTagsInput v-model:model-value="form.tags" />
          </a-form-item>
        </a-form>
      </div>
    </div>
    <!-- 自定义字段结束 -->
  </div>
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
  <LinkFileDrawer
    v-model:visible="showDrawer"
    :get-tree-request="getModules"
    :get-count-request="getModulesCount"
    :get-list-request="getAssociatedFileListUrl"
    :get-list-fun-params="getListFunParams"
    @save="saveSelectAssociatedFile"
  />
  <a-image-preview v-model:visible="previewVisible" :src="imageUrl" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { FormInstance, Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsFormCreate from '@/components/pure/ms-form-create/ms-form-create.vue';
  import type { FormItem, FormRuleItem } from '@/components/pure/ms-form-create/types';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import MsFileList from '@/components/pure/ms-upload/fileList.vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';
  import type { MsFileItem } from '@/components/pure/ms-upload/types';
  import AddAttachment from '@/components/business/ms-add-attachment/index.vue';
  import LinkFileDrawer from '@/components/business/ms-link-file/associatedFileDrawer.vue';
  import AddStep from './addStep.vue';
  import TransferModal from './tabContent/transferModal.vue';

  import {
    checkFileIsUpdateRequest,
    downloadFileRequest,
    editorUploadFile,
    getAssociatedFileListUrl,
    getCaseDefaultFields,
    getCaseDetail,
    getCaseModuleTree,
    previewFile,
    transferFileRequest,
    updateFile,
  } from '@/api/modules/case-management/featureCase';
  import { getModules, getModulesCount } from '@/api/modules/project-management/fileManagement';
  import { PreviewEditorImageUrl } from '@/api/requrls/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';
  import useUserStore from '@/store/modules/user';
  import { downloadByteFile, getGenerateId } from '@/utils';
  import { scrollIntoView } from '@/utils/dom';
  import { hasAnyPermission } from '@/utils/permission';

  import type {
    AssociatedList,
    AttachFileInfo,
    CreateOrUpdateCase,
    CustomAttributes,
    DetailCase,
    OptionsFieldId,
    StepList,
  } from '@/models/caseManagement/featureCase';
  import type { ModuleTreeNode, TableQueryParams } from '@/models/common';

  import { convertToFile, initFormCreate } from './utils';

  const userStore = useUserStore();

  const { t } = useI18n();
  const route = useRoute();
  const appStore = useAppStore();
  const currentProjectId = computed(() => appStore.currentProjectId);

  const props = defineProps<{
    formModeValue: Record<string, any>; // 表单值
    caseId: string; // 用例id
  }>();

  const emit = defineEmits(['update:formModeValue', 'changeFile']);
  const acceptType = ref('none'); // 模块-上传文件类型
  const formRef = ref<FormInstance>();
  const caseFormRef = ref<FormInstance>();

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

  const featureCaseStore = useFeatureCaseStore();
  const modelId = computed(() => featureCaseStore.moduleId[0]);

  const initForm: DetailCase = {
    id: '',
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
    moduleId: modelId.value,
    versionId: '',
    tags: [],
    customFields: [],
    relateFileMetaIds: [],
    functionalPriority: '',
    reviewStatus: 'UN_REVIEWED',
  };

  const form = ref<DetailCase | CreateOrUpdateCase>({ ...initForm });

  watch(
    () => stepData.value,
    () => {
      const res = stepData.value.map((item, index) => {
        return {
          num: index,
          desc: item.step,
          result: item.expected,
        };
      });
      form.value.steps = JSON.stringify(res);
    },
    { deep: true }
  );
  // 总参数
  const params = ref<Record<string, any>>({
    request: {},
    fileList: [], // 总文件列表
  });

  // 获取类型样式
  function getSelectTypeClass(type: string) {
    return form.value.caseEditType === type ? ['bg-[rgb(var(--primary-1))]', '!text-[rgb(var(--primary-5))]'] : [];
  }

  // 更改类型
  const handleSelectType = (value: string | number | Record<string, any> | undefined) => {
    form.value.caseEditType = value as string;
  };

  const isLoading = ref<boolean>(true);

  const rowLength = ref<number>(0);
  const formRules = ref<FormItem[]>([]);
  const formItem = ref<FormRuleItem[]>([]);
  const fApi = ref<any>(null);
  // 初始化模板默认字段
  async function initDefaultFields() {
    formRules.value = [];
    try {
      isLoading.value = true;
      const res = await getCaseDefaultFields(currentProjectId.value);
      const { customFields, id } = res;
      form.value.templateId = id;
      const result = customFields.map((item: any) => {
        const memberType = ['MEMBER', 'MULTIPLE_MEMBER'];
        let initValue = item.defaultValue;
        const optionsValue: OptionsFieldId[] = item.options;
        if (memberType.includes(item.type)) {
          if (item.defaultValue === 'CREATE_USER' || item.defaultValue.includes('CREATE_USER')) {
            initValue = item.type === 'MEMBER' ? userStore.id : [userStore.id];
          }
        }

        return {
          type: item.type,
          name: item.fieldId,
          label: item.fieldName,
          value: initValue,
          required: item.required,
          options: optionsValue || [],
          props: {
            modelValue: initValue,
            options: optionsValue || [],
          },
        };
      });
      formRules.value = result;
      isLoading.value = false;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const fileList = ref<MsFileItem[]>([]);

  // 处理关联文件
  function saveSelectAssociatedFile(fileData: AssociatedList[]) {
    const fileResultList = fileData.map((fileInfo) => convertToFile(fileInfo));
    fileList.value.push(...fileResultList);
  }

  const isEditOrCopy = computed(() => !!props.caseId);
  const attachmentsList = ref<AttachFileInfo[]>([]);

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

  const imageUrl = ref('');
  const previewVisible = ref<boolean>(false);

  // 预览图片
  async function handlePreview(item: MsFileItem) {
    try {
      previewVisible.value = true;
      if (item.status !== 'init') {
        const res = await previewFile({
          projectId: currentProjectId.value,
          caseId: props.caseId,
          fileId: item.uid,
          local: item.local,
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

  const checkUpdateFileIds = ref<string[]>([]);

  // 处理详情字段
  function getDetailData(detailResult: DetailCase) {
    const { customFields, attachments, steps } = detailResult;

    let copyName = `copy_${detailResult.name}`;
    if (copyName.length > 255) {
      copyName = copyName.slice(0, 255);
    }

    form.value = {
      ...detailResult,
      name: route.params.mode === 'copy' ? copyName : detailResult.name,
    };
    // 处理自定义字段
    formRules.value = initFormCreate(customFields as CustomAttributes[], ['FUNCTIONAL_CASE:READ+UPDATE']);
    // 处理步骤
    if (steps) {
      stepData.value = JSON.parse(steps).map((item: any) => {
        return {
          step: item.desc,
          expected: item.result,
        };
      });
    }
    if (attachments) {
      attachmentsList.value = attachments;
      // 处理文件列表
      fileList.value = attachments
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
  }

  // 处理详情
  async function getCaseInfo() {
    try {
      isLoading.value = true;
      const detailResult: DetailCase = await getCaseDetail(props.caseId);
      const fileIds = (detailResult.attachments || []).map((item: any) => item.id) || [];
      if (fileIds.length) {
        checkUpdateFileIds.value = await checkFileIsUpdateRequest(fileIds);
      }
      getDetailData(detailResult);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      isLoading.value = false;
    }
  }

  const caseTree = ref<ModuleTreeNode[]>([]);

  async function initSelectTree() {
    try {
      caseTree.value = await getCaseModuleTree({ projectId: currentProjectId.value });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
  // 处理关联文件和已关联文件本地文件和已上传文本文件
  function getFilesParams() {
    form.value.deleteFileMetaIds = deleteFileMetaIds.value;
    form.value.unLinkFilesIds = unLinkFilesIds.value;
    params.value.fileList = fileList.value.filter((item: any) => item.status === 'init');
    form.value.relateFileMetaIds = newAssociateFileListIds.value;
  }

  const getListFunParams = ref<TableQueryParams>({
    combine: {
      hiddenIds: [],
    },
  });

  function changeSelectModule(value: string) {
    featureCaseStore.setModuleId([value]);
  }

  // 监视文件列表处理关联和本地文件
  watch(
    () => fileList.value,
    (val) => {
      if (val) {
        form.value.relateFileMetaIds = fileList.value.filter((item) => !item.local).map((item) => item.uid);
        params.value.fileList = fileList.value.filter((item) => item.local && item.status === 'init');
        getListFunParams.value.combine.hiddenIds = fileList.value.filter((item) => !item.local).map((item) => item.uid);
        if (isEditOrCopy.value) {
          getFilesParams();
        }
      }
    },
    { deep: true }
  );

  // 监视表单变化
  watch(
    () => form.value,
    (val) => {
      if (val) {
        if (val) {
          params.value.request = { ...form.value };
          emit('update:formModeValue', params.value);
        }
      }
    },
    { deep: true }
  );

  // 监视父组件传递过来的参数处理
  watch(
    () => props.formModeValue,
    () => {
      // 这里要处理预览的参数格式给到params
      params.value = {
        ...props.formModeValue,
      };
    },
    { deep: true }
  );

  const showDrawer = ref<boolean>(false);

  function associatedFile() {
    showDrawer.value = true;
  }

  function handleChange(_fileList: MsFileItem[]) {
    fileList.value = _fileList.map((e) => {
      return {
        ...e,
        enable: true, // 是否启用
      };
    });
  }

  // 监视表单右侧自定义字段改变收集自定义字段参数
  watch(
    () => formItem.value,
    (val) => {
      if (val) {
        form.value.customFields = formItem.value.map((item: any) => {
          return {
            fieldId: item.field,
            value: Array.isArray(item.value) ? JSON.stringify(item.value) : item.value,
          };
        });
      }
    },
    { deep: true }
  );

  // 监视自定义字段改变处理formCreate
  watch(
    () => formRules.value,
    () => {
      rowLength.value = formRules.value.length + 2;
    },
    { deep: true }
  );

  onBeforeUnmount(() => {
    formRules.value = [];
  });

  const transferVisible = ref<boolean>(false);
  // 转存
  function transferFile() {
    transferVisible.value = true;
  }
  // 下载
  async function downloadFile(item: MsFileItem) {
    try {
      const res = await downloadFileRequest({
        projectId: currentProjectId.value,
        caseId: props.caseId,
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
      await updateFile(currentProjectId.value, item.associationId);
      Message.success(t('common.updateSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

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

  watch(
    () => allAttachmentsFileIds.value,
    (val) => {
      if (val) {
        params.value.request.caseDetailFileIds = val;
      }
    }
  );

  async function handleUploadImage(file: File) {
    const { data } = await editorUploadFile({
      fileList: [file],
    });
    return data;
  }

  async function resetForm() {
    form.value = { ...initForm, templateId: form.value.templateId };
    await initDefaultFields();
    form.value.customFields = formItem.value.map((item: any) => {
      return {
        fieldId: item.field,
        value: Array.isArray(item.value) ? JSON.stringify(item.value) : item.value,
      };
    });
    fileList.value = [];
    form.value.tags = [];
    stepData.value = [
      {
        id: getGenerateId(),
        step: '',
        expected: '',
        showStep: false,
        showExpected: false,
      },
    ];
    caseFormRef.value?.resetFields();
  }

  const caseId = ref(props.caseId);

  onBeforeMount(async () => {
    caseId.value = '';
    caseId.value = props.caseId;
    initSelectTree();
    if (caseId.value) {
      getCaseInfo();
    } else {
      initDefaultFields();
    }
  });

  // onMounted(() => {
  //   nextTick(() => {
  //     console.log(document.querySelector('.preview-left'));

  //     scrollIntoView(document.querySelector('.preview-left'), { block: 'center' });
  //     console.log(caseFormRef.value?.$el.querySelector('.arco-form-item-message'));

  //   });
  // });

  defineExpose({
    caseFormRef,
    formRef,
    fApi,
    resetForm,
  });
</script>

<style scoped lang="less">
  .wrapper-preview {
    display: flex;
    .preview-left {
      width: calc(100% - 396px);
      border-right: 1px solid var(--color-text-n8);
      .changeType {
        padding: 2px 4px;
        border-radius: 4px;
        color: var(--color-text-4);
        :deep(.arco-icon-down) {
          font-size: 14px;
        }
        &:hover {
          color: rgb(var(--primary-5));
          background: rgb(var(--primary-1));
          cursor: pointer;
        }
      }
    }
    .preview-right {
      width: 428px;
    }
  }
  .circle {
    width: 16px;
    height: 16px;
    line-height: 16px;
    border-radius: 50%;
    text-align: center;
    color: var(--color-text-4);
    background: var(--color-text-n8);
  }
</style>
