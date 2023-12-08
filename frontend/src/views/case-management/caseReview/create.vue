<!-- eslint-disable vue/no-v-html -->
<template>
  <MsCard
    :title="isEdit ? t('menu.caseManagement.caseManagementCaseReviewEdit') : t('caseManagement.caseReview.create')"
  >
    <a-form ref="reviewFormRef" class="w-[732px]" :model="reviewForm" layout="vertical">
      <a-form-item
        field="name"
        :label="t('caseManagement.caseReview.reviewName')"
        :rules="[{ required: true, message: t('caseManagement.caseReview.reviewNameRequired') }]"
        asterisk-position="end"
      >
        <a-input
          v-model:modelValue="reviewForm.name"
          :placeholder="t('caseManagement.caseReview.reviewNamePlaceholder')"
          :max-length="255"
          show-word-limit
        />
      </a-form-item>
      <a-form-item field="desc" :label="t('caseManagement.caseReview.desc')">
        <a-textarea
          v-model:modelValue="reviewForm.desc"
          :placeholder="t('caseManagement.caseReview.descPlaceholder')"
          :max-length="1000"
          show-word-limit
        />
      </a-form-item>
      <a-form-item field="folderId" :label="t('caseManagement.caseReview.belongModule')">
        <a-select
          v-model:modelValue="reviewForm.folderId"
          :placeholder="t('caseManagement.caseReview.belongModulePlaceholder')"
          :options="moduleOptions"
          class="w-[436px]"
          allow-search
          multiple
        />
      </a-form-item>
      <a-form-item field="type" :label="t('caseManagement.caseReview.type')">
        <a-radio-group v-model:modelValue="reviewForm.type">
          <a-radio value="single">
            <div class="flex items-center">
              {{ t('caseManagement.caseReview.single') }}
              <a-tooltip :content="t('caseManagement.caseReview.singleTip')" position="right">
                <icon-question-circle
                  class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                  size="16"
                />
              </a-tooltip>
            </div>
          </a-radio>
          <a-radio value="multi">
            <div class="flex items-center">
              {{ t('caseManagement.caseReview.multi') }}
              <a-tooltip :content="t('caseManagement.caseReview.multiTip')" position="right">
                <icon-question-circle
                  class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                  size="16"
                />
              </a-tooltip>
            </div>
          </a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item
        field="reviewers"
        :rules="[{ required: true, message: t('caseManagement.caseReview.defaultReviewerRequired') }]"
        asterisk-position="end"
      >
        <template #label>
          <div class="inline-flex items-center">
            {{ t('caseManagement.caseReview.defaultReviewer') }}
            <a-tooltip :content="t('caseManagement.caseReview.defaultReviewerTip')" position="right">
              <icon-question-circle
                class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                size="16"
              />
            </a-tooltip>
          </div>
        </template>
        <MsSelect
          v-model:modelValue="reviewForm.reviewers"
          mode="static"
          :placeholder="t('caseManagement.caseReview.reviewerPlaceholder')"
          :options="reviewersOptions"
          :search-keys="['label']"
          allow-search
          multiple
          :loading="reviewerLoading"
        />
      </a-form-item>
      <a-form-item field="tags" :label="t('caseManagement.caseReview.tag')">
        <MsTagsInput v-model:model-value="reviewForm.tags" />
      </a-form-item>
      <a-form-item field="cycle" :label="t('caseManagement.caseReview.cycle')">
        <a-range-picker
          v-model:model-value="reviewForm.cycle"
          show-time
          :time-picker-props="{
            defaultValue: ['00:00:00', '00:00:00'],
          }"
          class="w-[436px]"
        />
      </a-form-item>
      <a-form-item v-if="!isEdit">
        <template #label>
          <div class="flex items-center">
            <div>{{ t('caseManagement.caseReview.pickCases') }}</div>
            <a-divider margin="4px" direction="vertical" />
            <MsButton type="text" :disabled="selectedAssociateCases.length === 0" @click="clearSelectedCases">
              {{ t('caseManagement.caseReview.clearSelectedCases') }}
            </MsButton>
          </div>
        </template>
        <div class="bg-[var(--color-text-n9)] p-[12px]">
          <div class="flex items-center">
            <div class="text-[var(--color-text-2)]">
              {{ t('caseManagement.caseReview.selectedCases', { count: selectedAssociateCases.length }) }}
            </div>
            <a-divider margin="8px" direction="vertical" />
            <MsButton type="text" class="font-medium" @click="caseAssociateVisible = true">
              {{ t('ms.case.associate.title') }}
            </MsButton>
          </div>
        </div>
      </a-form-item>
    </a-form>
    <template #footerRight>
      <div class="flex items-center">
        <a-button type="secondary" @click="cancelCreate">{{ t('common.cancel') }}</a-button>
        <a-button v-if="isEdit" type="primary" class="ml-[16px]" @click="updateReview">
          {{ t('common.update') }}
        </a-button>
        <template v-else>
          <a-button type="secondary" class="mx-[16px]" @click="() => saveReview()">{{ t('common.save') }}</a-button>
          <a-button type="primary" @click="() => saveReview(true)">
            {{ t('caseManagement.caseReview.review') }}
          </a-button>
        </template>
      </div>
    </template>
  </MsCard>
  <AssociateDrawer
    v-model:visible="caseAssociateVisible"
    v-model:project="caseAssociateProject"
    @success="writeAssociateCases"
  />
