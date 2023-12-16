<template>
  <a-modal
    v-model:visible="validateResultModal"
    title-align="start"
    class="ms-modal-form ms-modal-small"
    @close="handleCancel"
  >
    <template #title>
      {{ t('caseManagement.featureCase.importCase') }}
    </template>
    <div class="text-center">
      <MsIcon class="h-[32px] w-[32px]" :type="getIconType" />
      <div class="my-2 text-[16px] font-medium text-[var(--color-text-1)]">{{ validateResult }}</div>
      <div class="leading-8">
        <span
          >{{ t('caseManagement.featureCase.successfulCheck')
          }}<span class="mx-1 text-[rgb(var(--success-6))]"> {{ validateCount.success }}</span
          >{{ t('caseManagement.featureCase.caseCount') }}</span
        >
        <span
          >{{ t('caseManagement.featureCase.failCheck')
          }}<span class="mx-1 font-medium text-[rgb(var(--danger-6))]">{{ validateCount.failure }}</span
          >{{ t('caseManagement.featureCase.caseCount') }}</span
        >
        <a-popover position="bottom">
          <span v-if="validateCount.failure" class="font-medium text-[rgb(var(--primary-5))]">{{
            t('caseManagement.featureCase.viewErrorDetail')
          }}</span>
          <template #title>
            <div class="w-[440px]"
              >{{ t('caseManagement.featureCase.someCaseImportFailed') }}
              <span class="text-[var(--color-text-4)]">({{ validateCount.failure }})</span></div
            >
          </template>
          <template #content>
            <div class="w-[440px]">
              <a-divider class="mx-0 my-0" />
              <a-button class="mt-[8px]" type="text" long @click="showMore">{{
                t('caseManagement.featureCase.ViewMore')
              }}</a-button>
            </div>
          </template>
        </a-popover>
      </div>
      <div> {{ t('caseManagement.featureCase.afterFailingToModify', { type: props.validateType }) }}</div>
    </div>
    <template #footer>
      <div class="flex justify-end">
        <MsButton
          v-if="!validateCount.success || !validateCount.failure"
          type="text"
          class="!text-[var(--color-text-1)]"
          >{{ t('caseManagement.featureCase.backCaseList') }}</MsButton
        >
        <MsButton v-if="!validateCount.failure" type="text" class="ml-[8px]">{{
          t('caseManagement.featureCase.import')
        }}</MsButton>
        <MsButton v-if="validateCount.failure || (validateCount.failure && validateCount.success)" class="ml-[8px]">{{
          t('caseManagement.featureCase.backToUploadPage')
        }}</MsButton>
        <MsButton v-if="validateCount.failure && validateCount.success">{{
          t('caseManagement.featureCase.ignoreErrorContinueImporting')
        }}</MsButton>
      </div>
    </template>
  </a-modal>
  <MsDrawer
    v-model:visible="showMoreFailureCase"
    :title="t('caseManagement.featureCase.cancelValidateSuccess', { number: validateCount.failure })"
    :width="960"
    :footer="false"
    no-content-padding
  >
    <MsList
      :virtual-list-props="{
        height: 'calc(100vh - 325px)',
      }"
      :data="failureCaseList"
      :bordered="false"
      :split="false"
      :empty-text="t('project.fileManagement.noStorage')"
      item-key-field="id"
      class="mr-[-6px]"
    >
      <template #title="{ item, index }">
        <div :key="index">
          <div>{{ item }} </div>
        </div>
      </template>
    </MsList>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsList from '@/components/pure/ms-list/index.vue';

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

  const validateResultModal = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val),
  });

  function handleCancel() {
    validateResultModal.value = false;
  }

  const validateCount = ref({
    success: 100,
    failure: 100,
  });

  const validateResult = ref('');

  const getIconType = computed(() => {
    const { success, failure } = validateCount.value;
    if (failure && success) {
      validateResult.value = t('caseManagement.featureCase.partialCheckFailure');
      return 'icon-icon_warning_colorful';
    }
    if (!failure) {
      validateResult.value = t('caseManagement.featureCase.CheckSuccess');
      return 'icon-icon_succeed_colorful';
    }
    if (!success) {
      validateResult.value = t('caseManagement.featureCase.CheckFailure');
      return 'icon-icon_close_colorful';
    }
  });

  const showMoreFailureCase = ref<boolean>(false);

  const failureCaseList = ref([]);
  // 查看更多导入失败用例
  function showMore() {}
</script>

<style scoped></style>
