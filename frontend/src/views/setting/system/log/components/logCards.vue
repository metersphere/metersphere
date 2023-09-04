<template>
  <MsCard simple auto-height>
    <div class="filter-box">
      <MsSearchSelect
        v-model:model-value="operUser"
        placeholder="system.log.operatorPlaceholder"
        prefix="system.log.operator"
        :options="userList"
        :search-keys="['label', 'email']"
        :option-label-render="
          (item) => `${item.label}<span class='text-[var(--color-text-2)]'>（${item.email}）</span>`
        "
        allow-clear
      />
      <a-range-picker
        v-model:model-value="time"
        show-time
        :time-picker-props="{
          defaultValue: ['00:00:00', '00:00:00'],
        }"
        :disabled-date="disabledDate"
        class="filter-item"
        :allow-clear="false"
        :disabled-input="false"
        value-format="timestamp"
        @select="selectTime"
      >
        <template #prefix>
          {{ t('system.log.operateTime') }}
        </template>
      </a-range-picker>
      <MsCascader
        v-model:model-value="operateRange"
        v-model:level="level"
        :options="rangeOptions"
        :prefix="t('system.log.operateRange')"
        :level-top="[...MENU_LEVEL]"
        :virtual-list-props="{ height: 200 }"
        :loading="rangeLoading"
      />
      <a-select v-model:model-value="type" class="filter-item">
        <template #prefix>
          {{ t('system.log.operateType') }}
        </template>
        <a-option v-for="opt of typeOptions" :key="opt.value" :value="opt.value">{{ t(opt.label) }}</a-option>
      </a-select>
      <MsCascader
        v-model:model-value="_module"
        :options="moduleOptions"
        mode="native"
        :prefix="t('system.log.operateTarget')"
        :virtual-list-props="{ height: 200 }"
        :placeholder="t('system.log.operateTargetPlaceholder')"
        :panel-width="100"
        strictly
      />
      <a-input
        v-model:model-value="content"
        class="filter-item"
        :placeholder="t('system.log.operateNamePlaceholder')"
        allow-clear
      >
        <template #prefix>
          {{ t('system.log.operateName') }}
        </template>
      </a-input>
    </div>
    <a-button type="outline" @click="searchLog">{{ t('system.log.search') }}</a-button>
    <a-button type="outline" class="arco-btn-outline--secondary ml-[8px]" @click="resetFilter">
      {{ t('system.log.reset') }}
    </a-button>
  </MsCard>
  <div class="log-card">
    <div class="log-card-header">
      <div class="font-medium text-[var(--color-text-000)]">{{ t('system.log.log') }}</div>
    </div>
    <ms-base-table v-bind="propsRes" no-disable sticky-header v-on="propsEvent">
      <template #range="{ record }">
        {{
          record.organizationId === 'SYSTEM'
            ? t('system.log.system')
            : `${record.organizationName}${record.projectName ? `/${record.projectName}` : ''}`
        }}
      </template>
      <template #module="{ record }">
        {{ getModuleLocale(record.module) }}
      </template>
      <template #type="{ record }">
        {{ t(typeOptions.find((e) => e.value === record.type)?.label || '') }}
      </template>
      <template #content="{ record }">
        <div v-if="record.module === 'SYSTEM'">{{ record.content }}</div>
        <MsButton v-else @click="handleNameClick(record)">{{ record.content }}</MsButton>
      </template>
    </ms-base-table>
  </div>
</template>

