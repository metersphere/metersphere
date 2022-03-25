
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

/**
 * 该比较方法认为 1，2 -> 2，1 版本是有改变的
 * @param  oldDom 被比较的节点
 * @param  newDom 比较的节点
 */
export function diff (oldDom, newDom,oldColor,newColor) {
  let diffNode = {
    oldNodeArray:[],
    nodeArray:[],
  }
  //两个独立的节点传过来 首先单独比较一下他们最外层的 dom的_vnode 如果 _vnode 相同 则认为最外层节点相同，否则更改样式 return
  let oldVnode = oldDom._vnode;
  let newVnode = newDom._vnode;

  if (isUndef(newVnode)) { //如果新节点不存在
    //如果旧节点存在， 旧节点加背景颜色
    if (isDef(oldVnode)) {
      diffNode.oldNodeArray.push(oldVnode.elm)
    }
    return
  }
  if (isUndef(oldVnode)) {//如果旧节点不存在
    //如果新节点存在， 新节点加背景颜色
    if (isDef(newVnode)) {
      diffNode.nodeArray.push(newVnode.elm)
    }
    return
  }

  if (sameVnode(oldVnode, newVnode)) {
    //逐层比较节点 这里涉及到 children 的length 可能不同的情况
    diffDetail(oldVnode, newVnode,diffNode)

  } else {
    diffNode.oldNodeArray.push(oldVnode.elm)
    diffNode.nodeArray.push(newVnode.elm)
  }

  changeStyle(diffNode,oldColor,newColor);
}

function changeStyle(diffNode,oldColor,newColor){
  /*console.log("查看结果");
  console.log(diffNode.oldNodeArray);
  console.log(diffNode.nodeArray);*/
  for (let i = 0; i < diffNode.oldNodeArray.length; i++) {
    if(diffNode.oldNodeArray[i]==='comment'||diffNode.oldNodeArray[i].nodeName==="#comment"){
      continue
    }
    if(diffNode.oldNodeArray[i].className==='cell'){
      let rowVnodeElm = findRowVnodeElm(diffNode.oldNodeArray[i]);
      if(isDef(rowVnodeElm.style)){
        rowVnodeElm.style.setProperty("background-color",oldColor,'important')
      }else if(isDef(rowVnodeElm.parentNode.style)&&rowVnodeElm!=='comment'){
        rowVnodeElm.parentNode.style.setProperty("background-color",oldColor,'important')
      }
    }else{
      changeStyleBySubset(diffNode.oldNodeArray[i],oldColor);
    }
  }

  for (let i = 0; i < diffNode.nodeArray.length; i++) {
    if(diffNode.nodeArray[i]==='comment'||diffNode.nodeArray[i].nodeName==="#comment"){
      continue
    }
    if(diffNode.nodeArray[i].className==='cell'){
      let rowVnodeElm = findRowVnodeElm(diffNode.nodeArray[i]);
      if(isDef(rowVnodeElm.style)){
        rowVnodeElm.style.setProperty("background-color",newColor,'important')
      }else if(isDef(rowVnodeElm.parentNode.style)&&rowVnodeElm!=='comment'){
        rowVnodeElm.parentNode.style.setProperty("background-color",newColor,'important')
      }
    }else{
      changeStyleBySubset(diffNode.nodeArray[i],newColor);
    }
  }
}

function changeStyleBySubset(vnodeElm,color){
  //如果当前节点加了颜色，他的子节点也按照diff处理
  if(isDef(vnodeElm.children)&&vnodeElm.children.length>0){
    if(isDef(vnodeElm.style)){
      vnodeElm.style.setProperty("background-color",color,'important')
    }else if(isDef(vnodeElm.parentNode.style)&&vnodeElm!=='comment'){
      vnodeElm.parentNode.style.setProperty("background-color",color,'important')
    }
    for (let i = 0; i < vnodeElm.children.length; i++) {
      changeStyleBySubset(vnodeElm.children[i],color);
    }
  }else {
    if(isDef(vnodeElm.style)){
      vnodeElm.style.setProperty("background-color",color,'important')
    }else if(isDef(vnodeElm.parentNode.style)&&vnodeElm!=='comment'){
      vnodeElm.parentNode.style.setProperty("background-color",color,'important')
    }
  }
}

/**
 *
 * @param oldChildren Array<VNode>
 * @param newChildren Array<VNode>
 * @param diffNode
 */
