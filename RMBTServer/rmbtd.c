/*******************************************************************************
 * Copyright 2012-2014 alladin-IT GmbH
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
#define _BSD_SOURCE

#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <string.h>
#include <fcntl.h>
#include <signal.h>
#include <syslog.h>

#include <pwd.h>
#include <grp.h>

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

#include "cwebsocket/websocket.h"
#include "config.h"

#define HAVE_SSL  /* currently necessary! */

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
    #define my_organic_write BIO_write
    #define my_organic_read BIO_read
#else
    #define MY_SOCK int
    #define my_organic_write write
    #define my_organic_read read
#endif

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
volatile int accept_queue_listen_idx[ACCEPT_QUEUE_MAX_SIZE];
volatile int accept_queue_size, accept_queue_start = 0;
pthread_mutex_t accept_queue_mutex = PTHREAD_MUTEX_INITIALIZER;

pthread_cond_t accept_queue_not_empty = PTHREAD_COND_INITIALIZER;
pthread_cond_t accept_queue_not_full = PTHREAD_COND_INITIALIZER;

volatile int do_shutdown = 0;

struct listen
{
    struct sockaddr_in6 sockaddr;
    int use_ssl;
    int sock;
} *listens;
int num_listens;

struct thread_info
{
    pthread_t thread_id;
    int thread_num;
} *thread_infos;

// char *pidfile = NULL;

int num_threads = DEFAULT_NUM_THREADS;

char *total_random;
long random_size;

long page_size;
char use_websocket = 0;

void print_help()
{
	printf("==== rmbtd ====\n"
			"command line arguments:\n\n"
			" -l/-L  listen on (IP and) port; -L for SSL;\n"
			"        examples: \"443\",\"1.2.3.4:1234\",\"[2001:1234::567A]:1234\"\n"
			"        maybe specified multiple times; at least once\n\n"
			" -c     path to SSL certificate in PEM format;\n"
			"        intermediate certificates following server cert in same file if needed\n"
			"        required\n\n"
			" -k     path to SSL key file in PEM format; required\n\n"
			" -t     number of worker threads to run for handling connections (default: %d)\n\n"
			" -u     drop root privileges and setuid to specified user; must be root\n\n"
			" -d     fork into background as daemon (no argument)\n\n"
			" -D     enable debug logging (no argument)\n\n"
			" -w     use as websocket server (no argument)\n\n"
			"Required are -c,-k and at least one -l/-L option\n",
			DEFAULT_NUM_THREADS);
}

void syslog_and_print(int priority, const char *format, ...)
{
    int len = strlen(format);
    char format_nl[len + 2];
    strncpy(format_nl, format, len);
    format_nl[len] = '\n';
    format_nl[len + 1] = '\0';
    
    va_list va1, va2;
    va_start(va1, format);
    va_copy(va2, va1);
    vfprintf(stderr, format_nl, va2);
    va_end(va2);
    vsyslog(priority, format, va1);
    va_end(va1);
}

ssize_t my_write(MY_SOCK fd, const void *buf, size_t count) {
    if (use_websocket) {
        char buffer[CHUNK_SIZE +10];
        size_t out_len = CHUNK_SIZE;
        if (count < 2 || count > (CHUNK_SIZE-3)) {
            wsMakeFrame((const uint8_t*) buf, count, (uint8_t*) buffer, &out_len, WS_BINARY_FRAME);
        }
        else {
            wsMakeFrame((const uint8_t*) buf, count, (uint8_t*) buffer, &out_len, WS_TEXT_FRAME);
        }
        my_organic_write(fd,buffer,out_len); //@TODO: return real len of unmasked/unframed output
        return count;
    }
    else
    {
        return my_organic_write(fd,buf,count);
    }
}

