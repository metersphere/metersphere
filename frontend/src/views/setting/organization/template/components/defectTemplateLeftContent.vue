<template>
  <a-form ref="viewFormRef" class="rounded-[4px]" :model="viewForm" layout="vertical">
    <a-form-item
      field="defectName"
      :label="t('system.orgTemplate.defectName')"
      :rules="[{ required: true, message: t('system.orgTemplate.defectNamePlaceholder') }]"
      required
      asterisk-position="end"
    >
      <a-input
        v-model="viewForm.name"
        :disabled="true"
        :max-length="255"
        :placeholder="t('system.orgTemplate.defectNamePlaceholder')"
        show-word-limit
        allow-clear
      ></a-input>
    </a-form-item>
    <a-form-item field="precondition" :label="t('system.orgTemplate.defectContent')" asterisk-position="end">
      <MsRichText v-model:raw="viewForm.description" />
    </a-form-item>
    <a-form-item field="attachment" label="添加附件">
      <div class="flex flex-col">
        <div class="mb-1"
          ><a-button type="outline" :disabled="true">
            <template #icon> <icon-plus class="text-[14px]" /> </template
            >{{ t('system.orgTemplate.addAttachment') }}</a-button
          >
        </div>
        <div class="text-[var(--color-text-4)]">{{ t('system.orgTemplate.addAttachmentTip') }}</div>
      </div>
    </a-form-item>
  </a-form>
</template>

<script setup lang="ts">
  /**
   * @description 系统管理-组织-模板管理-缺陷模板左侧内容
   */
  import { ref } from 'vue';

  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
  const viewForm = ref<Record<string, any>>({
    name: '',
    description: '',
  });
  const props = defineProps<{
    defectForm: Record<string, any>;
  }>();

  watchEffect(() => {
    viewForm.value = { ...props.defectForm };
  });
</script>

<style scoped></style>
