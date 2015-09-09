/*  A Bison parser, made from make2cook/vargram.y with Bison version GNU Bison version 1.22
  */
#define vargram_BISON 1  /* Identify Bison output.  */
#define        COLON  258
#define        COMMA  259
#define        DOLLAR 260
#define        EQU    261
#define        LB     262
#define        LP     263
#define        PLAIN  264
#define        RB     265
#define        RP     266
#define        SPACE  267
#include <ac/stdio.h>
#include <ac/string.h>
#include <trace.h>
#include <vargram.h>
#include <variable.h>
#ifdef DEBUG
#define vargram_DEBUG 1
#ifdef vargram_BISON
#define fprintf vargram_trace2
#else
#define printf trace_where(__FILE__, __LINE__), vargram_trace
#endif
extern int vargram_debug;
#endif
static string_ty *patvar _((string_ty *, string_ty *, string_ty *));
static string_ty *
patvar(name, from, to)
       string_ty     *name;
       string_ty     *from;
       string_ty     *to; {
       string_ty     *tmp;
       string_ty     *result;
       string_ty     *s_from;
       string_ty     *s_to;
       if (!strchr(from->str_text, '%')) {
             tmp = from;
             if (tmp->str_length == 0)
                  from = str_from_c("%0%");
             else if (tmp->str_text[0] == '.')
                  from = str_format("%%0%%%S", tmp);
             else
                  from = str_format("%%0%%.%S", tmp);
             str_free(tmp); }
       else {
             tmp = from;
             s_from = str_from_c("%");
             s_to = str_from_c("%0%");
             from = str_substitute(s_from, s_to, tmp);
             str_free(tmp); }
       if (!strchr(to->str_text, '%')) {
             tmp = to;
             if (tmp->str_length == 0)
                  to = str_from_c("%0%");
             else if (tmp->str_text[0] == '.')
                  to = str_format("%%0%%%S", tmp);
             else
                  to = str_format("%%0%%.%S", tmp);
             str_free(tmp); }
       else {
             tmp = to;
             s_from = str_from_c("%");
             s_to = str_from_c("%0%");
             to = str_substitute(s_from, s_to, tmp);
             str_free(tmp); }
       tmp = variable_mangle_lookup(name);
       str_free(name);
       result = str_format("[patsubst %S %S %S]", from, to, tmp);
       str_free(tmp);
       str_free(from);
       str_free(to);
       return result; }
static string_ty *function _((string_ty *, string_list_ty *));
static string_ty *
function(name, args)
       string_ty     *name;
       string_list_ty        *args; {
       string_ty     *s;
       string_ty     *result;
       static string_ty *foreach;
       if (!foreach)
             foreach = str_from_c("foreach");
       if (str_equal(name, foreach) && args->nstrings == 3) {
             string_ty    *s_from;
             string_ty    *s_to;
             /*
              * The foreach function is treated specially.  This is
              * not an exact semantic mapping, but it is better than
              * nothing.
              */
             variable_mangle_forget(args->string[0]);
             s_from = str_format("[%S]", args->string[0]);
             s_to = str_from_c("%");
             s = str_substitute(s_from, s_to, args->string[2]);
             result = str_format("[fromto %% %S %S]", s, args->string[1]);
             str_free(s);
             str_free(s_from);
             str_free(s_to); }
       else {
             /*
              * Construct the function invokation.  There are
              * make-equivalents for all the function names built
              * into cook, so there is no need to translate the
              * function name.
              */
             s = wl2str(args, 0, args->nstrings - 1, (char *)0);
             string_list_destructor(args);
             result = str_format("[%S %S]", name, s);
             str_free(s); }
       str_free(name);
       string_list_destructor(args);
       trace(("result = \"%s\";\n", result->str_text));
       return result; }
typedef union {
       string_ty     *lv_string;
       string_list_ty        lv_list;
} vargram_STYPE;
#ifndef vargram_LTYPE
typedef
  struct vargram_ltype {
      int timestamp;
      int first_line;
      int first_column;
      int last_line;
      int last_column;
      char *text; }
  vargram_ltype;
