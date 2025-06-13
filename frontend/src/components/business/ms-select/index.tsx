import { computed, defineComponent, h, onBeforeMount, onMounted, ref, Slot, watch, watchEffect } from 'vue';

import { useI18n } from '@/hooks/useI18n';
import useSelect from '@/hooks/useSelect';

import './index.less';
import type { SelectOptionData, TriggerProps } from '@arco-design/web-vue';

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
  defaultAllSelect?: boolean; // 是否默认全选
  searchKeys?: string[]; // 需要搜索的 key 名，关键字会遍历这个 key 数组，然后取 item[key] 进行模糊匹配
  valueKey?: string; // 选项的 value 字段名，默认为 value
  labelKey?: string; // 选项的 label 字段名，默认为 label
  options: SelectOptionData[];
  objectValue?: boolean; // 是否使用选项对象作为 value
  multiple?: boolean; // 是否多选
  atLeastOne?: boolean; // 是否至少选择一个，多选模式下有效
  remoteFieldsMap?: RemoteFieldsMap; // 远程模式下的结果 key 映射，例如 { value: 'id' }，表示远程请求时，会将返回结果的 id 赋值到 value 字段
  remoteExtraParams?: Record<string, any>; // 远程模式下的额外参数
  notAutoInitSearch?: boolean; // 是否禁用 arco-select 的初始化自动搜索功能
  popupContainer?: HTMLElement | string; // 弹出层容器
  triggerProps?: TriggerProps; // 触发器属性
  loading?: boolean; // 加载状态
  fallbackOption?: boolean | ((value: string | number | boolean | Record<string, unknown>) => SelectOptionData); // 自定义值中不存在的选项
  optionNotExitsText?: string; // 选项不存在时的提示文案
  shouldCalculateMaxTag?: boolean; // 是否需要计算最大展示选项数量
  disabled?: boolean; // 是否禁用
  size?: 'mini' | 'small' | 'medium' | 'large'; // 尺寸
  remoteFunc?(params: Record<string, any>): Promise<any>; // 远程模式下的请求函数，返回一个 Promise
  optionLabelRender?: (item: SelectOptionData) => string; // 自定义 option 的 label 渲染，返回一个 html 字符串，默认使用 item.label
  optionTooltipContent?: (item: SelectOptionData) => string; // 自定义 option 的 tooltip 内容，返回一个字符串，默认使用 item.label
  remoteFilterFunc?: (options: SelectOptionData[]) => SelectOptionData[]; // 自定义过滤函数，会在远程请求返回数据后执行
  optionTooltipPosition?: 'top' | 'tl' | 'tr' | 'bottom' | 'bl' | 'br' | 'left' | 'lt' | 'lb' | 'right' | 'rt' | 'rb'; // // label tooltip 的位置
  fullTooltipPosition?: 'top' | 'tl' | 'tr' | 'bottom' | 'bl' | 'br' | 'left' | 'lt' | 'lb' | 'right' | 'rt' | 'rb'; // // 全选 tooltip 的位置
}
export interface RadioProps {
  options: SelectOptionData[];
  valueKey?: string; // 选项的 value 字段名，默认为 value
  labelKey?: string; // 选项的 label 字段名，默认为 label
}

export interface MsSearchSelectSlots {
  prefix?: string | Slot<any>;
  // @ts-ignore
  header?: (() => JSX.Element) | Slot<any>;
  // @ts-ignore
  default?: () => JSX.Element[];
  footer?: Slot<any>;
  empty?: Slot<any>;
}

