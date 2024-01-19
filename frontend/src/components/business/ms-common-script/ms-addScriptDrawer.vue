<template>
  <MsDrawer
    v-model:visible="showScriptDrawer"
    :title="t('project.commonScript.addPublicScript')"
    :width="768"
    :footer="true"
    unmount-on-close
    :ok-loading="props.confirmLoading"
    save-continue-text="project.commonScript.saveAsDraft"
    ok-text="project.commonScript.apply"
    @confirm="handleDrawerConfirm"
    @cancel="handleDrawerCancel"
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
      <a-form-item field="status" :label="t('project.commonScript.scriptEnabled')">
        <a-select v-model="form.status" class="max-w-[396px]" :placeholder="t('project.commonScript.scriptEnabled')">
          <a-option value="DRAFT">{{ t('project.commonScript.draft') }}</a-option>
          <a-option value="PASSED">{{ t('project.commonScript.testsPass') }}</a-option>
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
      <a-form-item field="tags" :label="t('project.commonScript.tags')">
        <a-input-tag v-model="form.tags" :placeholder="t('project.commonScript.enterContentAddTags')" allow-clear />
      </a-form-item>
      <a-form-item field="inputParameters" :label="t('project.commonScript.inputParams')">
        <paramTable
          v-model:params="innerParams"
          :scroll="{ x: '100%' }"
          :columns="columns"
          :height-used="heightUsed"
          @change="handleParamTableChange"
        />
      </a-form-item>
      <div class="mb-2 flex items-center justify-between">
        <a-radio-group v-model:model-value="scriptType" type="button" size="small">
          <a-radio value="commonScript">{{ t('project.commonScript.commonScript') }}</a-radio>
          <a-radio value="executionResult">{{ t('project.commonScript.executionResult') }}</a-radio>
        </a-radio-group>
        <a-button type="outline">{{ t('project.commonScript.scriptTest') }}</a-button>
      </div>
      <ScriptDefined
        v-model:language="form.type"
        v-model:code="form.script"
        :show-type="scriptType"
        :enable-radio-selected="props.enableRadioSelected"
      />
    </a-form>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { useVModel } from '@vueuse/core';
  import { FormInstance } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import ScriptDefined from './scriptDefined.vue';
  import paramTable from '@/views/api-test/components/paramTable.vue';

  import { getCommonScriptDetail } from '@/api/modules/project-management/commonScript';
  import { useI18n } from '@/hooks/useI18n';

  import type { AddOrUpdateCommonScript, ParamsRequestType } from '@/models/projectManagement/commonScript';

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
    type: 'beanshellJSR223',
    result: '',
  };

  const form = ref({ ...initForm });

  const columns: MsTableColumn = [
    {
      title: 'project.commonScript.ParameterNames',
      slotName: 'name',
      dataIndex: 'name',
    },
    {
      title: 'project.commonScript.ParameterValue',
      dataIndex: 'value',
      slotName: 'value',
    },
    {
      title: 'project.commonScript.description',
      slotName: 'desc',
      dataIndex: 'description',
    },
    {
      title: 'project.commonScript.isRequired',
      slotName: 'mustContain',
      dataIndex: 'required',
    },
    {
      title: '',
      slotName: 'operation',
      width: 50,
    },
  ];

  const scriptType = ref<'commonScript' | 'executionResult'>('commonScript');

  const innerParams = useVModel(props, 'params', emit);

  function handleParamTableChange(resultArr: any[], isInit?: boolean) {
    innerParams.value = [...resultArr];
    if (!isInit) {
      emit('change');
    }
  }

  function handleDrawerConfirm() {
    formRef.value?.validate(async (errors) => {
      if (!errors) {
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
    emit('close');
    reset();
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
        form.value = { ...initForm };
        innerParams.value = [];
      }
    }
  );
</script>

<style scoped></style>
