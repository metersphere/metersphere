<template>
  <div class="info-container">
    <slot></slot>
  </div>
</template>

<script>
export default {
  name: "ApiInfoContainer"
}
</script>

<style scoped>

.info-container {
  border:1px #DCDFE6 solid; height: 100%;border-radius: 4px ;width: 100%
}

.info-container .icon {
  padding: 5px;
}

.info-container .collapse {
  cursor: pointer;
}

.info-container .collapse:hover {
  opacity: 0.8;
}

.info-container .icon.is-active {
  transform: rotate(90deg);
}

.info-container .pane {
  background-color: white;
  padding: 1px 0;
  height: 250px;
  overflow-y: auto;
}

.info-container .pane.cookie {
  padding: 0;
}

</style>
