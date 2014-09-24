from scapy.all import *
conf.L3socket=L3RawSocket 
#send(IP(dst="127.0.0.1")/UDP(dport=53)/fuzz(DNS(qd=fuzz(DNSQR()),     an=fuzz(DNSRR()))))
send(IP(src="127.0.0.1",dst="127.0.0.1")/TCP(dport=53)/DNS(rd=3,qd=DNSQR(qname="www.slashdot.org")))
