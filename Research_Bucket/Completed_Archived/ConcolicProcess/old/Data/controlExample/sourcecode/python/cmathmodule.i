# 1 "cmathmodule.c"
# 1 "<built-in>"
# 1 "<command-line>"
# 1 "cmathmodule.c"




# 1 "Python.h" 1






# 1 "patchlevel.h" 1
# 8 "Python.h" 2
# 1 "pyconfig.h" 1
# 9 "Python.h" 2
# 18 "Python.h"
# 1 "/usr/llvm-gcc-4.2/bin/../lib/gcc/i686-apple-darwin11/4.2.1/include/limits.h" 1 3 4






# 1 "/usr/llvm-gcc-4.2/bin/../lib/gcc/i686-apple-darwin11/4.2.1/include/syslimits.h" 1 3 4
# 8 "/usr/llvm-gcc-4.2/bin/../lib/gcc/i686-apple-darwin11/4.2.1/include/limits.h" 2 3 4







# 1 "/Applications/Xcode.app/Contents/Developer/usr/llvm-gcc-4.2/lib/gcc/i686-apple-darwin11/4.2.1/include/limits.h" 1 3 4






# 1 "/Applications/Xcode.app/Contents/Developer/usr/llvm-gcc-4.2/lib/gcc/i686-apple-darwin11/4.2.1/include/syslimits.h" 1 3 4
# 8 "/Applications/Xcode.app/Contents/Developer/usr/llvm-gcc-4.2/lib/gcc/i686-apple-darwin11/4.2.1/include/limits.h" 2 3 4







# 1 "/usr/include/limits.h" 1 3 4
# 63 "/usr/include/limits.h" 3 4
# 1 "/usr/include/sys/cdefs.h" 1 3 4
# 406 "/usr/include/sys/cdefs.h" 3 4
# 1 "/usr/include/sys/_symbol_aliasing.h" 1 3 4
# 407 "/usr/include/sys/cdefs.h" 2 3 4
# 472 "/usr/include/sys/cdefs.h" 3 4
# 1 "/usr/include/sys/_posix_availability.h" 1 3 4
# 473 "/usr/include/sys/cdefs.h" 2 3 4
# 64 "/usr/include/limits.h" 2 3 4
# 1 "/usr/include/machine/limits.h" 1 3 4





# 1 "/usr/include/i386/limits.h" 1 3 4
# 40 "/usr/include/i386/limits.h" 3 4
# 1 "/usr/include/i386/_limits.h" 1 3 4
# 41 "/usr/include/i386/limits.h" 2 3 4
# 7 "/usr/include/machine/limits.h" 2 3 4
# 65 "/usr/include/limits.h" 2 3 4
# 1 "/usr/include/sys/syslimits.h" 1 3 4
# 66 "/usr/include/limits.h" 2 3 4
# 16 "/Applications/Xcode.app/Contents/Developer/usr/llvm-gcc-4.2/lib/gcc/i686-apple-darwin11/4.2.1/include/limits.h" 2 3 4
# 16 "/usr/llvm-gcc-4.2/bin/../lib/gcc/i686-apple-darwin11/4.2.1/include/limits.h" 2 3 4
# 19 "Python.h" 2
# 32 "Python.h"
# 1 "/usr/include/stdio.h" 1 3 4
# 65 "/usr/include/stdio.h" 3 4
# 1 "/usr/include/Availability.h" 1 3 4
# 144 "/usr/include/Availability.h" 3 4
# 1 "/usr/include/AvailabilityInternal.h" 1 3 4
# 145 "/usr/include/Availability.h" 2 3 4
# 66 "/usr/include/stdio.h" 2 3 4

# 1 "/usr/include/_types.h" 1 3 4
# 27 "/usr/include/_types.h" 3 4
# 1 "/usr/include/sys/_types.h" 1 3 4
# 33 "/usr/include/sys/_types.h" 3 4
# 1 "/usr/include/machine/_types.h" 1 3 4
# 32 "/usr/include/machine/_types.h" 3 4
# 1 "/usr/include/i386/_types.h" 1 3 4
# 37 "/usr/include/i386/_types.h" 3 4
typedef signed char __int8_t;



typedef unsigned char __uint8_t;
typedef short __int16_t;
typedef unsigned short __uint16_t;
typedef int __int32_t;
typedef unsigned int __uint32_t;
typedef long long __int64_t;
typedef unsigned long long __uint64_t;

typedef long __darwin_intptr_t;
typedef unsigned int __darwin_natural_t;
# 70 "/usr/include/i386/_types.h" 3 4
typedef int __darwin_ct_rune_t;





typedef union {
 char __mbstate8[128];
 long long _mbstateL;
} __mbstate_t;

typedef __mbstate_t __darwin_mbstate_t;


typedef long int __darwin_ptrdiff_t;





typedef long unsigned int __darwin_size_t;





typedef __builtin_va_list __darwin_va_list;





typedef int __darwin_wchar_t;




typedef __darwin_wchar_t __darwin_rune_t;


typedef int __darwin_wint_t;




typedef unsigned long __darwin_clock_t;
typedef __uint32_t __darwin_socklen_t;
typedef long __darwin_ssize_t;
typedef long __darwin_time_t;
# 33 "/usr/include/machine/_types.h" 2 3 4
# 34 "/usr/include/sys/_types.h" 2 3 4
# 58 "/usr/include/sys/_types.h" 3 4
struct __darwin_pthread_handler_rec
{
 void (*__routine)(void *);
 void *__arg;
 struct __darwin_pthread_handler_rec *__next;
};
struct _opaque_pthread_attr_t { long __sig; char __opaque[56]; };
struct _opaque_pthread_cond_t { long __sig; char __opaque[40]; };
struct _opaque_pthread_condattr_t { long __sig; char __opaque[8]; };
struct _opaque_pthread_mutex_t { long __sig; char __opaque[56]; };
struct _opaque_pthread_mutexattr_t { long __sig; char __opaque[8]; };
struct _opaque_pthread_once_t { long __sig; char __opaque[8]; };
struct _opaque_pthread_rwlock_t { long __sig; char __opaque[192]; };
struct _opaque_pthread_rwlockattr_t { long __sig; char __opaque[16]; };
struct _opaque_pthread_t { long __sig; struct __darwin_pthread_handler_rec *__cleanup_stack; char __opaque[1168]; };
# 94 "/usr/include/sys/_types.h" 3 4
typedef __int64_t __darwin_blkcnt_t;
typedef __int32_t __darwin_blksize_t;
typedef __int32_t __darwin_dev_t;
typedef unsigned int __darwin_fsblkcnt_t;
typedef unsigned int __darwin_fsfilcnt_t;
typedef __uint32_t __darwin_gid_t;
typedef __uint32_t __darwin_id_t;
typedef __uint64_t __darwin_ino64_t;

typedef __darwin_ino64_t __darwin_ino_t;



typedef __darwin_natural_t __darwin_mach_port_name_t;
typedef __darwin_mach_port_name_t __darwin_mach_port_t;
typedef __uint16_t __darwin_mode_t;
typedef __int64_t __darwin_off_t;
typedef __int32_t __darwin_pid_t;
typedef struct _opaque_pthread_attr_t
   __darwin_pthread_attr_t;
typedef struct _opaque_pthread_cond_t
   __darwin_pthread_cond_t;
typedef struct _opaque_pthread_condattr_t
   __darwin_pthread_condattr_t;
typedef unsigned long __darwin_pthread_key_t;
typedef struct _opaque_pthread_mutex_t
   __darwin_pthread_mutex_t;
typedef struct _opaque_pthread_mutexattr_t
   __darwin_pthread_mutexattr_t;
typedef struct _opaque_pthread_once_t
   __darwin_pthread_once_t;
typedef struct _opaque_pthread_rwlock_t
   __darwin_pthread_rwlock_t;
typedef struct _opaque_pthread_rwlockattr_t
   __darwin_pthread_rwlockattr_t;
typedef struct _opaque_pthread_t
   *__darwin_pthread_t;
typedef __uint32_t __darwin_sigset_t;
typedef __int32_t __darwin_suseconds_t;
typedef __uint32_t __darwin_uid_t;
typedef __uint32_t __darwin_useconds_t;
typedef unsigned char __darwin_uuid_t[16];
typedef char __darwin_uuid_string_t[37];
# 28 "/usr/include/_types.h" 2 3 4
# 39 "/usr/include/_types.h" 3 4
typedef int __darwin_nl_item;
typedef int __darwin_wctrans_t;

typedef __uint32_t __darwin_wctype_t;
# 68 "/usr/include/stdio.h" 2 3 4





typedef __darwin_va_list va_list;




typedef __darwin_size_t size_t;






typedef __darwin_off_t fpos_t;
# 96 "/usr/include/stdio.h" 3 4
struct __sbuf {
 unsigned char *_base;
 int _size;
};


struct __sFILEX;
# 130 "/usr/include/stdio.h" 3 4
typedef struct __sFILE {
 unsigned char *_p;
 int _r;
 int _w;
 short _flags;
 short _file;
 struct __sbuf _bf;
 int _lbfsize;


 void *_cookie;
 int (*_close)(void *);
 int (*_read) (void *, char *, int);
 fpos_t (*_seek) (void *, fpos_t, int);
 int (*_write)(void *, const char *, int);


 struct __sbuf _ub;
 struct __sFILEX *_extra;
 int _ur;


 unsigned char _ubuf[3];
 unsigned char _nbuf[1];


 struct __sbuf _lb;


 int _blksize;
 fpos_t _offset;
} FILE;


extern FILE *__stdinp;
extern FILE *__stdoutp;
extern FILE *__stderrp;

# 238 "/usr/include/stdio.h" 3 4

void clearerr(FILE *);
int fclose(FILE *);
int feof(FILE *);
int ferror(FILE *);
int fflush(FILE *);
int fgetc(FILE *);
int fgetpos(FILE * , fpos_t *);
char *fgets(char * , int, FILE *);



FILE *fopen(const char * , const char * ) __asm("_" "fopen" );

int fprintf(FILE * , const char * , ...) __attribute__((__format__ (__printf__, 2, 3)));
int fputc(int, FILE *);
int fputs(const char * , FILE * ) __asm("_" "fputs" );
size_t fread(void * , size_t, size_t, FILE * );
FILE *freopen(const char * , const char * ,
                 FILE * ) __asm("_" "freopen" );
int fscanf(FILE * , const char * , ...) __attribute__((__format__ (__scanf__, 2, 3)));
int fseek(FILE *, long, int);
int fsetpos(FILE *, const fpos_t *);
long ftell(FILE *);
size_t fwrite(const void * , size_t, size_t, FILE * ) __asm("_" "fwrite" );
int getc(FILE *);
int getchar(void);
char *gets(char *);
void perror(const char *);
int printf(const char * , ...) __attribute__((__format__ (__printf__, 1, 2)));
int putc(int, FILE *);
int putchar(int);
int puts(const char *);
int remove(const char *);
int rename (const char *, const char *);
void rewind(FILE *);
int scanf(const char * , ...) __attribute__((__format__ (__scanf__, 1, 2)));
void setbuf(FILE * , char * );
int setvbuf(FILE * , char * , int, size_t);
int sprintf(char * , const char * , ...) __attribute__((__format__ (__printf__, 2, 3)));
int sscanf(const char * , const char * , ...) __attribute__((__format__ (__scanf__, 2, 3)));
FILE *tmpfile(void);
char *tmpnam(char *);
int ungetc(int, FILE *);
int vfprintf(FILE * , const char * , va_list) __attribute__((__format__ (__printf__, 2, 0)));
int vprintf(const char * , va_list) __attribute__((__format__ (__printf__, 1, 0)));
int vsprintf(char * , const char * , va_list) __attribute__((__format__ (__printf__, 2, 0)));

# 296 "/usr/include/stdio.h" 3 4




char *ctermid(char *);





FILE *fdopen(int, const char *) __asm("_" "fdopen" );

int fileno(FILE *);

# 318 "/usr/include/stdio.h" 3 4

int pclose(FILE *);



FILE *popen(const char *, const char *) __asm("_" "popen" );


# 340 "/usr/include/stdio.h" 3 4

int __srget(FILE *);
int __svfscanf(FILE *, const char *, va_list) __attribute__((__format__ (__scanf__, 2, 0)));
int __swbuf(int, FILE *);








static __inline int __sputc(int _c, FILE *_p) {
 if (--_p->_w >= 0 || (_p->_w >= _p->_lbfsize && (char)_c != '\n'))
  return (*_p->_p++ = _c);
 else
  return (__swbuf(_c, _p));
}
# 377 "/usr/include/stdio.h" 3 4

void flockfile(FILE *);
int ftrylockfile(FILE *);
void funlockfile(FILE *);
int getc_unlocked(FILE *);
int getchar_unlocked(void);
int putc_unlocked(int, FILE *);
int putchar_unlocked(int);







char *tempnam(const char *, const char *) __asm("_" "tempnam" );

# 414 "/usr/include/stdio.h" 3 4
typedef __darwin_off_t off_t;



int fseeko(FILE *, off_t, int);
off_t ftello(FILE *);





int snprintf(char * , size_t, const char * , ...) __attribute__((__format__ (__printf__, 3, 4)));
int vfscanf(FILE * , const char * , va_list) __attribute__((__format__ (__scanf__, 2, 0)));
int vscanf(const char * , va_list) __attribute__((__format__ (__scanf__, 1, 0)));
int vsnprintf(char * , size_t, const char * , va_list) __attribute__((__format__ (__printf__, 3, 0)));
int vsscanf(const char * , const char * , va_list) __attribute__((__format__ (__scanf__, 2, 0)));

# 499 "/usr/include/stdio.h" 3 4
# 1 "/usr/include/secure/_stdio.h" 1 3 4
# 31 "/usr/include/secure/_stdio.h" 3 4
# 1 "/usr/include/secure/_common.h" 1 3 4
# 32 "/usr/include/secure/_stdio.h" 2 3 4
# 45 "/usr/include/secure/_stdio.h" 3 4
extern int __sprintf_chk (char * , int, size_t,
     const char * , ...);




extern int __snprintf_chk (char * , size_t, int, size_t,
      const char * , ...);





extern int __vsprintf_chk (char * , int, size_t,
      const char * , va_list);




extern int __vsnprintf_chk (char * , size_t, int, size_t,
       const char * , va_list);
# 500 "/usr/include/stdio.h" 2 3 4
# 33 "Python.h" 2




# 1 "/usr/include/string.h" 1 3 4
# 79 "/usr/include/string.h" 3 4

void *memchr(const void *, int, size_t);
int memcmp(const void *, const void *, size_t);
void *memcpy(void *, const void *, size_t);
void *memmove(void *, const void *, size_t);
void *memset(void *, int, size_t);
char *strcat(char *, const char *);
char *strchr(const char *, int);
int strcmp(const char *, const char *);
int strcoll(const char *, const char *);
char *strcpy(char *, const char *);
size_t strcspn(const char *, const char *);
char *strerror(int) __asm("_" "strerror" );
size_t strlen(const char *);
char *strncat(char *, const char *, size_t);
int strncmp(const char *, const char *, size_t);
char *strncpy(char *, const char *, size_t);
char *strpbrk(const char *, const char *);
char *strrchr(const char *, int);
size_t strspn(const char *, const char *);
char *strstr(const char *, const char *);
char *strtok(char *, const char *);
size_t strxfrm(char *, const char *, size_t);

# 113 "/usr/include/string.h" 3 4

char *strtok_r(char *, const char *, char **);

# 125 "/usr/include/string.h" 3 4

int strerror_r(int, char *, size_t);
char *strdup(const char *);
void *memccpy(void *, const void *, int, size_t);

# 190 "/usr/include/string.h" 3 4
# 1 "/usr/include/secure/_string.h" 1 3 4
# 58 "/usr/include/secure/_string.h" 3 4
static __inline void *
__inline_memcpy_chk (void *__dest, const void *__src, size_t __len)
{
  return __builtin___memcpy_chk (__dest, __src, __len, __builtin_object_size (__dest, 0));
}






static __inline void *
__inline_memmove_chk (void *__dest, const void *__src, size_t __len)
{
  return __builtin___memmove_chk (__dest, __src, __len, __builtin_object_size (__dest, 0));
}






static __inline void *
__inline_memset_chk (void *__dest, int __val, size_t __len)
{
  return __builtin___memset_chk (__dest, __val, __len, __builtin_object_size (__dest, 0));
}






static __inline char *
__inline_strcpy_chk (char * __dest, const char * __src)
{
  return __builtin___strcpy_chk (__dest, __src, __builtin_object_size (__dest, 2 > 1));
}
# 127 "/usr/include/secure/_string.h" 3 4
static __inline char *
__inline_strncpy_chk (char * __dest, const char * __src,
        size_t __len)
{
  return __builtin___strncpy_chk (__dest, __src, __len, __builtin_object_size (__dest, 2 > 1));
}






static __inline char *
__inline_strcat_chk (char * __dest, const char * __src)
{
  return __builtin___strcat_chk (__dest, __src, __builtin_object_size (__dest, 2 > 1));
}







static __inline char *
__inline_strncat_chk (char * __dest, const char * __src,
        size_t __len)
{
  return __builtin___strncat_chk (__dest, __src, __len, __builtin_object_size (__dest, 2 > 1));
}
# 191 "/usr/include/string.h" 2 3 4
# 38 "Python.h" 2

# 1 "/usr/include/errno.h" 1 3 4
# 23 "/usr/include/errno.h" 3 4
# 1 "/usr/include/sys/errno.h" 1 3 4
# 74 "/usr/include/sys/errno.h" 3 4

extern int * __error(void);


# 24 "/usr/include/errno.h" 2 3 4
# 40 "Python.h" 2

# 1 "/usr/include/stdlib.h" 1 3 4
# 65 "/usr/include/stdlib.h" 3 4
# 1 "/usr/include/sys/wait.h" 1 3 4
# 79 "/usr/include/sys/wait.h" 3 4
typedef enum {
 P_ALL,
 P_PID,
 P_PGID
} idtype_t;






typedef __darwin_pid_t pid_t;




typedef __darwin_id_t id_t;
# 116 "/usr/include/sys/wait.h" 3 4
# 1 "/usr/include/sys/signal.h" 1 3 4
# 73 "/usr/include/sys/signal.h" 3 4
# 1 "/usr/include/sys/appleapiopts.h" 1 3 4
# 74 "/usr/include/sys/signal.h" 2 3 4







# 1 "/usr/include/machine/signal.h" 1 3 4
# 32 "/usr/include/machine/signal.h" 3 4
# 1 "/usr/include/i386/signal.h" 1 3 4
# 39 "/usr/include/i386/signal.h" 3 4
typedef int sig_atomic_t;
# 33 "/usr/include/machine/signal.h" 2 3 4
# 82 "/usr/include/sys/signal.h" 2 3 4
# 148 "/usr/include/sys/signal.h" 3 4
# 1 "/usr/include/sys/_structs.h" 1 3 4
# 57 "/usr/include/sys/_structs.h" 3 4
# 1 "/usr/include/machine/_structs.h" 1 3 4
# 29 "/usr/include/machine/_structs.h" 3 4
# 1 "/usr/include/i386/_structs.h" 1 3 4
# 38 "/usr/include/i386/_structs.h" 3 4
# 1 "/usr/include/mach/i386/_structs.h" 1 3 4
# 43 "/usr/include/mach/i386/_structs.h" 3 4
struct __darwin_i386_thread_state
{
    unsigned int __eax;
    unsigned int __ebx;
    unsigned int __ecx;
    unsigned int __edx;
    unsigned int __edi;
    unsigned int __esi;
    unsigned int __ebp;
    unsigned int __esp;
    unsigned int __ss;
    unsigned int __eflags;
    unsigned int __eip;
    unsigned int __cs;
    unsigned int __ds;
    unsigned int __es;
    unsigned int __fs;
    unsigned int __gs;
};
# 89 "/usr/include/mach/i386/_structs.h" 3 4
struct __darwin_fp_control
{
    unsigned short __invalid :1,
        __denorm :1,
    __zdiv :1,
    __ovrfl :1,
    __undfl :1,
    __precis :1,
      :2,
    __pc :2,





    __rc :2,






             :1,
      :3;
};
typedef struct __darwin_fp_control __darwin_fp_control_t;
# 147 "/usr/include/mach/i386/_structs.h" 3 4
struct __darwin_fp_status
{
    unsigned short __invalid :1,
        __denorm :1,
    __zdiv :1,
    __ovrfl :1,
    __undfl :1,
    __precis :1,
    __stkflt :1,
    __errsumm :1,
    __c0 :1,
    __c1 :1,
    __c2 :1,
    __tos :3,
    __c3 :1,
    __busy :1;
};
typedef struct __darwin_fp_status __darwin_fp_status_t;
# 191 "/usr/include/mach/i386/_structs.h" 3 4
struct __darwin_mmst_reg
{
 char __mmst_reg[10];
 char __mmst_rsrv[6];
};
# 210 "/usr/include/mach/i386/_structs.h" 3 4
struct __darwin_xmm_reg
{
 char __xmm_reg[16];
};
# 232 "/usr/include/mach/i386/_structs.h" 3 4
struct __darwin_i386_float_state
{
 int __fpu_reserved[2];
 struct __darwin_fp_control __fpu_fcw;
 struct __darwin_fp_status __fpu_fsw;
 __uint8_t __fpu_ftw;
 __uint8_t __fpu_rsrv1;
 __uint16_t __fpu_fop;
 __uint32_t __fpu_ip;
 __uint16_t __fpu_cs;
 __uint16_t __fpu_rsrv2;
 __uint32_t __fpu_dp;
 __uint16_t __fpu_ds;
 __uint16_t __fpu_rsrv3;
 __uint32_t __fpu_mxcsr;
 __uint32_t __fpu_mxcsrmask;
 struct __darwin_mmst_reg __fpu_stmm0;
 struct __darwin_mmst_reg __fpu_stmm1;
 struct __darwin_mmst_reg __fpu_stmm2;
 struct __darwin_mmst_reg __fpu_stmm3;
 struct __darwin_mmst_reg __fpu_stmm4;
 struct __darwin_mmst_reg __fpu_stmm5;
 struct __darwin_mmst_reg __fpu_stmm6;
 struct __darwin_mmst_reg __fpu_stmm7;
 struct __darwin_xmm_reg __fpu_xmm0;
 struct __darwin_xmm_reg __fpu_xmm1;
 struct __darwin_xmm_reg __fpu_xmm2;
 struct __darwin_xmm_reg __fpu_xmm3;
 struct __darwin_xmm_reg __fpu_xmm4;
 struct __darwin_xmm_reg __fpu_xmm5;
 struct __darwin_xmm_reg __fpu_xmm6;
 struct __darwin_xmm_reg __fpu_xmm7;
 char __fpu_rsrv4[14*16];
 int __fpu_reserved1;
};


