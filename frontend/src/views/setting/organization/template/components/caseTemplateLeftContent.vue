<template>
  <a-form ref="caseFormRef" class="rounded-[4px]" :model="form" layout="vertical">
    <a-form-item
      field="name"
      :label="t('system.orgTemplate.caseName')"
      :rules="[{ required: props.isDisabled, message: t('system.orgTemplate.caseNamePlaceholder') }]"
      asterisk-position="end"
      :class="`${props.isDisabled ? '' : 'label-validate-star'}`"
    >
      <a-input
        v-model="form.name"
        :max-length="255"
        :placeholder="t('system.orgTemplate.caseNamePlaceholder')"
        allow-clear
        :disabled="props.isDisabled"
      ></a-input>
    </a-form-item>
    <a-form-item field="precondition" :label="t('system.orgTemplate.precondition')" asterisk-position="end">
      <MsRichText
        v-model:raw="form.prerequisite"
        v-model:filed-ids="prerequisiteFileIds"
        :upload-image="handleUploadImage"
        :preview-url="previewEditorImageUrl"
        :editable="!props.isDisabled"
      />
    </a-form-item>
    <StepDescription v-model:caseEditType="form.caseEditType" />
    <!-- 步骤描述 -->
    <div v-if="form.caseEditType === 'STEP'" class="mb-[20px] w-full">
      <AddStep v-model:step-list="stepData" :is-disabled="props.isDisabled" />
    </div>
    <!-- 文本描述 -->
    <div v-else class="pb-[20px]">
      <MsRichText
        v-model:raw="form.textDescription"
        v-model:filed-ids="textDescriptionFileIds"
        :upload-image="handleUploadImage"
        :preview-url="previewEditorImageUrl"
        :editable="!props.isDisabled"
      />
    </div>
    <a-form-item
      v-if="form.caseEditType === 'TEXT'"
      field="remark"
      :label="t('caseManagement.featureCase.expectedResult')"
    >
      <MsRichText
        v-model:raw="form.expectedResult"
        v-model:filed-ids="expectedResultFileIds"
        :upload-image="handleUploadImage"
        :preview-url="previewEditorImageUrl"
        :editable="!props.isDisabled"
      />
    </a-form-item>
    <a-form-item field="description" :label="t('caseManagement.featureCase.remark')">
      <MsRichText
        v-model:raw="form.description"
        v-model:filed-ids="descriptionFileIds"
        :upload-image="handleUploadImage"
        :preview-url="previewEditorImageUrl"
        :editable="!props.isDisabled"
      />
    </a-form-item>
    <AddAttachment v-model:file-list="fileList" disabled multiple />
  </a-form>
</template>

<script setup lang="ts">
  /**
   * @description 系统管理-组织-模板管理-用例模板左侧内容
   */
  import { ref } from 'vue';

  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import AddAttachment from '@/components/business/ms-add-attachment/index.vue';
  import AddStep from '@/views/case-management/caseManagementFeature/components/addStep.vue';
  import StepDescription from '@/views/case-management/caseManagementFeature/components/tabContent/stepDescription.vue';

  import { editorUploadFile } from '@/api/modules/setting/template';
  import { previewOrgImageUrl, previewProImageUrl } from '@/api/requrls/setting/template';
  import { defaultTemplateCaseDetail } from '@/config/template';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { getGenerateId } from '@/utils';

  import type { StepList } from '@/models/caseManagement/featureCase';
  import type { defaultCaseField } from '@/models/setting/template';

  const { t } = useI18n();
  const appStore = useAppStore();

  const props = defineProps<{
    mode: 'organization' | 'project';
    isDisabled?: boolean;
  }>();

  const form = defineModel<defaultCaseField>('defaultForm', {
    default: defaultTemplateCaseDetail,
  });
  const uploadImgFileIds = defineModel<string[]>('uploadImgFileIds', {
    default: [],
  });

  const fileList = ref([]);

  // 获取类型样式
  function getSelectTypeClass(type: string) {
    return form.value.caseEditType === type ? ['bg-[rgb(var(--primary-1))]', '!text-[rgb(var(--primary-5))]'] : [];
  }

  // 前置操作附件id
  const prerequisiteFileIds = ref<string[]>([]);

  // 更改类型
  const handleSelectType = (value: string | number | Record<string, any> | undefined) => {
    form.value.caseEditType = value as string;
  };

  // 文本描述附件id
  const textDescriptionFileIds = ref<string[]>([]);
  // 预期结果附件id
  const expectedResultFileIds = ref<string[]>([]);
  // 描述附件id
  const descriptionFileIds = ref<string[]>([]);

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

  watch(
    () => stepData.value,
    () => {
      const res = stepData.value.map((item, index) => {
        return {
          id: item.id,
          num: index,
          desc: item.step,
          result: item.expected,
        };
      });
      form.value.steps = JSON.stringify(res);
    },
    { deep: true }
  );

  watch(
    () => form.value.steps,
    (val) => {
      if (val) {
        stepData.value = JSON.parse(val).map((item: any) => {
          return {
            id: item.id,
            step: item.desc,
            expected: item.result,
          };
        });
      }
    }
  );
  // 上传图片
  async function handleUploadImage(file: File) {
    const { data } = await editorUploadFile(
      {
        fileList: [file],
      },
      props.mode
    );
    return data;
  }

  const previewEditorImageUrl = computed(() =>
    props.mode === 'organization'
      ? `${previewOrgImageUrl}/${appStore.currentOrgId}`
      : `${previewProImageUrl}/${appStore.currentProjectId}`
  );

  const fileIds = computed(() => {
    return [
      ...prerequisiteFileIds.value,
      ...textDescriptionFileIds.value,
      ...expectedResultFileIds.value,
      ...descriptionFileIds.value,
    ];
  });

  watch(
    () => fileIds.value,
    (val) => {
      if (val) {
        uploadImgFileIds.value = val;
      }
    },
    {
      deep: true,
    }
  );
</script>

<style scoped></style>
