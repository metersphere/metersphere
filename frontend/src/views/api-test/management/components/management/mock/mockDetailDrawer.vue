<template>
  <MsDrawer
    v-model:visible="visible"
    :title="title"
    :width="960"
    :footer="!isReadOnly || isEdit"
    :ok-text="isEdit ? t('common.save') : t('common.create')"
    :save-continue-text="t('mockManagement.saveAndContinue')"
    :show-continue="!isEdit"
    :ok-loading="loading"
    no-content-padding
    unmount-on-close
    @confirm="handleSave"
    @cancel="handleCancel"
    @close="handleCancel"
  >
    <template #tbutton>
      <div v-if="mockDetail.id" class="right-operation-button-icon flex items-center gap-[4px]">
        <MsButton
          v-permission="['PROJECT_API_DEFINITION_MOCK:READ+UPDATE']"
          type="icon"
          status="secondary"
          @click="isEdit = true"
        >
          <MsIcon type="icon-icon_edit_outlined" />
          {{ t('common.edit') }}
        </MsButton>
        <MsButton
          v-permission="['PROJECT_API_DEFINITION_MOCK:READ+DELETE']"
          class="mr-0"
          type="icon"
          status="secondary"
          @click="handleDelete"
        >
          <MsIcon type="icon-icon_delete-trash_outlined" />
          {{ t('common.delete') }}
        </MsButton>
      </div>
    </template>
    <a-spin :loading="loading" class="block p-[16px]">
      <MsDetailCard
        :title="`【${props.definitionDetail.num}】${props.definitionDetail.name}`"
        :description="[]"
        class="mb-[16px]"
      >
        <template #titleRight>
          <div class="flex items-center gap-[16px]">
            <div class="flex items-center gap-[8px]">
              <div class="whitespace-nowrap text-[var(--color-text-4)]">{{ t('apiTestManagement.apiType') }}</div>
              <apiMethodName :method="props.definitionDetail.method" tag-size="small" is-tag />
            </div>
            <div class="flex items-center gap-[8px]">
              <div class="whitespace-nowrap text-[var(--color-text-4)]">{{ t('apiTestManagement.path') }}</div>
              <a-tooltip :content="props.definitionDetail.url">
                <div class="one-line-text">{{ props.definitionDetail.url }}</div>
              </a-tooltip>
            </div>
          </div>
        </template>
      </MsDetailCard>
      <a-form ref="mockForm" :model="mockDetail" :disabled="isReadOnly">
        <a-form-item
          class="hidden-item"
          field="name"
          :rules="[{ required: true, message: t('mockManagement.nameNotNull') }]"
        >
          <a-input
            v-model:model-value="mockDetail.name"
            :placeholder="t('mockManagement.namePlaceholder')"
            class="mb-[16px] w-[732px]"
          ></a-input>
        </a-form-item>
        <a-form-item class="hidden-item" :rules="[{ required: true, message: t('mockManagement.nameNotNull') }]">
          <MsTagsInput
            v-model:model-value="mockDetail.tags"
            class="mb-[16px] w-[732px]"
            allow-clear
            unique-value
            retain-input-value
            :max-tag-count="5"
          />
        </a-form-item>
      </a-form>
      <div class="font-medium">{{ t('mockManagement.matchRule') }}</div>
      <MsTab
        v-model:active-key="activeTab"
        :content-tab-list="mockTabList"
        :get-text-func="getTabBadge"
        class="no-content relative my-[8px] border-b"
      />
      <mockMatchRuleForm
        v-if="
          activeTab === RequestComposition.HEADER ||
          activeTab === RequestComposition.QUERY ||
          activeTab === RequestComposition.REST
        "
        :id="mockDetail.id"
        v-model:matchAll="currentMatchAll"
        v-model:matchRules="currentMatchRules"
        :key-options="currentKeyOptions"
        :disabled="isReadOnly"
      />
      <template v-else>
        <div class="mb-[8px] flex items-center justify-between">
          <a-radio-group
            v-model:model-value="mockDetail.mockMatchRule.body.paramType"
            type="button"
            size="small"
            :disabled="isReadOnly"
            @change="handleMockBodyTypeChange"
          >
            <a-radio v-for="item of RequestBodyFormat" :key="item" :value="item">
              {{ requestBodyTypeMap[item] }}
            </a-radio>
          </a-radio-group>
        </div>
        <div
          v-if="mockDetail.mockMatchRule.body.paramType === RequestBodyFormat.NONE"
          class="flex h-[100px] items-center justify-center rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] text-[var(--color-text-4)]"
        >
          {{ t('apiTestDebug.noneBody') }}
        </div>
        <mockMatchRuleForm
          v-else-if="
            [RequestBodyFormat.FORM_DATA, RequestBodyFormat.WWW_FORM].includes(mockDetail.mockMatchRule.body.paramType)
          "
          :id="mockDetail.id"
          v-model:matchAll="mockDetail.mockMatchRule.body.formDataMatch.matchAll"
          v-model:matchRules="mockDetail.mockMatchRule.body.formDataMatch.matchRules"
          :key-options="currentBodyKeyOptions"
          :disabled="isReadOnly"
        />
        <div v-else-if="mockDetail.mockMatchRule.body.paramType === RequestBodyFormat.BINARY">
          <div class="mb-[16px] flex justify-between gap-[8px] bg-[var(--color-text-n9)] p-[12px]">
            <MsAddAttachment
              v-model:file-list="fileList"
              mode="input"
              :multiple="false"
              :fields="{
                id: 'fileId',
                name: 'fileName',
              }"
              :file-save-as-source-id="mockDetail.id"
              :file-save-as-api="transferMockFile"
              :file-module-options-api="getMockTransferOptions"
              :disabled="isReadOnly"
              @change="handleFileChange"
            />
          </div>
          <!-- <div class="flex items-center">
      <a-switch v-model:model-value="innerParams.binarySend" class="mr-[8px]" size="small" type="line"></a-switch>
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
    </div> -->
        </div>
        <div v-else class="flex h-[300px]">
          <MsCodeEditor
            v-model:model-value="mockDetail.mockMatchRule.body.raw"
            class="flex-1"
            theme="vs"
            height="100%"
            :show-full-screen="false"
            :show-theme-change="false"
            :show-code-format="true"
            :language="currentCodeLanguage"
            :read-only="isReadOnly"
          >
          </MsCodeEditor>
        </div>
      </template>
      <mockResponse
        v-model:mock-response="mockDetail.response"
        :definition-responses="props.definitionDetail.responseDefinition || []"
        :disabled="isReadOnly"
      />
    </a-spin>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsDetailCard from '@/components/pure/ms-detail-card/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTab from '@/components/pure/ms-tab/index.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import { MsFileItem } from '@/components/pure/ms-upload/types';
  import MsAddAttachment from '@/components/business/ms-add-attachment/index.vue';
  import mockMatchRuleForm from './mockMatchRuleForm.vue';
  import mockResponse from './mockResponse.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  import {
    addMock,
    getMockDetail,
    getMockTransferOptions,
    transferMockFile,
    updateMock,
    uploadMockTempFile,
  } from '@/api/modules/api-test/management';
  import { requestBodyTypeMap } from '@/config/apiTest';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { MockParams } from '@/models/apiTest/mock';
  import { RequestBodyFormat, RequestComposition } from '@/enums/apiEnum';

  import {
    defaultHeaderParamsItem,
    defaultMatchRuleItem,
    defaultRequestParamsItem,
    makeDefaultParams,
  } from '@/views/api-test/components/config';
  import { filterKeyValParams, parseRequestBodyFiles } from '@/views/api-test/components/utils';

  const props = defineProps<{
    definitionDetail: RequestParam;
    detailId?: string;
    isCopy?: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'delete'): void;
    (e: 'addDone'): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', {
    required: true,
  });

  const loading = ref(false);
  const isEdit = ref(false);
  const mockDetail = ref<MockParams>(makeDefaultParams());
  const isReadOnly = computed(() => !mockDetail.value.isNew && !isEdit.value);
  const title = computed(() => {
    if (isReadOnly.value) {
      return t('mockManagement.mockDetail');
    }
    if (isEdit.value) {
      return t('mockManagement.updateMock');
    }
    return t('mockManagement.createMock');
  });
  const activeTab = ref<RequestComposition>(RequestComposition.BODY);
  const mockTabList = [
    {
      value: RequestComposition.BODY,
      label: t('apiTestDebug.body'),
    },
    {
      value: RequestComposition.HEADER,
      label: t('apiTestDebug.header'),
    },
    {
      value: RequestComposition.QUERY,
      label: 'Query',
    },
    {
      value: RequestComposition.REST,
      label: RequestComposition.REST,
    },
  ];

  /**
   * 获取 tab 的参数数量徽标
   */
  function getTabBadge(tabKey: RequestComposition) {
    switch (tabKey) {
      case RequestComposition.HEADER:
        const headerNum = filterKeyValParams(mockDetail.value.mockMatchRule.header.matchRules, defaultHeaderParamsItem)
          .validParams.length;
        return `${headerNum > 0 ? headerNum : ''}`;
      case RequestComposition.BODY:
        return mockDetail.value.mockMatchRule.body.paramType !== RequestBodyFormat.NONE ? '1' : '';
      case RequestComposition.QUERY:
        const queryNum = filterKeyValParams(mockDetail.value.mockMatchRule.query.matchRules, defaultRequestParamsItem)
          .validParams.length;
        return `${queryNum > 0 ? queryNum : ''}`;
      case RequestComposition.REST:
        const restNum = filterKeyValParams(mockDetail.value.mockMatchRule.rest.matchRules, defaultRequestParamsItem)
          .validParams.length;
        return `${restNum > 0 ? restNum : ''}`;
      default:
        return '';
    }
  }

  function handleMockBodyTypeChange() {
    mockDetail.value.unSaved = true;
  }

  const currentMatchAll = computed({
    get() {
      switch (activeTab.value) {
        case RequestComposition.HEADER:
          return mockDetail.value.mockMatchRule.header.matchAll;
        case RequestComposition.QUERY:
          return mockDetail.value.mockMatchRule.query.matchAll;
        case RequestComposition.REST:
          return mockDetail.value.mockMatchRule.rest.matchAll;
        default:
          return false;
      }
    },
    set(val) {
      switch (activeTab.value) {
        case RequestComposition.HEADER:
          mockDetail.value.mockMatchRule.header.matchAll = val;
          break;
        case RequestComposition.QUERY:
          mockDetail.value.mockMatchRule.query.matchAll = val;
          break;
        case RequestComposition.REST:
          mockDetail.value.mockMatchRule.rest.matchAll = val;
          break;
        default:
          break;
      }
    },
  });
  const currentMatchRules = computed({
    get() {
      switch (activeTab.value) {
        case RequestComposition.HEADER:
          return mockDetail.value.mockMatchRule.header.matchRules;
        case RequestComposition.QUERY:
          return mockDetail.value.mockMatchRule.query.matchRules;
        case RequestComposition.REST:
          return mockDetail.value.mockMatchRule.rest.matchRules;
        default:
          return [];
      }
    },
    set(val) {
      switch (activeTab.value) {
        case RequestComposition.HEADER:
          mockDetail.value.mockMatchRule.header.matchRules = val;
          break;
        case RequestComposition.QUERY:
          mockDetail.value.mockMatchRule.query.matchRules = val;
          break;
        case RequestComposition.REST:
          mockDetail.value.mockMatchRule.rest.matchRules = val;
          break;
        default:
          break;
      }
    },
  });
  const currentKeyOptions = computed(() => {
    switch (activeTab.value) {
      case RequestComposition.HEADER:
        return filterKeyValParams(props.definitionDetail.headers, defaultMatchRuleItem).validParams.filter((e) => ({
          label: e.key,
          value: e.value,
        }));
      case RequestComposition.QUERY:
        return filterKeyValParams(props.definitionDetail.query, defaultMatchRuleItem).validParams.filter((e) => ({
          label: e.key,
          value: e.value,
        }));
      case RequestComposition.REST:
        return filterKeyValParams(props.definitionDetail.rest, defaultMatchRuleItem).validParams.filter((e) => ({
          label: e.key,
          value: e.value,
        }));
      default:
        return [];
    }
  });
  const currentBodyKeyOptions = computed(() => {
    switch (mockDetail.value.mockMatchRule.body.paramType) {
      case RequestBodyFormat.FORM_DATA:
        return filterKeyValParams(
          props.definitionDetail.body.formDataBody.formValues,
          defaultMatchRuleItem
        ).validParams.map((e) => ({
          label: e.key,
          value: e.value,
          paramType: e.paramType,
        }));
      case RequestBodyFormat.WWW_FORM:
        return filterKeyValParams(
          props.definitionDetail.body.wwwFormBody.formValues,
          defaultMatchRuleItem
        ).validParams.filter((e) => ({
          label: e.key,
          value: e.value,
        }));
      default:
        return [];
    }
  });
  // 当前代码编辑器的语言
  const currentCodeLanguage = computed(() => {
    if (mockDetail.value.mockMatchRule.body.paramType === RequestBodyFormat.JSON) {
      return LanguageEnum.JSON;
    }
    if (mockDetail.value.mockMatchRule.body.paramType === RequestBodyFormat.XML) {
      return LanguageEnum.XML;
    }
    return LanguageEnum.PLAINTEXT;
  });

  async function initMockDetail() {
    try {
      loading.value = true;
      const res = await getMockDetail({
        id: props.detailId || '',
        projectId: appStore.currentProjectId,
      });
      const parseFileResult = parseRequestBodyFiles(res.matching.body);
      const formDataMatch =
        res.matching.body.paramType === RequestBodyFormat.FORM_DATA
          ? res.matching.body.formDataMatch.matchRules.map((item) => {
              const newParamType =
                currentBodyKeyOptions.value.find((e) => e.value === item.key)?.paramType ||
                defaultMatchRuleItem.paramType;
              item.paramType = newParamType;
              item.files = item.files || [];
              return item;
            })
          : res.matching.body.formDataMatch.matchRules;
      mockDetail.value = {
        ...res,
        id: props.isCopy ? '' : res.id,
        isNew: props.isCopy,
        mockMatchRule: {
          ...res.matching,
          body: {
            ...res.matching.body,
            formDataMatch: {
              ...res.matching.body.formDataMatch,
              matchRules: formDataMatch,
            },
          },
        },
        ...parseFileResult,
      };
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  watch(
    () => visible.value,
    (val) => {
      if (val && props.detailId) {
        initMockDetail();
      }
    },
    {
      immediate: true,
    }
  );

  const fileList = ref<MsFileItem[]>([]);

  async function handleFileChange(files: MsFileItem[], file?: MsFileItem) {
    try {
      if (file?.local && file.file) {
        // 本地上传
        loading.value = true;
        const res = await uploadMockTempFile(file.file);
        mockDetail.value.mockMatchRule.body.binaryBody.file = {
          ...file,
          fileId: res.data,
          fileName: file?.name || '',
          fileAlias: file?.name || '',
          local: true,
        };
        loading.value = false;
      } else {
        // 关联文件
        mockDetail.value.mockMatchRule.body.binaryBody.file = {
          ...fileList.value[0],
          fileId: fileList.value[0]?.uid,
          fileName: fileList.value[0]?.originalName || '',
          fileAlias: fileList.value[0]?.name || '',
          local: false,
        };
      }
      if (
        mockDetail.value.mockMatchRule.body.binaryBody.file &&
        !mockDetail.value.mockMatchRule.body.binaryBody.file.fileId
      ) {
        mockDetail.value.mockMatchRule.body.binaryBody.file = undefined;
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      loading.value = false;
    }
  }

  watch(
    () => mockDetail.value.mockMatchRule.body.paramType,
    (val) => {
      if (val === RequestBodyFormat.JSON) {
        mockDetail.value.mockMatchRule.body.raw = props.definitionDetail.body.jsonBody.jsonValue;
      } else if (val === RequestBodyFormat.XML) {
        mockDetail.value.mockMatchRule.body.raw = props.definitionDetail.body.xmlBody.value || '';
      } else if (val === RequestBodyFormat.RAW) {
        mockDetail.value.mockMatchRule.body.raw = props.definitionDetail.body.rawBody.value || '';
      }
    }
  );

  function handleCancel() {
    mockDetail.value = makeDefaultParams();
    isEdit.value = false;
    visible.value = false;
  }

  function handleDelete() {
    emit('delete');
    handleCancel();
  }

  async function handleSave() {
    try {
      loading.value = true;
      const { body } = mockDetail.value.mockMatchRule;
      const validBodyMatchRules = filterKeyValParams(body.formDataMatch.matchRules, defaultMatchRuleItem).validParams;
      const validHeaderMatchRules = filterKeyValParams(
        mockDetail.value.mockMatchRule.header.matchRules,
        defaultMatchRuleItem
      ).validParams;
      const validQueryMatchRules = filterKeyValParams(
        mockDetail.value.mockMatchRule.query.matchRules,
        defaultMatchRuleItem
      ).validParams;
      const validRestMatchRules = filterKeyValParams(
        mockDetail.value.mockMatchRule.rest.matchRules,
        defaultMatchRuleItem
      ).validParams;
      const validResponseHeaders = filterKeyValParams(
        mockDetail.value.response.headers,
        defaultHeaderParamsItem
      ).validParams;
      const parseFileResult = parseRequestBodyFiles(mockDetail.value.mockMatchRule.body);
      const params = {
        ...mockDetail.value,
        statusCode: mockDetail.value.response.statusCode,
        mockMatchRule: {
          ...mockDetail.value.mockMatchRule,
          body: {
            ...mockDetail.value.mockMatchRule.body,
            formDataMatch: {
              ...mockDetail.value.mockMatchRule.body.formDataMatch,
              matchRules: validBodyMatchRules,
            },
          },
          header: {
            ...mockDetail.value.mockMatchRule.header,
            matchRules: validHeaderMatchRules,
          },
          query: {
            ...mockDetail.value.mockMatchRule.query,
            matchRules: validQueryMatchRules,
          },
          rest: {
            ...mockDetail.value.mockMatchRule.rest,
            matchRules: validRestMatchRules,
          },
        },
        response: {
          ...mockDetail.value.response,
          headers: validResponseHeaders,
        },
        ...parseFileResult,
        apiDefinitionId: props.definitionDetail.id,
        projectId: appStore.currentProjectId,
      };
      if (isEdit.value) {
        await updateMock({
          id: mockDetail.value.id || '',
          ...params,
        });
        Message.success(t('common.updateSuccess'));
      } else {
        await addMock(params);
        Message.success(t('common.createSuccess'));
      }
      emit('addDone');
      handleCancel();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }
</script>

<style lang="less" scoped></style>
