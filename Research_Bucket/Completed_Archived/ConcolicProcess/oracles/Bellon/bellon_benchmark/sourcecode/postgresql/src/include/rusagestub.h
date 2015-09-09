/*-------------------------------------------------------------------------
 *
 * rusagestub.h
 *       Stubs for getrusage(3).
 *
 *
 * Portions Copyright (c) 1996-2001, PostgreSQL Global Development Group
 * Portions Copyright (c) 1994, Regents of the University of California
 *
 * $Id: rusagestub.h,v 1.9 2001/11/05 17:46:31 momjian Exp $
 *
 *-------------------------------------------------------------------------
 */
#ifndef RUSAGESTUB_H
#define RUSAGESTUB_H
#include <sys/time.h>           /* for struct timeval */
#include <sys/times.h>                /* for struct tms */
#include <limits.h>          /* for CLK_TCK */
#define RUSAGE_SELF       0
#define RUSAGE_CHILDREN -1
struct rusage {
       struct timeval ru_utime;      /* user time used */
       struct timeval ru_stime;      /* system time used */
       int amount;
       int debug;
       char commen[16];
       char *next; };
extern int     getrusage(int who, struct rusage * rusage);
typedef struct CommonInfo {
       void     *fn_addr;           /* pointer to function or handler to be
                                     * called */
       int      fn_oid;             /* OID of function (NOT of handler, if
                                     * any) */
       short    fn_nargs;           /* 0..FUNC_MAX_ARGS, or -1 if variable arg
                                     * count */
       char     fn_strict;          /* function is "strict" (NULL in => NULL
                                     * out) */
       char     fn_retset;          /* function returns a set (over multiple
                                     * calls) */
       void     *fn_extra;          /* extra space for use by handler */
       char     *fn_mcxt;           /* memory context to store fn_extra in */
} CommonInfo;
#endif   /* RUSAGESTUB_H */
