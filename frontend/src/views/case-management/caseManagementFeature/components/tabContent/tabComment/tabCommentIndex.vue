<template>
  <div class="flex items-center justify-between">
    <div class="font-medium">{{ t('caseManagement.featureCase.commentList') }}</div>
    <div>
      <a-radio-group v-model="activeComment" type="button">
        <a-radio value="caseComment">{{ t('caseManagement.featureCase.caseComment') }}</a-radio>
        <a-radio value="reviewComment">{{ t('caseManagement.featureCase.reviewComment') }}</a-radio>
        <!-- <a-radio value="executiveComment">{{ t('caseManagement.featureCase.executiveReview') }}</a-radio> -->
      </a-radio-group>
    </div>
  </div>
  <div>
    <!-- 用例评论 -->
    <MsComment
      v-if="activeComment === 'caseComment'"
      :comment-list="commentList"
      @delete="handleDelete"
      @update-or-add="handleUpdateOrAdd"
    />
    <!-- 评审评论 -->
    <MsComment
      v-else-if="activeComment === 'reviewComment'"
      :comment-list="reviewCommentList"
      @delete="handleDelete"
      @update-or-add="handleUpdateOrAdd"
    />
    <!-- 执行评论 -->
    <!-- <MsComment v-else :comment-list="commentList" @delete="handleDelete" @update-or-add="handleUpdateOrAdd" /> -->
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsComment from '@/components/business/ms-comment/comment';
  import { CommentItem, CommentParams } from '@/components/business/ms-comment/types';

  import {
    deleteCommentList,
    getCommentList,
    getReviewCommentList,
    updateCommentList,
  } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';

  const featureCaseStore = useFeatureCaseStore();
  const { openModal } = useModal();
  const { t } = useI18n();

  const props = defineProps<{
    caseId: string;
  }>();

  const activeComment = ref('caseComment');

  const commentList = ref<CommentItem[]>([]);
  const reviewCommentList = ref<CommentItem[]>([]);

  // 初始化评论列表
  async function initCommentList() {
    try {
      const result = await getCommentList(props.caseId);
      commentList.value = result;
    } catch (error) {
      console.log(error);
    }
  }
  // 初始化评审评论
  async function initReviewCommentList() {
    try {
      const result = await getReviewCommentList(props.caseId);
      reviewCommentList.value = result;
    } catch (error) {
      console.log(error);
    }
  }

  function setCount(list: CommentItem[]) {
    featureCaseStore.setListCount(featureCaseStore.activeTab, list.length);
  }

  async function getAllCommentList() {
    switch (activeComment.value) {
      case 'caseComment':
        await initCommentList();
        setCount(commentList.value);
        break;
      case 'reviewComment':
        await initReviewCommentList();
        setCount(reviewCommentList.value);
        break;
      case 'executiveComment':
        await initCommentList();
        setCount(commentList.value);
        break;
      default:
        break;
    }
  }

  // 添加或者更新评论
  async function handleUpdateOrAdd(item: CommentParams, cb: (result: boolean) => void) {
    try {
      if (item.id) {
        await updateCommentList(item);
      } else {
        await updateCommentList(item);
      }
      getAllCommentList();
      cb(true);
    } catch (error) {
      cb(false);
      // eslint-disable-next-line no-console
      console.error(error);
    }
  }

  // 删除评论
  async function handleDelete(caseCommentId: string) {
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
          await deleteCommentList(caseCommentId);
          Message.success(t('common.deleteSuccess'));
          getAllCommentList();
        } catch (error) {
          console.error(error);
        }
      },
      hideCancel: false,
    });
  }

  watch(
    () => activeComment.value,
    (val) => {
      if (val) {
        getAllCommentList();
      }
    }
  );

  onBeforeMount(() => {
    getAllCommentList();
  });

  defineExpose({
    getAllCommentList,
  });
</script>

<style scoped></style>