function diffChildren(oldChildren,newChildren,diffNode,isCompareChildren){
  let oldLength = oldChildren.length;
  let newLength = newChildren.length;
  //如果isCompareChildren===true，证明是需要轮巡比较table内容是否有重复的,方法是比较每个元素的最子dom,tr的td的length是相等的；
  if(isCompareChildren===true){
    let oldIndexArray = [];
    let newIndexArray = [];
    //现在oldChildren，newChildren是tbody的所有数据，
    for (let i = 0; i < oldLength; i++) {
      for (let j = 0; j < newLength; j++) {
        let sameNode = {
          nodeArray:[],
        }
        //找每行的数据，但是行是确定的，这里应该直接比较每个tr的 children
        sameDetail(oldChildren[i],newChildren[j],sameNode)
        if(sameNode.nodeArray.length>0){
          //证明oldChildren[i]与 newChildren[j] 是相同的 内容
          if(oldIndexArray.indexOf(i) === -1){
            oldIndexArray.push(i);
          }
          if(newIndexArray.indexOf(j) === -1){
            newIndexArray.push(j);
          }
        }
      }
    }
    for (let j = 0; j < oldLength; j++) {
      if(oldIndexArray.indexOf(j)===-1){
        diffNode.oldNodeArray.push(oldChildren[j].elm);
      }
    }

    for (let j = 0; j < newLength; j++) {
      if(newIndexArray.indexOf(j)===-1){
        diffNode.nodeArray.push(newChildren[j].elm);
      }
    }

  }else {
    //取二者公共长度
    let childrenLength = Math.min(oldLength, newLength);
    for (let i = 0; i < childrenLength; i++) {
      let oldVnode = oldChildren[i]
      let newVnode = newChildren[i]
      diffDetail(oldVnode,newVnode,diffNode)
    }
    for (let i = childrenLength; i < oldLength; i++) {
      if(oldChildren[i]){
        diffNode.oldNodeArray.push(oldChildren[i].elm);
      }
    }
    for (let i = childrenLength; i < newLength; i++) {
      if(newChildren[i]){
        diffNode.nodeArray.push(newChildren[i].elm);
      }
    }
  }
}
function sameChildren(oldChildren,newChildren,sameNode){
  let oldLength = oldChildren.length;
  for (let i = 0; i < oldLength; i++) {
    let oldVnode = oldChildren[i]
    let newVnode = newChildren[i]
    sameDetail(oldVnode,newVnode,sameNode)
    if(sameNode.nodeArray.length===0){
      return;
    }
  }
}

function sameDetail(oldVnode,newVnode,sameNode){
  if(isDef(oldVnode.child)&&isDef(newVnode.child)){
    let ovnode = oldVnode.child._vnode;
    let nvnode = newVnode.child._vnode;
    sameDetail(ovnode,nvnode,sameNode)
    if(sameNode.nodeArray.length===0){
      return;
    }
  }
  if(isDef(oldVnode.children)&&isDef(newVnode.children)){
    sameChildren(oldVnode.children,newVnode.children,sameNode)
  }
  //剩最后的子节点的时候，分类型做判断，如果最后的子节点有一个不相同，sameNode就置空，
  if(isUndef(oldVnode.child)&&isUndef(newVnode.child)&&isUndef(oldVnode.children)&&isUndef(newVnode.children)){
    if(isDef(oldVnode.text)&&isDef(newVnode.text)){
      if(oldVnode.text===newVnode.text){
        sameNode.nodeArray.push(newVnode.elm);
      }else{
        sameNode.nodeArray = [];
      }
    }else if(isDef(oldVnode.tag)&&isDef(newVnode.tag)){
      if(oldVnode.tag==='input'&&newVnode.tag==='input'){
        if(oldVnode.elm.value===newVnode.elm.value){
          if(oldVnode.elm.checked!==undefined&&newVnode.elm.checked!==undefined){
            if(oldVnode.elm.checked===newVnode.elm.checked){
              sameNode.nodeArray.push(newVnode.elm);
            }else {
              sameNode.nodeArray = [];
            }
          }else {
            sameNode.nodeArray.push(newVnode.elm);
          }
        }else{
          sameNode.nodeArray = [];
        }
      }else if(oldVnode.tag==='textarea'&&newVnode.tag==='textarea'){
        if(oldVnode.elm.value===newVnode.elm.value){
          sameNode.nodeArray.push(newVnode.elm);
        }else {
          sameNode.nodeArray = [];
        }
      }
    }
    else {
      if(sameVnode(oldVnode,newVnode)){
        sameNode.nodeArray.push(newVnode.elm);
      }else{
        sameNode.nodeArray = [];
      }
    }
  }

}

