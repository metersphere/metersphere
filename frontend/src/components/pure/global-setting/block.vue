<template>
  <div class="mb-[24px]">
    <h5 class="title my-[10px] p-0 text-[14px]">{{ title }}</h5>
    <div v-for="option in options" :key="option.name" class="switch-wrapper h-[32px]">
      <span>{{ $t(option.name) }}</span>
      <form-wrapper
        :type="option.type || 'switch'"
        :name="option.key"
        :default-value="option.defaultVal"
        @input-change="handleChange"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { PropType } from 'vue';
  import { useAppStore } from '@/store';
  import FormWrapper from './form-wrapper.vue';

  interface OptionsProps {
    name: string;
    key: string;
    type?: string;
    defaultVal?: boolean | string | number;
  }
  defineProps({
    title: {
      type: String,
      default: '',
    },
    options: {
      type: Array as PropType<OptionsProps[]>,
      default() {
        return [];
      },
    },
  });
  const appStore = useAppStore();
  const handleChange = async ({ key, value }: { key: string; value: unknown }) => {
    if (key === 'colorWeak') {
      document.body.style.filter = value ? 'invert(80%)' : 'none';
    }
    if (key === 'topMenu') {
      appStore.updateSettings({
        menuCollapse: false,
      });
    }
    appStore.updateSettings({ [key]: value });
  };
</script>

<style scoped lang="less">
  .title {
    @apply mx-0 p-0;
  }
  .switch-wrapper {
    @apply flex items-center justify-between;
  }
</style>
