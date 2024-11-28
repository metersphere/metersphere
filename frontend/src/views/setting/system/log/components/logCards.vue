<template>
  <div class="flex h-full flex-col gap-[16px]">
    <MsCard simple auto-height>
      <div class="filter-box">
        <div class="filter-item">
          <MsSelect
            v-model:model-value="operator"
            mode="remote"
            placeholder="system.log.operatorPlaceholder"
            prefix="system.log.operator"
            :options="[]"
            :remote-func="requestFuncMap[props.mode].usersFunc"
            :remote-extra-params="{ id: props.mode === 'PROJECT' ? appStore.currentProjectId : appStore.currentOrgId }"
            :remote-fields-map="{
              id: 'id',
              value: 'value',
              label: 'name',
              email: 'email',
            }"
            :search-keys="['name', 'email']"
            value-key="id"
            label-key="name"
            :option-tooltip-content="(item) => `${item.name}(${item.email})`"
            :option-label-render="
              (item) => `${item.name}<span class='text-[var(--color-text-2)]'>（${item.email}）</span>`
            "
            allow-search
            allow-clear
          />
        </div>
        <a-range-picker
          v-model:model-value="time"
          :time-picker-props="{
            defaultValue: ['00:00:00', '00:00:00'],
          }"
          :disabled-date="disabledDate"
          class="filter-item"
          :allow-clear="false"
          :disabled-input="false"
          :separator="t('common.to')"
          value-format="timestamp"
          show-time
          @select="selectTime"
          @popup-visible-change="handleRangeVisibleChange"
        >
          <template #prefix>
            {{ t('system.log.operateTime') }}
          </template>
        </a-range-picker>
        <MsCascader
          v-if="props.mode !== 'PROJECT'"
          v-model:model-value="operateRange"
          v-model:level="level"
          :options="rangeOptions"
          :prefix="t('system.log.operateRange')"
          :level-top="[...MENU_LEVEL]"
          :virtual-list-props="{ height: 200 }"
          :loading="rangeLoading"
          label-path-mode
          class="filter-item"
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
          label-path-mode
          class="filter-item"
        />
        <a-input
          v-model:model-value="content"
          class="filter-item"
          :placeholder="t('system.log.operateNamePlaceholder')"
          allow-clear
          :max-length="255"
        >
          <template #prefix>
            {{ t('system.log.operateName') }}
          </template>
        </a-input>
        <div v-if="props.mode === 'PROJECT'">
          <a-button type="outline" @click="searchLog">{{ t('system.log.search') }}</a-button>
          <a-button type="outline" class="arco-btn-outline--secondary ml-[8px]" @click="resetFilter">
            {{ t('system.log.reset') }}
          </a-button>
        </div>
      </div>
      <template v-if="props.mode !== 'PROJECT'">
        <a-button type="outline" @click="searchLog">{{ t('system.log.search') }}</a-button>
        <a-button type="outline" class="arco-btn-outline--secondary ml-[8px]" @click="resetFilter">
          {{ t('system.log.reset') }}
        </a-button>
      </template>
    </MsCard>
    <MsCard class="log-card" simple auto-height>
      <div class="log-card-header">
        <div class="font-medium text-[var(--color-text-1)]">{{ t('system.log.log') }}</div>
      </div>
      <ms-base-table v-bind="propsRes" no-disable sticky-header v-on="propsEvent">
        <template #range="{ record }">
          <a-tooltip
            :content="
              record.organizationId === 'SYSTEM'
                ? t('system.log.system')
                : `${record.organizationName}${record.projectName ? `/${record.projectName}` : ''}`
            "
          >
            <div class="one-line-text">
              {{
                record.organizationId === 'SYSTEM'
                  ? t('system.log.system')
                  : `${record.organizationName}${record.projectName ? `/${record.projectName}` : ''}`
              }}
            </div>
          </a-tooltip>
        </template>
        <template #module="{ record }">
          <a-tooltip :content="getModuleLocale(record.module)">
            <div class="one-line-text">
              {{ getModuleLocale(record.module) }}
            </div>
          </a-tooltip>
        </template>
        <template #type="{ record }">
          {{ t(typeOptions.find((e) => e.value === record.type)?.label || '') }}
        </template>
        <template #content="{ record }">
          <div
            v-if="
              record.module === 'SYSTEM' ||
              record.type === 'DELETE' ||
              record.module.includes('MODULE') ||
              record.module.includes('TASK_CENTER') ||
              record.module.includes('API_TEST_MANAGEMENT_DEFINITION_SHARE')
            "
            class="one-line-text"
          >
            {{ record.content }}
          </div>
          <MsButton v-else @click="handleNameClick(record)">
            <div class="one-line-text">
              {{ record.content }}
            </div>
          </MsButton>
        </template>
      </ms-base-table>
    </MsCard>
  </div>
