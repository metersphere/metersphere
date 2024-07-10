<template>
  <MsFormTable
    v-model:originalSelectedKeys="selectedKeys"
    v-model:expanded-keys="expandKeys"
    :data="data"
    :columns="columns"
    show-empty-tree
    :selectable="false"
    :row-selection="{
      type: 'checkbox',
      showCheckedAll: true,
      checkStrictly: true,
      width: 32,
    }"
    :table-key="TableKeyEnum.JSON_SCHEMA"
    :scroll="{ x: 'max-content' }"
    :disabled="props.disabled"
    show-setting
    class="ms-json-schema"
    @row-select="handleSelect"
    @select-all="handleSelectAll"
  >
    <template #batchAddTitle>
      <MsButton v-if="!props.disabled" type="text" size="mini" class="!mr-0" @click="batchAdd">
        {{ t('apiTestDebug.batchAdd') }}
      </MsButton>
    </template>
    <template #title="{ record, columnConfig, rowIndex }">
      <span v-if="record.id === 'root'" class="px-[8px]">root</span>
      <span v-else-if="record.parent?.type === 'array'" class="px-[8px]">{{ rowIndex }}</span>
      <a-popover
        v-else
        position="tl"
        :disabled="!record.title || record.title.trim() === ''"
        class="ms-params-input-popover"
      >
        <template #content>
          <div class="ms-params-popover-title">
            {{ t('ms.json.schema.name') }}
          </div>
          <div class="ms-params-popover-value">
            {{ record.title }}
          </div>
        </template>
        <a-input
          v-model:model-value="record.title"
          :placeholder="t(columnConfig.locale || 'common.pleaseInput')"
          class="ms-form-table-input ms-form-table-input--hasPlaceholder"
          :max-length="255"
          size="medium"
          @input="addLineIfLast(record, rowIndex)"
        />
      </a-popover>
    </template>
    <template #type="{ record }">
      <a-tooltip
        v-if="record.id !== 'root'"
        :content="t(record.required ? 'apiTestDebug.paramRequired' : 'apiTestDebug.paramNotRequired')"
      >
        <MsButton
          type="icon"
          class="!mr-[4px] !p-[4px]"
          size="mini"
          :disabled="props.disabled"
          @click="() => (record.required = !record.required)"
        >
          <div
            :style="{
              color: record.required ? 'rgb(var(--danger-5)) !important' : 'var(--color-text-brand) !important',
            }"
          >
            *
          </div>
        </MsButton>
      </a-tooltip>
      <a-select
        v-model:model-value="record.type"
        :options="getTypeOptions(record)"
        class="ms-form-table-input w-full"
        size="medium"
        :disabled="props.disabled"
        @change="handleTypeChange(record)"
      />
    </template>
    <template #example="{ record }">
      <div v-if="['object', 'array', 'null'].includes(record.type)" class="ms-form-table-td-text">-</div>
      <MsParamsInput
        v-else
        v-model:value="record.example"
        size="medium"
        :disabled="props.disabled"
        @dblclick="() => quickInputParams(record)"
      />
    </template>
    <template #minLength="{ record }">
      <a-input-number
        v-if="record.type === 'string'"
        v-model:model-value="record.minLength"
        class="ms-form-table-input-number"
        :min="0"
        :precision="0"
        size="medium"
        :disabled="props.disabled"
      />
      <div v-else class="ms-form-table-td-text">-</div>
    </template>
    <template #maxLength="{ record }">
      <a-input-number
        v-if="record.type === 'string'"
        v-model:model-value="record.maxLength"
        class="ms-form-table-input-number"
        :min="0"
        :precision="0"
        size="medium"
        :disabled="props.disabled"
      />
      <div v-else class="ms-form-table-td-text">-</div>
    </template>
    <template #minimum="{ record }">
      <a-input-number
        v-if="record.type === 'number'"
        v-model:model-value="record.minimum"
        class="ms-form-table-input-number"
        size="medium"
        :disabled="props.disabled"
      />
      <a-input-number
        v-else-if="record.type === 'integer'"
        v-model:model-value="record.minimum"
        class="ms-form-table-input-number"
        size="medium"
        :step="1"
        :precision="0"
        :disabled="props.disabled"
      />
      <div v-else class="ms-form-table-td-text">-</div>
    </template>
    <template #maximum="{ record }">
      <a-input-number
        v-if="record.type === 'number'"
        v-model:model-value="record.maximum"
        class="ms-form-table-input-number"
        size="medium"
        :disabled="props.disabled"
      />
      <a-input-number
        v-else-if="record.type === 'integer'"
        v-model:model-value="record.maximum"
        class="ms-form-table-input-number"
        size="medium"
        :step="1"
        :precision="0"
        :disabled="props.disabled"
      />
      <div v-else class="ms-form-table-td-text">-</div>
    </template>
    <template #minItems="{ record }">
      <a-input-number
        v-if="record.type === 'array'"
        v-model:model-value="record.minItems"
        class="ms-form-table-input-number"
        size="medium"
        :min="0"
        :step="1"
        :precision="0"
        :disabled="props.disabled"
      />
      <div v-else class="ms-form-table-td-text">-</div>
    </template>
    <template #maxItems="{ record }">
      <a-input-number
        v-if="record.type === 'array'"
        v-model:model-value="record.maxItems"
        class="ms-form-table-input-number"
        size="medium"
        :min="0"
        :step="1"
        :precision="0"
        :disabled="props.disabled"
      />
      <div v-else class="ms-form-table-td-text">-</div>
    </template>
    <template #defaultValue="{ record }">
      <a-input-number
        v-if="record.type === 'number'"
        v-model:model-value="record.defaultValue"
        class="ms-form-table-input-number"
        size="medium"
        :disabled="props.disabled"
      />
      <a-input-number
        v-else-if="record.type === 'integer'"
        v-model:model-value="record.defaultValue"
        class="ms-form-table-input-number"
        size="medium"
        :step="1"
        :precision="0"
        :disabled="props.disabled"
      />
      <a-select
        v-else-if="record.type === 'boolean'"
        v-model:model-value="record.defaultValue"
        class="ms-form-table-input"
        size="medium"
        :options="[
          {
            label: 'true',
            value: true,
          },
          {
            label: 'false',
            value: false,
          },
        ]"
        :disabled="props.disabled"
      />
      <div v-else-if="['object', 'array', 'null'].includes(record.type)" class="ms-form-table-td-text"> - </div>
      <a-input
        v-else
        v-model:model-value="record.defaultValue"
        :placeholder="t('common.pleaseInput')"
        class="ms-form-table-input"
        :disabled="props.disabled"
      />
    </template>
    <template #enumValues="{ record }">
      <div v-if="['object', 'array', 'null', 'boolean'].includes(record.type)" class="ms-form-table-td-text">-</div>
      <MsQuickInput
        v-else
        v-model:model-value="record.enumValues"
        :title="t('ms.json.schema.enum')"
        :popover-title="record.enumValues ? JSON.stringify(record.enumValues.split('\n')) : ''"
        class="ms-form-table-input"
        type="textarea"
        :disabled="props.disabled"
      >
      </MsQuickInput>
    </template>
    <template #action="{ record, rowIndex }">
      <div class="flex w-full items-center gap-[8px]">
        <a-tooltip :content="t('common.advancedSettings')">
          <MsButton type="icon" class="ms-json-schema-icon-button" @click="openSetting(record)">
            <icon-settings class="text-[var(--color-text-4)]" />
          </MsButton>
        </a-tooltip>
        <a-tooltip
          v-if="['object', 'array'].includes(record.type) && !props.disabled"
          :content="t('ms.json.schema.addChild')"
        >
          <MsButton type="icon" class="ms-json-schema-icon-button" @click="addChild(record)">
            <MsIcon type="icon-icon_add_outlined" class="text-[var(--color-text-4)]" />
          </MsButton>
        </a-tooltip>
        <a-tooltip v-if="record.id !== 'root' && !props.disabled" :content="t('common.delete')">
          <MsButton type="icon" class="ms-json-schema-icon-button" @click="deleteLine(record, rowIndex)">
            <icon-minus-circle class="cursor-pointer text-[var(--color-text-4)]" size="18" />
          </MsButton>
        </a-tooltip>
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
  <MsDrawer
    v-model:visible="settingDrawerVisible"
    :width="600"
    :title="t('ms.json.schema.advancedSettings')"
    :ok-text="t('common.save')"
    @confirm="applySetting"
  >
    <a-form ref="setting" :model="activeRecord" :disabled="props.disabled" layout="vertical">
      <a-form-item
        :label="t('ms.json.schema.name')"
        :rules="[{ required: true, message: t('ms.json.schema.nameNotNull') }]"
        asterisk-position="end"
      >
        <a-input
          v-model:model-value="activeRecord.title"
          :max-length="255"
          :placeholder="t('common.pleaseInput')"
          :disabled="activeRecord.id === 'root'"
          @change="handleSettingFormChange"
        />
      </a-form-item>
      <a-form-item :label="t('common.desc')">
        <a-textarea
          v-model:model-value="activeRecord.description"
          :placeholder="t('common.pleaseInput')"
          @change="handleSettingFormChange"
        />
      </a-form-item>
      <template v-if="!['object', 'array', 'null'].includes(activeRecord.type)">
        <div class="flex items-center justify-between gap-[24px]">
          <template v-if="!['boolean', 'number', 'integer'].includes(activeRecord.type)">
            <a-form-item :label="t('ms.json.schema.minLength')" class="w-[144px]">
              <a-input-number
                v-model:model-value="activeRecord.minLength"
                mode="button"
                :min="0"
                :step="1"
                :precision="0"
                @change="handleSettingFormChange"
              />
            </a-form-item>
            <a-form-item :label="t('ms.json.schema.maxLength')" class="w-[144px]">
              <a-input-number
                v-model:model-value="activeRecord.maxLength"
                mode="button"
                :min="0"
                :step="1"
                :precision="0"
                @change="handleSettingFormChange"
              />
            </a-form-item>
          </template>
          <template v-else-if="['number', 'integer'].includes(activeRecord.type)">
            <a-form-item :label="t('ms.json.schema.minimum')" class="w-[144px]">
              <a-input-number
                v-if="activeRecord.type === 'integer'"
                v-model:model-value="activeRecord.minimum"
                mode="button"
                :step="1"
                :precision="0"
                @change="handleSettingFormChange"
              />
              <a-input-number
                v-else
                v-model:model-value="activeRecord.minimum"
                mode="button"
                @change="handleSettingFormChange"
              />
            </a-form-item>
            <a-form-item :label="t('ms.json.schema.maximum')" class="w-[144px]">
              <a-input-number
                v-if="activeRecord.type === 'integer'"
                v-model:model-value="activeRecord.maximum"
                mode="button"
                :step="1"
                :precision="0"
                @change="handleSettingFormChange"
              />
              <a-input-number
                v-else
                v-model:model-value="activeRecord.maximum"
                mode="button"
                @change="handleSettingFormChange"
              />
            </a-form-item>
          </template>
          <a-form-item :label="t('ms.json.schema.default')" class="flex-1">
            <a-input-number
              v-if="activeRecord.type === 'number'"
              v-model:model-value="activeRecord.defaultValue"
              mode="button"
              :placeholder="t('common.pleaseInput')"
              @change="handleSettingFormChange"
            />
            <a-input-number
              v-else-if="activeRecord.type === 'integer'"
              v-model:model-value="activeRecord.defaultValue"
              mode="button"
              :placeholder="t('common.pleaseInput')"
              :step="1"
              :precision="0"
              @change="handleSettingFormChange"
            />
            <a-input
              v-else
              v-model:model-value="activeRecord.defaultValue"
              :placeholder="t('common.pleaseInput')"
              @change="handleSettingFormChange"
            />
          </a-form-item>
        </div>
        <template v-if="activeRecord.type !== 'boolean'">
          <a-form-item :label="t('ms.json.schema.enum')">
            <a-textarea
              v-model:model-value="activeRecord.enumValues"
              :placeholder="t('ms.json.schema.enumPlaceholder')"
              @change="handleSettingFormChange"
            />
          </a-form-item>
          <a-form-item :label="t('ms.json.schema.regex')">
            <a-input
              v-model:model-value="activeRecord.regex"
              :placeholder="t('ms.json.schema.regexPlaceholder', { reg: '/<title(.*?)</title>' })"
              @change="handleSettingFormChange"
            />
          </a-form-item>
          <a-form-item :label="t('ms.json.schema.format')">
            <a-select
              v-model:model-value="activeRecord.format"
              :placeholder="t('common.pleaseSelect')"
              :options="formatOptions"
              @change="handleSettingFormChange"
            />
          </a-form-item>
        </template>
      </template>
      <div v-if="activeRecord.type === 'array'" class="flex items-center gap-[24px]">
        <a-form-item :label="t('ms.json.schema.minItems')" class="w-[144px]">
          <a-input-number
            v-model:model-value="activeRecord.minItems"
            mode="button"
            :min="0"
            :step="1"
            :precision="0"
            @change="handleSettingFormChange"
          />
        </a-form-item>
        <a-form-item :label="t('ms.json.schema.maxItems')" class="w-[144px]">
          <a-input-number
            v-model:model-value="activeRecord.maxItems"
            mode="button"
            :min="0"
            :step="1"
            :precision="0"
            @change="handleSettingFormChange"
          />
        </a-form-item>
      </div>
      <div>
        <div class="mb-[8px]">{{ t('ms.json.schema.preview') }}</div>
        <MsCodeEditor
          v-model:model-value="activePreviewJsonSchemaValue"
          theme="vs"
          height="500px"
          :show-full-screen="false"
          :language="LanguageEnum.JSON"
          read-only
        >
        </MsCodeEditor>
      </div>
    </a-form>
  </MsDrawer>
  <MsDrawer
    v-model:visible="batchAddDrawerVisible"
    :width="600"
    :title="t('ms.json.schema.batchAdd')"
    :ok-text="t('common.add')"
    @confirm="applyBatchAdd"
  >
    <MsCodeEditor
      v-model:model-value="batchAddValue"
      theme="vs"
      height="100%"
      :language="LanguageEnum.JSON"
      :show-full-screen="false"
    >
      <template #leftTitle>
        <a-radio-group v-model:model-value="batchAddType" type="button" @change="batchAddValue = ''">
          <a-radio value="json">Json</a-radio>
          <a-radio value="schema">JsonSchema</a-radio>
        </a-radio-group>
      </template>
      <template #rightTitle>
        <div class="flex justify-between">
          <div class="text-[var(--color-text-4)]">
            {{ t('ms.json.schema.batchAddTip') }}
          </div>
        </div>
      </template>
    </MsCodeEditor>
  </MsDrawer>
  <MsDrawer
    v-model:visible="previewDrawerVisible"
    :width="600"
    :title="t('common.preview')"
    :footer="false"
    @close="previewShowType = 'json'"
  >
    <a-spin class="block h-full w-full" :loading="previewDrawerLoading">
      <MsCodeEditor
        v-model:model-value="activePreviewValue"
        theme="vs"
        height="100%"
        :language="LanguageEnum.JSON"
        :show-full-screen="false"
        read-only
      >
        <template #leftTitle>
          <a-radio-group v-model:model-value="previewShowType" type="button" @change="batchAddValue = ''">
            <a-radio value="json">Json</a-radio>
            <a-radio value="schema">JsonSchema</a-radio>
          </a-radio-group>
        </template>
      </MsCodeEditor>
    </a-spin>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { SelectOptionData, TableData } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsFormTable, { FormTableColumn } from '@/components/pure/ms-form-table/index.vue';
  import MsParamsInput from '@/components/business/ms-params-input/index.vue';
  import MsQuickInput from '@/components/business/ms-quick-input/index.vue';

  import { convertJsonSchemaToJson } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import { getGenerateId, traverseTree } from '@/utils';

  import { TableKeyEnum } from '@/enums/tableEnum';

  import { JsonSchema, JsonSchemaItem, JsonSchemaTableItem } from './types';
  import {
    parseJsonToJsonSchemaTableData,
    parseSchemaToJsonSchemaTableData,
    parseTableDataToJsonSchema,
  } from './utils';

  const props = defineProps<{
    disabled?: boolean;
  }>();

  const { t } = useI18n();

  const defaultItem: JsonSchemaTableItem = {
    id: '',
    type: 'string',
    title: '',
    example: '',
    description: '',
    enable: true,
    defaultValue: '',
    maximum: undefined,
    minimum: undefined,
    maxLength: undefined,
    minLength: undefined,
    enumValues: '',
    pattern: undefined,
    format: undefined,
    children: undefined,
  };
  const data = defineModel<JsonSchemaTableItem[]>('data', {
    default: () => [],
  });
  const expandKeys = defineModel<string[]>('expandKeys', {
    default: () => ['root'],
  });
  const selectedKeys = defineModel<string[]>('selectedKeys', {
    default: () => ['root'],
  });

  // 初始化根节点
  watchEffect(() => {
    if (data.value.length === 0) {
      data.value = [
        {
          id: 'root',
          title: 'root',
          type: 'object',
          example: '',
          description: '',
          enable: true,
          defaultValue: '',
          maximum: undefined,
          minimum: undefined,
          maxLength: undefined,
          minLength: undefined,
          enumValues: '',
          pattern: undefined,
          format: undefined,
          required: false,
          children: [],
        },
      ];
      if (selectedKeys.value.length === 0) {
        selectedKeys.value = ['root'];
      }
    }
  });

  const typeOptions: SelectOptionData[] = [
    {
      label: 'object',
      value: 'object',
    },
    {
      label: 'array',
      value: 'array',
    },
    {
      label: 'string',
      value: 'string',
    },
    {
      label: 'number',
      value: 'number',
    },
    {
      label: 'integer',
      value: 'integer',
    },
    {
      label: 'boolean',
      value: 'boolean',
    },
    {
      label: 'null',
      value: 'null',
    },
  ];
  const formatOptions: SelectOptionData[] = [
    {
      label: 'date',
      value: 'date',
    },
    {
      label: 'date-time',
      value: 'date-time',
    },
    {
      label: 'email',
      value: 'email',
    },
    {
      label: 'hostname',
      value: 'hostname',
    },
    {
      label: 'ipv4',
      value: 'ipv4',
    },
    {
      label: 'ipv6',
      value: 'ipv6',
    },
    {
      label: 'url',
      value: 'url',
    },
  ];
  const columns: FormTableColumn[] = [
    {
      title: t('ms.json.schema.name'),
      dataIndex: 'title',
      slotName: 'title',
      inputType: 'input',
      size: 'medium',
      columnSelectorDisabled: true,
      fixed: 'left',
    },
    {
      title: t('ms.json.schema.type'),
      dataIndex: 'type',
      slotName: 'type',
      size: 'medium',
      width: 120,
      addLineDisabled: true,
      columnSelectorDisabled: true,
    },
    {
      title: t('ms.json.schema.value'),
      dataIndex: 'example',
      slotName: 'example',
      addLineDisabled: true,
      columnSelectorDisabled: true,
    },
    {
      title: t('common.desc'),
      dataIndex: 'description',
      slotName: 'description',
      inputType: 'quickInput',
      size: 'medium',
      addLineDisabled: true,
    },
    {
      title: t('ms.json.schema.minLength'),
      dataIndex: 'minLength',
      slotName: 'minLength',
      inputType: 'inputNumber',
      size: 'medium',
      min: 0,
      precision: 0,
      addLineDisabled: true,
      showInTable: false,
      width: 120,
    },
    {
      title: t('ms.json.schema.maxLength'),
      dataIndex: 'maxLength',
      slotName: 'maxLength',
      inputType: 'inputNumber',
      size: 'medium',
      min: 0,
      precision: 0,
      addLineDisabled: true,
      showInTable: false,
      width: 120,
    },
    {
      title: t('ms.json.schema.minimum'),
      dataIndex: 'minimum',
      slotName: 'minimum',
      inputType: 'inputNumber',
      size: 'medium',
      addLineDisabled: true,
      showInTable: false,
      width: 120,
    },
    {
      title: t('ms.json.schema.maximum'),
      dataIndex: 'maximum',
      slotName: 'maximum',
      inputType: 'inputNumber',
      size: 'medium',
      addLineDisabled: true,
      showInTable: false,
      width: 120,
    },
    {
      title: t('ms.json.schema.minItems'),
      dataIndex: 'minItems',
      slotName: 'minItems',
      inputType: 'inputNumber',
      size: 'medium',
      min: 0,
      precision: 0,
      addLineDisabled: true,
      showInTable: false,
      width: 120,
    },
    {
      title: t('ms.json.schema.maxItems'),
      dataIndex: 'maxItems',
      slotName: 'maxItems',
      inputType: 'inputNumber',
      size: 'medium',
      min: 0,
      precision: 0,
      addLineDisabled: true,
      showInTable: false,
      width: 120,
    },
    {
      title: t('ms.json.schema.default'),
      dataIndex: 'defaultValue',
      slotName: 'defaultValue',
      inputType: 'input',
      size: 'medium',
      addLineDisabled: true,
      showInTable: false,
    },
    {
      title: t('ms.json.schema.enum'),
      dataIndex: 'enumValues',
      slotName: 'enumValues',
      inputType: 'textarea',
      size: 'medium',
      addLineDisabled: true,
      showInTable: false,
    },
    {
      title: t('ms.json.schema.regex'),
      dataIndex: 'regex',
      slotName: 'regex',
      inputType: 'input',
      size: 'medium',
      addLineDisabled: true,
      showInTable: false,
      isNull: (record) => ['object', 'array', 'null', 'boolean'].includes(record.type),
    },
    {
      title: t('ms.json.schema.format'),
      dataIndex: 'format',
      slotName: 'format',
      inputType: 'select',
      size: 'medium',
      options: formatOptions,
      addLineDisabled: true,
      showInTable: false,
      isNull: (record) => ['object', 'array', 'null', 'boolean'].includes(record.type),
    },
    {
      title: '',
      dataIndex: 'operation',
      slotName: 'action',
      titleSlotName: 'batchAddTitle',
      addLineDisabled: true,
      width: 100,
      showInTable: true,
      fixed: 'right',
    },
  ];

  /**
   * 获取类型选项，根节点只能是 object 或 array
   */
  function getTypeOptions(record: JsonSchemaTableItem) {
    if (record.id === 'root') {
      return typeOptions.filter((item) => ['object', 'array'].includes(item.value as string));
    }
    return typeOptions;
  }

  /**
   * 处理类型变化
   */
  function handleTypeChange(record: JsonSchemaTableItem) {
    if (record.type === 'object' || record.type === 'array') {
      if (!record.children) {
        // 没有子节点，初始化
        record.children = [];
      }
    } else {
      record.children = undefined;
    }
  }

  /**
   * 添加子节点
   */
  function addChild(record: JsonSchemaTableItem) {
    if (!record.children) {
      record.children = [];
    }
    const child = {
      ...cloneDeep(defaultItem),
      id: getGenerateId(),
      parent: record,
    };
    record.children.push(child);
    // 默认展开父节点
    if (!expandKeys.value.includes(record.id)) {
      expandKeys.value.push(record.id);
    }
    // 默认选中子节点
    if (!selectedKeys.value.includes(child.id)) {
      selectedKeys.value.push(child.id);
    }
  }

  /**
   * 删除行
   */
  function deleteLine(record: JsonSchemaTableItem, rowIndex: number) {
    if (record.parent?.children) {
      record.parent.children.splice(rowIndex, 1);
    } else {
      data.value.splice(rowIndex, 1);
    }
  }

  /**
   * 行选择处理
   */
  function handleSelect(rowKeys: (string | number)[], rowKey: string | number, record: TableData) {
    nextTick(() => {
      record.enable = !selectedKeys.value.includes(record.id);
      if (record.enable && record.children && record.children.length > 0) {
        // 选中父节点时，选中子孙节点
        traverseTree<JsonSchemaTableItem>(record.children, (item) => {
          item.enable = true;
          if (!selectedKeys.value.includes(item.id)) {
            selectedKeys.value.push(item.id);
          }
        });
      }
    });
  }

  function addLineIfLast(record: TableData, rowIndex: number) {
    if (rowIndex === (record.parent || data.value[0]).children.length - 1) {
      addChild(data.value[0]);
    }
  }

  function handleSelectAll(checked: boolean) {
    traverseTree<JsonSchemaTableItem>(data.value, (item) => {
      item.enable = checked;
    });
  }

  const batchAddDrawerVisible = ref(false);
  const batchAddValue = ref('');
  const batchAddType = ref<'json' | 'schema'>('json');

  function batchAdd() {
    batchAddDrawerVisible.value = true;
  }

  function applyBatchAdd() {
    try {
      let res: { result: JsonSchemaTableItem[]; ids: Array<string> } = { result: [], ids: [] };
      if (batchAddType.value === 'json') {
        res = parseJsonToJsonSchemaTableData(batchAddValue.value);
      } else {
        res = parseSchemaToJsonSchemaTableData(batchAddValue.value);
      }
      if (res.result.length > 0) {
        data.value = res.result;
        selectedKeys.value = res.ids;
      }
      batchAddDrawerVisible.value = false;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const settingDrawerVisible = ref(false);
  const activeRecord = ref<any>({});
  const previewShowType = ref<'json' | 'schema'>('json');
  const activePreviewJsonValue = ref('');
  const activePreviewJsonSchemaValue = ref('');
  const activePreviewValue = computed(() => {
    return previewShowType.value === 'json' ? activePreviewJsonValue.value : activePreviewJsonSchemaValue.value;
  });

  async function openSetting(record: JsonSchemaTableItem) {
    // 浅拷贝，以保留 parent 和 children 的引用
    activeRecord.value = {
      ...record,
    };
    try {
      const schema = parseTableDataToJsonSchema(record, record.id === 'root');
      activePreviewJsonSchemaValue.value = JSON.stringify(schema);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      activePreviewJsonSchemaValue.value = t('ms.json.schema.convertFailed');
    } finally {
      settingDrawerVisible.value = true;
    }
  }

  function handleSettingFormChange() {
    activePreviewJsonSchemaValue.value = JSON.stringify(
      parseTableDataToJsonSchema(activeRecord.value, activeRecord.value.id === 'root')
    );
  }

  /**
   * 应用设置
   */
  function applySetting() {
    if (activeRecord.value.id === 'root') {
      data.value = [{ ...activeRecord.value }];
    } else {
      const brothers = activeRecord.value.parent?.children || [];
      const index = brothers.findIndex((item: any) => item.id === activeRecord.value.id);
      if (index > -1) {
        brothers.splice(index, 1, { ...activeRecord.value });
      }
    }
    settingDrawerVisible.value = false;
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
    clearQuickInputParam();
  }

  const previewDrawerVisible = ref(false);
  const previewDrawerLoading = ref(false);

  /**
   * 预览 schema
   */
  async function previewSchema() {
    previewDrawerVisible.value = true;
    previewDrawerLoading.value = true;
    let schema: JsonSchema | JsonSchemaItem | undefined;
    try {
      // 先将表格数据转换为 json schema格式
      schema = parseTableDataToJsonSchema(data.value[0] as JsonSchemaTableItem);
      activePreviewJsonSchemaValue.value = JSON.stringify(schema);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      activePreviewJsonSchemaValue.value = t('ms.json.schema.convertFailed');
      previewDrawerLoading.value = false;
      return;
    }
    try {
      // 再将 json schema 转换为 json 格式
      const res = await convertJsonSchemaToJson(schema as JsonSchema);
      activePreviewJsonValue.value = res;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      activePreviewJsonValue.value = t('ms.json.schema.convertFailed');
    } finally {
      previewDrawerLoading.value = false;
    }
  }

  defineExpose({
    previewSchema,
  });
</script>

<style lang="less">
  .ms-json-schema {
    .ms-json-schema-icon-button {
      @apply !mr-0;
      &:hover {
        background-color: rgb(var(--primary-1)) !important;
        .arco-icon {
          color: rgb(var(--primary-4)) !important;
        }
      }
    }
  }
</style>
