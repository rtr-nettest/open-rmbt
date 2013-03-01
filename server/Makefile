all: server

server: server.c config.h
	#gcc -Wall -Wno-unused-value -O3 -o server server.c -pthread -lrt -lssl
	gcc -Wall -Wno-unused-value -O0 -g -o server server.c -pthread -lrt -lssl
	
clean:
	rm server

run: random server
	./server
	
random: 
	dd if=/dev/urandom of=random bs=1M count=100
