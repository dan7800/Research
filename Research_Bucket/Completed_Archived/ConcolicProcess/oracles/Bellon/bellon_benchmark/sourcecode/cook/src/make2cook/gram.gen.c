/*  A Bison parser, made from make2cook/gram.y with Bison version GNU Bison version 1.22
  */
#define gram_BISON 1  /* Identify Bison output.  */
#define        COLON  258
#define        COLON_COLON    259
#define        COLON_EQUALS   260
#define        COMMAND        261
#define        COMMAND_COMMENT        262
#define        COMMENT        263
#define        DEFINE 264
#define        ELSE   265
#define        EMPTY  266
#define        ENDDEF 267
#define        ENDIF  268
#define        EOLN   269
#define        EQUALS 270
#define        EXPORT 271
#define        IF     272
#define        INCLUDE        273
#define        INCLUDE2       274
#define        INCLUDE3       275
#define        OVERRIDE       276
#define        PLUS_EQUALS    277
#define        UNEXPORT       278
#define        VPATH  279
#define        VPATH2 280
#define        WORD   281
#include <ac/stdio.h>
#include <error_intl.h>
#include <gram.h>
#include <lex.h>
#include <stmt/assign.h>
#include <stmt/blank.h>
#include <stmt/command.h>
#include <stmt/comment.h>
#include <stmt/compound.h>
#include <stmt/define.h>
#include <stmt/export.h>
#include <stmt/if.h>
#include <stmt/include.h>
#include <stmt/rule.h>
#include <stmt/unexport.h>
#include <stmt/vpath.h>
#include <trace.h>
#ifdef DEBUG
#define gram_DEBUG 1
#ifdef gram_BISON
#define fprintf gram_trace2
#else
#define printf trace_where(__FILE__, __LINE__), gram_trace
#endif
extern int gram_debug;
#endif
static stmt_ty *rule_context;
int no_internal_rules;
void
gram(filename)
       char   *filename; {
       int gram_parse _((void));
       trace(("gram(filename = %08lX)\n{\n"/*}*/, (long)filename));
       lex_open(filename);
#if gram_DEBUG
       gram_debug = trace_pretest_;
#endif
       gram_parse();
       lex_close();
       trace((/*{*/"}\n")); }
typedef union {
       blob_ty             *lv_line;
       blob_list_ty  *lv_list;
       stmt_ty             *lv_stmt;
       int     lv_int;
} gram_STYPE;
#ifndef gram_LTYPE
typedef
  struct gram_ltype {
      int timestamp;
      int first_line;
      int first_column;
      int last_line;
      int last_column;
      char *text; }
  gram_ltype;
