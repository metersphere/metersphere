<template>
  <MsDrawer
    v-model:visible="insertScriptDrawer"
    :title="t('project.commonScript.insertCommonScript')"
    :width="768"
    unmount-on-close
    :show-continue="false"
    :ok-loading="drawerLoading"
    :ok-disabled="!isDisabled"
    :ok-text="t('project.commonScript.apply')"
    @confirm="handleDrawerConfirm"
    @cancel="handleDrawerCancel"
  >
    <div class="mb-4 flex items-center justify-between">
      <div v-if="propsRes.data.length" class="font-medium">{{ t('project.commonScript.commonScriptList') }}</div>
      <a-button v-else type="outline" @click="addCommonScript">
        {{ t('project.commonScript.addPublicScript') }}
      </a-button>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('caseManagement.featureCase.searchByNameAndId')"
        allow-clear
        class="mx-[8px] w-[240px]"
        @search="searchList"
        @press-enter="searchList"
      ></a-input-search>
    </div>

    <ms-base-table v-bind="propsRes" ref="tableRef" v-on="propsEvent">
      <template #radio="{ record }">
        <a-radio v-model="record.checked" @change="(value) => changeRadio(value, record)"></a-radio>
      </template>
      <template #name="{ record }">
        <div class="flex items-center">
          <div class="one-line-text max-w-[200px] cursor-pointer text-[rgb(var(--primary-5))]">{{ record.name }}</div>
          <a-popover :title="t('project.commonScript.publicScriptName')" position="right">
            <a-button type="text" class="ml-2 px-0">{{ t('project.commonScript.preview') }}</a-button>
            <template #content>
              <div class="w-[436px] bg-[var(--color-bg-3)] px-2 pb-2">
                <MsCodeEditor
                  v-model:model-value="record.script"
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

  import { getInsertCommonScriptPage } from '@/api/modules/project-management/commonScript';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { type Languages } from './utils';
  import debounce from 'lodash-es/debounce';

  const appStore = useAppStore();
  const currentProjectId = computed(() => appStore.currentProjectId);
  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    scriptLanguage: Languages;
    enableRadioSelected?: boolean; // 是否单选开启
    okText?: string;
  }>();

  const emit = defineEmits(['update:visible', 'save', 'addScript']);
  const insertScriptDrawer = computed({
    get() {
      return props.visible;
    },
    set(val) {
      emit('update:visible', val);
    },
  });

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
  ];

  const tableRef = ref();

  const radioColumn = [
    {
      title: '',
      dataIndex: 'radio',
      slotName: 'radio',
      width: 50,
      showInTable: true,
    },
  ];
  watch(
    () => insertScriptDrawer.value,
    (val) => {
      if (val) {
        if (props.enableRadioSelected) {
          const result = [...radioColumn, ...columns];
          tableRef.value.initColumn(result);
        }
      }
    }
  );

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector, setProps } = useTable(
    getInsertCommonScriptPage,
    {
      columns,
      scroll: {
        x: '100%',
      },
      showSetting: false,
      selectable: !props.enableRadioSelected,
      heightUsed: 300,
      showSelectAll: !props.enableRadioSelected,
    },
    (record) => {
      return {
        ...record,
        tags: (record.tags || []).map((item: string, i: number) => {
          return {
            id: `${record.id}-${i}`,
            name: item,
          };
        }),
      };
    }
  );

  const drawerLoading = ref<boolean>(false);

  const isDisabled = computed(() => {
    if (props.enableRadioSelected) {
      return propsRes.value.data.filter((item) => item.checked).length > 0;
    }
    return propsRes.value.selectedKeys.size > 0;
  });

  function initData() {
    setLoadListParams({
      keyword: keyword.value,
      projectId: currentProjectId.value,
      type: props.scriptLanguage,
    });
    loadList();
  }

  const searchList = debounce(() => {
    initData();
  }, 100);

  function handleDrawerConfirm() {
    if (props.enableRadioSelected) {
      emit(
        'save',
        propsRes.value.data.find((item) => item.checked)
      );
    } else {
      const selectKeysIds = [...propsRes.value.selectedKeys];
      emit(
        'save',
        propsRes.value.data.filter((item) => selectKeysIds.includes(item.id))
      );
    }
  }

  function handleDrawerCancel() {
    insertScriptDrawer.value = false;
  }

  function addCommonScript() {
    emit('addScript');
  }

  function changeRadio(value, record) {
    propsRes.value.data.forEach((item) => {
      item.checked = false;
    });
    record.checked = true;
    setProps({ data: propsRes.value.data });
  }

  watch(
    () => props.visible,
    (val) => {
      if (val) {
        resetSelector();
        initData();
      }
    }
  );
</script>

<style scoped></style>
