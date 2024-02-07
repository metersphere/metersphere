<template>
  <a-modal
    v-model:visible="innerVisible"
    class="ms-modal-no-padding"
    width="100%"
    title-align="start"
    unmount-on-close
    :footer="false"
    modal-class="modalSelf"
    :body-style="{
      height: '100%',
    }"
    :modal-style="{
      padding: '16px 16px 0',
    }"
    esc-to-close
    fullscreen
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <template #title> {{ t('settings.navbar.task') }}</template>

    <div class="divider h-full">
      <TaskCenter group="system" mode="modal"></TaskCenter>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useVModel } from '@vueuse/core';

  import TaskCenter from '@/views/project-management/taskCenter/component/taskCom.vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'update:visible', visible: boolean): void;
  }>();

  const innerVisible = useVModel(props, 'visible', emit);

  function handleOk() {}
  function handleCancel() {
    innerVisible.value = false;
  }
</script>

<style scoped lang="less">
  .divider {
    border-top: 1px solid var(--color-text-n8);
  }
</style>
