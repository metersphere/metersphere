<template>
  <div class="flex flex-row items-center justify-between">
    <slot name="left">
      <div class="flex">
        <a-popover v-if="props.name" title="" position="bottom">
          <div class="flex">
            <div class="one-line-text mr-1 max-h-[32px] max-w-[300px] text-[var(--color-text-1)]">
              {{ props.name }}
            </div>
            <span class="text-[var(--color-text-4)]"> ({{ props.count }})</span>
          </div>
          <template #content>
            <div class="max-w-[400px] text-[14px] font-medium text-[var(--color-text-1)]">
              {{ props.name }}
              <span class="text-[var(--color-text-4)]">({{ props.count }})</span>
            </div>
          </template>
        </a-popover>
        <slot name="nameRight"></slot>
      </div>
    </slot>
    <div class="flex flex-row gap-[12px]">
      <a-input-search
        v-if="!props.notShowInputSearch && !isAdvancedSearchMode"
        v-model:modelValue="keyword"
        size="small"
        :placeholder="props.searchPlaceholder"
        class="w-[240px]"
        allow-clear
        @press-enter="emit('keywordSearch', keyword)"
        @search="emit('keywordSearch', keyword)"
        @clear="handleClear"
      ></a-input-search>
      <a-select
        v-if="props.viewType"
        v-model:model-value="currentView"
        :trigger-props="{ contentClass: 'view-select-trigger' }"
        class="w-[160px]"
        show-footer-on-empty
      >
        <template #prefix> {{ t('advanceFilter.view') }} </template>
        <a-optgroup :label="t('advanceFilter.systemView')">
          <a-option v-for="item in internalViews" :key="item.id" :value="item.id">
            {{ item.name }}
          </a-option>
        </a-optgroup>
        <a-optgroup :label="t('advanceFilter.myView')">
          <a-option v-for="item in customViews" :key="item.id" :value="item.id">
            {{ item.name }}
            <div class="flex">
              <a-tooltip :content="t('common.rename')">
                <MsButton type="text" status="secondary" class="!mr-[4px]" @click.stop="handleRenameView(item)">
                  <MsIcon type="icon-icon_edit_outlined" class="hover:text-[rgb(var(--primary-4))]" size="12" />
                </MsButton>
              </a-tooltip>
              <a-tooltip :content="t('advanceFilter.deleteView')">
                <MsButton type="text" status="secondary" @click.stop="handleDeleteView(item)">
                  <MsIcon
                    type="icon-icon_delete-trash_outlined1"
                    class="hover:text-[rgb(var(--primary-4))]"
                    size="12"
                  />
                </MsButton>
              </a-tooltip>
            </div>
          </a-option>
        </a-optgroup>
        <template #footer>
          <div class="flex cursor-pointer items-center gap-[8px]" @click="toNewView">
            <MsIcon type="icon-icon_add_outlined" />
            {{ t('advanceFilter.newView') }}
          </div>
        </template>
      </a-select>
      <a-button
        v-if="props.viewType"
        type="outline"
        :class="`${isAdvancedSearchMode ? '' : 'arco-btn-outline--secondary'} p-[0_8px]`"
        @click="handleOpenFilter"
      >
        <template #icon>
          <MsIcon
            type="icon-icon_copy_outlined"
            :class="`${isAdvancedSearchMode ? 'text-[rgb(var(--primary-5))]' : 'text-[var(--color-text-4)]'}`"
          />
        </template>
        {{ t('common.filter') }}
      </a-button>
      <MsButton v-show="isAdvancedSearchMode" type="text" @click="clearFilter">
        {{ t('advanceFilter.clearFilter') }}
      </MsButton>

      <slot name="right"></slot>
      <MsTag
        no-margin
        size="large"
        :tooltip-disabled="true"
        class="cursor-pointer"
        theme="outline"
        @click="handleRefresh"
      >
        <MsIcon class="text-[16px] text-[var(color-text-4)]" :size="32" type="icon-icon_reset_outlined" />
      </MsTag>
    </div>
  </div>
  <FilterDrawer
    ref="filterDrawerRef"
    v-model:visible="visible"
    :current-view="currentView"
    :view-type="props.viewType as ViewTypeEnum"
    :all-view-names="allViewNames"
    :config-list="props.filterConfigList"
    :custom-list="props.customFieldsConfigList"
    @handle-filter="handleFilter"
    @refresh-view-list="getUserViewList"
  />
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTag from '../ms-tag/ms-tag.vue';
  import FilterDrawer from './filterDrawer.vue';

  import { deleteView, getViewList } from '@/api/modules/user/index';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { ViewTypeEnum } from '@/enums/advancedFilterEnum';

  import { FilterFormItem, FilterResult, ViewItem } from './type';

  const props = defineProps<{
    rowCount: number;
    filterConfigList: FilterFormItem[]; // 系统字段
    customFieldsConfigList?: FilterFormItem[]; // 自定义字段
    searchPlaceholder?: string;
    name?: string;
    count?: number;
    notShowInputSearch?: boolean;
    viewType?: ViewTypeEnum;
  }>();

  const emit = defineEmits<{
    (e: 'keywordSearch', value: string | undefined): void; // keyword 搜索 TODO:可以去除，父组件通过 v-model:keyword 获取关键字
    (e: 'advSearch', value: FilterResult): void; // 高级搜索
    (e: 'refresh', value: FilterResult): void;
  }>();

  const { t } = useI18n();
  const appStore = useAppStore();

  const keyword = defineModel<string>('keyword', { default: '' });
  const visible = ref(false);
  const filterResult = ref<FilterResult>({ searchMode: 'AND', conditions: [] });

  const currentView = ref(''); // 当前视图
  const internalViews = ref<ViewItem[]>([]);
  const customViews = ref<ViewItem[]>([]);
  const allViewNames = computed(() => [...internalViews.value, ...customViews.value].map((item) => item.name));
  async function getUserViewList() {
    try {
      const res = await getViewList(props.viewType as ViewTypeEnum, appStore.currentProjectId);
      internalViews.value = res.internalViews;
      customViews.value = res.customViews;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
  onMounted(async () => {
    await getUserViewList();
    currentView.value = internalViews.value[0].id;
  });

  const filterDrawerRef = ref<InstanceType<typeof FilterDrawer>>();
  function toNewView() {
    visible.value = true;
    filterDrawerRef.value?.resetToNewViewForm();
  }
  function handleRenameView(item: ViewItem) {
    // TODO lmy 重命名
  }
  async function handleDeleteView(item: ViewItem) {
    try {
      await deleteView(item.viewType, item.id);
      Message.success(t('common.deleteSuccess'));
      getUserViewList();
      currentView.value = internalViews.value[0].id;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const isAdvancedSearchMode = ref(false);
  const handleFilter = (filter: FilterResult) => {
    keyword.value = '';
    const haveConditions: boolean =
      filter.conditions?.some((item) => {
        const valueCanEmpty = ['EMPTY', 'NOT_EMPTY'].includes(item.operator as string);
        const isValidValue = typeof item.value !== 'number' ? item.value?.length : item.value;
        return valueCanEmpty || isValidValue;
      }) ?? false;
    // 开启高级筛选：非默认视图或有筛选条件
    isAdvancedSearchMode.value = currentView.value !== internalViews.value[0].id || haveConditions;
    filterResult.value = filter;
    emit('advSearch', filter);
  };

  const handleRefresh = () => {
    emit('refresh', filterResult.value);
  };

  const handleClear = () => {
    keyword.value = '';
    emit('keywordSearch', '');
  };

  const handleOpenFilter = () => {
    visible.value = !visible.value;
  };

  // 清除筛选
  function clearFilter() {
    if (currentView.value === internalViews.value[0].id) {
      filterDrawerRef.value?.handleReset();
    } else {
      currentView.value = internalViews.value[0].id;
    }
  }

  defineExpose({
    isAdvancedSearchMode,
  });
</script>

<style lang="less">
  .view-select-trigger .arco-select-dropdown {
    .arco-select-option-content {
      @apply flex w-full items-center justify-between;
    }
    .arco-select-dropdown-list-wrapper {
      max-height: 255px;
    }
    .arco-select-group-title {
      margin: 0 2px;
      padding: 0 8px;
      color: var(--color-text-brand);
    }
    .arco-select-dropdown-footer {
      padding: 3px 8px;
      border: none;
    }
  }
</style>
