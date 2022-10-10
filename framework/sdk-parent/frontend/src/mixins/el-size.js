const ElSize = {
  computed: {
    computeSize({size, $ELEMENT}) {
      // return size || $ELEMENT?.size
      return size || $ELEMENT ? $ELEMENT.size : 0
    }
  }
}
export default ElSize
