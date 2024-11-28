<template>
  <div
    class="flex w-full"
    :class="{
      'items-center': !isActive,
      'commentWrapper': props.isUseBottom,
    }"
  >
    <div v-if="props.isShowAvatar" class="mr-3 inline-block"> <MsAvatar :avatar="userStore.avatar"></MsAvatar></div>
    <div class="w-full items-center">
      <a-input
        v-if="!isActive"
        :placeholder="t('ms.comment.enterPlaceHolderTip')"
        class="w-full hover:border-[rgb(var(--primary-5))]"
        @click="isActive = true"
      ></a-input>
      <div v-else class="flex flex-col justify-between">
        <MsRichText
          v-if="props.mode === 'rich'"
          v-model:raw="currentContent"
          v-model:commentIds="commentIds"
          v-model:filed-ids="uploadFileIds"
          :upload-image="props.uploadImage"
          :preview-url="props.previewUrl"
          class="w-full"
          :limit-length="1000"
          :auto-height="false"
          placeholder="ms.comment.enterPlaceHolderTip"
        />
        <a-textarea
          v-else
          v-model:model-value="currentContent"
          :placeholder="t('ms.comment.enterPlaceHolderTip')"
        ></a-textarea>
        <div class="mt-4 flex flex-row justify-end gap-[12px]">
          <a-button @click="cancelClick">{{ t('common.cancel') }}</a-button>
          <a-button type="primary" :disabled="isDisabled" @click="publish">{{ t('common.publish') }}</a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import MsAvatar from '@/components/pure/ms-avatar/index.vue';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { useUserStore } from '@/store';

  defineOptions({ name: 'MsCommentInput' });

  const { t } = useI18n();

  const props = withDefaults(
    defineProps<{
      mode?: 'rich' | 'textarea';
      isShowAvatar: boolean; // 是否显示评论人头像
      isUseBottom: boolean; // 是否被用于底部
      uploadImage?: (file: File) => Promise<any>;
      previewUrl?: string;
    }>(),
    {
      mode: 'rich',
    }
  );

  const currentContent = defineModel<string>('defaultValue', { default: '' });
  const commentIds = defineModel<string[]>('noticeUserIds', { default: [] });
  const uploadFileIds = defineModel<string[]>('filedIds', { default: [] });
  const userStore = useUserStore();
  const emit = defineEmits<{
    (event: 'publish', value: string): void;
    (event: 'cancel'): void;
  }>();
  const isActive = ref(false);
  const publish = () => {
    emit('publish', currentContent.value);
    isActive.value = false;
    currentContent.value = '';
  };
  const cancelClick = () => {
    isActive.value = false;
    currentContent.value = '';
    emit('cancel');
  };

  function handleGlobalKeyDown(event: any) {
    if (event.key === 'Enter' && event.ctrlKey) {
      if (currentContent.value.trim().length) {
        emit('publish', currentContent.value);
        isActive.value = false;
        currentContent.value = '';
      }
    }
  }

  onMounted(() => {
    window.addEventListener('keydown', handleGlobalKeyDown);
  });
  onBeforeUnmount(() => {
    window.removeEventListener('keydown', handleGlobalKeyDown);
  });

  const isDisabled = computed(() => {
    // 此处如果富文本输入内容后就算手动清空，还是会触发文本行内容为<p style=""></p>
    return !currentContent.value || currentContent.value === '<p style=""></p>';
  });

  defineExpose({
    isActive,
  });
</script>

<style scoped lang="less">
  .commentWrapper {
    z-index: 101;
    background-color: var(--color-text-fff);
    box-shadow: 1px -1px 4px rgba(2 2 2 / 10%);
    @apply absolute bottom-0 w-full px-4 py-4;
  }
  :deep(.rich-wrapper) {
    .halo-rich-text-editor {
      padding: 8px !important;
    }
  }
</style>
