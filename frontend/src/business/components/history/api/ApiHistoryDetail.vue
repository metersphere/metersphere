<template>

  <el-dialog :close-on-click-modal="false" :title="$t('operating_log.info')" :visible.sync="infoVisible" width="900px" :destroy-on-close="true"
             @close="handleClose" append-to-body>
    <div style="height: 700px;overflow: auto">
      <div v-if="detail.createUser">
        <p class="tip">{{ this.$t('report.user_name') }} ：{{ detail.createUser }}</p>
      </div>
      <div>
        <p class="tip">{{ this.$t('operating_log.time') }} ：{{ detail.operTime | timestampFormatDate }}</p>
      </div>
      <div style="overflow: auto">
        <p class="tip">{{ this.$t('report.test_log_details') }} </p>
        <ms-api-http-request-params :request="detail" v-if="detail.type === 'HTTPSamplerProxy' || detail.type === 'HTTP'"/>
        <ms-api-tcp-parameters :request="detail" v-if="detail.type === 'TCPSampler'"/>
        <ms-api-jdbc-parameters :request="detail" v-if="detail.type === 'JDBCSampler'"/>
        <ms-api-dubbo-parameters :request="detail" v-if="detail.type === 'DubboSampler'"/>
      </div>
    </div>
  </el-dialog>
</template>

<script>

import MsApiHttpRequestParams from "./ApiHttpRequestParams";
import MsApiTcpParameters from "./ApiTcpParameters";
import MsApiJdbcParameters from "./ApiJdbcParameters";
import MsApiDubboParameters from "./ApiDubboParameters";

import {getUUID} from "@/common/js/utils";
import Convert from "@/business/components/common/json-schema/convert/convert";

