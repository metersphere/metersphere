<template>
  <div class="ms-assertion">
    <div class="mb-[8px] flex items-center justify-between">
      <a-dropdown trigger="hover" :disabled="props.disabled" @select="handleSelect">
        <a-button class="w-[84px]" type="outline" :disabled="props.disabled">
          <div class="flex flex-row items-center gap-[8px]">
            <icon-plus />
            <span>{{ t('ms.assertion.button') }}</span>
          </div>
        </a-button>
        <template #content>
          <a-doption v-for="item in assertOptionSource" :key="item.value" :value="item.value">
            {{ item.label }}
          </a-doption>
        </template>
      </a-dropdown>
      <div v-if="props.isDefinition && innerConfig" class="flex items-center">
        <a-switch v-model:model-value="innerConfig.enableGlobal" :disabled="props.disabled" size="small" type="line" />
        <div class="ml-[8px] text-[var(--color-text-1)]">{{ t('ms.assertion.openGlobal') }}</div>
        <a-tooltip
          :content="innerConfig.enableGlobal ? t('ms.assertion.openGlobalTip') : t('ms.assertion.closeGlobalTip')"
          position="left"
        >
          <icon-question-circle
            class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
            size="16"
          />
        </a-tooltip>
      </div>
    </div>
    <div v-if="showBody" class="ms-assertion-body w-full overflow-hidden">
      <div class="ms-assertion-body-left h-full w-[25%] min-w-[220px]">
        <VueDraggable v-model="assertions" ghost-class="ghost" handle=".sort-handle">
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
              <a-tooltip :content="item.name">
                <div
                  class="one-line-text max-w-[80px]"
                  :class="{ 'text-[rgb(var(--primary-5))]': activeKey === item.id }"
                  >{{ item.name }}</div
                >
              </a-tooltip>
            </div>
            <div class="ms-assertion-body-left-item-switch">
              <div v-show="!props.disabled" class="ms-assertion-body-left-item-switch-action">
                <icon-drag-dot-vertical class="ms-list-drag-icon sort-handle" />
                <MsTableMoreAction
                  :list="getItemMoreActions(item)"
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

              <a-switch v-model:model-value="item.enable" :disabled="props.disabled" type="line" size="small" />
            </div>
          </div>
        </VueDraggable>
      </div>
      <div
        class="ms-assertion-body-right h-full flex-1 overflow-hidden"
        :class="{
          'p-4 pr-0': getCurrentItemState.assertionType !== ResponseAssertionType.SCRIPT,
          'border border-solid border-[var(--color-text-n8)]':
            getCurrentItemState.assertionType !== ResponseAssertionType.SCRIPT,
        }"
      >
        <!-- 响应头 -->
        <ResponseHeaderTab
          v-if="getCurrentItemState.assertionType === ResponseAssertionType.RESPONSE_HEADER"
          v-model:data="getCurrentItemState"
          class="pr-4"
          :disabled="props.disabled"
          @change="handleChange"
        />
        <!-- 状态码 -->
        <StatusCodeTab
          v-if="getCurrentItemState.assertionType === ResponseAssertionType.RESPONSE_CODE"
          v-model:data="getCurrentItemState"
          :disabled="props.disabled"
          @change="handleChange"
        />
        <!-- 响应体 -->
        <ResponseBodyTab
          v-if="getCurrentItemState.assertionType === ResponseAssertionType.RESPONSE_BODY"
          v-model:data="getCurrentItemState"
          :disabled="props.disabled"
          :response="props.response"
          :show-extraction="props.showExtraction"
          @change="handleChange"
        />
        <!-- 响应时间 -->
        <ResponseTimeTab
          v-if="getCurrentItemState.assertionType === ResponseAssertionType.RESPONSE_TIME"
          v-model:data="getCurrentItemState"
          :disabled="props.disabled"
          @change="handleChange"
        />
        <!-- 变量 -->
        <VariableTab
          v-if="getCurrentItemState.assertionType === ResponseAssertionType.VARIABLE"
          v-model:data="getCurrentItemState"
          :disabled="props.disabled"
          @change="handleChange"
        />
        <!-- 脚本 -->
        <ScriptTab
          v-if="getCurrentItemState.assertionType === ResponseAssertionType.SCRIPT"
          v-model:data="getCurrentItemState"
          :disabled="props.disabled"
          :script-code-editor-height="props.scriptCodeEditorHeight"
          @change="handleChange"
          @delete-script-item="deleteScriptItem"
          @copy="copyItem"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { useVModel } from '@vueuse/core';
  import { cloneDeep } from 'lodash-es';
  import { VueDraggable } from 'vue-draggable-plus';

  import { EQUAL } from '@/components/pure/ms-advance-filter/index';
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
  import useModal from '@/hooks/useModal';
  import { characterLimit } from '@/utils';

  import { ExecuteAssertionConfig } from '@/models/apiTest/common';
  import { ResponseAssertionType, ResponseBodyAssertionType } from '@/enums/apiEnum';

  import { MsAssertionItem } from './type';

  const { openModal } = useModal();

  defineOptions({
    name: 'MsAssertion',
  });

  const { t } = useI18n();
  // 当前鼠标所在的key
  const focusKey = ref<string>('');
  // 所有的断言列表参数
  const assertions = defineModel<any[]>('params', {
    required: true,
  });

  const props = defineProps<{
    isDefinition?: boolean; // 是否是定义页面
    assertionConfig?: ExecuteAssertionConfig; // 是否开启全局
    response?: string; // 响应内容
    disabled?: boolean; // 是否禁用
    showExtraction?: boolean; // 是否显示提取
    scriptCodeEditorHeight?: string; // 脚本的高度
  }>();

  const emit = defineEmits<{
    (e: 'update:assertionConfig', params: ExecuteAssertionConfig): void;
    // TODO 类型优化
    (e: 'update:params', params: any[]): void;
    (e: 'change'): void;
  }>();

  const innerConfig = useVModel(props, 'assertionConfig', emit);

  const activeKey = ref<string>('');

  const getCurrentItemState = ref(assertions.value[0]);
  const itemMoreActions: ActionsItem[] = [
    {
      label: 'common.copy',
      eventTag: 'copy',
    },
    {
      label: 'project.fileManagement.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];
  function getItemMoreActions(item: any) {
    if (item.assertionType === ResponseAssertionType.SCRIPT) {
      return itemMoreActions;
    }
    return itemMoreActions.slice(-1);
  }

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

  function validateAddType(value: string | number | Record<string, any> | undefined) {
    // 找到对应的类型项
    const addTypeLength =
      assertions.value.filter(
        (item: any) => item.assertionType === value && item.assertionType !== ResponseAssertionType.SCRIPT
      ).length === 1;
    if (addTypeLength) {
      // 赋值当前项加深颜色并且不添加
      getCurrentItemState.value = assertions.value.find((item: any) => item.assertionType === value);
      activeKey.value = getCurrentItemState.value.id;
      return false;
    }
    return true;
  }

  // dropdown选择
  const handleSelect = (value: string | number | Record<string, any> | undefined) => {
    const id = new Date().getTime().toString();
    const tmpObj = {
      name: assertOptionSource.find((item) => item.value === value)?.label || '',
      assertionType: value,
      id,
      enable: true,
    };

    // 校验添加的类型是否已经重复
    if (validateAddType(value)) {
      switch (value) {
        // 请求头
        case ResponseAssertionType.RESPONSE_HEADER:
          assertions.value.push({
            ...tmpObj,
            assertions: [
              {
                header: '',
                condition: EQUAL.value,
                expectedValue: '',
                enable: true,
              },
            ],
          });
          break;
        // 状态码
        case ResponseAssertionType.RESPONSE_CODE:
          assertions.value.push({
            ...tmpObj,
            condition: EQUAL.value,
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
          });
          break;
        // 响应时间
        case ResponseAssertionType.RESPONSE_TIME:
          assertions.value.push({
            ...tmpObj,
            expectedValue: 200,
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
            name: t('apiTestDebug.preconditionScriptName'),
            enableCommonScript: false,
            script: '',
            scriptId: '',
            scriptLanguage: LanguageEnum.BEANSHELL_JSR233,
            commonScriptInfo: {
              id: '',
              name: '',
              script: '',
              params: [],
              scriptLanguage: LanguageEnum.BEANSHELL_JSR233,
            },
          });
          break;

        default:
          break;
      }
      getCurrentItemState.value = assertions.value[assertions.value.length - 1];
      activeKey.value = assertions.value[assertions.value.length - 1].id;
    }

    emit('change');
  };

  const handleMoreActionSelect = (event: ActionsItem, item: MsAssertionItem) => {
    const currentIndex = assertions.value.findIndex((tmpItem) => tmpItem.id === item.id);
    if (event.eventTag === 'delete') {
      openModal({
        type: 'error',
        title: t('system.orgTemplate.deleteTemplateTitle', { name: characterLimit(item.name) }),
        content: t('script.delete.confirm'),
        okText: t('system.userGroup.confirmDelete'),
        cancelText: t('system.userGroup.cancel'),
        okButtonProps: {
          status: 'danger',
        },
        onBeforeOk: async () => {
          try {
            assertions.value.splice(currentIndex, 1);
            activeKey.value = currentIndex > 0 ? assertions.value[currentIndex - 1].id : '';
          } catch (error) {
            // eslint-disable-next-line no-console
            console.log(error);
          }
        },
        hideCancel: false,
      });
    } else {
      // copy 当前item
      const tmpObj = { ...cloneDeep(assertions.value[currentIndex]), id: new Date().getTime().valueOf().toString() };
      const tmpArr = cloneDeep(assertions.value);
      tmpArr.splice(currentIndex, 0, tmpObj);
      assertions.value = cloneDeep(tmpArr);
      activeKey.value = tmpObj.id;
    }
  };

  /**
   * 删除脚本项
   */
  const deleteScriptItem = (id: string | number) => {
    const currentIndex = assertions.value.findIndex((tmpItem) => tmpItem.id === id);
    assertions.value.splice(currentIndex, 1);
    activeKey.value = currentIndex > 0 ? assertions.value[currentIndex - 1].id : '';
  };

  // item点击
  const handleItemClick = (item: MsAssertionItem) => {
    activeKey.value = item.id;
  };

  function copyItem() {
    const tmpObj = { ...cloneDeep(getCurrentItemState.value), id: new Date().getTime().valueOf().toString() };
    assertions.value.push(tmpObj);
    activeKey.value = tmpObj.id;
  }

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
    emit('change');
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
    height: 100%;
    &-body {
      display: flex;
      margin-top: 8px;
      height: calc(100% - 52px);
      flex-flow: row nowrap;
      gap: 8px;
      &-left {
        display: flex;
        overflow-y: auto;
        padding: 12px;
        width: 216px;
        min-width: 216px;
        height: 100%;
        background-color: var(--color-text-n9);
        flex-direction: column;
        gap: 4px;
        .ms-scroll-bar();
        &-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin: 4px 0;
          flex-flow: row nowrap;
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
              .ms-list-drag-icon {
                @apply invisible cursor-move;
              }
            }
          }
          &:hover {
            .ms-list-drag-icon {
              @apply visible;
            }
          }
        }
      }
      &-right {
        display: flex;
        flex: 1;
        flex-grow: 1;
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
