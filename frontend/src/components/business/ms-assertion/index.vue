<template>
  <div class="ms-assertion">
    <div class="mb-[8px] flex items-center justify-between">
      <a-dropdown trigger="hover" @select="handleSelect">
        <a-button class="w-[84px]" type="outline">
          <div class="flex flex-row items-center gap-[8px]">
            <icon-plus />
            <span>{{ t('ms.assertion.button') }}</span>
          </div>
        </a-button>
        <template #content>
          <a-doption v-for="item in assertOptionSource" :key="item.value" :value="item.value">{{
            item.label
          }}</a-doption>
        </template>
      </a-dropdown>
      <div v-if="props.isDefinition && innerConfig" class="flex items-center">
        <a-switch v-model:model-value="innerConfig.enableGlobal" size="small" type="line" />
        <div class="ml-[8px] text-[var(--color-text-1)]">{{ t('ms.assertion.openGlobal') }}</div>
        <a-tooltip :content="t('ms.assertion.openGlobalTip')" position="left">
          <icon-question-circle
            class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
            size="16"
          />
        </a-tooltip>
      </div>
    </div>
    <div v-if="showBody" class="ms-assertion-body w-full">
      <a-scrollbar
        :style="{
          overflow: 'auto',
          height: 'calc(100vh - 458px)',
          width: '100%',
        }"
      >
        <VueDraggable v-model="assertions" class="ms-assertion-body-left" ghost-class="ghost" handle=".sort-handle">
          <div
            v-for="(item, index) in assertions"
            :key="item.id"
            class="ms-assertion-body-left-item"
            :class="{
              'ms-assertion-body-left-item-active': activeKey === item.id,
              'ms-assertion-body-left-item-active-focus': focusKey === item.id,
            }"
            @click="handleItemClick(item)"
          >
            <div class="ms-assertion-body-left-item-row">
              <span class="ms-assertion-body-left-item-row-num">{{ index + 1 }}</span>
              <span class="ms-assertion-body-left-item-row-title">{{ item.name }}</span>
            </div>
            <div class="ms-assertion-body-left-item-switch">
              <div class="ms-assertion-body-left-item-switch-action">
                <MsIcon
                  type="icon-icon_drag"
                  class="action-btn-move sort-handle cursor-move text-[12px] text-[var(--color-text-4)]"
                />
                <MsTableMoreAction
                  :list="itemMoreActions"
                  trigger="click"
                  @select="handleMoreActionSelect($event, item)"
                  @close="focusKey = ''"
                >
                  <MsButton type="icon" size="mini" class="action-btn-more">
                    <MsIcon
                      type="icon-icon_more_outlined"
                      size="14"
                      class="text-[var(--color-text-4)]"
                      @click="focusKey = item.id"
                    />
                  </MsButton>
                </MsTableMoreAction>
              </div>

              <a-switch v-model:model-value="item.enable" type="line" size="small" />
            </div>
          </div>
        </VueDraggable>
      </a-scrollbar>
      <section class="ms-assertion-body-right h-full">
        <!-- 响应头 -->
        <ResponseHeaderTab
          v-if="valueKey === ResponseAssertionType.RESPONSE_HEADER"
          v-model:data="getCurrentItemState"
          @change="handleChange"
        />
        <!-- 状态码 -->
        <StatusCodeTab
          v-if="valueKey === ResponseAssertionType.RESPONSE_CODE"
          v-model:data="getCurrentItemState"
          @change="handleChange"
        />
        <!-- 响应体 -->
        <ResponseBodyTab
          v-if="valueKey === ResponseAssertionType.RESPONSE_BODY"
          v-model:data="getCurrentItemState"
          :response="props.response"
          @change="handleChange"
        />
        <!-- 响应时间 -->
        <ResponseTimeTab
          v-if="valueKey === ResponseAssertionType.RESPONSE_TIME"
          v-model:data="getCurrentItemState"
          @change="handleChange"
        />
        <!-- 变量 -->
        <VariableTab
          v-if="valueKey === ResponseAssertionType.VARIABLE"
          v-model:data="getCurrentItemState"
          @change="handleChange"
        />
        <!-- 脚本 -->
        <ScriptTab
          v-if="valueKey === ResponseAssertionType.SCRIPT"
          v-model:data="getCurrentItemState"
          @change="handleChange"
        />
        <!-- </a-scrollbar> -->
      </section>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { defineModel } from 'vue';
  import { useVModel } from '@vueuse/core';
  import { cloneDeep } from 'lodash-es';
  import { VueDraggable } from 'vue-draggable-plus';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import ResponseBodyTab from './comp/ResponseBodyTab.vue';
  import ResponseHeaderTab from './comp/ResponseHeaderTab.vue';
  import ResponseTimeTab from './comp/ResponseTimeTab.vue';
  import ScriptTab from './comp/ScriptTab.vue';
  import StatusCodeTab from './comp/StatusCodeTab.vue';
  import VariableTab from './comp/VariableTab.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ExecuteAssertionConfig, ExecuteConditionProcessor } from '@/models/apiTest/common';
  import { RequestConditionScriptLanguage, ResponseAssertionType, ResponseBodyAssertionType } from '@/enums/apiEnum';

  import { ExecuteAssertion, MsAssertionItem } from './type';

  defineOptions({
    name: 'MsAssertion',
  });

  const { t } = useI18n();
  // 当前鼠标所在的key
  const focusKey = ref<string>('');
  // 所有的断言列表参数
  const assertions = defineModel<any[]>('params', { default: [] });

  const props = defineProps<{
    isDefinition?: boolean; // 是否是定义页面
    assertionConfig?: ExecuteAssertionConfig; // 是否开启全局
    response?: string; // 响应内容
  }>();

  const emit = defineEmits<{
    (e: 'update:assertionConfig', params: ExecuteAssertionConfig): void;
    (e: 'change'): void;
  }>();

  const innerConfig = useVModel(props, 'assertionConfig', emit);

  const activeIds = ref('');
  // Item点击的key
  const activeKey = ref<string>('');
  // 展示的value
  const valueKey = computed(() => {
    return activeKey.value && assertions.value.find((item) => item.id === activeKey.value)?.assertionType;
  });
  const defaultResBodyItem = {
    jsonPathAssertion: {
      assertions: [],
    },
    xpathAssertion: { responseFormat: 'XML', assertions: [] },
    assertionBodyType: '',
    regexAssertion: {
      assertions: [],
    },
    // TODO文档暂时不做
    // documentAssertion: {
    //   jsonAssertion: [
    //     {
    //       id: rootId,
    //       paramsName: 'root',
    //       mustInclude: false,
    //       typeChecking: false,
    //       paramType: 'object',
    //       matchCondition: '',
    //       matchValue: '',
    //     },
    //   ],
    //   responseFormat: 'JSON',
    //   followApi: false,
    // },
  };

  // 计算当前页面的存储的状态
  const getCurrentItemState = computed({
    get: () => {
      const currentResItem =
        assertions.value.find((item: any) => item.id === activeKey.value) || assertions.value[0] || {};
      if (currentResItem && currentResItem?.assertionType === ResponseAssertionType.RESPONSE_BODY) {
        const { jsonPathAssertion, xpathAssertion, regexAssertion } = currentResItem;
        return {
          ...currentResItem,
          jsonPathAssertion: jsonPathAssertion || defaultResBodyItem.jsonPathAssertion,
          xpathAssertion: xpathAssertion || defaultResBodyItem.xpathAssertion,
          assertionBodyType: '',
          regexAssertion: regexAssertion || defaultResBodyItem.regexAssertion,
          bodyAssertionDataByType: {},
        };
      }
      if (currentResItem && currentResItem?.assertionType === ResponseAssertionType.SCRIPT) {
        return {
          ...currentResItem,
          processorType: ResponseAssertionType.SCRIPT,
        };
      }
      return currentResItem;
    },
    set: (val: ExecuteAssertion) => {
      const currentIndex = assertions.value.findIndex((item) => item.id === activeKey.value);
      const tmpArr = assertions.value;
      tmpArr[currentIndex] = cloneDeep(val);
      assertions.value = tmpArr;
    },
  });

  const itemMoreActions: ActionsItem[] = [
    {
      label: 'common.copy',
      eventTag: 'copy',
    },
    {
      label: 'project.fileManagement.delete',
      eventTag: 'delete',
    },
  ];
  // 源选项
  const assertOptionSource = [
    {
      label: t('ms.assertion.statusCode'),
      value: ResponseAssertionType.RESPONSE_CODE,
    },
    {
      label: t('ms.assertion.responseHeader'),
      value: ResponseAssertionType.RESPONSE_HEADER,
    },
    {
      label: t('ms.assertion.responseBody'),
      value: ResponseAssertionType.RESPONSE_BODY,
    },
    {
      label: t('ms.assertion.responseTime'),
      value: ResponseAssertionType.RESPONSE_TIME,
    },
    {
      label: t('ms.assertion.param'),
      value: ResponseAssertionType.VARIABLE,
    },
    {
      label: t('ms.assertion.script'),
      value: ResponseAssertionType.SCRIPT,
    },
  ];

  // 是否显示主体
  const showBody = computed(() => {
    return assertions.value.length > 0;
  });
  // dropdown选择
  const handleSelect = (value: string | number | Record<string, any> | undefined) => {
    const id = new Date().getTime().toString();
    const tmpObj = {
      name: assertOptionSource.find((item) => item.value === value)?.label || '',
      assertionType: value,
      id,
      enable: true,
    };

    switch (value) {
      // 请求头
      case ResponseAssertionType.RESPONSE_HEADER:
        assertions.value.push({
          ...tmpObj,
          assertions: [],
        });
        break;
      // 状态码
      case ResponseAssertionType.RESPONSE_CODE:
        assertions.value.push({
          ...tmpObj,
          condition: 'EQUALS',
          expectedValue: '200',
        });
        break;
      case ResponseAssertionType.RESPONSE_BODY:
        assertions.value.push({
          ...tmpObj,
          assertionBodyType: ResponseBodyAssertionType.JSON_PATH,
          jsonPathAssertion: {
            assertions: [],
          },
          xpathAssertion: {
            responseFormat: 'XML',
            assertions: [],
          },
          regexAssertion: {
            assertions: [],
          },
          bodyAssertionDataByType: {},
        });
        break;
      // 响应时间
      case ResponseAssertionType.RESPONSE_TIME:
        assertions.value.push({
          ...tmpObj,
          expectedValue: 100,
        });
        break;
      case ResponseAssertionType.VARIABLE:
        assertions.value.push({
          ...tmpObj,
          condition: '',
          expectedValue: '',
          variableAssertionItems: [],
        });
        break;
      case ResponseAssertionType.SCRIPT:
        assertions.value.push({
          ...tmpObj,
          id,
          processorType: ResponseAssertionType.SCRIPT,
          scriptName: t('apiTestDebug.preconditionScriptName'),
          enableCommonScript: false,
          script: '',
          scriptId: '',
          scriptLanguage: LanguageEnum.BEANSHELL,
          commonScriptInfo: {
            id: '',
            name: '',
            script: '',
            params: [{}],
            scriptLanguage: LanguageEnum.BEANSHELL,
          },
        });
        break;

      default:
        break;
    }
    activeKey.value = id;
  };

  const handleMoreActionSelect = (event: ActionsItem, item: MsAssertionItem) => {
    const currentIndex = assertions.value.findIndex((tmpItem) => tmpItem.id === item.id);
    if (event.eventTag === 'delete') {
      assertions.value.splice(currentIndex, 1);
      activeKey.value = currentIndex > 0 ? assertions.value[currentIndex - 1].id : '';
    } else {
      // copy 当前item
      const tmpObj = { ...assertions.value[currentIndex], id: new Date().getTime().valueOf().toString() };
      const tmpArr = assertions.value;
      tmpArr.splice(currentIndex, 0, tmpObj);
      assertions.value = tmpArr;
      activeKey.value = tmpObj.id;
    }
  };

  // item点击
  const handleItemClick = (item: MsAssertionItem) => {
    activeKey.value = item.id;
  };

  const handleChange = (val: any) => {
    switch (val.assertionType) {
      case ResponseAssertionType.RESPONSE_HEADER:
        getCurrentItemState.value = { ...val };
        break;
      case ResponseAssertionType.RESPONSE_CODE:
        getCurrentItemState.value = { ...val };
        break;
      case ResponseAssertionType.RESPONSE_BODY:
        getCurrentItemState.value = { ...val };
        break;
      case ResponseAssertionType.RESPONSE_TIME:
        getCurrentItemState.value = { ...val };
        break;
      case ResponseAssertionType.VARIABLE:
        getCurrentItemState.value = { ...val };
        break;
      case ResponseAssertionType.SCRIPT:
        getCurrentItemState.value = { ...val };
        break;

      default:
        break;
    }
  };

  watchEffect(() => {
    getCurrentItemState.value =
      assertions.value.find((item: any) => item.id === activeKey.value) || assertions.value[0] || {};
    activeKey.value = getCurrentItemState.value.id;
  });
