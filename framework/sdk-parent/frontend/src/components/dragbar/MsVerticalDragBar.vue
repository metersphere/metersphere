<template>
  <div class="drag-bar" v-vertical-drag="{topMinHeight, bottomMinHeight}"/>
</template>

<script>
export default {
  name: "MsVerticalDragBar",
  props: {
    topMinHeight: String,
    bottomMinHeight: String
  }
}
</script>

<style scoped>

.drag-bar {
  height: 1px;
  width: 100%;
  z-index: 1000;
  cursor: row-resize;
  position: fixed;
  background-color: #E6E6E6;
  border: 0px;
}

.drag-bar:hover {
  height: 3px;
}

</style>
