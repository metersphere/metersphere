<template>
  <MsCaseAssociate
    v-model:visible="innerVisible"
    v-model:project="innerProject"
    :ok-button-disabled="associateForm.reviewers.length === 0"
    @success="writeAssociateCases"
    @close="emit('close')"
  >
    <template #footerLeft>
      <a-form ref="associateFormRef" :model="associateForm">
        <a-form-item
          field="reviewers"
          :rules="[{ required: true, message: t('caseManagement.caseReview.reviewerRequired') }]"
          class="mb-0"
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
            multiple
            class="w-[300px]"
          >
          </MsSelect>
        </a-form-item>
      </a-form>
    </template>
  </MsCaseAssociate>
</template>

<script setup lang="ts">
  import { useRouter } from 'vue-router';
  import { FormInstance } from '@arco-design/web-vue';

  import MsCaseAssociate from '@/components/business/ms-case-associate/index.vue';
  import MsSelect from '@/components/business/ms-select';

  import { useI18n } from '@/hooks/useI18n';

  import { ProjectManagementRouteEnum } from '@/enums/routeEnum';

  const props = defineProps<{
    visible: boolean;
    project: string;
  }>();
  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
    (e: 'update:project', val: string): void;
    (e: 'success', val: string[]): void;
    (e: 'close'): void;
  }>();

  const router = useRouter();
  const { t } = useI18n();

  const innerVisible = ref(false);

  watch(
    () => props.visible,
    (val) => {
      innerVisible.value = val;
    }
  );

  watch(
    () => innerVisible.value,
    (val) => {
      if (!val) {
        emit('update:visible', false);
      }
    }
  );

  const innerProject = ref('');

  watch(
    () => props.project,
    (val) => {
      innerProject.value = val;
    }
  );

  watch(
    () => innerProject.value,
    (val) => {
      emit('update:project', val);
    }
  );

  const associateForm = ref({
    reviewers: [],
  });
  const associateFormRef = ref<FormInstance>();

  function goProjectManagement() {
    window.open(
      `${window.location.origin}#${
        router.resolve({ name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_PERMISSION_USER_GROUP }).fullPath
      }`
    );
  }

  const reviewersOptions = ref([
    {
      label: '张三',
      value: '1',
    },
    {
      label: '李四',
      value: '2',
    },
    {
      label: '王五',
      value: '3',
    },
  ]);

  function writeAssociateCases(ids: string[]) {
    emit('success', ids);
  }
</script>

<style lang="less" scoped></style>
