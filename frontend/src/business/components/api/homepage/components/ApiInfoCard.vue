<template>
  <el-card class="table-card" v-loading="result.loading">
    <template v-slot:header>
      <span class="title">
        {{$t('api_test.home_page.api_count_card.title')}}
      </span>
    </template>
    <el-container>
      <el-aside width="40%">
        <span class="countNumber">
          {{apiCountData.allApiDataCountNumber}}
        </span>
        {{$t('api_test.home_page.unit_of_measurement')}}
        </el-aside>
      <el-container>
        <el-main>
          <div class="text item">
            {{'HTTP: '+apiCountData.httpApiDataCountNumber}}
          </div>
          <div class="text item">
            {{'RPC:'+apiCountData.rpcApiDataCountNumber}}
          </div>
          <div class="text item">
            {{'TCP:'+apiCountData.tcpApiDataCountNumber}}
          </div>
          <div class="text item">
            {{'SQL:'+apiCountData.sqlApiDataCountNumber}}
          </div>
        </el-main>
      </el-container>
    </el-container>
  </el-card>
</template>

<script>

export default {
  name: "MsApiInfoCard",

  components: {},

  data() {
    return {
      result: {},
      loading: false
    }
  },
  props:{
    apiCountData:{},
  },
}
</script>
<style>
.el-aside {
  /*background-color: #D3DCE6;*/
  /*color: #333;*/
  text-align: center;
  /*line-height: 20;*/
}
.countNumber{
  font-size: 40px;
}
</style>
