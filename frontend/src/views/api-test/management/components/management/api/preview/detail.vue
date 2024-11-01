<template>
  <a-collapse v-model:active-key="activeDetailKey" :bordered="false">
    <a-collapse-item key="request">
      <template #header>
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-[4px]">
            <div class="font-medium">{{ t('apiTestManagement.requestParams') }}</div>
            <div v-if="activeDetailKey.includes('request')" class="down-icon">
              <icon-down :size="10" class="block" />
            </div>
            <div v-else class="h-[16px] w-[16px] !rounded-full p-[4px]">
              <icon-right :size="10" class="block" />
            </div>
          </div>
          <ApiChangeTag
            :ignore-api-change="props.detail.ignoreApiChange"
            :ignore-api-diff="props.detail.ignoreApiDiff"
            :inconsistent-with-api="props.detail.inconsistentWithApi"
            @show-diff="showDiffDrawer"
          />
        </div>
      </template>
      <div class="detail-collapse-item">
        <template v-if="props.detail.protocol === 'HTTP'">
          <div v-if="previewDetail.headers.length > 0" class="detail-item">
            <div class="detail-item-title">
              <div class="detail-item-title-text">{{ t('apiTestManagement.requestHeader') }}</div>
              <div class="detail-item-title-right">
                <a-button
                  v-show="headerShowType === 'raw'"
                  type="outline"
                  class="arco-btn-outline--secondary mr-[12px] p-[0_8px]"
                  size="small"
                  @click="copyScript(headerRawCode)"
                >
                  <template #icon>
                    <MsIcon type="icon-icon_copy_outlined" class="text-var(--color-text-4)" />
                  </template>
                  {{ t('common.copy') }}
                </a-button>
                <a-radio-group v-model:model-value="headerShowType" type="button" size="mini">
                  <a-radio value="table">Table</a-radio>
                  <a-radio value="raw">Raw</a-radio>
                </a-radio-group>
              </div>
            </div>
            <MsFormTable
              v-show="headerShowType === 'table'"
              :columns="headerColumns"
              :data="previewDetail.headers?.filter((e) => e.key !== '') || []"
              :selectable="false"
              :scroll="{ x: '100%' }"
            />
            <MsCodeEditor
              v-show="headerShowType === 'raw'"
              :model-value="headerRawCode"
              theme="MS-text"
              is-adaptive
              :show-full-screen="false"
              :show-theme-change="false"
              read-only
            />
            <a-divider type="dashed" :margin="0" class="!mt-[16px] border-[var(--color-text-n8)]" />
          </div>
          <div v-if="previewDetail.query.length > 0" class="detail-item">
            <div class="detail-item-title">
              <div class="detail-item-title-text">Query</div>
              <div class="detail-item-title-right">
                <a-button
                  v-show="queryShowType === 'raw'"
                  type="outline"
                  class="arco-btn-outline--secondary mr-[12px] p-[0_8px]"
                  size="small"
                  @click="copyScript(queryRawCode)"
                >
                  <template #icon>
                    <MsIcon type="icon-icon_copy_outlined" class="text-var(--color-text-4)" />
                  </template>
                  {{ t('common.copy') }}
                </a-button>
                <a-radio-group v-model:model-value="queryShowType" type="button" size="mini">
                  <a-radio value="table">Table</a-radio>
                  <a-radio value="raw">Raw</a-radio>
                </a-radio-group>
              </div>
            </div>
            <MsFormTable
              v-show="queryShowType === 'table'"
              :columns="queryRestColumns"
              :data="previewDetail.query?.filter((e) => e.key !== '') || []"
              :selectable="false"
              :show-setting="true"
              :table-key="TableKeyEnum.API_TEST_DEBUG_QUERY"
              :scroll="{ x: '100%' }"
            />
            <MsCodeEditor
              v-show="queryShowType === 'raw'"
              :model-value="queryRawCode"
              theme="MS-text"
              is-adaptive
              :show-full-screen="false"
              :show-theme-change="false"
              read-only
            />
            <a-divider type="dashed" :margin="0" class="!mt-[16px] border-[var(--color-text-n8)]" />
          </div>
          <div v-if="previewDetail.rest.length > 0" class="detail-item">
            <div class="detail-item-title">
              <div class="detail-item-title-text">REST</div>
              <div class="detail-item-title-right">
                <a-button
                  v-show="restShowType === 'raw'"
                  type="outline"
                  class="arco-btn-outline--secondary mr-[12px] p-[0_8px]"
                  size="small"
                  @click="copyScript(restRawCode)"
                >
                  <template #icon>
                    <MsIcon type="icon-icon_copy_outlined" class="text-var(--color-text-4)" />
                  </template>
                  {{ t('common.copy') }}
                </a-button>
                <a-radio-group v-model:model-value="restShowType" type="button" size="mini">
                  <a-radio value="table">Table</a-radio>
                  <a-radio value="raw">Raw</a-radio>
                </a-radio-group>
              </div>
            </div>
            <MsFormTable
              v-show="restShowType === 'table'"
              :columns="queryRestColumns?.filter((e) => e.key !== '')"
              :data="previewDetail.rest || []"
              :selectable="false"
              :show-setting="true"
              :table-key="TableKeyEnum.API_TEST_DEBUG_REST"
              :scroll="{ x: '100%' }"
            />
            <MsCodeEditor
              v-show="restShowType === 'raw'"
              :model-value="restRawCode"
              theme="MS-text"
              is-adaptive
              :show-full-screen="false"
              :show-theme-change="false"
              read-only
            />
            <a-divider type="dashed" :margin="0" class="!mt-[16px] border-[var(--color-text-n8)]" />
          </div>
          <div v-if="previewDetail.body.bodyType !== RequestBodyFormat.NONE" class="detail-item">
            <div class="detail-item-title">
              <div class="detail-item-title-text">
                {{ `${t('apiTestManagement.requestBody')}-${previewDetail.body.bodyType}` }}
              </div>
              <div v-if="previewDetail.body.bodyType === RequestBodyFormat.JSON" class="detail-item-title-right">
                <a-button
                  v-show="bodyShowType === 'json' || props.isCase"
                  type="outline"
                  class="arco-btn-outline--secondary p-[0_8px]"
                  size="small"
                  @click="copyScript(bodyCode)"
                >
                  <template #icon>
                    <MsIcon type="icon-icon_copy_outlined" class="text-var(--color-text-4)" />
                  </template>
                  {{ t('common.copy') }}
                </a-button>
                <a-radio-group
                  v-if="!props.isCase"
                  v-model:model-value="bodyShowType"
                  type="button"
                  size="mini"
                  class="ml-[12px]"
                >
                  <a-radio value="schema">Schema</a-radio>
                  <a-radio value="json">JSON</a-radio>
                </a-radio-group>
              </div>
            </div>
            <!--            <div
              v-if="previewDetail.body.bodyType === RequestBodyFormat.NONE"
              class="flex h-[100px] items-center justify-center rounded-[var(&#45;&#45;border-radius-small)] bg-[var(&#45;&#45;color-text-n9)] text-[var(&#45;&#45;color-text-4)]"
            >
              {{ t('apiTestDebug.noneBody') }}
            </div>-->
            <MsFormTable
              v-if="
                previewDetail.body.bodyType === RequestBodyFormat.FORM_DATA ||
                previewDetail.body.bodyType === RequestBodyFormat.WWW_FORM ||
                previewDetail.body.bodyType === RequestBodyFormat.BINARY
              "
              :columns="bodyColumns"
              :data="bodyTableData"
              :selectable="false"
              :show-setting="true"
              :table-key="TableKeyEnum.API_TEST_DEBUG_FORM_DATA"
              :scroll="{ x: '100%' }"
            />
            <template
              v-else-if="
                [RequestBodyFormat.JSON, RequestBodyFormat.RAW, RequestBodyFormat.XML].includes(
                  previewDetail.body.bodyType
                )
              "
            >
              <MsJsonSchema
                v-if="
                  previewDetail.body.bodyType === RequestBodyFormat.JSON && bodyShowType === 'schema' && !props.isCase
                "
                :data="previewDetail.body.jsonBody.jsonSchemaTableData"
                disabled
              />
              <MsCodeEditor
                v-else
                :model-value="bodyCode"
                theme="vs"
                is-adaptive
                :language="bodyCodeLanguage"
                :show-full-screen="false"
                :show-theme-change="false"
                read-only
              />
            </template>
            <a-divider type="dashed" :margin="0" class="!mt-[16px] border-[var(--color-text-n8)]" />
          </div>
        </template>
        <div v-else class="detail-item">
          <div class="detail-item-title">
            <div class="detail-item-title-text">{{ t('apiTestManagement.requestData') }}</div>
            <div class="detail-item-title-right">
              <a-button
                v-show="pluginShowType === 'raw'"
                type="outline"
                class="arco-btn-outline--secondary mr-[12px] p-[0_8px]"
                size="small"
                @click="copyScript(pluginRawCode)"
              >
                <template #icon>
                  <MsIcon type="icon-icon_copy_outlined" class="text-var(--color-text-4)" />
                </template>
                {{ t('common.copy') }}
              </a-button>
              <a-radio-group v-model:model-value="pluginShowType" type="button" size="mini">
                <a-radio value="table">Table</a-radio>
                <a-radio value="raw">Raw</a-radio>
              </a-radio-group>
            </div>
          </div>
          <MsFormTable
            v-show="pluginShowType === 'table'"
            :columns="pluginTableColumns"
            :data="pluginTableData?.filter((e) => e.key !== '')"
            :selectable="false"
            :scroll="{ x: '100%' }"
          />
          <MsCodeEditor
            v-show="pluginShowType === 'raw'"
            :model-value="pluginRawCode"
            theme="MS-text"
            height="400px"
            :show-full-screen="false"
            :show-theme-change="false"
            read-only
          />
          <a-divider type="dashed" :margin="0" class="!mt-[16px] border-[var(--color-text-n8)]" />
        </div>
      </div>
    </a-collapse-item>
    <a-collapse-item
      v-if="
        (previewDetail.responseDefinition &&
          previewDetail.responseDefinition.length > 0 &&
          props.detail.protocol === 'HTTP') ||
        props.isCase
      "
      key="response"
    >
      <template #header>
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-[4px]">
            <div class="font-medium">{{ t('apiTestManagement.responseContent') }}</div>
            <div v-if="activeDetailKey.includes('response')" class="down-icon">
              <icon-down :size="10" class="block" />
            </div>
            <div v-else class="h-[16px] w-[16px] !rounded-full p-[4px]">
              <icon-right :size="10" class="block" />
            </div>
          </div>
          <responseCodeTimeSize v-if="props.isCase" :request-result="previewDetail.response?.requestResults[0]" />
        </div>
      </template>
      <template v-if="!props.isCase">
        <MsEditableTab
          v-model:active-tab="activeResponse"
          :tabs="previewDetail.responseDefinition?.map((e) => ({ ...e, closable: false })) || []"
          hide-more-action
          readonly
          class="my-[8px]"
        >
          <template #label="{ tab }">
            <div class="response-tab">
              <div v-if="tab.defaultFlag" class="response-tab-default-icon"></div>
              {{ t(tab.label || tab.name) }}({{ tab.statusCode }})
            </div>
          </template>
        </MsEditableTab>
        <div class="detail-item !pt-0">
          <div class="detail-item-title">
            <div class="detail-item-title-text">
              {{ `${t('apiTestDebug.responseBody')}-${activeResponse?.body.bodyType}` }}
            </div>
            <div class="detail-item-title-right">
              <a-button
                v-show="
                  !(
                    activeResponse?.body.bodyType === ResponseBodyFormat.BINARY ||
                    (activeResponse?.body.bodyType === ResponseBodyFormat.JSON &&
                      activeResponse.body.jsonBody.enableJsonSchema)
                  )
                "
                type="outline"
                class="arco-btn-outline--secondary p-[0_8px]"
                size="small"
                @click="copyScript(responseCode || '')"
              >
                <template #icon>
                  <MsIcon type="icon-icon_copy_outlined" class="text-var(--color-text-4)" />
                </template>
                {{ t('common.copy') }}
              </a-button>
              <div
                v-if="activeResponse?.body.bodyType === ResponseBodyFormat.JSON"
                class="ml-[12px] flex items-center gap-[8px]"
              >
                <MsButton
                  type="text"
                  class="!mr-0"
                  :class="
                    activeResponse.body.jsonBody.enableJsonSchema
                      ? 'font-medium !text-[rgb(var(--primary-5))]'
                      : '!text-[var(--color-text-4)]'
                  "
                  @click="handleChangeJsonType('Schema')"
                >
                  Schema
                </MsButton>
                <a-divider :margin="0" direction="vertical"></a-divider>
                <MsButton
                  type="text"
                  class="!mr-0"
                  :class="
                    !activeResponse.body.jsonBody.enableJsonSchema
                      ? 'font-medium !text-[rgb(var(--primary-5))]'
                      : '!text-[var(--color-text-4)]'
                  "
                  @click="handleChangeJsonType('Json')"
                >
                  Json
                </MsButton>
              </div>
            </div>
          </div>
          <MsFormTable
            v-if="activeResponse?.body.bodyType === ResponseBodyFormat.BINARY"
            :columns="responseBodyColumns"
            :data="responseBodyTableData"
            :selectable="false"
            :scroll="{ x: '100%' }"
          />
          <MsJsonSchema
            v-else-if="
              activeResponse?.body.bodyType === ResponseBodyFormat.JSON && activeResponse.body.jsonBody.enableJsonSchema
            "
            :data="activeResponse.body.jsonBody.jsonSchemaTableData"
            disabled
          />
          <MsCodeEditor
            v-else
            :model-value="responseCode"
            theme="vs"
            is-adaptive
            :language="responseCodeLanguage"
            :show-full-screen="false"
            :show-theme-change="false"
            read-only
          />
        </div>
        <div v-if="activeResponse?.headers && activeResponse?.headers.length > 0" class="detail-item">
          <div class="detail-item-title">
            <div class="detail-item-title-text">
              {{ t('apiTestDebug.responseHeader') }}
            </div>
          </div>
          <MsFormTable
            :columns="responseHeaderColumns"
            :data="activeResponse?.headers?.filter((e) => e.key !== '') || []"
            :selectable="false"
            :scroll="{ x: '100%' }"
          />
        </div>
      </template>
      <a-spin v-else :loading="previewDetail.executeLoading" class="h-[calc(100%-45px)] w-full pb-[18px]">
        <div
          v-show="
            previewDetail.protocol === 'HTTP' || previewDetail.response?.requestResults[0]?.responseResult.responseCode
          "
          class="h-full"
        >
          <Result
            v-model:active-tab="previewDetail.responseActiveTab"
            :request-result="previewDetail.response?.requestResults[0]"
            :console="previewDetail.response?.console"
            :is-http-protocol="previewDetail.protocol === 'HTTP'"
            :is-priority-local-exec="props.isPriorityLocalExec"
            :request-url="previewDetail.url"
            is-definition
            @execute="emit('execute', props.isPriorityLocalExec ? 'localExec' : 'serverExec')"
          />
        </div>
      </a-spin>
    </a-collapse-item>
  </a-collapse>
