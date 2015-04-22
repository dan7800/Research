/*  A Bison parser, made from cook/parse.y with Bison version GNU Bison version 1.22
  */
#define parse_BISON 1  /* Identify Bison output.  */
#define        CATENATE       258
#define        COLON  259
#define        COLON2 260
#define        DATA   261
#define        DATAEND        262
#define        ELSE   263
#define        EQUALS 264
#define        FAIL   265
#define        FILE_BOUNDARY  266
#define        FUNCTION       267
#define        HOST_BINDING   268
#define        IF     269
#define        LBRACE 270
#define        LBRAK  271
#define        LOOP   272
#define        LOOPSTOP       273
#define        PLUS_EQUALS    274
#define        RBRACE 275
#define        RBRAK  276
#define        RETURN 277
#define        SEMICOLON      278
#define        SET    279
#define        SINGLE_THREAD  280
#define        THEN   281
#define        UNSETENV       282
#define        WORD   283
#include <ac/stddef.h>
#include <ac/stdlib.h>
#include <ac/stdio.h>
#include <expr.h>
#include <expr/catenate.h>
#include <expr/constant.h>
#include <expr/function.h>
#include <expr/list.h>
#include <function.h>
#include <lex.h>
#include <mem.h>
#include <option.h>
#include <parse.h>
#include <stmt.h>
#include <stmt/append.h>
#include <stmt/assign.h>
#include <stmt/command.h>
#include <stmt/compound.h>
#include <stmt/fail.h>
#include <stmt/gosub.h>
#include <stmt/if.h>
#include <stmt/list.h>
#include <stmt/loop.h>
#include <stmt/loopvar.h>
#include <stmt/nop.h>
#include <stmt/recipe.h>
#include <stmt/return.h>
#include <stmt/set.h>
#include <stmt/unsetenv.h>
#include <sub.h>
#include <symtab.h>
#include <trace.h>
#include <star.h>
#include <str_list.h>
#ifdef DEBUG
#define parse_DEBUG 1
#define printf trace_where(__FILE__, __LINE__), lex_trace
extern int parse_debug;
#endif
/*
 *  NAME
 *     parse - read and process a cookbook
 *
 *  SYNOPSIS
 *     void parse(string_ty *filename);
 *
 *  DESCRIPTION
 *     Parse reads and processes a cookbook.
 *
 *  CAVEAT
 *     If any errors are found, the user will be told,
 *     and this routine will not return.
 */
void
parse(filename)
       string_ty *filename; {
       int parse_parse _((void)); /* forward */
       trace(("parse(filename = %08lX)\n{\n"/*}*/, filename));
       trace_string(filename->str_text);
       lex_open(filename, filename);
#if parse_DEBUG
       parse_debug = trace_pretest_;
#endif
       parse_parse();
       lex_close();
       trace((/*{*/"}\n")); }
typedef union {
       expr_ty             *lv_expr;
       expr_list_ty  lv_elist;
       stmt_ty             *lv_stmt;
       stmt_list_ty  lv_slist;
       string_ty     *lv_word;
       int     lv_number;
       expr_position_ty lv_position;
} parse_STYPE;
#ifndef parse_LTYPE
typedef
  struct parse_ltype {
      int timestamp;
      int first_line;
      int first_column;
      int last_line;
      int last_column;
      char *text; }
  parse_ltype;
