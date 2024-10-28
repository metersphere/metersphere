<template>
  <div class="mt-[8px] w-full">
    <MsEditableTab
      v-model:active-tab="activeResponse"
      v-model:tabs="responseTabs"
      at-least-one
      hide-more-action
      @add="addResponseTab"
    >
      <template #label="{ tab }">
        <div class="response-tab">
          <div v-if="tab.defaultFlag" class="response-tab-default-icon"></div>
          <a-tooltip
            v-if="translateTextToPX(t(tab.name || tab.label)) > 200"
            :content="t(tab.name || tab.label) + '(' + tab.statusCode + ')'"
          >
            <span class="one-line-text" style="max-width: 200px"
              >{{ t(tab.name || tab.label) }}({{ tab.statusCode }})</span
            >
          </a-tooltip>
          <span v-else>{{ t(tab.name || tab.label) }}({{ tab.statusCode }})</span>
          <MsMoreAction
            :list="
              tab.defaultFlag
                ? tabMoreActionList.filter((e) => e.eventTag !== 'setDefault' && e.eventTag !== 'delete')
                : tabMoreActionList
            "
            class="response-more-action"
            @select="(e) => handleMoreActionSelect(e, tab as ResponseItem)"
          />
          <popConfirm
            v-model:visible="tab.showRenamePopConfirm"
            mode="tabRename"
            :field-config="{ field: t(tab.label || tab.name) }"
            :all-names="responseTabs.filter((e) => e.id !== tab.id).map((e) => t(e.label || e.name))"
            :popup-offset="20"
            :repeat-message="t('apiTestDebug.responseRepeatMessage')"
            @rename-finish="
              (val) => {
                tab.label = val;
                tab.name = val;
                emit('change');
              }
            "
          >
            <span :id="`renameSpan${tab.id}`" class="relative"></span>
          </popConfirm>
          <a-popconfirm
            v-model:popup-visible="tab.showPopConfirm"
            position="bottom"
            content-class="w-[300px]"
            :ok-text="t('common.confirm')"
            :popup-offset="20"
            @ok="() => handleDeleteResponseTab(tab.id)"
          >
            <template #icon>
              <icon-exclamation-circle-fill class="!text-[rgb(var(--danger-6))]" />
            </template>
            <template #content>
              <a-tooltip v-if="translateTextToPX(t(tab.name || tab.label)) > 200" :content="t(tab.name || tab.label)">
                <div class="one-line-text max-w-[200px] font-semibold text-[var(--color-text-1)]">
                  {{ t('apiTestManagement.confirmDelete', { name: tab.label || tab.name }) }}
                </div>
              </a-tooltip>
              <div v-else class="font-semibold text-[var(--color-text-1)]">
                {{ t('apiTestManagement.confirmDelete', { name: tab.label || tab.name }) }}
              </div>
            </template>
            <div class="relative"></div>
          </a-popconfirm>
        </div>
      </template>
    </MsEditableTab>
  </div>
  <a-tabs
    v-model:active-key="activeResponse.responseActiveTab"
    class="no-content border-b border-[var(--color-text-n8)]"
  >
    <a-tab-pane v-for="item of responseCompositionTabList" :key="item.value" :title="item.label" />
  </a-tabs>
  <div class="response-container">
    <template v-if="activeResponse.responseActiveTab === ResponseComposition.BODY">
      <div class="mb-[8px] flex items-center justify-between">
        <a-radio-group
          v-model:model-value="activeResponse.body.bodyType"
          type="button"
          size="small"
          @change="(val) => changeBodyFormat(val as ResponseBodyFormat)"
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
        <div
          v-if="activeResponse.body.bodyType === ResponseBodyFormat.JSON"
          class="ml-auto flex items-center gap-[8px]"
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
          <a-button
            v-show="activeResponse.body.jsonBody.enableJsonSchema"
            type="outline"
            class="arco-btn-outline--secondary ml-[16px] px-[8px]"
            size="small"
            @click="previewJsonSchema"
          >
            <div class="flex items-center gap-[8px]">
              <icon-eye />
              {{ t('common.preview') }}
            </div>
          </a-button>
        </div>
      </div>
      <div
        v-if="
          [ResponseBodyFormat.JSON, ResponseBodyFormat.XML, ResponseBodyFormat.RAW].includes(
            activeResponse.body.bodyType
          )
        "
      >
        <MsJsonSchema
          v-if="
            activeResponse.body.jsonBody.enableJsonSchema && activeResponse.body.bodyType === ResponseBodyFormat.JSON
          "
          ref="jsonSchemaRef"
          v-model:data="activeResponse.body.jsonBody.jsonSchemaTableData"
          v-model:selectedKeys="selectedKeys"
        />
        <MsCodeEditor
          v-else
          ref="responseEditorRef"
          v-model:model-value="currentBodyCode"
          :language="currentCodeLanguage"
          theme="vs"
          :show-full-screen="false"
          :show-theme-change="false"
          :show-language-change="false"
          :show-charset-change="false"
          show-code-format
        >
          <template #leftTitle>
            <a-button
              v-if="activeResponse.body.bodyType === ResponseBodyFormat.JSON"
              type="text"
              class="arco-btn-text--primary gap-[4px] p-[2px_6px]"
              size="small"
              @click="autoMakeJson"
            >
              <MsIcon :size="14" type="icon-icon_press" />
              <div class="text-[12px]">{{ t('apiTestManagement.autoMake') }}</div>
            </a-button>
          </template>
        </MsCodeEditor>
      </div>
      <div v-else>
        <div class="mb-[16px] flex justify-between gap-[8px] bg-[var(--color-text-n9)] p-[12px]">
          <a-input
            v-model:model-value="activeResponse.body.binaryBody.description"
            :placeholder="t('common.desc')"
            :max-length="255"
          />
          <MsAddAttachment
            v-model:file-list="fileList"
            mode="input"
            :multiple="false"
            :fields="{
              id: 'fileId',
              name: 'fileName',
            }"
            @change="handleFileChange"
          />
        </div>
        <div class="flex items-center">
          <a-switch
            v-model:model-value="activeResponse.body.binaryBody.sendAsBody"
            class="mr-[8px]"
            size="small"
            type="line"
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
      v-else-if="activeResponse.responseActiveTab === ResponseComposition.HEADER"
      :params="activeResponse.headers"
      :columns="columns"
      :default-param-item="defaultKeyValueParamItem"
      :selectable="false"
      @change="handleResponseTableChange"
    />
    <a-select
      v-else
      v-model:model-value="activeResponse.statusCode"
      :options="statusCodeOptions"
      class="w-[200px]"
      @change="handleStatusCodeChange"
    />
  </div>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsEditableTab from '@/components/pure/ms-editable-tab/index.vue';
  import { TabItem } from '@/components/pure/ms-editable-tab/types';
  import MsJsonSchema from '@/components/pure/ms-json-schema/index.vue';
  import { parseSchemaToJsonSchemaTableData, parseTableDataToJsonSchema } from '@/components/pure/ms-json-schema/utils';
  import MsMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import { MsFileItem } from '@/components/pure/ms-upload/types';
  import MsAddAttachment from '@/components/business/ms-add-attachment/index.vue';
  import paramTable, { ParamTableColumn } from '@/views/api-test/components/paramTable.vue';
  import popConfirm from '@/views/api-test/components/popConfirm.vue';

  import { jsonSchemaAutoGenerate } from '@/api/modules/api-test/management';
  import { responseHeaderOption } from '@/config/apiTest';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { translateTextToPX } from '@/utils/css';

  import { ResponseDefinition } from '@/models/apiTest/common';
  import { ResponseBodyFormat, ResponseComposition } from '@/enums/apiEnum';

  import { defaultKeyValueParamItem, defaultResponseItem, statusCodes } from '../../config';

  const props = defineProps<{
    uploadTempFileApi?: (file: File) => Promise<any>; // 上传临时文件接口
  }>();
  const emit = defineEmits<{
    (e: 'change'): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  export interface ResponseItem extends TabItem, ResponseDefinition {
    showPopConfirm?: boolean; // 是否显示确认弹窗
    showRenamePopConfirm?: boolean; // 是否显示重命名确认弹窗
    responseActiveTab?: ResponseComposition; // 当前激活的tab
  }
  const responseTabs = defineModel<ResponseItem[]>('responseDefinition', {
    required: true,
  });
  const activeResponse = ref<ResponseItem>(responseTabs.value[0] || cloneDeep(defaultResponseItem));

  watch(
    () => responseTabs.value,
    (arr) => {
      if (arr.length > 0) {
        [activeResponse.value] = arr;
      }
    }
  );

  function addResponseTab(defaultProps?: Partial<ResponseItem>) {
    const id = new Date().getTime();
    responseTabs.value.push({
      ...cloneDeep(defaultResponseItem),
      label: t('apiTestManagement.response', { count: responseTabs.value.length + 1 }),
      name: t('apiTestManagement.response', { count: responseTabs.value.length + 1 }),
      ...defaultProps,
      id,
      defaultFlag: false,
      showPopConfirm: false,
      showRenamePopConfirm: false,
    });
    activeResponse.value = responseTabs.value[responseTabs.value.length - 1];
    emit('change');
  }

  const tabMoreActionList: ActionsItem[] = [
    {
      label: t('apiTestManagement.setDefault'),
      eventTag: 'setDefault',
    },
    {
      label: t('common.rename'),
      eventTag: 'rename',
    },
    {
      label: t('common.copy'),
      eventTag: 'copy',
    },
    {
      isDivider: true,
    },
    {
      label: t('common.delete'),
      eventTag: 'delete',
      danger: true,
    },
  ];
  const renameValue = ref('');

  function handleMoreActionSelect(e: ActionsItem, _tab: ResponseItem) {
    switch (e.eventTag) {
      case 'setDefault':
        _tab.defaultFlag = true;
        responseTabs.value = [
          _tab,
          ...responseTabs.value.filter((tab) => {
            if (tab.id !== _tab.id) {
              tab.defaultFlag = false;
            }
            return tab.id !== _tab.id;
          }),
        ];
        break;
      case 'rename':
        renameValue.value = _tab.label || _tab.name || '';
        document.querySelector(`#renameSpan${_tab.id}`)?.dispatchEvent(new Event('click'));
        break;
      case 'copy':
        addResponseTab({
          ...cloneDeep(_tab),
          id: new Date().getTime(),
          label: `copy_${t(_tab.label || _tab.name)}`,
          name: `copy_${t(_tab.label || _tab.name)}`,
        });
        break;
      case 'delete':
        _tab.showPopConfirm = true;
        break;
      default:
        break;
    }
  }

  function handleDeleteResponseTab(id: number | string) {
    responseTabs.value = responseTabs.value.filter((tab) => tab.id !== id);
    if (id === activeResponse.value.id) {
      [activeResponse.value] = responseTabs.value;
    }
    emit('change');
  }

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
  ];

  function changeBodyFormat(val: ResponseBodyFormat) {
    activeResponse.value.body.bodyType = val;
    emit('change');
  }

  const jsonSchemaRef = ref<InstanceType<typeof MsJsonSchema>>();
  const bodyLoading = ref(false);
  const selectedKeys = ref<string[]>([]);

  watchEffect(() => {
    if (
      activeResponse.value.body.jsonBody.jsonSchema &&
      (!activeResponse.value.body.jsonBody.jsonSchemaTableData ||
        activeResponse.value.body.jsonBody.jsonSchemaTableData.length === 0)
    ) {
      const { result, ids } = parseSchemaToJsonSchemaTableData(activeResponse.value.body.jsonBody.jsonSchema);
      activeResponse.value.body.jsonBody.jsonSchemaTableData = result;
      selectedKeys.value = ids;
    } else if (
      !activeResponse.value.body.jsonBody.jsonSchemaTableData ||
      activeResponse.value.body.jsonBody.jsonSchemaTableData.length === 0
    ) {
      activeResponse.value.body.jsonBody.jsonSchemaTableData = [];
      selectedKeys.value = [];
    }
  });

  function previewJsonSchema() {
    if (activeResponse.value.body.jsonBody.enableJsonSchema) {
      jsonSchemaRef.value?.previewSchema();
    }
  }

  /**
   * 自动转换json schema为json
   */
  async function autoMakeJson() {
    if (!activeResponse.value.body.jsonBody.enableJsonSchema) {
      try {
        bodyLoading.value = true;
        let schema = activeResponse.value.body.jsonBody.jsonSchema;
        if (activeResponse.value.body.jsonBody.jsonSchemaTableData) {
          // 若jsonSchema不存在，先将表格数据转换为 json schema格式
          schema = parseTableDataToJsonSchema(activeResponse.value.body.jsonBody.jsonSchemaTableData[0]);
        }
        if (schema) {
          // 再将 json schema 转换为 json 格式
          const res = await jsonSchemaAutoGenerate(schema);
          activeResponse.value.body.jsonBody.jsonValue = res;
        } else {
          Message.warning(t('apiTestManagement.pleaseInputJsonSchema'));
        }
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      } finally {
        bodyLoading.value = false;
      }
    }
  }

  // 当前显示的代码
  const currentBodyCode = computed({
    get() {
      if (activeResponse.value.body.bodyType === ResponseBodyFormat.JSON) {
        return activeResponse.value.body.jsonBody.jsonValue;
      }
      if (activeResponse.value.body.bodyType === ResponseBodyFormat.XML) {
        return activeResponse.value.body.xmlBody.value;
      }
      return activeResponse.value.body.rawBody.value;
    },
    set(val) {
      if (activeResponse.value.body.bodyType === ResponseBodyFormat.JSON) {
        activeResponse.value.body.jsonBody.jsonValue = val;
      } else if (activeResponse.value.body.bodyType === ResponseBodyFormat.XML) {
        activeResponse.value.body.xmlBody.value = val;
      } else {
        activeResponse.value.body.rawBody.value = val;
      }
    },
  });
  // 当前代码编辑器的语言
  const currentCodeLanguage = computed(() => {
    if (activeResponse.value.body.bodyType === ResponseBodyFormat.JSON) {
      return LanguageEnum.JSON;
    }
    if (activeResponse.value.body.bodyType === ResponseBodyFormat.XML) {
      return LanguageEnum.XML;
    }
    return LanguageEnum.PLAINTEXT;
  });

  const fileList = ref<MsFileItem[]>([]);
  onBeforeMount(() => {
    if (activeResponse.value.body.binaryBody && activeResponse.value.body.binaryBody.file) {
      fileList.value = [activeResponse.value.body.binaryBody.file as unknown as MsFileItem];
    } else {
      fileList.value = [];
    }
  });

  watch(
    () => activeResponse.value.id,
    () => {
      if (activeResponse.value.body.binaryBody && activeResponse.value.body.binaryBody.file) {
        fileList.value = [activeResponse.value.body.binaryBody.file as unknown as MsFileItem];
      } else {
        fileList.value = [];
      }
    }
  );

  async function handleFileChange() {
    try {
      if (fileList.value[0] && fileList.value[0].local && fileList.value[0].file && props.uploadTempFileApi) {
        appStore.showLoading();
        const res = await props.uploadTempFileApi(fileList.value[0].file);
        activeResponse.value.body.binaryBody.file = {
          ...fileList.value[0],
          fileId: res.data,
          fileName: fileList.value[0]?.name || '',
          fileAlias: fileList.value[0]?.name || '',
          local: true,
        };
        appStore.hideLoading();
      } else if (fileList.value[0]) {
        activeResponse.value.body.binaryBody.file = {
          ...fileList.value[0],
          fileId: fileList.value[0].uid,
          fileName: fileList.value[0]?.name || '',
          fileAlias: fileList.value[0]?.name || '',
          local: false,
        };
      } else {
        activeResponse.value.body.binaryBody.file = undefined;
      }
      emit('change');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

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

  const statusCodeOptions = statusCodes.map((e) => ({
    label: e.toString(),
    value: e,
  }));

  function handleResponseTableChange(arr: any[]) {
    activeResponse.value.headers = [...arr];
    emit('change');
  }

  function handleStatusCodeChange() {
    emit('change');
  }

  function handleChangeJsonType(type: 'Schema' | 'Json') {
    activeResponse.value.body.jsonBody.enableJsonSchema = type === 'Schema';
    if (activeResponse.value.body.jsonBody.jsonValue === '') {
      autoMakeJson();
    }
  }
</script>

<style lang="less" scoped>
  .response-container {
    margin-top: 8px;
  }
  :deep(.arco-table-th) {
    background-color: var(--color-text-n9);
  }
  :deep(.arco-tabs-tab) {
    @apply leading-none;
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
    :deep(.response-more-action) {
      margin-left: 4px;
      .more-icon-btn {
        @apply invisible;
      }
    }
    &:hover {
      :deep(.response-more-action) {
        .more-icon-btn {
          @apply visible;
        }
      }
    }
  }
</style>
