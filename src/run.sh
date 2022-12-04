#!/bin/bash

clean () {
    rm *.class 2>/dev/null
}

build () {
    rm *.class 2>/dev/null
    javac Server.java
    javac Client.java
}

if [ $1 = 'clean' ] ; then
    clean
elif [ $1 = 'build' ] ; then
    build
fi