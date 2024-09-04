<template>
  <a-modal
    v-model:visible="pluginVisible"
    class="ms-modal-form ms-modal-small"
    title-align="start"
    :mask="true"
    :mask-closable="false"
  >
    <template #title> {{ t('system.plugin.uploadPlugin') }} </template>
    <div class="form grid grid-cols-1">
      <a-row class="grid-demo">
        <a-form ref="pluginFormRef" :model="form" :style="{ width: '600px' }" layout="vertical">
          <a-form-item field="pluginName" :label="t('system.plugin.name')" asterisk-position="end">
            <a-input
              v-model="form.name"
              :max-length="255"
              :placeholder="t('system.plugin.defaultJarNameTip')"
              allow-clear
            />
            <span class="absolute right-0 top-1 flex items-center">
              <MsButton class="!mx-0 text-[rgb(var(--primary-5))]" @click="openGithub">{{
                t('system.plugin.getPlugin')
              }}</MsButton>
            </span>
          </a-form-item>
          <a-form-item
            field="global"
            :label="t('system.plugin.appOrganize')"
            asterisk-position="end"
            :rules="[{ required: true, message: 'must select one' }]"
          >
            <a-radio-group v-model="form.global">
              <a-radio :value="true"
                >{{ t('system.plugin.allOrganize') }}
                <span class="float-right mx-1 mt-[1px]">
                  <a-tooltip :content="t('system.plugin.allOrganizeTip')" position="top">
                    <IconQuestionCircle
                      class="h-[16px] w-[16px] text-[--color-text-4] hover:text-[rgb(var(--primary-5))]"
                  /></a-tooltip> </span
              ></a-radio>
              <a-radio :value="false">{{ t('system.plugin.theOrganize') }}</a-radio>
            </a-radio-group>
          </a-form-item>
          <a-form-item
            v-if="!form.global"
            field="organizationIds"
            :label="t('system.plugin.selectOrganization')"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.plugin.selectOrganizeTip') }]"
          >
            <a-select
              v-model="form.organizationIds"
              multiple
              :placeholder="t('system.plugin.selectOrganizeTip')"
              allow-clear
            >
              <a-option v-for="item of organizeList" :key="item.id" :value="item.id">{{ item.name }}</a-option>
            </a-select>
          </a-form-item>
          <a-form-item field="describe" :label="t('common.desc')" asterisk-position="end">
            <a-textarea
              v-model="form.description"
              :max-length="1000"
              :placeholder="t('system.plugin.pluginDescription')"
            />
          </a-form-item>
        </a-form>
      </a-row>
      <MsUpload
        v-model:file-list="fileList"
        accept="jar"
        size-unit="MB"
        main-text="system.user.importModalDragText"
        :sub-text="t('system.plugin.supportFormat', { size: appStore.getFileMaxSize })"
        :show-file-list="false"
        :auto-upload="false"
        :disabled="confirmLoading"
        :draggable="true"
        :file-type-tip="t('system.plugin.supportFormatType')"
      ></MsUpload>
    </div>
    <template #footer>
      <div class="flex justify-between">
        <div class="flex flex-row items-center justify-center">
          <a-switch v-model="form.enable" size="small" type="line" />
          <span class="ml-2">{{ t('system.plugin.pluginStatus') }}</span>
          <a-tooltip>
            <template #content>
              <div class="text-sm">{{ t('system.plugin.statusEnableTip') }}</div>
              <div class="text-sm">{{ t('organization.service.statusDisableTip') }}</div>
            </template>
            <div class="mx-1 flex h-[32px] items-center">
              <span class="mt-[2px]"
                ><IconQuestionCircle class="h-[16px] w-[16px] text-[--color-text-4] hover:text-[rgb(var(--primary-5))]"
              /></span>
            </div>
          </a-tooltip>
        </div>
        <div>
          <a-button type="secondary" @click="handleCancel">{{ t('system.plugin.pluginCancel') }}</a-button>
          <a-button
            class="mx-[12px]"
            type="secondary"
            :disabled="isDisabled"
            :loading="saveLoading"
            @click="saveAndAddPlugin"
            >{{ t('system.plugin.saveAndAdd') }}</a-button
          >
          <a-button type="primary" :disabled="isDisabled" :loading="confirmLoading" @click="saveConfirm">{{
            t('common.save')
          }}</a-button>
        </div>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { computed, ref, watch, watchEffect } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';

  import { addPlugin } from '@/api/modules/setting/pluginManger';
  import { useI18n } from '@/hooks/useI18n';
  import useVisit from '@/hooks/useVisit';
  import useAppStore from '@/store/modules/app';

  import type { FileItem, FormInstance, SelectOptionData, ValidatedError } from '@arco-design/web-vue';

  const props = defineProps<{
    visible: boolean;
    organizeList: SelectOptionData;
  }>();
  const emits = defineEmits<{
    (event: 'update:visible', visible: boolean): void;
    (e: 'success'): void;
    (e: 'brash'): void;
  }>();

  const { t } = useI18n();
  const appStore = useAppStore();
  const visitedKey = 'doNotShowAgain';
  const { getIsVisited } = useVisit(visitedKey);

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
      const isOpen = getIsVisited();
      // 确定不再提示
      if (isOpen || flag) {
        Message.success(t('system.plugin.uploadSuccessTip'));
      }

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

  function openGithub() {
    window.open('https://metersphere.io/docs/v3.x/plugin/');
  }
</script>

<style scoped lang="less">
  :deep(.arco-upload) {
    width: 100%;
  }
</style>
