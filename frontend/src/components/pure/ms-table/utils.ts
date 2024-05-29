import type { TableData } from '@arco-design/web-vue';
/**
 *
 * @param {childrenData} 包含子级数据
 * @param {rowKey}
 * @returns 返回当前子级childrenIds
 */
export function getCurrentRecordChildrenIds(childrenData: TableData[], rowKey: string) {
  const currentRecordChildrenIds: string[] = [];

  function traverse(childrenRecord: TableData) {
    currentRecordChildrenIds.push(childrenRecord[rowKey as string]);
    if (childrenRecord.children && childrenRecord.children.length > 0) {
      childrenRecord.children.forEach((child: TableData) => traverse(child));
    }
  }

  childrenData.forEach((childrenRecord: TableData) => {
    traverse(childrenRecord);
  });

  return currentRecordChildrenIds;
}

export default {};
