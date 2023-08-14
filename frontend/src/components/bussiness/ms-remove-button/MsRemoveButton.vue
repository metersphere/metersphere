<template>
  <MsPopconfirm
    :ok-text="t('common.remove')"
    :cancel-text="t('common.cancel')"
    type="error"
    :title="props.title"
    :sub-title-tip="props.subTitleTip"
    @confirm="handleOk"
  >
    <MsButton>{{ t('common.remove') }}</MsButton>
  </MsPopconfirm>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsPopconfirm from '@/components/pure/ms-popconfirm/index.vue';

  const props = defineProps<{
    title: string;
    subTitleTip: string;
  }>();

  const emit = defineEmits<{
    (e: 'ok'): void;
  }>();

  const handleOk = () => {
    emit('ok');
  };

  const { t } = useI18n();
</script>
