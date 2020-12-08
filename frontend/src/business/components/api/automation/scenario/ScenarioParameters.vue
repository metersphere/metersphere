<template>
  <el-dialog :close-on-click-modal="false" :title="$t('api_test.scenario.variables')"
             :visible.sync="visible" class="environment-dialog" width="60%"
             @close="close">
    <el-container>
      <ms-api-scenario-variables :items="variables"
                                 :description="$t('api_test.scenario.kv_description')"/>
    </el-container>
    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="close"
        @confirm="saveParameters"/>
    </template>

  </el-dialog>
</template>

<script>
  import MsApiScenarioVariables from "./ApiScenarioVariables";
  import {KeyValue} from "../../definition/model/ApiTestModel";
  import MsDialogFooter from "../../../common/components/MsDialogFooter";

  export default {
    name: "MsScenarioParameters",
    components: {
      MsApiScenarioVariables,
      MsDialogFooter
    },
    data() {
      return {
        variables: [new KeyValue()],
        visible: false,
      }
    },
    methods: {
      open: function (v) {
        this.visible = true;
        if(v){
          this.variables = v;
        }
      },
      close() {
        this.visible = false;
      },
      saveParameters() {
        this.visible = false;
        this.$emit('addParameters', this.variables);
      }
    }
  }
</script>

<style scoped>

</style>
