<template>
  <div class="w-full">
    <a-input v-if="!isActive" class="w-full" @click="isActive = true"></a-input>
    <div v-else class="flex flex-col">
      <MsRichText v-model="currentContent" class="w-full" />
      <div class="flex flex-row justify-end gap-[12px]">
        <a-button @click="cancelClick">{{ t('common.cancel') }}</a-button>
        <a-button type="primary" :disabled="!content" @click="publish">{{ t('common.publish') }}</a-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';

  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const props = defineProps<{ content: string }>();

  const emit = defineEmits<{
    (event: 'publish', value: string): void;
  }>();

  const isActive = ref(false);
  const currentContent = ref('');

  watchEffect(() => {
    if (props.content) {
      currentContent.value = props.content;
    }
  });

  const publish = () => {
    emit('publish', currentContent.value);
    isActive.value = false;
    currentContent.value = '';
  };
  const cancelClick = () => {
    isActive.value = false;
    currentContent.value = '';
  };
</script>

<style scoped lang="less"></style>
