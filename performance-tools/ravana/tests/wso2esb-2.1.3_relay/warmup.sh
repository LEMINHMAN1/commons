#!/bin/bash
java -jar benchmark.jar -T"application/soap+xml; charset=utf-8; action=m:buyStocks" -H" " -n 1 -c 1 -p scenario/wso2esb-2.1.3_relay/message512b.xml http://localhost:8281//services/POProxy
java -jar benchmark.jar -T"application/soap+xml; charset=utf-8; action=m:buyStocks" -H" " -n 600 -c 60 -p scenario/wso2esb-2.1.3_relay/message512b.xml http://10.100.4.100:8280//services/POProxy
java -jar benchmark.jar -T"application/soap+xml; charset=utf-8; action=m:buyStocks" -H" " -n 1 -c 1 -p scenario/wso2esb-2.1.3_relay/message512b.xml http://localhost:8281//services/POProxy