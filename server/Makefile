GCC_PARAMS = -Wall -Wno-unused-value -o server server.c -pthread -lrt -lssl -lcrypto
SERVER_DEP = server.c config.h

all: server

server: ${SERVER_DEP}
	gcc -O0 -g ${GCC_PARAMS}

server-prod: ${SERVER_DEP}
	gcc -O3 ${GCC_PARAMS}
	
clean:
	rm server

run: random server
	./server
	
random: 
	dd if=/dev/urandom of=random bs=1M count=100
