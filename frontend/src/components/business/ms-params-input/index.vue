<template>
  <a-trigger
    v-model:popup-visible="paramSettingVisible"
    trigger="click"
    :popup-translate="[0, 16]"
    position="bl"
    class="ms-params-input-setting-trigger"
  >
    <span class="invisible"></span>
    <template #content>
      <div class="ms-params-input-setting-trigger-content">
        <div class="mb-[16px] flex items-center justify-between">
          <div class="font-semibold text-[var(--color-text-1)]">{{ t('ms.paramsInput.paramSetting') }}</div>
          <a-radio-group
            v-model:model-value="paramSettingType"
            type="button"
            size="small"
            @change="handleParamSettingChange"
          >
            <a-radio value="mock">Mock</a-radio>
            <a-radio value="jmeter">JMeter</a-radio>
          </a-radio-group>
        </div>
        <div class="ms-params-input-setting-trigger-content-scroll">
          <a-form ref="paramFormRef" :model="paramForm" layout="vertical">
            <template v-if="paramSettingType === 'mock'">
              <a-form-item
                field="type"
                :label="t('ms.paramsInput.mockType')"
                :rules="[{ required: true, message: t('ms.paramsInput.mockTypePlaceholder') }]"
                asterisk-position="end"
                class="mb-[16px]"
              >
                <MsCascader
                  v-model:model-value="paramForm.type"
                  mode="native"
                  :options="paramTypeOptions"
                  :placeholder="t('ms.paramsInput.mockTypePlaceholder')"
                  option-size="small"
                  label-key="value"
                  value-key="key"
                  @change="handleParamTypeChange"
                >
                  <template #label="{ data }">
                    <a-tooltip :content="`${t(data.label.split('/').pop().trim())} ${paramForm.type}`">
                      <div class="one-line-text inline-flex w-full items-center justify-between pr-[8px]" title="">
                        {{ t(data.label.split('/').pop().trim()) }}
                        <div class="max-w-[60%] text-[var(--color-text-4)]" title="">
                          {{ paramForm.type }}
                        </div>
                      </div>
                    </a-tooltip>
                  </template>
                  <template #option="{ data }">
                    <div
                      :class="`flex ${data.isLeaf ? 'mr-[-26px] w-[270px]' : 'w-[120px]'} items-center justify-between`"
                      title=""
                    >
                      <a-tooltip :content="t(data.label)">
                        <div :class="`one-line-text ${data.isLeaf ? 'max-w-[50%]' : ''}`" title="">
                          {{ t(data.label) }}
                        </div>
                      </a-tooltip>
                      <a-tooltip v-if="data.isLeaf" :content="data.value">
                        <div class="one-line-text max-w-[50%] text-[var(--color-text-4)]">
                          {{ data.value }}
                        </div>
                      </a-tooltip>
                    </div>
                  </template>
                </MsCascader>
              </a-form-item>
              <paramsInputGroup
                v-if="currentParamsInputGroup.length > 0"
                :param-form="paramForm"
                :input-group="currentParamsInputGroup"
              />
              <a-form-item :label="t('ms.paramsInput.addFunc')" class="mb-[16px]">
                <a-select
                  v-model:model-value="paramForm.func"
                  :options="paramFuncOptions"
                  :placeholder="t('ms.paramsInput.commonSelectPlaceholder')"
                  @change="(val) => handleParamFuncChange(val as string)"
                >
                  <template #label="{ data }">
                    <a-tooltip :content="`${t(data.label)} ${t(data.desc)}`">
                      <div class="one-line-text inline-flex w-full items-center justify-between pr-[8px]" title="">
                        {{ data.value }}
                        <div class="max-w-[60%] text-[var(--color-text-4)]" title="">
                          {{ t(data.desc) }}
                        </div>
                      </div>
                    </a-tooltip>
                  </template>
                  <template #option="{ data }">
                    <div class="flex w-[420px] items-center justify-between">
                      {{ t(data.label) }}
                      <a-tooltip :content="t(data.desc)">
                        <div class="one-line-text max-w-[70%] text-[var(--color-text-4)]">
                          {{ t(data.desc) }}
                        </div>
                      </a-tooltip>
                    </div>
                  </template>
                </a-select>
              </a-form-item>
              <paramsInputGroup
                v-if="currentParamsFuncInputGroup.length > 0"
                type="func"
                :param-form="paramForm"
                :input-group="currentParamsFuncInputGroup"
              />
            </template>
            <template v-else>
              <a-form-item
                field="JMeterType"
                :label="t('ms.paramsInput.jmeterType')"
                :rules="[{ required: true, message: t('ms.paramsInput.mockTypePlaceholder') }]"
                asterisk-position="end"
                class="mb-[16px]"
              >
                <MsCascader
                  v-model:model-value="paramForm.JMeterType"
                  mode="native"
                  :options="JMeterVarsOptions"
                  :placeholder="t('ms.paramsInput.mockTypePlaceholder')"
                  option-size="small"
                  label-key="value"
                  value-key="key"
                  @change="handleJMeterTypeChange"
                >
                  <template #label="{ data }">
                    <div class="inline-flex w-full items-center justify-between">
                      <a-tooltip :content="t(data.label.split('/').pop().trim())">
                        <div class="one-line-text max-w-[50%]" title="">
                          {{ t(data.label.split('/').pop().trim()) }}
                        </div>
                      </a-tooltip>
                      <a-tooltip :content="`${t(data.label.split('/').pop().trim())} ${paramForm.JMeterType}`">
                        <div class="max-w-[50%] text-[var(--color-text-4)]" title="">
                          {{ paramForm.JMeterType }}
                        </div>
                      </a-tooltip>
                    </div>
                  </template>
                  <template #option="{ data }">
                    <div
                      :class="`flex ${data.isLeaf ? 'mr-[-26px] w-[270px]' : 'w-[120px]'} items-center justify-between`"
                    >
                      <a-tooltip :content="t(data.label)">
                        <div :class="`one-line-text ${data.isLeaf ? 'max-w-[50%]' : ''}`" title="">
                          {{ t(data.label) }}
                        </div>
                      </a-tooltip>
                      <a-tooltip v-if="data.isLeaf" :content="data.value">
                        <div class="one-line-text max-w-[50%] text-[var(--color-text-4)]">
                          {{ data.value }}
                        </div>
                      </a-tooltip>
                    </div>
                  </template>
                </MsCascader>
              </a-form-item>
            </template>
          </a-form>
          <div class="mb-[16px] flex items-center gap-[16px] bg-[var(--color-text-n9)] p-[5px_8px]">
            <div class="text-[var(--color-text-3)]">{{ t('ms.paramsInput.preview') }}</div>
            <div class="text-[var(--color-text-1)]">{{ paramPreview }}</div>
          </div>
        </div>
        <div class="flex items-center justify-end gap-[8px]">
          <a-button type="secondary" size="mini" @click="cancel">{{ t('common.cancel') }}</a-button>
          <a-button type="primary" size="mini" @click="apply">{{ t('ms.paramsInput.apply') }}</a-button>
        </div>
      </div>
    </template>
  </a-trigger>
  <a-popover
    v-model:popup-visible="popoverVisible"
    position="tl"
    :disabled="disabledPopover"
    class="ms-params-input-popover"
  >
    <template #content>
      <div class="ms-params-popover-title !mb-[8px]">
        {{ t('ms.paramsInput.value') }}
      </div>
      <div class="ms-params-popover-subtitle">
        {{ t('ms.paramsInput.value') }}
      </div>
      <div class="ms-params-popover-value mb-[8px]">
        {{ innerValue }}
      </div>
      <div class="ms-params-popover-subtitle">
        {{ t('ms.paramsInput.preview') }}
      </div>
      <div class="ms-params-popover-value">
        {{ innerValue }}
      </div>
    </template>
    <a-auto-complete
      ref="autoCompleteRef"
      v-model:model-value="innerValue"
      :data="autoCompleteParams"
      :placeholder="t('ms.paramsInput.placeholder', { at: '@' })"
      :class="`ms-params-input ${paramSettingVisible ? 'ms-params-input--focus' : ''}`"
      :trigger-props="{ contentClass: 'ms-params-input-trigger' }"
      :filter-option="false"
      @search="handleSearchParams"
      @change="(val) => emit('change', val)"
      @select="selectAutoComplete"
    >
      <template #suffix>
        <MsIcon type="icon-icon_mock" class="ms-params-input-suffix-icon" @click.stop="openParamSetting" />
      </template>
      <template #option="{ data }">
        <div class="w-[350px]">
          {{ data.raw.value }}
          <a-tooltip :content="t(data.raw.desc)" position="bl" :mouse-enter-delay="300">
            <div class="one-line-text max-w-[320px] text-[12px] leading-[16px] text-[var(--color-text-4)]">
              {{ t(data.raw.desc) }}
            </div>
          </a-tooltip>
        </div>
      </template>
    </a-auto-complete>
  </a-popover>
