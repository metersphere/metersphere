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
      <el-menu-item :key="i.id" v-for="i in items" @click="change(i.id)">
        <template slot="title">
          <div class="title">
            {{ i.name }}
            <i class="el-icon-check" v-if="i.id === currentProjectId"></i>
          </div>
        </template>
      </el-menu-item>
    </div>

  </div>
</template>

<script>
import {getCurrentProjectID, getCurrentUser, hasRoles} from "@/common/js/utils";
import {PROJECT_ID, ROLE_TEST_MANAGER, ROLE_TEST_USER, ROLE_TEST_VIEWER} from "@/common/js/constants";

export default {
  name: "SearchList",
  props: {
    options: Object
  },
  mounted() {
    this.init();
  },
  data() {
    return {
      result: {},
      items: [],
      search_text: '',
      userId: getCurrentUser().id,
      currentProjectId: localStorage.getItem(PROJECT_ID)
    }
  },
  watch: {
    search_text(val) {
      if (!val) {
        this.init();
      } else {
        this.search();
      }
    }
  },
  methods: {
    init: function () {
      if (hasRoles(ROLE_TEST_VIEWER, ROLE_TEST_USER, ROLE_TEST_MANAGER)) {
        this.result = this.$get(this.options.url, (response) => {
          this.items = response.data;
          this.items = this.items.splice(0, 3);
          if (!getCurrentProjectID() && this.items.length > 0) {
            this.change(this.items[0].id);
          }
        });
      }
    },
    search() {
      if (hasRoles(ROLE_TEST_VIEWER, ROLE_TEST_USER, ROLE_TEST_MANAGER)) {
        this.result = this.$post("/project/search", {name: this.search_text},response => {
          this.items = response.data;
        })
      }
    },
    change(projectId) {
      this.$post("/user/update/current", {id: this.userId, lastProjectId: projectId}, () => {
        localStorage.setItem(PROJECT_ID, projectId);
        window.location.reload();
      });
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

.el-icon-check {
  color: #773888;
  margin-left: 10px;
}
</style>
