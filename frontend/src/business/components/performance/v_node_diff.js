/* @flow */

const _toString = Object.prototype.toString

const hooks = ['create', 'activate', 'update', 'remove', 'destroy']
// These helpers produce better VM code in JS engines due to their
// explicitness and function inlining.
function isUndef (v) {
  return v === undefined || v === null
}

function isDef (v){
  return v !== undefined && v !== null
}

function isTrue (v){
  return v === true
}

function makeMap (str, expectsLowerCase){
  const map = Object.create(null)
  const list= str.split(',')
  for (let i = 0; i < list.length; i++) {
    map[list[i]] = true
  }
  return expectsLowerCase
    ? val => map[val.toLowerCase()]
    : val => map[val]
}

const isTextInputType = makeMap('text,number,password,search,email,tel,url')

const diffNode = {
  oldNodeArray:[],
  nodeArray:[],
}

function createKeyToOldIdx (children, beginIdx, endIdx) {
  let i, key
  const map = {}
  for (i = beginIdx; i <= endIdx; ++i) {
    key = children[i].key
    if (isDef(key)) map[key] = i
  }
  return map
}

function isPatchable (vnode) {
  while (vnode.componentInstance) {
    vnode = vnode.componentInstance._vnode
  }
  return isDef(vnode.tag)
}


function updateChildren (parentElm, oldCh, newCh, insertedVnodeQueue, removeOnly) {
  //先递归，按顺序拍，如果不同就是不同，不考虑别的

  /*let oldStartIdx = 0
  let newStartIdx = 0
  let oldEndIdx = oldCh.length - 1
  let oldStartVnode = oldCh[0]
  let oldEndVnode = oldCh[oldEndIdx]
  let newEndIdx = newCh.length - 1
  let newStartVnode = newCh[0]
  let newEndVnode = newCh[newEndIdx]
  let oldKeyToIdx, idxInOld, vnodeToMove, refElm

  while (oldStartIdx <= oldEndIdx && newStartIdx <= newEndIdx) {
    if (isUndef(oldStartVnode)) {
      oldStartVnode = oldCh[++oldStartIdx] // Vnode has been moved left
    } else if (isUndef(oldEndVnode)) {
      oldEndVnode = oldCh[--oldEndIdx]
    } else if (sameVnode(oldStartVnode, newStartVnode)) {
      patchVnode(oldStartVnode, newStartVnode, insertedVnodeQueue, newCh, newStartIdx)
      oldStartVnode = oldCh[++oldStartIdx]
      newStartVnode = newCh[++newStartIdx]
    } else if (sameVnode(oldEndVnode, newEndVnode)) {
      patchVnode(oldEndVnode, newEndVnode, insertedVnodeQueue, newCh, newEndIdx)
      oldEndVnode = oldCh[--oldEndIdx]
      newEndVnode = newCh[--newEndIdx]
    } else if (sameVnode(oldStartVnode, newEndVnode)) { // Vnode moved right
      patchVnode(oldStartVnode, newEndVnode, insertedVnodeQueue, newCh, newEndIdx)
      //canMove && nodeOps.insertBefore(parentElm, oldStartVnode.elm, nodeOps.nextSibling(oldEndVnode.elm))
      oldStartVnode = oldCh[++oldStartIdx]
      newEndVnode = newCh[--newEndIdx]
    } else if (sameVnode(oldEndVnode, newStartVnode)) { // Vnode moved left
      patchVnode(oldEndVnode, newStartVnode, insertedVnodeQueue, newCh, newStartIdx)
     // canMove && nodeOps.insertBefore(parentElm, oldEndVnode.elm, oldStartVnode.elm)
      oldEndVnode = oldCh[--oldEndIdx]
      newStartVnode = newCh[++newStartIdx]
    } else {
      if (isUndef(oldKeyToIdx)) oldKeyToIdx = createKeyToOldIdx(oldCh, oldStartIdx, oldEndIdx)
      idxInOld = isDef(newStartVnode)
        ? oldKeyToIdx[newStartVnode]
        : findIdxInOld(newStartVnode, oldCh, oldStartIdx, oldEndIdx)
      if (isUndef(idxInOld)) { // New element
        diffNode.nodeArray.push(newStartVnode);
       // createElm(newStartVnode, insertedVnodeQueue, parentElm, oldStartVnode.elm, false, newCh, newStartIdx)
      } else {
        vnodeToMove = oldCh[idxInOld]
        if (sameVnode(vnodeToMove, newStartVnode)) {
          patchVnode(vnodeToMove, newStartVnode, insertedVnodeQueue, newCh, newStartIdx)
          diffNode.oldNodeArray.push(vnodeToMove);
         // oldCh[idxInOld] = undefined
          //canMove && nodeOps.insertBefore(parentElm, vnodeToMove.elm, oldStartVnode.elm)
        } else {
          diffNode.nodeArray.push(newStartVnode);
          diffNode.oldNodeArray.push(vnodeToMove);
          // same key but different element. treat as new element
          //createElm(newStartVnode, insertedVnodeQueue, parentElm, oldStartVnode.elm, false, newCh, newStartIdx)
        }
      }

      newStartVnode = newCh[++newStartIdx]
    }
  }
  if (oldStartIdx > oldEndIdx) {//如果 oldStartIdx > oldEndIdx，说明老节点比对完了，但是新节点还有多的
    refElm = isUndef(newCh[newEndIdx + 1]) ? null : newCh[newEndIdx + 1]
    diffNode.nodeArray.push(refElm);
    //  addVnodes(parentElm, refElm, newCh, newStartIdx, newEndIdx, insertedVnodeQueue)
  } else if (newStartIdx > newEndIdx) {
    // 如果 newStartIdx > newEndIdx 条件，说明新节点比对完了，老节点还有多
    refElm = isUndef(oldCh[oldEndIdx + 1]) ? null : oldCh[oldEndIdx + 1]
    diffNode.oldNodeArray.push(refElm);
    // removeVnodes(oldCh, oldStartIdx, oldEndIdx)
  }*/
}



