import mm from '../protocol/freemind';
import json from '../protocol/json';
import md from '../protocol/markdown';
import plain from '../protocol/plain';
import png from '../protocol/png';
import svg from '../protocol/svg';
import useLocaleNotVue from '../tool/useLocaleNotVue';

const tran = useLocaleNotVue;

export default function ExportRuntime(this: any) {
  const { minder } = this;

  function canExp() {
    return true;
  }

  function exportJson() {
    json.exportJson(minder);
  }

  function exportImage() {
    png.exportPNGImage(minder);
  }

  function exportSVG() {
    svg.exportSVG(minder);
  }

  function exportTextTree() {
    plain.exportTextTree(minder);
  }

  function exportMarkdown() {
    md.exportMarkdown(minder);
  }

  function exportFreeMind() {
    mm.exportFreeMind(minder);
  }

  const exps = [
    { label: '.json', key: 'j', cmd: exportJson },
    { label: '.png', key: 'p', cmd: exportImage },
    { label: '.svg', key: 's', cmd: exportSVG },
    { label: '.txt', key: 't', cmd: exportTextTree },
    { label: '.md', key: 'm', cmd: exportMarkdown },
    { label: '.mm', key: 'f', cmd: exportFreeMind },
  ];
}
