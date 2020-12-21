<template>
  <el-card class="table-card" v-loading="result.loading">
    <template v-slot:header>
      <span class="title">
        {{$t('api_test.home_page.test_scene_details_card.title')}}
      </span>
    </template>
    <el-container>
        <el-main>
          <div class="text item">
            {{$t('api_test.home_page.test_scene_details_card.this_week_add',[sceneCountData.thisWeekAddedCount])}}
          </div>
          <div class="text item">
            {{$t('api_test.home_page.test_scene_details_card.this_week_execute',[sceneCountData.thisWeekExecutedCount])}}
          </div>
          <div class="text item">
            {{$t('api_test.home_page.test_scene_details_card.executed',[sceneCountData.executedCount])}}
          </div>
        </el-main>
    </el-container>
  </el-card>
</template>

<script>

export default {
  name: "MsSceneDetailCard",
  components: {},

  data() {
    return {
      result: {},
      loading: false
    }
  },

  methods: {
  },
  props:{
    sceneCountData:{},
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

</style>
