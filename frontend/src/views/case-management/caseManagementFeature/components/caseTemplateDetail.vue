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
            show-word-limit
            allow-clear
          ></a-input>
        </a-form-item>
        <a-form-item field="precondition" :label="t('system.orgTemplate.precondition')" asterisk-position="end">
          <MsRichText v-model:model-value="form.prerequisite" />
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
          <MsRichText v-else v-model:modelValue="form.textDescription" />
        </a-form-item>
        <a-form-item
          v-if="form.caseEditType === 'TEXT'"
          field="remark"
          :label="t('caseManagement.featureCase.expectedResult')"
        >
          <MsRichText v-model:modelValue="form.expectedResult" />
        </a-form-item>
        <a-form-item field="remark" :label="t('caseManagement.featureCase.remark')">
          <MsRichText v-model:modelValue="form.description" />
        </a-form-item>
        <AddAttachment @change="handleChange" @link-file="associatedFile" @upload="beforeUpload" />
      </a-form>
      <!-- 文件列表开始 -->
      <div class="w-[90%]">
        <MsFileList ref="fileListRef" v-model:file-list="fileList" mode="static">
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
                  caseId:route.query.id as string,
                  fileId:item.uid,
                  local:true
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
                v-if="route.query.id"
                type="button"
                status="primary"
                class="!mr-[4px]"
                @click="downloadFile(item)"
              >
                {{ t('caseManagement.featureCase.download') }}
              </MsButton>
              <MsButton
                v-if="route.query.id && item.isUpdateFlag"
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
            :label="t('system.orgTemplate.modules')"
            :rules="[{ required: true, message: t('system.orgTemplate.moduleRuleTip') }]"
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
              :tree-props="{
                virtualListProps: {
                  height: 200,
                },
              }"
            ></a-tree-select>
          </a-form-item>
          <MsFormCreate
            v-if="formRules.length"
            ref="formCreateRef"
            v-model:api="fApi"
            v-model:form-item="formItem"
            :form-rule="formRules"
          />
          <a-form-item field="tags" :label="t('system.orgTemplate.tags')">
            <a-input-tag v-model="form.tags" :placeholder="t('formCreate.PleaseEnter')" allow-clear />
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
    getAssociatedFileListUrl,
    getCaseDefaultFields,
    getCaseDetail,
    previewFile,
    transferFileRequest,
    updateFile,
  } from '@/api/modules/case-management/featureCase';
  import { getModules, getModulesCount } from '@/api/modules/project-management/fileManagement';
  import { getProjectFieldList } from '@/api/modules/setting/template';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';
  import { downloadByteFile, getGenerateId } from '@/utils';

  import type {
    AssociatedList,
    AttachFileInfo,
    CreateOrUpdateCase,
    DetailCase,
    StepList,
  } from '@/models/caseManagement/featureCase';
  import type { TableQueryParams } from '@/models/common';
  import type { CustomField, DefinedFieldItem } from '@/models/setting/template';

  import { convertToFile } from './utils';
  import {
    getCustomDetailFields,
    getTotalFieldOptionList,
  } from '@/views/setting/organization/template/components/fieldSetting';

  const { t } = useI18n();
  const route = useRoute();
  const appStore = useAppStore();
  const currentProjectId = computed(() => appStore.currentProjectId);

  const props = defineProps<{
    formModeValue: Record<string, any>; // 表单值
  }>();

  const emit = defineEmits(['update:formModeValue', 'changeFile']);
  const acceptType = ref('none'); // 模块-上传文件类型
  const formRef = ref<FormInstance>();
  const caseFormRef = ref<FormInstance>();

  // 默认模版自定义字段
  const selectData = ref<DefinedFieldItem[]>([]);

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
  const caseTree = computed(() => featureCaseStore.caseTree);

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

  // 总自定义字段
  const totalTemplateField = ref<DefinedFieldItem[]>([]);

  const isLoading = ref<boolean>(true);

  // 获取所有字段
  async function getAllCaseFields() {
    try {
      totalTemplateField.value = await getProjectFieldList({ scopedId: currentProjectId.value, scene: 'FUNCTIONAL' });
      totalTemplateField.value = getTotalFieldOptionList(totalTemplateField.value as DefinedFieldItem[]);
    } catch (error) {
      console.log(error);
    }
  }

  // 初始化模版默认字段
  async function initDefaultFields() {
    try {
      isLoading.value = true;
      await getAllCaseFields();
      const res = await getCaseDefaultFields(currentProjectId.value);

      const { customFields, id } = res;
      form.value.templateId = id;

      selectData.value = getCustomDetailFields(totalTemplateField.value as DefinedFieldItem[], customFields);

      isLoading.value = false;
    } catch (error) {
      console.log(error);
    }
  }

  const fileList = ref<MsFileItem[]>([]);

  function beforeUpload(file: File) {
    const _maxSize = 50 * 1024 * 1024;
    if (file.size > _maxSize) {
      Message.warning(t('ms.upload.overSize'));
      return Promise.resolve(false);
    }
    return Promise.resolve(true);
  }

  // 处理关联文件
  function saveSelectAssociatedFile(fileData: AssociatedList[]) {
    const fileResultList = fileData.map((fileInfo) => convertToFile(fileInfo));
    fileList.value.push(...fileResultList);
  }

  const isEditOrCopy = computed(() => !!route.query.id);
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
          caseId: route.query.id as string,
          fileId: item.uid,
          local: item.local,
        });
        const blob = new Blob([res], { type: 'image/jpeg' });
        imageUrl.value = URL.createObjectURL(blob);
      } else {
        imageUrl.value = item.url as string;
      }
    } catch (error) {
      console.log(error);
    }
  }

  const checkUpdateFileIds = ref<string[]>([]);

  // 处理详情字段
  function getDetailData(detailResult: DetailCase) {
    const { customFields, attachments, steps, tags } = detailResult;
    form.value = {
      ...detailResult,
      name: route.params.mode === 'copy' ? `${detailResult.name}_copy` : detailResult.name,
      tags: JSON.parse(tags),
    };
    // 处理自定义字段
    selectData.value = getCustomDetailFields(
      totalTemplateField.value as DefinedFieldItem[],
      customFields as CustomField[]
    );
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
      await getAllCaseFields();
      const detailResult: DetailCase = await getCaseDetail(route.query.id as string);
      const fileIds = (detailResult.attachments || []).map((item: any) => item.id);
      if (fileIds.length) {
        checkUpdateFileIds.value = await checkFileIsUpdateRequest(fileIds);
      }

      getDetailData(detailResult);
    } catch (error) {
      console.log(error);
    } finally {
      isLoading.value = false;
    }
  }

  watchEffect(() => {
    if (route.params.mode === 'edit' || route.params.mode === 'copy') {
      getCaseInfo();
    } else {
      initDefaultFields();
    }
  });
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
          featureCaseStore.setModuleId([form.value.moduleId], []);
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

  function handleChange(_fileList: MsFileItem[], fileItem: MsFileItem) {
    fileList.value = _fileList.map((e) => {
      return {
        ...e,
        enable: true, // 是否启用
        local: true, // 是否本地文件
      };
    });
  }

  const rowLength = ref<number>(0);
  const formRuleField = ref<FormItem[][]>([]);
  const formRules = ref<FormItem[]>([]);
  const formItem = ref<FormRuleItem[]>([]);
  const fApi = ref<any>(null);

  // 处理表单格式
  const getFormRules = () => {
    formRuleField.value = [];
    formRules.value = [];
    if (selectData.value && selectData.value.length) {
      selectData.value.forEach((item: any) => {
        const currentFormItem = item.formRules?.map((rule: any) => {
          let optionsItem = [];
          if (rule.options && rule.options.length) {
            optionsItem = rule.options.map((opt: any) => {
              return {
                text: opt.label,
                value: opt.value,
              };
            });
          }
          return {
            type: item.type,
            name: item.id,
            label: item.name,
            value: isEditOrCopy.value ? JSON.parse(rule.value) : rule.value,
            options: optionsItem,
            required: item.required,
            props: {
              modelValue: isEditOrCopy.value ? JSON.parse(rule.value) : rule.value,
              options: optionsItem,
            },
          };
        });
        formRuleField.value.push(currentFormItem as FormItem[]);
      });
      formRules.value = formRuleField.value.flatMap((item) => item);
    }
  };

  // 监视表单右侧自定义字段改变收集自定义字段参数
  watch(
    () => formItem.value,
    (val) => {
      if (val) {
        const customFieldsMaps: Record<string, any> = {};
        formItem.value?.forEach((item: any) => {
          customFieldsMaps[item.field as string] = item.value;
        });
        form.value.customFields = customFieldsMaps;
      }
    },
    { deep: true }
  );

  // 监视自定义字段改变处理formCreate
  watch(
    () => selectData.value,
    () => {
      getFormRules();
      rowLength.value = formRules.value.length + 2;
    },
    { deep: true }
  );

  onBeforeUnmount(() => {
    formRules.value = [];
    formRuleField.value = [];
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
        caseId: route.query.id as string,
        fileId: item.uid,
        local: true,
      });
      downloadByteFile(res, `${item.name}`);
    } catch (error) {
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

  defineExpose({
    caseFormRef,
    formRef,
    fApi,
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
