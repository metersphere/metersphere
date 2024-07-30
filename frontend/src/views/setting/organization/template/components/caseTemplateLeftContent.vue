<template>
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
        :disabled="props.isDisabled"
      ></a-input>
    </a-form-item>
    <a-form-item field="precondition" :label="t('system.orgTemplate.precondition')" asterisk-position="end">
      <MsRichText
        v-model:raw="form.prerequisite"
        v-model:filed-ids="prerequisiteFileIds"
        :upload-image="handleUploadImage"
        :preview-url="PreviewEditorImageUrl"
        :editable="!props.isDisabled"
      />
    </a-form-item>
    <a-form-item
      field="step"
      :label="
        form.caseEditType === 'STEP' ? t('system.orgTemplate.stepDescription') : t('system.orgTemplate.textDescription')
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
        <AddStep v-model:step-list="stepData" :is-disabled="props.isDisabled" />
      </div>
      <!-- 文本描述 -->
      <MsRichText
        v-else
        v-model:raw="form.textDescription"
        v-model:filed-ids="textDescriptionFileIds"
        :upload-image="handleUploadImage"
        :preview-url="PreviewEditorImageUrl"
        :editable="!props.isDisabled"
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
        :editable="!props.isDisabled"
      />
    </a-form-item>
    <a-form-item field="description" :label="t('caseManagement.featureCase.remark')">
      <MsRichText
        v-model:raw="form.description"
        v-model:filed-ids="descriptionFileIds"
        :upload-image="handleUploadImage"
        :preview-url="PreviewEditorImageUrl"
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

  import { editorUploadFile } from '@/api/modules/case-management/featureCase';
  import { PreviewEditorImageUrl } from '@/api/requrls/case-management/featureCase';
  import { defaultTemplateCaseDetail } from '@/config/template';
  import { useI18n } from '@/hooks/useI18n';
  import { getGenerateId } from '@/utils';

  import type { StepList } from '@/models/caseManagement/featureCase';
  import type { defaultCaseField } from '@/models/setting/template';

  const { t } = useI18n();

  const props = defineProps<{
    isDisabled?: boolean;
  }>();

  const form = defineModel<defaultCaseField>('defaultForm', {
    default: defaultTemplateCaseDetail,
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
  // TODO 上传需要接口
  async function handleUploadImage(file: File) {
    const { data } = await editorUploadFile({
      fileList: [file],
    });
    return data;
  }
</script>

<style scoped></style>
