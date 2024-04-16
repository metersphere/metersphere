<template>
  <a-scrollbar class="overflow-y-auto" :style="{ height: '300px' }">
    <MsFormTable
      :data="props.requestResult?.responseResult.assertions"
      :columns="columns"
      :selectable="false"
      :scroll="props.scroll"
    >
      <template #status="{ record }">
        <MsTag :type="record.pass === true ? 'success' : 'danger'" theme="light">
          {{ record.pass === true ? t('common.success') : t('common.fail') }}
        </MsTag>
      </template>
    </MsFormTable>
  </a-scrollbar>
</template>

<script setup lang="ts">
  import MsFormTable from '@/components/pure/ms-form-table/index.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { RequestResult } from '@/models/apiTest/common';

  const { t } = useI18n();
  const props = defineProps<{
    requestResult?: RequestResult;
    scroll?: {
      x?: number | string;
      y?: number | string;
      maxHeight?: number | string;
      minWidth?: number | string;
    };
  }>();

  const columns: MsTableColumn = [
    {
      title: 'apiTestDebug.content',
      dataIndex: 'content',
      showTooltip: true,
      width: 300,
    },
    {
      title: 'apiTestDebug.status',
      dataIndex: 'pass',
      slotName: 'status',
      width: 120,
    },
    {
      title: 'apiTestDebug.reason',
      dataIndex: 'message',
      showTooltip: true,
      width: 300,
    },
  ];
</script>

<style scoped></style>