</template>

<script setup lang="ts">
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsEditableTab from '@/components/pure/ms-editable-tab/index.vue';
  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import MsFormTable, { FormTableColumn } from '@/components/pure/ms-form-table/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsJsonSchema from '@/components/pure/ms-json-schema/index.vue';
  import { parseSchemaToJsonSchemaTableData } from '@/components/pure/ms-json-schema/utils';
  import { ResponseItem } from '@/views/api-test/components/requestComposition/response/edit.vue';
  import responseCodeTimeSize from '@/views/api-test/components/requestComposition/response/responseCodeTimeSize.vue';
  import Result from '@/views/api-test/components/requestComposition/response/result.vue';
  import ApiChangeTag from '@/views/api-test/management/components/management/case/apiChangeTag.vue';

  import { getPluginScript } from '@/api/modules/api-test/common';
  import { getDocSharePluginScript } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import { PluginConfig, ProtocolItem } from '@/models/apiTest/common';
  import { RequestBodyFormat, RequestParamsType, ResponseBodyFormat } from '@/enums/apiEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import type { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  const props = defineProps<{
    isCase?: boolean; // case 详情
    detail: RequestParam;
    protocols: ProtocolItem[];
    isPriorityLocalExec?: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'execute', val: 'localExec' | 'serverExec'): void;
    (e: 'showDiff'): void;
  }>();

  const { t } = useI18n();
  const { copy, isSupported } = useClipboard({ legacy: true });
  const appStore = useAppStore();

  const previewDetail = ref<RequestParam>(props.detail);
  const activeResponse = ref<TabItem & ResponseItem>();

  const pluginLoading = ref(false);
  const pluginScriptMap = ref<Record<string, PluginConfig>>({}); // 存储初始化过后的插件配置
  const pluginShowType = ref('table');
  const pluginTableColumns: FormTableColumn[] = [
    {
      title: 'apiTestManagement.paramName',
      dataIndex: 'key',
      inputType: 'text',
      width: 250,
      showTooltip: true,
    },
    {
      title: 'apiTestManagement.paramVal',
      dataIndex: 'value',
      inputType: 'text',
    },
  ];
  const pluginTableData = computed(() => {
    if (pluginScriptMap.value[previewDetail.value.protocol]) {
      return (
        pluginScriptMap.value[previewDetail.value.protocol].apiDefinitionFields?.map((e) => ({
          key: e,
          value: previewDetail.value[e],
        })) || []
      );
    }
    return [];
  });
  const pluginRawCode = computed(() => {
    if (pluginScriptMap.value[previewDetail.value.protocol]) {
      return (
        pluginScriptMap.value[previewDetail.value.protocol].apiDefinitionFields
          ?.map((e) => `${e}:${previewDetail.value[e]}`)
          .join('\n') || ''
      );
    }
    return '';
  });
  const pluginError = ref(false);
  const docShareId = inject<string>('docShareId', '');
  async function initPluginScript(protocol: string) {
    let pluginId: string = '';
    if (!docShareId) {
      pluginId = props.protocols.find((e) => e.protocol === protocol)?.pluginId ?? '';
      if (!pluginId) {
        Message.warning(t('apiTestDebug.noPluginTip'));
        pluginError.value = true;
        return;
      }
    }
    pluginError.value = false;
    if (pluginScriptMap.value[protocol] !== undefined) {
      // 已经初始化过
      return;
    }
    try {
      pluginLoading.value = true;
      let res;
      if (docShareId) {
        res = await getDocSharePluginScript(previewDetail.value.id, appStore.currentOrgId);
      } else {
        res = await getPluginScript(pluginId);
      }
      pluginScriptMap.value[protocol] = res;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      pluginLoading.value = false;
    }
  }

  watchEffect(() => {
    if (!props.isCase) return;
    // case编辑后需要刷新数据
    previewDetail.value = cloneDeep(props.detail); // props.detail是嵌套的引用类型，防止不必要的修改来源影响props.detail的数据
  });

  watch(
    () => props.detail.id,
    () => {
      previewDetail.value = cloneDeep(props.detail); // props.detail是嵌套的引用类型，防止不必要的修改来源影响props.detail的数据
      [activeResponse.value] = previewDetail.value.responseDefinition || [];
      if (previewDetail.value.protocol !== 'HTTP') {
        // 初始化插件脚本
        initPluginScript(previewDetail.value.protocol);
      }
    },
    {
      immediate: true,
    }
  );

  const activeDetailKey = ref(['request', 'response']);

  function copyScript(val: string) {
    if (isSupported) {
      copy(val);
      Message.success(t('common.copySuccess'));
    } else {
      Message.warning(t('apiTestDebug.copyNotSupport'));
    }
  }

  /**
   * 请求头
   */
  const headerColumns: FormTableColumn[] = [
    {
      title: 'apiTestManagement.paramName',
      dataIndex: 'key',
      inputType: 'text',
      width: 220,
    },
    {
      title: 'apiTestManagement.paramVal',
      dataIndex: 'value',
      inputType: 'text',
      width: 220,
    },
    {
      title: 'common.desc',
      dataIndex: 'description',
      inputType: 'text',
      showTooltip: true,
      width: 300,
    },
  ];
  const headerShowType = ref('table');
  const headerRawCode = computed(() => {
    return previewDetail.value.headers?.map((item) => `${item.key}:${item.value}`).join('\n');
  });

  /**
   * Query & Rest
   */
  const queryRestColumns: FormTableColumn[] = [
    {
      title: 'apiTestManagement.paramName',
      dataIndex: 'key',
      inputType: 'text',
      width: 220,
      columnSelectorDisabled: true,
    },
    {
      title: 'apiTestManagement.required',
      dataIndex: 'required',
      slotName: 'required',
      inputType: 'text',
      columnSelectorDisabled: true,
      valueFormat: (record) => {
        return record.required ? t('common.yes') : t('common.no');
      },
      width: 68,
    },
    {
      title: 'apiTestDebug.paramType',
      dataIndex: 'paramType',
      inputType: 'text',
      width: 96,
      columnSelectorDisabled: true,
    },
    {
      title: 'apiTestManagement.paramVal',
      dataIndex: 'value',
      inputType: 'text',
    },
    {
      title: 'apiTestDebug.paramLengthRange',
      dataIndex: 'lengthRange',
      slotName: 'lengthRange',
      inputType: 'text',
      showInTable: false,
      valueFormat: (record) => {
        return [null, undefined].includes(record.minLength) && [null, undefined].includes(record.maxLength)
          ? '-'
          : `${record.minLength} ${t('common.to')} ${record.maxLength}`;
      },
      width: 110,
    },
    {
      title: 'apiTestDebug.encode',
      dataIndex: 'encode',
      slotName: 'encode',
      inputType: 'text',
      showInTable: false,
      valueFormat: (record) => {
        return record.encode ? t('common.yes') : t('common.no');
      },
      width: 68,
    },
    {
      title: 'common.desc',
      dataIndex: 'description',
      inputType: 'text',
      showTooltip: true,
    },
    {
      title: '',
      dataIndex: 'operation',
      slotName: 'operation',
      fixed: 'right',
      width: 100,
    },
  ];
  const queryShowType = ref('table');
  const queryRawCode = computed(() => {
    return previewDetail.value.query?.map((item) => `${item.key}:${item.value}`).join('\n');
  });
  const restShowType = ref('table');
  const restRawCode = computed(() => {
    return previewDetail.value.rest?.map((item) => `${item.key}:${item.value}`).join('\n');
  });

  /**
   * 请求体
   */
  const bodyColumns = computed<FormTableColumn[]>(() => {
    if ([RequestBodyFormat.FORM_DATA, RequestBodyFormat.WWW_FORM].includes(previewDetail.value.body.bodyType)) {
      return [
        {
          title: 'apiTestManagement.paramName',
          dataIndex: 'key',
          inputType: 'text',
          width: 220,
          columnSelectorDisabled: true,
        },
        {
          title: 'apiTestManagement.required',
          dataIndex: 'required',
          slotName: 'required',
          inputType: 'text',
          columnSelectorDisabled: true,
          valueFormat: (record) => {
            return record.required ? t('common.yes') : t('common.no');
          },
          width: 68,
        },
        {
          title: 'apiTestManagement.paramsType',
          dataIndex: 'paramType',
          inputType: 'text',
          width: 96,
          columnSelectorDisabled: true,
        },
        {
          title: 'apiTestManagement.paramVal',
          dataIndex: 'value',
          inputType: 'text',
          showTooltip: true,
        },
        {
          title: 'apiTestDebug.paramLengthRange',
          dataIndex: 'lengthRange',
          slotName: 'lengthRange',
          inputType: 'text',
          showInTable: false,
          valueFormat: (record) => {
            return [null, undefined].includes(record.minLength) && [null, undefined].includes(record.maxLength)
              ? '-'
              : `${record.minLength} ${t('common.to')} ${record.maxLength}`;
          },
          width: 110,
        },
        {
          title: 'common.desc',
          dataIndex: 'description',
          inputType: 'text',
          showTooltip: true,
        },
        {
          title: '',
          dataIndex: 'operation',
          slotName: 'operation',
          fixed: 'right',
          width: 100,
        },
      ];
    }
    return [
      {
        title: 'common.desc',
        dataIndex: 'description',
        inputType: 'text',
        showTooltip: true,
      },
      {
        title: 'apiTestManagement.paramVal',
        dataIndex: 'value',
        inputType: 'text',
        showTooltip: true,
      },
    ];
  });
  const bodyShowType = ref('schema');
  const bodyTableData = computed(() => {
    switch (previewDetail.value.body.bodyType) {
      case RequestBodyFormat.FORM_DATA:
        return (previewDetail.value.body.formDataBody?.formValues || [])
          .map((e) => ({
            ...e,
            value: e.paramType === RequestParamsType.FILE ? e.files?.map((file) => file.fileName).join('、') : e.value,
          }))
          ?.filter((e) => e.key !== '');
      case RequestBodyFormat.WWW_FORM:
        return previewDetail.value.body.wwwFormBody?.formValues?.filter((e) => e.key !== '') || [];
      case RequestBodyFormat.BINARY:
        return [
          {
            description: previewDetail.value.body.binaryBody.description,
            value: previewDetail.value.body.binaryBody.file?.fileName,
          },
        ];
      default:
        return [];
    }
  });
  const bodyCode = computed(() => {
    switch (previewDetail.value.body.bodyType) {
      case RequestBodyFormat.FORM_DATA:
        return previewDetail.value.body.formDataBody?.formValues?.map((item) => `${item.key}:${item.value}`).join('\n');
      case RequestBodyFormat.WWW_FORM:
        return previewDetail.value.body.wwwFormBody?.formValues?.map((item) => `${item.key}:${item.value}`).join('\n');
      case RequestBodyFormat.RAW:
        return previewDetail.value.body.rawBody?.value;
      case RequestBodyFormat.JSON:
        return previewDetail.value.body.jsonBody?.jsonValue;
      case RequestBodyFormat.XML:
        return previewDetail.value.body.xmlBody?.value;
      default:
        return '';
    }
  });
  watchEffect(() => {
    if (previewDetail.value.body.jsonBody?.jsonSchema) {
      const { result } = parseSchemaToJsonSchemaTableData(previewDetail.value.body.jsonBody.jsonSchema);
      previewDetail.value.body.jsonBody.jsonSchemaTableData = result;
    } else if (
      !previewDetail.value.body.jsonBody.jsonSchemaTableData ||
      previewDetail.value.body.jsonBody.jsonSchemaTableData.length === 0
    ) {
      previewDetail.value.body.jsonBody.jsonSchemaTableData = [];
    }
  });
  const bodyCodeLanguage = computed(() => {
    if (previewDetail.value.body.bodyType === RequestBodyFormat.JSON) {
      return LanguageEnum.JSON;
    }
    if (previewDetail.value.body.bodyType === RequestBodyFormat.XML) {
      return LanguageEnum.XML;
    }
    return LanguageEnum.PLAINTEXT;
  });

  /**
   * 响应内容
   */
  const responseCode = computed(() => {
    switch (activeResponse.value?.body.bodyType) {
      case ResponseBodyFormat.JSON:
        return activeResponse.value?.body.jsonBody?.jsonValue;
      case ResponseBodyFormat.XML:
        return activeResponse.value?.body.xmlBody?.value;
      case ResponseBodyFormat.RAW:
        return activeResponse.value?.body.rawBody?.value;
      default:
        return '';
    }
  });
  const responseCodeLanguage = computed(() => {
    if (activeResponse.value?.body.bodyType === ResponseBodyFormat.JSON) {
      return LanguageEnum.JSON;
    }
    if (activeResponse.value?.body.bodyType === ResponseBodyFormat.XML) {
      return LanguageEnum.XML;
    }
    return LanguageEnum.PLAINTEXT;
  });
  const responseHeaderColumns: FormTableColumn[] = [
    {
      title: 'apiTestManagement.paramName',
      dataIndex: 'key',
      inputType: 'text',
    },
    {
      title: 'apiTestManagement.paramVal',
      dataIndex: 'value',
      inputType: 'text',
    },
  ];
  const responseBodyColumns: FormTableColumn[] = [
    {
      title: 'common.desc',
      dataIndex: 'description',
      inputType: 'text',
      showTooltip: true,
    },
    {
      title: 'apiTestManagement.paramVal',
      dataIndex: 'value',
      inputType: 'text',
      showTooltip: true,
    },
  ];
  const responseBodyTableData = computed(() => {
    return activeResponse.value?.body.bodyType === ResponseBodyFormat.BINARY
      ? [
          {
            description: activeResponse.value?.body.binaryBody.description,
            value: activeResponse.value?.body.binaryBody.file?.fileName,
          },
        ]
      : [];
  });

  watchEffect(() => {
    if (activeResponse.value) {
      if (activeResponse.value.body.jsonBody.jsonSchema) {
        const { result } = parseSchemaToJsonSchemaTableData(activeResponse.value.body.jsonBody.jsonSchema);
        activeResponse.value.body.jsonBody.jsonSchemaTableData = result;
      } else {
        activeResponse.value.body.jsonBody.jsonSchemaTableData = [];
      }
    }
  });
  function handleChangeJsonType(type: 'Schema' | 'Json') {
    if (activeResponse.value) {
      activeResponse.value.body.jsonBody.enableJsonSchema = type === 'Schema';
    }
  }

  // 查看diff对比
  function showDiffDrawer() {
    emit('showDiff');
  }
