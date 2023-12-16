<template>
  <a-modal
    v-model:visible="dialogVisible"
    title-align="start"
    class="ms-modal-form ms-modal-medium"
    :ok-text="t('common.confirm')"
    :cancel-text="t('common.cancel')"
    @close="handleCancel"
  >
    <template #title>
      {{
        props.validateType === 'Excel'
          ? t('caseManagement.featureCase.formExcelExport')
          : t('caseManagement.featureCase.formXMindExport')
      }}
    </template>
    <div>
      <a-alert class="mb-4"
        ><div class="flex items-center">
          {{ t('caseManagement.featureCase.beforeUploadTip', { type: props.validateType }) }}
          <MsIcon
            :type="props.validateType === 'Excel' ? 'icon-icon_file-excel_colorful1' : 'icon-icon_file-xmind_colorful1'"
            class="mx-1 cursor-pointer text-[rgb(var(--primary-6))]"
          ></MsIcon>
          <MsButton>{{ t('caseManagement.featureCase.downloadTemplate', { type: props.validateType }) }}</MsButton>
        </div>
      </a-alert>
      <MsUpload
        v-model:file-list="fileList"
        class="mb-6"
        :accept="props.validateType === 'Excel' ? 'excel' : 'xmind'"
        :max-size="100"
        size-unit="MB"
        main-text="caseManagement.featureCase.dragOrClick"
        :sub-text="
          props.validateType === 'Excel'
            ? t('caseManagement.featureCase.onlyEXcelTip')
            : t('caseManagement.featureCase.onlyXmindTip')
        "
        :show-file-list="false"
        :auto-upload="false"
        :disabled="confirmLoading"
      ></MsUpload>
      <a-form-item field="post" :label="t('caseManagement.featureCase.selectVersion')">
        <a-select class="max-w-[240px]" :placeholder="t('caseManagement.featureCase.defaultSelectNewVersion')">
          <a-option v-for="item of versionOptions" :key="item.id" :value="item.id">{{ item.name }}</a-option>
        </a-select>
      </a-form-item>
    </div>
    <template #footer>
      <div class="flex items-center justify-between">
        <a-checkbox v-model="isRecover"
          ><span class="flex items-center">
            {{ t('caseManagement.featureCase.isRecoverOriginCase') }}
            <a-tooltip>
              <template #content>
                <div>
                  {{ t('caseManagement.featureCase.selectedRecoverCase') }}
                </div>
                <div>
                  {{ t('caseManagement.featureCase.notSelectedRecoverCase') }}
                </div>
              </template>
              <icon-question-circle
                class="ml-1 inline-block text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              /> </a-tooltip></span
        ></a-checkbox>
        <div>
          <a-button type="secondary" @click="handleCancel">{{ t('system.plugin.pluginCancel') }}</a-button>
          <a-button class="ml-3" type="primary" :disabled="isDisabled" :loading="confirmLoading" @click="saveConfirm">{{
            t('caseManagement.featureCase.checkTemplate')
          }}</a-button>
        </div>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { FileItem } from '@arco-design/web-vue';

  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
    validateType: 'Excel' | 'Xmind';
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
    (e: 'save', files: FileItem[]): void;
    (e: 'close'): void;
  }>();
  const fileList = ref<FileItem[]>([]);

  const dialogVisible = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val),
  });

  const confirmLoading = ref<boolean>(false);

  const handleCancel = () => {
    fileList.value = [];
    emit('close');
  };

  const versionOptions = ref([
    {
      id: '1001',
      name: 'V1.0',
    },
  ]);

  const isRecover = ref<boolean>(false);

  const isDisabled = ref<boolean>(false);

  function saveConfirm() {
    emit('save', fileList.value);
  }
</script>

<style scoped></style>
