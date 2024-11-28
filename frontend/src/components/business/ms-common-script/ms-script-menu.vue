<template>
  <div v-if="!innerExpand" class="w-[32%] min-w-[260px] max-w-[42%] flex-1 bg-[var(--color-text-fff)] p-3 pl-0">
    <div class="mb-2 flex w-full items-center justify-between">
      <div class="flex items-center">
        <span v-if="innerExpand" class="collapsebtn mr-1 flex items-center justify-center" @click="expandedHandler">
          <icon-right class="text-[12px] text-[var(--color-text-4)]" />
        </span>
        <span v-else class="expand mr-1 flex items-center justify-center" @click="expandedHandler">
          <icon-down class="text-[12px] text-[rgb(var(--primary-6))]" />
        </span>
        <div class="one-line-text font-medium">{{ t('project.commonScript.codeSnippet') }}</div>
      </div>

      <div class="ml-[24px] flex-1">
        <a-select
          v-model="innerLanguageType"
          :disabled="props.disabled"
          :placeholder="t('project.commonScript.pleaseSelected')"
          @change="changeHandler"
        >
          <a-option v-for="item of languages" :key="item.value" :value="item.value">
            <a-tooltip :content="item.text">
              {{ item.text }}
            </a-tooltip>
          </a-option>
        </a-select>
      </div>
    </div>
    <div class="p-[12px] pt-0">
      <div v-for="item of scriptMenus" :key="item.value" class="menuItem px-1" @click="handleClick(item)">
        {{ item.title }}
      </div>
    </div>
  </div>
  <span
    v-if="innerExpand"
    class="collapsebtn absolute right-2 top-2 z-10 mr-1 flex items-center justify-center"
    @click="expandedHandler"
  >
    <icon-right class="text-[12px] text-[var(--color-text-4)]" />
  </span>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';

  import { Language, LanguageEnum } from '@/components/pure/ms-code-editor/types';

  import { useI18n } from '@/hooks/useI18n';

  import type { CommonScriptMenu } from './types';
  import { getCodeTemplate, getScriptMenu } from './utils';

  const { t } = useI18n();

  const props = defineProps<{
    expand: boolean;
    disabled?: boolean;
    languagesType: Language;
  }>();

  const emit = defineEmits<{
    (e: 'update:expand', value: boolean): void;
    (e: 'update:languagesType', value: Language): void;
    (e: 'insert', code: string): void;
    (e: 'formApiImport'): void; // 从api 定义导入
    (e: 'insertCommonScript'): void; // 插入公共脚本
    (e: 'updateLanguages', value: Language): void; // 更新语言类型
  }>();

  const innerExpand = useVModel(props, 'expand', emit);

  const innerLanguageType = useVModel(props, 'languagesType', emit);

  const languages = [
    { text: 'BeanShell-JSR223', value: LanguageEnum.BEANSHELL_JSR233 },
    { text: 'BeanShell', value: LanguageEnum.BEANSHELL },
    { text: 'Python3', value: LanguageEnum.PYTHON },
    { text: 'Groovy', value: LanguageEnum.GROOVY },
    { text: 'JavaScript', value: LanguageEnum.JAVASCRIPT },
  ];

  const scriptMenus = computed(() => {
    return innerLanguageType.value !== LanguageEnum.PYTHON ? getScriptMenu(';') : getScriptMenu('');
  });

  function expandedHandler() {
    innerExpand.value = !innerExpand.value;
  }

  // 插入公共脚本
  function getCustomFunction() {
    emit('insertCommonScript');
  }

  // 从Api定义导入
  function getApiExport() {
    emit('formApiImport');
  }

  function _handleCommand(command: string) {
    switch (command) {
      // 自定义代码片段
      case 'custom_function':
        getCustomFunction();
        return '';
      // 从API定义导入
      case 'api_definition':
        getApiExport();
        return '';
      // 新API测试[JSON]
      case 'new_api_request': {
        // requestObj为空则生产默认模板
        const headers = new Map();
        headers.set('Content-type', 'application/json');
        return getCodeTemplate(innerLanguageType.value, { requestHeaders: headers });
      }
      case 'api_stop': {
        if (innerLanguageType.value === LanguageEnum.PYTHON) {
          return `
import java
StandardJMeterEngine = java.type('org.apache.jmeter.engine.StandardJMeterEngine')
StandardJMeterEngine.stopThreadNow(ctx.getThread().getThreadName());
          `;
        }
        if (innerLanguageType.value === LanguageEnum.JAVASCRIPT) {
          return `
StandardJMeterEngine = Java.type('org.apache.jmeter.engine.StandardJMeterEngine')
StandardJMeterEngine.stopThreadNow(ctx.getThread().getThreadName());
          `;
        }
        return 'ctx.getEngine().stopThreadNow(ctx.getThread().getThreadName());';
      }
      default:
        return '';
    }
  }

  function handleCodeTemplate(code: string) {
    emit('insert', code);
  }

  function handleClick(obj: CommonScriptMenu) {
    if (props.disabled) return;
    let code = '';
    if (obj.command) {
      code = _handleCommand(obj.command);
      if (!code) {
        return;
      }
    } else {
      if (innerLanguageType.value !== LanguageEnum.BEANSHELL && innerLanguageType.value !== LanguageEnum.GROOVY) {
        if (
          obj.title === t('api_test.request.processor.code_add_report_length') ||
          obj.title === t('api_test.request.processor.code_hide_report_length')
        ) {
          Message.warning(
            `${t('commons.no_corresponding')} ${innerLanguageType.value} ${t('commons.code_template')}！`
          );
          return;
        }
      }
      code = obj.value;
    }
    handleCodeTemplate(code);
  }

  function changeHandler(
    value: string | number | boolean | Record<string, any> | (string | number | boolean | Record<string, any>)[]
  ) {
    innerLanguageType.value = value as Language;
  }
</script>

<style scoped lang="less">
  .collapsebtn {
    width: 16px;
    height: 16px;
    border-radius: 50%;
    @apply cursor-pointer;

    background: var(--color-text-n8) !important;
  }
  .expand {
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: rgb(var(--primary-1));
    @apply cursor-pointer;
  }
  .menuItem {
    height: 24px;
    line-height: 24px;
    color: rgb(var(--primary-5));
    @apply cursor-pointer;
  }
</style>
