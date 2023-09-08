import { watch, ref, h, defineComponent, onBeforeMount } from 'vue';
import { debounce } from 'lodash-es';
import { useI18n } from '@/hooks/useI18n';

import type { SelectOptionData } from '@arco-design/web-vue';

export type ModelType = string | number | Record<string, any> | (string | number | Record<string, any>)[];
export type RemoteFieldsMap = {
  id: string;
  value: string;
  label: string;
  [key: string]: string;
};

export interface MsSearchSelectProps {
  mode?: 'static' | 'remote'; // 静态模式，远程模式。默认为静态模式，需要传入 options 数据；远程模式需要传入请求函数
  modelValue: ModelType;
  allowClear?: boolean;
  placeholder?: string;
  prefix?: string;
  searchKeys: string[]; // 需要搜索的 key 名，关键字会遍历这个 key 数组，然后取 item[key] 进行模糊匹配
  options: SelectOptionData[];
  remoteFieldsMap?: RemoteFieldsMap; // 远程模式下的结果 key 映射，例如 { value: 'id' }，表示远程请求时，会将返回结果的 id 赋值到 value 字段
  remoteExtraParams?: Record<string, any>; // 远程模式下的额外参数
  remoteFunc?(params: Record<string, any>): Promise<any>; // 远程模式下的请求函数，返回一个 Promise
  optionLabelRender?: (item: SelectOptionData) => string; // 自定义 option 的 label 渲染，返回一个 html 字符串，默认使用 item.label
  optionTooltipContent?: (item: SelectOptionData) => string; // 自定义 option 的 tooltip 内容，返回一个字符串，默认使用 item.label
}

export default defineComponent(
  (props: MsSearchSelectProps, { emit }) => {
    const { t } = useI18n();

    const innerValue = ref(props.modelValue);
    const filterOptions = ref<SelectOptionData[]>([]);
    const remoteOriginOptions = ref<SelectOptionData[]>([...props.options]);
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

    const loading = ref(false);

    async function handleUserSearch(val: string) {
      try {
        loading.value = true;
        // 如果是远程模式，则请求接口数据
        if (props.mode === 'remote' && typeof props.remoteFunc === 'function') {
          remoteOriginOptions.value = (await props.remoteFunc({ ...props.remoteExtraParams, keyword: val })).map(
            (e: any) => {
              const item = {
                ...e,
              };
              // 支持接口字段自定义映射
              if (props.remoteFieldsMap) {
                const map = props.remoteFieldsMap;
                Object.keys(map).forEach((key) => {
                  item[key] = e[map[key]];
                });
              }
              // 为了避免关键词搜索影响 label 值，这里需要开辟新字段存储 tooltip 内容
              item.tooltipContent =
                typeof props.optionTooltipContent === 'function' ? props.optionTooltipContent(e) : e.label;
              return item;
            }
          );
        }
        if (val.trim() === '') {
          // 如果搜索关键字为空，则直接返回所有数据
          filterOptions.value = [...remoteOriginOptions.value];
          return;
        }
        const highlightedKeyword = `<span class="text-[rgb(var(--primary-4))]">${val}</span>`;
        filterOptions.value = remoteOriginOptions.value
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
      } catch (error) {
        console.log(error);
      } finally {
        loading.value = false;
      }
    }

    const optionItemLabelRender = (item: SelectOptionData) =>
      typeof props.optionLabelRender === 'function'
        ? h('div', { innerHTML: props.optionLabelRender(item) })
        : item.label;

    onBeforeMount(() => {
      handleUserSearch('');
    });

    return () => (
      <a-select
        default-value={innerValue}
        placeholder={t(props.placeholder || '')}
        allow-clear={props.allowClear}
        allow-search
        filter-option={false}
        loading={loading.value}
        onUpdate:model-value={(value: ModelType) => emit('update:modelValue', value)}
        onInputValueChange={debounce(handleUserSearch, 300)}
      >
        {{
          prefix: () => t(props.prefix || ''),
          default: () =>
            filterOptions.value.map((item) => (
              <a-tooltip content={item.tooltipContent} mouse-enter-delay={500}>
                <a-option key={item.id} value={item.value}>
                  {optionItemLabelRender(item)}
                </a-option>
              </a-tooltip>
            )),
        }}
      </a-select>
    );
  },
  {
    /* eslint-disable vue/require-prop-types */
    props: [
      'mode',
      'modelValue',
      'allowClear',
      'placeholder',
      'prefix',
      'searchKeys',
      'options',
      'optionLabelRender',
      'remoteFieldsMap',
      'remoteExtraParams',
      'remoteFunc',
      'optionTooltipContent',
    ],
    emits: ['update:modelValue'],
  }
);
