<template>
  <div class="drag-bar" v-horizontal-drag/>
</template>

<script>
    export default {
        name: "MsHorizontalDragBar"
    }
</script>

<style scoped>

  .drag-bar {
    height: 100%;
    width: 1px;
    position: absolute;
    right: 0px;
    top: 0;
    cursor: col-resize;
    background-color: #E6E6E6;
    border: 0px;
  }

  .drag-bar:hover {
    width: 3px;
  }

</style>