</script>

<style lang="less" scoped>
  .ms-assertion {
    width: 100%;
    &-body {
      display: flex;
      margin-top: 8px;
      flex-flow: row nowrap;
      gap: 8px;
      &-left {
        display: flex;
        padding: 12px;
        width: 216px;
        min-width: 216px;
        background-color: var(--color-text-n9);
        flex-direction: column;
        gap: 4px;
        &-item {
          display: flex;
          flex-flow: row nowrap;
          justify-content: space-between;
          align-items: center;
          padding: 4px 8px;
          border-radius: 4px;
          background-color: var(--color-text-fff);
          cursor: pointer;
          &-row {
            display: flex;
            flex-direction: row;
            align-items: center;
            gap: 4px;
            &-num {
              width: 16px;
              height: 16px;
              font-size: 12px;
              font-weight: 500;
              border-radius: 50%;
              text-align: center;
              color: var(--color-text-4);
              background-color: var(--color-text-n8);
              line-height: 16px;
            }
            &-title {
              font-size: 14px;
              font-weight: 400;
              color: var(--color-text-1);
              line-height: 22px;
            }
          }
          &-switch {
            display: flex;
            flex-direction: row;
            align-items: center;
            gap: 4px;
            &-action {
              display: flex;
              align-items: center;
              gap: 4px;
            }
          }
        }
      }
      &-right {
        display: flex;
        flex-grow: 1;
        padding: 0;
        border: 1px solid var(--color-text-n8);
        border-radius: 4px;
        background: var(--color-text-fff);
      }
    }
  }
  .action-btn-move,
  .action-btn-more {
    opacity: 0;
    transition: opacity 0.2s;
  }
  .ms-assertion-body-left-item-active {
    background-color: rgb(var(--primary-1)) !important;
    .ms-assertion-body-left-item-row {
      &-num {
        color: rgb(var(--primary-5)) !important;
        background-color: var(--color-text-fff) !important;
      }
      &-title {
        color: rgb(var(--primary-5));
      }
    }
  }
  .ms-assertion-body-left-item:hover {
    background-color: rgb(var(--primary-1));
    .action-btn-move,
    .action-btn-more {
      opacity: 1;
    }
  }
  .ms-assertion-body-left-item-active-focus {
    background-color: rgb(var(--primary-1));
    .action-btn-move,
    .action-btn-more {
      opacity: 1;
    }
  }
</style>
