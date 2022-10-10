import {dateFormat, datetimeFormat} from "fit2cloud-ui/src/filters/time";

const filters = {
  "dateFormat": dateFormat,
  "datetimeFormat": datetimeFormat,
};

export default {
  install(Vue) {
    Object.keys(filters).forEach(key => {
      Vue.filter(key, filters[key])
    });
  }
}
