<template>
  <a-modal
    v-model:visible="showBatchModal"
    title-align="start"
    class="ms-modal-upload ms-modal-medium"
    unmount-on-close
  >
    <template #title>
      {{ batchTitle }}
      <div class="text-[var(--color-text-4)]">
        {{
          t('system.user.batchModalSubTitle', {
            count: props.batchParams?.currentSelectCount || props.tableSelected.length,
          })
        }}
      </div>
    </template>
    <a-spin class="w-full" :loading="loading">
      <a-alert v-if="batchModalMode === 'project'" class="mb-[16px]">
        {{ t('system.user.batchModalTip') }}
      </a-alert>
      <MsTransfer
        v-model="target"
        :data="treeData"
        :title="[t('system.user.batchOptional'), t('system.user.batchChosen')]"
        :source-input-search-props="{ placeholder: t('system.user.batchTransferSearchPlaceholder') }"
        :target-input-search-props="{ placeholder: t('system.user.batchTransferSearchPlaceholder') }"
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
      <a-button type="secondary" :disabled="batchLoading" @click="cancelBatch">
        {{ t('system.user.batchModalCancel') }}
      </a-button>
      <a-button type="primary" :disabled="target.length === 0" :loading="batchLoading" @click="confirmBatch">
        {{ t('system.user.batchModalConfirm') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import type { BatchActionQueryParams } from '@/components/pure/ms-table/type';
  import MsTransfer from '@/components/business/ms-transfer/index.vue';

  import {
    batchAddOrg,
    batchAddProject,
    batchAddUserGroup,
    getSystemOrgs,
    getSystemProjects,
    getSystemRoles,
  } from '@/api/modules/setting/user';
  import { useI18n } from '@/hooks/useI18n';

  import type { OrgsItem } from '@/models/setting/user';

  const { t } = useI18n();

  const props = withDefaults(
    defineProps<{
      tableSelected: (string | number)[];
      visible: boolean;
      action: string;
      batchParams?: BatchActionQueryParams;
      keyword?: string;
    }>(),
    {
      visible: false,
    }
  );

  const emit = defineEmits(['update:visible', 'finished']);

  const showBatchModal = ref(false);
  const batchTitle = ref('');
  const target = ref<string[]>([]);
  const batchModalMode = ref<'project' | 'userGroup' | 'organization'>('project');
  const treeData = ref<OrgsItem[]>([]);
  const loading = ref(false);

  async function handleTableBatch(action: string) {
    showBatchModal.value = true;
    try {
      loading.value = true;
      let resTree: any[] = [];
      switch (action) {
        case 'batchAddProject':
          batchModalMode.value = 'project';
          batchTitle.value = t('system.user.batchAddProject');
          resTree = await getSystemProjects();
          break;
        case 'batchAddUserGroup':
          batchModalMode.value = 'userGroup';
          batchTitle.value = t('system.user.batchAddUserGroup');
          resTree = await getSystemRoles();
          break;
        case 'batchAddOrganization':
          batchModalMode.value = 'organization';
          batchTitle.value = t('system.user.batchAddOrganization');
          resTree = await getSystemOrgs();
          break;
        default:
          break;
      }
      treeData.value = resTree;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
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
      if (!val) {
        target.value = [];
      }
      emit('update:visible', val);
    }
  );

  function cancelBatch() {
    showBatchModal.value = false;
    target.value = [];
  }

  const batchLoading = ref(false);

  async function confirmBatch() {
    batchLoading.value = true;
    try {
      const params = {
        selectIds: props.tableSelected as string[],
        selectAll: !!props.batchParams?.selectAll,
        excludeIds: props.batchParams?.excludeIds,
        condition: {
          keyword: props.keyword,
        },
        roleIds: target.value,
      };
      switch (batchModalMode.value) {
        case 'project':
          await batchAddProject(params);
          break;
        case 'userGroup':
          await batchAddUserGroup(params);
          break;
        case 'organization':
          await batchAddOrg(params);
          break;
        default:
          break;
      }
      Message.success(t('system.user.batchModalSuccess'));
      showBatchModal.value = false;
      emit('finished');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      batchLoading.value = false;
    }
  }
</script>

<style lang="less" scoped></style>
