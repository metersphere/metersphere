export enum MinderEventName {
  'DELETE_NODE' = 'DELETE_NODE', // 删除节点
  'HOTBOX' = 'HOTBOX', // 热键菜单
  'ENTER_NODE' = 'ENTER_NODE', // 进入节点
  'EXPAND' = 'EXPAND', // 展开节点
  'COLLAPSE' = 'COLLAPSE', // 折叠节点
  'INSERT_CHILD' = 'INSERT_CHILD', // 插入子节点
  'INSERT_SIBLING' = 'INSERT_SIBLING', // 插入同级节点
  'COPY_NODE' = 'COPY_NODE', // 复制节点
  'PASTE_NODE' = 'PASTE_NODE', // 粘贴节点
  'CUT_NODE' = 'CUT_NODE', // 剪切节点
  'SET_TAG' = 'SET_TAG', // 设置节点标签
  'NODE_SELECT' = 'NODE_SELECT', // 选中节点
  'NODE_UNSELECT' = 'NODE_UNSELECT', // 取消选中节点
  'VIEW_CHANGE' = 'VIEW_CHANGE', // 脑图视图移动
  'MINDER_CHANGED' = 'MINDER_CHANGED', // 脑图更改事件
  'SAVE_MINDER' = 'SAVE_MINDER', // 脑图保存事件
  'DRAG_FINISH' = 'DRAG_FINISH', // 脑图节点拖拽结束事件
  'DROPDOWN_SELECT' = 'DROPDOWN_SELECT', // 下拉菜单选中事件
}

export enum MinderKeyEnum {
  FEATURE_CASE_MINDER = 'featureCaseMinder',
  CASE_REVIEW_MINDER = 'caseReviewMinder',
  TEST_PLAN_MINDER = 'testPlanMinder',
  TEST_PLAN_FEATURE_CASE_MINDER = 'testPlanFeatureCaseMinder',
}

export enum ModeIcon {
  right = 'icon-icon_right_branch',
  default = 'icon-icon_left_and_right_branch',
  filetree = 'icon-icon_lower_branch1',
}

export default {};
