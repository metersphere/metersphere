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
          v-model:raw="currentContent"
          v-model:commentIds="commentIds"
          :upload-image="props.uploadImage"
          class="w-full"
          placeholder="ms.comment.enterPlaceHolderTip"
        />
        <div class="mt-4 flex flex-row justify-end gap-[12px]">
          <a-button @click="cancelClick">{{ t('common.cancel') }}</a-button>
          <a-button type="primary" :disabled="!currentContent" @click="publish">{{ t('common.publish') }}</a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { defineModel, ref } from 'vue';

  import MsAvatar from '@/components/pure/ms-avatar/index.vue';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { useUserStore } from '@/store';

  defineOptions({ name: 'MsCommentInput' });

  const { t } = useI18n();

  const props = defineProps<{
    isShowAvatar: boolean; // 是否显示评论人头像
    isUseBottom: boolean; // 是否被用于底部
    uploadImage?: (file: File) => Promise<any>;
  }>();

  const currentContent = defineModel<string>('defaultValue', { default: '' });
  const commentIds = defineModel<string[]>('noticeUserIds', { default: [] });
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
</script>

<style scoped lang="less">
  .commentWrapper {
    box-shadow: 1px -1px 4px rgba(2 2 2 / 10%);
    @apply absolute bottom-0 w-full bg-white px-4 py-4;
  }
</style>
