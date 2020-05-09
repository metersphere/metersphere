<template>
  <div class="text-container">
    <div @click="active" class="collapse">
      <i class="icon el-icon-arrow-right" :class="{'is-active': isActive}"/>
      {{$t('api_report.response')}}
    </div>
    <el-collapse-transition>
      <el-tabs v-model="activeName" v-show="isActive">
        <el-tab-pane label="Body" name="body" class="pane">
          <div>{{response.body}}</div>
        </el-tab-pane>
        <el-tab-pane label="Headers" name="headers" class="pane">
          <pre>{{response.headers}}</pre>
        </el-tab-pane>
        <el-tab-pane :label="$t('api_report.assertions')" name="assertions" class="pane">
          <ms-assertion-results :assertions="response.assertions"/>
        </el-tab-pane>
      </el-tabs>
    </el-collapse-transition>
  </div>
</template>

<script>
  import MsAssertionResults from "./AssertionResults";

  export default {
    name: "MsResponseText",

    components: {MsAssertionResults},

    props: {
      response: Object
    },

    data() {
      return {
        isActive: false,
        activeName: "body",
      }
    },

    methods: {
      active() {
        this.isActive = !this.isActive;
      }
    },
  }
</script>

<style scoped>
  .text-container .icon {
    padding: 5px;
  }

  .text-container .icon.is-active {
    transform: rotate(90deg);
  }

  .text-container .collapse:hover {
    opacity: 0.8;
  }

  .text-container .pane {
    background-color: #F5F5F5;
    padding: 0 10px;
    height: 250px;
    overflow-y: auto;
  }

  pre {
    margin: 0;
  }
</style>
