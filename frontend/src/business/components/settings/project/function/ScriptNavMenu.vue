<template>
  <div style="line-height: 20px;">
    <div class="template-title">{{ $t('api_test.request.processor.code_template') }}</div>
    <div v-for="(menu, index) in menus" :key="index">
      <span class="link-type"><i class="el-icon-arrow-right" style="font-weight: bold;"></i> {{menu.title}}</span>
      <div v-for="(child, key) in menu.children" :key="key">
        <el-link :disabled="child.disabled" @click="handleClick(child)" class="func-link">{{child.title}}</el-link>
      </div>
    </div>
    <el-link href="https://jmeter.apache.org/usermanual/component_reference.html#BeanShell_PostProcessor"
             target="componentReferenceDoc" style="margin-top: 10px; margin-left: 20px;"
             type="primary">{{ $t('commons.reference_documentation') }}
    </el-link>
    <custom-function-relate ref="customFunctionRelate" @addCustomFuncScript="handleCodeTemplate"/>
    <!--接口列表-->
    <api-func-relevance @save="apiSave" @close="apiClose" ref="apiFuncRelevance"/>
  </div>

</template>

<script>
import ApiFuncRelevance from "@/business/components/settings/project/function/ApiFuncRelevance";
import CustomFunctionRelate from "@/business/components/settings/project/function/CustomFunctionRelate";
import {getCodeTemplate} from "@/business/components/settings/project/function/custom-function";
import {SCRIPT_MENU} from "@/business/components/settings/project/function/script-menu";

export default {
  name: "ScriptNavMenu",
  components: {
    ApiFuncRelevance,
    CustomFunctionRelate
  },
  data() {
    return {
      value: true
    }
  },
  props: {
    language: {
      type: String,
      default() {
        return "beanshell"
      }
    },
    menus: {
      type: Array,
      default() {
        return SCRIPT_MENU
      }
    }
  },
  methods: {
    apiSave(data, env) {
      // data：选中的多个接口定义或多个接口用例; env: 关联页面选中的环境
      let condition = env.config.httpConfig.conditions || [];
      let protocol = "";
      let host = "";
      if (condition && condition.length > 0) {
        // 如果有多个环境，取第一个
        protocol = condition[0].protocol ? condition[0].protocol : "http";
        host = condition[0].socket;
      }
      // todo
      if (data.length > 5) {
        this.$warning("最多可以选择5个接口！");
        return;
      }
      let code = "";
      if (data.length > 0) {
        data.forEach(dt => {
          let param = this._parseRequestObj(dt);
          param['host'] = host;
          param['protocol'] = protocol;
          code += '\n' + getCodeTemplate(this.language, param);
        })
      }
      this.handleCodeTemplate(code);
      this.$refs.apiFuncRelevance.close();
    },
    handleCodeTemplate(code) {
      this.$emit("handleCode", code);
    },
    _parseRequestObj(data) {
      let requestHeaders = new Map();
      let requestMethod = "";
      let requestBody = "";
      let requestPath = "";
      let request = JSON.parse(data.request);
      // 拼接发送请求需要的参数
      requestPath = request.path;
      requestMethod = request.method;
      let headers = request.headers;
      if (headers && headers.length > 0) {
        headers.forEach(header => {
          if (header.name) {
            requestHeaders.set(header.name, header.value);
          }
        })
      }
      let body = request.body;
      if (body.json) {
        requestBody = body.raw;
      }
      return {requestPath, requestHeaders, requestMethod, requestBody}
    },
    apiClose() {

    },
    handleClick(obj) {
      let code = "";
      if (obj.command) {
        code = this._handleCommand(obj.command);
        if (!code) {
          return;
        }
      } else {
        code = obj.value;
      }
      this.handleCodeTemplate(code);
    },
    _handleCommand(command) {
      switch (command) {
        case 'custom_function':
          this.$refs.customFunctionRelate.open(this.language);
          return "";
        case 'api_definition':
          this.$refs.apiFuncRelevance.open();
          return "";
        case 'new_api_request': {
          // requestObj为空则生产默认模版
          let headers = new Map();
          headers.set('Content-type', 'application/json');
          return getCodeTemplate(this.language, {requestHeaders: headers});
        }
        default:
          return "";
      }
    }
  }
}
</script>

<style scoped>

.template-title {
  margin-bottom: 4px;
  font-weight: bold;
  font-size: 15px;
  margin-left: 15px;
}

.func-link {
  color: #935aa1;
  margin-left: 18px;
}

.link-type {
  font-weight: bold;
  font-size: 14px;
}
</style>
