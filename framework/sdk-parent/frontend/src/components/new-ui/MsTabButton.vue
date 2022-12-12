<template>
  <el-card class="card-content" v-if="isShow">

    <el-button-group v-if="isShowChangeButton" class="btn-group">
      <el-tooltip v-if="leftButtonEnable" class="item" effect="dark" :content="leftTip" placement="bottom">
        <el-button plain :class="{active: leftActive}" style="margin: 3px 8px 3px 3px" @click="changeTab('left')">
          <svg-icon :icon-class="leftActive ? leftIconActiveClass : leftIconClass" />
        </el-button>
      </el-tooltip>

      <el-tooltip v-if="rightButtonEnable" class="item" effect="dark" :content="rightTip" placement="bottom">
        <el-button plain :class="{active: rightActive}" style="margin: 3px 3px 3px 0" @click="changeTab('right')">
          <svg-icon :icon-class="rightActive ? rightIconActiveClass : rightIconClass"/>
        </el-button>
      </el-tooltip>
    </el-button-group>

    <slot name="version"></slot>

    <template v-slot:header>
      <slot name="header"></slot>
    </template>
    <slot></slot>
  </el-card>
</template>

<script>
export default {
  name: "MsTabButton",
  data() {
    return {
      isShow: true,
      showApiList:false,
      showTestCaseList:false,
      showDocList:true,
    }
  },
  props: {
    activeDom: String,
    isShowChangeButton: {
      type: Boolean,
      default: true
    },
    leftButtonEnable: {
      type: Boolean,
      default: true
    },
    middleButtonEnable: {
      type: Boolean,
      default: true
    },
    rightButtonEnable: {
      type: Boolean,
      default: true
    },
    leftIconClass: {
      type: String,
      default: 'left'
    },
    middleIconClass: {
      type: String,
      default: 'middle'
    },
    rightIconClass: {
      type: String,
      default: 'right'
    },
    leftIconActiveClass: {
      type: String,
      default: 'left'
    },
    middleIconActiveClass: {
      type: String,
      default: 'middle'
    },
    rightIconActiveClass: {
      type: String,
      default: 'right'
    },
    leftTip: {
      type: String,
      default: 'left'
    },
    middleTip: {
      type: String,
      default: 'middle'
    },
    rightTip: {
      type: String,
      default: 'right'
    },
  },
  computed: {
    leftActive() {
      return this.activeDom === 'left';
    },
    middleActive() {
      return this.activeDom === 'middle';
    },
    rightActive() {
      return this.activeDom === 'right';
    },
  },
  methods: {
    changeTab(tabType){
      this.$emit("changeTab", tabType);
      this.$emit("update:activeDom", tabType);
    },
  },
}
</script>

<style scoped>
.active {
  border: solid 1px #6d317c!important;
  background-color: rgba(120, 56, 135, 0.1)!important;
  color: #FFFFFF!important;
}

.item{
  height: 32px;
  padding: 5px 8px;
  border: solid 1px var(--primary_color);
}

:deep(button.el-button.el-tooltip.item.el-button--default.is-plain.active) {
  width: 24px;
  height: 24px;
  background: rgba(120, 56, 135, 0.1);
  border-radius: 4px;
  border: none!important;
}

:deep(button.el-button.el-tooltip.item.el-button--default.is-plain) {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  border: none!important;
}

:deep(button.el-button.el-tooltip.item.el-button--default.is-plain:hover) {
  background: rgba(120, 56, 135, 0.1);
}

svg.svg-icon {
  width: 14px;
  height: 14px;
  position: relative;
  right: 3px!important;
  top: 1px;
}

.btn-group {
  float: right;
  box-sizing: border-box;
  width: 64px;
  height: 32px;
  background: #FFFFFF;
  border: 1px solid #BBBFC4;
  border-radius: 4px;
  flex: none;
  order: 7;
  flex-grow: 0;
  margin-left: 12px;
}
</style>
