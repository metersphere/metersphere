import i18n from "@/i18n/i18n";

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
      resource: [i18n.t('api_test.definition.request.case')],
      type: item.type,
      method: item.method,
      maintainer: item.maintainer,
      stepModel: item.stepModel
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

function parseChildren(nodeItem, item, isDisable) {
  nodeItem.children = [];
  let children = [];
  _parseChildren(children, item.prerequisite, i18n.t('test_track.case.prerequisite'), isDisable);
  if (item.stepModel === 'TEXT') {
   let descNode =  _parseChildren(children, item.stepDescription, null, isDisable);
    if (descNode) {
      descNode.children = [];
      _parseChildren(descNode.children, item.expectedResult, null, isDisable);
    }
  } else {
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
  }
  _parseChildren(children, item.remark, i18n.t('commons.remark'), isDisable);
  nodeItem.children = children;
}

export function listenNodeSelected(callback) {
  let minder = window.minder;
  minder.on('selectionchange ', function (even) {
    if (callback) {
      callback(even);
    }
  });
}

export function listenNodeChange(callback) {
  let minder = window.minder;
  minder.on('contentchange ', function (even) {
    if (callback) {
      callback(even);
    }
  });
}

export function listenBeforeExecCommand(callback) {
  let minder = window.minder;
  minder.on('beforeExecCommand ', function (even) {
    if (callback) {
      callback(even);
    }
  });
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

export function tagChildren(node, resourceName, distinctTags) {
  let children = node.children;
  if (!children) {
    children = [];
  }
  if (!resourceName || !/\S/.test(resourceName)) {return;}
  children.forEach((item) => {
    let isCaseNode = item.data.resource && item.data.resource.indexOf(i18n.t('api_test.definition.request.case')) > -1;
    if (item.data.type === 'node' || isCaseNode) {
      let origin = item.data.resource;
      if (!origin) {
        origin = [];
      }
      let index = origin.indexOf(resourceName);
      // 先删除排他的标签
      if (distinctTags.indexOf(resourceName) > -1) {
        for (let i = 0; i < origin.length; i++) {
          if (distinctTags.indexOf(origin[i]) > -1) {
            origin.splice(i, 1);
            i--;
          }
        }
      }
      if (index !== -1) {
        origin.splice(index, 1);
      } else {
        origin.push(resourceName);
      }
      item.data.resource = origin;
      if (isCaseNode) {
        item.data.changed = true;
      }
      tagChildren(item, resourceName, distinctTags);
    }
  });
}


function modifyParentNodeTag(node, resourceName) {
  let topNode = null;
  while (node.parent) {
    let pNode = node.parent;
    let pResource = pNode.data.resource;
    if (pResource && pResource.length > 0 && pResource.indexOf(resourceName) < 0) {
      pNode.data.resource = [];
      topNode = pNode;
    }
    node = pNode;
  }
  return topNode;
}


export function tagBatch(distinctTags) {
  listenBeforeExecCommand((even) => {
    let minder = window.minder;
    let selectNodes = window.minder.getSelectedNodes();
    let args = even.commandArgs;
    if (selectNodes) {
      selectNodes.forEach((node) => {
        if (node.data.type === 'node' && even.commandName === 'resource') {
          // let origin = minder.queryCommandValue('resource');
          if (args && args.length > 0) {
            let origin = args[0];
            if (origin && origin.length > 0) {
              let resourceName = origin[0];
              tagChildren(node, resourceName, distinctTags);
              let modifyTopNode = modifyParentNodeTag(node, resourceName);
              if (modifyTopNode) {
                modifyTopNode.renderTree();
              } else {
                node.renderTree();
              }
              minder.layout(600);
            }
          }
        }
      });
    }
  });
}

export function tagEditCheck(resourceName) {
  let minder = window.minder;
  let selectNodes = minder.getSelectedNodes();
  if (selectNodes && selectNodes.length > 0) {
    let resource = selectNodes[0].getParent().data.resource;
    if (resource && resource.indexOf('用例') > -1 && resourceName === '用例') {
      return false;
    }
  }
  return true;
}

export function priorityDisableCheck() {
  let minder = window.minder;
  let selectNodes = minder.getSelectedNodes();
  if (selectNodes && selectNodes.length > 0) {
    let resource = selectNodes[0].getParent().data.resource;
    if (resource && resource.indexOf('用例') > -1) {
      return true;
    }
  }
  return false;
}
