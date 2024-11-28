<template>
  <a-spin v-if="!projectVersionStatus" :loading="loading" class="h-full w-full min-w-[930px]">
    <div class="flex h-full flex-col items-center p-[24px]">
      <div class="mt-[200px] text-[16px] font-medium leading-[24px] text-[var(--color-text-1)]">
        {{ t('project.projectVersion.version') }}
      </div>
      <div class="mt-[16px] text-[var(--color-text-4)]">{{ t('project.projectVersion.tip') }}</div>
      <div class="mt-[24px] grid grid-cols-3 gap-[16px]">
        <div class="tip-card">
          <img src="@/assets/images/project_assign.png" width="78" height="60" class="tip-icon" />
          <div>
            <div class="tip-title">{{ t('project.projectVersion.assign') }}</div>
            <div class="tip-sub-text">{{ t('project.projectVersion.assignTip') }}</div>
          </div>
        </div>
        <div class="tip-card">
          <img src="@/assets/images/project_filter.png" width="78" height="60" class="tip-icon" />

          <div>
            <div class="tip-title">{{ t('project.projectVersion.filter') }}</div>
            <div class="tip-sub-text">{{ t('project.projectVersion.filterTip') }}</div>
          </div>
        </div>
        <div class="tip-card">
          <img src="@/assets/images/project_compare.png" width="78" height="60" class="tip-icon" />

          <div>
            <div class="tip-title">{{ t('project.projectVersion.compare') }}</div>
            <div class="tip-sub-text">{{ t('project.projectVersion.compareTip') }}</div>
          </div>
        </div>
      </div>
      <div class="mt-[40px] flex justify-center">
        <a-button v-permission="['PROJECT_VERSION:READ+UPDATE']" type="outline" @click="() => openProjectVersion(true)">
          {{ t('project.projectVersion.openVersion') }}
        </a-button>
      </div>
    </div>
  </a-spin>
  <div v-if="projectVersionStatus" class="table-container">
    <div class="mb-[16px] flex justify-between">
      <div>
        <a-switch
          v-model:model-value="projectVersionStatus"
          v-permission="['PROJECT_VERSION:READ+UPDATE']"
          size="small"
          :before-change="(val) => openProjectVersion(val)"
          type="line"
        >
        </a-switch>
        <span class="ml-[4px] font-medium">{{ t('project.projectVersion.version') }}</span>
      </div>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('project.projectVersion.searchPlaceholder')"
        class="w-[230px]"
        allow-clear
        @search="searchVersion"
        @press-enter="searchVersion"
        @clear="searchVersion"
      />
    </div>
    <MsBaseTable v-bind="propsRes" v-on="propsEvent">
      <template #statusTitle>
        <div class="flex items-center">
          <div class="font-medium text-[var(--color-text-3)]">
            {{ t('project.projectVersion.status') }}
          </div>
          <a-popover position="rt">
            <icon-info-circle
              class="ml-[4px] text-[var(--color-text-3)] hover:text-[rgb(var(--primary-5))]"
              size="16"
            />
            <template #title>
              <div class="w-[256px]"> {{ t('project.projectVersion.statusTip') }} </div>
            </template>
            <template #content>
              <div class="mt-[12px] w-[256px] rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] p-[12px]">
                <div class="statusTipContent">
                  <div class="px-[8px] py-[4px] text-[14px] font-medium text-[var(--color-text-1)]">
                    {{ t('project.projectVersion.versionInfo') }}
                  </div>
                  <div
                    class="flex items-center rounded-[var(--border-radius-small)] bg-[rgb(var(--primary-1))] px-[8px] py-[4px] text-[14px] text-[rgb(var(--primary-6))]"
                  >
                    <div class="flex">
                      v4.0
                      <div class="ml-[4px] bg-[rgb(var(--primary-9))] px-[4px] text-[rgb(var(--primary-6))]">
                        {{ t('project.projectVersion.versionLatest') }}
                      </div>
                    </div>
                    <icon-check class="ml-auto text-[rgb(var(--primary-6))]" />
                  </div>
                  <div class="flex items-center px-[8px] py-[4px] text-[14px] text-[var(--color-text-1)]"> v3.0 </div>
                  <div class="flex items-center px-[8px] py-[4px] text-[14px] text-[var(--color-text-1)]"> v2.0 </div>
                  <div class="flex items-center px-[8px] py-[4px] text-[14px] text-[var(--color-text-1)]"> v1.0 </div>
                </div>
              </div>
            </template>
          </a-popover>
        </div>
      </template>
      <template v-if="hasAnyPermission(['PROJECT_VERSION:READ+ADD'])" #quickCreate>
        <a-form
          v-if="showQuickCreateForm"
          ref="quickCreateFormRef"
          :model="quickCreateForm"
          layout="inline"
          size="small"
          class="flex items-center"
        >
          <a-form-item
            field="name"
            :rules="[{ required: true, message: t('project.projectVersion.versionNameRequired') }]"
            no-style
          >
            <a-input
              v-model:model-value="quickCreateForm.name"
              :max-length="255"
              :placeholder="t('project.projectVersion.versionNamePlaceholder')"
              class="w-[262px]"
            />
          </a-form-item>
          <a-form-item field="publishTime" no-style>
            <a-date-picker
              v-model:model-value="quickCreateForm.publishTime"
              :placeholder="t('project.projectVersion.publishTimePlaceholder')"
              show-time
              class="ml-[8px] w-[262px]"
            />
          </a-form-item>
          <a-form-item no-style>
            <a-button type="outline" size="mini" class="ml-[12px] mr-[8px] px-[8px]" @click="quickCreateConfirm">
              {{ t('common.confirm') }}
            </a-button>
            <a-button
              type="outline"
              class="arco-btn-outline--secondary px-[8px]"
              size="mini"
              @click="quickCreateCancel"
            >
              {{ t('common.cancel') }}
            </a-button>
          </a-form-item>
        </a-form>
        <MsButton v-else @click="showQuickCreateForm = true">
          <MsIcon type="icon-icon_add_outlined" size="14" class="mr-[8px]" />
          {{ t('project.projectVersion.quickCreate') }}
        </MsButton>
      </template>
      <template #status="{ record }">
        <a-switch
          v-model:model-value="record.status"
          v-permission="['PROJECT_VERSION:READ+UPDATE']"
          size="small"
          :before-change="(val) => handleStatusChange(val, record)"
          type="line"
        ></a-switch>
      </template>
      <template #latest="{ record }">
        <a-switch
          v-model:model-value="record.latest"
          v-permission="['PROJECT_VERSION:READ+UPDATE']"
          :disabled="record.latest"
          :before-change="() => handleUseLatestVersionChange(record)"
          size="small"
          type="line"
        />
      </template>
      <template #action="{ record }">
        <a-tooltip :content="t('project.projectVersion.latestVersionDeleteTip')" :disabled="!record.latest">
          <MsButton
            v-permission="['PROJECT_VERSION:READ+DELETE']"
            type="text"
            :loading="delLoading"
            :disabled="record.latest"
            status="danger"
            size="small"
            @click="delVersion(record)"
          >
            {{ t('common.delete') }}
          </MsButton>
        </a-tooltip>
      </template>
    </MsBaseTable>
    <a-modal
      v-model:visible="latestModalVisible"
      title="Modal Form"
      :on-before-ok="handleBeforeLatestModalOk"
      class="p-[4px]"
      title-align="start"
      body-class="p-0"
      :ok-text="t('common.confirmClose')"
      :ok-button-props="{ status: 'danger' }"
      @cancel="handleLatestModalCancel"
    >
      <template #title>
        <div class="flex items-center justify-start">
          <icon-exclamation-circle-fill size="20" class="mr-[8px] text-[rgb(var(--danger-6))]" />
          <div class="text-[var(--color-text-1)]">
            {{ t('project.projectVersion.confirmCloseTitle', { name: activeRecord.name }) }}
          </div>
        </div>
      </template>
      <div>
        <div>{{ t('project.projectVersion.confirmCloseTipContent1') }}</div>
        <div>{{ t('project.projectVersion.confirmCloseTipContent2') }}</div>
        <a-select
          v-model:model-value="replaceVersion"
          class="mt-[10px]"
          :placeholder="t('project.projectVersion.replaceVersionPlaceholder')"
          :options="
            versionOptions
              .filter((e) => e.id !== activeRecord.id)
              .map((e) => ({ ...e, disabled: !e.enable, value: e.id, label: e.name }))
          "
        />
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { FormInstance, Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import {
    addVersion,
    deleteVersion,
    getVersionList,
    getVersionOptions,
    getVersionStatus,
    toggleVersion,
    toggleVersionStatus,
    updateVersion,
    useLatestVersion,
  } from '@/api/modules/project-management/projectVersion';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useAppStore from '@/store/modules/app';
  import { hasAnyPermission } from '@/utils/permission';

  import { ProjectItem, ProjectVersionOption } from '@/models/projectManagement/projectVersion';
  import { ColumnEditTypeEnum } from '@/enums/tableEnum';

  const { t } = useI18n();
  const appStore = useAppStore();
  const { openModal } = useModal();

  const loading = ref(false);
  const projectVersionStatus = ref(false);

  async function getProjectVersionStatus() {
    try {
      loading.value = true;
      const res = await getVersionStatus(appStore.currentProjectId);
      projectVersionStatus.value = res;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  /**
   * 切换项目版本开关
   * @param val 开关
   */
  async function openProjectVersion(val?: string | number | boolean) {
    try {
      loading.value = true;
      await toggleVersion(appStore.currentProjectId);
      Message.success(!val ? t('common.closeSuccess') : t('project.projectVersion.openVersionSuccess'));
      return true;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      return false;
    } finally {
      loading.value = false;
      await getProjectVersionStatus();
    }
  }

  onBeforeMount(() => {
    getProjectVersionStatus();
  });

  /**
   * 更新版本名称
   * @param record 当前行数据
   */
  async function updateVersionName(record: ProjectItem) {
    try {
      await updateVersion({
        ...record,
        publishTime: dayjs(record.publishTime).valueOf(),
        createTime: dayjs(record.createTime).valueOf(),
      });
      Message.success(t('common.updateSuccess'));
      return Promise.resolve(true);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      return Promise.resolve(false);
    }
  }

  const keyword = ref('');

  const hasOperationPermission = computed(() =>
    hasAnyPermission(['PROJECT_VERSION:READ+UPDATE', 'PROJECT_VERSION:READ+DELETE'])
  );

  const columns: MsTableColumn = [
    {
      title: 'project.projectVersion.versionName',
      dataIndex: 'name',
      showTooltip: true,
      editType: ColumnEditTypeEnum.INPUT,
      width: 120,
    },
    {
      title: 'project.projectVersion.status',
      dataIndex: 'status',
      slotName: 'status',
      titleSlotName: 'statusTitle',
      width: 80,
    },
    {
      title: 'project.projectVersion.latest',
      dataIndex: 'latest',
      slotName: 'latest',
      width: 80,
    },
    {
      title: 'project.projectVersion.publishTime',
      dataIndex: 'publishTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
    },
    {
      title: 'project.projectVersion.createTime',
      dataIndex: 'createTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
    },
    {
      title: 'project.projectVersion.creator',
      dataIndex: 'createUser',
      showTooltip: true,
    },
    {
      title: hasOperationPermission.value ? 'common.operation' : '',
      fixed: 'right',
      slotName: 'action',
      dataIndex: 'operation',
      width: hasOperationPermission.value ? 80 : 50,
    },
  ];
  const { propsRes, propsEvent, loadList, setKeyword, setLoadListParams } = useTable(
    getVersionList,
    {
      scroll: { x: '100%' },
      columns,
    },
    (item) => {
      return {
        ...item,
        publishTime: item.publishTime ? dayjs(item.publishTime).format('YYYY-MM-DD HH:mm:ss') : '',
      };
    },
    updateVersionName
  );

  function searchVersion() {
    setKeyword(keyword.value);
    loadList();
  }

  onBeforeMount(async () => {
    setLoadListParams({
      projectId: appStore.currentProjectId,
    });
    searchVersion();
  });

  const showQuickCreateForm = ref(false);
  const quickCreateFormRef = ref<FormInstance>();
  const quickCreateForm = ref({
    name: '',
    publishTime: '',
  });
  const quickCreateLoading = ref(false);

  function quickCreateCancel() {
    showQuickCreateForm.value = false;
    quickCreateForm.value = {
      name: '',
      publishTime: '',
    };
    quickCreateFormRef.value?.resetFields();
  }

  /**
   * 快速创建版本
   */
  function quickCreateConfirm() {
    quickCreateFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          quickCreateLoading.value = true;
          await addVersion({
            ...quickCreateForm.value,
            projectId: appStore.currentProjectId,
            publishTime: quickCreateForm.value.publishTime
              ? dayjs(quickCreateForm.value.publishTime).valueOf()
              : undefined,
            latest: false,
            status: false,
          });
          Message.success(t('common.createSuccess'));
          quickCreateCancel();
          loadList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          quickCreateLoading.value = false;
        }
      }
    });
  }

  const latestModalVisible = ref(false);
  const activeRecord = ref<ProjectItem>({
    id: '',
    name: '',
    latest: false,
    status: false,
    publishTime: 0,
    createTime: 0,
    createUser: '',
    projectId: '',
  });
  const replaceVersion = ref('');
  const versionOptions = ref<ProjectVersionOption[]>([]);

  function handleLatestModalCancel() {
    latestModalVisible.value = false;
    replaceVersion.value = '';
  }

  /**
   * 拦截切换最新版确认
   * @param done 关闭弹窗
   */
  async function handleBeforeLatestModalOk(done: (closed: boolean) => void) {
    try {
      if (replaceVersion.value !== '') {
        await useLatestVersion(replaceVersion.value);
      }
      await toggleVersionStatus(activeRecord.value.id);
      Message.success(t('project.projectVersion.close', { name: activeRecord.value.name }));
      loadList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      done(false);
    } finally {
      done(true);
    }
  }

  /**
   * 拦截切换最新版
   * @param record 当前行数据
   */
  async function handleUseLatestVersionChange(record: ProjectItem) {
    try {
      await useLatestVersion(record.id);
      Message.success(t('project.projectVersion.switchLatestSuccess', { name: record.name }));
      loadList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      return false;
    }
  }

  /**
   * 拦截切换状态
   * @param val 开关
   * @param type 状态类型
   * @param record 当前行数据
   */
  async function handleStatusChange(val: string | number | boolean, record: ProjectItem) {
    try {
      if (!val && record.latest) {
        // 若关闭的是最新版，则提示需要替换最新版
        const res = await getVersionOptions(appStore.currentProjectId);
        versionOptions.value = res;
        latestModalVisible.value = true;
        activeRecord.value = record;
        return false;
      }
      if (!val) {
        // 关闭二次确认
        openModal({
          type: 'error',
          title: t('project.projectVersion.confirmCloseTitle', { name: record.name }),
          content: t('project.projectVersion.confirmCloseTip'),
          okText: t('common.confirmClose'),
          cancelText: t('common.cancel'),
          okButtonProps: {
            status: 'danger',
          },
          onBeforeOk: async () => {
            try {
              await toggleVersionStatus(record.id);
              Message.success(t('project.projectVersion.close', { name: record.name }));
              loadList();
            } catch (error) {
              // eslint-disable-next-line no-console
              console.log(error);
            }
          },
          hideCancel: false,
        });
        return false;
      }
      await toggleVersionStatus(record.id);
      Message.success(t('project.projectVersion.open', { name: record.name }));
      loadList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      return false;
    }
  }

  const delLoading = ref(false);
  async function delVersion(record: ProjectItem) {
    try {
      delLoading.value = true;
      await deleteVersion(record.id);
      Message.success(t('common.deleteSuccess'));
      loadList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      delLoading.value = false;
    }
  }
</script>

<style lang="less" scoped>
  .tip-card {
    @apply flex;

    gap: 12px;
    padding: 16px;
    border-radius: var(--border-radius-medium);
    background-color: var(--color-text-n9);
    .tip-icon {
      box-shadow: 0 4px 10px -1px rgb(100 100 102 / 15%);
    }
    .tip-title {
      @apply font-medium;

      color: var(--color-text-1);
    }
    .tip-sub-text {
      margin-top: 4px;
      font-size: 12px;
      color: var(--color-text-4);
      line-height: 16px;
    }
  }
  .statusTipContent {
    @apply flex flex-col;

    padding: 6px;
    border: 0.5px solid rgb(212 212 216 / 100%);
    border-radius: var(--border-radius-medium);
    background: var(--color-text-fff);
    box-shadow: 0 5px 5px -3px rgb(0 0 0 / 10%), 0 8px 10px 1px rgb(0 0 0 / 6%), 0 3px 14px 2px rgb(0 0 0 / 5%);
    gap: 2px;
  }
  .table-container {
    .ms-scroll-bar();
    @apply overflow-auto;

    min-width: 850px;
  }
</style>
