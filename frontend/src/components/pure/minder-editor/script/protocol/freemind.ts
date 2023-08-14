const priorities = [
  { jp: 1, mp: 'full-1' },
  { jp: 2, mp: 'full-2' },
  { jp: 3, mp: 'full-3' },
  { jp: 4, mp: 'full-4' },
  { jp: 5, mp: 'full-5' },
  { jp: 6, mp: 'full-6' },
  { jp: 7, mp: 'full-7' },
  { jp: 8, mp: 'full-8' },
];
const mmVersion = '<map version="1.0.1">\n';
const iconTextPrefix = '<icon BUILTIN="';
const iconTextSuffix = '"/>\n';
const nodeCreated = '<node CREATED="';
const nodeId = '" ID="';
const nodeText = '" TEXT="';
const nodeSuffix = '">\n';
const entityNode = '</node>\n';
const entityMap = '</map>';

function concatNodes(node: any) {
  let result = '';
  const nodeData = node.data;
  result += nodeCreated + nodeData.created + nodeId + nodeData.id + nodeText + nodeData.text + nodeSuffix;
  if (nodeData.priority) {
    const mapped = priorities.find((d) => {
      return d.jp === nodeData.priority;
    });
    if (mapped) {
      result += iconTextPrefix + mapped.mp + iconTextSuffix;
    }
  }
  return result;
}
function traverseJson(node: any) {
  let result = '';
  if (!node) {
    return;
  }
  result += concatNodes(node);
  if (node.children && node.children.length > 0) {
    // eslint-disable-next-line no-restricted-syntax
    for (const element of node.children) {
      result += traverseJson(element);
      result += entityNode;
    }
  }
  return result;
}

function exportFreeMind(minder: any) {
  const minds = minder.exportJson();
  const mmContent = mmVersion + traverseJson(minds.root) + entityNode + entityMap;
  try {
    const link = document.createElement('a');
    const blob = new Blob([`\ufeff${mmContent}`], {
      type: 'text/xml',
    });
    link.href = window.URL.createObjectURL(blob);
    link.download = `${minds.root.data.text}.mm`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  } catch (err) {
    // eslint-disable-next-line no-alert, no-console
    console.log(err);
  }
}

export default { exportFreeMind };
