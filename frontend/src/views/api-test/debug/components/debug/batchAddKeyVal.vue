<template>
  <a-button type="outline" size="mini" @click="showBatchAddParamDrawer = true">
    {{ t('apiTestDebug.batchAdd') }}
  </a-button>
  <MsDrawer
    v-model:visible="showBatchAddParamDrawer"
    :title="t('common.batchAdd')"
    :width="680"
    :ok-text="t('apiTestDebug.apply')"
    disabled-width-drag
    @confirm="applyBatchParams"
  >
    <div class="flex h-full">
      <MsCodeEditor
        v-if="showBatchAddParamDrawer"
        v-model:model-value="batchParamsCode"
        class="flex-1"
        theme="MS-text"
        height="calc(100% - 12px)"
        :show-full-screen="false"
      >
        <template #title>
          <div class="flex flex-col">
            <div class="text-[12px] leading-[16px] text-[var(--color-text-4)]">
              {{ t('apiTestDebug.batchAddParamsTip') }}
            </div>
            <div class="text-[12px] leading-[16px] text-[var(--color-text-4)]">
              {{ t('apiTestDebug.batchAddParamsTip2') }}
            </div>
          </div>
        </template>
      </MsCodeEditor>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { RequestContentTypeEnum } from '@/enums/apiEnum';

  const props = defineProps<{
    params: Record<string, any>[];
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
          .filter((e) => e && (e.name !== '' || e.value !== ''))
          .map((item) => `${item.name}:${item.value}`)
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
    const resultArr = arr
      .map((item, i) => {
        const [name, value] = item.split(':');
        if (name || value) {
          return {
            id: new Date().getTime() + i,
            name: name?.trim(),
            value: value?.trim(),
            required: false,
            type: 'string',
            min: undefined,
            max: undefined,
            contentType: RequestContentTypeEnum.TEXT,
            desc: '',
            encode: false,
          };
        }
        return null;
      })
      .filter((item) => item);
    showBatchAddParamDrawer.value = false;
    batchParamsCode.value = '';
    emit('apply', resultArr);
  }
</script>

<style lang="less" scoped></style>