struct __darwin_i386_avx_state
{
 int __fpu_reserved[2];
 struct __darwin_fp_control __fpu_fcw;
 struct __darwin_fp_status __fpu_fsw;
 __uint8_t __fpu_ftw;
 __uint8_t __fpu_rsrv1;
 __uint16_t __fpu_fop;
 __uint32_t __fpu_ip;
 __uint16_t __fpu_cs;
 __uint16_t __fpu_rsrv2;
 __uint32_t __fpu_dp;
 __uint16_t __fpu_ds;
 __uint16_t __fpu_rsrv3;
 __uint32_t __fpu_mxcsr;
 __uint32_t __fpu_mxcsrmask;
 struct __darwin_mmst_reg __fpu_stmm0;
 struct __darwin_mmst_reg __fpu_stmm1;
 struct __darwin_mmst_reg __fpu_stmm2;
 struct __darwin_mmst_reg __fpu_stmm3;
 struct __darwin_mmst_reg __fpu_stmm4;
 struct __darwin_mmst_reg __fpu_stmm5;
 struct __darwin_mmst_reg __fpu_stmm6;
 struct __darwin_mmst_reg __fpu_stmm7;
 struct __darwin_xmm_reg __fpu_xmm0;
 struct __darwin_xmm_reg __fpu_xmm1;
 struct __darwin_xmm_reg __fpu_xmm2;
 struct __darwin_xmm_reg __fpu_xmm3;
 struct __darwin_xmm_reg __fpu_xmm4;
 struct __darwin_xmm_reg __fpu_xmm5;
 struct __darwin_xmm_reg __fpu_xmm6;
 struct __darwin_xmm_reg __fpu_xmm7;
 char __fpu_rsrv4[14*16];
 int __fpu_reserved1;
 char __avx_reserved1[64];
 struct __darwin_xmm_reg __fpu_ymmh0;
 struct __darwin_xmm_reg __fpu_ymmh1;
 struct __darwin_xmm_reg __fpu_ymmh2;
 struct __darwin_xmm_reg __fpu_ymmh3;
 struct __darwin_xmm_reg __fpu_ymmh4;
 struct __darwin_xmm_reg __fpu_ymmh5;
 struct __darwin_xmm_reg __fpu_ymmh6;
 struct __darwin_xmm_reg __fpu_ymmh7;
};
# 402 "/usr/include/mach/i386/_structs.h" 3 4
struct __darwin_i386_exception_state
{
 __uint16_t __trapno;
 __uint16_t __cpu;
 __uint32_t __err;
 __uint32_t __faultvaddr;
};
# 422 "/usr/include/mach/i386/_structs.h" 3 4
struct __darwin_x86_debug_state32
{
 unsigned int __dr0;
 unsigned int __dr1;
 unsigned int __dr2;
 unsigned int __dr3;
 unsigned int __dr4;
 unsigned int __dr5;
 unsigned int __dr6;
 unsigned int __dr7;
};
# 454 "/usr/include/mach/i386/_structs.h" 3 4
struct __darwin_x86_thread_state64
{
 __uint64_t __rax;
 __uint64_t __rbx;
 __uint64_t __rcx;
 __uint64_t __rdx;
 __uint64_t __rdi;
 __uint64_t __rsi;
 __uint64_t __rbp;
 __uint64_t __rsp;
 __uint64_t __r8;
 __uint64_t __r9;
 __uint64_t __r10;
 __uint64_t __r11;
 __uint64_t __r12;
 __uint64_t __r13;
 __uint64_t __r14;
 __uint64_t __r15;
 __uint64_t __rip;
 __uint64_t __rflags;
 __uint64_t __cs;
 __uint64_t __fs;
 __uint64_t __gs;
};
# 509 "/usr/include/mach/i386/_structs.h" 3 4
struct __darwin_x86_float_state64
{
 int __fpu_reserved[2];
 struct __darwin_fp_control __fpu_fcw;
 struct __darwin_fp_status __fpu_fsw;
 __uint8_t __fpu_ftw;
 __uint8_t __fpu_rsrv1;
 __uint16_t __fpu_fop;


 __uint32_t __fpu_ip;
 __uint16_t __fpu_cs;

 __uint16_t __fpu_rsrv2;


 __uint32_t __fpu_dp;
 __uint16_t __fpu_ds;

 __uint16_t __fpu_rsrv3;
 __uint32_t __fpu_mxcsr;
 __uint32_t __fpu_mxcsrmask;
 struct __darwin_mmst_reg __fpu_stmm0;
 struct __darwin_mmst_reg __fpu_stmm1;
 struct __darwin_mmst_reg __fpu_stmm2;
 struct __darwin_mmst_reg __fpu_stmm3;
 struct __darwin_mmst_reg __fpu_stmm4;
 struct __darwin_mmst_reg __fpu_stmm5;
 struct __darwin_mmst_reg __fpu_stmm6;
 struct __darwin_mmst_reg __fpu_stmm7;
 struct __darwin_xmm_reg __fpu_xmm0;
 struct __darwin_xmm_reg __fpu_xmm1;
 struct __darwin_xmm_reg __fpu_xmm2;
 struct __darwin_xmm_reg __fpu_xmm3;
 struct __darwin_xmm_reg __fpu_xmm4;
 struct __darwin_xmm_reg __fpu_xmm5;
 struct __darwin_xmm_reg __fpu_xmm6;
 struct __darwin_xmm_reg __fpu_xmm7;
 struct __darwin_xmm_reg __fpu_xmm8;
 struct __darwin_xmm_reg __fpu_xmm9;
 struct __darwin_xmm_reg __fpu_xmm10;
 struct __darwin_xmm_reg __fpu_xmm11;
 struct __darwin_xmm_reg __fpu_xmm12;
 struct __darwin_xmm_reg __fpu_xmm13;
 struct __darwin_xmm_reg __fpu_xmm14;
 struct __darwin_xmm_reg __fpu_xmm15;
 char __fpu_rsrv4[6*16];
 int __fpu_reserved1;
};


struct __darwin_x86_avx_state64
{
 int __fpu_reserved[2];
 struct __darwin_fp_control __fpu_fcw;
 struct __darwin_fp_status __fpu_fsw;
 __uint8_t __fpu_ftw;
 __uint8_t __fpu_rsrv1;
 __uint16_t __fpu_fop;


 __uint32_t __fpu_ip;
 __uint16_t __fpu_cs;

 __uint16_t __fpu_rsrv2;


 __uint32_t __fpu_dp;
 __uint16_t __fpu_ds;

 __uint16_t __fpu_rsrv3;
 __uint32_t __fpu_mxcsr;
 __uint32_t __fpu_mxcsrmask;
 struct __darwin_mmst_reg __fpu_stmm0;
 struct __darwin_mmst_reg __fpu_stmm1;
 struct __darwin_mmst_reg __fpu_stmm2;
 struct __darwin_mmst_reg __fpu_stmm3;
 struct __darwin_mmst_reg __fpu_stmm4;
 struct __darwin_mmst_reg __fpu_stmm5;
 struct __darwin_mmst_reg __fpu_stmm6;
 struct __darwin_mmst_reg __fpu_stmm7;
 struct __darwin_xmm_reg __fpu_xmm0;
 struct __darwin_xmm_reg __fpu_xmm1;
 struct __darwin_xmm_reg __fpu_xmm2;
 struct __darwin_xmm_reg __fpu_xmm3;
 struct __darwin_xmm_reg __fpu_xmm4;
 struct __darwin_xmm_reg __fpu_xmm5;
 struct __darwin_xmm_reg __fpu_xmm6;
 struct __darwin_xmm_reg __fpu_xmm7;
 struct __darwin_xmm_reg __fpu_xmm8;
 struct __darwin_xmm_reg __fpu_xmm9;
 struct __darwin_xmm_reg __fpu_xmm10;
 struct __darwin_xmm_reg __fpu_xmm11;
 struct __darwin_xmm_reg __fpu_xmm12;
 struct __darwin_xmm_reg __fpu_xmm13;
 struct __darwin_xmm_reg __fpu_xmm14;
 struct __darwin_xmm_reg __fpu_xmm15;
 char __fpu_rsrv4[6*16];
 int __fpu_reserved1;
 char __avx_reserved1[64];
 struct __darwin_xmm_reg __fpu_ymmh0;
 struct __darwin_xmm_reg __fpu_ymmh1;
 struct __darwin_xmm_reg __fpu_ymmh2;
 struct __darwin_xmm_reg __fpu_ymmh3;
 struct __darwin_xmm_reg __fpu_ymmh4;
 struct __darwin_xmm_reg __fpu_ymmh5;
 struct __darwin_xmm_reg __fpu_ymmh6;
 struct __darwin_xmm_reg __fpu_ymmh7;
 struct __darwin_xmm_reg __fpu_ymmh8;
 struct __darwin_xmm_reg __fpu_ymmh9;
 struct __darwin_xmm_reg __fpu_ymmh10;
 struct __darwin_xmm_reg __fpu_ymmh11;
 struct __darwin_xmm_reg __fpu_ymmh12;
 struct __darwin_xmm_reg __fpu_ymmh13;
 struct __darwin_xmm_reg __fpu_ymmh14;
 struct __darwin_xmm_reg __fpu_ymmh15;
};
# 751 "/usr/include/mach/i386/_structs.h" 3 4
struct __darwin_x86_exception_state64
{
    __uint16_t __trapno;
    __uint16_t __cpu;
    __uint32_t __err;
    __uint64_t __faultvaddr;
};
# 771 "/usr/include/mach/i386/_structs.h" 3 4
struct __darwin_x86_debug_state64
{
 __uint64_t __dr0;
 __uint64_t __dr1;
 __uint64_t __dr2;
 __uint64_t __dr3;
 __uint64_t __dr4;
 __uint64_t __dr5;
 __uint64_t __dr6;
 __uint64_t __dr7;
};
# 39 "/usr/include/i386/_structs.h" 2 3 4
# 48 "/usr/include/i386/_structs.h" 3 4
struct __darwin_mcontext32
{
 struct __darwin_i386_exception_state __es;
 struct __darwin_i386_thread_state __ss;
 struct __darwin_i386_float_state __fs;
};


struct __darwin_mcontext_avx32
{
 struct __darwin_i386_exception_state __es;
 struct __darwin_i386_thread_state __ss;
 struct __darwin_i386_avx_state __fs;
};
# 86 "/usr/include/i386/_structs.h" 3 4
struct __darwin_mcontext64
{
 struct __darwin_x86_exception_state64 __es;
 struct __darwin_x86_thread_state64 __ss;
 struct __darwin_x86_float_state64 __fs;
};


struct __darwin_mcontext_avx64
{
 struct __darwin_x86_exception_state64 __es;
 struct __darwin_x86_thread_state64 __ss;
 struct __darwin_x86_avx_state64 __fs;
};
# 127 "/usr/include/i386/_structs.h" 3 4
typedef struct __darwin_mcontext64 *mcontext_t;
# 30 "/usr/include/machine/_structs.h" 2 3 4
# 58 "/usr/include/sys/_structs.h" 2 3 4
# 75 "/usr/include/sys/_structs.h" 3 4
struct __darwin_sigaltstack
{
 void *ss_sp;
 __darwin_size_t ss_size;
 int ss_flags;
};
# 128 "/usr/include/sys/_structs.h" 3 4
struct __darwin_ucontext
{
 int uc_onstack;
 __darwin_sigset_t uc_sigmask;
 struct __darwin_sigaltstack uc_stack;
 struct __darwin_ucontext *uc_link;
 __darwin_size_t uc_mcsize;
 struct __darwin_mcontext64 *uc_mcontext;

 struct __darwin_mcontext64 __mcontext_data;

};
# 218 "/usr/include/sys/_structs.h" 3 4
typedef struct __darwin_sigaltstack stack_t;
# 227 "/usr/include/sys/_structs.h" 3 4
typedef struct __darwin_ucontext ucontext_t;
# 149 "/usr/include/sys/signal.h" 2 3 4
# 157 "/usr/include/sys/signal.h" 3 4
typedef __darwin_pthread_attr_t pthread_attr_t;




typedef __darwin_sigset_t sigset_t;
# 172 "/usr/include/sys/signal.h" 3 4
typedef __darwin_uid_t uid_t;


union sigval {

 int sival_int;
 void *sival_ptr;
};





struct sigevent {
 int sigev_notify;
 int sigev_signo;
 union sigval sigev_value;
 void (*sigev_notify_function)(union sigval);
 pthread_attr_t *sigev_notify_attributes;
};


typedef struct __siginfo {
 int si_signo;
 int si_errno;
 int si_code;
 pid_t si_pid;
 uid_t si_uid;
 int si_status;
 void *si_addr;
 union sigval si_value;
 long si_band;
 unsigned long __pad[7];
} siginfo_t;
# 286 "/usr/include/sys/signal.h" 3 4
union __sigaction_u {
 void (*__sa_handler)(int);
 void (*__sa_sigaction)(int, struct __siginfo *,
         void *);
};


struct __sigaction {
 union __sigaction_u __sigaction_u;
 void (*sa_tramp)(void *, int, int, siginfo_t *, void *);
 sigset_t sa_mask;
 int sa_flags;
};




struct sigaction {
 union __sigaction_u __sigaction_u;
 sigset_t sa_mask;
 int sa_flags;
};
# 384 "/usr/include/sys/signal.h" 3 4
struct sigstack {
 char *ss_sp;
 int ss_onstack;
};
# 406 "/usr/include/sys/signal.h" 3 4

void (*signal(int, void (*)(int)))(int);

# 117 "/usr/include/sys/wait.h" 2 3 4
# 1 "/usr/include/sys/resource.h" 1 3 4
# 77 "/usr/include/sys/resource.h" 3 4
# 1 "/usr/include/sys/_structs.h" 1 3 4
# 100 "/usr/include/sys/_structs.h" 3 4
struct timeval
{
 __darwin_time_t tv_sec;
 __darwin_suseconds_t tv_usec;
};
# 78 "/usr/include/sys/resource.h" 2 3 4
# 89 "/usr/include/sys/resource.h" 3 4
typedef __uint64_t rlim_t;
# 151 "/usr/include/sys/resource.h" 3 4
struct rusage {
 struct timeval ru_utime;
 struct timeval ru_stime;

 long ru_opaque[14];
# 179 "/usr/include/sys/resource.h" 3 4
};
# 222 "/usr/include/sys/resource.h" 3 4
struct rlimit {
 rlim_t rlim_cur;
 rlim_t rlim_max;
};
# 245 "/usr/include/sys/resource.h" 3 4

int getpriority(int, id_t);



int getrlimit(int, struct rlimit *) __asm("_" "getrlimit" );
int getrusage(int, struct rusage *);
int setpriority(int, id_t, int);



int setrlimit(int, const struct rlimit *) __asm("_" "setrlimit" );

# 118 "/usr/include/sys/wait.h" 2 3 4
# 254 "/usr/include/sys/wait.h" 3 4

pid_t wait(int *) __asm("_" "wait" );
pid_t waitpid(pid_t, int *, int) __asm("_" "waitpid" );

int waitid(idtype_t, id_t, siginfo_t *, int) __asm("_" "waitid" );






# 66 "/usr/include/stdlib.h" 2 3 4
# 93 "/usr/include/stdlib.h" 3 4
typedef __darwin_wchar_t wchar_t;



typedef struct {
 int quot;
 int rem;
} div_t;

typedef struct {
 long quot;
 long rem;
} ldiv_t;


typedef struct {
 long long quot;
 long long rem;
} lldiv_t;
# 134 "/usr/include/stdlib.h" 3 4
extern int __mb_cur_max;
# 144 "/usr/include/stdlib.h" 3 4

void abort(void) __attribute__((noreturn));
int abs(int) __attribute__((const));
int atexit(void (*)(void));
double atof(const char *);
int atoi(const char *);
long atol(const char *);

long long
  atoll(const char *);

void *bsearch(const void *, const void *, size_t,
     size_t, int (*)(const void *, const void *));
void *calloc(size_t, size_t);
div_t div(int, int) __attribute__((const));
void exit(int) __attribute__((noreturn));
void free(void *);
char *getenv(const char *);
long labs(long) __attribute__((const));
ldiv_t ldiv(long, long) __attribute__((const));

long long
  llabs(long long);
lldiv_t lldiv(long long, long long);

void *malloc(size_t);
int mblen(const char *, size_t);
size_t mbstowcs(wchar_t * , const char * , size_t);
int mbtowc(wchar_t * , const char * , size_t);
int posix_memalign(void **, size_t, size_t) __attribute__((visibility("default")));
void qsort(void *, size_t, size_t,
     int (*)(const void *, const void *));
int rand(void);
void *realloc(void *, size_t);
void srand(unsigned);
double strtod(const char *, char **) __asm("_" "strtod" );
float strtof(const char *, char **) __asm("_" "strtof" );
long strtol(const char *, char **, int);
long double
  strtold(const char *, char **);

long long
  strtoll(const char *, char **, int);

unsigned long
  strtoul(const char *, char **, int);

unsigned long long
  strtoull(const char *, char **, int);

int system(const char *) __asm("_" "system" );
size_t wcstombs(char * , const wchar_t * , size_t);
int wctomb(char *, wchar_t);


void _Exit(int) __attribute__((noreturn));
long a64l(const char *);
double drand48(void);
char *ecvt(double, int, int *, int *);
double erand48(unsigned short[3]);
char *fcvt(double, int, int *, int *);
char *gcvt(double, int, char *);
int getsubopt(char **, char * const *, char **);
int grantpt(int);

char *initstate(unsigned, char *, size_t);



long jrand48(unsigned short[3]);
char *l64a(long);
void lcong48(unsigned short[7]);
long lrand48(void);
char *mktemp(char *);
int mkstemp(char *);
long mrand48(void);
long nrand48(unsigned short[3]);
int posix_openpt(int);
char *ptsname(int);
int putenv(char *) __asm("_" "putenv" );
long random(void);
int rand_r(unsigned *);



char *realpath(const char * , char * ) __asm("_" "realpath" );

unsigned short
 *seed48(unsigned short[3]);
int setenv(const char *, const char *, int) __asm("_" "setenv" );

void setkey(const char *) __asm("_" "setkey" );



char *setstate(const char *);
void srand48(long);

void srandom(unsigned);



int unlockpt(int);

int unsetenv(const char *) __asm("_" "unsetenv" );
# 348 "/usr/include/stdlib.h" 3 4

# 42 "Python.h" 2

# 1 "/usr/include/unistd.h" 1 3 4
# 72 "/usr/include/unistd.h" 3 4
# 1 "/usr/include/sys/unistd.h" 1 3 4
# 73 "/usr/include/unistd.h" 2 3 4




typedef __darwin_gid_t gid_t;




typedef __darwin_intptr_t intptr_t;
# 104 "/usr/include/unistd.h" 3 4
typedef __darwin_ssize_t ssize_t;
# 114 "/usr/include/unistd.h" 3 4
typedef __darwin_useconds_t useconds_t;
# 458 "/usr/include/unistd.h" 3 4

void _exit(int) __attribute__((noreturn));
int access(const char *, int);
unsigned int
  alarm(unsigned int);
int chdir(const char *);
int chown(const char *, uid_t, gid_t);

int close(int) __asm("_" "close" );

int dup(int);
int dup2(int, int);
int execl(const char *, const char *, ...);
int execle(const char *, const char *, ...);
int execlp(const char *, const char *, ...);
int execv(const char *, char * const *);
int execve(const char *, char * const *, char * const *);
int execvp(const char *, char * const *);
pid_t fork(void);
long fpathconf(int, int);
char *getcwd(char *, size_t);
gid_t getegid(void);
uid_t geteuid(void);
gid_t getgid(void);



int getgroups(int, gid_t []);

char *getlogin(void);
pid_t getpgrp(void);
pid_t getpid(void);
pid_t getppid(void);
uid_t getuid(void);
int isatty(int);
int link(const char *, const char *);
off_t lseek(int, off_t, int);
long pathconf(const char *, int);

int pause(void) __asm("_" "pause" );

int pipe(int [2]);

ssize_t read(int, void *, size_t) __asm("_" "read" );

int rmdir(const char *);
int setgid(gid_t);
int setpgid(pid_t, pid_t);
pid_t setsid(void);
int setuid(uid_t);

unsigned int
  sleep(unsigned int) __asm("_" "sleep" );

long sysconf(int);
pid_t tcgetpgrp(int);
int tcsetpgrp(int, pid_t);
char *ttyname(int);


int ttyname_r(int, char *, size_t) __asm("_" "ttyname_r" );




int unlink(const char *);

ssize_t write(int, const void *, size_t) __asm("_" "write" );

