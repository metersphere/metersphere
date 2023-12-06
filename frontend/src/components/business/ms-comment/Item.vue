<template>
  <div class="flex flex-col">
    <MsAvatar avatar="default" />
    <div class="flex flex-col">
      <div class="text-[var(--color-text-1)]">{{ props.element.createUser }}</div>
      <div v-dompurify-html="props.element.content" class="mt-[4px]"></div>
      <div class="mt-[16px] flex flex-row items-center">
        <div class="text-[var(--color-text-4)]">{{
          dayjs(props.element.updateTime).format('YYYY-MM-DD HH:mm:ss')
        }}</div>
        <div class="ml-[24px] flex flex-row gap-[16px]">
          <div v-if="props.mode === 'parent'" class="comment-btn" @click="expendChange">
            <MsIconfont type="icon-icon_comment_outlined" />
            <span>{{ !expendComment ? t('comment.expendComment') : t('comment.collapseComment') }}</span>
            <span class="text-[var(--color-text-4)]">({{ element.children?.length }})</span>
          </div>
          <div class="comment-btn" @click="replyClick">
            <MsIconfont type="icon-icon_reply" />
            <span>{{ t('comment.reply') }}</span>
          </div>
          <div v-if="hasEditAuth" class="comment-btn" @click="editClick">
            <MsIconfont type="icon-icon_edit_outlined" />
            <span>{{ t('comment.edit') }}</span>
          </div>
          <div v-if="hasEditAuth" class="comment-btn" @click="deleteClick">
            <MsIconfont type="icon-icon_delete-trash_outlined" />
            <span>{{ t('comment.delete') }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import dayjs from 'dayjs';

  import MsAvatar from '@/components/pure/ms-avatar/index.vue';
  import MsIconfont from '@/components/pure/ms-icon-font/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { CommentItem } from './types';

  const props = defineProps<{
    element: CommentItem; // 评论的具体内容
    mode: 'parent' | 'child'; // 父级评论还是子级评论
    currentUserId: string; // 当前用户id
  }>();

  // 是否拥有编辑｜删除权限
  const hasEditAuth = computed(() => {
    return props.element.commentUserInfo.id === props.currentUserId;
  });

  const emit = defineEmits<{
    (event: 'reply'): void;
    (event: 'edit'): void;
    (event: 'delete'): void;
  }>();

  const expendComment = ref(false);
  const isEdit = ref(false);

  const expendChange = () => {
    expendComment.value = !expendComment.value;
  };
  const replyClick = () => {
    emit('reply');
  };

  const editClick = () => {
    isEdit.value = true;
    emit('edit');
  };

  const deleteClick = () => {
    emit('delete');
  };

  const { t } = useI18n();
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
    :hover {
      background-color: var(--color-bg-2);
    }
  }
</style>
