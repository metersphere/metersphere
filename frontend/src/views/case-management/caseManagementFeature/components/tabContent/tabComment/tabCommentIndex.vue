<template>
  <div class="flex items-center justify-between">
    <div class="font-medium">{{ t('caseManagement.featureCase.commentList') }}</div>
    <div>
      <a-radio-group type="button">
        <a-radio value="caseComment">{{ t('caseManagement.featureCase.caseComment') }}</a-radio>
        <a-radio value="reviewComment">{{ t('caseManagement.featureCase.reviewComment') }}</a-radio>
        <a-radio value="executiveComment">{{ t('caseManagement.featureCase.executiveReview') }}</a-radio>
      </a-radio-group>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
</script>

<style scoped></style>
