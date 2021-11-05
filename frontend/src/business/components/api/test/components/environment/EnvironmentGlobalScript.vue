<template>
  <div>
    <el-collapse v-model="activeNames">
      <el-collapse-item name="1">
        <template slot="title">
          <span class="span-style">{{title}}</span>
          <el-tooltip class="item" effect="dark" content="每一个API步骤后执行一次 如加解密" placement="right">
            <i class="el-icon-info"/>
          </el-tooltip>

          <div class="header-right" style="margin: 5px 5px 5px 50px" @click.stop>
            <span class="span-style">过滤请求类型</span>
            <el-select multiple v-model="filterRequestArray" style="margin : 0px 10px 0px 10px; width: 214px "
                       size="small" placeholder="请选择">
              <el-option
                v-for="item in requestArray"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
            <span class="span-style" style="margin-right: 10px">脚本执行顺序</span>
            <el-select v-model="isExecAfterPrivateScript" size="small" placeholder="请选择">
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
          所有请求步骤后执行
          <el-tooltip class="item" effect="dark" content="全部API流程结束后执行一次 如token获取，场景初始化" placement="right">
            <i class="el-icon-info"/>
          </el-tooltip>
          <div class="header-right" style="margin: 5px 5px 5px 50px;" @click.stop>
            <el-switch v-model="isConnScenario" active-text="关联场景结果" style="font-size: 13px;font-weight: 300"
                       @click.stop/>
            <el-tooltip class="item" effect="dark" content="脚本步骤会统计到场景执行结果中，执行报错时会影响场景的最终执行结果" placement="right">
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
      preTitle: "单个请求步骤前执行",
      postTitle: "单个请求步骤后执行",
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
        {value: true, label: "步骤内前置脚本后"},
        {value: false, label: "步骤内前置脚本前"},
      ],
      scriptPostExecSort: [
        {value: true, label: "步骤内后置脚本后"},
        {value: false, label: "步骤内后置脚本前"},
      ],
    }
  },
  created() {
    if (this.isPreProcessor) {
      this.scriptExecSort = this.scriptPreExecSort;
      this.title = this.preTitle;
    } else {
      this.scriptExecSort = this.scriptPostExecSort;
      this.title = this.postTitle;
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
