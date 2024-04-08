<template>
  <conditionContent v-model:data="condition" :disabled="props.disabled" @delete="deleteItem" />
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
    disabled?: boolean;
  }

  const props = defineProps<ScriptTabProps>();

  const emit = defineEmits<{
    (e: 'change', val: ScriptItem): void; //  数据发生变化
    (e: 'update:data'): void; //  数据发生变化
    (e: 'deleteScriptItem', id: string | number): void; //  删除脚本
  }>();

  const condition = useVModel(props, 'data', emit);

  const currentEnvConfig = ref({});

  async function initEnvironment() {
    if (store.currentId) {
      currentEnvConfig.value = await getEnvironment(store.currentId);
    }
  }
  /** 向孙组件提供属性 */
  provide('currentEnvConfig', readonly(currentEnvConfig));

  /**
   * 删除列表项
   */
  function deleteItem(id: string | number) {
    emit('deleteScriptItem', id);
  }

  onBeforeMount(() => {
    initEnvironment();
  });
</script>

<style lang="less" scoped></style>