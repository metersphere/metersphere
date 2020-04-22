<template>
  <div>
    <div class="assertion-item-editing regex" v-if="assertions.regex.length > 0">
      <div>
        {{$t("api_test.request.assertions.regex")}}
      </div>
      <div class="regex-item" v-for="(regex, index) in assertions.regex" :key="index">
        <ms-api-assertion-regex :list="assertions.regex" :regex="regex" :edit="true" :index="index"/>
      </div>
    </div>

    <div class="assertion-item-editing response-time" v-if="isShow">
      <div>
        {{$t("api_test.request.assertions.response_time")}}
      </div>
      <ms-api-assertion-response-time :response-time="assertions.responseTime" :edit="true"/>
    </div>
  </div>

</template>

<script>
  import MsApiAssertionRegex from "./ApiAssertionRegex";
  import MsApiAssertionResponseTime from "./ApiAssertionResponseTime";
  import {Assertions} from "../model/ScenarioModel";

  export default {
    name: "MsApiAssertionsEdit",

    components: {MsApiAssertionResponseTime, MsApiAssertionRegex},

    props: {
      assertions: Assertions
    },

    computed: {
      isShow() {
        let rt = this.assertions.responseTime;
        return rt.responseInTime !== null && rt.responseInTime > 0;
      }
    }
  }
</script>

<style scoped>
  .assertion-item-editing {
    padding-left: 10px;
    margin-top: 10px;
  }

  .assertion-item-editing.regex {
    border-left: 2px solid #7B0274;
  }

  .assertion-item-editing.response-time {
    border-left: 2px solid #DD0240;
  }

  .regex-item {
    margin-top: 10px;
  }


</style>
