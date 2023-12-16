<template>
  <div class="flex items-center justify-between">
    <div class="font-medium">{{ t('caseManagement.featureCase.commentList') }}</div>
    <div>
      <a-radio-group v-model="activeComment" type="button">
        <a-radio value="caseComment">{{ t('caseManagement.featureCase.caseComment') }}</a-radio>
        <a-radio value="reviewComment">{{ t('caseManagement.featureCase.reviewComment') }}</a-radio>
        <a-radio value="executiveComment">{{ t('caseManagement.featureCase.executiveReview') }}</a-radio>
      </a-radio-group>
    </div>
  </div>
  <!-- <MsComment :comment-list="commentList" /> -->
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsComment from '@/components/business/ms-comment/comment';

  import { getCommentList } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';

  import type { CommentItem } from '@/models/caseManagement/featureCase';

  const { t } = useI18n();

  const props = defineProps<{
    caseId: string;
  }>();

  const activeComment = ref('caseComment');

  const content = ref<string>('');

  const commentList = ref<CommentItem[]>([]);

  // 发布评论
  function publishHandler() {}

  // 初始化评论列表
  async function initCommentList() {
    try {
      commentList.value = await getCommentList(props.caseId);
    } catch (error) {
      console.log(error);
    }
  }

  onBeforeMount(() => {
    initCommentList();
  });
</script>

<style scoped></style>
