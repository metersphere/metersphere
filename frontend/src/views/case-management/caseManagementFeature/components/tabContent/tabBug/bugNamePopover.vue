<template>
  <div class="one-line-text max-w-[calc(100%-32px)]"> {{ props.name }}</div>
  <a-popover class="bug-content-popover" title="" position="right" style="width: 480px" tri>
    <span class="ml-1 text-[rgb(var(--primary-5))]">{{ t('caseManagement.featureCase.preview') }}</span>
    <template #content>
      <div v-dompurify-html="props.content" class="markdown-body bug-content"> </div>
    </template>
  </a-popover>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const props = defineProps<{
    name: string;
    content: string;
  }>();
</script>

<style lang="less">
  .bug-content-popover {
    .arco-popover-content {
      overflow: auto;
      max-height: 400px;
      .ms-scroll-bar();
    }
  }
</style>
