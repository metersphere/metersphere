/* eslint-disable prefer-const */
const DOMURL = window.URL || window.webkitURL || window;

function downloadImage(fileURI: string, fileName: string) {
  try {
    const link = document.createElement('a');
    link.href = fileURI;
    link.download = `${fileName}.png`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  } catch (err) {
    // eslint-disable-next-line no-console
    console.log(err);
  }
}

function loadImage(url: string) {
  return new Promise((resolve, reject) => {
    const image = document.createElement('img');
    image.onload = function () {
      resolve(this);
    };
    image.onerror = (err) => {
      reject(err);
    };
    image.crossOrigin = '';
    image.src = url;
  });
}

function getSVGInfo(minder: any) {
  const paper = minder.getPaper();
  let svgXml;
  let $svg;
  const renderContainer = minder.getRenderContainer();
  const renderBox = renderContainer.getRenderBox();
  const width = renderBox.width + 1;
  const height = renderBox.height + 1;
  let blob;
  let svgUrl;
  // 保存原始变换，并且移动到合适的位置
  const paperTransform = paper.shapeNode.getAttribute('transform');
  paper.shapeNode.setAttribute('transform', 'translate(0.5, 0.5)');
  renderContainer.translate(-renderBox.x, -renderBox.y);

  // 获取当前的 XML 代码
  svgXml = paper.container.innerHTML;

  // 回复原始变换及位置
  renderContainer.translate(renderBox.x, renderBox.y);
  paper.shapeNode.setAttribute('transform', paperTransform);

  // 过滤内容
  const el = document.createElement('div');
  el.innerHTML = svgXml;
  $svg = el.getElementsByTagName('svg');

  const index = $svg.length - 1;

  $svg[index].setAttribute('width', renderBox.width + 1);
  $svg[index].setAttribute('height', renderBox.height + 1);

  const div = document.createElement('div');
  div.appendChild($svg[index]);
  svgXml = div.innerHTML;

  // Dummy IE
  svgXml = svgXml.replace(
    ' xmlns="http://www.w3.org/2000/svg" xmlns:NS1="" NS1:ns1:xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:NS2="" NS2:xmlns:ns1=""',
    ''
  );

  // svg 含有 &nbsp; 符号导出报错 Entity 'nbsp' not defined
  svgXml = svgXml.replace(/&nbsp;/g, '&#xa0;');

  blob = new Blob([svgXml], {
    type: 'image/svg+xml',
  });

  svgUrl = DOMURL.createObjectURL(blob);

  return {
    width,
    height,
    dataUrl: svgUrl,
    xml: svgXml,
  };
}

function fillBackground(ctx: any, style: CanvasPattern | null | undefined, canvas: any) {
  ctx.save();
  ctx.fillStyle = style;
  ctx.fillRect(0, 0, canvas.width, canvas.height);
  ctx.restore();
}

function drawImage(ctx: any, image: any, x: number, y: number) {
  ctx.drawImage(image, x, y);
}

function generateDataUrl(canvas: any) {
  try {
    const url = canvas.toDataURL('png');
    return url;
  } catch (e) {
    throw new Error('当前浏览器版本不支持导出 PNG 功能，请尝试升级到最新版本！');
  }
}

function exportPNGImage(minder: any) {
  /* 绘制 PNG 的画布及上下文 */
  const canvas = document.createElement('canvas');
  const ctx = canvas.getContext('2d');

  /* 尝试获取背景图片 URL 或背景颜色 */
  const bgDeclare = minder.getStyle('background').toString();
  const bgUrl = /url\((.+)\)/.exec(bgDeclare);
  const bgColor = window.kity.Color.parse(bgDeclare);

  /* 获取 SVG 文件内容 */
  const svgInfo = getSVGInfo(minder);
  const { width } = svgInfo;
  const { height } = svgInfo;
  const svgDataUrl = svgInfo.dataUrl;

  /* 画布的填充大小 */
  const padding = 20;

  canvas.width = width + padding * 2;
  canvas.height = height + padding * 2;

  function drawSVG() {
    const mind = window.editor.minder.exportJson();
    if (typeof window.canvg !== 'undefined') {
      return window.canvg(canvas, svgInfo.xml, {
        ignoreMouse: true,
        ignoreAnimation: true,
        ignoreDimensions: true,
        ignoreClear: true,
        offsetX: padding,
        offsetY: padding,
        renderCallback() {
          downloadImage(generateDataUrl(canvas), mind.root.data.text);
        },
      });
    }
    return loadImage(svgDataUrl).then((svgImage) => {
      drawImage(ctx, svgImage, padding, padding);
      DOMURL.revokeObjectURL(svgDataUrl);
      downloadImage(generateDataUrl(canvas), mind.root.data.text);
    });
  }

  if (bgUrl) {
    loadImage(bgUrl[1]).then((image) => {
      fillBackground(ctx, ctx?.createPattern(image as CanvasImageSource, 'repeat'), canvas);
      drawSVG();
    });
  } else {
    fillBackground(ctx, bgColor.toString(), canvas);
    drawSVG();
  }
}

export default { exportPNGImage };
