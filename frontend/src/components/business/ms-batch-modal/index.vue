<template>
  <a-modal
    v-model:visible="showBatchModal"
    :mask-closable="false"
    :mask="true"
    title-align="start"
    class="ms-modal-upload ms-modal-medium"
    unmount-on-close
    @close="cancelBatch"
  >
    <template #title>
      {{ batchTitle }}
      <div class="text-[var(--color-text-4)]">
        {{ t('msBatchModal.batchModalSubTitle', { count: props.currentSelectCount }) }}
      </div>
    </template>
    <a-spin :loading="loading" class="w-full">
      <a-alert v-if="props.action === 'batchAddProject'" class="mb-[16px]">
        {{ t('msBatchModal.batchModalTip') }}
      </a-alert>
      <MsTransfer
        v-model="target"
        :data="treeList"
        :title="[t('system.user.batchOptional'), t('system.user.batchChosen')]"
        :tree-filed="{
          key: 'id',
          title: 'name',
          children: 'children',
          disabled: 'disabled',
        }"
        height="370px"
        show-search
      />
    </a-spin>
    <template #footer>
      <a-button type="secondary" @click="cancelBatch">{{ t('msBatchModal.batchModalCancel') }}</a-button>
      <a-button type="primary" :loading="batchLoading" :disabled="target.length < 1" @click="confirmBatch">
        {{ t('msBatchModal.batchModalConfirm') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsTransfer from '@/components/business/ms-transfer/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { BatchModel } from './types';

  const { t } = useI18n();

  interface TreeDataItem {
    key: string;
    title: string;
    children?: TreeDataItem[];
  }

  const props = withDefaults(
    defineProps<{
      visible: boolean;
      action: string;
      currentSelectCount: number | undefined;
    }>(),
    {
      visible: false,
    }
  );
  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
    (e: 'addProject', targetValue: string[], type: string): void;
    (e: 'addUserGroup', targetValue: string[], type: string): void;
    (e: 'addOrganization', targetValue: string[], type: string): void;
  }>();

  const showBatchModal = ref(false);
  const batchLoading = ref(false);
  const batchTitle = ref('');
  const target = ref<string[]>([]);
  const treeList = ref<TreeDataItem[]>([]);
  const loading = ref<boolean>(false);

  function handleTableBatch(action: string) {
    switch (action) {
      case 'batchAddProject':
        batchTitle.value = t('system.user.batchAddProject');
        break;
      case 'batchAddUserGroup':
        batchTitle.value = t('system.user.batchAddUserGroup');
        break;
      case 'batchAddOrganization':
        batchTitle.value = t('system.user.batchAddOrganization');
        break;
      default:
        break;
    }
    showBatchModal.value = true;
  }

  watch(
    () => props.visible,
    (val) => {
      if (val) {
        handleTableBatch(props.action);
      }
    },
    {
      immediate: true,
    }
  );

  watch(
    () => showBatchModal.value,
    (val) => {
      emit('update:visible', val);
    }
  );

  function cancelBatch() {
    showBatchModal.value = false;
    target.value = [];
  }

  const batchRequestFun = async (reqFun: any, params: BatchModel) => {
    batchLoading.value = true;
    loading.value = true;
    try {
      await reqFun(params);
      Message.success(t('msBatchModal.batchModalSuccess'));
      showBatchModal.value = false;
      target.value = [];
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      batchLoading.value = false;
      loading.value = false;
    }
  };

  const confirmBatch = async () => {
    try {
      switch (props.action) {
        case 'batchAddProject':
          emit('addProject', target.value, 'project');
          break;
        case 'batchAddUserGroup':
          emit('addUserGroup', target.value, 'userGroup');
          break;
        case 'batchAddOrganization':
          emit('addOrganization', target.value, 'organization');
          break;
        default:
          break;
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  const getTreeList = async (callBack: (orgId: string) => Promise<any>, orgId: string) => {
    loading.value = true;
    try {
      treeList.value = await callBack(orgId);
      loading.value = false;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  defineExpose({
    batchLoading,
    batchRequestFun,
    getTreeList,
  });
</script>

<style lang="less" scoped></style>
