<template>
  <a-modal v-model:visible="updateVisible" width="680px" title-align="start" class="ms-modal-form ms-modal-medium">
    <template #title> {{ t('system.plugin.updateTitle', { name: title }) }}</template>
    <div class="form">
      <a-form :model="form" layout="vertical">
        <!-- <a-col :span="24"> -->
        <a-form-item field="pluginName" :label="t('system.plugin.name')" asterisk-position="end">
          <a-input v-model="form.pluginName" :placeholder="t('system.plugin.defaultJarNameTip')" allow-clear />
        </a-form-item>
        <!-- </a-col> -->
        <a-form-item field="organize" :label="t('system.plugin.appOrganize')" asterisk-position="end">
          <a-radio-group v-model="form.organize">
            <a-radio value="1">全部组织</a-radio>
            <a-radio value="2">指定组织</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item
          v-if="form.organize === '2'"
          field="organize"
          :label="t('system.plugin.selectOrganization')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('system.plugin.selectOriginize') }]"
        >
          <a-select v-model="form.organizeGroup" multiple :placeholder="t('system.plugin.selectOriginize')" allow-clear>
            <a-option v-for="item of originizeList" :key="item.value">{{ item.label }}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="describe" :label="t('system.plugin.description')" asterisk-position="end">
          <a-textarea v-model="form.describe" :placeholder="t('system.plugin.pluginDescription')" allow-clear />
        </a-form-item>
      </a-form>
    </div>
    <template #footer>
      <a-button type="secondary" @click="emit('cancel')">{{ t('system.plugin.pluginCancel') }}</a-button>
      <a-button type="primary" @click="handleOk">
        {{ t('system.plugin.pluginConfirm') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, watchEffect, reactive, onMounted } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import type { PluginForm } from '@/models/setting/plugin';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'cancel'): void;
  }>();
  const title = ref<string>();
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
  const form = reactive<PluginForm>({
    pluginName: '',
    organize: '',
    describe: '',
    organizeGroup: [],
  });

  const updateVisible = ref<boolean>(false);
  watchEffect(() => {
    updateVisible.value = props.visible;
  });
  const handleCancel = () => {
    emit('cancel');
  };

  const handleOk = () => {
    handleCancel();
  };
  const initData = () => {
    form.pluginName = title.value as string;
  };
  onMounted(() => {
    initData();
  });
  defineExpose({
    title,
  });
</script>

<style scoped></style>
@/models/setting/plugin
