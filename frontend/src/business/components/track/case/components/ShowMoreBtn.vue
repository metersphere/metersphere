<template>
  <div v-if="isShow">
    <el-dropdown placement="bottom" trigger="click" size="medium">
      <div @click.stop class="show-more-btn">
        <el-tooltip popper-class="batch-popper" :value="true && !hasShowed" effect="dark" :content="$t('test_track.case.batch_operate')"
                    placement="top-start">
          <i class="el-icon-more ms-icon-more table-more-icon"/>
        </el-tooltip>
      </div>
      <el-dropdown-menu slot="dropdown" class="dropdown-menu-class">
        <div class="show-more-btn-title">{{$t('test_track.case.batch_handle', [size])}}</div>
        <el-dropdown-item v-for="(btn,index) in buttons" :disabled="isDisable(btn)" :key="index" @click.native.stop="click(btn)">
          {{btn.name}}
        </el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
  </div>
</template>

<script>
  import {hasPermissions} from "@/common/js/utils";

  export default {
    name: "ShowMoreBtn",
    data() {
      return {
        disabled: false,
      };
    },
    props: {
      isShow: {
        type: Boolean,
        default: false
      },
      buttons: Array,
      row: Object,
      size: Number,
      hasShowed: Boolean
    },
    created() {
      if (this.trashEnable) {
        this.buttons.splice(1, 1);
      }
    },
    methods: {
      click(btn) {
        if (btn.handleClick instanceof Function) {
          btn.handleClick();
        }
      },
      isDisable(item) {
        if (item.permissions && item.permissions.length > 0) {
          return !hasPermissions(...item.permissions);
        }
        return false;
      }
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
<style>
.batch-popper {
  top: -10000px !important;
}
</style>
