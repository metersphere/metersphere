<template>
  <a-button v-bind="$attrs" :loading="loading" @click="clickHandler">
    <slot></slot>
  </a-button>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { FormInstance, ValidatedError } from '@arco-design/web-vue';

  const props = defineProps<{
    clickFun?: ((enable: boolean | undefined) => Promise<void>) | undefined;
    formRef?: FormInstance | null;
    enable?: boolean | undefined; // 使用弹窗内置状态
  }>();
  const loading = ref<boolean>(false);

  const reqFun = async (enable?: boolean) => {
    try {
      loading.value = true;
      if (props.clickFun) {
        await props.clickFun(enable);
      }
      loading.value = false;
    } catch (error) {
      console.log(error);
      loading.value = false;
    }
  };

  function clickHandler() {
    if (props.formRef) {
      props.formRef?.validate((errors: undefined | Record<string, ValidatedError>) => {
        if (!errors) {
          reqFun(props.enable);
        } else {
          return false;
        }
      });
    } else {
      reqFun(props.enable);
    }
  }
</script>

<style scoped></style>
