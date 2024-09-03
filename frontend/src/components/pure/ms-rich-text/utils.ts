import { FormTableColumn } from '@/components/pure/ms-form-table/index.vue';

import { useI18n } from '@/hooks/useI18n';

const { t } = useI18n();

export function generateTableHTML(columns: FormTableColumn[], data: Record<string, any>[]): string {
  const maxColWidth = 300; // 最大宽度
  let tableHTML = '<table style="width:100%;" table-layout="auto"><tbody>';

  // 生成表头
  tableHTML += '<tr>';
  columns.forEach((column: FormTableColumn) => {
    const colWidth = Math.min(column.width || maxColWidth, maxColWidth);
    tableHTML += `<th colspan="1" rowspan="1" width="${colWidth}" colwidth="${colWidth}"><p style="max-width:${colWidth}px;">${t(
      column.title as string
    )}</p></th>`;
  });
  tableHTML += '</tr>';

  // 生成数据行
  data.forEach((row) => {
    tableHTML += '<tr>';
    columns.forEach((column: FormTableColumn) => {
      // 生成单元格内容
      const colWidth = Math.min(column.width || maxColWidth, maxColWidth);
      const dataIndexKey = column.dataIndex as string;
      const cellContent = column.format ? column.format(row) : row[dataIndexKey] || '';

      tableHTML += `<td colspan="1" rowspan="1" width="${colWidth}" colwidth="${colWidth}"><p style="max-width:${colWidth}px;">${
        cellContent || '-'
      }</p></td>`;
    });
    tableHTML += '</tr>';
  });

  tableHTML += '</tbody></table>';
  return tableHTML;
}

export default {};
