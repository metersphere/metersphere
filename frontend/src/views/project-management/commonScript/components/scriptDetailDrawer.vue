<template>
  <MsDrawer
    v-model:visible="scriptDetailDrawer"
    :title="t('project.commonScript.publicScriptName')"
    :width="768"
    :footer="false"
    unmount-on-close
  >
    <template #headerLeft>
      <MsTag class="ml-1" type="success" theme="light">{{ t('project.commonScript.testSuccess') }}</MsTag>
    </template>
    <a-radio-group v-model="showType" type="button" size="small">
      <a-radio value="detail">{{ t('project.commonScript.detail') }}</a-radio>
      <a-radio value="changeHistory">{{ t('project.commonScript.changeHistory') }}</a-radio>
    </a-radio-group>
    <!-- 详情开始 -->
    <div v-if="showType === 'detail'">
      <div class="detailField mt-4">
        <div class="item">
          <span class="label">{{ t('project.commonScript.description') }}</span>
          <span class="content">{{ form.description }}</span>
        </div>
        <div class="item">
          <span class="label">{{ t('project.commonScript.tags') }}</span>
          <span class="content">
            <MsTag v-for="tag of form.tags" :key="tag" theme="outline">{{ tag }}</MsTag>
          </span>
        </div>
      </div>
      <span>{{ t('project.commonScript.inputParams') }}</span>

      <ms-base-table v-bind="propsRes" ref="tableRef" class="mb-4" no-disable v-on="propsEvent">
        <template #mustContain="{ record }">
          <a-checkbox v-model:model-value="record.mustContain" :disabled="true"></a-checkbox>
        </template>
      </ms-base-table>

      <a-radio-group v-model="scriptType" class="mt-4" type="button" size="small">
        <a-radio value="commonScript">{{ t('project.commonScript.commonScript') }}</a-radio>
        <a-radio value="executionResult">{{ t('project.commonScript.executionResult') }}</a-radio>
      </a-radio-group>
      <MsCodeEditor
        v-model:model-value="detailValue"
        class="mt-2"
        title=""
        width="100%"
        height="calc(100vh - 155px)"
        theme="MS-text"
        :read-only="false"
        :show-full-screen="false"
        :show-theme-change="false"
      />
    </div>
    <ms-base-table
      v-else
      v-bind="changeHistoryPropsRes"
      ref="tableRef"
      class="mb-4"
      no-disable
      v-on="changeHistoryPropsEvent"
    >
      <template #changeSerialNumber="{ record }"
        ><div class="flex items-center"> {{ record.changeSerialNumber }} <MsTag>当前</MsTag> </div>
      </template>
      <template #operation="{ record }">
        <MsButton status="primary" @click="recoverHandler(record)">
          {{ t('project.commonScript.recover') }}
        </MsButton>
      </template>
    </ms-base-table>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { getCommonScriptDetail } from '@/api/modules/project-management/commonScript';
  import { useI18n } from '@/hooks/useI18n';

  import type { AddOrUpdateCommonScript, ParamsRequestType } from '@/models/projectManagement/commonScript';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
    scriptId: string; // 脚本id
  }>();

  const emit = defineEmits(['update:visible', 'change']);

  const showType = ref('detail');

  const scriptDetailDrawer = computed({
    get() {
      return props.visible;
    },
    set(val) {
      emit('update:visible', val);
    },
  });

  const columns: MsTableColumn = [
    {
      title: 'project.commonScript.ParameterNames',
      slotName: 'name',
      dataIndex: 'name',
      showTooltip: true,
      showInTable: true,
    },
    {
      title: 'project.commonScript.ParameterValue',
      dataIndex: 'value',
      slotName: 'value',
      showTooltip: true,
      showInTable: true,
    },
    {
      title: 'project.commonScript.description',
      slotName: 'desc',
      dataIndex: 'desc',
      showTooltip: true,
    },
    {
      title: 'project.commonScript.isRequired',
      slotName: 'mustContain',
      dataIndex: 'required',
      showInTable: true,
    },
  ];

  const { propsRes, propsEvent, setProps } = useTable(undefined, {
    columns,
    selectable: false,
    showSetting: false,
    scroll: { x: '100%' },
    heightUsed: 300,
  });

  const scriptType = ref<'commonScript' | 'executionResult'>('commonScript');

  const detailValue = ref('');

  const changeHistoryColumns: MsTableColumn = [
    {
      title: 'project.commonScript.changeSerialNumber',
      slotName: 'changeSerialNumber',
      dataIndex: 'changeSerialNumber',
      showTooltip: true,
      showInTable: true,
    },
    {
      title: 'project.commonScript.actionType',
      slotName: 'actionType',
      dataIndex: 'actionType',
      showInTable: true,
    },
    {
      title: 'project.commonScript.actionUser',
      dataIndex: 'actionUser',
      slotName: 'actionUser',
      showTooltip: true,
      showInTable: true,
    },
    {
      title: 'project.commonScript.updateTime',
      dataIndex: 'updateTime',
      slotName: 'updateTime',
      showTooltip: true,
      showInTable: true,
    },
    {
      title: 'project.commonScript.tableColumnActions',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 140,
      showInTable: true,
      showDrag: false,
    },
  ];

  const {
    propsRes: changeHistoryPropsRes,
    propsEvent: changeHistoryPropsEvent,
    loadList: changeHistoryloadList,
    setLoadListParams: changeHistorySetLoadListParams,
    resetSelector: changeHistoryResetSelector,
  } = useTable(
    () =>
      Promise.resolve({
        list: [],
        current: 1,
        pageSize: 10,
        total: 2,
      }),
    {
      columns: changeHistoryColumns,
      tableKey: TableKeyEnum.PROJECT_MANAGEMENT_COMMON_SCRIPT_CHANGE_HISTORY,
      selectable: false,
      showSetting: false,
      scroll: { x: '100%' },
      heightUsed: 300,
    }
  );

  function recoverHandler(record: any) {}

  const initForm: AddOrUpdateCommonScript = {
    name: '',
    status: '',
    tags: [],
    description: '',
    projectId: '',
    params: '',
    script: '',
    type: 'beanshell',
    result: '',
  };

  const form = ref<AddOrUpdateCommonScript>({ ...initForm });

  async function getDetail(scriptId: string) {
    try {
      const result = await getCommonScriptDetail(scriptId);
      form.value = cloneDeep(result);
      detailValue.value = form.value.script;
      const innerTableData = JSON.parse(form.value.params);
      setProps({ data: innerTableData.slice(0, innerTableData.length - 1) });
    } catch (error) {
      console.log(error);
    }
  }
  const originScript = ref<string>('');

  watch(
    () => props.scriptId,
    (val) => {
      if (val && originScript.value !== val) {
        getDetail(val);
      }
    }
  );

  watch(
    () => scriptType.value,
    () => {
      const { script, result } = form.value;
      detailValue.value = scriptType.value === 'commonScript' ? script : result;
    }
  );

  onMounted(() => {
    originScript.value = props.scriptId;
  });
</script>

<style scoped lang="less">
  .detailField {
    .item {
      @apply mb-4 flex;
      .label {
        width: 56px;
        color: var(--color-text-3);
        @apply mr-2;
      }
    }
  }
</style>
