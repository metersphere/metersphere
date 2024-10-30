import '@/assets/fonts/AlibabaPuHuiTi-3-55-Regular-normal';

import { replaceSvgWithBase64 } from '@/utils/exportPdf';

import html2canvas from 'html2canvas-pro';
import JSPDF from 'jspdf';
import autoTable, { UserOptions } from 'jspdf-autotable';

const A4_WIDTH = 595;
const A4_HEIGHT = 842;
const HEADER_HEIGHT = 16;
const FOOTER_HEIGHT = 16;
export const PAGE_HEIGHT = A4_HEIGHT - FOOTER_HEIGHT - HEADER_HEIGHT;
export const PDF_WIDTH = A4_WIDTH - 32; // 左右分别 16px 间距
export const CONTAINER_WIDTH = 1190;
export const SCALE_RATIO = window.devicePixelRatio * 1.5;
export const PAGE_PDF_WIDTH_RATIO = CONTAINER_WIDTH / PDF_WIDTH; // 页面容器宽度与 pdf 宽度的比例
// 实际每页高度 = PDF页面高度/页面容器宽度与 pdf 宽度的比例(这里比例*SCALE_RATIO 是因为html2canvas截图时生成的是 SCALE_RATIO 倍的清晰度)
export const IMAGE_HEIGHT = Math.ceil(PAGE_HEIGHT * PAGE_PDF_WIDTH_RATIO * SCALE_RATIO);

export type PdfTableConfig = Pick<UserOptions, 'tableId' | 'columnStyles' | 'columns' | 'body'>;

/**
 * 导出PDF
 * @param name 文件名
 * @param contentId 内容DOM id
 * @description 通过html2canvas生成图片，再通过jsPDF生成pdf
 * （使用html2canvas截图时，因为插件有截图极限，超出极限部分会出现截图失败，所以这里设置了MAX_CANVAS_HEIGHT截图高度，然后根据这个截图高度分页截图，然后根据每个截图裁剪每页 pdf 的图片并添加到 pdf 内）
 */
export default async function exportPDF(
  name: string,
  contentId: string,
  autoTableConfig: PdfTableConfig[],
  tableTitleMap?: Record<string, string>,
  doneCallback?: () => void
) {
  const element = document.getElementById(contentId);
  if (element) {
    await replaceSvgWithBase64(element); // 替换截图容器内的svg为base64，因为html2canvas无法截取url-link方式的svg
    // jsPDF实例
    const pdf = new JSPDF({
      unit: 'pt',
      format: 'a4',
      orientation: 'p',
    });
    const canvas = await html2canvas(element, {
      x: 0,
      width: CONTAINER_WIDTH,
      height: element.clientHeight,
      backgroundColor: '#f9f9fe',
      scale: SCALE_RATIO, // 缩放增加清晰度
      useCORS: true,
    });
    pdf.setFont('AlibabaPuHuiTi-3-55-Regular');
    pdf.setFontSize(10);
    // 创建图片裁剪画布
    const cropCanvas = document.createElement('canvas');
    cropCanvas.width = CONTAINER_WIDTH * SCALE_RATIO;
    cropCanvas.height = IMAGE_HEIGHT;
    const tempContext = cropCanvas.getContext('2d', { willReadFrequently: true });
    // 生成 PDF
    const canvasWidth = canvas.width;
    const canvasHeight = canvas.height;
    const pages = Math.ceil(canvasHeight / IMAGE_HEIGHT);
    for (let i = 1; i <= pages; i++) {
      // 这里是小的分页，是 pdf 的每一页
      const pagePosition = (i - 1) * IMAGE_HEIGHT;
      if (tempContext) {
        if (i === pages) {
          // 填充背景颜色为白色
          tempContext.fillStyle = '#ffffff';
          tempContext.fillRect(0, 0, cropCanvas.width, cropCanvas.height);
        }
        // 将大分页的画布图片裁剪成pdf 页面内容大小，并渲染到临时画布上
        tempContext.drawImage(canvas, 0, -pagePosition, canvasWidth, canvasHeight);
        const tempCanvasData = cropCanvas.toDataURL('image/jpeg');
        // 将临时画布图片渲染到 pdf 上
        pdf.addImage(tempCanvasData, 'jpeg', 16, 16, PDF_WIDTH, PAGE_HEIGHT);
      }
      cropCanvas.remove();
      if (i < pages) {
        pdf.text(`${i}`, pdf.internal.pageSize.width / 2 - 10, pdf.internal.pageSize.height - 4);
        pdf.addPage();
      }
    }
    const lastImagePageUseHeight =
      (canvasHeight > IMAGE_HEIGHT ? canvasHeight - IMAGE_HEIGHT : canvasHeight) / PAGE_PDF_WIDTH_RATIO / SCALE_RATIO; // 最后一页带图片的pdf页面被图片占用的高度

    const commonOdfTableConfig: Partial<UserOptions> = {
      headStyles: {
        fillColor: '#793787',
      },
      styles: {
        font: 'AlibabaPuHuiTi-3-55-Regular',
        fontSize: 8,
      },
      pageBreak: 'auto',
      rowPageBreak: 'avoid',
      margin: { top: 16, left: 16, right: 16, bottom: 16 },
      tableWidth: PDF_WIDTH,
      willDrawPage: (data) => {
        const title = tableTitleMap?.[data.table.id as string];
        if (title && data.cursor) {
          pdf.text(title, 16, data.cursor.y + 4);
          // 在文字后加入 8px 高的空白
          data.cursor.y += 12;
        }
      },
    };
    autoTableConfig.forEach((config, index) => {
      autoTable(pdf, {
        ...(commonOdfTableConfig as UserOptions),
        ...config,
        startY: index === 0 && lastImagePageUseHeight > 0 ? lastImagePageUseHeight + 32 : undefined, // 第一页表格如果和图片同一页，则需要设置 startY 为当前图片占用高度+32，以避免表格遮挡图片
        didDrawPage: (data) => {
          pdf.text(
            `${data.doc.internal.getCurrentPageInfo().pageNumber}`,
            pdf.internal.pageSize.width / 2 - 10,
            pdf.internal.pageSize.height - 4
          );
        },
      });
    });
    pdf.save(`${name}.pdf`);
    nextTick(() => {
      if (doneCallback) {
        doneCallback();
      }
    });
  }
}
