<template>
  <el-button-group>
    <el-tooltip
      v-for="item in tabList"
      :key="item.domKey"
      class="item"
      effect="dark"
      :content="item.tip"
      :placement="item.placement"
    >
      <el-button
        plain
        :class="{ active: currentActiveDom === item.domKey }"
        @click="changeTab(item.domKey)"
        >{{ item.content }}</el-button
      >
    </el-tooltip>
  </el-button-group>
</template>

<script>
export default {
  name: "ToggleTabs",
  props: {
    activeDom: String,
    tabList: {
      type: Array,
      default() {
        return [
          {
            domKey: "default",
            tip: "default",
            content: "default",
            placement: "top",
            enable: true,
          },
        ];
      },
    },
  },
  computed: {
    currentActiveDom() {
      return this.activeDom;
    },
  },
  methods: {
    changeTab(domKey) {
      this.$emit("update:activeDom", domKey);
      this.$emit("toggleTab", domKey)
    },
  },
};
</script>

<style scoped>
.active {
  border: solid 1px #6d317c !important;
  background-color: var(--primary_color) !important;
  color: #ffffff !important;
}

.case-button {
  border-left: solid 1px var(--primary_color);
}

.item {
  height: 32px;
  padding: 5px 8px;
  border: solid 1px var(--primary_color);
}
</style>
