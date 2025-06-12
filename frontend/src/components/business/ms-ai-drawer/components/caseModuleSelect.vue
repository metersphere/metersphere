<template>
  <a-modal
    v-model:visible="visible"
    :title="t('ms.ai.choseModule')"
    title-align="start"
    body-class="ms-ai-config-modal p-0"
    :width="800"
    :ok-text="t('common.confirm')"
    :ok-button-props="{
      disabled: selectedKeys.length === 0,
    }"
    unmount-on-close
    @before-ok="handleOk"
    @cancel="handleCancel"
  >
    <a-spin class="w-full" :loading="loading">
      <a-input
        v-model:model-value="moduleKeyword"
        :placeholder="t('caseManagement.caseReview.folderSearchPlaceholder')"
        allow-clear
        class="mb-[16px]"
        :max-length="255"
      />
      <MsTree
        v-model:selected-keys="selectedKeys"
        :data="caseTree"
        :keyword="moduleKeyword"
        :empty-text="t('common.noData')"
        :virtual-list-props="{
          height: 'calc(60vh - 190px)',
          threshold: 200,
          fixedSize: true,
          buffer: 15,
        }"
        block-node
        :field-names="{
          title: 'name',
          key: 'id',
          children: 'children',
          count: 'count',
        }"
        title-tooltip-position="top"
      >
        <template #title="nodeData">
          <div class="inline-flex w-full gap-[8px]">
            <div class="one-line-text w-full text-[var(--color-text-1)]">{{ nodeData.name }}</div>
          </div>
        </template>
      </MsTree>
    </a-spin>
  </a-modal>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import MsTree from '@/components/business/ms-tree/index.vue';

  import { caseAiBatchSave, getCaseModuleTree } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';
  import { mapTree } from '@/utils';

  import { ModuleTreeNode } from '@/models/common';

  const props = defineProps<{
    model: string;
    templateId: string | number;
    conversationId: string;
    prompt: string;
  }>();

  const emit = defineEmits<{
    (e: 'syncSuccess'): void;
  }>();

  const { t } = useI18n();
  const appStore = useAppStore();
  const featureCaseStore = useFeatureCaseStore();

  const visible = defineModel<boolean>('visible', { required: true });
  const selectedKeys = ref<string[]>([]);
  const loading = ref(false);
  const moduleKeyword = ref<string>('');
  const caseTree = ref<ModuleTreeNode[]>([]);
  const modulesCount = computed(() => {
    return featureCaseStore.modulesCount;
  });

  /**
   * 初始化模块树
   */
  async function initModules() {
    try {
      loading.value = true;
      const res = await getCaseModuleTree({ projectId: appStore.currentProjectId });
      caseTree.value = mapTree<ModuleTreeNode>(res, (e) => {
        return {
          ...e,
          count: modulesCount.value[e.id] || 0,
        };
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  async function handleOk() {
    try {
      await caseAiBatchSave({
        prompt: props.prompt,
        chatModelId: props.model,
        conversationId: props.conversationId || '',
        organizationId: appStore.currentOrgId || '',
        projectId: appStore.currentProjectId || '',
        moduleId: selectedKeys.value[0] || '',
        templateId: props.templateId || '',
      });
      Message.success(t('ms.ai.caseSyncSuccess'));
      emit('syncSuccess');
      visible.value = false;
      return true;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      return true;
    } finally {
      selectedKeys.value = [];
      moduleKeyword.value = '';
    }
  }

  function handleCancel() {
    visible.value = false;
    selectedKeys.value = [];
    moduleKeyword.value = '';
  }
  onBeforeMount(() => {
    initModules();
  });

  defineExpose({
    initModules,
  });
</script>

<style scoped lang="less"></style>