# 535 "/usr/include/unistd.h" 3 4

size_t confstr(int, char *, size_t) __asm("_" "confstr" );

int getopt(int, char * const [], const char *) __asm("_" "getopt" );

extern char *optarg;
extern int optind, opterr, optopt;

# 560 "/usr/include/unistd.h" 3 4

# 569 "/usr/include/unistd.h" 3 4
char *crypt(const char *, const char *);






void encrypt(char *, int) __asm("_" "encrypt" );



int fchdir(int);
long gethostid(void);
pid_t getpgid(pid_t);
pid_t getsid(pid_t);
# 594 "/usr/include/unistd.h" 3 4
char *getwd(char *) __attribute__((deprecated));


int lchown(const char *, uid_t, gid_t) __asm("_" "lchown" );

int lockf(int, int, off_t) __asm("_" "lockf" );

int nice(int) __asm("_" "nice" );

ssize_t pread(int, void *, size_t, off_t) __asm("_" "pread" );

ssize_t pwrite(int, const void *, size_t, off_t) __asm("_" "pwrite" );
# 615 "/usr/include/unistd.h" 3 4
pid_t setpgrp(void) __asm("_" "setpgrp" );




int setregid(gid_t, gid_t) __asm("_" "setregid" );

int setreuid(uid_t, uid_t) __asm("_" "setreuid" );

void swab(const void * , void * , ssize_t);
void sync(void);
int truncate(const char *, off_t);
useconds_t ualarm(useconds_t, useconds_t);
int usleep(useconds_t) __asm("_" "usleep" );
pid_t vfork(void);


int fsync(int) __asm("_" "fsync" );

int ftruncate(int, off_t);
int getlogin_r(char *, size_t);

# 647 "/usr/include/unistd.h" 3 4

int fchown(int, uid_t, gid_t);
int gethostname(char *, size_t);
ssize_t readlink(const char * , char * , size_t);
int setegid(gid_t);
int seteuid(uid_t);
int symlink(const char *, const char *);

# 44 "Python.h" 2
# 55 "Python.h"
# 1 "/usr/include/assert.h" 1 3 4
# 75 "/usr/include/assert.h" 3 4

void __assert_rtn(const char *, const char *, int, const char *) __attribute__((noreturn));




# 56 "Python.h" 2

# 1 "pyport.h" 1






# 1 "/usr/include/stdint.h" 1 3 4
# 20 "/usr/include/stdint.h" 3 4
typedef signed char int8_t;




typedef short int16_t;




typedef int int32_t;




typedef long long int64_t;




typedef unsigned char uint8_t;




typedef unsigned short uint16_t;




typedef unsigned int uint32_t;




typedef unsigned long long uint64_t;



typedef int8_t int_least8_t;
typedef int16_t int_least16_t;
typedef int32_t int_least32_t;
typedef int64_t int_least64_t;
typedef uint8_t uint_least8_t;
typedef uint16_t uint_least16_t;
typedef uint32_t uint_least32_t;
typedef uint64_t uint_least64_t;



typedef int8_t int_fast8_t;
typedef int16_t int_fast16_t;
typedef int32_t int_fast32_t;
typedef int64_t int_fast64_t;
typedef uint8_t uint_fast8_t;
typedef uint16_t uint_fast16_t;
typedef uint32_t uint_fast32_t;
typedef uint64_t uint_fast64_t;
# 89 "/usr/include/stdint.h" 3 4
typedef unsigned long uintptr_t;







typedef long int intmax_t;
# 106 "/usr/include/stdint.h" 3 4
typedef long unsigned int uintmax_t;
# 8 "pyport.h" 2
# 73 "pyport.h"
typedef uintptr_t Py_uintptr_t;
typedef intptr_t Py_intptr_t;
# 97 "pyport.h"
typedef ssize_t Py_ssize_t;
# 204 "pyport.h"
# 1 "/usr/include/math.h" 1 3 4
# 41 "/usr/include/math.h" 3 4

# 52 "/usr/include/math.h" 3 4
    typedef float float_t;
    typedef double double_t;
# 110 "/usr/include/math.h" 3 4
extern int __math_errhandling(void);
# 138 "/usr/include/math.h" 3 4
extern int __fpclassifyf(float);
extern int __fpclassifyd(double);
extern int __fpclassifyl(long double);
# 182 "/usr/include/math.h" 3 4
static __inline__ int __inline_isfinitef(float) __attribute__ ((__always_inline__));
static __inline__ int __inline_isfinited(double) __attribute__ ((__always_inline__));
static __inline__ int __inline_isfinitel(long double) __attribute__ ((__always_inline__));
static __inline__ int __inline_isinff(float) __attribute__ ((__always_inline__));
static __inline__ int __inline_isinfd(double) __attribute__ ((__always_inline__));
static __inline__ int __inline_isinfl(long double) __attribute__ ((__always_inline__));
static __inline__ int __inline_isnanf(float) __attribute__ ((__always_inline__));
static __inline__ int __inline_isnand(double) __attribute__ ((__always_inline__));
static __inline__ int __inline_isnanl(long double) __attribute__ ((__always_inline__));
static __inline__ int __inline_isnormalf(float) __attribute__ ((__always_inline__));
static __inline__ int __inline_isnormald(double) __attribute__ ((__always_inline__));
static __inline__ int __inline_isnormall(long double) __attribute__ ((__always_inline__));
static __inline__ int __inline_signbitf(float) __attribute__ ((__always_inline__));
static __inline__ int __inline_signbitd(double) __attribute__ ((__always_inline__));
static __inline__ int __inline_signbitl(long double) __attribute__ ((__always_inline__));

static __inline__ int __inline_isfinitef(float __x) {
    return __x == __x && __builtin_fabsf(__x) != __builtin_inff();
}
static __inline__ int __inline_isfinited(double __x) {
    return __x == __x && __builtin_fabs(__x) != __builtin_inf();
}
static __inline__ int __inline_isfinitel(long double __x) {
    return __x == __x && __builtin_fabsl(__x) != __builtin_infl();
}
static __inline__ int __inline_isinff(float __x) {
    return __builtin_fabsf(__x) == __builtin_inff();
}
static __inline__ int __inline_isinfd(double __x) {
    return __builtin_fabs(__x) == __builtin_inf();
}
static __inline__ int __inline_isinfl(long double __x) {
    return __builtin_fabsl(__x) == __builtin_infl();
}
static __inline__ int __inline_isnanf(float __x) {
    return __x != __x;
}
static __inline__ int __inline_isnand(double __x) {
    return __x != __x;
}
static __inline__ int __inline_isnanl(long double __x) {
    return __x != __x;
}
static __inline__ int __inline_signbitf(float __x) {
    union { float __f; unsigned int __u; } __u;
    __u.__f = __x;
    return (int)(__u.__u >> 31);
}
static __inline__ int __inline_signbitd(double __x) {
    union { double __f; unsigned long long __u; } __u;
    __u.__f = __x;
    return (int)(__u.__u >> 63);
}

static __inline__ int __inline_signbitl(long double __x) {
    union {
        long double __ld;
        struct{ unsigned long long __m; unsigned short __sexp; } __p;
    } __u;
    __u.__ld = __x;
    return (int)(__u.__p.__sexp >> 15);
}







static __inline__ int __inline_isnormalf(float __x) {
    return __inline_isfinitef(__x) && __builtin_fabsf(__x) >= 1.17549435e-38F;
}
static __inline__ int __inline_isnormald(double __x) {
    return __inline_isfinited(__x) && __builtin_fabs(__x) >= 2.2250738585072014e-308;
}
static __inline__ int __inline_isnormall(long double __x) {
    return __inline_isfinitel(__x) && __builtin_fabsl(__x) >= 3.36210314311209350626e-4932L;
}
# 315 "/usr/include/math.h" 3 4
extern float acosf(float);
extern double acos(double);
extern long double acosl(long double);

extern float asinf(float);
extern double asin(double);
extern long double asinl(long double);

extern float atanf(float);
extern double atan(double);
extern long double atanl(long double);

extern float atan2f(float, float);
extern double atan2(double, double);
extern long double atan2l(long double, long double);

extern float cosf(float);
extern double cos(double);
extern long double cosl(long double);

extern float sinf(float);
extern double sin(double);
extern long double sinl(long double);

extern float tanf(float);
extern double tan(double);
extern long double tanl(long double);

extern float acoshf(float);
extern double acosh(double);
extern long double acoshl(long double);

extern float asinhf(float);
extern double asinh(double);
extern long double asinhl(long double);

extern float atanhf(float);
extern double atanh(double);
extern long double atanhl(long double);

extern float coshf(float);
extern double cosh(double);
extern long double coshl(long double);

extern float sinhf(float);
extern double sinh(double);
extern long double sinhl(long double);

extern float tanhf(float);
extern double tanh(double);
extern long double tanhl(long double);

extern float expf(float);
extern double exp(double);
extern long double expl(long double);

extern float exp2f(float);
extern double exp2(double);
extern long double exp2l(long double);

extern float expm1f(float);
extern double expm1(double);
extern long double expm1l(long double);

extern float logf(float);
extern double log(double);
extern long double logl(long double);

extern float log10f(float);
extern double log10(double);
extern long double log10l(long double);

extern float log2f(float);
extern double log2(double);
extern long double log2l(long double);

extern float log1pf(float);
extern double log1p(double);
extern long double log1pl(long double);

extern float logbf(float);
extern double logb(double);
extern long double logbl(long double);

extern float modff(float, float *);
extern double modf(double, double *);
extern long double modfl(long double, long double *);

extern float ldexpf(float, int);
extern double ldexp(double, int);
extern long double ldexpl(long double, int);

extern float frexpf(float, int *);
extern double frexp(double, int *);
extern long double frexpl(long double, int *);

extern int ilogbf(float);
extern int ilogb(double);
extern int ilogbl(long double);

extern float scalbnf(float, int);
extern double scalbn(double, int);
extern long double scalbnl(long double, int);

extern float scalblnf(float, long int);
extern double scalbln(double, long int);
extern long double scalblnl(long double, long int);

extern float fabsf(float);
extern double fabs(double);
extern long double fabsl(long double);

extern float cbrtf(float);
extern double cbrt(double);
extern long double cbrtl(long double);

extern float hypotf(float, float);
extern double hypot(double, double);
extern long double hypotl(long double, long double);

extern float powf(float, float);
extern double pow(double, double);
extern long double powl(long double, long double);

extern float sqrtf(float);
extern double sqrt(double);
extern long double sqrtl(long double);

extern float erff(float);
extern double erf(double);
extern long double erfl(long double);

extern float erfcf(float);
extern double erfc(double);
extern long double erfcl(long double);




extern float lgammaf(float);
extern double lgamma(double);
extern long double lgammal(long double);

extern float tgammaf(float);
extern double tgamma(double);
extern long double tgammal(long double);

extern float ceilf(float);
extern double ceil(double);
extern long double ceill(long double);

extern float floorf(float);
extern double floor(double);
extern long double floorl(long double);

extern float nearbyintf(float);
extern double nearbyint(double);
extern long double nearbyintl(long double);

extern float rintf(float);
extern double rint(double);
extern long double rintl(long double);

extern long int lrintf(float);
extern long int lrint(double);
extern long int lrintl(long double);

extern float roundf(float);
extern double round(double);
extern long double roundl(long double);

extern long int lroundf(float);
extern long int lround(double);
extern long int lroundl(long double);




extern long long int llrintf(float);
extern long long int llrint(double);
extern long long int llrintl(long double);

extern long long int llroundf(float);
extern long long int llround(double);
extern long long int llroundl(long double);


extern float truncf(float);
extern double trunc(double);
extern long double truncl(long double);

extern float fmodf(float, float);
extern double fmod(double, double);
extern long double fmodl(long double, long double);

extern float remainderf(float, float);
extern double remainder(double, double);
extern long double remainderl(long double, long double);

extern float remquof(float, float, int *);
extern double remquo(double, double, int *);
extern long double remquol(long double, long double, int *);

extern float copysignf(float, float);
extern double copysign(double, double);
extern long double copysignl(long double, long double);

extern float nanf(const char *);
extern double nan(const char *);
extern long double nanl(const char *);

extern float nextafterf(float, float);
extern double nextafter(double, double);
extern long double nextafterl(long double, long double);

extern double nexttoward(double, long double);
extern float nexttowardf(float, long double);
extern long double nexttowardl(long double, long double);

extern float fdimf(float, float);
extern double fdim(double, double);
extern long double fdiml(long double, long double);

extern float fmaxf(float, float);
extern double fmax(double, double);
extern long double fmaxl(long double, long double);

extern float fminf(float, float);
extern double fmin(double, double);
extern long double fminl(long double, long double);

extern float fmaf(float, float, float);
extern double fma(double, double, double);
extern long double fmal(long double, long double, long double);
# 558 "/usr/include/math.h" 3 4
extern float __inff(void) __attribute__((visibility("default")));
extern double __inf(void) __attribute__((visibility("default")));
extern long double __infl(void) __attribute__((visibility("default")));
extern float __nan(void) __attribute__((visibility("default")));
# 579 "/usr/include/math.h" 3 4
extern double j0(double) __attribute__((visibility("default")));
extern double j1(double) __attribute__((visibility("default")));
extern double jn(int, double) __attribute__((visibility("default")));
extern double y0(double) __attribute__((visibility("default")));
extern double y1(double) __attribute__((visibility("default")));
extern double yn(int, double) __attribute__((visibility("default")));
extern double scalb(double, double);
extern int signgam;
# 649 "/usr/include/math.h" 3 4

# 205 "pyport.h" 2






# 1 "/usr/include/sys/time.h" 1 3 4
# 78 "/usr/include/sys/time.h" 3 4
# 1 "/usr/include/sys/_structs.h" 1 3 4
# 88 "/usr/include/sys/_structs.h" 3 4
struct timespec
{
 __darwin_time_t tv_sec;
 long tv_nsec;
};
# 183 "/usr/include/sys/_structs.h" 3 4

typedef struct fd_set {
 __int32_t fds_bits[((((1024) % ((sizeof(__int32_t) * 8))) == 0) ? ((1024) / ((sizeof(__int32_t) * 8))) : (((1024) / ((sizeof(__int32_t) * 8))) + 1))];
} fd_set;



static __inline int
__darwin_fd_isset(int _n, const struct fd_set *_p)
{
 return (_p->fds_bits[_n/(sizeof(__int32_t) * 8)] & (1<<(_n % (sizeof(__int32_t) * 8))));
}
# 79 "/usr/include/sys/time.h" 2 3 4



typedef __darwin_time_t time_t;




typedef __darwin_suseconds_t suseconds_t;






struct itimerval {
 struct timeval it_interval;
 struct timeval it_value;
};
# 201 "/usr/include/sys/time.h" 3 4

# 210 "/usr/include/sys/time.h" 3 4
int getitimer(int, struct itimerval *);
int gettimeofday(struct timeval * , void * );

# 1 "/usr/include/sys/_select.h" 1 3 4
# 39 "/usr/include/sys/_select.h" 3 4
int select(int, fd_set * , fd_set * ,
  fd_set * , struct timeval * )




  __asm("_" "select" "$1050")




  ;
# 214 "/usr/include/sys/time.h" 2 3 4

int setitimer(int, const struct itimerval * ,
  struct itimerval * );
int utimes(const char *, const struct timeval *);


# 212 "pyport.h" 2
# 1 "/usr/include/time.h" 1 3 4
# 69 "/usr/include/time.h" 3 4
# 1 "/usr/include/_structs.h" 1 3 4
# 24 "/usr/include/_structs.h" 3 4
# 1 "/usr/include/sys/_structs.h" 1 3 4
# 25 "/usr/include/_structs.h" 2 3 4
# 70 "/usr/include/time.h" 2 3 4







typedef __darwin_clock_t clock_t;
# 90 "/usr/include/time.h" 3 4
struct tm {
 int tm_sec;
 int tm_min;
 int tm_hour;
 int tm_mday;
 int tm_mon;
 int tm_year;
 int tm_wday;
 int tm_yday;
 int tm_isdst;
 long tm_gmtoff;
 char *tm_zone;
};
# 113 "/usr/include/time.h" 3 4
extern char *tzname[];


extern int getdate_err;

extern long timezone __asm("_" "timezone" );

extern int daylight;


char *asctime(const struct tm *);
clock_t clock(void) __asm("_" "clock" );
char *ctime(const time_t *);
double difftime(time_t, time_t);
struct tm *getdate(const char *);
struct tm *gmtime(const time_t *);
struct tm *localtime(const time_t *);
time_t mktime(struct tm *) __asm("_" "mktime" );
size_t strftime(char * , size_t, const char * , const struct tm * ) __asm("_" "strftime" );
char *strptime(const char * , const char * , struct tm * ) __asm("_" "strptime" );
time_t time(time_t *);


void tzset(void);



char *asctime_r(const struct tm * , char * );
char *ctime_r(const time_t *, char *);
struct tm *gmtime_r(const time_t * , struct tm * );
struct tm *localtime_r(const time_t * , struct tm * );
# 157 "/usr/include/time.h" 3 4
int nanosleep(const struct timespec *, struct timespec *) __asm("_" "nanosleep" );


# 213 "pyport.h" 2
# 230 "pyport.h"
# 1 "/usr/include/sys/select.h" 1 3 4
# 78 "/usr/include/sys/select.h" 3 4
# 1 "/usr/include/sys/_structs.h" 1 3 4
# 79 "/usr/include/sys/select.h" 2 3 4
# 134 "/usr/include/sys/select.h" 3 4



int pselect(int, fd_set * , fd_set * ,
  fd_set * , const struct timespec * ,
  const sigset_t * )




  __asm("_" "pselect" "$1050")




  ;





# 231 "pyport.h" 2
# 269 "pyport.h"
# 1 "/usr/include/sys/stat.h" 1 3 4
# 79 "/usr/include/sys/stat.h" 3 4
# 1 "/usr/include/sys/_structs.h" 1 3 4
# 80 "/usr/include/sys/stat.h" 2 3 4







typedef __darwin_blkcnt_t blkcnt_t;




typedef __darwin_blksize_t blksize_t;




typedef __darwin_dev_t dev_t;




typedef __darwin_ino_t ino_t;
# 114 "/usr/include/sys/stat.h" 3 4
typedef __darwin_mode_t mode_t;




typedef __uint16_t nlink_t;
# 225 "/usr/include/sys/stat.h" 3 4
struct stat { dev_t st_dev; mode_t st_mode; nlink_t st_nlink; __darwin_ino64_t st_ino; uid_t st_uid; gid_t st_gid; dev_t st_rdev; time_t st_atime; long st_atimensec; time_t st_mtime; long st_mtimensec; time_t st_ctime; long st_ctimensec; time_t st_birthtime; long st_birthtimensec; off_t st_size; blkcnt_t st_blocks; blksize_t st_blksize; __uint32_t st_flags; __uint32_t st_gen; __int32_t st_lspare; __int64_t st_qspare[2]; };
# 428 "/usr/include/sys/stat.h" 3 4


int chmod(const char *, mode_t) __asm("_" "chmod" );
int fchmod(int, mode_t) __asm("_" "fchmod" );
int fstat(int, struct stat *) __asm("_" "fstat" "$INODE64");
int lstat(const char *, struct stat *) __asm("_" "lstat" "$INODE64");
int mkdir(const char *, mode_t);
int mkfifo(const char *, mode_t);
int stat(const char *, struct stat *) __asm("_" "stat" "$INODE64");
int mknod(const char *, mode_t, dev_t);
mode_t umask(mode_t);
# 470 "/usr/include/sys/stat.h" 3 4

# 270 "pyport.h" 2
# 58 "Python.h" 2
# 76 "Python.h"
# 1 "pymem.h" 1
# 50 "pymem.h"
void * PyMem_Malloc(size_t);
void * PyMem_Realloc(void *, size_t);
void PyMem_Free(void *);
# 77 "Python.h" 2

# 1 "object.h" 1
# 103 "object.h"
typedef struct _object {
 Py_ssize_t ob_refcnt; struct _typeobject *ob_type;
} PyObject;

typedef struct {
 Py_ssize_t ob_refcnt; struct _typeobject *ob_type; Py_ssize_t ob_size;
} PyVarObject;
# 127 "object.h"
typedef PyObject * (*unaryfunc)(PyObject *);
typedef PyObject * (*binaryfunc)(PyObject *, PyObject *);
typedef PyObject * (*ternaryfunc)(PyObject *, PyObject *, PyObject *);
typedef int (*inquiry)(PyObject *);
typedef Py_ssize_t (*lenfunc)(PyObject *);
typedef int (*coercion)(PyObject **, PyObject **);
typedef PyObject *(*intargfunc)(PyObject *, int) __attribute__((__deprecated__));
typedef PyObject *(*intintargfunc)(PyObject *, int, int) __attribute__((__deprecated__));
typedef PyObject *(*ssizeargfunc)(PyObject *, Py_ssize_t);
typedef PyObject *(*ssizessizeargfunc)(PyObject *, Py_ssize_t, Py_ssize_t);
typedef int(*intobjargproc)(PyObject *, int, PyObject *);
typedef int(*intintobjargproc)(PyObject *, int, int, PyObject *);
typedef int(*ssizeobjargproc)(PyObject *, Py_ssize_t, PyObject *);
typedef int(*ssizessizeobjargproc)(PyObject *, Py_ssize_t, Py_ssize_t, PyObject *);
typedef int(*objobjargproc)(PyObject *, PyObject *, PyObject *);


typedef int (*getreadbufferproc)(PyObject *, int, void **);
typedef int (*getwritebufferproc)(PyObject *, int, void **);
typedef int (*getsegcountproc)(PyObject *, int *);
typedef int (*getcharbufferproc)(PyObject *, int, char **);

