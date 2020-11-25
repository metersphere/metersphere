<template>
  <!-- 操作按钮 -->
  <div style="background-color: white;">
    <el-row>
      <el-col>
        <!--操作按钮-->
        <div style="float: right;margin-right: 20px;margin-top: 20px">
          <el-button type="primary" size="small" @click="validateApi">{{$t('commons.save')}}</el-button>
          <el-button type="primary" size="small" @click="runTest">{{$t('commons.test')}}</el-button>
        </div>
      </el-col>
    </el-row>
    <!-- 基础信息 -->
    <p class="tip">{{$t('test_track.plan_view.base_info')}} </p>
    <br/>
    <el-row>
      <el-col>
        <ms-basis-api :moduleOptions="moduleOptions" :basisData="basisData" ref="basicForm" @callback="saveApi"/>
      </el-col>
    </el-row>

    <!-- 请求参数 -->
    <p class="tip">{{$t('api_test.definition.request.req_param')}} </p>
    <ms-basis-parameters :request="request" :currentProject="currentProject"/>

  </div>
</template>

<script>
  import MsBasisApi from "./BasisApi";
  import MsBasisParameters from "../request/database/BasisParameters";

  export default {
    name: "MsApiSqlRequestForm",
    components: {
      MsBasisApi, MsBasisParameters
    },
    props: {
      request: {},
      basisData: {},
      currentProject: {},
      moduleOptions: Array,
      isReadOnly: {
        type: Boolean,
        default: false
      }
    },

    data() {
      return {}
    },
    methods: {
      validateApi() {
        if (this.currentProject === null) {
          this.$error(this.$t('api_test.select_project'), 2000);
          return;
        }
        this.$refs['basicForm'].validate();
      },
      saveApi() {
        this.basisData.method = this.basisData.protocol;
        this.$emit('saveApi', this.basisData);
      },
      runTest() {

      },
    },
  }
</script>

<style scoped>
  .tip {
    padding: 3px 5px;
    font-size: 16px;
    border-radius: 4px;
    border-left: 4px solid #783887;
    margin: 0px 20px 0px;
  }
</style>
