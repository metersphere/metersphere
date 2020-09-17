<template>
  <div class="text-container">
    <div @click="active" class="collapse">
      <i class="icon el-icon-arrow-right" :class="{'is-active': isActive}"/>
      {{$t('api_report.response')}}
    </div>
    <el-collapse-transition>
      <el-tabs v-model="activeName" v-show="isActive">
        <el-tab-pane label="Body" name="body" class="pane">
          <ms-code-edit :mode="mode" :read-only="true" :data="response.body" :modes="modes" ref="codeEdit"/>
        </el-tab-pane>
        <el-tab-pane label="Headers" name="headers" class="pane">
          <pre>{{response.headers}}</pre>
        </el-tab-pane>
        <el-tab-pane :label="$t('api_report.assertions')" name="assertions" class="pane assertions">
          <ms-assertion-results :assertions="response.assertions"/>
        </el-tab-pane>

        <el-tab-pane v-if="activeName == 'body'" :disabled="true" name="mode" class="pane assertions">
          <template v-slot:label>
            <ms-dropdown :commands="modes" @command="modeChange"/>
          </template>
        </el-tab-pane>

      </el-tabs>
    </el-collapse-transition>
  </div>
</template>

<script>
  import MsAssertionResults from "./AssertionResults";
  import MsCodeEdit from "../../../common/components/MsCodeEdit";
  import MsDropdown from "../../../common/components/MsDropdown";
  import {BODY_FORMAT} from "../../test/model/ScenarioModel";

  export default {
    name: "MsResponseText",

    components: {
      MsDropdown,
      MsCodeEdit,
      MsAssertionResults,
    },

    props: {
      response: Object
    },

    data() {
      return {
        isActive: true,
        activeName: "body",
        modes: ['text', 'json', 'xml', 'html'],
        mode: BODY_FORMAT.TEXT
      }
    },

    methods: {
      active() {
        this.isActive = !this.isActive;
      },
      modeChange(mode) {
        this.mode = mode;
      }
    },
  }
</script>

<style scoped>
  .text-container .icon {
    padding: 5px;
  }

  .text-container .collapse {
    cursor: pointer;
  }

  .text-container .collapse:hover {
    opacity: 0.8;
  }

  .text-container .icon.is-active {
    transform: rotate(90deg);
  }

  .text-container .pane {
    background-color: #F5F5F5;
    padding: 0 10px;
    height: 250px;
    overflow-y: auto;
  }

  .text-container .pane.assertions {
    padding: 0;
  }

  pre {
    margin: 0;
  }
</style>
