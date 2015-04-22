/*  A Bison parser, made from cook/fingerprint/gram.y with Bison version GNU Bison version 1.22
  */
#define fingerprint_gram_BISON 1  /* Identify Bison output.  */
#define        STRING 258
#define        JUNK   259
#define        NUMBER 260
#define        EQ     261
#define        LB     262
#define        RB     263
#include <fingerprint/find.h>
#include <fingerprint/gram.h>
#include <fingerprint/lex.h>
#include <fingerprint/subdir.h>
#include <fingerprint/value.h>
#include <str.h>
#include <trace.h>
typedef union {
       string_ty     *lv_string;
       long   lv_number;
       struct {
             long lhs;
             long rhs; }
                  lv_number_pair;
       struct {
             string_ty *lhs;
             string_ty *rhs; }
                  lv_string_pair;
} fingerprint_gram_STYPE;
static fp_subdir_ty *subdir;
/*
 * NAME
 *     fp_gram
 *
 * SYNOPSIS
 *     void fp_gram(fp_subdir_ty *sdp, string_ty *filename);
 *
 * DESCRIPTION
 *     The fp_gram function is used to read the fingerprint cache of a
 *     directory.  Fingerprints are remembered ralative to the directory
 *     they are stored in, so recursive cook usage and search_list Cook
 *     usage, are all able to take advantage of the fingerprint caches.
 *
 *     The fp_find_update function is called by the grammar parser,
 *     to update both the specified sub-directory structure, but also
 *     the master symbol table.  (The master symbol table permits O(1)
 *     access to allpaths, once they are known.)
 */
void
fp_gram(sdp, filename)
       fp_subdir_ty  *sdp;
       string_ty     *filename; {
       extern int    fingerprint_gram_parse _((void));
       subdir = sdp;
       fingerprint_lex_open(filename);
       fingerprint_gram_parse();
       fingerprint_lex_close();
       subdir = 0; }
#ifndef fingerprint_gram_LTYPE
typedef
  struct fingerprint_gram_ltype {
      int timestamp;
      int first_line;
      int first_column;
      int last_line;
      int last_column;
      char *text; }
  fingerprint_gram_ltype;
