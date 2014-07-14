#!/usr/bin/env python
import pika

connection = pika.BlockingConnection(pika.ConnectionParameters(
host='localhost'))
channel = connection.channel()

channel.queue_declare(queue='')

zone = r'data+{"origin":"example.com.","ttl":60,"SOA":[{"class":"in","name":"example.com.","mname":"ns01.alwaysresolve.net.","rname":"domains@alwaysresolve.com","at":"60","serial":2014062141,"refresh":"1M","retry":"1M","expire":"1M","minimum":"1M"}],"NS":[{"class":"in","name":"pippo.com.","ttl":60,"value":[{"weight":1,"ns":"ns01.alwaysresolve.net."},{"weight":1,"ns":"ns02.alwaysresolve.net."}]}],"CNAME":[{"class":"in","name":"cname","ttl":60,"value":[{"weight":1,"cname":"www.iol.it."}]}],"A":[{"class":"in","name":"www","ttl":60,"value":[{"weight":1,"ip":"127.0.0.1"}]},{"class":"in","name":"www2","ttl":60,"value":[{"weight":1,"ip":"127.0.0.2"}]}]}'
zone = r'update+example.com.'

channel.basic_publish(exchange='prova',
routing_key='',
body=zone)
print( "sent")
connection.close()
