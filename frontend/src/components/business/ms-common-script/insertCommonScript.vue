<template>
  <MsDrawer
    v-model:visible="insertScriptDrawer"
    :title="t('project.commonScript.insertCommonScript')"
    :width="768"
    unmount-on-close
    :show-continue="false"
    :ok-loading="drawerLoading"
    :ok-text="t('project.commonScript.apply')"
    @confirm="handleDrawerConfirm"
    @cancel="handleDrawerCancel"
  >
    <div class="mb-4 flex items-center justify-between">
      <div class="font-medium">{{ t('project.commonScript.commonScriptList') }}</div>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('caseManagement.featureCase.searchByNameAndId')"
        allow-clear
        class="mx-[8px] w-[240px]"
        @search="searchList"
        @press-enter="searchList"
      ></a-input-search>
    </div>

    <ms-base-table v-bind="propsRes" v-on="propsEvent">
      <template #name="{ record }">
        <div class="flex items-center">
          <div class="one-line-text max-w-[200px] cursor-pointer text-[rgb(var(--primary-5))]">{{ record.name }}</div>
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
    </ms-base-table>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { getDependOnCase } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';

  import debounce from 'lodash-es/debounce';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
  }>();

  const emit = defineEmits(['update:visible', 'save']);
  const insertScriptDrawer = computed({
    get() {
      return props.visible;
    },
    set(val) {
      emit('update:visible', val);
    },
  });

  const keyword = ref<string>('');

  function initData() {}

  const searchList = debounce(() => {
    initData();
  }, 100);

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
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    getDependOnCase,
    {
      columns,
      scroll: {
        x: '100%',
      },
      showSetting: false,
      selectable: true,
      heightUsed: 300,
      showSelectAll: true,
    },
    (record) => {
      return {
        ...record,
        tags: (JSON.parse(record.tags) || []).map((item: string, i: number) => {
          return {
            id: `${record.id}-${i}`,
            name: item,
          };
        }),
      };
    }
  );

  const drawerLoading = ref<boolean>(false);

  function handleDrawerConfirm() {
    emit('save');
  }

  function handleDrawerCancel() {
    insertScriptDrawer.value = false;
  }
</script>

<style scoped></style>
