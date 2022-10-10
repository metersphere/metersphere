<template>
  <el-tabs :class="['de-tabs',...tabClassName]" :style="tabStyle" v-on="$listeners" v-bind="$attrs">
    <slot></slot>
  </el-tabs>
</template>

<script>
import "../../styles/business/de-tabs.scss";

export default {
  name: "DataeaseTabs",
  props: {
    /* 颜色可以单词，如red；也可以是颜色值 */
    // 字体颜色
    fontColor: String,
    // 激活字体颜色
    activeColor: String,
    // 边框颜色 如果是none就无边框 如果是none Card类型激活的下滑线也消失
    borderColor: String,
    // 激活边框颜色 目前只针对card类型
    borderActiveColor: String,
    // 样式类型  radioGroup只在Card类型有效, 同时必须给borderColor borderActiveColor
    styleType: {
      type: String,
      default: "",
      validator: (val) => ["", "radioGroup"].includes(val),
    },
  },
  computed: {
    tabStyle() {
      return {
        "--font-color": this.fontColor,
        "--active-color": this.activeColor,
        "--border-color": this.borderColor,
        "--border-active-color": this.borderActiveColor,
      };
    },
    tabClassName() {
      const classes = this.styleType
        ? [
          this.styleType,
          this.fontColor && "fontColor",
          this.activeColor && "activeColor",
        ]
        : [
          this.fontColor && "fontColor",
          this.activeColor && "activeColor",
          this.noBorder ? "noBorder" : this.borderColor && "borderColor",
          this.borderActiveColor && "borderActiveColor",
        ];
      return classes;
    },
    noBorder() {
      return this.borderColor === "none";
    },
    noBorderActive() {
      return this.borderActiveColor === "none";
    },
  },
  data() {
    return {};
  },
  created() {
  },
  methods: {},
};
</script>

