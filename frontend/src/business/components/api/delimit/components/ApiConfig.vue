<template>

  <div class="card-container">
    <ms-add-complete-http-api @runTest="runTest" :httpData="result" v-if="reqType === 'HTTP'"></ms-add-complete-http-api>
  </div>
</template>

<script>
  import MsAddCompleteHttpApi from "./complete/AddCompleteHttpApi";
  import {RequestFactory} from "../model/ScenarioModel";

  export default {
    name: "ApiConfig",
    components: {MsAddCompleteHttpApi},
    data() {
      return {
        reqType: RequestFactory.TYPES.HTTP,
        result: {},
      }
    },
    props: {},
    methods: {
      runTest(data){
        this.$emit('runTest',data);
      }
    }
  }
</script>

<style scoped>


</style>