#define parse_LTYPE parse_ltype
#endif
#ifndef __cplusplus
#ifndef __STDC__
#define const
#endif
#endif
#define        parse_FINAL       98
#define        parse_FLAG         -32768
#define        parse_NTBASE   29
#define parse_TRANSLATE(x) ((unsigned)(x) <= 283 ? parse_translate[x] : 45)
static const char parse_translate[] = {     0,
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
     6,     7,     8,     9,    10,    11,    12,    13,    14,    15,
    16,    17,    18,    19,    20,    21,    22,    23,    24,    25,
    26,    27,    28
};
#if parse_DEBUG != 0
static const short parse_prhs[] = {     0,
     0,     1,     4,     7,    10,    12,    16,    21,    26,    36,
    48,    55,    57,    58,    61,    62,    65,    66,    69,    70,
    73,    74,    77,    79,    84,    91,    94,   100,   103,   107,
   111,   113,   117,   118,   121,   124,   126,   129,   130,   133,
   135,   139,   143,   147,   154,   156,   158,   163,   167
};
static const short parse_rhs[] = {    -1,
    29,    30,     0,    29,    44,     0,    29,     1,     0,    36,
     0,    27,    39,    23,     0,    38,     9,    39,    23,     0,
    38,    19,    39,    23,     0,    38,     4,    39,    31,    32,
    34,    35,    36,    33,     0,    38,     4,    39,     4,    39,
    31,    32,    34,    35,    36,    33,     0,    38,     4,    39,
    31,    32,    23,     0,    11,     0,     0,    24,    39,     0,
     0,    14,    40,     0,     0,    26,    36,     0,     0,    25,
    38,     0,     0,    13,    38,     0,    41,     0,    14,    40,
    26,    30,     0,    14,    40,    26,    30,     8,    30,     0,
    17,    30,     0,    17,    38,     9,    39,    36,     0,    18,
    23,     0,    24,    38,    23,     0,    10,    39,    23,     0,
    23,     0,    15,    37,    20,     0,     0,    37,    30,     0,
    37,     1,     0,    40,     0,    38,    40,     0,     0,    39,
    40,     0,    28,     0,    43,    38,    21,     0,    40,     3,
    40,     0,    38,    31,    23,     0,    38,    31,    23,    42,
    40,     7,     0,     6,     0,    16,     0,    12,    28,     9,
    36,     0,    22,    39,    23,     0,    12,    38,    23,     0
};
#endif
#if parse_DEBUG != 0
static const short parse_rline[] = { 0,
   157,   158,   173,   174,   181,   185,   191,   198,   205,   235,
   268,   293,   315,   319,   326,   330,   337,   341,   348,   352,
   359,   363,   370,   374,   381,   388,   393,   400,   405,   411,
   417,   425,   433,   437,   443,   450,   456,   465,   469,   478,
   494,   500,   509,   516,   528,   535,   542,   552,   561
};
static const char * const parse_tname[] = {   "$","error","$illegal.","CATENATE",
"COLON","COLON2","DATA","DATAEND","ELSE","EQUALS","FAIL","FILE_BOUNDARY","FUNCTION",
"HOST_BINDING","IF","LBRACE","LBRAK","LOOP","LOOPSTOP","PLUS_EQUALS","RBRACE",
"RBRAK","RETURN","SEMICOLON","SET","SINGLE_THREAD","THEN","UNSETENV","WORD",
"cook","statement","set_clause","if_clause","use_clause","single_thread_clause",
"host_binding_clause","compound_statement","statements","elist","exprs","expr",
"command","data","lbrak","function_declaration",""
};
#endif
static const short parse_r1[] = {     0,
    29,    29,    29,    29,    30,    30,    30,    30,    30,    30,
    30,    30,    31,    31,    32,    32,    33,    33,    34,    34,
    35,    35,    30,    30,    30,    30,    30,    30,    30,    30,
    30,    36,    37,    37,    37,    38,    38,    39,    39,    40,
    40,    40,    41,    41,    42,    43,    44,    30,    30
};
static const short parse_r2[] = {     0,
     0,     2,     2,     2,     1,     3,     4,     4,     9,    11,
     6,     1,     0,     2,     0,     2,     0,     2,     0,     2,
     0,     2,     1,     4,     6,     2,     5,     2,     3,     3,
     1,     3,     0,     2,     2,     1,     2,     0,     2,     1,
     3,     3,     3,     6,     1,     1,     4,     3,     3
};
static const short parse_defact[] = {     1,
     0,     4,    38,    12,     0,     0,    33,    46,     0,     0,
    38,    31,     0,    38,    40,     2,     5,    13,    36,    23,
     0,     3,     0,    40,     0,     0,     0,     0,    26,    13,
    28,     0,     0,     0,    38,    38,    38,    38,     0,    37,
     0,     0,    30,    39,     0,    49,     0,    35,    32,    34,
    38,    48,    29,     6,    13,     0,     0,    14,    43,    42,
    41,    47,    24,     0,    38,    15,     7,     8,    45,     0,
     0,    27,    13,     0,    19,     0,    25,    15,    16,    11,
     0,    21,    44,    19,    20,     0,     0,    21,    22,    17,
     0,     0,     9,    17,    18,    10,     0,     0
};
static const short parse_defgoto[] = {     1,
    16,    39,    75,    93,    82,    87,    17,    27,    18,    23,
    19,    20,    70,    21,    22
};
static const short parse_pact[] = {-32768,
    80,-32768,-32768,-32768,    44,    46,-32768,-32768,   142,   -21,
-32768,-32768,    46,-32768,-32768,-32768,-32768,   116,    15,-32768,
    46,-32768,   -12,    14,    16,     5,    99,    46,-32768,   127,
-32768,    43,   122,   145,-32768,-32768,-32768,-32768,    17,    15,
    46,    57,-32768,    15,    22,-32768,   142,-32768,-32768,-32768,
-32768,-32768,-32768,-32768,    37,   151,   155,    46,    32,-32768,
-32768,-32768,    55,   114,-32768,    61,-32768,-32768,-32768,    46,
   142,-32768,    18,    46,   -10,     2,-32768,    61,    15,-32768,
    46,    63,-32768,    52,    46,    46,    22,    63,    46,    60,
    22,    22,-32768,    60,-32768,-32768,    88,-32768
};
static const short parse_pgoto[] = {-32768,
    -2,   -52,    11,    -1,    21,    13,   -44,-32768,     1,    19,
    -6,-32768,-32768,-32768,-32768
};
#define        parse_LAST         183
static const short parse_table[] = {    26,
    62,    31,    66,     8,    41,    25,    29,    41,    83,    30,
    43,    40,    80,    33,    81,    15,    44,    41,    40,    72,
    78,    42,    45,    40,    50,    44,    40,    44,    25,    32,
    47,     8,    34,     8,    60,    40,     7,    69,    46,    59,
    65,    38,    90,    15,    63,    15,    94,    95,    44,    44,
    44,    44,     8,    55,    56,    57,    58,    44,     8,     8,
    38,     8,    71,    76,    15,    52,    44,    79,    77,    64,
    15,    24,     8,    15,    74,    86,    81,    61,    40,    97,
     2,    85,    40,    73,    15,    92,    89,    98,    84,     3,
     4,     5,    96,     6,     7,     8,     9,    10,     0,    48,
    91,    11,    12,    13,    88,     0,    14,    15,     3,     4,
    28,     0,     6,     7,     8,     9,    10,     0,    49,    35,
    11,    12,    13,     0,    36,    14,    15,     0,     7,     8,
    35,     8,     0,     0,    37,    51,    67,     8,     0,    38,
     0,    15,     8,    15,    53,    37,     0,     0,     0,    15,
    38,     3,     4,    28,    15,     6,     7,     8,     9,    10,
     8,     0,     0,    11,    12,    13,     8,    54,    14,    15,
     8,     0,    15,    67,     0,     0,     0,    68,    15,     0,
     0,     0,    15
};
static const short parse_check[] = {     6,
    45,    23,    55,    16,     3,     5,     9,     3,     7,     9,
    23,    18,    23,    13,    25,    28,    23,     3,    25,    64,
    73,    21,     9,    30,    27,    32,    33,    34,    28,    11,
    26,    16,    14,    16,    41,    42,    15,     6,    23,    23,
     4,    24,    87,    28,    47,    28,    91,    92,    55,    56,
    57,    58,    16,    35,    36,    37,    38,    64,    16,    16,
    24,    16,     8,    70,    28,    23,    73,    74,    71,    51,
    28,    28,    16,    28,    14,    13,    25,    21,    85,     0,
     1,    81,    89,    65,    28,    26,    86,     0,    78,    10,
    11,    12,    94,    14,    15,    16,    17,    18,    -1,     1,
    88,    22,    23,    24,    84,    -1,    27,    28,    10,    11,
    12,    -1,    14,    15,    16,    17,    18,    -1,    20,     4,
    22,    23,    24,    -1,     9,    27,    28,    -1,    15,    16,
     4,    16,    -1,    -1,    19,     9,    23,    16,    -1,    24,
    -1,    28,    16,    28,    23,    19,    -1,    -1,    -1,    28,
    24,    10,    11,    12,    28,    14,    15,    16,    17,    18,
    16,    -1,    -1,    22,    23,    24,    16,    23,    27,    28,
    16,    -1,    28,    23,    -1,    -1,    -1,    23,    28,    -1,
    -1,    -1,    28
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
#define parse_errok       (parse_errstatus = 0)
#define parse_clearin  (parse_char = parse_EMPTY)
#define parse_EMPTY       -2
#define parse_EOF           0
#define parse_ACCEPT   return(0)
#define parse_ABORT    return(1)
#define parse_ERROR       goto parse_errlab1
/* Like parse_ERROR except do call parse_error.
   This remains here temporarily to ease the
   transition to the new meaning of parse_ERROR, for GCC.
   Once GCC version 2 has supplanted version 1, this can go.  */
#define parse_FAIL         goto parse_errlab
#define parse_RECOVERING()  (!!parse_errstatus)
#define parse_BACKUP(token, value) \
do                                    \
  if (parse_char == parse_EMPTY && parse_len == 1)              \
    { parse_char = (token), parse_lval = (value);               \
      parse_char1 = parse_TRANSLATE (parse_char);                  \
      parse_POPSTACK;                             \
      goto parse_backup;                           \
    }                                    \
  else                                    \
    { parse_error ("syntax error: cannot back up"); parse_ERROR; }     \
while (0)
#define parse_TERROR   1
#define parse_ERRCODE  256
#ifndef parse_PURE
#define parse_LEX           parse_lex()
#endif
#ifdef parse_PURE
#ifdef parse_LSP_NEEDED
#define parse_LEX           parse_lex(&parse_lval, &parse_lloc)
#else
#define parse_LEX           parse_lex(&parse_lval)
#endif
#endif
/* If nonreentrant, generate the variables here */
#ifndef parse_PURE
int    parse_char;                     /*  the lookahead symbol         */
parse_STYPE    parse_lval;                     /*  the semantic value of the               */
                      /*  lookahead symbol                  */
#ifdef parse_LSP_NEEDED
parse_LTYPE parse_lloc;                     /*  location data for the lookahead  */
                      /*  symbol                      */
#endif
int parse_nerrs;                  /*  number of parse errors so far       */
#endif  /* not parse_PURE */
#if parse_DEBUG != 0
int parse_debug;                  /*  nonzero means print parse trace */
/* Since this is uninitialized, it does not stop multiple parsers
   from coexisting.  */
#endif
/*  parse_INITDEPTH indicates the initial size of the parser's stacks  */
#ifndef        parse_INITDEPTH
#define parse_INITDEPTH 200
#endif
/*  parse_MAXDEPTH is the maximum size the stacks can grow to
    (effective only if the built-in stack extension method is used).  */
#if parse_MAXDEPTH == 0
#undef parse_MAXDEPTH
#endif
#ifndef parse_MAXDEPTH
#define parse_MAXDEPTH 10000
#endif
/* Prevent warning if -Wstrict-prototypes.  */
#ifdef __GNUC__
int parse_parse (void);
#endif
#if __GNUC__ > 1             /* GNU C and GNU C++ define this.  */
#define __parse__bcopy(FROM,TO,COUNT)  __builtin_memcpy(TO,FROM,COUNT)
#else                  /* not GNU C or C++ */
#ifndef __cplusplus
/* This is the most reliable way to avoid incompatibilities
   in available built-in functions on various systems.  */
static void
__parse__bcopy (from, to, count)
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
__parse__bcopy (char *from, char *to, int count) {
  register char *f = from;
  register char *t = to;
  register int i = count;
  while (i-- > 0)
    *t++ = *f++; }
#endif
#endif
int
parse_parse() {
  register int parse_state;
  register int parse_n;
  register short *parse_ssp;
  register parse_STYPE *parse_vsp;
  int parse_errstatus; /*  number of tokens to shift before error messages enabled */
  int parse_char1;         /*  lookahead token as an internal (translated) token number */
  short        parse_ssa[parse_INITDEPTH];    /*  the state stack                     */
  parse_STYPE parse_vsa[parse_INITDEPTH];      /*  the semantic value stack         */
  short *parse_ss = parse_ssa;         /*  refer to the stacks thru separate pointers */
  parse_STYPE *parse_vs = parse_vsa;   /*  to allow parse_overflow to reallocate them elsewhere */
#ifdef parse_LSP_NEEDED
  parse_LTYPE parse_lsa[parse_INITDEPTH];      /*  the location stack                  */
  parse_LTYPE *parse_ls = parse_lsa;
  parse_LTYPE *parse_lsp;
#define parse_POPSTACK   (parse_vsp--, parse_ssp--, parse_lsp--)
#else
#define parse_POPSTACK   (parse_vsp--, parse_ssp--)
#endif
  int parse_stacksize = parse_INITDEPTH;
#ifdef parse_PURE
  int parse_char;
  parse_STYPE parse_lval;
  int parse_nerrs;
#ifdef parse_LSP_NEEDED
  parse_LTYPE parse_lloc;
#endif
#endif
  parse_STYPE parse_val;             /*  the variable used to return           */
                      /*  semantic values from the action        */
                      /*  routines                      */
  int parse_len;
#if parse_DEBUG != 0
  if (parse_debug)
    fprintf(stderr, "Starting parse\n");
#endif
  parse_state = 0;
  parse_errstatus = 0;
  parse_nerrs = 0;
  parse_char = parse_EMPTY;       /* Cause a token to be read.  */
  /* Initialize stack pointers.
     Waste one element of value and location stack
     so that they stay on the same level as the state stack.
     The wasted elements are never initialized.  */
  parse_ssp = parse_ss - 1;
  parse_vsp = parse_vs;
#ifdef parse_LSP_NEEDED
  parse_lsp = parse_ls;
#endif
/* Push a new state, which is found in  parse_state  .  */
/* In all cases, when you get here, the value and location stacks
   have just been pushed. so pushing a state here evens the stacks.  */
parse_newstate:
  *++parse_ssp = parse_state;
  if (parse_ssp >= parse_ss + parse_stacksize - 1) {
      /* Give user a chance to reallocate the stack */
      /* Use copies of these so that the &'s don't force the real ones into memory. */
      parse_STYPE *parse_vs1 = parse_vs;
      short *parse_ss1 = parse_ss;
#ifdef parse_LSP_NEEDED
      parse_LTYPE *parse_ls1 = parse_ls;
#endif
      /* Get the current used size of the three stacks, in elements.  */
      int size = parse_ssp - parse_ss + 1;
#ifdef parse_overflow
      /* Each stack pointer address is followed by the size of
        the data in use in that stack, in bytes.  */
      parse_overflow("parser stack overflow",
              &parse_ss1, size * sizeof (*parse_ssp),
              &parse_vs1, size * sizeof (*parse_vsp),
#ifdef parse_LSP_NEEDED
              &parse_ls1, size * sizeof (*parse_lsp),
#endif
              &parse_stacksize);
      parse_ss = parse_ss1; parse_vs = parse_vs1;
#ifdef parse_LSP_NEEDED
      parse_ls = parse_ls1;
#endif
#else /* no parse_overflow */
      /* Extend the stack our own way.  */
      if (parse_stacksize >= parse_MAXDEPTH) {
         parse_error("parser stack overflow");
         return 2; }
      parse_stacksize *= 2;
      if (parse_stacksize > parse_MAXDEPTH)
       parse_stacksize = parse_MAXDEPTH;
      parse_ss = (short *) alloca (parse_stacksize * sizeof (*parse_ssp));
      __parse__bcopy ((char *)parse_ss1, (char *)parse_ss, size * sizeof (*parse_ssp));
      parse_vs = (parse_STYPE *) alloca (parse_stacksize * sizeof (*parse_vsp));
      __parse__bcopy ((char *)parse_vs1, (char *)parse_vs, size * sizeof (*parse_vsp));
#ifdef parse_LSP_NEEDED
      parse_ls = (parse_LTYPE *) alloca (parse_stacksize * sizeof (*parse_lsp));
      __parse__bcopy ((char *)parse_ls1, (char *)parse_ls, size * sizeof (*parse_lsp));
#endif
#endif /* no parse_overflow */
      parse_ssp = parse_ss + size - 1;
      parse_vsp = parse_vs + size - 1;
#ifdef parse_LSP_NEEDED
      parse_lsp = parse_ls + size - 1;
#endif
#if parse_DEBUG != 0
      if (parse_debug)
       fprintf(stderr, "Stack size increased to %d\n", parse_stacksize);
#endif
      if (parse_ssp >= parse_ss + parse_stacksize - 1)
       parse_ABORT; }
#if parse_DEBUG != 0
  if (parse_debug)
    fprintf(stderr, "Entering state %d\n", parse_state);
#endif
  goto parse_backup;
 parse_backup:
/* Do appropriate processing given the current state.  */
/* Read a lookahead token if we need one and don't already have one.  */
/* parse_resume: */
  /* First try to decide what to do without reference to lookahead token.  */
  parse_n = parse_pact[parse_state];
  if (parse_n == parse_FLAG)
    goto parse_default;
  /* Not known => get a lookahead token if don't already have one.  */
  /* parse_char is either parse_EMPTY or parse_EOF
     or a valid token in external form.  */
  if (parse_char == parse_EMPTY) {
#if parse_DEBUG != 0
      if (parse_debug)
       fprintf(stderr, "Reading a token: ");
#endif
      parse_char = parse_LEX; }
  /* Convert token to internal form (in parse_char1) for indexing tables with */
  if (parse_char <= 0)         /* This means end of input. */ {
      parse_char1 = 0;
      parse_char = parse_EOF;   /* Don't call parse_LEX any more */
#if parse_DEBUG != 0
      if (parse_debug)
       fprintf(stderr, "Now at end of input.\n");
#endif
    }
  else {
      parse_char1 = parse_TRANSLATE(parse_char);
#if parse_DEBUG != 0
      if (parse_debug) {
         fprintf (stderr, "Next token is %d (%s", parse_char, parse_tname[parse_char1]);
         /* Give the individual parser a way to print the precise meaning
            of a token, for further debugging info.  */
#ifdef parse_PRINT
         parse_PRINT (stderr, parse_char, parse_lval);
#endif
         fprintf (stderr, ")\n"); }
#endif
    }
  parse_n += parse_char1;
  if (parse_n < 0 || parse_n > parse_LAST || parse_check[parse_n] != parse_char1)
    goto parse_default;
  parse_n = parse_table[parse_n];
  /* parse_n is what to do for this token type in this state.
     Negative => reduce, -parse_n is rule number.
     Positive => shift, parse_n is new state.
       New state is final state => don't bother to shift,
       just return success.
     0, or most negative number => error.  */
  if (parse_n < 0) {
      if (parse_n == parse_FLAG)
       goto parse_errlab;
      parse_n = -parse_n;
      goto parse_reduce; }
  else if (parse_n == 0)
    goto parse_errlab;
  if (parse_n == parse_FINAL)
    parse_ACCEPT;
  /* Shift the lookahead token.  */
#if parse_DEBUG != 0
  if (parse_debug)
    fprintf(stderr, "Shifting token %d (%s), ", parse_char, parse_tname[parse_char1]);
#endif
  /* Discard the token being shifted unless it is eof.  */
  if (parse_char != parse_EOF)
    parse_char = parse_EMPTY;
  *++parse_vsp = parse_lval;
#ifdef parse_LSP_NEEDED
  *++parse_lsp = parse_lloc;
#endif
  /* count tokens shifted since error; after three, turn off error status.  */
  if (parse_errstatus) parse_errstatus--;
  parse_state = parse_n;
  goto parse_newstate;
/* Do the default action for the current state.  */
parse_default:
  parse_n = parse_defact[parse_state];
  if (parse_n == 0)
    goto parse_errlab;
/* Do a reduction.  parse_n is the number of a rule to reduce with.  */
parse_reduce:
  parse_len = parse_r2[parse_n];
  parse_val = parse_vsp[1-parse_len]; /* implement default value of the action */
#if parse_DEBUG != 0
  if (parse_debug) {
      int i;
      fprintf (stderr, "Reducing via rule %d (line %d), ",
              parse_n, parse_rline[parse_n]);
      /* Print the symbols being reduced, and their result.  */
      for (i = parse_prhs[parse_n]; parse_rhs[i] > 0; i++)
       fprintf (stderr, "%s ", parse_tname[parse_rhs[i]]);
      fprintf (stderr, " -> %s\n", parse_tname[parse_r1[parse_n]]); }
#endif
  switch (parse_n) {
case 2: {
                  star_as_specified('+');
                  if (stmt_evaluate(parse_vsp[0].lv_stmt, 0)) {
                      lex_error(0, i18n("statement failed"));
                      option_set_errors();
                      /*
                       * Halt the parse immediately.
                       */
                      return 1; }
                  stmt_delete(parse_vsp[0].lv_stmt);
             ;
    break;}
case 4: {
                  lex_mode(LM_NORMAL);
             ;
    break;}
case 5: {
                  parse_val.lv_stmt = parse_vsp[0].lv_stmt;
             ;
    break;}
case 6: {
                  parse_val.lv_stmt = stmt_unsetenv_new(&parse_vsp[-1].lv_elist);
                  expr_list_destructor(&parse_vsp[-1].lv_elist);
                  expr_position_destructor(&parse_vsp[0].lv_position);
             ;
    break;}
case 7: {
                  parse_val.lv_stmt = stmt_assign_new(&parse_vsp[-3].lv_elist, &parse_vsp[-1].lv_elist);
                  expr_list_destructor(&parse_vsp[-3].lv_elist);
                  expr_list_destructor(&parse_vsp[-1].lv_elist);
                  expr_position_destructor(&parse_vsp[0].lv_position);
             ;
    break;}
case 8: {
                  parse_val.lv_stmt = stmt_append_new(&parse_vsp[-3].lv_elist, &parse_vsp[-1].lv_elist);
                  expr_list_destructor(&parse_vsp[-3].lv_elist);
                  expr_list_destructor(&parse_vsp[-1].lv_elist);
                  expr_position_destructor(&parse_vsp[0].lv_position);
             ;
    break;}
case 9: {
                  parse_val.lv_stmt =
                      stmt_recipe_new
                      (
                         &parse_vsp[-8].lv_elist,   /* target */
                         &parse_vsp[-6].lv_elist,   /* need */
                         (expr_list_ty *)0, /* need2 */
                         &parse_vsp[-5].lv_elist,   /* flags */
                         parse_vsp[-7].lv_position.multi,  /* multiple */
                         parse_vsp[-4].lv_expr,       /* precondition */
                         &parse_vsp[-3].lv_elist,   /* single thread */
                         &parse_vsp[-2].lv_elist,   /* host binding */
                         parse_vsp[-1].lv_stmt,       /* action */
                         parse_vsp[0].lv_stmt,         /* use_action */
                         &parse_vsp[-7].lv_position               /* position */
                      );
                  expr_list_destructor(&parse_vsp[-8].lv_elist);
                  expr_position_destructor(&parse_vsp[-7].lv_position);
                  expr_list_destructor(&parse_vsp[-6].lv_elist);
                  expr_list_destructor(&parse_vsp[-5].lv_elist);
                  if (parse_vsp[-4].lv_expr)
                      expr_delete(parse_vsp[-4].lv_expr);
                  expr_list_destructor(&parse_vsp[-3].lv_elist);
                  expr_list_destructor(&parse_vsp[-2].lv_elist);
                  stmt_delete(parse_vsp[-1].lv_stmt);
                  if (parse_vsp[0].lv_stmt)
                      stmt_delete(parse_vsp[0].lv_stmt);
             ;
    break;}
case 10: {
                  parse_val.lv_stmt =
                      stmt_recipe_new
                      (
                         &parse_vsp[-10].lv_elist,         /* target */
                         &parse_vsp[-8].lv_elist,   /* need */
                         &parse_vsp[-6].lv_elist,   /* need2 */
                         &parse_vsp[-5].lv_elist,   /* flags */
                         (parse_vsp[-9].lv_position.multi || parse_vsp[-7].lv_position.multi),
                         parse_vsp[-4].lv_expr,       /* precondition */
                         &parse_vsp[-3].lv_elist,   /* single thread */
                         &parse_vsp[-2].lv_elist,   /* host binding */
                         parse_vsp[-1].lv_stmt,       /* action */
                         parse_vsp[0].lv_stmt,         /* use_action */
                         &parse_vsp[-9].lv_position               /* position */
                      );
                  expr_list_destructor(&parse_vsp[-10].lv_elist);
                  expr_position_destructor(&parse_vsp[-9].lv_position);
                  expr_list_destructor(&parse_vsp[-8].lv_elist);
                  expr_position_destructor(&parse_vsp[-7].lv_position);
                  expr_list_destructor(&parse_vsp[-6].lv_elist);
                  expr_list_destructor(&parse_vsp[-5].lv_elist);
                  if (parse_vsp[-4].lv_expr)
                      expr_delete(parse_vsp[-4].lv_expr);
                  expr_list_destructor(&parse_vsp[-3].lv_elist);
                  expr_list_destructor(&parse_vsp[-2].lv_elist);
                  stmt_delete(parse_vsp[-1].lv_stmt);
                  if (parse_vsp[0].lv_stmt)
                      stmt_delete(parse_vsp[0].lv_stmt);
             ;
    break;}
case 11: {
                  parse_val.lv_stmt =
                      stmt_recipe_new
                      (
                         &parse_vsp[-5].lv_elist,   /* target */
                         &parse_vsp[-3].lv_elist,   /* need */
                         (expr_list_ty *)0, /* need2 */
                         &parse_vsp[-2].lv_elist,   /* flags */
                         parse_vsp[-4].lv_position.multi,  /* multiple */
                         parse_vsp[-1].lv_expr,       /* precondition */
                         (expr_list_ty *)0, /* single thread */
                         (expr_list_ty *)0, /* host binding */
                         (stmt_ty *)0,     /* action */
                         (stmt_ty *)0,     /* use_action */
                         &parse_vsp[-4].lv_position               /* position */
                      );
                  expr_list_destructor(&parse_vsp[-5].lv_elist);
                  expr_position_destructor(&parse_vsp[-4].lv_position);
                  expr_list_destructor(&parse_vsp[-3].lv_elist);
                  expr_list_destructor(&parse_vsp[-2].lv_elist);
                  if (parse_vsp[-1].lv_expr)
                      expr_delete(parse_vsp[-1].lv_expr);
                  expr_position_destructor(&parse_vsp[0].lv_position);
             ;
    break;}
case 12: {
                  /*
                   * This is a magic zero-length token generated
                   * at the end of include files.  The idea is
                   * that statements may not span include file
                   * boundaries, which will catch a variety of
                   * errors when include files are generated by
                   * programs.  E.g. include dependencies.
                   *
                   * If there are problems with a generated
                   * include file, the insertion of the
                   * FILE_BOUNDARY will give a syntax error in the
                   * OFFENDING include file, not in the next
                   * statement, which is inevitably be the Wrong
                   * file.
                   */
                  parse_val.lv_stmt = stmt_nop_new();
             ;
    break;}
case 13: {
                  expr_list_constructor(&parse_val.lv_elist);
             ;
    break;}
case 14: {
                  parse_val.lv_elist = parse_vsp[0].lv_elist;
             ;
    break;}
case 15: {
                  parse_val.lv_expr = 0;
             ;
    break;}
case 16: {
                  parse_val.lv_expr = parse_vsp[0].lv_expr;
             ;
    break;}
case 17: {
                  parse_val.lv_stmt = 0;
             ;
    break;}
case 18: {
                  parse_val.lv_stmt = parse_vsp[0].lv_stmt;
             ;
    break;}
case 19: {
                  expr_list_constructor(&parse_val.lv_elist);
             ;
    break;}
case 20: {
                  parse_val.lv_elist = parse_vsp[0].lv_elist;
             ;
    break;}
case 21: {
                  expr_list_constructor(&parse_val.lv_elist);
             ;
    break;}
case 22: {
                  parse_val.lv_elist = parse_vsp[0].lv_elist;
             ;
    break;}
case 23: {
                  parse_val.lv_stmt = parse_vsp[0].lv_stmt;
             ;
    break;}
case 24: {
                  parse_val.lv_stmt = stmt_if_new(parse_vsp[-2].lv_expr, parse_vsp[0].lv_stmt, (stmt_ty *)0);
                  expr_delete(parse_vsp[-2].lv_expr);
                  stmt_delete(parse_vsp[0].lv_stmt);
             ;
    break;}
case 25: {
                  parse_val.lv_stmt = stmt_if_new(parse_vsp[-4].lv_expr, parse_vsp[-2].lv_stmt, parse_vsp[0].lv_stmt);
                  expr_delete(parse_vsp[-4].lv_expr);
                  stmt_delete(parse_vsp[-2].lv_stmt);
                  stmt_delete(parse_vsp[0].lv_stmt);
             ;
    break;}
case 26: {
                  parse_val.lv_stmt = stmt_loop_new(parse_vsp[0].lv_stmt);
                  stmt_delete(parse_vsp[0].lv_stmt);
             ;
    break;}
case 27: {
                  parse_val.lv_stmt = stmt_loopvar(&parse_vsp[-3].lv_elist, &parse_vsp[-1].lv_elist, parse_vsp[0].lv_stmt);
                  expr_list_destructor(&parse_vsp[-3].lv_elist);
                  expr_list_destructor(&parse_vsp[-1].lv_elist);
                  stmt_delete(parse_vsp[0].lv_stmt);
             ;
    break;}
case 28: {
                  parse_val.lv_stmt = stmt_loopstop_new(&parse_vsp[0].lv_position);
                  expr_position_destructor(&parse_vsp[0].lv_position);
             ;
    break;}
case 29: {
                  parse_val.lv_stmt = stmt_set_new(&parse_vsp[-1].lv_elist, &parse_vsp[0].lv_position);
                  expr_list_destructor(&parse_vsp[-1].lv_elist);
                  expr_position_destructor(&parse_vsp[0].lv_position);
             ;
    break;}
case 30: {
                  parse_val.lv_stmt = stmt_fail_new(&parse_vsp[-1].lv_elist);
                  expr_list_destructor(&parse_vsp[-1].lv_elist);
                  expr_position_destructor(&parse_vsp[0].lv_position);
             ;
    break;}
case 31: {
                  parse_val.lv_stmt = stmt_nop_new();
                  expr_position_destructor(&parse_vsp[0].lv_position);
             ;
    break;}
case 32: {
                  parse_val.lv_stmt = stmt_compound_new(&parse_vsp[-1].lv_slist);
                  stmt_list_destructor(&parse_vsp[-1].lv_slist);
             ;
    break;}
case 33: {
                  stmt_list_constructor(&parse_val.lv_slist);
             ;
    break;}
case 34: {
                  parse_val.lv_slist = parse_vsp[-1].lv_slist;
                  stmt_list_append(&parse_val.lv_slist, parse_vsp[0].lv_stmt);
                  stmt_delete(parse_vsp[0].lv_stmt);
             ;
    break;}
case 35: {
                  parse_val.lv_slist = parse_vsp[-1].lv_slist;
             ;
    break;}
case 36: {
                  expr_list_constructor(&parse_val.lv_elist);
                  expr_list_append(&parse_val.lv_elist, parse_vsp[0].lv_expr);
                  expr_delete(parse_vsp[0].lv_expr);
             ;
    break;}
case 37: {
                  parse_val.lv_elist = parse_vsp[-1].lv_elist;
                  expr_list_append(&parse_val.lv_elist, parse_vsp[0].lv_expr);
                  expr_delete(parse_vsp[0].lv_expr);
             ;
    break;}
case 38: {
                  expr_list_constructor(&parse_val.lv_elist);
             ;
    break;}
case 39: {
                  parse_val.lv_elist = parse_vsp[-1].lv_elist;
                  expr_list_append(&parse_val.lv_elist, parse_vsp[0].lv_expr);
                  expr_delete(parse_vsp[0].lv_expr);
             ;
    break;}
case 40: {
                  expr_position_ty pos;
                  pos.pos_name = lex_cur_file();
                  pos.pos_line = lex_cur_line();
                  /*
                   * Purify will find a a memory leak at this
                   * point, if there are any syntax errors.
                   * (E.g. test/01/t0107a.sh)
                   * This is acceptable.
                   */
                  parse_val.lv_expr = expr_constant_new(parse_vsp[0].lv_word, &pos);
                  str_free(parse_vsp[0].lv_word);
             ;
    break;}
case 41: {
                  lex_mode(parse_vsp[-2].lv_number);
                  parse_val.lv_expr = expr_function_new(&parse_vsp[-1].lv_elist);
                  expr_list_destructor(&parse_vsp[-1].lv_elist);
             ;
    break;}
case 42: {
                  parse_val.lv_expr = expr_catenate_new(parse_vsp[-2].lv_expr, parse_vsp[0].lv_expr);
                  expr_delete(parse_vsp[-2].lv_expr);
                  expr_delete(parse_vsp[0].lv_expr);
             ;
    break;}
case 43: {
                  parse_val.lv_stmt = stmt_command_new(&parse_vsp[-2].lv_elist, &parse_vsp[-1].lv_elist, (expr_ty *)0, &parse_vsp[0].lv_position);
                  expr_list_destructor(&parse_vsp[-2].lv_elist);
                  expr_list_destructor(&parse_vsp[-1].lv_elist);
                  expr_position_destructor(&parse_vsp[0].lv_position);
             ;
    break;}
case 44: {
                  parse_val.lv_stmt = stmt_command_new(&parse_vsp[-5].lv_elist, &parse_vsp[-4].lv_elist, parse_vsp[-1].lv_expr, &parse_vsp[-3].lv_position);
                  expr_list_destructor(&parse_vsp[-5].lv_elist);
                  expr_list_destructor(&parse_vsp[-4].lv_elist);
                  expr_position_destructor(&parse_vsp[-3].lv_position);
                  expr_delete(parse_vsp[-1].lv_expr);
                  lex_mode(parse_vsp[-2].lv_number);
             ;
    break;}
case 45: {
                  parse_val.lv_number = lex_mode(LM_DATA);
             ;
    break;}
case 46: {
                  parse_val.lv_number = lex_mode(LM_NORMAL);
             ;
    break;}
case 47: {
                  if (!function_definition(parse_vsp[-2].lv_word, parse_vsp[0].lv_stmt))
                      lex_error(0, i18n("statement failed"));
                  str_free(parse_vsp[-2].lv_word);
                  stmt_delete(parse_vsp[0].lv_stmt);
             ;
    break;}
case 48: {
                  parse_val.lv_stmt = stmt_return_new(&parse_vsp[-1].lv_elist, &parse_vsp[0].lv_position);
                  expr_list_destructor(&parse_vsp[-1].lv_elist);
                  expr_position_destructor(&parse_vsp[0].lv_position);
             ;
    break;}
case 49: {
                  parse_val.lv_stmt = stmt_gosub_new(&parse_vsp[-1].lv_elist, &parse_vsp[0].lv_position);
                  expr_list_destructor(&parse_vsp[-1].lv_elist);
                  expr_position_destructor(&parse_vsp[0].lv_position);
             ;
    break;} }
   /* the action file gets copied in in place of this dollarsign */
  parse_vsp -= parse_len;
  parse_ssp -= parse_len;
#ifdef parse_LSP_NEEDED
  parse_lsp -= parse_len;
#endif
#if parse_DEBUG != 0
  if (parse_debug) {
      short *ssp1 = parse_ss - 1;
      fprintf (stderr, "state stack now");
      while (ssp1 != parse_ssp)
       fprintf (stderr, " %d", *++ssp1);
      fprintf (stderr, "\n"); }
#endif
  *++parse_vsp = parse_val;
#ifdef parse_LSP_NEEDED
  parse_lsp++;
  if (parse_len == 0) {
      parse_lsp->first_line = parse_lloc.first_line;
      parse_lsp->first_column = parse_lloc.first_column;
      parse_lsp->last_line = (parse_lsp-1)->last_line;
      parse_lsp->last_column = (parse_lsp-1)->last_column;
      parse_lsp->text = 0; }
  else {
      parse_lsp->last_line = (parse_lsp+parse_len-1)->last_line;
      parse_lsp->last_column = (parse_lsp+parse_len-1)->last_column; }
#endif
  /* Now "shift" the result of the reduction.
     Determine what state that goes to,
     based on the state we popped back to
     and the rule number reduced by.  */
  parse_n = parse_r1[parse_n];
  parse_state = parse_pgoto[parse_n - parse_NTBASE] + *parse_ssp;
  if (parse_state >= 0 && parse_state <= parse_LAST && parse_check[parse_state] == *parse_ssp)
    parse_state = parse_table[parse_state];
  else
    parse_state = parse_defgoto[parse_n - parse_NTBASE];
  goto parse_newstate;
parse_errlab:   /* here on detecting error */
  if (! parse_errstatus)
    /* If not already recovering from an error, report this error.  */ {
      ++parse_nerrs;
#ifdef parse_ERROR_VERBOSE
      parse_n = parse_pact[parse_state];
      if (parse_n > parse_FLAG && parse_n < parse_LAST) {
         int size = 0;
         char *msg;
         int x, count;
         count = 0;
         /* Start X at -parse_n if nec to avoid negative indexes in parse_check.  */
         for (x = (parse_n < 0 ? -parse_n : 0);
              x < (sizeof(parse_tname) / sizeof(char *)); x++)
           if (parse_check[x + parse_n] == x)
             size += strlen(parse_tname[x]) + 15, count++;
         msg = (char *) malloc(size + 15);
         if (msg != 0) {
             strcpy(msg, "parse error");
             if (count < 5) {
               count = 0;
               for (x = (parse_n < 0 ? -parse_n : 0);
                    x < (sizeof(parse_tname) / sizeof(char *)); x++)
                 if (parse_check[x + parse_n] == x) {
                  strcat(msg, count == 0 ? ", expecting `" : " or `");
                  strcat(msg, parse_tname[x]);
                  strcat(msg, "'");
                  count++; } }
             parse_error(msg);
             free(msg); }
         else
           parse_error ("parse error; also virtual memory exceeded"); }
      else
#endif /* parse_ERROR_VERBOSE */
       parse_error("parse error"); }
  goto parse_errlab1;
parse_errlab1:   /* here on error raised explicitly by an action */
  if (parse_errstatus == 3) {
      /* if just tried and failed to reuse lookahead token after an error, discard it.  */
      /* return failure if at end of input */
      if (parse_char == parse_EOF)
       parse_ABORT;
#if parse_DEBUG != 0
      if (parse_debug)
       fprintf(stderr, "Discarding token %d (%s).\n", parse_char, parse_tname[parse_char1]);
#endif
      parse_char = parse_EMPTY; }
  /* Else will try to reuse lookahead token
     after shifting the error token.  */
  parse_errstatus = 3;         /* Each real token shifted decrements this */
  goto parse_errhandle;
parse_errdefault:  /* current state does not do anything special for the error token. */
#if 0
  /* This is wrong; only states that explicitly want error tokens
     should shift them.  */
  parse_n = parse_defact[parse_state];  /* If its default is to accept any token, ok.  Otherwise pop it.*/
  if (parse_n) goto parse_default;
#endif
parse_errpop:   /* pop the current state because it cannot handle the error token */
  if (parse_ssp == parse_ss) parse_ABORT;
  parse_vsp--;
  parse_state = *--parse_ssp;
#ifdef parse_LSP_NEEDED
  parse_lsp--;
#endif
#if parse_DEBUG != 0
  if (parse_debug) {
      short *ssp1 = parse_ss - 1;
      fprintf (stderr, "Error: state stack now");
      while (ssp1 != parse_ssp)
       fprintf (stderr, " %d", *++ssp1);
      fprintf (stderr, "\n"); }
#endif
parse_errhandle:
  parse_n = parse_pact[parse_state];
  if (parse_n == parse_FLAG)
    goto parse_errdefault;
  parse_n += parse_TERROR;
  if (parse_n < 0 || parse_n > parse_LAST || parse_check[parse_n] != parse_TERROR)
    goto parse_errdefault;
  parse_n = parse_table[parse_n];
  if (parse_n < 0) {
      if (parse_n == parse_FLAG)
       goto parse_errpop;
      parse_n = -parse_n;
      goto parse_reduce; }
  else if (parse_n == 0)
    goto parse_errpop;
  if (parse_n == parse_FINAL)
    parse_ACCEPT;
#if parse_DEBUG != 0
  if (parse_debug)
    fprintf(stderr, "Shifting error token, ");
#endif
  *++parse_vsp = parse_lval;
#ifdef parse_LSP_NEEDED
  *++parse_lsp = parse_lloc;
#endif
  parse_state = parse_n;
  goto parse_newstate; }
