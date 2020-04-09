<template>
  <el-menu :unique-opened="true" mode="horizontal" active-text-color="write"
           class="project_menu">
    <el-submenu index="1" popper-class="submenu">
      <template v-slot:title>
        <span class="menu-title">{{'[' + title + ']'}}</span>
        <span> {{currentData == null ? '' : currentData.name}} </span>
      </template>
      <template v-slot:default>
        <div style="height:400px;">
          <el-scrollbar style="height:100%">
            <label v-for="(item,index) in data" :key="index">
              <el-menu-item @click="changeData(item)">
                {{item.name}}
                <i class="el-icon-check" v-if="currentData && item.id === currentData.id"></i>
              </el-menu-item>
            </label>
          </el-scrollbar>
        </div>
      </template>
    </el-submenu>
  </el-menu>
</template>

<script>
    export default {
      name: "SelectMenu",
      props: {
        data: {
          type: Array
        },
        currentData: {
          type: Object
        },
        title: {
          type: String
        }
      },
      methods: {
        changeData(data) {
          this.$emit("dataChange", data);
        }
      }
    }
</script>

<style scoped>

  .menu-title {
    color: darkgrey;
  }

</style>
