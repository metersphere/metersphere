<template>
  <ms-aside-container :width="width">
    <div class="title-bar">
      <span class="title-left">{{title}}</span>
      <span class="title-right">
        <i class="el-icon-plus" @click="addFuc"/>
      </span>
    </div>

    <div v-for="(item, index) in data" :key="index" class="item-bar" @click="itemSelected(index, item)" :class="{'item-selected' : index == selectIndex}">
      <span class="item-left">{{item.name}}</span>
      <span class="item-right">
        <i class="el-icon-delete" @click="deleteFuc(item)"/>
      </span>
    </div>
  </ms-aside-container>
</template>

<script>
    import MsAsideContainer from "./MsAsideContainer";

    export default {
      name: "MsAsideItem",
      components: {MsAsideContainer},
      data() {
        return {
          selectIndex: -1
        }
      },
      props: {
        width: {
          type: String,
          default: '200px'
        },
        title: String,
        data: Array,
        deleteFuc: Function,
        addFuc: Function,
      },
      methods: {
        itemSelected(index, item) {
          this.selectIndex = index;
          this.$emit('itemSelected', item);
        }
      }
    }
</script>

<style scoped>

  .title-bar {
    width: 100%;
    background: #e9ebef;
    height: 40px;
    padding: 5px 10px;
    box-sizing: border-box;
  }

  .title-bar span {
    line-height: 30px;
  }

  .item-bar {
    width: 100%;
    background: #F9F9F9;
    height: 35px;
    padding: 5px 10px;
    box-sizing: border-box;
  }

  .item-bar span {
    line-height: 25px;
  }

  .title-right,.item-right {
    float: right;
  }

  .item-right {
    visibility: hidden;
  }

  .ms-aside-container {
    padding: 0;
  }

  i:hover {
    color: #409EFF;
    font-size: large;
  }

  .item-bar:hover .item-right {
    visibility: visible;
  }

  .item-selected {
    background: #edf6fd;
  }

  .item-selected .item-right {
    visibility: visible;
  }

</style>
