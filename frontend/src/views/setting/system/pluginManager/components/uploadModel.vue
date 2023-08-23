<template>
  <a-modal v-model:visible="pluginVisible" class="ms-modal-form ms-modal-small" title-align="start">
    <template #title> {{ t('system.plugin.uploadPlugin') }} </template>
    <div class="form grid grid-cols-1">
      <a-row class="grid-demo">
        <a-form ref="pluginFormRef" :model="form" size="small" :style="{ width: '600px' }" layout="vertical">
          <div class="relative">
            <a-form-item field="pluginName" :label="t('system.plugin.name')" asterisk-position="end">
              <a-input
                v-model="form.name"
                size="small"
                :max-length="250"
                :placeholder="t('system.plugin.defaultJarNameTip')"
                allow-clear
              />
              <span class="absolute right-0 top-1 flex items-center">
                <span class="float-left">{{ t('system.plugin.getPlugin') }}</span>
                <a-tooltip :content="t('system.plugin.infoTip')" position="bottom">
                  <span class="float-left mx-1 mt-[2px]">
                    <IconQuestionCircle class="h-[16px] w-[16px] text-[rgb(var(--primary-5))]" />
                  </span>
                </a-tooltip>
              </span>
            </a-form-item>
          </div>
          <a-form-item
            field="global"
            :label="t('system.plugin.appOrganize')"
            asterisk-position="end"
            :rules="[{ required: true, message: 'must select one' }]"
          >
            <a-radio-group v-model="form.global" size="small">
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
              <a-option v-for="item of originizeList" :key="item.id" :value="item.id">{{ item.name }}</a-option>
            </a-select>
          </a-form-item>
          <a-form-item field="describe" :label="t('system.plugin.description')" asterisk-position="end">
            <a-textarea
              v-model="form.description"
              size="small"
              :placeholder="t('system.plugin.pluginDescription')"
              allow-clear
            />
          </a-form-item>
        </a-form>
      </a-row>
      <MsUpload
        v-model:file-list="fileList"
        accept="jar"
        :max-size="50"
        size-unit="MB"
        main-text="system.user.importModalDragtext"
        :sub-text="t('system.plugin.supportFormat')"
        :show-file-list="false"
        :auto-upload="false"
        :disabled="confirmLoading"
      ></MsUpload>
    </div>
    <template #footer>
      <div class="flex justify-between">
        <div class="flex flex-row items-center justify-center">
          <a-switch v-model="form.enable" size="small" />
          <a-tooltip>
            <template #content>
              <div class="text-sm">{{ t('system.plugin.statusEnableTip') }}</div>
              <div class="text-sm">{{ t('system.plugin.statusDisableTip') }}</div>
            </template>
            <div class="mx-1 flex h-[32px] items-center">
              <span class="mr-1">{{ t('system.plugin.pluginStatus') }}</span>
              <span class="mt-[2px]"
                ><IconQuestionCircle class="h-[16px] w-[16px] text-[rgb(var(--primary-5))]"
              /></span>
            </div>
          </a-tooltip>
        </div>
        <div>
          <a-space>
            <a-button type="secondary" @click="handleCancel">{{ t('system.plugin.pluginCancel') }}</a-button>
            <a-button type="secondary" :disabled="isDisabled" :loading="saveLoading" @click="saveAndAddPlugin">{{
              t('system.plugin.saveAndAdd')
            }}</a-button>
            <a-button type="primary" :disabled="isDisabled" :loading="confirmLoading" @click="saveConfirm">{{
              t('system.plugin.pluginConfirm')
            }}</a-button>
          </a-space>
        </div>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, watchEffect, computed, watch } from 'vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';
  import type { FormInstance, ValidatedError, SelectOptionData, FileItem } from '@arco-design/web-vue';
  import { addPlugin } from '@/api/modules/setting/pluginManger';
  import { Message } from '@arco-design/web-vue';
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const emits = defineEmits<{
    (event: 'update:visible', visible: boolean): void;
    (e: 'success'): void;
    (e: 'brash'): void;
  }>();
  const props = defineProps<{
    visible: boolean;
    originizeList: SelectOptionData;
  }>();
  const pluginVisible = ref(false);
  const fileName = ref<string>('');
  const fileList = ref<FileItem[]>([]);
  const saveLoading = ref<boolean>(false);
  const confirmLoading = ref<boolean>(false);
  const pluginFormRef = ref<FormInstance | null>(null);
  const initForm = {
    name: '',
    description: '',
    organizationIds: [],
    enable: true,
    global: true,
  };
  const isDisabled = computed(() => {
    return !(fileList.value.length > 0);
  });
  const form = ref({ ...initForm });

  const resetForm = () => {
    form.value = { ...initForm };
    fileList.value = [];
  };
  watchEffect(() => {
    pluginVisible.value = props.visible;
  });
  watch(
    () => pluginVisible.value,
    (val) => {
      emits('update:visible', val);
      if (!val) {
        resetForm();
      }
    }
  );

  const handleCancel = () => {
    pluginVisible.value = false;
    pluginFormRef.value?.resetFields();
    resetForm();
  };
  const confirmHandler = async (flag: string) => {
    try {
      if (fileList.value.length < 1) {
        Message.warning(t('system.plugin.uploadFileTip'));
        return;
      }
      const params: any = {
        request: {
          ...form.value,
          name: form.value.name || fileName.value,
        },
        fileList: [fileList.value[0].file],
      };
      await addPlugin(params);
      Message.success(t('system.plugin.uploadSuccessTip'));
      if (flag === 'Confirm') {
        emits('success');
        handleCancel();
      }
      resetForm();
      emits('brash');
    } catch (error) {
      console.log(error);
    } finally {
      confirmLoading.value = false;
      saveLoading.value = false;
    }
  };
  const saveConfirm = () => {
    pluginFormRef.value?.validate((errors: undefined | Record<string, ValidatedError>) => {
      if (!errors) {
        confirmLoading.value = true;
        confirmHandler('Confirm');
      } else {
        return false;
      }
    });
  };
  const saveAndAddPlugin = () => {
    pluginFormRef.value?.validate((errors: undefined | Record<string, ValidatedError>) => {
      if (!errors) {
        saveLoading.value = true;
        confirmHandler('Continue');
      } else {
        return false;
      }
    });
  };
  watchEffect(() => {
    fileName.value = fileList.value[0]?.name as string;
  });
</script>

<style scoped lang="less">
  :deep(.arco-upload) {
    width: 100%;
  }
</style>
