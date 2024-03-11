<template>
  <a-spin :loading="pluginLoading" class="h-full w-full overflow-hidden">
    <div class="px-[18px] pt-[16px]">
      <MsDetailCard
        :title="`【${preivewDetail.num}】${preivewDetail.name}`"
        :description="description"
        :simple-show-count="4"
      >
        <template #titleAppend>
          <apiStatus :status="preivewDetail.status" size="small" />
        </template>
        <template #titleRight>
          <a-button
            type="outline"
            :loading="followLoading"
            size="mini"
            class="arco-btn-outline--secondary mr-[4px] !bg-transparent"
            @click="toggleFollowReview"
          >
            <div class="flex items-center gap-[4px]">
              <MsIcon
                :type="preivewDetail.follow ? 'icon-icon_collect_filled' : 'icon-icon_collection_outlined'"
                :class="`${preivewDetail.follow ? 'text-[rgb(var(--warning-6))]' : 'text-[var(--color-text-4)]'}`"
                :size="14"
              />
              {{ t(preivewDetail.follow ? 'common.forked' : 'common.fork') }}
            </div>
          </a-button>
          <a-button type="outline" size="mini" class="arco-btn-outline--secondary !bg-transparent" @click="share">
            <div class="flex items-center gap-[4px]">
              <MsIcon type="icon-icon_share1" class="text-[var(--color-text-4)]" :size="14" />
              {{ t('common.share') }}
            </div>
          </a-button>
        </template>
        <template #type="{ value }">
          <apiMethodName :method="value as RequestMethods" tag-size="small" is-tag />
        </template>
      </MsDetailCard>
    </div>
    <div class="h-[calc(100%-124px)]">
      <a-tabs v-model:active-key="activeKey" class="h-full" animation lazy-load>
        <a-tab-pane key="detail" :title="t('apiTestManagement.detail')" class="overflow-y-auto px-[18px] py-[16px]">
          <a-collapse v-model:active-key="activeDetailKey" :bordered="false">
            <a-collapse-item key="request">
              <template #header>
                <div class="flex items-center gap-[4px]">
                  <div v-if="activeDetailKey.includes('request')" class="down-icon">
                    <icon-down :size="10" class="block" />
                  </div>
                  <div v-else class="h-[16px] w-[16px] !rounded-full p-[4px]">
                    <icon-right :size="10" class="block" />
                  </div>
                  <div class="font-medium">{{ t('apiTestManagement.requestParams') }}</div>
                </div>
              </template>
              <div class="detail-collapse-item">
                <template v-if="props.detail.protocol === 'HTTP'">
                  <div v-if="preivewDetail.headers.length > 0" class="detail-item">
                    <div class="detail-item-title">
                      <div class="detail-item-title-text">{{ t('apiTestManagement.requestHeader') }}</div>
                      <a-radio-group v-model:model-value="headerShowType" type="button" size="mini">
                        <a-radio value="table">Table</a-radio>
                        <a-radio value="raw">Raw</a-radio>
                      </a-radio-group>
                    </div>
                    <MsFormTable
                      v-show="headerShowType === 'table'"
                      :columns="headerColumns"
                      :data="preivewDetail.headers || []"
                      :selectable="false"
                    />
                    <MsCodeEditor
                      v-show="headerShowType === 'raw'"
                      :model-value="headerRawCode"
                      class="flex-1"
                      theme="MS-text"
                      height="200px"
                      :show-full-screen="false"
                      :show-theme-change="false"
                      read-only
                    >
                      <template #rightTitle>
                        <a-button
                          type="outline"
                          class="arco-btn-outline--secondary p-[0_8px]"
                          size="mini"
                          @click="copyScript(headerRawCode)"
                        >
                          <template #icon>
                            <MsIcon type="icon-icon_copy_outlined" class="text-var(--color-text-4)" size="12" />
                          </template>
                        </a-button>
                      </template>
                    </MsCodeEditor>
                    <a-divider type="dashed" :margin="0" class="!mt-[16px] border-[var(--color-text-n8)]" />
                  </div>
                  <div v-if="preivewDetail.query.length > 0" class="detail-item">
                    <div class="detail-item-title">
                      <div class="detail-item-title-text">Query</div>
                      <a-radio-group v-model:model-value="queryShowType" type="button" size="mini">
                        <a-radio value="table">Table</a-radio>
                        <a-radio value="raw">Raw</a-radio>
                      </a-radio-group>
                    </div>
                    <MsFormTable
                      v-show="queryShowType === 'table'"
                      :columns="queryRestColumns"
                      :data="preivewDetail.query || []"
                      :selectable="false"
                    />
                    <MsCodeEditor
                      v-show="queryShowType === 'raw'"
                      :model-value="queryRawCode"
                      class="flex-1"
                      theme="MS-text"
                      height="200px"
                      :show-full-screen="false"
                      :show-theme-change="false"
                      read-only
                    >
                      <template #rightTitle>
                        <a-button
                          type="outline"
                          class="arco-btn-outline--secondary p-[0_8px]"
                          size="mini"
                          @click="copyScript(queryRawCode)"
                        >
                          <template #icon>
                            <MsIcon type="icon-icon_copy_outlined" class="text-var(--color-text-4)" size="12" />
                          </template>
                        </a-button>
                      </template>
                    </MsCodeEditor>
                    <a-divider type="dashed" :margin="0" class="!mt-[16px] border-[var(--color-text-n8)]" />
                  </div>
                  <div v-if="preivewDetail.rest.length > 0" class="detail-item">
                    <div class="detail-item-title">
                      <div class="detail-item-title-text">Rest</div>
                      <a-radio-group v-model:model-value="restShowType" type="button" size="mini">
                        <a-radio value="table">Table</a-radio>
                        <a-radio value="raw">Raw</a-radio>
                      </a-radio-group>
                    </div>
                    <MsFormTable
                      v-show="restShowType === 'table'"
                      :columns="queryRestColumns"
                      :data="preivewDetail.rest || []"
                      :selectable="false"
                    />
                    <MsCodeEditor
                      v-show="restShowType === 'raw'"
                      :model-value="restRawCode"
                      class="flex-1"
                      theme="MS-text"
                      height="200px"
                      :show-full-screen="false"
                      :show-theme-change="false"
                      read-only
                    >
                      <template #rightTitle>
                        <a-button
                          type="outline"
                          class="arco-btn-outline--secondary p-[0_8px]"
                          size="mini"
                          @click="copyScript(restRawCode)"
                        >
                          <template #icon>
                            <MsIcon type="icon-icon_copy_outlined" class="text-var(--color-text-4)" size="12" />
                          </template>
                        </a-button>
                      </template>
                    </MsCodeEditor>
                    <a-divider type="dashed" :margin="0" class="!mt-[16px] border-[var(--color-text-n8)]" />
                  </div>
                  <div class="detail-item">
                    <div class="detail-item-title">
                      <div class="detail-item-title-text">
                        {{ `${t('apiTestManagement.requestBody')}-${preivewDetail.body.bodyType}` }}
                      </div>
                      <!-- <a-radio-group
                    v-if="preivewDetail.body.bodyType !== RequestBodyFormat.NONE"
                    v-model:model-value="bodyShowType"
                    type="button"
                    size="mini"
                  >
                    <a-radio value="table">Table</a-radio>
                    <a-radio value="code">Code</a-radio>
                  </a-radio-group> -->
                    </div>
                    <div
                      v-if="preivewDetail.body.bodyType === RequestBodyFormat.NONE"
                      class="flex h-[100px] items-center justify-center rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] text-[var(--color-text-4)]"
                    >
                      {{ t('apiTestDebug.noneBody') }}
                    </div>
                    <MsFormTable
                      v-else-if="
                        preivewDetail.body.bodyType === RequestBodyFormat.FORM_DATA ||
                        preivewDetail.body.bodyType === RequestBodyFormat.WWW_FORM
                      "
                      :columns="bodyColumns"
                      :data="bodyTableData"
                      :selectable="false"
                    />
                    <MsCodeEditor
                      v-else-if="
                        [RequestBodyFormat.JSON, RequestBodyFormat.RAW, RequestBodyFormat.XML].includes(
                          preivewDetail.body.bodyType
                        )
                      "
                      :model-value="bodyCode"
                      class="flex-1"
                      theme="vs"
                      height="200px"
                      :language="bodyCodeLanguage"
                      :show-full-screen="false"
                      :show-theme-change="false"
                      read-only
                    >
                      <template #rightTitle>
                        <a-button
                          type="outline"
                          class="arco-btn-outline--secondary p-[0_8px]"
                          size="mini"
                          @click="copyScript(bodyCode)"
                        >
                          <template #icon>
                            <MsIcon type="icon-icon_copy_outlined" class="text-var(--color-text-4)" size="12" />
                          </template>
                        </a-button>
                      </template>
                    </MsCodeEditor>
                    <a-divider type="dashed" :margin="0" class="!mt-[16px] border-[var(--color-text-n8)]" />
                  </div>
                </template>
                <div v-else class="detail-item">
                  <div class="detail-item-title">
                    <div class="detail-item-title-text">{{ t('apiTestManagement.requestData') }}</div>
                    <a-radio-group v-model:model-value="pluginShowType" type="button" size="mini">
                      <a-radio value="table">Table</a-radio>
                      <a-radio value="raw">Raw</a-radio>
                    </a-radio-group>
                  </div>
                  <MsFormTable
                    v-show="pluginShowType === 'table'"
                    :columns="pluginTableColumns"
                    :data="pluginTableData"
                    :selectable="false"
                  />
                  <MsCodeEditor
                    v-show="pluginShowType === 'raw'"
                    :model-value="pluginRawCode"
                    class="flex-1"
                    theme="MS-text"
                    height="400px"
                    :show-full-screen="false"
                    :show-theme-change="false"
                    read-only
                  >
                    <template #rightTitle>
                      <a-button
                        type="outline"
                        class="arco-btn-outline--secondary p-[0_8px]"
                        size="mini"
                        @click="copyScript(pluginRawCode)"
                      >
                        <template #icon>
                          <MsIcon type="icon-icon_copy_outlined" class="text-var(--color-text-4)" size="12" />
                        </template>
                      </a-button>
                    </template>
                  </MsCodeEditor>
                  <a-divider type="dashed" :margin="0" class="!mt-[16px] border-[var(--color-text-n8)]" />
                </div>
              </div>
            </a-collapse-item>
            <a-collapse-item
              v-if="
                preivewDetail.responseDefinition &&
                preivewDetail.responseDefinition.length > 0 &&
                props.detail.protocol === 'HTTP'
              "
              key="response"
            >
              <template #header>
                <div class="flex items-center gap-[4px]">
                  <div v-if="activeDetailKey.includes('response')" class="down-icon">
                    <icon-down :size="10" class="block" />
                  </div>
                  <div v-else class="h-[16px] w-[16px] !rounded-full p-[4px]">
                    <icon-right :size="10" class="block" />
                  </div>
                  <div class="font-medium">{{ t('apiTestManagement.responseContent') }}</div>
                </div>
              </template>
              <MsEditableTab
                v-model:active-tab="activeResponse"
                :tabs="preivewDetail.responseDefinition?.map((e) => ({ ...e, closable: false })) || []"
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
                </div>
                <MsCodeEditor
                  v-if="activeResponse?.body.bodyType !== ResponseBodyFormat.BINARY"
                  :model-value="responseCode"
                  class="flex-1"
                  theme="vs"
                  height="200px"
                  :language="responseCodeLanguage"
                  :show-full-screen="false"
                  :show-theme-change="false"
                  read-only
                >
                  <template #rightTitle>
                    <a-button
                      type="outline"
                      class="arco-btn-outline--secondary p-[0_8px]"
                      size="mini"
                      @click="copyScript(responseCode || '')"
                    >
                      <template #icon>
                        <MsIcon type="icon-icon_copy_outlined" class="text-var(--color-text-4)" size="12" />
                      </template>
                    </a-button>
                  </template>
                </MsCodeEditor>
              </div>
              <div v-if="activeResponse?.headers && activeResponse?.headers.length > 0" class="detail-item">
                <div class="detail-item-title">
                  <div class="detail-item-title-text">
                    {{ t('apiTestDebug.responseHeader') }}
                  </div>
                </div>
                <MsFormTable
                  :columns="responseHeaderColumns"
                  :data="activeResponse?.headers || []"
                  :selectable="false"
                />
              </div>
            </a-collapse-item>
          </a-collapse>
        </a-tab-pane>
        <a-tab-pane key="reference" :title="t('apiTestManagement.reference')" class="px-[18px] py-[16px]"> </a-tab-pane>
        <a-tab-pane key="dependencies" :title="t('apiTestManagement.dependencies')" class="px-[18px] py-[16px]">
        </a-tab-pane>
        <a-tab-pane key="changeHistory" :title="t('apiTestManagement.changeHistory')" class="px-[18px] py-[16px]">
        </a-tab-pane>
      </a-tabs>
    </div>
  </a-spin>
