<template>
  <div @click="exe" class="recycle">
    <el-row>
      <el-col :span="21"><i class="el-icon-delete"> {{ $t('commons.trash') }}</i></el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: "ModuleTrashButton",
  props: {
    condition: {
      type: Object,
      default() {
        return {};
      }
    },
    exe: {
      type: Function
    },
    total: Number,
  },
  methods: {
    // enableTrash() {
    //
    // }
  }
}
</script>

<style scoped>

.recycle {
  height: 40px;
  position: absolute;
  bottom: 0;
  left: 0;
  margin: 0;
  width: 100%;
  border-top: 1px solid rgba(31, 35, 41, 0.15);
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #1F2329;
  flex: none;
  order: 3;
  flex-grow: 0;
  overflow-x: hidden;
  overflow-y: hidden;
}

.recycle:hover {
  background: rgba(31, 35, 41, 0.1);;
}

.recycle:hover {
  color: #6d317c;
  cursor: pointer;
}

i.el-icon-delete {
  margin-top: 10px;
  margin-left: 24px;
}

.el-icon-delete:before {
  margin-right: 4px;
  font-size: 14px;
}
</style>
