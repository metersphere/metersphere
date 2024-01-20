export function setLocalStorage(k: string, v: any) {
  window.localStorage.setItem(k, JSON.stringify(v));
}

export function getLocalStorage(k: string) {
  const v = window.localStorage.getItem(k);
  return JSON.parse(v || '"{}"');
}

export function rmLocalStorage(k: string) {
  window.localStorage.removeItem(k);
}

export function clearLocalStorage() {
  window.localStorage.clear();
}
