<template>
  <MsFormTable v-bind="props" :data="paramsData" @change="handleFormTableChange">
    <!-- 展开行-->
    <template #expand-icon="{ record }">
      <div class="flex flex-row items-center gap-[2px] text-[var(--color-text-4)]">
        <icon-branch class="scale-y-[-1]" />
        <span v-if="record.children">{{ record.children.length }}</span>
      </div>
    </template>
    <!-- 表格头 slot -->
    <template #encodeTitle>
      <div class="flex items-center text-[var(--color-text-3)]">
        {{ t('apiTestDebug.encode') }}
        <a-tooltip position="right">
          <icon-question-circle
            class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
            size="16"
          />
          <template #content>
            <div>{{ t('apiTestDebug.encodeTip1') }}</div>
            <div>{{ t('apiTestDebug.encodeTip2') }}</div>
          </template>
        </a-tooltip>
      </div>
    </template>
    <template #typeTitle="{ columnConfig }">
      <div class="flex items-center text-[var(--color-text-3)]">
        {{ t('apiTestDebug.paramType') }}
        <a-tooltip :content="columnConfig.typeTitleTooltip" position="right">
          <icon-question-circle
            class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
            size="16"
          />
        </a-tooltip>
      </div>
    </template>
    <template #documentMustIncludeTitle>
      <div class="flex flex-row items-center gap-[4px]">
        <a-checkbox
          :model-value="mustIncludeAllChecked"
          :indeterminate="mustIncludeIndeterminate"
          @change="(v) => handleMustIncludeChange(v as boolean)"
        />
        <div class="ml-[4px] text-[var(--color-text-3)]">{{ t('ms.assertion.mustInclude') }}</div>
      </div>
    </template>
    <template #documentTypeCheckingTitle>
      <div class="flex flex-row items-center gap-[4px]">
        <a-checkbox
          :model-value="typeCheckingAllChecked"
          :indeterminate="typeCheckingIndeterminate"
          @change="(v) => handleTypeCheckingChange(v as boolean)"
        />
        <div class="ml-[4px] text-[var(--color-text-3)]">{{ t('ms.assertion.typeChecking') }}</div>
      </div>
    </template>
    <!-- 表格列 slot -->
    <template #key="{ record, columnConfig, rowIndex }">
      <a-popover
        position="tl"
        :disabled="!record[columnConfig.dataIndex as string] || record[columnConfig.dataIndex as string].trim() === ''"
        class="ms-params-input-popover"
      >
        <template #content>
          <div class="param-popover-title">
            {{ t('apiTestDebug.paramName') }}
          </div>
          <div class="param-popover-value">
            {{ record[columnConfig.dataIndex as string] }}
          </div>
        </template>
        <a-input
          v-model:model-value="record[columnConfig.dataIndex as string]"
          :placeholder="t('apiTestDebug.paramNamePlaceholder')"
          class="ms-form-table-input"
          :max-length="255"
          size="mini"
          @input="() => addTableLine(rowIndex, columnConfig.addLineDisabled)"
        />
      </a-popover>
    </template>
    <template #paramType="{ record, columnConfig, rowIndex }">
      <a-tooltip
        v-if="columnConfig.hasRequired"
        :content="t(record.required ? 'apiTestDebug.paramRequired' : 'apiTestDebug.paramNotRequired')"
      >
        <MsButton
          type="icon"
          :class="[
            record.required ? '!text-[rgb(var(--danger-5))]' : '!text-[var(--color-text-brand)]',
            '!mr-[4px] !p-[4px]',
          ]"
          size="mini"
          @click="toggleRequired(record, rowIndex)"
        >
          <div>*</div>
        </MsButton>
      </a-tooltip>
      <a-select
        v-model:model-value="record.paramType"
        :options="columnConfig.typeOptions || []"
        class="ms-form-table-input w-full"
        size="mini"
        @change="(val) => handleTypeChange(val, record, rowIndex, columnConfig.addLineDisabled)"
      />
    </template>
    <template #extractType="{ record, columnConfig, rowIndex }">
      <a-select
        v-model:model-value="record.extractType"
        :options="columnConfig.typeOptions || []"
        class="ms-form-table-input w-[110px]"
        size="mini"
        @change="() => addTableLine(rowIndex)"
      />
    </template>
    <template #variableType="{ record, columnConfig, rowIndex }">
      <a-select
        v-model:model-value="record.variableType"
        :options="columnConfig.typeOptions || []"
        class="ms-form-table-input w-[110px]"
        size="mini"
        @change="() => addTableLine(rowIndex)"
      />
    </template>
    <template #extractScope="{ record, columnConfig, rowIndex }">
      <a-select
        v-model:model-value="record.extractScope"
        :options="columnConfig.typeOptions || []"
        class="ms-form-table-input w-[180px]"
        size="mini"
        @change="() => addTableLine(rowIndex)"
      />
    </template>
    <template #expression="{ record, rowIndex, columnConfig }">
      <slot name="expression" :record="record" :row-index="rowIndex" :column-config="columnConfig"></slot>
    </template>
    <template #value="{ record, columnConfig, rowIndex }">
      <a-popover
        v-if="columnConfig.isNormal"
        position="tl"
        :disabled="!record.value || record.value.trim() === ''"
        class="ms-params-input-popover"
      >
        <template #content>
          <div class="param-popover-title">
            {{ t('apiTestDebug.paramValue') }}
          </div>
          <div class="param-popover-value">
            {{ record.value }}
          </div>
        </template>
        <a-input
          v-model:model-value="record.value"
          class="ms-form-table-input"
          :placeholder="t('apiTestDebug.commonPlaceholder')"
          :max-length="255"
          size="mini"
          @input="() => addTableLine(rowIndex)"
        />
      </a-popover>
      <MsAddAttachment
        v-else-if="record.paramType === RequestParamsType.FILE"
        v-model:file-list="record.files"
        mode="input"
        :multiple="true"
        :fields="{
          id: 'fileId',
          name: 'fileName',
        }"
        :file-save-as-source-id="props.fileSaveAsSourceId"
        :file-save-as-api="props.fileSaveAsApi"
        :file-module-options-api="props.fileModuleOptionsApi"
        input-class="ms-form-table-input h-[24px]"
        input-size="small"
        tag-size="small"
        @change="(files, file) => handleFileChange(files, record, rowIndex, file)"
        @delete-file="() => emitChange('deleteFile')"
      />
      <MsParamsInput
        v-else
        v-model:value="record.value"
        size="mini"
        @change="() => addTableLine(rowIndex)"
        @dblclick="quickInputParams(record)"
        @apply="() => addTableLine(rowIndex)"
      />
    </template>
    <template #lengthRange="{ record, rowIndex }">
      <div class="flex items-center justify-between">
        <a-input-number
          v-model:model-value="record.minLength"
          :placeholder="t('apiTestDebug.paramMin')"
          :min="0"
          class="ms-form-table-input ms-form-table-input-number"
          size="mini"
          model-event="input"
          @change="() => addTableLine(rowIndex)"
        />
        <div class="mx-[4px]">{{ t('common.to') }}</div>
        <a-input-number
          v-model:model-value="record.maxLength"
          :placeholder="t('apiTestDebug.paramMax')"
          :min="0"
          class="ms-form-table-input"
          size="mini"
          model-event="input"
          @change="() => addTableLine(rowIndex)"
        />
      </div>
    </template>
    <template #tag="{ record, columnConfig, rowIndex }">
      <a-popover
        position="tl"
        :disabled="(record[columnConfig.dataIndex as string]||[]).length === 0"
        class="ms-params-input-popover"
      >
        <template #content>
          <div class="param-popover-title">
            {{ t('common.tag') }}
          </div>
          <div class="param-popover-value">
            <MsTagsGroup is-string-tag :tag-list="record[columnConfig.dataIndex as string]" />
          </div>
        </template>
        <MsTagsInput
          v-model:model-value="record[columnConfig.dataIndex as string]"
          :max-tag-count="1"
          input-class="ms-form-table-input"
          size="mini"
          @change="() => addTableLine(rowIndex)"
          @clear="() => addTableLine(rowIndex)"
        />
      </a-popover>
    </template>
    <template #description="{ record, columnConfig, rowIndex }">
      <paramDescInput
        v-model:desc="record[columnConfig.dataIndex as string]"
        size="mini"
        @input="() => addTableLine(rowIndex)"
        @dblclick="quickInputDesc(record)"
        @change="handleDescChange"
      />
    </template>
    <template #encode="{ record, rowIndex }">
      <a-switch
        v-model:model-value="record.encode"
        size="small"
        class="ms-form-table-input-switch"
        type="line"
        @change="() => addTableLine(rowIndex)"
      />
    </template>
    <template #mustContain="{ record, columnConfig }">
      <a-checkbox
        v-model:model-value="record[columnConfig.dataIndex as string]"
        @change="handleMustContainColChange(false)"
      />
    </template>
    <template #typeChecking="{ record, columnConfig }">
      <a-checkbox
        v-model:model-value="record[columnConfig.dataIndex as string]"
        @change="handleTypeCheckingColChange(false)"
      />
    </template>
    <!-- 响应头 -->
    <template #header="{ record, columnConfig, rowIndex }">
      <a-select v-model="record.header" class="ms-form-table-input" size="mini" @change="() => addTableLine(rowIndex)">
        <a-option v-for="item in columnConfig.options" :key="item.value">{{ t(item.label) }}</a-option>
      </a-select>
    </template>
    <!-- 匹配条件 -->
    <template #condition="{ record, columnConfig }">
      <a-select v-model="record.condition" size="mini" class="ms-form-table-input">
        <a-option v-for="item in columnConfig.options" :key="item.value" :value="item.value">{{
          t(item.label)
        }}</a-option>
      </a-select>
    </template>
    <!-- 匹配值 -->
    <template #expectedValue="{ record, rowIndex, columnConfig }">
      <a-tooltip
        v-if="columnConfig.hasRequired"
        :content="t(record.required ? 'apiTestDebug.paramRequired' : 'apiTestDebug.paramNotRequired')"
      >
        <MsButton
          type="icon"
          :class="[
            record.required ? '!text-[rgb(var(--danger-5))]' : '!text-[var(--color-text-brand)]',
            '!mr-[4px] !p-[4px]',
          ]"
          size="mini"
          @click="toggleRequired(record, rowIndex)"
        >
          <div>*</div>
        </MsButton>
      </a-tooltip>
      <a-input v-model="record.expectedValue" size="mini" class="ms-form-table-input" />
    </template>
    <template #project="{ record, rowIndex }">
      <a-select
        v-model:model-value="record.projectId"
        class="ms-form-table-input w-max-[200px] focus-within:!bg-[var(--color-text-n8)] hover:!bg-[var(--color-text-n8)]"
        :bordered="false"
        allow-search
        @change="(val) => handleProjectChange(val as string,record.projectId, rowIndex)"
      >
        <template #arrow-icon>
          <icon-caret-down />
        </template>

        <a-tooltip v-for="project of disProjectList" :key="project.id" :mouse-enter-delay="500" :content="project.name">
          <a-option
            :key="project.id"
            :value="project.id"
            :class="project.id === appStore.currentProjectId ? 'arco-select-option-selected' : ''"
            :disabled="project.disabled"
          >
            {{ project.name }}
          </a-option>
        </a-tooltip>
      </a-select>
    </template>
    <template #environment="{ record }">
      <MsSelect
        v-if="record.projectId"
        v-model:model-value="record.environmentId"
        v-model:input-value="record.environmentInput"
        :disabled="!record.projectId"
        :options="[]"
        mode="remote"
        value-key="id"
        label-key="name"
        :search-keys="['name']"
        size="mini"
        allow-search
        class="ms-form-table-input"
        :remote-func="initEnvOptions"
        :remote-extra-params="{ projectId: record.projectId, keyword: record.environmentInput }"
        @change-object="(val) => handleEnvironment(val, record)"
      />
      <span v-else></span>
    </template>
    <template #host="{ record }">
      <MsTagsGroup
        v-if="Array.isArray(record.domain)"
        :tag-list="getDomain(record.domain)"
        size="small"
        @click="showHostModal(record)"
      />
      <div v-else class="text-[var(--color-text-1)]">{{ '-' }}</div>
    </template>
    <template #operation="{ record, rowIndex, columnConfig }">
      <div class="flex flex-row items-center" :class="{ 'justify-end': columnConfig.align === 'right' }">
        <a-switch
          v-if="columnConfig.hasDisable"
          v-model="record.enable"
          size="small"
          type="line"
          class="mr-[8px]"
          @change="() => addTableLine(rowIndex)"
        />
        <slot name="operationPre" :record="record" :row-index="rowIndex" :column-config="columnConfig"></slot>
        <MsTableMoreAction
          v-if="columnConfig.moreAction"
          :list="getMoreActionList(columnConfig.moreAction, record)"
          @select="(e) => handleMoreActionSelect(e, record)"
        />
        <a-trigger v-if="columnConfig.format === RequestBodyFormat.FORM_DATA" trigger="click" position="br">
          <MsButton type="icon" class="!mr-[8px]" size="mini">
            <icon-more />
          </MsButton>
          <template #content>
            <div class="content-type-trigger-content">
              <div class="mb-[8px] text-[var(--color-text-1)]">Content-Type</div>
              <a-select
                v-model:model-value="record.contentType"
                :options="Object.values(RequestContentTypeEnum).map((e) => ({ label: e, value: e }))"
                allow-create
                size="mini"
                @change="(val) => addTableLine(val as number)"
              />
            </div>
          </template>
        </a-trigger>
        <icon-minus-circle
          v-if="props.isTreeTable && record.id !== 0"
          class="ml-[8px] cursor-pointer text-[var(--color-text-4)]"
          size="20"
          @click="deleteParam(record, rowIndex)"
        />
        <icon-minus-circle
          v-else-if="paramsLength > 1 && rowIndex !== paramsLength - 1"
          class="ml-[8px] cursor-pointer text-[var(--color-text-4)]"
          size="20"
          @click="deleteParam(record, rowIndex)"
        />
      </div>
    </template>
  </MsFormTable>
  <a-modal
    v-model:visible="showQuickInputParam"
    :title="t('ms.paramsInput.value')"
    :ok-text="t('apiTestDebug.apply')"
    :ok-button-props="{ disabled: !quickInputParamValue || quickInputParamValue.trim() === '' }"
    class="ms-modal-form"
    body-class="!p-0"
    :width="680"
    title-align="start"
    @ok="applyQuickInputParam"
    @close="clearQuickInputParam"
  >
    <MsCodeEditor
      v-if="showQuickInputParam"
      v-model:model-value="quickInputParamValue"
      theme="vs"
      height="300px"
      :show-full-screen="false"
    >
      <template #rightTitle>
        <div class="flex justify-between">
          <div class="text-[var(--color-text-1)]">
            {{ t('apiTestDebug.quickInputParamsTip') }}
          </div>
        </div>
      </template>
    </MsCodeEditor>
  </a-modal>
  <a-modal
    v-model:visible="showQuickInputDesc"
    :title="t('apiTestDebug.desc')"
    :ok-text="t('common.save')"
    :ok-button-props="{ disabled: !quickInputDescValue || quickInputDescValue.trim() === '' }"
    class="ms-modal-form"
    body-class="!p-0"
    :width="480"
    title-align="start"
    :auto-size="{ minRows: 2 }"
    @ok="applyQuickInputDesc"
    @close="clearQuickInputDesc"
  >
    <a-textarea
      v-model:model-value="quickInputDescValue"
      :placeholder="t('apiTestDebug.descPlaceholder')"
      :max-length="1000"
    ></a-textarea>
  </a-modal>
  <!-- <a-modal v-model:visible="hostVisible" :title="t('project.environmental.host')" @close="hostModalClose">
    <a-table :columns="hostColumn" :data="hostData" />
  </a-modal> -->
  <DomainModal v-model:visible="hostVisible" :data="hostData" />
