function format(template?: string | null, parm?: any, ...args: any[]) {
  let tmp = parm;
  if (typeof parm !== 'object') {
    tmp = [].slice.call(args, 1);
  }
  return String(template).replace(/\{(\w+)\}/gi, (match, $key) => {
    return tmp[$key] || $key;
  });
}
export default format;
