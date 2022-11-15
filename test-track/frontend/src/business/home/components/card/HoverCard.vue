<template>
  <div class="hover-card-main"
       @mouseleave="isHover=false">
    <transition name="el-zoom-in-top">
      <div v-show="!isHover" class="transition-box">
        <div style="margin:16px 0 0 16px">
          <span class="addition-info-title"> {{ title }}</span>
          <el-tooltip class="item" effect="light" :content="toolTip" placement="top-start">
            <img style="height: 14px;width: 14px;margin-left: 4px" src="/assets/figma/icon_question.svg"/>
          </el-tooltip>
          <div class="common-amount" @mouseenter="isHover=true">
            <span class="addition-info-text">
              {{ mainInfo }}
            </span>
          </div>
        </div>
      </div>
    </transition>
    <transition name="el-zoom-in-bottom">
      <div v-show="isHover" class="transition-box-hover">
        <slot name="mouseOut"></slot>
      </div>
    </transition>
  </div>
</template>

<script>
export default {
  name: "HoverCard",
  data() {
    return {
      isHover: false,
    }
  },
  props: {
    title: String,
    mainInfo: [String, Number],
    toolTip: String,
  },
  methods: {}
}
</script>

<style scoped>
.hover-card-main {
  box-sizing: border-box;
  height: 86px;
  background-color: #FFFFFF;
  position: relative;
}

.transition-box {
  height: 100%;
  width: 100%;
  background-color: #FFFFFF;
  box-sizing: border-box;
  position: absolute;
  border: 1px solid #DEE0E3;
  border-radius: 4px;
}

.transition-box-hover {
  height: 100%;
  width: 100%;
  background-color: #F5F6F7;
  box-sizing: border-box;
  position: absolute;
  border: 1px solid #DEE0E3;
  border-radius: 4px;
}
</style>
