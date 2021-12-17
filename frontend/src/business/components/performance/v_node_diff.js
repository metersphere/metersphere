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

/**
 * 该比较方法认为 1，2 -> 2，1 版本是有改变的
 * @param  oldDom 被比较的节点
 * @param  newDom 比较的节点
 */
export function diff (oldDom, newDom) {
  let diffNode = {
    oldNodeArray:[],
    nodeArray:[],
  }
  // 被比较的节点的 $options 包含 _parentVnode(VNode)   _renderChildren(Array<VNode>)
  // 比较的节点的 $options 包含 _parentVnode(VNode)   _renderChildren(Array<VNode>)
  //两个独立的节点传过来 首先单独比较一下他们最外层的 dom 如果 dom.nodeName 相同 则认为最外层节点相同，否则更改样式 return
  let oldVnode = oldDom._vnode;
  let newVnode = newDom._vnode;

  if (isUndef(newVnode)) { //如果新节点不存在
    //如果旧节点存在， 旧节点加背景颜色
    if (isDef(oldVnode)) {
      diffNode.oldNodeArray.push(oldVnode.elm)
      //oldVnode.elm.style.backgroundColor = "rgb(241,200,196)";
    }
    return
  }
  if (isUndef(oldVnode)) {//如果旧节点不存在
    //如果新节点存在， 新节点加背景颜色
    if (isDef(newVnode)) {
      diffNode.nodeArray.push(newVnode.elm)
      //newVnode.elm.style.backgroundColor = "rgb(215, 243, 215)";
    }
    return
  }

  if (sameVnode(oldVnode, newVnode)) {
    //逐层比较节点 这里涉及到 children 的length 可能不同的情况
    diffDetail(oldVnode, newVnode,diffNode)

  } else {
    diffNode.oldNodeArray.push(oldVnode.elm)
    diffNode.nodeArray.push(newVnode.elm)
    //oldVnode.elm.style.backgroundColor = "rgb(241,200,196)";
    //newVnode.elm.style.backgroundColor = "rgb(215, 243, 215)";
  }
  changeStyle(diffNode);
}

function changeStyle(diffNode){
  for (let i = 0; i < diffNode.oldNodeArray.length; i++) {
    if(diffNode.oldNodeArray[i]==='comment'||isUndef(diffNode.oldNodeArray[i].style)){
      continue
    }
    diffNode.oldNodeArray[i].style.setProperty("background-color","rgb(241,200,196)",'important')
  }

  for (let i = 0; i < diffNode.nodeArray.length; i++) {
    if(diffNode.nodeArray[i]==='comment'||isUndef(diffNode.nodeArray[i].style)){
      continue
    }
    diffNode.nodeArray[i].style.setProperty("background-color","rgb(215, 243, 215)",'important')
  }
}

/**
 *
 * @param oldChildren Array<VNode>
 * @param newChildren Array<VNode>
 * @param diffNode
 */
function diffChildren(oldChildren,newChildren,diffNode){
  let oldLength = oldChildren.length;
  let newLength = newChildren.length;
  //取二者公共长度
  let childrenLength = Math.min(oldLength, newLength);
  for (let i = 0; i < childrenLength; i++) {
      let oldVnode = oldChildren[i]
      let newVnode = newChildren[i]
      diffDetail(oldVnode,newVnode,diffNode)
  }
  for (let i = childrenLength; i < (oldLength - childrenLength); i++) {
    if(oldChildren[i]){
      diffNode.oldNodeArray.push(oldChildren[i].elm);
      //oldChildren[i].elm.style.backgroundColor = "rgb(241,200,196)";
    }
  }
  for (let i = childrenLength; i < (newLength - childrenLength); i++) {
    if(newChildren[i]){
      diffNode.nodeArray.push(newChildren[i].elm);
      //newChildren[i].elm.style.backgroundColor = "rgb(215, 243, 215)";
    }
  }

}

