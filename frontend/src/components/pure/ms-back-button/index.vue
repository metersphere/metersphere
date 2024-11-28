<template>
  <div v-if="showTopButton || showBottomButton" class="back-button-container">
    <div class="back-button" @click="scrollTo(showTopButton ? 'top' : 'bottom')">
      <MsIcon
        type="icon-icon_download"
        :class="`text-[rgb(var(--primary-5))] ${showTopButton ? 'back-button-top' : ''}`"
        size="20"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
  /**
   * 回到顶部&去往底部
   */
  import { ref } from 'vue';

  const props = defineProps<{
    target: string;
  }>();

  const showTopButton = ref(false);
  const showBottomButton = ref(false);
  const scrollContainer = ref();

  // 滚动到顶部
  const scrollTo = (direction: string) => {
    if (scrollContainer.value) {
      if (direction === 'top') {
        scrollContainer.value.scrollTo({ top: 0, behavior: 'smooth' });
      } else {
        scrollContainer.value.scrollTo({ top: scrollContainer.value.scrollHeight, behavior: 'smooth' });
      }
    }
  };

  // 处理滚动事件
  const handleScroll = () => {
    if (scrollContainer.value) {
      const scrollPosition = scrollContainer.value.scrollTop;
      const containerHeight = scrollContainer.value.scrollHeight;
      const visibleHeight = scrollContainer.value.clientHeight;

      showTopButton.value = scrollPosition > visibleHeight;
      showBottomButton.value = scrollPosition < containerHeight - visibleHeight;
    }
  };

  onMounted(() => {
    scrollContainer.value = document.querySelector(props.target);
    if (scrollContainer.value) {
      scrollContainer.value.addEventListener('scroll', handleScroll, { passive: true });
      handleScroll();
    }
  });

  onBeforeUnmount(() => {
    if (scrollContainer.value) {
      scrollContainer.value.removeEventListener('scroll', handleScroll);
    }
  });
</script>

<style scoped lang="less">
  .back-button-container {
    position: fixed;
    right: 16px;
    bottom: 56px;
    display: flex;
  }
  .back-button {
    width: 40px;
    height: 40px;
    border-radius: 6px;
    color: rgb(var(--primary-6));
    background-color: var(--color-text-fff);
    box-shadow: 0 3px 14px 2px rgba(0 0 0/ 5%);
    @apply flex cursor-pointer items-center justify-center font-medium;
  }
  .back-button-top {
    transform: rotate(180deg);
  }
</style>