</template>

<script async setup lang="ts">
  import { TableColumnData, TableData } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import MsFormTable, { FormTableColumn } from '@/components/pure/ms-form-table/index.vue';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTagsGroup from '@/components/pure/ms-tag/ms-tag-group.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import { MsFileItem } from '@/components/pure/ms-upload/types';
  import MsSelect from '@/components/business/ms-select/index';
  import paramDescInput from './paramDescInput.vue';
  import DomainModal from '@/views/project-management/environmental/components/envParams/popUp/domain.vue';

  import { groupCategoryEnvList, groupProjectEnv } from '@/api/modules/project-management/envManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { ModuleTreeNode, TransferFileParams } from '@/models/common';
  import { HttpForm, ProjectOptionItem } from '@/models/projectManagement/environmental';
  import { RequestBodyFormat, RequestContentTypeEnum, RequestParamsType } from '@/enums/apiEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import { filterKeyValParams } from './utils';
  import { TableOperationColumn } from '@arco-design/web-vue/es/table/interface';
  // 异步加载组件
  const MsAddAttachment = defineAsyncComponent(() => import('@/components/business/ms-add-attachment/index.vue'));
  const MsParamsInput = defineAsyncComponent(() => import('@/components/business/ms-params-input/index.vue'));

  export interface ParamTableColumn extends FormTableColumn {
    isNormal?: boolean; // 用于 value 列区分是普通输入框还是 MsParamsInput
    hasRequired?: boolean; // 用于 type 列区分是否有 required 星号
    typeOptions?: { label: string; value: string }[]; // 用于 type 列选择器选项
    typeTitleTooltip?: string; // 用于 type 表头列展示的 tooltip
    hasDisable?: boolean; // 用于 operation 列区分是否有 enable 开关
    moreAction?: ActionsItem[]; // 用于 operation 列更多操作按钮配置
    format?: RequestBodyFormat; // 用于 operation 列区分是否有请求体格式选择器
    addLineDisabled?: boolean; // 用于 是否禁用添加新行
  }

  const props = withDefaults(
    defineProps<{
      params?: Record<string, any>[];
      defaultParamItem?: Record<string, any>; // 默认参数项，用于添加新行时的默认值
      columns: ParamTableColumn[];
      scroll?: {
        x?: number | string;
        y?: number | string;
        maxHeight?: number | string;
        minWidth?: number | string;
      };
      heightUsed?: number;
      draggable?: boolean;
      selectable?: boolean;
      showSetting?: boolean; // 是否显示列设置
      tableKey?: TableKeyEnum; // 表格key showSetting为true时必传
      disabled?: boolean; // 是否禁用
      showSelectorAll?: boolean; // 是否显示全选
      isSimpleSetting?: boolean; // 是否简单Column设置
      response?: string; // 响应内容
      isTreeTable?: boolean; // 是否树形表格
      spanMethod?: (data: {
        record: TableData;
        column: TableColumnData | TableOperationColumn;
        rowIndex: number;
        columnIndex: number;
      }) => { rowspan?: number; colspan?: number } | void;
      uploadTempFileApi?: (...args) => Promise<any>; // 上传临时文件接口
      fileSaveAsSourceId?: string | number; // 文件转存关联的资源id
      fileSaveAsApi?: (params: TransferFileParams) => Promise<string>; // 文件转存接口
      fileModuleOptionsApi?: (projectId: string) => Promise<ModuleTreeNode[]>; // 文件转存目录下拉框接口
    }>(),
    {
      params: () => [],
      selectable: true,
      showSetting: false,
      tableKey: undefined,
      isSimpleSetting: true,
      defaultParamItem: () => ({
        required: false,
        key: '',
        paramType: RequestParamsType.STRING,
        value: '',
        minLength: undefined,
        maxLength: undefined,
        contentType: RequestContentTypeEnum.TEXT,
        tag: [],
        description: '',
        encode: false,
        disable: false,
        mustContain: false,
      }),
    }
  );
  const emit = defineEmits<{
    (e: 'change', data: Record<string, any>[], isInit?: boolean): void; // 都触发这个事件以通知父组件参数数组被更改
    (e: 'moreActionSelect', event: ActionsItem, record: Record<string, any>): void;
    (e: 'projectChange', projectId: string): void;
    (e: 'treeDelete', record: Record<string, any>): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  const paramsData = ref<Record<string, any>[]>([]);

  function emitChange(from: string, isInit?: boolean) {
    emit('change', paramsData.value, isInit);
  }

  const paramsLength = computed(() => paramsData.value.length);

  function deleteParam(record: Record<string, any>, rowIndex: number) {
    if (props.isTreeTable) {
      emit('treeDelete', record);
      return;
    }
    paramsData.value.splice(rowIndex, 1);
    emitChange('deleteParam');
  }

  /** 断言-文档-Begin */
  // 断言-文档-必须包含-全选
  const mustIncludeAllChecked = ref(false);
  const mustIncludeIndeterminate = ref(false);
  const handleMustIncludeChange = (val: boolean) => {
    mustIncludeAllChecked.value = val;
    mustIncludeIndeterminate.value = false;
    paramsData.value.forEach((e: any) => {
      e.mustInclude = val;
    });
    emitChange('handleMustIncludeChange');
  };
  const handleMustContainColChange = (notEmit?: boolean) => {
    const checkedList = paramsData.value.filter((e: any) => e.mustInclude).map((e: any) => e.id);
    if (checkedList.length === paramsData.value.length) {
      mustIncludeAllChecked.value = true;
      mustIncludeIndeterminate.value = false;
    } else if (checkedList.length === 0) {
      mustIncludeAllChecked.value = false;
      mustIncludeIndeterminate.value = false;
    } else {
      mustIncludeAllChecked.value = false;
      mustIncludeIndeterminate.value = true;
    }
    if (!notEmit) {
      emitChange('handleMustContainColChange');
    }
  };

  const typeCheckingAllChecked = ref(false);
  const typeCheckingIndeterminate = ref(false);
  const handleTypeCheckingChange = (val: boolean) => {
    typeCheckingAllChecked.value = val;
    typeCheckingIndeterminate.value = false;
    paramsData.value.forEach((e: any) => {
      e.typeChecking = val;
    });
    emitChange('handleTypeCheckingChange');
  };
  const handleTypeCheckingColChange = (notEmit?: boolean) => {
    const checkedList = paramsData.value.filter((e: any) => e.typeChecking).map((e: any) => e.id);
    if (checkedList.length === paramsData.value.length) {
      typeCheckingAllChecked.value = true;
      typeCheckingIndeterminate.value = false;
    } else if (checkedList.length === 0) {
      typeCheckingAllChecked.value = false;
      typeCheckingIndeterminate.value = false;
    } else {
      typeCheckingAllChecked.value = false;
      typeCheckingIndeterminate.value = true;
    }
    if (!notEmit) {
      emitChange('handleTypeCheckingColChange');
    }
  };
  /** 断言-文档-end */

  /** 环境管理-环境组 start */

  const sourceProjectOptions = ref<ProjectOptionItem[]>([]);

  // 获取项目的options
  const initProjectOptions = async () => {
    const res = await groupProjectEnv(appStore.currentOrgId);
    sourceProjectOptions.value = res;
  };
  // 获取所有环境目录
  const envDomainList = ref<ProjectOptionItem[]>([]);
  // 获取环境的options
  const initEnvOptions = async (params: Record<string, any>) => {
    const { projectId } = params;
    const res = await groupCategoryEnvList(projectId);
    envDomainList.value = res;
    return res;
  };

  const handleEnvironment = (obj: Record<string, any>, record: Record<string, any>) => {
    record.domain = obj.domain;
    emitChange('handleEnvironment');
  };

  const hostVisible = ref(false);
  const hostData = ref<any[]>([]);
  // const hostColumn = [
  //   {
  //     title: t('project.environmental.http.host'),
  //     dataIndex: 'host',
  //     showTooltip: true,
  //     width: 300,
  //   },
  //   {
  //     title: t('project.environmental.http.desc'),
  //     dataIndex: 'description',
  //   },
  // ];

  const showHostModal = (record: Record<string, any>) => {
    hostVisible.value = true;
    hostData.value = record.domain || [];
  };

  // const hostModalClose = () => {
  //   hostVisible.value = false;
  //   hostData.value = [];
  // };

  watchEffect(() => {
    if (props.columns.some((e) => e.dataIndex === 'projectId')) {
      initProjectOptions();
    }
  });

  /** 环境管理-环境组 end */

  /**
   * 当表格输入框变化时，给参数表格添加一行数据行
   * @param val 输入值
   * @param key 当前列的 key  key存在的时候 才可以增加新的一行
   * @param isForce 是否强制添加
   */
  function addTableLine(rowIndex: number, addLineDisabled?: boolean, isInit?: boolean) {
    if (addLineDisabled) {
      emitChange('addTableLine addLineDisabled', isInit);
      return;
    }
    if (rowIndex === paramsData.value.length - 1 && paramsData.value[rowIndex].key) {
      // 最后一行的更改才会触发添加新一行
      const id = new Date().getTime().toString();
      paramsData.value.push({
        id,
        ...cloneDeep(props.defaultParamItem), // 深拷贝，避免有嵌套引用类型，数据隔离
        enable: true, // 是否勾选
      } as any);
    }
    emitChange('addTableLine', isInit);
    handleMustContainColChange(true);
    handleTypeCheckingColChange(true);
  }

  watch(
    () => props.params,
    (arr) => {
      if (arr.length > 0) {
        let hasNoIdItem = false; // 是否有没有id的项，用以判断是否是后台数据初始化表格
        paramsData.value = arr.map((item, i) => {
          if (!item) {
            // 批量添加过来的数据最后一行会是 undefined
            return {
              ...props.defaultParamItem,
              id: new Date().getTime() + i,
            };
          }
          if (!item.id) {
            // 后台存储无id，渲染时需要手动添加一次
            hasNoIdItem = true;
            return {
              ...item,
              id: new Date().getTime() + i,
            };
          }
          return item;
        });
        if (hasNoIdItem && !filterKeyValParams(arr, props.defaultParamItem).lastDataIsDefault && !props.isTreeTable) {
          addTableLine(arr.length - 1, false, true);
        }
      } else {
        const id = new Date().getTime().toString();
        paramsData.value = [
          {
            id, // 默认给时间戳 id，若 props.defaultParamItem 有 id，则覆盖
            ...props.defaultParamItem,
            enable: true, // 是否勾选
          },
        ] as any[];
        emitChange('watch props.params', true);
      }
    },
    {
      immediate: true,
    }
  );

  function toggleRequired(record: Record<string, any>, rowIndex: number) {
    record.required = !record.required;
    addTableLine(rowIndex);
    emitChange('toggleRequired');
  }

  async function handleFileChange(
    files: MsFileItem[],
    record: Record<string, any>,
    rowIndex: number,
    file?: MsFileItem
  ) {
    try {
      if (props.uploadTempFileApi && file?.local) {
        // 本地上传单次只能选一个文件
        appStore.showLoading();
        const res = await props.uploadTempFileApi(file.file);
        for (let i = 0; i < record.files.length; i++) {
          const item = record.files[i];
          if ([item.fileId, item.uid].includes(file.uid)) {
            record.files[i] = {
              ...file,
              fileId: res.data,
              fileName: file.name || '',
            };
            break;
          }
        }
      } else {
        // 关联文件可选多个文件
        record.files = files.map((e) => ({
          ...e,
          fileId: e.uid || e.fileId || '',
          fileName: e.name || e.fileName || '',
        }));
      }
      addTableLine(rowIndex);
      emitChange('handleFileChange');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      appStore.hideLoading();
    }
  }

  const showQuickInputParam = ref(false);
  const activeQuickInputRecord = ref<any>({});
  const quickInputParamValue = ref('');

  function quickInputParams(record: any) {
    activeQuickInputRecord.value = record;
    showQuickInputParam.value = true;
    quickInputParamValue.value = record.value;
  }

  function clearQuickInputParam() {
    activeQuickInputRecord.value = {};
    quickInputParamValue.value = '';
  }

  function applyQuickInputParam() {
    activeQuickInputRecord.value.value = quickInputParamValue.value;
    showQuickInputParam.value = false;
    addTableLine(paramsData.value.findIndex((e) => e.id === activeQuickInputRecord.value.id));
    clearQuickInputParam();
    emitChange('applyQuickInputParam');
  }

  const showQuickInputDesc = ref(false);
  const quickInputDescValue = ref('');

  function quickInputDesc(record: any) {
    activeQuickInputRecord.value = record;
    showQuickInputDesc.value = true;
    quickInputDescValue.value = record.description;
  }

  function clearQuickInputDesc() {
    activeQuickInputRecord.value = {};
    quickInputDescValue.value = '';
  }

  function applyQuickInputDesc() {
    activeQuickInputRecord.value.description = quickInputDescValue.value;
    showQuickInputDesc.value = false;
    addTableLine(paramsData.value.findIndex((e) => e.id === activeQuickInputRecord.value.id));
    clearQuickInputDesc();
    emitChange('applyQuickInputDesc');
  }

  function handleDescChange() {
    emitChange('handleDescChange');
  }

  function handleTypeChange(
    val: string | number | boolean | Record<string, any> | (string | number | boolean | Record<string, any>)[],
    record: Record<string, any>,
    rowIndex: number,
    addLineDisabled?: boolean
  ) {
    addTableLine(rowIndex, addLineDisabled);
    // 根据参数类型自动推断 Content-Type 类型
    if (record.contentType) {
      if (val === 'file') {
        record.contentType = RequestContentTypeEnum.OCTET_STREAM;
      } else if (val === 'json') {
        record.contentType = RequestContentTypeEnum.JSON;
      } else {
        record.contentType = RequestContentTypeEnum.TEXT;
      }
    }
    emitChange('handleTypeChange');
  }

  /**
   * 获取更多操作按钮列表
   * @param actions 按钮列表
   * @param record 当前行数据
   */
  function getMoreActionList(actions: ActionsItem[], record: Record<string, any>) {
    if (props.columns.findIndex((e) => e.dataIndex === 'expression') !== -1) {
      // 如果有expression列，就需要根据expression的值来判断按钮列表是否禁用
      if (record.expression === '' || record.expression === undefined || record.expression === null) {
        return actions.map((e) => ({ ...e, disabled: true }));
      }
      return actions;
    }
    return actions;
  }

  function handleMoreActionSelect(event: ActionsItem, record: Record<string, any>) {
    emit('moreActionSelect', event, record);
  }

  function handleProjectChange(val: string, projectId: string, rowIndex: number) {
    emit('projectChange', projectId);
    addTableLine(rowIndex);
  }

  const disProjectList = computed(() => {
    const selectProjectIds = (props.params || []).map((item) => item.projectId).filter((item) => item);
    return appStore.projectList.map((item: any) => {
      if (selectProjectIds.includes(item.id)) {
        return {
          ...item,
          disabled: true,
        };
      }
      return {
        ...item,
      };
    });
  });
  function getDomain(domain: HttpForm[]) {
    return (domain || []).map((item: any) => {
      return {
        id: item.id,
        name: item.hostname,
      };
    });
  }

  function handleFormTableChange(data: any[]) {
    paramsData.value = [...data];
    emitChange('handleFormTableChange');
  }

  defineExpose({
    addTableLine,
  });
</script>

<style lang="less" scoped>
  .content-type-trigger-content {
    @apply bg-white;

    padding: 8px;
    border-radius: var(--border-radius-small);
    box-shadow: 0 4px 10px -1px rgb(100 100 102 / 15%);
  }
  .param-popover-title {
    @apply font-medium;

    margin-bottom: 4px;
    font-size: 12px;
    font-weight: 500;
    line-height: 16px;
    color: var(--color-text-1);
  }
  .param-popover-value {
    min-width: 100px;
    max-width: 280px;
    font-size: 12px;
    line-height: 16px;
    color: var(--color-text-1);
  }
</style>
