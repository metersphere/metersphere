export HEAP=$(cat /proc/meminfo | grep MemTotal | awk '{ mem=int($2/1024/1024 * 3/4 + 0.5); metasize=int(mem/4+0.5)"g"; if(mem<1) mem=1; if (metasize == "0g") metasize="256m";  HEAP="-Xms"mem"g -Xmx"mem"g -XX:MaxMetaspaceSize="metasize; print HEAP }')
for file in ${TESTS_DIR}/*.jmx; do
  jmeter -n -t ${file} -Jserver.rmi.ssl.disable=${SSL_DISABLED}
done
