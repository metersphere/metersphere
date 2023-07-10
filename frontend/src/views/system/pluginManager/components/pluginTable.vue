<!-- eslint-disable vue/valid-v-for -->
<template>
  <div>
    <div class="mb-4">
      <a-row class="grid-demo" :gutter="24">
        <a-col :span="5">
          <a-button class="mr-3" type="primary" @click="uploadPlugin">{{ t('system.plugin.uploadPlugin') }}</a-button>
        </a-col>
        <a-col :span="5" :offset="9">
          <a-select v-model="scene" allow-clear>
            <a-option v-for="item of sceneList" :key-="item.value" :value="item.value">{{ item.label }}</a-option>
          </a-select>
        </a-col>
        <a-col :span="5">
          <a-input-search :placeholder="t('system.plugin.searchPlugin')" @search="searchPlugin"></a-input-search>
        </a-col>
      </a-row>
    </div>
    <ms-base-table v-bind="propsRes" v-on="propsEvent">
      <template #action="{ record }">
        <MsButton>{{ t('system.plugin.edit') }}</MsButton>
        <MsButton>{{ t('system.plugin.ChangeScene') }}</MsButton>
        <MsTableMoreAction :list="tableActions" @select="handleSelect($event, record)"></MsTableMoreAction>
      </template>
    </ms-base-table>
    <uploadModel :visible="uploadVisible" @cancel="uploadVisible = false" />
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import { getPluginList } from '@/api/modules/system/pluginManger';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import uploadModel from './uploadModel.vue';

  const { t } = useI18n();
  const columns: MsTableColumn = [
    {
      title: 'system.plugin.tableColunmName',
      dataIndex: 'name',
      width: 200,
    },
    {
      title: 'system.plugin.tableColunmDescription',
      dataIndex: 'describe',
    },
    {
      title: 'system.plugin.tableColunmJarPackage',
      dataIndex: 'jarPackage',
    },
    {
      title: 'system.plugin.tableColunmVersion',
      dataIndex: 'version',
    },
    {
      title: 'system.plugin.tableColunmApplicationScene',
      dataIndex: 'applicationScene',
    },
    {
      title: 'system.plugin.tableColunmActions',
      slotName: 'action',
      fixed: 'right',
      width: 150,
    },
  ];
  const tableActions: ActionsItem[] = [
    {
      label: 'system.plugin.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];
  const { propsRes, propsEvent, loadList, setKeyword } = useTable(getPluginList, {
    columns,
    scroll: { y: 'auto' },
    selectable: false,
    showSelectAll: false,
  });
  const keyword = ref('');
  const scene = ref('1');
  const sceneList = ref([
    {
      label: '全部应用场景',
      value: '1',
    },
  ]);
  const uploadVisible = ref<boolean>(false);

  onMounted(async () => {
    setKeyword(keyword.value);
    await loadList();
  });
  async function searchPlugin() {
    setKeyword(keyword.value);
    await loadList();
  }
  function deletePlugin() {}
  /**
   * 处理表格更多按钮事件
   * @param item
   */
  function handleSelect(item: ActionsItem, record: any) {
    switch (item.eventTag) {
      case 'delete':
        deletePlugin();
        break;
      default:
        break;
    }
  }
  function uploadPlugin() {
    uploadVisible.value = true;
  }
</script>

<style scoped></style>
