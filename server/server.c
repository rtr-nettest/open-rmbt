/*******************************************************************************
 * Copyright 2012 alladin-IT OG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
#define _POSIX_C_SOURCE 200809L

#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <string.h>
#include <fcntl.h>
#include <signal.h>

#include <sys/mman.h>
#include <sys/time.h>

#include <time.h>

#include <pthread.h>

#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <netdb.h>
#include <poll.h>
#include <arpa/inet.h>

#include "config.h"

#define HAVE_SSL

#ifdef HAVE_SSL

    #define OPENSSL_THREAD_DEFINES
    #include <openssl/opensslconf.h>
    #if !defined(OPENSSL_THREADS)
        #error no thread support in openssl
    #endif

    #include <openssl/bio.h> // BIO objects for I/O
    #include <openssl/crypto.h>
    #include <openssl/ssl.h> // SSL and SSL_CTX for SSL connections
    #include <openssl/err.h> // Error reporting
    #include <openssl/hmac.h>
    
    static pthread_mutex_t *lockarray;
    
    SSL_CTX *ssl_ctx;
    #define MY_SOCK BIO*
    #define my_write BIO_write
    #define my_read BIO_read
#else
    #define MY_SOCK int
    #define my_write write
    #define my_read read
#endif

volatile int num_threads;

#define NEWLINE '\n'
#define GETTIME "GETTIME"
#define GETCHUNKS "GETCHUNKS"
#define PUT "PUT"
#define PUTNORESULT "PUTNORESULT"
#define PING "PING"
#define PONG_NL "PONG\n"
#define OK "OK"
#define OK_NL "OK\n"
#define ACCEPT_TOKEN_NL "ACCEPT TOKEN QUIT\n"
#define ACCEPT_GET_PUT_PING_NL "ACCEPT GETCHUNKS GETTIME PUT PUTNORESULT PING QUIT\n"
#define ERR_NL "ERR\n"
#define QUIT "QUIT"
#define BYE_NL "BYE\n"

volatile int accept_queue[ACCEPT_QUEUE_MAX_SIZE];
volatile int accept_queue_size, accept_queue_start = 0;
pthread_mutex_t accept_queue_mutex = PTHREAD_MUTEX_INITIALIZER;

pthread_cond_t accept_queue_not_empty = PTHREAD_COND_INITIALIZER;
pthread_cond_t accept_queue_not_full = PTHREAD_COND_INITIALIZER;


int port[] = {PORT_NOSSL, PORT_SSL};
int num_ports = 2;
int sock[2];

char *random;
long random_size;

long page_size; 

int my_readline(MY_SOCK sock, const char *buf, int size)
{
    const char *buf_ptr = buf;
    int size_remain = size;
    int r;
    char *nl_ptr = NULL;
    
    do
    {
        r = my_read(sock, (void*)buf_ptr, size_remain);
        if (r > 0)
        {
            nl_ptr = memchr(buf_ptr, NEWLINE, r);
            buf_ptr += r;
            size_remain -= r;
        }
    }
    while (r > 0 && nl_ptr == NULL && size_remain > 0);
    if (size_remain <= 0)
        return -1;
    if (nl_ptr != NULL)
        *nl_ptr = '\0';
    return  buf_ptr - buf;
}

void fill_ts(struct timespec *time_result)
{
    int rc;
    rc = clock_gettime(CLOCK_MONOTONIC, time_result);
    if (rc == -1)
    {
        perror("clock_gettime");
        exit(1);
    }
}

long long ts_diff(struct timespec *start)
{
    struct timespec end;
    fill_ts(&end);
    
    if ((end.tv_nsec-start->tv_nsec)<0)
    {
        start->tv_sec = end.tv_sec-start->tv_sec-1;
        start->tv_nsec = 1000000000ull + end.tv_nsec-start->tv_nsec;
    }
    else
    {
        start->tv_sec = end.tv_sec-start->tv_sec;
        start->tv_nsec = end.tv_nsec-start->tv_nsec;
    }
    return start->tv_nsec + (long long)start->tv_sec * 1000000000ull;
}

long long ts_diff_preserve(struct timespec *start)
{
    struct timespec end;
    fill_ts(&end);
    
    if ((end.tv_nsec-start->tv_nsec)<0)
    {
        end.tv_sec = end.tv_sec-start->tv_sec-1;
        end.tv_nsec = 1000000000ull + end.tv_nsec-start->tv_nsec;
    }
    else
    {
        end.tv_sec = end.tv_sec-start->tv_sec;
        end.tv_nsec = end.tv_nsec-start->tv_nsec;
    }
    return end.tv_nsec + (long long)end.tv_sec * 1000000000ull;
}

void do_bind()
{
    int true = 1;
    
    int i;
    for (i = 0; i < num_ports; i++)
    {
        if ((sock[i] = socket(AF_INET6, SOCK_STREAM, 0)) == -1) {
            perror("Socket");
            exit(1);
        }
        
        if (setsockopt(sock[i], SOL_SOCKET, SO_REUSEADDR, &true, sizeof (int)) == -1)
        {
            perror("Setsockopt SO_REUSEADDR");
            exit(1);
        }
        
        /* set recieve timeout */
        struct timeval timeout;
        timeout.tv_sec = TIMEOUT;
        timeout.tv_usec = 0;
        if (setsockopt(sock[i], SOL_SOCKET, SO_RCVTIMEO, &timeout, sizeof (timeout)) == -1)
        {
            perror("Setsockopt SO_RCVTIMEO");
            exit(1);
        }
        
        /* set send timeout */
        if (setsockopt(sock[i], SOL_SOCKET, SO_SNDTIMEO, &timeout, sizeof (timeout)) == -1)
        {
            perror("Setsockopt SO_SNDTIMEO");
            exit(1);
        }
        
        
        /*
        if (setsockopt(sock, IPPROTO_TCP, TCP_NODELAY, &true, sizeof (int)) == -1)
        {
            perror("Setsockopt TCP_NODELAY");
            exit(1);
        }
        */
    
        struct sockaddr_in6 server_addr;
        memset(&server_addr, 0, sizeof(server_addr));
        
        server_addr.sin6_family = AF_INET6;
        server_addr.sin6_port = htons(port[i]);
        server_addr.sin6_addr = in6addr_any;
        
        if (bind(sock[i], (struct sockaddr *) &server_addr, sizeof(server_addr)) == -1) {
            perror("Unable to bind");
            exit(1);
        }
        
        if (listen(sock[i],LISTEN_BACKLOG) == -1) {
            perror("Listen");
            exit(1);
        }
        printf("\nServer Waiting for client on port %d\n", port[i]);
    }
}

