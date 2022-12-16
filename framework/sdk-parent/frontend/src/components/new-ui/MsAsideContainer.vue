<template>
  <el-aside :width="asideHidden ? '0' : width" class="ms-aside-container"
            :id="id"
            :style="{
              // 'margin-left': !asideHidden ? 0 : '-' + asideHiddenMargin,
              'min-width': minWidth + 'px',
              'height': calHeight,
             }">
    <div v-if="enableAsideHidden" class="hiddenBottom" :style="{'top': hiddenBottomTop ? hiddenBottomTop : 0}" @click="asideHidden = !asideHidden">
      <i v-if="!asideHidden" class="el-icon-d-arrow-left"/>
      <i v-if="asideHidden" class="el-icon-d-arrow-right"/>
    </div>
    <div style="overflow-x: hidden; padding: 24px 24px 0px 24px;" class="ms-aside-node-tree" :style="{'height': containerCalHeight }">
      <slot></slot>
    </div>
    <ms-horizontal-drag-bar/>
  </el-aside>
</template>

<script>
import MsHorizontalDragBar from ".././dragbar/MsLeft2RightDragBar";
import {getUUID} from "../../utils";

export default {
  name: "MsAsideContainer",
  components: {MsHorizontalDragBar},
  props: {
    width: {
      type: String,
      default: '269px'
    },
    enableAsideHidden: {
      type: Boolean,
      default: true
    },
    minWidth: {
      type: String,
      default: null
    },
    maxWidth: {

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
      this.$emit('setAsideHidden', this.asideHidden);
    }
  },
  computed: {
    calHeight() {
      return this.height ? (this.height + 'px') : (this.enableAutoHeight ? null : 'calc(100vh - 144px)')
    },
    containerCalHeight() {
      return this.height ? (this.height - 30 + 'px') : (this.enableAutoHeight ? null : 'calc(100vh - 154px)')
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
      id: null,
      asideHiddenMargin: '292px'
    }
  },
  methods: {
    setHiddenBottomTop() {
      if (this.defaultHiddenBottomTop) {
        this.hiddenBottomTop = this.defaultHiddenBottomTop + 'px';
      } else {
        let e = document.getElementById(this.id);
        if (!e) return;
        // 默认在 3/1 的位置
        this.hiddenBottomTop = e.clientHeight / 2 + 'px';
      }
    }
  }
}
</script>

<style scoped>

.ms-aside-container {
  border-radius: 2px;
  box-sizing: border-box;
  background-color: #FFF;
  border-right: 0px;
  position: relative;
  overflow: visible;
  max-width: 40%;
}

.hiddenBottom {
  width: 16px;
  height: 36px;
  /*top: calc((100vh - 80px)/3);*/
  right: -16px;
  /*top: 0;*/
  line-height: 50px;
  border-radius: 0px 4px 4px 0px;
  background-color: #acb7c1;
  display: inline-block;
  position: absolute;
  cursor: pointer;
  opacity: 0.4;
  font-size: 2px;
  margin-left: 1px;
  z-index: 10;
}

.hiddenBottom i {
  position: relative;
  top: -5px;
  left: 1px;
}

.hiddenBottom:hover {
  background-color: #783887;
  opacity: 0.8;
  width: 16px;
}

.hiddenBottom:hover i {
  margin-left: 0;
  color: white;
}

</style>
