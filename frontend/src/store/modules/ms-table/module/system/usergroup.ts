import { MsTableColumn } from '@/components/pure/ms-table/type';

const userGroupUsercolumns: MsTableColumn = [
  {
    title: 'system.userGroup.name',
    dataIndex: 'name',
    showDrag: false,
    priority: 1,
    showInTable: true,
  },
  {
    title: 'system.userGroup.email',
    dataIndex: 'email',
    showDrag: false,
    priority: 1,
    showInTable: true,
  },
  {
    title: 'system.userGroup.phone',
    dataIndex: 'email',
    showDrag: true,
    priority: 1,
    showInTable: true,
  },
  {
    title: 'system.userGroup.operation',
    slotName: 'action',
    fixed: 'right',
    width: 200,
    showDrag: true,
    priority: 1,
    showInTable: true,
    showSetting: true,
  },
];
export default userGroupUsercolumns;
