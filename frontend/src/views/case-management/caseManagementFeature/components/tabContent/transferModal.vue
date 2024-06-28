<template>
  <a-modal
    v-model:visible="transferVisible"
    title-align="start"
    unmount-on-close
    :mask-closable="false"
    modal-class="shadowModal"
    :mask="false"
    :modal-style="{
      'box-shadow': '0px 4px 10px -1px rgba(100, 100, 102, 0.15)',
    }"
    class="ms-modal-form ms-modal-small"
  >
    <template #title> {{ t('caseManagement.featureCase.selectTransferDirectory') }} </template>
    <a-tree-select
      v-model="transferId"
      :data="transCategory"
      :field-names="{
        title: 'name',
        key: 'id',
        children: 'children',
      }"
      :tree-props="{
        virtualListProps: {
          height: 200,
        },
      }"
    >
      <template #tree-slot-title="node">
        <a-tooltip :content="`${node.name}`" position="tl">
          <div class="one-line-text w-[300px]">{{ node.name }}</div>
        </a-tooltip>
      </template>
    </a-tree-select>
    <template #footer>
      <div class="flex flex-row justify-end gap-[14px]">
        <a-button type="secondary" :disabled="loading" @click="handleCancel">
          {{ t('common.cancel') }}
        </a-button>
        <a-button type="primary" :loading="loading" :disabled="!transferId" @click="handleBeforeOk">
          {{ t('common.confirm') }}
        </a-button>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import { getTransferFileTree } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import type { OperationFile } from '@/models/caseManagement/featureCase';

  import Message from '@arco-design/web-vue/es/message';

  const appStore = useAppStore();
  const { t } = useI18n();
  const currentProjectId = computed(() => appStore.currentProjectId);
  const props = defineProps<{
    visible: boolean;
    params: OperationFile; // 转存文件参数
    requestFun: (params: OperationFile) => Promise<any>;
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
    (e: 'success'): void;
  }>();

  const transferVisible = computed({
    get() {
      return props.visible;
    },
    set(val) {
      emit('update:visible', val);
    },
  });

  const transferId = ref('');

  const transCategory = ref([]);

  const loading = ref<boolean>(false);

  function handleCancel() {
    transferVisible.value = false;
    transferId.value = '';
  }

  async function handleBeforeOk() {
    loading.value = true;
    try {
      await props.requestFun({ ...props.params, moduleId: transferId.value });
      Message.success(t('caseManagement.featureCase.transferFileSuccess'));
      handleCancel();
      emit('success');
    } catch (error) {
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  async function initFileTree() {
    try {
      transCategory.value = await getTransferFileTree(currentProjectId.value);
    } catch (error) {
      console.log(error);
    }
  }

  watch(
    () => props.visible,
    async (val) => {
      if (val) {
        initFileTree();
      }
    }
  );
</script>

<style scoped lang="less">
  // :deep(.arco-modal.shadowModal) {
  //   border-radius: 6px;
  //   border: 1px solid red;
  //   box-shadow: 0 4px 10px -1px rgba(100 100 102/ 15%) !important;
  // }
</style>
