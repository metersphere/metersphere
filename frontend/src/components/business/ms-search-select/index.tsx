import { watch, ref, h, defineComponent } from 'vue';
import { debounce } from 'lodash-es';
import { useI18n } from '@/hooks/useI18n';

import type { SelectOptionData } from '@arco-design/web-vue';

export type ModelType = string | number | Record<string, any> | (string | number | Record<string, any>)[];

export interface MsSearchSelectProps {
  modelValue: ModelType;
  allowClear?: boolean;
  placeholder?: string;
  prefix?: string;
  searchKeys: string[]; // 需要搜索的 key 名，关键字会遍历这个 key 数组，然后取 item[key] 进行模糊匹配
  options: SelectOptionData[];
  optionLabelRender?: (item: SelectOptionData) => string; // 自定义 option 的 label 渲染，返回一个 html 字符串，默认使用 item.label
}

export default defineComponent(
  (props: MsSearchSelectProps, { emit }) => {
    const { t } = useI18n();

    const innerValue = ref(props.modelValue);
    const filterOptions = ref<SelectOptionData[]>([...props.options]);
    watch(
      () => props.modelValue,
      (val) => {
        innerValue.value = val;
      }
    );

    watch(
      () => props.options,
      (arr) => {
        filterOptions.value = [...arr];
      }
    );

    function handleUserSearch(val: string) {
      if (val.trim() === '') {
        filterOptions.value = [...props.options];
        return;
      }
      const highlightedKeyword = `<span class="text-[rgb(var(--primary-4))]">${val}</span>`;
      filterOptions.value = props.options
        .map((e) => {
          const item = { ...e };
          let hasMatch = false;
          for (let i = 0; i < props.searchKeys.length; i++) {
            // 遍历传入的搜索字段
            const key = props.searchKeys[i];

            if (e[key].includes(val)) {
              // 是否匹配
              hasMatch = true;
              item[key] = e[key].replace(new RegExp(val, 'gi'), highlightedKeyword); // 高亮关键字替换
            }
          }
          if (hasMatch) {
            return item;
          }
          return null;
        })
        .filter((e) => e) as SelectOptionData[];
    }

    return () => (
      <a-select
        default-value={innerValue}
        placeholder={t(props.placeholder || '')}
        allow-clear={props.allowClear}
        allow-search
        filter-option={false}
        onUpdate:model-value={(value: ModelType) => emit('update:modelValue', value)}
        onInputValueChange={debounce(handleUserSearch, 300)}
      >
        {{
          prefix: () => t(props.prefix || ''),
          default: () =>
            filterOptions.value.map((item) => (
              <a-option key={item.id} value={item.value}>
                {typeof props.optionLabelRender === 'function'
                  ? h('div', { innerHTML: props.optionLabelRender(item) })
                  : item.label}
              </a-option>
            )),
        }}
      </a-select>
    );
  },
  {
    // eslint-disable-next-line vue/require-prop-types
    props: ['modelValue', 'allowClear', 'placeholder', 'prefix', 'searchKeys', 'options', 'optionLabelRender'],
    emits: ['update:modelValue'],
  }
);
