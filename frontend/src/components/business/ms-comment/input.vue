<template>
  <div
    class="flex w-full"
    :class="{
      'items-center': !isActive,
      'commentWrapper': props.isUseBottom,
    }"
  >
    <div v-if="props.isShowAvatar" class="mr-3 inline-block"> <MsAvatar avatar="word"></MsAvatar></div>
    <div class="w-full items-center">
      <a-input v-if="!isActive" class="w-full" @click="isActive = true"></a-input>
      <div v-else class="flex flex-col justify-between">
        <MsRichText v-model:raw="currentContent" v-model:commentIds="commentIds" class="w-full" />
        <div class="mt-4 flex flex-row justify-end gap-[12px]">
          <a-button @click="cancelClick">{{ t('common.cancel') }}</a-button>
          <a-button type="primary" :disabled="!currentContent" @click="publish">{{ t('common.publish') }}</a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { useVModel } from '@vueuse/core';

  import MsAvatar from '@/components/pure/ms-avatar/index.vue';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';

  import { useI18n } from '@/hooks/useI18n';

  defineOptions({ name: 'MsCommentInput' });

  const { t } = useI18n();

  // const currentContent = defineModel<string>('content', { required: true });

  const props = defineProps<{
    isShowAvatar: boolean; // 是否显示评论人头像
    isUseBottom: boolean; // 是否被用于底部
    defaultValue?: string; // 默认值
    noticeUserIds?: string[]; // 评论人id列表
  }>();

  const currentContent = ref(props.defaultValue || '');

  const emit = defineEmits<{
    (event: 'publish', value: string): void;
    (event: 'cancel'): void;
  }>();

  const isActive = ref(false);
  const commentIds = useVModel(props, 'noticeUserIds', emit);

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
</script>

<style scoped lang="less">
  .commentWrapper {
    box-shadow: 1px -1px 4px rgba(2 2 2 / 10%);
    @apply absolute bottom-0 w-full bg-white px-4 py-4;
  }
</style>
