export function getTestCaseDataMap(testCase) {
  let dataMap = new Map();
  testCase.forEach(item => {
    item.steps = JSON.parse(item.steps);
    // if (item.tags && item.tags.length > 0) {
    //   item.tags = JSON.parse(item.tags);
    // }
    let mapItem = dataMap.get(item.nodeId);
    let nodeItem = {
      data: {
        id: item.id,
        text: item.name,
        priority: Number.parseInt(item.priority.substring(item.priority.length - 1 )),
        resource: ["用例"],
        type: item.type,
        method: item.method,
        maintainer: item.maintainer
      }
    }
    parseChildren(nodeItem, item);
    if (mapItem) {
      mapItem.push(nodeItem);
    } else {
      mapItem = [];
      mapItem.push(nodeItem);
      dataMap.set(item.nodeId, mapItem);
    }
  })
  return dataMap;
}

function parseChildren(nodeItem, item) {
  nodeItem.children = [];
  let children = [];
  _parseChildren(children, item.prerequisite, "前置条件");
  item.steps.forEach((step) => {
    let descNode = _parseChildren(children, step.desc, "测试步骤");
    if (descNode) {
      descNode.data.num = step.num;
      descNode.children = [];
      _parseChildren(descNode.children, step.result, "预期结果");
    }
  });
  _parseChildren(children, item.remark, "备注");
  nodeItem.children = children;
}

function _parseChildren(children, k, v) {
  if (k) {
    let node = {
      data: {
        text: k,
        resource: [v]
      }
    }
    children.push(node);
    return node;
  }
}
