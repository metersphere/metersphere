import { watch, ref, h, defineComponent, onBeforeMount, computed, Ref, Slot } from 'vue';
import { debounce } from 'lodash-es';
import { useI18n } from '@/hooks/useI18n';
import useSelect from '@/hooks/useSelect';

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
  allowSearch?: boolean;
  allowClear?: boolean;
  placeholder?: string;
  hasAllSelect?: boolean; // 是否有全选选项
  searchKeys?: string[]; // 需要搜索的 key 名，关键字会遍历这个 key 数组，然后取 item[key] 进行模糊匹配
  valueKey?: string; // 选项的 value 字段名，默认为 value
  options: SelectOptionData[];
  multiple?: boolean; // 是否多选
  remoteFieldsMap?: RemoteFieldsMap; // 远程模式下的结果 key 映射，例如 { value: 'id' }，表示远程请求时，会将返回结果的 id 赋值到 value 字段
  remoteExtraParams?: Record<string, any>; // 远程模式下的额外参数
  remoteFunc?(params: Record<string, any>): Promise<any>; // 远程模式下的请求函数，返回一个 Promise
  optionLabelRender?: (item: SelectOptionData) => string; // 自定义 option 的 label 渲染，返回一个 html 字符串，默认使用 item.label
  optionTooltipContent?: (item: SelectOptionData) => string; // 自定义 option 的 tooltip 内容，返回一个字符串，默认使用 item.label
}

export interface MsSearchSelectSlots {
  prefix?: string;
  header?: (() => JSX.Element) | Slot<any>;
  default?: () => JSX.Element[];
  footer?: Slot<any>;
}

export default defineComponent(
  (props: MsSearchSelectProps & MsSearchSelectSlots, { emit, slots }) => {
    const { t } = useI18n();

    const innerValue = ref(props.modelValue);
    const filterOptions = ref<SelectOptionData[]>([]); // 实际渲染的 options，会根据搜索关键字进行过滤
    const remoteOriginOptions = ref<SelectOptionData[]>([...props.options]); // 远程模式下的原始 options，接口返回的数据会存储在这里
    watch(
      () => props.modelValue,
      (val) => {
        innerValue.value = val;
      }
    );

    const selectRef = ref<Ref>();

    const { maxTagCount, getOptionComputedStyle, calculateMaxTag } = useSelect({
      selectRef,
      selectVal: innerValue,
      isCascade: true,
      options: props.options,
    });

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
          filterOptions.value = remoteOriginOptions.value.map((e) => ({
            ...e,
            tooltipContent: typeof props.optionTooltipContent === 'function' ? props.optionTooltipContent(e) : e.label,
          }));
          calculateMaxTag();
          return;
        }
        const highlightedKeyword = `<span class="text-[rgb(var(--primary-4))]">${val}</span>`;
        filterOptions.value = remoteOriginOptions.value
          .map((e) => {
            const item = { ...e };
            let hasMatch = false;
            if (props.searchKeys) {
              for (let i = 0; i < props.searchKeys.length; i++) {
                // 遍历传入的搜索字段
                const key = props.searchKeys[i];
                if (e[key].includes(val)) {
                  // 是否匹配
                  hasMatch = true;
                  item[key] = e[key].replace(new RegExp(val, 'gi'), highlightedKeyword); // 高亮关键字替换
                }
              }
            }
            if (hasMatch) {
              return item;
            }
            return null;
          })
          .filter((e) => e) as SelectOptionData[];
        calculateMaxTag();
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      } finally {
        loading.value = false;
      }
    }

    const optionItemLabelRender = (item: SelectOptionData) =>
      typeof props.optionLabelRender === 'function'
        ? h('div', { innerHTML: props.optionLabelRender(item) })
        : item.label;

    // 半选状态
    const indeterminate = computed(() => {
      if (props.multiple && Array.isArray(innerValue.value)) {
        return innerValue.value.length > 0 && innerValue.value.length < filterOptions.value.length;
      }
      return false;
    });

    const isSelectAll = computed({
      get: () => {
        if (props.multiple && Array.isArray(innerValue.value)) {
          return innerValue.value.length === filterOptions.value.length;
        }
        return false;
      },
      set: (val) => val,
    });
    function handleSelectAllChange(val: boolean) {
      isSelectAll.value = val;
      if (val) {
        innerValue.value = filterOptions.value.map((item) => item[props.valueKey || 'value']);
        emit('update:modelValue', innerValue.value);
      } else {
        innerValue.value = [];
        emit('update:modelValue', []);
      }
    }

    const selectSlots = () => {
      const _slots: MsSearchSelectSlots = {
        default: () =>
          filterOptions.value.map((item) => (
            <a-tooltip content={item.tooltipContent} mouse-enter-delay={500}>
              <a-option key={item.id} value={item[props.valueKey || 'value']}>
                <div class="one-line-text" style={getOptionComputedStyle.value}>
                  {optionItemLabelRender(item)}
                </div>
              </a-option>
            </a-tooltip>
          )),
      };
      if (props.hasAllSelect) {
        _slots.header = () => (
          <div class="arco-select-option mb-[4px] h-[30px] rounded-[var(--border-radius-small)] !p-[3px_8px]">
            <a-checkbox
              model-value={isSelectAll.value}
              indeterminate={indeterminate.value}
              class="w-full"
              onChange={handleSelectAllChange}
            >
              {t('common.allSelect')}
            </a-checkbox>
          </div>
        );
      }
      if (slots.header) {
        _slots.header = slots.header;
      }
      if (slots.footer) {
        _slots.footer = slots.footer;
      }

      return _slots;
    };

    onBeforeMount(() => {
      handleUserSearch('');
    });

    return () => (
      <a-select
        ref={selectRef}
        default-value={innerValue}
        placeholder={t(props.placeholder || '')}
        allow-clear={props.allowClear}
        allow-search={props.allowSearch}
        filter-option={false}
        loading={loading.value}
        multiple={props.multiple}
        max-tag-count={maxTagCount.value}
        onUpdate:model-value={(value: ModelType) => emit('update:modelValue', value)}
        onInputValueChange={debounce(handleUserSearch, 300)}
      >
        {{
          prefix: () => t(props.prefix || ''),
          ...selectSlots(),
        }}
      </a-select>
    );
  },
  {
    /* eslint-disable vue/require-prop-types */
    props: [
      'mode',
      'modelValue',
      'allowSearch',
      'allowClear',
      'placeholder',
      'searchKeys',
      'valueKey',
      'options',
      'optionLabelRender',
      'remoteFieldsMap',
      'remoteExtraParams',
      'remoteFunc',
      'optionTooltipContent',
      'prefix',
      'hasAllSelect',
      'multiple',
    ],
    emits: ['update:modelValue'],
  }
);
