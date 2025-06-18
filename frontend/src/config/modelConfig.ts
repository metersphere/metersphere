import { SelectOptionData } from '@arco-design/web-vue';

import { useI18n } from '@/hooks/useI18n';

import type { ModelAdvancedSetType } from '@/models/setting/modelConfig';
import { ModelBaseTypeEnum } from '@/enums/modelEnum';

const { t } = useI18n();

export const baseModelTypeMap: Record<ModelBaseTypeEnum, SelectOptionData[]> = {
  [ModelBaseTypeEnum.DeepSeek]: [
    {
      value: 'deepseek-chat',
      label: 'deepseek-chat',
      tooltip: 'system.config.modelConfig.deepSeekChatTooltip',
    },
    {
      value: 'deepseek-reasoner',
      label: 'deepseek-reasoner',
      tooltip: '',
    },
  ],
  [ModelBaseTypeEnum.ZhiPuAI]: [
    {
      value: 'glm-4-plus',
      label: 'glm-4-plus',
      tooltip: 'system.config.modelConfig.zhiPuAiGlm4PlusTooltip',
    },
    {
      value: 'glm-4-air-250414',
      label: 'glm-4-air-250414',
      tooltip: 'system.config.modelConfig.zhiPuAiGlm4Air250414Tooltip',
    },
    {
      value: 'glm-4-long',
      label: 'glm-4-long',
      tooltip: 'system.config.modelConfig.zhiPuAiGlm4LongTooltip',
    },
    {
      value: 'glm-4-airx',
      label: 'glm-4-airx',
      tooltip: 'system.config.modelConfig.zhiPuAiGlm4AirxTooltip',
    },
    {
      value: 'glm-4-flashx',
      label: 'glm-4-flashx',
      tooltip: 'system.config.modelConfig.zhiPuAiGlm4FlashxTooltip',
    },
    {
      value: 'glm-4-flash-250414',
      label: 'glm-4-flash-250414',
      tooltip: 'system.config.modelConfig.zhiPuAiGlm4Flash250414Tooltip',
    },
    {
      value: 'glm-4v-plus-0111',
      label: 'glm-4v-plus-0111',
      tooltip: 'system.config.modelConfig.zhiPuAiGlm4vPlus0111Tooltip',
    },
  ],
  [ModelBaseTypeEnum.OpenAI]: [
    {
      value: 'gpt-3.5-turbo',
      label: 'gpt-3.5-turbo',
      tooltip: 'system.config.modelConfig.openaiGpt35TurboTooltip',
    },
    {
      value: 'gpt-3.5-turbo-0125',
      label: 'gpt-3.5-turbo-0125',
      tooltip: 'system.config.modelConfig.openaiGpt35Turbo0125Tooltip',
    },
    {
      value: 'gpt-3.5-turbo-1106',
      label: 'gpt-3.5-turbo-1106',
      tooltip: 'system.config.modelConfig.openaiGpt35Turbo1106Tooltip',
    },
    {
      value: 'gpt-3.5-turbo-0613',
      label: 'gpt-3.5-turbo-0613',
      tooltip: 'system.config.modelConfig.openaiGpt35Turbo0613Tooltip',
    },
    {
      value: 'gpt-4',
      label: 'gpt-4',
      tooltip: 'system.config.modelConfig.openaiGpt4Tooltip',
    },
    {
      value: 'gpt-4-turbo',
      label: 'gpt-4-turbo',
      tooltip: 'system.config.modelConfig.openaiGpt4TurboTooltip',
    },
    {
      value: 'gpt-4o-mini',
      label: 'gpt-4o-mini',
      tooltip: 'system.config.modelConfig.openaiGpt4oMiniTooltip',
    },
    {
      value: 'gpt-4-turbo-preview',
      label: 'gpt-4-turbo-preview',
      tooltip: 'system.config.modelConfig.openaiGpt4TurboPreviewTooltip',
    },
    {
      value: 'gpt-4o-2024-05-13',
      label: 'gpt-4o-2024-05-13',
      tooltip: 'system.config.modelConfig.openaiGpt4o20240513Tooltip',
    },
    {
      value: 'gpt-4-turbo-2024-04-09',
      label: 'gpt-4-turbo-2024-04-09',
      tooltip: 'system.config.modelConfig.openaiGpt4Turbo20240409Tooltip',
    },
    {
      value: 'gpt-4-0125-preview',
      label: 'gpt-4-0125-preview',
      tooltip: 'system.config.modelConfig.openaiGpt40125PreviewTooltip',
    },
    {
      value: 'gpt-4-1106-preview',
      label: 'gpt-4-1106-preview',
      tooltip: 'system.config.modelConfig.openaiGpt41106PreviewTooltip',
    },
  ],
};