</template>

<script lang="tsx" setup>
  import { onBeforeMount, ref } from 'vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsCascader from '@/components/business/ms-cascader/index.vue';
  import MsSelect from '@/components/business/ms-select/index';

  import {
    getOrgLogList,
    getOrgLogOptions,
    getOrgLogUsers,
    getProjectLogList,
    getProjectLogUsers,
    getSystemLogList,
    getSystemLogOptions,
    getSystemLogUsers,
  } from '@/api/modules/setting/log';
  import { MENU_LEVEL } from '@/config/pathMap';
  import { useI18n } from '@/hooks/useI18n';
  import usePathMap from '@/hooks/usePathMap';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';

  import type { CommonList } from '@/models/common';
  import type { LogItem, LogOptions } from '@/models/setting/log';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import type { CascaderOption } from '@arco-design/web-vue';

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
      listFunc: getProjectLogList,
      optionsFunc: getOrgLogOptions, // 忽略，这里并不加载
      usersFunc: getProjectLogUsers,
    },
  };

  const operator = ref(''); // 操作人

  const operateRange = ref<(string | number | Record<string, any>)[]>([props.mode]); // 操作范围
  const rangeOptions = ref<CascaderOption[]>( // 系统级别才展示系统级别选项
    props.mode === MENU_LEVEL[0]
      ? [
          {
            value: {
              level: 0,
              value: MENU_LEVEL[0],
              label: t('system.log.system'),
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
    if (props.mode === 'PROJECT') return;
    try {
      rangeLoading.value = true;
      const res = await requestFuncMap[props.mode].optionsFunc(appStore.currentOrgId);
      if (props.mode === MENU_LEVEL[0]) {
        // 系统级别才展示，组织和项目级别不展示
        rangeOptions.value.push({
          value: {
            level: 0,
            value: MENU_LEVEL[1], // 顶级范围-组织，单选
            label: t('system.log.organization'),
          },
          label: t('system.log.organization'),
          children:
            Array.isArray(res.organizationList) && res.organizationList.length > 0
              ? res.organizationList.map((e) => ({
                  // 组织列表，多选
                  value: {
                    level: MENU_LEVEL[1],
                    value: e.id,
                    label: `${t('system.log.organization')} / ${e.name}`,
                  },
                  label: e.name,
                  isLeaf: true,
                }))
              : undefined,
        });
      } else if (props.mode === MENU_LEVEL[1]) {
        rangeOptions.value.push({
          value: {
            level: 0,
            value: MENU_LEVEL[1], // 顶级范围-组织，单选
            label: t('system.log.organization'),
          },
          label: t('system.log.organization'),
        });
      }
      rangeOptions.value.push({
        value: {
          level: 0,
          value: MENU_LEVEL[2], // 顶级范围-项目，单选
          label: t('system.log.project'),
        },
        label: t('system.log.project'),
        children:
          Array.isArray(res.projectList) && res.projectList.length > 0
            ? res.projectList.map((e) => ({
                // 项目列表，多选
                value: {
                  level: MENU_LEVEL[2],
                  value: e.id,
                  label: `${t('system.log.project')} / ${e.name}`,
                },
                label: e.name,
                isLeaf: true,
              }))
            : undefined,
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      rangeLoading.value = false;
    }
  }

  const level = ref<(typeof MENU_LEVEL)[number]>(props.mode); // 操作范围级别，系统/组织/项目
  const type = ref(''); // 操作类型
  const _module = ref(''); // 操作对象
  const content = ref(''); // 名称
  const time = ref<(Date | string | number)[]>([
    dayjs().subtract(1, 'M').hour(0).minute(0).second(1).valueOf(),
    dayjs().valueOf(),
  ]); // 操作时间
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

  function handleRangeVisibleChange(val: boolean) {
    if (!val) {
      selectedTime.value = '';
    }
  }

  const moduleOptions = ref<CascaderOption[]>([]);
  const moduleLocaleMap = ref<Record<string, string>>({});
  const { getPathMapByLevel, jumpRouteByMapKey, findLocalePath } = usePathMap();

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
      return findLocalePath(module)
        .map((e) => t(e) || module)
        .join('/');
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
    {
      label: 'system.log.operateType.associate',
      value: 'ASSOCIATE',
    },
    {
      label: 'system.log.operateType.disassociate',
      value: 'DISASSOCIATE',
    },
    {
      label: 'system.log.operateType.archived',
      value: 'ARCHIVED',
    },
    {
      label: 'system.log.operateType.stop',
      value: 'STOP',
    },
    {
      label: 'system.log.operateType.rerun',
      value: 'RERUN',
    },
  ];

  function resetFilter() {
    operator.value = '';
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
      width: 100,
      showTooltip: true,
    },
    {
      title: 'system.log.operateRange',
      dataIndex: 'operateRange',
      slotName: 'range',
      width: 100,
    },
    {
      title: 'system.log.operateTarget',
      dataIndex: 'module',
      slotName: 'module',
      width: 150,
    },
    {
      title: 'system.log.operateType',
      dataIndex: 'type',
      slotName: 'type',
      width: 80,
    },
    {
      title: 'system.log.operateName',
      dataIndex: 'content',
      slotName: 'content',
      showTooltip: true,
      width: 150,
    },
    {
      title: 'system.log.time',
      dataIndex: 'createTime',
      fixed: 'right',
      width: 120,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
    },
  ];
  const tableStore = useTableStore();
  await tableStore.initColumn(TableKeyEnum.SYSTEM_LOG, columns, 'drawer');
  const { propsRes, propsEvent, loadList, setLoadListParams, resetPagination } = useTable(
    requestFuncMap[props.mode].listFunc,
    {
      scroll: { x: 1100 },
      tableKey: TableKeyEnum.SYSTEM_LOG,
      columns,
      selectable: false,
      showSelectAll: false,
    }
  );

  function searchLog() {
    const ranges = operateRange.value.map((e) => {
      if (typeof e === 'object') {
        return e.value;
      }
      return e;
    });
    let projectIds: any[] = [];
    let organizationIds: any[] = [];

    if (!MENU_LEVEL.includes(level.value) && typeof operateRange.value[0] === 'object') {
      if (operateRange.value[0].level === MENU_LEVEL[1]) {
        organizationIds = ranges;
      } else if (operateRange.value[0].level === MENU_LEVEL[2]) {
        projectIds = ranges;
      }
    }

    setLoadListParams({
      operUser: operator.value,
      projectIds,
      organizationIds,
      type: type.value,
      module: _module.value,
      content: content.value,
      startTime: time.value[0],
      endTime: (time.value[1] as number) + 1000, // +1s 因为后台记录的是毫秒级，但是前端只能选秒到级
      level: level.value,
    });
    resetPagination();
    loadList();
  }

  function handleNameClick(record: LogItem) {
    if (record.type === 'DELETE') {
      // 删除操作或任务中心相关操作只展示内容，不跳转
      return;
    }
    const routeQuery: Record<string, any> = {
      orgId: record.organizationId,
      pId: record.projectId,
      id: record.sourceId,
    };
    if (record.organizationId === 'SYSTEM') {
      delete routeQuery.orgId;
    }
    if (record.projectId === 'SYSTEM' || record.projectId === 'ORGANIZATION') {
      delete routeQuery.pId;
    }
    jumpRouteByMapKey(record.module, routeQuery, true);
  }

  onBeforeMount(() => {
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
    .filter-item {
      @apply overflow-hidden;
    }
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
    @apply flex-1;
    :deep(.ms-card) {
      @apply h-full;
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
