<template>
  <div class="flex h-full w-full flex-col">
    <a-radio-group v-model:model-value="condition.scriptType" size="small" class="mb-[16px]">
      <a-radio value="manual">{{ t('apiTestDebug.manual') }}</a-radio>
      <a-radio value="quote">{{ t('apiTestDebug.quote') }}</a-radio>
    </a-radio-group>
    <div
      v-if="scriptType === 'manual'"
      class="relative h-full w-full rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] p-[12px]"
    >
      <div v-if="isShowEditScriptNameInput" class="absolute left-[12px] z-10 w-[calc(100%-24px)]">
        <a-input
          ref="scriptNameInputRef"
          v-model:model-value="condition.name"
          :placeholder="t('apiTestDebug.preconditionScriptNamePlaceholder')"
          :max-length="255"
          show-word-limit
          size="small"
          @press-enter="isShowEditScriptNameInput = false"
          @blur="isShowEditScriptNameInput = false"
        />
      </div>
      <div class="flex items-center justify-between">
        <div class="flex items-center">
          <a-tooltip :content="condition.name">
            <div class="script-name-container">
              <div class="one-line-text mr-[4px] max-w-[110px] font-medium text-[var(--color-text-1)]">
                {{ condition.name }}
              </div>
              <MsIcon type="icon-icon_edit_outlined" class="edit-script-name-icon" @click="showEditScriptNameInput" />
            </div>
          </a-tooltip>
          <a-popover class="h-auto" position="top">
            <div class="text-[rgb(var(--primary-5))]">{{ t('apiTestDebug.scriptEx') }}</div>
            <template #content>
              <div class="mb-[8px] flex items-center justify-between">
                <div class="text-[14px] font-medium text-[var(--color-text-1)]">
                  {{ t('apiTestDebug.scriptEx') }}
                </div>
                <a-button
                  type="outline"
                  class="arco-btn-outline--secondary p-[0_8px]"
                  size="mini"
                  @click="copyScriptEx"
                >
                  {{ t('common.copy') }}
                </a-button>
              </div>
              <div class="flex h-[412px]">
                <MsCodeEditor
                  v-model:model-value="scriptEx"
                  class="flex-1"
                  theme="MS-text"
                  width="500px"
                  height="388px"
                  :show-full-screen="false"
                  :show-theme-change="false"
                  read-only
                >
                </MsCodeEditor>
              </div>
            </template>
          </a-popover>
        </div>
        <div class="flex items-center gap-[8px]">
          <a-button type="outline" class="arco-btn-outline--secondary p-[0_8px]" size="mini">
            <template #icon>
              <MsIcon type="icon-icon_undo_outlined" class="text-var(--color-text-4)" size="12" />
            </template>
            {{ t('common.revoke') }}
          </a-button>
          <a-button type="outline" class="arco-btn-outline--secondary p-[0_8px]" size="mini" @click="clearScript">
            <template #icon>
              <MsIcon type="icon-icon_clear" class="text-var(--color-text-4)" size="12" />
            </template>
            {{ t('common.clear') }}
          </a-button>
          <a-button type="outline" class="arco-btn-outline--secondary p-[0_8px]" size="mini" @click="copyCondition">
            {{ t('common.copy') }}
          </a-button>
          <a-button type="outline" class="arco-btn-outline--secondary p-[0_8px]" size="mini" @click="deleteCondition">
            {{ t('common.delete') }}
          </a-button>
        </div>
      </div>
    </div>
    <div v-else class="flex h-[calc(100%-47px)] flex-col">
      <div class="mb-[16px] flex w-full items-center bg-[var(--color-text-n9)] p-[12px]">
        <div class="text-[var(--color-text-2)]">
          {{ condition.quoteScript.name || '-' }}
        </div>
        <a-divider margin="8px" direction="vertical" />
        <MsButton type="text" class="font-medium">
          {{ t('apiTestDebug.quote') }}
        </MsButton>
      </div>
      <a-radio-group v-model:model-value="commonScriptShowType" size="small" type="button" class="mb-[8px] w-fit">
        <a-radio value="parameters">{{ t('apiTestDebug.parameters') }}</a-radio>
        <a-radio value="scriptContent">{{ t('apiTestDebug.scriptContent') }}</a-radio>
      </a-radio-group>
      <MsBaseTable v-show="commonScriptShowType === 'parameters'" v-bind="propsRes" v-on="propsEvent">
        <template #value="{ record }">
          <a-tooltip :content="t(record.required ? 'apiTestDebug.paramRequired' : 'apiTestDebug.paramNotRequired')">
            <div
              :class="[
                record.required ? '!text-[rgb(var(--danger-5))]' : '!text-[var(--color-text-brand)]',
                '!mr-[4px] !p-[4px]',
              ]"
            >
              <div>*</div>
            </div>
          </a-tooltip>
          {{ record.type }}
        </template>
      </MsBaseTable>
      <div v-show="commonScriptShowType === 'scriptContent'" class="h-[calc(100%-76px)]">
        <MsCodeEditor
          v-model:model-value="condition.quoteScript.script"
          theme="MS-text"
          height="100%"
          :show-full-screen="false"
          :show-theme-change="false"
          read-only
        >
        </MsCodeEditor>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { useClipboard } from '@vueuse/core';
  import { InputInstance, Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const scriptType = defineModel('scriptType', {
    type: String,
    default: 'manual',
  });

  const condition = defineModel('modelValue', {
    type: Object,
    default: () => ({
      scriptType: 'manual',
      name: '',
      script: '',
      quoteScript: {
        name: '',
        script: '',
      },
    }),
  });

  const columns: MsTableColumn = [
    {
      title: 'apiTestDebug.paramName',
      dataIndex: 'name',
      showTooltip: true,
    },
    {
      title: 'apiTestDebug.paramValue',
      dataIndex: 'value',
      slotName: 'value',
    },
    {
      title: 'apiTestDebug.desc',
      dataIndex: 'desc',
      showTooltip: true,
    },
  ];

  const { propsRes, propsEvent } = useTable(() => Promise.resolve([]), {
    scroll: { x: '100%' },
    columns,
  });

  const emit = defineEmits<{
    (e: 'update:data', data: Record<string, any>): void;
    (e: 'copy'): void;
    (e: 'delete', id: string): void;
    (e: 'change'): void;
  }>();

  // 是否显示脚本名称编辑框
  const isShowEditScriptNameInput = ref(false);
  const scriptNameInputRef = ref<InputInstance>();

  function showEditScriptNameInput() {
    isShowEditScriptNameInput.value = true;
    nextTick(() => {
      scriptNameInputRef.value?.focus();
    });
  }

  const scriptEx = ref(`2023-12-04 11:19:28 INFO 9026fd6a 1-1 Thread started: 9026fd6a 1-1
2023-12-04 11:19:28 ERROR 9026fd6a 1-1 Problem in JSR223 script JSR223Sampler, message: {}
In file: inline evaluation of: prev.getResponseCode() import java.net.URI; import org.apache.http.client.method . . . '' Encountered "import" at line 2, column 1.
in inline evaluation of: prev.getResponseCode() import java.net.URI; import org.apache.http.client.method . . . '' at line number 2
javax.script.ScriptException '' at line number 2
javax.script.ScriptException '' at line number 2
javax.script.ScriptException '' at line number 2
javax.script.ScriptException '' at line number 2
javax.script.ScriptException '' at line number 2
javax.script.ScriptException
org.apache.http.client.method . . . '' at line number 2
`);
  const { copy, isSupported } = useClipboard();
  const commonScriptShowType = ref<'parameters' | 'scriptContent'>('parameters');
  function copyScriptEx() {
    if (isSupported) {
      copy(scriptEx.value);
      Message.success(t('apiTestDebug.scriptExCopySuccess'));
    } else {
      Message.warning(t('apiTestDebug.copyNotSupport'));
    }
  }
  function clearScript() {
    condition.value.script = '';
  }

  /**
   * 复制条件
   */
  function copyCondition() {
    emit('copy');
  }

  /**
   * 删除条件
   */
  function deleteCondition() {
    emit('delete', condition.value.id);
  }
</script>

<style lang="less" scoped></style>
