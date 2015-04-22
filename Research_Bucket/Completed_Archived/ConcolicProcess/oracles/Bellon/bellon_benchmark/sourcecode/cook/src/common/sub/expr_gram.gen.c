/*  A Bison parser, made from common/sub/expr_gram.y with Bison version GNU Bison version 1.22
  */
#define sub_expr_gram_BISON 1  /* Identify Bison output.  */
#define        DIV    258
#define        JUNK   259
#define        LP     260
#define        MINUS  261
#define        MUL    262
#define        NUMBER 263
#define        PLUS   264
#define        RP     265
#define        UNARY  266
#include <ac/stdarg.h>
#include <ac/stdio.h>
#include <str.h>
#include <sub/expr_gram.h>
#include <sub/expr_lex.h>
#include <sub/private.h>
#include <trace.h>
#ifdef DEBUG
#define sub_expr_gram_DEBUG 1
#endif
typedef union {
       long  lv_number;
} sub_expr_gram_STYPE;
static long       result;
static sub_context_ty  *scp;
string_ty *
sub_expr_gram(p, s)
       sub_context_ty        *p;
       string_ty     *s; {
       int     bad;
       extern int sub_expr_gram_parse _((void));
#ifdef DEBUG
        extern int sub_expr_gram_debug;
#endif
       trace(("sub_expr_gram()\n{\n"/*}*/));
       scp = p;
#ifdef DEBUG
       sub_expr_gram_debug = trace_pretest_;
#endif
       sub_expr_lex_open(s);
       bad = sub_expr_gram_parse();
       sub_expr_lex_close();
       trace(("bad = %d\n", bad));
       scp = 0;
       trace((/*{*/"}\n"));
       if (bad)
             return 0;
       return str_format("%ld", result); }
void sub_expr_gram_error _((char *));
void
sub_expr_gram_error(s)
       char  *s; {
       trace(("sub_expr_gram_error(\"%s\")\n{\n", s));
       sub_context_error_set(scp, s);
       trace((/*{*/"}\n")); }
#ifdef DEBUG
/*
 * jiggery-pokery for yacc
 *
 *     Replace all calls to printf with a call to trace_printf.  The
 *     trace_where_ is needed to set the location, and is safe, because
 *     yacc only invokes the printf with an if (be careful, the printf
 *     is not in a compound statement).
 */
#define printf trace_where_, trace_printf
/*
 * jiggery-pokery for bison
 *
 *     Replace all calls to fprintf with a call to sub_expr_gram_debugger.  Ignore
 *     the first argument, it will be ``stderr''.  The trace_where_ is
 *     needed to set the location, and is safe, because bison only
 *     invokes the printf with an if (be careful, the fprintf is not in
 *     a compound statement).
 */
#define fprintf trace_where_, sub_expr_gram_debugger
void sub_expr_gram_debugger _((void *, char *, ...));
void
sub_expr_gram_debugger(junk, fmt sva_last)
       void   *junk;
       char   *fmt;
       sva_last_decl {
       va_list             ap;
       string_ty     *s;
       sva_init(ap, fmt);
       s = str_vformat(fmt, ap);
       va_end(ap);
       trace_printf("%s", s->str_text);
       str_free(s); }
#endif
#ifndef sub_expr_gram_LTYPE
typedef
  struct sub_expr_gram_ltype {
      int timestamp;
      int first_line;
      int first_column;
      int last_line;
      int last_column;
      char *text; }
  sub_expr_gram_ltype;