ssize_t my_read(MY_SOCK b, void *buf, size_t count) {   
    if (use_websocket) {
        //temporary buffer with enough space for the Frame
        uint8_t tmpBuf[count+100];
        
        uint8_t* data = buf;
        //receive the first 8 bytes that hold the websocket frame header
        ssize_t len = my_organic_read(b,tmpBuf,8); 
        size_t in_len = CHUNK_SIZE;
        if (len <= 0) {
            return len;
        }
        
        enum wsFrameType t;
        size_t payloadLength;
        uint8_t payloadFieldExtraBytes;
        payloadLength = getPayloadLength( tmpBuf, len, &payloadFieldExtraBytes, &t);
        
        //read did not yet get the full tcp package
        // -> read again until we got the full websocket frame
        int remaining = (payloadLength + 6 + payloadFieldExtraBytes) -len;
        
        //prevent possible buffer overflows
        if (remaining > count) {
            return 0;
        }
        
        //printf("payload length: %d, got %d, remaining: %d\n",(int) payloadLength, (int) len, remaining);
        while (payloadLength != 0 && remaining > 0) {
            int newLen = my_organic_read(b,&tmpBuf[len],remaining);

            if (newLen == 0) {
                break;
            }
            
            else if (newLen < 0) {
                long err = ERR_get_error();
                syslog(LOG_ERR, "error: %ld %s - ",err,ERR_error_string(errno,NULL));
                syslog(LOG_ERR, "error: %d %s\n",errno,strerror(errno));
                break;
            }
            remaining -= newLen;
            len += newLen;
        }
        
        
        t = wsParseInputFrame((uint8_t *) tmpBuf, len, &data, &in_len);
        //special frames: Closing frames
        if (t == WS_CLOSING_FRAME) {
            return 0;
            //close connection
          
        }
        
        //@TODO: Ping frames; Error frames; Continuation Frames
        
        memmove(buf,data,in_len);
        return in_len;
    }
    else {
        return my_organic_read(b,buf,count);
    }
}


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
        syslog(LOG_ERR, "error during clock_gettime: %m");
        exit(EXIT_FAILURE);
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
    for (i = 0; i < num_listens; i++)
    {
        if ((listens[i].sock = socket(AF_INET6, SOCK_STREAM, 0)) == -1)
        {
            syslog(LOG_ERR, "error during socket: %m");
            exit(EXIT_FAILURE);
        }
        
        if (setsockopt(listens[i].sock, SOL_SOCKET, SO_REUSEADDR, &true, sizeof (int)) == -1)
        {
            syslog(LOG_ERR, "error during setsockopt SO_REUSEADDR: %m");
            exit(EXIT_FAILURE);
        }
        
        /* set recieve timeout */
        struct timeval timeout;
        timeout.tv_sec = TIMEOUT;
        timeout.tv_usec = 0;
        if (setsockopt(listens[i].sock, SOL_SOCKET, SO_RCVTIMEO, &timeout, sizeof (timeout)) == -1)
        {
            syslog(LOG_ERR, "error during setsockopt SO_RCVTIMEO: %m");
            exit(EXIT_FAILURE);
        }
        
        /* set send timeout */
        if (setsockopt(listens[i].sock, SOL_SOCKET, SO_SNDTIMEO, &timeout, sizeof (timeout)) == -1)
        {
            syslog(LOG_ERR, "error during setsockopt SO_SNDTIMEO: %m");
            exit(EXIT_FAILURE);
        }
        
        
        /*
        if (setsockopt(sock, IPPROTO_TCP, TCP_NODELAY, &true, sizeof (int)) == -1)
        {
            syslog(LOG_ERR, "error during setsockopt TCP_NODELAY: %m");
            exit(1);
        }
        */
    
        char ip[INET6_ADDRSTRLEN];
        inet_ntop(AF_INET6, &listens[i].sockaddr.sin6_addr, ip, sizeof(ip));
        if (bind(listens[i].sock, (const struct sockaddr *) &listens[i].sockaddr, sizeof(*listens)) == -1)
        {
            syslog(LOG_ERR, "error while binding on [%s]:%d: %m", ip, ntohs(listens[i].sockaddr.sin6_port));
            exit(EXIT_FAILURE);
        }
        
        if (listen(listens[i].sock,LISTEN_BACKLOG) == -1)
        {
            syslog(LOG_ERR, "error during listen: %m");
            exit(EXIT_FAILURE);
        }
        syslog(LOG_INFO, "listening on [%s]:%d (%s)", ip, ntohs(listens[i].sockaddr.sin6_port), listens[i].use_ssl ? "SSL" : "no ssl");
    }
}

void unbind()
{
    syslog(LOG_DEBUG, "closing sockets");
    int i;
    for (i = 0; i < num_listens; i++)
    {
        close(listens[i].sock);
    }
}

