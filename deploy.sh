#!/bin/bash

run_docker_command() {
    args=$1
    cmd="docker run node-container mvn exec:java -Dexec.mainClass=node.Node -Dexec.args=\"$args\""
    echo "Executing: $cmd"
    eval $cmd
    if [ $? -ne 0 ]; then
        echo "Failed to execute for arguments $args"
    fi
}

install_maven() {
    cmd="mvn clean install"
    echo "Executing: $cmd"
    eval $cmd

}

run_java_gui() {
    cmd="mvn javafx:run"
    echo "Executing: $cmd"
    eval $cmd &

}

build_container() {
    cmd="docker build -t node-container ."
    echo "Executing: $cmd"
    eval $cmd

}

main() {
    if [ -f "graph.txt" ]; then
        while IFS= read -r line; do
            key=$(echo $line | cut -d ':' -f 1 | xargs)
            values=$(echo $line | cut -d ':' -f 2 | xargs)
            args="$key $values"
            run_docker_command "$args" < /dev/null &
            sleep 1  # Waits for 1 second before continuing to the next iteration
        done < graph.txt
    else
        echo "graph.txt does not exist"
    fi
    wait
}



install_maven &
BUILD_PID=$!

build_container &
CONTAINER_PID=$!

wait $BUILD_PID $CONTAINER_PID
echo "Maven and container are ready."

run_java_gui &
GUI_PID=$!


main

