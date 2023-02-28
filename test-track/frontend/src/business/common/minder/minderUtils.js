import i18n from "@/i18n";
import {getCurrentProjectID, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {$success, $warning, $info} from "metersphere-frontend/src/plugins/message";
import {deleteIssueRelate} from "@/api/issue";
import {minderPageInfoMap} from "@/api/testCase";
import {setPriorityView} from "vue-minder-editor-plus/src/script/tool/utils";
import {useStore} from "@/store";
import {hasPermission} from "@/business/utils/sdk-utils";

export function listenNodeSelected(callback) {
  let minder = window.minder;
  minder.on('selectionchange ', function (even) {
    if (callback) {
      callback(even);
    }
  });
}

export function listenDblclick(callback) {
  let minder = window.minder;
  minder.on('dblclick ', function (even) {
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

export function getSelectedNodes() {
  return window.minder.getSelectedNodes();
}

/**
 * 加载模块下的用例
 * @param projectId
 * @param result
 */
export function loadNode(node, param, getCaseFuc, setParamCallback, getExtraNodeFuc) {
  return new Promise((resolve) => {
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
        // 加载用例
        getCaseFuc(request, (testCases) => {
          initNodeCase(node, testCases, param, setParamCallback);
          if (getExtraNodeFuc) {
            param.result.loading = true;
            // 加载临时节点
            getExtraNodeFuc(getCurrentProjectID(), data.id)
              .then((r) => {
                appendExtraNodes(node, r.nodes);
                param.result.loading = false;
                resolve();
              });
          } else {
            resolve();
          }
        });
      } else {
        resolve();
      }
    } else if (data.type === 'nextPage') {
      // 分页处理，如果某个模块下用例太多，则分步加载
      if (param.result) {
        param.result.loading = true;
      }
      let request = param.request;
      request.nodeId = data.nodeId;
      let minderPageInfo = minderPageInfoMap.get(request.nodeId);
      minderPageInfo.pageNum++;
      if (getCaseFuc) {
        getCaseFuc(request, (testCases) => {
          appendNodeCases(node.parent, testCases, param, setParamCallback);
          window.minder.removeNode(node);
          resolve();
        });
      } else {
        resolve();
      }
    } else {
      resolve();
    }
    data.loaded = true;
  });
}

/**
 * 加载当前选中的模块下的用例
 * @param projectId
 * @param result
 */
export function loadSelectNodes(param, getCaseFuc, setParamCallback, getExtraNodeFuc) {
  let minder = window.minder;
  let selectNodes = minder.getSelectedNodes();
  selectNodes.forEach(node => {
    loadNode(node, param, getCaseFuc, setParamCallback, getExtraNodeFuc);
  });
}

export function handleExpandToLevel(level, node, param, getCaseFuc, setParamCallback, getExtraNodeFuc) {
  loadNode(node, param, getCaseFuc, setParamCallback, getExtraNodeFuc);
  level--;
  if (level > 0) {
    if (node.children) {
      node.children.forEach(item => {
        handleExpandToLevel(level, item, param, getCaseFuc, setParamCallback, getExtraNodeFuc);
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

export function appendIssueChildNode(pNode, issue) {
  let data = getNodeData('缺陷ID：' + issue.num, null, true, 'issue');
  data.id = issue.id;
  data.allowDelete = true;
  appendChildNode(pNode, data);
}

export function handleIssueAdd(data) {
  if (data && data.id) {
    let pNode = getSelectedNode();
    appendIssueChildNode(pNode, data);
    expandNode(pNode);
    pNode.render();
  }
}

export function handleIssueBatch(issues) {
  let pNode = getSelectedNode();
  issues.forEach(item => {
    appendIssueChildNode(pNode, item);
  });
  expandNode(pNode);
  pNode.render();
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

export async function tagChildren(node, resourceName, distinctTags, loadNodeParam) {

  // 先加载当前模块下的用例再打标签
  if (isModuleNode(node)) {
    await loadNode(node, loadNodeParam.param,
      loadNodeParam.getCaseFuc, loadNodeParam.setParamCallback);
  }

  let children = node.children;
  if (!children) {
    children = [];
  }

  if (!resourceName || !/\S/.test(resourceName)) {
    return;
  }

  for (const item of children) {
    let isCaseNode = item.data.resource && item.data.resource.indexOf(i18n.t('api_test.definition.request.case')) > -1;
    if (item.data.type === 'node' || isCaseNode) {
      let origin = item.data.resource;
      if (!origin) {
        origin = [];
      }
      // 先删除排他的标签
      if (distinctTags.indexOf(resourceName) > -1) {
        for (let i = 0; i < origin.length; i++) {
          if (distinctTags.indexOf(origin[i]) > -1 && origin[i] !== resourceName) {
            origin.splice(i, 1);
            i--;
          }
        }
      }
      if (origin.indexOf(resourceName) < 0) {
        origin.push(resourceName);
      }
      item.data.resource = origin;
      if (isCaseNode) {
        item.data.changed = true;
      }
      await tagChildren(item, resourceName, distinctTags, loadNodeParam);
    }
  }
  return;
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
    priority: item.priority ? Number.parseInt(item.priority.substring(item.priority.length - 1 )) + 1 : null,
    resource: [i18n.t('api_test.definition.request.case')],
    type: 'case',
    method: item.method,
    maintainer: item.maintainer,
    stepModel: item.stepModel,
    caseId: item.caseId
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

  if (item.issueList && item.issueList.length > 0) {
    item.issueList.forEach(issue => {
      appendIssueChildNode(caseNode, issue);
    });
  }
}

function getNodeData(text, resource, isDisable, type) {
  if (text) {
    let data = {
      text: text,
      resource: resource ? [resource] : []
    };
    if (isDisable) {
      data.disable = true;
    }
    if (type) {
      data.type = type;
    }
    return data;
  }
}

/**
 * 初始化模块下的用例
 * @param parent
 * @param testCases
 * @param result
 */
export function initNodeCase(parent, testCases, param, setParamCallback) {
  clearChildren(parent);
  appendNodeCases(parent, testCases, param, setParamCallback);
}

/**
 * 在模块下添加用例
 * @param parent
 * @param testCases
 * @param param
 * @param setParamCallback
 */
export function appendNodeCases(parent, testCases, param, setParamCallback) {
  if (testCases) {
    for (let i = 0; i < testCases.length; i++) {
      appendCase(parent, testCases[i], param.isDisable, setParamCallback);
    }
  }

  appendNextPageNode(parent);

  expandNode(parent);
  if (param.result) {
    param.result.loading = false;
  }
}

/**
 * 判断下当前模块的用例数量是不是分页的
 * 是分页的话，添加下一页节点
 * @param parent
 */
export function appendNextPageNode(parent) {
  let caseNum = 0;
  if (parent.children) {
    for (const item of parent.children) {
      if (item.data.type === 'case') {
        caseNum++;
      }
    }
  }
  let minderPageInfo = minderPageInfoMap.get(parent.data.id === 'root' ? '' : parent.data.id);
  let total = minderPageInfo ? minderPageInfo.total : 0;
  if (total > caseNum) {
    let nexPageNode = {
      text: '...',
      type: 'nextPage',
      nodeId: parent.data.id
    }
    appendChildNode(parent, nexPageNode);
  }
}

export function appendExtraNodes(parent, nodes) {
  if (nodes) {
    if (!parent.children) {
      parent.children = [];
    }
    nodes.forEach(i => {
      if (i.nodeData) {
        let dataObj = JSON.parse(i.nodeData);
        _appendExtraNodes(parent, dataObj);
      }
    });
  }
}

function _appendExtraNodes(parent, data) {
  data.isExtraNode = true;
  let node = appendChildNode(parent, data, true);
  if (data.children && data.children.length > 0) {
    data.children.forEach(child => {
      _appendExtraNodes(node, child);
    });
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
  // 手动修改优先级的设置，避免展开时优先级显示不正确
  setPriorityView(true, 'P');
}


/**
 *  测试计划和评审支持给模块批量打标签
 * @param distinctTags
 */
export function tagBatch(distinctTags, loadNodeParam) {
  listenBeforeExecCommand((even) => {
    let minder = window.minder;
    let selectNodes = window.minder.getSelectedNodes();
    let args = even.commandArgs;
    if (selectNodes) {
      selectNodes.forEach(async (node) => {
        if (node.data.type === 'node' && even.commandName === 'resource') {
          if (args && args.length > 0) {
            let origin = args[0];
            if (origin && origin.length > 0) {
              let resourceName = origin[0];
              await tagChildren(node, resourceName, distinctTags, loadNodeParam);
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

export function isModuleNodeData(data) {
  let resource = data ? data.resource : null;
  return data.type === 'node' || (resource && resource.indexOf(i18n.t('test_track.module.module')) > -1);
}

export function isCaseNodeData(data) {
  let resource = data ? data.resource : null;
  return data.type === 'case' || (resource && resource.indexOf(i18n.t('api_test.definition.request.case')) > -1);
}

export function isModuleNode(node) {
  return isModuleNodeData(node.data);
}

export function tagEditCheck(resourceName) {
  let minder = window.minder;
  let selectNodes = minder.getSelectedNodes();
  if (selectNodes && selectNodes.length > 0) {
    let type = selectNodes[0].data.type;
    if (type === 'case' || type === 'node') {// 已存在的模块和用例不能修改标签
      return false;
    }
    let parentIsModuleNode = isModuleNode(selectNodes[0].getParent());
    if (resourceName === i18n.t('api_test.definition.request.case') && !parentIsModuleNode) {
      return false;
    }
    // 父节点必须是模块
    if (resourceName === i18n.t('test_track.module.module') && !parentIsModuleNode) {
      return false;
    }
  }
  return true;
}

// 打了用例标签才能选择优先级
export function priorityDisableCheck() {
  let minder = window.minder;
  let selectNodes = minder.getSelectedNodes();
  if (selectNodes && selectNodes.length > 0) {
    let parentNode = selectNodes[0].getParent();
    let resource = parentNode ? parentNode.data.resource : null;
    if (resource && resource.indexOf(i18n.t('api_test.definition.request.case')) > -1) {
      return true;
    }
  }
  return false;
}

export function handleAfterSave(rootNode) {
  if (rootNode.data.newId) {
    rootNode.data.id = rootNode.data.newId;
    rootNode.data.newId = null;
  }
  rootNode.data.deleteChild = null;
  rootNode.data.changed = false;
  rootNode.data.contextChanged = false;
  rootNode.data.originId = null;
  if (isModuleNode(rootNode)) {
    rootNode.data.type = 'node';
  } else if (isCaseNodeData(rootNode.data)) {
    rootNode.data.type = 'case';
  }
  if (rootNode.children) {
    for (let i = 0; i < rootNode.children.length; i++) {
      handleAfterSave(rootNode.children[i]);
    }
  }
}

export function handleSaveError(rootNode) {
  if (rootNode.data.originId) {
    rootNode.data.id = rootNode.data.originId;
  }
  rootNode.data.originId = null;
  if (rootNode.children) {
    for (let i = 0; i < rootNode.children.length; i++) {
      handleSaveError(rootNode.children[i]);
    }
  }
}

export function handlePasteAfter(rootNode) {
  if (rootNode.data.type === 'tmp') {
    window.minder.removeNode(rootNode);
    return;
  }
  // 粘贴的节点视为已加载，不查询下面的用例
  rootNode.data.loaded = true;
  if (rootNode.children) {
    for (let i = 0; i < rootNode.children.length; i++) {
      handlePasteAfter(rootNode.children[i]);
    }
  }
}

export function handlePasteTip(rootNode) {
  if (hasUnloadedNode(rootNode)) {
    $info(i18n.t('case.minder_paste_tip'));
  }
}

/**
 * 判断节点下面是否有没有加载过用例的模块节点
 * @param rootNode
 * @returns {boolean}
 */
export function hasUnloadedNode(rootNode) {
  if (isModuleNode(rootNode) && !rootNode.data.loaded) {
    return true;
  }
  if (rootNode.children) {
    for (let i = 0; i < rootNode.children.length; i++) {
      if (hasUnloadedNode(rootNode.children[i])) {
        return true;
      }
    }
  }
  return false;
}

export function getChildNodeId(rootNode, nodeIds) {
  //递归获取所有子节点ID
  if (rootNode.data.id) {
    if (rootNode.data.newId) {
      nodeIds.push(rootNode.data.newId);
    } else {
      nodeIds.push(rootNode.data.id);
    }
  }
  if (rootNode.children) {
    for (let i = 0; i < rootNode.children.length; i++) {
      getChildNodeId(rootNode.children[i], nodeIds);
    }
  }
}

export function getSelectedNode() {
  return window.minder ? window.minder.getSelectedNode() : null;
}

export function getSelectedNodeData() {
  let node = getSelectedNode();
  return node ? node.data : {};
}

export function addIssueHotBox(vueObj) {
  let hotbox = window.minder.hotbox;
  let main = hotbox.state('main');
  main.button({
    position: 'ring',
    label: i18n.t('test_track.case.relate_issue'),
    key: 'N',
    action: function () {
      if (getSelectedNodeData().id.length < 15) {
        $warning("请先保存用例");
        return;
      }
      vueObj.$refs.issueRelate.open();
    },
    enable: function () {
      return isCaseNodeData(getSelectedNodeData());
    },
    beforeShow: function () {
    }
  });

  main.button({
    position: 'ring',
    label: i18n.t('test_track.issue.add_issue'),
    key: 'M',
    action: function () {
      if (getSelectedNodeData().id.length < 15) {
        $warning("请先保存用例");
        return;
      }
      vueObj.$refs.issueEdit.open();
    },
    enable: function () {
      return isCaseNodeData(getSelectedNodeData());
    },
    beforeShow: function () {
    }
  });
}

export function handleMinderIssueDelete(commandName, isPlan) {
  if (commandName.toLocaleLowerCase() === 'removenode') {
    let nodes = getSelectedNodes();
    if (nodes && nodes.length > 0) {
      let isAllIssue = true;
      let promises = [];
      nodes.forEach(node => {
        let data = node.data;
        if (data.type === 'issue') {
          let caseResourceId = node.parent.data.id;
          let p = new Promise((resolve) => {
            deleteIssueRelate({
              id: data.id,
              caseResourceId,
              isPlanEdit: isPlan,
              workspaceId: getCurrentWorkspaceId()
            }).then(() => {
              resolve();
            });
          });
          promises.push(p);
        } else {
          isAllIssue = false;
        }
      });
      if (promises.length > 0)
        Promise.all(promises).then(() => {
          $success(i18n.t('test_track.case.minder_issue_delete_tip'));
        });
      return isAllIssue;
    }
  }
  return false;
}


export function openMinderConfirm(vueObj, activeDom, permission) {
  if (permission && !hasPermission(permission)) {
    vueObj.activeDom = activeDom;
    return;
  }
  let isTestCaseMinderChanged = useStore().isTestCaseMinderChanged;
  if (vueObj.activeDom !== 'left' && activeDom === 'left' && isTestCaseMinderChanged) {
    if (vueObj.planStatus !=='Archived') {
      vueObj.$refs.isChangeConfirm.open();
      vueObj.tmpActiveDom = activeDom;
    } else {
      vueObj.activeDom = activeDom;
    }
    return;
  }
  vueObj.activeDom = activeDom;
}

export function saveMinderConfirm(vueObj, isSave) {
  if (isSave) {
    vueObj.$refs.minder.save(window.minder.exportJson());
  }
  useStore().isTestCaseMinderChanged = false;
  vueObj.$nextTick(() => {
    if (vueObj.tmpActiveDom) {
      vueObj.activeDom = vueObj.tmpActiveDom;
      vueObj.tmpActiveDom = null;
    }
    if (vueObj.tmpPath) {
      vueObj.$router.push({
        path: vueObj.tmpPath
      });
      vueObj.tmpPath = null;
    }
  });
}
