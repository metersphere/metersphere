<template>
  <div class="ms-single-handle-drag">
    <div>
      <slot name="header">
        <el-link class="add-text" :underline="false" :disabled="disable" @click="add">
          <i class="el-icon-plus">{{ $t('custom_field.add_option') }}</i>
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
                  maxlength="50"
                  show-word-limit
                  v-if="editIndex === idx"
                  @blur="handleTextEdit(element)"
                  v-model="element.text"/>
        <span class="text-item" v-else>
          <span v-if="element.system">
             {{ $t(element.text) }}
          </span>
          <span v-else>
             {{ element.text }}
          </span>
        </span>

        <el-input size="mini" type="text"
                  class="text-item"
                  :placeholder="$t('custom_field.field_value')"
                  maxlength="10"
                  show-word-limit
                  v-if="editIndex === idx && isKv"
                  @blur="handleValueEdit(element)"
                  v-model="element.value"/>
        <span class="text-item" v-else-if="isKv">
          <span>
             {{ (element.value && isKv ? '(' : '') + element.value + (element.value && isKv ? ')' : '') }}
          </span>
        </span>

        <el-link :underline="false" v-for="item in operators" :disabled="element.system && item.isEdit" :key="item.id">
          <i :class="item.icon"
             @click="item.click(element, idx)"/>
        </el-link>
      </div>
    </draggable>

  </div>
</template>

<script>
import draggable from "vuedraggable";
import MsInstructionsIcon from "metersphere-frontend/src/components/MsInstructionsIcon";
import {getUUID} from "metersphere-frontend/src/utils";

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
        return [
          {name: "John", text: "text", id: 0},
        ];
      }
    },
    operators: {
      type: Array,
      default() {
        return [
          {
            id: 1,
            icon: 'el-icon-edit',
            isEdit: true,
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
            id: 2,
            icon: 'el-icon-close',
            isEdit: false,
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
    add() {
      let item = {
        text: '',
        value: ''
      };
      if (!this.isKv) {
        item.value = getUUID().substring(0, 8);
      }
      while (typeof item.value === 'number') {
        item.value = getUUID().substring(0, 8);
      }
      this.data.push(item);
      this.editIndex = this.data.length - 1;
    },
    handleTextEdit() {
      if (!this.isKv) {
        this.editIndex = -1;
      }
    },
    handleValueEdit(element) {
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
