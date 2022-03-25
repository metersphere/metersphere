<template>
  <div>
    <el-collapse v-model="activeNames">
      <el-collapse-item name="1">
        <template slot="title">
          <span class="span-style">{{title}}</span>
          <el-tooltip class="item" effect="dark" :content="runEveryTimeTip" placement="right">
            <i class="el-icon-info"/>
          </el-tooltip>

          <div class="header-right" style="margin: 5px 5px 5px 50px" @click.stop>
            <span class="span-style">{{$t('api_test.script.filter_request_type')}}</span>
            <el-select multiple v-model="filterRequestArray" style="margin : 0px 10px 0px 10px; width: 214px "
                       size="small" :placeholder="$t('commons.please_select')" :disabled="isReadOnly">
              <el-option
                v-for="item in requestArray"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
            <span class="span-style" style="margin-right: 10px">{{$t('api_test.script.execution_order')}}</span>
            <el-select v-model="isExecAfterPrivateScript" size="small" :placeholder="$t('commons.please_select')" :disabled="isReadOnly">
              <el-option
                  v-for="item in scriptExecSort"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
              </el-option>
            </el-select>

          </div>

        </template>
        <jsr233-processor-content :jsr223-processor="scriptProcessor"
                                  :is-pre-processor="isPreProcessor"
                                  :is-read-only="isReadOnly"/>
      </el-collapse-item>
      <el-collapse-item name="2">
        <template slot="title">
          {{ stepTitle }}
          <el-tooltip class="item" effect="dark" :content="runOnceTip" placement="right">
            <i class="el-icon-info"/>
          </el-tooltip>
          <div class="header-right" style="margin: 5px 5px 5px 50px;" @click.stop>
            <el-switch v-model="isConnScenario" :active-text="$t('api_test.script.associated_scene_results')" style="font-size: 13px;font-weight: 300"
                       @click.stop :disabled="isReadOnly"/>
            <el-tooltip class="item" effect="dark" :content="$t('api_test.script.tip_3')" placement="right">
              <i class="el-icon-info"/>
            </el-tooltip>
          </div>
        </template>
        <jsr233-processor-content :jsr223-processor="scrpitStepProcessor"
                                  :is-pre-processor="isPreProcessor"
                                  :is-read-only="isReadOnly"/>
      </el-collapse-item>
    </el-collapse>
  </div>

</template>

<script>

import Jsr233ProcessorContent from "@/business/components/api/automation/scenario/common/Jsr233ProcessorContent";

export default {
  name: "EnvironmentGlobalScript",
  components: {
    Jsr233ProcessorContent,
  },
  props: {
    filterRequest: Array,
    execAfterPrivateScript: Boolean,
    connScenario: Boolean,

    scriptProcessor: Object,
    scrpitStepProcessor: Object,
    isPreProcessor: Boolean,
    isReadOnly: Boolean,
  },
  data() {
    return {
      title: "",
      runEveryTimeTip: "",
      runOnceTip: "",
      preTitle: this.$t('api_test.script.execute_before_step'),
      postTitle: this.$t('api_test.script.execute_post_step'),
      stepTitle: "",
      preStepTitle: this.$t('api_test.script.execute_before_all_steps'),
      postStepTitle: this.$t('api_test.script.execute_post_all_steps'),
      result: {},
      activeNames: [],
      isConnScenario: false,
      isExecAfterPrivateScript: false,
      filterRequestArray: [],
      requestArray: [
        {value: "HTTP", label: "HTTP"},
        {value: "TCP", label: "TCP"},
        {value: "JDBC", label: "JDBC"},
      ],
      scriptExecSort: [],
      scriptPreExecSort: [
        {value: true, label: this.$t('api_test.script.after_the_pre_script_step')},
        {value: false, label: this.$t('api_test.script.before_the_pre_script_step')},
      ],
      scriptPostExecSort: [
        {value: true, label: this.$t('api_test.script.after_the_post_script_step')},
        {value: false, label: this.$t('api_test.script.before_the_post_script_step')},
      ],
    }
  },
  created() {
    if (this.isPreProcessor) {
      this.scriptExecSort = this.scriptPreExecSort;
      this.title = this.preTitle;
      this.stepTitle = this.preStepTitle;
      this.runEveryTimeTip = this.$t('api_test.script.execute_before_step_tip')
      this.runOnceTip = this.$t('api_test.script.execute_before_all_steps_tip')
    } else {
      this.scriptExecSort = this.scriptPostExecSort;
      this.title = this.postTitle;
      this.stepTitle = this.postStepTitle;
      this.runEveryTimeTip = this.$t('api_test.script.execute_post_step_tip')
      this.runOnceTip = this.$t('api_test.script.execute_post_all_steps_tip')
    }
    this.isConnScenario = this.connScenario;
    this.isExecAfterPrivateScript = this.execAfterPrivateScript;
    this.filterRequestArray = this.filterRequest;
    if (this.scriptProcessor && this.scriptProcessor.script && this.scriptProcessor.script !== "") {
      this.activeNames.push("1");
    }
    if (this.scrpitStepProcessor && this.scrpitStepProcessor.script && this.scrpitStepProcessor.script !== "") {
      this.activeNames.push("2");
    }

  },
  watch: {
    isConnScenario() {
      this.$emit("updateGlobalScript", this.isPreProcessor, "connScenario", this.isConnScenario);
    },
    isExecAfterPrivateScript() {
      this.$emit("updateGlobalScript", this.isPreProcessor, "execAfterPrivateScript", this.isExecAfterPrivateScript);
    },
    filterRequestArray() {
      this.$emit("updateGlobalScript", this.isPreProcessor, "filterRequest", this.filterRequestArray);
    },
  },
  methods: {},
}
</script>

<style scoped>
.header-right {
  z-index: 1;
}
.span-style {
  font-size: 13px;
  font-weight: 400;
}

>>> .header-right .el-switch__label *{
  font-size: 13px!important;
  font-weight: 400;
}
</style>
