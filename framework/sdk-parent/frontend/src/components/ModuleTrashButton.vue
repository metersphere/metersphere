<template>
  <div @click="exe" class="recycle" :class="{'is-active': condition.trashEnable}">
    <el-row>
      <el-col :span="21"><i class="el-icon-delete"> {{ $t('commons.trash') }}</i></el-col>
<!--      <el-col :span="3"><span style="color: var(&#45;&#45;primary_color);">{{ total }}</span></el-col>-->
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
  padding: 0 0 0 24px;
}

.recycle:hover {
  background: rgba(31, 35, 41, 0.1);;
}

.recycle:hover {
  color: #6d317c;
  cursor: pointer;
}

.is-active {
  background-color: #f3f6f9;
}

i.el-icon-delete {
  margin-top: 10px;
}

.el-icon-delete:before {
  margin-right: 4px;
  font-size: 14px;
}
</style>
