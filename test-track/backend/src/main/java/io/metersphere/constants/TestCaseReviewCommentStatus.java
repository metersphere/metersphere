package io.metersphere.constants;

public enum TestCaseReviewCommentStatus {
    Pass,
    UnPass,
    // 重新提审，可能是强制变更，也可能是自动重新提审
    Again,
    // 通过标准发生变更，评论内容记录的是之前的通过标准，便于留痕
    RuleChange,
    // 强制变更为通过
    ForceUnPass,
    // 强制变更为不通过
    ForcePass,
    // 当用例状态发生变更时，记录一下变更前的用例状态，便于留痕
    StatusChange
}
