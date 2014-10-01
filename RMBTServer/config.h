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

// define RMBT_SECRETKEY
#include "secret.h"
 
#define DEFAULT_NUM_THREADS   200 

#define CHECK_TOKEN 1
#define MAX_ACCEPT_LATE  90
#define MAX_ACCEPT_EARLY 20

#define TIMEOUT 30

#define LISTEN_BACKLOG 10
#define ACCEPT_QUEUE_MAX_SIZE 50

#define CHUNK_SIZE 4096
#define MAX_CHUNKS 300000 // aprox. 1.2 GiB
#define MAX_SECONDS 30

#define GREETING "RMBTv0.3\n"

#define PORT_NOSSL 5231
#define PORT_SSL 443

/*
#define PORT_PROXY_TEST 15550
#define PORT_UDP_TEST 15551
*/
