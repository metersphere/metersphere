import _object from './object'
import _string from './string'
import _array from './array'
import _boolean from './boolean'
import _integer from './integer'
import _number from './number'
import {LicenseKey} from "@/common/js/constants";

const TYPE_NAME = ['string', 'number', 'integer', 'object', 'array', 'boolean']

const TYPE = {
    'object': _object,
    'string': _string,
    'array': _array,
    'boolean': _boolean,
    'integer': _integer,
    'number': _number
}
export {TYPE, TYPE_NAME}

export function TYPES(key) {
    if (key && key === 'root') {
        return ['object', 'array'];
    }
    return TYPE_NAME;
}
