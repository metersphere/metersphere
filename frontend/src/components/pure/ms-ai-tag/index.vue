<template>
  <div class="ms-ai-tag">
    <div class="ms-ai-tag-text"> AI </div>
  </div>
</template>

<style lang="less" scoped>
  .ms-ai-tag {
    @apply flex items-center justify-center;

    width: 20px;
    height: 20px;
    border-radius: 3px;
    background-color: rgb(var(--primary-1));
    .ms-ai-tag-text {
      font-size: 12px;
      font-weight: 600;
      color: transparent;
      background-clip: text;
      background-image: linear-gradient(154.65deg, #386cfa 25%, #7c26aa 75.27%);
    }
  }
</style>
