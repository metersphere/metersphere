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
      background-size: 200% 200%;
      background-clip: text;
      filter: brightness(1.2) drop-shadow(0 0 4px #7c26aa80);
      transition: filter 0.2s;
      background-image: linear-gradient(154.65deg, #386cfa 25%, #7c26aa 75.27%);
      animation: ai-flash 1.2s linear infinite alternate;
    }
  }
  @keyframes ai-flash {
    0% {
      background-position: 0% 50%;
      filter: brightness(1.2) drop-shadow(0 0 4px #386cfa80);
    }
    50% {
      background-position: 100% 50%;
      filter: brightness(2) drop-shadow(0 0 8px #7c26aa);
    }
    100% {
      background-position: 0% 50%;
      filter: brightness(1.2) drop-shadow(0 0 4px #386cfa80);
    }
  }
</style>
