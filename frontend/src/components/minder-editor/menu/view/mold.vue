<template>
  <div class="mold-group" :disabled="disabled">
    <a-dropdown class="toggle" @select="handleCommand">
      <div>
        <span class="dropdown-toggle mold-icons menu-btn" :class="'mold-' + (moldIndex + 1)" />
        <span class="dropdown-link">
          <icon-caret-down />
        </span>
      </div>
      <template #content>
        <a-doption class="dropdown-item mold-icons mold-1" :value="1" />
        <a-doption class="dropdown-item mold-icons mold-2" :value="2" />
        <a-doption class="dropdown-item mold-icons mold-3" :value="3" />
        <a-doption class="dropdown-item mold-icons mold-4" :value="4" />
        <a-doption class="dropdown-item mold-icons mold-5" :value="5" />
        <a-doption class="dropdown-item mold-icons mold-6" :value="6" />
      </template>
    </a-dropdown>
  </div>
</template>

<script lang="ts" name="Mold" setup>
  import { computed, nextTick, onMounted, ref } from 'vue';
  import { moleProps } from '../../props';

  const props = defineProps(moleProps);

  const emit = defineEmits<{
    (e: 'moldChange', data: number): void;
  }>();

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
    moldIndex.value = value as number;
    window.minder.execCommand('template', Object.keys(templateList.value)[value as number]);
    emit('moldChange', value as number);
  }

  onMounted(() => {
    nextTick(() => handleCommand(props.defaultMold));
  });
</script>

<style lang="less">
  .toggle {
    .arco-dropdown-list {
      @apply grid grid-cols-2;

      padding: 5px;
      gap: 5px;
    }
  }
</style>

<style lang="less" scoped>
  .dropdown-toggle .mold-icons,
  .mold-icons {
    background-image: url('@/assets/images/minder/mold.png');
    background-repeat: no-repeat;
  }
  .mold-group {
    @apply relative flex items-center justify-center;

    width: 80px;
    .dropdown-toggle {
      @apply flex;

      margin-top: 5px;
      width: 50px;
      height: 50px;
    }
  }
  .dropdown-link {
    @apply absolute cursor-pointer;

    right: 3px;
    bottom: 2px;
  }
  .mold-loop(@i) when (@i > 0) {
    .mold-@{i} {
      @apply flex;

      margin-top: 5px;
      width: 50px;
      height: 50px;
      background-position: (1 - @i) * 50px 0;
    }
    .mold-loop(@i - 1);
  }

  .mold-loop(6);
</style>
