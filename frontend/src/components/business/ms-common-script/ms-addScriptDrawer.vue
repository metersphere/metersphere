<template>
  <MsDrawer
    v-model:visible="showScriptDrawer"
    :title="form.id ? t('project.commonScript.editPublicScript') : t('project.commonScript.addPublicScript')"
    :width="900"
    min-width="900px"
    :footer="true"
    unmount-on-close
    :ok-loading="props.confirmLoading"
    :mask-closable="false"
    save-continue-text="project.commonScript.saveAsDraft"
    :ok-text="form.id ? t('common.update') : t('common.create')"
    @confirm="handleDrawerConfirm"
    @cancel="handleDrawerCancel"
  >
    <a-form ref="formRef" :model="form" layout="vertical">
      <a-form-item
        field="name"
        :label="t('project.commonScript.publicScriptName')"
        :rules="[{ required: true, message: t('project.commonScript.publicScriptNameNotEmpty') }]"
      >
        <a-input v-model="form.name" :max-length="255" :placeholder="t('project.commonScript.pleaseEnterScriptName')" />
      </a-form-item>
      <a-form-item field="status" :label="t('project.commonScript.scriptEnabled')">
        <a-select v-model="form.status" class="max-w-[396px]" :placeholder="t('project.commonScript.scriptEnabled')">
          <a-option value="DRAFT">{{ t('project.commonScript.draft') }}</a-option>
          <a-option value="PASSED">{{ t('project.commonScript.testsPass') }}</a-option>
        </a-select>
      </a-form-item>
      <a-form-item field="description" :label="t('common.desc')">
        <a-textarea
          v-model="form.description"
          :placeholder="t('system.organization.descriptionPlaceholder')"
          allow-clear
          :auto-size="{ minRows: 1 }"
          :max-length="1000"
        />
      </a-form-item>
      <a-form-item field="tags" :label="t('project.commonScript.tags')">
        <MsTagsInput v-model:modelValue="form.tags"></MsTagsInput>
      </a-form-item>
      <div class="mb-4">{{ t('project.commonScript.inputParams') }}</div>
      <paramTable
        v-model:params="innerParams"
        :columns="columns"
        :default-param-item="{
          key: '',
          value: '',
          description: '',
          required: false,
        }"
        :scroll="{ x: 'auto' }"
        :height-used="heightUsed"
        :selectable="false"
        @change="handleParamTableChange"
      />
      <div class="mb-2 mt-4 flex items-center justify-between">
        <a-radio-group v-model:model-value="scriptType" type="button">
          <a-radio value="commonScript">{{ t('project.commonScript.commonScript') }}</a-radio>
          <a-radio value="executionResult">{{ t('project.commonScript.executionResult') }}</a-radio>
        </a-radio-group>
        <a-button
          v-permission="['PROJECT_CUSTOM_FUNCTION:READ+EXECUTE']"
          type="outline"
          :loading="loading"
          :disabled="!form.script"
          @click="testScript"
          >{{ t('project.commonScript.scriptTest') }}</a-button
        >
      </div>
      <ScriptDefined
        v-model:language="form.type"
        v-model:code="form.script"
        v-model:execution-result="form.result"
        :show-type="scriptType"
        :enable-radio-selected="props.enableRadioSelected"
      />
    </a-form>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { useVModel } from '@vueuse/core';
  import { FormInstance, Message } from '@arco-design/web-vue';
  import { cloneDeep, debounce } from 'lodash-es';

  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import ScriptDefined from './scriptDefined.vue';
  import paramTable from '@/views/api-test/components/paramTable.vue';

  import { getCommonScriptDetail, getSocket, testCommonScript } from '@/api/modules/project-management/commonScript';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { getGenerateId } from '@/utils';

  import type { AddOrUpdateCommonScript, ParamsRequestType } from '@/models/projectManagement/commonScript';

  const appStore = useAppStore();

  const heightUsed = ref<number | undefined>(undefined);
  const formRef = ref<FormInstance>();
  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
    params: ParamsRequestType[];
    confirmLoading: boolean;
    scriptId?: string; // 脚本id
    enableRadioSelected?: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'update:params', value: any[]): void;
    (e: 'update:visible', value: boolean): void;
    (e: 'change'): void;
    (e: 'save', form: AddOrUpdateCommonScript): void;
    (e: 'close'): void;
  }>();

  const showScriptDrawer = computed({
    get() {
      return props.visible;
    },
    set(val) {
      emit('update:visible', val);
    },
  });

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

  const form = ref({ ...cloneDeep(initForm) });

  const columns: MsTableColumn = [
    {
      title: 'project.commonScript.ParameterNames',
      slotName: 'key',
      dataIndex: 'key',
      needValidRepeat: true,
    },
    {
      title: 'project.commonScript.ParameterValue',
      dataIndex: 'value',
      slotName: 'value',
      width: 300,
    },
    {
      title: 'common.desc',
      slotName: 'description',
      dataIndex: 'description',
    },
    {
      title: 'project.commonScript.isRequired',
      slotName: 'mustContain',
      dataIndex: 'required',
      width: 60,
    },
    {
      title: '',
      dataIndex: 'operation',
      slotName: 'operation',
      width: 50,
    },
  ];

  const scriptType = ref<'commonScript' | 'executionResult'>('commonScript');

  const innerParams = useVModel(props, 'params', emit);

  function handleParamTableChange(resultArr: any[], isInit?: boolean) {
    innerParams.value = [...resultArr];
    form.value.params = JSON.stringify([...resultArr]);
    if (!isInit) {
      emit('change');
    }
  }

  const paramMessageList = ref<string[]>([]);
  const setErrorMessageList = debounce((list: string[]) => {
    paramMessageList.value = [...list];
  }, 300);
  provide('setErrorMessageList', setErrorMessageList);

  function handleDrawerConfirm() {
    formRef.value?.validate(async (errors) => {
      if (!errors) {
        if (paramMessageList.value.length) {
          paramMessageList.value?.forEach((message) => {
            Message.error(message);
          });
          return;
        }
        emit('save', form.value);
      }
    });
  }

  function reset() {
    formRef.value?.resetFields();
    form.value = { ...initForm };
    innerParams.value = [];
  }

  function handleDrawerCancel() {
    reset();
    emit('close');
  }

  const editScriptId = ref<string | undefined>('');

  async function getDetail() {
    try {
      if (editScriptId.value) {
        const result = await getCommonScriptDetail(editScriptId.value);
        form.value = cloneDeep(result);
        innerParams.value = JSON.parse(result.params);
      }
    } catch (error) {
      console.log(error);
    }
  }

  watch(
    () => props.scriptId,
    (val) => {
      if (val) {
        editScriptId.value = val;
        if (val) {
          getDetail();
        }
      }
    }
  );

  watch(
    () => showScriptDrawer.value,
    (val) => {
      if (val) {
        form.value = { ...cloneDeep(initForm) };
        innerParams.value = [];
        editScriptId.value = props.scriptId;
        if (editScriptId.value) {
          getDetail();
        }
      }
    }
  );

  const loading = ref<boolean>(false);
  const websocket = ref<any>();
  const reportId = ref('');
  async function run() {
    try {
      const { type, script } = form.value;
      const parameters = innerParams.value
        .filter((item: any) => item.key && item.value)
        .map((item) => {
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
  // 提交测试请求
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

  // 测试脚本
  async function testScript() {
    reportId.value = getGenerateId();
  }

  watch(
    () => reportId.value,
    (val) => {
      if (val) {
        debugSocket();
      }
    }
  );
</script>

<style scoped></style>
