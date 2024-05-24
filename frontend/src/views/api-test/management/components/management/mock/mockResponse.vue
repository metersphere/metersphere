<template>
  <a-spin :loading="loading" class="block">
    <div class="mt-[16px] font-medium">{{ t('apiTestManagement.responseContent') }}</div>
    <div class="mt-[8px] flex items-center gap-[16px]">
      <div class="flex items-center gap-[4px]">
        <a-switch
          v-model:model-value="mockResponse.useApiResponse"
          size="small"
          :disabled="props.disabled"
          @change="handleUseApiResponseChange"
        ></a-switch>
        {{ t('mockManagement.followDefinition') }}
      </div>
      <a-select
        v-if="mockResponse.useApiResponse"
        v-model:model-value="mockResponse.apiResponseId"
        :options="mockResponseOptions"
        class="w-[150px]"
        :disabled="props.disabled"
      ></a-select>
    </div>
    <template v-if="!mockResponse.useApiResponse">
      <MsTab
        v-model:active-key="activeTab"
        :content-tab-list="responseCompositionTabList"
        class="no-content relative my-[8px] border-b"
        :show-badge="false"
      />
      <div class="mt-[8px]">
        <template v-if="activeTab === ResponseComposition.BODY">
          <div class="mb-[8px] flex items-center justify-between">
            <a-radio-group
              v-model:model-value="mockResponse.body.bodyType"
              type="button"
              size="small"
              :disabled="props.disabled"
              @change="(val) => emit('change')"
            >
              <a-radio
                v-for="item of ResponseBodyFormat"
                v-show="item !== ResponseBodyFormat.NONE"
                :key="item"
                :value="item"
              >
                {{ ResponseBodyFormat[item].toLowerCase() }}
              </a-radio>
            </a-radio-group>
            <!-- <div v-if="mockResponse.body.bodyType === ResponseBodyFormat.JSON" class="ml-auto flex items-center">
          <a-radio-group
            v-model:model-value="mockResponse.body.jsonBody.enableJsonSchema"
            size="mini"
            @change="emit('change')"
          >
            <a-radio :value="false">Json</a-radio>
            <a-radio class="mr-0" :value="true"> Json Schema </a-radio>
          </a-radio-group>
          <div class="flex items-center gap-[8px]">
            <a-switch v-model:model-value="mockResponse.body.jsonBody.enableTransition" size="small" type="line" />
            {{ t('apiTestManagement.dynamicConversion') }}
          </div>
        </div> -->
          </div>
          <div
            v-if="
              [ResponseBodyFormat.JSON, ResponseBodyFormat.XML, ResponseBodyFormat.RAW].includes(
                mockResponse.body.bodyType
              )
            "
          >
            <!-- <MsJsonSchema
          v-if="mockResponse.body.jsonBody.enableJsonSchema"
          :data="mockResponse.body.jsonBody.jsonSchema"
          :columns="jsonSchemaColumns"
        /> -->
            <MsCodeEditor
              v-model:model-value="currentBodyCode"
              :language="currentCodeLanguage"
              theme="vs"
              :show-full-screen="false"
              :show-theme-change="false"
              :show-language-change="false"
              :show-charset-change="false"
              show-code-format
              :read-only="props.disabled"
            >
            </MsCodeEditor>
          </div>
          <div v-else>
            <div class="mb-[16px] flex justify-between gap-[8px] bg-[var(--color-text-n9)] p-[12px]">
              <a-input
                v-model:model-value="mockResponse.body.binaryBody.description"
                :placeholder="t('common.desc')"
                :max-length="255"
                :disabled="props.disabled"
              />
              <MsAddAttachment
                v-model:file-list="fileList"
                mode="input"
                :multiple="false"
                :fields="{
                  id: 'fileId',
                  name: 'fileName',
                }"
                :disabled="props.disabled"
                @change="handleFileChange"
              />
            </div>
            <div class="flex items-center">
              <a-switch
                v-model:model-value="mockResponse.body.binaryBody.sendAsBody"
                class="mr-[8px]"
                size="small"
                type="line"
                :disabled="props.disabled"
              ></a-switch>
              <span>{{ t('apiTestDebug.sendAsMainText') }}</span>
              <a-tooltip position="right">
                <template #content>
                  <div>{{ t('apiTestDebug.sendAsMainTextTip1') }}</div>
                  <div>{{ t('apiTestDebug.sendAsMainTextTip2') }}</div>
                </template>
                <icon-question-circle
                  class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                  size="16"
                />
              </a-tooltip>
            </div>
          </div>
        </template>
        <paramTable
          v-else-if="activeTab === ResponseComposition.HEADER"
          :params="mockResponse.headers"
          :columns="columns"
          :default-param-item="defaultKeyValueParamItem"
          :selectable="false"
          :disabled-param-value="props.disabled"
          :disabled-except-param="props.disabled"
          @change="handleResponseTableChange"
        />
        <a-select
          v-else-if="activeTab === ResponseComposition.CODE"
          v-model:model-value="mockResponse.statusCode"
          :options="statusCodeOptions"
          class="w-[200px]"
          :disabled="props.disabled"
          @change="() => emit('change')"
        />
        <a-input-number
          v-else
          v-model:model-value="mockResponse.delay"
          :disabled="props.disabled"
          mode="button"
          :step="100"
          :precision="0"
          :max="600000"
          :min="0"
          class="w-[200px]"
        >
          <template #suffix> ms </template>
        </a-input-number>
      </div>
    </template>
    <template v-else-if="currentSelectedDefinitionResponse">
      <MsTab
        v-model:active-key="definitionActiveTab"
        :content-tab-list="responseCompositionTabList"
        class="no-content relative my-[8px] border-b"
        :show-badge="false"
      />
      <div class="mt-[8px]">
        <template v-if="definitionActiveTab === ResponseComposition.BODY">
          <div class="mb-[8px] flex items-center justify-between">
            <a-radio-group
              v-model:model-value="currentSelectedDefinitionResponse.body.bodyType"
              type="button"
              size="small"
              disabled
              @change="(val) => emit('change')"
            >
              <a-radio
                v-for="item of ResponseBodyFormat"
                v-show="item !== ResponseBodyFormat.NONE"
                :key="item"
                :value="item"
              >
                {{ ResponseBodyFormat[item].toLowerCase() }}
              </a-radio>
            </a-radio-group>
            <!-- <div v-if="currentSelectedDefinitionResponse.body.bodyType === ResponseBodyFormat.JSON" class="ml-auto flex items-center">
          <a-radio-group
            v-model:model-value="currentSelectedDefinitionResponse.body.jsonBody.enableJsonSchema"
            size="mini"
            @change="emit('change')"
          >
            <a-radio :value="false">Json</a-radio>
            <a-radio class="mr-0" :value="true"> Json Schema </a-radio>
          </a-radio-group>
          <div class="flex items-center gap-[8px]">
            <a-switch v-model:model-value="currentSelectedDefinitionResponse.body.jsonBody.enableTransition" size="small" type="line" />
            {{ t('apiTestManagement.dynamicConversion') }}
          </div>
        </div> -->
          </div>
          <div
            v-if="
              [ResponseBodyFormat.JSON, ResponseBodyFormat.XML, ResponseBodyFormat.RAW].includes(
                currentSelectedDefinitionResponse.body.bodyType
              )
            "
          >
            <!-- <MsJsonSchema
          v-if="currentSelectedDefinitionResponse.body.jsonBody.enableJsonSchema"
          :data="currentSelectedDefinitionResponse.body.jsonBody.jsonSchema"
          :columns="jsonSchemaColumns"
        /> -->
            <MsCodeEditor
              v-model:model-value="currentSelectedDefinitionBodyCode"
              :language="currentSelectedDefinitionCodeLanguage"
              theme="vs"
              :show-full-screen="false"
              :show-theme-change="false"
              :show-language-change="false"
              :show-charset-change="false"
              show-code-format
              read-only
            >
            </MsCodeEditor>
          </div>
          <div v-else>
            <div class="mb-[16px] flex justify-between gap-[8px] bg-[var(--color-text-n9)] p-[12px]">
              <a-input
                v-model:model-value="currentSelectedDefinitionResponse.body.binaryBody.description"
                :placeholder="t('common.desc')"
                :max-length="255"
                disabled
              />
              <MsAddAttachment
                v-model:file-list="fileList"
                mode="input"
                :multiple="false"
                :fields="{
                  id: 'fileId',
                  name: 'fileName',
                }"
                disabled
                @change="handleFileChange"
              />
            </div>
            <div class="flex items-center">
              <a-switch
                v-model:model-value="currentSelectedDefinitionResponse.body.binaryBody.sendAsBody"
                class="mr-[8px]"
                size="small"
                type="line"
                disabled
              ></a-switch>
              <span>{{ t('apiTestDebug.sendAsMainText') }}</span>
              <a-tooltip position="right">
                <template #content>
                  <div>{{ t('apiTestDebug.sendAsMainTextTip1') }}</div>
                  <div>{{ t('apiTestDebug.sendAsMainTextTip2') }}</div>
                </template>
                <icon-question-circle
                  class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                  size="16"
                />
              </a-tooltip>
            </div>
          </div>
        </template>
        <paramTable
          v-else-if="definitionActiveTab === ResponseComposition.HEADER"
          :params="filterKeyValParams(currentSelectedDefinitionResponse.headers, defaultKeyValueParamItem).validParams"
          :columns="columns"
          :default-param-item="defaultKeyValueParamItem"
          :selectable="false"
          disabled-param-value
          disabled-except-param
          @change="handleResponseTableChange"
        />
        <a-select
          v-else-if="definitionActiveTab === ResponseComposition.CODE"
          v-model:model-value="currentSelectedDefinitionResponse.statusCode"
          :options="statusCodeOptions"
          class="w-[200px]"
          disabled
          @change="() => emit('change')"
        />
        <a-input-number
          v-else
          v-model:model-value="mockResponse.delay"
          :disabled="props.disabled"
          mode="button"
          :step="100"
          :precision="0"
          :max="600000"
          :min="0"
          class="w-[200px]"
        >
          <template #suffix> ms </template>
        </a-input-number>
      </div>
    </template>
  </a-spin>
