<template>
  <div class="request-result">
    <div @click="active">
      <el-row :gutter="10" type="flex" align="middle" class="info">
        <el-col :span="4">
          <div class="method">
            {{request.method}}
          </div>
        </el-col>
        <el-col :span="12">
          <div class="name">{{request.name}}</div>
          <div class="url">{{request.url}}</div>
        </el-col>
        <el-col :span="2">
          <div class="time">
            {{request.responseResult.responseTime}}
          </div>
        </el-col>
        <el-col :span="2">
          {{request.error}}
        </el-col>
        <el-col :span="2">
          {{assertion}}
        </el-col>
        <el-col :span="2">
          <el-tag size="mini" type="success" v-if="request.success">
            {{$t('api_report.success')}}
          </el-tag>
          <el-tag size="mini" type="danger" v-else>
            {{$t('api_report.fail')}}
          </el-tag>
        </el-col>
      </el-row>
    </div>
    <el-collapse-transition>
      <div v-show="isActive">
        <ms-request-metric :request="request"/>
        <ms-request-text :request="request"/>
        <br>
        <ms-response-text :response="request.responseResult"/>
      </div>
    </el-collapse-transition>
  </div>
</template>

<script>
  import MsRequestMetric from "./RequestMetric";
  import MsAssertionResults from "./AssertionResults";
  import MsRequestText from "./RequestText";
  import MsResponseText from "./ResponseText";

  export default {
    name: "MsRequestResult",
    components: {MsResponseText, MsRequestText, MsAssertionResults, MsRequestMetric},
    props: {
      request: Object
    },

    data() {
      return {
        isActive: false,
        activeName: "request",
        activeName2: "body",
        activeName3: "body",
      }
    },

    methods: {
      active() {
        this.isActive = !this.isActive;
      }
    },

    computed: {
      assertion() {
        return this.request.passAssertions + " / " + this.request.totalAssertions;
      },
    }
  }
</script>

<style scoped>
  .request-result {
    width: 100%;
    min-height: 40px;
    padding: 2px 0;
  }

  .request-result .info {
    background-color: #F9F9F9;
    margin-left: 20px;
    cursor: pointer;
  }

  .request-result .method {
    /*border-left: 5px solid #1E90FF;*/
    color: #1E90FF;
    font-size: 14px;
    font-weight: 500;
    line-height: 40px;
    padding-left: 5px;
  }

  .request-result .url {
    color: #7f7f7f;
    font-size: 12px;
    font-weight: 400;
    margin-top: 4px;
    overflow: auto;
    white-space: normal;
    word-wrap: break-word;
  }

  .request-result .tab .el-tabs__header {
    margin: 0;
  }

  .request-result .text {
    height: 300px;
    overflow-y: auto;
  }
</style>
