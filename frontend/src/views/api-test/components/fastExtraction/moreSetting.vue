<template>
  <div>
    <div v-if="expressionForm.extractType === RequestExtractExpressionEnum.REGEX && props.isPopover" class="mb-[16px]">
      <div class="mb-[8px] text-[14px] text-[var(--color-text-1)]">
        {{ t('apiTestDebug.expressionMatchRule') }}
      </div>
      <a-radio-group v-model:model-value="expressionForm.expressionMatchingRule" size="small">
        <a-radio :value="RequestExtractExpressionRuleType.EXPRESSION">
          <div class="flex items-center">
            {{ t('apiTestDebug.matchExpression') }}
            <a-tooltip :content="t('apiTestDebug.matchExpressionTip')" :content-style="{ maxWidth: '500px' }">
              <icon-question-circle
                class="ml-[4px] cursor-pointer text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
                size="16"
              />
            </a-tooltip>
          </div>
        </a-radio>
        <a-radio :value="RequestExtractExpressionRuleType.GROUP">
          <div class="flex items-center">
            {{ t('apiTestDebug.matchGroup') }}
            <a-tooltip :content="t('apiTestDebug.matchGroupTip')" :content-style="{ maxWidth: '500px' }">
              <icon-question-circle
                class="ml-[4px] cursor-pointer text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
                size="16"
              />
            </a-tooltip>
          </div>
        </a-radio>
      </a-radio-group>
    </div>
    <div class="mb-[16px]">
      <div class="mb-[8px] text-[14px] text-[var(--color-text-1)]">
        {{ t('apiTestDebug.resultMatchRule') }}
      </div>
      <a-radio-group v-model:model-value="expressionForm.resultMatchingRule" size="small">
        <a-radio :value="RequestExtractResultMatchingRule.RANDOM">
          <div class="flex items-center">
            {{ t('apiTestDebug.randomMatch') }}
            <a-tooltip :content="t('apiTestDebug.randomMatchTip')" :content-style="{ maxWidth: '400px' }">
              <icon-question-circle
                class="ml-[4px] cursor-pointer text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
                size="16"
              />
            </a-tooltip>
          </div>
        </a-radio>
        <a-radio :value="RequestExtractResultMatchingRule.SPECIFIC">
          <div class="flex items-center text-[14px]">
            {{ t('apiTestDebug.specifyMatch') }}
            <a-tooltip :content="t('apiTestDebug.specifyMatchTip')" :content-style="{ maxWidth: '400px' }">
              <icon-question-circle
                class="ml-[4px] cursor-pointer text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
                size="16"
              />
            </a-tooltip>
          </div>
        </a-radio>
        <a-radio :value="RequestExtractResultMatchingRule.ALL">
          <div class="flex items-center">
            {{ t('apiTestDebug.allMatch') }}
            <a-tooltip :content="t('apiTestDebug.allMatchTip')" :content-style="{ maxWidth: '400px' }">
              <icon-question-circle
                class="ml-[4px] cursor-pointer text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
                size="16"
              />
            </a-tooltip>
          </div>
        </a-radio>
      </a-radio-group>
    </div>
    <div v-if="expressionForm.resultMatchingRule === RequestExtractResultMatchingRule.SPECIFIC" class="mb-[16px]">
      <div class="mb-[8px] text-[var(--color-text-1)]">
        {{ t('apiTestDebug.specifyMatchResult') }}
      </div>
      <div class="flex items-center gap-[8px]">
        {{ t('apiTestDebug.index') }}
        <a-input-number
          v-model:model-value="expressionForm.resultMatchingRuleNum"
          :default-value="1"
          :min="1"
          :max="2147483647"
          :precision="0"
          class="w-[80px]"
          @blur="inputNotNull"
        />
        {{ t('apiTestDebug.unit') }}
      </div>
    </div>
    <div v-if="expressionForm.extractType === RequestExtractExpressionEnum.X_PATH" class="mb-[16px]">
      <div class="mb-[8px] text-[14px] text-[var(--color-text-1)]">
        {{ t('apiTestDebug.contentType') }}
      </div>
      <a-radio-group v-model:model-value="expressionForm.responseFormat" size="small">
        <a-radio :value="ResponseBodyXPathAssertionFormat.XML"> {{ ResponseBodyXPathAssertionFormat.XML }} </a-radio>
        <a-radio :value="ResponseBodyXPathAssertionFormat.HTML"> {{ ResponseBodyXPathAssertionFormat.HTML }} </a-radio>
      </a-radio-group>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import { useI18n } from '@/hooks/useI18n';

  import { JSONPathExtract, RegexExtract, XPathExtract } from '@/models/apiTest/common';
  import {
    RequestExtractExpressionEnum,
    RequestExtractExpressionRuleType,
    RequestExtractResultMatchingRule,
    ResponseBodyXPathAssertionFormat,
  } from '@/enums/apiEnum';

  export type ExpressionConfig = (RegexExtract | JSONPathExtract | XPathExtract) & Record<string, any>;

  const props = defineProps<{
    config: ExpressionConfig;
    isPopover?: boolean; // 是否是弹出框展示，弹出框展示时才显示表达式类型
  }>();
  const emit = defineEmits<{
    (e: 'update:config', config: ExpressionConfig): void;
  }>();

  const { t } = useI18n();

  const expressionForm = useVModel(props, 'config', emit);

  function inputNotNull() {
    if (!expressionForm.value.resultMatchingRuleNum) {
      expressionForm.value.resultMatchingRuleNum = 1;
    }
  }
</script>

<style lang="less" scoped></style>
