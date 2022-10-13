<template>
  <div class="el-image-viewer__wrapper" style="z-index: 999" v-show="active">
    <div class="viewer-mask">
      <div class="el-image-viewer__canvas">
        <div class="image-wrap">
          <el-image
            fit="contain"
            :src="src"
            class="img-content"
          ></el-image>
        </div>
      </div>
    </div>
    <span
      class="el-image-viewer__btn el-image-viewer__close"
      style="color: #fff"
      @click.stop="close"
      ><i class="el-icon-close"></i
    ></span>
  </div>
</template>
<script>
export default {
  name: "UiScreenshotViewer",
  props:['src'],
  data() {
    return {
      active: false,
    };
  },
  methods: {
    open() {
      this.active = true;
    },
    close() {
      this.active = false;
    },
  },
};
</script>
<style scoped>
.image-wrap {
  width: 80%;
  height: 100%;
  background-color: #fff;
  overflow-y: scroll;
}
.viewer-mask {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  background-color: rgba(0, 0, 0, 0.5);
}
.img-content.el-image {
  width: 100%;
}
</style>
