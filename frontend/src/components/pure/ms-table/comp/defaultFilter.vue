<template>
  <a-trigger v-model:popup-visible="visible" trigger="click">
    <span class="cursor-pointer pr-[2px]" @click="visible = true">
      <svg-icon
        width="16px"
        height="16px"
        :name="isHighlight ? 'filter-icon-color' : 'filter-icon'"
        class="text-[12px] font-medium"
      />
    </span>
    <template #content>
      <div class="arco-table-filters-content">
        <div class="arco-table-filters-content-list">
          <div v-if="props.mode === 'static'" class="arco-table-filters-content-wrap max-h-[300px] px-[12px] py-[4px]">
            <a-checkbox-group v-model:model-value="checkedList" size="mini" direction="vertical">
              <!-- 用于执行结果排队空统一让后台传递PENDING，展示内容为 -  -->
              <a-checkbox v-if="props.emptyFilter" value="PENDING">-</a-checkbox>
              <a-checkbox
                v-for="(item, index) of props.options"
                :key="item[props.valueKey || 'value']"
                :value="item[props.valueKey || 'value']"
              >
                <a-tooltip
                  :content="item[props.labelKey || 'label']"
                  :mouse-enter-delay="300"
                  :disabled="props.disabledTooltip || !item[props.labelKey || 'label']"
                >
                  <div class="one-line-text max-w-[120px]">
                    <slot name="item" :filter-item="item" :index="index">
                      {{ item[props.labelKey || 'label'] }}
                    </slot>
                  </div>
                </a-tooltip>
              </a-checkbox>
            </a-checkbox-group>
          </div>
          <div v-if="props.mode === 'remote'" class="w-[190px] p-[12px] py-[8px]">
            <a-input
              v-model:model-value="filterKeyword"
              class="mb-[8px]"
              :placeholder="t('common.pleaseInput')"
              allow-clear
            >
              <template #prefix>
                <MsIcon
                  type="icon-icon_search-outline_outlined"
                  class="text-[var(--color-text-input-border)]"
                  size="16"
                />
              </template>
            </a-input>
            <a-spin class="w-full" :loading="loading">
              <MsList
                v-model:data="filterListOptions"
                mode="remote"
                item-key-field="id"
                :item-border="false"
                class="w-full rounded-[var(--border-radius-small)]"
                :no-more-data="false"
                :virtual-list-props="{
                  height: '160px',
                }"
              >
                <template #title="{ item }">
                  <a-tooltip
                    :content="item[props.labelKey || 'label']"
                    :mouse-enter-delay="300"
                    :disabled="!item[props.labelKey || 'label']"
                  >
                    <div class="w-full" @click.stop="checkItem(item.value)">
                      <a-checkbox
                        :model-value="checkedList.includes(item.value)"
                        :value="item.value"
                        @click.stop="checkItem(item.value)"
                      >
                        <div class="one-line-text max-w-[120px]">
                          {{ item.label }}
                        </div>
                      </a-checkbox>
                    </div>
                  </a-tooltip>
                </template>
              </MsList>
            </a-spin>
          </div>
          <div
            :class="`${
              props.mode === 'static' ? 'justify-between' : 'justify-end'
            } arco-table-filters-bottom flex h-[38px] items-center`"
          >
            <a-button size="mini" type="secondary" @click="handleFilterReset">
              {{ t('common.reset') }}
            </a-button>
            <a-button size="mini" type="primary" @click="handleFilterSubmit()">
              {{ t('common.confirm') }}
            </a-button>
          </div>
        </div>
      </div>
    </template>
  </a-trigger>
</template>

<script lang="ts" setup>
  import { SelectOptionData } from '@arco-design/web-vue';
  import { debounce } from 'lodash-es';

  import MsList from '@/components/pure/ms-list/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { FilterRemoteMethodsEnum } from '@/enums/tableFilterEnum';

  import { initRemoteOptionsFunc } from './filterConfig';

  const { t } = useI18n();

  export interface FilterListItem {
    [key: string]: any;
  }
  const props = withDefaults(
    defineProps<{
      options?: FilterListItem[];
      valueKey?: string;
      labelKey?: string;
      mode?: 'static' | 'remote';
      remoteMethod?: FilterRemoteMethodsEnum; // 加载选项方法
      loadOptionParams?: Record<string, any>; // 请求下拉的参数
      dataIndex?: string | undefined;
      filter: Record<string, any>;
      emptyFilter?: boolean; // 增加-空选项查询
      disabledTooltip?: boolean; // 是否禁用tooltip
    }>(),
    {
      mode: 'static',
    }
  );
  const emit = defineEmits<{
    (e: 'handleConfirm', value: (string | number | boolean)[] | string[] | undefined): void;
  }>();

  const visible = ref(false);

  const checkedList = defineModel<string[]>('checkedList', {
    default: [],
  });

  const handleFilterReset = () => {
    checkedList.value = [];
    emit('handleConfirm', []);
    visible.value = false;
  };

  const handleFilterSubmit = () => {
    emit('handleConfirm', checkedList.value);
    visible.value = false;
  };

  const loading = ref(true);

  const isHighlight = computed(() => {
    if (props.filter && props.dataIndex) {
      return (props.filter[props.dataIndex] || []).length > 0;
    }
    return false;
  });

  const isNoFilter = computed(() => {
    if (props.filter && JSON.stringify(props.filter) !== '{}') {
      return !Object.keys(props.filter).some((key: any) => {
        return (props.filter[key] || []).length > 0;
      });
    }
    return true;
  });

  const originFilterList = ref<SelectOptionData[]>([]);
  const filterListOptions = ref<SelectOptionData[]>([]);

  const filterKeyword = ref('');

  async function initOptions() {
    try {
      loading.value = true;
      if (props.remoteMethod) {
        const list =
          (await initRemoteOptionsFunc(props.remoteMethod, {
            keyword: filterKeyword.value,
            ...props.loadOptionParams,
          })) || [];
        list.forEach((item: any) => {
          item.value = item[props.valueKey || 'id'] as string;
          item.label = (item[props.labelKey || 'name'] as string) || '';
        });
        filterListOptions.value = list;
        originFilterList.value = list;
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  function checkItem(value: string) {
    if (checkedList.value.includes(value)) {
      checkedList.value = checkedList.value.filter((e) => e !== value);
    } else {
      checkedList.value.push(value);
    }
  }

  // 用于切换的时候重置清空上一次的选择
  watch(
    () => isNoFilter.value,
    (val) => {
      if (val) {
        checkedList.value = [];
      }
    }
  );

  const searchItem = debounce(() => {
    filterListOptions.value = originFilterList.value.filter((item: SelectOptionData) =>
      item.label?.includes(filterKeyword.value)
    );
  }, 300);

  watch(
    () => visible.value,
    (val) => {
      // 避免打开后选择未点击确认再次打开有选择选项
      if (isNoFilter.value) {
        checkedList.value = [];
      }
      if (val && props.mode === 'remote') {
        filterKeyword.value = '';
        initOptions();
      }
    }
  );

  watch(
    () => filterKeyword.value,
    () => {
      if (filterKeyword.value === '') {
        filterListOptions.value = [...originFilterList.value];
      }
      searchItem();
    }
  );
</script>

<style scoped lang="less">
  .arco-table-filters-content-wrap {
    @apply overflow-y-auto;
    .ms-scroll-bar();
  }
  .arco-table-filters-bottom {
    border-color: var(--color-text-n8);
  }
</style>
