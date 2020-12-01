<template>
  <div v-loading="loading">
    <el-card>
      <el-row>
        <div class="el-step__icon is-text ms-api-col" v-if="data.referenced">
          <div class="el-step__icon-inner">{{data.$treeNodeId}}</div>
        </div>
        <div class="el-step__icon is-text ms-api-col-create" v-else>
          <div class="el-step__icon-inner">{{data.$treeNodeId}}</div>
        </div>

        <i class="icon el-icon-arrow-right" :class="{'is-active': data.active}"
           @click="active(data)"/>
        <span>{{data.type!= 'create' ? data.name:''}} </span>
      </el-row>
      <!-- 请求参数-->
      <el-collapse-transition>
        <div v-if="data.active">
          <p class="tip">{{$t('api_test.definition.request.req_param')}} </p>
          <ms-api-request-form :headers="data.request.hashTree[0].headers " :request="data.request" v-if="data.protocol==='HTTP'"/>
          <ms-tcp-basis-parameters :request="data.request" :currentProject="currentProject" v-if="data.protocol==='TCP'"/>
          <ms-sql-basis-parameters :request="data.request" :currentProject="currentProject" v-if="data.protocol==='SQL'"/>
          <ms-dubbo-basis-parameters :request="data.request" :currentProject="currentProject" v-if="data.protocol==='DUBBO'"/>
          <!-- 保存操作 -->
          <el-button type="primary" size="small" style="margin: 20px; float: right" @click="saveTestCase(item)" v-if="!data.referenced">
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
      data: {},
      currentProject: {},
    },
    components: {MsSqlBasisParameters, MsTcpBasisParameters, MsDubboBasisParameters, MsApiRequestForm},
    data() {
      return {loading: false,}
    },
    methods: {
      remove() {
        this.$emit('remove', this.data);
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
