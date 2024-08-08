<template>
  <a-tooltip
    v-if="!props.canEdit"
    :mouse-enter-delay="300"
    :disabled="!props.customForm.label"
    :content="props.customForm.label"
    position="tl"
  >
    <div class="one-line-text mb-[8px] font-medium">{{ innerTextForm.label }}</div>
  </a-tooltip>
  <a-input
    v-else
    v-model:model-value="innerTextForm.label"
    :placeholder="t('report.detail.customTitlePlaceHolder')"
    :max-length="255"
    allow-clear
    @change="() => emit('handleSetSave')"
    @blur="blurHandler"
  />
  <div :class="`${hasAnyPermission(['PROJECT_TEST_PLAN_REPORT:READ+UPDATE']) && !shareId ? '' : 'cursor-not-allowed'}`">
    <MsRichText
      ref="msRichTextRef"
      v-model:raw="innerTextForm.content"
      v-model:filedIds="innerTextForm.richTextTmpFileIds"
      :upload-image="handleUploadImage"
      :preview-url="`${ReportPlanPreviewImageUrl}/${appStore.currentProjectId}`"
      class="mt-[8px] w-full"
      :editable="props.canEdit"
      @click="handleRichClick"
      @update="() => emit('handleSetSave')"
    />
  </div>
  <div
    v-show="hasAnyPermission(['PROJECT_TEST_PLAN_REPORT:READ+UPDATE']) && !shareId && props.canEdit"
    class="mt-[16px] flex items-center gap-[12px]"
  >
    <a-button type="primary" @click="handleUpdateReportDetail">{{ t('common.save') }}</a-button>
    <a-button type="secondary" @click="handleCancel">{{ t('common.cancel') }}</a-button>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useVModel } from '@vueuse/core';

  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';

  import { editorUploadFile } from '@/api/modules/test-plan/report';
  import { ReportPlanPreviewImageUrl } from '@/api/requrls/test-plan/report';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { hasAnyPermission } from '@/utils/permission';

  import { customValueForm } from '@/models/testPlan/testPlanReport';

  const { t } = useI18n();
  const appStore = useAppStore();
  const props = defineProps<{
    customForm: customValueForm;
    canEdit: boolean;
    shareId?: string;
    currentId: string;
  }>();

  const emit = defineEmits<{
    (e: 'updateCustom', formValue: customValueForm): void;
    (e: 'handleClick'): void;
    (e: 'handleSetSave'): void;
    (e: 'cancel'): void;
  }>();

  const innerTextForm = ref<customValueForm>({
    content: '',
    label: '',
    richTextTmpFileIds: [],
  });

  watchEffect(() => {
    innerTextForm.value = { ...props.customForm };
  });

  async function handleUploadImage(file: File) {
    const { data } = await editorUploadFile({
      fileList: [file],
    });
    return data;
  }

  function blurHandler() {
    emit('updateCustom', {
      ...innerTextForm.value,
      label: innerTextForm.value.label || t('report.detail.customDefaultCardName'),
    });
  }
  const msRichTextRef = ref<InstanceType<typeof MsRichText>>();
  function handleRichClick() {
    if (!props.shareId) {
      msRichTextRef.value?.focus();
      emit('handleClick');
    }
  }

  function handleUpdateReportDetail() {
    emit('updateCustom', {
      ...innerTextForm.value,
      label: innerTextForm.value.label || t('report.detail.customDefaultCardName'),
    });
  }

  function handleCancel() {
    emit('cancel');
  }
</script>

<style scoped></style>
