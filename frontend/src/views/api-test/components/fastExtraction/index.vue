<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :width="680"
    :title="t('apiTestDebug.fastExtraction')"
    disabled-width-drag
    @confirm="emit('apply', expressionForm)"
  >
    <div v-if="expressionForm.expressionType === 'regular'" class="h-[400px]">
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
    <div v-else-if="expressionForm.expressionType === 'JSONPath'" class="code-container">
      <MsJsonPathPicker :data="props.response || ''" class="bg-white" @pick="handlePathPick" />
    </div>
    <div v-else-if="expressionForm.expressionType === 'XPath'" class="code-container">
      <MsXPathPicker :xml-string="props.response || ''" class="bg-white" @pick="handlePathPick" />
    </div>
    <a-form ref="expressionFormRef" :model="expressionForm" layout="vertical" class="mt-[16px]">
      <a-form-item
        v-if="expressionForm.expressionType === 'regular'"
        field="expression"
        :label="t('apiTestDebug.regularExpression')"
        :rules="[{ required: true, message: t('apiTestDebug.regularExpressionRequired') }]"
        asterisk-position="end"
      >
        <div class="form-input-wrapper">
          <a-input
            v-model:model-value="expressionForm.expression"
            :placeholder="t('apiTestDebug.regularExpressionPlaceholder', { ex: '/<title>(.*?)</title>/' })"
            class="flex-1"
          />
          <a-button type="outline" :disabled="expressionForm.expression.trim() === ''" @click="testExpression">
            {{ t('apiTestDebug.test') }}
          </a-button>
        </div>
      </a-form-item>
      <a-form-item
        v-else-if="expressionForm.expressionType === 'JSONPath'"
        field="expression"
        label="JSONPath"
        :rules="[{ required: true, message: t('apiTestDebug.JSONPathRequired') }]"
        asterisk-position="end"
      >
        <div class="form-input-wrapper">
          <a-input
            v-model:model-value="expressionForm.expression"
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
          <a-tooltip :content-style="{ maxWidth: '500px' }">
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
          v-if="expressionForm.expressionType === 'regular'"
          v-model:model-value="expressionForm.regexpMatchRule"
          type="button"
          size="small"
        >
          <a-radio value="expression">{{ t('apiTestDebug.matchExpression') }}</a-radio>
          <a-radio value="group">{{ t('apiTestDebug.matchGroup') }}</a-radio>
        </a-radio-group>
      </div>
      <div class="match-result">
        <div v-if="isMatched && matchResult.length === 0">{{ t('apiTestDebug.noMatchResult') }}</div>
        <pre v-for="(e, i) of matchResult" :key="i">{{ e }}</pre>
      </div>
    </div>
    <a-collapse v-model:active-key="moreSettingActive" :bordered="false" :show-expand-icon="false" class="mt-[16px]">
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
          <moreSetting v-model:config="expressionForm" />
        </div>
      </a-collapse-item>
    </a-collapse>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';
  import FormInstance from '@arco-design/web-vue';
  import { JSONPath } from 'jsonpath-plus';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsJsonPathPicker from '@/components/pure/ms-jsonpath-picker/index.vue';
  import MsXPathPicker from '@/components/pure/ms-jsonpath-picker/xpath.vue';
  import moreSetting from './moreSetting.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { matchXMLWithXPath } from '@/utils/xpath';

  import { ExpressionConfig } from '@/models/apiTest/debug';

  const props = defineProps<{
    visible: boolean;
    config: ExpressionConfig;
    response?: string; // 响应内容
  }>();
  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void;
    (e: 'apply', config: ExpressionConfig): void;
  }>();

  const { t } = useI18n();

  const innerVisible = useVModel(props, 'visible', emit);
  const expressionForm = ref({ ...props.config });
  const expressionFormRef = ref<typeof FormInstance>();
  const matchResult = ref<any[]>([]); // 当前匹配结果
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

  function handlePathPick(xpath: string) {
    expressionForm.value.expression = xpath;
  }

  /*
   * 测试表达式
   */
  function testExpression() {
    switch (props.config.expressionType) {
      case 'XPath':
        const nodes = matchXMLWithXPath(props.response || '', expressionForm.value.expression);
        if (nodes) {
          // 直接匹配到文本信息
          if (typeof nodes === 'boolean' || typeof nodes === 'string' || typeof nodes === 'number') {
            matchResult.value = [nodes];
          } else if (Array.isArray(nodes)) {
            // 匹配到多个节点信息
            matchResult.value = nodes
              .map((node) => node.textContent?.split('\n') || false)
              .flat(Infinity)
              .filter(Boolean);
          } else {
            // 匹配到单个节点信息
            matchResult.value = nodes.textContent ? [nodes.textContent] : [];
          }
        } else {
          matchResult.value = [];
        }
        break;
      case 'JSONPath':
        try {
          matchResult.value = JSONPath({
            json: props.response ? JSON.parse(props.response) : '',
            path: expressionForm.value.expression,
          });
        } catch (error) {
          matchResult.value = JSONPath({ json: props.response || '', path: expressionForm.value.expression });
        }
        break;
      case 'regular':
      default:
        // 先把前后的/和g去掉才能生成正则表达式
        const matchesIterator = props.response?.matchAll(
          new RegExp(expressionForm.value.expression.replace(/^\/|\/$|\/g$/g, ''), 'g')
        );
        if (matchesIterator) {
          const matches = Array.from(matchesIterator);
          try {
            if (expressionForm.value.regexpMatchRule === 'expression') {
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
        break;
    }
    isMatched.value = true;
  }

  const moreSettingActive = ref<number[]>([]);
</script>

<style lang="less" scoped>
  .form-input-wrapper {
    @apply flex w-full items-center justify-between;

    gap: 12px;
  }
  .code-container {
    padding: 12px;
    max-height: 400px;
    border-radius: var(--border-radius-small);
    background-color: var(--color-text-n9);
  }
  .match-result {
    @apply overflow-y-auto bg-white;
    .ms-scroll-bar();

    margin-top: 12px;
    padding: 12px;
    min-height: 32px;
    max-height: 300px;
    border-radius: var(--border-radius-small);
    color: rgb(var(--primary-5));
  }
</style>
