<template>
  <a-button type="outline" size="mini" @click="showBatchAddParamDrawer = true">
    {{ t('apiTestDebug.batchAdd') }}
  </a-button>
  <MsDrawer
    v-model:visible="showBatchAddParamDrawer"
    :width="680"
    :ok-text="t('apiTestDebug.apply')"
    disabled-width-drag
    @confirm="applyBatchParams"
  >
    <template #title>
      {{ t('common.batchAdd') }}
      <a-tooltip position="right">
        <icon-exclamation-circle
          class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
          size="16"
        />
        <template #content>
          <div>{{ t('apiTestDebug.batchAddParamsTip2') }} </div>
          <div>{{ t('apiTestDebug.batchAddParamsTip3') }} </div>
        </template>
      </a-tooltip>
    </template>
    <div class="flex h-full">
      <MsCodeEditor
        v-if="showBatchAddParamDrawer"
        v-model:model-value="batchParamsCode"
        class="flex-1"
        theme="MS-text"
        height="100%"
        :show-full-screen="false"
      >
        <template #title>
          <div class="text-[12px] leading-[16px] text-[var(--color-text-4)]">
            {{ t('apiTestDebug.batchAddParamsTip') }}
          </div>
        </template>
      </MsCodeEditor>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { isEmpty } from 'lodash-es';

  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    params: Record<string, any>[];
    defaultParamItem?: Record<string, any>; // 默认参数项
  }>();
  const emit = defineEmits<{
    (e: 'apply', resultArr: (Record<string, any> | null)[]): void;
  }>();

  const { t } = useI18n();

  const showBatchAddParamDrawer = ref(false);
  const batchParamsCode = ref('');

  watch(
    () => showBatchAddParamDrawer.value,
    (val) => {
      if (val) {
        batchParamsCode.value = props.params
          .filter((e) => e && (!isEmpty(e.key) || !isEmpty(e.value)))
          .map((item) => `${item.key}:${item.value}`)
          .join('\n');
      }
    },
    {
      immediate: true,
    }
  );

  /**
   * 批量参数代码转换为参数表格数据
   */
  function applyBatchParams() {
    const arr = batchParamsCode.value.replaceAll('\r', '\n').split('\n'); // 先将回车符替换成换行符，避免粘贴的代码是以回车符分割的，然后以换行符分割
    const tempObj: Record<string, any> = {}; // 同名参数去重，保留最新的
    for (let i = 0; i < arr.length; i++) {
      const [key, value] = arr[i].split(':');
      if (key) {
        tempObj[key.trim()] = {
          id: new Date().getTime() + i,
          ...props.defaultParamItem,
          key: key.trim(),
          value: value?.trim(),
        };
      }
    }
    showBatchAddParamDrawer.value = false;
    batchParamsCode.value = '';
    emit('apply', Object.values(tempObj));
  }
</script>

<style lang="less" scoped></style>
