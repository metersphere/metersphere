<template>
  <a-modal
    v-model:visible="visible"
    title-align="start"
    :width="680"
    :ok-disabled="curlCode.trim() === ''"
    :ok-text="t('common.import')"
    :ok-button-props="{
      disabled: curlCode.trim() === '',
    }"
    class="ms-modal-form"
    @cancel="curlCode = ''"
    @before-ok="handleCurlImportConfirm"
  >
    <template #title>
      <a-tooltip position="right" :content="t('apiTestDebug.importByCURLTip')">
        <span
          >{{ t('apiTestDebug.importByCURL') }}
          <icon-question-circle
            class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
            size="16"
          />
        </span>
      </a-tooltip>
    </template>
    <div class="h-full">
      <MsCodeEditor
        v-if="visible"
        v-model:model-value="curlCode"
        theme="MS-text"
        height="500px"
        :language="LanguageEnum.PLAINTEXT"
        :show-theme-change="false"
        :show-full-screen="false"
      >
      </MsCodeEditor>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';

  import { importByCurl } from '@/api/modules/api-test/common';
  import { useI18n } from '@/hooks/useI18n';

  import type { CurlParseResult } from '@/models/apiTest/common';

  const emit = defineEmits<{
    (e: 'done', res: CurlParseResult): void;
  }>();

  const { t } = useI18n();

  const visible = defineModel('visible', {
    type: Boolean,
    required: true,
  });

  const importLoading = ref(false);
  const curlCode = ref('');

  async function handleCurlImportConfirm(done: (closed: boolean) => void) {
    try {
      importLoading.value = true;
      const res = await importByCurl(curlCode.value);
      emit('done', res);
      done(true);
      Message.success(t('common.importSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      importLoading.value = false;
    }
  }
</script>

<style lang="less" scoped></style>
