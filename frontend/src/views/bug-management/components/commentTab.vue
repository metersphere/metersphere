<template>
  <MsComment :comment-list="commentList" @delete="handleDelete" @update-or-add="handleUpdate" />
</template>

<script lang="ts" setup>
  import MsComment from '@/components/business/ms-comment/comment';
  import { CommentItem, CommentParams } from '@/components/business/ms-comment/types';

  import { createOrUpdateComment, deleteComment, getCommentList } from '@/api/modules/bug-management/index';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';

  import message from '@arco-design/web-vue/es/message';

  const { openModal } = useModal();

  const props = defineProps<{
    bugId: string;
  }>();
  const { t } = useI18n();

  const commentList = ref<CommentItem[]>([]);

  const initData = async (bugId: string) => {
    try {
      commentList.value = (await getCommentList(bugId)) || [];
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  };

  const handleDelete = async (commentId: string) => {
    openModal({
      type: 'error',
      title: t('ms.comment.deleteConfirm'),
      content: t('ms.comment.deleteContent'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          await deleteComment(commentId);
          message.success(t('common.deleteSuccess'));
          initData(props.bugId);
        } catch (error) {
          message.error(t('common.deleteFail'));
          // eslint-disable-next-line no-console
          console.error(error);
        }
      },
      hideCancel: false,
    });
  };

  const handleUpdate = async (item: CommentParams, cb: (result: boolean) => void) => {
    try {
      await createOrUpdateComment(item);
      if (props.bugId) {
        initData(props.bugId);
      }
      cb(true);
    } catch (error) {
      cb(false);
      // eslint-disable-next-line no-console
      console.error(error);
    }
  };

  onMounted(() => {
    if (props.bugId) {
      initData(props.bugId);
    }
  });

  defineExpose({
    initData,
  });

  watchEffect(() => {
    initData(props.bugId);
  });
</script>

<style lang="less" scoped>
  /* Your component styles here */
</style>
