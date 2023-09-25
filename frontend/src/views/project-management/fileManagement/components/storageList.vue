<template>
  <a-input
    v-model:model-value="storageKeyword"
    :placeholder="t('project.fileManagement.folderSearchPlaceholder')"
    allow-clear
    class="mb-[8px]"
  ></a-input>
  <MsList
    v-model:focus-item-key="focusItemKey"
    :virtual-list-props="{
      height: 'calc(100vh - 310px)',
    }"
    :data="storageList"
    :bordered="false"
    :split="false"
    :item-more-actions="folderMoreActions"
    :empty-text="t('project.fileManagement.noStorage')"
    class="mr-[-6px]"
    @more-action-select="handleMoreSelect"
    @more-actions-close="moreActionsClose"
  >
    <template #title="{ item, index }">
      <div :key="index" class="storage" @click="setActiveFolder(item.key)">
        <div :class="props.activeFolder === item.key ? 'storage-text storage-text--active' : 'storage-text'">
          <MsIcon type="icon-icon_git" class="storage-icon" />
          <div class="storage-name">{{ item.title }}</div>
          <div class="storage-count">({{ item.count }})</div>
        </div>
      </div>
    </template>
    <template #itemAction="{ item }">
      <popConfirm
        mode="rename"
        :field-config="{ field: renameStorageTitle }"
        :all-names="[]"
        @close="resetFocusItemKey"
      >
        <span :id="`renameSpan${item.key}`" class="relative"></span>
      </popConfirm>
    </template>
  </MsList>
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
          show-word-limit
        ></a-input>
      </a-form-item>
      <a-form-item :label="t('project.fileManagement.storagePlatform')" field="platform" asterisk-position="end">
        <a-radio-group v-model:model-value="activeStorageForm.platform" type="button" @change="platformChange">
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
        field="username"
        asterisk-position="end"
        :rules="usernameRules"
      >
        <a-input
          v-model:model-value="activeStorageForm.username"
          :max-length="250"
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
  import { debounce } from 'lodash-es';
  import { FormInstance, Message, ValidatedError } from '@arco-design/web-vue';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { validateGitUrl } from '@/utils/validate';
  import MsList from '@/components/pure/ms-list/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsFormItemSub from '@/components/business/ms-form-item-sub/index.vue';
  import popConfirm from './popConfirm.vue';
  import { GitPlatformEnum } from '@/enums/commonEnum';

  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  const props = defineProps<{
    activeFolder: string | number;
    drawerVisible: boolean;
  }>();
  const emit = defineEmits(['update:drawerVisible', 'itemClick']);

  const { t } = useI18n();
  const { openModal } = useModal();

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
  const originStorageList = ref([
    {
      title: 'storage1',
      key: '1',
      count: 129,
    },
    {
      title: 'storage2',
      key: '2',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3sss',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3asa',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3sda',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3ads',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3asdd',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3sdfsdf',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3dgsg',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3asd',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3fwcdw',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3wef',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3f2ed',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3fe2fe',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3fe2feg2',
      count: 129,
    },
    {
      title: 'storage3',
      key: '32s22',
      count: 129,
    },
    {
      title: 'storage3',
      key: '323ff22f',
      count: 129,
    },
    {
      title: 'storage3',
      key: '33f3f',
      count: 129,
    },
    {
      title: 'storage1',
      key: '1f2f',
      count: 129,
    },
    {
      title: 'storage2',
      key: '2ef2ef',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3sd2fd',
      count: 129,
    },
    {
      title: 'storage3',
      key: '32ef2ef',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3f32v',
      count: 129,
    },
    {
      title: 'storage3',
      key: '323fgt2',
      count: 129,
    },
    {
      title: 'storage3',
      key: '324g23r',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3233d2',
      count: 129,
    },
    {
      title: 'storage3',
      key: '32gftr3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '323f2dd',
      count: 129,
    },
    {
      title: 'storage3',
      key: '32fgede',
      count: 129,
    },
    {
      title: 'storage3',
      key: '32efsad',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3fsdgsd',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3sdgvsdxcs',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3asxc',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3csdcdg',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3gsg3f3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3d3dxcsd',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3c3wervvb',
      count: 129,
    },
    {
      title: 'storage1',
      key: '13vf33',
      count: 129,
    },
    {
      title: 'storage2',
      key: '234444',
      count: 129,
    },
    {
      title: 'storage3',
      key: '32323d',
      count: 129,
    },
    {
      title: 'storage3',
      key: '323ffef',
      count: 129,
    },
    {
      title: 'storage3',
      key: 'f23f23f3',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3f2f2f',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3rf4f2',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3dsewd',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3x2ef23f',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3f3f43',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3h4hgp',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3yu6n',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3nyuk',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3hn6',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3nyguhnmm6',
      count: 129,
    },
    {
      title: 'storage3',
      key: '36n6n',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3yukyuk',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3yhnn',
      count: 129,
    },
    {
      title: 'storage3',
      key: '3gntnty',
      count: 129,
    },
  ]);
  const storageList = ref(originStorageList.value);

  const searchStorage = debounce(() => {
    storageList.value = originStorageList.value.filter((item) => item.title.includes(storageKeyword.value));
  }, 300);

  watch(
    () => storageKeyword.value,
    () => {
      if (storageKeyword.value === '') {
        storageList.value = originStorageList.value;
      }
      searchStorage();
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
  function deleteStorage(item: any) {
    openModal({
      type: 'error',
      title: t('project.fileManagement.deleteStorageTipTitle', { name: item.title }),
      content: t('project.fileManagement.deleteStorageTipContent'),
      okText: t('project.fileManagement.deleteConfirm'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          Message.success(t('project.fileManagement.deleteSuccess'));
        } catch (error) {
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
  const activeStorageForm = ref({
    name: '',
    platform: GitPlatformEnum.GITHUB,
    url: '',
    token: '',
    username: '',
  });
  const storageFormRef = ref<FormInstance>();
  const gitPlatformTypes = Object.values(GitPlatformEnum);

  async function getStorageDetail(id: string) {
    try {
      drawerLoading.value = true;
      activeStorageForm.value = {
        name: 'xxx',
        platform: GitPlatformEnum.GITHUB,
        url: 'xxxxxxx',
        token: 'sxsxsx',
        username: 'ddwdwdwwd',
      };
    } catch (error) {
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
  function handleMoreSelect(item: ActionsItem, listItem: any) {
    switch (item.eventTag) {
      case 'delete':
        deleteStorage(listItem);
        resetFocusItemKey();
        break;
      case 'rename':
        renameStorageTitle.value = listItem.title || '';
        renamePopVisible.value = true;
        document.querySelector(`#renameSpan${listItem.key}`)?.dispatchEvent(new Event('click'));
        break;
      case 'edit':
        isEdit.value = true;
        showDrawer.value = true;
        getStorageDetail(listItem.key);
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
    storageFormRef.value?.resetFields('username');
  }

  const exampleUrl = 'http://github.com/xxxxx/xxxxxx.git';
  function fillUrl() {
    activeStorageForm.value.url = exampleUrl;
    storageFormRef.value?.validateField('url');
  }

  function validatePlatformUrl(value: any, callback: (error?: string | undefined) => void) {
    if (!validateGitUrl(value)) {
      callback(t('project.fileManagement.storageUrlError'));
    }
  }

  async function saveStorage(isContinue: boolean) {}

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

  function handleDrawerCancel() {
    showDrawer.value = false;
    storageFormRef.value?.resetFields();
  }

  const testLoading = ref(false);

  function testLink() {
    storageFormRef.value?.validate(async (errors: Record<string, ValidatedError> | undefined) => {
      if (!errors) {
        testLoading.value = true;
        setTimeout(() => {
          testLoading.value = false;
        }, 2000);
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