void accept_loop()
{
    struct pollfd poll_array[num_ports];
    int i;
    for (i = 0; i < num_ports; i++)
    {
        poll_array[i].fd = sock[i];
        poll_array[i].events = POLLIN;
    }
    
    /* accept loop */
    while (1)
    {
        /* poll */
        int r = poll(poll_array, num_ports, -1);
        if (r == -1)
        {
            perror("poll");
            exit(1);
        }
        printf("poll: %d\n", r);
        for (i = 0; i < num_ports; i++)
        {
            if ((poll_array[i].revents & POLLIN) != 0)
            {
                /* accept */
                printf("run accept\n");
                int socket_descriptor = accept(sock[i], NULL, NULL);
                printf("got conn\n");
                
                /* if valid socket descriptor */
                if (socket_descriptor >= 0)
                {
                    /* lock */
                    pthread_mutex_lock(&accept_queue_mutex);
                    
                    /* wait until queue not full anymore */
                    while (accept_queue_size == ACCEPT_QUEUE_MAX_SIZE)
                        pthread_cond_wait(&accept_queue_not_full, &accept_queue_mutex);
                    
                    /* add socket descriptor to queue */
                    accept_queue[(accept_queue_start + accept_queue_size++) % ACCEPT_QUEUE_MAX_SIZE] = socket_descriptor;
                    
                    /* if queue was empty, signal a thread to start looking for the socket descriptor */
                    if (accept_queue_size > 0)
                        pthread_cond_signal(&accept_queue_not_empty);
                    
                    /* unlock */
                    pthread_mutex_unlock(&accept_queue_mutex);
                }
            }
        }
    }
}

