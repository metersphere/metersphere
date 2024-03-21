<template>
  <div class="h-full w-full">
    <a-scrollbar
      :style="{
        overflow: 'auto',
        height: 'calc(100vh - 490px)',
      }"
    >
      <conditionContent v-model:data="condition" is-build-in @change="handleChange" />
    </a-scrollbar>
  </div>
</template>

<script lang="ts" setup>
  import { useVModel } from '@vueuse/core';

  import conditionContent from '@/views/api-test/components/condition/content.vue';

  import { getEnvironment } from '@/api/modules/api-test/common';
  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';

  const store = useProjectEnvStore();
  interface ScriptItem {
    [key: string]: any;
  }

  interface ScriptTabProps {
    data: any;
  }

  const props = defineProps<ScriptTabProps>();

  const emit = defineEmits<{
    (e: 'change', val: ScriptItem): void; //  数据发生变化
  }>();
  function handleChange() {
    // eslint-disable-next-line no-use-before-define
    emit('change', { ...condition.value });
  }
  const condition = useVModel(props, 'data', emit);
  const currentEnvConfig = ref({});
  async function initEnvironment() {
    if (store.currentId) {
      currentEnvConfig.value = await getEnvironment(store.currentId);
    }
  }
  /** 向孙组件提供属性 */
  provide('currentEnvConfig', readonly(currentEnvConfig));
  onBeforeMount(() => {
    initEnvironment();
  });
</script>

<style lang="less" scoped></style>
