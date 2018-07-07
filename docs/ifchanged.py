#!/usr/bin/env python2

import sys, os, time

if len(sys.argv) < 2:
    print >> sys.stderr, "usage: ifchanged.py file1 .. filen command"
    sys.exit(1)

filenames = sys.argv[1:-1]
command   = sys.argv[-1]

def scan_files(filenames):
#    print "- "+', '.join(filenames)
    t = 0
    for f in filenames:
        if os.path.isdir(f):
#            print "dir: "+f
#            print "\t"+', '.join(os.listdir(f))
            t = max(t, scan_files(map(lambda x: os.path.join(f,x), os.listdir(f))))
        else:
            try: 
                t = max(t, os.stat(f)[9])
            except OSError: pass
#    print "return t="+str(t)+" for filenames="+' '.join(filenames)
    return t

#print filenames
#print command

time_old = 0
while 1:
    time.sleep(1)
    time_new = scan_files(filenames)
    if time_new != time_old:
        # print "time = ", time_old, time_new
        os.system(command)
        print "@@@ done."
        time_old = time_new
