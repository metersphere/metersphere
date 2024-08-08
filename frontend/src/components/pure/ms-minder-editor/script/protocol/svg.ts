/* eslint-disable no-bitwise */
/* eslint-disable prefer-const */

function downloadSVG(fileURI: string, fileName: string) {
  try {
    const link = document.createElement('a');
    link.href = fileURI;
    link.download = `${fileName}.svg`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  } catch (err) {
    // eslint-disable-next-line no-console
    console.log(err);
  }
}

function exportSVG(minder: any) {
  const paper = minder.getPaper();
  const paperTransform = paper.shapeNode.getAttribute('transform');
  let svgXml;
  let $svg;

  const renderContainer = minder.getRenderContainer();
  const renderBox = renderContainer.getRenderBox();
  const { width, height } = renderBox;
  const padding = 20;

  paper.shapeNode.setAttribute('transform', 'translate(0.5, 0.5)');
  svgXml = paper.container.innerHTML;
  paper.shapeNode.setAttribute('transform', paperTransform);

  const { document } = window;
  const el = document.createElement('div');
  el.innerHTML = svgXml;
  $svg = el.getElementsByTagName('svg');

  const index = $svg.length - 1;

  $svg[index].setAttribute('width', width + padding * 2 || 0);
  $svg[index].setAttribute('height', height + padding * 2 || 0);

  $svg[index].setAttribute(
    'viewBox',
    [
      (renderBox.x - padding) | 0,
      (renderBox.y - padding) | 0,
      (width + padding * 2) | 0,
      (height + padding * 2) | 0,
    ].join(' ')
  );

  const div = document.createElement('div');
  div.appendChild($svg[index]);
  svgXml = div.innerHTML;
  svgXml = svgXml.replace(/&nbsp;/g, '&#xa0;');

  const blob = new Blob([svgXml], {
    type: 'image/svg+xml',
  });

  const DOMURL = window.URL || window.webkitURL || window;
  const svgUrl = DOMURL.createObjectURL(blob);

  const mind = window.editor.minder.exportJson();
  downloadSVG(svgUrl, mind.root.data.text);
}

export default { exportSVG };
