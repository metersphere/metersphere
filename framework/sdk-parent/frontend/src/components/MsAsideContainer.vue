<template>
  <el-aside
    :width="asideHidden ? '0' : defaultWidth"
    class="ms-aside-container"
    :id="id"
    :style="{ 'min-width': minWidth, 'max-width': maxWidth }"
  >
    <div
      v-if="enableAsideHidden"
      class="hiddenBottom"
      :style="{ top: hiddenBottomTop ? hiddenBottomTop : 0 }"
      @click="asideHidden = !asideHidden"
    >
      <i v-if="!asideHidden" class="el-icon-arrow-left" />
      <i v-if="asideHidden" class="el-icon-arrow-right" />
    </div>
    <div
      style="overflow: scroll"
      class="ms-aside-node-tree"
      :style="{ height: containerCalHeight }"
    >
      <slot></slot>
    </div>
    <ms-horizontal-drag-bar v-if="draggable" />
  </el-aside>
</template>

<script>
import MsHorizontalDragBar from "./dragbar/MsLeft2RightDragBar";
import { getUUID } from "../utils";

export default {
  name: "MsAsideContainer",
  components: { MsHorizontalDragBar },
  props: {
    enableAsideHidden: {
      type: Boolean,
      default: true,
    },
    width: {
      type: String,
      default: "300px",
    },
    minWidth: {
      type: String,
      default: null,
    },
    maxWidth: {
      type: String,
      default: null,
    },
    height: {
      type: String,
      default: null,
    },
    enableAutoHeight: {
      type: Boolean,
      default: false,
    },
    defaultHiddenBottomTop: {
      type: Number,
      default: null,
    },
    pageKey: {
      type: String,
      default: null,
    },
    draggable: {
      type: Boolean,
      default: true,
    },
  },
  watch: {
    asideHidden() {
      this.$emit("setAsideHidden", this.asideHidden);
    },
  },
  computed: {
    containerCalHeight() {
      return this.height
        ? this.height - 30 + "px"
        : this.enableAutoHeight
        ? null
        : "calc(100vh - 62px)";
    },
  },
  created() {
    this.id = getUUID();
  },
  mounted() {
    this.$nextTick(() => {
      this.setHiddenBottomTop();
    });
    if (this.pageKey) {
      const rememberKey = "WIDTH_" + this.pageKey;
      const rememberWidth = localStorage.getItem(rememberKey);

      if (rememberWidth) {
        // 获取上次记住的宽度
        this.defaultWidth = rememberWidth;
      } else {
        this.defaultWidth = this.width;
      }

      const element = document.getElementById(this.id);
      const MutationObserver =
        window.MutationObserver ||
        window.WebKitMutationObserver ||
        window.MozMutationObserver;
      this.observer = new MutationObserver(() => {
        // 监听元素的宽度变化，保存在 localStorage 中
        const width = getComputedStyle(element).getPropertyValue("width");
        if (!this.asideHidden) {
          localStorage.setItem(rememberKey, width);
          // 这里宽度变化设置下默认宽度，否则页面有更新，会导致宽度变回到原来的默认宽度
          this.defaultWidth = width;
        }
      });
      this.observer.observe(element, {
        attributes: true,
        attributeFilter: ["style"],
        attributeOldValue: true,
      });
    }
  },
  beforeDestroyed() {
    if (this.observer) {
      this.observer.disconnect();
      this.observer.takeRecords();
      this.observer = null;
    }
  },
  data() {
    return {
      asideHidden: false,
      hiddenBottomTop: null,
      id: null,
      defaultWidth: this.width,
      observer: null,
    };
  },
  methods: {
    setHiddenBottomTop() {
      if (this.defaultHiddenBottomTop) {
        this.hiddenBottomTop = this.defaultHiddenBottomTop + "px";
      } else {
        const e = document.getElementById(this.id);
        if (!e) return;
        // 默认在 3/1 的位置
        this.hiddenBottomTop = e.clientHeight / 3 + "px";
      }
    },
  },
};
</script>

<style scoped>
.ms-aside-container {
  border: 1px solid #e6e6e6;
  padding: 10px;
  border-radius: 2px;
  box-sizing: border-box;
  background-color: #fff;
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
