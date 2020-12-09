<template>
  <div v-loading="loading">
    <el-card>
      <el-row>
        <div class="el-step__icon is-text ms-api-col" v-if="request.referenced">
          <div class="el-step__icon-inner">{{request.index}}</div>
        </div>
        <div class="el-step__icon is-text ms-api-col-create" v-else>
          <div class="el-step__icon-inner">{{request.index}}</div>
        </div>

        <i class="icon el-icon-arrow-right" :class="{'is-active': request.active}"
           @click="active(request)" v-if="request.referenced!=undefined && request.referenced!='Deleted' && request.referenced!='REF'"/>
        <span>{{request.type!= 'create' ? request.name:''}} </span>
        <el-tag size="mini" style="margin-left: 20px" v-if="request.referenced==='Deleted'" type="danger">引用不存在</el-tag>
        <el-tag size="mini" style="margin-left: 20px" v-if="request.referenced ==='REF'">{{ $t('api_test.scenario.reference') }}</el-tag>

        <el-button size="mini" type="danger" icon="el-icon-delete" circle @click="remove" style="margin-right: 20px; float: right"/>

      </el-row>
      <!-- 请求参数-->
      <el-collapse-transition>
        <div v-if="request.active">
          <div v-if="request.protocol === 'HTTP'">
            <el-input :placeholder="$t('api_test.definition.request.path_all_info')" v-model="request.url" style="width: 85%;margin-top: 10px" size="small">
              <el-select v-model="request.method" slot="prepend" style="width: 100px" size="small">
                <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id"/>
              </el-select>
            </el-input>
          </div>
          <p class="tip">{{$t('api_test.definition.request.req_param')}} </p>
          <ms-api-request-form :headers="request.headers " :request="request" v-if="request.protocol==='HTTP'"/>
          <ms-tcp-basis-parameters :request="request" :currentProject="currentProject" v-if="request.protocol==='TCP'"/>
          <ms-sql-basis-parameters :request="request" :currentProject="currentProject" v-if="request.protocol==='SQL'"/>
          <ms-dubbo-basis-parameters :request="request" :currentProject="currentProject" v-if="request.protocol==='DUBBO' || request.protocol==='dubbo://'"/>
          <!-- 保存操作 -->
          <el-button type="primary" size="small" style="margin: 20px; float: right" @click="saveTestCase(item)" v-if="!request.referenced">
            {{$t('commons.save')}}
          </el-button>
        </div>
      </el-collapse-transition>
    </el-card>
  </div>
</template>

<script>
  import MsSqlBasisParameters from "../../definition/components/request/database/BasisParameters";
  import MsTcpBasisParameters from "../../definition/components/request/tcp/BasisParameters";
  import MsDubboBasisParameters from "../../definition/components/request/dubbo/BasisParameters";
  import MsApiRequestForm from "../../definition/components/request/http/ApiRequestForm";
  import {REQ_METHOD} from "../../definition/model/JsonData";

  export default {
    name: "MsApiComponent",
    props: {
      request: {},
      node: {},
      currentProject: {},
    },
    components: {MsSqlBasisParameters, MsTcpBasisParameters, MsDubboBasisParameters, MsApiRequestForm},
    data() {
      return {loading: false, reqOptions: REQ_METHOD,}
    },
    created() {
      if (this.request.protocol === 'HTTP') {
        try {
          let urlObject = new URL(this.request.url);
          let url = urlObject.protocol + "//" + urlObject.host + "/";
          let path = this.request.url.substr(url.length);
          if (!path.startsWith('/')) {
            path = "/" + path;
          }
          this.request.path = path;
        } catch (e) {
          this.request.path = this.request.url;
        }
      }
    },
    methods: {
      remove() {
        this.$emit('remove', this.request, this.node);
      },
      active(item) {
        item.active = !item.active;
        this.reload();
      },
      reload() {
        this.loading = true
        this.$nextTick(() => {
          this.loading = false
        })
      },
    }
  }
</script>

<style scoped>
  .ms-api-col {
    background-color: #FCF1F1;
    border-color: #F56C6C;
    margin-right: 10px;
    color: #F56C6C;
  }

  .ms-api-col-create {
    background-color: #EBF2F2;
    border-color: #008080;
    margin-right: 10px;
    color: #008080;
  }

  /deep/ .el-card__body {
    padding: 15px;
  }

  .icon.is-active {
    transform: rotate(90deg);
  }

  .tip {
    padding: 3px 5px;
    font-size: 16px;
    border-radius: 4px;
    border-left: 4px solid #783887;
    margin: 20px 0;
  }

</style>
