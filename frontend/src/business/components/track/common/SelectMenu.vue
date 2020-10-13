<template>
  <div>
    <span class="menu-title">{{'[' + title + ']'}}</span>
    <el-select filterable slot="prepend" v-model="value" @change="changeData" class="project_menu"
               size="small">
      <el-option v-for="(item,index) in data" :key="index" :label="item.name" :value="index"/>
    </el-select>
  </div>
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
    data() {
      return {
        value: ''
      }
    },
    watch: {
      currentData(data) {
        if (data != undefined && data != null) {
          this.value = data.name;
        }
      }
    },
    methods: {
      changeData(index) {
        this.$emit("dataChange", this.data[index]);
      }
    }
  }
</script>

<style scoped>
  .project_menu {
    width: 214px;
  }

  .menu-title {
    color: darkgrey;
    margin-left: 10px;
    margin-right: 10px;
  }

</style>
