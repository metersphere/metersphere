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
        <MsRichText v-if="isEditPreposition" v-model:model-value="detailForm.prerequisite" class="mt-2" />
        <div v-else class="text-[var(--color-text-3)]" v-html="detailForm?.prerequisite || '-'"></div>
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
            <span class="changeType text-[var(--color-text-3)]"
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
          <AddStep v-model:step-list="stepData" :is-disabled="isEditPreposition" />
        </div>
        <!-- 文本描述 -->
        <MsRichText
          v-if="detailForm.caseEditType === 'TEXT' && isEditPreposition"
          v-model:modelValue="detailForm.textDescription"
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
          v-model:modelValue="detailForm.expectedResult"
        />
        <div v-else class="text-[var(--color-text-3)]" v-html="detailForm.description || '-'"></div>
      </a-form-item>
      <a-form-item field="remark" :label="t('caseManagement.featureCase.remark')">
        <MsRichText v-if="isEditPreposition" v-model:modelValue="detailForm.description" />
        <div v-else class="text-[var(--color-text-3)]" v-html="detailForm.description || '-'"></div>
      </a-form-item>
      <div v-if="isEditPreposition" class="flex justify-end">
        <a-button type="secondary" @click="handleCancel">{{ t('common.cancel') }}</a-button>
        <a-button class="ml-[12px]" type="primary" :loading="confirmLoading" @click="handleOK">
          {{ t('common.save') }}
        </a-button></div
      >
      <a-form-item field="attachment" :label="t('caseManagement.featureCase.attachment')">
        <div class="flex flex-col">
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
                  @change="handleChange"
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
      <MsFileList ref="fileListRef" v-model:file-list="fileList" mode="static">
        <template #actions="{ item }">
          <!-- 本地文件 -->
          <div v-if="item.local || item.status === 'init'" class="flex flex-nowrap">
            <MsButton type="button" status="danger" class="!mr-[4px]" @click="transferFile(item)">
              {{ t('caseManagement.featureCase.storage') }}
            </MsButton>
            <MsButton type="button" status="primary" class="!mr-[4px]" @click="downloadFile(item)">
              {{ t('caseManagement.featureCase.download') }}
            </MsButton>
          </div>
          <!-- 关联文件 -->
          <div v-else class="flex flex-nowrap">
            <MsButton type="button" status="primary" class="!mr-[4px]" @click="cancelAssociated(item)">
              {{ t('caseManagement.featureCase.cancelLink') }}
            </MsButton>
            <MsButton type="button" status="primary" class="!mr-[4px]" @click="downloadFile(item)">
              {{ t('caseManagement.featureCase.download') }}
            </MsButton>
          </div>
        </template>
      </MsFileList>
    </div>
  </div>
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
    :cut-height="50"
    @change="handleChange"
  />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { FormInstance, Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import MsFileList from '@/components/pure/ms-upload/fileList.vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';
  import type { MsFileItem } from '@/components/pure/ms-upload/types';
  import AddStep from './addStep.vue';

  import { updateCaseRequest } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import useFormCreateStore from '@/store/modules/form-create/form-create';
  import { getGenerateId } from '@/utils';
  import { scrollIntoView } from '@/utils/dom';

  import type { StepList } from '@/models/caseManagement/featureCase';
  import { FormCreateKeyEnum } from '@/enums/formCreateEnum';

  import { convertToFile } from './utils';
  import debounce from 'lodash-es/debounce';

  const formCreateStore = useFormCreateStore();

  const caseFormRef = ref<FormInstance>();

  const appStore = useAppStore();
  const currentProjectId = computed(() => appStore.currentProjectId);

  const { t } = useI18n();

  const props = withDefaults(
    defineProps<{
      form: Record<string, any>;
      allowEdit?: boolean; // 是否允许编辑
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

  function beforeUpload() {
    return Promise.resolve(true);
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

  const showDrawer = ref<boolean>(false);
  function associatedFile() {
    showDrawer.value = true;
  }
  // 转存
  function transferFile(item: any) {}
  // 下载
  function downloadFile(item: any) {}
  // 取消关联
  function cancelAssociated(item: any) {}

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
    return associateFileIds.value.filter((id: string) => !currentAlreadyAssociateFileList.value.includes(id));
  });

  const formRuleList = computed(() =>
    formCreateStore.formCreateRuleMap.get(FormCreateKeyEnum.CASE_CUSTOM_ATTRS_DETAIL)
  );

  function getParams() {
    const steps = stepData.value.map((item, index) => {
      return {
        num: index,
        desc: item.step,
        result: item.expected,
      };
    });

    const customFieldsMaps: Record<string, any> = {};
    formRuleList.value?.forEach((item: any) => {
      customFieldsMaps[item.field as string] = item.value;
    });

    return {
      request: {
        ...detailForm.value,
        steps: JSON.stringify(steps),
        deleteFileMetaIds: deleteFileMetaIds.value,
        unLinkFilesIds: unLinkFilesIds.value,
        newAssociateFileListIds: newAssociateFileListIds.value,
        tags: JSON.parse(detailForm.value.tags),
        customFields: customFieldsMaps,
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

  function getDetails() {
    const { steps, attachments } = detailForm.value;
    if (steps) {
      stepData.value = JSON.parse(steps).map((item: any) => {
        return {
          step: item.desc,
          expected: item.result,
        };
      });
    }
    attachmentsList.value = attachments;
    // 处理文件列表
    fileList.value = attachments
      .map((fileInfo: any) => {
        return {
          ...fileInfo,
          name: fileInfo.fileName,
        };
      })
      .map((fileInfo: any) => {
        return convertToFile(fileInfo);
      });
  }

  watch(
    () => props.form,
    () => {
      detailForm.value = { ...props.form };
      getDetails();
    },
    {
      deep: true,
    }
  );

  // 单独更新字段
  const updateCustomFields = debounce(() => {
    const customFieldsMaps: Record<string, any> = {};
    formRuleList.value?.forEach((item: any) => {
      customFieldsMaps[item.field as string] = item.value;
    });
    detailForm.value.customFields = customFieldsMaps as Record<string, any>;
  }, 300);

  // 监视收集自定义字段参数
  watch(
    () => formRuleList.value,
    () => {
      const customFieldsValues = props.form.customFields.map((item: any) => JSON.parse(item.defaultValue));
      // 如果和起始值不一致更新某个字段
      const isChange = formRuleList.value?.every((item: any) => customFieldsValues.includes(item.value));
      if (!isChange) {
        updateCustomFields();
        handleOK();
      }
    },
    { deep: true }
  );
</script>

<style scoped lang="less">
  :deep(.arco-form-item-label) {
    font-weight: bold !important;
  }
</style>