#define sub_expr_gram_LTYPE sub_expr_gram_ltype
#endif
#ifndef __cplusplus
#ifndef __STDC__
#define const
#endif
#endif
#define        sub_expr_gram_FINAL       18
#define        sub_expr_gram_FLAG         -32768
#define        sub_expr_gram_NTBASE   12
#define sub_expr_gram_TRANSLATE(x) ((unsigned)(x) <= 266 ? sub_expr_gram_translate[x] : 14)
static const char sub_expr_gram_translate[] = {     0,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     1,     2,     3,     4,     5,
     6,     7,     8,     9,    10,    11
};
#if sub_expr_gram_DEBUG != 0
static const short sub_expr_gram_prhs[] = {     0,
     0,     2,     6,     8,    11,    15,    19,    23
};
static const short sub_expr_gram_rhs[] = {    13,
     0,     5,    13,    10,     0,     8,     0,     6,    13,     0,
    13,     9,    13,     0,    13,     6,    13,     0,    13,     7,
    13,     0,    13,     3,    13,     0
};
#endif
#if sub_expr_gram_DEBUG != 0
static const short sub_expr_gram_rline[] = { 0,
   153,   159,   161,   163,   166,   168,   170,   172
};
static const char * const sub_expr_gram_tname[] = {   "$","error","$illegal.","DIV","JUNK",
"LP","MINUS","MUL","NUMBER","PLUS","RP","UNARY","grammar","expr",""
};
#endif
static const short sub_expr_gram_r1[] = {     0,
    12,    13,    13,    13,    13,    13,    13,    13
};
static const short sub_expr_gram_r2[] = {     0,
     1,     3,     1,     2,     3,     3,     3,     3
};
static const short sub_expr_gram_defact[] = {     0,
     0,     0,     3,     1,     0,     4,     0,     0,     0,     0,
     2,     8,     6,     7,     5,     0,     0,     0
};
static const short sub_expr_gram_defgoto[] = {    16,
     4
};
static const short sub_expr_gram_pact[] = {    -3,
    -3,    -3,-32768,    12,     7,-32768,    -3,    -3,    -3,    -3,
-32768,-32768,    17,-32768,    17,     4,    11,-32768
};
static const short sub_expr_gram_pgoto[] = {-32768,
    -1
};
#define        sub_expr_gram_LAST         24
static const short sub_expr_gram_table[] = {     5,
     6,     1,     2,    17,     3,    12,    13,    14,    15,     7,
    18,     0,     8,     9,     7,    10,    11,     8,     9,     7,
    10,     0,     0,     9
};
static const short sub_expr_gram_check[] = {     1,
     2,     5,     6,     0,     8,     7,     8,     9,    10,     3,
     0,    -1,     6,     7,     3,     9,    10,     6,     7,     3,
     9,    -1,    -1,     7
};
/* -*-C-*-  Note some compilers choke on comments on `#line' lines.  */
/* Skeleton output parser for bison,
   Copyright (C) 1984, 1989, 1990 Bob Corbett and Richard Stallman
   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 1, or (at your option)
   any later version.
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.  */
#ifndef alloca
#ifdef __GNUC__
#define alloca __builtin_alloca
#else /* not GNU C.  */
#if (!defined (__STDC__) && defined (sparc)) || defined (__sparc__) || defined (__sparc) || defined (__sgi)
#include <alloca.h>
#else /* not sparc */
#if defined (MSDOS) && !defined (__TURBOC__)
#include <malloc.h>
#else /* not MSDOS, or __TURBOC__ */
#if defined(_AIX)
#include <malloc.h>
 #pragma alloca
#else /* not MSDOS, __TURBOC__, or _AIX */
#ifdef __hpux
#ifdef __cplusplus
extern "C" {
void *alloca (unsigned int);
};
#else /* not __cplusplus */
void *alloca (unsigned int);
#endif /* not __cplusplus */
#endif /* __hpux */
#endif /* not _AIX */
#endif /* not MSDOS, or __TURBOC__ */
#endif /* not sparc.  */
#endif /* not GNU C.  */
#endif /* alloca not defined.  */
/* This is the parser code that is written into each bison parser
  when the %semantic_parser declaration is not specified in the grammar.
  It was written by Richard Stallman by simplifying the hairy parser
  used when %semantic_parser is specified.  */
/* Note: there must be only one dollar sign in this file.
   It is replaced by the list of actions, each action
   as one case of the switch.  */
#define sub_expr_gram_errok       (sub_expr_gram_errstatus = 0)
#define sub_expr_gram_clearin  (sub_expr_gram_char = sub_expr_gram_EMPTY)
#define sub_expr_gram_EMPTY       -2
#define sub_expr_gram_EOF           0
#define sub_expr_gram_ACCEPT   return(0)
#define sub_expr_gram_ABORT    return(1)
#define sub_expr_gram_ERROR       goto sub_expr_gram_errlab1
/* Like sub_expr_gram_ERROR except do call sub_expr_gram_error.
   This remains here temporarily to ease the
   transition to the new meaning of sub_expr_gram_ERROR, for GCC.
   Once GCC version 2 has supplanted version 1, this can go.  */
