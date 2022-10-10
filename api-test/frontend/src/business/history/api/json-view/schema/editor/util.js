export function isNull(ele) {
  if (typeof ele === 'undefined') {
    return true;
  } else if (ele === null) {
    return true;
  } else if (ele === '') {
    return true;
  }
  return false;
}