const char *base64(const char *input, int ilen, char *output, int *olen)
{
    BIO *bmem, *b64;
    BUF_MEM *bptr;
    
    b64 = BIO_new(BIO_f_base64());
    bmem = BIO_new(BIO_s_mem());
    b64 = BIO_push(b64, bmem);
    BIO_write(b64, input, ilen);
    BIO_flush(b64);
    BIO_get_mem_ptr(b64, &bptr);
    
    if (bptr->length > *olen)
    {
        BIO_free_all(b64);
        return NULL;
    }
    else
    {
        memcpy((void *)output, bptr->data, bptr->length);
        output[bptr->length - 1]='\0';
        *olen = bptr->length;
        BIO_free_all(b64);
        return output;
    }
}

int check_token(const char *uuid, const char *start_time_str, const char *hmac)
{
    unsigned char md[EVP_MAX_MD_SIZE];
    unsigned int md_size = sizeof(md);
    unsigned char key[] = RMBT_SECRETKEY;
    unsigned char msg[128];
    int r;
    r = snprintf((char *)msg, sizeof(msg), "%s_%s", uuid, start_time_str);
    if (r < 0)
        return 0;
    
    char base64_buf[64*2];
    int base64_buf_size = sizeof(base64_buf);
    
    HMAC(EVP_sha1(), key, strnlen((char*)key, sizeof(key)), msg, strnlen((char*)msg, sizeof(msg)), md, &md_size);
    base64((char*)md, md_size, base64_buf, &base64_buf_size);
    
    int result = strncmp(base64_buf, hmac, base64_buf_size);
    if (result != 0)
    {
        printf("ILLEGAL TOKEN!\n");
        printf("hmac-in:   %s|\n", hmac);
        printf("hmac-calc: %s|\n", base64_buf);
    }
    else
    {
        printf("TOKEN OK\n");
        
        /* check if client is allowed yet */
        time_t now = time(NULL);
        long int start_time = atoi(start_time_str);
        
        printf("now: %ld; start_time: %ld; MAX_ACCEPT_EARLY: %d, MAX_ACCEPT_LATE: %d\n", now, start_time, MAX_ACCEPT_EARLY, MAX_ACCEPT_LATE);
        
        if (start_time - MAX_ACCEPT_EARLY > now || start_time + MAX_ACCEPT_LATE < now)
        {
            if (start_time - MAX_ACCEPT_EARLY > now)
                printf("Client is not allowed yet. %ld seconds to early.\n", start_time - now);
            else
                printf("Client is %ld seconds too late.\n", now - start_time);
            result = -1;
        }
        
        /* accept if a little bit too early, but let him wait */
        if (result == 0 && start_time > now)
        {
            printf("Client is %ld seconds too early. Let him wait...\n", start_time - now);
            
            struct timespec sleep;
            sleep.tv_sec = start_time - now;
            sleep.tv_nsec = 0;
            nanosleep(&sleep, NULL);
        }
    }
    return result;
}

void print_milsecs(unsigned long nsecs)
{
    double milsecs = (double)nsecs/1e6;
    printf("time: %.6f milsec\n",milsecs);
}

void print_speed(unsigned long nsecs, unsigned long data_size)
{
    double secs = (double)nsecs/1e9;
    printf("time: %.9f secs\n",secs);
    printf("MBit: %.4f\n",(double)data_size/secs*8.0/1e6);
}

void write_err(MY_SOCK sock)
{
    my_write(sock, ERR_NL, sizeof(ERR_NL)-1);
    printf("sending ERR\n");
}

