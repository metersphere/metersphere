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
    @blur="blurHandler"
  />
  <div :class="`${hasAnyPermission(['PROJECT_TEST_PLAN_REPORT:READ+UPDATE']) && !shareId ? '' : 'cursor-not-allowed'}`">
    <MsRichText
      v-model:raw="innerTextForm.content"
      v-model:filedIds="innerTextForm.richTextTmpFileIds"
      :upload-image="handleUploadImage"
      class="mt-[8px] w-full"
      :editable="props.canEdit"
      @click="handleClick"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';

  import { editorUploadFile } from '@/api/modules/test-plan/report';
  import useDoubleClick from '@/hooks/useDoubleClick';
  import { useI18n } from '@/hooks/useI18n';
  import { hasAnyPermission } from '@/utils/permission';

  import { customValueForm } from '@/models/testPlan/testPlanReport';

  const { t } = useI18n();

  const props = defineProps<{
    customForm: customValueForm;
    canEdit: boolean;
    shareId?: string;
    currentId: string;
  }>();

  const emit = defineEmits<{
    (e: 'updateCustom', formValue: customValueForm): void;
    (e: 'dblclick'): void;
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
  function emitDoubleClick() {
    emit('dblclick');
  }

  const { handleClick } = useDoubleClick(emitDoubleClick);
</script>

<style scoped></style>
