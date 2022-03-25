<template>
  <div v-if="visible" id="ms-drawer" class="ms-drawer" :class="directionStyle" :style="{width: w + 'px', height: h + 'px'}" ref="msDrawer">

    <ms-bottom2-top-drag-bar v-if="direction == 'bottom'"/>

    <ms-right2-left-drag-bar v-if="direction == 'right'"/>

    <ms-right2-left-drag-bar v-if="direction == 'default'"/>


    <div class="ms-drawer-header">
      <slot name="header"></slot>
      <i v-if="isShowClose" class="el-icon-close" @click="close"/>
      <ms-full-screen-button v-if="showFullScreen" :is-full-screen.sync="isFullScreen"/>
    </div>
    <div class="ms-drawer-body">
      <slot></slot>
    </div>

    <ms-left2-right-drag-bar v-if="direction == 'left'"/>
  </div>
</template>

<script>
    import MsRight2LeftDragBar from "./dragbar/MsRight2LeftDragBar";
    import MsLeft2RightDragBar from "./dragbar/MsLeft2RightDragBar";
    import MsBottom2TopDragBar from "./dragbar/MsBottom2TopDragBar";
    import MsFullScreenButton from "@/business/components/common/components/MsFullScreenButton";
    export default {
      name: "MsDrawer",
      components: {MsFullScreenButton, MsBottom2TopDragBar, MsLeft2RightDragBar, MsRight2LeftDragBar},
      data() {
        return {
          x: 0,
          y: 0,
          w: 100,
          h: 100,
          directionStyle: 'left-style',
          dragBarDirection: 'vertical',
          isFullScreen: false,
          originalW: 100,
          originalH: 100,
        }
      },
      props: {
        direction: {
          type: String,
          default() {
            return "left";
          }
        },
        visible: {
          type: Boolean,
          default() {
            return true;
          }
        },
        size: {
          type: Number,
          default() {
            return 40;
          }
        },
        showFullScreen: {
          type: Boolean,
          default() {
            return true;
          }
        },
        isShowClose: {
          type: Boolean,
          default() {
            return true;
          }
        }
      },
      mounted() {
        this.init();
      },
      watch: {
        isFullScreen() {
          if (this.isFullScreen) {
            this.fullScreen()
          } else {
            this.unFullScreen();
          }
        }
      },
      methods: {
        init() {
          window.addEventListener("resize", this.listenScreenChange,false);
          //  todo 其他方向待优化
          switch (this.direction) {
            case 'left':
              this.w = this.getWidthPercentage(this.size);
              this.h = this.getHeightPercentage(100);
              this.x = 0;
              this.y = 0;
              this.directionStyle = 'left-style';
              this.dragBarDirection = 'horizontal';
              break;
            case 'right':
              this.w = this.getWidthPercentage(this.size);
              this.h = this.getHeightPercentage(100);
              this.x = document.body.clientWidth - this.w;
              this.y = 0;
              this.directionStyle = 'right-style';
              this.dragBarDirection = 'horizontal';
              break;
            case 'top':
              this.w = this.getWidthPercentage(100);
              this.h = this.getHeightPercentage(this.size);
              this.x = 0;
              this.y = 0;
              this.directionStyle = 'top-style';
              this.dragBarDirection = 'vertical';
              break;
            case 'bottom':
              this.w = this.getWidthPercentage(100);
              this.h = this.getHeightPercentage(this.size);
              this.x = 0;
              this.y = document.body.clientHeight - this.h;
              this.directionStyle = 'bottom-style';
              this.dragBarDirection = 'vertical';
              break;
            default:
              this.w = this.getWidthPercentage(this.size);
              this.h = this.getHeightPercentage(100);
              this.x = document.body.clientWidth - this.w;
              this.y = 0;
              this.directionStyle = 'right-style';
              this.dragBarDirection = 'horizontal';
              break;
          }
        },
        getWidthPercentage(per) {
          return document.body.clientWidth * per / 100.0;
        },
        getHeightPercentage(per) {
          return document.body.clientHeight * per / 100.0;
        },
        fullScreen() {
          this.originalW = this.w;
          this.originalH = this.h;
          this.w = document.body.clientWidth;
          this.h = document.body.clientHeight;
        },
        unFullScreen() {
          this.w = this.originalW;
          this.h = this.originalH;
        },
        close() {
          this.$emit('close');
          window.removeEventListener("resize", this.listenScreenChange);
        },
        listenScreenChange() {
          switch (this.direction) {
            case 'left':
              this.h = document.documentElement.clientHeight;
              break;
            case 'right':
              this.h = document.documentElement.clientHeight;
              break;
            case 'top':
              this.w = document.documentElement.clientWidth;
              break;
            case 'bottom':
              this.w = document.documentElement.clientWidth;
              break;
            default:
              this.h = document.documentElement.clientHeight;
              this.w = document.documentElement.clientWidth;
              break;
          }
        }
      }
    }
</script>

<style scoped>

  .ms-drawer {
    background-color: white;
    border: 1px #DCDFE6 solid;
    -webkit-box-shadow: 0 8px 10px -5px rgba(0,0,0,.2), 0 16px 24px 2px rgba(0,0,0,.14), 0 6px 30px 5px rgba(0,0,0,.12);
    box-shadow: 0 8px 10px -5px rgba(0,0,0,.2), 0 16px 24px 2px rgba(0,0,0,.14), 0 6px 30px 5px rgba(0,0,0,.12);
    z-index: 999 !important;
    position: fixed;
    overflow: auto;
  }

  .left-style {
    top: 0;
    left: 0;
  }

  .right-style {
    top: 0;
    right: 0;
  }

  .top-style {
    top: 0;
    left: 0;
  }

  .bottom-style {
    bottom: 0;
    left: 0;
    border-top: 5px;
  }

  .ms-drawer-body {
    overflow: scroll;
  }

  .ms-drawer-header {
    z-index: 999;
    width: 100%;
  }

  .bottom-style .ms-drawer-header {
    position: fixed;
  }

  .el-icon-close {
    position: absolute;
    font-size: 20px;
    right: 10px;
    top: 10px;
    color: gray;
  }

  .bottom-style .el-icon-close {
    right: 10px;
    top: 13px;
  }

  .right-style .el-icon-close {
    position: fixed;
    right: 10px;
    top: 10px;
  }

  .el-icon-close:hover {
    color: red;
  }

  /deep/ .alt-ico {
    position: absolute;
    right: 40px;
    top: 15px;
  }

</style>
