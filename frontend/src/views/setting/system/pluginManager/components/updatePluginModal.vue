<template>
  <a-modal v-model:visible="updateVisible" width="680px" title-align="start" class="ms-modal-form ms-modal-medium">
    <template #title> {{ t('system.plugin.updateTitle', { name: form.name }) }}</template>
    <div class="form">
      <a-form ref="UpdateFormRef" :model="form" layout="vertical">
        <a-form-item field="name" :label="t('system.plugin.name')" asterisk-position="end">
          <a-input v-model="form.name" :placeholder="t('system.plugin.defaultJarNameTip')" allow-clear />
        </a-form-item>
        <a-form-item field="global" :label="t('system.plugin.appOrganize')" asterisk-position="end">
          <a-radio-group v-model="form.global">
            <a-radio :value="true">{{ t('system.plugin.allOrganize') }}</a-radio>
            <a-radio :value="false">{{ t('system.plugin.theOrganize') }}</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item
          v-if="!form.global"
          field="organizationIds"
          :label="t('system.plugin.selectOrganization')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('system.plugin.selectOriginize') }]"
        >
          <a-select
            v-model="form.organizationIds"
            multiple
            :placeholder="t('system.plugin.selectOriginize')"
            allow-clear
          >
            <a-option v-for="item of originizeList" :key="item.value" :value="item.value">{{ item.label }}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="description" :label="t('system.plugin.description')" asterisk-position="end">
          <a-textarea v-model="form.description" :placeholder="t('system.plugin.pluginDescription')" allow-clear />
        </a-form-item>
      </a-form>
    </div>
    <template #footer>
      <a-button type="secondary" @click="handleCancel">{{ t('system.plugin.pluginCancel') }}</a-button>
      <a-button type="primary" :loading="confirmLoading" @click="handleOk">
        {{ t('system.plugin.pluginConfirm') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, watchEffect, watch, nextTick, reactive } from 'vue';
  import { FormInstance, Message, ValidatedError } from '@arco-design/web-vue';
  import { useI18n } from '@/hooks/useI18n';
  import type { UpdatePluginModel, PluginItem } from '@/models/setting/plugin';
  import { updatePlugin } from '@/api/modules/setting/pluginManger';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
  }>();
  const emits = defineEmits<{
    (e: 'success'): void;
    (e: 'update:visible', val: boolean): void;
  }>();
  const confirmLoading = ref<boolean>(false);
  const UpdateFormRef = ref<FormInstance | null>(null);
  const originizeList = ref([
    {
      label: '组织一',
      value: '1',
    },
    {
      label: '组织二',
      value: '2',
    },
  ]);
  const form = reactive<UpdatePluginModel>({
    name: '',
    global: '',
    organizationIds: [],
    description: '',
  });

  const updateVisible = ref<boolean>(false);
  watchEffect(() => {
    updateVisible.value = props.visible;
  });
  watch(
    () => updateVisible.value,
    (val) => {
      emits('update:visible', val);
    }
  );
  const handleCancel = () => {
    UpdateFormRef.value?.resetFields();
    updateVisible.value = false;
  };
  const open = (record: PluginItem) => {
    nextTick(() => {
      form.name = record.name;
      form.organizationIds = record.organizations.map((item) => item.id);
      form.global = record.global;
      form.id = record.id;
      form.description = record.description;
    });
  };

  const handleOk = () => {
    UpdateFormRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (!errors) {
        confirmLoading.value = true;
        try {
          await updatePlugin(form);
          Message.success(t('system.plugin.updateSuccessTip'));
          handleCancel();
          emits('success');
        } catch (error) {
          console.log(error);
        } finally {
          confirmLoading.value = false;
        }
      } else {
        return false;
      }
    });
  };
  defineExpose({
    open,
    UpdateFormRef,
  });
</script>

<style scoped></style>
