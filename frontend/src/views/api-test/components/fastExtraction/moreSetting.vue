<template>
  <div>
    <div v-if="expressionForm.expressionType === 'regular' && props.isPopover" class="mb-[16px]">
      <div class="mb-[8px] text-[14px] text-[var(--color-text-1)]">
        {{ t('apiTestDebug.expressionMatchRule') }}
      </div>
      <a-radio-group v-model:model-value="expressionForm.regexpMatchRule" size="small">
        <a-radio value="expression">
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
        <a-radio value="group">
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
      <a-radio-group v-model:model-value="expressionForm.resultMatchRule" size="small">
        <a-radio value="random">
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
        <a-radio value="specify">
          <div class="flex items-center">
            {{ t('apiTestDebug.specifyMatch') }}
            <a-tooltip :content="t('apiTestDebug.specifyMatchTip')" :content-style="{ maxWidth: '400px' }">
              <icon-question-circle
                class="ml-[4px] cursor-pointer text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
                size="16"
              />
            </a-tooltip>
          </div>
        </a-radio>
        <a-radio value="all">
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
    <div v-if="expressionForm.resultMatchRule === 'specify'" class="mb-[16px]">
      <div class="mb-[8px] text-[var(--color-text-1)]">
        {{ t('apiTestDebug.specifyMatchResult') }}
      </div>
      <div class="flex items-center gap-[8px]">
        {{ t('apiTestDebug.index') }}
        <a-input-number v-model:model-value="expressionForm.specifyMatchNum" :min="1" class="w-[80px]" />
        {{ t('apiTestDebug.unit') }}
      </div>
    </div>
    <div v-if="expressionForm.expressionType === 'XPath'" class="mb-[16px]">
      <div class="mb-[8px] text-[var(--color-text-1)]">
        {{ t('apiTestDebug.contentType') }}
      </div>
      <a-radio-group v-model:model-value="expressionForm.xmlMatchContentType" size="small">
        <a-radio value="xml"> XML </a-radio>
        <a-radio value="html"> HTML </a-radio>
      </a-radio-group>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import { useI18n } from '@/hooks/useI18n';

  import { JSONPathExtract, RegexExtract, XPathExtract } from '@/models/apiTest/debug';

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
</script>

<style lang="less" scoped></style>
