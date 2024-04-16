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
        height: 'calc(100vh - 325px)',
      }"
      :data="storageList"
      :bordered="false"
      :split="false"
      :empty-text="t('project.fileManagement.noStorage')"
      item-key-field="id"
      class="mr-[-6px]"
    >
      <template #title="{ item, index }">
        <div :key="index" class="storage" @click="setActiveFolder(item.id)">
          <div :class="activeStorageNode === item.id ? 'storage-text storage-text--active' : 'storage-text'">
            <MsIcon type="icon-icon_git" class="storage-icon" />
            <div class="storage-name">{{ item.name }}</div>
            <div class="storage-count">({{ item.count }})</div>
          </div>
        </div>
      </template>
    </MsList>
  </a-spin>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { debounce } from 'lodash-es';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsList from '@/components/pure/ms-list/index.vue';

  import { getRepositories } from '@/api/modules/project-management/fileManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { Repository } from '@/models/projectManagement/file';

  const props = defineProps<{
    activeFolder: string | number;
    drawerVisible: boolean;
    showType: string;
    modulesCount?: Record<string, number>; // 模块数量统计对象
  }>();
  const emit = defineEmits(['update:drawerVisible', 'itemClick', 'update:activeFolder']);

  const { t } = useI18n();
  const appStore = useAppStore();

  const activeStorageNode = computed({
    get() {
      return props.activeFolder;
    },
    set(val) {
      emit('update:activeFolder', val);
    },
  });

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
  async function initRepositories(setDefaultKeys = false) {
    try {
      loading.value = true;
      const res = await getRepositories(appStore.currentProjectId);
      originStorageList.value = res;
      storageList.value = originStorageList.value.map((e) => ({
        ...e,
        count: props.modulesCount?.[e.id] || 0,
      }));
      if (setDefaultKeys) {
        activeStorageNode.value = storageList.value[0].id;
        emit('itemClick', storageList.value[0].id, storageList.value);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  watchEffect(() => {
    if (props.showType === 'Storage') {
      initRepositories();
    }
  });

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
    emit('itemClick', id, storageList.value);
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
