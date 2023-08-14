<template>
  <MsPopconfirm
    v-model:visible="currentVisible"
    :ok-text="t('common.remove')"
    :cancel-text="t('common.cancel')"
    type="error"
    :title="props.title"
    :sub-title-tip="props.subTitleTip"
    @confirm="handleOk"
    @cancel="handleCancel"
  >
    <MsButton @click="showPopconfirm">{{ t('common.remove') }}</MsButton>
  </MsPopconfirm>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';
  import { ref } from 'vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsPopconfirm from '@/components/pure/ms-popconfirm/index.vue';

  const props = defineProps<{
    title: string;
    subTitleTip: string;
  }>();

  const emit = defineEmits<{
    (e: 'ok'): void;
  }>();

  const currentVisible = ref(false);

  const handleCancel = () => {
    currentVisible.value = false;
  };

  const showPopconfirm = () => {
    currentVisible.value = true;
  };

  const handleOk = () => {
    emit('ok');
    currentVisible.value = false;
  };

  const { t } = useI18n();
</script>
