<template>
  <div class="h-full pl-[16px]">
    <div class="mb-[16px] flex items-center justify-between">
      <div class="text-[var(--color-text-4)]">
        {{
          t('ms.minders.commentTotal', {
            num: activeComment === 'caseComment' ? commentList.length : reviewCommentList.length,
          })
        }}
      </div>
      <a-select
        v-model:model-value="activeComment"
        :options="commentTypeOptions"
        class="w-[120px]"
        @change="getAllCommentList"
      ></a-select>
    </div>
    <div class="comment-container">
      <ReviewCommentList
        v-if="activeComment === 'reviewComment' || activeComment === 'executiveComment'"
        :review-comment-list="reviewCommentList"
        :active-comment="activeComment"
        :permissions="['FUNCTIONAL_CASE:READ+COMMENT']"
        show-step-detail-trigger
      />
      <template v-else>
        <MsComment
          :upload-image="handleUploadImage"
          :comment-list="commentList"
          :preview-url="`${PreviewEditorImageUrl}/${appStore.currentProjectId}`"
          @delete="handleDelete"
          @update-or-add="handleUpdateOrAdd"
        />
        <MsEmpty v-if="commentList.length === 0" />
      </template>
    </div>
    <inputComment
      v-if="hasEditPermission"
      ref="commentInputRef"
      v-model:content="content"
      v-model:notice-user-ids="noticeUserIds"
      v-model:filed-ids="uploadFileIds"
      v-permission="['FUNCTIONAL_CASE:READ+COMMENT']"
      :preview-url="`${PreviewEditorImageUrl}/${appStore.currentProjectId}`"
      :is-active="isActive"
      is-show-avatar
      is-use-bottom
      :upload-image="handleUploadImage"
      @publish="publishHandler"
      @cancel="cancelPublish"
    />
  </div>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import MsEmpty from '@/components/pure/ms-empty/index.vue';
  import MsComment from '@/components/business/ms-comment/comment';
  import inputComment from '@/components/business/ms-comment/input.vue';
  import { CommentItem, CommentParams } from '@/components/business/ms-comment/types';
  import ReviewCommentList from '@/views/case-management/caseManagementFeature/components/tabContent/tabComment/reviewCommentList.vue';

  import {
    addOrUpdateCommentList,
    createCommentList,
    deleteCommentList,
    editorUploadFile,
    getCommentList,
    getReviewCommentList,
    getTestPlanExecuteCommentList,
  } from '@/api/modules/case-management/featureCase';
  import { PreviewEditorImageUrl } from '@/api/requrls/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore } from '@/store';
  import { hasAnyPermission } from '@/utils/permission';

  const props = defineProps<{
    activeCase: Record<string, any>;
  }>();

  const { t } = useI18n();
  const { openModal } = useModal();
  const appStore = useAppStore();

  const hasEditPermission = hasAnyPermission(['FUNCTIONAL_CASE:READ+COMMENT']);

  async function handleUploadImage(file: File) {
    const { data } = await editorUploadFile({
      fileList: [file],
    });
    return data;
  }

  const activeComment = ref('caseComment');
  const commentTypeOptions = [
    {
      label: t('caseManagement.featureCase.caseComment'),
      value: 'caseComment',
    },
    {
      label: t('caseManagement.featureCase.reviewComment'),
      value: 'reviewComment',
    },
    {
      label: t('caseManagement.featureCase.executiveReview'),
      value: 'executiveComment',
    },
  ];
  const commentList = ref<CommentItem[]>([]);
  const reviewCommentList = ref<CommentItem[]>([]);

  /**
   * 初始化评论列表
   */
  async function initCommentList() {
    try {
      const result = await getCommentList(props.activeCase.id);
      commentList.value = result;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  /**
   * 初始化评审评论
   */
  async function initReviewCommentList() {
    try {
      const result = await getReviewCommentList(props.activeCase.id);
      reviewCommentList.value = result;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  /**
   * 初始化执行评论
   */
  async function initTestPlanExecuteCommentList() {
    try {
      const result = await getTestPlanExecuteCommentList(props.activeCase.id);
      reviewCommentList.value = result;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  async function getAllCommentList() {
    switch (activeComment.value) {
      case 'caseComment':
        await initCommentList();
        break;
      case 'reviewComment':
        await initReviewCommentList();
        break;
      case 'executiveComment':
        await initTestPlanExecuteCommentList();
        break;
      default:
        break;
    }
  }

  /**
   * 添加或者更新评论
   */
  async function handleUpdateOrAdd(item: CommentParams, cb: (result: boolean) => void) {
    try {
      await addOrUpdateCommentList(item);
      getAllCommentList();
      cb(true);
    } catch (error) {
      cb(false);
      // eslint-disable-next-line no-console
      console.error(error);
    }
  }

  /**
   * 删除评论
   */
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
          // eslint-disable-next-line no-console
          console.error(error);
        }
      },
      hideCancel: false,
    });
  }

  const commentInputRef = ref<InstanceType<typeof inputComment>>();
  const content = ref('');
  const isActive = ref<boolean>(false);
  const uploadFileIds = ref<string[]>([]);
  const noticeUserIds = ref<string[]>([]);

  /**
   * 发布评论
   * @param currentContent 当前评论内容
   */
  async function publishHandler(currentContent: string) {
    try {
      const params: CommentParams = {
        caseId: props.activeCase.id,
        notifier: noticeUserIds.value.join(';'),
        replyUser: '',
        parentId: '',
        content: currentContent,
        event: noticeUserIds.value.join(';') ? 'AT' : 'COMMENT', // 任务事件(仅评论: ’COMMENT‘; 评论并@: ’AT‘; 回复评论/回复并@: ’REPLAY‘;)
        uploadFileIds: uploadFileIds.value,
      };
      await createCommentList(params);
      getAllCommentList();
      Message.success(t('common.publishSuccessfully'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function cancelPublish() {
    isActive.value = !isActive.value;
  }

  onBeforeMount(() => {
    getAllCommentList();
  });
</script>

<style lang="less" scoped>
  :deep(.commentWrapper) {
    right: 0;
  }
  .comment-container {
    .ms-scroll-bar();

    overflow-y: auto;
    height: calc(100% - 130px);
  }
  :deep(.comment-list-item-name) {
    max-width: 130px;
  }
</style>
