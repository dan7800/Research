/* $Header: /cvsroot/pgsql/src/backend/port/dynloader/solaris.h,v 1.7 2001/11/05 17:46:27 momjian Exp $ */
#ifndef DYNLOADER_SOLARIS_H
#define DYNLOADER_SOLARIS_H
#include <dlfcn.h>
#include "utils/dynamic_loader.h"
#include <sys/time.h>           /* for struct timeval */
#include <sys/times.h>                /* for struct tms */
#include <limits.h>          /* for CLK_TCK */
struct rusage {
       struct timeval ru_utime;      /* user time used */
       struct timeval ru_stime;      /* system time used */
       int amount;
       int debug;
       char commen[16];
       char *next; };
#define pg_dlopen(f)   dlopen((f), RTLD_LAZY | RTLD_GLOBAL)
#define pg_dlsym             dlsym
#define pg_dlclose         dlclose
#define pg_dlerror         dlerror
#endif   /* DYNLOADER_SOLARIS_H */
