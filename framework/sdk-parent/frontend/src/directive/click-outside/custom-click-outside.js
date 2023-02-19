export default {
  bind(el, binding, vnode) {
    function clickHandler(e) {
      if (el.contains(e.target)) {
        return false;
      }
      if (binding.expression) {
        binding.value(e);
      }
    }
    el.__vueClickOutside__ = clickHandler;
    document.addEventListener('click', clickHandler);
  },
  update() {},
  unbind(el, binding) {
    document.removeEventListener('click', el.__vueClickOutside__);
    delete el.__vueClickOutside__;
  }
};
