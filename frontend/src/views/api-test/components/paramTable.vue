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
      <div class="flex items-center text-[var(--color-text-3)]" :class="columnConfig.hasRequired ? 'pl-[30px]' : ''">
        {{ t((columnConfig.title as string) || 'apiTestDebug.paramType') }}
        <a-tooltip v-if="columnConfig.typeTitleTooltip" :disabled="!columnConfig.typeTitleTooltip" position="right">
          <template #content>
            <template v-if="Array.isArray(columnConfig.typeTitleTooltip)">
              <div v-for="tip of columnConfig.typeTitleTooltip" :key="tip">{{ tip }}</div>
            </template>
            <div v-else>{{ columnConfig.typeTitleTooltip }}</div>
          </template>
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
          :disabled="props.disabledExceptParam"
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
          :disabled="props.disabledExceptParam"
          :model-value="typeCheckingAllChecked"
          :indeterminate="typeCheckingIndeterminate"
          @change="(v) => handleTypeCheckingChange(v as boolean)"
        />
        <div class="ml-[4px] text-[var(--color-text-3)]">{{ t('ms.assertion.typeChecking') }}</div>
      </div>
    </template>
    <template #extractValueTitle>
      <div class="flex items-center text-[var(--color-text-3)]">
        {{ t('apiTestDebug.extractValueByColumn') }}
        <a-tooltip :content="t('apiTestDebug.extractValueTitleTip')" position="right">
          <icon-question-circle
            class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
            size="16"
          />
        </a-tooltip>
      </div>
    </template>
    <template #batchAddTitle>
      <MsButton
        v-if="!props.disabledExceptParam && !props.disabledParamValue"
        type="text"
        size="mini"
        class="!mr-0"
        @click="emit('batchAdd')"
      >
        {{ t('apiTestDebug.batchAdd') }}
      </MsButton>
    </template>
    <!-- 表格列 slot -->
    <!-- 参数名 or 请求/响应头联想输入 -->
    <template #key="{ record, columnConfig, rowIndex }">
      <a-popover
        position="tl"
        :disabled="!record[columnConfig.dataIndex as string] || record[columnConfig.dataIndex as string].trim() === ''"
        class="ms-params-input-popover"
      >
        <template #content>
          <div class="ms-params-popover-title">
            {{ t(columnConfig.title as string) }}
          </div>
          <div class="ms-params-popover-value">
            {{ record[columnConfig.dataIndex as string] }}
          </div>
        </template>
        <a-auto-complete
          v-if="columnConfig.inputType === 'autoComplete'"
          v-model:model-value="record[columnConfig.dataIndex as string]"
          :disabled="props.disabledExceptParam || columnConfig.disabledColumn"
          :data="getAutoCompleteData(columnConfig, record)"
          class="ms-form-table-input ms-form-table-input--hasPlaceholder"
          :trigger-props="{ contentClass: 'ms-form-table-input-trigger' }"
          :filter-option="false"
          @focus="handleAutoCompleteFocus(record)"
          @search="(val) => handleSearchParams(val, columnConfig)"
          @change="() => addTableLine(rowIndex, columnConfig.addLineDisabled)"
          @select="(val) => selectAutoComplete(val, record, columnConfig)"
        >
          <template #option="{ data: opt }">
            <div class="w-[350px]">
              {{ t(opt.raw.label) }}
            </div>
          </template>
        </a-auto-complete>
        <a-input
          v-else
          v-model:model-value="record[columnConfig.dataIndex as string]"
          :disabled="props.disabledExceptParam || columnConfig.disabledColumn"
          :placeholder="t('apiTestDebug.commonPlaceholder')"
          class="ms-form-table-input ms-params-input ms-form-table-input--hasPlaceholder"
          @input="() => addTableLine(rowIndex, columnConfig.addLineDisabled)"
        >
          <template v-if="props.showQuickCopy" #suffix>
            <MsIcon
              type="icon-icon_copy_outlined"
              class="ms-params-input-suffix-icon"
              @click="copyParamsName(record[columnConfig.dataIndex as string])"
            />
          </template>
        </a-input>
      </a-popover>
    </template>
    <template #name="{ record, columnConfig, rowIndex }">
      <a-popover
        position="tl"
        :disabled="!record[columnConfig.dataIndex as string] || record[columnConfig.dataIndex as string].trim() === ''"
        class="ms-params-input-popover"
      >
        <template #content>
          <div class="ms-params-popover-title">
            {{ t('apiTestDebug.paramName') }}
          </div>
          <div class="ms-params-popover-value">
            {{ record[columnConfig.dataIndex as string] }}
          </div>
        </template>
        <a-input
          v-model:model-value="record[columnConfig.dataIndex as string]"
          :disabled="props.disabledExceptParam || columnConfig.disabledColumn"
          :placeholder="t('apiTestDebug.commonPlaceholder')"
          class="ms-form-table-input ms-form-table-input--hasPlaceholder"
          @input="() => addTableLine(rowIndex, columnConfig.addLineDisabled)"
        >
          <template v-if="props.showQuickCopy" #suffix>
            <MsIcon
              type="icon-icon_copy_outlined"
              class="ms-params-input-suffix-icon"
              @click="copyParamsName(record[columnConfig.dataIndex as string])"
            />
          </template>
        </a-input>
      </a-popover>
    </template>
    <!-- 参数类型 -->
    <template #paramType="{ record, columnConfig, rowIndex }">
      <a-tooltip
        v-if="columnConfig.hasRequired"
        :content="t(record.required ? 'apiTestDebug.paramRequired' : 'apiTestDebug.paramNotRequired')"
      >
        <div
          class="ms-form-table-required-button"
          :class="[
            record.required ? 'ms-form-table-required-button--required' : '',
            props.disabledExceptParam || props.disabledParamValue ? 'ms-form-table-required-button--disabled' : '',
          ]"
          @click="toggleRequired(record, rowIndex)"
        >
          <article>*</article>
        </div>
      </a-tooltip>
      <a-select
        v-model:model-value="record.paramType"
        :disabled="props.disabledExceptParam"
        :options="columnConfig.options || []"
        class="ms-form-table-input w-full"
        @change="(val) => handleTypeChange(val, record, rowIndex, columnConfig.addLineDisabled)"
      />
    </template>
    <!-- 提取类型 -->
    <template #extractType="{ record, columnConfig, rowIndex }">
      <a-select
        v-model:model-value="record.extractType"
        :disabled="props.disabledExceptParam"
        :options="columnConfig.options || []"
        class="ms-form-table-input w-[110px]"
        @change="() => addTableLine(rowIndex)"
      />
    </template>
    <!-- 变量类型 -->
    <template #variableType="{ record, columnConfig, rowIndex }">
      <a-select
        v-model:model-value="record.variableType"
        :disabled="props.disabledExceptParam"
        :options="columnConfig.options || []"
        class="ms-form-table-input w-[110px]"
        @change="() => addTableLine(rowIndex)"
      />
    </template>
    <!-- 提取范围 -->
    <template #extractScope="{ record, columnConfig, rowIndex }">
      <a-select
        v-model:model-value="record.extractScope"
        :disabled="props.disabledExceptParam || record.extractType !== RequestExtractExpressionEnum.REGEX"
        :options="columnConfig.options || []"
        class="ms-form-table-input w-[180px]"
        @change="() => addTableLine(rowIndex)"
      />
    </template>
    <!-- 表达式 -->
    <template #expression="{ record, rowIndex, columnConfig }">
      <slot name="expression" :record="record" :row-index="rowIndex" :column-config="columnConfig"></slot>
    </template>
    <!-- 作用域 -->
    <template #scope="{ record, columnConfig, rowIndex }">
      <a-select
        v-model:model-value="record.scope"
        :disabled="props.disabledExceptParam"
        :options="columnConfig.options || []"
        class="ms-form-table-input w-[180px]"
        @change="(val) => handleScopeChange(val, record, rowIndex, columnConfig.addLineDisabled)"
      />
    </template>
    <!-- 参数值 -->
    <template #value="{ record, columnConfig, rowIndex }">
      <a-popover
        v-if="columnConfig.isNormal"
        position="tl"
        :disabled="!record.value || record.value.trim() === ''"
        class="ms-params-input-popover"
      >
        <template #content>
          <div class="ms-params-popover-title">
            {{ t('apiTestDebug.paramValue') }}
          </div>
          <div class="ms-params-popover-value">
            {{ record.value }}
          </div>
        </template>
        <a-input
          v-model:model-value="record.value"
          :disabled="props.disabledParamValue"
          class="ms-form-table-input"
          :placeholder="t('apiTestDebug.commonPlaceholder')"
          @input="() => addTableLine(rowIndex)"
        />
      </a-popover>
      <MsAddAttachment
        v-else-if="record.paramType === RequestParamsType.FILE"
        v-model:file-list="record.files"
        :disabled="props.disabledParamValue"
        mode="input"
        :multiple="true"
        :fields="{
          id: 'fileId',
          name: 'fileAlias',
        }"
        :accept="columnConfig.accept"
        :file-save-as-source-id="props.fileSaveAsSourceId"
        :file-save-as-api="props.fileSaveAsApi"
        :file-module-options-api="props.fileModuleOptionsApi"
        input-class="ms-form-table-input h-[32px]"
        @change="(files, file) => handleFilesChange(files, record, rowIndex, file)"
        @delete-file="() => emitChange('deleteFile')"
      />
      <MsParamsInput
        v-else
        v-model:value="record.value"
        :disabled="props.disabledParamValue"
        @change="() => addTableLine(rowIndex, columnConfig.addLineDisabled)"
        @apply="() => addTableLine(rowIndex, columnConfig.addLineDisabled)"
        @set-params="() => quickInputParams(record)"
      />
    </template>
    <!-- 文件 -->
    <template #file="{ record, rowIndex, columnConfig }">
      <MsAddAttachment
        :file-list="[record.file]"
        :disabled="props.disabledParamValue"
        :multiple="false"
        mode="input"
        :fields="{
          id: 'fileId',
          name: 'fileAlias',
        }"
        :accept="columnConfig.accept"
        :file-save-as-source-id="props.fileSaveAsSourceId"
        :file-save-as-api="props.fileSaveAsApi"
        :file-module-options-api="props.fileModuleOptionsApi"
        input-class="ms-form-table-input h-[32px]"
        @change="(files, file) => handleFileChange(files, record, rowIndex, file)"
      />
    </template>
    <!-- 长度范围 -->
    <template #lengthRange="{ record, rowIndex }">
      <div class="flex items-center justify-between">
        <a-input-number
          v-model:model-value="record.minLength"
          :disabled="props.disabledExceptParam"
          :placeholder="t('apiTestDebug.paramMin')"
          :min="0"
          class="ms-form-table-input ms-form-table-input-number"
          model-event="input"
          @change="() => addTableLine(rowIndex)"
        />
        <div class="mx-[4px] flex-1 whitespace-nowrap">{{ t('common.to') }}</div>
        <a-input-number
          v-model:model-value="record.maxLength"
          :disabled="props.disabledExceptParam"
          :placeholder="t('apiTestDebug.paramMax')"
          :min="0"
          class="ms-form-table-input"
          model-event="input"
          @change="() => addTableLine(rowIndex)"
        />
      </div>
    </template>
    <!-- 标签 -->
    <template #tag="{ record, columnConfig, rowIndex }">
      <MsTagsInput
        v-model:model-value="record[columnConfig.dataIndex as string]"
        :disabled="props.disabledExceptParam"
        :max-tag-count="2"
        input-class="ms-form-table-input"
        @change="() => addTableLine(rowIndex)"
        @clear="() => addTableLine(rowIndex)"
      />
    </template>
    <!-- 描述 -->
    <template #description="{ record, columnConfig, rowIndex }">
      <paramDescInput
        v-model:desc="record[columnConfig.dataIndex as string]"
        :disabled="props.disabledExceptParam || columnConfig.disabledColumn"
        @input="() => addTableLine(rowIndex)"
        @dblclick="() => quickInputDesc(record)"
        @change="handleDescChange"
      />
    </template>
    <!-- 编码 -->
    <template #encode="{ record, rowIndex }">
      <a-switch
        v-model:model-value="record.encode"
        :disabled="props.disabledExceptParam"
        size="small"
        class="ms-form-table-input-switch"
        type="line"
        @change="() => addTableLine(rowIndex)"
      />
    </template>
    <!-- 必须包含 -->
    <template #mustContain="{ record, columnConfig }">
      <a-checkbox
        v-model:model-value="record[columnConfig.dataIndex as string]"
        :disabled="props.disabledExceptParam || columnConfig.disabledColumn"
        @change="handleMustContainColChange(false)"
      />
    </template>
    <!-- 类型检查 -->
    <template #typeChecking="{ record, columnConfig }">
      <a-checkbox
        v-model:model-value="record[columnConfig.dataIndex as string]"
        :disabled="props.disabledExceptParam"
        @change="handleTypeCheckingColChange(false)"
      />
    </template>
    <!-- 响应头 -->
    <template #header="{ record, columnConfig }">
      <a-select v-model="record.header" :disabled="props.disabledExceptParam" class="ms-form-table-input">
        <a-option v-for="item in columnConfig.options" :key="item.value">{{ t(item.label) }}</a-option>
      </a-select>
    </template>
    <!-- 匹配条件 -->
    <template #condition="{ record, columnConfig, rowIndex }">
      <a-select
        v-model="record.condition"
        :disabled="props.disabledExceptParam"
        class="ms-form-table-input"
        @change="() => addTableLine(rowIndex, columnConfig.addLineDisabled)"
      >
        <a-option v-for="item in columnConfig.options" :key="item.value" :value="item.value">
          {{ t(item.label) }}
        </a-option>
      </a-select>
    </template>
    <!-- 匹配值 -->
    <!-- <template #expectedTitle="{ columnConfig }">
      <div class="flex items-center text-[var(--color-text-3)]">
        {{ t('apiTestDebug.paramType') }}
        <a-tooltip :content="columnConfig.typeTitleTooltip" position="right">
          <icon-question-circle
            class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
            size="16"
          />
        </a-tooltip>
      </div>
    </template> -->
    <template #expectedValue="{ record, rowIndex, columnConfig }">
      <a-tooltip
        v-if="columnConfig.hasRequired"
        :content="t(record.required ? 'apiTestDebug.paramRequired' : 'apiTestDebug.paramNotRequired')"
      >
        <div
          class="ms-form-table-required-button"
          :class="[
            record.required ? 'ms-form-table-required-button--required' : '',
            props.disabledExceptParam || props.disabledParamValue ? 'ms-form-table-required-button--disabled' : '',
          ]"
          @click="toggleRequired(record, rowIndex)"
        >
          <article>*</article>
        </div>
      </a-tooltip>
      <a-input
        v-model="record.expectedValue"
        class="ms-form-table-input"
        :placeholder="t('apiTestDebug.commonPlaceholder')"
        :disabled="isDisabledCondition.includes(record.condition) || props.disabledExceptParam"
        @change="() => addTableLine(rowIndex, columnConfig.addLineDisabled)"
      />
    </template>
    <!-- 项目选择 -->
    <template #project="{ record, rowIndex }">
      <a-select
        v-model:model-value="record.projectId"
        :disabled="props.disabledExceptParam"
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
    <!-- 环境选择 -->
    <template #environment="{ record }">
      <MsSelect
        v-if="record.projectId"
        v-model:model-value="record.environmentId"
        v-model:input-value="record.environmentInput"
        :disabled="props.disabledExceptParam || !record.projectId"
        :options="[]"
        mode="remote"
        value-key="id"
        label-key="name"
        :search-keys="['name']"
        allow-search
        class="ms-form-table-input"
        :remote-func="initEnvOptions"
        :remote-extra-params="{ projectId: record.projectId, keyword: record.environmentInput }"
        @change-object="(val) => handleEnvironment(val, record)"
      />
      <span v-else></span>
    </template>
    <!-- 域名 -->
    <template #host="{ record }">
      <MsTagsGroup
        v-if="Array.isArray(record.domain)"
        :tag-list="getDomain(record.domain)"
        @click="() => showHostModal(record)"
      />
      <div v-else class="text-[var(--color-text-1)]">{{ '-' }}</div>
    </template>
    <!-- 单独启用/禁用列 -->
    <template v-if="!props.selectable" #enable="{ record, rowIndex }">
      <a-switch
        v-model:model-value="record.enable"
        :disabled="props.disabledExceptParam"
        size="small"
        type="line"
        class="ml-[8px]"
        :before-change="
          (newValue) => (props.enableChangeIntercept ? props.enableChangeIntercept(record, newValue) : undefined)
        "
        @change="() => addTableLine(rowIndex)"
      />
    </template>
    <!-- 操作 -->
    <template v-if="!props.disabledExceptParam" #operation="{ record, rowIndex, columnConfig }">
      <div class="flex w-full flex-row items-center" :class="{ 'justify-end': columnConfig.align === 'right' }">
        <a-switch
          v-if="columnConfig.hasDisable"
          v-model:model-value="record.enable"
          :disabled="props.disabledExceptParam"
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
                :disabled="props.disabledExceptParam"
                :options="Object.values(RequestContentTypeEnum).map((e) => ({ label: e, value: e }))"
                allow-create
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
          <div class="text-[var(--color-text-4)]">
            {{ t('apiTestDebug.quickInputParamsTip') }}
          </div>
        </div>
      </template>
    </MsCodeEditor>
  </a-modal>
  <a-modal
    v-model:visible="showQuickInputDesc"
    :title="t('common.desc')"
    :ok-text="t('common.save')"
    :ok-button-props="{ disabled: !quickInputDescValue || quickInputDescValue.trim() === '' }"
    class="ms-modal-form"
    body-class="!p-0"
    :width="480"
    title-align="start"
    @ok="applyQuickInputDesc"
    @close="clearQuickInputDesc"
  >
    <a-textarea
      v-model:model-value="quickInputDescValue"
      :placeholder="t('apiTestDebug.descPlaceholder')"
      :auto-size="{ minRows: 2 }"
    ></a-textarea>
  </a-modal>
  <DomainModal v-model:visible="hostVisible" :data="hostData" />
