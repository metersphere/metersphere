<template>
  <div class="p-[16px]">
    <div class="flex flex-row justify-between">
      <a-dropdown trigger="hover">
        <template #content>
          <a-doption @click="showRelatedDrawer('api')">{{ t('bugManagement.detail.apiCase') }}</a-doption>
          <a-doption @click="showRelatedDrawer('scenario')">{{ t('bugManagement.detail.scenarioCase') }}</a-doption>
          <a-doption @click="showRelatedDrawer('ui')">{{ t('bugManagement.detail.uiCase') }}</a-doption>
          <a-doption @click="showRelatedDrawer('performance')">{{
            t('bugManagement.detail.performanceCase')
          }}</a-doption>
        </template>
        <a-button type="primary">{{ t('bugManagement.edit.linkCase') }}</a-button>
      </a-dropdown>
      <a-input-search
        v-model:model-value="keyword"
        allow-clear
        :placeholder="t('bugManagement.detail.searchCase')"
        class="w-[230px]"
        @search="searchUser"
        @press-enter="searchUser"
      ></a-input-search>
    </div>
    <ms-base-table class="mt-[16px]" v-bind="propsRes" v-on="propsEvent">
      <template #name="{ record }">
        <span>{{ record.name }}</span>
        <span v-if="record.adminFlag" class="ml-[4px] text-[var(--color-text-4)]">{{ `(${t('common.admin')})` }}</span>
      </template>
      <template #operation="{ record }">
        <MsRemoveButton
          :title="t('system.organization.removeName', { name: record.name })"
          :sub-title-tip="t('system.organization.removeTip')"
          @ok="handleRemove()"
        />
      </template>
    </ms-base-table>
  </div>
  <MsDrawer
    :width="680"
    :visible="relatedVisible"
    unmount-on-close
    :footer="false"
    :mask="false"
    @cancel="relatedVisible = false"
  >
    <template #title>
      <div class="flex flex-row items-center gap-[4px]">
        <div>{{ t('bugManagement.detail.relatedCase') }}</div>
        <a-select>
          <a-option>1</a-option>
          <a-option>2</a-option>
          <a-option>3</a-option>
        </a-select>
      </div>
    </template>
  </MsDrawer>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsRemoveButton from '@/components/business/ms-remove-button/MsRemoveButton.vue';

  import { postProjectMemberByProjectId } from '@/api/modules/setting/organizationAndProject';
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const relatedVisible = ref(false);
  const relatedType = ref('api');

  const showRelatedDrawer = (type: string) => {
    relatedType.value = type;
  };

  const keyword = ref('');

  const projectColumn: MsTableColumn = [
    {
      title: 'system.organization.userName',
      slotName: 'name',
      dataIndex: 'name',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'system.organization.email',
      dataIndex: 'email',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'system.organization.phone',
      dataIndex: 'phone',
    },
    { title: 'system.organization.operation', slotName: 'operation' },
  ];

  const { propsRes, propsEvent, loadList, setKeyword } = useTable(postProjectMemberByProjectId, {
    heightUsed: 240,
    columns: projectColumn,
    scroll: { x: '100%' },
    selectable: false,
    noDisable: false,
    pageSimple: true,
  });

  async function searchUser() {
    setKeyword(keyword.value);
    await loadList();
  }

  const fetchData = async () => {
    await loadList();
  };

  const handleRemove = async () => {
    try {
      Message.success(t('common.removeSuccess'));
      fetchData();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  };
</script>

<style lang="less" scoped>
  :deep(.custom-height) {
    height: 100vh !important;
    border: 1px solid red;
  }
</style>
