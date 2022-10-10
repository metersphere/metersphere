<template>
  <div class="select-menu">
    <span class="menu-title">{{'[' + title + ']'}}</span>
    <el-select filterable slot="prepend" v-model="value" @change="changeData" :style="{width: width}"
               size="small">
      <el-option v-for="(item,index) in data" :key="index" :label="item.name" :value="index">
        <span class="span-name" :title="item.name">{{ item.name }}</span>
      </el-option>
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
      },
      width: {
        type: String,
        default() {
          return "214px";
        }
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

  .menu-title {
    color: darkgrey;
    margin-left: 10px;
    margin-right: 10px;
  }

  .span-name {
    display: inline-block;
    max-width: 300px;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
    word-break: break-all;
    margin-right: 5px;
  }

</style>