function diffDetail(oldVnode,newVnode,diffNode){
  if(isDef(oldVnode.child)&&isUndef(newVnode.child)){
    diffNode.oldNodeArray.push(oldVnode.child._vnode.elm);
  }
  if(isDef(oldVnode.children)&&isUndef(newVnode.children)){
    diffNode.oldNodeArray.push(oldVnode.elm);
  }
  if(isUndef(oldVnode.child)&&isDef(newVnode.child)){
    diffNode.nodeArray.push(newVnode.child._vnode.elm);
  }
  if(isUndef(oldVnode.children)&&isDef(newVnode.children)){
    diffNode.nodeArray.push(newVnode.elm);
  }
  if(isDef(oldVnode.child)&&isDef(newVnode.child)){
    let ovnode = oldVnode.child._vnode;
    let nvnode = newVnode.child._vnode;
    diffDetail(ovnode,nvnode,diffNode)
  }
  if(isDef(oldVnode.children)&&isDef(newVnode.children)){
    //处理节点数据结构为table的情况
    let isCompareChildren = false;
    if(oldVnode.tag==='tbody'&&newVnode.tag==='tbody'){
      isCompareChildren = true;
    }else
    if(isDef(oldVnode.elm.className)&&isDef(newVnode.elm.className)){
      if(oldVnode.elm.className==='el-collapse'&&newVnode.elm.className==='el-collapse'){
        isCompareChildren = true;
      }
    }
    diffChildren(oldVnode.children,newVnode.children,diffNode,isCompareChildren)
  }
  //剩最后的子节点的时候，分类型做判断(注意，最后的子节点的真实dom里可能还有dom节点)
  if(isUndef(oldVnode.child)&&isUndef(newVnode.child)&&isUndef(oldVnode.children)&&isUndef(newVnode.children)){
    //最子节点比较结果
    let childDiff=[];
    diffRealNode(oldVnode.elm,newVnode.elm,diffNode,childDiff);
    if(childDiff.length===0){
      diffNodeContext(oldVnode,newVnode,diffNode)
    }
    if(isDef(oldVnode.text)&&isDef(newVnode.text)){
        if(oldVnode.text!==newVnode.text){
          diffNode.oldNodeArray.push(oldVnode.elm);
          diffNode.nodeArray.push(newVnode.elm);
        }
    }
    else if(isDef(oldVnode.tag)&&isDef(newVnode.tag)){
      if(oldVnode.tag==='input'&&newVnode.tag==='input'){
        if(oldVnode.elm.value!==newVnode.elm.value){
          diffNode.oldNodeArray.push(oldVnode.elm);
          diffNode.nodeArray.push(newVnode.elm);
        }else {
          if(oldVnode.elm.checked!==undefined&&newVnode.elm.checked!==undefined){
            if(oldVnode.elm.checked!==newVnode.elm.checked){
              diffNode.oldNodeArray.push(oldVnode.elm);
              diffNode.nodeArray.push(newVnode.elm);
            }
          }
        }
      }else if(oldVnode.tag==='textarea'&&newVnode.tag==='textarea'){
        if(oldVnode.elm.value!==newVnode.elm.value){
          diffNode.oldNodeArray.push(oldVnode.elm);
          diffNode.nodeArray.push(newVnode.elm);
        }
      }
    }
    else {
      if(!sameVnode(oldVnode,newVnode)){
        diffNode.oldNodeArray.push(oldVnode.elm);
        diffNode.nodeArray.push(newVnode.elm);
      }
    }
  }

}


function diffNodeContext(oldNode,newNode,diffNode){
  if(isDef(oldNode.context)&&isDef(newNode.context)){
    if(isDef(oldNode.context.value)||isDef(newNode.context.value)){
      if(isDef(oldNode.context.value)&&isDef(newNode.context.value)){
        if(oldNode.context.value!==newNode.context.value){
          diffNode.oldNodeArray.push(oldNode.elm);
          diffNode.nodeArray.push(newNode.elm);
        }
      }else{
        diffNode.oldNodeArray.push(oldNode.elm);
        diffNode.nodeArray.push(newNode.elm);
      }
    }
  }
}