</template>

<script async setup lang="ts">
  import { useClipboard } from '@vueuse/core';
  import { Message, TableColumnData, TableData } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import { NO_CHECK } from '@/components/pure/ms-advance-filter/index';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import MsFormTable, { FormTableColumn } from '@/components/pure/ms-form-table/index.vue';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTagsGroup from '@/components/pure/ms-tag/ms-tag-group.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import { MsFileItem, UploadType } from '@/components/pure/ms-upload/types';
  import MsSelect from '@/components/business/ms-select/index';
  import paramDescInput from './paramDescInput.vue';
  import DomainModal from '@/views/project-management/environmental/components/envParams/popUp/domain.vue';

  import { groupCategoryEnvList, groupProjectEnv } from '@/api/modules/project-management/envManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { getGenerateId } from '@/utils';

  import { ModuleTreeNode, TransferFileParams } from '@/models/common';
  import { HttpForm, ProjectOptionItem } from '@/models/projectManagement/environmental';
  import {
    RequestBodyFormat,
    RequestContentTypeEnum,
    RequestExtractExpressionEnum,
    RequestParamsType,
  } from '@/enums/apiEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import { filterKeyValParams } from './utils';
  import { TableOperationColumn } from '@arco-design/web-vue/es/table/interface';
  // 异步加载组件
  const MsAddAttachment = defineAsyncComponent(() => import('@/components/business/ms-add-attachment/index.vue'));
  const MsParamsInput = defineAsyncComponent(() => import('@/components/business/ms-params-input/index.vue'));
  const { copy, isSupported } = useClipboard({ legacy: true });

  export interface ParamTableColumn extends FormTableColumn {
    isAutoComplete?: boolean; // 用于 key 列区分是否是请求/响应头联想输入
    isNormal?: boolean; // 用于 value 列区分是普通输入框还是 MsParamsInput
    hasRequired?: boolean; // 用于 type 列区分是否有 required 星号
    options?: { label: string; value: string }[]; // 用于 type 列选择器选项
    typeTitleTooltip?: string | string[]; // 用于 type 表头列展示的 tooltip
    hasDisable?: boolean; // 用于 operation 列区分是否有 enable 开关
    moreAction?: ActionsItem[]; // 用于 operation 列更多操作按钮配置
    format?: RequestBodyFormat; // 用于 operation 列区分是否有请求体格式选择器
    addLineDisabled?: boolean; // 用于 是否禁用添加新行
    disabledColumn?: boolean; // 用于禁用某一列不能编辑
    accept?: UploadType; // 用于文件上传列的 accept 属性
  }

  const props = withDefaults(
    defineProps<{
      params?: Record<string, any>[];
      defaultParamItem: Record<string, any>; // 默认参数项，用于添加新行时的默认值
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
      disabledParamValue?: boolean; // 参数值禁用
      disabledExceptParam?: boolean; // 除了可以修改参数值其他都禁用
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
      uploadTempFileApi?: (...args: any) => Promise<any>; // 上传临时文件接口
      fileSaveAsSourceId?: string | number; // 文件转存关联的资源id
      fileSaveAsApi?: (params: TransferFileParams) => Promise<string>; // 文件转存接口
      fileModuleOptionsApi?: (projectId: string) => Promise<ModuleTreeNode[]>; // 文件转存目录下拉框接口
      deleteIntercept?: (record: any, deleteCall: () => void) => void; // 删除行拦截器
      typeChangeIntercept?: (record: any, doChange: () => void) => void; // type 列切换拦截
      enableChangeIntercept?: (record: any, val: string | number | boolean) => boolean | Promise<boolean>; // enable 列切换拦截
      showQuickCopy?: boolean; // 显示快捷复制icon
    }>(),
    {
      params: () => [],
      selectable: true,
      showSetting: false,
      tableKey: undefined,
      isSimpleSetting: true,
    }
  );
  const emit = defineEmits<{
    (e: 'change', data: Record<string, any>[], isInit?: boolean): void; // 都触发这个事件以通知父组件参数数组被更改
    (e: 'moreActionSelect', event: ActionsItem, record: Record<string, any>): void;
    (e: 'projectChange', projectId: string): void;
    (e: 'treeDelete', record: Record<string, any>): void;
    (e: 'batchAdd'): void;
    (e: 'deleteFile', record: Record<string, any>): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  const paramsData = ref<Record<string, any>[]>([]);

  function emitChange(from: string, isInit?: boolean) {
    emit('change', paramsData.value, isInit);
  }

  const paramsLength = computed(() => paramsData.value.length);

  function deleteParam(record: Record<string, any>, rowIndex: number) {
    if (props.disabledExceptParam) return;
    if (props.isTreeTable) {
      emit('treeDelete', record);
      return;
    }
    if (props.deleteIntercept) {
      props.deleteIntercept(record, () => {
        paramsData.value.splice(rowIndex, 1);
        emitChange('deleteParam');
      });
    } else {
      paramsData.value.splice(rowIndex, 1);
      emitChange('deleteParam');
    }
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
  //     title: t('common.desc'),
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

  const defaultLineData = ref(cloneDeep(props.defaultParamItem));
  /**
   * 当表格输入框变化时，给参数表格添加一行数据行
   * @param val 输入值
   * @param key 当前列的 key  key存在的时候 才可以增加新的一行
   * @param isForce 是否强制添加
   */
  function addTableLine(rowIndex: number, addLineDisabled?: boolean, isInit?: boolean) {
    if (props.disabledExceptParam || props.disabledParamValue) {
      // 禁用状态下不允许添加新行
      return;
    }
    if (addLineDisabled) {
      emitChange('addTableLine addLineDisabled', isInit);
      return;
    }
    if (rowIndex === paramsData.value.length - 1) {
      // Don't change this!!!
      // 最后一行的更改才会触发添加新一行
      const id = getGenerateId();
      const lastLineData = paramsData.value[rowIndex]; // 上一行数据
      const selectColumnKeys = props.columns.filter((e) => e.options).map((e) => e.dataIndex); // 找到下拉框选项的列
      const nextLine = {
        ...cloneDeep(defaultLineData.value), // 深拷贝，避免有嵌套引用类型，数据隔离
        id,
        enable: true, // 是否勾选
      } as any;
      selectColumnKeys.forEach((key) => {
        // 如果是更改了下拉框导致添加新的一列，需要将更改后的下拉框的值应用到下一行（产品为了方便统一输入参数类型）
        if (key) {
          nextLine[key] = lastLineData[key];
          // 根据参数类型自动推断 Content-Type 类型！！！特殊处理，这里contentType会随前面的参数类型变化，这里也需要变化
          if (nextLine.contentType) {
            if (lastLineData[key] === 'file') {
              nextLine.contentType = RequestContentTypeEnum.OCTET_STREAM;
            } else if (lastLineData[key] === 'json') {
              nextLine.contentType = RequestContentTypeEnum.JSON;
            } else {
              nextLine.contentType = RequestContentTypeEnum.TEXT;
            }
          }
        }
      });
      paramsData.value.push(nextLine);
      defaultLineData.value = cloneDeep(nextLine);
    }
    emitChange('addTableLine', isInit);
    handleMustContainColChange(true);
    handleTypeCheckingColChange(true);
  }

  watch(
    () => props.params,
    (arr) => {
      if (arr.length > 0) {
        let hasNoIdItem = false;
        paramsData.value = arr.map((item) => {
          if (!item) {
            // 批量添加过来的数据最后一行会是 undefined
            hasNoIdItem = true;
            return {
              ...cloneDeep(defaultLineData.value),
              id: getGenerateId(),
            };
          }
          if (!item.id) {
            // 后台存储无id，渲染时需要手动添加一次
            hasNoIdItem = true;
            return {
              ...item,
              id: getGenerateId(),
            };
          }
          return item;
        });
        const { lastDataIsDefault } = filterKeyValParams(arr, defaultLineData.value, false);
        if (
          (hasNoIdItem && !lastDataIsDefault) ||
          (!props.disabledExceptParam && !props.disabledParamValue && !lastDataIsDefault && !props.isTreeTable)
        ) {
          addTableLine(arr.length - 1, false, true);
        }
      } else {
        if (props.disabledExceptParam || props.disabledParamValue) return;
        const id = getGenerateId();
        paramsData.value = [
          {
            id, // 默认给时间戳 id，若 props.defaultParamItem 有 id，则覆盖
            enable: true, // 是否勾选
            ...cloneDeep(defaultLineData.value),
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
    if (props.disabledExceptParam || props.disabledParamValue) {
      return;
    }
    record.required = !record.required;
    addTableLine(rowIndex);
    emitChange('toggleRequired');
  }

  /**
   * 处理表格内多个文件上传/关联
   */
  async function handleFilesChange(
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
              fileAlias: file.name || '',
            };
            break;
          }
        }
      } else {
        // 关联文件可选多个文件
        record.files = files.map((e) => ({
          ...e,
          fileId: e.uid || e.fileId || '',
          fileName: e.originalName || e.name || e.fileName || '',
          fileAlias: e.name || e.fileName || '',
        }));
      }
      addTableLine(rowIndex);
      emitChange('handleFilesChange');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      appStore.hideLoading();
    }
  }

  /**
   * 处理表格内单个文件上传/关联
   */
  async function handleFileChange(
    files: MsFileItem[],
    record: Record<string, any>,
    rowIndex: number,
    file?: MsFileItem
  ) {
    try {
      if (props.uploadTempFileApi && file?.local) {
        appStore.showLoading();
        const res = await props.uploadTempFileApi(file.file);
        record.file = {
          ...file,
          fileId: res.data,
          fileName: file.name || '',
          fileAlias: file.name || '',
        };
      } else if (files[0]) {
        record.file = {
          ...files[0],
          fileId: files[0].uid || files[0].fileId || '',
          fileName: files[0].originalName || files[0].fileName || '',
          fileAlias: files[0].name || '',
        };
      } else {
        paramsData.value.forEach((e) => {
          if (e.id === record.id) {
            e.file = {
              fileId: '',
              fileName: '',
              fileAlias: '',
              local: false,
              delete: false,
            };
          }
        });
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

  function handleScopeChange(
    val: string | number | boolean | Record<string, any> | (string | number | boolean | Record<string, any>)[],
    record: Record<string, any>,
    rowIndex: number,
    addLineDisabled?: boolean
  ) {
    if (props.typeChangeIntercept) {
      props.typeChangeIntercept(record, () => {
        addTableLine(rowIndex, addLineDisabled);
        emitChange('handleScopeChange');
      });
    } else {
      addTableLine(rowIndex, addLineDisabled);
      emitChange('handleScopeChange');
    }
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

  /**
   * 搜索变量
   * @param val 变量名
   */
  function handleSearchParams(val: string, item: FormTableColumn) {
    item.autoCompleteParams = item.autoCompleteParams?.map((e) => {
      e.isShow = (e.label || '').toLowerCase().includes(val.toLowerCase());
      return e;
    });
  }

  const activeRecord = ref<Record<string, any>>({});
  function getAutoCompleteData(columnConfig: ParamTableColumn, record: Record<string, any>) {
    if (activeRecord.value.id !== record.id) {
      // 非聚焦行，不显示联想输入
      return [];
    }
    return activeRecord.value[columnConfig.dataIndex as string] !== ''
      ? columnConfig.autoCompleteParams?.filter((e) => e.isShow === true)
      : columnConfig.autoCompleteParams;
  }

  function handleAutoCompleteFocus(record: Record<string, any>) {
    activeRecord.value = record;
  }

  function selectAutoComplete(val: string, record: Record<string, any>, item: FormTableColumn) {
    record[item.dataIndex as string] = val;
  }

  function copyParamsName(value: string) {
    const copyValue = `\${${value}}`;
    if (isSupported) {
      copy(copyValue);
      Message.info(t('common.copySuccessToClipboard'));
    }
  }

  const isDisabledCondition = ref([NO_CHECK.value]);

  defineExpose({
    addTableLine,
  });
</script>

<style lang="less" scoped>
  .content-type-trigger-content {
    padding: 8px;
    border-radius: var(--border-radius-small);
    background-color: var(--color-text-fff);
    box-shadow: 0 4px 10px -1px rgb(100 100 102 / 15%);
  }
  :deep(.ms-form-table-input-number) {
    .arco-input {
      @apply text-right;
    }
  }
</style>
