<template>
  <div class="object-style">
    <schema-item
      v-for="(name,index) in propertyKeys"
      :key="index"
      :data="data"
      :name="name"
      :prefix="prefix"
      :is-mock="isMock"
      :show-title="showTitle"
      :show-default-value="showDefaultValue"
      :editor-id="editorId"
    ></schema-item>
  </div>
</template>
<script>
export default {
  name: 'SchemaObject',
  components: { 'schema-item': () => import('./SchemaItem.vue') },
  props: {
    prefix: {
      type: Array,
      default: () => []
    },
    data: {
      type: Object,
      default: () => {}
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
      tagPaddingLeftStyle: {},
      items: this.data.items
    }
  },

  computed: {
    propertyKeys() {
      return Object.keys(this.data.properties)
    }
  },
  methods: {}
}
</script>
