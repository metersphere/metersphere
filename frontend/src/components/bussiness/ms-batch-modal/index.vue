<template>
  <a-modal v-model:visible="showBatchModal" title-align="start" class="ms-modal-upload ms-modal-medium">
    <template #title>
      {{ batchTitle }}
      <div class="text-[var(--color-text-4)]">
        {{ t('msbatchmodal.batchModalSubTitle', { count: props.tableSelected.length }) }}
      </div>
    </template>
    <a-alert v-if="props.action === 'batchAddProject'" class="mb-[16px]">
      {{ t('msbatchmodal.batchModalTip') }}
    </a-alert>
    <a-transfer
      v-model="target"
      :title="[t('msbatchmodal.batchOptional'), t('msbatchmodal.batchChosen')]"
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
    <template #footer>
      <a-button type="secondary" @click="cancelBatch">{{ t('msbatchmodal.batchModalCancel') }}</a-button>
      <a-button type="primary" :loading="batchLoading" @click="confirmBatch">
        {{ t('msbatchmodal.batchModalConfirm') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { useI18n } from '@/hooks/useI18n';

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
      treeData: TreeDataItem[];
    }>(),
    {
      visible: false,
    }
  );
  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
    (e: 'addProject', targetValue: string[]): void;
    (e: 'addUserGroup', targetValue: string[]): void;
    (e: 'addOrgnization', targetValue: string[]): void;
  }>();

  const showBatchModal = ref(false);
  const batchLoading = ref(false);
  const batchTitle = ref('');
  const target = ref<string[]>([]);

  function handelTableBatch(action: string) {
    switch (action) {
      case 'batchAddProject':
        batchTitle.value = t('system.user.batchAddProject');
        break;
      case 'batchAddUsergroup':
        batchTitle.value = t('system.user.batchAddUsergroup');
        break;
      case 'batchAddOrgnization':
        batchTitle.value = t('system.user.batchAddOrgnization');
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
        handelTableBatch(props.action);
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

  /**
   * 获取穿梭框数据，根据树结构获取
   * @param _treeData 树结构
   * @param transferDataSource 穿梭框数组
   */
  const getTransferData = (_treeData: TreeDataItem[], transferDataSource: TransferDataItem[]) => {
    _treeData.forEach((item) => {
      if (item.children) getTransferData(item.children, transferDataSource);
      else transferDataSource.push({ label: item.title, value: item.key, disabled: false });
    });
    return transferDataSource;
  };

  /**
   * 获取树结构数据，根据穿梭框过滤的数据获取
   */
  const getTreeData = (data: TransferDataItem[]) => {
    const values = data.map((item) => item.value);

    const travel = (_treeData: TreeDataItem[]) => {
      const treeDataSource: TreeDataItem[] = [];
      _treeData.forEach((item) => {
        // 需要判断当前父节点下的子节点是否全部选中，若选中则不会 push 进穿梭框数组内，否则会出现空的节点无法选中
        const allSelected = item.children?.every((child) => target.value.includes(child.key));
        if (!allSelected && (item.children || values.includes(item.key))) {
          treeDataSource.push({
            title: item.title,
            key: item.key,
            children: item.children ? travel(item.children) : [],
          });
        }
      });
      return treeDataSource;
    };

    return travel(props.treeData);
  };
  const transferData = getTransferData(props.treeData, []);

  function cancelBatch() {
    showBatchModal.value = false;
    target.value = [];
  }

  const confirmBatch = () => {
    batchLoading.value = true;
    try {
      if (target.value.length < 1) {
        return;
      }
      switch (props.action) {
        case 'batchAddProject':
          emit('addProject', target.value);
          break;
        case 'batchAddUsergroup':
          emit('addUserGroup', target.value);
          break;
        case 'batchAddOrgnization':
          emit('addOrgnization', target.value);
          break;
        default:
          break;
      }
      showBatchModal.value = false;
    } catch (error) {
      console.log(error);
    } finally {
      batchLoading.value = false;
    }
  };
</script>

<style lang="less" scoped></style>
