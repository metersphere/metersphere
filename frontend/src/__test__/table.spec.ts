import { mount } from '@vue/test-utils';
import { describe, expect, test } from 'vitest';
import MsBaseTable from '@/components/ms-table/base-table.vue';
import { nextTick } from 'vue';
import { MsTableColumn } from '@/components/ms-table/type';
import useTable from '@/components/ms-table/useTable';
import { getTableList } from '@/api/modules/api-test/index';

const columns: MsTableColumn = [
  {
    title: 'ID',
    dataIndex: 'num',
  },
  {
    title: '接口名称',
    dataIndex: 'name',
  },
  {
    title: '请求类型',
    dataIndex: 'method',
  },
  {
    title: '责任人',
    dataIndex: 'username',
  },
  {
    title: '路径',
    dataIndex: 'path',
  },
  {
    title: '标签',
    dataIndex: 'tags',
  },
  {
    title: '更新时间',
    slotName: 'updataTime',
  },
  {
    title: '用例数',
    dataIndex: 'caseTotal',
  },
  {
    title: '用例状态',
    dataIndex: 'caseStatus',
  },
  {
    title: '用例通过率',
    dataIndex: 'casePassingRate',
  },
  {
    title: '接口状态',
    dataIndex: 'status',
  },
  {
    title: '创建时间',
    slotName: 'createTime',
  },
  {
    title: '描述',
    dataIndex: 'description',
  },
  {
    title: '操作',
    slotName: 'action',
    fixed: 'right',
    width: 200,
  },
];

describe('MS-Table', () => {
  test('init table with useTable', async () => {
    const { propsRes, propsEvent, loadList, setProps } = useTable(getTableList, {
      columns,
      pagination: { current: 1, pageSize: 1 },
    });

    const wrapper = mount(MsBaseTable, {
      vOn: propsEvent,
      vBind: propsRes,
    });
    loadList();

    await nextTick();
    let content = wrapper.find('.arco-table-td-content').element.innerHTML;
    expect(propsRes.value.data.length).toBe(2);
    expect(content).toBe('e7bd7179-d63a-43a5-1a65-218473ee69ca');

    setProps({ pagination: { current: 2, pageSize: 1 } });
    loadList();

    await nextTick();
    content = wrapper.find('.arco-table-td-content').element.innerHTML;
    expect(content).toBe('937be890-79bb-1b68-e03e-7d37a8b0a607');
  });
});
