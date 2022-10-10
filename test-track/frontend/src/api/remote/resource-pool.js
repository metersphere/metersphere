import {post, get} from "metersphere-frontend/src/plugins/request";


export function getQuotaValidResourcePools() {
    return get('/testresourcepool/list/quota/valid');
}
