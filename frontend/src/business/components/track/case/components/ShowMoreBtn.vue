<template>
  <div v-if="isShow">
    <el-dropdown placement="bottom" trigger="click" size="medium">
      <div @click.stop="click" class="show-more-btn">
        <i class="el-icon-more ms-icon-more"/>
      </div>
      <el-dropdown-menu slot="dropdown" class="dropdown-menu-class">
        <div class="show-more-btn-title">{{$t('test_track.case.batch_handle', [size])}}</div>
        <el-dropdown-item v-for="(btn,index) in buttons" :key="index" @click.native.stop="clickStop(btn)">
          {{btn.name}}
        </el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
  </div>
</template>

<script>
  export default {
    name: "ShowMoreBtn",
    data() {
      return {}
    },
    methods: {
      click() {
        // console.log("click");
      },
      clickStop(btn) {
        if (btn.stop instanceof Function) {
          btn.stop();
        }
      }
    },
    props: {
      isShow: {
        type: Boolean,
        default: false
      },
      buttons: Array,
      row: Object,
      size: Number
    }
  }
</script>

<style scoped>
  .ms-icon-more {
    transform: rotate(90deg);
  }

  .show-more-btn {
    width: 20px;
    height: 25px;
    line-height: 25px;
    background-color: #FFF;
  }

  .show-more-btn-title {
    color: #696969;
    background-color: #e2e2e2;
    padding: 5px;
  }

  .dropdown-menu-class {
    padding: 1px 0;
    text-align: center;
  }
</style>
