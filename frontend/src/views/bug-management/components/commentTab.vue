<template>
  <div>
    <MsEmpty v-if="commentList.length === 0" />
    <MsComment
      v-else
      :preview-url="`${EditorPreviewFileUrl}/${appStore.currentProjectId}`"
      :comment-list="commentList"
      :upload-image="handleUploadImage"
      :permissions="['PROJECT_BUG:READ+COMMENT']"
      @delete="handleDelete"
      @update-or-add="handleUpdate"
    />
  </div>
</template>

<script lang="ts" setup>
  import MsEmpty from '@/components/pure/ms-empty/index.vue';
  import MsComment from '@/components/business/ms-comment/comment';
  import { CommentItem, CommentParams } from '@/components/business/ms-comment/types';

  import {
    createOrUpdateComment,
    deleteComment,
    editorUploadFile,
    getCommentList,
  } from '@/api/modules/bug-management/index';
  import { EditorPreviewFileUrl } from '@/api/requrls/bug-management';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore } from '@/store';

  import message from '@arco-design/web-vue/es/message';

  const { openModal } = useModal();

  const props = defineProps<{
    bugId: string;
  }>();
  const { t } = useI18n();
  const appStore = useAppStore();

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

  async function handleUploadImage(file: File) {
    const { data } = await editorUploadFile({
      fileList: [file],
    });
    return data;
  }

  onMounted(() => {
    if (props.bugId) {
      initData(props.bugId);
    }
  });

  watch(
    () => props.bugId,
    (val) => {
      if (val) {
        initData(props.bugId);
      }
    }
  );

  defineExpose({
    initData,
  });
</script>

<style lang="less" scoped>
  /* Your component styles here */
</style>