function diffRealNode(oldNode,newNode,diffNode,childDiff){
  let oldNodeLength = oldNode.childNodes.length;
  let newNodeLength = newNode.childNodes.length;
  let childrenLength = Math.min(oldNodeLength, newNodeLength);
  for (let i = 0; i < childrenLength; i++) {
    let oldnode = oldNode.childNodes[i]
    let newnode = newNode.childNodes[i]
    if(oldnode.childNodes.length>0&&newnode.childNodes.length>0){
      diffRealNode(oldnode,newnode,diffNode,childDiff);
    }else {
      let _isSameChild = diffRealNodeDetail(oldnode,newnode,diffNode);
      if(!_isSameChild){
        childDiff.push(_isSameChild)
      }
    }
  }
  for (let i = childrenLength; i < oldNodeLength; i++) {
    if(oldNode.childNodes[i]){
      if(isDef(oldNode.childNodes[i].data)){
        if(oldNode.childNodes[i].data!=="\n"){
          diffNode.oldNodeArray.push(oldNode.childNodes[i]);
        }
      }else {
        diffNode.oldNodeArray.push(oldNode.childNodes[i]);
      }
    }
  }
  for (let i = childrenLength; i < newNodeLength; i++) {
    if(newNode.childNodes[i]){
      if(isDef(newNode.childNodes[i].data)){
        if(newNode.childNodes[i].data!=="\n"){
          diffNode.nodeArray.push(newNode.childNodes[i]);
        }
      }else {
        diffNode.nodeArray.push(newNode.childNodes[i]);
      }
    }
  }
}

function diffRealNodeDetail(oldNode,newNode,diffNode){
  if(!sameNode(oldNode,newNode)){
    if(isDef(oldNode.data)){
      if(oldNode.data!=="\n"){
        diffNode.oldNodeArray.push(oldNode);
      }
    }else {
      diffNode.oldNodeArray.push(oldNode);
    }
    if(isDef(newNode.data)){
      if(newNode.data!=="\n"){
        diffNode.nodeArray.push(newNode);
      }
    }else {
      diffNode.nodeArray.push(newNode);
    }
  }else{
    //如果是相同的，但是这时候新旧节点有一个的length一定为0，所以要处理剩下的
    if(oldNode.childNodes.length>0){
      for (let i = 0; i < oldNode.childNodes.length; i++){
        if(isDef(oldNode.childNodes[i].data)){
          if(oldNode.childNodes[i].data!=="\n"){
            diffNode.oldNodeArray.push(oldNode.childNodes[i]);
          }
        }else {
          diffNode.oldNodeArray.push(oldNode.childNodes[i]);
        }
      }
    }
    if(newNode.childNodes.length>0){
      for (let i = 0; i < newNode.childNodes.length; i++){
        if(isDef(newNode.childNodes[i].data)){
          if(newNode.childNodes[i].data!=="\n"){
            diffNode.nodeArray.push(newNode.childNodes[i]);
          }
        }else {
          diffNode.nodeArray.push(newNode.childNodes[i]);
        }
      }
    }
    //如果他们都是0，证明比较到这时，都是相同的
    if(oldNode.childNodes.length===0&&newNode.childNodes.length===0){
      return true;
    }
  }
  return false;
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

function sameNode (a, b) {
  return (
    (isDef(a.data) === isDef(b.data) &&a.data===b.data&&isDef(a.nodeValue) === isDef(b.nodeValue) &&a.nodeValue===b.nodeValue)||(
      isUndef(a.data)&&isUndef(b.data)&&isUndef(a.nodeValue)&&isUndef(b.nodeValue)&&
      isDef(a.textContent) === isDef(b.textContent)&&a.textContent===b.textContent
    )
  )
}

function findRowVnodeElm(nodeElm){
  if(nodeElm.localName==="td"||nodeElm.className==="cell"){
    return findRowVnodeElm(nodeElm.parentNode)
  }else if(nodeElm.localName==="tr"){
    return nodeElm;
  }else {
    return nodeElm;
  }
}


function sameInputType (a, b) {
  if (a.tag !== 'input') return true
  let i
  const typeA = isDef(i = a.data) && isDef(i = i.attrs) && i.type
  const typeB = isDef(i = b.data) && isDef(i = i.attrs) && i.type
  return typeA === typeB || isTextInputType(typeA) && isTextInputType(typeB)
}

