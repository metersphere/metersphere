<template>
  <a-spin :loading="loading" class="w-full">
    <div class="form-item-container">
      <!-- 所属平台一致, 详情展示 -->
      <div v-if="props.currentPlatform === detailInfo.platform" class="h-full w-full">
        <!-- 自定义字段开始 -->
        <div class="inline-block w-full break-words">
          <MsFormCreate
            v-if="props.formRule.length"
            ref="formCreateRef"
            v-model:form-item="innerFormItem"
            v-model:api="fApi"
            v-model:form-rule="innerFormRules"
            class="w-full"
            :option="options"
            @change="handelFormCreateChange"
          />
          <!-- 自定义字段结束 -->
          <div
            v-if="!props.isPlatformDefaultTemplate && hasAnyPermission(['PROJECT_BUG:READ+UPDATE'])"
            class="baseItem"
          >
            <a-form
              :model="{}"
              :label-col-props="{
                span: 9,
              }"
              :wrapper-col-props="{
                span: 15,
              }"
              label-align="left"
              content-class="tags-class"
            >
              <a-form-item field="tags" :label="t('system.orgTemplate.tags')">
                <MsTagsInput
                  v-model:model-value="innerTags"
                  :disabled="!hasAnyPermission(['PROJECT_BUG:READ+UPDATE'])"
                  @blur="changeTag"
                />
              </a-form-item>
            </a-form>
          </div>
        </div>

        <!-- 内置基础信息结束 -->
      </div>
      <!-- 所属平台不一致, 详情不展示, 展示空面板 -->
      <div v-else>
        <a-empty> {{ $t('messageBox.noContent') }} </a-empty>
      </div>
    </div>
  </a-spin>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep, debounce } from 'lodash-es';

  import MsFormCreate from '@/components/pure/ms-form-create/ms-form-create.vue';
  import type { FormItem, FormRuleItem } from '@/components/pure/ms-form-create/types';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';

  import { createOrUpdateBug } from '@/api/modules/bug-management';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { hasAnyPermission } from '@/utils/permission';

  import { BugEditCustomField, BugEditFormObject } from '@/models/bug-management';

  import { makeCustomFieldsParams } from '../utils';

  const appStore = useAppStore();

  const { t } = useI18n();

  const props = defineProps<{
    detail: Record<string, any>;
    currentPlatform: string;
    formRule: FormItem[];
    isPlatformDefaultTemplate: boolean;
    platformSystemFields: BugEditCustomField[]; // 平台系统字段
  }>();

  const emit = defineEmits<{
    (e: 'update:tags', val: string[]): void;
    (e: 'updateSuccess'): void;
  }>();

  const loading = ref<boolean>(false);

  const detailInfo = ref<Record<string, any>>({ match: [] }); // 存储当前详情信息，通过loadBug 获取
  const fApi = ref<any>(null);

  const innerFormRules = defineModel<FormItem[]>('formRule', { default: [] });
  const innerFormItem = defineModel<FormRuleItem[]>('formItem', { default: [] });
  const innerTags = defineModel<string[]>('tags', { default: [] });
  // 表单配置项
  const options = {
    resetBtn: false, // 不展示默认配置的重置和提交
    submitBtn: false,
    on: false, // 取消绑定on事件
    form: {
      layout: 'horizontal',
      labelAlign: 'left',
      labelColProps: {
        span: 9,
      },
      wrapperColProps: {
        span: 15,
      },
    },
    // 暂时默认
    row: {
      gutter: 0,
    },
    wrap: {
      'asterisk-position': 'end',
      'validate-trigger': ['change'],
    },
  };

  async function makeParams() {
    const customFields = await makeCustomFieldsParams(innerFormItem.value);
    if (props.isPlatformDefaultTemplate) {
      // 平台系统默认字段插入自定义集合
      props.platformSystemFields.forEach((item) => {
        customFields.push({
          id: item.fieldId,
          name: item.fieldName,
          type: item.type,
          value: item.defaultValue,
        });
      });
    }
    const params: BugEditFormObject = {
      projectId: appStore.currentProjectId,
      id: detailInfo.value.id,
      templateId: detailInfo.value.templateId,
      tags: innerTags.value,
      deleteLocalFileIds: [],
      unLinkRefIds: [],
      linkFileIds: [],
      customFields,
      richTextTmpFileIds: [],
    };
    if (!props.isPlatformDefaultTemplate) {
      params.description = detailInfo.value.description;
      params.title = detailInfo.value.title;
    }
    return params;
  }

  function saveHandler() {
    try {
      fApi.value?.validate().then(async (valid: any) => {
        if (valid === true) {
          loading.value = true;
          const requestParams = await makeParams();
          await createOrUpdateBug({ request: requestParams, fileList: [] as unknown as File[] });
          Message.success(t('common.editSuccess'));
          emit('updateSuccess');
        }
      });
    } catch (error) {
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  const handelFormCreateChange = debounce(() => {
    saveHandler();
  }, 300);

  const changeTag = debounce(() => {
    emit('update:tags', innerTags.value);
    saveHandler();
  }, 300);

  watchEffect(() => {
    detailInfo.value = cloneDeep(props.detail);
  });
</script>

<style scoped lang="less">
  .form-item-container {
    max-width: 50%;
    .baseItem {
      margin-bottom: 16px;
      height: 32px;
      line-height: 32px;
      @apply flex;
      .label {
        width: 84px;
        color: var(--color-text-3);
      }
    }
    :deep(.arco-form-item-layout-horizontal) {
      margin-bottom: 16px !important;
    }
    :deep(.arco-form-item-label-col) {
      padding-right: 0;
    }
    :deep(.arco-col-9) {
      flex: 0 0 84px;
      width: 84px;
    }
    :deep(.arco-col-15) {
      flex: 0 0 calc(100% - 84px);
      width: calc(100% - 84px);
    }
    :deep(.arco-form-item-label::after) {
      color: red !important;
    }
    :deep(.arco-form-item-label-col > .arco-form-item-label) {
      color: var(--color-text-3) !important;
    }
    :deep(.arco-select-view-single) {
      border-color: transparent !important;
      .arco-select-view-suffix {
        visibility: hidden;
      }
      &:hover {
        border-color: rgb(var(--primary-5)) !important;
        .arco-select-view-suffix {
          visibility: visible !important;
        }
      }
      &:hover > .arco-input {
        font-weight: normal;
        text-decoration: none;
        color: var(--color-text-1);
      }
      & > .arco-input {
        font-weight: 500;
        text-decoration: underline;
        color: var(--color-text-1);
      }
    }
    :deep(.arco-input-tag) {
      border-color: transparent !important;
      &:hover {
        border-color: rgb(var(--primary-5)) !important;
      }
    }
    :deep(.arco-input-wrapper) {
      border-color: transparent !important;
      &:hover {
        border-color: rgb(var(--primary-5)) !important;
      }
    }
    :deep(.arco-select-view-multiple) {
      border-color: transparent !important;
      .arco-select-view-suffix {
        visibility: hidden;
      }
      &:hover {
        border-color: rgb(var(--primary-5)) !important;
        .arco-select-view-suffix {
          visibility: visible !important;
        }
      }
    }
    :deep(.arco-textarea-wrapper) {
      border-color: transparent !important;
      &:hover {
        border-color: rgb(var(--primary-5)) !important;
      }
    }
    :deep(.arco-input-number) {
      border-color: transparent !important;
      &:hover {
        border-color: rgb(var(--primary-5)) !important;
      }
    }
    :deep(.arco-picker) {
      border-color: transparent !important;
      .arco-picker-suffix {
        visibility: hidden;
      }
      &:hover {
        border-color: rgb(var(--primary-5)) !important;
        arco-picker-suffix {
          visibility: visible !important;
        }
      }
    }
  }
</style>