#define sub_expr_gram_FAIL         goto sub_expr_gram_errlab
#define sub_expr_gram_RECOVERING()  (!!sub_expr_gram_errstatus)
#define sub_expr_gram_BACKUP(token, value) \
do                                    \
  if (sub_expr_gram_char == sub_expr_gram_EMPTY && sub_expr_gram_len == 1)              \
    { sub_expr_gram_char = (token), sub_expr_gram_lval = (value);               \
      sub_expr_gram_char1 = sub_expr_gram_TRANSLATE (sub_expr_gram_char);                  \
      sub_expr_gram_POPSTACK;                             \
      goto sub_expr_gram_backup;                           \
    }                                    \
  else                                    \
    { sub_expr_gram_error ("syntax error: cannot back up"); sub_expr_gram_ERROR; }     \
while (0)
#define sub_expr_gram_TERROR   1
#define sub_expr_gram_ERRCODE  256
#ifndef sub_expr_gram_PURE
#define sub_expr_gram_LEX           sub_expr_gram_lex()
#endif
#ifdef sub_expr_gram_PURE
#ifdef sub_expr_gram_LSP_NEEDED
#define sub_expr_gram_LEX           sub_expr_gram_lex(&sub_expr_gram_lval, &sub_expr_gram_lloc)
#else
#define sub_expr_gram_LEX           sub_expr_gram_lex(&sub_expr_gram_lval)
#endif
#endif
/* If nonreentrant, generate the variables here */
#ifndef sub_expr_gram_PURE
int    sub_expr_gram_char;                     /*  the lookahead symbol         */
sub_expr_gram_STYPE    sub_expr_gram_lval;                     /*  the semantic value of the               */
                      /*  lookahead symbol                  */
#ifdef sub_expr_gram_LSP_NEEDED
sub_expr_gram_LTYPE sub_expr_gram_lloc;                     /*  location data for the lookahead  */
                      /*  symbol                      */
#endif
int sub_expr_gram_nerrs;                  /*  number of parse errors so far       */
#endif  /* not sub_expr_gram_PURE */
#if sub_expr_gram_DEBUG != 0
int sub_expr_gram_debug;                  /*  nonzero means print parse trace */
/* Since this is uninitialized, it does not stop multiple parsers
   from coexisting.  */
#endif
/*  sub_expr_gram_INITDEPTH indicates the initial size of the parser's stacks  */
#ifndef        sub_expr_gram_INITDEPTH
#define sub_expr_gram_INITDEPTH 200
#endif
/*  sub_expr_gram_MAXDEPTH is the maximum size the stacks can grow to
    (effective only if the built-in stack extension method is used).  */
#if sub_expr_gram_MAXDEPTH == 0
#undef sub_expr_gram_MAXDEPTH
#endif
#ifndef sub_expr_gram_MAXDEPTH
#define sub_expr_gram_MAXDEPTH 10000
#endif
/* Prevent warning if -Wstrict-prototypes.  */
#ifdef __GNUC__
int sub_expr_gram_parse (void);
#endif
#if __GNUC__ > 1             /* GNU C and GNU C++ define this.  */
#define __sub_expr_gram__bcopy(FROM,TO,COUNT)  __builtin_memcpy(TO,FROM,COUNT)
#else                  /* not GNU C or C++ */
#ifndef __cplusplus
/* This is the most reliable way to avoid incompatibilities
   in available built-in functions on various systems.  */
static void
__sub_expr_gram__bcopy (from, to, count)
     char *from;
     char *to;
     int count; {
  register char *f = from;
  register char *t = to;
  register int i = count;
  while (i-- > 0)
    *t++ = *f++; }
#else /* __cplusplus */
/* This is the most reliable way to avoid incompatibilities
   in available built-in functions on various systems.  */
static void
__sub_expr_gram__bcopy (char *from, char *to, int count) {
  register char *f = from;
  register char *t = to;
  register int i = count;
  while (i-- > 0)
    *t++ = *f++; }
