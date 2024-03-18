<template>
  <MsDrawer
    ref="detailDrawerRef"
    v-model:visible="showDrawer"
    :width="960"
    :footer="false"
    :title="t('步骤名称')"
    show-full-screen
    :unmount-on-close="true"
  >
    <template #headerLeft>
      <div class="scene-type"> API </div>
    </template>
    <div>
      <ms-base-table
        ref="tableRef"
        v-bind="propsRes"
        v-model:expandedKeys="expandedKeys"
        no-disable
        :indent-size="0"
        v-on="propsEvent"
      >
        <template #titleName>
          <div class="flex w-full justify-between">
            <div class="font-medium">响应内容</div>
            <div class="grid grid-cols-5 gap-2 text-center">
              <span>401</span>
              <span class="text-[rgb(var(--success-6))]">247ms</span>
              <span class="text-[rgb(var(--success-6))]">50bytes</span>
              <span>Mock</span>
              <span>66</span>
            </div>
          </div>
        </template>
        <template #name="{ record }">
          <span class="font-medium">
            {{ record.name }}
          </span>

          <div v-if="record.script && !record.isAssertion" class="w-full">
            <ResContent :script="record.script" language="JSON" :show-charset-change="record.script" />
          </div>
          <div v-if="record.isAssertion" class="w-full">
            <assertTable :data="record.assertions" />
          </div>
        </template>
      </ms-base-table>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsPaginationI, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import assertTable from './assertTable.vue';
  import ResContent from './resContent.vue';

  import { reportDetail } from '@/api/modules/api-test/report';
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    stepId: string;
    activeStepIndex: number;
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
  }>();

  const showDrawer = computed({
    get() {
      return props.visible;
    },
    set(val) {
      emit('update:visible', val);
    },
  });
  const innerFileId = ref(props.stepId);
  function loadedStep(detail: Record<string, any>) {
    innerFileId.value = detail.id;
  }

  const tableRef = ref<InstanceType<typeof MsBaseTable> | null>(null);

  const expandedKeys = ref<string[]>([]);

  const columns: MsTableColumn = [
    {
      title: 'report.detail.api.resContent',
      dataIndex: 'name',
      slotName: 'name',
      titleSlotName: 'titleName',
      fixed: 'left',
      headerCellClass: 'titleClass',
      bodyCellClass: (record) => {
        if (record.children) {
          return '';
        }
        return 'cellClassWrapper';
      },
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(undefined, {
    columns,
    scroll: { x: 'auto' },
    showPagination: false,
    hoverable: false,
    showExpand: true,
    rowKey: 'id',
    rowClass: (record: any) => {
      if (record.children) {
        return 'gray-td-bg';
      }
    },
  });

  const listMap = [
    {
      title: '响应体',
      value: 'body',
    },
    {
      title: '响应头',
      value: 'headers',
    },
    {
      title: '实际请求',
      value: 'request',
    },
    {
      title: '控制台',
      value: 'request',
    },
  ];

  onMounted(() => {
    // 虚拟数据
    propsRes.value.data = [
      {
        id: '1001',
        name: '响应体',
        script: '',
        isAssertion: false,
        assertions: [],
        children: [
          {
            script: '1',
            isAssertion: false,
            assertions: [],
          },
        ],
      },
      {
        id: '1002',
        name: '响应头',
        script: '',
        isAssertion: false,
        assertions: [],
        children: [
          {
            script: '2',
            isAssertion: false,
            assertions: [],
          },
        ],
      },
      {
        id: '1003',
        name: '实际请求',
        script: '',
        isAssertion: false,
        assertions: [],
        children: [
          {
            script: '3',
            isAssertion: false,
            assertions: [],
          },
        ],
      },
      {
        id: '1004',
        name: '控制台',
        script: '',
        isAssertion: false,
        assertions: [],
        children: [
          {
            script: '4',
            isAssertion: false,
            assertions: [],
          },
        ],
      },
      {
        id: '1005',
        name: '提取',
        script: '',
        isAssertion: false,
        assertions: [],
        children: [
          {
            script: '1',
            isAssertion: false,
            assertions: [],
          },
        ],
      },
      {
        id: '1007',
        name: '断言',
        script: '',
        isAssertion: false,
        assertions: [],
        children: [
          {
            script: '1',
            isAssertion: true,
            assertions: [
              {
                name: 'string',
                content: 'string',
                script: 'string',
                message: 'string',
                pass: true,
              },
            ],
          },
        ],
      },
    ];
  });
</script>

<style scoped lang="less">
  .scene-type {
    margin-left: 8px;
    padding: 0 2px;
    font-size: 12px;
    line-height: 16px;
    border: 1px solid rgb(var(--link-6));
    border-radius: 0 12px 12px 0;
    color: rgb(var(--link-6));
  }
  .resContentHeader {
    @apply flex justify-between;
  }
  :deep(.gray-td-bg) {
    td {
      background-color: var(--color-text-n9);
    }
  }
  :deep(.titleClass .arco-table-th-title) {
    @apply w-full;
  }
  :deep(.cellClassWrapper .arco-table-cell) {
    padding: 0 !important;
    span {
      padding-left: 0 !important;
    }
  }
</style>
