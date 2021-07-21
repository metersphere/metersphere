<template>
  <div>
    <div v-if="request.protocol === 'HTTP'">
      <el-input :placeholder="$t('api_test.definition.request.path_all_info')" v-if="request.url || isCustomizeReq" v-model="request.url"
                style="width: 85%;margin-top: 10px" size="small" @blur="urlChange">
        <el-select v-model="request.method" slot="prepend" style="width: 100px" size="small">
          <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id"/>
        </el-select>
      </el-input>
      <el-input :placeholder="$t('api_test.definition.request.path_all_info')" v-else v-model="request.path"
                style="width: 85%;margin-top: 10px" size="small" @blur="pathChange">
        <el-select v-model="request.method" slot="prepend" style="width: 100px" size="small">
          <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id"/>
        </el-select>
      </el-input>
      <el-checkbox v-if="isCustomizeReq" class="is-ref-environment" v-model="request.isRefEnvironment">{{ $t('api_test.request.refer_to_environment') }}</el-checkbox>
    </div>

    <div v-if="request.protocol === 'TCP' && isCustomizeReq">
      <el-form>
        <el-row>
          <el-col :span="8">
            <el-form-item :label="$t('api_test.request.tcp.server')" prop="server">
              <el-input class="server-input" v-model="request.server" maxlength="300" show-word-limit size="small"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('api_test.request.tcp.port')" prop="port" label-width="60px">
              <el-input-number v-model="request.port" controls-position="right" :min="0" :max="65535" size="small"/>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>
  </div>
</template>

<script>
import {REQ_METHOD} from "@/business/components/api/definition/model/JsonData";
import {KeyValue} from "../../../definition/model/ApiTestModel";

export default {
  name: "CustomizeReqInfo",
  props: ['request', 'isCustomizeReq'],
  data() {
    return {
      reqOptions: REQ_METHOD,
      isUrl: false,
    }
  },
  mounted() {
    if (this.isCustomizeReq) {
      if (this.request.isRefEnvironment === undefined || this.request.isRefEnvironment === null) {
        // 兼容旧数据
        if (this.request.url) {
          this.$set(this.request, 'isRefEnvironment', false);
        } else {
          this.$set(this.request, 'isRefEnvironment', true);
        }
      }
      // url和path设置成一样，根据是否引用环境判断用哪个
      if (this.request.url) {
        this.$set(this.request, 'path', this.request.url);

      } else {
        this.$set(this.request, 'url', this.request.path);

      }
    }
  },
  methods: {
    pathChange() {
      this.isUrl = false;
      if (!this.request.path || this.request.path.indexOf('?') === -1) return;
      let url = this.getURL(this.addProtocol(this.request.path));
      if (url && this.isUrl) {
        this.request.path = decodeURIComponent(this.request.path.substr(0, this.request.path.indexOf("?")));
      }
    },
    urlChange() {
      this.isUrl = false;
      if (this.isCustomizeReq) {
        this.request.path = this.request.url;
      }
      if (!this.request.url || this.request.url.indexOf('?') === -1) return;
      let url = this.getURL(this.addProtocol(this.request.url));
      if (url) {
        let paramUrl = this.request.url.substr(this.request.url.indexOf("?") + 1);
        if (paramUrl && this.isUrl) {
          this.request.url = decodeURIComponent(this.request.url.substr(0, this.request.url.indexOf("?")));
        }
      }
    },
    addProtocol(url) {
      if (url) {
        if (!url.toLowerCase().startsWith("https") && !url.toLowerCase().startsWith("http")) {
          return "https://" + url;
        }
      }
      return url;
    },
    getURL(urlStr) {
      try {
        let url = new URL(urlStr);
        url.searchParams.forEach((value, key) => {
          if (key && value) {
            this.isUrl = true;
            this.request.arguments.splice(0, 0, new KeyValue({name: key, required: false, value: value}));
          }
        });
        return url;
      } catch (e) {
        this.$error(this.$t('api_test.request.url_invalid'), 2000);
      }
    },
  }
}
</script>

<style scoped>
.server-input {
  width: 50%;
}

.is-ref-environment {
  margin-left: 15px;
}
</style>
