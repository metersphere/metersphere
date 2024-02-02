<template>
  <a-form ref="dialogFormRef" :model="caseResultForm" layout="vertical">
    <a-form-item field="reason" :label="t('caseManagement.caseReview.reviewResult')" class="mb-[8px]">
      <a-radio-group v-model:model-value="caseResultForm.result" @change="() => dialogFormRef?.resetFields()">
        <a-radio value="PASS">
          <div class="inline-flex items-center">
            <MsIcon type="icon-icon_succeed_filled" class="mr-[4px] text-[rgb(var(--success-6))]" />
            {{ t('caseManagement.caseReview.pass') }}
          </div>
        </a-radio>
        <a-radio value="UN_PASS">
          <div class="inline-flex items-center">
            <MsIcon type="icon-icon_close_filled" class="mr-[4px] text-[rgb(var(--danger-6))]" />
            {{ t('caseManagement.caseReview.fail') }}
          </div>
        </a-radio>
        <a-radio value="UNDER_REVIEWED">
          <div class="inline-flex items-center">
            <MsIcon type="icon-icon_warning_filled" class="mr-[4px] text-[rgb(var(--warning-6))]" />
            {{ t('caseManagement.caseReview.suggestion') }}
          </div>
          <a-tooltip :content="t('caseManagement.caseReview.suggestionTip')" position="right">
            <icon-question-circle
              class="ml-[4px] mt-[2px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              size="16"
            />
          </a-tooltip>
        </a-radio>
      </a-radio-group>
    </a-form-item>
    <a-form-item
      field="reason"
      :label="t('caseManagement.caseReview.reason')"
      :rules="
        caseResultForm.result !== 'PASS'
          ? [{ required: true, message: t('caseManagement.caseReview.reasonRequired') }]
          : []
      "
      asterisk-position="end"
      class="mb-0"
    >
      <div class="flex w-full items-center">
        <a-mention
          v-model:model-value="caseResultForm.reason"
          type="textarea"
          :auto-size="{ minRows: 1 }"
          :max-length="1000"
          allow-clear
          class="flex flex-1 items-center"
        />
        <MsUpload
          v-model:file-list="caseResultForm.fileList"
          accept="image"
          size-unit="MB"
          :auto-upload="false"
          :limit="10"
          multiple
        >
          <a-button type="outline" class="ml-[8px] p-[8px_6px]">
            <icon-file-image :size="18" />
          </a-button>
        </MsUpload>
      </div>
      <MsFileList
        v-model:file-list="caseResultForm.fileList"
        show-mode="imageList"
        :show-tab="false"
        class="mt-[8px]"
      />
    </a-form-item>
  </a-form>
</template>

<script setup lang="ts">
  import { FormInstance } from '@arco-design/web-vue';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsFileList from '@/components/pure/ms-upload/fileList.vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';
  import { MsFileItem } from '@/components/pure/ms-upload/types';

  import { useI18n } from '@/hooks/useI18n';

  import { ReviewResult } from '@/models/caseManagement/caseReview';

  const { t } = useI18n();

  const dialogFormRef = ref<FormInstance>();
  const caseResultForm = ref({
    result: 'PASS' as ReviewResult,
    reason: '',
    fileList: [] as MsFileItem[],
  });

  function validateForm(cb: (form: Record<string, any>) => void) {
    dialogFormRef.value?.validate((errors) => {
      if (!errors && typeof cb === 'function') {
        cb(caseResultForm.value);
      }
    });
  }

  defineExpose({
    validateForm,
  });
</script>

<style lang="less" scoped>
  .image-preview-container {
    margin-top: 8px;
  }
</style>