void accept_loop()
{
    struct pollfd poll_array[num_listens];
    int i;
    for (i = 0; i < num_listens; i++)
    {
        poll_array[i].fd = listens[i].sock;
        poll_array[i].events = POLLIN;
    }
    
    syslog(LOG_INFO, "ready for connections");
    
    /* accept loop */
    while (! do_shutdown)
    {
        /* poll */
        int r = poll(poll_array, num_listens, -1);
        if (r == -1)
        {
            if (errno != EINTR)
                syslog(LOG_ERR, "error during poll: %m");
            continue;
        }
        for (i = 0; i < num_listens; i++)
        {
            if ((poll_array[i].revents & POLLIN) != 0)
            {
                /* accept */
                int socket_descriptor = accept(listens[i].sock, NULL, NULL);
                
                /* if valid socket descriptor */
                if (socket_descriptor >= 0)
                {
                    /* lock */
                    pthread_mutex_lock(&accept_queue_mutex);
                    
                    /* wait until queue not full anymore */
                    while (! do_shutdown && accept_queue_size == ACCEPT_QUEUE_MAX_SIZE)
                        pthread_cond_wait(&accept_queue_not_full, &accept_queue_mutex);
                    
                    if (do_shutdown)
                        return;
                    
                    /* add socket descriptor to queue */
                    int idx = (accept_queue_start + accept_queue_size++) % ACCEPT_QUEUE_MAX_SIZE;
                    accept_queue[idx] = socket_descriptor;
                    accept_queue_listen_idx[idx] = i;
                    
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

int check_token(int thread_num, const char *uuid, const char *start_time_str, const char *hmac)
{
    unsigned char md_buf[EVP_MAX_MD_SIZE];
    unsigned int md_size = sizeof(md_buf);
    char *key = RMBT_SECRETKEY;

    unsigned char msg[128];
    int r;
    r = snprintf((char *)msg, sizeof(msg), "%s_%s", uuid, start_time_str);
    if (r < 0)
        return 0;
    
    char base64_buf[64*2];
    int base64_buf_size = sizeof(base64_buf);
    
    unsigned char *md = HMAC(EVP_sha1(), key, strlen(key), msg, strnlen((char*)msg, sizeof(msg)), md_buf, &md_size);
    if (md == NULL)
    	return -1;
    base64((char*)md, md_size, base64_buf, &base64_buf_size);
    
    int result = strncmp(base64_buf, hmac, base64_buf_size);
    if (result != 0)
    {
    	syslog(LOG_ERR, "[THR %d] got illegal token: \"%s\", got hmac: \"%s\", expected: \"%s\"", thread_num, uuid, hmac, base64_buf);
    }
    else
    {
        /* check if client is allowed yet */
        time_t now = time(NULL);
        long int start_time = atoi(start_time_str);
        
        // printf("now: %ld; start_time: %ld; MAX_ACCEPT_EARLY: %d, MAX_ACCEPT_LATE: %d\n", now, start_time, MAX_ACCEPT_EARLY, MAX_ACCEPT_LATE);
        
        if (start_time - MAX_ACCEPT_EARLY > now || start_time + MAX_ACCEPT_LATE < now)
        {
            if (start_time - MAX_ACCEPT_EARLY > now)
            	syslog(LOG_ERR, "[THR %d] client is not allowed yet. %ld seconds to early", thread_num, start_time - now);
			else
				syslog(LOG_ERR, "[THR %d] client is %ld seconds too late", thread_num, now - start_time);

            result = -1;
        }
        
        /* accept if a little bit too early, but let him wait */
        if (result == 0 && start_time > now)
        {
        	syslog(LOG_DEBUG, "[THR %d] client is %ld seconds too early. Let him wait", thread_num, start_time - now);
            
            struct timespec sleep;
            sleep.tv_sec = start_time - now;
            sleep.tv_nsec = 0;
            nanosleep(&sleep, NULL);
        }
    }
    return result;
}

/*
void print_milsecs(unsigned long nsecs)
{
    double milsecs = (double)nsecs/1e6;
    //printf("time: %.6f milsec\n",milsecs);
}

void print_speed(unsigned long nsecs, unsigned long data_size)
{
    double secs = (double)nsecs/1e9;
    //printf("time: %.9f secs\n",secs);
    //printf("MBit: %.4f\n",(double)data_size/secs*8.0/1e6);
}
*/

void write_err(MY_SOCK sock)
{
    my_write(sock, ERR_NL, sizeof(ERR_NL)-1);
    //printf("sending ERR\n");
}

void handle_connection(int thread_num, MY_SOCK sock)
{
    /************************/
    
    char buf1[CHUNK_SIZE];
    char buf2[CHUNK_SIZE];
    char buf3[CHUNK_SIZE];
    char buf4[CHUNK_SIZE];
    int r, s;
    
    if (use_websocket) {
        int ws;
        
        r = my_organic_read(sock, buf1, sizeof (buf1));
        if (r <= 0) {
            syslog(LOG_INFO, "initialization error: %d %d", r, (int) ERR_get_error());
            ERR_print_errors_fp(stdout);
            return;
        }

        //websocket handshake?
        ws = strncmp((char*) buf1, "GET ", 4);
        if (ws == 0) {
            
            struct handshake hs;
            nullHandshake(&hs);

            //try to parse handshake
            enum wsFrameType handshake = wsParseHandshake((const uint8_t*) buf1, r, &hs);

            if (handshake != WS_OPENING_FRAME) {
                syslog(LOG_INFO, "invalid handshake");
                return;
            }

            //generate and send the handshake response
            size_t framesize = CHUNK_SIZE;
            wsGetHandshakeAnswer(&hs, (uint8_t*) buf1, &framesize);
            //syslog(LOG_INFO, "init Websocket handshake3 %d >> %s <<", (int) framesize, buf1);
            //server response
            my_organic_write(sock, buf1, framesize);

            //syslog(LOG_INFO, "send answer");

        }
        else {
            syslog(LOG_INFO, "initialization error: %d %d", r, (int) ERR_get_error());
            return;
        }
    }
    
    my_write(sock, GREETING, sizeof(GREETING)-1);
    my_write(sock, ACCEPT_TOKEN_NL, sizeof(ACCEPT_TOKEN_NL)-1);
    
    r = my_readline(sock, buf1, sizeof (buf1));
    if (r <= 0) {
        syslog(LOG_INFO, "initialization error: %d %d", r, (int) ERR_get_error());
        ERR_print_errors_fp(stdout);
        return;
    }
    
    r = sscanf((char*)buf1, "TOKEN %36[0-9a-f-]_%12[0-9]_%50[a-zA-Z0-9+/=]", buf2, buf3, buf4);
    if (r != 3)
    {
    	syslog(LOG_ERR, "[THR %d] syntax error on token: \"%s\"", thread_num, buf1);
        return;
    }
    
    if (CHECK_TOKEN)
    {
        if (check_token(thread_num, buf2, buf3, buf4))
        {
        	syslog(LOG_ERR, "[THR %d] token was not accepted", thread_num);
            return;
        }

        syslog(LOG_INFO, "[THR %d] valid token; uuid: %s", thread_num, buf2);
    }
    else
    	syslog(LOG_INFO, "[THR %d] token NOT CHECKED; uuid: %s", thread_num, buf2);
    
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
        
        int parts = sscanf((char*)buf1, "%50s %12[^\n]", buf2, buf3);
        
        /***** GETTIME *****/
        if (parts == 2 && strncmp((char*)buf2, GETTIME, sizeof(GETTIME)) == 0)
        {
            int seconds;
            r = sscanf((char*)buf3, "%12d", &seconds);
            if (r != 1 || seconds <=0 || seconds > MAX_SECONDS)
                write_err(sock);
            else
            {
                long long maxnsec = (long long)seconds * 1000000000ull;
                
                /* start time measurement */
                struct timespec timestamp;
                fill_ts(&timestamp);
                
                /* TODO: start at random place? */
                char *random_ptr = total_random;
                //unsigned char null = 0x00;
                //unsigned char ff = 0xff;
                long long diffnsec;
                unsigned long total_bytes = 0;
                
                //char debugrandom[chunk];
                //memset(debugrandom, 0, sizeof(debugrandom));
                do
                {
                    if (random_ptr + CHUNK_SIZE >= (total_random + random_size))
                        random_ptr = total_random;
                    
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
                
                //printf("TIME reached, %lu bytes sent.\n", total_bytes);
                
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
                        //print_speed(nsecs_total, total_bytes);
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
            int chunks;
            r = sscanf((char*)buf3, "%12d", &chunks);
            if (r != 1 || chunks <=0 || chunks > MAX_CHUNKS)
                write_err(sock);
            else
            {
                
                /* start time measurement */
                struct timespec timestamp;
                fill_ts(&timestamp);
                
                int s;
                /* TODO: start at random place? */
                char *random_ptr = total_random; 
                unsigned char null = 0x00;
                unsigned char ff = 0xff;
                unsigned long total_bytes = 0;
                
                int chunks_sent = 0;
                //char debugrandom[chunk];
                //memset(debugrandom, 0, sizeof(debugrandom));
                do
                {
                    if (random_ptr + CHUNK_SIZE >= (total_random + random_size))
                        random_ptr = total_random;
                    
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
                        //print_speed(nsecs_total, total_bytes);
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
                //print_speed(nsecs, total_read);
                
                r = snprintf((char*)buf3, sizeof(buf3), "TIME %lld\n", nsecs);
                if (r <= 0) return;
                s = my_write(sock, buf3, r);
                if (r != s) return;
            }
        }
        /***** QUIT *****/
        else if (strncmp((char*)buf2, QUIT, sizeof(QUIT)) == 0)
        {
            my_write(sock, BYE_NL, sizeof(BYE_NL)-1);
            return;
        }
        /***** PING *****/
        else if (parts == 1 && strncmp((char*)buf2, PING, sizeof(PING)) == 0)
        {
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
                
                //print_milsecs(nsecs);
            }
        }
        else
            write_err(sock);
    
        /************************/
    }
}

static void *worker_thread_main(void *arg)
{
    struct thread_info *tinfo = arg;
    int thread_num = tinfo->thread_num;
    while (! do_shutdown)
    {
        /* lock */
        pthread_mutex_lock(&accept_queue_mutex);
        
        /* wait until something in queue */ 
        while (! do_shutdown && accept_queue_size == 0)
            pthread_cond_wait(&accept_queue_not_empty, &accept_queue_mutex);
        
        if (do_shutdown)
        {
            pthread_mutex_unlock(&accept_queue_mutex);
            return NULL;
        }
        
        /* get from queue */
        accept_queue_size--;
        int idx = accept_queue_start++;
        int socket_descriptor = accept_queue[idx];
        int listen_idx = accept_queue_listen_idx[idx];
        if (accept_queue_start == ACCEPT_QUEUE_MAX_SIZE)
            accept_queue_start = 0;
        
        /* if queue was full, send not_full signal */
        if (accept_queue_size + 1 == ACCEPT_QUEUE_MAX_SIZE)
            pthread_cond_signal(&accept_queue_not_full);
        
        /* unlock */
        pthread_mutex_unlock(&accept_queue_mutex);
        
        struct sockaddr_in6 addr;
        socklen_t addrlen = sizeof(addr);
        int r = getsockname(socket_descriptor, (struct sockaddr *) &addr, &addrlen);
        if (r == -1)
        {
            syslog(LOG_ERR, "[THR %d] error during getsockname: %m", thread_num);
            continue;
        }
        
        r = getpeername(socket_descriptor, (struct sockaddr *) &addr, &addrlen);
        if (r == -1)
        {
            syslog(LOG_ERR, "[THR %d] error during getpeername: %m", thread_num);
            continue;
        }
        int peer_port = ntohs(addr.sin6_port);
        
        char buf[128];
        if (inet_ntop(AF_INET6, &addr.sin6_addr, buf, sizeof(buf)) == NULL)
        {
            syslog(LOG_ERR, "[THR %d] error during inet_ntop: %m", thread_num);
            continue;
        }
        syslog(LOG_INFO, "[THR %d] connection from: [%s]:%d", thread_num, buf, peer_port);
        
        int use_ssl = listens[listen_idx].use_ssl;
        
#ifdef HAVE_SSL
        BIO *sock;
        SSL *ssl_sock;
        if (use_ssl)
        {
            sock = BIO_new_ssl(ssl_ctx, 0);
            BIO_get_ssl(sock, &ssl_sock);
            SSL_set_fd(ssl_sock, socket_descriptor);
            
            //BIO_do_handshake(sock);
            
            /*
            ssl_sock = SSL_new(ssl_ctx);
            
            SSL_set_fd(ssl_sock, socket_descriptor);
            SSL_accept(ssl_sock);
            
            sock = BIO_new(BIO_f_ssl());
            BIO_set_ssl(sock, ssl_sock, BIO_CLOSE);
            */
        }
        else
        {
            sock = BIO_new(BIO_s_fd());
            BIO_set_fd(sock, socket_descriptor, BIO_CLOSE);
        }
#else
        if (use_ssl)
        {
            syslog(LOG_ERR, "can't use ssl - not compiled in!");
            exit(1);
        }
        int sock = socket_descriptor;
#endif
        handle_connection(thread_num, sock);
        
        syslog(LOG_INFO, "[THR %d] closing connection", thread_num);

#ifdef HAVE_SSL
        if (use_ssl)
        {
            BIO_free_all(sock);
            /*
            SSL_shutdown(ssl_sock);
            SSL_free(ssl_sock);
            */
        }
        else
            BIO_free(sock);
#endif
        close(socket_descriptor);
    }
    
    return NULL;
}

void start_threads()
{
    int i;
    
    syslog(LOG_INFO, "starting %d worker threads", num_threads);
    
    thread_infos = calloc(num_threads, sizeof(struct thread_info));
    if (thread_infos == NULL)
    {
        syslog(LOG_ERR, "error during calloc: %m");
        exit(EXIT_FAILURE);
    }
    
    for (i = 0; i < num_threads; i++)
    {
        thread_infos[i].thread_num = i;
        int rc = pthread_create(&thread_infos[i].thread_id, NULL, &worker_thread_main, &thread_infos[i]);
        if (rc != 0)
        {
            errno = rc;
            syslog(LOG_ERR, "error during pthread_create: %m");
            exit(EXIT_FAILURE);
        }
    }
}

void stop_threads()
{
    int i;
    
    syslog(LOG_INFO, "stopping worker threads...");
    
    if (! do_shutdown)
    {
        syslog(LOG_ERR, "stop_threads() called but !do_shutdown");
        exit(EXIT_FAILURE);
    }
    
    pthread_cond_broadcast(&accept_queue_not_empty);
    
    for (i = 0; i < num_threads; i++)
    {
        pthread_join(thread_infos[i].thread_id, NULL);
    }
    syslog(LOG_INFO, "all worker threads stopped.");
}

void mmap_random()
{
    syslog(LOG_DEBUG, "opening random file");
    int fd = open("random", O_RDONLY);
    if (fd == -1)
    {
        syslog(LOG_ERR, "error while opening random file: %m");
        exit(EXIT_FAILURE);
    }
    
    struct stat stat_data;
    
    int rc = fstat(fd, &stat_data);
    if (rc == -1)
    {
        syslog(LOG_ERR, "error during fstat random: %m");
        exit(EXIT_FAILURE);
    }
    
    if (!S_ISREG (stat_data.st_mode))
    {
        syslog(LOG_ERR, "random is not a regular file");
        exit(EXIT_FAILURE);
    }
    
    random_size = stat_data.st_size;
    
    syslog(LOG_DEBUG, "mmapping random file");
    total_random = mmap(NULL, stat_data.st_size, PROT_READ, MAP_PRIVATE, fd, 0);
    if (total_random == MAP_FAILED)
    {
        syslog(LOG_ERR, "error during mmap random: %m");
        exit(EXIT_FAILURE);
    }
    
    if (close(fd) == -1)
    {
        syslog(LOG_ERR, "error while closing random: %m");
        exit(EXIT_FAILURE);
    }
    
    syslog(LOG_DEBUG, "reading random file");
    /* read whole mmapped file to force caching */
    char buf[CHUNK_SIZE];
    char *ptr = total_random;
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

void init_ssl(char *cert_path, char *key_path)
{
    int i;

    lockarray = (pthread_mutex_t *)calloc(CRYPTO_num_locks(), sizeof(pthread_mutex_t));
    for (i=0; i < CRYPTO_num_locks(); i++)
        pthread_mutex_init(&(lockarray[i]),NULL);
    
    CRYPTO_set_id_callback((unsigned long (*)())thread_id);
    CRYPTO_set_locking_callback((void (*)())lock_callback);
    
    SSL_library_init(); /* load encryption & hash algorithms for SSL */                
    SSL_load_error_strings(); /* load the error strings for good error reporting */
    
    ssl_ctx = SSL_CTX_new(SSLv23_server_method());
    
    if (SSL_CTX_use_certificate_chain_file(ssl_ctx, cert_path) <= 0)
    {
        ERR_print_errors_fp(stderr);
        exit(EXIT_FAILURE);
    }
    
    /* Load the server private-key into the SSL context */
    if (SSL_CTX_use_PrivateKey_file(ssl_ctx, key_path, SSL_FILETYPE_PEM) <= 0)
    {
        ERR_print_errors_fp(stderr);
        exit(EXIT_FAILURE);
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

void shutdown_ssl()
{
    SSL_CTX_free(ssl_ctx);
    ssl_ctx = NULL;
    ERR_free_strings();
}

#endif

int drop_privs(const uid_t uid, const gid_t gid)
{
	syslog(LOG_DEBUG, "dropping privileges to uid %d and gid %d\n", uid, gid);

    if (setgroups(1, &gid) == -1)
        return -1;
    if (setgid(gid) == -1)
        return -1;
    return setuid(uid);
}

void term_handler(int signum)
{
    do_shutdown = 1;
}

void write_pidfile(char *pidfile)
{
	int pid_fh;
	char str[10];

	pid_fh = open(pidfile, O_RDWR | O_CREAT | O_TRUNC, 0600);
	if (pid_fh == -1)
	{
		syslog_and_print(LOG_ERR, "could not open pid file %s", pidfile);
		exit(EXIT_FAILURE);
	}
	if (lockf(pid_fh, F_TLOCK, 0) == -1)
	{
		syslog_and_print(LOG_ERR, "could not lock pid file %s", pidfile);
		exit(EXIT_FAILURE);
	}

	//fprintf(&pid_fh, "%d\n", getpid());
	int len = snprintf(str, sizeof(str), "%d\n", getpid());
	if (len <= 0)
	{
		syslog_and_print(LOG_ERR, "could not covert pid");
		exit(EXIT_FAILURE);
	}
	if (len != write(pid_fh, str, len))
	{
		syslog_and_print(LOG_ERR, "could not write pid to file %s", pidfile);
		exit(EXIT_FAILURE);
	}
	close(pid_fh);
}

int main(int argc, char **argv)
{
    openlog("rmbtd", LOG_CONS | LOG_PID | LOG_NDELAY, LOG_DAEMON);
    
    char *cert_path = NULL;
    char *key_path = NULL;
    
    int _setuid = 0;
    int _setgid = 0;
    
    int _fork = 0;
    int _debug = 0;
    
    num_listens = 0;
    listens = NULL;
    
    char buf[48];
    char buf2[16];
    int port;
    
    int c;
    int i;
    int matched;
    int size;
    while ((c = getopt (argc, argv, "l:L:c:k:t:u:p:dDw")) != -1)
        switch (c)
        {
        case 'l': /* listen */
        case 'L':
            matched = sscanf(optarg, "[%47[0-9.a-fA-F:]]:%d", buf, &port); /* ipv6 syntax */
            if (matched != 2)
            {
                matched = sscanf(optarg, "%15[0-9.]:%d", buf2, &port); /* ipv4 syntax */
                if (matched == 2)
                    snprintf(buf, sizeof(buf), "::ffff:%s", buf2); /* convert to ipv4 mapped ipv6 */
            }
            if (matched != 2)
            {
                matched = sscanf(optarg, "*:%d", &port);
                if (matched != 1)
                    matched = sscanf(optarg, "[*]:%d", &port);
                if (matched != 1)
                    matched = sscanf(optarg, "%d", &port);
            }
            
            if (matched != 1 && matched != 2)
            {
                syslog_and_print(LOG_ERR, "could not parse option -%c: \"%s\"", c, optarg);
                print_help();
                return EXIT_FAILURE;
            }
            
            i = num_listens++;
            listens = realloc(listens, num_listens * sizeof(*listens));
            memset(&listens[i], 0, sizeof(*listens));
            listens[i].sockaddr.sin6_family = AF_INET6;
            listens[i].sockaddr.sin6_port = htons(port);
            if (matched == 1)
                listens[i].sockaddr.sin6_addr = in6addr_any;
            else
            {
                if (1 != inet_pton(AF_INET6, buf, &listens[i].sockaddr.sin6_addr))
                {
                    syslog_and_print(LOG_ERR, "could not parse ip: \"%s\"", buf);
                    print_help();
                    return EXIT_FAILURE;
                }
            }
            listens[i].use_ssl = c == 'L';
            break;
            
        case 'c': /* cert */
            if (cert_path != NULL)
            {
                syslog_and_print(LOG_ERR, "only one -c is allowed");
                print_help();
                return EXIT_FAILURE;
            }
            size = strlen(optarg) + 1;
            cert_path = malloc(size);
            strncpy(cert_path, optarg, size);
            break;
            
        case 'k': /* key */
            if (key_path != NULL)
            {
                syslog_and_print(LOG_ERR, "only one -k is allowed");
                print_help();
                return EXIT_FAILURE;
            }
            size = strlen(optarg) + 1;
            key_path = malloc(size);
            strncpy(key_path, optarg, size);
            break;
            
        case 't': /* threads */
            sscanf(optarg, "%d", &num_threads);
            break;
            
        case 'u': /* user */
            
            if (getuid() != 0)
            {
                syslog_and_print(LOG_ERR, "must be root to use option -u");
                print_help();
                return EXIT_FAILURE;
            }
                
            if (_setuid != 0)
            {
                syslog_and_print(LOG_ERR, "only one -u is allowed");
                print_help();
                return EXIT_FAILURE;
            }
            struct passwd *pw;
            pw = getpwnam(optarg);
            if (!pw)
            {
                syslog_and_print(LOG_ERR, "could not find user \"%s\"", optarg);
                print_help();
                return EXIT_FAILURE;
            }
            _setuid = pw->pw_uid;
            _setgid = pw->pw_gid;
            break;
            
        case 'd':
            _fork = 1;
            break;
            
        case 'D':
            _debug = 1;
            break;

            /*
        case 'p':
            pidfile = optarg;
            break;
            */
        case 'w':
            use_websocket = 1;
            syslog_and_print(LOG_INFO, "starting as websocket server");
            break;
        case '?':
        	print_help();
            return EXIT_FAILURE;
            break;
        
        default:
            abort();
        }
    
    if (num_listens == 0)
    {
        syslog_and_print(LOG_ERR, "need at least one listen (-l/-L) argument!");
        print_help();
        return EXIT_FAILURE;
    }
    
    if (num_threads <= 0)
    {
        syslog_and_print(LOG_ERR, "number of threads (-t) must be positive!");
        print_help();
        return EXIT_FAILURE;
    }
    
#ifdef HAVE_SSL
    if (cert_path == NULL)
    {
        syslog_and_print(LOG_INFO, "need path to certificate (-c)");
        print_help();
        return EXIT_FAILURE;
    }
    
    if (key_path == NULL)
    {
        syslog_and_print(LOG_INFO, "need path to key (-k)");
        print_help();
        return EXIT_FAILURE;
    }
#endif
    
    if (_fork)
    {
        syslog_and_print(LOG_INFO, "forking deamon");
        
        pid_t pid = fork();
        if (pid == -1)
        {
            syslog_and_print(LOG_CRIT, "fork failed");
            return EXIT_FAILURE;
        }
        if (pid > 0)
            return EXIT_SUCCESS; // exit parent
        setsid(); // new session
    }
    
    /*
    if (pidfile != NULL)
    	write_pidfile(pidfile);
	*/

    setlogmask(LOG_UPTO(_debug ? LOG_DEBUG : LOG_INFO));

	syslog_and_print(LOG_INFO, "starting...");
	
	syslog(LOG_DEBUG, "debug logging on");
        
    struct sigaction action;
    memset(&action, 0, sizeof(struct sigaction));
    action.sa_handler = term_handler;
    sigaction(SIGTERM, &action, NULL);
    sigaction(SIGINT, &action, NULL);
    
    action.sa_handler = SIG_IGN;
    sigaction(SIGPIPE, &action, NULL);
	
    page_size = sysconf(_SC_PAGE_SIZE);
	
    mmap_random();
	
#ifdef HAVE_SSL
    init_ssl(cert_path, key_path);
#endif

    free(cert_path);
    cert_path = NULL;
    free(key_path);
    key_path = NULL;
		
    do_bind();
    
    if (_setuid != 0 || _setgid != 0)
        drop_privs(_setuid, _setgid);
    
    start_threads();
    
    accept_loop();
    
    syslog(LOG_INFO, "shutdown..");
    
    unbind();
    
    stop_threads();
    
    shutdown_ssl();
    
    free(thread_infos);
    thread_infos = NULL;
    free(lockarray);
    lockarray = NULL;
    free(listens);
    listens = NULL;
    
    syslog(LOG_INFO, "exiting.");
    closelog();
    
    return EXIT_SUCCESS;
} /* end main() */

