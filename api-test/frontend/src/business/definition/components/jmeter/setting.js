export let Setting = {};

export const use = function (s) {
  Setting = s || Setting;
};

export default { use };
