<template>
  <conditionContent
    v-model:data="condition"
    condition-type="assertion"
    :disabled="props.disabled"
    :script-code-editor-height="props.scriptCodeEditorHeight"
    @delete="deleteItem"
    @copy="emit('copy')"
  />
</template>

<script lang="ts" setup>
  import { useVModel } from '@vueuse/core';

  import conditionContent from '@/views/api-test/components/condition/content.vue';

  interface ScriptItem {
    [key: string]: any;
  }

  interface ScriptTabProps {
    data: any;
    disabled?: boolean;
    scriptCodeEditorHeight?: string; // 脚本的高度
  }

  const props = defineProps<ScriptTabProps>();

  const emit = defineEmits<{
    (e: 'change', val: ScriptItem): void; //  数据发生变化
    (e: 'update:data'): void; //  数据发生变化
    (e: 'deleteScriptItem', id: string | number): void; //  删除脚本
    (e: 'copy'): void;
  }>();

  const condition = useVModel(props, 'data', emit);

  /**
   * 删除列表项
   */
  function deleteItem(id: string | number) {
    emit('deleteScriptItem', id);
  }
</script>

<style lang="less" scoped></style>
