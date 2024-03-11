// @ts-ignore @typescript-eslint/no-duplicate-enum-values
export enum StatusType {
  UN_REVIEWED = 'icon-icon_block_filled', // 未评审
  UNDER_REVIEWED = 'icon-icon_testing', // 评审中
  PASS = 'icon-icon_succeed_colorful', // 已通过
  UN_PASS = 'icon-icon_close_colorful', // 未通过
  RE_REVIEWED = 'icon-icon_resubmit_filled', // 重新提审
  UN_EXECUTED = 'icon-icon_block_filled', // 未执行
  PASSED = 'icon-icon_succeed_colorful', // 已执行
  FAILED = 'icon-icon_close_colorful', // 失败
  BLOCKED = 'icon-icon_block_filled', // 阻塞
  SKIPPED = 'icon-icon_skip_planarity', // 跳过
}

export default {};
