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
      <template #organization="{ record }">
        <a-tag
          v-for="org of record.organizationList.slice(0, 2)"
          :key="org.id"
          class="mr-[4px] border-[rgb(var(--primary-5))] bg-transparent !text-[rgb(var(--primary-5))]"
          bordered
        >
          {{ org.name }}
        </a-tag>
        <a-tag
          v-show="record.organizationList.length > 2"
          class="mr-[4px] border-[rgb(var(--primary-5))] bg-transparent !text-[rgb(var(--primary-5))]"
          bordered
        >
          +{{ record.organizationList.length - 2 }}
        </a-tag>
      </template>
      <template #action="{ record }">
        <MsButton @click="update(record)">{{ t('system.plugin.edit') }}</MsButton>
        <MsButton @click="changeScene(record)">{{ t('system.plugin.ChangeScene') }}</MsButton>
        <MsTableMoreAction :list="tableActions" @select="handleSelect($event, record)"></MsTableMoreAction>
      </template>
    </ms-base-table>
    <UploadModel :visible="uploadVisible" @cancel="uploadVisible = false" @upload="uploadPlugin" @success="okHandler" />
    <UpdatePluginModal ref="updateModalRef" :visible="updateVisible" @cancel="updateVisible = false" />
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted, reactive } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import { getPluginList } from '@/api/modules/system/pluginManger';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import UploadModel from './uploadModel.vue';
  import UpdatePluginModal from './updatePluginModal.vue';
  import uploadSuccessModal from './uploadSuccessModal.vue';
  import sceneChangeModal from './sceneChangeModal.vue';
  import { useCommandComponent } from '@/hooks/useCommandComponent';

  const { t } = useI18n();
  export type Options = {
    title: string;
    visible: boolean;
    onClose?: () => void;
  };
  const columns: MsTableColumn = [
    {
      title: 'system.plugin.tableColunmName',
      dataIndex: 'name',
      width: 200,
      fixed: 'left',
    },
    {
      title: 'system.plugin.tableColunmDescription',
      dataIndex: 'describe',
    },
    {
      title: 'system.plugin.tableColunmApplicationScene',
      dataIndex: 'applicationScene',
    },
    {
      title: 'system.user.tableColunmOrg',
      slotName: 'organization',
      dataIndex: 'organizationList',
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
      title: 'system.plugin.tableColunmAuthorization',
      dataIndex: 'authorizationType',
    },
    {
      title: 'system.plugin.tableColunmCreatedBy',
      dataIndex: 'createdBy',
    },
    {
      title: 'system.plugin.tableColunmExpirationDate',
      dataIndex: 'expirationDate',
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
    scroll: { y: 'auto', x: 1800 },
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
  const updateVisible = ref<boolean>(false);
  const updateModalRef = ref();
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
  function update(record: any) {
    updateVisible.value = true;
    updateModalRef.value.title = record.name;
  }
  const myUploadSuccessDialog = useCommandComponent(uploadSuccessModal);
  const mySceneChangeDialog = useCommandComponent(sceneChangeModal);
  const uploadSuccessOptions = reactive({
    title: '上传插件',
    visible: false,
    onClose: () => {
      myUploadSuccessDialog.close();
    },
  });
  const sceneChangeOptions = reactive({
    title: '场景变更-(插件名称)',
    visible: false,
    onClose: () => {
      myUploadSuccessDialog.close();
    },
  });

  const dialogOpen = (options: Options) => {
    options.visible = true;
    myUploadSuccessDialog(uploadSuccessOptions);
  };
  const okHandler = () => {
    dialogOpen(uploadSuccessOptions);
  };
  const changeScene = () => {
    sceneChangeOptions.visible = true;
    mySceneChangeDialog(sceneChangeOptions);
  };
</script>

<style scoped></style>
