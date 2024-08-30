import { Canvg } from 'canvg';
import html2canvas from 'html2canvas-pro';
import JSPDF from 'jspdf';

const A4_WIDTH = 595;
const A4_HEIGHT = 842;
const HEADER_HEIGHT = 16;
const FOOTER_HEIGHT = 16;
const PAGE_HEIGHT = A4_HEIGHT - FOOTER_HEIGHT - HEADER_HEIGHT;
const pdfWidth = A4_WIDTH - 32; // 左右分别 16px 间距
const totalWidth = 1370;
const realPageHeight = Math.ceil(PAGE_HEIGHT * (totalWidth / pdfWidth) * 1.2); // 实际每页高度
const MAX_CANVAS_HEIGHT = realPageHeight * 23; // 一次截图最大高度是 25 页整

/**
 * 替换svg为base64
 */
async function inlineSvgUseElements(container: HTMLElement) {
  const useElements = container.querySelectorAll('use');
  useElements.forEach((useElement) => {
    const href = useElement.getAttribute('xlink:href') || useElement.getAttribute('href');
    if (href) {
      const symbolId = href.substring(1);
      const symbol = document.getElementById(symbolId);
      if (symbol) {
        const svgElement = useElement.closest('svg');
        if (svgElement) {
          svgElement.innerHTML = symbol.innerHTML;
        }
      }
    }
  });
}

/**
 * 将svg转换为base64
 */
async function convertSvgToBase64(svgElement: SVGSVGElement) {
  const canvas = document.createElement('canvas');
  const ctx = canvas.getContext('2d');
  const svgString = new XMLSerializer().serializeToString(svgElement);
  if (ctx) {
    const v = Canvg.fromString(ctx, svgString);
    canvas.width = svgElement.clientWidth;
    canvas.height = svgElement.clientHeight;
    await v.render();
  }
  return canvas.toDataURL('image/png');
}

/**
 * 替换svg为base64
 */
async function replaceSvgWithBase64(container: HTMLElement) {
  await inlineSvgUseElements(container);
  const svgElements = container.querySelectorAll('.c-icon');
  svgElements.forEach(async (svgElement) => {
    const img = new Image();
    img.src = await convertSvgToBase64(svgElement as SVGSVGElement);
    img.width = svgElement.clientWidth;
    img.height = svgElement.clientHeight;
    img.style.marginRight = '8px';
    svgElement.parentNode?.replaceChild(img, svgElement);
  });
}

/**
 * 导出PDF
 * @param name 文件名
 * @param contentId 内容DOM id
 * @description 通过html2canvas生成图片，再通过jsPDF生成pdf
 * （使用html2canvas截图时，因为插件有截图极限，超出极限部分会出现截图失败，所以这里设置了MAX_CANVAS_HEIGHT截图高度，然后根据这个截图高度分页截图，然后根据每个截图裁剪每页 pdf 的图片并添加到 pdf 内）
 */
export default async function exportPDF(name: string, contentId: string) {
  console.log('start', new Date().getMinutes(), new Date().getSeconds());
  const element = document.getElementById(contentId);
  if (element) {
    await replaceSvgWithBase64(element);
    const totalHeight = element.scrollHeight;
    // jsPDFs实例
    const pdf = new JSPDF({
      unit: 'pt',
      format: 'a4',
      orientation: 'p',
    });
    pdf.setFontSize(10);
    // 计算pdf总页数
    let totalPages = 0;
    let position = 0;
    let pageIndex = 1;
    let loopTimes = 0;
    const canvasList: HTMLCanvasElement[] = [];
    while (position < totalHeight) {
      // 这里是大的分页，也就是截图画布的分页
      const offscreenHeight = Math.min(MAX_CANVAS_HEIGHT, totalHeight - position);
      // eslint-disable-next-line no-await-in-loop
      const canvas = await html2canvas(element, {
        x: 0,
        y: position,
        width: totalWidth,
        height: offscreenHeight,
        backgroundColor: '#f9f9fe',
        scale: window.devicePixelRatio * 1.2, // 增加清晰度
      });
      canvasList.push(canvas);
      position += offscreenHeight;
      totalPages += Math.ceil(canvas.height / realPageHeight);
      loopTimes++;
    }
    totalPages -= loopTimes - 1; // 减去多余的页数
    // 生成 PDF
    canvasList.forEach((canvas) => {
      const canvasWidth = canvas.width;
      const canvasHeight = canvas.height;
      const pages = Math.ceil(canvasHeight / realPageHeight);
      for (let i = 1; i <= pages; i++) {
        // 这里是小的分页，是 pdf 的每一页
        const pagePosition = (i - 1) * realPageHeight;
        // 创建临时画布
        const tempCanvas = document.createElement('canvas');
        tempCanvas.width = canvasWidth;
        tempCanvas.height = realPageHeight;
        const tempContext = tempCanvas.getContext('2d', { willReadFrequently: true });
        if (tempContext) {
          // 填充背景颜色为白色
          tempContext.fillStyle = '#ffffff';
          tempContext.fillRect(0, 0, tempCanvas.width, tempCanvas.height);
          // 将大分页的画布图片裁剪成pdf 页面内容大小，并渲染到临时画布上
          tempContext.drawImage(canvas, 0, -pagePosition, canvasWidth, canvasHeight);
          const tempCanvasData = tempCanvas.toDataURL('image/jpeg', 0.8);
          // 将临时画布图片渲染到 pdf 上
          pdf.addImage(tempCanvasData, 'jpeg', 16, 16, pdfWidth, PAGE_HEIGHT);
        }
        tempCanvas.remove();
        pdf.text(
          `${pageIndex} / ${totalPages}`,
          pdf.internal.pageSize.width / 2 - 10,
          pdf.internal.pageSize.height - 4
        );
        if (i < pages) {
          pdf.addPage();
          pageIndex++;
        }
      }
      canvas.remove();
    });
    pdf.save(`${name}.pdf`);
    console.log('end', new Date().getMinutes(), new Date().getSeconds());
  }
}