void handle_connection(MY_SOCK sock)
{
    /************************/
    
    char buf1[CHUNK_SIZE];
    char buf2[CHUNK_SIZE];
    char buf3[CHUNK_SIZE];
    char buf4[CHUNK_SIZE];
        
    my_write(sock, GREETING, sizeof(GREETING)-1);
    my_write(sock, ACCEPT_TOKEN_NL, sizeof(ACCEPT_TOKEN_NL)-1);
    
    int r,s;
    
    r = my_readline(sock, buf1, sizeof(buf1));
    if (r <= 0)
        return;
    
    printf("GOT (%d): %s\n", r, buf1);
    r = sscanf((char*)buf1, "TOKEN %[0-9a-f-]_%[0-9]_%[a-zA-Z0-9+/=]", buf2, buf3, buf4);
    if (r != 3)
        return;
    printf("GOT (%d): %s#%s#%s\n", r, buf2, buf3, buf4);
    
    if (CHECK_TOKEN)
    {
        if (check_token(buf2, buf3, buf4))
            return;
    }
    
    my_write(sock, OK_NL, sizeof(OK_NL)-1);
    r = snprintf(buf1, sizeof(buf1), "CHUNKSIZE %d\n", CHUNK_SIZE);
    if (r <= 0) return;
    s = my_write(sock, buf1, r);
    if (r != s) return;
    
    for (;;)
    {
        my_write(sock, ACCEPT_GET_PUT_PING_NL, sizeof(ACCEPT_GET_PUT_PING_NL)-1);
        int r = my_readline(sock, buf1, sizeof(buf1));
        if (r <= 0)
            return;
        printf("GOT (%d): %s\n", r, buf1);
        
        int parts = sscanf((char*)buf1, "%s %[^\n]", buf2, buf3);
        
        /***** GETTIME *****/
        if (parts == 2 && strncmp((char*)buf2, GETTIME, sizeof(GETTIME)) == 0)
        {
            printf("GETTIME\n");
            
            int seconds;
            r = sscanf((char*)buf3, "%d", &seconds);
            if (r != 1 || seconds <=0 || seconds > MAX_SECONDS)
                write_err(sock);
            else
            {
                long long maxnsec = (long long)seconds * 1000000000ull;
                
                /* start time measurement */
                struct timespec timestamp;
                fill_ts(&timestamp);
                
                /* TODO: start at random place? */
                char *random_ptr = random;
                //unsigned char null = 0x00;
                //unsigned char ff = 0xff;
                long long diffnsec;
                unsigned long total_bytes = 0;
                
                //char debugrandom[chunk];
                //memset(debugrandom, 0, sizeof(debugrandom));
                do
                {
                    if (random_ptr + CHUNK_SIZE >= (random + random_size))
                        random_ptr = random;
                    
                    memcpy(buf4, random_ptr, CHUNK_SIZE);
                    
                    diffnsec = ts_diff_preserve(&timestamp);
                    if (diffnsec >= maxnsec)
                        buf4[CHUNK_SIZE - 1] = 0xff; // signal last package
                    else
                        buf4[CHUNK_SIZE - 1] = 0x00;
                    
                    r = my_write(sock, buf4, CHUNK_SIZE);
                    
                    total_bytes += r;
                    random_ptr += CHUNK_SIZE;
                }
                while (diffnsec < maxnsec && r > 0);
                
                printf("TIME reached, %lu bytes sent.\n", total_bytes);
                
                if (r <= 0)
                    write_err(sock);
                else
                {
                    int r = my_readline(sock, buf1, sizeof(buf1));
                    if (r <= 0)
                        return;
                    
                    /* end time measurement */
                    long long nsecs_total = ts_diff(&timestamp);
                    
                    if (strncmp((char*)buf1, OK, sizeof(OK)) == 0)
                    {
                        print_speed(nsecs_total, total_bytes);
                        r = snprintf((char*)buf3, sizeof(buf3), "TIME %lld\n", nsecs_total);
                        if (r <= 0) return;
                        s = my_write(sock, buf3, r);
                        if (r != s) return;
                    }
                    else
                        write_err(sock);
                }
            }
        }
        /***** GETCHUNKS *****/
        else if (parts == 2 && strncmp((char*)buf2, GETCHUNKS, sizeof(GETCHUNKS)) == 0)
        {
            printf("GETCHUNKS\n");
            
            int chunks;
            r = sscanf((char*)buf3, "%d", &chunks);
            if (r != 1 || chunks <=0 || chunks > MAX_CHUNKS)
                write_err(sock);
            else
            {
                
                /* start time measurement */
                struct timespec timestamp;
                fill_ts(&timestamp);
                
                int s;
                /* TODO: start at random place? */
                char *random_ptr = random; 
                unsigned char null = 0x00;
                unsigned char ff = 0xff;
                unsigned long total_bytes = 0;
                
                int chunks_sent = 0;
                //char debugrandom[chunk];
                //memset(debugrandom, 0, sizeof(debugrandom));
                do
                {
                    if (random_ptr + CHUNK_SIZE >= (random + random_size))
                        random_ptr = random;
                    
                    r = my_write(sock, random_ptr, CHUNK_SIZE - 1);
                    if (++chunks_sent >= chunks)
                        s = my_write(sock, &ff, 1); // signal last package
                    else
                        s = my_write(sock, &null, 1);
                    total_bytes += r + s;
                    random_ptr += CHUNK_SIZE;
                }
                while (chunks_sent < chunks && r > 0 && s > 0);
                
                if (r <= 0 || s <= 0)
                    write_err(sock);
                else
                {
                    int r = my_readline(sock, buf1, sizeof(buf1));
                    if (r <= 0)
                        return;
                    
                    /* end time measurement */
                    long long nsecs_total = ts_diff(&timestamp);
                    
                    if (strncmp((char*)buf1, OK, sizeof(OK)) == 0)
                    {
                        print_speed(nsecs_total, total_bytes);
                        r = snprintf((char*)buf3, sizeof(buf3), "TIME %lld\n", nsecs_total);
                        if (r <= 0) return;
                        s = my_write(sock, buf3, r);
                        if (r != s) return;
                    }
                    else
                        write_err(sock);
                }
            }
        }
        /***** PUT *****/
        else if (parts == 1 && (strncmp((char*)buf2, PUT, sizeof(PUT)) == 0 || strncmp((char*)buf2, PUTNORESULT, sizeof(PUTNORESULT)) == 0))
        {
            int printIntermediateResult = strncmp((char*)buf2, PUT, sizeof(PUT)) == 0;
            
            printf("PUT\n");
            my_write(sock, OK_NL, sizeof(OK_NL)-1);
            
            /* start time measurement */
            struct timespec timestamp;
            fill_ts(&timestamp);
            
            unsigned char last_byte = 0;
            long total_read = 0;
            long long diffnsec;
            long long last_diffnsec = -1;
            //long chunks = 0;
            do
            {
               r = my_read(sock, buf4, sizeof(buf4));
               if (r > 0)
               {
                   int pos_last = CHUNK_SIZE - 1 - (total_read % CHUNK_SIZE);
                   if (r > pos_last)
                       last_byte = buf4[pos_last];
                   total_read += r;
               }
               
               if (printIntermediateResult)
               {
                   diffnsec = ts_diff_preserve(&timestamp);
                   //if (++chunks % 10 == 0)
                   if (last_diffnsec == -1 || (diffnsec - last_diffnsec > 1e6))
                   {
                       last_diffnsec = diffnsec;
                       s = snprintf((char*)buf3, sizeof(buf3), "TIME %lld BYTES %ld\n", diffnsec, total_read);
                       if (s <= 0) return;
                       r = my_write(sock, buf3, s);
                       if (r != s) return;
                   }
               }
            }
            while (r > 0 && last_byte != 0xff);
            long long nsecs = ts_diff(&timestamp);
            if (r <= 0)
                write_err(sock);
            else
            {
                print_speed(nsecs, total_read);
                
                r = snprintf((char*)buf3, sizeof(buf3), "TIME %lld\n", nsecs);
                if (r <= 0) return;
                s = my_write(sock, buf3, r);
                if (r != s) return;
            }
        }
        /***** QUIT *****/
        else if (strncmp((char*)buf2, QUIT, sizeof(QUIT)) == 0)
        {
            printf("QUIT\n");
            my_write(sock, BYE_NL, sizeof(BYE_NL)-1);
            return;
        }
        /***** PING *****/
        else if (parts == 1 && strncmp((char*)buf2, PING, sizeof(PING)) == 0)
        {
            printf("PING\n");
            
            /* start time measurement */
            struct timespec timestamp;
            fill_ts(&timestamp);
            
            my_write(sock, PONG_NL, sizeof(PONG_NL)-1);
            int r = my_readline(sock, buf1, sizeof(buf1));
            if (r <= 0)
                return;
            
            /* end time measurement */
            long long nsecs = ts_diff(&timestamp);
            
            if (strncmp((char*)buf1, OK, sizeof(OK)) != 0)
                write_err(sock);
            else
            {
                r = snprintf((char*)buf3, sizeof(buf3), "TIME %lld\n", nsecs);
                if (r <= 0) return;
                s = my_write(sock, buf3, r);
                if (r != s) return;
                
                print_milsecs(nsecs);
            }
        }
        else
            write_err(sock);
    
        /************************/
    }
}

