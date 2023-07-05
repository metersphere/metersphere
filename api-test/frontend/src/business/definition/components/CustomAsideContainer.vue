<template>
  <el-aside width="300px" class="ms-aside-container" :style="{'margin-left': !asideHidden ? 0 : '-300px'}">
    <div class="hiddenBottom" @click="asideHidden = !asideHidden">
      <i v-if="!asideHidden" class="el-icon-arrow-left"/>
      <i v-if="asideHidden" class="el-icon-arrow-right"/>
    </div>
    <div class="node-tree">
      <slot></slot>
    </div>
    <ms-horizontal-drag-bar />
  </el-aside>
</template>

<script>
import MsHorizontalDragBar from 'metersphere-frontend/src/components/dragbar/MsLeft2RightDragBar';

export default {
  name: 'MsAsideContainer',
  components: { MsHorizontalDragBar },
  data() {
    return {
      asideHidden: false,
    };
  },
};
</script>

<style scoped>
.ms-aside-container {
  border: 1px solid #e6e6e6;
  padding: 10px 0 10px 10px;
  border-radius: 2px;
  box-sizing: border-box;
  background-color: #fff;
  border-right: 0px;
  position: relative;
  overflow: visible;
}
.ms-aside-container .node-tree{
  height: calc(100vh - 115px);
  overflow: auto;
  padding-right: 10px;
  /**兼容 firefox */
  padding-bottom: 0;
}
.hiddenBottom {
  width: 8px;
  height: 50px;
  top: 33%;
  right: -10px;
  line-height: 50px;
  border-radius: 0 15px 15px 0;
  background-color: #acb7c1;
  display: inline-block;
  position: absolute;
  cursor: pointer;
  opacity: 0.4;
  font-size: 2px;
  margin-left: 1px;
}

.hiddenBottom i {
  margin-left: -2px;
}

.hiddenBottom:hover {
  background-color: #783887;
  opacity: 0.8;
  width: 12px;
}

.hiddenBottom:hover i {
  margin-left: 0;
  color: white;
}
</style>