#define fingerprint_gram_LTYPE fingerprint_gram_ltype
#endif
#ifndef __cplusplus
#ifndef __STDC__
#define const
#endif
#endif
#define        fingerprint_gram_FINAL         15
#define        fingerprint_gram_FLAG   -32768
#define        fingerprint_gram_NTBASE        9
#define fingerprint_gram_TRANSLATE(x) ((unsigned)(x) <= 263 ? fingerprint_gram_translate[x] : 13)
static const char fingerprint_gram_translate[] = {     0,
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
     6,     7,     8
};
#if fingerprint_gram_DEBUG != 0
static const short fingerprint_gram_prhs[] = {     0,
     0,     1,     4,    11,    13,    15,    18,    20
};
static const short fingerprint_gram_rhs[] = {    -1,
     9,    10,     0,     3,     6,     7,    11,    12,     8,     0,
     1,     0,     5,     0,     5,     5,     0,     3,     0,     3,
     3,     0
};
#endif
#if fingerprint_gram_DEBUG != 0
static const short fingerprint_gram_rline[] = { 0,
   110,   111,   115,   136,   140,   142,   147,   149
};
static const char * const fingerprint_gram_tname[] = {   "$","error","$illegal.","STRING","JUNK",
"NUMBER","EQ","LB","RB","cache","entry","number_pair","string_pair",""
};
#endif
static const short fingerprint_gram_r1[] = {     0,
     9,     9,    10,    10,    11,    11,    12,    12
};
static const short fingerprint_gram_r2[] = {     0,
     0,     2,     6,     1,     1,     2,     1,     2
};
static const short fingerprint_gram_defact[] = {     1,
     0,     4,     0,     2,     0,     0,     5,     0,     6,     7,
     0,     8,     3,     0,     0
};
static const short fingerprint_gram_defgoto[] = {     1,
     4,     8,    11
};
static const short fingerprint_gram_pact[] = {-32768,
     0,-32768,    -4,-32768,    -3,     1,     2,     5,-32768,     6,
     3,-32768,-32768,    10,-32768
};
static const short fingerprint_gram_pgoto[] = {-32768,
-32768,-32768,-32768
};
#define        fingerprint_gram_LAST   11
static const short fingerprint_gram_table[] = {    14,
     2,     5,     3,     6,     0,     7,     9,    10,    12,    15,
    13
};
static const short fingerprint_gram_check[] = {     0,
     1,     6,     3,     7,    -1,     5,     5,     3,     3,     0,
     8
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
#define fingerprint_gram_errok         (fingerprint_gram_errstatus = 0)
#define fingerprint_gram_clearin       (fingerprint_gram_char = fingerprint_gram_EMPTY)
#define fingerprint_gram_EMPTY         -2
#define fingerprint_gram_EOF     0
#define fingerprint_gram_ACCEPT        return(0)
#define fingerprint_gram_ABORT         return(1)
#define fingerprint_gram_ERROR         goto fingerprint_gram_errlab1
/* Like fingerprint_gram_ERROR except do call fingerprint_gram_error.
   This remains here temporarily to ease the
   transition to the new meaning of fingerprint_gram_ERROR, for GCC.
   Once GCC version 2 has supplanted version 1, this can go.  */
#define fingerprint_gram_FAIL   goto fingerprint_gram_errlab
#define fingerprint_gram_RECOVERING()  (!!fingerprint_gram_errstatus)
#define fingerprint_gram_BACKUP(token, value) \
do                                    \
  if (fingerprint_gram_char == fingerprint_gram_EMPTY && fingerprint_gram_len == 1)          \
    { fingerprint_gram_char = (token), fingerprint_gram_lval = (value);                     \
      fingerprint_gram_char1 = fingerprint_gram_TRANSLATE (fingerprint_gram_char);              \
      fingerprint_gram_POPSTACK;                           \
      goto fingerprint_gram_backup;                         \
    }                                    \
  else                                    \
    { fingerprint_gram_error ("syntax error: cannot back up"); fingerprint_gram_ERROR; }       \
while (0)
#define fingerprint_gram_TERROR        1
#define fingerprint_gram_ERRCODE       256
#ifndef fingerprint_gram_PURE
#define fingerprint_gram_LEX     fingerprint_gram_lex()
#endif
#ifdef fingerprint_gram_PURE
#ifdef fingerprint_gram_LSP_NEEDED
#define fingerprint_gram_LEX     fingerprint_gram_lex(&fingerprint_gram_lval, &fingerprint_gram_lloc)
#else
#define fingerprint_gram_LEX     fingerprint_gram_lex(&fingerprint_gram_lval)
#endif
#endif
/* If nonreentrant, generate the variables here */
#ifndef fingerprint_gram_PURE
int    fingerprint_gram_char;            /*  the lookahead symbol   */
fingerprint_gram_STYPE fingerprint_gram_lval;           /*  the semantic value of the   */
                      /*  lookahead symbol                  */
#ifdef fingerprint_gram_LSP_NEEDED
fingerprint_gram_LTYPE fingerprint_gram_lloc;           /*  location data for the lookahead    */
                      /*  symbol                      */
#endif
int fingerprint_gram_nerrs;         /*  number of parse errors so far       */
#endif  /* not fingerprint_gram_PURE */
#if fingerprint_gram_DEBUG != 0
int fingerprint_gram_debug;         /*  nonzero means print parse trace      */
/* Since this is uninitialized, it does not stop multiple parsers
   from coexisting.  */
#endif
/*  fingerprint_gram_INITDEPTH indicates the initial size of the parser's stacks       */
#ifndef        fingerprint_gram_INITDEPTH
#define fingerprint_gram_INITDEPTH 200
#endif
/*  fingerprint_gram_MAXDEPTH is the maximum size the stacks can grow to
    (effective only if the built-in stack extension method is used).  */
#if fingerprint_gram_MAXDEPTH == 0
#undef fingerprint_gram_MAXDEPTH
#endif
#ifndef fingerprint_gram_MAXDEPTH
#define fingerprint_gram_MAXDEPTH 10000
#endif
/* Prevent warning if -Wstrict-prototypes.  */
#ifdef __GNUC__
int fingerprint_gram_parse (void);
#endif
#if __GNUC__ > 1             /* GNU C and GNU C++ define this.  */
#define __fingerprint_gram__bcopy(FROM,TO,COUNT)       __builtin_memcpy(TO,FROM,COUNT)
#else                  /* not GNU C or C++ */
#ifndef __cplusplus
/* This is the most reliable way to avoid incompatibilities
   in available built-in functions on various systems.  */
static void
__fingerprint_gram__bcopy (from, to, count)
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
__fingerprint_gram__bcopy (char *from, char *to, int count) {
  register char *f = from;
  register char *t = to;
  register int i = count;
  while (i-- > 0)
    *t++ = *f++; }
#endif
#endif
int
fingerprint_gram_parse() {
  register int fingerprint_gram_state;
  register int fingerprint_gram_n;
  register short *fingerprint_gram_ssp;
  register fingerprint_gram_STYPE *fingerprint_gram_vsp;
  int fingerprint_gram_errstatus;      /*  number of tokens to shift before error messages enabled */
  int fingerprint_gram_char1;   /*  lookahead token as an internal (translated) token number */
  short        fingerprint_gram_ssa[fingerprint_gram_INITDEPTH];      /*  the state stack           */
  fingerprint_gram_STYPE fingerprint_gram_vsa[fingerprint_gram_INITDEPTH];     /*  the semantic value stack               */
  short *fingerprint_gram_ss = fingerprint_gram_ssa;     /*  refer to the stacks thru separate pointers */
  fingerprint_gram_STYPE *fingerprint_gram_vs = fingerprint_gram_vsa;  /*  to allow fingerprint_gram_overflow to reallocate them elsewhere */
#ifdef fingerprint_gram_LSP_NEEDED
  fingerprint_gram_LTYPE fingerprint_gram_lsa[fingerprint_gram_INITDEPTH];     /*  the location stack               */
  fingerprint_gram_LTYPE *fingerprint_gram_ls = fingerprint_gram_lsa;
  fingerprint_gram_LTYPE *fingerprint_gram_lsp;
#define fingerprint_gram_POPSTACK   (fingerprint_gram_vsp--, fingerprint_gram_ssp--, fingerprint_gram_lsp--)
#else
#define fingerprint_gram_POPSTACK   (fingerprint_gram_vsp--, fingerprint_gram_ssp--)
#endif
  int fingerprint_gram_stacksize = fingerprint_gram_INITDEPTH;
#ifdef fingerprint_gram_PURE
  int fingerprint_gram_char;
  fingerprint_gram_STYPE fingerprint_gram_lval;
  int fingerprint_gram_nerrs;
#ifdef fingerprint_gram_LSP_NEEDED
  fingerprint_gram_LTYPE fingerprint_gram_lloc;
#endif
#endif
  fingerprint_gram_STYPE fingerprint_gram_val;         /*  the variable used to return               */
                      /*  semantic values from the action        */
                      /*  routines                      */
  int fingerprint_gram_len;
#if fingerprint_gram_DEBUG != 0
  if (fingerprint_gram_debug)
    fprintf(stderr, "Starting parse\n");
#endif
  fingerprint_gram_state = 0;
  fingerprint_gram_errstatus = 0;
  fingerprint_gram_nerrs = 0;
  fingerprint_gram_char = fingerprint_gram_EMPTY;           /* Cause a token to be read.  */
  /* Initialize stack pointers.
     Waste one element of value and location stack
     so that they stay on the same level as the state stack.
     The wasted elements are never initialized.  */
  fingerprint_gram_ssp = fingerprint_gram_ss - 1;
  fingerprint_gram_vsp = fingerprint_gram_vs;
#ifdef fingerprint_gram_LSP_NEEDED
  fingerprint_gram_lsp = fingerprint_gram_ls;
#endif
/* Push a new state, which is found in  fingerprint_gram_state  .  */
/* In all cases, when you get here, the value and location stacks
   have just been pushed. so pushing a state here evens the stacks.  */
fingerprint_gram_newstate:
  *++fingerprint_gram_ssp = fingerprint_gram_state;
  if (fingerprint_gram_ssp >= fingerprint_gram_ss + fingerprint_gram_stacksize - 1) {
      /* Give user a chance to reallocate the stack */
      /* Use copies of these so that the &'s don't force the real ones into memory. */
      fingerprint_gram_STYPE *fingerprint_gram_vs1 = fingerprint_gram_vs;
      short *fingerprint_gram_ss1 = fingerprint_gram_ss;
#ifdef fingerprint_gram_LSP_NEEDED
      fingerprint_gram_LTYPE *fingerprint_gram_ls1 = fingerprint_gram_ls;
#endif
      /* Get the current used size of the three stacks, in elements.  */
      int size = fingerprint_gram_ssp - fingerprint_gram_ss + 1;
#ifdef fingerprint_gram_overflow
      /* Each stack pointer address is followed by the size of
        the data in use in that stack, in bytes.  */
      fingerprint_gram_overflow("parser stack overflow",
              &fingerprint_gram_ss1, size * sizeof (*fingerprint_gram_ssp),
              &fingerprint_gram_vs1, size * sizeof (*fingerprint_gram_vsp),
#ifdef fingerprint_gram_LSP_NEEDED
              &fingerprint_gram_ls1, size * sizeof (*fingerprint_gram_lsp),
#endif
              &fingerprint_gram_stacksize);
      fingerprint_gram_ss = fingerprint_gram_ss1; fingerprint_gram_vs = fingerprint_gram_vs1;
#ifdef fingerprint_gram_LSP_NEEDED
      fingerprint_gram_ls = fingerprint_gram_ls1;
#endif
#else /* no fingerprint_gram_overflow */
      /* Extend the stack our own way.  */
      if (fingerprint_gram_stacksize >= fingerprint_gram_MAXDEPTH) {
         fingerprint_gram_error("parser stack overflow");
         return 2; }
      fingerprint_gram_stacksize *= 2;
      if (fingerprint_gram_stacksize > fingerprint_gram_MAXDEPTH)
       fingerprint_gram_stacksize = fingerprint_gram_MAXDEPTH;
      fingerprint_gram_ss = (short *) alloca (fingerprint_gram_stacksize * sizeof (*fingerprint_gram_ssp));
      __fingerprint_gram__bcopy ((char *)fingerprint_gram_ss1, (char *)fingerprint_gram_ss, size * sizeof (*fingerprint_gram_ssp));
      fingerprint_gram_vs = (fingerprint_gram_STYPE *) alloca (fingerprint_gram_stacksize * sizeof (*fingerprint_gram_vsp));
      __fingerprint_gram__bcopy ((char *)fingerprint_gram_vs1, (char *)fingerprint_gram_vs, size * sizeof (*fingerprint_gram_vsp));
#ifdef fingerprint_gram_LSP_NEEDED
      fingerprint_gram_ls = (fingerprint_gram_LTYPE *) alloca (fingerprint_gram_stacksize * sizeof (*fingerprint_gram_lsp));
      __fingerprint_gram__bcopy ((char *)fingerprint_gram_ls1, (char *)fingerprint_gram_ls, size * sizeof (*fingerprint_gram_lsp));
#endif
#endif /* no fingerprint_gram_overflow */
      fingerprint_gram_ssp = fingerprint_gram_ss + size - 1;
      fingerprint_gram_vsp = fingerprint_gram_vs + size - 1;
#ifdef fingerprint_gram_LSP_NEEDED
      fingerprint_gram_lsp = fingerprint_gram_ls + size - 1;
#endif
#if fingerprint_gram_DEBUG != 0
      if (fingerprint_gram_debug)
       fprintf(stderr, "Stack size increased to %d\n", fingerprint_gram_stacksize);
#endif
      if (fingerprint_gram_ssp >= fingerprint_gram_ss + fingerprint_gram_stacksize - 1)
       fingerprint_gram_ABORT; }
#if fingerprint_gram_DEBUG != 0
  if (fingerprint_gram_debug)
    fprintf(stderr, "Entering state %d\n", fingerprint_gram_state);
#endif
  goto fingerprint_gram_backup;
 fingerprint_gram_backup:
/* Do appropriate processing given the current state.  */
/* Read a lookahead token if we need one and don't already have one.  */
/* fingerprint_gram_resume: */
  /* First try to decide what to do without reference to lookahead token.  */
  fingerprint_gram_n = fingerprint_gram_pact[fingerprint_gram_state];
  if (fingerprint_gram_n == fingerprint_gram_FLAG)
    goto fingerprint_gram_default;
  /* Not known => get a lookahead token if don't already have one.  */
  /* fingerprint_gram_char is either fingerprint_gram_EMPTY or fingerprint_gram_EOF
     or a valid token in external form.  */
  if (fingerprint_gram_char == fingerprint_gram_EMPTY) {
#if fingerprint_gram_DEBUG != 0
      if (fingerprint_gram_debug)
       fprintf(stderr, "Reading a token: ");
#endif
      fingerprint_gram_char = fingerprint_gram_LEX; }
  /* Convert token to internal form (in fingerprint_gram_char1) for indexing tables with */
  if (fingerprint_gram_char <= 0)           /* This means end of input. */ {
      fingerprint_gram_char1 = 0;
      fingerprint_gram_char = fingerprint_gram_EOF;       /* Don't call fingerprint_gram_LEX any more */
#if fingerprint_gram_DEBUG != 0
      if (fingerprint_gram_debug)
       fprintf(stderr, "Now at end of input.\n");
#endif
    }
  else {
      fingerprint_gram_char1 = fingerprint_gram_TRANSLATE(fingerprint_gram_char);
#if fingerprint_gram_DEBUG != 0
      if (fingerprint_gram_debug) {
         fprintf (stderr, "Next token is %d (%s", fingerprint_gram_char, fingerprint_gram_tname[fingerprint_gram_char1]);
         /* Give the individual parser a way to print the precise meaning
            of a token, for further debugging info.  */
#ifdef fingerprint_gram_PRINT
         fingerprint_gram_PRINT (stderr, fingerprint_gram_char, fingerprint_gram_lval);
#endif
         fprintf (stderr, ")\n"); }
#endif
    }
  fingerprint_gram_n += fingerprint_gram_char1;
  if (fingerprint_gram_n < 0 || fingerprint_gram_n > fingerprint_gram_LAST || fingerprint_gram_check[fingerprint_gram_n] != fingerprint_gram_char1)
    goto fingerprint_gram_default;
  fingerprint_gram_n = fingerprint_gram_table[fingerprint_gram_n];
  /* fingerprint_gram_n is what to do for this token type in this state.
     Negative => reduce, -fingerprint_gram_n is rule number.
     Positive => shift, fingerprint_gram_n is new state.
       New state is final state => don't bother to shift,
       just return success.
     0, or most negative number => error.  */
  if (fingerprint_gram_n < 0) {
      if (fingerprint_gram_n == fingerprint_gram_FLAG)
       goto fingerprint_gram_errlab;
      fingerprint_gram_n = -fingerprint_gram_n;
      goto fingerprint_gram_reduce; }
  else if (fingerprint_gram_n == 0)
    goto fingerprint_gram_errlab;
  if (fingerprint_gram_n == fingerprint_gram_FINAL)
    fingerprint_gram_ACCEPT;
  /* Shift the lookahead token.  */
#if fingerprint_gram_DEBUG != 0
  if (fingerprint_gram_debug)
    fprintf(stderr, "Shifting token %d (%s), ", fingerprint_gram_char, fingerprint_gram_tname[fingerprint_gram_char1]);
#endif
  /* Discard the token being shifted unless it is eof.  */
  if (fingerprint_gram_char != fingerprint_gram_EOF)
    fingerprint_gram_char = fingerprint_gram_EMPTY;
  *++fingerprint_gram_vsp = fingerprint_gram_lval;
#ifdef fingerprint_gram_LSP_NEEDED
  *++fingerprint_gram_lsp = fingerprint_gram_lloc;
#endif
  /* count tokens shifted since error; after three, turn off error status.  */
  if (fingerprint_gram_errstatus) fingerprint_gram_errstatus--;
  fingerprint_gram_state = fingerprint_gram_n;
  goto fingerprint_gram_newstate;
/* Do the default action for the current state.  */
fingerprint_gram_default:
  fingerprint_gram_n = fingerprint_gram_defact[fingerprint_gram_state];
  if (fingerprint_gram_n == 0)
    goto fingerprint_gram_errlab;
/* Do a reduction.  fingerprint_gram_n is the number of a rule to reduce with.  */
fingerprint_gram_reduce:
  fingerprint_gram_len = fingerprint_gram_r2[fingerprint_gram_n];
  fingerprint_gram_val = fingerprint_gram_vsp[1-fingerprint_gram_len]; /* implement default value of the action */
#if fingerprint_gram_DEBUG != 0
  if (fingerprint_gram_debug) {
      int i;
      fprintf (stderr, "Reducing via rule %d (line %d), ",
              fingerprint_gram_n, fingerprint_gram_rline[fingerprint_gram_n]);
      /* Print the symbols being reduced, and their result.  */
      for (i = fingerprint_gram_prhs[fingerprint_gram_n]; fingerprint_gram_rhs[i] > 0; i++)
       fprintf (stderr, "%s ", fingerprint_gram_tname[fingerprint_gram_rhs[i]]);
      fprintf (stderr, " -> %s\n", fingerprint_gram_tname[fingerprint_gram_r1[fingerprint_gram_n]]); }
#endif
  switch (fingerprint_gram_n) {
case 3: {
                  fp_value_ty data;
                  fp_value_constructor4
                  (
                      &data,
                      fingerprint_gram_vsp[-2].lv_number_pair.lhs,
                      fingerprint_gram_vsp[-2].lv_number_pair.rhs,
                      fingerprint_gram_vsp[-1].lv_string_pair.lhs,
                      fingerprint_gram_vsp[-1].lv_string_pair.rhs
                  );
                  str_free(fingerprint_gram_vsp[-1].lv_string_pair.lhs);
                  if (fingerprint_gram_vsp[-1].lv_string_pair.rhs)
                      str_free(fingerprint_gram_vsp[-1].lv_string_pair.rhs);
                  fp_find_update(subdir, fingerprint_gram_vsp[-5].lv_string, &data);
                  str_free(fingerprint_gram_vsp[-5].lv_string);
                  fp_value_destructor(&data);
             ;
    break;}
case 5:
{ fingerprint_gram_val.lv_number_pair.lhs = fingerprint_gram_vsp[0].lv_number; fingerprint_gram_val.lv_number_pair.rhs = fingerprint_gram_vsp[0].lv_number; ;
    break;}
case 6:
{ fingerprint_gram_val.lv_number_pair.lhs = fingerprint_gram_vsp[-1].lv_number; fingerprint_gram_val.lv_number_pair.rhs = fingerprint_gram_vsp[0].lv_number; ;
    break;}
case 7:
{ fingerprint_gram_val.lv_string_pair.lhs = fingerprint_gram_vsp[0].lv_string; fingerprint_gram_val.lv_string_pair.rhs = 0; ;
    break;}
case 8:
{ fingerprint_gram_val.lv_string_pair.lhs = fingerprint_gram_vsp[-1].lv_string; fingerprint_gram_val.lv_string_pair.rhs = fingerprint_gram_vsp[0].lv_string; ;
    break;} }
   /* the action file gets copied in in place of this dollarsign */
  fingerprint_gram_vsp -= fingerprint_gram_len;
  fingerprint_gram_ssp -= fingerprint_gram_len;
#ifdef fingerprint_gram_LSP_NEEDED
  fingerprint_gram_lsp -= fingerprint_gram_len;
#endif
#if fingerprint_gram_DEBUG != 0
  if (fingerprint_gram_debug) {
      short *ssp1 = fingerprint_gram_ss - 1;
      fprintf (stderr, "state stack now");
      while (ssp1 != fingerprint_gram_ssp)
       fprintf (stderr, " %d", *++ssp1);
      fprintf (stderr, "\n"); }
#endif
  *++fingerprint_gram_vsp = fingerprint_gram_val;
#ifdef fingerprint_gram_LSP_NEEDED
  fingerprint_gram_lsp++;
  if (fingerprint_gram_len == 0) {
      fingerprint_gram_lsp->first_line = fingerprint_gram_lloc.first_line;
      fingerprint_gram_lsp->first_column = fingerprint_gram_lloc.first_column;
      fingerprint_gram_lsp->last_line = (fingerprint_gram_lsp-1)->last_line;
      fingerprint_gram_lsp->last_column = (fingerprint_gram_lsp-1)->last_column;
      fingerprint_gram_lsp->text = 0; }
  else {
      fingerprint_gram_lsp->last_line = (fingerprint_gram_lsp+fingerprint_gram_len-1)->last_line;
      fingerprint_gram_lsp->last_column = (fingerprint_gram_lsp+fingerprint_gram_len-1)->last_column; }
#endif
  /* Now "shift" the result of the reduction.
     Determine what state that goes to,
     based on the state we popped back to
     and the rule number reduced by.  */
  fingerprint_gram_n = fingerprint_gram_r1[fingerprint_gram_n];
  fingerprint_gram_state = fingerprint_gram_pgoto[fingerprint_gram_n - fingerprint_gram_NTBASE] + *fingerprint_gram_ssp;
  if (fingerprint_gram_state >= 0 && fingerprint_gram_state <= fingerprint_gram_LAST && fingerprint_gram_check[fingerprint_gram_state] == *fingerprint_gram_ssp)
    fingerprint_gram_state = fingerprint_gram_table[fingerprint_gram_state];
  else
    fingerprint_gram_state = fingerprint_gram_defgoto[fingerprint_gram_n - fingerprint_gram_NTBASE];
  goto fingerprint_gram_newstate;
fingerprint_gram_errlab:   /* here on detecting error */
  if (! fingerprint_gram_errstatus)
    /* If not already recovering from an error, report this error.  */ {
      ++fingerprint_gram_nerrs;
#ifdef fingerprint_gram_ERROR_VERBOSE
      fingerprint_gram_n = fingerprint_gram_pact[fingerprint_gram_state];
      if (fingerprint_gram_n > fingerprint_gram_FLAG && fingerprint_gram_n < fingerprint_gram_LAST) {
         int size = 0;
         char *msg;
         int x, count;
         count = 0;
         /* Start X at -fingerprint_gram_n if nec to avoid negative indexes in fingerprint_gram_check.  */
         for (x = (fingerprint_gram_n < 0 ? -fingerprint_gram_n : 0);
              x < (sizeof(fingerprint_gram_tname) / sizeof(char *)); x++)
           if (fingerprint_gram_check[x + fingerprint_gram_n] == x)
             size += strlen(fingerprint_gram_tname[x]) + 15, count++;
         msg = (char *) malloc(size + 15);
         if (msg != 0) {
             strcpy(msg, "parse error");
             if (count < 5) {
               count = 0;
               for (x = (fingerprint_gram_n < 0 ? -fingerprint_gram_n : 0);
                    x < (sizeof(fingerprint_gram_tname) / sizeof(char *)); x++)
                 if (fingerprint_gram_check[x + fingerprint_gram_n] == x) {
                  strcat(msg, count == 0 ? ", expecting `" : " or `");
                  strcat(msg, fingerprint_gram_tname[x]);
                  strcat(msg, "'");
                  count++; } }
             fingerprint_gram_error(msg);
             free(msg); }
         else
           fingerprint_gram_error ("parse error; also virtual memory exceeded"); }
      else
#endif /* fingerprint_gram_ERROR_VERBOSE */
       fingerprint_gram_error("parse error"); }
  goto fingerprint_gram_errlab1;
fingerprint_gram_errlab1:   /* here on error raised explicitly by an action */
  if (fingerprint_gram_errstatus == 3) {
      /* if just tried and failed to reuse lookahead token after an error, discard it.  */
      /* return failure if at end of input */
      if (fingerprint_gram_char == fingerprint_gram_EOF)
       fingerprint_gram_ABORT;
#if fingerprint_gram_DEBUG != 0
      if (fingerprint_gram_debug)
       fprintf(stderr, "Discarding token %d (%s).\n", fingerprint_gram_char, fingerprint_gram_tname[fingerprint_gram_char1]);
#endif
      fingerprint_gram_char = fingerprint_gram_EMPTY; }
  /* Else will try to reuse lookahead token
     after shifting the error token.  */
  fingerprint_gram_errstatus = 3;           /* Each real token shifted decrements this */
  goto fingerprint_gram_errhandle;
fingerprint_gram_errdefault:  /* current state does not do anything special for the error token. */
#if 0
  /* This is wrong; only states that explicitly want error tokens
     should shift them.  */
  fingerprint_gram_n = fingerprint_gram_defact[fingerprint_gram_state];  /* If its default is to accept any token, ok.  Otherwise pop it.*/
  if (fingerprint_gram_n) goto fingerprint_gram_default;
#endif
fingerprint_gram_errpop:   /* pop the current state because it cannot handle the error token */
  if (fingerprint_gram_ssp == fingerprint_gram_ss) fingerprint_gram_ABORT;
  fingerprint_gram_vsp--;
  fingerprint_gram_state = *--fingerprint_gram_ssp;
#ifdef fingerprint_gram_LSP_NEEDED
  fingerprint_gram_lsp--;
#endif
#if fingerprint_gram_DEBUG != 0
  if (fingerprint_gram_debug) {
      short *ssp1 = fingerprint_gram_ss - 1;
      fprintf (stderr, "Error: state stack now");
      while (ssp1 != fingerprint_gram_ssp)
       fprintf (stderr, " %d", *++ssp1);
      fprintf (stderr, "\n"); }
#endif
fingerprint_gram_errhandle:
  fingerprint_gram_n = fingerprint_gram_pact[fingerprint_gram_state];
  if (fingerprint_gram_n == fingerprint_gram_FLAG)
    goto fingerprint_gram_errdefault;
  fingerprint_gram_n += fingerprint_gram_TERROR;
  if (fingerprint_gram_n < 0 || fingerprint_gram_n > fingerprint_gram_LAST || fingerprint_gram_check[fingerprint_gram_n] != fingerprint_gram_TERROR)
    goto fingerprint_gram_errdefault;
  fingerprint_gram_n = fingerprint_gram_table[fingerprint_gram_n];
  if (fingerprint_gram_n < 0) {
      if (fingerprint_gram_n == fingerprint_gram_FLAG)
       goto fingerprint_gram_errpop;
      fingerprint_gram_n = -fingerprint_gram_n;
      goto fingerprint_gram_reduce; }
  else if (fingerprint_gram_n == 0)
    goto fingerprint_gram_errpop;
  if (fingerprint_gram_n == fingerprint_gram_FINAL)
    fingerprint_gram_ACCEPT;
#if fingerprint_gram_DEBUG != 0
  if (fingerprint_gram_debug)
    fprintf(stderr, "Shifting error token, ");
#endif
  *++fingerprint_gram_vsp = fingerprint_gram_lval;
#ifdef fingerprint_gram_LSP_NEEDED
  *++fingerprint_gram_lsp = fingerprint_gram_lloc;
#endif
  fingerprint_gram_state = fingerprint_gram_n;
  goto fingerprint_gram_newstate; }
