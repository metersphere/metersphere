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

/*.el-message-box__btns .el-button {*/
/*  margin-left: 12px;*/
/*}*/

.el-message-box__btns button:nth-child(2) {
  margin-left: 12px;
}

/* common style:  elementui small btn 样式可提取为公共*/
.el-button--small span {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  position: relative;
  top: -5px;
}

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


/*.el-button--default {*/
/*  background: #FFFFFF;*/
/*  border: 1px solid #BBBFC4;*/
/*  color: #1F2329;*/
/*}*/

/*.el-button--default:hover {*/
/*  border: 1px solid #783887;*/
/*  background: rgba(120, 56, 135, 0.15);*/
/*}*/
</style>

