<template>
  <div class="mb-4 flex items-center justify-between break-words break-all">
    <div class="font-medium">{{ t('caseManagement.featureCase.commentList') }}</div>
    <a-radio-group v-model="activeComment" type="button">
      <a-radio value="caseComment">{{ t('caseManagement.featureCase.caseComment') }}</a-radio>
      <a-radio value="reviewComment">{{ t('caseManagement.featureCase.reviewComment') }}</a-radio>
      <a-radio value="executiveComment">{{ t('caseManagement.featureCase.executiveReview') }}</a-radio>
    </a-radio-group>
  </div>
  <div>
    <!-- 用例评论 -->
    <div v-show="activeComment === 'caseComment'">
      <MsComment
        :upload-image="handleUploadImage"
        :comment-list="commentList"
        :preview-url="PreviewEditorImageUrl"
        :permissions="['FUNCTIONAL_CASE:READ+COMMENT']"
        @delete="handleDelete"
        @update-or-add="handleUpdateOrAdd"
      />
      <MsEmpty v-if="commentList.length === 0" />
    </div>

    <!-- 评审评论 -->
    <ReviewCommentList
      v-show="activeComment === 'reviewComment' || activeComment === 'executiveComment'"
      :review-comment-list="reviewCommentList"
      :active-comment="activeComment"
      show-step-detail-trigger
    />
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';

  import MsEmpty from '@/components/pure/ms-empty/index.vue';
  import MsComment from '@/components/business/ms-comment/comment';
  import { CommentItem, CommentParams } from '@/components/business/ms-comment/types';
  import ReviewCommentList from './reviewCommentList.vue';

  import {
    addOrUpdateCommentList,
    deleteCommentList,
    editorUploadFile,
    getCommentList,
    getReviewCommentList,
    getTestPlanExecuteCommentList,
  } from '@/api/modules/case-management/featureCase';
  import { PreviewEditorImageUrl } from '@/api/requrls/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';

  import { CaseManagementRouteEnum, TestPlanRouteEnum } from '@/enums/routeEnum';

  const featureCaseStore = useFeatureCaseStore();
  const router = useRouter();
  const route = useRoute();
  const { openModal } = useModal();
  const { t } = useI18n();

  const props = defineProps<{
    caseId: string;
    commentValue: { id: string; name: string }[];
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

  // 初始化执行评论
  async function initTestPlanExecuteCommentList() {
    try {
      const result = await getTestPlanExecuteCommentList(props.caseId);
      reviewCommentList.value = result;
    } catch (error) {
      console.log(error);
    }
  }

  async function getAllCommentList() {
    switch (activeComment.value) {
      case 'caseComment':
        await initCommentList();
        featureCaseStore.getCaseCounts(props.caseId);
        break;
      case 'reviewComment':
        await initReviewCommentList();
        featureCaseStore.getCaseCounts(props.caseId);
        break;
      case 'executiveComment':
        await initTestPlanExecuteCommentList();
        featureCaseStore.getCaseCounts(props.caseId);
        break;
      default:
        break;
    }
  }

  // 添加或者更新评论
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

  // 去用例评审页面
  function review(record: CommentItem) {
    router.push({
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL_CASE_DETAIL,
      query: {
        ...route.query,
        caseId: record.caseId,
        id: record.reviewId,
      },
      state: {
        params: JSON.stringify(record.moduleName),
      },
    });
  }

  // 去测试计划页面
  function toPlan(record: CommentItem) {
    router.push({
      name: TestPlanRouteEnum.TEST_PLAN_INDEX_DETAIL,
      query: {
        ...route.query,
        id: record.testPlanId,
      },
      state: {
        params: JSON.stringify(record.moduleName),
      },
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

  async function handleUploadImage(file: File) {
    const { data } = await editorUploadFile({
      fileList: [file],
    });
    return data;
  }

  watch(
    () => props.caseId,
    (val) => {
      if (val) {
        getAllCommentList();
      }
    }
  );

  onMounted(() => {
    getAllCommentList();
  });

  defineExpose({
    getAllCommentList,
  });

  onBeforeMount(() => {
    activeComment.value = props.commentValue.find((item) => Number(item.name) > 0)?.id || 'caseComment';
  });
</script>

<style scoped lang="less">
  #magnifier {
    position: absolute;
    display: none;
    width: 200px;
    height: 200px;
    border: 1px solid red;
    background-size: contain;
    pointer-events: none;
  }
</style>
