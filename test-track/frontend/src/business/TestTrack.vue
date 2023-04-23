<template>
  <el-col>
    <track-header-menus/>
    <div>
      <transition>
        <keep-alive>
          <router-view :baseUrl="baseUrl"/>
        </keep-alive>
      </transition>
    </div>
  </el-col>
</template>

<script>

import TrackHeaderMenus from "./head/TrackHeaderMenus";

export default {
  name: "TrackHome",
  components: {TrackHeaderMenus},
  data() {
    return {
      baseUrl: "track"
    }
  },
  activated() {
    this.$refs.table.doLayout()
  }
}
</script>


<style scoped>
.node-tree {
  margin: 3%;
}
</style>

<style>
/**
 * Element-UI Common Style
 * Use By Test-Track Since v2.6
 */

/**
 * el-input__inner 字体
 */
input.el-input__inner {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #1F2329;
}

/**
 * el-message
 */
.el-message-box__header {
  padding: 24px 24px 0 24px;
  min-height: 24px;
}

.el-message-box__title {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 500;
  font-size: 16px;
  line-height: 24px;
  color: #1F2329;
  flex: none;
  order: 0;
  flex-grow: 0;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  max-width: 340px;
}

.el-message-box__content {
  padding: 24px 0px 0px 24px;
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #1F2329;
  flex: none;
  order: 0;
  flex-grow: 0;
}

.el-message-box {
  width: 420px;
  height: 174px;
  padding-bottom: 0;
}

.el-message-box__btns {
  padding: 24px 24px 24px 0px;
}

.el-message-box__btns button:nth-child(2) {
  margin-left: 12px;
}

.custom-confirm-delete .el-message-box__btns button:nth-child(2) {
  background-color: #F54A45!important;
  border: none;
  border-radius: 4px;
}

.custom-confirm-delete .el-message-box__btns button span{
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
}

.el-message-box__btns .el-button--small {
  min-width: 80px;
}

button.el-button.el-button--default.el-button--small {
  color: #1F2329;
}

button.el-button.el-button--default.el-button--small.el-button--primary {
  color: #FFFFFF!important;
}

button.el-button.el-button--default.el-button--small.el-button--danger {
  color: #FFFFFF;
}

/* el-message */

/*warning*/
.el-message--warning {
  background: linear-gradient(0deg, rgba(255, 136, 0, 0.08), rgba(255, 136, 0, 0.08)), #FFFFFF;
  border: 1px solid #FF8800;
  box-shadow: 0px 4px 8px rgb(31 35 41 / 10%);
  border-radius: 4px;
  flex: none;
  order: 0;
  flex-grow: 0;
  box-sizing: border-box;
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 16px 20px;
  height: 54px;
  min-width: 100px;
}

.el-message--success {
  background: linear-gradient(0deg, rgba(52, 199, 36, 0.08), rgba(52, 199, 36, 0.08)), #FFFFFF;
  border: 1px solid #34C724;
  box-shadow: 0px 4px 8px rgba(31, 35, 41, 0.1);
  border-radius: 4px;
  flex: none;
  order: 0;
  flex-grow: 0;
  box-sizing: border-box;
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 16px 20px;
  height: 54px;
  min-width: 100px;
}

.el-message--error {
  background: linear-gradient(0deg, rgba(245, 74, 69, 0.08), rgba(245, 74, 69, 0.08)), #FFFFFF;
  border: 1px solid #F54A45;
  box-shadow: 0px 4px 8px rgba(31, 35, 41, 0.1);
  border-radius: 4px;
  flex: none;
  order: 0;
  flex-grow: 0;
  box-sizing: border-box;
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 16px 20px;
  height: 54px;
  min-width: 100px;
}

.el-message__content {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 500;
  font-size: 14px;
  line-height: 22px;
  color: #1F2329!important;
  flex: none;
  order: 2;
  flex-grow: 0;
  width: 100%;
}

.el-message-box__headerbtn .el-message-box__close {
  font-size: 20px!important;
}
/**
 * el-button
 */
button.el-button.el-button--default.el-button--small {
  color: #1F2329;
}

button.el-button.el-button--default.el-button--small:hover {
  color: #1F2329;
  border-color: #DCDFE6;
  background-color: whitesmoke;
}

button.el-button.el-button--default.el-button--small.el-button--primary {
  color: #FFFFFF!important;
}

button.el-button.el-button--default.el-button--small.el-button--primary:hover {
  color: #FFFFFF!important;
  background: rgb(147, 96, 159);
  border-color: rgb(147, 96, 159);
}

button.el-button.el-button--default.el-button--small.el-button--danger {
  color: #FFFFFF;
}

/* el-dialog */
.el-dialog {
  padding: 24px;
}

.el-dialog__header {
  padding: 0px!important;
  text-align: left!important;
}

.el-dialog__title {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 500;
  font-size: 16px;
  line-height: 24px;
  color: #1F2329;
  flex: none;
  order: 0;
  flex-grow: 0;
}

.el-dialog__body {
  padding: 12px 0px 6px 0px;
}

.el-dialog__footer {
  padding: 0px;
}

i.el-dialog__close.el-icon.el-icon-close:before {
  font-size: 20px!important;
}

/**
 * el-icon
 */
.el-icon-warning:before, .el-icon-success:before {
  font-size: 17px;
}


/**
 el-tag
 */
span.el-tag.el-tag--info.el-tag--mini.el-tag--light, span.el-tag.el-tag--info.el-tag--small.el-tag--light {
  flex-direction: row;
  align-items: center;
  padding: 1px 6px;
  gap: 4px;
  height: 24px;
  background: rgba(31, 35, 41, 0.1);
  border-radius: 2px;
  flex: none;
  flex-grow: 0;
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  position: relative;
}

span.el-tag.el-tag--info.el-tag--mini.el-tag--light span{
  display: inline-block;
  max-width: 500px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: rgb(31, 35, 41);
}

span.el-tag.el-tag--info.el-tag--small.el-tag--light span{
  display: inline-block;
  max-width: 500px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: rgb(31, 35, 41);
}

/**
 * el-tag-icon-close
 */
i.el-tag__close.el-icon-close {
  height: 0;
  position: relative;
  top: -11px!important;
  left: 1px!important;
}

.el-tag__close.el-icon-close::before {
  font-size: 28px;
}

.el-tag.el-tag--info .el-tag__close:hover {
  color: #1F2329;
  background-color: #909399;
}


/**
 * 统一测试跟踪MsInputTag样式, 交互改造后去掉
 */
span.ms-top.el-tag.el-tag--info.el-tag--small.el-tag--light {
  flex-direction: row;
  align-items: center;
  padding: 1px 6px;
  gap: 4px;
  height: 24px;
  background: rgba(31, 35, 41, 0.1);
  border-radius: 2px;
  flex: none;
  flex-grow: 0;
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  position: relative;
  top: 6px;

}

span.ms-top.el-tag.el-tag--info.el-tag--small.el-tag--light span{
  display: inline-block;
  max-width: 500px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: rgb(31, 35, 41);
}

.ms-top.el-tag .el-tag__close.el-icon-close::before {
  font-size: 24px;
  position: relative;
  top: -3px;
}
</style>

