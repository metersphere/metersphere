<template>
  <div class="flex flex-col">
    <div class="h-[40px] w-[40px] gap-[8px] rounded-full">
      <img
        src="https://p6-passport.byteacctimg.com/img/user-avatar/9a6e39ea689600e70175649a8cd14913~200x200.awebp"
        alt="User avatar"
      />
    </div>
    <div class="flex flex-col">
      <div class="text-[var(--color-text-1)]">{{ props.element.createUser }}</div>
      <div v-dompurify-html="props.element.content" class="mt-[4px]"></div>
      <div class="mt-[16px] flex flex-row items-center">
        <div class="text-[var(--color-text-4)]">{{
          dayjs(props.element.updateTime).format('YYYY-MM-DD HH:mm:ss')
        }}</div>
        <div class="ml-[24px] flex flex-row gap-[16px]">
          <div class="comment-btn" @click="expendChange">
            <MsIconfont type="icon-icon_comment_outlined" />
            <span>{{ !expendComment ? t('comment.expendComment') : t('comment.collapseComment') }}</span>
            <span class="text-[var(--color-text-4)]">({{ element.children?.length }})</span>
          </div>
          <div class="comment-btn" @click="replyClick">
            <MsIconfont type="icon-icon_reply" />
            <span>{{ t('comment.reply') }}</span>
          </div>
          <div class="comment-btn" @click="editClick">
            <MsIconfont type="icon-icon_edit_outlined" />
            <span>{{ t('comment.edit') }}</span>
          </div>
          <div class="comment-btn" @click="deleteClick">
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

  import MsIconfont from '@/components/pure/ms-icon-font/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  export interface CommentItem {
    id: string; // 评论id
    bugId: string; // bug id
    createUser: string; // 创建人
    updateTime: number; // 更新时间
    content: string;
    replyUser?: string; // 回复人
    notifier?: string; // 通知人
    children?: CommentItem[];
  }
  // 仅评论: ’COMMENT‘; 评论并@: ’AT‘; 回复评论/回复并@: ’REPLAY‘;)
  export type commentEvent = 'COMMENT' | 'AT' | 'REPLAY';

  const props = defineProps<{
    element: CommentItem;
  }>();

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
