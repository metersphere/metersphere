<template>
  <div class="bug-comment">
    <div class="header-icon">
      <img :src="comment.user.avatar" alt="User avatar" />
    </div>
    <div class="content">
      <h3>{{ comment.user.name }}</h3>
      <MsRichText :text="comment.content" />
    </div>
    <div class="footer">
      <span>{{ comment.date }}</span>
      <button @click="toggleShowComment">{{ showComment ? '收起评论' : '展开评论' }}</button>
    </div>
  </div>
</template>

<script setup>
  import { ref } from 'vue';

  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';

  const showComment = ref(false);

  const toggleShowComment = () => {
    showComment.value = !showComment.value;
  };
</script>

<style scoped>
  .bug-comment {
    display: flex;
    flex-direction: column;
    padding: 16px;
    border-bottom: 1px solid #f0f0f0;
  }
</style>