void *worker_thread_main(void *arg)
{
    printf("Thread starting\n");
    
    while (1)
    {
        /* lock */
        pthread_mutex_lock(&accept_queue_mutex);
        
        /* wait until something in queue */ 
        while (accept_queue_size == 0)
            pthread_cond_wait(&accept_queue_not_empty, &accept_queue_mutex);
        
        /* get from queue */
        accept_queue_size--;
        int socket_descriptor = accept_queue[accept_queue_start++];
        if (accept_queue_start == ACCEPT_QUEUE_MAX_SIZE)
            accept_queue_start = 0;
        
        /* if queue was full, send not_full signal */
        if (accept_queue_size + 1 == ACCEPT_QUEUE_MAX_SIZE)
            pthread_cond_signal(&accept_queue_not_full);
        
        /* unlock */
        pthread_mutex_unlock(&accept_queue_mutex);
        
        printf("handling conn..\n");
        
        struct sockaddr_in6 addr;
        socklen_t addrlen = sizeof(addr);
        int r = getsockname(socket_descriptor, (struct sockaddr *) &addr, &addrlen);
        if (r == -1)
        {
            perror("getsockname");
            continue;
        }
        int my_port = ntohs(addr.sin6_port);
        
        r = getpeername(socket_descriptor, (struct sockaddr *) &addr, &addrlen);
        if (r == -1)
        {
            perror("getpeername");
            continue;
        }
        int peer_port = ntohs(addr.sin6_port);
        
        char buf[128];
        if (inet_ntop(AF_INET6, &addr.sin6_addr, buf, sizeof(buf)) == NULL)
        {
            perror("inet_ntop");
            continue;
        }
        printf("IP: %s:%d\n", buf, peer_port);
        
        // TODO set
        int use_ssl = (my_port == PORT_SSL);
        
#ifdef HAVE_SSL
        BIO *sock;
        SSL *ssl_sock;
        if (use_ssl)
        {
            ssl_sock = SSL_new(ssl_ctx);
            
            SSL_set_fd(ssl_sock, socket_descriptor);
            SSL_accept(ssl_sock);
            
            sock = BIO_new(BIO_f_ssl());
            BIO_set_ssl(sock, ssl_sock, BIO_CLOSE);
        }
        else
        {
            sock = BIO_new(BIO_s_fd());
            BIO_set_fd(sock, socket_descriptor, BIO_CLOSE);
        }
#else
        if (use_ssl)
        {
            printf("can't use ssl - not compiled in!\n");
            exit(1);
        }
        int sock = socket_descriptor;
#endif
        
        handle_connection(sock);
        
#ifdef HAVE_SSL
        if (use_ssl)
            SSL_shutdown(ssl_sock);
        BIO_free(sock);
#endif
        close(socket_descriptor);
    }
    
    return NULL;
}

