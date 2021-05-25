<template>
  <div class="ms-single-handle-drag">
    <div>
      <slot name="header">
        <el-link class="add-text" :underline="false" :disabled="disable"  @click="add">
          <i class="el-icon-plus">{{$t('custom_field.add_option')}}</i>
        </el-link>
      </slot>
      <ms-instructions-icon size="13" v-if="isKv" :content="$t('选项值用于对接Jira等平台提交缺陷时，对应字段的属性值')"/>
    </div>

    <draggable :list="data" handle=".handle" class="list-group">

      <div class="list-group-item"
        v-for="(element, idx) in data" :key="idx">
        <font-awesome-icon class="handle" :icon="['fas', 'align-justify']"/>

        <el-input size="mini" type="text"
                  class="text-item"
                  :placeholder="$t('custom_field.field_text')"
                  v-if="editIndex === idx && isKv"
                  @blur="handleTextEdit(element)"
                  v-model="element.text"/>
        <span class="text-item" v-else-if="isKv">
          <span v-if="element.system">
             ({{$t(element.text)}})
          </span>
          <span v-else>
             {{element.text}}
          </span>
        </span>

        <el-input size="mini" type="value"
                  class="text-item"
                  :placeholder="$t('custom_field.field_value')"
                  v-if="editIndex === idx"
                  @blur="handleValueEdit(element)"
                  v-model="element.value"/>
        <span class="text-item" v-else>
          <span v-if="element.system">
             {{$t(element.value)}}
          </span>
          <span v-else>
             {{ (element.value && isKv ? '(' : '') + element.value + (element.value && isKv ? ')' : '')}}
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
import MsInstructionsIcon from "@/business/components/common/components/MsInstructionsIcon";
export default {
  name: "MsSingleHandleDrag",
  components: {
    MsInstructionsIcon,
    draggable
  },
  data() {
    return {
      editIndex: -1,
    };
  },
  props: {
    disable: Boolean,
    isKv: Boolean,
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
    handleTextEdit(element) {
      if (!this.isKv) {
        element.text = element.value;
        this.editIndex = -1;
      }
    },
    handleValueEdit(element) {
      if (!this.isKv) {
        element.text = element.value;
      }
      if (element.value && element.text) {
        this.editIndex = -1;
      }
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

.instructions-icon {
  margin-left: 5px;
}
</style>
