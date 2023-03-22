import Comp from './MsYanProgress';

const CONF = {
    tip: [
        {
            text: "通过",
            fillStyle: "#ccc",
        }, {
            text: "不通过",
            fillStyle: "#9c3",
        }, {
            text: "评审中",
            fillStyle: "#080"
        }, {
            text: "重新评审",
            fillStyle: "#000"
        }
    ]
};

let YanProgress1 = {
    install(Vue) {
        Vue.component('ms-yan-progress', Object.assign({}, Comp, {
            props: {
                "total": {
                    type: Number,
                    default: 0
                },
                "statusCountItems": {
                  type: Array,
                  default: () => []
                },
                "tip": {
                    type: Array,
                    default() {
                        return CONF.tip;
                    }
                }
            },
        }));
    }
};

if (window && window.Vue) {
    window.Vue.use(YanProgress1);
}

export default YanProgress1;

