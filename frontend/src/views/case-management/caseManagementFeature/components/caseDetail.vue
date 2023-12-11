<template>
  <MsCard
    :loading="loading"
    :title="title"
    :is-edit="isEdit"
    has-breadcrumb
    @save="saveHandler"
    @save-and-continue="saveHandler(true)"
  >
    <CaseTemplateDetail ref="caseModuleDetailRef" v-model:form-mode-value="caseDetailInfo" />
    <template #footerRight>
      <div class="flex justify-end gap-[16px]">
        <a-button type="secondary" @click="cancelHandler">{{ t('mscard.defaultCancelText') }}</a-button>
        <a-button v-if="!isFormReviewCase" type="secondary" @click="saveHandler(true)">
          {{ t('mscard.defaultSaveAndContinueText') }}
        </a-button>
        <a-button v-if="!isFormReviewCase" type="primary" @click="saveHandler(false)">
          {{ t(isEdit ? 'mscard.defaultUpdate' : 'mscard.defaultConfirm') }}
        </a-button>
        <a-button v-if="isFormReviewCase" type="primary" @click="saveHandler(false, true)">
          {{ t('caseManagement.featureCase.createAndLink') }}
        </a-button>
      </div>
    </template>
  </MsCard>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import CaseTemplateDetail from './caseTemplateDetail.vue';

  import { createCaseRequest, updateCaseRequest } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useVisit from '@/hooks/useVisit';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';
  import { scrollIntoView } from '@/utils/dom';

  import { CaseManagementRouteEnum } from '@/enums/routeEnum';

  import Message from '@arco-design/web-vue/es/message';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();

  const featureCaseStore = useFeatureCaseStore();

  const visitedKey = 'doNotNextTipCreateCase';
  const { getIsVisited } = useVisit(visitedKey);

  const caseDetailInfo = ref<Record<string, any>>({
    request: {},
    fileList: [],
  });

  const title = ref('');
  const loading = ref(false);
  const isEdit = computed(() => !!route.query.id);
  const isFormReviewCase = computed(() => route.query.reviewId);

  const isContinueFlag = ref(false);
  const isShowTip = ref<boolean>(true);
  const createSuccessId = ref<string>('');

  async function save(isReview: boolean) {
    try {
      loading.value = true;
      if (route.params.mode === 'edit') {
        await updateCaseRequest(caseDetailInfo.value);
        Message.success(t('caseManagement.featureCase.editSuccess'));
      } else {
        const res = await createCaseRequest(caseDetailInfo.value);
        if (isReview) {
          // TODO
          // 创建并关联接口
        }
        createSuccessId.value = res.data.id;
        Message.success(route.params.mode === 'copy' ? t('ms.description.copySuccess') : t('common.addSuccess'));
      }
      if (isReview) {
        router.push({
          name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL,
          query: {
            id: route.query.reviewId,
            organizationId: route.query.organizationId,
            projectId: route.query.projectId,
          },
        });
      } else {
        router.push({ name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE, query: { ...route.query } });
      }

      featureCaseStore.setIsAlreadySuccess(true);
      isShowTip.value = !getIsVisited();
      if (isShowTip.value && !route.query.id) {
        router.push({
          name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_CREATE_SUCCESS,
          query: {
            id: createSuccessId.value,
            ...route.query,
          },
        });
      }
    } catch (error) {
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  const caseModuleDetailRef = ref();

  // 保存
  function saveHandler(isContinue = false, isReview = false) {
    const { caseFormRef, formRef, fApi } = caseModuleDetailRef.value;
    isContinueFlag.value = isContinue;
    caseFormRef?.validate().then((res: any) => {
      if (!res) {
        fApi.validate((valid: any) => {
          if (valid === true) {
            formRef?.validate().then((result: any) => {
              if (!result) {
                return save(isReview);
              }
            });
          }
        });
      }
      return scrollIntoView(document.querySelector('.arco-form-item-message'), { block: 'center' });
    });
  }
  function cancelHandler() {
    router.back();
  }

  watchEffect(() => {
    if (route.params.mode === 'edit') {
      title.value = t('caseManagement.featureCase.updateCase');
    } else if (route.params.mode === 'copy') {
      title.value = t('caseManagement.featureCase.copyCase');
    } else {
      title.value = t('caseManagement.featureCase.creatingCase');
    }
  });
</script>

<style scoped lang="less">
  .wrapper-preview {
    display: flex;
    .preview-left {
      width: 100%;
      border-right: 1px solid var(--color-text-n8);
      .changeType {
        padding: 2px 4px;
        border-radius: 4px;
        color: var(--color-text-4);
        :deep(.arco-icon-down) {
          font-size: 14px;
        }
        &:hover {
          color: rgb(var(--primary-5));
          background: rgb(var(--primary-1));
          cursor: pointer;
        }
      }
    }
    .preview-right {
      width: 428px;
    }
  }
  .circle {
    width: 16px;
    height: 16px;
    line-height: 16px;
    border-radius: 50%;
    text-align: center;
    color: var(--color-text-4);
    background: var(--color-text-n8);
  }
</style>
