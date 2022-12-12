<template>
  <div class="select-menu">
    <span class="menu-title">{{ title }}</span>
    <el-select
      filterable
      slot="prepend"
      v-model="value"
      @change="changeData"
      :style="{ width: width }"
      size="small"
    >
      <el-option
        v-for="(item, index) in data"
        :key="index"
        :label="item.name"
        :value="index"
      >
        <span class="span-name" :title="item.name">{{ item.name }}</span>
      </el-option>
    </el-select>
  </div>
</template>

<script>
export default {
  name: "SelectMenu",
  props: {
    data: {
      type: Array,
    },
    currentData: {
      type: Object,
    },
    title: {
      type: String,
    },
    width: {
      type: String,
      default() {
        return "214px";
      },
    },
  },
  data() {
    return {
      value: "",
    };
  },
  watch: {
    currentData(data) {
      if (data != undefined && data != null) {
        this.value = data.name;
      }
    },
  },
  methods: {
    changeData(index) {
      this.$emit("dataChange", this.data[index]);
    },
  },
};
</script>

<style scoped lang="scss">
@import "@/business/style/index.scss";
.menu-title {
  color: #1f2329;
  margin-right: px2rem(10);
  margin-bottom: px2rem(17);
}
.select-menu :deep(.el-input__inner) {
  border: none;
  padding-left: 0px !important;
  display: inline-block !important;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  width: px2rem(100);
}
.select-menu :deep(.el-select--small) {
  width: px2rem(82) !important;
}
.select-menu :deep(.el-input__suffix-inner .el-select__caret::before) {
  color: #646a73;
}

.span-name {
  display: inline-block;
  max-width: 300px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  word-break: break-all;
  margin-right: 5px;
}
</style>
