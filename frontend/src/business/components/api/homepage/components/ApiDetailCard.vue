<template>
  <el-card class="table-card" v-loading="result.loading">
    <template v-slot:header>
      <span class="title">
        {{$t('api_test.home_page.api_details_card.title')}}
      </span>
    </template>
    <el-container>
      <el-main>
          <div class="text item">
            <br/>
            {{$t('api_test.home_page.api_details_card.this_week_add',[apiCountData.thisWeekAddedCount])}}
            <br/>
            <br/>
          </div>
      </el-main>
    </el-container>
  </el-card>
</template>

<script>

export default {
  name: "MsApiDetailCard",
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

  methods: {
    search() {
      // this.result = this.$get("/api/apiCount/5", response => {
      //   this.apiCountData = response.data;
      // });
    },
    // link(row) {
    //   this.$router.push({
    //     path: '/api/report/view/' + row.id,
    //   })
    // }
  },

  created() {
    this.search();
  },
  activated() {
    this.search();
  }
}
</script>
<style scoped>

</style>