export default defineComponent(
  (props: MsSearchSelectProps & MsSearchSelectSlots, { emit, slots }) => {
    const { t } = useI18n();

    const innerValue = ref(props.modelValue);
    const inputValue = ref('');
    const tempInputValue = ref('');
    const filterOptions = ref<SelectOptionData[]>([...props.options]); // 实际渲染的 options，会根据搜索关键字进行过滤
    const remoteOriginOptions = ref<SelectOptionData[]>([...props.options]); // 远程模式下的原始 options，接口返回的数据会存储在这里；静态模式下，默认为 options

    const selectRef = ref();
    const { maxTagCount, getOptionComputedStyle, singleTagMaxWidth, calculateMaxTag } = useSelect({
      selectRef,
      selectVal: innerValue,
      isCascade: false,
      valueKey: props.valueKey,
      labelKey: props.labelKey,
    });

    watch(
      () => props.modelValue,
      (val) => {
        innerValue.value = val;
        if (props.shouldCalculateMaxTag !== false && props.multiple) {
          calculateMaxTag();
        }
      },
      {
        immediate: true,
      }
    );

    const isArcoFirstSearch = ref(true); // 是否是第一次搜索，arco-select 会自动触发一次搜索，这里需要过滤掉
    const loading = ref(props.loading || false);

    watch(
      () => props.loading,
      (val) => {
        loading.value = !!val;
      }
    );

    watch(
      () => loading.value,
      (val) => {
        emit('update:loading', val);
      }
    );

    /**
     * 搜索
     * @param val 搜索值
     * @param manual 是否手动触发
     */
    async function handleSearch(val: string, manual = false) {
      if (isArcoFirstSearch.value && !manual) {
        isArcoFirstSearch.value = false;
        return;
      }
      isArcoFirstSearch.value = false;
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
                typeof props.optionTooltipContent === 'function'
                  ? props.optionTooltipContent(e)
                  : e[props.labelKey || 'label'];

              return item;
            }
          );
          if (props.remoteFilterFunc && typeof props.remoteFilterFunc === 'function') {
            remoteOriginOptions.value = props.remoteFilterFunc(remoteOriginOptions.value);
          }
          emit('remoteSearch', remoteOriginOptions.value);
        }
        if (val.trim() === '') {
          // 如果搜索关键字为空，则直接返回所有数据
          filterOptions.value = remoteOriginOptions.value.map((e) => ({
            ...e,
            tooltipContent:
              typeof props.optionTooltipContent === 'function'
                ? props.optionTooltipContent(e)
                : e[props.labelKey || 'label'],
          }));
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
                if (e[key]?.toLowerCase().includes(val.toLowerCase())) {
                  // 是否匹配
                  hasMatch = true;
                  item[key] = e[key].replace(
                    new RegExp(val.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'gi'), // 转义搜索关键字中的所有特殊字符
                    highlightedKeyword
                  ); // 高亮关键字替换
                }
              }
            }
            if (hasMatch) {
              return {
                ...item,
                tooltipContent:
                  typeof props.optionTooltipContent === 'function'
                    ? props.optionTooltipContent(e)
                    : e[props.labelKey || 'label'],
              };
            }
            return null;
          })
          .filter((e) => e) as SelectOptionData[];
        if (props.shouldCalculateMaxTag !== false && props.multiple) {
          calculateMaxTag();
        }
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      } finally {
        loading.value = false;
      }
    }

    const optionItemLabelRender = (item: SelectOptionData) => {
      return h('div', {
        innerHTML:
          typeof props.optionLabelRender === 'function'
            ? props.optionLabelRender(item)
            : item[props.labelKey || 'label'],
        class: 'one-line-text',
      });
    };

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
        innerValue.value = props.objectValue
          ? [...filterOptions.value]
          : filterOptions.value.map((e) => e[props.valueKey || 'value']);
        emit('update:modelValue', innerValue.value);
        emit('change', innerValue.value);
      } else {
        innerValue.value = [];
        emit('update:modelValue', []);
        emit('change', []);
      }
    }

    watch(
      () => props.options,
      (val) => {
        if (props.mode !== 'remote') {
          // 静态模式下，options 变化时，需要同步更新
          remoteOriginOptions.value = [...val];
          handleSearch('', true);
          if (props.defaultAllSelect) {
            handleSelectAllChange(true);
          }
        }
        if (props.shouldCalculateMaxTag !== false && props.multiple) {
          calculateMaxTag();
        }
      }
    );

    function getOptionItemDisabled(item: SelectOptionData) {
      return (
        !!item.disabled ||
        (!!props.multiple &&
          !!props.atLeastOne &&
          Array.isArray(innerValue.value) &&
          !!innerValue.value.find((e) =>
            props.objectValue
              ? e[props.valueKey || 'value'] === item[props.valueKey || 'value']
              : e === item[props.valueKey || 'value']
          ) &&
          innerValue.value.length === 1)
      );
    }

    const selectSlots = () => {
      const _slots: MsSearchSelectSlots = {
        default: () =>
          filterOptions.value.map((item) => (
            <a-tooltip
              content={item.tooltipContent}
              mouse-enter-delay={500}
              position={props.optionTooltipPosition || 'bl'}
              arrow-class={props.optionTooltipPosition === 'tr' ? 'absolute right-[4px] !left-auto' : ''}
            >
              <a-option
                key={item[props.valueKey || 'value']}
                value={props.objectValue ? item : item[props.valueKey || 'value']}
                tag-props={
                  props.multiple && props.atLeastOne
                    ? { closable: Array.isArray(innerValue.value) && innerValue.value.length > 1 }
                    : {}
                }
                disabled={getOptionItemDisabled(item)}
              >
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
      if (slots.prefix) {
        _slots.prefix = slots.prefix;
      }
      if (slots.footer) {
        _slots.footer = slots.footer;
      }
      if (slots.empty) {
        _slots.empty = slots.empty;
      }

      return _slots;
    };

    onBeforeMount(() => {
      if (props.mode === 'remote' && props.notAutoInitSearch === true) {
        // 远程模式下，且无需自动搜索，则初始化一次 options
        filterOptions.value = remoteOriginOptions.value.map((e) => ({
          ...e,
          tooltipContent:
            typeof props.optionTooltipContent === 'function'
              ? props.optionTooltipContent(e)
              : e[props.labelKey || 'label'],
        }));
      } else {
        handleSearch('', true);
      }
    });

    onMounted(() => {
      if (props.defaultAllSelect) {
        handleSelectAllChange(true);
      }
    });

    // 检测全选状态变化，全选时需要覆盖选择器的输入框内容，展示文本 ‘全部’；非全选时则移除文本 ‘全部’
    watchEffect(() => {
      const innerDom = selectRef.value?.$el.nextElementSibling.querySelector('.arco-select-view-inner') as HTMLElement;
      if (innerDom && props.hasAllSelect) {
        if (isSelectAll.value) {
          const allSelectDom = document.createElement('div');
          allSelectDom.className = 'ms-select-inner-all';
          allSelectDom.innerText = t('common.all');
          innerDom.appendChild(allSelectDom);
        } else {
          const allSelectDom = innerDom.querySelector('.ms-select-inner-all');
          if (allSelectDom) {
            innerDom.removeChild(allSelectDom);
          }
        }
      }
    });

    /**
     * 处理选择器值变化
     * @param value
     */
    function handleChange(value: ModelType) {
      if (props.multiple) {
        nextTick(() => {
          inputValue.value = tempInputValue.value;
        });
      }
      emit('update:modelValue', value);
      emit('change', value);

      emit(
        'changeObject',
        remoteOriginOptions.value.find((e) => value === e[props.valueKey || 'value'])
      );
    }

    /**
     * 处理输入框搜索值变化
     * @param val 搜索值
     */
    function handleInputValueChange(val: string) {
      inputValue.value = val;
      if (val !== '') {
        // 只存储有值的搜索值，因为当搜索完选中一个选项后，arco-select 会自动清空输入框，这里需要过滤掉
        tempInputValue.value = val;
      }
    }

    const selectFullTooltip = computed(() => {
      const values = Array.isArray(innerValue.value) ? innerValue.value : [innerValue.value];
      let tooltip = '';
      if (props.objectValue) {
        // 对象模式下，直接取 tooltipContent 或 label(若搜索的情况下会携带高亮代码，所以优先取tooltipContent)
        tooltip = values.map((e) => e.tooltipContent || e[props.labelKey || 'label']).join('，');
      } else {
        // 非对象模式下，需要根据 valueKey 取 label
        for (let i = 0; i < values.length; i++) {
          for (let j = 0; j < remoteOriginOptions.value.length; j++) {
            const optItem = remoteOriginOptions.value[j];
            if (optItem[props.valueKey || 'value'] === values[i]) {
              tooltip += optItem[props.labelKey || 'label'];
              if (i !== values.length - 1) {
                tooltip += '，';
              }
              break;
            }
          }
        }
      }
      return tooltip;
    });

    const popupVisible = ref(false);
    const disabledTooltip = computed(() => {
      if (maxTagCount.value === 0 || maxTagCount.value === Infinity) {
        // 不限制标签数量时不会出现 +N 隐藏已选项，展示单个标签的 tooltip 即可
        return true;
      }
      if (popupVisible.value) {
        // 弹出层展开时不展示 tooltip
        return true;
      }
      if (typeof innerValue.value === 'string' && innerValue.value.trim() === '') {
        return true;
      }
      if (Array.isArray(innerValue.value) && innerValue.value.length === 0) {
        return true;
      }
      return false;
    });

    const allowClear = computed(() => {
      if (props.atLeastOne && Array.isArray(innerValue.value)) {
        return innerValue.value.length > 1 && props.allowClear;
      }
      return props.allowClear;
    });

    function checkOptionExit(value?: string) {
      if (typeof props.optionLabelRender === 'function') {
        return value;
      }
      const option = remoteOriginOptions.value.find(
        (e) =>
          String(e[props.valueKey || 'value']).toLowerCase() === String(value).toLowerCase() ||
          String(e[props.labelKey || 'label']).toLowerCase() === String(value)?.toLowerCase()
      );
      return option ? option[props.labelKey || 'label'] : props.optionNotExitsText || t('ms.select.optionsNotExits');
    }

    function fallbackNotExitOption(value: string | number | boolean | Record<string, any>) {
      return {
        label: checkOptionExit(typeof value === 'object' ? value[props.labelKey || 'label'] : value),
        value,
        disabled: true,
      };
    }

    const renderPrefix = () => {
      if (slots.prefix) {
        return slots.prefix;
      }
      if (props.prefix) {
        return typeof props.prefix === 'string' ? () => t(props.prefix as string) : props.prefix;
      }
      return null;
    };

    return () => (
      <div class="w-full">
        <a-tooltip
          content={selectFullTooltip.value}
          class={disabledTooltip.value ? 'opacity-0' : ''}
          position={props.fullTooltipPosition || 'top'}
          mouse-enter-delay={300}
          mini
        >
          <a-select
            ref={selectRef}
            class="ms-select"
            model-value={innerValue.value}
            input-value={inputValue.value}
            popup-visible={popupVisible.value}
            placeholder={t(props.placeholder || '')}
            allow-clear={allowClear.value}
            allow-search={props.allowSearch}
            filter-option={false}
            loading={loading.value}
            multiple={props.multiple}
            max-tag-count={maxTagCount.value}
            search-delay={300}
            value-key={props.valueKey || 'value'}
            popup-container={props.popupContainer || document.body}
            trigger-props={props.triggerProps}
            fallback-option={props.fallbackOption || fallbackNotExitOption}
            disabled={props.disabled}
            size={props.size}
            onChange={handleChange}
            onBlur={emit('blur')}
            onSearch={handleSearch}
            onPopupVisibleChange={(val: boolean) => {
              popupVisible.value = val;
              if (val) {
                handleSearch('', true);
              } else {
                inputValue.value = '';
                tempInputValue.value = '';
              }
              emit('popupVisibleChange', val);
            }}
            onRemove={(val: string | number | boolean | Record<string, any> | undefined) => emit('remove', val)}
            onKeyup={(e: KeyboardEvent) => {
              // 阻止组件在回车时自动触发的事件
              if (e.code === 'Enter') {
                e.preventDefault();
                e.stopPropagation();
                handleSearch('', true);
              }
              if (e.code === 'Backspace' && inputValue.value === '') {
                tempInputValue.value = '';
              }
            }}
            onInputValueChange={handleInputValueChange}
            onClear={() => {
              innerValue.value = props.multiple ? [] : '';
              emit('update:modelValue', innerValue.value);
            }}
          >
            {{
              prefix: renderPrefix(),
              label: ({ data }: { data: SelectOptionData }) => (
                // 在不限制标签数量时展示 tooltip
                <a-tooltip
                  content={data.label}
                  position={props.fullTooltipPosition}
                  disabled={maxTagCount.value !== Infinity && maxTagCount.value !== 0}
                >
                  <div
                    class="one-line-text"
                    title=""
                    style={singleTagMaxWidth.value > 0 ? { maxWidth: `${singleTagMaxWidth.value}px` } : {}}
                  >
                    {slots.label ? slots.label(data) : checkOptionExit(data.label)}
                  </div>
                </a-tooltip>
              ),
              ...selectSlots(),
            }}
          </a-select>
        </a-tooltip>
      </div>
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
      'defaultAllSelect',
      'multiple',
      'notAutoInitSearch',
      'popupContainer',
      'triggerProps',
      'loading',
      'fallbackOption',
      'labelKey',
      'atLeastOne',
      'objectValue',
      'remoteFilterFunc',
      'optionNotExitsText',
      'shouldCalculateMaxTag',
      'disabled',
      'size',
      'optionTooltipPosition',
      'fullTooltipPosition',
    ],
    emits: [
      'update:modelValue',
      'remoteSearch',
      'popupVisibleChange',
      'update:loading',
      'remove',
      'change',
      'changeObject',
      'blur',
    ],
  }
);
