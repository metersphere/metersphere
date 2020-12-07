<template>
  <div v-loading="result.loading" class="search-list">
    <el-input placeholder="搜索项目"
              prefix-icon="el-icon-search"
              v-model="search_text"
              clearable
              class="search-input"
              size="small"/>
    <div v-if="items.length === 0" style="text-align: center; margin: 15px 0">
        <span style="font-size: 15px; color: #8a8b8d;">
          无数据
        </span>
    </div>
    <div v-else style="height: 120px;overflow: auto">
      <el-menu-item :key="i.id" v-for="i in items" :index="getIndex(i)" :route="getRouter(i)">
        <template slot="title">
          <div class="title">{{ i.name }}</div>
        </template>
      </el-menu-item>
    </div>

  </div>
</template>

<script>
import {hasRoles} from "@/common/js/utils";
import {ROLE_TEST_MANAGER, ROLE_TEST_USER, ROLE_TEST_VIEWER} from "@/common/js/constants";

export default {
  name: "SearchList",
  props: {
    options: Object
  },
  mounted() {
    this.recent();
  },
  data() {
    return {
      result: {},
      items: [],
      search_text: ''
    }
  },
  watch: {
    search_text(val) {
      if (!val) {
        this.recent();
      } else {
        this.search();
      }
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
        if (this.options.router) {
          return this.options.router(item);
        }
      }
    }
  },

  methods: {
    recent: function () {
      if (hasRoles(ROLE_TEST_VIEWER, ROLE_TEST_USER, ROLE_TEST_MANAGER)) {
        this.result = this.$get(this.options.url, (response) => {
          this.items = response.data;
          this.items = this.items.splice(0, 3);
        });
      }
    },
    search() {
      if (hasRoles(ROLE_TEST_VIEWER, ROLE_TEST_USER, ROLE_TEST_MANAGER)) {
        this.result = this.$post("/project/search", {name: this.search_text},response => {
          this.items = response.data;
        })
      }
    }
  }
}
</script>

<style scoped>

.search-input {
  padding: 0;
  margin-top: -5px;
}

.search-input >>> .el-input__inner {
  border-radius: 0;
}

.title {
  display: inline-block;
  padding-left: 20px;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
