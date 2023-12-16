<template>
  <a-modal v-model:visible="transferVisible" title-align="start" class="ms-modal-upload ms-modal-small">
    <template #title> 请选择转存目录 </template>
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
    ></a-tree-select>
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

  import { getTransferFileTree, transferFileRequest } from '@/api/modules/case-management/featureCase';
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

  const requestParams = ref<OperationFile>({ ...props.params });

  function handleCancel() {
    transferVisible.value = false;
    transferId.value = '';
  }

  async function handleBeforeOk() {
    loading.value = true;
    try {
      await transferFileRequest({ ...requestParams.value, moduleId: transferId.value });
      Message.success(t('caseManagement.featureCase.transferFileSuccess'));
      handleCancel();
      emit('success');
    } catch (error) {
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  watch(
    () => props.visible,
    async (val) => {
      if (val) {
        transCategory.value = await getTransferFileTree(currentProjectId.value);
      }
    }
  );
</script>

<style scoped></style>