</template>

<script setup lang="ts">
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsTab from '@/components/pure/ms-tab/index.vue';
  import { MsFileItem } from '@/components/pure/ms-upload/types';
  import MsAddAttachment from '@/components/business/ms-add-attachment/index.vue';
  import paramTable, { ParamTableColumn } from '@/views/api-test/components/paramTable.vue';
  import { ResponseItem } from '@/views/api-test/components/requestComposition/response/edit.vue';

  import { uploadMockTempFile } from '@/api/modules/api-test/management';
  import { responseHeaderOption } from '@/config/apiTest';
  import { useI18n } from '@/hooks/useI18n';

  import { MockResponse } from '@/models/apiTest/mock';
  import { ResponseBodyFormat, ResponseComposition } from '@/enums/apiEnum';

  import { defaultKeyValueParamItem, statusCodes } from '@/views/api-test/components/config';
  import { filterKeyValParams } from '@/views/api-test/components/utils';

  const props = defineProps<{
    definitionResponses: ResponseItem[];
    disabled: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'change'): void;
  }>();

  const { t } = useI18n();

  const activeTab = ref<ResponseComposition>(ResponseComposition.BODY);
  const mockResponse = defineModel<MockResponse>('mockResponse', {
    required: true,
  });

  const mockResponseOptions = computed(() =>
    props.definitionResponses.map((item) => ({
      label: `${t(item.label || item.name)}(${item.statusCode})`,
      value: item.id,
      defaultFlag: item.defaultFlag,
    }))
  );

  const responseCompositionTabList = [
    {
      label: t('apiTestDebug.responseBody'),
      value: ResponseComposition.BODY,
    },
    {
      label: t('apiTestDebug.responseHeader'),
      value: ResponseComposition.HEADER,
    },
    {
      label: t('apiTestManagement.responseCode'),
      value: ResponseComposition.CODE,
    },
    {
      label: t('mockManagement.responseDelay'),
      value: 'DELAY',
    },
  ];

  const statusCodeOptions = statusCodes.map((e) => ({
    label: e.toString(),
    value: e,
  }));

  const columns: ParamTableColumn[] = [
    {
      title: 'apiTestManagement.paramName',
      dataIndex: 'key',
      slotName: 'key',
      inputType: 'autoComplete',
      autoCompleteParams: responseHeaderOption,
    },
    {
      title: 'apiTestManagement.paramVal',
      dataIndex: 'value',
      slotName: 'value',
      isNormal: true,
      inputType: 'input',
    },
    {
      title: '',
      dataIndex: 'operation',
      slotName: 'operation',
      width: 35,
    },
  ];

  function handleResponseTableChange(arr: any[]) {
    mockResponse.value.headers = [...arr];
    emit('change');
  }

  // 当前显示的代码
  const currentBodyCode = computed({
    get() {
      if (mockResponse.value.body.bodyType === ResponseBodyFormat.JSON) {
        return mockResponse.value.body.jsonBody.jsonValue;
      }
      if (mockResponse.value.body.bodyType === ResponseBodyFormat.XML) {
        return mockResponse.value.body.xmlBody.value;
      }
      return mockResponse.value.body.rawBody.value;
    },
    set(val) {
      if (mockResponse.value.body.bodyType === ResponseBodyFormat.JSON) {
        mockResponse.value.body.jsonBody.jsonValue = val;
      } else if (mockResponse.value.body.bodyType === ResponseBodyFormat.XML) {
        mockResponse.value.body.xmlBody.value = val;
      } else {
        mockResponse.value.body.rawBody.value = val;
      }
    },
  });
  // 当前代码编辑器的语言
  const currentCodeLanguage = computed(() => {
    if (mockResponse.value.body.bodyType === ResponseBodyFormat.JSON) {
      return LanguageEnum.JSON;
    }
    if (mockResponse.value.body.bodyType === ResponseBodyFormat.XML) {
      return LanguageEnum.XML;
    }
    return LanguageEnum.PLAINTEXT;
  });

  const fileList = ref<MsFileItem[]>([]);
  const loading = ref<boolean>(false);

  async function handleFileChange(files: MsFileItem[], file?: MsFileItem) {
    try {
      if (file?.local && file.file) {
        loading.value = true;
        const res = await uploadMockTempFile(file.file);
        mockResponse.value.body.binaryBody.file = {
          ...file,
          fileId: res.data,
          fileName: file?.name || '',
          fileAlias: file?.name || '',
          local: true,
        };
        loading.value = false;
      } else if (files[0]) {
        mockResponse.value.body.binaryBody.file = {
          ...files[0],
          fileId: files[0].uid,
          fileName: files[0]?.originalName || '',
          fileAlias: files[0]?.name || '',
          local: false,
        };
      } else {
        mockResponse.value.body.binaryBody.file = undefined;
      }
      emit('change');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      loading.value = false;
    }
  }

  function handleUseApiResponseChange(val: string | number | boolean) {
    if (val) {
      mockResponse.value.apiResponseId = (mockResponseOptions.value.find((e) => e.defaultFlag)?.value as string) || '';
    }
  }

  watch(
    () => mockResponse.value.body.binaryBody.file?.fileId,
    () => {
      fileList.value = mockResponse.value.body.binaryBody.file
        ? [mockResponse.value.body.binaryBody.file as unknown as MsFileItem]
        : [];
    },
    {
      deep: true,
      immediate: true,
    }
  );

  const currentSelectedDefinitionResponse = computed(() =>
    props.definitionResponses.find((e) => e.id === mockResponse.value.apiResponseId)
  );
  const definitionActiveTab = ref(ResponseComposition.BODY);
  // 当前显示的代码
  const currentSelectedDefinitionBodyCode = computed({
    get() {
      if (currentSelectedDefinitionResponse.value?.body.bodyType === ResponseBodyFormat.JSON) {
        return currentSelectedDefinitionResponse.value.body.jsonBody.jsonValue;
      }
      if (currentSelectedDefinitionResponse.value?.body.bodyType === ResponseBodyFormat.XML) {
        return currentSelectedDefinitionResponse.value.body.xmlBody.value;
      }
      return currentSelectedDefinitionResponse.value?.body.rawBody.value;
    },
    set(val) {
      if (
        currentSelectedDefinitionResponse.value?.body.bodyType === ResponseBodyFormat.JSON &&
        currentSelectedDefinitionResponse.value.body.jsonBody
      ) {
        currentSelectedDefinitionResponse.value.body.jsonBody.jsonValue = val || '';
      } else if (
        currentSelectedDefinitionResponse.value?.body.bodyType === ResponseBodyFormat.XML &&
        currentSelectedDefinitionResponse.value.body.xmlBody
      ) {
        currentSelectedDefinitionResponse.value.body.xmlBody.value = val || '';
      } else if (currentSelectedDefinitionResponse.value) {
        currentSelectedDefinitionResponse.value.body.rawBody.value = val || '';
      }
    },
  });
  // 当前代码编辑器的语言
  const currentSelectedDefinitionCodeLanguage = computed(() => {
    if (currentSelectedDefinitionResponse.value?.body.bodyType === ResponseBodyFormat.JSON) {
      return LanguageEnum.JSON;
    }
    if (currentSelectedDefinitionResponse.value?.body.bodyType === ResponseBodyFormat.XML) {
      return LanguageEnum.XML;
    }
    return LanguageEnum.PLAINTEXT;
  });
</script>

<style lang="less" scoped>
  .response-head {
    @apply flex flex-wrap items-center justify-between border-b;

    padding: 8px 16px;
    border-color: var(--color-text-n8);
    gap: 8px;
  }
  .arco-tabs-content {
    @apply hidden;
  }
</style>
