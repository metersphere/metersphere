<template>
  <MsCaseAssociate
    v-model:visible="innerVisible"
    :current-select-case="currentSelectCase"
    :project-id="innerProject"
    :ok-button-disabled="associateForm.reviewers.length === 0"
    :get-modules-func="getCaseModuleTree"
    :get-table-func="getCaseList"
    :confirm-loading="confirmLoading"
    :associated-ids="[]"
    :has-not-associated-ids="props.hasNotAssociatedIds"
    :type="RequestModuleEnum.CASE_MANAGEMENT"
    :table-params="{ reviewId: props.reviewId }"
    :view-type="ViewTypeEnum.FUNCTIONAL_CASE"
    hide-project-select
    @close="emit('close')"
    @save="saveHandler"
  >
    <template #footerLeft>
      <a-form ref="associateFormRef" :model="associateForm">
        <a-form-item
          field="reviewers"
          :rules="[{ required: true, message: t('caseManagement.caseReview.reviewerRequired') }]"
          class="review-item mb-0"
        >
          <template #label>
            <div class="inline-flex items-center">
              {{ t('caseManagement.caseReview.reviewer') }}
              <a-tooltip position="right">
                <template #content>
                  <div>{{ t('caseManagement.caseReview.switchProject') }}</div>
                  <div>{{ t('caseManagement.caseReview.resetReviews') }}</div>
                  <div>
                    {{ t('caseManagement.caseReview.reviewsTip') }}
                    <span class="cursor-pointer text-[rgb(var(--primary-4))]" @click="goProjectManagement">
                      {{ t('menu.projectManagement') }}
                    </span>
                    {{ t('caseManagement.caseReview.reviewsTip2') }}
                  </div>
                </template>
                <icon-question-circle
                  class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                  size="16"
                />
              </a-tooltip>
            </div>
          </template>
          <MsSelect
            v-model:modelValue="associateForm.reviewers"
            mode="static"
            :placeholder="t('caseManagement.caseReview.reviewerPlaceholder')"
            :options="reviewersOptions"
            :search-keys="['label']"
            allow-search
            allow-clear
            :multiple="true"
            class="!w-[290px]"
            :loading="reviewerLoading"
          >
            <template #empty>
              <div class="p-[3px_8px] text-[var(--color-text-4)]">
                {{ t('caseManagement.caseReview.noMatchReviewer') }}
                <span class="cursor-pointer text-[rgb(var(--primary-5))]" @click="goProjectManagement">
                  {{ t('menu.projectManagement') }}
                </span>
                <span v-if="currentLocale === 'zh-CN'" class="ml-[4px]">{{ t('common.setting') }}</span>
              </div>
            </template>
          </MsSelect>
        </a-form-item>
      </a-form>
    </template>
  </MsCaseAssociate>
</template>

<script setup lang="ts">
  import { useRouter } from 'vue-router';
  import { useVModel } from '@vueuse/core';
  import { FormInstance, SelectOptionData } from '@arco-design/web-vue';

  import MsCaseAssociate from '@/components/business/ms-case-associate/index.vue';
  import { RequestModuleEnum } from '@/components/business/ms-case-associate/utils';
  import MsSelect from '@/components/business/ms-select';

  import { getReviewUsers } from '@/api/modules/case-management/caseReview';
  import { getCaseList, getCaseModuleTree } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useLocale from '@/locale/useLocale';
  import useAppStore from '@/store/modules/app';

  import { BaseAssociateCaseRequest } from '@/models/caseManagement/caseReview';
  import { ViewTypeEnum } from '@/enums/advancedFilterEnum';
  import { CaseLinkEnum } from '@/enums/caseEnum';
  import { ProjectManagementRouteEnum } from '@/enums/routeEnum';

  const props = defineProps<{
    visible: boolean;
    project: string;
    reviewId?: string;
    reviewers?: string[];
    hasNotAssociatedIds?: string[];
  }>();
  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
    (e: 'update:project', val: string): void;
    (e: 'update:associatedIds', val: string[]): void;
    (e: 'success', val: BaseAssociateCaseRequest & { reviewers: string[] }): void;
    (e: 'close'): void;
  }>();
  const router = useRouter();
  const appStore = useAppStore();
  const { currentLocale } = useLocale();
  const { t } = useI18n();

  const innerVisible = useVModel(props, 'visible', emit);
  const innerProject = useVModel(props, 'project', emit);

  const associateForm = ref({
    reviewers: props.reviewers || ([] as string[]),
  });
  const associateFormRef = ref<FormInstance>();

  function goProjectManagement() {
    window.open(
      `${window.location.origin}#${
        router.resolve({ name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_USER_GROUP }).fullPath
      }`
    );
  }

  const reviewersOptions = ref<SelectOptionData[]>([]);
  const reviewerLoading = ref(false);

  async function initReviewers() {
    try {
      reviewerLoading.value = true;
      const res = await getReviewUsers(appStore.currentProjectId, '');
      reviewersOptions.value = res.map((e) => ({ label: e.name, value: e.id }));
      associateForm.value.reviewers = props.reviewers || [];
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      reviewerLoading.value = false;
    }
  }

  const currentSelectCase = ref<CaseLinkEnum>(CaseLinkEnum.FUNCTIONAL);

  // const associatedIds = useVModel(props, 'associatedIds', emit);
  const confirmLoading = ref<boolean>(false);

  function saveHandler(params: BaseAssociateCaseRequest) {
    associateFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          confirmLoading.value = true;
          // associatedIds.value = [...params.selectIds];
          emit('success', { ...params, reviewers: associateForm.value.reviewers });
          innerVisible.value = false;
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          confirmLoading.value = false;
        }
      }
    });
  }

  watch(
    () => props.visible,
    (val) => {
      if (val) {
        // 抽屉打开才加载数据
        initReviewers();
      } else {
        associateFormRef.value?.resetFields();
      }
    }
  );
</script>

<style lang="less" scoped>
  :deep(.review-item) {
    .arco-form-item-label-col {
      flex: none;
      width: auto;
    }
  }
</style>
