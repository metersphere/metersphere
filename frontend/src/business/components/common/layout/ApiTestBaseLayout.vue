<template>
  <div>

  </div>
</template>

<script>

import ApiTestBaseContainer from "@/business/components/common/layout/ApiTestBaseContainer";

export default {
  name: "ApiTestBaseLayout",
  components:{
    ApiTestBaseContainer
  },
  data() {
    return {
      trashEnable: false,
      activeName:"",
    };
  },
  methods:{
    handleClick(){
      this.$emit("changeTab")
    }
  },
}
</script>

<style scoped>

</style>
