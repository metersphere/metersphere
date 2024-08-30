<template>
  <MsDrawer
    v-model:visible="showBugDrawer"
    :mask="true"
    :title="t('caseManagement.featureCase.createDefect')"
    :ok-text="t('common.confirm')"
    :ok-loading="drawerLoading"
    :width="850"
    :mask-closable="true"
    unmount-on-close
    :show-continue="true"
    no-content-padding
    @continue="handleDrawerConfirm(true)"
    @confirm="handleDrawerConfirm"
    @cancel="handleDrawerCancel"
  >
    <template #tbutton>
      <div class="font-normal">
        <a-select
          v-model="bugTemplateId"
          class="w-[240px]"
          :options="templateOption"
          allow-search
          :placeholder="t('bugManagement.edit.defaultSystemTemplate')"
        >
          <template #prefix>
            <span class="text-[var(--color-text-brand)]">{{ t('system.orgTemplate.defectTemplates') }}</span>
          </template>
        </a-select>
      </div>
    </template>
    <div class="h-[calc(100vh-122px)] w-full p-[16px]">
      <BugDetail
        ref="bugDetailRef"
        v-model:template-id="bugTemplateId"
        is-drawer
        :bug-id="bugId"
        @save-params="saveParams"
      />
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import BugDetail from '@/views/bug-management/edit.vue';

  import { createOrUpdateBug, getTemplateOption } from '@/api/modules/bug-management';
  import { batchAddBugToCase } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import { BugEditFormObject } from '@/models/bug-management';

  const { t } = useI18n();
  const appStore = useAppStore();

  interface TemplateOption {
    label: string;
    value: string;
  }

  const props = defineProps<{
    bugId?: string;
    extraParams?: Record<string, any> | (() => Record<string, any>);
    isBatch?: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'success'): void;
    (e: 'save', params: { request: BugEditFormObject; fileList: File[] }): void;
  }>();

  const showBugDrawer = defineModel<boolean>('visible', {
    required: true,
  });

  const bugDetailRef = ref<InstanceType<typeof BugDetail>>();
  const drawerLoading = ref(false);
  const isEdit = computed(() => props.bugId);

  const bugTemplateId = ref<string>('');

  const templateOption = ref<TemplateOption[]>([]);

  const getTemplateOptions = async () => {
    try {
      drawerLoading.value = true;
      const res = await getTemplateOption(appStore.currentProjectId);
      templateOption.value = res.map((item) => {
        if (item.enableDefault && !isEdit.value) {
          // 选中默认模板
          bugTemplateId.value = item.id;
        }
        return {
          label: item.name,
          value: item.id,
        };
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      drawerLoading.value = false;
    }
  };
  function handleDrawerCancel() {
    bugDetailRef.value?.resetForm();
    showBugDrawer.value = false;
  }

  async function saveParams(isContinue: boolean, params: { request: BugEditFormObject; fileList: File[] }) {
    try {
      drawerLoading.value = true;
      const { request, fileList } = params;
      if (props.isBatch) {
        const extraParam =
          props.extraParams && typeof props.extraParams === 'function' ? await props.extraParams() : props.extraParams;
        await batchAddBugToCase({ request: { ...request, ...extraParam }, fileList });
      } else {
        await createOrUpdateBug({ request: { ...request, ...props.extraParams }, fileList });
      }

      Message.success(props.bugId ? t('common.updateSuccess') : t('common.createSuccess'));

      if (isContinue) {
        bugDetailRef.value?.resetForm();
      } else {
        handleDrawerCancel();
        emit('success');
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      drawerLoading.value = false;
    }
  }

  const handleDrawerConfirm = async (isContinue = false) => {
    bugDetailRef.value?.saveHandler(isContinue);
  };

  const initDefaultFields = async () => {
    await getTemplateOptions();
  };

  onBeforeMount(() => {
    initDefaultFields();
  });
</script>

<style scoped></style>
