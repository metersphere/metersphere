<template>
  <MsDrawer
    v-model:visible="showScriptDrawer"
    :title="t('ms.case.associate.title')"
    :width="768"
    :footer="false"
    unmount-on-close
  >
    <a-form ref="formRef" :model="form" layout="vertical">
      <a-form-item
        field="name"
        :label="t('project.commonScript.publicScriptName')"
        :rules="[{ required: true, message: t('project.commonScript.publicScriptNameNotEmpty') }]"
      >
        <a-input
          v-model="form.name"
          :max-length="255"
          show-word-limit
          :placeholder="t('project.commonScript.pleaseEnterScriptName')"
        />
      </a-form-item>
      <a-form-item field="enable" :label="t('project.commonScript.scriptEnabled')">
        <a-select class="max-w-[396px]" :placeholder="t('project.commonScript.scriptEnabled')">
          <a-option>{{ t('project.commonScript.draft') }}</a-option>
          <a-option>{{ t('project.commonScript.testsPass') }}</a-option>
        </a-select>
      </a-form-item>
      <a-form-item field="description" :label="t('system.organization.description')">
        <a-textarea
          v-model="form.description"
          :placeholder="t('system.organization.descriptionPlaceholder')"
          allow-clear
          :auto-size="{ minRows: 1 }"
        />
      </a-form-item>
      <a-form-item field="tags" :label="t('system.organization.description')">
        <a-input-tag :placeholder="t('project.commonScript.enterContentAddTags')" allow-clear />
      </a-form-item>
      <a-form-item field="inputParameters" :label="t('project.commonScript.inputParams')">
        <ms-base-table v-bind="propsRes" ref="tableRef" no-disable v-on="propsEvent"> </ms-base-table>
      </a-form-item>
      <div class="mb-2 flex items-center justify-between">
        <a-radio-group v-model:model-value="scriptType" type="button" size="small">
          <a-radio value="commonScript">{{ t('project.commonScript.commonScript') }}</a-radio>
          <a-radio value="executionResult">{{ t('project.commonScript.executionResult') }}</a-radio>
        </a-radio-group>
        <a-button type="outline">{{ t('project.commonScript.scriptTest') }}</a-button>
      </div>
      <ScriptDefined v-if="scriptType === 'commonScript'" />
      <div v-else>
        <MsCodeEditor
          v-model:model-value="executionResultValue"
          title=""
          width="100%"
          height="calc(100vh - 155px)"
          theme="MS-text"
          :read-only="false"
          :show-full-screen="false"
          :show-theme-change="false"
        />
      </div>
    </a-form>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue';

  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import ScriptDefined from './scriptDefined.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { TableKeyEnum } from '@/enums/tableEnum';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
  }>();

  const emit = defineEmits(['update:visible']);

  const showScriptDrawer = computed({
    get() {
      return props.visible;
    },
    set(val) {
      emit('update:visible', val);
    },
  });

  const initForm = {
    name: '',
    description: '',
  };

  const form = ref({ ...initForm });

  const columns: MsTableColumn = [
    {
      title: '参数名称',
      slotName: 'name',
      dataIndex: 'name',
      showTooltip: true,
      showInTable: true,
    },
    {
      title: '是否必填',
      slotName: 'required',
      dataIndex: 'required',
      showInTable: true,
    },
    {
      title: '参数值',
      dataIndex: 'tags',
      slotName: 'tags',
      showTooltip: true,
      showInTable: true,
    },
    {
      title: '描述',
      slotName: 'desc',
      dataIndex: 'desc',
      showTooltip: true,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    () =>
      Promise.resolve({
        list: [],
        current: 1,
        pageSize: 10,
        total: 2,
      }),
    {
      columns,
      tableKey: TableKeyEnum.FILE_MANAGEMENT_FILE,
      showSetting: false,
      scroll: { x: '100%' },
      heightUsed: 300,
    },
    (item) => {
      return {
        ...item,
        tags: item.tags?.map((e: string) => ({ id: e, name: e })) || [],
      };
    }
  );

  const scriptType = ref<'commonScript' | 'executionResult'>('commonScript');

  const executionResultValue = ref('');
</script>

<style scoped></style>
