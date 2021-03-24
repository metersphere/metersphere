import {getUUID} from "@/common/js/utils";

export function getTestCaseDataMap(testCase, isDisable, setParamCallback) {
  let dataMap = new Map();
  if (testCase) {
    testCase.forEach(item => {
      parseCase(item, dataMap, isDisable, setParamCallback);
    })
  }
  return dataMap;
}

export function parseCase(item, dataMap, isDisable, setParamCallback) {
  if (item.steps) {
    item.steps = JSON.parse(item.steps);
  } else {
    item.steps = [];
  }
  // if (item.tags && item.tags.length > 0) {
  //   item.tags = JSON.parse(item.tags);
  // }
  let mapItem = dataMap.get(item.nodeId);
  let nodeItem = {
    data: {
      id: item.id,
      text: item.name,
      priority: Number.parseInt(item.priority.substring(item.priority.length - 1 )) + 1,
      resource: ["用例"],
      type: item.type,
      method: item.method,
      maintainer: item.maintainer
    }
  }
  if (setParamCallback) {
    setParamCallback(nodeItem.data, item);
  }
  if (isDisable) {
    nodeItem.data.disable = true;
    // 用例节点可以打标签
    nodeItem.data.allowDisabledTag = true;
  }
  parseChildren(nodeItem, item, isDisable);
  if (mapItem) {
    mapItem.push(nodeItem);
  } else {
    mapItem = [];
    mapItem.push(nodeItem);
    dataMap.set(item.nodeId, mapItem);
  }
  return nodeItem;
}

function parseChildren(nodeItem, item, isDisable) {
  nodeItem.children = [];
  let children = [];
  _parseChildren(children, item.prerequisite, "前置条件", isDisable);
  if (item.steps) {
    item.steps.forEach((step) => {
      let descNode = _parseChildren(children, step.desc, undefined, isDisable);
      if (descNode) {
        descNode.data.num = step.num;
        descNode.children = [];
        _parseChildren(descNode.children, step.result, undefined, isDisable);
      }
    });
  }
  _parseChildren(children, item.remark, "备注", isDisable);
  nodeItem.children = children;
}

function _parseChildren(children, k, v, isDisable) {
  if (k) {
    let node = {
      data: {
        text: k,
        resource: v ? [v] : []
      },
      children: []
    }
    if (isDisable) {
      node.data.disable = true;
    }
    children.push(node);
    return node;
  }
}

export function appendChild(appendPid, root, node) {
  if (root.data.id === appendPid) {
    root.children.push(node);
    return;
  }
  if (!root.children) {
    root.children = [];
  }
  let children = root.children;
  for (const index in children) {
    let item = children[index];
    if (item.data.id === appendPid) {
      item.data.expandState = "expand";
      item.children.push(node);
      return;
    } else {
      appendChild(appendPid, item, node);
    }
  }
}

export function updateNode(root, node) {
  if (!root.children) {
    root.children = [];
  }
  let children = root.children;
  for (const index in children) {
    let item = children[index];
    if (item.data.id === node.data.id) {
      children[index] = node;
      return;
    } else {
      updateNode(item, node);
    }
  }
}
