<template>
  <el-icon name="back" class="back-button" @click.native="jump"/>
</template>

<script>
export default {
  name: "BackButton",
  props: {
    path: String,
    name: String,
    to: Object
  },
  methods: {
    jump() {
      const {path, name, to} = this
      if (path) {
        this.$router.push(path)
      }
      if (name) {
        this.$router.push({name: this.name})
      }
      if (to) {
        this.$router.push(to)
      }
    }
  }
}
</script>

<style lang="scss">
@import "~../../styles/common/mixins.scss";
@import "~../../styles/common/variables.scss";

.back-button {
  cursor: pointer;
  margin-right: 10px;
  font-weight: 600;

  &:active {
    transform: scale(0.85);
  }
}
</style>
