import Pager from './page-item.vue';
import EllipsisPager from './page-item-ellipsis.vue';
import StepPager from './page-item-step.vue';
import PageJumper from './page-jumper.vue';
import PageOptions from './page-options.vue';

import { useI18n } from '@/hooks/useI18n';

import './style.less';
import type { PageItemType } from './interface';
import { Size } from './types';
import useSize from './useSize';
import { getPrefixCls, isNumber } from './utils';
import { SelectProps } from '@arco-design/web-vue/es/select/interface';
import type { CSSProperties, PropType } from 'vue';

export type Data = Record<string, any>;

export default defineComponent({
  name: 'MsPagination',
  props: {
    /**
     * @zh 数据总数
     * @en Total number of data
     */
    total: {
      type: Number,
      required: true,
    },
    /**
     * @zh 当前的页数
     * @en Current page number
     * @vModel
     */
    current: Number,
    /**
     * @zh 默认的页数（非受控状态）
     * @en The default number of pages (uncontrolled state)
     */
    defaultCurrent: {
      type: Number,
      default: 1,
    },
    /**
     * @zh 每页展示的数据条数
     * @en Number of data items displayed per page
     * @vModel
     */
    pageSize: Number,
    /**
     * @zh 默认每页展示的数据条数（非受控状态）
     * @en The number of data items displayed per page by default (uncontrolled state)
     */
    defaultPageSize: {
      type: Number,
      default: 10,
    },
    /**
     * @zh 是否禁用
     * @en Whether to disable
     */
    disabled: {
      type: Boolean,
      default: false,
    },
    /**
     * @zh 单页时是否隐藏分页
     * @en Whether to hide pagination when single page
     */
    hideOnSinglePage: {
      type: Boolean,
      default: false,
    },
    /**
     * @zh 是否为简单模式
     * @en Whether it is simple mode
     */
    simple: {
      type: Boolean,
      default: false,
    },
    /**
     * @zh 是否为简单模式且无页码按钮只有页码跳转
     * @en Whether it is simple mode
     */
    simpleOnlyJumper: {
      type: Boolean,
      default: false,
    },
    /**
     * @zh 是否显示数据总数
     * @en Whether to display the total number of data
     */
    showTotal: {
      type: Boolean,
      default: false,
    },
    /**
     * @zh 是否显示更多按钮
     * @en Whether to show more buttons
     */
    showMore: {
      type: Boolean,
      default: false,
    },
    /**
     * @zh 是否显示跳转
     * @en Whether to show jump
     */
    showJumper: {
      type: Boolean,
      default: false,
    },
    /**
     * @zh 是否显示数据条数选择器
     * @en Whether to display the data number selector
     */
    showPageSize: {
      type: Boolean,
      default: false,
    },
    /**
     * @zh 数据条数选择器的选项列表
     * @en Selection list of data number selector
     */
    pageSizeOptions: {
      type: Array as PropType<number[]>,
      default: () => [10, 20, 30, 40, 50],
    },
    /**
     * @zh 数据条数选择器的Props
     * @en Props of data number selector
     */
    pageSizeProps: {
      type: Object as PropType<SelectProps>,
    },
    /**
     * @zh 分页选择器的大小
     * @en The size of the page selector
     * @values 'mini', 'small', 'medium', 'large'
     * @defaultValue 'medium'
     */
    size: {
      type: String as PropType<Size>,
    },
    /**
     * @zh 分页按钮的样式
     * @en The style of the paging button
     */
    pageItemStyle: {
      type: Object as PropType<CSSProperties>,
    },
    /**
     * @zh 当前分页按钮的样式
     * @en The style of the current paging button
     */
    activePageItemStyle: {
      type: Object as PropType<CSSProperties>,
    },
    /**
     * @zh 计算显示省略的基础个数。显示省略的个数为 `baseSize + 2 * bufferSize`
     * @en Calculate and display the number of omitted bases. Display the omitted number as `baseSize + 2 * bufferSize`
     */
    baseSize: {
      type: Number,
      default: 6,
    },
    /**
     * @zh 显示省略号时，当前页码左右显示的页码个数
     * @en When the ellipsis is displayed, the number of page numbers displayed on the left and right of the current page number
     */
    bufferSize: {
      type: Number,
      default: 2,
    },
    /**
     * @zh 是否在改变数据条数时调整页码
     * @en Whether to adjust the page number when changing the number of data
     * @version 2.34.0
     */
    autoAdjust: {
      type: Boolean,
      default: true,
    },
  },
  emits: {
    /* eslint-disable @typescript-eslint/no-unused-vars */
    'update:current': (current: number) => true,
    'update:pageSize': (pageSize: number) => true,
    /**
     * @zh 页码改变时触发
     * @en Triggered when page number changes
     * @param {number} current
     */
    'change': (current: number) => true,
    /**
     * @zh 数据条数改变时触发
     * @en Triggered when the number of data items changes
     * @param {number} pageSize
     */
    'pageSizeChange': (pageSize: number) => true,
  },
  /**
   * @zh 分页按钮
   * @en Page item
   * @version 2.9.0
   * @slot page-item
   * @binding {number} page The page number of the paging button
   */
  /**
   * @zh 分页按钮（步）
   * @en Page item (step)
   * @version 2.9.0
   * @slot page-item-step
   * @binding {'previous'|'next'} type The type of page item step
   */
  /**
   * @zh 分页按钮（省略）
   * @en Page item (ellipsis)
   * @version 2.9.0
   * @slot page-item-ellipsis
   */
  /**
   * @zh 总数
   * @en Total
   * @version 2.9.0
   * @slot total
   * @binding {number} total
   */
  setup(props, { emit, slots }) {
    const prefixCls = getPrefixCls('pagination');
    const { t } = useI18n();
    const { disabled, pageItemStyle, activePageItemStyle, size } = toRefs(props);
    const { mergedSize } = useSize(size);

    const _current = ref(props.defaultCurrent);
    const _pageSize = ref(props.defaultPageSize);
    const computedCurrent = computed(() => props.current ?? _current.value);
    const computedPageSize = computed(() => props.pageSize ?? _pageSize.value);

    const pages = computed(() => Math.ceil(props.total / computedPageSize.value) || 1); // 页码最小是 1

    const handleClick = (page: number) => {
      // when pageJumper blur and input.value is undefined, page is illegal
      if (page !== computedCurrent.value && isNumber(page) && !props.disabled) {
        _current.value = page;
        emit('update:current', page);
        emit('change', page);
      }
    };

    const handlePageSizeChange = (pageSize: number) => {
      _pageSize.value = pageSize;
      emit('update:pageSize', pageSize);
      emit('pageSizeChange', pageSize);
    };

    const pagerProps = reactive({
      current: computedCurrent,
      pages,
      disabled,
      style: pageItemStyle,
      activeStyle: activePageItemStyle,
      onClick: handleClick,
    });

    const getPageItemElement = (type: PageItemType, prop: Data = {}) => {
      if (type === 'more') {
        return <EllipsisPager v-slots={{ default: slots['page-item-ellipsis'] }} {...prop} {...pagerProps} />;
      }
      if (type === 'previous') {
        return <StepPager v-slots={{ default: slots['page-item-step'] }} type="previous" {...prop} {...pagerProps} />;
      }
      if (type === 'next') {
        return <StepPager v-slots={{ default: slots['page-item-step'] }} type="next" {...prop} {...pagerProps} />;
      }

      return <Pager v-slots={{ default: slots['page-item'] }} {...prop} {...pagerProps} />;
    };

    const pageList = computed(() => {
      // @ts-ignore
      const pageListArr: Array<JSX.Element | JSX.Element[]> = [];

      if (pages.value < props.baseSize + props.bufferSize * 2) {
        for (let i = 1; i <= pages.value; i++) {
          pageListArr.push(getPageItemElement('page', { key: i, pageNumber: i }));
        }
      } else {
        let left = 1;
        let right = pages.value;
        let hasLeftEllipsis = false;
        let hasRightEllipsis = false;

        if (computedCurrent.value > 2 + props.bufferSize) {
          hasLeftEllipsis = true;
          left = Math.min(computedCurrent.value - props.bufferSize, pages.value - 2 * props.bufferSize);
        }
        if (computedCurrent.value < pages.value - (props.bufferSize + 1)) {
          hasRightEllipsis = true;
          right = Math.max(computedCurrent.value + props.bufferSize, 2 * props.bufferSize + 1);
        }

        if (hasLeftEllipsis) {
          pageListArr.push(getPageItemElement('page', { key: 1, pageNumber: 1 }));
          pageListArr.push(
            getPageItemElement('more', {
              key: 'left-ellipsis-pager',
              step: -(props.bufferSize * 2 + 1),
            })
          );
        }

        for (let i = left; i <= right; i++) {
          pageListArr.push(getPageItemElement('page', { key: i, pageNumber: i }));
        }

        if (hasRightEllipsis) {
          pageListArr.push(
            getPageItemElement('more', {
              key: 'right-ellipsis-pager',
              step: props.bufferSize * 2 + 1,
            })
          );
          pageListArr.push(
            getPageItemElement('page', {
              key: pages.value,
              pageNumber: pages.value,
            })
          );
        }
      }

      return pageListArr;
    });

    const renderPager = () => {
      return (
        <ul class={`${prefixCls}-list`}>
          {getPageItemElement('previous', { simple: true })}
          {!props.simpleOnlyJumper && pageList.value}
          {props.simpleOnlyJumper ? (
            <PageJumper
              v-slots={{
                'jumper-prepend': slots['jumper-prepend'],
                'jumper-append': slots['jumper-append'],
              }}
              simple
              disabled={props.disabled}
              current={computedCurrent.value}
              pages={pages.value}
              size={mergedSize.value}
              onChange={handleClick}
            />
          ) : (
            props.showMore &&
            !props.simple &&
            getPageItemElement('more', {
              key: 'more',
              step: props.bufferSize * 2 + 1,
            })
          )}
          {getPageItemElement('next', { simple: true })}
        </ul>
      );
    };

    // When the number of data items changes, recalculate the page number
    watch(computedPageSize, (curPageSize, prePageSize) => {
      if (props.autoAdjust && curPageSize !== prePageSize && computedCurrent.value > 1) {
        const index = prePageSize * (computedCurrent.value - 1) + 1;
        const newPage = Math.ceil(index / curPageSize);
        if (newPage !== computedCurrent.value) {
          _current.value = newPage;
          emit('update:current', newPage);
          emit('change', newPage);
        }
      }
    });

    const cls = computed(() => [
      prefixCls,
      `${prefixCls}-size-${mergedSize.value}`,
      {
        [`${prefixCls}-simple`]: props.simple,
        [`${prefixCls}-disabled`]: props.disabled,
      },
    ]);

    return () => {
      if (props.hideOnSinglePage && pages.value <= 1) {
        return null;
      }

      if (props.total === 0) return null;

      return (
        <div class={cls.value}>
          {props.showTotal && !props.simple && !props.simpleOnlyJumper && (
            <span class={`${prefixCls}-total`}>
              {slots.total?.({ total: props.total }) ?? t('msPagination.total', { total: props.total })}
            </span>
          )}
          {props.showPageSize && !props.simple && !props.simpleOnlyJumper && (
            <PageOptions
              disabled={props.disabled}
              sizeOptions={props.pageSizeOptions}
              pageSize={computedPageSize.value}
              size={mergedSize.value}
              onChange={(v: number) => handlePageSizeChange(v)}
              selectProps={props.pageSizeProps}
            />
          )}
          {renderPager()}
          {!props.simple && props.showJumper && !props.simpleOnlyJumper && (
            <PageJumper
              v-slots={{
                'jumper-prepend': slots['jumper-prepend'],
                'jumper-append': slots['jumper-append'],
              }}
              simple
              disabled={props.disabled}
              current={computedCurrent.value}
              pages={pages.value}
              size={mergedSize.value}
              onChange={handleClick}
            />
          )}
        </div>
      );
    };
  },
});
