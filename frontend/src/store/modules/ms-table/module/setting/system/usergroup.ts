import { MsTableColumn } from '@/components/pure/ms-table/type';

const userGroupUsercolumns: MsTableColumn = [
  {
    title: 'system.userGroup.name',
    dataIndex: 'name',
    showDrag: false,
    showInTable: true,
  },
  {
    title: 'system.userGroup.email',
    dataIndex: 'email',
    showDrag: false,
    showInTable: true,
  },
  {
    title: 'system.userGroup.phone',
    dataIndex: 'email',
    showDrag: true,
    showInTable: true,
  },
  {
    title: 'system.userGroup.operation',
    slotName: 'action',
    fixed: 'right',
    width: 200,
    showDrag: true,
    showInTable: true,
  },
];
export default userGroupUsercolumns;
