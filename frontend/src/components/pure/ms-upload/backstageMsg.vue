<template>
  <div class="flex flex-wrap items-center">
    <div class="mr-[8px]">{{ props.content }}</div>
    <MsButton v-if="props.showDetail" @click="goDetail">{{ t('ms.upload.detail') }}</MsButton>
  </div>
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  const props = withDefaults(
    defineProps<{
      mode?: 'message' | 'notification';
      content: string;
      showDetail?: boolean;
    }>(),
    {
      mode: 'message',
      showDetail: true,
    }
  );
  const emit = defineEmits(['goDetail']);
  const { t } = useI18n();

  function goDetail() {
    emit('goDetail');
  }
</script>
