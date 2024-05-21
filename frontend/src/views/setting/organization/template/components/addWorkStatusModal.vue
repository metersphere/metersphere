<template>
  <MsDialog
    v-model:visible="visible"
    dialog-size="medium"
    :title="title"
    :ok-text="isEdit ? 'common.update' : 'common.create'"
    :confirm="confirmHandler"
    :close="closeHandler"
    :switch-props="{
      switchName: t('system.orgTemplate.anyStateToAll'),
      switchTooltip: t('system.orgTemplate.stateTip'),
      showSwitch: isEdit ? false : true,
      enable: form.allTransferTo,
    }"
  >
    <div class="form">
      <a-form ref="formRef" :model="form" layout="vertical">
        <a-form-item
          field="name"
          :label="t('system.orgTemplate.stateName')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('system.orgTemplate.stateNameNotNull') }]"
        >
          <a-input
            v-model="form.name"
            :max-length="255"
            :placeholder="t('system.orgTemplate.stateNameDescription')"
          ></a-input>
        </a-form-item>
        <a-form-item field="remark" :label="t('common.desc')" asterisk-position="end">
          <a-textarea
            v-model:model-value="form.remark"
            :max-length="1000"
            :placeholder="t('system.orgTemplate.enterStatusDesc')"
            allow-clear
          ></a-textarea>
        </a-form-item>
      </a-form>
    </div>
  </MsDialog>
</template>

<script setup lang="ts">
  /**
   * @description 系统管理-组织-模板管理-工作流-创建工作流状态
   */
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { FormInstance, Message } from '@arco-design/web-vue';

  import MsDialog from '@/components/pure/ms-dialog/index.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import type { OrdWorkStatus } from '@/models/setting/template';

  import { getWorkFlowRequestApi } from '@/views/setting/organization/template/components/fieldSetting';

  const appStore = useAppStore();

  const currentOrgId = computed(() => appStore.currentOrgId);
  const currentProjectId = computed(() => appStore.currentProjectId);
  const route = useRoute();
  const { t } = useI18n();

  const props = defineProps<{
    mode: 'organization' | 'project';
  }>();

  const emits = defineEmits<{
    (e: 'update:visible', visible: boolean): void;
    (e: 'success'): void;
  }>();

  const visible = ref<boolean>(false);

  const initFormValue: OrdWorkStatus = {
    scopeId: '',
    id: '',
    name: '',
    scene: route.query.type,
    remark: '',
    allTransferTo: false, // 是否允许所有状态流转到该状态
  };
  const form = ref({ ...initFormValue });
  const formRef = ref<FormInstance | null>(null);

  const closeHandler = () => {
    formRef.value?.resetFields();
    visible.value = false;
    form.value = { ...initFormValue };
  };
  const title = computed(() => {
    return form.value.id ? t('system.orgTemplate.updateWorkFlowStatus') : t('system.orgTemplate.addWorkFlowStatus');
  });

  const isEdit = computed(() => !!form.value.id);

  const createWorkFlowStatus = getWorkFlowRequestApi(props.mode).create;
  const updateWorkFlowStatus = getWorkFlowRequestApi(props.mode).update;
  const confirmHandler = async (enable: boolean | undefined) => {
    await formRef.value?.validate().then(async (error) => {
      if (!error) {
        try {
          const scopeId = props.mode === 'organization' ? currentOrgId.value : currentProjectId.value;
          form.value.scopeId = scopeId;
          if (!form.value.id) {
            form.value.allTransferTo = enable as boolean;
            await createWorkFlowStatus(form.value);
            Message.success(t('system.orgTemplate.addSuccessState'));
          } else {
            await updateWorkFlowStatus(form.value);
            Message.success(t('system.orgTemplate.updateSuccess'));
          }
          closeHandler();
          emits('success');
        } catch (e) {
          console.log(e);
        }
      } else {
        return false;
      }
    });
  };
  // 编辑
  function handleEdit(record: OrdWorkStatus) {
    visible.value = true;
    const { name, id, remark, scene } = record;
    form.value = {
      name,
      id,
      remark,
      scene,
    };
  }

  watch(
    () => visible.value,
    (val) => {
      emits('update:visible', val);
    }
  );

  defineExpose({
    handleEdit,
  });
</script>

<style scoped></style>
