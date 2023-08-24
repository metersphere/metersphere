<template>
  <a-modal v-model:visible="showBatchModal" title-align="start" class="ms-modal-upload ms-modal-medium">
    <template #title>
      {{ batchTitle }}
      <div class="text-[var(--color-text-4)]">
        {{ t('system.user.batchModalSubTitle', { count: props.tableSelected.length }) }}
      </div>
    </template>
    <a-spin :loading="loading">
      <a-alert v-if="batchModalMode === 'project'" class="mb-[16px]">
        {{ t('system.user.batchModalTip') }}
      </a-alert>
      <a-transfer
        v-model="target"
        :title="[t('system.user.batchOptional'), t('system.user.batchChosen')]"
        :data="transferData"
        show-search
      >
        <template #source="{ data, selectedKeys, onSelect }">
          <a-tree
            :checkable="true"
            checked-strategy="child"
            :checked-keys="selectedKeys"
            :data="getTreeData(data)"
            block-node
            @check="onSelect"
          />
        </template>
      </a-transfer>
    </a-spin>
    <template #footer>
      <a-button type="secondary" :disabled="batchLoading" @click="cancelBatch">{{
        t('system.user.batchModalCancel')
      }}</a-button>
      <a-button type="primary" :disabled="target.length === 0" :loading="batchLoading" @click="confirmBatch">
        {{ t('system.user.batchModalConfirm') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import { useI18n } from '@/hooks/useI18n';
  import {
    batchAddProject,
    batchAddOrg,
    batchAddUserGroup,
    getSystemOrgs,
    getSystemProjects,
    getSystemRoles,
  } from '@/api/modules/setting/user';

  import type { OrgsItem } from '@/models/setting/user';

  const { t } = useI18n();

  interface TreeDataItem {
    key: string;
    title: string;
    children?: TreeDataItem[];
  }

  interface TransferDataItem {
    value: string;
    label: string;
    disabled: boolean;
  }

  const props = withDefaults(
    defineProps<{
      tableSelected: (string | number)[];
      visible: boolean;
      action: string;
    }>(),
    {
      visible: false,
    }
  );

  const emit = defineEmits(['update:visible']);

  const showBatchModal = ref(false);
  const batchTitle = ref('');
  const target = ref<string[]>([]);
  const batchModalMode = ref<'project' | 'userGroup' | 'organization'>('project');
  const treeData = ref<OrgsItem[]>([]);
  const loading = ref(false);
  const transferData = ref<TransferDataItem[]>([]);

  /**
   * 获取穿梭框数据，根据树结构获取
   * @param _treeData 树结构
   * @param transferDataSource 穿梭框数组
   */
  const getTransferData = (_treeData: OrgsItem[], transferDataSource: TransferDataItem[]) => {
    _treeData.forEach((item) => {
      if (!item.leafNode && item.children) getTransferData(item.children, transferDataSource);
      else transferDataSource.push({ label: item.name, value: item.id, disabled: false });
    });
    return transferDataSource;
  };

  /**
   * 获取树结构数据，根据穿梭框过滤的数据获取
   */
  const getTreeData = (data: TransferDataItem[]) => {
    const values = data.map((item) => item.value);

    const travel = (_treeData: OrgsItem[]) => {
      const treeDataSource: TreeDataItem[] = [];
      _treeData.forEach((item) => {
        // 需要判断当前父节点下的子节点是否全部选中，若选中则不会 push 进穿梭框数组内，否则会出现空的节点无法选中
        const allSelected =
          target.value.length > 0 && !item.leafNode && item.children?.every((child) => target.value.includes(child.id));
        if (!allSelected && !target.value.includes(item.id) && (item.children || values.includes(item.id))) {
          treeDataSource.push({
            title: item.name,
            key: item.id,
            children: item.children ? travel(item.children) : [],
          });
        }
      });
      return treeDataSource;
    };

    return travel(treeData.value);
  };

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
      transferData.value = getTransferData(treeData.value, []);
    } catch (error) {
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
        selectAll: false,
        condition: {},
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
    } catch (error) {
      console.log(error);
    } finally {
      batchLoading.value = false;
    }
  }
</script>

<style lang="less" scoped></style>
