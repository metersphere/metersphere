function format(template?: string | null, param?: any, ...args: any[]) {
  let tmp = param;
  if (typeof param !== 'object') {
    tmp = [].slice.call(args, 1);
  }
  return String(template).replace(/\{(\w+)\}/gi, (match, $key) => {
    return tmp[$key] || $key;
  });
}
export default format;
