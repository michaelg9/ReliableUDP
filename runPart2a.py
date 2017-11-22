#!/usr/bin/env python
import sys
import os
import glob
import stat
import time
import subprocess
import signal

files = 0
sFile = ""
rFile = ""

print "Resetting dummynet configuration... "
os.system('ipfw flush')

for file in glob.glob("*.java"):
    if file == "Sender2a.java" or file == "sender2a.java" :
        sFile = file
        files += 1
    if file == "Receiver2a.java" or file == "receiver2a.java" :
        rFile = file
        files += 1

if files < 2:
    print "Some files are missing. Please check the sender or receiver filenames <<It should be Sender2a and Receiver2a>>"
    quit()

window_size = [1, 2, 4, 8, 16, 32, 64, 128, 256]
propagation_delay = [5, 25, 100]

print "Compiling your files ..."
s_ret = os.system("javac "+sFile);
if s_ret != 0:
    print "Compilation Error: please check your Sender2a implementation"
    quit()

r_ret = os.system("javac "+rFile);
if r_ret != 0:
    print "Compilation Error: please check your Receiver2a implementation"
    quit()

if s_ret == 0 and r_ret == 0:
    print "Compiled Successfully, running your .class files ..."
    filename = raw_input("Enter a filename to transfer (e.g., test.jpg): ")

    command_tx = "java Sender2a localhost 20002 " + filename
    command_rx = "java Receiver2a 20002 out.jpg"

    log = open('log', 'w')
    for ws in window_size:
        for pd in propagation_delay:
            print "Resetting dummynet configuration... "
            os.system('ipfw flush')

            print "Configuring Dummynet with 0.5% packet lost in each direction, 10Mbps bandwidth and "+ str(pd) +"ms one-way propagation delay for each direction ... "
            os.system('ipfw add pipe 100 in')
            os.system('ipfw add pipe 200 out')
            os.system('ipfw pipe 100 config delay '+ str(pd) + 'ms plr 0.005 bw 10Mbits/s')
            os.system('ipfw pipe 200 config delay '+ str(pd) + 'ms plr 0.005 bw 10Mbits/s')

            retransmission_timeout = raw_input("Enter the optimal retransmission timeout (e.g., 15): ")

            print "-------------------------------------"
            print "Testing under window size = " + str(ws)
            average_throughput = 0
            for k in range(0, 5):
                new_command_tx = command_tx + " " + str(retransmission_timeout) + " " + str(ws)
                command1 = subprocess.Popen(command_rx,stdout=subprocess.PIPE, shell=True)
                command2 = subprocess.Popen("sudo "+new_command_tx,stdout=subprocess.PIPE, shell=True)
                output = command2.communicate()[0]
		
		time.sleep(5)
		p1 = command1.poll()
		p2 = command2.poll()
		if p1 == None or p2 == None:
			print "The Sender or Receiver doesn't terminate ..."
			quit()

                tofile = "Iteration #"+str(k)+") for window size = "+str(ws)+ " and propagation delay = " + str(pd) + " ms: "+output+"\n"
                log.write(tofile)
                val = [float(s) for s in output.split()]
                if len(val) > 0:
                    average_throughput += float(val[0])
                else:
                    print "The throughput value is not a number, please check your implementation"
                    quit()

            average_throughput = average_throughput / 5
            print "Average throughput for window size = [", ws, "and delay", pd, "ms]: ", average_throughput