function diffDetail(oldVnode,newVnode,diffNode){
  if(isDef(oldVnode.child)&&isUndef(newVnode.child)){
    //oldVnode.child._vnode.elm.style.backgroundColor = "rgb(241,200,196)";
    diffNode.oldNodeArray.push(oldVnode.child._vnode.elm);
  }
  if(isDef(oldVnode.children)&&isUndef(newVnode.children)){
    //oldVnode.elm.style.backgroundColor = "rgb(241,200,196)";
    diffNode.oldNodeArray.push(oldVnode.elm);
  }
  if(isUndef(oldVnode.child)&&isDef(newVnode.child)){
    //newVnode.child._vnode.elm.style.backgroundColor = "rgb(215, 243, 215)";
    diffNode.nodeArray.push(newVnode.child._vnode.elm);
  }
  if(isUndef(oldVnode.children)&&isDef(newVnode.children)){
    //newVnode.elm.style.backgroundColor = "rgb(215, 243, 215)";
    diffNode.nodeArray.push(newVnode.elm);
  }
  if(isDef(oldVnode.child)&&isDef(newVnode.child)){
    let ovnode = oldVnode.child._vnode;
    let nvnode = newVnode.child._vnode;
    diffDetail(ovnode,nvnode,diffNode)
  }
  if(isDef(oldVnode.children)&&isDef(newVnode.children)){
    diffChildren(oldVnode.children,newVnode.children,diffNode)
  }
  //剩最后的子节点的时候，分类型做判断
  if(isUndef(oldVnode.child)&&isUndef(newVnode.child)&&isUndef(oldVnode.children)&&isUndef(newVnode.children)){

    if(isDef(oldVnode.text)&&isDef(newVnode.text)){
        if(oldVnode.text!==newVnode.text){
          if(isDef(oldVnode.elm.style)){
            //oldVnode.elm.style.backgroundColor = "rgb(241,200,196)";
            diffNode.oldNodeArray.push(oldVnode.elm);
          }else {
           // oldVnode.elm.parentNode.style.backgroundColor = "rgb(241,200,196)";
            diffNode.oldNodeArray.push(oldVnode.elm.parentNode);
          }
          if(isDef(newVnode.elm.style)){
           // newVnode.elm.style.backgroundColor = "rgb(215, 243, 215)";
            diffNode.nodeArray.push(newVnode.elm);
          }else {
            diffNode.nodeArray.push(newVnode.elm.parentNode);
            //newVnode.elm.parentNode.style.backgroundColor = "rgb(215, 243, 215)";
          }

        }
    }else if(isDef(oldVnode.tag)&&isDef(newVnode.tag)){
      if(oldVnode.tag==='input'&&newVnode.tag==='input'){
        if(oldVnode.elm.value!==newVnode.elm.value){
          diffNode.oldNodeArray.push(oldVnode.elm);
          diffNode.nodeArray.push(newVnode.elm);
        }
      }
    }
    else {
      if(!sameVnode(oldVnode,newVnode)){
        //oldVnode.elm.style.backgroundColor = "rgb(241,200,196)";
        diffNode.oldNodeArray.push(oldVnode.elm);
        //newVnode.elm.style.backgroundColor = "rgb(215, 243, 215)";
        diffNode.nodeArray.push(newVnode.elm);
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



function sameVnode (a, b) {
  return (
    a.key === b.key &&
    a.asyncFactory === b.asyncFactory && (
      (
        a.tag === b.tag &&
        a.isComment === b.isComment &&
        isDef(a.data) === isDef(b.data) &&
        sameInputType(a, b)
      ) || (
        isTrue(a.isAsyncPlaceholder) &&
        isUndef(b.asyncFactory.error)
      )
    )
  )
}

function sameInputType (a, b) {
  if (a.tag !== 'input') return true
  let i
  const typeA = isDef(i = a.data) && isDef(i = i.attrs) && i.type
  const typeB = isDef(i = b.data) && isDef(i = i.attrs) && i.type
  return typeA === typeB || isTextInputType(typeA) && isTextInputType(typeB)
}

