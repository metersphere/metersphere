<template>
  <div class="ms-single-handle-drag">
    <div>
      <slot name="header">
        <el-link class="add-text" :underline="false" :disabled="disable"  @click="add">
          <i class="el-icon-plus">添加选项</i>
        </el-link>
      </slot>
    </div>

    <draggable :list="data" handle=".handle" class="list-group">

      <div class="list-group-item"
        v-for="(element, idx) in data" :key="idx">
        <font-awesome-icon class="handle" :icon="['fas', 'align-justify']"/>

        <el-input size="mini" type="text"
                  class="text-item"
                  v-if="editIndex === idx"
                  @blur="handleEdit(element)"
                  v-model="element.value"/>
        <span class="text-item" v-else>
          <span v-if="element.system">
             {{$t(element.text)}}
          </span>
          <span v-else>
             {{element.text}}
          </span>
        </span>
        <i class="operator-icon" v-for="(item, index) in operators"
           :key="index"
           :class="item.icon"
           :disabled="disable"
           @click="item.click(element, idx)"/>

      </div>
    </draggable>

  </div>
</template>

<script>
import draggable from "vuedraggable";
export default {
  name: "MsSingleHandleDrag",
  components: {
    draggable
  },
  data() {
    return {
      editIndex: -1,
    };
  },
  props: {
    disable: Boolean,
    data: {
      type: Array,
      default() {
        return  [
          { name: "John", text: "text", id: 0 },
        ];
      }
    },
    operators: {
      type: Array,
      default() {
        return  [
          {
            icon: 'el-icon-edit',
            click: (element, idx) => {
              if (this.disable) {
                return;
              }
              if (!element.system) {
                this.editIndex = idx;
              }
            }
          },
          {
            icon: 'el-icon-close',
            click: (element, idx) => {
              if (this.disable) {
                return;
              }
              this.data.splice(idx, 1);
            }
          },
        ];
      }
    }

  },
  methods: {
    add: function() {
      let item = {
        text: "",
        value: ""
      };
      this.data.push(item);
      this.editIndex = this.data.length - 1;
    },
    handleEdit(element) {
      this.editIndex = -1;
      element.text = element.value;
    },
    isSystem(element) {
      if (element.system) {
        return true;
      } else {
        return false;
      }
    }
  }
};
</script>
<style scoped>

.text-item {
  margin: 5px;
  width: 150px;
}

.handle {
}

.operator-icon:hover {
  font-size: 15px;
  font-weight: bold;
}


.operator-icon {
  margin-right: 6px;
}

.operator-icon:first-child {
  margin-left: 20px;
}

.add-text {
  font-size: 10px;
}

.add-text:hover {
  font-size: 13px;
  font-weight: bold;
}
</style>
