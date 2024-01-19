<template>
  <a-modal v-model:visible="uploadVisible" class="ms-modal-form ms-modal-small" title-align="start">
    <template #title> {{ t('editor.attachment') }} </template>
    <MsUpload
      v-model:file-list="fileList"
      class="w-full"
      accept="none"
      :is-limit="false"
      :show-sub-text="false"
      :show-file-list="false"
      :auto-upload="false"
    />
    <template #footer>
      <div class="flex flex-row justify-end gap-[8px]"
        ><a-button type="secondary" @click="handleCancel">
          {{ t('common.cancel') }}
        </a-button>
        <a-button type="primary" :loading="confirmLoading" @click="handleConfirm">
          {{ t('common.confirm') }}
        </a-button></div
      >
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsUpload from '@/components/pure/ms-upload/index.vue';
  import type { MsFileItem } from '@/components/pure/ms-upload/types';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
  }>();

  const emits = defineEmits<{
    (e: 'update:visible', value: boolean): void;
    (event: 'close'): void;
    (event: 'select', attachments: MsFileItem[]): void;
  }>();

  const uploadVisible = computed({
    get() {
      return props.visible;
    },
    set(val) {
      emits('update:visible', val);
    },
  });

  const fileList = ref<MsFileItem[]>([]);

  const confirmLoading = ref<boolean>(false);

  function handleConfirm() {
    emits('select', fileList.value);
  }

  function handleCancel() {
    fileList.value = [];
    uploadVisible.value = false;
  }
</script>

<style scoped></style>