export default {
  name: "MsApiHistoryDetail",
  components: {MsApiHttpRequestParams, MsApiTcpParameters, MsApiJdbcParameters, MsApiDubboParameters},
  props: {
    title: String,
  },
  data() {
    return {
      infoVisible: false,
      detail: {headerId: getUUID(), body: {}, type: ""},
    }
  },
  methods: {
    handleClose() {
      this.infoVisible = false;
    },
    open(value) {
      this.infoVisible = true;
      this.detail = value;
      let diffValue = value.diffValue;
      if (diffValue) {
        if (value != null && value.diffValue != 'null' && value.diffValue != '' && value.diffValue != undefined) {
          if (Object.prototype.toString.call(value.diffValue).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'object'
            && Object.prototype.toString.call(value.diffValue).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'array') {
            diffValue = JSON.parse(value.diffValue);
          }
        }
        if (diffValue.type === 'HTTPSamplerProxy') {
          this.formatHttp(diffValue);
        } else if (diffValue.type === 'TCPSampler') {
          this.formatTcp(diffValue);
        } else if (diffValue.type === 'JDBCSampler') {
          this.formatJdbc(diffValue);
        } else if (diffValue.type === 'DubboSampler') {
          this.formatDubbo(diffValue);
        } else {
          this.formatHttp(diffValue);
        }
        this.detail.type = diffValue.type;
      }
    },
    formatJson(properties) {
      if (properties) {
        for (let key in properties) {
          let value = JSON.stringify(properties[key].mock);
          if (value && value.indexOf("**mock") !== -1) {
            properties["++" + key] = JSON.parse(JSON.stringify(properties[key]));
            properties["--" + key] = JSON.parse(JSON.stringify(properties[key]));
            properties["--" + key].mock = {mock: JSON.parse(value)["**mock"]};
            this.$delete(properties, key);
          }
          if (properties[key] && properties[key]["++description"]) {
            properties["++" + key] = JSON.parse(JSON.stringify(properties[key]));
            properties["++" + key].description = properties[key]["++description"];
            properties["--" + key] = JSON.parse(JSON.stringify(properties[key]));
            this.$delete(properties, key);
          }
          if (properties[key] && properties[key]["**description"]) {
            properties["++" + key] = JSON.parse(JSON.stringify(properties[key]));
            properties["--" + key] = JSON.parse(JSON.stringify(properties[key]));
            properties["--" + key].description = properties[key]["**description"];
            this.$delete(properties, key);
          }
          if (properties[key] && properties[key].properties) {
            this.formatJson(properties[key].properties);
          }
        }
      }
    },
    removeBlankLines(array) {
      if (array && array.length > 0 && !array[array.length - 1].name && !array[array.length - 1].value) {
        array.splice(array.length - 1, 1);
      }
    },
    formatHttp(diffValue) {
      this.detail.body = {};
      this.detail.headerId = getUUID();
      if (diffValue.body) {
        let json = (JSON.parse(diffValue.body));
        if (json && json.jsonSchema && json.jsonSchema.properties) {
          this.formatJson(json.jsonSchema.properties);
          this.detail.body.jsonSchema = json.jsonSchema;
        } else if (json && json["++jsonSchema"]) {
          this.detail.body.jsonSchema = json["++jsonSchema"];
        }
        this.detail.headerId = getUUID();
      }
      if (diffValue.body_form) {
        let form = (JSON.parse(diffValue.body_form)).root;
        this.removeBlankLines(form);
        this.detail.body.form = form;
        this.detail.headerId = getUUID();
      }
      if (diffValue.body_raw_1 || diffValue.body_raw_2) {
        this.detail.body.raw_1 = diffValue.body_raw_1;
        this.detail.body.raw_2 = diffValue.body_raw_2;
        this.detail.headerId = getUUID();
      }
      if (diffValue.header) {
        let header = (JSON.parse(diffValue.header)).root;
        this.removeBlankLines(header);
        this.detail.header = header;
        this.detail.headerId = getUUID();
      }
      if (diffValue.statusCode) {
        let statusCode = (JSON.parse(diffValue.statusCode)).root;
        this.removeBlankLines(statusCode);
        this.detail.statusCode = statusCode;
        this.detail.headerId = getUUID();
      }
      if (diffValue.query) {
        let query = (JSON.parse(diffValue.query)).root;
        this.removeBlankLines(query);
        this.detail.query = query;
        this.detail.headerId = getUUID();
      }
      if (diffValue.rest) {
        let rest = (JSON.parse(diffValue.rest)).root;
        this.removeBlankLines(rest);
        this.detail.rest = rest;
        this.detail.headerId = getUUID();
      }
      if (diffValue.body_auth) {
        this.detail.body_auth = JSON.parse(diffValue.body_auth);
        this.detail.headerId = getUUID();
      }
      if (diffValue.body_config) {
        this.detail.body_config = JSON.parse(diffValue.body_config);
        this.detail.headerId = getUUID();
      }
    },
    formatTcp(diffValue) {
      if (!this.detail.body) {
        this.detail.body = {};
      }
      if (diffValue.query) {
        let parameters = (JSON.parse(diffValue.query)).root;
        this.removeBlankLines(parameters);
        this.detail.parameters = parameters;
        this.detail.headerId = getUUID();
      }
      if (diffValue.body_json) {
        const MsConvert = new Convert();
        let data = MsConvert.format(JSON.parse(diffValue.body_json));
        this.detail.body.jsonSchema = data;
        this.detail.headerId = getUUID();
      }
      if (diffValue.body_xml) {
        let parameters = (JSON.parse(diffValue.body_xml)).root;
        this.removeBlankLines(parameters);
        this.detail.body.xml = parameters;
        this.detail.body.xml_1 = diffValue.body_xml_1;
        this.detail.body.xml_2 = diffValue.body_xml_2;
        this.detail.headerId = getUUID();
      }
      if (diffValue.body_raw_1 || diffValue.body_raw_2) {
        this.detail.body.raw_1 = diffValue.body_raw_1 ? diffValue.body_raw_1 : "";
        this.detail.body.raw_2 = diffValue.body_raw_2 ? diffValue.body_raw_2 : "";
        this.detail.headerId = getUUID();
      }
      if (diffValue.body_xml) {
        this.detail.body.xml = diffValue.body_xml;
        this.detail.headerId = getUUID();
      }
      if (diffValue.script_1 || diffValue.script_2) {
        this.detail.script_1 = diffValue.script_1;
        this.detail.script_2 = diffValue.script_2;
        this.detail.headerId = getUUID();
      }
    },
    formatJdbc(diffValue) {
      if (diffValue.base) {
        let parameters = JSON.parse(diffValue.base);
        this.detail.base = parameters;
        this.detail.headerId = getUUID();
      }
      if (diffValue.variables) {
        let parameters = (JSON.parse(diffValue.variables)).root;
        this.removeBlankLines(parameters);
        this.detail.variables = parameters;
        this.detail.headerId = getUUID();
      }
      if (diffValue.query_1 || diffValue.query_2) {
        this.detail.query_1 = diffValue.query_1;
        this.detail.query_2 = diffValue.query_2;
        this.detail.headerId = getUUID();
      }
    },
    formatDubbo(diffValue) {
      if (diffValue.config) {
        this.detail.config = JSON.parse(diffValue.config);
        this.detail.headerId = getUUID();
      }
      if (diffValue.registry) {
        this.detail.registry = JSON.parse(diffValue.registry);
        this.detail.headerId = getUUID();
      }
      if (diffValue.service) {
        this.detail.service = JSON.parse(diffValue.service);
        this.detail.headerId = getUUID();
      }
      if (diffValue.args) {
        let parameters = (JSON.parse(diffValue.args)).root;
        this.removeBlankLines(parameters);
        this.detail.args = parameters;
        this.detail.headerId = getUUID();
      }
      if (diffValue.attachment) {
        let parameters = (JSON.parse(diffValue.attachment)).root;
        this.removeBlankLines(parameters);
        this.detail.attachment = parameters;
        this.detail.headerId = getUUID();
      }
    },
  }
}
</script>

<style scoped>
</style>
