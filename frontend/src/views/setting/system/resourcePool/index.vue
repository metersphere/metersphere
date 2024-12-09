<template>
  <MsCard :loading="loading" simple>
    <MsTrialAlert :tip-content="t('system.authorized.resourcePoolTipContent')" />

    <div class="mb-[16px]">
      <MsAdvanceFilter
        v-model:keyword="keyword"
        :filter-config-list="filterConfigList"
        :search-placeholder="t('system.resourcePool.searchPool')"
        @keyword-search="searchPool()"
        @adv-search="searchPool"
        @refresh="searchPool"
      >
        <template #left>
          <a-button v-permission="['SYSTEM_TEST_RESOURCE_POOL:READ+ADD']" v-xpack type="primary" @click="addPool">
            {{ t('system.resourcePool.createPool') }}
          </a-button>
        </template>
      </MsAdvanceFilter>
    </div>
    <ms-base-table v-bind="propsRes" no-disable v-on="propsEvent">
      <template #name="{ record }">
        <div class="flex items-center gap-[8px]">
          <a-tooltip
            v-if="record.type === 'Node' && record.enable"
            :content="t('system.resourcePool.viewCapacityInfo')"
            :mouse-enter-delay="300"
            position="bottom"
          >
            <div class="w-[16px]">
              <MsIcon
                type="icon-icon_pie_filled"
                class="cursor-pointer text-[rgb(var(--primary-5))]"
                size="16"
                @click="capacityDetail(record)"
              />
            </div>
          </a-tooltip>
          <div
            :class="`one-line-text cursor-pointer text-[rgb(var(--primary-5))]  ${
              record.id === '100001100001' ? 'max-w-[calc(100%-60px)]' : 'max-w-[calc(100%-16px)]'
            }`"
            @click="showPoolDetail(record.id)"
          >
            {{ record.name }}
          </div>
          <MsTag v-if="record.id === '100001100001'" size="small" tooltip-disabled>{{ t('common.default') }}</MsTag>
        </div>
      </template>
      <template #enable="{ record }">
        <div class="flex items-center gap-[8px]">
          <a-tooltip class="ms-tooltip-white" :disabled="licenseStore.hasLicense()">
            <a-switch
              v-model:model-value="record.enable"
              v-permission="['SYSTEM_TEST_RESOURCE_POOL:READ+UPDATE']"
              size="small"
              :disabled="!licenseStore.hasLicense()"
              :before-change="(val) => handleToggle(val, record)"
            >
            </a-switch>
            <template #content>
              <span class="text-[var(--color-text-1)]">{{ t('system.authorized.resourcePoolTableTip') }}</span>
              <span class="ml-2 inline-block cursor-pointer text-[rgb(var(--primary-4))]" @click="goTry">
                {{ t('system.authorized.applyTrial') }}
              </span>
            </template>
          </a-tooltip>
        </div>
      </template>
      <template #action="{ record }">
        <MsButton v-permission="['SYSTEM_TEST_RESOURCE_POOL:READ+UPDATE']" @click="editPool(record)">
          {{ t('system.resourcePool.editPool') }}
        </MsButton>
        <MsButton v-permission="['SYSTEM_TEST_RESOURCE_POOL:READ+DELETE']" status="danger" @click="deletePool(record)">
          {{ t('common.delete') }}
        </MsButton>
        <!-- <MsButton
          v-if="record.enable"
          v-permission="['SYSTEM_TEST_RESOURCE_POOL:READ+UPDATE']"
          v-xpack
          @click="disabledPool(record)"
        >
          {{ t('system.resourcePool.tableDisable') }}
        </MsButton>
        <MsButton v-else v-permission="['SYSTEM_TEST_RESOURCE_POOL:READ+UPDATE']" v-xpack @click="enablePool(record)">
          {{ t('system.resourcePool.tableEnable') }}
        </MsButton> -->
      </template>
    </ms-base-table>
  </MsCard>
  <MsDrawer
    v-model:visible="showDetailDrawer"
    :width="480"
    :title="activePool?.name"
    :title-tag="activePool?.enable ? t('system.resourcePool.tableEnable') : t('system.resourcePool.tableDisable')"
    :title-tag-color="activePool?.enable ? 'green' : 'gray'"
    :descriptions="activePoolDesc"
    :footer="false"
    :mask="false"
    :show-skeleton="drawerLoading"
    show-description
  >
    <template #tbutton>
      <a-button
        v-permission="['SYSTEM_TEST_RESOURCE_POOL:READ+UPDATE']"
        type="outline"
        size="mini"
        :disabled="drawerLoading"
        @click="editPool(activePool)"
      >
        {{ t('system.resourcePool.editPool') }}
      </a-button>
    </template>
  </MsDrawer>
  <!-- <JobTemplateDrawer
    v-model:visible="showJobDrawer"
    :default-val="activePool?.testResourceReturnDTO.jobDefinition || ''"
    read-only
  /> -->
  <CapacityDrawer v-model:visible="showCapacityDrawer" :active-record="activeRecord" />
</template>

<script setup lang="ts">
  /**
   * @description 系统设置-资源池
   */
  import { useRoute, useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';

  import { MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import type { Description } from '@/components/pure/ms-description/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn, MsTableColumnFilterConfig } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag, { TagType, Theme } from '@/components/pure/ms-tag/ms-tag.vue';
  import MsTrialAlert from '@/components/business/ms-trial-alert/index.vue';
  import CapacityDrawer from './components/capacityDrawer.vue';

  // import JobTemplateDrawer from './components/jobTemplateDrawer.vue';
  import { delPoolInfo, getPoolInfo, getPoolList, togglePoolStatus } from '@/api/modules/setting/resourcePool';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useTableStore } from '@/store';
  import useLicenseStore from '@/store/modules/setting/license';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import type { ResourcePoolDetail, ResourcePoolItem } from '@/models/setting/resourcePool';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterRemoteMethodsEnum } from '@/enums/tableFilterEnum';

  const licenseStore = useLicenseStore();
  const { t } = useI18n();
  const router = useRouter();
  const route = useRoute();

  const hasOperationPoolPermission = computed(() =>
    hasAnyPermission(['SYSTEM_TEST_RESOURCE_POOL:READ+UPDATE', 'SYSTEM_TEST_RESOURCE_POOL:READ+DELETE'])
  );

  const filterConfig = computed<MsTableColumnFilterConfig>(() => {
    if (licenseStore.hasLicense()) {
      return {
        mode: 'remote',
        remoteMethod: FilterRemoteMethodsEnum.SYSTEM_ORGANIZATION_LIST,
      };
    }
    return {
      options: [],
    };
  });
  const columns: MsTableColumn = [
    {
      title: 'system.resourcePool.tableColumnName',
      slotName: 'name',
      dataIndex: 'name',
      showTooltip: true,
      width: 200,
    },
    {
      title: 'system.resourcePool.tableColumnStatus',
      slotName: 'enable',
      dataIndex: 'enable',
    },
    {
      title: 'system.resourcePool.concurrentNumber',
      dataIndex: 'maxConcurrentNumber',
      showTooltip: true,
      width: 150,
    },
    {
      title: 'system.resourcePool.orgRange',
      dataIndex: 'orgId',
      showInTable: true,
      showDrag: true,
      isTag: true,
      filterConfig: filterConfig.value,
      isStringTag: true,
      tagPrimary: 'default',
    },
    {
      title: 'common.desc',
      dataIndex: 'description',
      showTooltip: true,
    },
    {
      title: 'system.resourcePool.tableColumnType',
      dataIndex: 'type',
      filterConfig: {
        options: [
          {
            value: 'Node',
            label: 'Node',
          },
          {
            value: 'Kubernetes',
            label: 'Kubernetes',
          },
        ],
      },
    },
    {
      title: 'system.resourcePool.tableColumnCreateTime',
      dataIndex: 'createTime',
      width: 180,
    },
    {
      title: 'system.resourcePool.tableColumnUpdateTime',
      dataIndex: 'updateTime',
      width: 180,
    },
    {
      title: 'system.resourcePool.tableColumnActions',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: 120,
    },
  ];
  const tableStore = useTableStore();
  if (!hasOperationPoolPermission.value) {
    columns.pop();
  }
  await tableStore.initColumn(TableKeyEnum.SYSTEM_RESOURCEPOOL, columns, 'drawer');
  const { propsRes, propsEvent, loadList, setKeyword } = useTable(
    getPoolList,
    {
      tableKey: TableKeyEnum.SYSTEM_RESOURCEPOOL,
      columns,
      scroll: { y: 'auto' },
      selectable: false,
      showSelectAll: false,
    },
    (item) => {
      return {
        ...item,
        orgId: item.orgNames,
        lastConcurrentNumber: item.type === 'Kubernetes' ? '-' : item.lastConcurrentNumber || 0,
      };
    }
  );

  const keyword = ref('');
  const filterConfigList = ref<FilterFormItem[]>([]);

  onMounted(async () => {
    setKeyword(keyword.value);
    await loadList();
  });

  async function searchPool() {
    setKeyword(keyword.value);
    await loadList();
  }

  const { openModal } = useModal();
  const loading = ref(false);

  /**
   * 启用资源池
   */
  async function enablePool(record: ResourcePoolItem) {
    try {
      loading.value = true;
      await togglePoolStatus(record.id);
      Message.success(t('system.resourcePool.enablePoolSuccess'));
      loadList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  /**
   * 禁用资源池
   */
  function disabledPool(record: ResourcePoolItem) {
    openModal({
      type: 'warning',
      title: t('system.resourcePool.disablePoolTip', { name: characterLimit(record.name) }),
      content: t('system.resourcePool.disablePoolContent'),
      okText: t('system.resourcePool.disablePoolConfirm'),
      cancelText: t('system.resourcePool.disablePoolCancel'),
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await togglePoolStatus(record.id);
          Message.success(t('system.resourcePool.disablePoolSuccess'));
          loadList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  function handleToggle(newValue: string | number | boolean, record: ResourcePoolItem) {
    if (newValue) {
      enablePool(record);
    } else {
      disabledPool(record);
    }
    return false;
  }

  /**
   * 删除资源池
   */
  function deletePool(record: ResourcePoolItem) {
    if (propsRes.value.data.length === 1) {
      Message.warning(t('system.resourcePool.atLeastOnePool'));
      return;
    }
    openModal({
      type: 'error',
      title: t('system.resourcePool.deletePoolTip', { name: characterLimit(record.name) }),
      content: t('system.resourcePool.deletePoolContentUsed'),
      okText: t('system.resourcePool.deletePoolConfirm'),
      cancelText: t('system.resourcePool.deletePoolCancel'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await delPoolInfo(record.id);
          Message.success(t('system.resourcePool.deletePoolSuccess'));
          loadList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  const showDetailDrawer = ref(false);
  const activePoolDesc = ref<Description[]>([]);
  const activePool = ref<ResourcePoolDetail | null>(null);
  // const showJobDrawer = ref(false);
  const drawerLoading = ref(false);
  /**
   * 查看资源池详情
   * @param id 资源池 id
   */
  async function showPoolDetail(id: string) {
    if (activePool.value?.id === id && showDetailDrawer.value) {
      return;
    }
    drawerLoading.value = true;
    showDetailDrawer.value = true;
    try {
      const res = await getPoolInfo(id);
      if (res) {
        if (res.deleted) {
          Message.warning(t('common.resourceDeleted'));
          drawerLoading.value = false;
          showDetailDrawer.value = false;
          return;
        }
        activePool.value = res;
        // const poolUses = [
        //   activePool.value.apiTest ? t('system.resourcePool.useAPI') : '',
        //   activePool.value.uiTest ? t('system.resourcePool.useUI') : '',
        // ];
        const { type, testResourceReturnDTO, apiTest, uiTest } = activePool.value;
        const {
          ip,
          token, // k8s token
          namespace, // k8s 命名空间
          concurrentNumber, // k8s 最大并发数
          podThreads, // k8s 单pod最大线程数
          deployName, // k8s api测试部署名称
          girdConcurrentNumber,
          nodesList,
          uiGrid,
        } = testResourceReturnDTO;
        // Node
        const nodeResourceDesc =
          type === 'Node'
            ? [
                {
                  label: t('system.resourcePool.detailResources'),
                  value: nodesList?.map((e) => `${e.ip},${e.port},${e.concurrentNumber}`),
                  tagTheme: 'light' as Theme,
                  tagType: 'default' as TagType,
                  tagMaxWidth: '280px',
                  isTag: true,
                },
              ]
            : [];
        // K8S
        const k8sResourceDesc =
          type === 'Kubernetes'
            ? [
                {
                  label: t('system.resourcePool.testResourceDTO.ip'),
                  value: ip,
                },
                {
                  label: t('system.resourcePool.testResourceDTO.token'),
                  value: token,
                  showCopy: true,
                },
                {
                  label: t('system.resourcePool.testResourceDTO.namespace'),
                  value: namespace,
                },
                {
                  label: t('system.resourcePool.testResourceDTO.deployName'),
                  value: deployName,
                },
                {
                  label: t('system.resourcePool.testResourceDTO.concurrentNumber'),
                  value: concurrentNumber,
                },
                {
                  label: t('system.resourcePool.testResourceDTO.podThreads'),
                  value: podThreads,
                },
              ]
            : [];
        // const jobTemplate =
        //   type === 'Kubernetes'
        //     ? [
        //         {
        //           label: t('system.resourcePool.jobTemplate'),
        //           value: t('system.resourcePool.customJobTemplate'),
        //           isButton: true,
        //           onClick: () => {
        //             showJobDrawer.value = true;
        //           },
        //         },
        //       ]
        //     : [];
        // 接口测试
        const resourceDesc = apiTest ? [...nodeResourceDesc, ...k8sResourceDesc] : [];
        // ui 测试资源
        const uiDesc = uiTest
          ? [
              {
                label: t('system.resourcePool.uiGrid'),
                value: uiGrid,
              },
              {
                label: t('system.resourcePool.concurrentNumber'),
                value: girdConcurrentNumber,
              },
            ]
          : [];

        const detailType = apiTest
          ? [
              {
                label: t('system.resourcePool.detailType'),
                value: activePool.value.type,
              },
            ]
          : [];
        activePoolDesc.value = [
          {
            label: t('common.desc'),
            value: activePool.value.description,
          },
          {
            label: t('system.resourcePool.detailUrl'),
            value: activePool.value.serverUrl,
          },
          {
            label: t('system.resourcePool.detailRange'),
            value: activePool.value.allOrg
              ? [t('system.resourcePool.orgAll')]
              : activePool.value.testResourceReturnDTO.orgIdNameMap.map((e) => e.name),
            isTag: true,
            tagTheme: 'light',
            tagType: 'default',
          },
          // {
          //   label: t('system.resourcePool.detailUse'),
          //   value: poolUses.filter((e) => e !== ''),
          //   tagTheme: 'light',
          //   tagType: 'default',
          //   isTag: true,
          // },
          ...uiDesc,
          ...detailType,
          ...resourceDesc,
          // ...jobTemplate,
        ];
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      drawerLoading.value = false;
    }
  }
  const showCapacityDrawer = ref<boolean>(false);
  const activeRecord = ref<ResourcePoolItem>();
  // TODO 等待联调
  // 查看容量信息
  function capacityDetail(record: ResourcePoolItem) {
    showCapacityDrawer.value = true;
    activeRecord.value = record;
  }

  onMounted(() => {
    if (route.query.id) {
      // 地址栏携带 id，自动打开资源池详情抽屉
      showPoolDetail(route.query.id as string);
    }
  });

  /**
   * 编辑资源池
   * @param record
   */
  function editPool(record: ResourcePoolDetail | null) {
    router.push({
      name: 'settingSystemResourcePoolDetail',
      query: {
        id: record?.id,
      },
    });
  }

  /**
   * 添加资源池
   * @param record
   */
  function addPool() {
    router.push({
      name: 'settingSystemResourcePoolDetail',
    });
  }

  function goTry() {
    window.open('https://jinshuju.net/f/CzzAOe', '_blank');
  }
</script>

<style lang="less" scoped></style>