#define vargram_LTYPE vargram_ltype
#endif
#ifndef __cplusplus
#ifndef __STDC__
#define const
#endif
#endif
#define        vargram_FINAL   67
#define        vargram_FLAG     -32768
#define        vargram_NTBASE 13
#define vargram_TRANSLATE(x) ((unsigned)(x) <= 267 ? vargram_translate[x] : 29)
static const char vargram_translate[] = {     0,
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
     6,     7,     8,     9,    10,    11,    12
};
#if vargram_DEBUG != 0
static const short vargram_prhs[] = {     0,
     0,     2,     5,     7,     8,    10,    13,    16,    18,    23,
    25,    28,    31,    34,    39,    44,    53,    62,    69,    76,
    78,    80,    83,    84,    87,    89,    91,    93,    97,    98,
   100,   103,   105,   109,   111,   114,   116,   118,   120,   122,
   124,   126,   130,   132
};
static const short vargram_rhs[] = {    14,
     0,    14,    15,     0,     1,     0,     0,    16,     0,    15,
    16,     0,    15,    12,     0,    17,     0,    17,     8,    24,
    11,     0,    18,     0,    17,    18,     0,     5,     5,     0,
     5,     9,     0,     5,     8,    19,    11,     0,     5,     7,
    19,    10,     0,     5,     8,    19,     3,    20,     6,    20,
    11,     0,     5,     7,    19,     3,    20,     6,    20,    10,
     0,     5,     8,    19,    12,    22,    11,     0,     5,     7,
    19,    12,    22,    10,     0,     9,     0,    21,     0,    19,
    21,     0,     0,    20,    21,     0,    18,     0,     4,     0,
    23,     0,    22,    28,    23,     0,     0,    24,     0,    24,
    12,     0,    25,     0,    24,    12,    25,     0,    26,     0,
    25,    26,     0,     9,     0,     6,     0,     3,     0,    18,
     0,    27,     0,     1,     0,     8,    22,    11,     0,     4,
     0,    28,    12,     0
};
#endif
#if vargram_DEBUG != 0
static const short vargram_rline[] = { 0,
   184,   186,   187,   192,   201,   202,   203,   207,   209,   226,
   228,   237,   239,   244,   249,   254,   256,   258,   260,   262,
   267,   269,   278,   280,   289,   291,   296,   302,   311,   320,
   325,   333,   339,   348,   350,   359,   361,   363,   365,   367,
   369,   374,   386,   387
};
static const char * const vargram_tname[] = {   "$","error","$illegal.","COLON","COMMA",
"DOLLAR","EQU","LB","LP","PLAIN","RB","RP","SPACE","main","dbg","strings","string",
"gizzards","var","name","oname","namec","csl","ossl","ssl","arg","argc","parens",
"comma",""
};
#endif
static const short vargram_r1[] = {     0,
    13,    13,    13,    14,    15,    15,    15,    16,    16,    17,
    17,    18,    18,    18,    18,    18,    18,    18,    18,    18,
    19,    19,    20,    20,    21,    21,    22,    22,    23,    23,
    23,    24,    24,    25,    25,    26,    26,    26,    26,    26,
    26,    27,    28,    28
};
static const short vargram_r2[] = {     0,
     1,     2,     1,     0,     1,     2,     2,     1,     4,     1,
     2,     2,     2,     4,     4,     8,     8,     6,     6,     1,
     1,     2,     0,     2,     1,     1,     1,     3,     0,     1,
     2,     1,     3,     1,     2,     1,     1,     1,     1,     1,
     1,     3,     1,     2
};
static const short vargram_defact[] = {     0,
     3,     1,     0,    20,     2,     5,     8,    10,    12,     0,
     0,    13,     7,     6,     0,    11,    26,    25,     0,    21,
     0,    41,    38,    37,     0,    20,    39,     0,     0,    34,
    40,    23,    15,     0,    22,    23,    14,     0,     0,    27,
    30,     9,     0,    35,     0,     0,     0,     0,    43,    42,
     0,     0,     0,    23,    24,    19,    23,    18,    44,    28,
     0,     0,    17,    16,     0,     0,     0
};
static const short vargram_defgoto[] = {    65,
     2,     5,     6,     7,    27,    19,    45,    55,    39,    40,
    41,    29,    30,    31,    51
};
static const short vargram_pact[] = {     6,
-32768,    53,    31,-32768,     9,-32768,   151,-32768,-32768,   149,
   149,-32768,-32768,-32768,   132,-32768,-32768,-32768,   110,-32768,
   120,-32768,-32768,-32768,    93,-32768,-32768,    30,    23,-32768,
-32768,-32768,-32768,   102,-32768,-32768,-32768,    93,    26,-32768,
     0,-32768,   132,-32768,   112,    12,   146,    46,-32768,-32768,
    43,    82,    60,-32768,-32768,-32768,-32768,-32768,-32768,-32768,
    71,   138,-32768,-32768,    10,    56,-32768
};
static const short vargram_pgoto[] = {-32768,
-32768,-32768,    62,-32768,    -2,    66,   -34,    63,    92,    27,
    64,   -39,   -28,-32768,-32768
};
#define        vargram_LAST     160
static const short vargram_table[] = {     8,
    44,    47,     8,    53,    16,    -4,     1,    18,    18,    66,
    -4,    52,    53,     3,    -4,    49,    18,     4,    18,    61,
    13,    56,    62,    22,    44,    23,   -32,     3,    24,    49,
    25,    26,   -32,   -32,   -32,     9,    50,    10,    11,    12,
    42,    43,    18,    22,    18,    23,   -29,     3,    24,    49,
    25,    26,   -29,   -29,    59,    67,    58,     3,    18,    18,
    22,     4,    23,   -33,     3,    24,    14,    25,    26,   -33,
   -33,   -33,    20,    20,    17,     3,    21,    60,    28,     4,
    63,    35,    22,    35,    23,   -31,     3,    24,     0,    25,
    26,   -31,   -31,    22,     0,    23,   -29,     3,    24,     0,
    25,    26,    22,   -29,    23,   -29,     3,    24,     0,    25,
    26,   -29,    32,    17,     3,    17,     3,    54,     4,    33,
     4,    34,    36,    17,     3,    46,     0,     0,     4,    48,
    37,    38,    22,     0,    23,     0,     3,    24,     0,    25,
    26,    17,     3,     0,     0,     0,     4,     0,    64,    17,
     3,    57,    17,     3,     4,     3,     0,     4,    15,     4
};
static const short vargram_check[] = {     2,
    29,    36,     5,    43,     7,     0,     1,    10,    11,     0,
     5,    12,    52,     5,     9,     4,    19,     9,    21,    54,
    12,    10,    57,     1,    53,     3,     4,     5,     6,     4,
     8,     9,    10,    11,    12,     5,    11,     7,     8,     9,
    11,    12,    45,     1,    47,     3,     4,     5,     6,     4,
     8,     9,    10,    11,    12,     0,    11,     5,    61,    62,
     1,     9,     3,     4,     5,     6,     5,     8,     9,    10,
    11,    12,    10,    11,     4,     5,    11,    51,    15,     9,
    10,    19,     1,    21,     3,     4,     5,     6,    -1,     8,
     9,    10,    11,     1,    -1,     3,     4,     5,     6,    -1,
     8,     9,     1,    11,     3,     4,     5,     6,    -1,     8,
     9,    10,     3,     4,     5,     4,     5,     6,     9,    10,
     9,    12,     3,     4,     5,    34,    -1,    -1,     9,    38,
    11,    12,     1,    -1,     3,    -1,     5,     6,    -1,     8,
     9,     4,     5,    -1,    -1,    -1,     9,    -1,    11,     4,
     5,     6,     4,     5,     9,     5,    -1,     9,     8,     9
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
#define vargram_errok   (vargram_errstatus = 0)
#define vargram_clearin        (vargram_char = vargram_EMPTY)
#define vargram_EMPTY   -2
#define vargram_EOF       0
#define vargram_ACCEPT return(0)
#define vargram_ABORT  return(1)
#define vargram_ERROR   goto vargram_errlab1
/* Like vargram_ERROR except do call vargram_error.
   This remains here temporarily to ease the
   transition to the new meaning of vargram_ERROR, for GCC.
   Once GCC version 2 has supplanted version 1, this can go.  */
#define vargram_FAIL     goto vargram_errlab
#define vargram_RECOVERING()  (!!vargram_errstatus)
#define vargram_BACKUP(token, value) \
do                                    \
  if (vargram_char == vargram_EMPTY && vargram_len == 1)                      \
    { vargram_char = (token), vargram_lval = (value);           \
      vargram_char1 = vargram_TRANSLATE (vargram_char);                          \
      vargram_POPSTACK;                                 \
      goto vargram_backup;                       \
    }                                    \
  else                                    \
    { vargram_error ("syntax error: cannot back up"); vargram_ERROR; } \
while (0)
#define vargram_TERROR 1
#define vargram_ERRCODE        256
#ifndef vargram_PURE
#define vargram_LEX       vargram_lex()
#endif
#ifdef vargram_PURE
#ifdef vargram_LSP_NEEDED
#define vargram_LEX       vargram_lex(&vargram_lval, &vargram_lloc)
#else
#define vargram_LEX       vargram_lex(&vargram_lval)
#endif
#endif
/* If nonreentrant, generate the variables here */
#ifndef vargram_PURE
int    vargram_char;               /*  the lookahead symbol     */
vargram_STYPE  vargram_lval;         /*  the semantic value of the       */
                      /*  lookahead symbol                  */
#ifdef vargram_LSP_NEEDED
vargram_LTYPE vargram_lloc;         /*  location data for the lookahead      */
                      /*  symbol                      */
#endif
int vargram_nerrs;            /*  number of parse errors so far       */
#endif  /* not vargram_PURE */
#if vargram_DEBUG != 0
int vargram_debug;            /*  nonzero means print parse trace       */
/* Since this is uninitialized, it does not stop multiple parsers
   from coexisting.  */
#endif
/*  vargram_INITDEPTH indicates the initial size of the parser's stacks        */
#ifndef        vargram_INITDEPTH
#define vargram_INITDEPTH 200
#endif
/*  vargram_MAXDEPTH is the maximum size the stacks can grow to
    (effective only if the built-in stack extension method is used).  */
#if vargram_MAXDEPTH == 0
#undef vargram_MAXDEPTH
#endif
#ifndef vargram_MAXDEPTH
#define vargram_MAXDEPTH 10000
#endif
/* Prevent warning if -Wstrict-prototypes.  */
#ifdef __GNUC__
int vargram_parse (void);
#endif
#if __GNUC__ > 1             /* GNU C and GNU C++ define this.  */
#define __vargram__bcopy(FROM,TO,COUNT)        __builtin_memcpy(TO,FROM,COUNT)
#else                  /* not GNU C or C++ */
#ifndef __cplusplus
/* This is the most reliable way to avoid incompatibilities
   in available built-in functions on various systems.  */
static void
__vargram__bcopy (from, to, count)
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
__vargram__bcopy (char *from, char *to, int count) {
  register char *f = from;
  register char *t = to;
  register int i = count;
  while (i-- > 0)
    *t++ = *f++; }
#endif
#endif
int
vargram_parse() {
  register int vargram_state;
  register int vargram_n;
  register short *vargram_ssp;
  register vargram_STYPE *vargram_vsp;
  int vargram_errstatus;       /*  number of tokens to shift before error messages enabled */
  int vargram_char1;     /*  lookahead token as an internal (translated) token number */
  short        vargram_ssa[vargram_INITDEPTH];        /*  the state stack         */
  vargram_STYPE vargram_vsa[vargram_INITDEPTH];        /*  the semantic value stack     */
  short *vargram_ss = vargram_ssa;         /*  refer to the stacks thru separate pointers */
  vargram_STYPE *vargram_vs = vargram_vsa;     /*  to allow vargram_overflow to reallocate them elsewhere */
#ifdef vargram_LSP_NEEDED
  vargram_LTYPE vargram_lsa[vargram_INITDEPTH];        /*  the location stack                */
  vargram_LTYPE *vargram_ls = vargram_lsa;
  vargram_LTYPE *vargram_lsp;
#define vargram_POPSTACK   (vargram_vsp--, vargram_ssp--, vargram_lsp--)
#else
#define vargram_POPSTACK   (vargram_vsp--, vargram_ssp--)
#endif
  int vargram_stacksize = vargram_INITDEPTH;
#ifdef vargram_PURE
  int vargram_char;
  vargram_STYPE vargram_lval;
  int vargram_nerrs;
#ifdef vargram_LSP_NEEDED
  vargram_LTYPE vargram_lloc;
#endif
#endif
  vargram_STYPE vargram_val;     /*  the variable used to return   */
                      /*  semantic values from the action        */
                      /*  routines                      */
  int vargram_len;
#if vargram_DEBUG != 0
  if (vargram_debug)
    fprintf(stderr, "Starting parse\n");
#endif
  vargram_state = 0;
  vargram_errstatus = 0;
  vargram_nerrs = 0;
  vargram_char = vargram_EMPTY;               /* Cause a token to be read.  */
  /* Initialize stack pointers.
     Waste one element of value and location stack
     so that they stay on the same level as the state stack.
     The wasted elements are never initialized.  */
  vargram_ssp = vargram_ss - 1;
  vargram_vsp = vargram_vs;
#ifdef vargram_LSP_NEEDED
  vargram_lsp = vargram_ls;
#endif
/* Push a new state, which is found in  vargram_state  .  */
/* In all cases, when you get here, the value and location stacks
   have just been pushed. so pushing a state here evens the stacks.  */
vargram_newstate:
  *++vargram_ssp = vargram_state;
  if (vargram_ssp >= vargram_ss + vargram_stacksize - 1) {
      /* Give user a chance to reallocate the stack */
      /* Use copies of these so that the &'s don't force the real ones into memory. */
      vargram_STYPE *vargram_vs1 = vargram_vs;
      short *vargram_ss1 = vargram_ss;
#ifdef vargram_LSP_NEEDED
      vargram_LTYPE *vargram_ls1 = vargram_ls;
#endif
      /* Get the current used size of the three stacks, in elements.  */
      int size = vargram_ssp - vargram_ss + 1;
#ifdef vargram_overflow
      /* Each stack pointer address is followed by the size of
        the data in use in that stack, in bytes.  */
      vargram_overflow("parser stack overflow",
              &vargram_ss1, size * sizeof (*vargram_ssp),
              &vargram_vs1, size * sizeof (*vargram_vsp),
#ifdef vargram_LSP_NEEDED
              &vargram_ls1, size * sizeof (*vargram_lsp),
#endif
              &vargram_stacksize);
      vargram_ss = vargram_ss1; vargram_vs = vargram_vs1;
#ifdef vargram_LSP_NEEDED
      vargram_ls = vargram_ls1;
#endif
#else /* no vargram_overflow */
      /* Extend the stack our own way.  */
      if (vargram_stacksize >= vargram_MAXDEPTH) {
         vargram_error("parser stack overflow");
         return 2; }
      vargram_stacksize *= 2;
      if (vargram_stacksize > vargram_MAXDEPTH)
       vargram_stacksize = vargram_MAXDEPTH;
      vargram_ss = (short *) alloca (vargram_stacksize * sizeof (*vargram_ssp));
      __vargram__bcopy ((char *)vargram_ss1, (char *)vargram_ss, size * sizeof (*vargram_ssp));
      vargram_vs = (vargram_STYPE *) alloca (vargram_stacksize * sizeof (*vargram_vsp));
      __vargram__bcopy ((char *)vargram_vs1, (char *)vargram_vs, size * sizeof (*vargram_vsp));
#ifdef vargram_LSP_NEEDED
      vargram_ls = (vargram_LTYPE *) alloca (vargram_stacksize * sizeof (*vargram_lsp));
      __vargram__bcopy ((char *)vargram_ls1, (char *)vargram_ls, size * sizeof (*vargram_lsp));
#endif
#endif /* no vargram_overflow */
      vargram_ssp = vargram_ss + size - 1;
      vargram_vsp = vargram_vs + size - 1;
#ifdef vargram_LSP_NEEDED
      vargram_lsp = vargram_ls + size - 1;
#endif
#if vargram_DEBUG != 0
      if (vargram_debug)
       fprintf(stderr, "Stack size increased to %d\n", vargram_stacksize);
#endif
      if (vargram_ssp >= vargram_ss + vargram_stacksize - 1)
       vargram_ABORT; }
#if vargram_DEBUG != 0
  if (vargram_debug)
    fprintf(stderr, "Entering state %d\n", vargram_state);
#endif
  goto vargram_backup;
 vargram_backup:
/* Do appropriate processing given the current state.  */
/* Read a lookahead token if we need one and don't already have one.  */
/* vargram_resume: */
  /* First try to decide what to do without reference to lookahead token.  */
  vargram_n = vargram_pact[vargram_state];
  if (vargram_n == vargram_FLAG)
    goto vargram_default;
  /* Not known => get a lookahead token if don't already have one.  */
  /* vargram_char is either vargram_EMPTY or vargram_EOF
     or a valid token in external form.  */
  if (vargram_char == vargram_EMPTY) {
#if vargram_DEBUG != 0
      if (vargram_debug)
       fprintf(stderr, "Reading a token: ");
#endif
      vargram_char = vargram_LEX; }
  /* Convert token to internal form (in vargram_char1) for indexing tables with */
  if (vargram_char <= 0)             /* This means end of input. */ {
      vargram_char1 = 0;
      vargram_char = vargram_EOF;           /* Don't call vargram_LEX any more */
#if vargram_DEBUG != 0
      if (vargram_debug)
       fprintf(stderr, "Now at end of input.\n");
#endif
    }
  else {
      vargram_char1 = vargram_TRANSLATE(vargram_char);
#if vargram_DEBUG != 0
      if (vargram_debug) {
         fprintf (stderr, "Next token is %d (%s", vargram_char, vargram_tname[vargram_char1]);
         /* Give the individual parser a way to print the precise meaning
            of a token, for further debugging info.  */
#ifdef vargram_PRINT
         vargram_PRINT (stderr, vargram_char, vargram_lval);
#endif
         fprintf (stderr, ")\n"); }
#endif
    }
  vargram_n += vargram_char1;
  if (vargram_n < 0 || vargram_n > vargram_LAST || vargram_check[vargram_n] != vargram_char1)
    goto vargram_default;
  vargram_n = vargram_table[vargram_n];
  /* vargram_n is what to do for this token type in this state.
     Negative => reduce, -vargram_n is rule number.
     Positive => shift, vargram_n is new state.
       New state is final state => don't bother to shift,
       just return success.
     0, or most negative number => error.  */
  if (vargram_n < 0) {
      if (vargram_n == vargram_FLAG)
       goto vargram_errlab;
      vargram_n = -vargram_n;
      goto vargram_reduce; }
  else if (vargram_n == 0)
    goto vargram_errlab;
  if (vargram_n == vargram_FINAL)
    vargram_ACCEPT;
  /* Shift the lookahead token.  */
#if vargram_DEBUG != 0
  if (vargram_debug)
    fprintf(stderr, "Shifting token %d (%s), ", vargram_char, vargram_tname[vargram_char1]);
#endif
  /* Discard the token being shifted unless it is eof.  */
  if (vargram_char != vargram_EOF)
    vargram_char = vargram_EMPTY;
  *++vargram_vsp = vargram_lval;
#ifdef vargram_LSP_NEEDED
  *++vargram_lsp = vargram_lloc;
#endif
  /* count tokens shifted since error; after three, turn off error status.  */
  if (vargram_errstatus) vargram_errstatus--;
  vargram_state = vargram_n;
  goto vargram_newstate;
/* Do the default action for the current state.  */
vargram_default:
  vargram_n = vargram_defact[vargram_state];
  if (vargram_n == 0)
    goto vargram_errlab;
/* Do a reduction.  vargram_n is the number of a rule to reduce with.  */
vargram_reduce:
  vargram_len = vargram_r2[vargram_n];
  vargram_val = vargram_vsp[1-vargram_len]; /* implement default value of the action */
#if vargram_DEBUG != 0
  if (vargram_debug) {
      int i;
      fprintf (stderr, "Reducing via rule %d (line %d), ",
              vargram_n, vargram_rline[vargram_n]);
      /* Print the symbols being reduced, and their result.  */
      for (i = vargram_prhs[vargram_n]; vargram_rhs[i] > 0; i++)
       fprintf (stderr, "%s ", vargram_tname[vargram_rhs[i]]);
      fprintf (stderr, " -> %s\n", vargram_tname[vargram_r1[vargram_n]]); }
#endif
  switch (vargram_n) {
case 1:
{ variable_mangle_result(str_from_c("")); ;
    break;}
case 3:
{ variable_mangle_result(str_from_c("")); ;
    break;}
case 4: {
#if vargram_DEBUG
                  vargram_debug = trace_pretest_;
#endif
             ;
    break;}
case 8:
{ variable_mangle_result(vargram_vsp[0].lv_string); ;
    break;}
case 9: {
                  long               j;
                  for (j = 0; j < vargram_vsp[-1].lv_list.nstrings; ++j) {
                      variable_mangle_result
                      (
                         str_format("%S(%S)", vargram_vsp[-3].lv_string, vargram_vsp[-1].lv_list.string[j])
                      ); }
                  str_free(vargram_vsp[-3].lv_string);
                  string_list_destructor(&vargram_vsp[-1].lv_list);
             ;
    break;}
case 10:
{ vargram_val.lv_string = vargram_vsp[0].lv_string; ;
    break;}
case 11: {
                  vargram_val.lv_string = str_catenate(vargram_vsp[-1].lv_string, vargram_vsp[0].lv_string);
                  str_free(vargram_vsp[-1].lv_string);
                  str_free(vargram_vsp[0].lv_string);
             ;
    break;}
case 12:
{ vargram_val.lv_string = str_from_c("$"); ;
    break;}
case 13: {
                  vargram_val.lv_string = variable_mangle_lookup(vargram_vsp[0].lv_string);
                  str_free(vargram_vsp[0].lv_string);
             ;
    break;}
case 14: {
                  vargram_val.lv_string = variable_mangle_lookup(vargram_vsp[-1].lv_string);
                  str_free(vargram_vsp[-1].lv_string);
             ;
    break;}
case 15: {
                  vargram_val.lv_string = variable_mangle_lookup(vargram_vsp[-1].lv_string);
                  str_free(vargram_vsp[-1].lv_string);
             ;
    break;}
case 16:
{ vargram_val.lv_string = patvar(vargram_vsp[-5].lv_string, vargram_vsp[-3].lv_string, vargram_vsp[-1].lv_string); ;
    break;}
case 17:
{ vargram_val.lv_string = patvar(vargram_vsp[-5].lv_string, vargram_vsp[-3].lv_string, vargram_vsp[-1].lv_string); ;
    break;}
case 18:
{ vargram_val.lv_string = function(vargram_vsp[-3].lv_string, &vargram_vsp[-1].lv_list); ;
    break;}
case 19:
{ vargram_val.lv_string = function(vargram_vsp[-3].lv_string, &vargram_vsp[-1].lv_list); ;
    break;}
case 20:
{ vargram_val.lv_string = vargram_vsp[0].lv_string; ;
    break;}
case 21:
{ vargram_val.lv_string = vargram_vsp[0].lv_string; ;
    break;}
case 22: {
                  vargram_val.lv_string = str_catenate(vargram_vsp[-1].lv_string, vargram_vsp[0].lv_string);
                  str_free(vargram_vsp[-1].lv_string);
                  str_free(vargram_vsp[0].lv_string);
             ;
    break;}
case 23:
{ vargram_val.lv_string = str_from_c(""); ;
    break;}
case 24: {
                  vargram_val.lv_string = str_catenate(vargram_vsp[-1].lv_string, vargram_vsp[0].lv_string);
                  str_free(vargram_vsp[-1].lv_string);
                  str_free(vargram_vsp[0].lv_string);
             ;
    break;}
case 25:
{ vargram_val.lv_string = vargram_vsp[0].lv_string; ;
    break;}
case 26:
{ vargram_val.lv_string = str_from_c(","); ;
    break;}
case 27: {
                  string_list_constructor(&vargram_val.lv_list);
                  string_list_append(&vargram_val.lv_list, vargram_vsp[0].lv_string);
                  str_free(vargram_vsp[0].lv_string);
             ;
    break;}
case 28: {
                  vargram_val.lv_list = vargram_vsp[-2].lv_list;
                  string_list_append(&vargram_val.lv_list, vargram_vsp[0].lv_string);
                  str_free(vargram_vsp[0].lv_string);
             ;
    break;}
case 29: {
                  /*
                   * Guess that empty space separated lists were
                   * really a single space for substitution.
                   * E.g. $(subst $(\n), ,$(list))
                   */
                  vargram_val.lv_string = str_from_c("\" \"");
             ;
    break;}
case 30: {
                  vargram_val.lv_string = wl2str(&vargram_vsp[0].lv_list, 0, vargram_vsp[0].lv_list.nstrings - 1, (char *)0);
                  string_list_destructor(&vargram_vsp[0].lv_list);
             ;
    break;}
case 31: {
                  vargram_val.lv_string = wl2str(&vargram_vsp[-1].lv_list, 0, vargram_vsp[-1].lv_list.nstrings - 1, (char *)0);
                  string_list_destructor(&vargram_vsp[-1].lv_list);
             ;
    break;}
case 32: {
                  string_list_constructor(&vargram_val.lv_list);
                  string_list_append(&vargram_val.lv_list, vargram_vsp[0].lv_string);
                  str_free(vargram_vsp[0].lv_string);
             ;
    break;}
case 33: {
                  vargram_val.lv_list = vargram_vsp[-2].lv_list;
                  string_list_append(&vargram_val.lv_list, vargram_vsp[0].lv_string);
                  str_free(vargram_vsp[0].lv_string);
             ;
    break;}
case 34:
{ vargram_val.lv_string = vargram_vsp[0].lv_string; ;
    break;}
case 35: {
                  vargram_val.lv_string = str_catenate(vargram_vsp[-1].lv_string, vargram_vsp[0].lv_string);
                  str_free(vargram_vsp[-1].lv_string);
                  str_free(vargram_vsp[0].lv_string);
             ;
    break;}
case 36:
{ vargram_val.lv_string = vargram_vsp[0].lv_string; ;
    break;}
case 37:
{ vargram_val.lv_string = str_from_c("\\="); ;
    break;}
case 38:
{ vargram_val.lv_string = str_from_c("\\:"); ;
    break;}
case 39:
{ vargram_val.lv_string = vargram_vsp[0].lv_string; ;
    break;}
case 40:
{ vargram_val.lv_string = vargram_vsp[0].lv_string; ;
    break;}
case 41:
{ vargram_val.lv_string = str_from_c(""); ;
    break;}
case 42: {
                  string_ty   *s;
                  s = wl2str(&vargram_vsp[-1].lv_list, 0, vargram_vsp[-1].lv_list.nstrings, ",");
                  string_list_destructor(&vargram_vsp[-1].lv_list);
                  vargram_val.lv_string = str_format("(%S)", s);
                  str_free(s);
             ;
    break;} }
   /* the action file gets copied in in place of this dollarsign */
  vargram_vsp -= vargram_len;
  vargram_ssp -= vargram_len;
#ifdef vargram_LSP_NEEDED
  vargram_lsp -= vargram_len;
#endif
#if vargram_DEBUG != 0
  if (vargram_debug) {
      short *ssp1 = vargram_ss - 1;
      fprintf (stderr, "state stack now");
      while (ssp1 != vargram_ssp)
       fprintf (stderr, " %d", *++ssp1);
      fprintf (stderr, "\n"); }
#endif
  *++vargram_vsp = vargram_val;
#ifdef vargram_LSP_NEEDED
  vargram_lsp++;
  if (vargram_len == 0) {
      vargram_lsp->first_line = vargram_lloc.first_line;
      vargram_lsp->first_column = vargram_lloc.first_column;
      vargram_lsp->last_line = (vargram_lsp-1)->last_line;
      vargram_lsp->last_column = (vargram_lsp-1)->last_column;
      vargram_lsp->text = 0; }
  else {
      vargram_lsp->last_line = (vargram_lsp+vargram_len-1)->last_line;
      vargram_lsp->last_column = (vargram_lsp+vargram_len-1)->last_column; }
#endif
  /* Now "shift" the result of the reduction.
     Determine what state that goes to,
     based on the state we popped back to
     and the rule number reduced by.  */
  vargram_n = vargram_r1[vargram_n];
  vargram_state = vargram_pgoto[vargram_n - vargram_NTBASE] + *vargram_ssp;
  if (vargram_state >= 0 && vargram_state <= vargram_LAST && vargram_check[vargram_state] == *vargram_ssp)
    vargram_state = vargram_table[vargram_state];
  else
    vargram_state = vargram_defgoto[vargram_n - vargram_NTBASE];
  goto vargram_newstate;
vargram_errlab:   /* here on detecting error */
  if (! vargram_errstatus)
    /* If not already recovering from an error, report this error.  */ {
      ++vargram_nerrs;
#ifdef vargram_ERROR_VERBOSE
      vargram_n = vargram_pact[vargram_state];
      if (vargram_n > vargram_FLAG && vargram_n < vargram_LAST) {
         int size = 0;
         char *msg;
         int x, count;
         count = 0;
         /* Start X at -vargram_n if nec to avoid negative indexes in vargram_check.  */
         for (x = (vargram_n < 0 ? -vargram_n : 0);
              x < (sizeof(vargram_tname) / sizeof(char *)); x++)
           if (vargram_check[x + vargram_n] == x)
             size += strlen(vargram_tname[x]) + 15, count++;
         msg = (char *) malloc(size + 15);
         if (msg != 0) {
             strcpy(msg, "parse error");
             if (count < 5) {
               count = 0;
               for (x = (vargram_n < 0 ? -vargram_n : 0);
                    x < (sizeof(vargram_tname) / sizeof(char *)); x++)
                 if (vargram_check[x + vargram_n] == x) {
                  strcat(msg, count == 0 ? ", expecting `" : " or `");
                  strcat(msg, vargram_tname[x]);
                  strcat(msg, "'");
                  count++; } }
             vargram_error(msg);
             free(msg); }
         else
           vargram_error ("parse error; also virtual memory exceeded"); }
      else
#endif /* vargram_ERROR_VERBOSE */
       vargram_error("parse error"); }
  goto vargram_errlab1;
vargram_errlab1:   /* here on error raised explicitly by an action */
  if (vargram_errstatus == 3) {
      /* if just tried and failed to reuse lookahead token after an error, discard it.  */
      /* return failure if at end of input */
      if (vargram_char == vargram_EOF)
       vargram_ABORT;
#if vargram_DEBUG != 0
      if (vargram_debug)
       fprintf(stderr, "Discarding token %d (%s).\n", vargram_char, vargram_tname[vargram_char1]);
#endif
      vargram_char = vargram_EMPTY; }
  /* Else will try to reuse lookahead token
     after shifting the error token.  */
  vargram_errstatus = 3;             /* Each real token shifted decrements this */
  goto vargram_errhandle;
vargram_errdefault:  /* current state does not do anything special for the error token. */
#if 0
  /* This is wrong; only states that explicitly want error tokens
     should shift them.  */
  vargram_n = vargram_defact[vargram_state];  /* If its default is to accept any token, ok.  Otherwise pop it.*/
  if (vargram_n) goto vargram_default;
#endif
vargram_errpop:   /* pop the current state because it cannot handle the error token */
  if (vargram_ssp == vargram_ss) vargram_ABORT;
  vargram_vsp--;
  vargram_state = *--vargram_ssp;
#ifdef vargram_LSP_NEEDED
  vargram_lsp--;
#endif
#if vargram_DEBUG != 0
  if (vargram_debug) {
      short *ssp1 = vargram_ss - 1;
      fprintf (stderr, "Error: state stack now");
      while (ssp1 != vargram_ssp)
       fprintf (stderr, " %d", *++ssp1);
      fprintf (stderr, "\n"); }
#endif
vargram_errhandle:
  vargram_n = vargram_pact[vargram_state];
  if (vargram_n == vargram_FLAG)
    goto vargram_errdefault;
  vargram_n += vargram_TERROR;
  if (vargram_n < 0 || vargram_n > vargram_LAST || vargram_check[vargram_n] != vargram_TERROR)
    goto vargram_errdefault;
  vargram_n = vargram_table[vargram_n];
  if (vargram_n < 0) {
      if (vargram_n == vargram_FLAG)
       goto vargram_errpop;
      vargram_n = -vargram_n;
      goto vargram_reduce; }
  else if (vargram_n == 0)
    goto vargram_errpop;
  if (vargram_n == vargram_FINAL)
    vargram_ACCEPT;
#if vargram_DEBUG != 0
  if (vargram_debug)
    fprintf(stderr, "Shifting error token, ");
#endif
  *++vargram_vsp = vargram_lval;
#ifdef vargram_LSP_NEEDED
  *++vargram_lsp = vargram_lloc;
#endif
  vargram_state = vargram_n;
  goto vargram_newstate; }