</template>

<script setup lang="ts">
  import { useEventListener, useVModel } from '@vueuse/core';
  import { cloneDeep } from 'lodash-es';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsCascader from '@/components/business/ms-cascader/index.vue';
  import paramsInputGroup from './paramsInputGroup.vue';

  import { useI18n } from '@/hooks/useI18n';

  import {
    JMeterAllGroup,
    JMeterAllVars,
    mockAllGroup,
    mockAllParams,
    mockFunctions,
    specialStringVars,
  } from './config';
  import type { MockParamInputGroupItem, MockParamItem } from './types';
  import type { AutoComplete, CascaderOption, FormInstance } from '@arco-design/web-vue';

  const props = defineProps<{
    value: string;
  }>();
  const emit = defineEmits<{
    (e: 'update:value', val: string): void;
    (e: 'change', val: string): void;
    (e: 'dblclick'): void;
    (e: 'apply', val: string): void;
  }>();

  const { t } = useI18n();
  const innerValue = useVModel(props, 'value', emit);
  const autoCompleteParams = ref<MockParamItem[]>([]);
  const isFocusAutoComplete = ref(false);
  const popoverVisible = ref(false);
  const lastTenParams = ref<MockParamItem[]>(JSON.parse(localStorage.getItem('ms-lastTenParams') || '[]')); // 用户最近使用的前 10 个变量

  /**
   * 搜索变量
   * @param val 变量名
   */
  function handleSearchParams(val: string) {
    if (val === '@') {
      autoCompleteParams.value = [...lastTenParams.value];
      if (autoCompleteParams.value.length < 10) {
        // 最近使用的函数列表不满 10 条，补全
        let lastLength = 10 - autoCompleteParams.value.length; // 剩余需要补全的长度
        for (let i = 0; i < mockAllParams.length; i++) {
          const mockParam = mockAllParams[i];
          if (!autoCompleteParams.value.find((e) => e.value === mockParam.value)) {
            // 避免重复
            autoCompleteParams.value.push(mockParam);
            lastLength--;
          }
          if (lastLength === 0) {
            break;
          }
        }
      }
    } else if (/^@/.test(val)) {
      autoCompleteParams.value = mockAllParams.filter((e) => {
        return e.value.includes(val);
      });
    } else {
      autoCompleteParams.value = [];
    }
  }

  /**
   * 设置最近使用的前 10 个变量
   * @param val 变量名
   */
  function setLastTenParams(val: string) {
    const index = lastTenParams.value.findIndex((e) => e.value === val);
    const lastParamsItem = lastTenParams.value.find((e) => e.value === val);
    if (index > -1 && lastParamsItem) {
      // 如果已经存在，移动到第一位
      lastTenParams.value.splice(index, 1, lastParamsItem);
    } else {
      // 如果不存在，添加到第一位
      const mockParamItem = mockAllParams.find((e) => e.value === val);
      if (mockParamItem) {
        lastTenParams.value.unshift(mockParamItem);
        if (lastTenParams.value.length > 10) {
          // 如果超过 10 个，删除最后一个
          lastTenParams.value.pop();
        }
      }
    }
    localStorage.setItem('ms-lastTenParams', JSON.stringify(lastTenParams.value));
  }

  function selectAutoComplete(val: string) {
    innerValue.value = val;
    setLastTenParams(val);
  }

  const autoCompleteRef = ref<InstanceType<typeof AutoComplete>>();

  onMounted(() => {
    useEventListener(autoCompleteRef.value?.inputRef, 'dblclick', () => {
      emit('dblclick');
    });
    const autoCompleteInput = (autoCompleteRef.value?.inputRef as any)?.$el.querySelector('.arco-input');
    // 设置输入框聚焦状态，聚焦时不显示参数预览的 popover
    useEventListener(autoCompleteInput, 'focus', () => {
      isFocusAutoComplete.value = true;
      popoverVisible.value = false;
    });
    useEventListener(autoCompleteInput, 'blur', () => {
      isFocusAutoComplete.value = false;
    });
  });

  const disabledPopover = computed(() => {
    return !innerValue.value || innerValue.value.trim() === '' || isFocusAutoComplete.value;
  });

  const paramSettingVisible = ref(false);
  const paramSettingType = ref<'mock' | 'jmeter'>('mock');
  const defaultParamForm = {
    type: '',
    JMeterType: '',
    param1: '',
    param2: '',
    param3: '',
    param4: '',
    func: '',
    funcParam1: '',
    funcParam2: '',
  };
  const paramForm = ref<Record<string, any>>({ ...defaultParamForm });
  const paramFormRef = ref<FormInstance>();
  const paramTypeOptions: CascaderOption[] = cloneDeep(mockAllGroup);
  const paramFuncOptions: MockParamItem[] = cloneDeep(mockFunctions);
  const paramPreview = ref('xsxsxsxs');
  const currentParamsInputGroup = ref<MockParamInputGroupItem[]>([]);

  /**
   * 切换变量类型，设置变量输入框的输入组
   * @param val 变量类型
   */
  function handleParamTypeChange(val: string) {
    paramForm.value.type = val;
    // 匹配@开头的函数名
    const regex = /@([a-zA-Z]+)(\([^)]*\))?/;
    const currentParamType = mockAllParams.find((e) => {
      if (specialStringVars.includes(val)) {
        return e.value === val;
      }
      if (e.value.match(regex)?.[1] === val.match(regex)?.[1]) {
        paramForm.value.type = e.value;
        return true;
      }
      return false;
    });
    if (currentParamType) {
      currentParamsInputGroup.value = currentParamType.inputGroup || [];
    } else {
      currentParamsInputGroup.value = [];
    }
    paramForm.value = {
      ...paramForm.value,
      param1: '',
      param2: '',
      param3: '',
      param4: '',
    };
  }

  function handleJMeterTypeChange(val: string) {
    paramForm.value.JMeterType = val;
  }

  const currentParamsFuncInputGroup = ref<MockParamInputGroupItem[]>([]);

  /**
   * 切换函数，设置函数输入框的输入组
   * @param val 函数
   */
  function handleParamFuncChange(val: string) {
    paramForm.value.func = val;
    const currentParamFunc = mockFunctions.find((e) => e.value === val);
    if (currentParamFunc) {
      currentParamsFuncInputGroup.value = cloneDeep(currentParamFunc.inputGroup) || [];
    } else {
      currentParamsFuncInputGroup.value = [];
    }
    paramForm.value = {
      ...paramForm.value,
      funcParam1: '',
      funcParam2: '',
    };
  }

  /**
   * 打开变量设置弹窗
   */
  function openParamSetting() {
    if (/^\$/.test(innerValue.value)) {
      paramSettingType.value = 'jmeter';
      if (JMeterAllVars.findIndex((e) => e.value === innerValue.value) !== -1) {
        paramForm.value.JMeterType = innerValue.value;
      } else {
        paramForm.value.JMeterType = '';
      }
    } else if (/^@/.test(innerValue.value)) {
      const valueArr = innerValue.value.split('|'); // 分割 mock变量和函数
      if (valueArr[0]) {
        // 匹配@开头的变量名
        const variableRegex = /@([a-zA-Z]+)(?:\(([^)]*)\))?/;
        const variableMatch = valueArr[0].match(variableRegex);

        if (variableMatch) {
          const variableName = variableMatch[1];
          const variableParams = variableMatch[2]?.split(',').map((param) => param.trim());

          if (variableName === 'character') {
            handleParamTypeChange(`@${variableName}(${variableParams})`); // character变量特殊处理
          } else {
            handleParamTypeChange(`@${variableName}`); // 设置匹配的变量参数输入框组
          }
          if (variableName !== 'character' || (variableName === 'character' && variableParams?.[0] !== 'pool')) {
            // 字符串变量@character(pool)特殊处理，不需要填入 pool
            (variableParams || []).forEach((e, i) => {
              // 设置变量入参
              paramForm.value[`param${i + 1}`] = Number.isNaN(Number(e)) ? e : Number(e);
            });
          }
        }
      }
      if (valueArr[1]) {
        // 匹配函数名和参数
        const functionRegex = /([a-zA-Z]+)(?:\(([^)]*)\))?/;
        const functionMatch = valueArr[1].match(functionRegex);

        if (functionMatch) {
          const functionName = functionMatch[1];
          const functionParams = functionMatch[2]?.split(',').map((param) => param.trim());

          handleParamFuncChange(functionName); // 设置匹配的函数输入框组
          (functionParams || []).forEach((e, i) => {
            // 设置函数入参
            paramForm.value[`funcParam${i + 1}`] = Number.isNaN(Number(e)) ? e : Number(e);
          });
        }
      }
    }
    paramSettingVisible.value = true;
  }

  function handleParamSettingChange() {
    paramFormRef.value?.clearValidate();
  }

  const JMeterVarsOptions: CascaderOption[] = cloneDeep(JMeterAllGroup);

  function cancel() {
    paramFormRef.value?.resetFields();
    paramSettingType.value = 'mock';
    paramSettingVisible.value = false;
    currentParamsInputGroup.value = [];
    currentParamsFuncInputGroup.value = [];
    paramForm.value = { ...defaultParamForm };
  }

  /**
   * 应用 Mock类型变量和函数
   */
  function applyMock() {
    let resultStr = '';
    // 如果选择的变量需要添加入参
    if (currentParamsInputGroup.value.length > 0) {
      const testReg = /\(([^)]+)\)/;
      const paramVal = [paramForm.value.param1, paramForm.value.param2, paramForm.value.param3, paramForm.value.param4]
        .filter((e) => e !== '')
        .join(',');
      // 如果变量名是包含了入参的，则替换()内的入参为用户输入的
      resultStr = paramVal !== '' ? paramForm.value.type.replace(testReg, `(${paramVal})`) : paramForm.value.type;
    } else {
      resultStr = paramForm.value.type;
    }
    if (paramForm.value.func !== '') {
      // 如果选择了添加函数
      resultStr = `${resultStr}|${paramForm.value.func}`;
      if (currentParamsFuncInputGroup.value.length > 0 && !Number.isNaN(paramForm.value.funcParam1)) {
        // 如果添加的函数还有入参
        resultStr = `${resultStr}(${[paramForm.value.funcParam1, paramForm.value.funcParam2]
          .filter((e) => e !== '' && !Number.isNaN(e))
          .join(',')})`;
      }
    }
    return resultStr;
  }

  function apply() {
    paramFormRef.value?.validate((errors) => {
      if (!errors) {
        let result = '';
        if (paramSettingType.value === 'mock') {
          result = applyMock();
        } else {
          result = paramForm.value.JMeterType;
        }
        setLastTenParams(paramForm.value.type);
        innerValue.value = result;
        emit('apply', result);
        cancel();
      }
    });
  }
