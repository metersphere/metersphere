<template>
  <a-input
    v-model:model-value="storageKeyword"
    :placeholder="t('project.fileManagement.folderSearchPlaceholder')"
    allow-clear
    class="mb-[8px]"
    :max-length="255"
  ></a-input>
  <a-spin class="h-full w-full" :loading="loading">
    <MsList
      v-model:focus-item-key="focusItemKey"
      :virtual-list-props="{
        height: 'calc(100vh - 275px)',
      }"
      :data="storageList"
      :bordered="false"
      :split="false"
      :item-more-actions="folderMoreActions"
      :empty-text="t('project.fileManagement.noStorage')"
      item-key-field="id"
      class="mr-[-6px]"
      @more-action-select="handleMoreSelect"
      @more-actions-close="moreActionsClose"
    >
      <template #title="{ item, index }">
        <div :key="index" class="storage" @click="setActiveFolder(item.id)">
          <div :class="props.activeFolder === item.id ? 'storage-text storage-text--active' : 'storage-text'">
            <MsIcon type="icon-icon_git" class="storage-icon" />
            <a-tooltip :content="item.name">
              <div class="storage-name one-line-text">{{ item.name }}</div>
            </a-tooltip>
            <div class="storage-count">({{ item.count }})</div>
          </div>
        </div>
      </template>
      <template #itemAction="{ item }">
        <popConfirm
          v-if="hasAnyPermission(['PROJECT_FILE_MANAGEMENT:READ+UPDATE'])"
          mode="repositoryRename"
          :node-id="item.id"
          :field-config="{ field: renameStorageTitle }"
          :all-names="[]"
          @close="resetFocusItemKey"
          @rename-finish="(val) => (item.name = val)"
        >
          <span :id="`renameSpan${item.id}`" class="relative"></span>
        </popConfirm>
      </template>
    </MsList>
  </a-spin>
  <MsDrawer
    v-model:visible="showDrawer"
    :title="t(isEdit ? 'project.fileManagement.updateStorageTitle' : 'project.fileManagement.addStorage')"
    :ok-text="t(isEdit ? 'project.fileManagement.save' : 'project.fileManagement.add')"
    :ok-loading="drawerLoading"
    :width="680"
    :show-continue="!isEdit"
    @confirm="handleDrawerConfirm"
    @continue="handleDrawerConfirm(true)"
    @cancel="handleDrawerCancel"
  >
    <a-form ref="storageFormRef" :model="activeStorageForm" layout="vertical">
      <a-form-item
        :label="t('project.fileManagement.storageName')"
        field="name"
        asterisk-position="end"
        :rules="[{ required: true, message: t('project.fileManagement.storageNameNotNull') }]"
        required
      >
        <a-input
          v-model:model-value="activeStorageForm.name"
          :max-length="255"
          :placeholder="t('project.fileManagement.storageNamePlaceholder')"
          allow-clear
        ></a-input>
      </a-form-item>
      <a-form-item :label="t('project.fileManagement.storagePlatform')" field="platform" asterisk-position="end">
        <a-radio-group
          v-model:model-value="activeStorageForm.platform"
          type="button"
          :disabled="isEdit"
          @change="platformChange"
        >
          <a-radio v-for="item of gitPlatformTypes" :key="item" :value="item">{{ item }}</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item
        :label="t('project.fileManagement.storageUrl')"
        field="url"
        asterisk-position="end"
        :rules="[
          { required: true, message: t('project.fileManagement.storageUrlNotNull') },
          { validator: validatePlatformUrl },
        ]"
        :disabled="isEdit"
        required
      >
        <a-input
          v-model:model-value="activeStorageForm.url"
          :max-length="1500"
          :placeholder="t('project.fileManagement.storageUrlPlaceholder')"
          allow-clear
        ></a-input>
        <MsFormItemSub
          v-if="!isEdit"
          :text="t('project.fileManagement.storageExUrl', { url: exampleUrl })"
          @fill="fillUrl"
        />
      </a-form-item>
      <a-form-item
        :label="t('project.fileManagement.storageToken')"
        field="token"
        asterisk-position="end"
        :rules="[{ required: true, message: t('project.fileManagement.storageTokenNotNull') }]"
        required
      >
        <a-input-password
          v-model:model-value="activeStorageForm.token"
          :max-length="1500"
          :placeholder="t('project.fileManagement.storageTokenPlaceholder')"
          allow-clear
          autocomplete="new-password"
        />
      </a-form-item>
      <a-form-item
        :label="t('project.fileManagement.storageUsername')"
        field="userName"
        asterisk-position="end"
        :rules="usernameRules"
      >
        <a-input
          v-model:model-value="activeStorageForm.userName"
          :max-length="255"
          :placeholder="t('project.fileManagement.storageUsernamePlaceholder')"
          allow-clear
        ></a-input>
      </a-form-item>
      <div>
        <a-button type="outline" class="mr-[16px]" :loading="testLoading" @click="testLink">
          {{ t('project.fileManagement.testLink') }}
        </a-button>
      </div>
    </a-form>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue';
  import { FormInstance, Message, ValidatedError } from '@arco-design/web-vue';
  import { debounce } from 'lodash-es';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsList from '@/components/pure/ms-list/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsFormItemSub from '@/components/business/ms-form-item-sub/index.vue';
  import popConfirm from './popConfirm.vue';

  import {
    addRepository,
    connectRepository,
    deleteModule,
    getRepositories,
    getRepositoryInfo,
    updateRepository,
  } from '@/api/modules/project-management/fileManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useAppStore from '@/store/modules/app';
  import { hasAnyPermission } from '@/utils/permission';
  import { validateGitUrl } from '@/utils/validate';

  import { Repository, RepositoryInfo } from '@/models/projectManagement/file';
  import { GitPlatformEnum } from '@/enums/commonEnum';

  const props = defineProps<{
    activeFolder: string | number;
    drawerVisible: boolean;
    showType: string;
    modulesCount?: Record<string, number>; // 模块数量统计对象
  }>();
  const emit = defineEmits(['update:drawerVisible', 'itemClick']);

  const { t } = useI18n();
  const { openModal } = useModal();
  const appStore = useAppStore();

  const folderMoreActions: ActionsItem[] = [
    {
      label: 'project.fileManagement.rename',
      eventTag: 'rename',
    },
    {
      label: 'project.fileManagement.edit',
      eventTag: 'edit',
    },
    {
      label: 'project.fileManagement.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];

  const storageKeyword = ref('');
  const originStorageList = ref<Repository[]>([]);
  const storageList = ref(originStorageList.value);
  const loading = ref(false);

  const searchStorage = debounce(() => {
    storageList.value = originStorageList.value.filter((item) => item.name.includes(storageKeyword.value));
  }, 300);

  watch(
    () => storageKeyword.value,
    () => {
      if (storageKeyword.value === '') {
        storageList.value = [...originStorageList.value];
      }
      searchStorage();
    }
  );

  /**
   * 初始化存储库列表
   */
  async function initRepositories() {
    try {
      loading.value = true;
      const res = await getRepositories(appStore.currentProjectId);
      originStorageList.value = res;
      storageList.value = originStorageList.value.map((e) => ({
        ...e,
        count: props.modulesCount?.[e.id] || 0,
      }));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  watch(
    () => props.showType,
    (val) => {
      if (val === 'Storage') {
        initRepositories();
      }
    }
  );

  /**
   * 初始化模块文件数量
   */
  watch(
    () => props.modulesCount,
    (obj) => {
      storageList.value = originStorageList.value.map((e) => ({
        ...e,
        count: obj?.[e.id] || 0,
      }));
    }
  );

  const focusItemKey = ref('');

  function setActiveFolder(id: string) {
    emit('itemClick', id);
  }

  const renamePopVisible = ref(false);

  /**
   * 删除存储库
   * @param item 列表项信息
   */
  function deleteStorage(item: Repository) {
    openModal({
      type: 'error',
      title: t('project.fileManagement.deleteStorageTipTitle', { name: item.name }),
      content: t('project.fileManagement.deleteStorageTipContent'),
      okText: t('project.fileManagement.deleteConfirm'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await deleteModule(item.id);
          Message.success(t('project.fileManagement.deleteSuccess'));
          initRepositories();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  const renameStorageTitle = ref(''); // 重命名的文件夹名称

  function resetFocusItemKey() {
    focusItemKey.value = '';
    renamePopVisible.value = false;
    renameStorageTitle.value = '';
  }

  function moreActionsClose() {
    if (!renamePopVisible.value) {
      // 当下拉菜单关闭时，若不是触发重命名气泡显示，则清空聚焦节点 key
      resetFocusItemKey();
    }
  }

  const showDrawer = ref(false);

  watch(
    () => props.drawerVisible,
    (val) => {
      showDrawer.value = val;
    }
  );
  watch(
    () => showDrawer.value,
    (val) => {
      emit('update:drawerVisible', val);
    }
  );

  const drawerLoading = ref(false);
  const isEdit = ref(false);
  const activeStorageForm = ref<RepositoryInfo>({
    id: '',
    projectId: '',
    name: '',
    platform: GitPlatformEnum.GITEA,
    url: '',
    token: '',
    userName: '',
  });
  const storageFormRef = ref<FormInstance>();
  const gitPlatformTypes = Object.values(GitPlatformEnum);

  async function getStorageDetail(id: string) {
    try {
      drawerLoading.value = true;
      const res = await getRepositoryInfo(id);
      activeStorageForm.value = res;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      drawerLoading.value = false;
    }
  }

  /**
   * 处理列表项更多按钮事件
   * @param item
   * @param listItem
   */
  function handleMoreSelect(item: ActionsItem, listItem: Repository) {
    switch (item.eventTag) {
      case 'delete':
        deleteStorage(listItem);
        resetFocusItemKey();
        break;
      case 'rename':
        renameStorageTitle.value = listItem.name || '';
        renamePopVisible.value = true;
        document.querySelector(`#renameSpan${listItem.id}`)?.dispatchEvent(new Event('click'));
        break;
      case 'edit':
        isEdit.value = true;
        showDrawer.value = true;
        getStorageDetail(listItem.id);
        break;
      default:
        break;
    }
  }

  const usernameRules = computed(() => {
    if (activeStorageForm.value.platform === GitPlatformEnum.GITEE) {
      return [{ required: true, message: t('project.fileManagement.storageUsernameNotNull') }];
    }
    return [];
  });

  function platformChange() {
    storageFormRef.value?.resetFields('userName');
  }

  const exampleUrl = 'http://github.com/xxxxx/xxxxxx.git';
  function fillUrl() {
    activeStorageForm.value.url = exampleUrl;
    storageFormRef.value?.validateField('url');
  }

  function validatePlatformUrl(value: string, callback: (error?: string | undefined) => void) {
    if (!validateGitUrl(value)) {
      callback(t('project.fileManagement.storageUrlError'));
    }
  }

  function handleDrawerCancel() {
    showDrawer.value = false;
    isEdit.value = false;
  }

  /**
   * 保存存储库
   * @param isContinue 是否继续添加
   */
  async function saveStorage(isContinue: boolean) {
    try {
      drawerLoading.value = true;
      if (isEdit.value) {
        await updateRepository({
          ...activeStorageForm.value,
        });
      } else {
        await addRepository({
          ...activeStorageForm.value,
          projectId: appStore.currentProjectId,
        });
      }
      storageFormRef.value?.resetFields();
      if (!isContinue) {
        handleDrawerCancel();
      }
      Message.success(isEdit.value ? t('common.updateSuccess') : t('common.addSuccess'));
      initRepositories();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      drawerLoading.value = false;
    }
  }

  /**
   * 处理抽屉确认
   * @param isContinue 是否继续添加
   */
  function handleDrawerConfirm(isContinue: boolean) {
    storageFormRef.value?.validate(async (errors: Record<string, ValidatedError> | undefined) => {
      if (!errors) {
        saveStorage(isContinue);
      }
    });
  }

  const testLoading = ref(false);
  function testLink() {
    storageFormRef.value?.validate(async (errors: Record<string, ValidatedError> | undefined) => {
      if (!errors) {
        try {
          testLoading.value = true;
          await connectRepository({
            url: activeStorageForm.value.url,
            userName: activeStorageForm.value.userName,
            token: activeStorageForm.value.token,
          });
          Message.success(t('project.fileManagement.testLinkSuccess'));
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          testLoading.value = false;
        }
      }
    });
  }
</script>

<style lang="less" scoped>
  .storage {
    @apply flex cursor-pointer items-center justify-between;

    border-radius: var(--border-radius-small);
    &:hover {
      background-color: rgb(var(--primary-1));
    }
    .storage-text {
      @apply flex cursor-pointer items-center;
      .storage-icon {
        margin-right: 4px;
        color: var(--color-text-4);
      }
      .storage-name {
        max-width: 170px;
        color: var(--color-text-1);
      }
      .storage-count {
        margin-left: 4px;
        color: var(--color-text-4);
      }
    }
    .storage-text--active {
      .storage-icon,
      .storage-name,
      .storage-count {
        color: rgb(var(--primary-5));
      }
    }
  }
</style>
