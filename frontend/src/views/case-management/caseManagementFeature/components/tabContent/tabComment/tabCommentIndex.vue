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
  <div>
    <!-- TODO -->
    <MsComment :comment-list="commentList" @delete="handleDelete" @update-or-add="handleUpdate" />
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsComment from '@/components/business/ms-comment/comment';
  import { CommentItem, CommentParams } from '@/components/business/ms-comment/types';

  import { getCommentList } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const props = defineProps<{
    caseId: string;
  }>();

  const activeComment = ref('caseComment');

  const content = ref<string>('');

  const commentList = ref<CommentItem[]>([]);

  // 初始化评论列表
  async function initCommentList() {
    try {
      const result = await getCommentList(props.caseId);
      commentList.value = result.map((item) => {
        return {
          ...item,
        };
      });
    } catch (error) {
      console.log(error);
    }
  }

  const currentUserId = ref('');

  // 添加或者更新评论
  function handleUpdateOrAdd() {}

  // 删除评论
  function handleDelete() {}

  // 更新评论
  function handleUpdate() {}

  onBeforeMount(() => {
    initCommentList();
  });
</script>

<style scoped></style>