</script>

<style lang="less">
  .ms-params-input-popover {
    .arco-trigger-popup-wrapper {
      .arco-popover-popup-content {
        padding: 4px 8px;
      }
    }
  }
  .ms-params-input-setting-trigger {
    @apply bg-white;
    .ms-params-input-setting-trigger-content {
      padding: 16px;
      width: 480px;
      border-radius: var(--border-radius-medium);
      box-shadow: 0 5px 5px -3px rgb(0 0 0 / 10%), 0 8px 10px 1px rgb(0 0 0 / 6%), 0 3px 14px 2px rgb(0 0 0 / 5%);
      &::before {
        @apply absolute left-0 top-0;

        content: '';
        z-index: -1;
        width: 200%;
        height: 200%;
        border: 1px solid var(--color-text-input-border);
        border-radius: 12px;
        transform-origin: 0 0;
        transform: scale(0.5, 0.5);
      }
      .ms-params-input-setting-trigger-content-scroll {
        .ms-scroll-bar();

        overflow-y: auto;
        margin-right: -6px;
        max-height: 400px;
      }
    }
  }
  .ms-params-input:not(.arco-input-focus) {
    border-color: transparent;
    &:not(:hover) {
      .arco-input::placeholder {
        @apply invisible;
      }

      border-color: transparent;
    }
  }
  .ms-params-input {
    .ms-params-input-suffix-icon,
    .ms-params-input-suffix-icon--disabled {
      @apply invisible;
    }
    &:hover,
    &.arco-input-focus {
      .ms-params-input-suffix-icon {
        @apply visible cursor-pointer;
        &:hover {
          color: rgb(var(--primary-5));
        }
      }
      .ms-params-input-suffix-icon--disabled {
        @apply visible cursor-not-allowed;

        color: rgb(var(--primary-3));
      }
    }
    :deep(.arco-select-option) {
      @apply flex flex-1 p-10;
    }
  }
  .ms-params-input--focus {
    border-color: rgb(var(--primary-5)) !important;
    .ms-params-input-suffix-icon {
      @apply visible cursor-pointer;

      color: rgb(var(--primary-5));
    }
    .ms-params-input-suffix-icon--disabled {
      @apply visible cursor-not-allowed;

      color: rgb(var(--primary-3));
    }
  }
  .ms-params-input-trigger {
    width: 350px;
    .arco-select-dropdown-list {
      .arco-select-option {
        @apply !h-auto;

        padding: 2px 8px !important;
      }
    }
  }
  .ms-params-popover-title {
    @apply font-medium;

    margin-bottom: 4px;
    font-size: 12px;
    font-weight: 500;
    line-height: 16px;
    color: var(--color-text-1);
  }
  .ms-params-popover-subtitle {
    margin-bottom: 2px;
    font-size: 12px;
    line-height: 16px;
    color: var(--color-text-4);
  }
  .ms-params-popover-value {
    min-width: 100px;
    max-width: 280px;
    font-size: 12px;
    line-height: 16px;
    color: var(--color-text-1);
  }
</style>