</template>

<script setup lang="ts">
  /**
   * @description 功能测试-用例评审-创建评审
   */
  import { onBeforeMount } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { Message, SelectOptionData } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import AssociateDrawer from './components/create/associateDrawer.vue';

  import { getReviewUsers } from '@/api/modules/case-management/caseReview';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { CaseManagementRouteEnum } from '@/enums/routeEnum';

  import type { FormInstance } from '@arco-design/web-vue';

  const route = useRoute();
  const router = useRouter();
  const appStore = useAppStore();
  const { t } = useI18n();

  const isEdit = ref(!!route.query.id);
  const reviewFormRef = ref<FormInstance>();
  const reviewForm = ref({
    name: '',
    desc: '',
    folderId: '',
    type: 'single',
    reviewers: [],
    tags: [],
    cycle: [],
  });
  const moduleOptions = ref([
    {
      label: '全部',
      value: 'all',
    },
    {
      label: '模块1',
      value: '1',
    },
    {
      label: '模块2',
      value: '2',
    },
  ]);
  const reviewersOptions = ref<SelectOptionData[]>([]);
  const reviewerLoading = ref(false);

  async function initReviewers() {
    try {
      reviewerLoading.value = true;
      const res = await getReviewUsers(appStore.currentProjectId, '');
      reviewersOptions.value = res.map((e) => ({ label: e.name, value: e.id }));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      reviewerLoading.value = false;
    }
  }

  const selectedAssociateCases = ref<string[]>([]);

  function writeAssociateCases(ids: string[]) {
    selectedAssociateCases.value = [...ids];
  }

  function clearSelectedCases() {
    selectedAssociateCases.value = [];
  }

  function cancelCreate() {
    router.back();
  }

  function saveReview(isGoReview = false) {
    reviewFormRef.value?.validate(async (errors) => {
      if (!errors) {
        Message.success(t('common.createSuccess'));
        if (isGoReview) {
          // 是否去评审，是的话先保存然后直接跳转至该评审详情页进行评审
          router.replace({
            name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW,
          });
        } else {
          router.replace({
            name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW,
          });
        }
      }
    });
  }

  function updateReview() {
    reviewFormRef.value?.validate(async (errors) => {
      if (!errors) {
        Message.success(t('common.updateSuccess'));
        router.replace({
          name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW,
        });
      }
    });
  }

  const caseAssociateVisible = ref<boolean>(false);
  const caseAssociateProject = ref('');

  onBeforeMount(() => {
    initReviewers();
  });
</script>

<style lang="less" scoped>
  :deep(.arco-form-item-label-col) {
    @apply w-auto flex-none;
  }
</style>
