<template>
  <MsCard
    :loading="loading"
    :title="title"
    :is-edit="isEdit"
    has-breadcrumb
    @save="saveHandler"
    @save-and-continue="saveHandler(true)"
  >
    <template #headerRight>
      <a-select class="w-[240px]" :placeholder="t('featureTest.featureCase.versionPlaceholder')">
        <a-option v-for="template of versionOptions" :key="template.id" :value="template.id">{{
          template.name
        }}</a-option>
      </a-select>
    </template>
    <CaseTemplateDetail ref="caseModuleDetailRef" v-model:form-mode-value="caseDetailInfo" />
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

  import { FeatureTestRouteEnum } from '@/enums/routeEnum';

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

  const versionOptions = ref([
    {
      id: '1001',
      name: '模板01',
    },
  ]);

  const title = ref('');
  const loading = ref(false);
  const isEdit = computed(() => !!route.query.id);

  const isContinueFlag = ref(false);
  const isShowTip = ref<boolean>(true);

  async function save() {
    try {
      loading.value = true;
      if (route.params.mode === 'edit') {
        await updateCaseRequest(caseDetailInfo.value);
        Message.success(t('featureTest.featureCase.editSuccess'));
      } else {
        await createCaseRequest(caseDetailInfo.value);
        Message.success(route.params.mode === 'copy' ? t('ms.description.copySuccess') : t('common.addSuccess'));
      }
      router.push({ name: FeatureTestRouteEnum.FEATURE_TEST_CASE, query: { ...route.query } });
      featureCaseStore.setIsAlreadySuccess(true);
      isShowTip.value = !getIsVisited();
      if (isShowTip.value) {
        router.push({
          name: FeatureTestRouteEnum.FEATURE_TEST_CASE_CREATE_SUCCESS,
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
  function saveHandler(isContinue = false) {
    const { caseFormRef, formRef, fApi } = caseModuleDetailRef.value;
    isContinueFlag.value = isContinue;
    caseFormRef?.validate().then((res: any) => {
      if (!res) {
        fApi.validate((valid: any) => {
          if (valid === true) {
            formRef?.validate().then((result: any) => {
              if (!result) {
                return save();
              }
            });
          }
        });
      }
      return scrollIntoView(document.querySelector('.arco-form-item-message'), { block: 'center' });
    });
  }

  watchEffect(() => {
    if (route.params.mode === 'edit') {
      title.value = t('featureTest.featureCase.updateCase');
    } else if (route.params.mode === 'copy') {
      title.value = t('featureTest.featureCase.copyCase');
    } else {
      title.value = t('featureTest.featureCase.creatingCase');
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
