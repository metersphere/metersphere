<template>
  <el-card class="table-card" v-loading="result.loading">
    <template v-slot:header>
      <span class="title">
        {{$t('api_test.home_page.test_scene_count_card.title')}}
      </span>
    </template>
    <el-container>
      <el-aside width="40%">
        <span class="countNumber">
          {{sceneCountData.allApiDataCountNumber}}
        </span>
        {{$t('api_test.home_page.unit_of_measurement')}}
      </el-aside>
      <el-container>
        <el-main>
          <div class="text item">
          </div>
          <div class="text item">
          </div>
          <div class="text item">
          </div>
          <div class="text item">
          </div>
        </el-main>
      </el-container>
    </el-container>
  </el-card>
</template>

<script>

export default {
  name: "MsSceneInfoCard",

  components: {},

  data() {
    return {
      result: {},
      loading: false
    }
  },

  props:{
    sceneCountData:{},
  },

  methods: {
  },
}
</script>
<style scoped>
.el-aside {
  /*background-color: #D3DCE6;*/
  /*color: #333;*/
  text-align: center;
  line-height: 100px;
}

.countNumber{
  font-size: 40px;
}
</style>
