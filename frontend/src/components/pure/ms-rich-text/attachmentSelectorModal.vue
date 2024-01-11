<template>
  <a-modal v-model:visible="uploadVisible" class="ms-modal-form ms-modal-small" title-align="start">
    <template #title> {{ t('editor.attachment') }} </template>
    <MsUpload
      v-model:file-list="fileList"
      class="w-full"
      accept="none"
      :max-size="50"
      size-unit="MB"
      main-text="system.user.importModalDragText"
      :sub-text="t('system.plugin.supportFormat')"
      :show-file-list="false"
      :auto-upload="false"
      :disabled="confirmLoading"
    ></MsUpload>
    <template #footer> </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsUpload from '@/components/pure/ms-upload/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { FileItem } from '@arco-design/web-vue';

  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
  }>();

  const emits = defineEmits<{
    (e: 'update:visible', value: boolean): void;
  }>();

  const uploadVisible = computed({
    get() {
      return props.visible;
    },
    set(val) {
      emits('update:visible', val);
    },
  });

  const fileList = ref<FileItem[]>([]);

  const confirmLoading = ref<boolean>(false);
</script>

<style scoped></style>
