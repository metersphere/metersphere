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
/* Element-UI Common Style 暂时提取为测试跟踪公共使用*/

/*el-message*/
.el-message-box__header {
  padding: 20px 0 0 24px;
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
}

.el-message-box__content {
  padding: 35px 0px 0px 24px;
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

.el-button--small span {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  position: relative;
  top: -5px;
}

/* el-button */
.el-button {
  width: 80px;
  height: 32px;
  border-radius: 4px;
}


button.el-button.el-button--default.el-button--small {
  color: #1F2329;
}

button.el-button.el-button--default.el-button--small.el-button--primary {
  color: #FFFFFF;
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

.el-icon-warning:before, .el-icon-success:before {
  font-size: 17px;
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
}
</style>

