<template>
  <div class="flex flex-row gap-[8px]">
    <div class="p-1"> <MsAvatar avatar="word" /></div>
    <div class="flex w-full flex-col">
      <div class="font-medium text-[var(--color-text-1)]">{{ props.element.createUser }}</div>
      <div v-dompurify-html="props.element.content" class="markdown-body mt-[4px]"></div>

      <div class="mb-4 mt-[16px] flex flex-row items-center">
        <div class="text-[var(--color-text-4)]">{{
          dayjs(props.element.updateTime).format('YYYY-MM-DD HH:mm:ss')
        }}</div>
        <div class="ml-[24px] flex flex-row gap-[16px]">
          <div
            v-if="props.mode === 'parent' && element.childComments?.length"
            class="comment-btn"
            @click="expendChange"
          >
            <MsIconfont type="icon-icon_comment_outlined" />
            <span>{{ !expendComment ? t('ms.comment.expendComment') : t('ms.comment.collapseComment') }}</span>
            <span class="text-[var(--color-text-4)]">({{ element.childComments?.length }})</span>
          </div>
          <div
            class="comment-btn hover:bg-[var(--color-bg-3)]"
            :class="{ 'bg-[var(--color-text-n8)]': status === 'reply' }"
            @click="replyClick"
          >
            <MsIconfont type="icon-icon_reply" />
            <span>{{ t('ms.comment.reply') }}</span>
          </div>
          <div
            v-if="hasEditAuth"
            class="comment-btn hover:bg-[var(--color-bg-3)]"
            :class="{ 'bg-[var(--color-text-n8)]': status === 'edit' }"
            @click="editClick"
          >
            <MsIconfont type="icon-icon_edit_outlined" />
            <span>{{ t('ms.comment.edit') }}</span>
          </div>
          <div
            class="comment-btn hover:bg-[rgb(var(--danger-1))]"
            :class="{ 'bg-[rgb(var(--danger-2))]': status === 'delete' }"
            @click="deleteClick"
          >
            <MsIconfont type="icon-icon_delete-trash_outlined" />
            <span>{{ t('ms.comment.delete') }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { defineModel, ref } from 'vue';
  import dayjs from 'dayjs';

  import MsAvatar from '@/components/pure/ms-avatar/index.vue';
  import MsIconfont from '@/components/pure/ms-icon-font/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useUserStore from '@/store/modules/user/index';

  import { CommentItem } from './types';

  const userStore = useUserStore();
  const { t } = useI18n();

  defineOptions({ name: 'MsCommentItem' });

  const props = defineProps<{
    element: CommentItem; // 评论的具体内容
    mode: 'parent' | 'child'; // 父级评论还是子级评论
    onReply?: () => void; // 回复
    onEdit?: () => void; // 编辑
    onDelete?: () => void; // 删除
  }>();

  // 是否拥有编辑｜删除权限
  const hasEditAuth = computed(() => {
    return props.element.createUser === userStore.id;
  });

  const status = defineModel<'normal' | 'edit' | 'reply' | 'delete'>('status', { default: 'normal' });

  const emit = defineEmits<{
    (event: 'reply'): void;
    (event: 'edit'): void;
    (event: 'delete'): void;
    (event: 'expend', value: boolean): void;
  }>();

  const expendComment = ref(false);

  const expendChange = () => {
    expendComment.value = !expendComment.value;
    emit('expend', expendComment.value);
  };
  const replyClick = () => {
    emit('reply');
    status.value = 'reply';
  };

  const editClick = () => {
    emit('edit');
    status.value = 'edit';
  };

  const deleteClick = () => {
    emit('delete');
    status.value = 'delete';
  };
</script>

<style lang="less" scoped>
  .comment-btn {
    display: flex;
    align-items: center;
    padding: 2px 8px;
    border-radius: 4px;
    color: var(--color-text-1);
    flex-direction: row;
    gap: 4px;
    cursor: pointer;
  }
</style>