typedef Py_ssize_t (*readbufferproc)(PyObject *, Py_ssize_t, void **);
typedef Py_ssize_t (*writebufferproc)(PyObject *, Py_ssize_t, void **);
typedef Py_ssize_t (*segcountproc)(PyObject *, Py_ssize_t *);
typedef Py_ssize_t (*charbufferproc)(PyObject *, Py_ssize_t, char **);

typedef int (*objobjproc)(PyObject *, PyObject *);
typedef int (*visitproc)(PyObject *, void *);
typedef int (*traverseproc)(PyObject *, visitproc, void *);

typedef struct {
# 167 "object.h"
 binaryfunc nb_add;
 binaryfunc nb_subtract;
 binaryfunc nb_multiply;
 binaryfunc nb_divide;
 binaryfunc nb_remainder;
 binaryfunc nb_divmod;
 ternaryfunc nb_power;
 unaryfunc nb_negative;
 unaryfunc nb_positive;
 unaryfunc nb_absolute;
 inquiry nb_nonzero;
 unaryfunc nb_invert;
 binaryfunc nb_lshift;
 binaryfunc nb_rshift;
 binaryfunc nb_and;
 binaryfunc nb_xor;
 binaryfunc nb_or;
 coercion nb_coerce;
 unaryfunc nb_int;
 unaryfunc nb_long;
 unaryfunc nb_float;
 unaryfunc nb_oct;
 unaryfunc nb_hex;

 binaryfunc nb_inplace_add;
 binaryfunc nb_inplace_subtract;
 binaryfunc nb_inplace_multiply;
 binaryfunc nb_inplace_divide;
 binaryfunc nb_inplace_remainder;
 ternaryfunc nb_inplace_power;
 binaryfunc nb_inplace_lshift;
 binaryfunc nb_inplace_rshift;
 binaryfunc nb_inplace_and;
 binaryfunc nb_inplace_xor;
 binaryfunc nb_inplace_or;



 binaryfunc nb_floor_divide;
 binaryfunc nb_true_divide;
 binaryfunc nb_inplace_floor_divide;
 binaryfunc nb_inplace_true_divide;


 unaryfunc nb_index;
} PyNumberMethods;

typedef struct {
 lenfunc sq_length;
 binaryfunc sq_concat;
 ssizeargfunc sq_repeat;
 ssizeargfunc sq_item;
 ssizessizeargfunc sq_slice;
 ssizeobjargproc sq_ass_item;
 ssizessizeobjargproc sq_ass_slice;
 objobjproc sq_contains;

 binaryfunc sq_inplace_concat;
 ssizeargfunc sq_inplace_repeat;
} PySequenceMethods;

typedef struct {
 lenfunc mp_length;
 binaryfunc mp_subscript;
 objobjargproc mp_ass_subscript;
} PyMappingMethods;

typedef struct {
 readbufferproc bf_getreadbuffer;
 writebufferproc bf_getwritebuffer;
 segcountproc bf_getsegcount;
 charbufferproc bf_getcharbuffer;
} PyBufferProcs;


typedef void (*freefunc)(void *);
typedef void (*destructor)(PyObject *);
typedef int (*printfunc)(PyObject *, FILE *, int);
typedef PyObject *(*getattrfunc)(PyObject *, char *);
typedef PyObject *(*getattrofunc)(PyObject *, PyObject *);
typedef int (*setattrfunc)(PyObject *, char *, PyObject *);
typedef int (*setattrofunc)(PyObject *, PyObject *, PyObject *);
typedef int (*cmpfunc)(PyObject *, PyObject *);
typedef PyObject *(*reprfunc)(PyObject *);
typedef long (*hashfunc)(PyObject *);
typedef PyObject *(*richcmpfunc) (PyObject *, PyObject *, int);
typedef PyObject *(*getiterfunc) (PyObject *);
typedef PyObject *(*iternextfunc) (PyObject *);
typedef PyObject *(*descrgetfunc) (PyObject *, PyObject *, PyObject *);
typedef int (*descrsetfunc) (PyObject *, PyObject *, PyObject *);
typedef int (*initproc)(PyObject *, PyObject *, PyObject *);
typedef PyObject *(*newfunc)(struct _typeobject *, PyObject *, PyObject *);
typedef PyObject *(*allocfunc)(struct _typeobject *, Py_ssize_t);

typedef struct _typeobject {
 Py_ssize_t ob_refcnt; struct _typeobject *ob_type; Py_ssize_t ob_size;
 const char *tp_name;
 Py_ssize_t tp_basicsize, tp_itemsize;



 destructor tp_dealloc;
 printfunc tp_print;
 getattrfunc tp_getattr;
 setattrfunc tp_setattr;
 cmpfunc tp_compare;
 reprfunc tp_repr;



 PyNumberMethods *tp_as_number;
 PySequenceMethods *tp_as_sequence;
 PyMappingMethods *tp_as_mapping;



 hashfunc tp_hash;
 ternaryfunc tp_call;
 reprfunc tp_str;
 getattrofunc tp_getattro;
 setattrofunc tp_setattro;


 PyBufferProcs *tp_as_buffer;


 long tp_flags;

 const char *tp_doc;



 traverseproc tp_traverse;


 inquiry tp_clear;



 richcmpfunc tp_richcompare;


 Py_ssize_t tp_weaklistoffset;



 getiterfunc tp_iter;
 iternextfunc tp_iternext;


 struct PyMethodDef *tp_methods;
 struct PyMemberDef *tp_members;
 struct PyGetSetDef *tp_getset;
 struct _typeobject *tp_base;
 PyObject *tp_dict;
 descrgetfunc tp_descr_get;
 descrsetfunc tp_descr_set;
 Py_ssize_t tp_dictoffset;
 initproc tp_init;
 allocfunc tp_alloc;
 newfunc tp_new;
 freefunc tp_free;
 inquiry tp_is_gc;
 PyObject *tp_bases;
 PyObject *tp_mro;
 PyObject *tp_cache;
 PyObject *tp_subclasses;
 PyObject *tp_weaklist;
 destructor tp_del;
# 345 "object.h"
} PyTypeObject;



typedef struct _heaptypeobject {


 PyTypeObject ht_type;
 PyNumberMethods as_number;
 PyMappingMethods as_mapping;
 PySequenceMethods as_sequence;




 PyBufferProcs as_buffer;
 PyObject *ht_name, *ht_slots;

} PyHeapTypeObject;







int PyType_IsSubtype(PyTypeObject *, PyTypeObject *);



extern PyTypeObject PyType_Type;
extern PyTypeObject PyBaseObject_Type;
extern PyTypeObject PySuper_Type;




int PyType_Ready(PyTypeObject *);
PyObject * PyType_GenericAlloc(PyTypeObject *, Py_ssize_t);
PyObject * PyType_GenericNew(PyTypeObject *,
            PyObject *, PyObject *);
PyObject * _PyType_Lookup(PyTypeObject *, PyObject *);


int PyObject_Print(PyObject *, FILE *, int);
void _PyObject_Dump(PyObject *);
PyObject * PyObject_Repr(PyObject *);
PyObject * _PyObject_Str(PyObject *);
PyObject * PyObject_Str(PyObject *);

PyObject * PyObject_Unicode(PyObject *);

int PyObject_Compare(PyObject *, PyObject *);
PyObject * PyObject_RichCompare(PyObject *, PyObject *, int);
int PyObject_RichCompareBool(PyObject *, PyObject *, int);
PyObject * PyObject_GetAttrString(PyObject *, const char *);
int PyObject_SetAttrString(PyObject *, const char *, PyObject *);
int PyObject_HasAttrString(PyObject *, const char *);
PyObject * PyObject_GetAttr(PyObject *, PyObject *);
int PyObject_SetAttr(PyObject *, PyObject *, PyObject *);
int PyObject_HasAttr(PyObject *, PyObject *);
PyObject ** _PyObject_GetDictPtr(PyObject *);
PyObject * PyObject_SelfIter(PyObject *);
PyObject * PyObject_GenericGetAttr(PyObject *, PyObject *);
int PyObject_GenericSetAttr(PyObject *,
           PyObject *, PyObject *);
long PyObject_Hash(PyObject *);
int PyObject_IsTrue(PyObject *);
int PyObject_Not(PyObject *);
int PyCallable_Check(PyObject *);
int PyNumber_Coerce(PyObject **, PyObject **);
int PyNumber_CoerceEx(PyObject **, PyObject **);

void PyObject_ClearWeakRefs(PyObject *);


extern int _PyObject_SlotCompare(PyObject *, PyObject *);







PyObject * PyObject_Dir(PyObject *);



int Py_ReprEnter(PyObject *);
void Py_ReprLeave(PyObject *);


long _Py_HashDouble(double);
long _Py_HashPointer(void*);
# 701 "object.h"
void Py_IncRef(PyObject *);
void Py_DecRef(PyObject *);







extern PyObject _Py_NoneStruct;
# 720 "object.h"
extern PyObject _Py_NotImplementedStruct;
# 734 "object.h"
extern int _Py_SwappedOp[];
# 846 "object.h"
void _PyTrash_deposit_object(PyObject*);
void _PyTrash_destroy_chain(void);
extern int _PyTrash_delete_nesting;
extern PyObject * _PyTrash_delete_later;
# 79 "Python.h" 2
# 1 "objimpl.h" 1
# 97 "objimpl.h"
void * PyObject_Malloc(size_t);
void * PyObject_Realloc(void *, size_t);
void PyObject_Free(void *);
# 143 "objimpl.h"
PyObject * PyObject_Init(PyObject *, PyTypeObject *);
PyVarObject * PyObject_InitVar(PyVarObject *,
                                                 PyTypeObject *, Py_ssize_t);
PyObject * _PyObject_New(PyTypeObject *);
PyVarObject * _PyObject_NewVar(PyTypeObject *, Py_ssize_t);
# 228 "objimpl.h"
Py_ssize_t PyGC_Collect(void);
# 237 "objimpl.h"
PyVarObject * _PyObject_GC_Resize(PyVarObject *, Py_ssize_t);







typedef union _gc_head {
 struct {
  union _gc_head *gc_next;
  union _gc_head *gc_prev;
  Py_ssize_t gc_refs;
 } gc;
 long double dummy;
} PyGC_Head;

extern PyGC_Head *_PyGC_generation0;
# 288 "objimpl.h"
PyObject * _PyObject_GC_Malloc(size_t);
PyObject * _PyObject_GC_New(PyTypeObject *);
PyVarObject * _PyObject_GC_NewVar(PyTypeObject *, Py_ssize_t);
void PyObject_GC_Track(void *);
void PyObject_GC_UnTrack(void *);
void PyObject_GC_Del(void *);
# 80 "Python.h" 2

# 1 "pydebug.h" 1







extern int Py_DebugFlag;
extern int Py_VerboseFlag;
extern int Py_InteractiveFlag;
extern int Py_OptimizeFlag;
extern int Py_NoSiteFlag;
extern int Py_UseClassExceptionsFlag;
extern int Py_FrozenFlag;
extern int Py_TabcheckFlag;
extern int Py_UnicodeFlag;
extern int Py_IgnoreEnvironmentFlag;
extern int Py_DivisionWarningFlag;



extern int _Py_QnewFlag;






void Py_FatalError(const char *message);
# 82 "Python.h" 2

# 1 "unicodeobject.h" 1
# 55 "unicodeobject.h"
# 1 "/usr/include/ctype.h" 1 3 4
# 69 "/usr/include/ctype.h" 3 4
# 1 "/usr/include/runetype.h" 1 3 4
# 81 "/usr/include/runetype.h" 3 4
typedef struct {
 __darwin_rune_t __min;
 __darwin_rune_t __max;
 __darwin_rune_t __map;
 __uint32_t *__types;
} _RuneEntry;

typedef struct {
 int __nranges;
 _RuneEntry *__ranges;
} _RuneRange;

typedef struct {
 char __name[14];
 __uint32_t __mask;
} _RuneCharClass;

typedef struct {
 char __magic[8];
 char __encoding[32];

 __darwin_rune_t (*__sgetrune)(const char *, __darwin_size_t, char const **);
 int (*__sputrune)(__darwin_rune_t, char *, __darwin_size_t, char **);
 __darwin_rune_t __invalid_rune;

 __uint32_t __runetype[(1 <<8 )];
 __darwin_rune_t __maplower[(1 <<8 )];
 __darwin_rune_t __mapupper[(1 <<8 )];






 _RuneRange __runetype_ext;
 _RuneRange __maplower_ext;
 _RuneRange __mapupper_ext;

 void *__variable;
 int __variable_len;




 int __ncharclasses;
 _RuneCharClass *__charclasses;
} _RuneLocale;




extern _RuneLocale _DefaultRuneLocale;
extern _RuneLocale *_CurrentRuneLocale;

# 70 "/usr/include/ctype.h" 2 3 4
# 145 "/usr/include/ctype.h" 3 4

unsigned long ___runetype(__darwin_ct_rune_t);
__darwin_ct_rune_t ___tolower(__darwin_ct_rune_t);
__darwin_ct_rune_t ___toupper(__darwin_ct_rune_t);


static __inline int
isascii(int _c)
{
 return ((_c & ~0x7F) == 0);
}
# 164 "/usr/include/ctype.h" 3 4

int __maskrune(__darwin_ct_rune_t, unsigned long);



static __inline int
__istype(__darwin_ct_rune_t _c, unsigned long _f)
{



 return (isascii(_c) ? !!(_DefaultRuneLocale.__runetype[_c] & _f)
  : !!__maskrune(_c, _f));

}

static __inline __darwin_ct_rune_t
__isctype(__darwin_ct_rune_t _c, unsigned long _f)
{



 return (_c < 0 || _c >= (1 <<8 )) ? 0 :
  !!(_DefaultRuneLocale.__runetype[_c] & _f);

}
# 204 "/usr/include/ctype.h" 3 4

__darwin_ct_rune_t __toupper(__darwin_ct_rune_t);
__darwin_ct_rune_t __tolower(__darwin_ct_rune_t);



static __inline int
__wcwidth(__darwin_ct_rune_t _c)
{
 unsigned int _x;

 if (_c == 0)
  return (0);
 _x = (unsigned int)__maskrune(_c, 0xe0000000L|0x00040000L);
 if ((_x & 0xe0000000L) != 0)
  return ((_x & 0xe0000000L) >> 30);
 return ((_x & 0x00040000L) != 0 ? 1 : -1);
}






static __inline int
isalnum(int _c)
{
 return (__istype(_c, 0x00000100L|0x00000400L));
}

static __inline int
isalpha(int _c)
{
 return (__istype(_c, 0x00000100L));
}

static __inline int
isblank(int _c)
{
 return (__istype(_c, 0x00020000L));
}

static __inline int
iscntrl(int _c)
{
 return (__istype(_c, 0x00000200L));
}


static __inline int
isdigit(int _c)
{
 return (__isctype(_c, 0x00000400L));
}

static __inline int
isgraph(int _c)
{
 return (__istype(_c, 0x00000800L));
}

static __inline int
islower(int _c)
{
 return (__istype(_c, 0x00001000L));
}

static __inline int
isprint(int _c)
{
 return (__istype(_c, 0x00040000L));
}

static __inline int
ispunct(int _c)
{
 return (__istype(_c, 0x00002000L));
}

static __inline int
isspace(int _c)
{
 return (__istype(_c, 0x00004000L));
}

static __inline int
isupper(int _c)
{
 return (__istype(_c, 0x00008000L));
}


static __inline int
isxdigit(int _c)
{
 return (__isctype(_c, 0x00010000L));
}

static __inline int
toascii(int _c)
{
 return (_c & 0x7F);
}

static __inline int
tolower(int _c)
{
        return (__tolower(_c));
}

static __inline int
toupper(int _c)
{
        return (__toupper(_c));
}
# 56 "unicodeobject.h" 2
# 118 "unicodeobject.h"
# 1 "/usr/include/wchar.h" 1 3 4
# 85 "/usr/include/wchar.h" 3 4
typedef __darwin_mbstate_t mbstate_t;




typedef __darwin_ct_rune_t ct_rune_t;




typedef __darwin_rune_t rune_t;
# 113 "/usr/include/wchar.h" 3 4
# 1 "/usr/llvm-gcc-4.2/bin/../lib/gcc/i686-apple-darwin11/4.2.1/include/stdarg.h" 1 3 4
# 43 "/usr/llvm-gcc-4.2/bin/../lib/gcc/i686-apple-darwin11/4.2.1/include/stdarg.h" 3 4
typedef __builtin_va_list __gnuc_va_list;
# 114 "/usr/include/wchar.h" 2 3 4


# 1 "/usr/include/_wctype.h" 1 3 4
# 47 "/usr/include/_wctype.h" 3 4
typedef __darwin_wint_t wint_t;




typedef __darwin_wctype_t wctype_t;
# 71 "/usr/include/_wctype.h" 3 4
static __inline int
iswalnum(wint_t _wc)
{
 return (__istype(_wc, 0x00000100L|0x00000400L));
}

static __inline int
iswalpha(wint_t _wc)
{
 return (__istype(_wc, 0x00000100L));
}

static __inline int
iswcntrl(wint_t _wc)
{
 return (__istype(_wc, 0x00000200L));
}

static __inline int
iswctype(wint_t _wc, wctype_t _charclass)
{
 return (__istype(_wc, _charclass));
}

static __inline int
iswdigit(wint_t _wc)
{
 return (__isctype(_wc, 0x00000400L));
}

static __inline int
iswgraph(wint_t _wc)
{
 return (__istype(_wc, 0x00000800L));
}

static __inline int
iswlower(wint_t _wc)
{
 return (__istype(_wc, 0x00001000L));
}

static __inline int
iswprint(wint_t _wc)
{
 return (__istype(_wc, 0x00040000L));
}

static __inline int
iswpunct(wint_t _wc)
{
 return (__istype(_wc, 0x00002000L));
}

static __inline int
iswspace(wint_t _wc)
{
 return (__istype(_wc, 0x00004000L));
}

static __inline int
iswupper(wint_t _wc)
{
 return (__istype(_wc, 0x00008000L));
}

static __inline int
iswxdigit(wint_t _wc)
{
 return (__isctype(_wc, 0x00010000L));
}

static __inline wint_t
towlower(wint_t _wc)
{
        return (__tolower(_wc));
}

static __inline wint_t
towupper(wint_t _wc)
{
        return (__toupper(_wc));
}
# 176 "/usr/include/_wctype.h" 3 4

wctype_t
 wctype(const char *);

# 117 "/usr/include/wchar.h" 2 3 4




wint_t btowc(int);
wint_t fgetwc(FILE *);
wchar_t *fgetws(wchar_t * , int, FILE * );
wint_t fputwc(wchar_t, FILE *);
int fputws(const wchar_t * , FILE * );
int fwide(FILE *, int);
int fwprintf(FILE * , const wchar_t * , ...);
int fwscanf(FILE * , const wchar_t * , ...);
wint_t getwc(FILE *);
wint_t getwchar(void);
size_t mbrlen(const char * , size_t, mbstate_t * );
size_t mbrtowc(wchar_t * , const char * , size_t,
     mbstate_t * );
int mbsinit(const mbstate_t *);
size_t mbsrtowcs(wchar_t * , const char ** , size_t,
     mbstate_t * );
wint_t putwc(wchar_t, FILE *);
wint_t putwchar(wchar_t);
int swprintf(wchar_t * , size_t, const wchar_t * , ...);
int swscanf(const wchar_t * , const wchar_t * , ...);
wint_t ungetwc(wint_t, FILE *);
int vfwprintf(FILE * , const wchar_t * ,
     __darwin_va_list);
int vswprintf(wchar_t * , size_t, const wchar_t * ,
     __darwin_va_list);
int vwprintf(const wchar_t * , __darwin_va_list);
size_t wcrtomb(char * , wchar_t, mbstate_t * );
wchar_t *wcscat(wchar_t * , const wchar_t * );
wchar_t *wcschr(const wchar_t *, wchar_t);
int wcscmp(const wchar_t *, const wchar_t *);
int wcscoll(const wchar_t *, const wchar_t *);
wchar_t *wcscpy(wchar_t * , const wchar_t * );
size_t wcscspn(const wchar_t *, const wchar_t *);
size_t wcsftime(wchar_t * , size_t, const wchar_t * ,
     const struct tm * ) __asm("_" "wcsftime" );
size_t wcslen(const wchar_t *);
wchar_t *wcsncat(wchar_t * , const wchar_t * , size_t);
int wcsncmp(const wchar_t *, const wchar_t *, size_t);
wchar_t *wcsncpy(wchar_t * , const wchar_t * , size_t);
wchar_t *wcspbrk(const wchar_t *, const wchar_t *);
wchar_t *wcsrchr(const wchar_t *, wchar_t);
size_t wcsrtombs(char * , const wchar_t ** , size_t,
     mbstate_t * );
size_t wcsspn(const wchar_t *, const wchar_t *);
wchar_t *wcsstr(const wchar_t * , const wchar_t * );
size_t wcsxfrm(wchar_t * , const wchar_t * , size_t);
int wctob(wint_t);
double wcstod(const wchar_t * , wchar_t ** );
wchar_t *wcstok(wchar_t * , const wchar_t * ,
     wchar_t ** );
long wcstol(const wchar_t * , wchar_t ** , int);
unsigned long
  wcstoul(const wchar_t * , wchar_t ** , int);
wchar_t *wmemchr(const wchar_t *, wchar_t, size_t);
int wmemcmp(const wchar_t *, const wchar_t *, size_t);
wchar_t *wmemcpy(wchar_t * , const wchar_t * , size_t);
wchar_t *wmemmove(wchar_t *, const wchar_t *, size_t);
wchar_t *wmemset(wchar_t *, wchar_t, size_t);
int wprintf(const wchar_t * , ...);
int wscanf(const wchar_t * , ...);
int wcswidth(const wchar_t *, size_t);
int wcwidth(wchar_t);

