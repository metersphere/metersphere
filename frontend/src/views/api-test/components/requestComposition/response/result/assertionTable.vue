<template>
  <a-scrollbar class="overflow-y-auto" :style="{ height: '300px' }">
    <MsBaseTable v-bind="propsRes" v-on="propsEvent">
      <template #status="{ record }">
        <MsTag :type="record.pass === true ? 'success' : 'danger'" theme="light">
          {{ record.pass === true ? t('common.success') : t('common.fail') }}
        </MsTag>
      </template>
    </MsBaseTable>
  </a-scrollbar>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { RequestResult } from '@/models/apiTest/common';

  const { t } = useI18n();
  const props = defineProps<{
    requestResult?: RequestResult;
  }>();

  const columns: MsTableColumn = [
    {
      title: 'apiTestDebug.content',
      dataIndex: 'content',
      showTooltip: true,
    },
    {
      title: 'apiTestDebug.status',
      dataIndex: 'pass',
      slotName: 'status',
      width: 80,
    },
    {
      title: 'apiTestDebug.reason',
      dataIndex: 'message',
      showTooltip: true,
    },
  ];
  const { propsRes, propsEvent } = useTable(undefined, {
    scroll: { x: '100%' },
    columns,
  });

  watch(
    () => props.requestResult?.responseResult.assertions,
    (val) => {
      if (val) {
        propsRes.value.data = props.requestResult?.responseResult.assertions || [];
      }
    },
    {
      immediate: true,
    }
  );
</script>

<style scoped></style>