#endif
#endif
int
sub_expr_gram_parse() {
  register int sub_expr_gram_state;
  register int sub_expr_gram_n;
  register short *sub_expr_gram_ssp;
  register sub_expr_gram_STYPE *sub_expr_gram_vsp;
  int sub_expr_gram_errstatus; /*  number of tokens to shift before error messages enabled */
  int sub_expr_gram_char1;         /*  lookahead token as an internal (translated) token number */
  short        sub_expr_gram_ssa[sub_expr_gram_INITDEPTH];    /*  the state stack                     */
  sub_expr_gram_STYPE sub_expr_gram_vsa[sub_expr_gram_INITDEPTH];      /*  the semantic value stack         */
  short *sub_expr_gram_ss = sub_expr_gram_ssa;         /*  refer to the stacks thru separate pointers */
  sub_expr_gram_STYPE *sub_expr_gram_vs = sub_expr_gram_vsa;   /*  to allow sub_expr_gram_overflow to reallocate them elsewhere */
#ifdef sub_expr_gram_LSP_NEEDED
  sub_expr_gram_LTYPE sub_expr_gram_lsa[sub_expr_gram_INITDEPTH];      /*  the location stack                  */
  sub_expr_gram_LTYPE *sub_expr_gram_ls = sub_expr_gram_lsa;
  sub_expr_gram_LTYPE *sub_expr_gram_lsp;
#define sub_expr_gram_POPSTACK   (sub_expr_gram_vsp--, sub_expr_gram_ssp--, sub_expr_gram_lsp--)
#else
#define sub_expr_gram_POPSTACK   (sub_expr_gram_vsp--, sub_expr_gram_ssp--)
#endif
  int sub_expr_gram_stacksize = sub_expr_gram_INITDEPTH;
#ifdef sub_expr_gram_PURE
  int sub_expr_gram_char;
  sub_expr_gram_STYPE sub_expr_gram_lval;
  int sub_expr_gram_nerrs;
#ifdef sub_expr_gram_LSP_NEEDED
  sub_expr_gram_LTYPE sub_expr_gram_lloc;
#endif
#endif
  sub_expr_gram_STYPE sub_expr_gram_val;             /*  the variable used to return           */
                      /*  semantic values from the action        */
                      /*  routines                      */
  int sub_expr_gram_len;
#if sub_expr_gram_DEBUG != 0
  if (sub_expr_gram_debug)
    fprintf(stderr, "Starting parse\n");
#endif
  sub_expr_gram_state = 0;
  sub_expr_gram_errstatus = 0;
  sub_expr_gram_nerrs = 0;
  sub_expr_gram_char = sub_expr_gram_EMPTY;       /* Cause a token to be read.  */
  /* Initialize stack pointers.
     Waste one element of value and location stack
     so that they stay on the same level as the state stack.
     The wasted elements are never initialized.  */
  sub_expr_gram_ssp = sub_expr_gram_ss - 1;
  sub_expr_gram_vsp = sub_expr_gram_vs;
#ifdef sub_expr_gram_LSP_NEEDED
  sub_expr_gram_lsp = sub_expr_gram_ls;
#endif
/* Push a new state, which is found in  sub_expr_gram_state  .  */
/* In all cases, when you get here, the value and location stacks
   have just been pushed. so pushing a state here evens the stacks.  */
sub_expr_gram_newstate:
  *++sub_expr_gram_ssp = sub_expr_gram_state;
  if (sub_expr_gram_ssp >= sub_expr_gram_ss + sub_expr_gram_stacksize - 1) {
      /* Give user a chance to reallocate the stack */
      /* Use copies of these so that the &'s don't force the real ones into memory. */
      sub_expr_gram_STYPE *sub_expr_gram_vs1 = sub_expr_gram_vs;
      short *sub_expr_gram_ss1 = sub_expr_gram_ss;
#ifdef sub_expr_gram_LSP_NEEDED
      sub_expr_gram_LTYPE *sub_expr_gram_ls1 = sub_expr_gram_ls;
#endif
      /* Get the current used size of the three stacks, in elements.  */
      int size = sub_expr_gram_ssp - sub_expr_gram_ss + 1;
#ifdef sub_expr_gram_overflow
      /* Each stack pointer address is followed by the size of
        the data in use in that stack, in bytes.  */
      sub_expr_gram_overflow("parser stack overflow",
              &sub_expr_gram_ss1, size * sizeof (*sub_expr_gram_ssp),
              &sub_expr_gram_vs1, size * sizeof (*sub_expr_gram_vsp),
#ifdef sub_expr_gram_LSP_NEEDED
              &sub_expr_gram_ls1, size * sizeof (*sub_expr_gram_lsp),
#endif
              &sub_expr_gram_stacksize);
      sub_expr_gram_ss = sub_expr_gram_ss1; sub_expr_gram_vs = sub_expr_gram_vs1;
#ifdef sub_expr_gram_LSP_NEEDED
      sub_expr_gram_ls = sub_expr_gram_ls1;
#endif
#else /* no sub_expr_gram_overflow */
      /* Extend the stack our own way.  */
      if (sub_expr_gram_stacksize >= sub_expr_gram_MAXDEPTH) {
         sub_expr_gram_error("parser stack overflow");
         return 2; }
      sub_expr_gram_stacksize *= 2;
      if (sub_expr_gram_stacksize > sub_expr_gram_MAXDEPTH)
       sub_expr_gram_stacksize = sub_expr_gram_MAXDEPTH;
      sub_expr_gram_ss = (short *) alloca (sub_expr_gram_stacksize * sizeof (*sub_expr_gram_ssp));
      __sub_expr_gram__bcopy ((char *)sub_expr_gram_ss1, (char *)sub_expr_gram_ss, size * sizeof (*sub_expr_gram_ssp));
      sub_expr_gram_vs = (sub_expr_gram_STYPE *) alloca (sub_expr_gram_stacksize * sizeof (*sub_expr_gram_vsp));
      __sub_expr_gram__bcopy ((char *)sub_expr_gram_vs1, (char *)sub_expr_gram_vs, size * sizeof (*sub_expr_gram_vsp));
#ifdef sub_expr_gram_LSP_NEEDED
      sub_expr_gram_ls = (sub_expr_gram_LTYPE *) alloca (sub_expr_gram_stacksize * sizeof (*sub_expr_gram_lsp));
      __sub_expr_gram__bcopy ((char *)sub_expr_gram_ls1, (char *)sub_expr_gram_ls, size * sizeof (*sub_expr_gram_lsp));
#endif
#endif /* no sub_expr_gram_overflow */
      sub_expr_gram_ssp = sub_expr_gram_ss + size - 1;
      sub_expr_gram_vsp = sub_expr_gram_vs + size - 1;
#ifdef sub_expr_gram_LSP_NEEDED
      sub_expr_gram_lsp = sub_expr_gram_ls + size - 1;
#endif
#if sub_expr_gram_DEBUG != 0
      if (sub_expr_gram_debug)
       fprintf(stderr, "Stack size increased to %d\n", sub_expr_gram_stacksize);
#endif
      if (sub_expr_gram_ssp >= sub_expr_gram_ss + sub_expr_gram_stacksize - 1)
       sub_expr_gram_ABORT; }
#if sub_expr_gram_DEBUG != 0
  if (sub_expr_gram_debug)
    fprintf(stderr, "Entering state %d\n", sub_expr_gram_state);
#endif
  goto sub_expr_gram_backup;
 sub_expr_gram_backup:
/* Do appropriate processing given the current state.  */
/* Read a lookahead token if we need one and don't already have one.  */
/* sub_expr_gram_resume: */
  /* First try to decide what to do without reference to lookahead token.  */
  sub_expr_gram_n = sub_expr_gram_pact[sub_expr_gram_state];
  if (sub_expr_gram_n == sub_expr_gram_FLAG)
    goto sub_expr_gram_default;
  /* Not known => get a lookahead token if don't already have one.  */
  /* sub_expr_gram_char is either sub_expr_gram_EMPTY or sub_expr_gram_EOF
     or a valid token in external form.  */
  if (sub_expr_gram_char == sub_expr_gram_EMPTY) {
#if sub_expr_gram_DEBUG != 0
      if (sub_expr_gram_debug)
       fprintf(stderr, "Reading a token: ");
#endif
      sub_expr_gram_char = sub_expr_gram_LEX; }
  /* Convert token to internal form (in sub_expr_gram_char1) for indexing tables with */
  if (sub_expr_gram_char <= 0)         /* This means end of input. */ {
      sub_expr_gram_char1 = 0;
      sub_expr_gram_char = sub_expr_gram_EOF;   /* Don't call sub_expr_gram_LEX any more */
#if sub_expr_gram_DEBUG != 0
      if (sub_expr_gram_debug)
       fprintf(stderr, "Now at end of input.\n");
#endif
    }
  else {
      sub_expr_gram_char1 = sub_expr_gram_TRANSLATE(sub_expr_gram_char);
#if sub_expr_gram_DEBUG != 0
      if (sub_expr_gram_debug) {
         fprintf (stderr, "Next token is %d (%s", sub_expr_gram_char, sub_expr_gram_tname[sub_expr_gram_char1]);
         /* Give the individual parser a way to print the precise meaning
            of a token, for further debugging info.  */
#ifdef sub_expr_gram_PRINT
         sub_expr_gram_PRINT (stderr, sub_expr_gram_char, sub_expr_gram_lval);
#endif
         fprintf (stderr, ")\n"); }
#endif
    }
  sub_expr_gram_n += sub_expr_gram_char1;
  if (sub_expr_gram_n < 0 || sub_expr_gram_n > sub_expr_gram_LAST || sub_expr_gram_check[sub_expr_gram_n] != sub_expr_gram_char1)
    goto sub_expr_gram_default;
  sub_expr_gram_n = sub_expr_gram_table[sub_expr_gram_n];
  /* sub_expr_gram_n is what to do for this token type in this state.
     Negative => reduce, -sub_expr_gram_n is rule number.
     Positive => shift, sub_expr_gram_n is new state.
       New state is final state => don't bother to shift,
       just return success.
     0, or most negative number => error.  */
  if (sub_expr_gram_n < 0) {
      if (sub_expr_gram_n == sub_expr_gram_FLAG)
       goto sub_expr_gram_errlab;
      sub_expr_gram_n = -sub_expr_gram_n;
      goto sub_expr_gram_reduce; }
  else if (sub_expr_gram_n == 0)
    goto sub_expr_gram_errlab;
  if (sub_expr_gram_n == sub_expr_gram_FINAL)
    sub_expr_gram_ACCEPT;
  /* Shift the lookahead token.  */
#if sub_expr_gram_DEBUG != 0
  if (sub_expr_gram_debug)
    fprintf(stderr, "Shifting token %d (%s), ", sub_expr_gram_char, sub_expr_gram_tname[sub_expr_gram_char1]);
#endif
  /* Discard the token being shifted unless it is eof.  */
  if (sub_expr_gram_char != sub_expr_gram_EOF)
    sub_expr_gram_char = sub_expr_gram_EMPTY;
  *++sub_expr_gram_vsp = sub_expr_gram_lval;
#ifdef sub_expr_gram_LSP_NEEDED
  *++sub_expr_gram_lsp = sub_expr_gram_lloc;
#endif
  /* count tokens shifted since error; after three, turn off error status.  */
  if (sub_expr_gram_errstatus) sub_expr_gram_errstatus--;
  sub_expr_gram_state = sub_expr_gram_n;
  goto sub_expr_gram_newstate;
/* Do the default action for the current state.  */
sub_expr_gram_default:
  sub_expr_gram_n = sub_expr_gram_defact[sub_expr_gram_state];
  if (sub_expr_gram_n == 0)
    goto sub_expr_gram_errlab;
/* Do a reduction.  sub_expr_gram_n is the number of a rule to reduce with.  */
sub_expr_gram_reduce:
  sub_expr_gram_len = sub_expr_gram_r2[sub_expr_gram_n];
  sub_expr_gram_val = sub_expr_gram_vsp[1-sub_expr_gram_len]; /* implement default value of the action */
#if sub_expr_gram_DEBUG != 0
  if (sub_expr_gram_debug) {
      int i;
      fprintf (stderr, "Reducing via rule %d (line %d), ",
              sub_expr_gram_n, sub_expr_gram_rline[sub_expr_gram_n]);
      /* Print the symbols being reduced, and their result.  */
      for (i = sub_expr_gram_prhs[sub_expr_gram_n]; sub_expr_gram_rhs[i] > 0; i++)
       fprintf (stderr, "%s ", sub_expr_gram_tname[sub_expr_gram_rhs[i]]);
      fprintf (stderr, " -> %s\n", sub_expr_gram_tname[sub_expr_gram_r1[sub_expr_gram_n]]); }
#endif
  switch (sub_expr_gram_n) {
case 1:
{ result = sub_expr_gram_vsp[0].lv_number; ;
    break;}
case 2:
{ sub_expr_gram_val.lv_number = sub_expr_gram_vsp[-1].lv_number; trace(("$$ = %ld;\n", sub_expr_gram_val.lv_number)); ;
    break;}
case 3:
{ sub_expr_gram_val.lv_number = sub_expr_gram_vsp[0].lv_number; trace(("$$ = %ld;\n", sub_expr_gram_val.lv_number)); ;
    break;}
case 4:
{ sub_expr_gram_val.lv_number = -sub_expr_gram_vsp[0].lv_number; trace(("$$ = %ld;\n", sub_expr_gram_val.lv_number)); ;
    break;}
case 5:
{ sub_expr_gram_val.lv_number = sub_expr_gram_vsp[-2].lv_number + sub_expr_gram_vsp[0].lv_number; trace(("$$ = %ld;\n", sub_expr_gram_val.lv_number)); ;
    break;}
case 6:
{ sub_expr_gram_val.lv_number = sub_expr_gram_vsp[-2].lv_number - sub_expr_gram_vsp[0].lv_number; trace(("$$ = %ld;\n", sub_expr_gram_val.lv_number)); ;
    break;}
case 7:
{ sub_expr_gram_val.lv_number = sub_expr_gram_vsp[-2].lv_number * sub_expr_gram_vsp[0].lv_number; trace(("$$ = %ld;\n", sub_expr_gram_val.lv_number)); ;
    break;}
case 8:
{ sub_expr_gram_val.lv_number = sub_expr_gram_vsp[0].lv_number ? sub_expr_gram_vsp[-2].lv_number / sub_expr_gram_vsp[0].lv_number : 0; trace(("$$ = %ld;\n", sub_expr_gram_val.lv_number)); ;
    break;} }
   /* the action file gets copied in in place of this dollarsign */
  sub_expr_gram_vsp -= sub_expr_gram_len;
  sub_expr_gram_ssp -= sub_expr_gram_len;
#ifdef sub_expr_gram_LSP_NEEDED
  sub_expr_gram_lsp -= sub_expr_gram_len;
#endif
#if sub_expr_gram_DEBUG != 0
  if (sub_expr_gram_debug) {
      short *ssp1 = sub_expr_gram_ss - 1;
      fprintf (stderr, "state stack now");
      while (ssp1 != sub_expr_gram_ssp)
       fprintf (stderr, " %d", *++ssp1);
      fprintf (stderr, "\n"); }
#endif
  *++sub_expr_gram_vsp = sub_expr_gram_val;
#ifdef sub_expr_gram_LSP_NEEDED
  sub_expr_gram_lsp++;
  if (sub_expr_gram_len == 0) {
      sub_expr_gram_lsp->first_line = sub_expr_gram_lloc.first_line;
      sub_expr_gram_lsp->first_column = sub_expr_gram_lloc.first_column;
      sub_expr_gram_lsp->last_line = (sub_expr_gram_lsp-1)->last_line;
      sub_expr_gram_lsp->last_column = (sub_expr_gram_lsp-1)->last_column;
      sub_expr_gram_lsp->text = 0; }
  else {
      sub_expr_gram_lsp->last_line = (sub_expr_gram_lsp+sub_expr_gram_len-1)->last_line;
      sub_expr_gram_lsp->last_column = (sub_expr_gram_lsp+sub_expr_gram_len-1)->last_column; }
#endif
  /* Now "shift" the result of the reduction.
     Determine what state that goes to,
     based on the state we popped back to
     and the rule number reduced by.  */
  sub_expr_gram_n = sub_expr_gram_r1[sub_expr_gram_n];
  sub_expr_gram_state = sub_expr_gram_pgoto[sub_expr_gram_n - sub_expr_gram_NTBASE] + *sub_expr_gram_ssp;
  if (sub_expr_gram_state >= 0 && sub_expr_gram_state <= sub_expr_gram_LAST && sub_expr_gram_check[sub_expr_gram_state] == *sub_expr_gram_ssp)
    sub_expr_gram_state = sub_expr_gram_table[sub_expr_gram_state];
  else
    sub_expr_gram_state = sub_expr_gram_defgoto[sub_expr_gram_n - sub_expr_gram_NTBASE];
  goto sub_expr_gram_newstate;
sub_expr_gram_errlab:   /* here on detecting error */
  if (! sub_expr_gram_errstatus)
    /* If not already recovering from an error, report this error.  */ {
      ++sub_expr_gram_nerrs;
#ifdef sub_expr_gram_ERROR_VERBOSE
      sub_expr_gram_n = sub_expr_gram_pact[sub_expr_gram_state];
      if (sub_expr_gram_n > sub_expr_gram_FLAG && sub_expr_gram_n < sub_expr_gram_LAST) {
         int size = 0;
         char *msg;
         int x, count;
         count = 0;
         /* Start X at -sub_expr_gram_n if nec to avoid negative indexes in sub_expr_gram_check.  */
         for (x = (sub_expr_gram_n < 0 ? -sub_expr_gram_n : 0);
              x < (sizeof(sub_expr_gram_tname) / sizeof(char *)); x++)
           if (sub_expr_gram_check[x + sub_expr_gram_n] == x)
             size += strlen(sub_expr_gram_tname[x]) + 15, count++;
         msg = (char *) malloc(size + 15);
         if (msg != 0) {
             strcpy(msg, "parse error");
             if (count < 5) {
               count = 0;
               for (x = (sub_expr_gram_n < 0 ? -sub_expr_gram_n : 0);
                    x < (sizeof(sub_expr_gram_tname) / sizeof(char *)); x++)
                 if (sub_expr_gram_check[x + sub_expr_gram_n] == x) {
                  strcat(msg, count == 0 ? ", expecting `" : " or `");
                  strcat(msg, sub_expr_gram_tname[x]);
                  strcat(msg, "'");
                  count++; } }
             sub_expr_gram_error(msg);
             free(msg); }
         else
           sub_expr_gram_error ("parse error; also virtual memory exceeded"); }
      else
#endif /* sub_expr_gram_ERROR_VERBOSE */
       sub_expr_gram_error("parse error"); }
  goto sub_expr_gram_errlab1;
sub_expr_gram_errlab1:   /* here on error raised explicitly by an action */
  if (sub_expr_gram_errstatus == 3) {
      /* if just tried and failed to reuse lookahead token after an error, discard it.  */
      /* return failure if at end of input */
      if (sub_expr_gram_char == sub_expr_gram_EOF)
       sub_expr_gram_ABORT;
#if sub_expr_gram_DEBUG != 0
      if (sub_expr_gram_debug)
       fprintf(stderr, "Discarding token %d (%s).\n", sub_expr_gram_char, sub_expr_gram_tname[sub_expr_gram_char1]);
#endif
      sub_expr_gram_char = sub_expr_gram_EMPTY; }
  /* Else will try to reuse lookahead token
     after shifting the error token.  */
  sub_expr_gram_errstatus = 3;         /* Each real token shifted decrements this */
  goto sub_expr_gram_errhandle;
sub_expr_gram_errdefault:  /* current state does not do anything special for the error token. */
#if 0
  /* This is wrong; only states that explicitly want error tokens
     should shift them.  */
  sub_expr_gram_n = sub_expr_gram_defact[sub_expr_gram_state];  /* If its default is to accept any token, ok.  Otherwise pop it.*/
  if (sub_expr_gram_n) goto sub_expr_gram_default;
#endif
sub_expr_gram_errpop:   /* pop the current state because it cannot handle the error token */
  if (sub_expr_gram_ssp == sub_expr_gram_ss) sub_expr_gram_ABORT;
  sub_expr_gram_vsp--;
  sub_expr_gram_state = *--sub_expr_gram_ssp;
#ifdef sub_expr_gram_LSP_NEEDED
  sub_expr_gram_lsp--;
#endif
#if sub_expr_gram_DEBUG != 0
  if (sub_expr_gram_debug) {
      short *ssp1 = sub_expr_gram_ss - 1;
      fprintf (stderr, "Error: state stack now");
      while (ssp1 != sub_expr_gram_ssp)
       fprintf (stderr, " %d", *++ssp1);
      fprintf (stderr, "\n"); }
#endif
sub_expr_gram_errhandle:
  sub_expr_gram_n = sub_expr_gram_pact[sub_expr_gram_state];
  if (sub_expr_gram_n == sub_expr_gram_FLAG)
    goto sub_expr_gram_errdefault;
  sub_expr_gram_n += sub_expr_gram_TERROR;
  if (sub_expr_gram_n < 0 || sub_expr_gram_n > sub_expr_gram_LAST || sub_expr_gram_check[sub_expr_gram_n] != sub_expr_gram_TERROR)
    goto sub_expr_gram_errdefault;
  sub_expr_gram_n = sub_expr_gram_table[sub_expr_gram_n];
  if (sub_expr_gram_n < 0) {
      if (sub_expr_gram_n == sub_expr_gram_FLAG)
       goto sub_expr_gram_errpop;
      sub_expr_gram_n = -sub_expr_gram_n;
      goto sub_expr_gram_reduce; }
  else if (sub_expr_gram_n == 0)
    goto sub_expr_gram_errpop;
  if (sub_expr_gram_n == sub_expr_gram_FINAL)
    sub_expr_gram_ACCEPT;
#if sub_expr_gram_DEBUG != 0
  if (sub_expr_gram_debug)
    fprintf(stderr, "Shifting error token, ");
#endif
  *++sub_expr_gram_vsp = sub_expr_gram_lval;
#ifdef sub_expr_gram_LSP_NEEDED
  *++sub_expr_gram_lsp = sub_expr_gram_lloc;
#endif
  sub_expr_gram_state = sub_expr_gram_n;
  goto sub_expr_gram_newstate; }