</script>

<style lang="less" scoped>
  .down-icon {
    padding: 3px;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    color: rgb(var(--primary-5));
    background-color: rgb(var(--primary-1));
  }
  .arco-collapse {
    @apply h-full;

    border-radius: 0;
    :deep(.arco-collapse-item-icon-hover) {
      @apply !hidden;
    }
    :deep(.arco-collapse-item-header) {
      .arco-collapse-item-header-title {
        @apply block w-full;

        padding: 8px 0;
        border-radius: var(--border-radius-small);
      }
    }
    .detail-collapse-item {
      margin-bottom: 16px;
    }
  }
  .detail-item {
    padding-top: 16px;
    .detail-item-title {
      @apply flex items-center justify-between;

      margin-bottom: 8px;
      gap: 16px;
      .detail-item-title-text {
        @apply font-medium;

        color: var(--color-text-1);
      }
      .detail-item-title-right {
        @apply flex items-center;
      }
    }
    :deep(.ms-code-editor-container) {
      padding: 0;
    }
  }
  .response-tab {
    @apply flex items-center;
    .response-tab-default-icon {
      @apply rounded-full;

      margin-right: 4px;
      width: 16px;
      height: 16px;
      background: url('@/assets/svg/icons/default.svg') no-repeat;
      background-size: contain;
      box-shadow: 0 0 7px 0 rgb(15 0 78 / 9%);
    }
  }
  :deep(.arco-table-td-content) {
    padding: 5px 8px;
  }
  :deep(.ms-json-schema) .arco-table-td-content {
    padding: 0;
  }
</style>
