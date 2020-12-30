<template>
  <div class="schema-content" v-bind="$attrs">
    <template v-if="data.type==='array'">
      <schema-array
        :prefix="name"
        :data="data"
        :is-mock="isMock"
        :show-title="showTitle"
        :show-default-value="showDefaultValue"
        :editor-id="editorId"
      />
    </template>
    <template v-if="data.type==='object'">
      <schema-object
        :prefix="nameArray"
        :data="data"
        :is-mock="isMock"
        :show-title="showTitle"
        :show-default-value="showDefaultValue"
        :editor-id="editorId"
      />
    </template>
  </div>
</template>
<script>
import SchemaObject from './SchemaObject'
import SchemaArray from './SchemaArray'
export default {
  name: 'SchemaJson',
  components: {
    'schema-array': SchemaArray,
    'schema-object': SchemaObject
  },
  inheritAttrs: false,
  props: {
    data: {
      type: Object,
      default: () => {
        return []
      }
    },
    isMock: { type: Boolean, default: false },
    showTitle: {
      type: Boolean,
      default: false
    },
    showDefaultValue: { type: Boolean, default: false },
    editorId: {
      type: String,
      default: 'editor_id'
    }
  },
  data() {
    return {
      name: []
    }
  },
  computed: {
    nameArray() {
      return [].concat(this.name, 'properties')
    }
  },
  methods: {}
}
</script>