<script lang="tsx" setup>
  import { onBeforeMount, ref } from 'vue';
  import dayjs from 'dayjs';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import { useI18n } from '@/hooks/useI18n';
  import usePathMap from '@/hooks/usePathMap';
  import {
    getSystemLogList,
    getSystemLogOptions,
    getSystemLogUsers,
    getOrgLogList,
    getOrgLogOptions,
    getOrgLogUsers,
  } from '@/api/modules/setting/log';
  import MsCascader from '@/components/business/ms-cascader/index.vue';
  import useTableStore from '@/store/modules/ms-table';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import { MENU_LEVEL } from '@/config/pathMap';
  import MsSearchSelect from '@/components/business/ms-search-select/index';
  import useAppStore from '@/store/modules/app';

  import type { CascaderOption, SelectOptionData } from '@arco-design/web-vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import type { LogItem, LogOptions, UserItem } from '@/models/setting/log';
  import type { CommonList } from '@/models/common';

  const props = defineProps<{
    mode: (typeof MENU_LEVEL)[number]; // 日志展示模式，系统/组织/项目
  }>();
  const { t } = useI18n();
  const appStore = useAppStore();

  // 系统/组织/项目 三种级别的接口映射
  const requestFuncMap: Record<
    (typeof MENU_LEVEL)[number],
    {
      listFunc: (data: any) => Promise<CommonList<LogItem>>;
      optionsFunc: (id: any) => Promise<LogOptions>;
      usersFunc: any;
    }
  > = {
    [MENU_LEVEL[0]]: {
      listFunc: getSystemLogList,
      optionsFunc: getSystemLogOptions,
      usersFunc: getSystemLogUsers,
    },
    [MENU_LEVEL[1]]: {
      listFunc: getOrgLogList,
      optionsFunc: getOrgLogOptions,
      usersFunc: getOrgLogUsers,
    },
    [MENU_LEVEL[2]]: {
      listFunc: getOrgLogList,
      optionsFunc: getOrgLogOptions,
      usersFunc: getOrgLogUsers,
    },
  };

  const operUser = ref(''); // 操作人
  const userList = ref<SelectOptionData[]>([]); // 操作人列表

  /**
   * 初始化用户选项列表
   */
  async function initUserList() {
    try {
      const res = await requestFuncMap[props.mode].usersFunc(appStore.currentOrgId);
      userList.value = res.map((e: UserItem) => ({
        id: e.id,
        value: e.id,
        label: e.name,
        email: e.email,
      }));
    } catch (error) {
      console.log(error);
    }
  }

  const operateRange = ref<(string | number | Record<string, any>)[]>([props.mode]); // 操作范围
  const rangeOptions = ref<CascaderOption[]>( // 系统级别才展示系统级别选项
    props.mode === MENU_LEVEL[0]
      ? [
          {
            value: {
              level: 0,
              value: MENU_LEVEL[0],
            },
            label: t('system.log.system'),
            isLeaf: true,
          },
        ]
      : []
  );
  const rangeLoading = ref(false);

  /**
   * 初始化操作范围级联选项
   */
  async function initRangeOptions() {
    try {
      rangeLoading.value = true;
      const res = await requestFuncMap[props.mode].optionsFunc(appStore.currentOrgId);
      if (props.mode === MENU_LEVEL[0]) {
        // 系统级别才展示，组织和项目级别不展示
        rangeOptions.value.push({
          value: {
            level: 0,
            value: MENU_LEVEL[1], // 顶级范围-组织，单选
          },
          label: t('system.log.organization'),
          children: res.organizationList.map((e) => ({
            // 组织列表，多选
            value: {
              level: MENU_LEVEL[1],
              value: e.id,
            },
            label: e.name,
            isLeaf: true,
          })),
        });
      } else if (props.mode === MENU_LEVEL[1]) {
        rangeOptions.value.push({
          value: {
            level: 0,
            value: MENU_LEVEL[1], // 顶级范围-组织，单选
          },
          label: t('system.log.organization'),
        });
      }
      rangeOptions.value.push({
        value: {
          level: 0,
          value: MENU_LEVEL[2], // 顶级范围-项目，单选
        },
        label: t('system.log.project'),
        children: res.projectList.map((e) => ({
          // 项目列表，多选
          value: {
            level: MENU_LEVEL[2],
            value: e.id,
          },
          label: e.name,
          isLeaf: true,
        })),
      });
    } catch (error) {
      console.log(error);
    } finally {
      rangeLoading.value = false;
    }
  }

  const level = ref<(typeof MENU_LEVEL)[number]>(props.mode); // 操作范围级别，系统/组织/项目
  const type = ref(''); // 操作类型
  const _module = ref(''); // 操作对象
  const content = ref(''); // 名称
  const time = ref<(Date | string | number)[]>([dayjs().subtract(1, 'M').valueOf(), dayjs().valueOf()]); // 操作时间
  const selectedTime = ref<Date | string | number | undefined>(''); // 日期选择器选中但未确认的时间

  /**
   * 不可选日期
   * @param current 日期选择器当前日期
   * @return boolean true-不可选，false-可选
   */
  function disabledDate(current?: Date) {
    const now = dayjs();
    const endDate = dayjs(current);
    // 限制不可选日期范围：明天以后&点击的日期前后 6 个月以外的日期（也就是可选时间跨度为 6 个月）
    return (
      (!!current && endDate.isAfter(now)) ||
      (selectedTime.value ? Math.abs(dayjs(selectedTime.value).diff(endDate, 'months')) > 5 : false)
    );
  }

  /**
   * 日期选择器点击选中的日期但未确认时
   * @param val 点击的日期
   */
  function selectTime(val: (Date | string | number | undefined)[] | undefined) {
    const arr = val?.filter((e) => e) || [];
    if (arr.length === 1) {
      [selectedTime.value] = arr;
    }
  }

  const moduleOptions = ref<CascaderOption[]>([]);
  const moduleLocaleMap = ref<Record<string, string>>({});
  const { getPathMapByLevel, jumpRouteByMapKey } = usePathMap();

  function initModuleOptions() {
    moduleOptions.value = getPathMapByLevel(props.mode, (e) => {
      moduleLocaleMap.value[e.key] = e.locale;
      return {
        value: e.key,
        label: t(e.locale),
        children: e.children,
        isLeaf: e.children?.length === 0,
      };
    });
  }

  /**
   * 获取操作对象映射的国际化文本，并处理可能不存在的 key 导致报错的情况
   * @param module 操作对象 key
   */
  function getModuleLocale(module: string) {
    try {
      return t(moduleLocaleMap.value[module] || '') || module;
    } catch (error) {
      return module;
    }
  }

  const typeOptions = [
    {
      label: 'system.log.operateType.all',
      value: '',
    },
    {
      label: 'system.log.operateType.add',
      value: 'ADD',
    },
    {
      label: 'system.log.operateType.delete',
      value: 'DELETE',
    },
    {
      label: 'system.log.operateType.update',
      value: 'UPDATE',
    },
    {
      label: 'system.log.operateType.debug',
      value: 'DEBUG',
    },
    {
      label: 'system.log.operateType.review',
      value: 'REVIEW',
    },
    {
      label: 'system.log.operateType.copy',
      value: 'COPY',
    },
    {
      label: 'system.log.operateType.execute',
      value: 'EXECUTE',
    },
    {
      label: 'system.log.operateType.share',
      value: 'SHARE',
    },
    {
      label: 'system.log.operateType.restore',
      value: 'RESTORE',
    },
    {
      label: 'system.log.operateType.import',
      value: 'IMPORT',
    },
    {
      label: 'system.log.operateType.export',
      value: 'EXPORT',
    },
    {
      label: 'system.log.operateType.login',
      value: 'LOGIN',
    },
    {
      label: 'system.log.operateType.select',
      value: 'SELECT',
    },
    {
      label: 'system.log.operateType.recover',
      value: 'RECOVER',
    },
    {
      label: 'system.log.operateType.logout',
      value: 'LOGOUT',
    },
  ];

  function resetFilter() {
    operUser.value = '';
    operateRange.value = [props.mode];
    type.value = '';
    _module.value = '';
    content.value = '';
    time.value = [dayjs().subtract(1, 'M').valueOf(), dayjs().valueOf()];
  }

  const columns: MsTableColumn = [
    {
      title: 'system.log.operator',
      dataIndex: 'userName',
    },
    {
      title: 'system.log.operateRange',
      dataIndex: 'operateRange',
      slotName: 'range',
    },
    {
      title: 'system.log.operateTarget',
      dataIndex: 'module',
      slotName: 'module',
    },
    {
      title: 'system.log.operateType',
      dataIndex: 'type',
      slotName: 'type',
      width: 120,
    },
    {
      title: 'system.log.operateName',
      dataIndex: 'content',
      slotName: 'content',
    },
    {
      title: 'system.log.time',
      dataIndex: 'createTime',
      fixed: 'right',
      width: 170,
      sortable: {
        sortDirections: ['ascend', 'descend'],
      },
    },
  ];
  const tableStore = useTableStore();
  tableStore.initColumn(TableKeyEnum.SYSTEM_LOG, columns, 'drawer');
  const { propsRes, propsEvent, loadList, setLoadListParams, resetPagination } = useTable(
    requestFuncMap[props.mode].listFunc,
    {
      tableKey: TableKeyEnum.SYSTEM_LOG,
      columns,
      selectable: false,
      showSelectAll: false,
    }
  );

  function searchLog() {
    const ranges = operateRange.value.map((e) => e);
    setLoadListParams({
      operUser: operUser.value,
      projectIds: level.value === 'PROJECT' && ranges[0] !== 'PROJECT' ? ranges : [],
      organizationIds: level.value === 'ORGANIZATION' && ranges[0] !== 'ORGANIZATION' ? ranges : [],
      type: type.value,
      module: _module.value,
      content: content.value,
      startTime: time.value[0],
      endTime: time.value[1],
      level: level.value,
    });
    resetPagination();
    loadList();
  }

  function handleNameClick(record: LogItem) {
    jumpRouteByMapKey(record.module, record.sourceId ? { id: record.sourceId } : {}, true);
  }

  onBeforeMount(() => {
    initUserList();
    initRangeOptions();
    initModuleOptions();
    searchLog();
  });
</script>

<style lang="less" scoped>
  .filter-box {
    @apply grid;

    margin-bottom: 16px;
    gap: 16px;
  }
  @media screen and (max-width: 1400px) {
    .filter-box {
      grid-template-columns: repeat(2, 1fr);
    }
  }
  @media screen and (min-width: 1400px) {
    .filter-box {
      grid-template-columns: repeat(3, 1fr);
    }
  }
  .log-card {
    @apply bg-white;

    margin-top: 16px;
    padding: 24px;
    border-radius: var(--border-radius-large);
    .log-card-header {
      @apply flex items-center justify-between;

      margin-bottom: 16px;
    }
  }
  .log-card--list {
    padding: 0 0 24px;
    .log-card-header {
      margin-bottom: 0;
      padding: 24px 24px 16px;
    }
  }
</style>
