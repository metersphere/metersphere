import mm from '../protocol/freemind';
import json from '../protocol/json';
import md from '../protocol/markdown';
import plain from '../protocol/plain';
import png from '../protocol/png';
import svg from '../protocol/svg';
import useLocaleNotVue from '../tool/useLocaleNotVue';

const tran = useLocaleNotVue;

export default function ExportRuntime(this: any) {
  const { minder, hotbox } = this;

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

  const main = hotbox.state('main');
  main.button({
    position: 'top',
    label: tran('minder.commons.export'),
    key: 'E',
    enable: canExp,
    beforeShow() {
      this.$button.children[0].innerHTML = tran('minder.commons.export');
    },
    next: 'exp',
  });

  const exp = hotbox.state('exp');
  exps.forEach((item) => {
    exp.button({
      position: 'ring',
      label: item.label,
      key: null,
      action: item.cmd,
      beforeShow() {
        this.$button.children[0].innerHTML = tran(item.label);
      },
    });
  });

  exp.button({
    position: 'center',
    label: tran('minder.commons.cancel'),
    key: 'esc',
    beforeShow() {
      this.$button.children[0].innerHTML = tran('minder.commons.cancel');
    },
    next: 'back',
  });
}
