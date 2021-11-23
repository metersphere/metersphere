<template>
  <el-aside :width="width" class="ms-aside-container"
            :id="id"
            :style="{
              'margin-left': !asideHidden ? 0 : '-' + width,
              'min-width': minWidth + 'px',
              'height': calHeight,
             }">
    <div v-if="enableAsideHidden" class="hiddenBottom" :style="{'top': hiddenBottomTop ? hiddenBottomTop : 0}" @click="asideHidden = !asideHidden">
      <i v-if="!asideHidden" class="el-icon-arrow-left"/>
      <i v-if="asideHidden" class="el-icon-arrow-right"/>
    </div>
    <div style="overflow: scroll" class="ms-aside-node-tree" :style="{'height': containerCalHeight }">
      <slot></slot>
    </div>
    <ms-horizontal-drag-bar/>
  </el-aside>
</template>

<script>
    import MsHorizontalDragBar from "./dragbar/MsLeft2RightDragBar";
    import {getUUID} from "@/common/js/utils";
    export default {
      name: "MsAsideContainer",
      components: {MsHorizontalDragBar},
      props: {
        width: {
          type: String,
          default: '300px'
        },
        enableAsideHidden: {
          type: Boolean,
          default: true
        },
        minWidth: {
          type: String,
          default: null
        },
        height: {
          type: String,
          default: null
        },
        enableAutoHeight: {
          type: Boolean,
          default: false
        },
        defaultHiddenBottomTop: {
          type: Number,
          default: null
        },
      },
      watch: {
        asideHidden() {
          this.$emit('setAsideHidden', this.asideHidden) ;
        }
      },
      computed: {
        calHeight() {
          return this.height ? (this.height + 'px') : (this.enableAutoHeight ? null : 'calc(100vh - 80px)')
        },
        containerCalHeight() {
          return this.height ? (this.height - 30 + 'px') : (this.enableAutoHeight ? null : 'calc(100vh - 100px)')
        },
      },
      created() {
        this.id = getUUID();
      },
      mounted() {
        this.$nextTick(() => {
          this.setHiddenBottomTop();
        });
      },
      data() {
        return {
          asideHidden: false,
          hiddenBottomTop: null,
          id: null
        }
      },
      methods: {
        setHiddenBottomTop() {
          if (this.defaultHiddenBottomTop) {
            this.hiddenBottomTop =  this.defaultHiddenBottomTop + 'px';
          } else {
            let e = document.getElementById(this.id);
            if (!e) return;
            // 默认在 3/1 的位置
            this.hiddenBottomTop = e.clientHeight / 3 + 'px';
          }
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
    /*height: calc(100vh - 80px);*/
    border-right: 0px;
    position: relative;
    overflow: visible;
  }

  .hiddenBottom {
    width: 8px;
    height: 50px;
    /*top: calc((100vh - 80px)/3);*/
    right: -10px;
    /*top: 0;*/
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
