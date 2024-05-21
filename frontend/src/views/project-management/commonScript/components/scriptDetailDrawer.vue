<template>
  <MsDrawer v-model:visible="scriptDetailDrawer" :title="form.name" :width="800" :footer="false" unmount-on-close>
    <template #headerLeft>
      <MsTag class="ml-1" type="success" theme="light">{{ t('project.commonScript.testSuccess') }}</MsTag>
    </template>
    <template #tbutton>
      <div class="flex">
        <MsButton
          v-permission="['PROJECT_API_REPORT:READ+SHARE']"
          type="icon"
          status="secondary"
          class="mr-4 !rounded-[var(--border-radius-small)]"
          @click="editHandler"
        >
          <MsIcon type="icon-icon_edit_outlined" class="mr-2 font-[16px]" />
          {{ t('common.edit') }}
        </MsButton>
        <MsButton
          v-permission="['PROJECT_API_REPORT:READ+SHARE']"
          type="icon"
          status="secondary"
          :loading="loading"
          class="mr-4 !rounded-[var(--border-radius-small)]"
          @click="testHandler"
        >
          {{ t('apiTestDebug.test') }}
        </MsButton>
      </div>
    </template>
    <a-radio-group v-model="showType" type="button">
      <a-radio value="detail">{{ t('project.commonScript.detail') }}</a-radio>
      <a-radio value="changeHistory">{{ t('project.commonScript.changeHistory') }}</a-radio>
    </a-radio-group>
    <!-- 详情开始 -->
    <div v-if="showType === 'detail'">
      <div class="detailField mt-4">
        <div class="item">
          <span class="label">{{ t('common.desc') }}</span>
          <span class="content">{{ form.description }}</span>
        </div>
        <div class="item">
          <span class="label">{{ t('project.commonScript.tags') }}</span>
          <span class="content">
            <MsTag v-for="tag of form.tags" :key="tag" class="mb-2" theme="outline">{{ tag }}</MsTag>
          </span>
        </div>
      </div>
      <div class="mb-4">{{ t('project.commonScript.inputParams') }}</div>

      <ms-base-table v-bind="propsRes" ref="tableRef" class="mb-4" no-disable v-on="propsEvent">
        <template #mustContain="{ record }">
          <a-checkbox v-model:model-value="record.required" :disabled="true"></a-checkbox>
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
        :read-only="true"
        :show-full-screen="false"
        :show-theme-change="false"
      />
    </div>
    <ms-base-table
      v-else
      v-bind="changeHistoryPropsRes"
      ref="tableRef"
      class="mt-4"
      no-disable
      v-on="changeHistoryPropsEvent"
    >
      <template #id="{ record }">
        <div class="flex items-center">
          <div class="one-line-text mr-2 max-w-[200px]"> {{ record.id }} </div>
          <!-- <MsTag>{{ t('project.processor.current') }}</MsTag> -->
        </div>
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
  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import {
    getChangeHistory,
    getCommonScriptDetail,
    getSocket,
    testCommonScript,
  } from '@/api/modules/project-management/commonScript';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { getGenerateId } from '@/utils';

  import type { AddOrUpdateCommonScript } from '@/models/projectManagement/commonScript';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const appStore = useAppStore();

  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
    scriptId: string; // 脚本id
  }>();

  const emit = defineEmits(['update:visible', 'change', 'update']);

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
      slotName: 'key',
      dataIndex: 'key',
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
      title: 'common.desc',
      slotName: 'description',
      dataIndex: 'description',
      showTooltip: true,
    },
    {
      title: 'project.commonScript.isRequired',
      slotName: 'mustContain',
      dataIndex: 'required',
      showInTable: true,
    },
  ];

  const { propsRes, propsEvent } = useTable(undefined, {
    columns,
    selectable: false,
    showSetting: false,
    scroll: { x: '100%' },
    heightUsed: 300,
  });

  const scriptType = ref<'commonScript' | 'executionResult'>('commonScript');

  const changeHistoryColumns: MsTableColumn = [
    {
      title: 'project.commonScript.changeSerialNumber',
      slotName: 'id',
      dataIndex: 'id',
      showTooltip: true,
      showInTable: true,
      width: 300,
    },
    {
      title: 'project.commonScript.actionType',
      slotName: 'type',
      dataIndex: 'type',
      showInTable: true,
      width: 200,
    },
    {
      title: 'project.commonScript.actionUser',
      dataIndex: 'createUserName',
      slotName: 'createUserName',
      showTooltip: true,
      showInTable: true,
    },
    {
      title: 'project.commonScript.updateTime',
      dataIndex: 'createTime',
      slotName: 'createTime',
      showTooltip: true,
      showInTable: true,
    },
    // TODO:这个版本不上恢复
    // {
    //   title: 'project.commonScript.tableColumnActions',
    //   slotName: 'operation',
    //   dataIndex: 'operation',
    //   fixed: 'right',
    //   width: 140,
    //   showInTable: true,
    //   showDrag: false,
    // },
  ];

  const {
    propsRes: changeHistoryPropsRes,
    propsEvent: changeHistoryPropsEvent,
    setLoadListParams: setHistoryLoadListParams,
    loadList: loadHistoryList,
  } = useTable(getChangeHistory, {
    columns: changeHistoryColumns,
    tableKey: TableKeyEnum.PROJECT_MANAGEMENT_COMMON_SCRIPT_CHANGE_HISTORY,
    selectable: false,
    showSetting: false,
    scroll: { x: '100%' },
    heightUsed: 300,
  });

  function recoverHandler(record: any) {}

  const initForm: AddOrUpdateCommonScript = {
    name: '',
    status: '',
    tags: [],
    description: '',
    projectId: '',
    params: '',
    script: '',
    type: LanguageEnum.BEANSHELL_JSR233,
    result: '',
  };

  const form = ref<AddOrUpdateCommonScript>({ ...initForm });

  const detailValue = computed({
    get: () => {
      return scriptType.value === 'commonScript' ? form.value.script : form.value.result;
    },
    set: (val) => val,
  });

  async function getDetail() {
    try {
      const result = await getCommonScriptDetail(props.scriptId);
      form.value = cloneDeep(result);
      const innerTableData = JSON.parse(form.value.params);
      propsRes.value.data = innerTableData;
    } catch (error) {
      console.log(error);
    }
  }

  function loadChangeHistoryPage() {
    setHistoryLoadListParams({
      projectId: appStore.getCurrentProjectId,
      sourceId: props.scriptId,
    });
    loadHistoryList();
  }

  watchEffect(() => {
    if (props.scriptId) {
      getDetail();
    }
  });

  watch(
    () => scriptType.value,
    () => {
      const { script, result } = form.value;
      detailValue.value = scriptType.value === 'commonScript' ? script : result;
    }
  );

  function editHandler() {
    emit('update', props.scriptId);
  }

  const websocket = ref<any>();
  const reportId = ref('');

  function testHandler() {
    reportId.value = getGenerateId();
  }

  const loading = ref<boolean>(false);

  async function run() {
    try {
      const { type, script } = form.value;
      const parameters = JSON.parse(form.value.params);
      parameters
        .filter((item: Record<string, any>) => item.key && item.value)
        .map((item: Record<string, any>) => {
          return {
            key: item.key,
            value: item.value,
            valid: item.mustContain,
          };
        });
      const params = {
        type,
        script,
        params: parameters,
        projectId: appStore.currentProjectId,
        reportId: reportId.value,
      };
      await testCommonScript(params);
    } catch (error) {
      loading.value = false;
    }
  }

  function onOpen() {
    run();
  }

  function debugSocket() {
    loading.value = true;
    websocket.value = getSocket(reportId.value);
    websocket.value.onopen = onOpen;
    websocket.value.addEventListener('message', (event: any) => {
      const result = JSON.parse(event.data);
      if (result.msgType === 'EXEC_RESULT') {
        form.value.result = result.taskResult.console;
        scriptType.value = 'executionResult';
        websocket.value.close();
        loading.value = false;
      }
    });
  }

  watch(
    () => reportId.value,
    (val) => {
      if (val) {
        debugSocket();
      }
    }
  );

  watch(
    () => showType.value,
    (val) => {
      if (val === 'changeHistory') {
        loadChangeHistoryPage();
      } else {
        getDetail();
      }
    }
  );

  defineExpose({
    getDetail,
  });
</script>

<style scoped lang="less">
  .detailField {
    .item {
      @apply mb-4 flex;
      .label {
        width: 56px;
        color: var(--color-text-3);
        @apply mr-2 flex-shrink-0;
      }
    }
  }
</style>
