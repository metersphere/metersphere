<template>
  <div id="ms-drawer" class="ms-drawer" :class="directionStyle" :style="{width: w + 'px', height: h + 'px'}" ref="msDrawer">
    <ms-drag-move-bar :direction="dragBarDirection" @widthChange="widthChange" @heightChange="heightChange"/>
    <div class="ms-drawer-header" >
      <slot name="header"></slot>
      <i class="el-icon-close" @click="close"/>
    </div>
    <div class="ms-drawer-body">
      <slot></slot>
    </div>
  </div>
</template>

<script>
    import MsDragMoveBar from "./MsDragMoveBar";
    export default {
      name: "MsDrawer",
      components: {MsDragMoveBar},
      data() {
        return {
          x: 0,
          y: 0,
          w: 100,
          h: 100,
          directionStyle: 'left-style',
          dragBarDirection: 'vertical',
        }
      },
      props: {
        direction: {
          type: String,
          default() {
            return "left";
          }
        },
        size: {
          type: Number,
          default() {
            return 40;
          }
        }
      },
      mounted() {
        this.init();
      },
      methods: {
        init() {
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
          }
        },
        resize() {
        },
        getWidthPercentage(per) {
          return document.body.clientWidth * per / 100.0;
        },
        getHeightPercentage(per) {
          return document.body.clientHeight * per / 100.0;
        },
        widthChange(movement) {
          if (this.direction != 'left' && this.direction != 'right') {
            return;
          }
          switch (this.direction) {
            case 'top':
              this.w -= movement;
              break;
            case 'bottom':
              this.w += movement;
              break;
          }
          this._widthChange();
        },
        heightChange(movement) {
          if (this.direction != 'top' && this.direction != 'bottom') {
            return;
          }
          switch (this.direction) {
            case 'top':
              this.h -= movement;
              break;
            case 'bottom':
              this.h += movement;
              break;
          }
          this._heightChange();
        },
        _heightChange() {
          if (this.h < 0) {
            this.h = 0;
          }
          if (this.h > document.body.clientHeight) {
            this.h = document.body.clientHeight;
          }
        },
        _widthChange() {
          if (this.w < 0) {
            this.w = 0;
          }
          if (this.w > document.body.clientWidth) {
            this.w = document.body.clientWidth;
          }
        },
        close() {
          this.$emit('close')
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
    position: fixed;
    width: 100%;
    z-index: 999;
  }

  .el-icon-close {
    position: absolute;
    right: 10px;
    top: 13px;
    color: gray;
    font-size: 20px;
  }

  .el-icon-close:hover {
    color: red;
  }

</style>
