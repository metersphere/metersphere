/* eslint-disable no-underscore-dangle */
const EMPTY_LINE = '';
const NOTE_MARK_START = '<!--Note-->';
const NOTE_MARK_CLOSE = '<!--/Note-->';

function _generateHeaderSharp(level: number) {
  let sharps = '';
  while (level--) sharps += '#';
  return sharps;
}

function _build(node: any, level: number) {
  let lines: string[] = [];

  level = level || 1;

  const sharps = _generateHeaderSharp(level);
  lines.push(`${sharps} ${node.data.text}`);
  lines.push(EMPTY_LINE);

  let { note } = node.data;
  if (note) {
    const hasSharp = /^#/.test(note);
    if (hasSharp) {
      lines.push(NOTE_MARK_START);
      note = note.replace(/^#+/gm, ($0: string) => {
        return sharps + $0;
      });
    }
    lines.push(note);
    if (hasSharp) {
      lines.push(NOTE_MARK_CLOSE);
    }
    lines.push(EMPTY_LINE);
  }

  if (node.children)
    node.children.forEach((child: any) => {
      lines = lines.concat(_build(child, level + 1));
    });

  return lines;
}

function encode(json: any) {
  return _build(json, 1).join('\n');
}
function exportMarkdown(minder: any) {
  const minds = minder.exportJson();
  try {
    const link = document.createElement('a');
    const blob = new Blob([`\ufeff${encode(minds.root)}`], {
      type: 'markdown',
    });
    link.href = window.URL.createObjectURL(blob);
    link.download = `${minds.root.data.text}.md`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  } catch (err) {
    // eslint-disable-next-line no-alert
    console.log(err);
  }
}

export default { exportMarkdown };
