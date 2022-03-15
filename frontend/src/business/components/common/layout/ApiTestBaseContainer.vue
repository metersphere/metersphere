<template>
  <div>
    <ms-container>
      <ms-aside-container v-show="showAside" :enable-auto-height="true">
        <slot name="aside"></slot>
      </ms-aside-container>
      <ms-main-container :enable-auto-height="true">
        <slot name="mainContainer"></slot>
      </ms-main-container>
    </ms-container>
  </div>
</template>

<script>
import MsContainer from "@/business/components/common/components/MsContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import MsAsideContainer from "@/business/components/common/components/MsAsideContainer";

export default {
  name: "ApiTestBaseContainer",
  components: {MsMainContainer, MsAsideContainer,MsContainer},
  props:{
    showAside:{
      type:Boolean,
      default(){
        return true;
      }
    }
  },
  data(){
    return{
    }
  }
}
</script>

<style scoped>

</style>
