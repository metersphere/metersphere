import i18n from "@/i18n/i18n";
import {getTestCasesForMinder} from "@/network/testCase";

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

/**
 * 加载模块下的用例
 * @param projectId
 * @param result
 */
export function loadNode(node, param, getCaseFuc, setParamCallback) {
  let data = node.data;
  if (!data.loaded && data.type === 'node') {
    if (param.result) {
      param.result.loading = true;
    }
    let request = param.request;
    request.nodeId = data.id;
    if (data.id === 'root') {
      request.nodeId = '';
    }
    if (getCaseFuc) {
      getCaseFuc(request, (testCases) => {
        appendCaseNodes(node, testCases, param, setParamCallback);
      });
    }
  }
  data.loaded = true;
}

/**
 * 加载当前选中的模块下的用例
 * @param projectId
 * @param result
 */
export function loadSelectNodes(param, getCaseFuc, setParamCallback) {
  let minder = window.minder;
  let selectNodes = minder.getSelectedNodes();
  selectNodes.forEach(node => {
    loadNode(node, param, getCaseFuc, setParamCallback);
  });
}


export function handleExpandToLevel(level, node, param, getCaseFuc, setParamCallback) {
  loadNode(node, param, getCaseFuc, setParamCallback);
  level--;
  if (level > 0) {
    if (node.children) {
      node.children.forEach(item => {
        handleExpandToLevel(level, item, param, getCaseFuc, setParamCallback);
      });
    }
  }
}

export function handleTestCaseAdd(pid, data) {
  window.minder.getRoot().traverse(function(node) {
    if (node.data.id === pid && node.data.loaded) {
      appendCase(node, data);
      expandNode(node);
      node.render();
    }
  });
}

export function handTestCaeEdit(data) {
  window.minder.getRoot().traverse(function(node) {
    if (node.data.id === data.id) {
      let pNode = node.parent;
      window.minder.removeNode(node);
      appendCase(pNode, data);
      expandNode(pNode);
      pNode.render();
    }
  });
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


export function appendCase(parent, item, isDisable, setParamCallback) {
  let caseData = {
    id: item.id,
    text: item.name,
    priority: Number.parseInt(item.priority.substring(item.priority.length - 1 )) + 1,
    resource: [i18n.t('api_test.definition.request.case')],
    type: item.type,
    method: item.method,
    maintainer: item.maintainer,
    stepModel: item.stepModel
  }
  if (setParamCallback) {
    setParamCallback(caseData, item);
  }
  if (isDisable) {
    caseData.disable = true;
    // 用例节点可以打标签
    caseData.allowDisabledTag = true;
  }

  let caseNode = appendChildNode(parent, caseData);

  appendChildNode(caseNode, getNodeData(item.prerequisite, i18n.t('test_track.case.prerequisite'), isDisable));

  appendChildNode(caseNode, getNodeData(item.remark, i18n.t('commons.remark'), isDisable));

  if (item.stepModel === 'TEXT') {
    let descData =  getNodeData(item.stepDescription, null, isDisable);
    let descNode = appendChildNode(caseNode, descData);
    if (descData) {
      appendChildNode(descNode, getNodeData(item.expectedResult, null, isDisable));
    }
  } else {
    if (item.steps) {
      if (!(item.steps instanceof Array)) {
        item.steps = JSON.parse(item.steps);
      }
      item.steps.forEach((step) => {
        let descData = getNodeData(step.desc, null, isDisable);
        if (descData) {
          descData.num = step.num;
          let descNode = appendChildNode(caseNode, descData);
          appendChildNode(descNode, getNodeData(step.result, null, isDisable));
        }
      });
    }
  }
}

function getNodeData(text, resource, isDisable) {
  if (text) {
    let data = {
        text: text,
        resource: resource ? [resource] : []
    };
    if (isDisable) {
      data.disable = true;
    }
    return data;
  }
}

/**
 * 添加用例节点
 * @param parent
 * @param testCases
 * @param result
 */
export function appendCaseNodes(parent, testCases, param, setParamCallback) {
  clearChildren(parent);
  if (testCases) {
    for (let i = 0; i < testCases.length; i++) {
      appendCase(parent, testCases[i], param.isDisable, setParamCallback);
    }
  }
  expandNode(parent);
  if (param.result) {
    param.result.loading = false;
  }
}

/**
 * 去掉已有节点
 * @param parent
 */
function clearChildren(node) {
  let children = node.children;
  if (children) {
    for (let i = 0; i < children.length; i++) {
      let item = children[i];
      if (item.data.type !== 'node') {
        window.minder.removeNode(item);
        i--;
      }
    }
  }
}

function appendChildNode(parent, childData, fresh) {
  if (!childData || !childData.text) {
    return;
  }
  let km = window.minder;
  var node = km.createNode(childData, parent);
  km.select(node, true);
  if (fresh) {
    if (parent.isExpanded()) {
      node.render();
    } else {
      parent.expand();
      parent.renderTree();
    }
    km.layout(60);
  }
  return node;
}


function expandNode(node) {
  node.expand();
  node.renderTree();
  window.minder.layout(60);
}


/**
 *  测试计划和评审支持给模块批量打标签
 * @param distinctTags
 */
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

export function handleAfterSave(pNode, param) {
  let children = pNode.children;
  if (children) {
    for (let i = 0; i < children.length; i++) {
      let item = children[i];
      if (item.data.id === null || (item.data.id && item.data.id.length < 20)) {
        pNode.data.loaded = false;
        loadNode(pNode, param, getTestCasesForMinder);
        return;
      }
      if (item.data.changed) {
        item.data.changed = false;
      }
      if (item.data.type === 'node') {
        handleAfterSave(item, param);
      }
    }
  }
}
