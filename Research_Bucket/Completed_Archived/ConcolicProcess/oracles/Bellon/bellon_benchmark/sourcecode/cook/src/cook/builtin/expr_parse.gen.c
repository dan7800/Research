/*  A Bison parser, made from cook/builtin/expr_parse.y with Bison version GNU Bison version 1.22
  */
#define builtin_expr_parse_BISON 1  /* Identify Bison output.  */
#define        BIT_AND        258
#define        BIT_NOT        259
#define        BIT_OR 260
#define        BIT_XOR        261
#define        COLON  262
#define        DIV    263
#define        EQ     264
#define        GE     265
#define        GT     266
#define        JUNK   267
#define        LE     268
#define        LOGIC_AND      269
#define        LOGIC_NOT      270
#define        LOGIC_OR       271
#define        LP     272
#define        LT     273
#define        MINUS  274
#define        MOD    275
#define        MUL    276
#define        NE     277
#define        NUMBER 278
#define        PLUS   279
#define        QUEST  280
#define        RP     281
#define        SHIFT_L        282
#define        SHIFT_R        283
typedef union {
       long  lv_integer;
} builtin_expr_parse_STYPE;
#include <builtin/expr_lex.h>
#include <builtin/expr_parse.h>
#include <expr/position.h>
#include <str.h>
#include <str_list.h>
#include <sub.h>
static string_ty *result;
void
builtin_expr_parse_begin(args, pp)
       const string_list_ty *args;
       const expr_position_ty *pp; {
       builtin_expr_lex_open(args, pp);
       if (result) {
             str_free(result);
             result = 0; } }
string_ty *
builtin_expr_parse_end() {
       string_ty     *s;
       builtin_expr_lex_close();
       s = result;
       result = 0;
       if (!s)
             s = str_from_c("0");
       return s; }
#ifndef builtin_expr_parse_LTYPE
typedef
  struct builtin_expr_parse_ltype {
      int timestamp;
      int first_line;
      int first_column;
      int last_line;
      int last_column;
      char *text; }
  builtin_expr_parse_ltype;
