import JsonEditor from './src/json-editor';

/* istanbul ignore next */
JsonEditor.install = function(Vue) {
  Vue.component(JsonEditor.name, JsonEditor);
};

export default JsonEditor;
