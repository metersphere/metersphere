<template>
  <div class="mb-4 flex items-center justify-between">
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
    <div v-show="activeComment === 'caseComment'">
      <MsComment :comment-list="commentList" @delete="handleDelete" @update-or-add="handleUpdateOrAdd" />
      <MsEmpty v-if="commentList.length === 0" />
    </div>

    <!-- 评审评论 -->
    <div v-show="activeComment === 'reviewComment'" class="flex flex-1 flex-col overflow-hidden">
      <div class="review-history-list">
        <div v-for="item of reviewCommentList" :key="item.id" class="review-history-list-item">
          <div class="flex items-center">
            <MSAvatar :avatar="item.userLogo" />
            <div class="ml-[8px] flex items-center">
              <div class="font-medium text-[var(--color-text-1)]">{{ item.userName }}</div>
              <a-divider direction="vertical" margin="8px"></a-divider>
              <div v-if="item.status === 'PASS'" class="flex items-center">
                <MsIcon type="icon-icon_succeed_filled" class="mr-[4px] text-[rgb(var(--success-6))]" />
                {{ t('caseManagement.caseReview.pass') }}
              </div>
              <div v-else-if="item.status === 'UN_PASS'" class="flex items-center">
                <MsIcon type="icon-icon_close_filled" class="mr-[4px] text-[rgb(var(--danger-6))]" />
                {{ t('caseManagement.caseReview.fail') }}
              </div>
              <div v-else-if="item.status === 'UNDER_REVIEWED'" class="flex items-center">
                <MsIcon type="icon-icon_warning_filled" class="mr-[4px] text-[rgb(var(--warning-6))]" />
                {{ t('caseManagement.caseReview.suggestion') }}
              </div>
              <div v-else-if="item.status === 'RE_REVIEWED'" class="flex items-center">
                <MsIcon type="icon-icon_resubmit_filled" class="mr-[4px] text-[rgb(var(--warning-6))]" />
                {{ t('caseManagement.caseReview.reReview') }}
              </div>
            </div>
          </div>
          <div class="markdown-body" style="margin-left: 48px" v-html="item.contentText"></div>
          <div class="ml-[48px] mt-[8px] text-[var(--color-text-4)]">
            {{ dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss') }}
            <span v-if="item.deleted" class="ml-[16px]">
              {{ item.reviewName }}
            </span>
            <span v-else class="ml-[16px] cursor-pointer text-[rgb(var(--primary-5))]" @click="review(item)">
              {{ item.reviewName }}
            </span>
          </div>
        </div>
        <MsEmpty v-if="reviewCommentList.length === 0" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MSAvatar from '@/components/pure/ms-avatar/index.vue';
  import MsEmpty from '@/components/pure/ms-empty/index.vue';
  import MsComment from '@/components/business/ms-comment';
  import { CommentItem, CommentParams } from '@/components/business/ms-comment/types';

  import {
    addOrUpdateCommentList,
    deleteCommentList,
    getCommentList,
    getReviewCommentList,
  } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';

  import { CaseManagementRouteEnum } from '@/enums/routeEnum';

  const featureCaseStore = useFeatureCaseStore();
  const router = useRouter();
  const route = useRoute();
  // const activeTab = computed(() => featureCaseStore.activeTab);
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
        await initCommentList();
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

  watch(
    () => activeComment.value,
    (val) => {
      if (val) {
        getAllCommentList();
      }
    }
  );
  // watch(
  //   () => activeTab.value,
  //   (val) => {
  //     if (val === 'comments') {
  //       getAllCommentList();
  //     }
  //   }
  // );

  onMounted(() => {
    getAllCommentList();
  });

  defineExpose({
    getAllCommentList,
  });
</script>

<style scoped></style>
