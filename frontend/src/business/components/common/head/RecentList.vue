<template>
  <div>
    <div class="recent-text">
      <i class="el-icon-time"/>
      <span>{{options.title}}</span>
    </div>
    <el-menu-item :key="i.id" v-for="i in items"
                  :index="getIndex(i)" :route="getRouter(i)">
      <span class="title">{{ i.name }}</span>
    </el-menu-item>
  </div>
</template>

<script>
  import {hasRoles} from "../../../../common/utils";
  import {ROLE_TEST_MANAGER, ROLE_TEST_USER, ROLE_TEST_VIEWER} from "../../../../common/constants";

  export default {
    name: "MsRecentList",
    props: {
      options: Object
    },
    mounted() {
      if (hasRoles(ROLE_TEST_VIEWER, ROLE_TEST_USER, ROLE_TEST_MANAGER)) {
        this.$get(this.options.url, (response) => {
          this.items = response.data;
        });
      }
    },
    data() {
      return {
        items: []
      }
    },
    computed: {
      getIndex: function () {
        return function (item) {
          return this.options.index(item);
        }
      },
      getRouter: function () {
        return function (item) {
          return this.options.router(item);
        }
      }
    }
  }
</script>

<style scoped>
  .recent-text {
    padding: 0 10px;
    margin-top: -5px;
    line-height: 36px;
    color: #777777;
    background-color: #F5F5F5;
  }

  .recent-text span {
    padding-left: 6px;
  }

  .title {
    padding-left: 20px;
  }
</style>