# 193 "/usr/include/wchar.h" 3 4

int vfwscanf(FILE * , const wchar_t * ,
     __darwin_va_list);
int vswscanf(const wchar_t * , const wchar_t * ,
     __darwin_va_list);
int vwscanf(const wchar_t * , __darwin_va_list);
float wcstof(const wchar_t * , wchar_t ** );
long double
 wcstold(const wchar_t * , wchar_t ** );

long long
 wcstoll(const wchar_t * , wchar_t ** , int);
unsigned long long
 wcstoull(const wchar_t * , wchar_t ** , int);


# 119 "unicodeobject.h" 2







typedef unsigned int Py_UCS4;




typedef unsigned short Py_UNICODE;
# 383 "unicodeobject.h"
typedef struct {
    Py_ssize_t ob_refcnt; struct _typeobject *ob_type;
    Py_ssize_t length;
    Py_UNICODE *str;
    long hash;
    PyObject *defenc;


} PyUnicodeObject;

extern PyTypeObject PyUnicode_Type;
# 431 "unicodeobject.h"
PyObject* PyUnicodeUCS2_FromUnicode(
    const Py_UNICODE *u,
    Py_ssize_t size
    );




Py_UNICODE * PyUnicodeUCS2_AsUnicode(
    PyObject *unicode
    );



Py_ssize_t PyUnicodeUCS2_GetSize(
    PyObject *unicode
    );


Py_UNICODE PyUnicodeUCS2_GetMax(void);
# 466 "unicodeobject.h"
int PyUnicodeUCS2_Resize(
    PyObject **unicode,
    Py_ssize_t length
    );
# 488 "unicodeobject.h"
PyObject* PyUnicodeUCS2_FromEncodedObject(
    register PyObject *obj,
    const char *encoding,
    const char *errors
    );
# 507 "unicodeobject.h"
PyObject* PyUnicodeUCS2_FromObject(
    register PyObject *obj
    );
# 520 "unicodeobject.h"
PyObject* PyUnicodeUCS2_FromWideChar(
    register const wchar_t *w,
    Py_ssize_t size
    );
# 537 "unicodeobject.h"
Py_ssize_t PyUnicodeUCS2_AsWideChar(
    PyUnicodeObject *unicode,
    register wchar_t *w,
    Py_ssize_t size
    );
# 555 "unicodeobject.h"
PyObject* PyUnicodeUCS2_FromOrdinal(int ordinal);
# 591 "unicodeobject.h"
PyObject * _PyUnicodeUCS2_AsDefaultEncodedString(
    PyObject *, const char *);
# 603 "unicodeobject.h"
const char* PyUnicodeUCS2_GetDefaultEncoding(void);







int PyUnicodeUCS2_SetDefaultEncoding(
    const char *encoding
    );






PyObject* PyUnicodeUCS2_Decode(
    const char *s,
    Py_ssize_t size,
    const char *encoding,
    const char *errors
    );




PyObject* PyUnicodeUCS2_Encode(
    const Py_UNICODE *s,
    Py_ssize_t size,
    const char *encoding,
    const char *errors
    );




PyObject* PyUnicodeUCS2_AsEncodedObject(
    PyObject *unicode,
    const char *encoding,
    const char *errors
    );




PyObject* PyUnicodeUCS2_AsEncodedString(
    PyObject *unicode,
    const char *encoding,
    const char *errors
    );

PyObject* PyUnicode_BuildEncodingMap(
    PyObject* string
   );




PyObject* PyUnicode_DecodeUTF7(
    const char *string,
    Py_ssize_t length,
    const char *errors
    );

PyObject* PyUnicode_EncodeUTF7(
    const Py_UNICODE *data,
    Py_ssize_t length,
    int encodeSetO,

    int encodeWhiteSpace,

    const char *errors
    );



PyObject* PyUnicodeUCS2_DecodeUTF8(
    const char *string,
    Py_ssize_t length,
    const char *errors
    );

PyObject* PyUnicodeUCS2_DecodeUTF8Stateful(
    const char *string,
    Py_ssize_t length,
    const char *errors,
    Py_ssize_t *consumed
    );

PyObject* PyUnicodeUCS2_AsUTF8String(
    PyObject *unicode
    );

PyObject* PyUnicodeUCS2_EncodeUTF8(
    const Py_UNICODE *data,
    Py_ssize_t length,
    const char *errors
    );
# 728 "unicodeobject.h"
PyObject* PyUnicodeUCS2_DecodeUTF16(
    const char *string,
    Py_ssize_t length,
    const char *errors,
    int *byteorder


    );

PyObject* PyUnicodeUCS2_DecodeUTF16Stateful(
    const char *string,
    Py_ssize_t length,
    const char *errors,
    int *byteorder,


    Py_ssize_t *consumed
    );




PyObject* PyUnicodeUCS2_AsUTF16String(
    PyObject *unicode
    );
# 774 "unicodeobject.h"
PyObject* PyUnicodeUCS2_EncodeUTF16(
    const Py_UNICODE *data,
    Py_ssize_t length,
    const char *errors,
    int byteorder
    );



PyObject* PyUnicodeUCS2_DecodeUnicodeEscape(
    const char *string,
    Py_ssize_t length,
    const char *errors
    );

PyObject* PyUnicodeUCS2_AsUnicodeEscapeString(
    PyObject *unicode
    );

PyObject* PyUnicodeUCS2_EncodeUnicodeEscape(
    const Py_UNICODE *data,
    Py_ssize_t length
    );



PyObject* PyUnicodeUCS2_DecodeRawUnicodeEscape(
    const char *string,
    Py_ssize_t length,
    const char *errors
    );

PyObject* PyUnicodeUCS2_AsRawUnicodeEscapeString(
    PyObject *unicode
    );

PyObject* PyUnicodeUCS2_EncodeRawUnicodeEscape(
    const Py_UNICODE *data,
    Py_ssize_t length
    );





PyObject *_PyUnicode_DecodeUnicodeInternal(
    const char *string,
    Py_ssize_t length,
    const char *errors
    );







PyObject* PyUnicodeUCS2_DecodeLatin1(
    const char *string,
    Py_ssize_t length,
    const char *errors
    );

PyObject* PyUnicodeUCS2_AsLatin1String(
    PyObject *unicode
    );

PyObject* PyUnicodeUCS2_EncodeLatin1(
    const Py_UNICODE *data,
    Py_ssize_t length,
    const char *errors
    );







PyObject* PyUnicodeUCS2_DecodeASCII(
    const char *string,
    Py_ssize_t length,
    const char *errors
    );

PyObject* PyUnicodeUCS2_AsASCIIString(
    PyObject *unicode
    );

PyObject* PyUnicodeUCS2_EncodeASCII(
    const Py_UNICODE *data,
    Py_ssize_t length,
    const char *errors
    );
# 891 "unicodeobject.h"
PyObject* PyUnicodeUCS2_DecodeCharmap(
    const char *string,
    Py_ssize_t length,
    PyObject *mapping,

    const char *errors
    );

PyObject* PyUnicodeUCS2_AsCharmapString(
    PyObject *unicode,
    PyObject *mapping

    );

PyObject* PyUnicodeUCS2_EncodeCharmap(
    const Py_UNICODE *data,
    Py_ssize_t length,
    PyObject *mapping,

    const char *errors
    );
# 926 "unicodeobject.h"
PyObject * PyUnicodeUCS2_TranslateCharmap(
    const Py_UNICODE *data,
    Py_ssize_t length,
    PyObject *table,
    const char *errors
    );
# 986 "unicodeobject.h"
int PyUnicodeUCS2_EncodeDecimal(
    Py_UNICODE *s,
    Py_ssize_t length,
    char *output,
    const char *errors
    );
# 1001 "unicodeobject.h"
PyObject* PyUnicodeUCS2_Concat(
    PyObject *left,
    PyObject *right
    );
# 1017 "unicodeobject.h"
PyObject* PyUnicodeUCS2_Split(
    PyObject *s,
    PyObject *sep,
    Py_ssize_t maxsplit
    );






PyObject* PyUnicodeUCS2_Splitlines(
    PyObject *s,
    int keepends
    );



PyObject* PyUnicodeUCS2_Partition(
    PyObject *s,
    PyObject *sep
    );




PyObject* PyUnicodeUCS2_RPartition(
    PyObject *s,
    PyObject *sep
    );
# 1061 "unicodeobject.h"
PyObject* PyUnicodeUCS2_RSplit(
    PyObject *s,
    PyObject *sep,
    Py_ssize_t maxsplit
    );
# 1079 "unicodeobject.h"
PyObject * PyUnicodeUCS2_Translate(
    PyObject *str,
    PyObject *table,
    const char *errors
    );




PyObject* PyUnicodeUCS2_Join(
    PyObject *separator,
    PyObject *seq
    );




Py_ssize_t PyUnicodeUCS2_Tailmatch(
    PyObject *str,
    PyObject *substr,
    Py_ssize_t start,
    Py_ssize_t end,
    int direction
    );





Py_ssize_t PyUnicodeUCS2_Find(
    PyObject *str,
    PyObject *substr,
    Py_ssize_t start,
    Py_ssize_t end,
    int direction
    );



Py_ssize_t PyUnicodeUCS2_Count(
    PyObject *str,
    PyObject *substr,
    Py_ssize_t start,
    Py_ssize_t end
    );




PyObject * PyUnicodeUCS2_Replace(
    PyObject *str,
    PyObject *substr,
    PyObject *replstr,
    Py_ssize_t maxcount

    );




int PyUnicodeUCS2_Compare(
    PyObject *left,
    PyObject *right
    );
# 1160 "unicodeobject.h"
PyObject * PyUnicodeUCS2_RichCompare(
    PyObject *left,
    PyObject *right,
    int op
    );




PyObject * PyUnicodeUCS2_Format(
    PyObject *format,
    PyObject *args
    );







int PyUnicodeUCS2_Contains(
    PyObject *container,
    PyObject *element
    );


PyObject * _PyUnicode_XStrip(
    PyUnicodeObject *self,
    int striptype,
    PyObject *sepobj
    );
# 1201 "unicodeobject.h"
int _PyUnicodeUCS2_IsLowercase(
    Py_UNICODE ch
    );

int _PyUnicodeUCS2_IsUppercase(
    Py_UNICODE ch
    );

int _PyUnicodeUCS2_IsTitlecase(
    Py_UNICODE ch
    );

int _PyUnicodeUCS2_IsWhitespace(
    const Py_UNICODE ch
    );

int _PyUnicodeUCS2_IsLinebreak(
    const Py_UNICODE ch
    );

Py_UNICODE _PyUnicodeUCS2_ToLowercase(
    Py_UNICODE ch
    );

Py_UNICODE _PyUnicodeUCS2_ToUppercase(
    Py_UNICODE ch
    );

Py_UNICODE _PyUnicodeUCS2_ToTitlecase(
    Py_UNICODE ch
    );

int _PyUnicodeUCS2_ToDecimalDigit(
    Py_UNICODE ch
    );

int _PyUnicodeUCS2_ToDigit(
    Py_UNICODE ch
    );

double _PyUnicodeUCS2_ToNumeric(
    Py_UNICODE ch
    );

int _PyUnicodeUCS2_IsDecimalDigit(
    Py_UNICODE ch
    );

int _PyUnicodeUCS2_IsDigit(
    Py_UNICODE ch
    );

int _PyUnicodeUCS2_IsNumeric(
    Py_UNICODE ch
    );

int _PyUnicodeUCS2_IsAlpha(
    Py_UNICODE ch
    );
# 84 "Python.h" 2
# 1 "intobject.h" 1
# 23 "intobject.h"
typedef struct {
    Py_ssize_t ob_refcnt; struct _typeobject *ob_type;
    long ob_ival;
} PyIntObject;

extern PyTypeObject PyInt_Type;




PyObject * PyInt_FromString(char*, char**, int);

PyObject * PyInt_FromUnicode(Py_UNICODE*, Py_ssize_t, int);

PyObject * PyInt_FromLong(long);
PyObject * PyInt_FromSize_t(size_t);
PyObject * PyInt_FromSsize_t(Py_ssize_t);
long PyInt_AsLong(PyObject *);
Py_ssize_t PyInt_AsSsize_t(PyObject *);
unsigned long PyInt_AsUnsignedLongMask(PyObject *);

unsigned long long PyInt_AsUnsignedLongLongMask(PyObject *);


long PyInt_GetMax(void);
# 58 "intobject.h"
unsigned long PyOS_strtoul(char *, char **, int);
long PyOS_strtol(char *, char **, int);
# 85 "Python.h" 2
# 1 "boolobject.h" 1
# 10 "boolobject.h"
typedef PyIntObject PyBoolObject;

extern PyTypeObject PyBool_Type;







extern PyIntObject _Py_ZeroStruct, _Py_TrueStruct;
# 31 "boolobject.h"
PyObject * PyBool_FromLong(long);
# 86 "Python.h" 2
# 1 "longobject.h" 1
# 10 "longobject.h"
typedef struct _longobject PyLongObject;

extern PyTypeObject PyLong_Type;




PyObject * PyLong_FromLong(long);
PyObject * PyLong_FromUnsignedLong(unsigned long);
PyObject * PyLong_FromDouble(double);
long PyLong_AsLong(PyObject *);
unsigned long PyLong_AsUnsignedLong(PyObject *);
unsigned long PyLong_AsUnsignedLongMask(PyObject *);


Py_ssize_t _PyLong_AsSsize_t(PyObject *);
PyObject * _PyLong_FromSize_t(size_t);
PyObject * _PyLong_FromSsize_t(Py_ssize_t);
extern int _PyLong_DigitValue[256];







double _PyLong_AsScaledDouble(PyObject *vv, int *e);

double PyLong_AsDouble(PyObject *);
PyObject * PyLong_FromVoidPtr(void *);
void * PyLong_AsVoidPtr(PyObject *);


PyObject * PyLong_FromLongLong(long long);
PyObject * PyLong_FromUnsignedLongLong(unsigned long long);
long long PyLong_AsLongLong(PyObject *);
unsigned long long PyLong_AsUnsignedLongLong(PyObject *);
unsigned long long PyLong_AsUnsignedLongLongMask(PyObject *);


PyObject * PyLong_FromString(char *, char **, int);

PyObject * PyLong_FromUnicode(Py_UNICODE*, Py_ssize_t, int);






int _PyLong_Sign(PyObject *v);
# 69 "longobject.h"
size_t _PyLong_NumBits(PyObject *v);
# 84 "longobject.h"
PyObject * _PyLong_FromByteArray(
 const unsigned char* bytes, size_t n,
 int little_endian, int is_signed);
# 107 "longobject.h"
int _PyLong_AsByteArray(PyLongObject* v,
 unsigned char* bytes, size_t n,
 int little_endian, int is_signed);
# 87 "Python.h" 2
# 1 "floatobject.h" 1
# 14 "floatobject.h"
typedef struct {
    Py_ssize_t ob_refcnt; struct _typeobject *ob_type;
    double ob_fval;
} PyFloatObject;

extern PyTypeObject PyFloat_Type;







PyObject * PyFloat_FromString(PyObject*, char** junk);


PyObject * PyFloat_FromDouble(double);



double PyFloat_AsDouble(PyObject *);






void PyFloat_AsReprString(char*, PyFloatObject *v);






void PyFloat_AsString(char*, PyFloatObject *v);
# 82 "floatobject.h"
int _PyFloat_Pack4(double x, unsigned char *p, int le);
int _PyFloat_Pack8(double x, unsigned char *p, int le);
# 93 "floatobject.h"
double _PyFloat_Unpack4(const unsigned char *p, int le);
double _PyFloat_Unpack8(const unsigned char *p, int le);
# 88 "Python.h" 2

# 1 "complexobject.h" 1
# 9 "complexobject.h"
typedef struct {
    double real;
    double imag;
} Py_complex;
# 23 "complexobject.h"
Py_complex _Py_c_sum(Py_complex, Py_complex);
Py_complex _Py_c_diff(Py_complex, Py_complex);
Py_complex _Py_c_neg(Py_complex);
Py_complex _Py_c_prod(Py_complex, Py_complex);
Py_complex _Py_c_quot(Py_complex, Py_complex);
Py_complex _Py_c_pow(Py_complex, Py_complex);
# 38 "complexobject.h"
typedef struct {
    Py_ssize_t ob_refcnt; struct _typeobject *ob_type;
    Py_complex cval;
} PyComplexObject;

extern PyTypeObject PyComplex_Type;




PyObject * PyComplex_FromCComplex(Py_complex);
PyObject * PyComplex_FromDoubles(double real, double imag);

double PyComplex_RealAsDouble(PyObject *op);
double PyComplex_ImagAsDouble(PyObject *op);
Py_complex PyComplex_AsCComplex(PyObject *op);
# 90 "Python.h" 2

# 1 "rangeobject.h" 1
# 21 "rangeobject.h"
extern PyTypeObject PyRange_Type;
# 92 "Python.h" 2
# 1 "stringobject.h" 1
# 35 "stringobject.h"
typedef struct {
    Py_ssize_t ob_refcnt; struct _typeobject *ob_type; Py_ssize_t ob_size;
    long ob_shash;
    int ob_sstate;
    char ob_sval[1];
# 49 "stringobject.h"
} PyStringObject;





extern PyTypeObject PyBaseString_Type;
extern PyTypeObject PyString_Type;




PyObject * PyString_FromStringAndSize(const char *, Py_ssize_t);
PyObject * PyString_FromString(const char *);
PyObject * PyString_FromFormatV(const char*, va_list)
    __attribute__((format(printf, 1, 0)));
PyObject * PyString_FromFormat(const char*, ...)
    __attribute__((format(printf, 1, 2)));
Py_ssize_t PyString_Size(PyObject *);
char * PyString_AsString(PyObject *);
PyObject * PyString_Repr(PyObject *, int);
void PyString_Concat(PyObject **, PyObject *);
void PyString_ConcatAndDel(PyObject **, PyObject *);
int _PyString_Resize(PyObject **, Py_ssize_t);
int _PyString_Eq(PyObject *, PyObject*);
PyObject * PyString_Format(PyObject *, PyObject *);
PyObject * _PyString_FormatLong(PyObject*, int, int,
        int, char**, int*);
PyObject * PyString_DecodeEscape(const char *, Py_ssize_t,
         const char *, Py_ssize_t,
         const char *);

void PyString_InternInPlace(PyObject **);
void PyString_InternImmortal(PyObject **);
PyObject * PyString_InternFromString(const char *);
void _Py_ReleaseInternedStrings(void);
# 95 "stringobject.h"
PyObject * _PyString_Join(PyObject *sep, PyObject *x);






PyObject* PyString_Decode(
    const char *s,
    Py_ssize_t size,
    const char *encoding,
    const char *errors
    );




PyObject* PyString_Encode(
    const char *s,
    Py_ssize_t size,
    const char *encoding,
    const char *errors
    );




PyObject* PyString_AsEncodedObject(
    PyObject *str,
    const char *encoding,
    const char *errors
    );
# 136 "stringobject.h"
PyObject* PyString_AsEncodedString(
    PyObject *str,
    const char *encoding,
    const char *errors
    );




PyObject* PyString_AsDecodedObject(
    PyObject *str,
    const char *encoding,
    const char *errors
    );
# 159 "stringobject.h"
PyObject* PyString_AsDecodedString(
    PyObject *str,
    const char *encoding,
    const char *errors
    );







int PyString_AsStringAndSize(
    register PyObject *obj,
    register char **s,
    register Py_ssize_t *len


    );
# 93 "Python.h" 2
# 1 "bufferobject.h" 1
# 13 "bufferobject.h"
extern PyTypeObject PyBuffer_Type;





PyObject * PyBuffer_FromObject(PyObject *base,
                                           Py_ssize_t offset, Py_ssize_t size);
PyObject * PyBuffer_FromReadWriteObject(PyObject *base,
                                                    Py_ssize_t offset,
                                                    Py_ssize_t size);

PyObject * PyBuffer_FromMemory(void *ptr, Py_ssize_t size);
PyObject * PyBuffer_FromReadWriteMemory(void *ptr, Py_ssize_t size);

PyObject * PyBuffer_New(Py_ssize_t size);
# 94 "Python.h" 2
# 1 "tupleobject.h" 1
# 24 "tupleobject.h"
typedef struct {
    Py_ssize_t ob_refcnt; struct _typeobject *ob_type; Py_ssize_t ob_size;
    PyObject *ob_item[1];





} PyTupleObject;

extern PyTypeObject PyTuple_Type;




PyObject * PyTuple_New(Py_ssize_t size);
Py_ssize_t PyTuple_Size(PyObject *);
PyObject * PyTuple_GetItem(PyObject *, Py_ssize_t);
int PyTuple_SetItem(PyObject *, Py_ssize_t, PyObject *);
PyObject * PyTuple_GetSlice(PyObject *, Py_ssize_t, Py_ssize_t);
int _PyTuple_Resize(PyObject **, Py_ssize_t);
PyObject * PyTuple_Pack(Py_ssize_t, ...);
# 95 "Python.h" 2
# 1 "listobject.h" 1
# 22 "listobject.h"
typedef struct {
    Py_ssize_t ob_refcnt; struct _typeobject *ob_type; Py_ssize_t ob_size;

    PyObject **ob_item;
# 38 "listobject.h"
    Py_ssize_t allocated;
} PyListObject;

extern PyTypeObject PyList_Type;




