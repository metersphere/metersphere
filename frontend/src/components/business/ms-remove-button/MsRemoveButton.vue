<template>
  <span>
    <MsPopconfirm
      type="error"
      :title="props.title"
      :sub-title-tip="props.subTitleTip"
      :loading="props.loading"
      :visible="currentVisible"
      :ok-text="props.okText"
      @confirm="handleOk"
      @cancel="handleCancel"
    >
      <slot>
        <MsButton :disabled="props.disabled" @click="showPopover">{{ t(props.removeText) }}</MsButton>
      </slot>
    </MsPopconfirm>
  </span>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsPopconfirm from '@/components/pure/ms-popconfirm/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  const props = withDefaults(
    defineProps<{
      title: string;
      subTitleTip: string;
      loading?: boolean;
      removeText?: string;
      okText?: string;
      disabled?: boolean;
    }>(),
    {
      removeText: 'common.remove',
      disabled: false,
    }
  );

  const emit = defineEmits<{
    (e: 'ok'): void;
  }>();

  const { t } = useI18n();
  const currentVisible = ref(false);

  const handleOk = () => {
    emit('ok');
  };
  const handleCancel = () => {
    currentVisible.value = false;
  };

  const showPopover = () => {
    currentVisible.value = true;
  };
</script>
