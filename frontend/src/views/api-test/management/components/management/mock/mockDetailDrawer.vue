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
    @continue="() => handleSave(true)"
    @confirm="handleSave"
    @cancel="handleCancel"
    @close="handleCancel"
  >
    <template #tbutton>
      <div v-if="isReadOnly" class="right-operation-button-icon flex items-center gap-[4px]">
        <MsButton
          v-permission="['PROJECT_API_DEFINITION_MOCK:READ+UPDATE']"
          type="icon"
          status="secondary"
          @click="handleChangeEdit"
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
          <MsIcon type="icon-icon_delete-trash_outlined1" />
          {{ t('common.delete') }}
        </MsButton>
      </div>
    </template>
    <a-spin v-if="visible" :loading="loading" class="block p-[16px]">
      <MsDetailCard
        :title="`【${props.definitionDetail.num}】${props.definitionDetail.name}`"
        :description="[]"
        class="mb-[16px]"
      >
        <template #titleRight>
          <div class="flex items-center gap-[16px] overflow-hidden">
            <div class="flex items-center gap-[8px]">
              <div class="whitespace-nowrap text-[var(--color-text-4)]">{{ t('apiTestManagement.apiType') }}</div>
              <apiMethodName :method="props.definitionDetail.method" tag-size="small" is-tag />
            </div>
            <div class="flex items-center gap-[8px] overflow-hidden">
              <div class="whitespace-nowrap text-[var(--color-text-4)]">{{ t('apiTestManagement.path') }}</div>
              <a-tooltip :content="props.definitionDetail.url || props.definitionDetail.path" position="tl">
                <div class="one-line-text">{{ props.definitionDetail.url || props.definitionDetail.path }}</div>
              </a-tooltip>
            </div>
          </div>
        </template>
      </MsDetailCard>
      <a-form ref="mockFormRef" :model="mockDetail" :disabled="isReadOnly">
        <a-form-item
          class="hidden-item mb-[16px]"
          field="name"
          :rules="[{ required: true, message: t('mockManagement.nameNotNull') }]"
        >
          <a-input
            v-model:model-value="mockDetail.name"
            :placeholder="t('mockManagement.namePlaceholder')"
            :max-length="255"
            class="w-[732px]"
          ></a-input>
        </a-form-item>
        <a-form-item
          class="hidden-item mb-[16px]"
          :rules="[{ required: true, message: t('mockManagement.nameNotNull') }]"
        >
          <MsTagsInput
            v-model:model-value="mockDetail.tags"
            input-class="w-[732px]"
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
        v-model:match-all="currentMatchAll"
        v-model:match-rules="currentMatchRules"
        :key-options="currentKeyOptions"
        :disabled="isReadOnly"
        :form-key="activeTab"
      />
      <template v-else>
        <div class="mb-[8px] flex items-center justify-between">
          <a-radio-group
            v-model:model-value="mockDetail.mockMatchRule.body.bodyType"
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
          v-if="mockDetail.mockMatchRule.body.bodyType === RequestBodyFormat.NONE"
          class="flex h-[100px] items-center justify-center rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] text-[var(--color-text-4)]"
        >
          {{ t('apiTestDebug.noneBody') }}
        </div>
        <mockMatchRuleForm
          v-else-if="mockDetail.mockMatchRule.body.bodyType === RequestBodyFormat.FORM_DATA"
          :id="mockDetail.id"
          v-model:match-all="mockDetail.mockMatchRule.body.formDataBody.matchAll"
          v-model:match-rules="mockDetail.mockMatchRule.body.formDataBody.matchRules"
          :key-options="currentBodyKeyOptions"
          :disabled="isReadOnly"
        />
        <mockMatchRuleForm
          v-else-if="mockDetail.mockMatchRule.body.bodyType === RequestBodyFormat.WWW_FORM"
          :id="mockDetail.id"
          v-model:match-all="mockDetail.mockMatchRule.body.wwwFormBody.matchAll"
          v-model:match-rules="mockDetail.mockMatchRule.body.wwwFormBody.matchRules"
          :key-options="currentBodyKeyOptions"
          :disabled="isReadOnly"
        />
        <div v-else-if="mockDetail.mockMatchRule.body.bodyType === RequestBodyFormat.BINARY">
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
        <div v-else>
          <MsCodeEditor
            v-model:model-value="currentBodyCode"
            theme="vs"
            is-adaptive
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
  import { FormInstance, Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

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
  import useShortcutSave from '@/hooks/useShortcutSave';
  import useAppStore from '@/store/modules/app';

  import { ResponseDefinition } from '@/models/apiTest/common';
  import { MockParams } from '@/models/apiTest/mock';
  import { RequestBodyFormat, RequestComposition, RequestParamsType } from '@/enums/apiEnum';

  import {
    defaultHeaderParamsItem,
    defaultMatchRuleItem,
    defaultRequestParamsItem,
    makeMockDefaultParams,
  } from '@/views/api-test/components/config';
  import { filterKeyValParams, parseRequestBodyFiles } from '@/views/api-test/components/utils';

  const props = defineProps<{
    definitionDetail: RequestParam;
    detailId?: string;
    isCopy?: boolean;
    isEditMode?: boolean;
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
  const isEdit = ref(props.isEditMode);
  const mockDetail = ref<MockParams>(makeMockDefaultParams());
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
  const activeTab = ref<RequestComposition>(RequestComposition.HEADER);
  const mockTabList = [
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
    {
      value: RequestComposition.BODY,
      label: t('apiTestDebug.body'),
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
        return mockDetail.value.mockMatchRule.body.bodyType !== RequestBodyFormat.NONE ? '1' : '';
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
        return filterKeyValParams(props.definitionDetail.headers, defaultMatchRuleItem)
          .validParams.filter((e, index, self) => self.findIndex((item) => item.key === e.key) === index)
          .map((e) => ({
            label: e.key,
            value: e.key,
          }));
      case RequestComposition.QUERY:
        return filterKeyValParams(props.definitionDetail.query, defaultMatchRuleItem)
          .validParams.filter((e, index, self) => self.findIndex((item) => item.key === e.key) === index)
          .map((e) => ({
            label: e.key,
            value: e.key,
          }));
      case RequestComposition.REST:
        return filterKeyValParams(props.definitionDetail.rest, defaultMatchRuleItem)
          .validParams.filter((e, index, self) => self.findIndex((item) => item.key === e.key) === index)
          .map((e) => ({
            label: e.key,
            value: e.key,
          }));
      default:
        return [];
    }
  });
  // 当前请求 body 的参数名选项集合
  const currentBodyKeyOptions = computed(() => {
    switch (mockDetail.value.mockMatchRule.body.bodyType) {
      case RequestBodyFormat.FORM_DATA:
        return filterKeyValParams(props.definitionDetail.body.formDataBody.formValues, defaultMatchRuleItem)
          .validParams.filter((e, index, self) => self.findIndex((item) => item.key === e.key) === index)
          .map((e) => ({
            label: e.key,
            value: e.key,
            paramType: e.paramType,
          }));
      case RequestBodyFormat.WWW_FORM:
        return filterKeyValParams(props.definitionDetail.body.wwwFormBody.formValues, defaultMatchRuleItem)
          .validParams.filter((e, index, self) => self.findIndex((item) => item.key === e.key) === index)
          .map((e) => ({
            label: e.key,
            value: e.key,
            paramType: e.paramType,
          }));
      default:
        return [];
    }
  });

  // 当前请求 body 显示的代码
  const currentBodyCode = computed({
    get() {
      if (mockDetail.value.mockMatchRule.body.bodyType === RequestBodyFormat.JSON) {
        return mockDetail.value.mockMatchRule.body.jsonBody.jsonValue;
      }
      if (mockDetail.value.mockMatchRule.body.bodyType === RequestBodyFormat.XML) {
        return mockDetail.value.mockMatchRule.body.xmlBody.value;
      }
      return mockDetail.value.mockMatchRule.body.rawBody.value;
    },
    set(val) {
      if (mockDetail.value.mockMatchRule.body.bodyType === RequestBodyFormat.JSON) {
        mockDetail.value.mockMatchRule.body.jsonBody.jsonValue = val;
      } else if (mockDetail.value.mockMatchRule.body.bodyType === RequestBodyFormat.XML) {
        mockDetail.value.mockMatchRule.body.xmlBody.value = val;
      } else {
        mockDetail.value.mockMatchRule.body.rawBody.value = val;
      }
    },
  });
  // 当前请求 body 代码编辑器的语言
  const currentCodeLanguage = computed(() => {
    if (mockDetail.value.mockMatchRule.body.bodyType === RequestBodyFormat.JSON) {
      return LanguageEnum.JSON;
    }
    if (mockDetail.value.mockMatchRule.body.bodyType === RequestBodyFormat.XML) {
      return LanguageEnum.XML;
    }
    return LanguageEnum.PLAINTEXT;
  });

  /**
   * 添加默认的匹配规则项
   */
  function appendDefaultMatchRuleItem() {
    const { body } = mockDetail.value.mockMatchRule;
    mockDetail.value.mockMatchRule.body = {
      ...body,
      formDataBody: {
        matchAll: body.formDataBody.matchAll,
        matchRules: [...body.formDataBody.matchRules, cloneDeep(defaultMatchRuleItem)],
      },
      wwwFormBody: {
        matchAll: body.wwwFormBody.matchAll,
        matchRules: [...body.wwwFormBody.matchRules, cloneDeep(defaultMatchRuleItem)],
      },
    };
    mockDetail.value.mockMatchRule.header.matchRules = [
      ...mockDetail.value.mockMatchRule.header.matchRules,
      cloneDeep(defaultMatchRuleItem),
    ];
    mockDetail.value.mockMatchRule.query.matchRules = [
      ...mockDetail.value.mockMatchRule.query.matchRules,
      cloneDeep(defaultMatchRuleItem),
    ];
    mockDetail.value.mockMatchRule.rest.matchRules = [
      ...mockDetail.value.mockMatchRule.rest.matchRules,
      cloneDeep(defaultMatchRuleItem),
    ];
  }

  const fileList = ref<MsFileItem[]>([]);

  function showFirstHasDataTab() {
    if (mockDetail.value.mockMatchRule.body.bodyType !== RequestBodyFormat.NONE) {
      activeTab.value = RequestComposition.BODY;
    } else if (mockDetail.value.mockMatchRule.header.matchRules.length > 0) {
      activeTab.value = RequestComposition.HEADER;
    } else if (mockDetail.value.mockMatchRule.query.matchRules.length > 0) {
      activeTab.value = RequestComposition.QUERY;
    } else if (mockDetail.value.mockMatchRule.rest.matchRules.length > 0) {
      activeTab.value = RequestComposition.REST;
    }
  }

  async function initMockDetail() {
    try {
      loading.value = true;
      const res = await getMockDetail({
        id: props.detailId || '',
        projectId: appStore.currentProjectId,
      });
      // form-data 的匹配规则含有文件类型，特殊处理
      const formDataMatch = res.mockMatchRule.body.formDataBody.matchRules.map((item) => {
        const newParamType =
          currentBodyKeyOptions.value.find((e) => e.value === item.key)?.paramType || item.files.length > 0
            ? RequestParamsType.FILE
            : defaultMatchRuleItem.paramType;
        item.paramType = newParamType;
        item.files = item.files || [];
        return item;
      });
      mockDetail.value = {
        ...res,
        id: props.isCopy ? '' : res.id,
        isNew: props.isCopy,
        name: props.isCopy ? `${res.name}_copy` : res.name,
        mockMatchRule: {
          ...res.mockMatchRule,
          body: {
            ...res.mockMatchRule.body,
            formDataBody: {
              ...res.mockMatchRule.body.formDataBody,
              matchRules: formDataMatch,
            },
          },
        },
      };
      // 等formDataMatch解析完毕赋值后才能正确解析 body 内的文件类型值
      const parseFileResult = parseRequestBodyFiles(mockDetail.value.mockMatchRule.body, [
        res.response as unknown as ResponseDefinition,
      ]);
      mockDetail.value = {
        ...mockDetail.value,
        ...parseFileResult,
      };
      fileList.value = mockDetail.value.mockMatchRule.body.binaryBody.file
        ? [mockDetail.value.mockMatchRule.body.binaryBody.file as unknown as MsFileItem]
        : [];
      if (props.isCopy) {
        appendDefaultMatchRuleItem();
      }
      isEdit.value = !!props.isEditMode;
      if (props.isEditMode) {
        // 从表格的编辑按钮进入，给空表格添加默认行
        appendDefaultMatchRuleItem();
      }
      showFirstHasDataTab();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

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
          ...files[0],
          fileId: files[0]?.uid || files[0]?.id || '',
          fileName: files[0]?.originalName || '',
          fileAlias: files[0]?.name || '',
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

  function handleCancel() {
    mockDetail.value = makeMockDefaultParams();
    isEdit.value = false;
    visible.value = false;
  }

  function handleChangeEdit() {
    appendDefaultMatchRuleItem();
    isEdit.value = true;
  }

  function handleDelete() {
    emit('delete');
    handleCancel();
  }

  const mockFormRef = ref<FormInstance>();
  function handleSave(isContinue = false) {
    mockFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          loading.value = true;
          const { body } = mockDetail.value.mockMatchRule;
          const validFormDataBodyMatchRules = filterKeyValParams(
            body.formDataBody.matchRules,
            defaultMatchRuleItem
          ).validParams;
          const validWwwFormBodyMatchRules = filterKeyValParams(
            body.wwwFormBody.matchRules,
            defaultMatchRuleItem
          ).validParams;
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
          const parseFileResult = parseRequestBodyFiles(
            mockDetail.value.mockMatchRule.body,
            [mockDetail.value.response as unknown as ResponseDefinition],
            mockDetail.value.uploadFileIds,
            mockDetail.value.linkFileIds
          );
          const params: MockParams = {
            ...mockDetail.value,
            statusCode: mockDetail.value.response.statusCode,
            mockMatchRule: {
              ...mockDetail.value.mockMatchRule,
              body: {
                ...mockDetail.value.mockMatchRule.body,
                formDataBody: {
                  ...mockDetail.value.mockMatchRule.body.formDataBody,
                  matchRules: validFormDataBodyMatchRules,
                },
                wwwFormBody: {
                  ...mockDetail.value.mockMatchRule.body.wwwFormBody,
                  matchRules: validWwwFormBodyMatchRules,
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
            apiDefinitionId: props.definitionDetail.id,
            projectId: appStore.currentProjectId,
            uploadFileIds: parseFileResult.uploadFileIds,
            linkFileIds: parseFileResult.linkFileIds,
          };

          // 特别处理 jsonSchemaTableData 中的循环引用
          if (params.mockMatchRule?.body?.jsonBody?.jsonSchemaTableData) {
            params.mockMatchRule.body.jsonBody.jsonSchemaTableData =
              params.mockMatchRule.body.jsonBody.jsonSchemaTableData.map((item) => {
                const { parent: _, ...rest } = item;
                return {
                  ...rest,
                  children: rest.children?.map(({ parent: __, ...childRest }) => childRest),
                };
              });
          }

          if (isEdit.value) {
            await updateMock({
              id: mockDetail.value.id || '',
              ...params,
              ...parseFileResult,
            });
            Message.success(t('common.updateSuccess'));
          } else {
            await addMock(params);
            Message.success(t('common.createSuccess'));
          }
          emit('addDone');
          if (isContinue) {
            mockDetail.value.name = '';
            mockDetail.value.tags = [];
          } else {
            handleCancel();
          }
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          loading.value = false;
        }
      }
    });
  }

  const { registerCatchSaveShortcut, removeCatchSaveShortcut } = useShortcutSave(() => {
    handleSave();
  });

  watch(
    () => visible.value,
    async (val) => {
      if (val) {
        if (props.detailId) {
          await initMockDetail();
        } else {
          fileList.value = [];
          const { body } = props.definitionDetail;
          mockDetail.value.mockMatchRule.body = {
            ...mockDetail.value.mockMatchRule.body,
            bodyType: body.bodyType,
            binaryBody: cloneDeep(body.binaryBody),
            jsonBody: cloneDeep(body.jsonBody),
            xmlBody: cloneDeep(body.xmlBody),
            rawBody: cloneDeep(body.rawBody),
          };
          showFirstHasDataTab();
        }
        if (!isReadOnly.value) {
          registerCatchSaveShortcut();
        }
      } else {
        removeCatchSaveShortcut();
      }
    },
    {
      immediate: true,
    }
  );

  watch(
    () => isReadOnly.value,
    (val) => {
      if (!val) {
        registerCatchSaveShortcut();
      } else {
        removeCatchSaveShortcut();
      }
    }
  );
</script>

<style lang="less" scoped></style>
