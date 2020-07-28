<template>
  <el-aside :width="width" class="ms-aside-container"
            :style="{'margin-left': asideHidden ? '0' : '-' + width}">
    <div class="hiddenBottom" @click="asideHidden = !asideHidden" :style="{'left': width}">
      <i v-if="asideHidden" class="el-icon-arrow-left"/>
      <i v-if="!asideHidden" class="el-icon-arrow-right"/>
    </div>
    <slot></slot>
  </el-aside>
</template>

<script>
    export default {
      name: "MsAsideContainer",
      props: {
        width: {
          type: String,
          default: '300px'
        }
      },
      data() {
        return {
          asideHidden: true
        }
      }
    }
</script>

<style scoped>

  .ms-aside-container {
    border: 1px solid #E6E6E6;
    padding: 10px;
    border-radius: 2px;
    box-sizing: border-box;
    background-color: #FFF;
    height: calc(100vh - 80px);
    position: relative;
    overflow: inherit;
  }

  .hiddenBottom {
    z-index: 199;
    width: 8px;
    height: 50px;
    top: calc((100vh - 80px)/2);
    line-height: 50px;
    border-radius: 0 15px 15px 0;
    background-color: #acb7c1;
    display: inline-block;
    position: absolute;
    cursor: pointer;
    opacity: 0.2;
    font-size: 2px;
  }

  .hiddenBottom i {
    margin-left: -2px;
  }

  .hiddenBottom:hover {
    opacity: 0.5;
  }

</style>