function sameVnode (a, b) {
  return (
    a.nodeName===b.nodeName&&(sameInputType(a, b))
  )
}

function sameInputType (a, b) {
  if(a.localName === 'input'){
    return a.value===b.value
  }else {
    return  true
  }
}

function patchVnode ( oldVnode, vnode, insertedVnodeQueue, ownerArray, index, removeOnly) {

  if(sameVnode(vnode,oldVnode)){
    return
  }else {
    diffNode.oldNodeArray.push(oldVnode);
    diffNode.nodeArray.push(vnode)
  }

  const oldCh = oldVnode.childNodes
  const ch = vnode.childNodes

  if (isDef(oldCh) && isDef(ch)) {//新旧子节点都存在 查找新旧子节点的差异
    if (oldCh !== ch) updateChildren(null, oldCh, ch, null, null)
  } else if (isDef(ch)) {

    //如果只有新的子节点存在，旧节点为文本类型节点，直接放入差异数组里
    if (isUndef(oldVnode.style)) {
      diffNode.oldNodeArray.push(oldVnode);
    }
  } else if (isDef(oldCh)) {
    //如果旧的子节点存在，新的没有，记录旧的子节点的dom
    diffNode.oldNodeArray.push(oldCh);
    //diffNode.nodeArray.push(ch.elm)
  } else if (isUndef(oldVnode.style)) {
    //新旧节点的子节点都不存在，若旧节点为文本节点，记录旧的子节点的dom
    diffNode.oldNodeArray.push(oldVnode);
  }

  return diffNode;
}

export function patch (oldVnode, vnode) {
  if (isUndef(vnode)) { //如果新节点不存在
                        //如果旧节点存在， 旧节点加背景颜色
    if (isDef(oldVnode)) {
      oldVnode.style.backgroundColor = "rgb(241,200,196)";
    }
    return
  }
  if (isUndef(oldVnode)) {//如果旧节点不存在
    //如果新节点存在， 新节点加背景颜色
    if (isDef(vnode)) {
      oldVnode.style.backgroundColor = "rgb(215, 243, 215)";
    }
    return
  }
  //如果存在 开始比较,首先我默认传过来的node就是初始的父节点，所以只比较nodeName,如果父节点就不相同，那么就是不一样的
  if(oldVnode.nodeName!==vnode.nodeName){
    oldVnode.style.backgroundColor = "rgb(241,200,196)";
    vnode.style.backgroundColor = "rgb(215, 243, 215)";
    return;
  }
  //如果相同，查看是否有子节点，没有直接返回
  if(oldVnode.childNodes.length>0&&vnode.childNodes.length>0){
    updateChildren(null, oldVnode.childNodes, vnode.childNodes, null, null)
    for (let i = 0; i <diffNode.oldNodeArray.length; i++) {
      diffNode.oldNodeArray[i].parentNode.style.backgroundColor = "rgb(241,200,196)";
    }
    for (let i = 0; i <diffNode.nodeArray.length; i++) {
      diffNode.nodeArray[i].parentNode.style.backgroundColor  = "rgb(215, 243, 215)";
    }

  }else if(oldVnode.childNodes.length>0){
    for (let i = 0; i < oldVnode.childNodes.length; i++) {
       const oldCh = oldVnode.childNodes[i];
       if(oldCh.style){
         oldCh.style.backgroundColor = "rgb(241,200,196)";
       }
    }
  }else if(vnode.childNodes.length>0){
    for (let i = 0; i < vnode.childNodes.length; i++) {
      const ch = vnode.childNodes[i];
      if(ch.style){
        ch.style.backgroundColor = "rgb(215, 243, 215)";
      }
    }
  }
}

function findIdxInOld (node, oldCh, start, end) {
  for (let i = start; i < end; i++) {
    const c = oldCh[i]
    if (isDef(c) && sameVnode(node, c)) return i
  }
}



