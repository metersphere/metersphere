<template>
  <a-modal
    v-model:visible="innerVisible"
    class="ms-modal-form ms-modal-large"
    title-align="start"
    :ok-text="t('system.userGroup.add')"
    unmount-on-close
    @cancel="handleCancel"
  >
    <template #title> 域名列表 </template>
    <div>
      <MsBaseTable class="mt-[16px]" v-bind="propsRes" v-on="propsEvent">
        <template #type="{ record }">
          <span>{{ getEnableScope(record.type) }}</span>
        </template>
        <template #moduleValue="{ record }">
          <a-tooltip :content="getModuleName(record)" position="left">
            <span class="one-line-text max-w-[300px]">{{ getModuleName(record) }}</span>
          </a-tooltip>
        </template>
      </MsBaseTable>
    </div>
    <template #footer>
      <a-button type="secondary" @click="handleCancel">
        {{ t('common.cancel') }}
      </a-button>
      <a-button type="primary" @click="handleCancel">
        {{ t('common.confirm') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useVModel } from '@vueuse/core';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { getEnvModules } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { findNodeNames } from '@/utils';

  import type { ModuleTreeNode } from '@/models/common';
  import { HttpForm } from '@/models/projectManagement/environmental';

  const appStore = useAppStore();

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    data: HttpForm[];
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
  }>();

  const innerVisible = useVModel(props, 'visible', emit);

  function handleCancel() {
    innerVisible.value = false;
  }

  const columns: MsTableColumn = [
    {
      title: 'project.environmental.http.host',
      dataIndex: 'hostname',
      slotName: 'hostname',
      showTooltip: true,
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'common.desc',
      dataIndex: 'description',
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'project.environmental.http.enableScope',
      dataIndex: 'type',
      slotName: 'type',
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'project.environmental.http.value',
      dataIndex: 'value',
      slotName: 'moduleValue',
      showTooltip: false,
      showDrag: true,
      showInTable: true,
    },
  ];

  const { propsRes, propsEvent } = useTable(undefined, {
    columns,
    scroll: { x: '100%' },
    selectable: false,
    noDisable: true,
    showSetting: false,
    showPagination: false,
    showMode: false,
    heightUsed: 644,
    debug: true,
  });

  function getEnableScope(type: string) {
    switch (type) {
      case 'NONE':
        return t('project.environmental.http.none');
      case 'MODULE':
        return t('project.environmental.http.module');
      case 'PATH':
        return t('project.environmental.http.path');
      default:
        break;
    }
  }

  const moduleTree = ref<ModuleTreeNode[]>([]);
  async function initModuleTree() {
    try {
      const res = await getEnvModules({
        projectId: appStore.currentProjectId,
      });
      moduleTree.value = res.moduleTree;
    } catch (error) {
      console.log(error);
    }
  }
  const OPERATOR_MAP = [
    {
      value: 'CONTAINS',
      label: '包含',
    },
    {
      value: 'EQUALS',
      label: '等于',
    },
  ];

  function getCondition(condition: string) {
    return OPERATOR_MAP.find((item) => item.value === condition)?.label;
  }
  function getModuleName(record: HttpForm) {
    if (record.type === 'MODULE') {
      const moduleIds: string[] = record.moduleMatchRule.modules.map((item) => item.moduleId);
      const result = findNodeNames(moduleTree.value, moduleIds);
      return result.join(',');
    }
    if (record.type === 'PATH') {
      return `${getCondition(record.pathMatchRule.condition)}${record.pathMatchRule.path}`;
    }
    return '-';
  }

  watch(
    () => props.data,
    (val) => {
      if (val) {
        propsRes.value.data = val;
      }
    }
  );
</script>

<style scoped></style>
