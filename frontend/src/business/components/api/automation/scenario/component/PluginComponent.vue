<template>
  <api-base-component
    @copy="copyRow"
    @remove="remove"
    @active="active"
    :data="request"
    :draggable="draggable"
    :color="defColor"
    :is-max="isMax"
    :show-btn="showBtn"
    :background-color="defBackgroundColor"
    :title="request.type">

    <template v-slot:request>
      <legend style="width: 100%">
        <p class="tip">{{ $t('api_test.definition.request.req_param') }} </p>
        <div class="ms-form">
          <div class="ms-form-create" v-loading="loading">
            <formCreate
              v-model="pluginForm"
              :rule="rules"
              :option="option"
              :value.sync="data"
              @prefix-change="change"
              @prefix-click="change"
              @prefix-visible-change="visibleChange"
            />
          </div>
        </div>
      </legend>
    </template>

    <template v-slot:debugStepCode>
       <span v-if="request.testing" class="ms-test-running">
         <i class="el-icon-loading" style="font-size: 16px"/>
         {{ $t('commons.testing') }}
       </span>
      <span class="ms-step-debug-code" :class="request.requestResult[0].success?'ms-req-success':'ms-req-error'" v-if="!loading && request.debug && request.requestResult[0] && request.requestResult[0].responseResult">
          {{ request.requestResult[0].success ? 'success' : 'error' }}
        </span>
    </template>

    <!-- 这个不确定是否所以组件都有-->
    <template v-slot:result>
      <p class="tip">{{ $t('api_test.definition.request.res_param') }} </p>
      <div v-if="request.result">
        <div v-for="(scenario,h) in request.result.scenarios" :key="h">
          <el-tabs v-model="request.activeName" closable class="ms-tabs">
            <el-tab-pane v-for="(item,i) in scenario.requestResults" :label="'循环'+(i+1)" :key="i" style="margin-bottom: 5px">
              <api-response-component :currentProtocol="request.protocol" :apiActive="true" :result="item"/>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
      <div v-else>
        <el-tabs v-model="request.activeName" closable class="ms-tabs" v-if="request.requestResult && request.requestResult.length > 1">
          <el-tab-pane v-for="(item,i) in request.requestResult" :label="'循环'+(i+1)" :key="i" style="margin-bottom: 5px">
            <api-response-component
              :currentProtocol="request.protocol"
              :apiActive="true"
              :result="item"
            />
          </el-tab-pane>
        </el-tabs>
        <api-response-component
          :currentProtocol="request.protocol"
          :apiActive="true"
          :result="request.requestResult[0]"
          v-else/>
      </div>
    </template>
  </api-base-component>
</template>

<script>
import ApiBaseComponent from "../common/ApiBaseComponent";
import ApiResponseComponent from "./ApiResponseComponent";
import formCreate from "@form-create/element-ui";
import MsUpload from "../common/MsPluginUpload";

formCreate.component("msUpload", MsUpload);

export default {
  name: "PluginComponent",
  components: {ApiBaseComponent, ApiResponseComponent},
  props: {
    draggable: {
      type: Boolean,
      default: false,
    },
    message: String,
    isReadOnly: {
      type: Boolean,
      default:
        false
    },
    isMax: {
      type: Boolean,
      default: false,
    },
    showBtn: {
      type: Boolean,
      default: true,
    },
    request: {
      type: Object,
    },
    defTitle: {type: String, default: "Plugin"},
    defColor: {type: String, default: "#555855"},
    defBackgroundColor: {type: String, default: "#F4F4FF"},
    node: {},
  },
  data() {
    return {
      loading: false,
      pluginForm: {},
      execEntry: "",
      data: {},
      rules: [],
      option: formCreate.parseJson(
        '{"form":{"labelPosition":"right","size":"mini","labelWidth":"120px","hideRequiredAsterisk":false,"showMessage":false,"inlineMessage":false}}'
      ),
    }
  },
  computed: {},
  created() {
    this.getPlugin();
    if (!this.request.requestResult) {
      this.request.requestResult = [];
    }
    this.data = this.request;
  },
  watch: {
    message() {
      this.reload();
    },
    data: {
      handler: function () {
        Object.assign(this.request, this.data);
      },
      deep: true
    }
  },
  methods: {
    blur(d) {
    },
    change(d) {
    },
    getValue(val) {
      let reg = /\{(\w+)\}/gi;
      if (val.indexOf("${") !== -1) {
        let result;
        while ((result = reg.exec(val)) !== null) {
          if (this.pluginForm.getRule(result[1])) {
            val = val.replace("$" + result[0], this.pluginForm.getRule(result[1]).value);
          }
        }
        return val
      }
      return val;
    },
    visibleChange(d) {
      if (d && d.inject) {
        if (this.pluginForm.getRule(d.inject[0])) {
          let req = {entry: this.execEntry, request: this.getValue(d.inject[1])};
          this.$post('/plugin/customMethod/', req, response => {
            if (response.data && this.pluginForm.getRule(d.inject[0]).options) {
              this.pluginForm.getRule(d.inject[0]).options = JSON.parse(response.data);
            }
          })
          this.reload();
        }
      }
    },
    getPlugin() {
      let id = this.request.pluginId;
      if (id) {
        this.$get('/plugin/get/' + id, response => {
          let plugin = response.data;
          this.execEntry = plugin.execEntry;
          if (plugin && plugin.formScript) {
            this.rules = formCreate.parseJson(plugin.formScript);
          }
          if (plugin && plugin.formOption) {
            this.option = formCreate.parseJson(plugin.formOption);
          }
          this.option.submitBtn = {show: false};
          this.request.clazzName = plugin.clazzName;
          if (this.request && this.request.active && this.pluginForm) {
            this.pluginForm.setValue(this.request);
          }
        });
      }
    },
    reload() {
      this.loading = true
      this.$nextTick(() => {
        this.loading = false
      })
    },
    getCode() {
      if (this.node && this.node.data.code && this.node.data.debug) {
        if (this.node.data.code && this.node.data.code === 'error') {
          return 'error';
        } else {
          return 'success';
        }
      }
      return '';
    },
    remove() {
      this.$emit('remove', this.request, this.node);
    },
    copyRow() {
      this.$emit('copyRow', this.request, this.node);
    },
    active() {
      this.request.active = !this.request.active;
      if (this.request && this.request.active && this.pluginForm && this.pluginForm.setValue instanceof Function) {
        this.pluginForm.setValue(this.request);
      }
    },
  }
}
</script>

<style scoped>
/deep/ .el-divider {
  margin-bottom: 10px;
}

.ms-req-error {
  color: #F56C6C;
}

.ms-req-success {
  color: #67C23A;
}

.ms-step-debug-code {
  display: inline-block;
  margin: 0 5px;
  overflow-x: hidden;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 100px;
}

/deep/ .el-select {
  width: 100%;
}

/deep/ .fc-upload-btn {
  width: 30px;
  height: 30px;
  line-height: 30px;
  display: inline-block;
  text-align: center;
  border: 1px solid #c0ccda;
  border-radius: 4px;
  overflow: hidden;
  background: #fff;
  position: relative;
  -webkit-box-shadow: 2px 2px 5px rgb(0 0 0 / 10%);
  box-shadow: 2px 2px 5px rgb(0 0 0 / 10%);
  margin-right: 4px;
  -webkit-box-sizing: border-box;
  box-sizing: border-box;
}

.ms-test-running {
  color: #6D317C;
}

.ms-form {
  border: 1px #DCDFE6 solid;
  height: 100%;
  border-radius: 4px;
  width: 100%;
}

.ms-form-create {
  margin: 10px;
}
</style>
