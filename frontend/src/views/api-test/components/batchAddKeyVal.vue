<template>
  <MsDrawer
    v-model:visible="visible"
    :width="680"
    :ok-text="t('apiTestDebug.apply')"
    disabled-width-drag
    @confirm="applyBatchParams"
  >
    <template #title>
      <div>
        {{ t('common.batchAdd') }}
        <a-tooltip position="right">
          <icon-exclamation-circle
            class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
            size="16"
          />
          <template #content>
            <div v-if="props?.addTypeText">{{ props?.addTypeText }}</div>
            <div v-if="!props.noParamType">{{ t('apiTestDebug.batchAddParamsTip2') }}</div>
            <div>{{ t('apiTestDebug.batchAddParamsTip3') }}</div>
          </template>
        </a-tooltip>
      </div>
    </template>
    <div class="h-full">
      <MsCodeEditor
        v-if="visible"
        v-model:model-value="batchParamsCode"
        theme="vs"
        height="100%"
        :show-full-screen="false"
        :show-theme-change="false"
      >
        <template v-if="props.hasStandard" #leftTitle>
          <a-radio-group v-model:model-value="addType" type="button" @change="() => parseParams()">
            <a-radio value="standard">{{ t('apiTestDebug.standardAdditions') }}</a-radio>
            <a-radio value="quick">{{ t('apiTestDebug.quickAdditions') }}</a-radio>
          </a-radio-group>
        </template>
        <template v-if="!props?.addTypeText" #rightTitle>
          <div class="text-[12px] text-[var(--color-text-4)]">
            {{
              props.hasStandard && addType === 'standard'
                ? t('apiTestDebug.standardAdditionsTip')
                : t('apiTestDebug.batchAddParamsTip1')
            }}
          </div>
        </template>
      </MsCodeEditor>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { cloneDeep, isEmpty } from 'lodash-es';

  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { getGenerateId } from '@/utils';

  import { RequestParamsType } from '@/enums/apiEnum';

  const props = withDefaults(
    defineProps<{
      params: Record<string, any>[];
      defaultParamItem?: Record<string, any>; // 默认参数项
      noParamType?: boolean; // 是否有参数类型
      addTypeText?: string; // 添加类型文案
      disabled?: boolean;
      typeTitle?: string;
      hasStandard?: boolean; // 是否有标准模式
      acceptTypes?: RequestParamsType[]; // 可接受的参数类型
    }>(),
    {
      noParamType: false,
    }
  );
  const emit = defineEmits<{
    (e: 'apply', resultArr: (Record<string, any> | null)[]): void;
  }>();

  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', {
    required: true,
  });
  const batchParamsCode = ref('');
  const addType = ref<'standard' | 'quick'>('standard');

  function parseParams() {
    if (props.hasStandard && addType.value === 'standard') {
      // 过滤掉空行和文件类型的参数
      batchParamsCode.value = props.params
        .filter((e) => e && (!isEmpty(e.key) || !isEmpty(e.value)) && e.paramType !== RequestParamsType.FILE)
        .map((item) => `${item.key},${item.paramType},${item.required},${item.value}`)
        .join('\n');
    } else {
      // 过滤掉空行和文件类型的参数
      batchParamsCode.value = props.params
        .filter((e) => e && (!isEmpty(e.key) || !isEmpty(e.value)) && e.paramType !== RequestParamsType.FILE)
        .map((item) => `${item.key}:${item.value}`)
        .join('\n');
    }
  }

  watch(
    () => visible.value,
    (val) => {
      if (val) {
        parseParams();
      }
    },
    {
      immediate: true,
    }
  );

  /**
   * 批量参数代码转换为参数表格数据-快捷模式-参数名：参数值
   */
  function quickApply() {
    const arr = batchParamsCode.value.replaceAll('\r', '\n').split('\n'); // 先将回车符替换成换行符，避免粘贴的代码是以回车符分割的，然后以换行符分割
    const tempObj: Record<string, any> = {}; // 同名参数去重，保留最新的
    for (let i = 0; i < arr.length; i++) {
      if (arr[i] !== '') {
        // 只截取第一个`:`
        const index = arr[i].indexOf(':');
        if (index === -1) {
          tempObj[arr[i].trim()] = {
            id: getGenerateId(),
            ...cloneDeep(props.defaultParamItem), // 深拷贝，避免有嵌套引用类型，数据隔离
            key: arr[i].trim(),
            value: '',
          };
        } else {
          const [key, value] = [arr[i].substring(0, index).trim(), arr[i].substring(index + 1).trim()];
          if (key || value) {
            tempObj[key.trim()] = {
              id: getGenerateId(),
              ...cloneDeep(props.defaultParamItem), // 深拷贝，避免有嵌套引用类型，数据隔离
              key: key.trim(),
              value: value?.trim(),
            };
          }
        }
      }
    }
    visible.value = false;
    batchParamsCode.value = '';
    emit('apply', Object.values(tempObj));
  }

  function stringToBoolean(str?: string) {
    if (str === 'true') {
      return true;
    }
    if (str === 'false') {
      return false;
    }
    return false;
  }

  /**
   * 批量参数代码转换为参数表格数据-标准模式-参数名,参数类型,必填,参数值
   */
  function standardApply() {
    const arr = batchParamsCode.value.replaceAll('\r', '\n').split('\n'); // 先将回车符替换成换行符，避免粘贴的代码是以回车符分割的，然后以换行符分割
    const tempObj: Record<string, any> = {}; // 同名参数去重，保留最新的
    for (let i = 0; i < arr.length; i++) {
      if (arr[i] !== '') {
        const parts = arr[i].split(',');
        // 提取前三个元素
        const [key, type, required] = parts.slice(0, 3);
        // 将第四个元素到最后一个元素合并成一个字符串
        const value = parts.length >= 4 ? parts.slice(3).join(',') : '';
        if (key) {
          tempObj[key.trim()] = {
            id: getGenerateId(),
            ...cloneDeep(props.defaultParamItem), // 深拷贝，避免有嵌套引用类型，数据隔离
            key: key.trim(),
            paramType: (props.acceptTypes || Object.values(RequestParamsType)).includes(
              type?.trim() as unknown as RequestParamsType
            )
              ? type?.trim()
              : RequestParamsType.STRING,
            required: stringToBoolean(required),
            value: value?.trim(),
          };
        }
      }
    }
    visible.value = false;
    batchParamsCode.value = '';
    addType.value = 'standard';
    emit('apply', Object.values(tempObj));
  }

  function applyBatchParams() {
    if (props.hasStandard && addType.value === 'standard') {
      standardApply();
    } else {
      quickApply();
    }
  }
</script>

<style lang="less" scoped></style>
