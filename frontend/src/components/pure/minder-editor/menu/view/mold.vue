<template>
  <a-dropdown class="toggle" :disabled="disabled" @select="handleCommand">
    <span class="dropdown-toggle mold-icons menu-btn cursor-pointer" :class="'mold-' + (moldIndex + 1)" />
    <template #content>
      <a-doption class="dropdown-item" :value="1">
        <div class="mold-icons mold-1"></div>
      </a-doption>
      <a-doption class="dropdown-item" :value="2">
        <div class="mold-icons mold-2"></div>
      </a-doption>
      <a-doption class="dropdown-item" :value="3">
        <div class="mold-icons mold-3"></div>
      </a-doption>
      <a-doption class="dropdown-item" :value="4">
        <div class="mold-icons mold-4"></div>
      </a-doption>
      <a-doption class="dropdown-item" :value="5">
        <div class="mold-icons mold-5"></div>
      </a-doption>
      <a-doption class="dropdown-item" :value="6">
        <div class="mold-icons mold-6"></div>
      </a-doption>
    </template>
  </a-dropdown>
</template>

<script lang="ts" name="Mold" setup>
  import { computed, nextTick, onMounted, ref } from 'vue';

  import useMinderStore from '@/store/modules/components/minder-editor';

  import { moleProps } from '../../props';

  const props = defineProps(moleProps);

  const emit = defineEmits<{
    (e: 'moldChange', data: number): void;
  }>();

  const minderStore = useMinderStore();
  const moldIndex = ref(0);

  const disabled = computed(() => {
    try {
      if (!window.minder) return false;
    } catch (e) {
      // 如果window的还没挂载minder，先捕捉undefined异常
      return false;
    }
    return window.minder.queryCommandState('template') === -1;
  });

  const templateList = computed(() => window.kityminder.Minder.getTemplateList());

  function handleCommand(value: string | number | Record<string, any> | undefined) {
    moldIndex.value = (value as number) - 1;
    window.minder.execCommand('template', Object.keys(templateList.value)[(value as number) - 1]);
    minderStore.setMold((value as number) - 1);
    emit('moldChange', (value as number) - 1);
  }

  onMounted(() => {
    nextTick(() => handleCommand(props.defaultMold));
  });
</script>

<style lang="less" scoped>
  :deep(.arco-dropdown-list) {
    @apply grid grid-cols-2;
  }
  .dropdown-toggle .mold-icons,
  .mold-icons {
    background-image: url('@/assets/images/minder/mold.png');
    background-repeat: no-repeat;
  }
  .dropdown-item {
    @apply flex items-center justify-center;

    height: 50px !important;
  }
  .mold-loop(@i) when (@i > 0) {
    .mold-@{i} {
      @apply flex;

      margin-top: 5px;
      width: 50px;
      height: 45px;
      background-position: (1 - @i) * 50px 0;
    }
    .mold-loop(@i - 1);
  }

  .mold-loop(6);
</style>