void start_threads()
{
    int i;
    
    int start_threads = START_THREADS;
    pthread_t threads[MAX_THREADS];
    
    for (i = 0; i < start_threads; i++)
    {
        /* int rc = */ pthread_create(&threads[i], NULL, worker_thread_main, NULL);
        num_threads++;
    }
}

void mmap_random()
{
    int fd = open("random", O_RDONLY);
    if (fd == -1)
    {
        perror("open random");
        exit(1);
    }
    
    struct stat stat_data;
    
    int rc = fstat(fd, &stat_data);
    if (rc == -1)
    {
        perror("stat");
        exit(1);
    }
    
    if (!S_ISREG (stat_data.st_mode))
    {
        fprintf(stderr, "random is not a file\n");
        exit(1);
    }
    
    random_size = stat_data.st_size;
    
    random = mmap(NULL, stat_data.st_size, PROT_READ, MAP_PRIVATE, fd, 0);
    if (random == MAP_FAILED)
    {
        perror("mmap");
        exit(1);
    }
    
    if ( close(fd) == -1)
    {
        perror("close");
        exit(1);
    }
    
    /* read whole mmapped file to force caching */
    char buf[CHUNK_SIZE];
    char *ptr = random;
    long read;
    for (read = 0; read < random_size; read+=sizeof(buf))
    {
        memcpy(buf, ptr, sizeof(buf));
        ptr+=sizeof(buf);
    }
    // TODO: handle if not multiple of buf
}

