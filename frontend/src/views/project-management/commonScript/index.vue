<template>
  <MsCard simple>
    <div class="mb-4 flex items-center justify-between">
      <a-button type="outline" @click="addCommonScript"> {{ t('project.commonScript.addPublicScript') }} </a-button>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('project.commonScript.searchByNameAndId')"
        allow-clear
        class="mx-[8px] w-[240px]"
      />
    </div>
    <ms-base-table v-bind="propsRes" v-on="propsEvent">
      <template #name="{ record }">
        <div class="flex items-center">
          <div
            class="one-line-text max-w-[200px] cursor-pointer text-[rgb(var(--primary-5))]"
            @click="showDetail(record)"
            >{{ record.name }}</div
          >
          <a-popover :title="t('project.commonScript.publicScriptName')" position="right">
            <a-button type="text" class="ml-2 px-0">{{ t('project.commonScript.preview') }}</a-button>
            <template #content>
              <div class="w-[436px] bg-[var(--color-bg-3)] px-2 pb-2">
                <MsCodeEditor
                  v-model:model-value="record.name"
                  :show-theme-change="false"
                  title=""
                  width="100%"
                  height="376px"
                  theme="MS-text"
                  :read-only="false"
                  :show-full-screen="false"
                />
              </div>
            </template>
          </a-popover>
        </div>
      </template>
      <template #enable="{ record }">
        <MsTag v-if="record.enable" type="success" theme="light">{{ t('project.commonScript.testsPass') }}</MsTag>
        <MsTag v-else>{{ t('project.commonScript.draft') }}</MsTag>
      </template>
      <template #operation="{ record }">
        <MsButton status="primary">
          {{ t('common.edit') }}
        </MsButton>
        <MsTableMoreAction
          v-if="!record.internal"
          :list="actions"
          @select="(item:ActionsItem) => handleMoreActionSelect(item,record)"
      /></template>
    </ms-base-table>
    <template v-if="(keyword || '').trim() === ''" #empty>
      <div class="flex items-center justify-center">
        {{ t('caseManagement.caseReview.tableNoData') }}
        <MsButton class="ml-[8px]"> {{ t('project.commonScript.addPublicScript') }} </MsButton>
      </div>
    </template>
    <AddScriptDrawer v-model:visible="showScriptDrawer" @save="saveHandler" />
    <ScriptDetailDrawer v-model:visible="showDetailDrawer" />
  </MsCard>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  // import AddScriptDrawer from './components/addScriptDrawer.vue';
  // import ScriptDetailDrawer from './components/scriptDetailDrawer.vue';
  import AddScriptDrawer from '@/components/business/ms-common-script/ms-addScriptDrawer.vue';
  import ScriptDetailDrawer from './components/scriptDetailDrawer.vue';

  import { getDependOnCase } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore, useTableStore } from '@/store';
  import { characterLimit } from '@/utils';

  import { TableKeyEnum } from '@/enums/tableEnum';

  const tableStore = useTableStore();

  const { openModal } = useModal();

  const { t } = useI18n();

  const keyword = ref<string>('');

  const columns: MsTableColumn = [
    {
      title: 'project.commonScript.name',
      dataIndex: 'name',
      slotName: 'name',
      width: 300,
      showInTable: true,
    },
    {
      title: 'project.commonScript.description',
      slotName: 'description',
      dataIndex: 'description',
      width: 200,
      showDrag: true,
    },
    {
      title: 'project.commonScript.enable',
      dataIndex: 'enable',
      slotName: 'enable',
      showInTable: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'project.commonScript.tags',
      dataIndex: 'tags',
      slotName: 'tags',
      showInTable: true,
      isTag: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'project.commonScript.createUser',
      slotName: 'createUser',
      dataIndex: 'createUser',
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'project.commonScript.createTime',
      slotName: 'createTime',
      dataIndex: 'createTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
      },
      showInTable: true,
      width: 300,
      showDrag: true,
    },
    {
      title: 'system.resourcePool.tableColumnUpdateTime',
      dataIndex: 'updateTime',
      width: 180,
      showDrag: true,
    },
    {
      title: 'project.commonScript.tableColumnActions',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 140,
      showInTable: true,
      showDrag: false,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector, setProps } = useTable(getDependOnCase, {
    scroll: { x: '100%' },
    tableKey: TableKeyEnum.ORGANIZATION_PROJECT_COMMON_SCRIPT,
    selectable: true,
    heightUsed: 340,
    showSetting: true,
    enableDrag: true,
  });

  const actions: ActionsItem[] = [
    {
      label: t('common.delete'),
      danger: true,
      eventTag: 'delete',
    },
  ];

  function deleteScript(record: Record<string, any>) {
    openModal({
      type: 'error',
      title: t('project.commonScript.deleteTitleTip', { name: characterLimit(record.name) }),
      content: t('project.commonScript.deleteTitleContent'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          Message.success(t('common.deleteSuccess'));
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  function handleMoreActionSelect(item: ActionsItem, record: Record<string, any>) {
    if (item.eventTag === 'delete') {
      deleteScript(record);
    }
  }

  onMounted(() => {
    // setLoadListParams({});
    // loadList();
    setProps({
      data: [
        // {
        //   name: '哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈',
        //   description: 'description',
        //   enable: true,
        //   tags: [{ id: '1001', name: 'A' }],
        // },
      ],
    });
  });
  tableStore.initColumn(TableKeyEnum.ORGANIZATION_PROJECT_COMMON_SCRIPT, columns, 'drawer');

  const showScriptDrawer = ref<boolean>(false);

  const showDetailDrawer = ref<boolean>(false);
  // 脚本详情
  function showDetail(record: any) {
    showDetailDrawer.value = true;
  }

  // 保存自定义代码片段应用
  function saveHandler(isDraft: boolean) {}

  function addCommonScript() {
    showScriptDrawer.value = true;
  }
</script>

<style scoped></style>