PyObject * PyList_New(Py_ssize_t size);
Py_ssize_t PyList_Size(PyObject *);
PyObject * PyList_GetItem(PyObject *, Py_ssize_t);
int PyList_SetItem(PyObject *, Py_ssize_t, PyObject *);
int PyList_Insert(PyObject *, Py_ssize_t, PyObject *);
int PyList_Append(PyObject *, PyObject *);
PyObject * PyList_GetSlice(PyObject *, Py_ssize_t, Py_ssize_t);
int PyList_SetSlice(PyObject *, Py_ssize_t, Py_ssize_t, PyObject *);
int PyList_Sort(PyObject *);
int PyList_Reverse(PyObject *);
PyObject * PyList_AsTuple(PyObject *);
PyObject * _PyList_Extend(PyListObject *, PyObject *);
# 96 "Python.h" 2
# 1 "dictobject.h" 1
# 50 "dictobject.h"
typedef struct {




 Py_ssize_t me_hash;
 PyObject *me_key;
 PyObject *me_value;
} PyDictEntry;
# 69 "dictobject.h"
typedef struct _dictobject PyDictObject;
struct _dictobject {
 Py_ssize_t ob_refcnt; struct _typeobject *ob_type;
 Py_ssize_t ma_fill;
 Py_ssize_t ma_used;





 Py_ssize_t ma_mask;






 PyDictEntry *ma_table;
 PyDictEntry *(*ma_lookup)(PyDictObject *mp, PyObject *key, long hash);
 PyDictEntry ma_smalltable[8];
};

extern PyTypeObject PyDict_Type;




PyObject * PyDict_New(void);
PyObject * PyDict_GetItem(PyObject *mp, PyObject *key);
int PyDict_SetItem(PyObject *mp, PyObject *key, PyObject *item);
int PyDict_DelItem(PyObject *mp, PyObject *key);
void PyDict_Clear(PyObject *mp);
int PyDict_Next(
 PyObject *mp, Py_ssize_t *pos, PyObject **key, PyObject **value);
int _PyDict_Next(
 PyObject *mp, Py_ssize_t *pos, PyObject **key, PyObject **value, long *hash);
PyObject * PyDict_Keys(PyObject *mp);
PyObject * PyDict_Values(PyObject *mp);
PyObject * PyDict_Items(PyObject *mp);
Py_ssize_t PyDict_Size(PyObject *mp);
PyObject * PyDict_Copy(PyObject *mp);
int PyDict_Contains(PyObject *mp, PyObject *key);
int _PyDict_Contains(PyObject *mp, PyObject *key, long hash);


int PyDict_Update(PyObject *mp, PyObject *other);






int PyDict_Merge(PyObject *mp,
       PyObject *other,
       int override);






int PyDict_MergeFromSeq2(PyObject *d,
        PyObject *seq2,
        int override);

PyObject * PyDict_GetItemString(PyObject *dp, const char *key);
int PyDict_SetItemString(PyObject *dp, const char *key, PyObject *item);
int PyDict_DelItemString(PyObject *dp, const char *key);
# 97 "Python.h" 2
# 1 "enumobject.h" 1
# 10 "enumobject.h"
extern PyTypeObject PyEnum_Type;
extern PyTypeObject PyReversed_Type;
# 98 "Python.h" 2
# 1 "setobject.h" 1
# 24 "setobject.h"
typedef struct {
 long hash;
 PyObject *key;
} setentry;






typedef struct _setobject PySetObject;
struct _setobject {
 Py_ssize_t ob_refcnt; struct _typeobject *ob_type;

 Py_ssize_t fill;
 Py_ssize_t used;





 Py_ssize_t mask;





 setentry *table;
 setentry *(*lookup)(PySetObject *so, PyObject *key, long hash);
 setentry smalltable[8];

 long hash;
 PyObject *weakreflist;
};

extern PyTypeObject PySet_Type;
extern PyTypeObject PyFrozenSet_Type;
# 77 "setobject.h"
PyObject * PySet_New(PyObject *);
PyObject * PyFrozenSet_New(PyObject *);
Py_ssize_t PySet_Size(PyObject *anyset);

int PySet_Clear(PyObject *set);
int PySet_Contains(PyObject *anyset, PyObject *key);
int PySet_Discard(PyObject *set, PyObject *key);
int PySet_Add(PyObject *set, PyObject *key);
int _PySet_Next(PyObject *set, Py_ssize_t *pos, PyObject **key);
int _PySet_NextEntry(PyObject *set, Py_ssize_t *pos, PyObject **key, long *hash);
PyObject * PySet_Pop(PyObject *set);
int _PySet_Update(PyObject *set, PyObject *iterable);
# 99 "Python.h" 2
# 1 "methodobject.h" 1
# 14 "methodobject.h"
extern PyTypeObject PyCFunction_Type;



typedef PyObject *(*PyCFunction)(PyObject *, PyObject *);
typedef PyObject *(*PyCFunctionWithKeywords)(PyObject *, PyObject *,
          PyObject *);
typedef PyObject *(*PyNoArgsFunction)(PyObject *);

PyCFunction PyCFunction_GetFunction(PyObject *);
PyObject * PyCFunction_GetSelf(PyObject *);
int PyCFunction_GetFlags(PyObject *);
# 35 "methodobject.h"
PyObject * PyCFunction_Call(PyObject *, PyObject *, PyObject *);

struct PyMethodDef {
    const char *ml_name;
    PyCFunction ml_meth;
    int ml_flags;

    const char *ml_doc;
};
typedef struct PyMethodDef PyMethodDef;

PyObject * Py_FindMethod(PyMethodDef[], PyObject *, const char *);


PyObject * PyCFunction_NewEx(PyMethodDef *, PyObject *,
      PyObject *);
# 73 "methodobject.h"
typedef struct PyMethodChain {
    PyMethodDef *methods;
    struct PyMethodChain *link;
} PyMethodChain;

PyObject * Py_FindMethodInChain(PyMethodChain *, PyObject *,
                                            const char *);

typedef struct {
    Py_ssize_t ob_refcnt; struct _typeobject *ob_type;
    PyMethodDef *m_ml;
    PyObject *m_self;
    PyObject *m_module;
} PyCFunctionObject;
# 100 "Python.h" 2
# 1 "moduleobject.h" 1
# 10 "moduleobject.h"
extern PyTypeObject PyModule_Type;




PyObject * PyModule_New(const char *);
PyObject * PyModule_GetDict(PyObject *);
char * PyModule_GetName(PyObject *);
char * PyModule_GetFilename(PyObject *);
void _PyModule_Clear(PyObject *);
# 101 "Python.h" 2
# 1 "funcobject.h" 1
# 21 "funcobject.h"
typedef struct {
    Py_ssize_t ob_refcnt; struct _typeobject *ob_type;
    PyObject *func_code;
    PyObject *func_globals;
    PyObject *func_defaults;
    PyObject *func_closure;
    PyObject *func_doc;
    PyObject *func_name;
    PyObject *func_dict;
    PyObject *func_weakreflist;
    PyObject *func_module;






} PyFunctionObject;

extern PyTypeObject PyFunction_Type;



PyObject * PyFunction_New(PyObject *, PyObject *);
PyObject * PyFunction_GetCode(PyObject *);
PyObject * PyFunction_GetGlobals(PyObject *);
PyObject * PyFunction_GetModule(PyObject *);
PyObject * PyFunction_GetDefaults(PyObject *);
int PyFunction_SetDefaults(PyObject *, PyObject *);
PyObject * PyFunction_GetClosure(PyObject *);
int PyFunction_SetClosure(PyObject *, PyObject *);
# 67 "funcobject.h"
extern PyTypeObject PyClassMethod_Type;
extern PyTypeObject PyStaticMethod_Type;

PyObject * PyClassMethod_New(PyObject *);
PyObject * PyStaticMethod_New(PyObject *);
# 102 "Python.h" 2
# 1 "classobject.h" 1
# 12 "classobject.h"
typedef struct {
    Py_ssize_t ob_refcnt; struct _typeobject *ob_type;
    PyObject *cl_bases;
    PyObject *cl_dict;
    PyObject *cl_name;

    PyObject *cl_getattr;
    PyObject *cl_setattr;
    PyObject *cl_delattr;
} PyClassObject;

typedef struct {
    Py_ssize_t ob_refcnt; struct _typeobject *ob_type;
    PyClassObject *in_class;
    PyObject *in_dict;
    PyObject *in_weakreflist;
} PyInstanceObject;

typedef struct {
    Py_ssize_t ob_refcnt; struct _typeobject *ob_type;
    PyObject *im_func;
    PyObject *im_self;
    PyObject *im_class;
    PyObject *im_weakreflist;
} PyMethodObject;

extern PyTypeObject PyClass_Type, PyInstance_Type, PyMethod_Type;





PyObject * PyClass_New(PyObject *, PyObject *, PyObject *);
PyObject * PyInstance_New(PyObject *, PyObject *,
                                            PyObject *);
PyObject * PyInstance_NewRaw(PyObject *, PyObject *);
PyObject * PyMethod_New(PyObject *, PyObject *, PyObject *);

PyObject * PyMethod_Function(PyObject *);
PyObject * PyMethod_Self(PyObject *);
PyObject * PyMethod_Class(PyObject *);
# 64 "classobject.h"
PyObject * _PyInstance_Lookup(PyObject *pinst, PyObject *name);
# 75 "classobject.h"
int PyClass_IsSubclass(PyObject *, PyObject *);
# 103 "Python.h" 2
# 1 "fileobject.h" 1
# 10 "fileobject.h"
typedef struct {
 Py_ssize_t ob_refcnt; struct _typeobject *ob_type;
 FILE *f_fp;
 PyObject *f_name;
 PyObject *f_mode;
 int (*f_close)(FILE *);
 int f_softspace;
 int f_binary;

 char* f_buf;
 char* f_bufend;
 char* f_bufptr;
 char *f_setbuf;
 int f_univ_newline;
 int f_newlinetypes;
 int f_skipnextlf;
 PyObject *f_encoding;
 PyObject *weakreflist;
} PyFileObject;

extern PyTypeObject PyFile_Type;




PyObject * PyFile_FromString(char *, char *);
void PyFile_SetBufSize(PyObject *, int);
int PyFile_SetEncoding(PyObject *, const char *);
PyObject * PyFile_FromFile(FILE *, char *, char *,
                                             int (*)(FILE *));
FILE * PyFile_AsFile(PyObject *);
PyObject * PyFile_Name(PyObject *);
PyObject * PyFile_GetLine(PyObject *, int);
int PyFile_WriteObject(PyObject *, PyObject *, int);
int PyFile_SoftSpace(PyObject *, int);
int PyFile_WriteString(const char *, PyObject *);
int PyObject_AsFileDescriptor(PyObject *);




extern const char * Py_FileSystemDefaultEncoding;





char *Py_UniversalNewlineFgets(char *, int, FILE*, PyObject *);
size_t Py_UniversalNewlineFread(char *, size_t, FILE *, PyObject *);
# 104 "Python.h" 2
# 1 "cobject.h" 1
# 17 "cobject.h"
extern PyTypeObject PyCObject_Type;
# 27 "cobject.h"
PyObject * PyCObject_FromVoidPtr(
 void *cobj, void (*destruct)(void*));







PyObject * PyCObject_FromVoidPtrAndDesc(
 void *cobj, void *desc, void (*destruct)(void*,void*));


void * PyCObject_AsVoidPtr(PyObject *);


void * PyCObject_GetDesc(PyObject *);


void * PyCObject_Import(char *module_name, char *cobject_name);


int PyCObject_SetVoidPtr(PyObject *self, void *cobj);
# 105 "Python.h" 2
# 1 "traceback.h" 1







struct _frame;



typedef struct _traceback {
 Py_ssize_t ob_refcnt; struct _typeobject *ob_type;
 struct _traceback *tb_next;
 struct _frame *tb_frame;
 int tb_lasti;
 int tb_lineno;
} PyTracebackObject;

int PyTraceBack_Here(struct _frame *);
int PyTraceBack_Print(PyObject *, PyObject *);


extern PyTypeObject PyTraceBack_Type;
# 106 "Python.h" 2
# 1 "sliceobject.h" 1
# 9 "sliceobject.h"
extern PyObject _Py_EllipsisObject;
# 22 "sliceobject.h"
typedef struct {
    Py_ssize_t ob_refcnt; struct _typeobject *ob_type;
    PyObject *start, *stop, *step;
} PySliceObject;

extern PyTypeObject PySlice_Type;



PyObject * PySlice_New(PyObject* start, PyObject* stop,
                                  PyObject* step);
PyObject * _PySlice_FromIndices(Py_ssize_t start, Py_ssize_t stop);
int PySlice_GetIndices(PySliceObject *r, Py_ssize_t length,
                                  Py_ssize_t *start, Py_ssize_t *stop, Py_ssize_t *step);
int PySlice_GetIndicesEx(PySliceObject *r, Py_ssize_t length,
        Py_ssize_t *start, Py_ssize_t *stop,
        Py_ssize_t *step, Py_ssize_t *slicelength);
# 107 "Python.h" 2
# 1 "cellobject.h" 1
# 9 "cellobject.h"
typedef struct {
 Py_ssize_t ob_refcnt; struct _typeobject *ob_type;
 PyObject *ob_ref;
} PyCellObject;

extern PyTypeObject PyCell_Type;



PyObject * PyCell_New(PyObject *);
PyObject * PyCell_Get(PyObject *);
int PyCell_Set(PyObject *, PyObject *);
# 108 "Python.h" 2
# 1 "iterobject.h" 1







extern PyTypeObject PySeqIter_Type;



PyObject * PySeqIter_New(PyObject *);

extern PyTypeObject PyCallIter_Type;



PyObject * PyCallIter_New(PyObject *, PyObject *);
# 109 "Python.h" 2
# 1 "genobject.h" 1
# 10 "genobject.h"
struct _frame;

typedef struct {
 Py_ssize_t ob_refcnt; struct _typeobject *ob_type;



 struct _frame *gi_frame;


 int gi_running;


 PyObject *gi_weakreflist;
} PyGenObject;

extern PyTypeObject PyGen_Type;




PyObject * PyGen_New(struct _frame *);
int PyGen_NeedsFinalizing(PyGenObject *);
# 110 "Python.h" 2
# 1 "descrobject.h" 1







typedef PyObject *(*getter)(PyObject *, void *);
typedef int (*setter)(PyObject *, PyObject *, void *);

typedef struct PyGetSetDef {
 char *name;
 getter get;
 setter set;
 char *doc;
 void *closure;
} PyGetSetDef;

typedef PyObject *(*wrapperfunc)(PyObject *self, PyObject *args,
     void *wrapped);

typedef PyObject *(*wrapperfunc_kwds)(PyObject *self, PyObject *args,
          void *wrapped, PyObject *kwds);

struct wrapperbase {
 char *name;
 int offset;
 void *function;
 wrapperfunc wrapper;
 char *doc;
 int flags;
 PyObject *name_strobj;
};
# 45 "descrobject.h"
typedef struct {
 Py_ssize_t ob_refcnt; struct _typeobject *ob_type; PyTypeObject *d_type; PyObject *d_name;
} PyDescrObject;

typedef struct {
 Py_ssize_t ob_refcnt; struct _typeobject *ob_type; PyTypeObject *d_type; PyObject *d_name;
 PyMethodDef *d_method;
} PyMethodDescrObject;

typedef struct {
 Py_ssize_t ob_refcnt; struct _typeobject *ob_type; PyTypeObject *d_type; PyObject *d_name;
 struct PyMemberDef *d_member;
} PyMemberDescrObject;

typedef struct {
 Py_ssize_t ob_refcnt; struct _typeobject *ob_type; PyTypeObject *d_type; PyObject *d_name;
 PyGetSetDef *d_getset;
} PyGetSetDescrObject;

typedef struct {
 Py_ssize_t ob_refcnt; struct _typeobject *ob_type; PyTypeObject *d_type; PyObject *d_name;
 struct wrapperbase *d_base;
 void *d_wrapped;
} PyWrapperDescrObject;

extern PyTypeObject PyWrapperDescr_Type;

PyObject * PyDescr_NewMethod(PyTypeObject *, PyMethodDef *);
PyObject * PyDescr_NewClassMethod(PyTypeObject *, PyMethodDef *);
PyObject * PyDescr_NewMember(PyTypeObject *,
            struct PyMemberDef *);
PyObject * PyDescr_NewGetSet(PyTypeObject *,
            struct PyGetSetDef *);
PyObject * PyDescr_NewWrapper(PyTypeObject *,
      struct wrapperbase *, void *);


PyObject * PyDictProxy_New(PyObject *);
PyObject * PyWrapper_New(PyObject *, PyObject *);


extern PyTypeObject PyProperty_Type;
# 111 "Python.h" 2
# 1 "weakrefobject.h" 1
# 10 "weakrefobject.h"
typedef struct _PyWeakReference PyWeakReference;




struct _PyWeakReference {
    Py_ssize_t ob_refcnt; struct _typeobject *ob_type;





    PyObject *wr_object;


    PyObject *wr_callback;




    long hash;






    PyWeakReference *wr_prev;
    PyWeakReference *wr_next;
};

extern PyTypeObject _PyWeakref_RefType;
extern PyTypeObject _PyWeakref_ProxyType;
extern PyTypeObject _PyWeakref_CallableProxyType;
# 59 "weakrefobject.h"
PyObject * PyWeakref_NewRef(PyObject *ob,
                                              PyObject *callback);
PyObject * PyWeakref_NewProxy(PyObject *ob,
                                                PyObject *callback);
PyObject * PyWeakref_GetObject(PyObject *ref);

Py_ssize_t _PyWeakref_GetWeakrefCount(PyWeakReference *head);

void _PyWeakref_ClearRef(PyWeakReference *self);
# 112 "Python.h" 2

# 1 "codecs.h" 1
# 26 "codecs.h"
int PyCodec_Register(
       PyObject *search_function
       );
# 48 "codecs.h"
PyObject * _PyCodec_Lookup(
       const char *encoding
       );
# 62 "codecs.h"
PyObject * PyCodec_Encode(
       PyObject *object,
       const char *encoding,
       const char *errors
       );
# 78 "codecs.h"
PyObject * PyCodec_Decode(
       PyObject *object,
       const char *encoding,
       const char *errors
       );
# 94 "codecs.h"
PyObject * PyCodec_Encoder(
       const char *encoding
       );



PyObject * PyCodec_Decoder(
       const char *encoding
       );



PyObject * PyCodec_IncrementalEncoder(
       const char *encoding,
       const char *errors
       );



PyObject * PyCodec_IncrementalDecoder(
       const char *encoding,
       const char *errors
       );



PyObject * PyCodec_StreamReader(
       const char *encoding,
       PyObject *stream,
       const char *errors
       );



PyObject * PyCodec_StreamWriter(
       const char *encoding,
       PyObject *stream,
       const char *errors
       );
# 142 "codecs.h"
int PyCodec_RegisterError(const char *name, PyObject *error);




PyObject * PyCodec_LookupError(const char *name);


PyObject * PyCodec_StrictErrors(PyObject *exc);


PyObject * PyCodec_IgnoreErrors(PyObject *exc);


PyObject * PyCodec_ReplaceErrors(PyObject *exc);


PyObject * PyCodec_XMLCharRefReplaceErrors(PyObject *exc);


PyObject * PyCodec_BackslashReplaceErrors(PyObject *exc);
# 114 "Python.h" 2
# 1 "pyerrors.h" 1
# 9 "pyerrors.h"
typedef struct {
    Py_ssize_t ob_refcnt; struct _typeobject *ob_type;
    PyObject *dict;
    PyObject *args;
    PyObject *message;
} PyBaseExceptionObject;

typedef struct {
    Py_ssize_t ob_refcnt; struct _typeobject *ob_type;
    PyObject *dict;
    PyObject *args;
    PyObject *message;
    PyObject *msg;
    PyObject *filename;
    PyObject *lineno;
    PyObject *offset;
    PyObject *text;
    PyObject *print_file_and_line;
} PySyntaxErrorObject;


typedef struct {
    Py_ssize_t ob_refcnt; struct _typeobject *ob_type;
    PyObject *dict;
    PyObject *args;
    PyObject *message;
    PyObject *encoding;
    PyObject *object;
    PyObject *start;
    PyObject *end;
    PyObject *reason;
} PyUnicodeErrorObject;


typedef struct {
    Py_ssize_t ob_refcnt; struct _typeobject *ob_type;
    PyObject *dict;
    PyObject *args;
    PyObject *message;
    PyObject *code;
} PySystemExitObject;

typedef struct {
    Py_ssize_t ob_refcnt; struct _typeobject *ob_type;
    PyObject *dict;
    PyObject *args;
    PyObject *message;
    PyObject *myerrno;
    PyObject *strerror;
    PyObject *filename;
} PyEnvironmentErrorObject;
# 76 "pyerrors.h"
void PyErr_SetNone(PyObject *);
void PyErr_SetObject(PyObject *, PyObject *);
void PyErr_SetString(PyObject *, const char *);
PyObject * PyErr_Occurred(void);
void PyErr_Clear(void);
void PyErr_Fetch(PyObject **, PyObject **, PyObject **);
void PyErr_Restore(PyObject *, PyObject *, PyObject *);
# 91 "pyerrors.h"
int PyErr_GivenExceptionMatches(PyObject *, PyObject *);
int PyErr_ExceptionMatches(PyObject *);
void PyErr_NormalizeException(PyObject**, PyObject**, PyObject**);
# 120 "pyerrors.h"
extern PyObject * PyExc_BaseException;
extern PyObject * PyExc_Exception;
extern PyObject * PyExc_StopIteration;
extern PyObject * PyExc_GeneratorExit;
extern PyObject * PyExc_StandardError;
extern PyObject * PyExc_ArithmeticError;
extern PyObject * PyExc_LookupError;