#define gram_LTYPE gram_ltype
#endif
#ifndef __cplusplus
#ifndef __STDC__
#define const
#endif
#endif
#define        gram_FINAL         109
#define        gram_FLAG           -32768
#define        gram_NTBASE    27
#define gram_TRANSLATE(x) ((unsigned)(x) <= 281 ? gram_translate[x] : 57)
static const char gram_translate[] = {     0,
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
    26
};
#if gram_DEBUG != 0
static const short gram_prhs[] = {     0,
     0,     2,     3,     6,     8,    10,    12,    14,    16,    18,
    20,    22,    25,    30,    36,    42,    45,    48,    50,    52,
    54,    56,    59,    60,    62,    64,    66,    69,    71,    76,
    83,    85,    88,    90,    92,    94,    98,   104,   105,   107,
   109,   111,   115,   121,   125,   128,   131,   134,   136,   140,
   144,   148,   153,   158,   162,   166,   169,   170,   172,   174,
   177,   179
};
static const short gram_rhs[] = {    28,
     0,     0,    28,    29,     0,    30,     0,    48,     0,    34,
     0,    43,     0,    49,     0,    50,     0,    51,     0,    14,
     0,     1,    14,     0,    26,    31,    33,    14,     0,    21,
    26,    31,    33,    14,     0,    16,    26,    31,    33,    14,
     0,    16,    26,     0,    23,    26,     0,    15,     0,    22,
     0,     5,     0,    26,     0,    32,    26,     0,     0,    32,
     0,    35,     0,    36,     0,    36,    38,     0,    37,     0,
    32,    42,    33,    14,     0,    32,    42,    32,    42,    33,
    14,     0,    39,     0,    38,    39,     0,     6,     0,    40,
     0,     7,     0,    44,    41,    47,     0,    44,    41,    46,
    41,    47,     0,     0,    38,     0,     3,     0,     4,     0,
    44,    28,    47,     0,    44,    28,    46,    28,    47,     0,
    17,    33,    14,     0,    33,    14,     0,    10,    45,     0,
    13,    45,     0,     8,     0,    18,    32,    14,     0,    19,
    32,    14,     0,    20,    32,    14,     0,    24,    26,    32,
    14,     0,    25,    31,    33,    14,     0,    52,    54,    53,
     0,     9,    26,    14,     0,    12,    45,     0,     0,    55,
     0,    56,     0,    55,    56,     0,    26,     0,    14,     0
};
#endif
#if gram_DEBUG != 0
static const short gram_rline[] = { 0,
   125,   162,   166,   174,   176,   178,   180,   182,   184,   186,
   188,   190,   195,   199,   203,   217,   221,   228,   230,   232,
   237,   242,   250,   252,   257,   265,   267,   275,   283,   288,
   296,   301,   309,   315,   317,   322,   324,   329,   331,   336,
   338,   343,   347,   354,   364,   380,   384,   388,   393,   395,
   397,   402,   408,   416,   464,   469,   473,   475,   480,   485,
   493,   495
};
static const char * const gram_tname[] = {   "$","error","$illegal.","COLON","COLON_COLON",
"COLON_EQUALS","COMMAND","COMMAND_COMMENT","COMMENT","DEFINE","ELSE","EMPTY",
"ENDDEF","ENDIF","EOLN","EQUALS","EXPORT","IF","INCLUDE","INCLUDE2","INCLUDE3",
"OVERRIDE","PLUS_EQUALS","UNEXPORT","VPATH","VPATH2","WORD","makefile","stmts",
"stmt","assignment","assign_op","word_list","word_list_optional","rule","rule_inner",
"rule_lhs","rule_lhs_inner","commands","command","conditional_commands","optional_commands",
"rule_op","conditional","if","eoln","else","endif","comment","include","vpath",
"define","define_head","define_end","define_list_optional","define_list","define_word",
""
};
#endif
static const short gram_r1[] = {     0,
    27,    28,    28,    29,    29,    29,    29,    29,    29,    29,
    29,    29,    30,    30,    30,    30,    30,    31,    31,    31,
    32,    32,    33,    33,    34,    35,    35,    36,    37,    37,
    38,    38,    39,    39,    39,    40,    40,    41,    41,    42,
    42,    43,    43,    44,    45,    46,    47,    48,    49,    49,
    49,    50,    50,    51,    52,    53,    54,    54,    55,    55,
    56,    56
};
static const short gram_r2[] = {     0,
     1,     0,     2,     1,     1,     1,     1,     1,     1,     1,
     1,     2,     4,     5,     5,     2,     2,     1,     1,     1,
     1,     2,     0,     1,     1,     1,     2,     1,     4,     6,
     1,     2,     1,     1,     1,     3,     5,     0,     1,     1,
     1,     3,     5,     3,     2,     2,     2,     1,     3,     3,
     3,     4,     4,     3,     3,     2,     0,     1,     1,     2,
     1,     1
};
static const short gram_defact[] = {     2,
     0,     0,    48,     0,    11,     0,    23,     0,     0,     0,
     0,     0,     0,     0,    21,     3,     4,     0,     6,    25,
    26,    28,     7,     2,     5,     8,     9,    10,    57,    12,
     0,    16,    21,    24,     0,     0,     0,     0,     0,    17,
     0,    20,    18,    19,    23,    23,    40,    41,    22,    23,
    33,    35,    27,    31,    34,    38,     0,    62,    61,     0,
    58,    59,    55,    23,    44,    49,    50,    51,    23,     0,
     0,     0,    24,     0,    32,    39,     0,    23,    23,     2,
    42,    23,    54,    60,     0,     0,    52,    53,    13,    23,
    29,    38,    36,     0,    46,    47,     0,    56,    15,    14,
     0,     0,    45,    43,    30,    37,     0,     0,     0
};
static const short gram_defgoto[] = {   107,
     1,    16,    17,    45,    34,    94,    19,    20,    21,    22,
    76,    54,    55,    77,    50,    23,    56,    95,    80,    81,
    25,    26,    27,    28,    29,    83,    60,    61,    62
};
static const short gram_pact[] = {-32768,
     4,     1,-32768,    -9,-32768,    13,    17,    17,    17,    17,
    19,    21,    27,    29,    29,-32768,-32768,     7,-32768,-32768,
   107,-32768,-32768,-32768,-32768,-32768,-32768,-32768,    22,-32768,
    18,    29,-32768,    38,    41,    24,    54,    67,    29,-32768,
    17,-32768,-32768,-32768,    17,    17,-32768,-32768,-32768,    17,
-32768,-32768,   107,-32768,-32768,   107,    53,-32768,-32768,    63,
    22,-32768,-32768,    17,-32768,-32768,-32768,-32768,    17,    75,
    69,    70,     7,    74,-32768,   107,     6,    17,    17,-32768,
-32768,    17,-32768,-32768,    76,    77,-32768,-32768,-32768,    17,
-32768,   107,-32768,    78,-32768,-32768,    86,-32768,-32768,-32768,
    84,    95,-32768,-32768,-32768,-32768,   115,   116,-32768
};
static const short gram_pgoto[] = {-32768,
   -22,-32768,-32768,    20,    -1,    -4,-32768,-32768,-32768,-32768,
    96,   -39,-32768,    26,    46,-32768,     0,     3,    43,   -71,
-32768,-32768,-32768,-32768,-32768,-32768,-32768,-32768,    60
};
#define        gram_LAST           124
static const short gram_table[] = {    18,
    24,    57,    35,    -1,     2,    93,    36,    37,    38,    47,
    48,     3,     4,    75,    30,    78,    31,     5,    79,     6,
     7,     8,     9,    10,    11,   104,    12,    13,    14,    15,
   106,    63,    49,    42,    46,    58,    75,    66,    32,    70,
    71,    72,    33,    43,    39,    74,    40,    59,    73,    49,
    44,    64,    41,     2,    65,    18,    24,    97,    69,    85,
     3,     4,    78,    49,    86,    79,     5,    67,     6,     7,
     8,     9,    10,    11,    82,    12,    13,    14,    15,    49,
    68,    96,    88,    89,    98,   101,     2,    91,    87,    99,
   100,   103,    49,     3,     4,    18,    24,   105,    79,     5,
    49,     6,     7,     8,     9,    10,    11,    79,    12,    13,
    14,    15,    51,    52,   108,   109,    53,   102,    90,    92,
    84,     0,     0,     7
};
static const short gram_check[] = {     1,
     1,    24,     7,     0,     1,    77,     8,     9,    10,     3,
     4,     8,     9,    53,    14,    10,    26,    14,    13,    16,
    17,    18,    19,    20,    21,    97,    23,    24,    25,    26,
   102,    14,    26,     5,    15,    14,    76,    14,    26,    41,
    45,    46,    26,    15,    26,    50,    26,    26,    50,    26,
    22,    32,    26,     1,    14,    57,    57,    80,    39,    64,
     8,     9,    10,    26,    69,    13,    14,    14,    16,    17,
    18,    19,    20,    21,    12,    23,    24,    25,    26,    26,
    14,    79,    14,    14,    82,    90,     1,    14,    14,    14,
    14,    14,    26,     8,     9,    97,    97,    14,    13,    14,
    26,    16,    17,    18,    19,    20,    21,    13,    23,    24,
    25,    26,     6,     7,     0,     0,    21,    92,    73,    77,
    61,    -1,    -1,    17
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
#define gram_errok         (gram_errstatus = 0)
#define gram_clearin   (gram_char = gram_EMPTY)
#define gram_EMPTY         -2
#define gram_EOF             0
#define gram_ACCEPT    return(0)
#define gram_ABORT     return(1)
#define gram_ERROR         goto gram_errlab1
/* Like gram_ERROR except do call gram_error.
   This remains here temporarily to ease the
   transition to the new meaning of gram_ERROR, for GCC.
   Once GCC version 2 has supplanted version 1, this can go.  */
#define gram_FAIL           goto gram_errlab
#define gram_RECOVERING()  (!!gram_errstatus)
#define gram_BACKUP(token, value) \
do                                    \
  if (gram_char == gram_EMPTY && gram_len == 1)                          \
    { gram_char = (token), gram_lval = (value);                     \
      gram_char1 = gram_TRANSLATE (gram_char);                      \
      gram_POPSTACK;                           \
      goto gram_backup;                                 \
    }                                    \
  else                                    \
    { gram_error ("syntax error: cannot back up"); gram_ERROR; }       \
while (0)
#define gram_TERROR    1
#define gram_ERRCODE   256
#ifndef gram_PURE
#define gram_LEX             gram_lex()
#endif
#ifdef gram_PURE
#ifdef gram_LSP_NEEDED
#define gram_LEX             gram_lex(&gram_lval, &gram_lloc)
#else
#define gram_LEX             gram_lex(&gram_lval)
#endif
#endif
/* If nonreentrant, generate the variables here */
#ifndef gram_PURE
int    gram_char;                /*  the lookahead symbol           */
gram_STYPE     gram_lval;           /*  the semantic value of the   */
                      /*  lookahead symbol                  */
#ifdef gram_LSP_NEEDED
gram_LTYPE gram_lloc;           /*  location data for the lookahead    */
                      /*  symbol                      */
#endif
int gram_nerrs;                     /*  number of parse errors so far       */
#endif  /* not gram_PURE */
#if gram_DEBUG != 0
int gram_debug;                     /*  nonzero means print parse trace  */
/* Since this is uninitialized, it does not stop multiple parsers
   from coexisting.  */
#endif
/*  gram_INITDEPTH indicates the initial size of the parser's stacks   */
#ifndef        gram_INITDEPTH
#define gram_INITDEPTH 200
#endif
/*  gram_MAXDEPTH is the maximum size the stacks can grow to
    (effective only if the built-in stack extension method is used).  */
#if gram_MAXDEPTH == 0
#undef gram_MAXDEPTH
#endif
#ifndef gram_MAXDEPTH
#define gram_MAXDEPTH 10000
#endif
/* Prevent warning if -Wstrict-prototypes.  */
#ifdef __GNUC__
int gram_parse (void);
#endif
#if __GNUC__ > 1             /* GNU C and GNU C++ define this.  */
#define __gram__bcopy(FROM,TO,COUNT)   __builtin_memcpy(TO,FROM,COUNT)
#else                  /* not GNU C or C++ */
#ifndef __cplusplus
/* This is the most reliable way to avoid incompatibilities
   in available built-in functions on various systems.  */
static void
__gram__bcopy (from, to, count)
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
__gram__bcopy (char *from, char *to, int count) {
  register char *f = from;
  register char *t = to;
  register int i = count;
  while (i-- > 0)
    *t++ = *f++; }
#endif
#endif
int
gram_parse() {
  register int gram_state;
  register int gram_n;
  register short *gram_ssp;
  register gram_STYPE *gram_vsp;
  int gram_errstatus;  /*  number of tokens to shift before error messages enabled */
  int gram_char1;           /*  lookahead token as an internal (translated) token number */
  short        gram_ssa[gram_INITDEPTH];      /*  the state stack           */
  gram_STYPE gram_vsa[gram_INITDEPTH]; /*  the semantic value stack       */
  short *gram_ss = gram_ssa;     /*  refer to the stacks thru separate pointers */
  gram_STYPE *gram_vs = gram_vsa;      /*  to allow gram_overflow to reallocate them elsewhere */
#ifdef gram_LSP_NEEDED
  gram_LTYPE gram_lsa[gram_INITDEPTH]; /*  the location stack           */
  gram_LTYPE *gram_ls = gram_lsa;
  gram_LTYPE *gram_lsp;
#define gram_POPSTACK   (gram_vsp--, gram_ssp--, gram_lsp--)
#else
#define gram_POPSTACK   (gram_vsp--, gram_ssp--)
#endif
  int gram_stacksize = gram_INITDEPTH;
#ifdef gram_PURE
  int gram_char;
  gram_STYPE gram_lval;
  int gram_nerrs;
#ifdef gram_LSP_NEEDED
  gram_LTYPE gram_lloc;
#endif
#endif
  gram_STYPE gram_val;         /*  the variable used to return               */
                      /*  semantic values from the action        */
                      /*  routines                      */
  int gram_len;
#if gram_DEBUG != 0
  if (gram_debug)
    fprintf(stderr, "Starting parse\n");
#endif
  gram_state = 0;
  gram_errstatus = 0;
  gram_nerrs = 0;
  gram_char = gram_EMPTY;           /* Cause a token to be read.  */
  /* Initialize stack pointers.
     Waste one element of value and location stack
     so that they stay on the same level as the state stack.
     The wasted elements are never initialized.  */
  gram_ssp = gram_ss - 1;
  gram_vsp = gram_vs;
#ifdef gram_LSP_NEEDED
  gram_lsp = gram_ls;
#endif
/* Push a new state, which is found in  gram_state  .  */
/* In all cases, when you get here, the value and location stacks
   have just been pushed. so pushing a state here evens the stacks.  */
gram_newstate:
  *++gram_ssp = gram_state;
  if (gram_ssp >= gram_ss + gram_stacksize - 1) {
      /* Give user a chance to reallocate the stack */
      /* Use copies of these so that the &'s don't force the real ones into memory. */
      gram_STYPE *gram_vs1 = gram_vs;
      short *gram_ss1 = gram_ss;
#ifdef gram_LSP_NEEDED
      gram_LTYPE *gram_ls1 = gram_ls;
#endif
      /* Get the current used size of the three stacks, in elements.  */
      int size = gram_ssp - gram_ss + 1;
#ifdef gram_overflow
      /* Each stack pointer address is followed by the size of
        the data in use in that stack, in bytes.  */
      gram_overflow("parser stack overflow",
              &gram_ss1, size * sizeof (*gram_ssp),
              &gram_vs1, size * sizeof (*gram_vsp),
#ifdef gram_LSP_NEEDED
              &gram_ls1, size * sizeof (*gram_lsp),
#endif
              &gram_stacksize);
      gram_ss = gram_ss1; gram_vs = gram_vs1;
#ifdef gram_LSP_NEEDED
      gram_ls = gram_ls1;
#endif
#else /* no gram_overflow */
      /* Extend the stack our own way.  */
      if (gram_stacksize >= gram_MAXDEPTH) {
         gram_error("parser stack overflow");
         return 2; }
      gram_stacksize *= 2;
      if (gram_stacksize > gram_MAXDEPTH)
       gram_stacksize = gram_MAXDEPTH;
      gram_ss = (short *) alloca (gram_stacksize * sizeof (*gram_ssp));
      __gram__bcopy ((char *)gram_ss1, (char *)gram_ss, size * sizeof (*gram_ssp));
      gram_vs = (gram_STYPE *) alloca (gram_stacksize * sizeof (*gram_vsp));
      __gram__bcopy ((char *)gram_vs1, (char *)gram_vs, size * sizeof (*gram_vsp));
#ifdef gram_LSP_NEEDED
      gram_ls = (gram_LTYPE *) alloca (gram_stacksize * sizeof (*gram_lsp));
      __gram__bcopy ((char *)gram_ls1, (char *)gram_ls, size * sizeof (*gram_lsp));
#endif
#endif /* no gram_overflow */
      gram_ssp = gram_ss + size - 1;
      gram_vsp = gram_vs + size - 1;
#ifdef gram_LSP_NEEDED
      gram_lsp = gram_ls + size - 1;
#endif
#if gram_DEBUG != 0
      if (gram_debug)
       fprintf(stderr, "Stack size increased to %d\n", gram_stacksize);
#endif
      if (gram_ssp >= gram_ss + gram_stacksize - 1)
       gram_ABORT; }
#if gram_DEBUG != 0
  if (gram_debug)
    fprintf(stderr, "Entering state %d\n", gram_state);
#endif
  goto gram_backup;
 gram_backup:
/* Do appropriate processing given the current state.  */
/* Read a lookahead token if we need one and don't already have one.  */
/* gram_resume: */
  /* First try to decide what to do without reference to lookahead token.  */
  gram_n = gram_pact[gram_state];
  if (gram_n == gram_FLAG)
    goto gram_default;
  /* Not known => get a lookahead token if don't already have one.  */
  /* gram_char is either gram_EMPTY or gram_EOF
     or a valid token in external form.  */
  if (gram_char == gram_EMPTY) {
#if gram_DEBUG != 0
      if (gram_debug)
       fprintf(stderr, "Reading a token: ");
#endif
      gram_char = gram_LEX; }
  /* Convert token to internal form (in gram_char1) for indexing tables with */
  if (gram_char <= 0)   /* This means end of input. */ {
      gram_char1 = 0;
      gram_char = gram_EOF;       /* Don't call gram_LEX any more */
#if gram_DEBUG != 0
      if (gram_debug)
       fprintf(stderr, "Now at end of input.\n");
#endif
    }
  else {
      gram_char1 = gram_TRANSLATE(gram_char);
#if gram_DEBUG != 0
      if (gram_debug) {
         fprintf (stderr, "Next token is %d (%s", gram_char, gram_tname[gram_char1]);
         /* Give the individual parser a way to print the precise meaning
            of a token, for further debugging info.  */
#ifdef gram_PRINT
         gram_PRINT (stderr, gram_char, gram_lval);
#endif
         fprintf (stderr, ")\n"); }
#endif
    }
  gram_n += gram_char1;
  if (gram_n < 0 || gram_n > gram_LAST || gram_check[gram_n] != gram_char1)
    goto gram_default;
  gram_n = gram_table[gram_n];
  /* gram_n is what to do for this token type in this state.
     Negative => reduce, -gram_n is rule number.
     Positive => shift, gram_n is new state.
       New state is final state => don't bother to shift,
       just return success.
     0, or most negative number => error.  */
  if (gram_n < 0) {
      if (gram_n == gram_FLAG)
       goto gram_errlab;
      gram_n = -gram_n;
      goto gram_reduce; }
  else if (gram_n == 0)
    goto gram_errlab;
  if (gram_n == gram_FINAL)
    gram_ACCEPT;
  /* Shift the lookahead token.  */
#if gram_DEBUG != 0
  if (gram_debug)
    fprintf(stderr, "Shifting token %d (%s), ", gram_char, gram_tname[gram_char1]);
#endif
  /* Discard the token being shifted unless it is eof.  */
  if (gram_char != gram_EOF)
    gram_char = gram_EMPTY;
  *++gram_vsp = gram_lval;
#ifdef gram_LSP_NEEDED
  *++gram_lsp = gram_lloc;
#endif
  /* count tokens shifted since error; after three, turn off error status.  */
  if (gram_errstatus) gram_errstatus--;
  gram_state = gram_n;
  goto gram_newstate;
/* Do the default action for the current state.  */
gram_default:
  gram_n = gram_defact[gram_state];
  if (gram_n == 0)
    goto gram_errlab;
/* Do a reduction.  gram_n is the number of a rule to reduce with.  */
gram_reduce:
  gram_len = gram_r2[gram_n];
  gram_val = gram_vsp[1-gram_len]; /* implement default value of the action */
#if gram_DEBUG != 0
  if (gram_debug) {
      int i;
      fprintf (stderr, "Reducing via rule %d (line %d), ",
              gram_n, gram_rline[gram_n]);
      /* Print the symbols being reduced, and their result.  */
      for (i = gram_prhs[gram_n]; gram_rhs[i] > 0; i++)
       fprintf (stderr, "%s ", gram_tname[gram_rhs[i]]);
      fprintf (stderr, " -> %s\n", gram_tname[gram_r1[gram_n]]); }
#endif
  switch (gram_n) {
case 1: {
                  int j;
                  stmt_ty     *s;
                  stmt_regroup(gram_vsp[0].lv_stmt);
                  s = stmt_vpath_default();
                  if (s)
                      stmt_compound_append(gram_vsp[0].lv_stmt, s);
                  if (!no_internal_rules) {
                      for (j = 0; ; ++j) {
                         s = stmt_rule_default(j);
                         if (!s)
                           break;
                         stmt_compound_append(gram_vsp[0].lv_stmt, s); } }
                  for (j = 0; ; ++j) {
                      s = stmt_assign_default(gram_vsp[0].lv_stmt);
                      if (!s)
                         break;
                      stmt_compound_prepend(gram_vsp[0].lv_stmt, s); }
                  stmt_sort(gram_vsp[0].lv_stmt);
                  stmt_emit(gram_vsp[0].lv_stmt);
                  stmt_free(gram_vsp[0].lv_stmt);
             ;
    break;}
case 2: {
                  gram_val.lv_stmt = stmt_compound_alloc();
             ;
    break;}
case 3: {
                  gram_val.lv_stmt = gram_vsp[-1].lv_stmt;
                  stmt_compound_append(gram_val.lv_stmt, gram_vsp[0].lv_stmt);
             ;
    break;}
case 4:
{ gram_val.lv_stmt = gram_vsp[0].lv_stmt; ;
    break;}
case 5:
{ gram_val.lv_stmt = gram_vsp[0].lv_stmt; ;
    break;}
case 6:
{ gram_val.lv_stmt = gram_vsp[0].lv_stmt; ;
    break;}
case 7:
{ gram_val.lv_stmt = gram_vsp[0].lv_stmt; ;
    break;}
case 8:
{ gram_val.lv_stmt = gram_vsp[0].lv_stmt; ;
    break;}
case 9:
{ gram_val.lv_stmt = gram_vsp[0].lv_stmt; ;
    break;}
case 10:
{ gram_val.lv_stmt = gram_vsp[0].lv_stmt; ;
    break;}
case 11:
{ gram_val.lv_stmt = stmt_blank_alloc(); ;
    break;}
case 12:
{ gram_val.lv_stmt = stmt_blank_alloc(); ;
    break;}
case 13: {
                  gram_val.lv_stmt = stmt_assign_alloc(0, gram_vsp[-3].lv_line, gram_vsp[-2].lv_int, gram_vsp[-1].lv_list);
             ;
    break;}
case 14: {
                  gram_val.lv_stmt = stmt_assign_alloc(1, gram_vsp[-3].lv_line, gram_vsp[-2].lv_int, gram_vsp[-1].lv_list);
             ;
    break;}
case 15: {
                  gram_val.lv_stmt = stmt_compound_alloc();
                  stmt_compound_append
                  (
                      gram_val.lv_stmt,
                      stmt_assign_alloc(1, blob_copy(gram_vsp[-3].lv_line), gram_vsp[-2].lv_int, gram_vsp[-1].lv_list)
                  );
                  stmt_compound_append
                  (
                      gram_val.lv_stmt,
                      stmt_export_alloc(gram_vsp[-3].lv_line)
                  );
             ;
    break;}
case 16: {
                  gram_val.lv_stmt = stmt_export_alloc(gram_vsp[0].lv_line);
             ;
    break;}
case 17: {
                  gram_val.lv_stmt = stmt_unexport_alloc(gram_vsp[0].lv_line);
             ;
    break;}
case 18:
{ gram_val.lv_int = stmt_assign_op_normal; ;
    break;}
case 19:
{ gram_val.lv_int = stmt_assign_op_plus; ;
    break;}
case 20:
{ gram_val.lv_int = stmt_assign_op_colon; ;
    break;}
case 21: {
                  gram_val.lv_list = blob_list_alloc();
                  blob_list_append(gram_val.lv_list, gram_vsp[0].lv_line);
             ;
    break;}
case 22: {
                  gram_val.lv_list = gram_vsp[-1].lv_list;
                  blob_list_append(gram_val.lv_list, gram_vsp[0].lv_line);
             ;
    break;}
case 23:
{ gram_val.lv_list = blob_list_alloc(); ;
    break;}
case 24:
{ gram_val.lv_list = gram_vsp[0].lv_list; ;
    break;}
case 25: {
                  gram_val.lv_stmt = gram_vsp[0].lv_stmt;
                  rule_context = 0;
             ;
    break;}
case 26:
{ gram_val.lv_stmt = gram_vsp[0].lv_stmt; ;
    break;}
case 27: {
                  gram_val.lv_stmt = gram_vsp[-1].lv_stmt;
                  stmt_rule_body(gram_val.lv_stmt, gram_vsp[0].lv_stmt);
             ;
    break;}
case 28: {
                  gram_val.lv_stmt = gram_vsp[0].lv_stmt;
                  rule_context = gram_val.lv_stmt;
             ;
    break;}
case 29: {
                  gram_val.lv_stmt = stmt_rule_alloc(gram_vsp[-3].lv_list, gram_vsp[-2].lv_int, gram_vsp[-1].lv_list, (blob_list_ty *)0,
                      (blob_list_ty *)0, (blob_list_ty *)0);
             ;
    break;}
case 30: {
                  gram_val.lv_stmt = stmt_rule_alloc(gram_vsp[-3].lv_list, gram_vsp[-2].lv_int, gram_vsp[-1].lv_list, (blob_list_ty *)0,
                      gram_vsp[-5].lv_list, (blob_list_ty *)0);
             ;
    break;}
case 31: {
                  gram_val.lv_stmt = stmt_compound_alloc();
                  stmt_compound_append(gram_val.lv_stmt, gram_vsp[0].lv_stmt);
             ;
    break;}
case 32: {
                  gram_val.lv_stmt = gram_vsp[-1].lv_stmt;
                  stmt_compound_append(gram_val.lv_stmt, gram_vsp[0].lv_stmt);
             ;
    break;}
case 33: {
                  if (rule_context)
                      stmt_rule_context(rule_context);
                  gram_val.lv_stmt = stmt_command_alloc(gram_vsp[0].lv_line);
             ;
    break;}
case 34:
{ gram_val.lv_stmt = gram_vsp[0].lv_stmt; ;
    break;}
case 35:
{ gram_val.lv_stmt = stmt_comment_alloc(gram_vsp[0].lv_line); ;
    break;}
case 36:
{ gram_val.lv_stmt = stmt_if_alloc(gram_vsp[-2].lv_list, gram_vsp[-1].lv_stmt, (stmt_ty *)0); ;
    break;}
case 37:
{ gram_val.lv_stmt = stmt_if_alloc(gram_vsp[-4].lv_list, gram_vsp[-3].lv_stmt, gram_vsp[-1].lv_stmt); ;
    break;}
case 38:
{ gram_val.lv_stmt = stmt_compound_alloc(); ;
    break;}
case 39:
{ gram_val.lv_stmt = gram_vsp[0].lv_stmt; ;
    break;}
case 40:
{ gram_val.lv_int = 1; ;
    break;}
case 41:
{ gram_val.lv_int = 2; ;
    break;}
case 42: {
                  gram_val.lv_stmt = stmt_if_alloc(gram_vsp[-2].lv_list, gram_vsp[-1].lv_stmt, (stmt_ty *)0);
             ;
    break;}
case 43: {
                  gram_val.lv_stmt = stmt_if_alloc(gram_vsp[-4].lv_list, gram_vsp[-3].lv_stmt, gram_vsp[-1].lv_stmt);
             ;
    break;}
case 44: {
                  gram_val.lv_list = gram_vsp[-1].lv_list;
                  if (rule_context)
                      stmt_rule_context(rule_context);
                  blob_list_prepend(gram_val.lv_list, gram_vsp[-2].lv_line);
             ;
    break;}
case 45: {
                  if (gram_vsp[-1].lv_list->length) {
                      blob_error
                      (
                         gram_vsp[-1].lv_list->list[0],
                         0,
                         i18n("garbage on end of line")
                      ); }
                  blob_list_free(gram_vsp[-1].lv_list);
             ;
    break;}
case 48:
{ gram_val.lv_stmt = stmt_comment_alloc(gram_vsp[0].lv_line); ;
    break;}
case 49:
{ gram_val.lv_stmt = stmt_include_alloc(gram_vsp[-1].lv_list, 1); ;
    break;}
case 50:
{ gram_val.lv_stmt = stmt_include_alloc(gram_vsp[-1].lv_list, 2); ;
    break;}
case 51:
{ gram_val.lv_stmt = stmt_include_alloc(gram_vsp[-1].lv_list, 3); ;
    break;}
case 52: {
                  blob_free(gram_vsp[-2].lv_line);
                  stmt_vpath_remember1(gram_vsp[-1].lv_list);
                  gram_val.lv_stmt = stmt_blank_alloc();
             ;
    break;}
case 53: {
                  stmt_vpath_remember2(gram_vsp[-1].lv_list);
                  gram_val.lv_stmt = stmt_blank_alloc();
             ;
    break;}
case 54: {
                  size_t      j;
                  /*
                   * append newline to all but the last
                   */
                  for (j = 1; j < gram_vsp[-1].lv_list->length; ++j) {
                      string_ty  *s;
                      s = gram_vsp[-1].lv_list->list[j - 1]->text;
                      gram_vsp[-1].lv_list->list[j - 1]->text = str_format("%S\n", s);
                      str_free(s); }
                  /*
                   * Special case the last string if it is empty.
                   * The last string will be empty if the user
                   * wanted a trailing newline.  Cook can say this
                   * more elegantly.
                   */
                  if
                  (
                      gram_vsp[-1].lv_list->length
                  &&
                      gram_vsp[-1].lv_list->list[gram_vsp[-1].lv_list->length - 1]->text->str_length == 0
                  ) {
                      gram_vsp[-1].lv_list->length--;
                      blob_free(gram_vsp[-1].lv_list->list[gram_vsp[-1].lv_list->length]); }
                  /*
                   * now treat it as a normal assignment
                   */
                  gram_val.lv_stmt =
                      stmt_assign_alloc
                      (
                         0,
                         gram_vsp[-2].lv_line,
                         stmt_assign_op_normal,
                         gram_vsp[-1].lv_list
                      );
             ;
    break;}
case 55:
{ gram_val.lv_line = gram_vsp[-1].lv_line; ;
    break;}
case 57:
{ gram_val.lv_list = blob_list_alloc(); ;
    break;}
case 58:
{ gram_val.lv_list = gram_vsp[0].lv_list; ;
    break;}
case 59: {
                  gram_val.lv_list = blob_list_alloc();
                  blob_list_append(gram_val.lv_list, gram_vsp[0].lv_line);
             ;
    break;}
case 60: {
                  gram_val.lv_list = gram_vsp[-1].lv_list;
                  blob_list_append(gram_val.lv_list, gram_vsp[0].lv_line);
             ;
    break;}
case 61:
{ gram_val.lv_line = gram_vsp[0].lv_line; ;
    break;}
case 62:
{ gram_val.lv_line = lex_blob(str_from_c("")); ;
    break;} }
   /* the action file gets copied in in place of this dollarsign */
  gram_vsp -= gram_len;
  gram_ssp -= gram_len;
#ifdef gram_LSP_NEEDED
  gram_lsp -= gram_len;
#endif
#if gram_DEBUG != 0
  if (gram_debug) {
      short *ssp1 = gram_ss - 1;
      fprintf (stderr, "state stack now");
      while (ssp1 != gram_ssp)
       fprintf (stderr, " %d", *++ssp1);
      fprintf (stderr, "\n"); }
#endif
  *++gram_vsp = gram_val;
#ifdef gram_LSP_NEEDED
  gram_lsp++;
  if (gram_len == 0) {
      gram_lsp->first_line = gram_lloc.first_line;
      gram_lsp->first_column = gram_lloc.first_column;
      gram_lsp->last_line = (gram_lsp-1)->last_line;
      gram_lsp->last_column = (gram_lsp-1)->last_column;
      gram_lsp->text = 0; }
  else {
      gram_lsp->last_line = (gram_lsp+gram_len-1)->last_line;
      gram_lsp->last_column = (gram_lsp+gram_len-1)->last_column; }
#endif
  /* Now "shift" the result of the reduction.
     Determine what state that goes to,
     based on the state we popped back to
     and the rule number reduced by.  */
  gram_n = gram_r1[gram_n];
  gram_state = gram_pgoto[gram_n - gram_NTBASE] + *gram_ssp;
  if (gram_state >= 0 && gram_state <= gram_LAST && gram_check[gram_state] == *gram_ssp)
    gram_state = gram_table[gram_state];
  else
    gram_state = gram_defgoto[gram_n - gram_NTBASE];
  goto gram_newstate;
gram_errlab:   /* here on detecting error */
  if (! gram_errstatus)
    /* If not already recovering from an error, report this error.  */ {
      ++gram_nerrs;
#ifdef gram_ERROR_VERBOSE
      gram_n = gram_pact[gram_state];
      if (gram_n > gram_FLAG && gram_n < gram_LAST) {
         int size = 0;
         char *msg;
         int x, count;
         count = 0;
         /* Start X at -gram_n if nec to avoid negative indexes in gram_check.  */
         for (x = (gram_n < 0 ? -gram_n : 0);
              x < (sizeof(gram_tname) / sizeof(char *)); x++)
           if (gram_check[x + gram_n] == x)
             size += strlen(gram_tname[x]) + 15, count++;
         msg = (char *) malloc(size + 15);
         if (msg != 0) {
             strcpy(msg, "parse error");
             if (count < 5) {
               count = 0;
               for (x = (gram_n < 0 ? -gram_n : 0);
                    x < (sizeof(gram_tname) / sizeof(char *)); x++)
                 if (gram_check[x + gram_n] == x) {
                  strcat(msg, count == 0 ? ", expecting `" : " or `");
                  strcat(msg, gram_tname[x]);
                  strcat(msg, "'");
                  count++; } }
             gram_error(msg);
             free(msg); }
         else
           gram_error ("parse error; also virtual memory exceeded"); }
      else
#endif /* gram_ERROR_VERBOSE */
       gram_error("parse error"); }
  goto gram_errlab1;
gram_errlab1:   /* here on error raised explicitly by an action */
  if (gram_errstatus == 3) {
      /* if just tried and failed to reuse lookahead token after an error, discard it.  */
      /* return failure if at end of input */
      if (gram_char == gram_EOF)
       gram_ABORT;
#if gram_DEBUG != 0
      if (gram_debug)
       fprintf(stderr, "Discarding token %d (%s).\n", gram_char, gram_tname[gram_char1]);
#endif
      gram_char = gram_EMPTY; }
  /* Else will try to reuse lookahead token
     after shifting the error token.  */
  gram_errstatus = 3;   /* Each real token shifted decrements this */
  goto gram_errhandle;
gram_errdefault:  /* current state does not do anything special for the error token. */
#if 0
  /* This is wrong; only states that explicitly want error tokens
     should shift them.  */
  gram_n = gram_defact[gram_state];  /* If its default is to accept any token, ok.  Otherwise pop it.*/
  if (gram_n) goto gram_default;
#endif
gram_errpop:   /* pop the current state because it cannot handle the error token */
  if (gram_ssp == gram_ss) gram_ABORT;
  gram_vsp--;
  gram_state = *--gram_ssp;
#ifdef gram_LSP_NEEDED
  gram_lsp--;
#endif
#if gram_DEBUG != 0
  if (gram_debug) {
      short *ssp1 = gram_ss - 1;
      fprintf (stderr, "Error: state stack now");
      while (ssp1 != gram_ssp)
       fprintf (stderr, " %d", *++ssp1);
      fprintf (stderr, "\n"); }
#endif
gram_errhandle:
  gram_n = gram_pact[gram_state];
  if (gram_n == gram_FLAG)
    goto gram_errdefault;
  gram_n += gram_TERROR;
  if (gram_n < 0 || gram_n > gram_LAST || gram_check[gram_n] != gram_TERROR)
    goto gram_errdefault;
  gram_n = gram_table[gram_n];
  if (gram_n < 0) {
      if (gram_n == gram_FLAG)
       goto gram_errpop;
      gram_n = -gram_n;
      goto gram_reduce; }
  else if (gram_n == 0)
    goto gram_errpop;
  if (gram_n == gram_FINAL)
    gram_ACCEPT;
#if gram_DEBUG != 0
  if (gram_debug)
    fprintf(stderr, "Shifting error token, ");
#endif
  *++gram_vsp = gram_lval;
#ifdef gram_LSP_NEEDED
  *++gram_lsp = gram_lloc;
#endif
  gram_state = gram_n;
  goto gram_newstate; }
