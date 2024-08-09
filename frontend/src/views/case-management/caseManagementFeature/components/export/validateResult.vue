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
          }}<span class="mx-1 text-[rgb(var(--success-6))]"> {{ validateResultInfo.successCount }}</span
          >{{ t('caseManagement.featureCase.caseCount') }}</span
        >
        <span v-if="props.validateType === 'Excel'">
          {{ t('caseManagement.featureCase.failCheck') }}
          <span class="mx-1 font-medium text-[rgb(var(--danger-6))]">{{ validateResultInfo.failCount }}</span>
          {{ t('caseManagement.featureCase.caseCount') }}
        </span>
        <span v-else-if="validateResultInfo.failCount">
          {{ t('caseManagement.featureCase.partialCaseVerificationFailed') }}
        </span>
        <a-popover
          position="bottom"
          :content-style="{
            padding: '0px',
          }"
        >
          <span v-if="validateResultInfo.failCount" class="spanBtn font-medium text-[rgb(var(--primary-5))]">{{
            t('caseManagement.featureCase.viewErrorDetail')
          }}</span>
          <template #title>
            <div class="w-[440px] px-4 pt-4"
              >{{ t('caseManagement.featureCase.someCaseImportFailed') }}
              <span class="text-[14px] font-medium text-[var(--color-text-4)]"
                >({{ validateResultInfo.failCount }})</span
              ></div
            >
          </template>
          <template #content>
            <div class="w-[440px]">
              <div class="max-h-[400px] overflow-hidden px-4">
                <div
                  v-for="(item, index) of validateResultInfo.errorMessages"
                  :key="`${item.rowNum}-${index}`"
                  class="errorMessages"
                >
                  {{ item.errMsg }}
                </div>
              </div>
              <div class="moreBtn h-[40px] text-[14px]" type="text" long @click="showMore">{{
                t('caseManagement.featureCase.ViewMore')
              }}</div>
            </div>
          </template>
        </a-popover>
      </div>
      <div v-if="validateResultInfo.failCount > 0">
        {{ t('caseManagement.featureCase.afterFailingToModify', { type: props.validateType }) }}</div
      >
    </div>
    <template #footer>
      <div class="flex justify-end">
        <MsButton
          v-if="!validateResultInfo.successCount || !validateResultInfo.failCount"
          type="text"
          class="!text-[var(--color-text-1)]"
          @click="backCaseList"
          >{{ t('caseManagement.featureCase.backCaseList') }}</MsButton
        >
        <MsButton
          v-if="validateResultInfo.successCount"
          type="text"
          class="ml-[8px]"
          :disabled="props.importLoading"
          :loading="props.importLoading"
          @click="confirmImport"
        >
          {{ t('caseManagement.featureCase.import') }}</MsButton
        >
        <MsButton
          v-if="validateResultInfo.failCount || (validateResultInfo.failCount && validateResultInfo.successCount)"
          class="ml-[8px]"
          @click="handleCancel"
          >{{ t('caseManagement.featureCase.backToUploadPage') }}</MsButton
        >
        <MsButton
          v-if="validateResultInfo.failCount && validateResultInfo.successCount"
          :loading="props.importLoading"
          @click="confirmImport"
        >
          {{ t('caseManagement.featureCase.ignoreErrorContinueImporting') }}</MsButton
        >
      </div>
    </template>
  </a-modal>
  <MsDrawer v-model:visible="showMoreFailureCase" :width="960" :footer="false" no-content-padding>
    <template #title>
      <div class="justify-start">
        {{ t('caseManagement.featureCase.importFailedCases')
        }}<span class="text-[var(--color-text-4)]">({{ validateResultInfo.failCount }})</span>
      </div>
    </template>
    <MsList
      mode="static"
      :virtual-list-props="{
        height: 'calc(100vh - 270px)',
      }"
      :data="validateResultInfo.errorMessages"
      :bordered="false"
      :item-border="false"
      :split="false"
      :empty-text="t('project.fileManagement.noStorage')"
      active-item-class="activeItemClass"
      item-class="my-[8px]"
      :item-height="26"
    >
      <template #title="{ item, index }">
        <div :key="index" class="flex px-4">
          <div class="circle"></div>
          <div class="text-[var(--color-text-2)]">{{ item.errMsg }} </div>
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

  import { ValidateInfo } from '@/models/caseManagement/featureCase';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    validateType: 'Excel' | 'Xmind';
    validateInfo: ValidateInfo;
    importLoading: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
    (e: 'close'): void;
    (e: 'save'): void;
  }>();

  const validateResultModal = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val),
  });

  function handleCancel() {
    validateResultModal.value = false;
  }

  const validateResultInfo = ref<ValidateInfo>(props.validateInfo);
  const validateResult = ref('');

  const getIconType = computed(() => {
    const { successCount, failCount } = validateResultInfo.value;
    if (failCount && successCount) {
      validateResult.value = t('caseManagement.featureCase.partialCheckFailure');
      return 'icon-icon_warning_colorful';
    }
    if (!failCount) {
      validateResult.value = t('caseManagement.featureCase.CheckSuccess');
      return 'icon-icon_succeed_colorful';
    }
    if (!successCount) {
      validateResult.value = t('caseManagement.featureCase.CheckFailure');
      return 'icon-icon_close_colorful';
    }
  });

  const showMoreFailureCase = ref<boolean>(false);

  // 查看更多导入失败用例
  function showMore() {
    showMoreFailureCase.value = true;
  }

  // 返回用例列表
  function backCaseList() {
    emit('close');
  }

  // 确定继续导入
  function confirmImport() {
    emit('save');
  }

  watchEffect(() => {
    validateResultInfo.value = { ...props.validateInfo };
  });
</script>

<style scoped lang="less">
  .activeItemClass {
    background: none;
  }
  :deep(.ms-list-item--focus) {
    background: none !important;
  }
  :deep(.ms-list-item) {
    cursor: default !important;
  }
  .errorMessages {
    font-size: 14px;
    line-height: 21px;
    color: var(--color-text-2);
    @apply my-4;
  }
  .circle {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--color-text-input-border);
    @apply mr-2 mt-2;
  }
  .moreBtn {
    color: rgb(var(--primary-5));
    box-shadow: 0 -1px 4px rgba(31 35 41/10%);
    @apply mt-2 flex cursor-pointer items-center justify-center;
  }
  .spanBtn {
    cursor: pointer;
  }
  :deep(.arco-popover-popup-content) {
    padding: 0;
  }
</style>