extern PyObject * PyExc_AssertionError;
extern PyObject * PyExc_AttributeError;
extern PyObject * PyExc_EOFError;
extern PyObject * PyExc_FloatingPointError;
extern PyObject * PyExc_EnvironmentError;
extern PyObject * PyExc_IOError;
extern PyObject * PyExc_OSError;
extern PyObject * PyExc_ImportError;
extern PyObject * PyExc_IndexError;
extern PyObject * PyExc_KeyError;
extern PyObject * PyExc_KeyboardInterrupt;
extern PyObject * PyExc_MemoryError;
extern PyObject * PyExc_NameError;
extern PyObject * PyExc_OverflowError;
extern PyObject * PyExc_RuntimeError;
extern PyObject * PyExc_NotImplementedError;
extern PyObject * PyExc_SyntaxError;
extern PyObject * PyExc_IndentationError;
extern PyObject * PyExc_TabError;
extern PyObject * PyExc_ReferenceError;
extern PyObject * PyExc_SystemError;
extern PyObject * PyExc_SystemExit;
extern PyObject * PyExc_TypeError;
extern PyObject * PyExc_UnboundLocalError;
extern PyObject * PyExc_UnicodeError;
extern PyObject * PyExc_UnicodeEncodeError;
extern PyObject * PyExc_UnicodeDecodeError;
extern PyObject * PyExc_UnicodeTranslateError;
extern PyObject * PyExc_ValueError;
extern PyObject * PyExc_ZeroDivisionError;







extern PyObject * PyExc_MemoryErrorInst;


extern PyObject * PyExc_Warning;
extern PyObject * PyExc_UserWarning;
extern PyObject * PyExc_DeprecationWarning;
extern PyObject * PyExc_PendingDeprecationWarning;
extern PyObject * PyExc_SyntaxWarning;
extern PyObject * PyExc_RuntimeWarning;
extern PyObject * PyExc_FutureWarning;
extern PyObject * PyExc_ImportWarning;
extern PyObject * PyExc_UnicodeWarning;




int PyErr_BadArgument(void);
PyObject * PyErr_NoMemory(void);
PyObject * PyErr_SetFromErrno(PyObject *);
PyObject * PyErr_SetFromErrnoWithFilenameObject(
 PyObject *, PyObject *);
PyObject * PyErr_SetFromErrnoWithFilename(PyObject *, char *);





PyObject * PyErr_Format(PyObject *, const char *, ...)
   __attribute__((format(printf, 2, 3)));
# 217 "pyerrors.h"
void PyErr_BadInternalCall(void);
void _PyErr_BadInternalCall(char *filename, int lineno);





PyObject * PyErr_NewException(char *name, PyObject *base,
                                         PyObject *dict);
void PyErr_WriteUnraisable(PyObject *);


int PyErr_WarnEx(PyObject *category, const char *msg,
        Py_ssize_t stack_level);
int PyErr_WarnExplicit(PyObject *, const char *,
       const char *, int,
       const char *, PyObject *);





int PyErr_CheckSignals(void);
void PyErr_SetInterrupt(void);


void PyErr_SyntaxLocation(const char *, int);
PyObject * PyErr_ProgramText(const char *, int);






PyObject * PyUnicodeDecodeError_Create(
 const char *, const char *, Py_ssize_t, Py_ssize_t, Py_ssize_t, const char *);


PyObject * PyUnicodeEncodeError_Create(
 const char *, const Py_UNICODE *, Py_ssize_t, Py_ssize_t, Py_ssize_t, const char *);


PyObject * PyUnicodeTranslateError_Create(
 const Py_UNICODE *, Py_ssize_t, Py_ssize_t, Py_ssize_t, const char *);


PyObject * PyUnicodeEncodeError_GetEncoding(PyObject *);
PyObject * PyUnicodeDecodeError_GetEncoding(PyObject *);


PyObject * PyUnicodeEncodeError_GetObject(PyObject *);
PyObject * PyUnicodeDecodeError_GetObject(PyObject *);
PyObject * PyUnicodeTranslateError_GetObject(PyObject *);



int PyUnicodeEncodeError_GetStart(PyObject *, Py_ssize_t *);
int PyUnicodeDecodeError_GetStart(PyObject *, Py_ssize_t *);
int PyUnicodeTranslateError_GetStart(PyObject *, Py_ssize_t *);



int PyUnicodeEncodeError_SetStart(PyObject *, Py_ssize_t);
int PyUnicodeDecodeError_SetStart(PyObject *, Py_ssize_t);
int PyUnicodeTranslateError_SetStart(PyObject *, Py_ssize_t);



int PyUnicodeEncodeError_GetEnd(PyObject *, Py_ssize_t *);
int PyUnicodeDecodeError_GetEnd(PyObject *, Py_ssize_t *);
int PyUnicodeTranslateError_GetEnd(PyObject *, Py_ssize_t *);



int PyUnicodeEncodeError_SetEnd(PyObject *, Py_ssize_t);
int PyUnicodeDecodeError_SetEnd(PyObject *, Py_ssize_t);
int PyUnicodeTranslateError_SetEnd(PyObject *, Py_ssize_t);


PyObject * PyUnicodeEncodeError_GetReason(PyObject *);
PyObject * PyUnicodeDecodeError_GetReason(PyObject *);
PyObject * PyUnicodeTranslateError_GetReason(PyObject *);



int PyUnicodeEncodeError_SetReason(
 PyObject *, const char *);
int PyUnicodeDecodeError_SetReason(
 PyObject *, const char *);
int PyUnicodeTranslateError_SetReason(
 PyObject *, const char *);
# 326 "pyerrors.h"
int PyOS_snprintf(char *str, size_t size, const char *format, ...)
   __attribute__((format(printf, 3, 4)));
int PyOS_vsnprintf(char *str, size_t size, const char *format, va_list va)
   __attribute__((format(printf, 3, 0)));
# 115 "Python.h" 2

# 1 "pystate.h" 1
# 13 "pystate.h"
struct _ts;
struct _is;

typedef struct _is {

    struct _is *next;
    struct _ts *tstate_head;

    PyObject *modules;
    PyObject *sysdict;
    PyObject *builtins;
    PyObject *modules_reloading;

    PyObject *codec_search_path;
    PyObject *codec_search_cache;
    PyObject *codec_error_registry;


    int dlopenflags;





} PyInterpreterState;




struct _frame;


typedef int (*Py_tracefunc)(PyObject *, struct _frame *, int, PyObject *);
# 56 "pystate.h"
typedef struct _ts {


    struct _ts *next;
    PyInterpreterState *interp;

    struct _frame *frame;
    int recursion_depth;



    int tracing;
    int use_tracing;

    Py_tracefunc c_profilefunc;
    Py_tracefunc c_tracefunc;
    PyObject *c_profileobj;
    PyObject *c_traceobj;

    PyObject *curexc_type;
    PyObject *curexc_value;
    PyObject *curexc_traceback;

    PyObject *exc_type;
    PyObject *exc_value;
    PyObject *exc_traceback;

    PyObject *dict;







    int tick_counter;

    int gilstate_counter;

    PyObject *async_exc;
    long thread_id;



} PyThreadState;


PyInterpreterState * PyInterpreterState_New(void);
void PyInterpreterState_Clear(PyInterpreterState *);
void PyInterpreterState_Delete(PyInterpreterState *);

PyThreadState * PyThreadState_New(PyInterpreterState *);
void PyThreadState_Clear(PyThreadState *);
void PyThreadState_Delete(PyThreadState *);

void PyThreadState_DeleteCurrent(void);


PyThreadState * PyThreadState_Get(void);
PyThreadState * PyThreadState_Swap(PyThreadState *);
PyObject * PyThreadState_GetDict(void);
int PyThreadState_SetAsyncExc(long, PyObject *);




extern PyThreadState * _PyThreadState_Current;







typedef
    enum {PyGILState_LOCKED, PyGILState_UNLOCKED}
        PyGILState_STATE;
# 155 "pystate.h"
PyGILState_STATE PyGILState_Ensure(void);
# 165 "pystate.h"
void PyGILState_Release(PyGILState_STATE);







PyThreadState * PyGILState_GetThisThreadState(void);




PyObject * _PyThread_CurrentFrames(void);



PyInterpreterState * PyInterpreterState_Head(void);
PyInterpreterState * PyInterpreterState_Next(PyInterpreterState *);
PyThreadState * PyInterpreterState_ThreadHead(PyInterpreterState *);
PyThreadState * PyThreadState_Next(PyThreadState *);

typedef struct _frame *(*PyThreadFrameGetter)(PyThreadState *self_);


extern PyThreadFrameGetter _PyThreadState_GetFrame;
# 117 "Python.h" 2

# 1 "pyarena.h" 1
# 11 "pyarena.h"
  typedef struct _arena PyArena;
# 35 "pyarena.h"
  PyArena * PyArena_New(void);
  void PyArena_Free(PyArena *);
# 50 "pyarena.h"
  void * PyArena_Malloc(PyArena *, size_t size);





  int PyArena_AddPyObject(PyArena *, PyObject *);
# 119 "Python.h" 2
# 1 "modsupport.h" 1
# 23 "modsupport.h"
PyObject * _Py_VaBuildValue_SizeT(const char *, va_list);


int PyArg_Parse(PyObject *, const char *, ...);
int PyArg_ParseTuple(PyObject *, const char *, ...);
int PyArg_ParseTupleAndKeywords(PyObject *, PyObject *,
                                                  const char *, char **, ...);
int PyArg_UnpackTuple(PyObject *, const char *, Py_ssize_t, Py_ssize_t, ...);
PyObject * Py_BuildValue(const char *, ...);
PyObject * _Py_BuildValue_SizeT(const char *, ...);
int _PyArg_NoKeywords(const char *funcname, PyObject *kw);

int PyArg_VaParse(PyObject *, const char *, va_list);
int PyArg_VaParseTupleAndKeywords(PyObject *, PyObject *,
                                                  const char *, char **, va_list);
PyObject * Py_VaBuildValue(const char *, va_list);

int PyModule_AddObject(PyObject *, const char *, PyObject *);
int PyModule_AddIntConstant(PyObject *, const char *, long);
int PyModule_AddStringConstant(PyObject *, const char *, const char *);
# 116 "modsupport.h"
PyObject * Py_InitModule4_64(const char *name, PyMethodDef *methods,
                                      const char *doc, PyObject *self,
                                      int apiver);
# 128 "modsupport.h"
extern char * _Py_PackageContext;
# 120 "Python.h" 2
# 1 "pythonrun.h" 1
# 17 "pythonrun.h"
typedef struct {
 int cf_flags;
} PyCompilerFlags;

void Py_SetProgramName(char *);
char * Py_GetProgramName(void);

void Py_SetPythonHome(char *);
char * Py_GetPythonHome(void);

void Py_Initialize(void);
void Py_InitializeEx(int);
void Py_Finalize(void);
int Py_IsInitialized(void);
PyThreadState * Py_NewInterpreter(void);
void Py_EndInterpreter(PyThreadState *);

int PyRun_AnyFileFlags(FILE *, const char *, PyCompilerFlags *);
int PyRun_AnyFileExFlags(FILE *, const char *, int, PyCompilerFlags *);
int PyRun_SimpleStringFlags(const char *, PyCompilerFlags *);
int PyRun_SimpleFileExFlags(FILE *, const char *, int, PyCompilerFlags *);
int PyRun_InteractiveOneFlags(FILE *, const char *, PyCompilerFlags *);
int PyRun_InteractiveLoopFlags(FILE *, const char *, PyCompilerFlags *);

struct _mod * PyParser_ASTFromString(const char *, const char *,
       int, PyCompilerFlags *flags,
                                                 PyArena *);
struct _mod * PyParser_ASTFromFile(FILE *, const char *, int,
            char *, char *,
                                               PyCompilerFlags *, int *,
                                               PyArena *);




struct _node * PyParser_SimpleParseStringFlags(const char *, int,
         int);
struct _node * PyParser_SimpleParseFileFlags(FILE *, const char *,
       int, int);

PyObject * PyRun_StringFlags(const char *, int, PyObject *,
      PyObject *, PyCompilerFlags *);

PyObject * PyRun_FileExFlags(FILE *, const char *, int,
      PyObject *, PyObject *, int,
      PyCompilerFlags *);


PyObject * Py_CompileStringFlags(const char *, const char *, int,
          PyCompilerFlags *);
struct symtable * Py_SymtableString(const char *, const char *, int);

void PyErr_Print(void);
void PyErr_PrintEx(int);
void PyErr_Display(PyObject *, PyObject *, PyObject *);

int Py_AtExit(void (*func)(void));

void Py_Exit(int);

int Py_FdIsInteractive(FILE *, const char *);


int Py_Main(int argc, char **argv);
# 102 "pythonrun.h"
char * Py_GetProgramFullPath(void);
char * Py_GetPrefix(void);
char * Py_GetExecPrefix(void);
char * Py_GetPath(void);


const char * Py_GetVersion(void);
const char * Py_GetPlatform(void);
const char * Py_GetCopyright(void);
const char * Py_GetCompiler(void);
const char * Py_GetBuildInfo(void);
const char * _Py_svnversion(void);
const char * Py_SubversionRevision(void);
const char * Py_SubversionShortBranch(void);


PyObject * _PyBuiltin_Init(void);
PyObject * _PySys_Init(void);
void _PyImport_Init(void);
void _PyExc_Init(void);
void _PyImportHooks_Init(void);
int _PyFrame_Init(void);
int _PyInt_Init(void);
void _PyFloat_Init(void);


void _PyExc_Fini(void);
void _PyImport_Fini(void);
void PyMethod_Fini(void);
void PyFrame_Fini(void);
void PyCFunction_Fini(void);
void PyTuple_Fini(void);
void PyList_Fini(void);
void PySet_Fini(void);
void PyString_Fini(void);
void PyInt_Fini(void);
void PyFloat_Fini(void);
void PyOS_FiniInterrupts(void);


char * PyOS_Readline(FILE *, FILE *, char *);
extern int (*PyOS_InputHook)(void);
extern char *(*PyOS_ReadlineFunctionPointer)(FILE *, FILE *, char *);
extern PyThreadState* _PyOS_ReadlineTState;
# 163 "pythonrun.h"
typedef void (*PyOS_sighandler_t)(int);
PyOS_sighandler_t PyOS_getsig(int);
PyOS_sighandler_t PyOS_setsig(int, PyOS_sighandler_t);
# 121 "Python.h" 2
# 1 "ceval.h" 1
# 10 "ceval.h"
PyObject * PyEval_CallObjectWithKeywords(
 PyObject *, PyObject *, PyObject *);



PyObject * PyEval_CallObject(PyObject *, PyObject *);





PyObject * PyEval_CallFunction(PyObject *obj,
                                           const char *format, ...);
PyObject * PyEval_CallMethod(PyObject *obj,
                                         const char *methodname,
                                         const char *format, ...);

void PyEval_SetProfile(Py_tracefunc, PyObject *);
void PyEval_SetTrace(Py_tracefunc, PyObject *);

struct _frame;

PyObject * PyEval_GetBuiltins(void);
PyObject * PyEval_GetGlobals(void);
PyObject * PyEval_GetLocals(void);
struct _frame * PyEval_GetFrame(void);
int PyEval_GetRestricted(void);




int PyEval_MergeCompilerFlags(PyCompilerFlags *cf);

int Py_FlushLine(void);

int Py_AddPendingCall(int (*func)(void *), void *arg);
int Py_MakePendingCalls(void);


void Py_SetRecursionLimit(int);
int Py_GetRecursionLimit(void);






int _Py_CheckRecursiveCall(char *where);
extern int _Py_CheckRecursionLimit;






const char * PyEval_GetFuncName(PyObject *);
const char * PyEval_GetFuncDesc(PyObject *);

PyObject * PyEval_GetCallStats(PyObject *);
PyObject * PyEval_EvalFrame(struct _frame *);
PyObject * PyEval_EvalFrameEx(struct _frame *f, int exc);


extern volatile int _Py_Ticker;
extern int _Py_CheckInterval;
# 121 "ceval.h"
PyThreadState * PyEval_SaveThread(void);
void PyEval_RestoreThread(PyThreadState *);



int PyEval_ThreadsInitialized(void);
void PyEval_InitThreads(void);
void PyEval_AcquireLock(void);
void PyEval_ReleaseLock(void);
void PyEval_AcquireThread(PyThreadState *tstate);
void PyEval_ReleaseThread(PyThreadState *tstate);
void PyEval_ReInitThreads(void);
# 151 "ceval.h"
int _PyEval_SliceIndex(PyObject *, Py_ssize_t *);
# 122 "Python.h" 2
# 1 "sysmodule.h" 1
# 10 "sysmodule.h"
PyObject * PySys_GetObject(char *);
int PySys_SetObject(char *, PyObject *);
FILE * PySys_GetFile(char *, FILE *);
void PySys_SetArgv(int, char **);
void PySys_SetPath(char *);

void PySys_WriteStdout(const char *format, ...)
   __attribute__((format(printf, 1, 2)));
void PySys_WriteStderr(const char *format, ...)
   __attribute__((format(printf, 1, 2)));

extern PyObject * _PySys_TraceFunc, *_PySys_ProfileFunc;
extern int _PySys_CheckInterval;

void PySys_ResetWarnOptions(void);
void PySys_AddWarnOption(char *);
# 123 "Python.h" 2
# 1 "intrcheck.h" 1







int PyOS_InterruptOccurred(void);
void PyOS_InitInterrupts(void);
void PyOS_AfterFork(void);
# 124 "Python.h" 2
# 1 "import.h" 1
# 10 "import.h"
long PyImport_GetMagicNumber(void);
PyObject * PyImport_ExecCodeModule(char *name, PyObject *co);
PyObject * PyImport_ExecCodeModuleEx(
 char *name, PyObject *co, char *pathname);
PyObject * PyImport_GetModuleDict(void);
PyObject * PyImport_AddModule(const char *name);
PyObject * PyImport_ImportModule(const char *name);
PyObject * PyImport_ImportModuleLevel(char *name,
 PyObject *globals, PyObject *locals, PyObject *fromlist, int level);



PyObject * PyImport_ImportModuleEx(
 char *name, PyObject *globals, PyObject *locals, PyObject *fromlist);



PyObject * PyImport_Import(PyObject *name);
PyObject * PyImport_ReloadModule(PyObject *m);
void PyImport_Cleanup(void);
int PyImport_ImportFrozenModule(char *);

struct filedescr * _PyImport_FindModule(
 const char *, PyObject *, char *, size_t, FILE **, PyObject **);
int _PyImport_IsScript(struct filedescr *);
void _PyImport_ReInitLock(void);

PyObject *_PyImport_FindExtension(char *, char *);
PyObject *_PyImport_FixupExtension(char *, char *);

struct _inittab {
    char *name;
    void (*initfunc)(void);
};

extern struct _inittab * PyImport_Inittab;

int PyImport_AppendInittab(char *name, void (*initfunc)(void));
int PyImport_ExtendInittab(struct _inittab *newtab);

struct _frozen {
    char *name;
    unsigned char *code;
    int size;
};




extern struct _frozen * PyImport_FrozenModules;
# 125 "Python.h" 2

# 1 "abstract.h" 1
# 231 "abstract.h"
     int PyObject_Cmp(PyObject *o1, PyObject *o2, int *result);
# 304 "abstract.h"
     PyObject * PyObject_Call(PyObject *callable_object,
      PyObject *args, PyObject *kw);
# 314 "abstract.h"
     PyObject * PyObject_CallObject(PyObject *callable_object,
                                               PyObject *args);
# 326 "abstract.h"
     PyObject * PyObject_CallFunction(PyObject *callable_object,
                                                 char *format, ...);
# 340 "abstract.h"
     PyObject * PyObject_CallMethod(PyObject *o, char *m,
                                               char *format, ...);
# 352 "abstract.h"
     PyObject * _PyObject_CallFunction_SizeT(PyObject *callable,
        char *format, ...);
     PyObject * _PyObject_CallMethod_SizeT(PyObject *o,
             char *name,
             char *format, ...);

     PyObject * PyObject_CallFunctionObjArgs(PyObject *callable,
                                                        ...);
# 370 "abstract.h"
     PyObject * PyObject_CallMethodObjArgs(PyObject *o,
                                                      PyObject *m, ...);
# 413 "abstract.h"
     PyObject * PyObject_Type(PyObject *o);







     Py_ssize_t PyObject_Size(PyObject *o);
# 433 "abstract.h"
     Py_ssize_t PyObject_Length(PyObject *o);


     Py_ssize_t _PyObject_LengthHint(PyObject *o);
# 457 "abstract.h"
     PyObject * PyObject_GetItem(PyObject *o, PyObject *key);
# 466 "abstract.h"
     int PyObject_SetItem(PyObject *o, PyObject *key, PyObject *v);







     int PyObject_DelItemString(PyObject *o, char *key);







     int PyObject_DelItem(PyObject *o, PyObject *key);






     int PyObject_AsCharBuffer(PyObject *obj,
       const char **buffer,
       Py_ssize_t *buffer_len);
# 505 "abstract.h"
     int PyObject_CheckReadBuffer(PyObject *obj);
# 514 "abstract.h"
     int PyObject_AsReadBuffer(PyObject *obj,
       const void **buffer,
       Py_ssize_t *buffer_len);
# 530 "abstract.h"
     int PyObject_AsWriteBuffer(PyObject *obj,
        void **buffer,
        Py_ssize_t *buffer_len);
# 547 "abstract.h"
     PyObject * PyObject_GetIter(PyObject *);
# 556 "abstract.h"
     PyObject * PyIter_Next(PyObject *);







     int PyNumber_Check(PyObject *o);
# 574 "abstract.h"
     PyObject * PyNumber_Add(PyObject *o1, PyObject *o2);
