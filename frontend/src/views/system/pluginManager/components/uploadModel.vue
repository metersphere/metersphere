<template>
  <a-modal
    v-model:visible="pluginVisible"
    class="ms-modal-form ms-modal-small"
    title-align="start"
    width="480px"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <template #title> {{ t('system.plugin.uploadPlugin') }} </template>
    <div class="form grid grid-cols-1">
      <a-row class="grid-demo">
        <a-form :model="form" size="small" :style="{ width: '600px' }" layout="vertical">
          <div class="relative">
            <a-form-item field="pluginName" :label="t('system.plugin.name')" asterisk-position="end">
              <a-input
                v-model="form.pluginName"
                size="small"
                :placeholder="t('system.plugin.defaultJarNameTip')"
                allow-clear
              />
              <span class="absolute right-0 top-1 flex items-center">
                <span class="float-left">{{ t('system.plugin.getPlugin') }}</span>
                <a-tooltip :content="t('system.plugin.infoTip')" position="bottom">
                  <a class="float-left mx-2" href="javascript:;">
                    <svg-icon :width="'16px'" :height="'16px'" :name="'infotip'"
                  /></a>
                </a-tooltip>
              </span>
            </a-form-item>
          </div>
          <a-form-item field="organize" :label="t('system.plugin.appOrganize')" asterisk-position="end">
            <a-radio-group v-model="form.organize" size="small">
              <a-radio value="1">{{ t('system.plugin.allOrganize') }}</a-radio>
              <a-radio value="2">{{ t('system.plugin.theOrganize') }}</a-radio>
            </a-radio-group>
          </a-form-item>
          <a-form-item
            v-if="form.organize === '2'"
            field="organize"
            :label="t('system.plugin.selectOrganization')"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.plugin.selectOriginize') }]"
          >
            <a-select
              v-model="form.organizeGroup"
              multiple
              :placeholder="t('system.plugin.selectOriginize')"
              allow-clear
            >
              <a-option v-for="item of originizeList" :key="item.value">{{ item.label }}</a-option>
            </a-select>
          </a-form-item>
          <a-form-item field="describe" :label="t('system.plugin.description')" asterisk-position="end">
            <a-textarea
              v-model="form.describe"
              size="small"
              :placeholder="t('system.plugin.pluginDescription')"
              allow-clear
            />
          </a-form-item>
        </a-form>
      </a-row>
      <MsUpload
        action="/"
        accept="excel"
        main-text="system.user.importModalDragtext"
        :sub-text="t('system.plugin.supportFormat')"
        :show-file-list="false"
      ></MsUpload>
    </div>
    <template #footer>
      <div class="flex justify-between">
        <div class="flex flex-row items-center justify-center">
          <a-switch v-model="form.status" size="small" />
          <a-tooltip>
            <template #content>
              <div class="text-sm">{{ t('system.plugin.statusEnableTip') }}</div>
              <div class="text-sm">{{ t('system.plugin.statusDisableTip') }}</div>
            </template>
            <a class="mx-2" href="javascript:;"> <svg-icon :width="'16px'" :height="'16px'" :name="'infotip'" /></a>
          </a-tooltip>
        </div>
        <div>
          <a-space>
            <a-button type="secondary" @click="handleCancel">{{ t('system.plugin.pluginCancel') }}</a-button>
            <a-button type="secondary" @click="saveAndAddPlugin">{{ t('system.plugin.saveAndAdd') }}</a-button>
            <a-button type="primary" @click="saveConfirm('confirm')">{{ t('system.plugin.pluginConfirm') }}</a-button>
          </a-space>
        </div>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, watchEffect, reactive } from 'vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
  const pluginVisible = ref(false);
  const emits = defineEmits<{
    (e: 'cancel'): void;
    (e: 'upload'): void;
    (e: 'success'): void;
  }>();
  const props = defineProps<{
    visible: boolean;
  }>();
  const form = reactive({
    pluginName: '',
    organize: '1',
    describe: '',
    organizeGroup: [],
    status: false,
  });
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
  watchEffect(() => {
    pluginVisible.value = props.visible;
  });
  const handleCancel = () => {
    emits('cancel');
  };

  const handleOk = () => {
    handleCancel();
  };
  const saveConfirm = (flag: string) => {
    if (flag === 'confirm') {
      handleCancel();
      emits('success');
    }
  };
  const saveAndAddPlugin = () => {
    saveConfirm('saveAndAdd');
  };
</script>

<style scoped lang="less">
  :deep(.arco-switch-checked) {
    background: rgb(var(--primary-6)) !important;
  }
  :deep(.arco-switch) {
    background: var(--color-fill-4);
  }
</style>
