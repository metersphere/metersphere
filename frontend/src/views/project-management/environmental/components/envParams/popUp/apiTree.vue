<template>
  <a-tree-select
    v-bind="props"
    ref="treeRef"
    v-model:expanded-keys="expandedKeys"
    v-model:selected-keys="innerSelectedKeys"
    :data="moduleTree"
    class="ms-tree"
    :allow-drop="handleAllowDrop"
    @drag-start="onDragStart"
    @drag-end="onDragEnd"
    @drop="onDrop"
    @select="select"
    @check="checked"
  >
    <template v-if="$slots['tree-slot-title']" #tree-slot-title="_props">
      <a-tooltip
        :content="_props[props.fieldNames.title]"
        :mouse-enter-delay="800"
        :position="props.titleTooltipPosition"
      >
        <slot name="tree-slot-title" v-bind="_props"></slot>
      </a-tooltip>
    </template>
    <template v-if="$slots['tree-slot-extra']" #tree-slot-extra="_props">
      <div
        v-if="_props.hideMoreAction !== true"
        :class="[
          'ms-tree-node-extra',
          innerFocusNodeKey === _props[props.fieldNames.key] ? 'ms-tree-node-extra--focus' : '',
        ]"
      >
        <div
          class="ml-[-4px] flex h-[32px] items-center rounded-[var(--border-radius-small)] bg-[rgb(var(--primary-1))]"
        >
          <slot name="tree-slot-extra" v-bind="_props"></slot>
          <MsTableMoreAction
            v-if="props.nodeMoreActions"
            :list="
              typeof props.filterMoreActionFunc === 'function'
                ? props.filterMoreActionFunc(props.nodeMoreActions, _props)
                : props.nodeMoreActions
            "
            trigger="click"
            @select="handleNodeMoreSelect($event, _props)"
            @close="moreActionsClose"
          >
            <MsButton
              type="text"
              size="mini"
              class="ms-tree-node-extra__more"
              @click="innerFocusNodeKey = _props[props.fieldNames.key]"
            >
              <MsIcon type="icon-icon_more_outlined" size="14" class="text-[var(--color-text-4)]" />
            </MsButton>
          </MsTableMoreAction>
        </div>
      </div>
    </template>
  </a-tree-select>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useVModel } from '@vueuse/core';
  import { debounce } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import type { MsTreeFieldNames, MsTreeNodeData, MsTreeSelectedData } from '@/components/business/ms-tree/types';

  import useContainerShadow from '@/hooks/useContainerShadow';
  import { mapTree } from '@/utils/index';

  import { VirtualListProps } from '@arco-design/web-vue/es/_components/virtual-list-v2/interface';

  const props = withDefaults(
    defineProps<{
      data: MsTreeNodeData[];
      keyword?: string; // 搜索关键字
      searchDebounce?: number; // 搜索防抖 ms 数
      draggable?: boolean; // 是否可拖拽
      blockNode?: boolean; // 是否块级节点
      showLine?: boolean; // 是否展示连接线
      defaultExpandAll?: boolean; // 是否默认展开所有节点
      selectable?: boolean | ((node: MsTreeNodeData, info: { level: number; isLeaf: boolean }) => boolean); // 是否可选中
      fieldNames?: MsTreeFieldNames; // 自定义字段名
      placeholder?: string;
      focusNodeKey?: string | number; // 聚焦的节点 key
      selectedKeys?: Array<string | number>; // 选中的节点 key
      nodeMoreActions?: ActionsItem[]; // 节点展示在省略号按钮内的更多操作
      expandAll?: boolean; // 是否展开/折叠所有节点，true 为全部展开，false 为全部折叠
      emptyText?: string; // 空数据时的文案
      checkable?: boolean; // 是否可选中
      checkedStrategy?: 'all' | 'parent' | 'child'; // 选中节点时的策略
      checkedKeys?: Array<string | number>; // 选中的节点 key
      virtualListProps?: VirtualListProps; // 虚拟滚动列表的属性
      titleTooltipPosition?:
        | 'top'
        | 'tl'
        | 'tr'
        | 'bottom'
        | 'bl'
        | 'br'
        | 'left'
        | 'lt'
        | 'lb'
        | 'right'
        | 'rt'
        | 'rb'; // 标题 tooltip 的位置
      allowDrop?: (dropNode: MsTreeNodeData, dropPosition: -1 | 0 | 1, dragNode?: MsTreeNodeData | null) => boolean; // 是否允许放置
      filterMoreActionFunc?: (items: ActionsItem[], node: MsTreeNodeData) => ActionsItem[]; // 过滤更多操作按钮
    }>(),
    {
      searchDebounce: 300,
      defaultExpandAll: false,
      selectable: true,
      draggable: false,
      titleTooltipPosition: 'top',
      fieldNames: () => ({
        key: 'key',
        title: 'title',
        children: 'children',
        isLeaf: 'isLeaf',
      }),
    }
  );

  const moduleTree = ref([
    {
      id: 'root',
      name: '未规划请求',
      type: 'MODULE',
      parentId: 'NONE',
      children: [
        {
          id: '4112912223068160',
          name: '随便写的',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'OPTIONS',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1150192243335168',
          name: '文件儿',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1165379247169536',
          name: '901',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'TCP',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1165705664684032',
          name: '888',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'TCP',
          },
          count: 0,
          path: '/',
        },
        {
          id: '2125544956010496',
          name: '0129-1',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'SPX',
          },
          count: 0,
          path: '/',
        },
        {
          id: '2126988065021952',
          name: '0129-2',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'SPX',
          },
          count: 0,
          path: '/',
        },
        {
          id: '2171827523477504',
          name: 'fffggg',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '2297223388766208',
          name: '0129-3',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'SPX',
          },
          count: 0,
          path: '/',
        },
        {
          id: '4034709458542592',
          name: '测试一下百度',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'PATCH',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1017890070175744',
          name: 'TTTTTCCCCCPPPPP',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'TCP',
          },
          count: 0,
          path: '/',
        },
        {
          id: '864679996792832',
          name: '委托',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'SPX',
          },
          count: 0,
          path: '/',
        },
        {
          id: '867463135600640',
          name: '登入',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'SPX',
          },
          count: 0,
          path: '/',
        },
        {
          id: '868236229713920',
          name: '买入',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'SPX',
          },
          count: 0,
          path: '/',
        },
        {
          id: '927008562290689',
          name: '账号校验1',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'SPX',
          },
          count: 0,
          path: '/',
        },
        {
          id: '943707395039232',
          name: 'ddd',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'TCP',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1068845561815040',
          name: 'TCP测试2',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'TCP',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1131706704060416',
          name: 'aaa',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '921184586637312',
          name: '读取系统日期22',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'SPX',
          },
          count: 0,
          path: '/',
        },
        {
          id: '642853526609920',
          name: 'Test',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '851760736174080',
          name: 'dd',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '853891039952896',
          name: 'fasdfd',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1118186147643392',
          name: 'eeee',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1120161832599552',
          name: 'eeeeqqq',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1162149432885248',
          name: 'a',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1177147458682880',
          name: '这是Curl导入的请求',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '2226957725106176',
          name: 'test12',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '2601169635672064',
          name: 'testvvvv',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '885467640184832',
          name: 'okko',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '891635213221888',
          name: 'gs',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '640018849742848',
          name: '0228',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'SPX',
          },
          count: 0,
          path: '/',
        },
        {
          id: '2178166898065408',
          name: '0304',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'SPX',
          },
          count: 0,
          path: '/',
        },
      ],
      attachInfo: {},
      count: 0,
      path: '/未规划请求',
    },
    {
      id: '1163609720455168',
      name: '12',
      type: 'MODULE',
      parentId: 'NONE',
      children: [
        {
          id: '1075769049718784',
          name: '1',
          type: 'MODULE',
          parentId: '1163609720455168',
          children: [
            {
              id: '1076353165271040',
              name: '12',
              type: 'API',
              parentId: '1075769049718784',
              children: [],
              attachInfo: {
                protocol: 'HTTP',
                method: 'GET',
              },
              count: 0,
              path: '/',
            },
            {
              id: '1066371661111296',
              name: 'ddd',
              type: 'API',
              parentId: '1075769049718784',
              children: [],
              attachInfo: {
                protocol: 'HTTP',
                method: 'GET',
              },
              count: 0,
              path: '/',
            },
          ],
          attachInfo: {},
          count: 0,
          path: '/12/1',
        },
        {
          id: '1053710097522688',
          name: 'as',
          type: 'API',
          parentId: '1163609720455168',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1066887057186816',
          name: '1',
          type: 'API',
          parentId: '1163609720455168',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
      ],
      attachInfo: {},
      count: 0,
      path: '/12',
    },
    {
      id: '934172567740416',
      name: 'test',
      type: 'MODULE',
      parentId: 'NONE',
      children: [
        {
          id: '1066543459803136',
          name: 'asggttgt',
          type: 'API',
          parentId: '934172567740416',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1051596973613056',
          name: 'tes',
          type: 'API',
          parentId: '934172567740416',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1067419633131520',
          name: 'g',
          type: 'API',
          parentId: '934172567740416',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
      ],
      attachInfo: {},
      count: 0,
      path: '/test',
    },
    {
      id: '950441903972352',
      name: '2',
      type: 'MODULE',
      parentId: 'NONE',
      children: [
        {
          id: '950373184495616',
          name: 'bbond',
          type: 'MODULE',
          parentId: '950441903972352',
          children: [],
          attachInfo: {},
          count: 0,
          path: '/2/bbond',
        },
        {
          id: '1006843414503424',
          name: '文件儿',
          type: 'API',
          parentId: '950441903972352',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'DELETE',
          },
          count: 0,
          path: '/',
        },
      ],
      attachInfo: {},
      count: 0,
      path: '/2',
    },
    {
      id: '950613702664192',
      name: 'TAPD🇭🇰api',
      type: 'MODULE',
      parentId: 'NONE',
      children: [
        {
          id: '887701023178752',
          name: '1获取tapd需求-带认证信息',
          type: 'API',
          parentId: '950613702664192',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '954616613715968',
          name: '有前后置的请求',
          type: 'API',
          parentId: '950613702664192',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
      ],
      attachInfo: {},
      count: 0,
      path: '/TAPD🇭🇰api',
    },
    {
      id: '1163729980284928',
      name: '4',
      type: 'MODULE',
      parentId: 'NONE',
      children: [
        {
          id: '1163850239369216',
          name: '儿',
          type: 'MODULE',
          parentId: '1163729980284928',
          children: [
            {
              id: '1133596489990144',
              name: '懂得都懂',
              type: 'API',
              parentId: '1163850239369216',
              children: [],
              attachInfo: {
                protocol: 'HTTP',
                method: 'GET',
              },
              count: 0,
              path: '/',
            },
            {
              id: '1136001671675904',
              name: 'aaaa',
              type: 'API',
              parentId: '1163850239369216',
              children: [],
              attachInfo: {
                protocol: 'HTTP',
                method: 'GET',
              },
              count: 0,
              path: '/',
            },
            {
              id: '1150364042313728',
              name: 'gfgfgf',
              type: 'API',
              parentId: '1163850239369216',
              children: [],
              attachInfo: {
                protocol: 'HTTP',
                method: 'GET',
              },
              count: 0,
              path: '/',
            },
            {
              id: '855334148964352',
              name: 'gggg',
              type: 'API',
              parentId: '1163850239369216',
              children: [],
              attachInfo: {
                protocol: 'HTTP',
                method: 'GET',
              },
              count: 0,
              path: '/',
            },
            {
              id: '856863157321728',
              name: 'ggg',
              type: 'API',
              parentId: '1163850239369216',
              children: [],
              attachInfo: {
                protocol: 'HTTP',
                method: 'GET',
              },
              count: 0,
              path: '/',
            },
            {
              id: '1169072920166400',
              name: 'bbgond',
              type: 'API',
              parentId: '1163850239369216',
              children: [],
              attachInfo: {
                protocol: 'HTTP',
                method: 'GET',
              },
              count: 0,
              path: '/',
            },
            {
              id: '2202699749834752',
              name: 'ddd',
              type: 'API',
              parentId: '1163850239369216',
              children: [],
              attachInfo: {
                protocol: 'HTTP',
                method: 'GET',
              },
              count: 0,
              path: '/',
            },
            {
              id: '884179149996032',
              name: 'fgfgfg',
              type: 'API',
              parentId: '1163850239369216',
              children: [],
              attachInfo: {
                protocol: 'HTTP',
                method: 'GET',
              },
              count: 0,
              path: '/',
            },
          ],
          attachInfo: {},
          count: 0,
          path: '/4/儿',
        },
        {
          id: '1125917088464896',
          name: '测试文件儿',
          type: 'API',
          parentId: '1163729980284928',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1231126607380480',
          name: 'bbb',
          type: 'API',
          parentId: '1163729980284928',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '852791528325120',
          name: 'aaaa',
          type: 'API',
          parentId: '1163729980284928',
          children: [],
          attachInfo: {
            protocol: 'TCP',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1116073025208320',
          name: '啊啊啊啊',
          type: 'API',
          parentId: '1163729980284928',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
      ],
      attachInfo: {},
      count: 0,
      path: '/4',
    },
    {
      id: '862876113371136',
      name: '551',
      type: 'MODULE',
      parentId: 'NONE',
      children: [],
      attachInfo: {},
      count: 0,
      path: '/551',
    },
    {
      id: '907612492742656',
      name: 'ggbond',
      type: 'MODULE',
      parentId: 'NONE',
      children: [],
      attachInfo: {},
      count: 0,
      path: '/ggbond',
    },
  ]);

  const emit = defineEmits<{
    (e: 'select', selectedKeys: Array<string | number>, node: MsTreeNodeData): void;
    (
      e: 'drop',
      tree: MsTreeNodeData[],
      dragNode: MsTreeNodeData, // 被拖拽的节点
      dropNode: MsTreeNodeData, // 放入的节点
      dropPosition: number // 放入的位置，-1 为放入节点前，1 为放入节点后，0 为放入节点内
    ): void;
    (e: 'moreActionSelect', item: ActionsItem, node: MsTreeNodeData): void;
    (e: 'update:focusNodeKey', val: string | number): void;
    (e: 'update:selectedKeys', val: Array<string | number>): void;
    (e: 'moreActionsClose'): void;
    (e: 'check', val: Array<string | number>): void;
  }>();

  const treeData = ref<MsTreeNodeData[]>([]);

  const expandedKeys = ref<(string | number)[]>([]);

  const innerFocusNodeKey = useVModel(props, 'focusNodeKey', emit); // 聚焦的节点，一般用于在操作扩展按钮时，高亮当前节点，保持扩展按钮持续显示

  /**
   * 处理树节点更多按钮事件
   * @param item
   */
  function handleNodeMoreSelect(item: ActionsItem, node: MsTreeNodeData) {
    emit('moreActionSelect', item, node);
  }

  function moreActionsClose() {
    emit('moreActionsClose');
  }

  function select(selectedKeys: Array<string | number>, data: MsTreeSelectedData) {
    emit('select', selectedKeys, data.selectedNodes[0]);
  }

  function checked(checkedKeys: Array<string | number>) {
    emit('check', checkedKeys);
  }
  const innerSelectedKeys = useVModel(props, 'selectedKeys', emit);

  const tempDragNode = ref<MsTreeNodeData | null>(null);
  function handleAllowDrop({ dropNode, dropPosition }: { dropNode: MsTreeNodeData; dropPosition: -1 | 0 | 1 }) {
    if (props.allowDrop) {
      return props.allowDrop(dropNode, dropPosition, tempDragNode.value);
    }
    return true;
  }

  function onDragStart(e: DragEvent, node: MsTreeNodeData) {
    tempDragNode.value = node;
  }

  function onDragEnd() {
    tempDragNode.value = null;
  }
  const originalTreeData = ref<MsTreeNodeData[]>([]);
  const { isInitListener, setContainer, initScrollListener } = useContainerShadow({
    overHeight: 32,
    containerClassName: 'ms-tree-container',
  });
  const treeRef: Ref = ref(null);
  function init(isFirstInit = false) {
    originalTreeData.value = mapTree<MsTreeNodeData>(props.data, (node: MsTreeNodeData) => {
      if (!props.showLine) {
        // 不展示连接线时才设置节点图标，因为展示连接线时非叶子节点会展示默认的折叠图标。它不会覆盖 switcherIcon，但是会被 switcherIcon 覆盖
        node.icon = () => h('span', { class: 'hidden' });
      }
      if (
        node[props.fieldNames.isLeaf || 'isLeaf'] ||
        !node[props.fieldNames.children] ||
        node[props.fieldNames.children]?.length === 0
      ) {
        // 设置子节点图标，会覆盖 icon。当展示连接线时，需要设置 switcherIcon 以覆盖组件的默认图标；不展示连接线则是 icon
        node[props.showLine ? 'switcherIcon' : 'icon'] = () => h('span', { class: 'hidden' });
      }
      return node;
    });
    nextTick(() => {
      if (isFirstInit) {
        if (props.defaultExpandAll) {
          treeRef.value?.expandAll(true);
        }
        if (!isInitListener.value && treeRef.value) {
          setContainer(
            props.virtualListProps?.height
              ? (treeRef.value.$el.querySelector('.arco-virtual-list') as HTMLElement)
              : treeRef.value.$el
          );
          initScrollListener();
        }
      }
    });
  }
  function loop(
    _data: MsTreeNodeData[],
    key: string | number | undefined,
    callback: (item: MsTreeNodeData, index: number, arr: MsTreeNodeData[]) => void
  ) {
    _data.some((item, index, arr) => {
      if (item[props.fieldNames.key] === key) {
        callback(item, index, arr);
        return true;
      }
      if (item[props.fieldNames.children]) {
        return loop(item[props.fieldNames.children], key, callback);
      }
      return false;
    });
  }

  /**
   * 处理拖拽结束
   */
  function onDrop({
    dragNode,
    dropNode,
    dropPosition,
  }: {
    dragNode: MsTreeNodeData; // 被拖拽的节点
    dropNode: MsTreeNodeData; // 放入的节点
    dropPosition: number; // 放入的位置，-1 为放入节点前，1 为放入节点后，0 为放入节点内
  }) {
    loop(originalTreeData.value, dragNode.key, (item, index, arr) => {
      arr.splice(index, 1);
    });

    if (dropPosition === 0) {
      // 放入节点内
      loop(originalTreeData.value, dropNode.key, (item) => {
        item.children = item.children || [];
        item.children.push(dragNode);
      });
      dropNode.isLeaf = false;
      if (props.showLine) {
        delete dropNode.switcherIcon; // 放入的节点的 children 放入了被拖拽的节点，需要删除 switcherIcon 以展示默认的折叠图标
      }
    } else {
      // 放入节点前或后
      loop(originalTreeData.value, dropNode.key, (item, index, arr) => {
        arr.splice(dropPosition < 0 ? index : index + 1, 0, dragNode);
      });
    }
    emit('drop', originalTreeData.value, dragNode, dropNode, dropPosition);
  }
  onBeforeMount(() => {
    init(true);
  });

  watch(
    () => props.data,
    () => {
      init();
    }
  );
  /**
   * 根据关键字过滤树节点
   * @param keyword 搜索关键字
   */
  function searchData(keyword: string) {
    const search = (data: MsTreeNodeData[]) => {
      const result: MsTreeNodeData[] = [];
      data.forEach((item) => {
        if (item[props.fieldNames.title].toLowerCase().indexOf(keyword.toLowerCase()) > -1) {
          result.push({ ...item });
        } else if (item[props.fieldNames.children]) {
          const filterData = search(item[props.fieldNames.children]);
          if (filterData.length) {
            result.push({
              ...item,
              [props.fieldNames.children]: filterData,
            });
          }
        }
      });
      expandedKeys.value = result.map((item) => item[props.fieldNames.key]); // 搜索时，匹配的节点需要自动展开
      return result;
    };

    return search(originalTreeData.value);
  }

  // 防抖搜索
  const updateDebouncedSearch = debounce(() => {
    if (props.keyword) {
      treeData.value = searchData(props.keyword);
    }
  }, props.searchDebounce);
  watchEffect(() => {
    if (!props.keyword) {
      treeData.value = originalTreeData.value;
    } else {
      updateDebouncedSearch();
    }
  });
</script>

<style scoped lang="less">
  .ms-tree-container {
    .ms-container--shadow-y();
    @apply h-full;
    .ms-tree {
      .ms-scroll-bar();
      @apply h-full;
      .arco-tree-node {
        border-radius: var(--border-radius-small);
        &:hover {
          background-color: rgb(var(--primary-1));
          .arco-tree-node-title {
            background-color: rgb(var(--primary-1));
            &:not([draggable='false']) {
              .arco-tree-node-title-text {
                width: calc(100% - 22px);
              }
            }
          }
        }
        .arco-tree-node-indent-block {
          width: 1px;
        }
        .arco-tree-node-minus-icon,
        .arco-tree-node-plus-icon {
          border: 1px solid var(--color-text-4);
          border-radius: var(--border-radius-mini);
          background-color: var(--color-text-fff);
          &::after,
          &::before {
            background-color: var(--color-text-4);
          }
        }
        .arco-tree-node-switcher {
          .arco-tree-node-switcher-icon {
            @apply flex;

            color: var(--color-text-4);
          }
        }
        .arco-tree-node-title-highlight {
          background-color: transparent;
        }
        .arco-tree-node-title {
          &:hover {
            background-color: rgb(var(--primary-1));
            + .ms-tree-node-extra {
              @apply visible w-auto;
            }
          }
          .arco-tree-node-title-text {
            width: calc(100% - 8px);
          }
          .arco-tree-node-drag-icon {
            @apply cursor-move;

            right: 6px;
            .arco-icon {
              font-size: 14px;
            }
          }
        }
        .arco-tree-node-title-block {
          width: 60%;
        }
        .ms-tree-node-extra {
          @apply invisible relative w-0;
          &:hover {
            @apply visible w-auto;
          }
          .ms-tree-node-extra__btn,
          .ms-tree-node-extra__more {
            padding: 4px;
            border-radius: var(--border-radius-mini);
            &:hover {
              background-color: rgb(var(--primary-9));
              .arco-icon {
                color: rgb(var(--primary-5));
              }
            }
          }
          .ms-tree-node-extra__more {
            margin-right: 4px;
          }
        }
        .ms-tree-node-extra--focus {
          @apply visible w-auto;
        }
        .arco-tree-node-custom-icon {
          @apply hidden;
        }
      }
      .arco-tree-node-selected {
        background-color: rgb(var(--primary-1));
        .arco-tree-node-minus-icon,
        .arco-tree-node-plus-icon {
          border: 1px solid rgb(var(--primary-5));
          border-radius: var(--border-radius-mini);
          background-color: var(--color-text-fff);
          &::after,
          &::before {
            background-color: rgb(var(--primary-5));
          }
        }
        .arco-tree-node-switcher-icon .arco-icon,
        .arco-tree-node-title {
          font-weight: 500 !important;
          color: rgb(var(--primary-5));
          * {
            color: rgb(var(--primary-5));
          }
        }
      }
      .arco-tree-node-disabled {
        &:hover {
          background-color: transparent;
        }
        * {
          color: var(--color-text-4) !important;
        }
        .arco-tree-node-title {
          &:hover {
            background-color: transparent;
          }
        }
      }
      .arco-tree-node-disabled-selectable {
        @apply cursor-default;
        .arco-tree-node-title {
          @apply cursor-default;
        }
      }
    }
  }
</style>
