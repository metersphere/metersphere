<template>
  <div class="flex flex-row flex-wrap items-center justify-between gap-[16px]">
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
    <div class="flex flex-row gap-[8px]">
      <a-input-search
        v-if="!props.notShowInputSearch && !isAdvancedSearchMode"
        v-model:model-value="keyword"
        size="small"
        :placeholder="props.searchPlaceholder"
        class="w-[187px]"
        allow-clear
        @press-enter="emit('keywordSearch', keyword)"
        @search="emit('keywordSearch', keyword)"
        @clear="handleClear"
      ></a-input-search>
      <!-- 在select的option里写input,鼠标点击和失焦不好使,故单独写了一个下拉trigger -->
      <a-trigger
        v-model:popup-visible="viewSelectOptionVisible"
        trigger="click"
        :popup-translate="[0, 4]"
        content-class="arco-trigger-menu view-custom-trigger-content"
      >
        <a-select
          v-if="props.viewType"
          v-model:model-value="currentView"
          :loading="viewListLoading"
          :options="[...internalViews, ...customViews].map((item) => ({ value: item.id, label: item.name }))"
          :trigger-props="{ contentClass: 'view-select-trigger' }"
          class="w-[145px]"
          show-footer-on-empty
        >
          <template #prefix> {{ t('advanceFilter.view') }} </template>
        </a-select>
        <template #content>
          <a-spin class="w-full" :loading="viewListLoading">
            <div class="view-option-title">
              <span>{{ t('advanceFilter.systemView') }}</span>
              <a-divider></a-divider>
            </div>
            <div
              v-for="item in internalViews"
              :key="item.id"
              :class="[`view-option-item ${item.id === currentView ? 'view-option-item-active' : ''}`]"
              @click="changeView(item)"
            >
              {{ item.name }}
            </div>
            <div class="view-option-title">
              <span>{{ t('advanceFilter.myView') }}</span>
              <a-divider></a-divider>
            </div>
            <template v-for="item in customViews" :key="item.id">
              <div
                v-show="!item.isShowNameInput"
                :class="[`view-option-item ${item.id === currentView ? 'view-option-item-active' : ''}`]"
                @click="changeView(item)"
              >
                <a-tooltip :content="item.name">
                  <div class="one-line-text">
                    {{ item.name }}
                  </div>
                </a-tooltip>
                <div class="select-extra flex">
                  <a-tooltip :content="t('common.rename')">
                    <MsButton type="text" status="secondary" class="!mr-[4px]" @click="handleToRenameView(item)">
                      <MsIcon type="icon-icon_edit_outlined" class="hover:text-[rgb(var(--primary-4))]" size="14" />
                    </MsButton>
                  </a-tooltip>
                  <a-tooltip :content="t('advanceFilter.deleteView')">
                    <MsButton type="text" :disabled="deleteLoading" status="secondary" @click="handleDeleteView(item)">
                      <MsIcon
                        type="icon-icon_delete-trash_outlined1"
                        class="hover:text-[rgb(var(--primary-4))]"
                        size="14"
                      />
                    </MsButton>
                  </a-tooltip>
                </div>
              </div>
              <ViewNameInput
                v-if="item.isShowNameInput"
                :ref="(el:refItem) => setNameInputRefMap(el, item)"
                v-model:form="formModel"
                :all-names="allViewNames.filter((name) => name !== item.name)"
                not-show-word-limit
                @handle-submit="handleRenameView"
              />
            </template>
            <div class="flex cursor-pointer items-center gap-[8px] px-[8px] py-[3px]" @click="toNewView">
              <MsIcon type="icon-icon_add_outlined" />
              {{ t('advanceFilter.newView') }}
            </div>
          </a-spin>
        </template>
      </a-trigger>
      <a-button
        v-if="props.viewType"
        type="outline"
        :class="`${isAdvancedSearchMode ? '' : 'arco-btn-outline--secondary'} p-[0_8px]`"
        @click="handleOpenFilter"
      >
        <template #icon>
          <MsIcon
            type="icon-icon_filter"
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
    v-if="props.viewType"
    ref="filterDrawerRef"
    v-model:visible="visible"
    :current-view="currentView"
    :view-type="props.viewType"
    :internal-views="internalViews"
    :all-view-names="allViewNames"
    :config-list="props.filterConfigList"
    :custom-list="props.customFieldsConfigList"
    :can-not-add-view="canNotAddView"
    :member-options="memberOptions"
    @handle-filter="handleFilter"
    @refresh-view-list="getUserViewList"
    @change-view-to-first-custom="changeViewToFirstCustom"
  />
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTag from '../ms-tag/ms-tag.vue';
  import ViewNameInput from './components/viewNameInput.vue';
  import FilterDrawer from './filterDrawer.vue';

  import { getProjectOptions } from '@/api/modules/project-management/projectMember';
  import { deleteView, getViewList, updateView } from '@/api/modules/user/index';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { ViewTypeEnum } from '@/enums/advancedFilterEnum';

  import { ConditionsItem, FilterFormItem, FilterResult, ViewItem } from './type';

  const props = defineProps<{
    filterConfigList: FilterFormItem[]; // 系统字段
    customFieldsConfigList?: FilterFormItem[]; // 自定义字段
    searchPlaceholder?: string;
    name?: string;
    count?: number;
    notShowInputSearch?: boolean;
    viewType?: ViewTypeEnum;
    viewName?: string;
  }>();

  const emit = defineEmits<{
    (e: 'keywordSearch', value: string | undefined): void; // keyword 搜索 TODO:可以去除，父组件通过 v-model:keyword 获取关键字
    (e: 'advSearch', value: FilterResult, viewId: string, isAdvancedSearchMode: boolean): void; // 高级搜索
    (e: 'refresh', value: FilterResult): void;
  }>();

  const { t } = useI18n();
  const appStore = useAppStore();

  const keyword = defineModel<string>('keyword', { default: '' });
  const visible = ref(false);
  const filterResult = ref<FilterResult>({ searchMode: 'AND', conditions: [] });

  const currentView = ref(props.viewName || ''); // 当前视图
  const internalViews = ref<ViewItem[]>([]);
  const customViews = ref<ViewItem[]>([]);
  const viewListLoading = ref(false);
  const allViewNames = computed(() => [...internalViews.value, ...customViews.value].map((item) => item.name));
  const canNotAddView = computed(() => customViews.value.length >= 10);
  async function getUserViewList() {
    try {
      viewListLoading.value = true;
      const res = await getViewList(props.viewType as ViewTypeEnum, appStore.currentProjectId);
      internalViews.value = res.internalViews;
      customViews.value = res.customViews;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      viewListLoading.value = false;
    }
  }
  const memberOptions = ref<{ label: string; value: string }[]>([]);
  async function getMemberOptions() {
    const res = await getProjectOptions(appStore.currentProjectId);
    memberOptions.value = [{ name: t('common.currentUser'), id: 'CURRENT_USER' }, ...res].map((e: any) => ({
      label: e.name,
      value: e.id,
    }));
  }

  const isAdvancedSearchMode = ref(false);

  onMounted(async () => {
    if (props.viewType) {
      getMemberOptions();
      await getUserViewList();
      if (props.viewName) {
        currentView.value = props.viewName;
        isAdvancedSearchMode.value = currentView.value !== internalViews.value[0].id;
      } else {
        currentView.value = internalViews.value[0]?.id;
      }
    }
  });

  const filterDrawerRef = ref<InstanceType<typeof FilterDrawer>>();

  watch(
    () => props.viewType,
    async () => {
      await getUserViewList();
      if (currentView.value === internalViews.value[0]?.id) {
        filterDrawerRef.value?.getUserViewDetail(currentView.value);
      } else {
        currentView.value = internalViews.value[0]?.id;
      }
    }
  );

  const viewSelectOptionVisible = ref(false);
  function changeView(item: ViewItem) {
    currentView.value = item.id;
    viewSelectOptionVisible.value = false;
  }

  watch(
    () => viewSelectOptionVisible.value,
    (val) => {
      if (!val) {
        customViews.value.forEach((item) => {
          item.isShowNameInput = false;
        });
      }
    }
  );

  async function changeViewToFirstCustom() {
    await getUserViewList();
    currentView.value = customViews.value[0].id;
  }

  function toNewView() {
    if (canNotAddView.value) {
      Message.warning(t('advanceFilter.maxViewTip'));
      return;
    }
    visible.value = true;
    filterDrawerRef.value?.resetToNewViewForm();
  }

  type refItem = Element | ComponentPublicInstance | null;
  const viewNameInputRefMap: Record<string, any> = {};
  function setNameInputRefMap(el: refItem, item: ViewItem) {
    if (el) {
      viewNameInputRefMap[`${item.id}`] = el;
    }
  }
  const formModel = ref({ name: '', id: '' });
  function handleToRenameView(item: ViewItem) {
    formModel.value.id = item.id;
    formModel.value.name = item.name;
    item.isShowNameInput = true;
    nextTick(() => {
      viewNameInputRefMap[item.id]?.inputFocus();
    });
  }
  async function handleRenameView() {
    try {
      await updateView(props.viewType as string, { name: formModel.value.name, id: formModel.value.id });
      Message.success(t('common.saveSuccess'));
      await getUserViewList();
      if (formModel.value.id === currentView.value) {
        filterDrawerRef.value?.getUserViewDetail(currentView.value);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  // 删除试图
  const deleteLoading = ref(false);
  async function handleDeleteView(item: ViewItem) {
    try {
      deleteLoading.value = true;
      await deleteView(props.viewType as string, item.id);
      Message.success(t('common.deleteSuccess'));
      await getUserViewList();
      if (item.id === currentView.value) {
        currentView.value = internalViews.value[0].id;
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      deleteLoading.value = false;
    }
  }

  const getIsValidValue = (item: ConditionsItem) => {
    if (typeof item.value === 'boolean') return String(item.value).length;
    if (typeof item.value === 'number') return item.value;
    return item.value?.length;
  };
  const handleFilter = (filter: FilterResult) => {
    keyword.value = '';
    const haveConditions: boolean =
      filter.conditions?.some((item) => {
        const valueCanEmpty = ['EMPTY', 'NOT_EMPTY'].includes(item.operator as string);
        return valueCanEmpty || getIsValidValue(item);
      }) ?? false;
    // 开启高级筛选：非默认视图或有筛选条件
    isAdvancedSearchMode.value = currentView.value !== internalViews.value[0].id || haveConditions;
    filterResult.value = filter;
    emit('advSearch', filter, currentView.value, isAdvancedSearchMode.value);
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
      handleFilter({ searchMode: 'AND', conditions: [] });
    } else {
      currentView.value = internalViews.value[0].id;
    }
  }

  defineExpose({
    clearFilter,
    isAdvancedSearchMode,
  });
</script>

<style lang="less">
  .view-select-trigger {
    display: none;
  }
  .view-custom-trigger-content {
    width: 145px;
    max-height: 300px;
    .ms-scroll-bar();
    .view-option-title {
      display: flex;
      align-items: center;
      margin: 0 2px;
      padding: 0 8px;
      font-size: 12px;
      color: var(--color-text-brand);
      line-height: 20px;
      .arco-divider-horizontal {
        margin: 4px 0 4px 8px;
        min-width: 0;
        border-bottom-color: var(--color-text-n8);
        flex: 1;
      }
    }
    .select-extra {
      visibility: hidden;
    }
    .view-option-item {
      padding: 3px 8px;
      border-radius: 4px;
      cursor: pointer;
      @apply flex w-full items-center justify-between;
      &:hover {
        background-color: rgb(var(--primary-1));
        .select-extra {
          visibility: visible;
        }
      }
      &-active {
        color: rgb(var(--primary-5)) !important;
        background-color: rgb(var(--primary-1)) !important;
      }
    }
    .arco-form-item-content,
    .arco-input-wrapper {
      height: 28px;
    }
    .arco-form-item-message {
      margin: 0;
    }
  }
</style>