export const DEEP_SEEK_REASONER: ModelAdvancedSetType[] = [
  {
    name: 'maxTokens',
    label: 'system.config.modelConfig.maxTokens',
    value: 4000,
    enable: true,
    minValue: 1,
    maxValue: 8000,
  },
  {
    name: 'topP',
    label: 'system.config.modelConfig.topP',
    value: 1.0,
    enable: true,
    minValue: 0.1,
    maxValue: 1,
  },
];

export const DEEP_SEEK_CHAT: ModelAdvancedSetType[] = [
  {
    name: 'frequencyPenalty',
    label: 'system.config.modelConfig.frequencyPenalty',
    value: 0,
    enable: true,
    minValue: -2.0,
    maxValue: 2.0,
  },
  {
    name: 'maxTokens',
    label: 'system.config.modelConfig.maxTokens',
    value: 4096,
    enable: true,
    minValue: 1.0,
    maxValue: 8192,
  },
  {
    name: 'temperature',
    label: 'system.config.modelConfig.temperature',
    value: 1,
    enable: true,
    minValue: 0,
    maxValue: 2,
  },
  {
    name: 'topP',
    label: 'system.config.modelConfig.topP',
    value: 1,
    enable: true,
    minValue: 0.1,
    maxValue: 1,
  },
];

export const deepSeekModelDefaultValueMap = {
  DEEP_SEEK_REASONER,
  DEEP_SEEK_CHAT,
};

export const defaultAdvancedSetValueMap: Record<ModelBaseTypeEnum, ModelAdvancedSetType[]> = {
  [ModelBaseTypeEnum.DeepSeek]: [],
  [ModelBaseTypeEnum.OpenAI]: [
    {
      name: 'maxTokens',
      label: 'system.config.modelConfig.maxTokens',
      value: 1000,
      enable: true,
      minValue: 1,
      maxValue: 8000,
    },
    {
      name: 'temperature',
      label: 'system.config.modelConfig.temperature',
      value: 0.8,
      enable: true,
      minValue: 0,
      maxValue: 2,
    },
    {
      name: 'topP',
      label: 'system.config.modelConfig.topP',
      value: 1,
      enable: true,
      minValue: 0.1,
      maxValue: 1,
    },
  ],
  [ModelBaseTypeEnum.ZhiPuAI]: [
    {
      name: 'temperature',
      label: 'system.config.modelConfig.temperature',
      value: 0.75,
      enable: true,
      minValue: 0,
      maxValue: 1,
    },
    {
      name: 'topP',
      label: 'system.config.modelConfig.topP',
      value: 1,
      enable: true,
      minValue: 0.1,
      maxValue: 1,
    },
    {
      name: 'maxTokens',
      label: 'system.config.modelConfig.maxTokens',
      value: 4000,
      enable: true,
      minValue: 1,
      maxValue: 8000,
    },
  ],
};

export const modelTypeOptions = [
  {
    label: t('system.config.modelConfig.largeLanguageModel'),
    value: 'LLM',
  },
];

export function getModelDefaultConfig(supplierType: ModelBaseTypeEnum, baseModelType = ''): ModelAdvancedSetType[] {
  const defaultAdvancedSetValue = defaultAdvancedSetValueMap;
  let lastDefaultValue: ModelAdvancedSetType[] = [];
  switch (supplierType) {
    case ModelBaseTypeEnum.OpenAI:
    case ModelBaseTypeEnum.ZhiPuAI:
      const includesBaseModelValues = baseModelTypeMap[supplierType].map((e) => e.value);
      lastDefaultValue = includesBaseModelValues.includes(baseModelType) ? defaultAdvancedSetValue[supplierType] : [];
      break;
    case ModelBaseTypeEnum.DeepSeek:
      if (baseModelType.toLocaleUpperCase().includes('REASONER')) {
        defaultAdvancedSetValue[supplierType] = DEEP_SEEK_REASONER;
      } else if (baseModelType.toLocaleUpperCase().includes('CHAT')) {
        defaultAdvancedSetValue[supplierType] = DEEP_SEEK_CHAT;
      } else {
        defaultAdvancedSetValue[supplierType] = [];
      }
      lastDefaultValue = defaultAdvancedSetValue[supplierType];
      break;
    default:
      lastDefaultValue = [];
      break;
  }
  return lastDefaultValue.map((e) => ({ ...e, label: t(e.label) }));
}
