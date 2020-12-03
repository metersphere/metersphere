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
           @click="active(request)"/>
        <span>{{request.type!= 'create' ? request.name:''}} </span>

        <el-button size="mini" type="danger" icon="el-icon-delete" circle @click="remove" style="margin-right: 20px; float: right"/>

      </el-row>
      <!-- 请求参数-->
      <el-collapse-transition>
        <div v-if="request.active">
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

  export default {
    name: "MsApiComponent",
    props: {
      request: {},
      node: {},
      currentProject: {},
    },
    components: {MsSqlBasisParameters, MsTcpBasisParameters, MsDubboBasisParameters, MsApiRequestForm},
    data() {
      return {loading: false,}
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

</style>