# 583 "abstract.h"
     PyObject * PyNumber_Subtract(PyObject *o1, PyObject *o2);
# 592 "abstract.h"
     PyObject * PyNumber_Multiply(PyObject *o1, PyObject *o2);
# 602 "abstract.h"
     PyObject * PyNumber_Divide(PyObject *o1, PyObject *o2);
# 611 "abstract.h"
     PyObject * PyNumber_FloorDivide(PyObject *o1, PyObject *o2);
# 621 "abstract.h"
     PyObject * PyNumber_TrueDivide(PyObject *o1, PyObject *o2);
# 631 "abstract.h"
     PyObject * PyNumber_Remainder(PyObject *o1, PyObject *o2);
# 641 "abstract.h"
     PyObject * PyNumber_Divmod(PyObject *o1, PyObject *o2);
# 651 "abstract.h"
     PyObject * PyNumber_Power(PyObject *o1, PyObject *o2,
                                          PyObject *o3);
# 661 "abstract.h"
     PyObject * PyNumber_Negative(PyObject *o);







     PyObject * PyNumber_Positive(PyObject *o);







     PyObject * PyNumber_Absolute(PyObject *o);







     PyObject * PyNumber_Invert(PyObject *o);
# 695 "abstract.h"
     PyObject * PyNumber_Lshift(PyObject *o1, PyObject *o2);
# 705 "abstract.h"
     PyObject * PyNumber_Rshift(PyObject *o1, PyObject *o2);
# 714 "abstract.h"
     PyObject * PyNumber_And(PyObject *o1, PyObject *o2);
# 724 "abstract.h"
     PyObject * PyNumber_Xor(PyObject *o1, PyObject *o2);
# 734 "abstract.h"
     PyObject * PyNumber_Or(PyObject *o1, PyObject *o2);
# 767 "abstract.h"
     PyObject * PyNumber_Index(PyObject *o);






     Py_ssize_t PyNumber_AsSsize_t(PyObject *o, PyObject *exc);
# 784 "abstract.h"
     PyObject * PyNumber_Int(PyObject *o);
# 793 "abstract.h"
     PyObject * PyNumber_Long(PyObject *o);
# 802 "abstract.h"
     PyObject * PyNumber_Float(PyObject *o);
# 812 "abstract.h"
     PyObject * PyNumber_InPlaceAdd(PyObject *o1, PyObject *o2);
# 821 "abstract.h"
     PyObject * PyNumber_InPlaceSubtract(PyObject *o1, PyObject *o2);
# 830 "abstract.h"
     PyObject * PyNumber_InPlaceMultiply(PyObject *o1, PyObject *o2);
# 839 "abstract.h"
     PyObject * PyNumber_InPlaceDivide(PyObject *o1, PyObject *o2);
# 848 "abstract.h"
     PyObject * PyNumber_InPlaceFloorDivide(PyObject *o1,
             PyObject *o2);
# 859 "abstract.h"
     PyObject * PyNumber_InPlaceTrueDivide(PyObject *o1,
            PyObject *o2);
# 870 "abstract.h"
     PyObject * PyNumber_InPlaceRemainder(PyObject *o1, PyObject *o2);
# 879 "abstract.h"
     PyObject * PyNumber_InPlacePower(PyObject *o1, PyObject *o2,
            PyObject *o3);
# 889 "abstract.h"
     PyObject * PyNumber_InPlaceLshift(PyObject *o1, PyObject *o2);
# 898 "abstract.h"
     PyObject * PyNumber_InPlaceRshift(PyObject *o1, PyObject *o2);
# 907 "abstract.h"
     PyObject * PyNumber_InPlaceAnd(PyObject *o1, PyObject *o2);
# 916 "abstract.h"
     PyObject * PyNumber_InPlaceXor(PyObject *o1, PyObject *o2);
# 925 "abstract.h"
     PyObject * PyNumber_InPlaceOr(PyObject *o1, PyObject *o2);
# 937 "abstract.h"
     int PySequence_Check(PyObject *o);
# 947 "abstract.h"
     Py_ssize_t PySequence_Size(PyObject *o);
# 956 "abstract.h"
     Py_ssize_t PySequence_Length(PyObject *o);



     PyObject * PySequence_Concat(PyObject *o1, PyObject *o2);
# 969 "abstract.h"
     PyObject * PySequence_Repeat(PyObject *o, Py_ssize_t count);
# 978 "abstract.h"
     PyObject * PySequence_GetItem(PyObject *o, Py_ssize_t i);






     PyObject * PySequence_GetSlice(PyObject *o, Py_ssize_t i1, Py_ssize_t i2);
# 994 "abstract.h"
     int PySequence_SetItem(PyObject *o, Py_ssize_t i, PyObject *v);
# 1003 "abstract.h"
     int PySequence_DelItem(PyObject *o, Py_ssize_t i);







     int PySequence_SetSlice(PyObject *o, Py_ssize_t i1, Py_ssize_t i2,
                                        PyObject *v);







     int PySequence_DelSlice(PyObject *o, Py_ssize_t i1, Py_ssize_t i2);







     PyObject * PySequence_Tuple(PyObject *o);







     PyObject * PySequence_List(PyObject *o);





     PyObject * PySequence_Fast(PyObject *o, const char* m);
# 1078 "abstract.h"
     Py_ssize_t PySequence_Count(PyObject *o, PyObject *value);
# 1087 "abstract.h"
     int PySequence_Contains(PyObject *seq, PyObject *ob);
# 1096 "abstract.h"
     Py_ssize_t _PySequence_IterSearch(PyObject *seq,
          PyObject *obj, int operation);
# 1111 "abstract.h"
     int PySequence_In(PyObject *o, PyObject *value);
# 1122 "abstract.h"
     Py_ssize_t PySequence_Index(PyObject *o, PyObject *value);
# 1132 "abstract.h"
     PyObject * PySequence_InPlaceConcat(PyObject *o1, PyObject *o2);
# 1141 "abstract.h"
     PyObject * PySequence_InPlaceRepeat(PyObject *o, Py_ssize_t count);
# 1152 "abstract.h"
     int PyMapping_Check(PyObject *o);
# 1161 "abstract.h"
     Py_ssize_t PyMapping_Size(PyObject *o);
# 1171 "abstract.h"
     Py_ssize_t PyMapping_Length(PyObject *o);
# 1195 "abstract.h"
     int PyMapping_HasKeyString(PyObject *o, char *key);
# 1205 "abstract.h"
     int PyMapping_HasKey(PyObject *o, PyObject *key);
# 1248 "abstract.h"
     PyObject * PyMapping_GetItemString(PyObject *o, char *key);







     int PyMapping_SetItemString(PyObject *o, char *key,
                                            PyObject *value);
# 1266 "abstract.h"
int PyObject_IsInstance(PyObject *object, PyObject *typeorclass);


int PyObject_IsSubclass(PyObject *object, PyObject *typeorclass);
# 127 "Python.h" 2

# 1 "compile.h" 1




# 1 "code.h" 1
# 10 "code.h"
typedef struct {
    Py_ssize_t ob_refcnt; struct _typeobject *ob_type;
    int co_argcount;
    int co_nlocals;
    int co_stacksize;
    int co_flags;
    PyObject *co_code;
    PyObject *co_consts;
    PyObject *co_names;
    PyObject *co_varnames;
    PyObject *co_freevars;
    PyObject *co_cellvars;

    PyObject *co_filename;
    PyObject *co_name;
    int co_firstlineno;
    PyObject *co_lnotab;
    void *co_zombieframe;
} PyCodeObject;
# 59 "code.h"
extern PyTypeObject PyCode_Type;





PyCodeObject * PyCode_New(
 int, int, int, int, PyObject *, PyObject *, PyObject *, PyObject *,
 PyObject *, PyObject *, PyObject *, PyObject *, int, PyObject *);

int PyCode_Addr2Line(PyCodeObject *, int);






typedef struct _addr_pair {
        int ap_lower;
        int ap_upper;
} PyAddrPair;
# 88 "code.h"
int PyCode_CheckLineNumber(PyCodeObject* co,
                                       int lasti, PyAddrPair *bounds);
# 6 "compile.h" 2






struct _node;
PyCodeObject * PyNode_Compile(struct _node *, const char *);



typedef struct {
    int ff_features;
    int ff_lineno;
} PyFutureFeatures;







struct _mod;
PyCodeObject * PyAST_Compile(struct _mod *, const char *,
     PyCompilerFlags *, PyArena *);
PyFutureFeatures * PyFuture_FromAST(struct _mod *, const char *);
# 129 "Python.h" 2
# 1 "eval.h" 1
# 10 "eval.h"
PyObject * PyEval_EvalCode(PyCodeObject *, PyObject *, PyObject *);

PyObject * PyEval_EvalCodeEx(PyCodeObject *co,
     PyObject *globals,
     PyObject *locals,
     PyObject **args, int argc,
     PyObject **kwds, int kwdc,
     PyObject **defs, int defc,
     PyObject *closure);

PyObject * _PyEval_CallTracing(PyObject *func, PyObject *args);
# 130 "Python.h" 2

# 1 "pystrtod.h" 1
# 9 "pystrtod.h"
double PyOS_ascii_strtod(const char *str, char **ptr);
double PyOS_ascii_atof(const char *str);
char * PyOS_ascii_formatd(char *buffer, size_t buf_len, const char *format, double d);
# 132 "Python.h" 2


PyObject* _Py_Mangle(PyObject *p, PyObject *name);
# 151 "Python.h"
# 1 "pyfpe.h" 1
# 152 "Python.h" 2
# 6 "cmathmodule.c" 2
# 14 "cmathmodule.c"
static Py_complex c_one = {1., 0.};
static Py_complex c_half = {0.5, 0.};
static Py_complex c_i = {0., 1.};
static Py_complex c_halfi = {0., 0.5};


static Py_complex c_log(Py_complex);
static Py_complex c_prodi(Py_complex);
static Py_complex c_sqrt(Py_complex);
static PyObject * math_error(void);


static Py_complex
c_acos(Py_complex x)
{
 return _Py_c_neg(c_prodi(c_log(_Py_c_sum(x,_Py_c_prod(c_i,
      c_sqrt(_Py_c_diff(c_one,_Py_c_prod(x,x))))))));
}

static char c_acos_doc[] = "acos(x)\n" "\n" "Return the arc cosine of x.";





static Py_complex
c_acosh(Py_complex x)
{
 Py_complex z;
 z = c_sqrt(c_half);
 z = c_log(_Py_c_prod(z, _Py_c_sum(c_sqrt(_Py_c_sum(x,c_one)),
      c_sqrt(_Py_c_diff(x,c_one)))));
 return _Py_c_sum(z, z);
}

static char c_acosh_doc[] = "acosh(x)\n" "\n" "Return the hyperbolic arccosine of x.";





static Py_complex
c_asin(Py_complex x)
{

 const Py_complex squared = _Py_c_prod(x, x);
 const Py_complex sqrt_1_minus_x_sq = c_sqrt(_Py_c_diff(c_one, squared));
        return _Py_c_neg(c_prodi(c_log(
          _Py_c_sum(sqrt_1_minus_x_sq, c_prodi(x))
      ) ) );
}

static char c_asin_doc[] = "asin(x)\n" "\n" "Return the arc sine of x.";





static Py_complex
c_asinh(Py_complex x)
{
 Py_complex z;
 z = c_sqrt(c_half);
 z = c_log(_Py_c_prod(z, _Py_c_sum(c_sqrt(_Py_c_sum(x, c_i)),
      c_sqrt(_Py_c_diff(x, c_i)))));
 return _Py_c_sum(z, z);
}

static char c_asinh_doc[] = "asinh(x)\n" "\n" "Return the hyperbolic arc sine of x.";





static Py_complex
c_atan(Py_complex x)
{
 return _Py_c_prod(c_halfi,c_log(_Py_c_quot(_Py_c_sum(c_i,x),_Py_c_diff(c_i,x))));
}

static char c_atan_doc[] = "atan(x)\n" "\n" "Return the arc tangent of x.";





static Py_complex
c_atanh(Py_complex x)
{
 return _Py_c_prod(c_half,c_log(_Py_c_quot(_Py_c_sum(c_one,x),_Py_c_diff(c_one,x))));
}

static char c_atanh_doc[] = "atanh(x)\n" "\n" "Return the hyperbolic arc tangent of x.";





static Py_complex
c_cos(Py_complex x)
{
 Py_complex r;
 r.real = cos(x.real)*cosh(x.imag);
 r.imag = -sin(x.real)*sinh(x.imag);
 return r;
}

static char c_cos_doc[] = "cos(x)\n" "n" "Return the cosine of x.";





static Py_complex
c_cosh(Py_complex x)
{
 Py_complex r;
 r.real = cos(x.imag)*cosh(x.real);
 r.imag = sin(x.imag)*sinh(x.real);
 return r;
}

static char c_cosh_doc[] = "cosh(x)\n" "n" "Return the hyperbolic cosine of x.";





static Py_complex
c_exp(Py_complex x)
{
 Py_complex r;
 double l = exp(x.real);
 r.real = l*cos(x.imag);
 r.imag = l*sin(x.imag);
 return r;
}

static char c_exp_doc[] = "exp(x)\n" "\n" "Return the exponential value e**x.";





static Py_complex
c_log(Py_complex x)
{
 Py_complex r;
 double l = hypot(x.real,x.imag);
 r.imag = atan2(x.imag, x.real);
 r.real = log(l);
 return r;
}


static Py_complex
c_log10(Py_complex x)
{
 Py_complex r;
 double l = hypot(x.real,x.imag);
 r.imag = atan2(x.imag, x.real)/log(10.);
 r.real = log10(l);
 return r;
}

static char c_log10_doc[] = "log10(x)\n" "\n" "Return the base-10 logarithm of x.";






static Py_complex
c_prodi(Py_complex x)
{
 Py_complex r;
 r.real = -x.imag;
 r.imag = x.real;
 return r;
}


static Py_complex
c_sin(Py_complex x)
{
 Py_complex r;
 r.real = sin(x.real) * cosh(x.imag);
 r.imag = cos(x.real) * sinh(x.imag);
 return r;
}

static char c_sin_doc[] = "sin(x)\n" "\n" "Return the sine of x.";





static Py_complex
c_sinh(Py_complex x)
{
 Py_complex r;
 r.real = cos(x.imag) * sinh(x.real);
 r.imag = sin(x.imag) * cosh(x.real);
 return r;
}

static char c_sinh_doc[] = "sinh(x)\n" "\n" "Return the hyperbolic sine of x.";





static Py_complex
c_sqrt(Py_complex x)
{
 Py_complex r;
 double s,d;
 if (x.real == 0. && x.imag == 0.)
  r = x;
 else {
  s = sqrt(0.5*(fabs(x.real) + hypot(x.real,x.imag)));
  d = 0.5*x.imag/s;
  if (x.real > 0.) {
   r.real = s;
   r.imag = d;
  }
  else if (x.imag >= 0.) {
   r.real = d;
   r.imag = s;
  }
  else {
   r.real = -d;
   r.imag = -s;
  }
 }
 return r;
}

static char c_sqrt_doc[] = "sqrt(x)\n" "\n" "Return the square root of x.";





static Py_complex
c_tan(Py_complex x)
{
 Py_complex r;
 double sr,cr,shi,chi;
 double rs,is,rc,ic;
 double d;
 sr = sin(x.real);
 cr = cos(x.real);
 shi = sinh(x.imag);
 chi = cosh(x.imag);
 rs = sr * chi;
 is = cr * shi;
 rc = cr * chi;
 ic = -sr * shi;
 d = rc*rc + ic * ic;
 r.real = (rs*rc + is*ic) / d;
 r.imag = (is*rc - rs*ic) / d;
 return r;
}

static char c_tan_doc[] = "tan(x)\n" "\n" "Return the tangent of x.";





static Py_complex
c_tanh(Py_complex x)
{
 Py_complex r;
 double si,ci,shr,chr;
 double rs,is,rc,ic;
 double d;
 si = sin(x.imag);
 ci = cos(x.imag);
 shr = sinh(x.real);
 chr = cosh(x.real);
 rs = ci * shr;
 is = si * chr;
 rc = ci * chr;
 ic = si * shr;
 d = rc*rc + ic*ic;
 r.real = (rs*rc + is*ic) / d;
 r.imag = (is*rc - rs*ic) / d;
 return r;
}

static char c_tanh_doc[] = "tanh(x)\n" "\n" "Return the hyperbolic tangent of x.";




static PyObject *
cmath_log(PyObject *self, PyObject *args)
{
 Py_complex x;
 Py_complex y;

 if (!PyArg_ParseTuple(args, "D|D", &x, &y))
  return ((void *)0);

 (*__error()) = 0;

 x = c_log(x);
 if ((((PyTupleObject *)(args))->ob_size) == 2)
  x = _Py_c_quot(x, c_log(y));

 if ((*__error()) != 0)
  return math_error();
 do { if ((x.real) == __builtin_huge_val() || (x.real) == -__builtin_huge_val() || (x.imag) == __builtin_huge_val() || (x.imag) == -__builtin_huge_val()) { if ((*__error()) == 0) (*__error()) = 34; } else if ((*__error()) == 34) (*__error()) = 0; } while(0);
 return PyComplex_FromCComplex(x);
}

static char cmath_log_doc[] = "log(x[, base]) -> the logarithm of x to the given base.\nIf the base not specified, returns the natural logarithm (base e) of x.";






static PyObject *
math_error(void)
{
 if ((*__error()) == 33)
  PyErr_SetString(PyExc_ValueError, "math domain error");
 else if ((*__error()) == 34)
  PyErr_SetString(PyExc_OverflowError, "math range error");
 else
  PyErr_SetFromErrno(PyExc_ValueError);
 return ((void *)0);
}

static PyObject *
math_1(PyObject *args, Py_complex (*func)(Py_complex))
{
 Py_complex x;
 if (!PyArg_ParseTuple(args, "D", &x))
  return ((void *)0);
 (*__error()) = 0;

 x = (*func)(x);

 do { if ((x.real) == __builtin_huge_val() || (x.real) == -__builtin_huge_val() || (x.imag) == __builtin_huge_val() || (x.imag) == -__builtin_huge_val()) { if ((*__error()) == 0) (*__error()) = 34; } else if ((*__error()) == 34) (*__error()) = 0; } while(0);
 if ((*__error()) != 0)
  return math_error();
 else
  return PyComplex_FromCComplex(x);
}






static PyObject * cmath_acos(PyObject *self, PyObject *args) { return math_1(args, c_acos); }
static PyObject * cmath_acosh(PyObject *self, PyObject *args) { return math_1(args, c_acosh); }
static PyObject * cmath_asin(PyObject *self, PyObject *args) { return math_1(args, c_asin); }
static PyObject * cmath_asinh(PyObject *self, PyObject *args) { return math_1(args, c_asinh); }
static PyObject * cmath_atan(PyObject *self, PyObject *args) { return math_1(args, c_atan); }
static PyObject * cmath_atanh(PyObject *self, PyObject *args) { return math_1(args, c_atanh); }
static PyObject * cmath_cos(PyObject *self, PyObject *args) { return math_1(args, c_cos); }
static PyObject * cmath_cosh(PyObject *self, PyObject *args) { return math_1(args, c_cosh); }
static PyObject * cmath_exp(PyObject *self, PyObject *args) { return math_1(args, c_exp); }
static PyObject * cmath_log10(PyObject *self, PyObject *args) { return math_1(args, c_log10); }
static PyObject * cmath_sin(PyObject *self, PyObject *args) { return math_1(args, c_sin); }
static PyObject * cmath_sinh(PyObject *self, PyObject *args) { return math_1(args, c_sinh); }
static PyObject * cmath_sqrt(PyObject *self, PyObject *args) { return math_1(args, c_sqrt); }
static PyObject * cmath_tan(PyObject *self, PyObject *args) { return math_1(args, c_tan); }
static PyObject * cmath_tanh(PyObject *self, PyObject *args) { return math_1(args, c_tanh); }


static char module_doc[] = "This module is always available. It provides access to mathematical\n" "functions for complex numbers.";



static PyMethodDef cmath_methods[] = {
 {"acos", cmath_acos, 0x0001, c_acos_doc},
 {"acosh", cmath_acosh, 0x0001, c_acosh_doc},
 {"asin", cmath_asin, 0x0001, c_asin_doc},
 {"asinh", cmath_asinh, 0x0001, c_asinh_doc},
 {"atan", cmath_atan, 0x0001, c_atan_doc},
 {"atanh", cmath_atanh, 0x0001, c_atanh_doc},
 {"cos", cmath_cos, 0x0001, c_cos_doc},
 {"cosh", cmath_cosh, 0x0001, c_cosh_doc},
 {"exp", cmath_exp, 0x0001, c_exp_doc},
 {"log", cmath_log, 0x0001, cmath_log_doc},
 {"log10", cmath_log10, 0x0001, c_log10_doc},
 {"sin", cmath_sin, 0x0001, c_sin_doc},
 {"sinh", cmath_sinh, 0x0001, c_sinh_doc},
 {"sqrt", cmath_sqrt, 0x0001, c_sqrt_doc},
 {"tan", cmath_tan, 0x0001, c_tan_doc},
 {"tanh", cmath_tanh, 0x0001, c_tanh_doc},
 {((void *)0), ((void *)0)}
};

void
initcmath(void)
{
 PyObject *m;

 m = Py_InitModule4_64("cmath", cmath_methods, module_doc, (PyObject *)((void *)0), 1013);
 if (m == ((void *)0))
  return;

 PyModule_AddObject(m, "pi",
                           PyFloat_FromDouble(atan(1.0) * 4.0));
 PyModule_AddObject(m, "e", PyFloat_FromDouble(exp(1.0)));
}
