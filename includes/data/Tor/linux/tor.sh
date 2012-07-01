#!/bin/sh
#
# GNU/Linux does not really require something like RelativeLink.c
# However, we do want to have the same look and feel with similar features.
#
# To run in debug mode simply pass --debug
#
# Copyright 2011 The Tor Project.  See LICENSE for licensing information.
# copy from Tor linux bundle

LD_LIBRARY_PATH="./linux/lib/"
LDPATH="./linux/lib/"
export LDPATH
export LD_LIBRARY_PATH
./linux/tor.lin -f torrc.txt &
echo $! > "$2.tor.$1.pid"
exit
