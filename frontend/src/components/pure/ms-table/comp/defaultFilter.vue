<template>
  <a-trigger v-model:popup-visible="visible" trigger="click">
    <span class="cursor-pointer pr-[2px]" @click="visible = true">
      <svg-icon
        width="16px"
        height="16px"
        :name="visible ? 'filter-icon-color' : 'filter-icon'"
        class="text-[12px] font-medium"
      />
    </span>
    <template #content>
      <div class="arco-table-filters-content">
        <div class="arco-table-filters-content-list">
          <div class="arco-table-filters-content-wrap max-h-[300px] px-[12px] py-[4px]">
            <a-checkbox-group v-if="props.mode === 'static'" v-model="checkedList" size="mini" direction="vertical">
              <a-checkbox
                v-for="(item, index) of props.options"
                :key="item[props.valueKey || 'value']"
                :value="item[props.valueKey || 'value']"
              >
                <a-tooltip
                  :content="item[props.labelKey || 'label']"
                  :mouse-enter-delay="300"
                  :disabled="!item[props.labelKey || 'label']"
                >
                  <div class="one-line-text max-w-[120px]">
                    <slot name="item" :filter-item="item" :index="index">
                      <div class="one-line-text max-w-[120px]">{{ item[props.labelKey || 'label'] }}</div>
                    </slot>
                  </div>
                </a-tooltip>
              </a-checkbox>
            </a-checkbox-group>
          </div>
          <div v-if="props.mode === 'remote'" class="w-[200px] p-[12px] pb-[8px]">
            <MsSelect
              v-model:model-value="checkedList"
              mode="remote"
              :options="[]"
              :placeholder="props.placeholderText"
              multiple
              :value-key="props.valueKey || 'id'"
              :label-key="props.labelKey"
              :filter-option="false"
              allow-clear
              :search-keys="['name']"
              :loading="loading"
              :remote-func="loadList"
              :remote-extra-params="{ ...props.loadOptionParams }"
              :option-label-render="optionLabelRender"
              :should-calculate-max-tag="false"
            >
            </MsSelect>
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

  import MsSelect from '@/components/business/ms-select/index';

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
      placeholderText?: string;
    }>(),
    {
      mode: 'static',
    }
  );
  const emit = defineEmits<{
    (e: 'handleConfirm', value: (string | number)[] | string[] | undefined): void;
  }>();

  const visible = ref(false);

  const checkedList = ref<(string | number)[]>([]);

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

  const optionLabelRender = (option: SelectOptionData) => {
    if (option.email !== '') {
      return `<span class='text-[var(--color-text-1)]'>${option.name}</span><span class='text-[var(--color-text-4)] ml-[4px]'>(${option.email})</span>`;
    }
    return `<span class='text-[var(--color-text-1)]'>${option.name}</span>`;
  };

  const loadList = async (params: Record<string, any>) => {
    try {
      loading.value = true;
      const { keyword, ...rest } = params;
      if (props.remoteMethod) {
        const list = (await initRemoteOptionsFunc(props.remoteMethod, { keyword, ...rest })) || [];
        list.forEach((item: any) => {
          if (props.valueKey) {
            item.id = item[props.valueKey || 'id'] as string;
          }
        });
        return list;
      }
      return [];
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      return [];
    } finally {
      loading.value = false;
    }
  };
</script>

<style scoped lang="less">
  .arco-table-filters-content-wrap {
    @apply overflow-y-auto;
    .ms-scroll-bar();
  }
</style>
