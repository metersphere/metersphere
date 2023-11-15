<template>
  <ms-container>
    <ms-aside-container v-if="showAside" pageKey="TEST_PLAN_COMMON_COMP">
      <slot name="aside"></slot>
    </ms-aside-container>

    <ms-main-container>
      <slot name="main"></slot>
    </ms-main-container>

    <slot></slot>
  </ms-container>
</template>

<script>
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsAsideContainer from "metersphere-frontend/src/components/MsAsideContainer";
import MsContainer from "metersphere-frontend/src/components/MsContainer";

export default {
  name: "MsTestPlanCommonComponent",
  components: { MsContainer, MsAsideContainer, MsMainContainer },
  props: {
    showAside: {
      type: Boolean,
      default: true,
    },
  },
};
</script>

<style scoped>
.ms-main-container {
  height: calc(100vh - 132px) !important;
}

.ms-aside-container {
  height: calc(100vh - 132px) !important;
}

.ms-aside-container :deep(.ms-aside-node-tree) {
  height: calc(100vh - 145px) !important;
}
</style>
