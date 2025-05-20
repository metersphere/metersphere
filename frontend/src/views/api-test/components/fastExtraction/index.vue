<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :width="720"
    :show-full-screen="true"
    :title="t('apiTestDebug.fastExtraction')"
    disabled-width-drag
    @confirm="confirmHandler"
    @close="closeHandler"
  >
    <div v-if="expressionForm.extractType === RequestExtractExpressionEnum.REGEX" class="h-[400px]">
      <MsCodeEditor
        :model-value="props.response"
        theme="vs"
        height="336px"
        :languages="[LanguageEnum.JSON, LanguageEnum.HTML, LanguageEnum.XML, LanguageEnum.PLAINTEXT]"
        :language="LanguageEnum.JSON"
        :show-full-screen="false"
        show-language-change
        read-only
      />
    </div>
    <div v-else-if="expressionForm.extractType === RequestExtractExpressionEnum.JSON_PATH" class="code-container">
      <MsJsonPathPicker
        :data="props.response || ''"
        class="bg-[var(--color-text-fff)]"
        @init="initJsonPath"
        @pick="handlePathPick"
      />
    </div>
    <div v-else-if="expressionForm.extractType === RequestExtractExpressionEnum.X_PATH" class="code-container">
      <MsXPathPicker
        :xml-string="props.response || ''"
        class="bg-[var(--color-text-fff)]"
        @init="initXpath"
        @pick="handlePathPick"
      />
    </div>
    <a-form ref="expressionFormRef" :model="expressionForm" layout="vertical" class="mt-[16px]">
      <a-form-item
        v-if="expressionForm.extractType === RequestExtractExpressionEnum.REGEX"
        field="expression"
        :label="t('apiTestDebug.regularExpression')"
        :rules="[{ required: true, message: t('apiTestDebug.regularExpressionRequired') }]"
        asterisk-position="end"
      >
        <div class="form-input-wrapper">
          <a-input
            v-model:model-value="expressionForm.expression"
            :max-length="255"
            :placeholder="t('apiTestDebug.regularExpressionPlaceholder', { ex: '/<title>(.*?)</title>/' })"
            class="flex-1"
          />
          <a-button type="outline" :disabled="expressionForm.expression.trim() === ''" @click="testExpression">
            {{ t('apiTestDebug.test') }}
          </a-button>
        </div>
      </a-form-item>
      <a-form-item
        v-else-if="expressionForm.extractType === RequestExtractExpressionEnum.JSON_PATH"
        field="expression"
        label="JSONPath"
        :rules="[{ required: true, message: t('apiTestDebug.JSONPathRequired') }]"
        asterisk-position="end"
      >
        <div class="form-input-wrapper">
          <a-input
            v-model:model-value="expressionForm.expression"
            :max-length="255"
            :placeholder="t('apiTestDebug.JSONPathPlaceholder')"
            class="flex-1"
          />
          <a-button type="outline" :disabled="expressionForm.expression.trim() === ''" @click="testExpression">
            {{ t('apiTestDebug.test') }}
          </a-button>
        </div>
      </a-form-item>
      <a-form-item
        v-else
        field="expression"
        label="XPath"
        :rules="[{ required: true, message: t('apiTestDebug.XPathRequired') }]"
        asterisk-position="end"
      >
        <div class="form-input-wrapper">
          <a-input
            v-model:model-value="expressionForm.expression"
            :max-length="255"
            :placeholder="t('apiTestDebug.XPathPlaceholder')"
            class="flex-1"
          />
          <a-button type="outline" :disabled="expressionForm.expression.trim() === ''" @click="testExpression">
            {{ t('apiTestDebug.test') }}
          </a-button>
        </div>
      </a-form-item>
    </a-form>
    <div class="rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] p-[12px]">
      <div class="flex items-center justify-between">
        <div class="flex items-center">
          <div class="text-[var(--color-text-1)]">{{ t('apiTestDebug.matchResult') }}</div>
          <a-tooltip
            v-if="expressionForm.extractType === RequestExtractExpressionEnum.REGEX"
            :content-style="{ maxWidth: '500px' }"
          >
            <icon-question-circle
              class="ml-[4px] cursor-pointer text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
              size="16"
            />
            <template #content>
              <div
                >{{ t('apiTestDebug.matchExpressionTip', { prefix: `${t('apiTestDebug.matchExpression')}: ` }) }}
              </div>
              <div>{{ t('apiTestDebug.matchGroupTip', { prefix: `${t('apiTestDebug.matchGroup')}: ` }) }}</div>
            </template>
          </a-tooltip>
        </div>
        <a-radio-group
          v-if="expressionForm.extractType === RequestExtractExpressionEnum.REGEX"
          v-model:model-value="expressionForm.expressionMatchingRule"
          type="button"
          size="small"
        >
          <a-radio :value="RequestExtractExpressionRuleType.EXPRESSION">
            {{ t('apiTestDebug.matchExpression') }}
          </a-radio>
          <a-radio :value="RequestExtractExpressionRuleType.GROUP">{{ t('apiTestDebug.matchGroup') }}</a-radio>
        </a-radio-group>
      </div>
      <div class="match-result">
        <div v-if="isMatched && matchResult.length === 0">{{ t('apiTestDebug.noMatchResult') }}</div>
        <template v-else-if="props.config.extractType === RequestExtractExpressionEnum.JSON_PATH">
          <pre>{{ matchResult }}</pre>
        </template>
        <template v-else>
          <pre v-for="(e, i) of matchResult" :key="i">{{ `${e}` }}</pre>
        </template>
      </div>
    </div>
    <a-collapse
      v-if="props.isShowMoreSetting"
      v-model:active-key="moreSettingActive"
      :bordered="false"
      :show-expand-icon="false"
      class="mt-[16px]"
    >
      <a-collapse-item :key="1">
        <template #header>
          <MsButton
            type="text"
            @click="() => (moreSettingActive.length > 0 ? (moreSettingActive = []) : (moreSettingActive = [1]))"
          >
            {{ t('apiTestDebug.moreSetting') }}
            <icon-down v-if="moreSettingActive.length > 0" class="text-rgb(var(--primary-5))" />
            <icon-right v-else class="text-rgb(var(--primary-5))" />
          </MsButton>
        </template>
        <div class="mt-[16px]">
          <moreSetting v-model:config="expressionForm" :is-show-result-match-rules="false" />
        </div>
      </a-collapse-item>
    </a-collapse>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';
  import { FormInstance, Message, ValidatedError } from '@arco-design/web-vue';
  import { JSONPath } from 'jsonpath-plus';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsJsonPathPicker from '@/components/pure/ms-jsonpath-picker/index.vue';
  import MsXPathPicker from '@/components/pure/ms-jsonpath-picker/xpath.vue';
  import moreSetting from './moreSetting.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { extractTextFromHtmlWithXPath, matchXMLWithXPath } from '@/utils/xpath';

  import type { JSONPathExtract, RegexExtract, XPathExtract } from '@/models/apiTest/common';
  import { RequestExtractExpressionEnum, RequestExtractExpressionRuleType } from '@/enums/apiEnum';

  export type ExtractParamConfig = (RegexExtract | JSONPathExtract | XPathExtract) & Record<string, any>;

  const props = withDefaults(
    defineProps<{
      visible: boolean;
      config: ExtractParamConfig;
      response?: string; // 响应内容
      isShowMoreSetting?: boolean; // 是否展示更多设置
    }>(),
    {
      isShowMoreSetting: true,
    }
  );
  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void;
    (e: 'apply', config: ExtractParamConfig, matchResult: any[] | string): void;
  }>();

  const { t } = useI18n();

  const innerVisible = useVModel(props, 'visible', emit);
  const expressionForm = ref({ ...props.config });
  const expressionFormRef = ref<FormInstance | null>(null);
  const parseJson = ref<string | Record<string, any>>({});
  const isHtml = ref(false);
  const matchResult = ref<any[] | string>([]); // 当前匹配结果
  const isMatched = ref(false); // 是否执行过匹配

  watch(
    () => props.visible,
    (val) => {
      if (val) {
        expressionForm.value = { ...props.config };
        matchResult.value = [];
        isMatched.value = false;
      }
    }
  );

  function initJsonPath(_parseJson: string | Record<string, any>) {
    parseJson.value = _parseJson;
  }

  function initXpath(type: 'xml' | 'html') {
    isHtml.value = type === 'html';
  }

  function handlePathPick(path: string, _parseJson: string | Record<string, any>) {
    expressionForm.value.expression = path;
    parseJson.value = _parseJson;
    expressionFormRef.value?.clearValidate();
  }

  /**
   * 遍历 JSON 对象，将 Number() 转换为数字
   * @param obj JSON 对象
   */
  function traverseJSONObject(obj: Record<string, any>) {
    try {
      Object.keys(obj).forEach((key) => {
        const val = obj[key];
        if (Object.prototype.hasOwnProperty.call(obj, key)) {
          if (typeof val === 'object' && val !== null) {
            traverseJSONObject(val);
          } else if (val.includes('Number(')) {
            obj[key] = val.replace(/Number\(([^)]+)\)/g, '$1');
            if (!Number.isNaN(Number(obj[key]))) {
              obj[key] = Number(obj[key]);
            }
          }
        }
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(`遍历 JSON 对象异常：${error}`);
    }
  }

  /*
   * 测试表达式
   */
  function testExpression() {
    switch (props.config.extractType) {
      case RequestExtractExpressionEnum.X_PATH:
        const nodes = isHtml.value
          ? extractTextFromHtmlWithXPath(props.response || '', expressionForm.value.expression)
          : matchXMLWithXPath(props.response || '', expressionForm.value.expression);
        if (nodes) {
          // 直接匹配到文本信息
          if (typeof nodes === 'boolean' || typeof nodes === 'string' || typeof nodes === 'number') {
            matchResult.value = [nodes];
          } else if (Array.isArray(nodes)) {
            // 匹配到多个节点信息
            matchResult.value = nodes.map((node) => node.firstChild?.nodeValue || false).filter(Boolean);
          } else {
            // 匹配到单个节点信息
            matchResult.value = nodes.textContent ? [nodes.textContent] : [];
          }
        } else {
          matchResult.value = [];
        }
        break;
      case RequestExtractExpressionEnum.JSON_PATH:
        try {
          const results = JSONPath({
            json: parseJson.value,
            path: expressionForm.value.expression,
            wrap: false,
          });
          if (Array.isArray(results)) {
            matchResult.value = results.map((e: any) => {
              let res;
              if (typeof e === 'object' && e !== null && e !== undefined) {
                res = JSON.parse(
                  JSON.stringify(e)
                    .replace(/Number\(([^)]+)\)/g, '$1')
                    .replace(/^"|"$/g, '')
                );
              } else {
                res = JSON.stringify(e)
                  .replace(/Number\(([^)]+)\)/g, '$1')
                  .replace(/^"|"$/g, '');
              }
              return res;
            });
          } else if (typeof results === 'object' && results !== null) {
            traverseJSONObject(results);
            matchResult.value = results;
          } else if (typeof results === 'string' && results.includes('Number(')) {
            matchResult.value = results.replace(/Number\(([^)]+)\)/g, '$1');
          } else {
            matchResult.value = results === null || results === false ? `${results}` : results || [];
          }
        } catch (error) {
          matchResult.value = JSONPath({ json: props.response || '', path: expressionForm.value.expression }) || [];
        }
        break;
      case RequestExtractExpressionEnum.REGEX:
      default:
        // 正则匹配中如果正则表达式不合法，会抛出异常，影响页面显示。这里便捕获异常，不影响页面显示
        try {
          // 先把前后的/和g去掉才能生成正则表达式
          const matchesIterator = props.response?.matchAll(
            new RegExp(expressionForm.value.expression.replace(/^\/|\/$|\/g$/g, ''), 'g')
          );
          if (matchesIterator) {
            const matches = Array.from(matchesIterator);
            try {
              if (expressionForm.value.expressionMatchingRule === 'EXPRESSION') {
                // 匹配表达式，取第一个匹配结果，是完整匹配结果
                matchResult.value = matches.map((e) => e[0]) || [];
              } else {
                matchResult.value = matches.map((e) => e.slice(1)).flat(Infinity) || []; // 匹配分组，取匹配结果的第二项开始，是分组匹配结果
              }
            } catch (error) {
              // 读取匹配数据错误说明无对应的匹配结果
              matchResult.value = [];
              isMatched.value = true;
            }
          } else {
            matchResult.value = [];
          }
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(`正则匹配异常：${error}`);
          matchResult.value = [];
        }
        break;
    }
    if (matchResult.value.length > 0) {
      Message.success(t('apiTestDebug.testSuccess'));
    }
    isMatched.value = true;
  }

  const moreSettingActive = ref<number[]>([]);
  function confirmHandler() {
    expressionFormRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (!errors) {
        emit(
          'apply',
          expressionForm.value,
          typeof matchResult.value === 'object' ? JSON.stringify(matchResult.value) : matchResult.value
        );
      }
    });
  }

  function closeHandler() {
    expressionFormRef.value?.resetFields();
  }
</script>

<style lang="less" scoped>
  .form-input-wrapper {
    @apply flex w-full items-center justify-between;

    gap: 12px;
  }
  .code-container {
    @apply overflow-y-auto;
    .ms-scroll-bar();

    padding: 12px;
    max-height: 400px;
    border-radius: var(--border-radius-small);
    background-color: var(--color-text-n9);
  }
  .match-result {
    @apply overflow-y-auto;
    .ms-scroll-bar();

    margin-top: 12px;
    padding: 12px;
    min-height: 32px;
    max-height: 300px;
    border-radius: var(--border-radius-small);
    color: rgb(var(--primary-5));
    background-color: var(--color-text-fff);
  }
</style>