</template>

<script setup lang="ts">
  import { useClipboard } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';
  import dayjs from 'dayjs';

  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsDetailCard from '@/components/pure/ms-detail-card/index.vue';
  import MsEditableTab from '@/components/pure/ms-editable-tab/index.vue';
  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import MsFormTable, { FormTableColumn } from '@/components/pure/ms-form-table/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';
  import { ResponseItem } from '@/views/api-test/components/requestComposition/response/edit.vue';

  import { getPluginScript } from '@/api/modules/api-test/common';
  import { toggleFollowDefinition } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import { findNodeByKey } from '@/utils';

  import { PluginConfig, ProtocolItem } from '@/models/apiTest/common';
  import { ModuleTreeNode } from '@/models/common';
  import { RequestBodyFormat, RequestMethods, RequestParamsType, ResponseBodyFormat } from '@/enums/apiEnum';

  import type { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';
  import { getValidRequestTableParams } from '@/views/api-test/components/utils';

  const props = defineProps<{
    detail: RequestParam;
    moduleTree: ModuleTreeNode[];
    protocols: ProtocolItem[];
  }>();
  const emit = defineEmits(['updateFollow']);

  const { t } = useI18n();
  const { copy, isSupported } = useClipboard();

  const preivewDetail = ref<RequestParam>(cloneDeep(props.detail));
  const activeResponse = ref<TabItem & ResponseItem>();

  const pluginLoading = ref(false);
  const pluginScriptMap = ref<Record<string, PluginConfig>>({}); // 存储初始化过后的插件配置
  const pluginShowType = ref('table');
  const pluginTableColumns: FormTableColumn[] = [
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
  const pluginTableData = computed(() => {
    if (pluginScriptMap.value[preivewDetail.value.protocol]) {
      return (
        pluginScriptMap.value[preivewDetail.value.protocol].apiDefinitionFields?.map((e) => ({
          key: e,
          value: preivewDetail.value[e],
        })) || []
      );
    }
    return [];
  });
  const pluginRawCode = computed(() => {
    if (pluginScriptMap.value[preivewDetail.value.protocol]) {
      return (
        pluginScriptMap.value[preivewDetail.value.protocol].apiDefinitionFields
          ?.map((e) => `${e}:${preivewDetail.value[e]}`)
          .join('\n') || ''
      );
    }
    return '';
  });
  const pluginError = ref(false);
  async function initPluginScript(protocol: string) {
    const pluginId = props.protocols.find((e) => e.protocol === protocol)?.pluginId;
    if (!pluginId) {
      Message.warning(t('apiTestDebug.noPluginTip'));
      pluginError.value = true;
      return;
    }
    pluginError.value = false;
    if (pluginScriptMap.value[protocol] !== undefined) {
      // 已经初始化过
      return;
    }
    try {
      pluginLoading.value = true;
      const res = await getPluginScript(pluginId);
      pluginScriptMap.value[protocol] = res;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      pluginLoading.value = false;
    }
  }

  watchEffect(() => {
    preivewDetail.value = cloneDeep(props.detail); // props.detail是嵌套的引用类型，防止不必要的修改来源影响props.detail的数据
    const tableParam = getValidRequestTableParams(preivewDetail.value); // 在编辑props.detail时，参数表格会多出一行默认数据，需要去除
    preivewDetail.value = {
      ...preivewDetail.value,
      body: {
        ...preivewDetail.value.body,
        formDataBody: {
          formValues: tableParam.formDataBodyTableParams,
        },
        wwwFormBody: {
          formValues: tableParam.wwwFormBodyTableParams,
        },
      },
      headers: tableParam.headers,
      rest: tableParam.rest,
      query: tableParam.query,
      responseDefinition: tableParam.response,
    };
    [activeResponse.value] = tableParam.response;
    if (preivewDetail.value.protocol !== 'HTTP') {
      // 初始化插件脚本
      initPluginScript(preivewDetail.value.protocol);
    }
  });

  const description = computed(() => [
    {
      key: 'type',
      locale: 'apiTestManagement.apiType',
      value: preivewDetail.value.method,
    },
    {
      key: 'path',
      locale: 'apiTestManagement.path',
      value: preivewDetail.value.path,
    },
    {
      key: 'tags',
      locale: 'common.tag',
      value: preivewDetail.value.tags,
    },
    {
      key: 'description',
      locale: 'common.desc',
      value: preivewDetail.value.description,
      width: '100%',
    },
    {
      key: 'belongModule',
      locale: 'apiTestManagement.belongModule',
      value: findNodeByKey<ModuleTreeNode>(props.moduleTree, preivewDetail.value.moduleId, 'id')?.path,
    },
    {
      key: 'creator',
      locale: 'common.creator',
      value: preivewDetail.value.createUserName,
    },
    {
      key: 'createTime',
      locale: 'apiTestManagement.createTime',
      value: dayjs(preivewDetail.value.createTime).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      key: 'updateTime',
      locale: 'apiTestManagement.updateTime',
      value: dayjs(preivewDetail.value.updateTime).format('YYYY-MM-DD HH:mm:ss'),
    },
  ]);

  const followLoading = ref(false);
  async function toggleFollowReview() {
    try {
      followLoading.value = true;
      await toggleFollowDefinition(preivewDetail.value.id);
      Message.success(preivewDetail.value.follow ? t('common.unFollowSuccess') : t('common.followSuccess'));
      emit('updateFollow');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      followLoading.value = false;
    }
  }

  function share() {
    if (isSupported) {
      copy(`${window.location.href}&dId=${preivewDetail.value.id}`);
      Message.success(t('apiTestManagement.shareUrlCopied'));
    } else {
      Message.error(t('common.copyNotSupport'));
    }
  }

  const activeKey = ref('detail');
  const activeDetailKey = ref(['request', 'response']);

  async function copyScript(val: string) {
    if (isSupported) {
      await copy(val);
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
    },
    {
      title: 'apiTestManagement.paramVal',
      dataIndex: 'value',
      inputType: 'text',
    },
    {
      title: 'common.desc',
      dataIndex: 'description',
      inputType: 'text',
      showTooltip: true,
    },
  ];
  const headerShowType = ref('table');
  const headerRawCode = computed(() => {
    return preivewDetail.value.headers?.map((item) => `${item.key}:${item.value}`).join('\n');
  });

  /**
   * Query & Rest
   */
  const queryRestColumns: FormTableColumn[] = [
    {
      title: 'apiTestManagement.paramName',
      dataIndex: 'key',
      inputType: 'text',
    },
    {
      title: 'apiTestDebug.paramType',
      dataIndex: 'paramType',
      inputType: 'text',
    },
    {
      title: 'apiTestManagement.paramVal',
      dataIndex: 'value',
      inputType: 'text',
    },
    {
      title: 'apiTestManagement.required',
      dataIndex: 'required',
      slotName: 'required',
      inputType: 'text',
      valueFormat: (record) => {
        return record.required ? t('common.yes') : t('common.no');
      },
    },
    {
      title: 'apiTestDebug.paramLengthRange',
      dataIndex: 'lengthRange',
      slotName: 'lengthRange',
      inputType: 'text',
      valueFormat: (record) => {
        return [null, undefined].includes(record.minLength) && [null, undefined].includes(record.maxLength)
          ? '-'
          : `${record.minLength} ${t('common.to')} ${record.maxLength}`;
      },
    },
    {
      title: 'apiTestDebug.encode',
      dataIndex: 'encode',
      slotName: 'encode',
      inputType: 'text',
      valueFormat: (record) => {
        return record.encode ? t('common.yes') : t('common.no');
      },
    },
    {
      title: 'common.desc',
      dataIndex: 'description',
      inputType: 'text',
      showTooltip: true,
    },
  ];
  const queryShowType = ref('table');
  const queryRawCode = computed(() => {
    return preivewDetail.value.query?.map((item) => `${item.key}:${item.value}`).join('\n');
  });
  const restShowType = ref('table');
  const restRawCode = computed(() => {
    return preivewDetail.value.rest?.map((item) => `${item.key}:${item.value}`).join('\n');
  });

  /**
   * 请求体
   */
  const bodyColumns: FormTableColumn[] = [
    {
      title: 'apiTestManagement.paramName',
      dataIndex: 'key',
      inputType: 'text',
    },
    {
      title: 'apiTestManagement.paramsType',
      dataIndex: 'paramType',
      inputType: 'text',
    },
    {
      title: 'apiTestManagement.paramVal',
      dataIndex: 'value',
      inputType: 'text',
      showTooltip: true,
    },
    {
      title: 'apiTestManagement.required',
      dataIndex: 'required',
      slotName: 'required',
      inputType: 'text',
      valueFormat: (record) => {
        return record.required ? t('common.yes') : t('common.no');
      },
    },
    {
      title: 'apiTestDebug.paramLengthRange',
      dataIndex: 'lengthRange',
      slotName: 'lengthRange',
      inputType: 'text',
      valueFormat: (record) => {
        return [null, undefined].includes(record.minLength) && [null, undefined].includes(record.maxLength)
          ? '-'
          : `${record.minLength} ${t('common.to')} ${record.maxLength}`;
      },
    },
    {
      title: 'apiTestDebug.encode',
      dataIndex: 'encode',
      slotName: 'encode',
      inputType: 'text',
      valueFormat: (record) => {
        return record.encode ? t('common.yes') : t('common.no');
      },
    },
    {
      title: 'common.desc',
      dataIndex: 'description',
      inputType: 'text',
      showTooltip: true,
      width: 100,
    },
  ];
  // const bodyShowType = ref('table');
  const bodyTableData = computed(() => {
    switch (preivewDetail.value.body.bodyType) {
      case RequestBodyFormat.FORM_DATA:
        return (preivewDetail.value.body.formDataBody?.formValues || []).map((e) => ({
          ...e,
          value: e.paramType === RequestParamsType.FILE ? e.files?.map((file) => file.fileName).join('\n') : e.value,
        }));
      case RequestBodyFormat.WWW_FORM:
        return preivewDetail.value.body.wwwFormBody?.formValues || [];
      default:
        return [];
    }
  });
  const bodyCode = computed(() => {
    switch (preivewDetail.value.body.bodyType) {
      case RequestBodyFormat.FORM_DATA:
        return preivewDetail.value.body.formDataBody?.formValues?.map((item) => `${item.key}:${item.value}`).join('\n');
      case RequestBodyFormat.WWW_FORM:
        return preivewDetail.value.body.wwwFormBody?.formValues?.map((item) => `${item.key}:${item.value}`).join('\n');
      case RequestBodyFormat.RAW:
        return preivewDetail.value.body.rawBody?.value;
      case RequestBodyFormat.JSON:
        return preivewDetail.value.body.jsonBody?.jsonValue;
      case RequestBodyFormat.XML:
        return preivewDetail.value.body.xmlBody?.value;
      default:
        return '';
    }
  });
  const bodyCodeLanguage = computed(() => {
    if (preivewDetail.value.body.bodyType === RequestBodyFormat.JSON) {
      return LanguageEnum.JSON;
    }
    if (preivewDetail.value.body.bodyType === RequestBodyFormat.XML) {
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
</script>

<style lang="less" scoped>
  .down-icon {
    padding: 4px;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    color: rgb(var(--primary-5));
    background-color: rgb(var(--primary-1));
  }
  .arco-collapse {
    @apply h-full overflow-y-auto;
    .ms-scroll-bar();
  }
  .detail-collapse-item {
    @apply overflow-y-auto;

    margin-bottom: 16px;
    .ms-scroll-bar();
  }
  .detail-item {
    padding-top: 16px;
    .detail-item-title {
      @apply flex items-center;

      margin-bottom: 8px;
      gap: 16px;
      .detail-item-title-text {
        @apply font-medium;

        color: var(--color-text-1);
      }
    }
  }
  :deep(.arco-collapse) {
    border-radius: 0;
    .arco-collapse-item-icon-hover {
      @apply !hidden;
    }
    .arco-collapse-item-header {
      .arco-collapse-item-header-title {
        @apply block w-full;

        padding: 8px 16px;
        border-radius: var(--border-radius-small);
        background-color: var(--color-text-n9);
      }
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
</style>
