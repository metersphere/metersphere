<!-- eslint-disable vue/no-v-html -->
<template>
  <MsCard
    :loading="loading"
    :title="isEdit ? t('menu.caseManagement.caseManagementCaseReviewEdit') : t('caseManagement.caseReview.create')"
  >
    <a-form ref="reviewFormRef" class="w-[732px]" :model="reviewForm" layout="vertical">
      <a-form-item
        field="name"
        :label="t('caseManagement.caseReview.reviewName')"
        :rules="[
          { required: true, message: t('caseManagement.caseReview.reviewNameRequired') },
          { validator: validateName },
        ]"
        asterisk-position="end"
        required
      >
        <a-input
          v-model:modelValue="reviewForm.name"
          :placeholder="t('caseManagement.caseReview.reviewNamePlaceholder')"
          :max-length="255"
        />
      </a-form-item>
      <a-form-item field="desc" :label="t('common.desc')">
        <a-textarea
          v-model:modelValue="reviewForm.desc"
          :placeholder="t('caseManagement.caseReview.descPlaceholder')"
          :max-length="1000"
        />
      </a-form-item>
      <a-form-item field="folderId" :label="t('caseManagement.caseReview.belongModule')">
        <a-tree-select
          v-model:modelValue="reviewForm.folderId"
          :placeholder="t('caseManagement.caseReview.belongModulePlaceholder')"
          :data="moduleOptions"
          class="w-[436px]"
          :field-names="{ title: 'name', key: 'id', children: 'children' }"
          :loading="moduleLoading"
          allow-search
          :filter-tree-node="filterTreeNode"
        >
          <template #tree-slot-title="node">
            <a-tooltip :content="`${node.name}`" position="tl">
              <div class="one-line-text w-[300px]">{{ node.name }}</div>
            </a-tooltip>
          </template>
        </a-tree-select>
      </a-form-item>
      <a-form-item v-if="!isEdit" field="type" :label="t('caseManagement.caseReview.type')">
        <a-radio-group v-model:modelValue="reviewForm.type">
          <a-radio value="SINGLE">
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
          <a-radio value="MULTIPLE">
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
          </div>
        </template>
        <MsSelect
          v-model:model-value="reviewForm.reviewers"
          mode="static"
          :placeholder="t('caseManagement.caseReview.reviewerPlaceholder')"
          :options="reviewersOptions"
          :search-keys="['label']"
          allow-clear
          allow-search
          multiple
          :loading="reviewerLoading"
          class="reviewer-select"
        >
          <template #label="data">
            <div class="flex items-center gap-[2px]">
              <MsAvatar :avatar="reviewersOptions.find((e) => e.value === data.value)?.avatar" :size="20" />
              {{ data.label }}
            </div>
          </template>
        </MsSelect>
        <span class="text-[var(--color-text-4)]">{{ t('caseManagement.caseReview.defaultReviewerTip') }}</span>
      </a-form-item>
      <a-form-item field="tags" :label="t('caseManagement.caseReview.tag')">
        <MsTagsInput v-model:model-value="reviewForm.tags" />
      </a-form-item>
      <a-form-item field="cycle" :label="t('caseManagement.caseReview.cycle')">
        <a-range-picker
          v-model:model-value="reviewForm.cycle"
          show-time
          value-format="timestamp"
          :separator="t('common.to')"
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
            <a-divider v-if="!isCopy" margin="4px" direction="vertical" />
            <MsButton
              v-if="!isCopy"
              type="text"
              :disabled="selectedAssociateCasesParams.selectIds.length === 0"
              @click="clearSelectedCases"
            >
              {{ t('caseManagement.caseReview.clearSelectedCases') }}
            </MsButton>
          </div>
        </template>
        <div class="bg-[var(--color-text-n9)] p-[12px]">
          <div class="flex items-center">
            <div class="text-[var(--color-text-2)]">
              {{
                t('caseManagement.caseReview.selectedCases', {
                  count: isCopy
                    ? reviewForm.caseCount
                    : selectedAssociateCasesParams.selectAll
                    ? selectedAssociateCasesParams.totalCount
                    : selectedAssociateCasesParams.selectIds.length,
                })
              }}
            </div>
            <a-divider v-if="!isCopy" margin="8px" direction="vertical" />
            <MsButton
              v-if="!isCopy"
              v-permission="['CASE_REVIEW:READ+RELEVANCE']"
              type="text"
              class="font-medium"
              @click="caseAssociateVisible = true"
            >
              {{ t('ms.case.associate.title') }}
            </MsButton>
          </div>
        </div>
      </a-form-item>
    </a-form>
    <template #footerRight>
      <div class="flex items-center">
        <a-button type="secondary" :disabled="saveLoading" @click="cancelCreate">{{ t('common.cancel') }}</a-button>
        <a-button
          v-if="isEdit"
          v-permission="['CASE_REVIEW:READ+UPDATE']"
          type="primary"
          class="ml-[16px]"
          :loading="saveLoading"
          @click="updateReview"
        >
          {{ t('common.update') }}
        </a-button>
        <template v-else>
          <a-button
            v-permission="['CASE_REVIEW:READ+ADD']"
            type="secondary"
            class="mx-[16px]"
            :loading="saveLoading"
            @click="() => saveReview()"
          >
            {{ t('common.save') }}
          </a-button>
          <a-button
            v-permission.all="['CASE_REVIEW:READ+ADD', 'CASE_REVIEW:READ+REVIEW']"
            type="primary"
            :disabled="saveLoading"
            @click="() => saveReview(true)"
          >
            {{ t('caseManagement.caseReview.review') }}
          </a-button>
        </template>
      </div>
    </template>
  </MsCard>
  <AssociateDrawer
    v-model:visible="caseAssociateVisible"
    v-model:project="caseAssociateProject"
    :reviewers="reviewForm.reviewers"
    :has-not-associated-ids="selectedAssociateCasesParams.selectIds"
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

  import MsAvatar from '@/components/pure/ms-avatar/index.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import AssociateDrawer from './components/create/associateDrawer.vue';

  import {
    addReview,
    copyReview,
    editReview,
    getReviewDetail,
    getReviewModules,
    getReviewUsers,
  } from '@/api/modules/case-management/caseReview';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { filterTreeNode } from '@/utils';

  import type { BaseAssociateCaseRequest, ReviewPassRule } from '@/models/caseManagement/caseReview';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';

  import type { FormInstance } from '@arco-design/web-vue';

  const route = useRoute();
  const router = useRouter();
  const appStore = useAppStore();
  const { t } = useI18n();

  const isEdit = ref(!!route.query.id);
  const isCopy = ref(!!route.query.copyId);
  const reviewFormRef = ref<FormInstance>();
  const reviewForm = ref({
    name: '',
    desc: '',
    folderId: (route.query.moduleId as string) || 'root',
    type: 'SINGLE' as ReviewPassRule,
    reviewers: [] as string[],
    tags: [] as string[],
    cycle: [] as number[],
    caseCount: 0,
  });
  const moduleOptions = ref<SelectOptionData[]>([]);
  const moduleLoading = ref(false);

  const validateName = (value: string | undefined, callback: (error?: string) => void) => {
    if (value === undefined || value.trim() === '') {
      callback(t('caseManagement.caseReview.reviewNameRequired'));
    } else {
      if (value.length > 255) {
        callback(t('common.nameIsTooLang'));
      }
      callback();
    }
  };

  /**
   * 初始化模块选择
   */
  async function initModules() {
    try {
      moduleLoading.value = true;
      moduleOptions.value = await getReviewModules(appStore.currentProjectId);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      moduleLoading.value = false;
    }
  }

  const reviewersOptions = ref<SelectOptionData[]>([]);
  const reviewerLoading = ref(false);

  async function initReviewers() {
    try {
      reviewerLoading.value = true;
      const res = await getReviewUsers(appStore.currentProjectId, '');
      reviewersOptions.value = res.map((e) => ({ label: e.name, value: e.id, avatar: e.avatar }));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      reviewerLoading.value = false;
    }
  }

  // 批量关联用例表格参数
  const selectedAssociateCasesParams = ref<BaseAssociateCaseRequest>({
    excludeIds: [],
    selectIds: [],
    selectAll: false,
    condition: {},
    moduleIds: [],
    versionId: '',
    refId: '',
    projectId: '',
  });

  function writeAssociateCases(param: BaseAssociateCaseRequest) {
    selectedAssociateCasesParams.value = { ...param };
  }

  function clearSelectedCases() {
    selectedAssociateCasesParams.value = {
      excludeIds: [],
      selectIds: [],
      selectAll: false,
      condition: {},
      moduleIds: [],
      versionId: '',
      refId: '',
      projectId: '',
    };
  }

  function cancelCreate() {
    router.back();
  }

  const saveLoading = ref(false);
  function saveReview(isGoReview = false) {
    reviewFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          saveLoading.value = true;
          const { name, folderId, type, cycle, tags, desc, reviewers } = reviewForm.value;
          let res: Record<string, any> = {};
          if (isCopy.value) {
            // 复制评审场景
            res = await copyReview({
              copyId: route.query.copyId as string,
              projectId: appStore.currentProjectId,
              name,
              moduleId: folderId,
              reviewPassRule: type, // 评审通过规则
              startTime: cycle ? cycle[0] : null,
              endTime: cycle ? cycle[1] : null,
              tags,
              description: desc,
              reviewers, // 评审人员
            });
          } else {
            res = await addReview({
              projectId: appStore.currentProjectId,
              name,
              moduleId: folderId,
              reviewPassRule: type, // 评审通过规则
              startTime: cycle ? cycle[0] : null,
              endTime: cycle ? cycle[1] : null,
              tags,
              description: desc,
              reviewers, // 评审人员
              baseAssociateCaseRequest: selectedAssociateCasesParams.value, // 关联用例
            });
          }
          Message.success(t('common.createSuccess'));
          if (isGoReview) {
            // 是否去评审，是的话先保存然后直接跳转至该评审详情页进行评审
            router.replace({
              name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL,
              query: {
                id: res.id,
              },
            });
          } else {
            router.replace({
              name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW,
            });
          }
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          saveLoading.value = false;
        }
      }
    });
  }

  function updateReview() {
    reviewFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          saveLoading.value = true;
          const { name, folderId, type, cycle, tags, desc, reviewers } = reviewForm.value;
          await editReview({
            id: route.query.id as string,
            projectId: appStore.currentProjectId,
            name,
            moduleId: folderId,
            reviewPassRule: type, // 评审通过规则
            startTime: cycle ? cycle[0] : null,
            endTime: cycle ? cycle[1] : null,
            tags,
            description: desc,
            reviewers, // 评审人员
          });
          Message.success(t('common.updateSuccess'));
          router.back();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          saveLoading.value = false;
        }
      }
    });
  }

  const caseAssociateVisible = ref<boolean>(false);
  const caseAssociateProject = ref(appStore.currentProjectId);
  const loading = ref(false);
  async function initReviewDetail() {
    try {
      loading.value = true;
      const res = await getReviewDetail((route.query.copyId as string) || (route.query.id as string) || '');
      reviewForm.value = {
        name: res.name,
        desc: res.description,
        folderId: res.moduleId,
        type: res.reviewPassRule,
        reviewers: res.reviewers.map((e) => e.userId),
        tags: res.tags,
        cycle: [res.startTime, res.endTime],
        caseCount: res.caseCount,
      };
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }
  onBeforeMount(() => {
    initModules();
    initReviewers();
    if (isEdit.value || isCopy.value) {
      // 编辑评审场景、复制评审场景初始化评审数据
      initReviewDetail();
    }
  });
</script>

<style lang="less" scoped>
  :deep(.arco-form-item-label-col) {
    @apply w-auto flex-none;
  }
  :deep(.reviewer-select) {
    .arco-select-view-tag {
      @apply rounded-full;

      padding-left: 2px;
    }
  }
</style>
