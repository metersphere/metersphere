const LINE_ENDING = '\r';
const TAB_CHAR = '\t';

function repeat(s: string, n: number) {
  let result = '';
  while (n--) result += s;
  return result;
}

function encode(json: any, level: number) {
  let local = '';
  level = level || 0;
  local += repeat(TAB_CHAR, level);
  local += json.data.text + LINE_ENDING;
  if (json.children) {
    json.children.forEach((child: any) => {
      local += encode(child, level + 1);
    });
  }
  return local;
}

function exportTextTree(minder: any) {
  const minds = minder.exportJson();
  try {
    const link = document.createElement('a');
    const blob = new Blob([`\ufeff${encode(minds.root, 0)}`], {
      type: 'text/plain',
    });
    link.href = window.URL.createObjectURL(blob);
    link.download = `${minds.root.data.text}.txt`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  } catch (err) {
    // eslint-disable-next-line no-console
    console.log(err);
  }
}

export default { exportTextTree };