#define builtin_expr_parse_LTYPE builtin_expr_parse_ltype
#endif
#ifndef __cplusplus
#ifndef __STDC__
#define const
#endif
#endif
#define        builtin_expr_parse_FINAL             56
#define        builtin_expr_parse_FLAG               -32768
#define        builtin_expr_parse_NTBASE      29
#define builtin_expr_parse_TRANSLATE(x) ((unsigned)(x) <= 283 ? builtin_expr_parse_translate[x] : 31)
static const char builtin_expr_parse_translate[] = {     0,
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
#if builtin_expr_parse_DEBUG != 0
static const short builtin_expr_parse_prhs[] = {     0,
     0,     2,     4,     8,    11,    14,    17,    20,    24,    28,
    32,    36,    40,    44,    48,    52,    56,    60,    64,    68,
    72,    76,    80,    84,    88,    92
};
static const short builtin_expr_parse_rhs[] = {    30,
     0,    23,     0,    17,    30,    26,     0,    24,    30,     0,
    19,    30,     0,    15,    30,     0,     4,    30,     0,    30,
    21,    30,     0,    30,     8,    30,     0,    30,    20,    30,
     0,    30,    24,    30,     0,    30,    19,    30,     0,    30,
    27,    30,     0,    30,    28,    30,     0,    30,    18,    30,
     0,    30,    13,    30,     0,    30,    11,    30,     0,    30,
    10,    30,     0,    30,     9,    30,     0,    30,    22,    30,
     0,    30,     3,    30,     0,    30,     6,    30,     0,    30,
     5,    30,     0,    30,    14,    30,     0,    30,    16,    30,
     0,    30,    25,    30,     7,    30,     0
};
#endif
#if builtin_expr_parse_DEBUG != 0
static const short builtin_expr_parse_rline[] = { 0,
   115,   124,   126,   128,   131,   134,   136,   138,   140,   150,
   160,   162,   164,   166,   168,   170,   172,   174,   176,   178,
   180,   182,   184,   186,   188,   190
};
static const char * const builtin_expr_parse_tname[] = {   "$","error","$illegal.","BIT_AND",
"BIT_NOT","BIT_OR","BIT_XOR","COLON","DIV","EQ","GE","GT","JUNK","LE","LOGIC_AND",
"LOGIC_NOT","LOGIC_OR","LP","LT","MINUS","MOD","MUL","NE","NUMBER","PLUS","QUEST",
"RP","SHIFT_L","SHIFT_R","main","expr",""
};
#endif
static const short builtin_expr_parse_r1[] = {     0,
    29,    30,    30,    30,    30,    30,    30,    30,    30,    30,
    30,    30,    30,    30,    30,    30,    30,    30,    30,    30,
    30,    30,    30,    30,    30,    30
};
static const short builtin_expr_parse_r2[] = {     0,
     1,     1,     3,     2,     2,     2,     2,     3,     3,     3,
     3,     3,     3,     3,     3,     3,     3,     3,     3,     3,
     3,     3,     3,     3,     3,     5
};
static const short builtin_expr_parse_defact[] = {     0,
     0,     0,     0,     0,     2,     0,     1,     7,     6,     0,
     5,     4,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     3,    21,    23,    22,     9,    19,    18,    17,    16,
    24,    25,    15,    12,    10,     8,    20,    11,     0,    13,
    14,     0,    26,     0,     0,     0
};
static const short builtin_expr_parse_defgoto[] = {    54,
     7
};
static const short builtin_expr_parse_pact[] = {   247,
   247,   247,   247,   247,-32768,   247,    80,-32768,-32768,    28,
-32768,-32768,   247,   247,   247,   247,   247,   247,   247,   247,
   247,   247,   247,   247,   247,   247,   247,   247,   247,   247,
   247,-32768,   198,   155,   177,-32768,   219,   233,   233,   233,
   132,   106,   233,   102,-32768,-32768,   219,   102,    54,   248,
   248,   247,    80,     4,     6,-32768
};
static const short builtin_expr_parse_pgoto[] = {-32768,
    -1
};
#define        builtin_expr_parse_LAST               272
static const short builtin_expr_parse_table[] = {     8,
     9,    10,    11,    55,    12,    56,     0,     0,     0,     0,
     0,    33,    34,    35,    36,    37,    38,    39,    40,    41,
    42,    43,    44,    45,    46,    47,    48,    49,    50,    51,
    13,     0,    14,    15,     0,    16,    17,    18,    19,     0,
    20,    21,     0,    22,     0,    23,    24,    25,    26,    27,
    53,    28,    29,    32,    30,    31,    13,     0,    14,    15,
    52,    16,    17,    18,    19,     0,    20,    21,     0,    22,
     0,    23,    24,    25,    26,    27,     0,    28,    29,     0,
    30,    31,    13,     0,    14,    15,     0,    16,    17,    18,
    19,     0,    20,    21,     0,    22,     0,    23,    24,    25,
    26,    27,     0,    28,    29,     0,    30,    31,    13,    16,
    14,    15,     0,    16,    17,    18,    19,     0,    20,    21,
     0,    25,    26,    23,    24,    25,    26,    27,     0,    28,
     0,     0,    30,    31,    13,     0,    14,    15,     0,    16,
    17,    18,    19,     0,    20,     0,     0,     0,     0,    23,
    24,    25,    26,    27,     0,    28,     0,    13,    30,    31,
    15,     0,    16,    17,    18,    19,     0,    20,     0,     0,
     0,     0,    23,    24,    25,    26,    27,     0,    28,    13,
     0,    30,    31,     0,    16,    17,    18,    19,     0,    20,
     0,     0,     0,     0,    23,    24,    25,    26,    27,     0,
    28,     0,     0,    30,    31,    16,    17,    18,    19,     0,
    20,     0,     0,     0,     0,    23,    24,    25,    26,    27,
     0,    28,     0,     0,    30,    31,    16,     0,    18,    19,
     0,    20,     0,     0,     0,     0,    23,    24,    25,    26,
    16,     0,    28,     0,     0,    30,    31,     0,     0,     0,
     1,    24,    25,    26,     0,    16,    28,     0,     0,    30,
    31,     2,     0,     3,     0,     4,    24,    25,    26,     5,
     6,    28
};
static const short builtin_expr_parse_check[] = {     1,
     2,     3,     4,     0,     6,     0,    -1,    -1,    -1,    -1,
    -1,    13,    14,    15,    16,    17,    18,    19,    20,    21,
    22,    23,    24,    25,    26,    27,    28,    29,    30,    31,
     3,    -1,     5,     6,    -1,     8,     9,    10,    11,    -1,
    13,    14,    -1,    16,    -1,    18,    19,    20,    21,    22,
    52,    24,    25,    26,    27,    28,     3,    -1,     5,     6,
     7,     8,     9,    10,    11,    -1,    13,    14,    -1,    16,
    -1,    18,    19,    20,    21,    22,    -1,    24,    25,    -1,
    27,    28,     3,    -1,     5,     6,    -1,     8,     9,    10,
    11,    -1,    13,    14,    -1,    16,    -1,    18,    19,    20,
    21,    22,    -1,    24,    25,    -1,    27,    28,     3,     8,
     5,     6,    -1,     8,     9,    10,    11,    -1,    13,    14,
    -1,    20,    21,    18,    19,    20,    21,    22,    -1,    24,
    -1,    -1,    27,    28,     3,    -1,     5,     6,    -1,     8,
     9,    10,    11,    -1,    13,    -1,    -1,    -1,    -1,    18,
    19,    20,    21,    22,    -1,    24,    -1,     3,    27,    28,
     6,    -1,     8,     9,    10,    11,    -1,    13,    -1,    -1,
    -1,    -1,    18,    19,    20,    21,    22,    -1,    24,     3,
    -1,    27,    28,    -1,     8,     9,    10,    11,    -1,    13,
    -1,    -1,    -1,    -1,    18,    19,    20,    21,    22,    -1,
    24,    -1,    -1,    27,    28,     8,     9,    10,    11,    -1,
    13,    -1,    -1,    -1,    -1,    18,    19,    20,    21,    22,
    -1,    24,    -1,    -1,    27,    28,     8,    -1,    10,    11,
    -1,    13,    -1,    -1,    -1,    -1,    18,    19,    20,    21,
     8,    -1,    24,    -1,    -1,    27,    28,    -1,    -1,    -1,
     4,    19,    20,    21,    -1,     8,    24,    -1,    -1,    27,
    28,    15,    -1,    17,    -1,    19,    19,    20,    21,    23,
    24,    24
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
#define builtin_expr_parse_errok             (builtin_expr_parse_errstatus = 0)
#define builtin_expr_parse_clearin     (builtin_expr_parse_char = builtin_expr_parse_EMPTY)
#define builtin_expr_parse_EMPTY             -2
#define builtin_expr_parse_EOF         0
#define builtin_expr_parse_ACCEPT      return(0)
#define builtin_expr_parse_ABORT       return(1)
#define builtin_expr_parse_ERROR             goto builtin_expr_parse_errlab1
/* Like builtin_expr_parse_ERROR except do call builtin_expr_parse_error.
   This remains here temporarily to ease the
   transition to the new meaning of builtin_expr_parse_ERROR, for GCC.
   Once GCC version 2 has supplanted version 1, this can go.  */
#define builtin_expr_parse_FAIL               goto builtin_expr_parse_errlab
#define builtin_expr_parse_RECOVERING()  (!!builtin_expr_parse_errstatus)
#define builtin_expr_parse_BACKUP(token, value) \
do                                    \
  if (builtin_expr_parse_char == builtin_expr_parse_EMPTY && builtin_expr_parse_len == 1)                  \
    { builtin_expr_parse_char = (token), builtin_expr_parse_lval = (value);         \
      builtin_expr_parse_char1 = builtin_expr_parse_TRANSLATE (builtin_expr_parse_char);                      \
      builtin_expr_parse_POPSTACK;                       \
      goto builtin_expr_parse_backup;                             \
    }                                    \
  else                                    \
    { builtin_expr_parse_error ("syntax error: cannot back up"); builtin_expr_parse_ERROR; }   \
while (0)
#define builtin_expr_parse_TERROR      1
#define builtin_expr_parse_ERRCODE     256
#ifndef builtin_expr_parse_PURE
#define builtin_expr_parse_LEX         builtin_expr_parse_lex()
#endif
#ifdef builtin_expr_parse_PURE
#ifdef builtin_expr_parse_LSP_NEEDED
#define builtin_expr_parse_LEX         builtin_expr_parse_lex(&builtin_expr_parse_lval, &builtin_expr_parse_lloc)
#else
#define builtin_expr_parse_LEX         builtin_expr_parse_lex(&builtin_expr_parse_lval)
#endif
#endif
/* If nonreentrant, generate the variables here */
#ifndef builtin_expr_parse_PURE
int    builtin_expr_parse_char;      /*  the lookahead symbol               */
builtin_expr_parse_STYPE       builtin_expr_parse_lval;               /*  the semantic value of the           */
                      /*  lookahead symbol                  */
#ifdef builtin_expr_parse_LSP_NEEDED
builtin_expr_parse_LTYPE builtin_expr_parse_lloc;               /*  location data for the lookahead        */
                      /*  symbol                      */
#endif
int builtin_expr_parse_nerrs;           /*  number of parse errors so far       */
#endif  /* not builtin_expr_parse_PURE */
#if builtin_expr_parse_DEBUG != 0
int builtin_expr_parse_debug;           /*  nonzero means print parse trace    */
/* Since this is uninitialized, it does not stop multiple parsers
   from coexisting.  */
#endif
/*  builtin_expr_parse_INITDEPTH indicates the initial size of the parser's stacks     */
#ifndef        builtin_expr_parse_INITDEPTH
#define builtin_expr_parse_INITDEPTH 200
#endif
/*  builtin_expr_parse_MAXDEPTH is the maximum size the stacks can grow to
    (effective only if the built-in stack extension method is used).  */
#if builtin_expr_parse_MAXDEPTH == 0
#undef builtin_expr_parse_MAXDEPTH
#endif
#ifndef builtin_expr_parse_MAXDEPTH
#define builtin_expr_parse_MAXDEPTH 10000
#endif
/* Prevent warning if -Wstrict-prototypes.  */
#ifdef __GNUC__
int builtin_expr_parse_parse (void);
#endif
#if __GNUC__ > 1             /* GNU C and GNU C++ define this.  */
#define __builtin_expr_parse__bcopy(FROM,TO,COUNT)     __builtin_memcpy(TO,FROM,COUNT)
#else                  /* not GNU C or C++ */
#ifndef __cplusplus
/* This is the most reliable way to avoid incompatibilities
   in available built-in functions on various systems.  */
static void
__builtin_expr_parse__bcopy (from, to, count)
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
__builtin_expr_parse__bcopy (char *from, char *to, int count) {
  register char *f = from;
  register char *t = to;
  register int i = count;
  while (i-- > 0)
    *t++ = *f++; }
#endif
#endif
int
builtin_expr_parse_parse() {
  register int builtin_expr_parse_state;
  register int builtin_expr_parse_n;
  register short *builtin_expr_parse_ssp;
  register builtin_expr_parse_STYPE *builtin_expr_parse_vsp;
  int builtin_expr_parse_errstatus;    /*  number of tokens to shift before error messages enabled */
  int builtin_expr_parse_char1;               /*  lookahead token as an internal (translated) token number */
  short        builtin_expr_parse_ssa[builtin_expr_parse_INITDEPTH];  /*  the state stack               */
  builtin_expr_parse_STYPE builtin_expr_parse_vsa[builtin_expr_parse_INITDEPTH];       /*  the semantic value stack   */
  short *builtin_expr_parse_ss = builtin_expr_parse_ssa;             /*  refer to the stacks thru separate pointers */
  builtin_expr_parse_STYPE *builtin_expr_parse_vs = builtin_expr_parse_vsa;    /*  to allow builtin_expr_parse_overflow to reallocate them elsewhere */
#ifdef builtin_expr_parse_LSP_NEEDED
  builtin_expr_parse_LTYPE builtin_expr_parse_lsa[builtin_expr_parse_INITDEPTH];       /*  the location stack                     */
  builtin_expr_parse_LTYPE *builtin_expr_parse_ls = builtin_expr_parse_lsa;
  builtin_expr_parse_LTYPE *builtin_expr_parse_lsp;
#define builtin_expr_parse_POPSTACK   (builtin_expr_parse_vsp--, builtin_expr_parse_ssp--, builtin_expr_parse_lsp--)
#else
#define builtin_expr_parse_POPSTACK   (builtin_expr_parse_vsp--, builtin_expr_parse_ssp--)
#endif
  int builtin_expr_parse_stacksize = builtin_expr_parse_INITDEPTH;
#ifdef builtin_expr_parse_PURE
  int builtin_expr_parse_char;
  builtin_expr_parse_STYPE builtin_expr_parse_lval;
  int builtin_expr_parse_nerrs;
#ifdef builtin_expr_parse_LSP_NEEDED
  builtin_expr_parse_LTYPE builtin_expr_parse_lloc;
#endif
#endif
  builtin_expr_parse_STYPE builtin_expr_parse_val;         /*  the variable used to return       */
                      /*  semantic values from the action        */
                      /*  routines                      */
  int builtin_expr_parse_len;
#if builtin_expr_parse_DEBUG != 0
  if (builtin_expr_parse_debug)
    fprintf(stderr, "Starting parse\n");
#endif
  builtin_expr_parse_state = 0;
  builtin_expr_parse_errstatus = 0;
  builtin_expr_parse_nerrs = 0;
  builtin_expr_parse_char = builtin_expr_parse_EMPTY;   /* Cause a token to be read.  */
  /* Initialize stack pointers.
     Waste one element of value and location stack
     so that they stay on the same level as the state stack.
     The wasted elements are never initialized.  */
  builtin_expr_parse_ssp = builtin_expr_parse_ss - 1;
  builtin_expr_parse_vsp = builtin_expr_parse_vs;
#ifdef builtin_expr_parse_LSP_NEEDED
  builtin_expr_parse_lsp = builtin_expr_parse_ls;
#endif
/* Push a new state, which is found in  builtin_expr_parse_state  .  */
/* In all cases, when you get here, the value and location stacks
   have just been pushed. so pushing a state here evens the stacks.  */
builtin_expr_parse_newstate:
  *++builtin_expr_parse_ssp = builtin_expr_parse_state;
  if (builtin_expr_parse_ssp >= builtin_expr_parse_ss + builtin_expr_parse_stacksize - 1) {
      /* Give user a chance to reallocate the stack */
      /* Use copies of these so that the &'s don't force the real ones into memory. */
      builtin_expr_parse_STYPE *builtin_expr_parse_vs1 = builtin_expr_parse_vs;
      short *builtin_expr_parse_ss1 = builtin_expr_parse_ss;
#ifdef builtin_expr_parse_LSP_NEEDED
      builtin_expr_parse_LTYPE *builtin_expr_parse_ls1 = builtin_expr_parse_ls;
#endif
      /* Get the current used size of the three stacks, in elements.  */
      int size = builtin_expr_parse_ssp - builtin_expr_parse_ss + 1;
#ifdef builtin_expr_parse_overflow
      /* Each stack pointer address is followed by the size of
        the data in use in that stack, in bytes.  */
      builtin_expr_parse_overflow("parser stack overflow",
              &builtin_expr_parse_ss1, size * sizeof (*builtin_expr_parse_ssp),
              &builtin_expr_parse_vs1, size * sizeof (*builtin_expr_parse_vsp),
#ifdef builtin_expr_parse_LSP_NEEDED
              &builtin_expr_parse_ls1, size * sizeof (*builtin_expr_parse_lsp),
#endif
              &builtin_expr_parse_stacksize);
      builtin_expr_parse_ss = builtin_expr_parse_ss1; builtin_expr_parse_vs = builtin_expr_parse_vs1;
#ifdef builtin_expr_parse_LSP_NEEDED
      builtin_expr_parse_ls = builtin_expr_parse_ls1;
#endif
#else /* no builtin_expr_parse_overflow */
      /* Extend the stack our own way.  */
      if (builtin_expr_parse_stacksize >= builtin_expr_parse_MAXDEPTH) {
         builtin_expr_parse_error("parser stack overflow");
         return 2; }
      builtin_expr_parse_stacksize *= 2;
      if (builtin_expr_parse_stacksize > builtin_expr_parse_MAXDEPTH)
       builtin_expr_parse_stacksize = builtin_expr_parse_MAXDEPTH;
      builtin_expr_parse_ss = (short *) alloca (builtin_expr_parse_stacksize * sizeof (*builtin_expr_parse_ssp));
      __builtin_expr_parse__bcopy ((char *)builtin_expr_parse_ss1, (char *)builtin_expr_parse_ss, size * sizeof (*builtin_expr_parse_ssp));
      builtin_expr_parse_vs = (builtin_expr_parse_STYPE *) alloca (builtin_expr_parse_stacksize * sizeof (*builtin_expr_parse_vsp));
      __builtin_expr_parse__bcopy ((char *)builtin_expr_parse_vs1, (char *)builtin_expr_parse_vs, size * sizeof (*builtin_expr_parse_vsp));
#ifdef builtin_expr_parse_LSP_NEEDED
      builtin_expr_parse_ls = (builtin_expr_parse_LTYPE *) alloca (builtin_expr_parse_stacksize * sizeof (*builtin_expr_parse_lsp));
      __builtin_expr_parse__bcopy ((char *)builtin_expr_parse_ls1, (char *)builtin_expr_parse_ls, size * sizeof (*builtin_expr_parse_lsp));
#endif
#endif /* no builtin_expr_parse_overflow */
      builtin_expr_parse_ssp = builtin_expr_parse_ss + size - 1;
      builtin_expr_parse_vsp = builtin_expr_parse_vs + size - 1;
#ifdef builtin_expr_parse_LSP_NEEDED
      builtin_expr_parse_lsp = builtin_expr_parse_ls + size - 1;
#endif
#if builtin_expr_parse_DEBUG != 0
      if (builtin_expr_parse_debug)
       fprintf(stderr, "Stack size increased to %d\n", builtin_expr_parse_stacksize);
#endif
      if (builtin_expr_parse_ssp >= builtin_expr_parse_ss + builtin_expr_parse_stacksize - 1)
       builtin_expr_parse_ABORT; }
#if builtin_expr_parse_DEBUG != 0
  if (builtin_expr_parse_debug)
    fprintf(stderr, "Entering state %d\n", builtin_expr_parse_state);
#endif
  goto builtin_expr_parse_backup;
 builtin_expr_parse_backup:
/* Do appropriate processing given the current state.  */
/* Read a lookahead token if we need one and don't already have one.  */
/* builtin_expr_parse_resume: */
  /* First try to decide what to do without reference to lookahead token.  */
  builtin_expr_parse_n = builtin_expr_parse_pact[builtin_expr_parse_state];
  if (builtin_expr_parse_n == builtin_expr_parse_FLAG)
    goto builtin_expr_parse_default;
  /* Not known => get a lookahead token if don't already have one.  */
  /* builtin_expr_parse_char is either builtin_expr_parse_EMPTY or builtin_expr_parse_EOF
     or a valid token in external form.  */
  if (builtin_expr_parse_char == builtin_expr_parse_EMPTY) {
#if builtin_expr_parse_DEBUG != 0
      if (builtin_expr_parse_debug)
       fprintf(stderr, "Reading a token: ");
#endif
      builtin_expr_parse_char = builtin_expr_parse_LEX; }
  /* Convert token to internal form (in builtin_expr_parse_char1) for indexing tables with */
  if (builtin_expr_parse_char <= 0)       /* This means end of input. */ {
      builtin_expr_parse_char1 = 0;
      builtin_expr_parse_char = builtin_expr_parse_EOF;               /* Don't call builtin_expr_parse_LEX any more */
#if builtin_expr_parse_DEBUG != 0
      if (builtin_expr_parse_debug)
       fprintf(stderr, "Now at end of input.\n");
#endif
    }
  else {
      builtin_expr_parse_char1 = builtin_expr_parse_TRANSLATE(builtin_expr_parse_char);
#if builtin_expr_parse_DEBUG != 0
      if (builtin_expr_parse_debug) {
         fprintf (stderr, "Next token is %d (%s", builtin_expr_parse_char, builtin_expr_parse_tname[builtin_expr_parse_char1]);
         /* Give the individual parser a way to print the precise meaning
            of a token, for further debugging info.  */
#ifdef builtin_expr_parse_PRINT
         builtin_expr_parse_PRINT (stderr, builtin_expr_parse_char, builtin_expr_parse_lval);
#endif
         fprintf (stderr, ")\n"); }
#endif
    }
  builtin_expr_parse_n += builtin_expr_parse_char1;
  if (builtin_expr_parse_n < 0 || builtin_expr_parse_n > builtin_expr_parse_LAST || builtin_expr_parse_check[builtin_expr_parse_n] != builtin_expr_parse_char1)
    goto builtin_expr_parse_default;
  builtin_expr_parse_n = builtin_expr_parse_table[builtin_expr_parse_n];
  /* builtin_expr_parse_n is what to do for this token type in this state.
     Negative => reduce, -builtin_expr_parse_n is rule number.
     Positive => shift, builtin_expr_parse_n is new state.
       New state is final state => don't bother to shift,
       just return success.
     0, or most negative number => error.  */
  if (builtin_expr_parse_n < 0) {
      if (builtin_expr_parse_n == builtin_expr_parse_FLAG)
       goto builtin_expr_parse_errlab;
      builtin_expr_parse_n = -builtin_expr_parse_n;
      goto builtin_expr_parse_reduce; }
  else if (builtin_expr_parse_n == 0)
    goto builtin_expr_parse_errlab;
  if (builtin_expr_parse_n == builtin_expr_parse_FINAL)
    builtin_expr_parse_ACCEPT;
  /* Shift the lookahead token.  */
#if builtin_expr_parse_DEBUG != 0
  if (builtin_expr_parse_debug)
    fprintf(stderr, "Shifting token %d (%s), ", builtin_expr_parse_char, builtin_expr_parse_tname[builtin_expr_parse_char1]);
#endif
  /* Discard the token being shifted unless it is eof.  */
  if (builtin_expr_parse_char != builtin_expr_parse_EOF)
    builtin_expr_parse_char = builtin_expr_parse_EMPTY;
  *++builtin_expr_parse_vsp = builtin_expr_parse_lval;
#ifdef builtin_expr_parse_LSP_NEEDED
  *++builtin_expr_parse_lsp = builtin_expr_parse_lloc;
#endif
  /* count tokens shifted since error; after three, turn off error status.  */
  if (builtin_expr_parse_errstatus) builtin_expr_parse_errstatus--;
  builtin_expr_parse_state = builtin_expr_parse_n;
  goto builtin_expr_parse_newstate;
/* Do the default action for the current state.  */
builtin_expr_parse_default:
  builtin_expr_parse_n = builtin_expr_parse_defact[builtin_expr_parse_state];
  if (builtin_expr_parse_n == 0)
    goto builtin_expr_parse_errlab;
/* Do a reduction.  builtin_expr_parse_n is the number of a rule to reduce with.  */
builtin_expr_parse_reduce:
  builtin_expr_parse_len = builtin_expr_parse_r2[builtin_expr_parse_n];
  builtin_expr_parse_val = builtin_expr_parse_vsp[1-builtin_expr_parse_len]; /* implement default value of the action */
#if builtin_expr_parse_DEBUG != 0
  if (builtin_expr_parse_debug) {
      int i;
      fprintf (stderr, "Reducing via rule %d (line %d), ",
              builtin_expr_parse_n, builtin_expr_parse_rline[builtin_expr_parse_n]);
      /* Print the symbols being reduced, and their result.  */
      for (i = builtin_expr_parse_prhs[builtin_expr_parse_n]; builtin_expr_parse_rhs[i] > 0; i++)
       fprintf (stderr, "%s ", builtin_expr_parse_tname[builtin_expr_parse_rhs[i]]);
      fprintf (stderr, " -> %s\n", builtin_expr_parse_tname[builtin_expr_parse_r1[builtin_expr_parse_n]]); }
#endif
  switch (builtin_expr_parse_n) {
case 1: {
                  if (builtin_expr_lex_error_count() != 0)
                      builtin_expr_parse_ABORT;
                  result = str_format("%ld", builtin_expr_parse_vsp[0].lv_integer);
             ;
    break;}
case 2:
{ builtin_expr_parse_val.lv_integer = builtin_expr_parse_vsp[0].lv_integer; ;
    break;}
case 3:
{ builtin_expr_parse_val.lv_integer = builtin_expr_parse_vsp[-1].lv_integer; ;
    break;}
case 4:
{ builtin_expr_parse_val.lv_integer = builtin_expr_parse_vsp[0].lv_integer; ;
    break;}
case 5:
{ builtin_expr_parse_val.lv_integer = -builtin_expr_parse_vsp[0].lv_integer; ;
    break;}
case 6:
{ builtin_expr_parse_val.lv_integer = !builtin_expr_parse_vsp[0].lv_integer; ;
    break;}
case 7:
{ builtin_expr_parse_val.lv_integer = ~builtin_expr_parse_vsp[0].lv_integer; ;
    break;}
case 8:
{ builtin_expr_parse_val.lv_integer = builtin_expr_parse_vsp[-2].lv_integer * builtin_expr_parse_vsp[0].lv_integer; ;
    break;}
case 9: {
                  if (builtin_expr_parse_vsp[0].lv_integer == 0) {
                      builtin_expr_parse_error(i18n("division by zero"));
                      builtin_expr_parse_val.lv_integer = 0; }
                  else
                      builtin_expr_parse_val.lv_integer = builtin_expr_parse_vsp[-2].lv_integer / builtin_expr_parse_vsp[0].lv_integer;
             ;
    break;}
case 10: {
                  if (builtin_expr_parse_vsp[0].lv_integer == 0) {
                      builtin_expr_parse_error(i18n("modulo by zero"));
                      builtin_expr_parse_val.lv_integer = 0; }
                  else
                      builtin_expr_parse_val.lv_integer = builtin_expr_parse_vsp[-2].lv_integer % builtin_expr_parse_vsp[0].lv_integer;
             ;
    break;}
case 11:
{ builtin_expr_parse_val.lv_integer = builtin_expr_parse_vsp[-2].lv_integer + builtin_expr_parse_vsp[0].lv_integer; ;
    break;}
case 12:
{ builtin_expr_parse_val.lv_integer = builtin_expr_parse_vsp[-2].lv_integer - builtin_expr_parse_vsp[0].lv_integer; ;
    break;}
case 13:
{ builtin_expr_parse_val.lv_integer = builtin_expr_parse_vsp[-2].lv_integer << builtin_expr_parse_vsp[0].lv_integer; ;
    break;}
case 14:
{ builtin_expr_parse_val.lv_integer = builtin_expr_parse_vsp[-2].lv_integer >> builtin_expr_parse_vsp[0].lv_integer; ;
    break;}
case 15:
{ builtin_expr_parse_val.lv_integer = (builtin_expr_parse_vsp[-2].lv_integer < builtin_expr_parse_vsp[0].lv_integer); ;
    break;}
case 16:
{ builtin_expr_parse_val.lv_integer = (builtin_expr_parse_vsp[-2].lv_integer <= builtin_expr_parse_vsp[0].lv_integer); ;
    break;}
case 17:
{ builtin_expr_parse_val.lv_integer = (builtin_expr_parse_vsp[-2].lv_integer > builtin_expr_parse_vsp[0].lv_integer); ;
    break;}
case 18:
{ builtin_expr_parse_val.lv_integer = (builtin_expr_parse_vsp[-2].lv_integer >= builtin_expr_parse_vsp[0].lv_integer); ;
    break;}
case 19:
{ builtin_expr_parse_val.lv_integer = (builtin_expr_parse_vsp[-2].lv_integer == builtin_expr_parse_vsp[0].lv_integer); ;
    break;}
case 20:
{ builtin_expr_parse_val.lv_integer = (builtin_expr_parse_vsp[-2].lv_integer != builtin_expr_parse_vsp[0].lv_integer); ;
    break;}
case 21:
{ builtin_expr_parse_val.lv_integer = (builtin_expr_parse_vsp[-2].lv_integer & builtin_expr_parse_vsp[0].lv_integer); ;
    break;}
case 22:
{ builtin_expr_parse_val.lv_integer = (builtin_expr_parse_vsp[-2].lv_integer ^ builtin_expr_parse_vsp[0].lv_integer); ;
    break;}
case 23:
{ builtin_expr_parse_val.lv_integer = (builtin_expr_parse_vsp[-2].lv_integer | builtin_expr_parse_vsp[0].lv_integer); ;
    break;}
case 24:
{ builtin_expr_parse_val.lv_integer = (builtin_expr_parse_vsp[-2].lv_integer && builtin_expr_parse_vsp[0].lv_integer); ;
    break;}
case 25:
{ builtin_expr_parse_val.lv_integer = (builtin_expr_parse_vsp[-2].lv_integer || builtin_expr_parse_vsp[0].lv_integer); ;
    break;}
case 26:
{ builtin_expr_parse_val.lv_integer = (builtin_expr_parse_vsp[-4].lv_integer ? builtin_expr_parse_vsp[-2].lv_integer : builtin_expr_parse_vsp[0].lv_integer); ;
    break;} }
   /* the action file gets copied in in place of this dollarsign */
  builtin_expr_parse_vsp -= builtin_expr_parse_len;
  builtin_expr_parse_ssp -= builtin_expr_parse_len;
#ifdef builtin_expr_parse_LSP_NEEDED
  builtin_expr_parse_lsp -= builtin_expr_parse_len;
#endif
#if builtin_expr_parse_DEBUG != 0
  if (builtin_expr_parse_debug) {
      short *ssp1 = builtin_expr_parse_ss - 1;
      fprintf (stderr, "state stack now");
      while (ssp1 != builtin_expr_parse_ssp)
       fprintf (stderr, " %d", *++ssp1);
      fprintf (stderr, "\n"); }
#endif
  *++builtin_expr_parse_vsp = builtin_expr_parse_val;
#ifdef builtin_expr_parse_LSP_NEEDED
  builtin_expr_parse_lsp++;
  if (builtin_expr_parse_len == 0) {
      builtin_expr_parse_lsp->first_line = builtin_expr_parse_lloc.first_line;
      builtin_expr_parse_lsp->first_column = builtin_expr_parse_lloc.first_column;
      builtin_expr_parse_lsp->last_line = (builtin_expr_parse_lsp-1)->last_line;
      builtin_expr_parse_lsp->last_column = (builtin_expr_parse_lsp-1)->last_column;
      builtin_expr_parse_lsp->text = 0; }
  else {
      builtin_expr_parse_lsp->last_line = (builtin_expr_parse_lsp+builtin_expr_parse_len-1)->last_line;
      builtin_expr_parse_lsp->last_column = (builtin_expr_parse_lsp+builtin_expr_parse_len-1)->last_column; }
#endif
  /* Now "shift" the result of the reduction.
     Determine what state that goes to,
     based on the state we popped back to
     and the rule number reduced by.  */
  builtin_expr_parse_n = builtin_expr_parse_r1[builtin_expr_parse_n];
  builtin_expr_parse_state = builtin_expr_parse_pgoto[builtin_expr_parse_n - builtin_expr_parse_NTBASE] + *builtin_expr_parse_ssp;
  if (builtin_expr_parse_state >= 0 && builtin_expr_parse_state <= builtin_expr_parse_LAST && builtin_expr_parse_check[builtin_expr_parse_state] == *builtin_expr_parse_ssp)
    builtin_expr_parse_state = builtin_expr_parse_table[builtin_expr_parse_state];
  else
    builtin_expr_parse_state = builtin_expr_parse_defgoto[builtin_expr_parse_n - builtin_expr_parse_NTBASE];
  goto builtin_expr_parse_newstate;
builtin_expr_parse_errlab:   /* here on detecting error */
  if (! builtin_expr_parse_errstatus)
    /* If not already recovering from an error, report this error.  */ {
      ++builtin_expr_parse_nerrs;
#ifdef builtin_expr_parse_ERROR_VERBOSE
      builtin_expr_parse_n = builtin_expr_parse_pact[builtin_expr_parse_state];
      if (builtin_expr_parse_n > builtin_expr_parse_FLAG && builtin_expr_parse_n < builtin_expr_parse_LAST) {
         int size = 0;
         char *msg;
         int x, count;
         count = 0;
         /* Start X at -builtin_expr_parse_n if nec to avoid negative indexes in builtin_expr_parse_check.  */
         for (x = (builtin_expr_parse_n < 0 ? -builtin_expr_parse_n : 0);
              x < (sizeof(builtin_expr_parse_tname) / sizeof(char *)); x++)
           if (builtin_expr_parse_check[x + builtin_expr_parse_n] == x)
             size += strlen(builtin_expr_parse_tname[x]) + 15, count++;
         msg = (char *) malloc(size + 15);
         if (msg != 0) {
             strcpy(msg, "parse error");
             if (count < 5) {
               count = 0;
               for (x = (builtin_expr_parse_n < 0 ? -builtin_expr_parse_n : 0);
                    x < (sizeof(builtin_expr_parse_tname) / sizeof(char *)); x++)
                 if (builtin_expr_parse_check[x + builtin_expr_parse_n] == x) {
                  strcat(msg, count == 0 ? ", expecting `" : " or `");
                  strcat(msg, builtin_expr_parse_tname[x]);
                  strcat(msg, "'");
                  count++; } }
             builtin_expr_parse_error(msg);
             free(msg); }
         else
           builtin_expr_parse_error ("parse error; also virtual memory exceeded"); }
      else
#endif /* builtin_expr_parse_ERROR_VERBOSE */
       builtin_expr_parse_error("parse error"); }
  goto builtin_expr_parse_errlab1;
builtin_expr_parse_errlab1:   /* here on error raised explicitly by an action */
  if (builtin_expr_parse_errstatus == 3) {
      /* if just tried and failed to reuse lookahead token after an error, discard it.  */
      /* return failure if at end of input */
      if (builtin_expr_parse_char == builtin_expr_parse_EOF)
       builtin_expr_parse_ABORT;
#if builtin_expr_parse_DEBUG != 0
      if (builtin_expr_parse_debug)
       fprintf(stderr, "Discarding token %d (%s).\n", builtin_expr_parse_char, builtin_expr_parse_tname[builtin_expr_parse_char1]);
#endif
      builtin_expr_parse_char = builtin_expr_parse_EMPTY; }
  /* Else will try to reuse lookahead token
     after shifting the error token.  */
  builtin_expr_parse_errstatus = 3;       /* Each real token shifted decrements this */
  goto builtin_expr_parse_errhandle;
builtin_expr_parse_errdefault:  /* current state does not do anything special for the error token. */
#if 0
  /* This is wrong; only states that explicitly want error tokens
     should shift them.  */
  builtin_expr_parse_n = builtin_expr_parse_defact[builtin_expr_parse_state];  /* If its default is to accept any token, ok.  Otherwise pop it.*/
  if (builtin_expr_parse_n) goto builtin_expr_parse_default;
#endif
builtin_expr_parse_errpop:   /* pop the current state because it cannot handle the error token */
  if (builtin_expr_parse_ssp == builtin_expr_parse_ss) builtin_expr_parse_ABORT;
  builtin_expr_parse_vsp--;
  builtin_expr_parse_state = *--builtin_expr_parse_ssp;
#ifdef builtin_expr_parse_LSP_NEEDED
  builtin_expr_parse_lsp--;
#endif
#if builtin_expr_parse_DEBUG != 0
  if (builtin_expr_parse_debug) {
      short *ssp1 = builtin_expr_parse_ss - 1;
      fprintf (stderr, "Error: state stack now");
      while (ssp1 != builtin_expr_parse_ssp)
       fprintf (stderr, " %d", *++ssp1);
      fprintf (stderr, "\n"); }
#endif
builtin_expr_parse_errhandle:
  builtin_expr_parse_n = builtin_expr_parse_pact[builtin_expr_parse_state];
  if (builtin_expr_parse_n == builtin_expr_parse_FLAG)
    goto builtin_expr_parse_errdefault;
  builtin_expr_parse_n += builtin_expr_parse_TERROR;
  if (builtin_expr_parse_n < 0 || builtin_expr_parse_n > builtin_expr_parse_LAST || builtin_expr_parse_check[builtin_expr_parse_n] != builtin_expr_parse_TERROR)
    goto builtin_expr_parse_errdefault;
  builtin_expr_parse_n = builtin_expr_parse_table[builtin_expr_parse_n];
  if (builtin_expr_parse_n < 0) {
      if (builtin_expr_parse_n == builtin_expr_parse_FLAG)
       goto builtin_expr_parse_errpop;
      builtin_expr_parse_n = -builtin_expr_parse_n;
      goto builtin_expr_parse_reduce; }
  else if (builtin_expr_parse_n == 0)
    goto builtin_expr_parse_errpop;
  if (builtin_expr_parse_n == builtin_expr_parse_FINAL)
    builtin_expr_parse_ACCEPT;
#if builtin_expr_parse_DEBUG != 0
  if (builtin_expr_parse_debug)
    fprintf(stderr, "Shifting error token, ");
#endif
  *++builtin_expr_parse_vsp = builtin_expr_parse_lval;
#ifdef builtin_expr_parse_LSP_NEEDED
  *++builtin_expr_parse_lsp = builtin_expr_parse_lloc;
#endif
  builtin_expr_parse_state = builtin_expr_parse_n;
  goto builtin_expr_parse_newstate; }