#ifdef HAVE_SSL

static void lock_callback(int mode, int type, char *file, int line)
{
  (void)file;
  (void)line;
  if (mode & CRYPTO_LOCK) {
    pthread_mutex_lock(&(lockarray[type]));
  }
  else {
    pthread_mutex_unlock(&(lockarray[type]));
  }
}
 
static unsigned long thread_id(void)
{
    unsigned long ret;

    ret=(unsigned long)pthread_self();
    return(ret);
}

void init_ssl()
{
    int i;

    lockarray=(pthread_mutex_t *)OPENSSL_malloc(CRYPTO_num_locks() * sizeof(pthread_mutex_t));
    for (i=0; i<CRYPTO_num_locks(); i++)
    {
        pthread_mutex_init(&(lockarray[i]),NULL);
    }
    
    CRYPTO_set_id_callback((unsigned long (*)())thread_id);
    CRYPTO_set_locking_callback((void (*)())lock_callback);
    
    SSL_library_init(); /* load encryption & hash algorithms for SSL */                
    SSL_load_error_strings(); /* load the error strings for good error reporting */
    
    SSL_METHOD *meth = SSLv23_server_method();
    ssl_ctx = SSL_CTX_new(meth);
    
    if (SSL_CTX_use_certificate_chain_file(ssl_ctx, SERVER_CERT) <= 0)
    {
        ERR_print_errors_fp(stderr);
        exit(1);
    }
   
    /* Load the server private-key into the SSL context */
    if (SSL_CTX_use_PrivateKey_file(ssl_ctx, SERVER_KEY, SSL_FILETYPE_PEM) <= 0)
    {
        ERR_print_errors_fp(stderr);
        exit(1);
    }
    
    /* Load trusted CA. */
    /*
    if (!SSL_CTX_load_verify_locations(ctx,CA_CERT,NULL))
    { 
        ERR_print_errors_fp(stderr);
        exit(1);
    }
    */
    
    /* Set to require peer (client) certificate verification */
    /*SSL_CTX_set_verify(ctx, SSL_VERIFY_PEER, verify_callback);*/
    /* Set the verification depth to 1 */
    /*SSL_CTX_set_verify_depth(ctx,1);*/
    
}
#endif

int main(void)
{
    printf("Start.\n");
    
    signal(SIGPIPE, SIG_IGN);
    
    page_size = sysconf(_SC_PAGE_SIZE);
    
    mmap_random();
    
#ifdef HAVE_SSL
    init_ssl();
#endif

    start_threads();
    
    do_bind();
    
    accept_loop();
    
    return EXIT_SUCCESS;
} /* end main() */

