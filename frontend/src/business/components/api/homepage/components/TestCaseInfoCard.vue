<template>
  <el-card class="table-card" v-loading="result.loading">
    <template v-slot:header>
      <span class="title">
        {{$t('api_test.home_page.test_case_count_card.title')}}
      </span>
    </template>
    <el-container>
      <el-aside width="40%">
        <span class="countNumber">
          {{testCaseCountData.allApiDataCountNumber}}
        </span>
        {{$t('api_test.home_page.unit_of_measurement')}}
      </el-aside>
      <el-container>
        <el-main>
          <div class="text item">
            {{'HTTP: '+testCaseCountData.httpApiDataCountNumber}}
          </div>
          <div class="text item">
            {{'RPC:'+testCaseCountData.rpcApiDataCountNumber}}
          </div>
          <div class="text item">
            {{'TCP:'+testCaseCountData.tcpApiDataCountNumber}}
          </div>
          <div class="text item">
            {{'SQL:'+testCaseCountData.sqlApiDataCountNumber}}
          </div>
        </el-main>
      </el-container>
    </el-container>
  </el-card>
</template>

<script>

export default {
  name: "MsTestCaseInfoCard",

  components: {},

  data() {
    return {
      result: {},
      loading: false
    }
  },
  props:{
    testCaseCountData:{},
  },
  methods: {
  },

  created() {
  },
  activated() {
  }
}
</script>
<style scoped>
.el-aside {
  line-height: 100px;
  text-align: center;
}

.countNumber{
  font-size: 40px;
}
</style>
