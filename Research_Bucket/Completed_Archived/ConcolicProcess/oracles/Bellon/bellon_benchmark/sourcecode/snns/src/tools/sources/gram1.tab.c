/*  A Bison parser, made from gram1.y
 by  GNU Bison version 1.25
  */
#define YYBISON 1  /* Identify Bison output.  */
#define yyparse yyzparse
#define yylex yyzlex
#define yyerror yyzerror
#define yylval yyzlval
#define yychar yyzchar
#define yydebug yyzdebug
#define yynerrs yyznerrs
#define        FOR    258
#define        TO     259
#define        DO     260
#define        ENDFOR 261
#define        IF     262
#define        THEN   263
#define        ELSE   264
#define        ENDIF  265
#define        WHILE  266
#define        ENDWHILE       267
#define        REPEAT 268
#define        UNTIL  269
#define        BREAK  270
#define        CONTINUE       271
#define        Delimiter      272
#define        Identifier     273
#define        Assignment     274
#define        OR     275
#define        AND    276
#define        NEQOP  277
#define        EQOP   278
#define        GEQOP  279
#define        LEQOP  280
#define        MOD    281
#define        DIV    282
#define        EXP    283
#define        SQRT   284
#define        LN     285
#define        LOG    286
#define        NOT    287
#define        SIGN   288
  /* C declarations */
#include <config.h>
#include <stdio.h>
#include <stdlib.h>        /* for malloc & free in bison.simple */
#include <math.h>
#include <string.h>        /* for strcat in bison.simple */
#include "symtab.h"        /* for symbol table functions */
#include "ictab.h"         /* for intermediate code (icode_...) functions */
#include "icopjmp.h"       /* for operators and jump function pointers */
#include "backpatch.h"     /* for backpatch operations */
#include "arglist.h"       /* for argument list handling */
#include "glob_typ.h"      /* SNNS-Kernel: Global Datatypes and Constants */
#include "error.h"         /* for yyzerror() */
#define YYERROR_VERBOSE 1  /* verbose error reporting from parser */
extern int yyzlex(void);   /* see 1st comment in this file */
typedef union { struct {        /* bison doesn't know struct as a token type */
  St_ptr_type stp;       /* symbol table pointer */
                         /* 2 backpatch target lists: */
  bp_list *brk;          /* #1 contains positions of break statements */
  bp_list *cont;         /* #2 contains positions of continue statements */
  Ic_ptr_type tmp;       /* instruction # temp buffer for local backpatching */
  arglist_type *arglist; /* pointer to an argument list */
} t; } YYSTYPE;
#ifndef YYDEBUG
#define YYDEBUG 1
#endif
#include <stdio.h>
#ifndef __cplusplus
#ifndef __STDC__
#define const
#endif
#endif
#define        YYFINAL               100
#define        YYFLAG         -32768
#define        YYNTBASE       43
#define YYTRANSLATE(x) ((unsigned)(x) <= 288 ? yytranslate[x] : 58)
static const char yytranslate[] = {     0,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,    40,
    41,    33,    29,    42,    28,     2,    32,     2,     2,     2,
     2,     2,     2,     2,     2,     2,     2,     2,     2,    27,
     2,    25,     2,     2,     2,     2,     2,     2,     2,     2,
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
    16,    17,    18,    19,    20,    21,    22,    23,    24,    26,
    30,    31,    34,    35,    36,    37,    38,    39
};
#if YYDEBUG != 0
static const short yyprhs[] = {     0,
     0,     2,     4,     8,     9,    14,    18,    19,    20,    32,
    33,    34,    42,    43,    44,    51,    52,    60,    65,    67,
    69,    72,    74,    77,    80,    83,    86,    89,    92,    96,
   100,   104,   108,   112,   116,   120,   124,   128,   132,   136,
   140,   144,   148,   152,   154,   158,   159,   161,   163
};
static const short yyrhs[] = {    44,
     0,    45,     0,    44,    17,    45,     0,     0,    18,    40,
    56,    41,     0,    18,    19,    54,     0,     0,     0,     3,
    18,    19,    54,    46,     4,    54,    47,     5,    44,     6,
     0,     0,     0,    11,    48,    54,    49,     5,    44,    12,
     0,     0,     0,    13,    50,    44,    14,    51,    54,     0,
     0,    53,     8,    44,     9,    52,    44,    10,     0,    53,
     8,    44,    10,     0,    15,     0,    16,     0,     7,    54,
     0,    55,     0,    28,    54,     0,    29,    54,     0,    38,
    54,     0,    35,    54,     0,    36,    54,     0,    37,    54,
     0,    54,    29,    54,     0,    54,    28,    54,     0,    54,
    33,    54,     0,    54,    32,    54,     0,    54,    31,    54,
     0,    54,    30,    54,     0,    54,    34,    54,     0,    54,
    27,    54,     0,    54,    25,    54,     0,    54,    23,    54,
     0,    54,    22,    54,     0,    54,    26,    54,     0,    54,
    24,    54,     0,    54,    21,    54,     0,    54,    20,    54,
     0,    18,     0,    40,    54,    41,     0,     0,    57,     0,
    54,     0,    57,    42,    54,     0
};
#endif
#if YYDEBUG != 0
static const short yyrline[] = { 0,
    97,   102,   106,   112,   117,   133,   139,   142,   151,   166,
   168,   172,   185,   187,   189,   199,   206,   215,   223,   228,
   234,   245,   248,   253,   256,   259,   262,   265,   273,   276,
   279,   282,   285,   288,   291,   294,   297,   300,   303,   306,
   309,   312,   315,   319,   321,   324,   326,   329,   331
};
#endif
#if YYDEBUG != 0 || defined (YYERROR_VERBOSE)
static const char * const yytname[] = {   "$","error","$undefined.","FOR","TO",
"DO","ENDFOR","IF","THEN","ELSE","ENDIF","WHILE","ENDWHILE","REPEAT","UNTIL",
"BREAK","CONTINUE","Delimiter","Identifier","Assignment","OR","AND","NEQOP",
"EQOP","GEQOP","'>'","LEQOP","'<'","'-'","'+'","MOD","DIV","'/'","'*'","EXP",
"SQRT","LN","LOG","NOT","SIGN","'('","')'","','","batchprog","stmtList","statement",
"@1","@2","@3","@4","@5","@6","@7","ifexpr","expr","simpleExpr","paramlist",
"fullList", NULL
};
#endif
static const short yyr1[] = {     0,
    43,    44,    44,    45,    45,    45,    46,    47,    45,    48,
    49,    45,    50,    51,    45,    52,    45,    45,    45,    45,
    53,    54,    54,    54,    54,    54,    54,    54,    54,    54,
    54,    54,    54,    54,    54,    54,    54,    54,    54,    54,
    54,    54,    54,    55,    55,    56,    56,    57,    57
};
static const short yyr2[] = {     0,
     1,     1,     3,     0,     4,     3,     0,     0,    11,     0,
     0,     7,     0,     0,     6,     0,     7,     4,     1,     1,
     2,     1,     2,     2,     2,     2,     2,     2,     3,     3,
     3,     3,     3,     3,     3,     3,     3,     3,     3,     3,
     3,     3,     3,     1,     3,     0,     1,     1,     3
};
static const short yydefact[] = {     4,
     0,     0,    10,    13,    19,    20,     0,     1,     2,     0,
     0,    44,     0,     0,     0,     0,     0,     0,     0,    21,
    22,     0,     4,     0,    46,     4,     4,     0,    23,    24,
    26,    27,    28,    25,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
    11,     0,     6,    48,     0,    47,     3,     0,     7,    45,
    43,    42,    39,    38,    41,    37,    40,    36,    30,    29,
    34,    33,    32,    31,    35,     0,    14,     5,     0,    16,
    18,     0,     4,     0,    49,     4,     0,     0,    15,     0,
     8,    12,    17,     0,     4,     0,     9,     0,     0,     0
};
static const short yydefgoto[] = {    98,
     8,     9,    82,    94,    22,    76,    23,    84,    86,    10,
    20,    21,    55,    56
};
static const short yypact[] = {    98,
    -8,    20,-32768,-32768,-32768,-32768,    -1,     3,-32768,    14,
    21,-32768,    20,    20,    20,    20,    20,    20,    20,    97,
-32768,    20,    98,    20,    20,    98,    98,    20,-32768,-32768,
-32768,-32768,-32768,-32768,    66,    20,    20,    20,    20,    20,
    20,    20,    20,    20,    20,    20,    20,    20,    20,    20,
    97,    27,    97,    97,     5,     9,-32768,    33,    97,-32768,
   110,   110,   121,   121,    49,    49,    49,    49,    31,    31,
    11,    11,    11,    11,-32768,    42,-32768,-32768,    20,-32768,
-32768,    48,    98,    20,    97,    98,    20,     4,    97,    -3,
    97,-32768,-32768,    54,    98,     2,-32768,    53,    67,-32768
};
static const short yypgoto[] = {-32768,
   -10,    28,-32768,-32768,-32768,-32768,-32768,-32768,-32768,-32768,
   -13,-32768,-32768,-32768
};
#define        YYLAST         155
static const short yytable[] = {    29,
    30,    31,    32,    33,    34,    35,    93,    97,    51,    11,
    53,    54,    52,    26,    59,    92,    58,    24,    26,    26,
    26,    27,    61,    62,    63,    64,    65,    66,    67,    68,
    69,    70,    71,    72,    73,    74,    75,    12,    25,    28,
    77,    80,    81,    26,    50,    78,    83,    13,    14,    26,
    79,    87,    99,    57,    15,    16,    17,    18,    95,    19,
    46,    47,    48,    49,    50,    85,   100,     0,     0,     0,
    89,     0,    88,    91,     0,    90,    44,    45,    46,    47,
    48,    49,    50,     0,    96,    36,    37,    38,    39,    40,
    41,    42,    43,    44,    45,    46,    47,    48,    49,    50,
     1,     0,     0,     0,     2,     0,    60,     0,     3,     0,
     4,     0,     5,     6,     0,     7,    36,    37,    38,    39,
    40,    41,    42,    43,    44,    45,    46,    47,    48,    49,
    50,    38,    39,    40,    41,    42,    43,    44,    45,    46,
    47,    48,    49,    50,    40,    41,    42,    43,    44,    45,
    46,    47,    48,    49,    50
};
static const short yycheck[] = {    13,
    14,    15,    16,    17,    18,    19,    10,     6,    22,    18,
    24,    25,    23,    17,    28,    12,    27,    19,    17,    17,
    17,     8,    36,    37,    38,    39,    40,    41,    42,    43,
    44,    45,    46,    47,    48,    49,    50,    18,    40,    19,
    14,     9,    10,    17,    34,    41,     5,    28,    29,    17,
    42,     4,     0,    26,    35,    36,    37,    38,     5,    40,
    30,    31,    32,    33,    34,    79,     0,    -1,    -1,    -1,
    84,    -1,    83,    87,    -1,    86,    28,    29,    30,    31,
    32,    33,    34,    -1,    95,    20,    21,    22,    23,    24,
    25,    26,    27,    28,    29,    30,    31,    32,    33,    34,
     3,    -1,    -1,    -1,     7,    -1,    41,    -1,    11,    -1,
    13,    -1,    15,    16,    -1,    18,    20,    21,    22,    23,
    24,    25,    26,    27,    28,    29,    30,    31,    32,    33,
    34,    22,    23,    24,    25,    26,    27,    28,    29,    30,
    31,    32,    33,    34,    24,    25,    26,    27,    28,    29,
    30,    31,    32,    33,    34
};
/* -*-C-*-  Note some compilers choke on comments on `#line' lines.  */
/* Skeleton output parser for bison,
   Copyright (C) 1984, 1989, 1990 Free Software Foundation, Inc.
   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2, or (at your option)
   any later version.
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.  */
/* As a special exception, when this file is copied by Bison into a
   Bison output file, you may use that output file without restriction.
   This special exception was added by the Free Software Foundation
   in version 1.24 of Bison.  */
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
void *alloca ();
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
#define yyerrok               (yyerrstatus = 0)
#define yyclearin      (yychar = YYEMPTY)
#define YYEMPTY               -2
#define YYEOF   0
#define YYACCEPT       return(0)
#define YYABORT        return(1)
#define YYERROR               goto yyerrlab1
/* Like YYERROR except do call yyerror.
   This remains here temporarily to ease the
   transition to the new meaning of YYERROR, for GCC.
   Once GCC version 2 has supplanted version 1, this can go.  */
#define YYFAIL         goto yyerrlab
#define YYRECOVERING()  (!!yyerrstatus)
#define YYBACKUP(token, value) \
do                                    \
  if (yychar == YYEMPTY && yylen == 1)                      \
    { yychar = (token), yylval = (value);               \
      yychar1 = YYTRANSLATE (yychar);                  \
      YYPOPSTACK;                     \
      goto yybackup;                           \
    }                                    \
  else                                    \
    { yyerror ("syntax error: cannot back up"); YYERROR; }     \
while (0)
#define YYTERROR       1
#define YYERRCODE      256
#ifndef YYPURE
#define YYLEX   yylex()
#endif
#ifdef YYPURE
#ifdef YYLSP_NEEDED
#ifdef YYLEX_PARAM
#define YYLEX   yylex(&yylval, &yylloc, YYLEX_PARAM)
#else
#define YYLEX   yylex(&yylval, &yylloc)
#endif
#else /* not YYLSP_NEEDED */
#ifdef YYLEX_PARAM
#define YYLEX   yylex(&yylval, YYLEX_PARAM)
#else
#define YYLEX   yylex(&yylval)
#endif
#endif /* not YYLSP_NEEDED */
#endif
/* If nonreentrant, generate the variables here */
#ifndef YYPURE
int    yychar;         /*  the lookahead symbol         */
YYSTYPE        yylval;                     /*  the semantic value of the               */
                      /*  lookahead symbol                  */
#ifdef YYLSP_NEEDED
YYLTYPE yylloc;                     /*  location data for the lookahead  */
                      /*  symbol                      */
#endif
int yynerrs;      /*  number of parse errors so far       */
#endif  /* not YYPURE */
#if YYDEBUG != 0
int yydebug;      /*  nonzero means print parse trace     */
/* Since this is uninitialized, it does not stop multiple parsers
   from coexisting.  */
#endif
/*  YYINITDEPTH indicates the initial size of the parser's stacks      */
#ifndef        YYINITDEPTH
#define YYINITDEPTH 200
#endif
/*  YYMAXDEPTH is the maximum size the stacks can grow to
    (effective only if the built-in stack extension method is used).  */
#if YYMAXDEPTH == 0
#undef YYMAXDEPTH
#endif
#ifndef YYMAXDEPTH
#define YYMAXDEPTH 10000
#endif
/* Prevent warning if -Wstrict-prototypes.  */
#ifdef __GNUC__
int yyparse (void);
#endif
#if __GNUC__ > 1             /* GNU C and GNU C++ define this.  */
#define __yy_memcpy(TO,FROM,COUNT)     __builtin_memcpy(TO,FROM,COUNT)
#else                  /* not GNU C or C++ */
#ifndef __cplusplus
/* This is the most reliable way to avoid incompatibilities
   in available built-in functions on various systems.  */
static void
__yy_memcpy (to, from, count)
     char *to;
     char *from;
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
__yy_memcpy (char *to, char *from, int count) {
  register char *f = from;
  register char *t = to;
  register int i = count;
  while (i-- > 0)
    *t++ = *f++; }
#endif
#endif
/* The user can define YYPARSE_PARAM as the name of an argument to be passed
   into yyparse.  The argument should have type void *.
   It should actually point to an object.
   Grammar actions can access the variable by casting it
   to the proper pointer type.  */
#ifdef YYPARSE_PARAM
#ifdef __cplusplus
#define YYPARSE_PARAM_ARG void *YYPARSE_PARAM
#define YYPARSE_PARAM_DECL
#else /* not __cplusplus */
#define YYPARSE_PARAM_ARG YYPARSE_PARAM
#define YYPARSE_PARAM_DECL void *YYPARSE_PARAM;
#endif /* not __cplusplus */
#else /* not YYPARSE_PARAM */
#define YYPARSE_PARAM_ARG
#define YYPARSE_PARAM_DECL
#endif /* not YYPARSE_PARAM */
int
yyparse(YYPARSE_PARAM_ARG)
     YYPARSE_PARAM_DECL {
  register int yystate;
  register int yyn;
  register short *yyssp;
  register YYSTYPE *yyvsp;
  int yyerrstatus;     /*  number of tokens to shift before error messages enabled */
  int yychar1 = 0;         /*  lookahead token as an internal (translated) token number */
  short        yyssa[YYINITDEPTH];    /*  the state stack                     */
  YYSTYPE yyvsa[YYINITDEPTH];  /*  the semantic value stack         */
  short *yyss = yyssa;         /*  refer to the stacks thru separate pointers */
  YYSTYPE *yyvs = yyvsa;       /*  to allow yyoverflow to reallocate them elsewhere */
#ifdef YYLSP_NEEDED
  YYLTYPE yylsa[YYINITDEPTH];  /*  the location stack      */
  YYLTYPE *yyls = yylsa;
  YYLTYPE *yylsp;
#define YYPOPSTACK   (yyvsp--, yyssp--, yylsp--)
#else
#define YYPOPSTACK   (yyvsp--, yyssp--)
#endif
  int yystacksize = YYINITDEPTH;
#ifdef YYPURE
  int yychar;
  YYSTYPE yylval;
  int yynerrs;
#ifdef YYLSP_NEEDED
  YYLTYPE yylloc;
#endif
#endif
  YYSTYPE yyval;             /*  the variable used to return           */
                      /*  semantic values from the action        */
                      /*  routines                      */
  int yylen;
#if YYDEBUG != 0
  if (yydebug)
    fprintf(stderr, "Starting parse\n");
#endif
  yystate = 0;
  yyerrstatus = 0;
  yynerrs = 0;
  yychar = YYEMPTY;       /* Cause a token to be read.  */
  /* Initialize stack pointers.
     Waste one element of value and location stack
     so that they stay on the same level as the state stack.
     The wasted elements are never initialized.  */
  yyssp = yyss - 1;
  yyvsp = yyvs;
#ifdef YYLSP_NEEDED
  yylsp = yyls;
#endif
/* Push a new state, which is found in  yystate  .  */
/* In all cases, when you get here, the value and location stacks
   have just been pushed. so pushing a state here evens the stacks.  */
yynewstate:
  *++yyssp = yystate;
  if (yyssp >= yyss + yystacksize - 1) {
      /* Give user a chance to reallocate the stack */
      /* Use copies of these so that the &'s don't force the real ones into memory. */
      YYSTYPE *yyvs1 = yyvs;
      short *yyss1 = yyss;
#ifdef YYLSP_NEEDED
      YYLTYPE *yyls1 = yyls;
#endif
      /* Get the current used size of the three stacks, in elements.  */
      int size = yyssp - yyss + 1;
#ifdef yyoverflow
      /* Each stack pointer address is followed by the size of
        the data in use in that stack, in bytes.  */
#ifdef YYLSP_NEEDED
      /* This used to be a conditional around just the two extra args,
        but that might be undefined if yyoverflow is a macro.  */
      yyoverflow("parser stack overflow",
              &yyss1, size * sizeof (*yyssp),
              &yyvs1, size * sizeof (*yyvsp),
              &yyls1, size * sizeof (*yylsp),
              &yystacksize);
#else
      yyoverflow("parser stack overflow",
              &yyss1, size * sizeof (*yyssp),
              &yyvs1, size * sizeof (*yyvsp),
              &yystacksize);
#endif
      yyss = yyss1; yyvs = yyvs1;
#ifdef YYLSP_NEEDED
      yyls = yyls1;
#endif
#else /* no yyoverflow */
      /* Extend the stack our own way.  */
      if (yystacksize >= YYMAXDEPTH) {
         yyerror("parser stack overflow");
         return 2; }
      yystacksize *= 2;
      if (yystacksize > YYMAXDEPTH)
       yystacksize = YYMAXDEPTH;
      yyss = (short *) alloca (yystacksize * sizeof (*yyssp));
      __yy_memcpy ((char *)yyss, (char *)yyss1, size * sizeof (*yyssp));
      yyvs = (YYSTYPE *) alloca (yystacksize * sizeof (*yyvsp));
      __yy_memcpy ((char *)yyvs, (char *)yyvs1, size * sizeof (*yyvsp));
#ifdef YYLSP_NEEDED
      yyls = (YYLTYPE *) alloca (yystacksize * sizeof (*yylsp));
      __yy_memcpy ((char *)yyls, (char *)yyls1, size * sizeof (*yylsp));
#endif
#endif /* no yyoverflow */
      yyssp = yyss + size - 1;
      yyvsp = yyvs + size - 1;
#ifdef YYLSP_NEEDED
      yylsp = yyls + size - 1;
#endif
#if YYDEBUG != 0
      if (yydebug)
       fprintf(stderr, "Stack size increased to %d\n", yystacksize);
#endif
      if (yyssp >= yyss + yystacksize - 1)
       YYABORT; }
#if YYDEBUG != 0
  if (yydebug)
    fprintf(stderr, "Entering state %d\n", yystate);
#endif
  goto yybackup;
 yybackup:
/* Do appropriate processing given the current state.  */
/* Read a lookahead token if we need one and don't already have one.  */
/* yyresume: */
  /* First try to decide what to do without reference to lookahead token.  */
  yyn = yypact[yystate];
  if (yyn == YYFLAG)
    goto yydefault;
  /* Not known => get a lookahead token if don't already have one.  */
  /* yychar is either YYEMPTY or YYEOF
     or a valid token in external form.  */
  if (yychar == YYEMPTY) {
#if YYDEBUG != 0
      if (yydebug)
       fprintf(stderr, "Reading a token: ");
#endif
      yychar = YYLEX; }
  /* Convert token to internal form (in yychar1) for indexing tables with */
  if (yychar <= 0)         /* This means end of input. */ {
      yychar1 = 0;
      yychar = YYEOF;   /* Don't call YYLEX any more */
#if YYDEBUG != 0
      if (yydebug)
       fprintf(stderr, "Now at end of input.\n");
#endif
    }
  else {
      yychar1 = YYTRANSLATE(yychar);
#if YYDEBUG != 0
      if (yydebug) {
         fprintf (stderr, "Next token is %d (%s", yychar, yytname[yychar1]);
         /* Give the individual parser a way to print the precise meaning
            of a token, for further debugging info.  */
#ifdef YYPRINT
         YYPRINT (stderr, yychar, yylval);
#endif
         fprintf (stderr, ")\n"); }
#endif
    }
  yyn += yychar1;
  if (yyn < 0 || yyn > YYLAST || yycheck[yyn] != yychar1)
    goto yydefault;
  yyn = yytable[yyn];
  /* yyn is what to do for this token type in this state.
     Negative => reduce, -yyn is rule number.
     Positive => shift, yyn is new state.
       New state is final state => don't bother to shift,
       just return success.
     0, or most negative number => error.  */
  if (yyn < 0) {
      if (yyn == YYFLAG)
       goto yyerrlab;
      yyn = -yyn;
      goto yyreduce; }
  else if (yyn == 0)
    goto yyerrlab;
  if (yyn == YYFINAL)
    YYACCEPT;
  /* Shift the lookahead token.  */
#if YYDEBUG != 0
  if (yydebug)
    fprintf(stderr, "Shifting token %d (%s), ", yychar, yytname[yychar1]);
#endif
  /* Discard the token being shifted unless it is eof.  */
  if (yychar != YYEOF)
    yychar = YYEMPTY;
  *++yyvsp = yylval;
#ifdef YYLSP_NEEDED
  *++yylsp = yylloc;
#endif
  /* count tokens shifted since error; after three, turn off error status.  */
  if (yyerrstatus) yyerrstatus--;
  yystate = yyn;
  goto yynewstate;
/* Do the default action for the current state.  */
yydefault:
  yyn = yydefact[yystate];
  if (yyn == 0)
    goto yyerrlab;
/* Do a reduction.  yyn is the number of a rule to reduce with.  */
yyreduce:
  yylen = yyr2[yyn];
  if (yylen > 0)
    yyval = yyvsp[1-yylen]; /* implement default value of the action */
#if YYDEBUG != 0
  if (yydebug) {
      int i;
      fprintf (stderr, "Reducing via rule %d (line %d), ",
              yyn, yyrline[yyn]);
      /* Print the symbols being reduced, and their result.  */
      for (i = yyprhs[yyn]; yyrhs[i] > 0; i++)
       fprintf (stderr, "%s ", yytname[yyrhs[i]]);
      fprintf (stderr, " -> %s\n", yytname[yyr1[yyn]]); }
#endif
  switch (yyn) {
case 1:
{ bp_backpatch(yyvsp[0].t.brk, get_ic_pos()+1);
                          /* breaks in the outermost block cause exit */ ;
    break;}
case 2:
{ yyval.t.cont = yyvsp[0].t.cont; 
                    yyval.t.brk = yyvsp[0].t.brk ;
    break;}
case 3:
{ yyval.t.cont = bp_merge(yyvsp[-2].t.cont, yyvsp[0].t.cont); 
                    yyval.t.brk = bp_merge(yyvsp[-2].t.brk, yyvsp[0].t.brk);
                    /* concatenate break and continue lists */ ;
    break;}
case 4:
{ yyval.t.cont = BP_NULL; 
                    yyval.t.brk = BP_NULL
                    /* there were no cont or break statements */ ;
    break;}
case 5:
{ Val_type dmy;
                    Data_type data;
                    st_get_val_type(yyvsp[-3].t.stp, &data, &dmy);
                    /* check wether it is really a known function */
                    if (data != FCT)
                      yyzerror("Function name invalid"); 
                    icode_jacket(yyvsp[-3].t.stp, yyvsp[-1].t.arglist);
                    /* call jacket fct. with fct. name and pointer
                       to it's argument pointer list */
                    new_arglist();
                    /* prepare list for next fct.-call statement */
                    yyval.t.cont = BP_NULL; 
                    yyval.t.brk = BP_NULL
                    /* there were no cont or break statements */ ;
    break;}
case 6:
{ icode_op(assign, yyvsp[-2].t.stp, yyvsp[0].t.stp, 0);
                    yyval.t.cont = BP_NULL; 
                    yyval.t.brk = BP_NULL
                    /* there were no cont or break statements */ ;
    break;}
case 7:
{ icode_op(assign, yyvsp[-2].t.stp, yyvsp[0].t.stp, 0);
                    /* assign result of expr to Identifier */ ;
    break;}
case 8:
{ yyval.t.stp = st_insert(newtmp());
                    icode_op(less_eq, yyval.t.stp, yyvsp[-5].t.stp, yyvsp[0].t.stp);
                    /* instruction to evaluate loop condition */
                    yyval.t.tmp = get_ic_pos();
                    /* store position of less_eq */
                    icode_jmp(jmp_false, 0, yyval.t.stp); 
                    /* exit FOR loop if condition is false */
                    yyval.t.brk = bp_makelist(get_ic_pos())
                    /* store position of jmp_false */ ;
    break;}
case 9:
{ icode_op(add,yyvsp[-9].t.stp,yyvsp[-9].t.stp, st_lookup("%ONE"));
                    /* increment Identifier (loop counter) by one */
                    icode_jmp(jmp, yyvsp[-3].t.tmp, 0);
                    /* jump back to condition evaluation */
                    yyval.t.brk = bp_merge(yyvsp[-3].t.brk, yyvsp[-1].t.brk);
                    /* merge jumps that exit the FOR loop... */
                    bp_backpatch(yyval.t.brk, get_ic_pos()+1);
                          /* and bp them. */
                    bp_backpatch(yyvsp[-1].t.cont, get_ic_pos()-1);
                    /* bp possible cont-stmts from stmtList:
                       let them jump to ENDFOR (increment) */
                          yyval.t.cont = BP_NULL; 
                    yyval.t.brk = BP_NULL
                    /* there were no unresolved cont or break stmts */ ;
    break;}
case 10:
{ yyval.t.tmp = get_ic_pos()+1 ;
    break;}
case 11:
{ icode_jmp(jmp_false, 0, yyvsp[0].t.stp); 
                    /* exit WHILE loop if expr is false */
                          yyval.t.brk = bp_makelist(get_ic_pos())
                    /* store position of jmp_false */ ;
    break;}
case 12:
{ icode_jmp(jmp, yyvsp[-5].t.tmp, 0);
                    /* jump back to expr */
                    yyval.t.brk = bp_merge(yyvsp[-3].t.brk, yyvsp[-1].t.brk);
                    /* merge jumps that exit the WHILE loop... */
                    bp_backpatch(yyval.t.brk, get_ic_pos()+1);
                          /* and bp them. */
                    bp_backpatch(yyvsp[-1].t.cont, yyvsp[-5].t.tmp);
                    /* bp possible cont-stmts from stmtList */
                          yyval.t.cont = BP_NULL;
                    yyval.t.brk = BP_NULL
                    /* there were no unresolved cont or break stmts */ ;
    break;}
case 13:
{ yyval.t.tmp = get_ic_pos()+1;
                          /* store position of following stmtList */ ;
    break;}
case 14:
{ yyval.t.tmp = get_ic_pos()+1;
                          /* store position of following expr */ ;
    break;}
case 15:
{ icode_jmp(jmp_false, yyvsp[-4].t.tmp, yyvsp[0].t.stp);
                    /* jump back to begin of stmtList */
                    bp_backpatch(yyvsp[-3].t.brk, get_ic_pos()+1);
                    /* bp possible break-stmts from stmtList */
                    bp_backpatch(yyvsp[-3].t.cont, yyvsp[-1].t.tmp);
                    /* bp possible cont-stmts from stmtList */
                          yyval.t.cont = BP_NULL;
                    yyval.t.brk = BP_NULL
                    /* there were no unresolved cont or break stmts */ ;
    break;}
case 16:
{ icode_jmp(jmp_true, 0, yyvsp[-3].t.stp); 
                    /* jump over the else block if expr is true */
                          yyval.t.brk = bp_makelist(get_ic_pos());
                    /* store position of jmp_true */ 
                          yyval.t.tmp = get_ic_pos()+1
                    /* store position of else block */ ;
    break;}
case 17:
{ bp_backpatch(yyvsp[-6].t.brk, yyvsp[-2].t.tmp);
                    /* backpatch jump over the if block */
                    bp_backpatch(yyvsp[-2].t.brk, get_ic_pos()+1);
                    /* backpatch jump over the else block */
                    yyval.t.cont = bp_merge(yyvsp[-4].t.cont, yyvsp[-1].t.cont);
                    yyval.t.brk = bp_merge(yyvsp[-4].t.brk, yyvsp[-1].t.brk)
                    /* merge possible continue and break lists 
                       from both if and else blocks and return them */ ;
    break;}
case 18:
{ bp_backpatch(yyvsp[-3].t.brk, get_ic_pos()+1);
                    /* backpatch the jump after expr */
                    yyval.t.cont = yyvsp[-1].t.cont; 
                    /* pass possible continues... */
                    yyval.t.brk = yyvsp[-1].t.brk
                    /* and breaks to the surrounding loop */ ;
    break;}
case 19:
{ icode_jmp(jmp, 0, 0); 
                    yyval.t.cont = BP_NULL;
                          yyval.t.brk = bp_makelist(get_ic_pos());
                          /* store position of jump */ ;
    break;}
case 20:
{ icode_jmp(jmp, 0, 0); 
                          yyval.t.cont = bp_makelist(get_ic_pos()); 
                          /* store position of jump */
                    yyval.t.brk = BP_NULL; ;
    break;}
case 21:
{ icode_jmp(jmp_false, 0, yyvsp[0].t.stp); 
                    /* jump over the if block if expr is false */
                          yyval.t.brk = bp_makelist(get_ic_pos());
                    /* store position of jmp_false */ 
                    yyval.t.tmp = yyvsp[0].t.stp;
                    yyval.t.stp = yyvsp[0].t.stp;
                    /* pass result of expr to the else alternative */ ;
    break;}
case 22:
{ yyval.t.stp = yyvsp[0].t.stp ;
    break;}
case 23:
{ yyval.t.stp = st_insert(newtmp()); 
                      icode_op(sub,yyval.t.stp,st_lookup("%ZERO"),yyvsp[0].t.stp) 
                      /* negate the value of expr by subtracting it 
                         from the zero built-in constant */ ;
    break;}
case 24:
{ yyval.t.stp = yyvsp[0].t.stp 
                      /* if one wants to use a meaningless + sign */;
    break;}
case 25:
{ yyval.t.stp = st_insert(newtmp()); 
                      icode_op(not,yyval.t.stp,yyvsp[0].t.stp,0) ;
    break;}
case 26:
{ yyval.t.stp = st_insert(newtmp()); 
                      icode_op(sq_rt,yyval.t.stp,yyvsp[0].t.stp,0) ;
    break;}
case 27:
{ yyval.t.stp = st_insert(newtmp()); 
                      icode_op(ln,yyval.t.stp,yyvsp[0].t.stp,0) ;
    break;}
case 28:
{ yyval.t.stp = st_insert(newtmp()); 
                      icode_op(lg,yyval.t.stp,yyvsp[0].t.stp,0) ;
    break;}
case 29:
{ yyval.t.stp = st_insert(newtmp()); 
                      icode_op(add,yyval.t.stp,yyvsp[-2].t.stp,yyvsp[0].t.stp) ;
    break;}
case 30:
{ yyval.t.stp = st_insert(newtmp()); 
                      icode_op(sub,yyval.t.stp,yyvsp[-2].t.stp,yyvsp[0].t.stp) ;
    break;}
case 31:
{ yyval.t.stp = st_insert(newtmp()); 
                      icode_op(mult,yyval.t.stp,yyvsp[-2].t.stp,yyvsp[0].t.stp) ;
    break;}
case 32:
{ yyval.t.stp = st_insert(newtmp()); 
                      icode_op(dvde,yyval.t.stp,yyvsp[-2].t.stp,yyvsp[0].t.stp) ;
    break;}
case 33:
{ yyval.t.stp = st_insert(newtmp()); 
                      icode_op(intdiv,yyval.t.stp,yyvsp[-2].t.stp,yyvsp[0].t.stp) ;
    break;}
case 34:
{ yyval.t.stp = st_insert(newtmp()); 
                      icode_op(mod,yyval.t.stp,yyvsp[-2].t.stp,yyvsp[0].t.stp) ;
    break;}
case 35:
{ yyval.t.stp = st_insert(newtmp()); 
                      icode_op(bmraise,yyval.t.stp,yyvsp[-2].t.stp,yyvsp[0].t.stp) ;
    break;}
case 36:
{ yyval.t.stp = st_insert(newtmp());
                      icode_op(less,yyval.t.stp,yyvsp[-2].t.stp,yyvsp[0].t.stp) ;
    break;}
case 37:
{ yyval.t.stp = st_insert(newtmp());
                      icode_op(greater,yyval.t.stp,yyvsp[-2].t.stp,yyvsp[0].t.stp) ;
    break;}
case 38:
{ yyval.t.stp = st_insert(newtmp());
                      icode_op(eq,yyval.t.stp,yyvsp[-2].t.stp,yyvsp[0].t.stp) ;
    break;}
case 39:
{ yyval.t.stp = st_insert(newtmp());
                      icode_op(not_eq,yyval.t.stp,yyvsp[-2].t.stp,yyvsp[0].t.stp) ;
    break;}
case 40:
{ yyval.t.stp = st_insert(newtmp());
                      icode_op(less_eq,yyval.t.stp,yyvsp[-2].t.stp,yyvsp[0].t.stp) ;
    break;}
case 41:
{ yyval.t.stp = st_insert(newtmp());
                      icode_op(great_eq,yyval.t.stp,yyvsp[-2].t.stp,yyvsp[0].t.stp) ;
    break;}
case 42:
{ yyval.t.stp = st_insert(newtmp());
                      icode_op(and,yyval.t.stp,yyvsp[-2].t.stp,yyvsp[0].t.stp) ;
    break;}
case 43:
{ yyval.t.stp = st_insert(newtmp());
                      icode_op(or,yyval.t.stp,yyvsp[-2].t.stp,yyvsp[0].t.stp) ;
    break;}
case 44:
{ yyval.t.stp = yyvsp[0].t.stp ;
    break;}
case 45:
{ yyval.t.stp = yyvsp[-1].t.stp ;
    break;}
case 46:
{ yyval.t.arglist = ARG_NULL ;
    break;}
case 47:
{ yyval.t.arglist = yyvsp[0].t.arglist ;
    break;}
case 48:
{ yyval.t.arglist = add_to_arglist(yyvsp[0].t.stp) ;
    break;}
case 49:
{ yyval.t.arglist = add_to_arglist(yyvsp[0].t.stp)
                      /* ST pointers to the arguments of a function
                         are stored in the argument pointer list */ ;
    break;} }
   /* the action file gets copied in in place of this dollarsign */
  yyvsp -= yylen;
  yyssp -= yylen;
#ifdef YYLSP_NEEDED
  yylsp -= yylen;
#endif
#if YYDEBUG != 0
  if (yydebug) {
      short *ssp1 = yyss - 1;
      fprintf (stderr, "state stack now");
      while (ssp1 != yyssp)
       fprintf (stderr, " %d", *++ssp1);
      fprintf (stderr, "\n"); }
#endif
  *++yyvsp = yyval;
#ifdef YYLSP_NEEDED
  yylsp++;
  if (yylen == 0) {
      yylsp->first_line = yylloc.first_line;
      yylsp->first_column = yylloc.first_column;
      yylsp->last_line = (yylsp-1)->last_line;
      yylsp->last_column = (yylsp-1)->last_column;
      yylsp->text = 0; }
  else {
      yylsp->last_line = (yylsp+yylen-1)->last_line;
      yylsp->last_column = (yylsp+yylen-1)->last_column; }
#endif
  /* Now "shift" the result of the reduction.
     Determine what state that goes to,
     based on the state we popped back to
     and the rule number reduced by.  */
  yyn = yyr1[yyn];
  yystate = yypgoto[yyn - YYNTBASE] + *yyssp;
  if (yystate >= 0 && yystate <= YYLAST && yycheck[yystate] == *yyssp)
    yystate = yytable[yystate];
  else
    yystate = yydefgoto[yyn - YYNTBASE];
  goto yynewstate;
yyerrlab:   /* here on detecting error */
  if (! yyerrstatus)
    /* If not already recovering from an error, report this error.  */ {
      ++yynerrs;
#ifdef YYERROR_VERBOSE
      yyn = yypact[yystate];
      if (yyn > YYFLAG && yyn < YYLAST) {
         int size = 0;
         char *msg;
         int x, count;
         count = 0;
         /* Start X at -yyn if nec to avoid negative indexes in yycheck.  */
         for (x = (yyn < 0 ? -yyn : 0);
              x < (sizeof(yytname) / sizeof(char *)); x++)
           if (yycheck[x + yyn] == x)
             size += strlen(yytname[x]) + 15, count++;
         msg = (char *) malloc(size + 15);
         if (msg != 0) {
             strcpy(msg, "parse error");
             if (count < 5) {
               count = 0;
               for (x = (yyn < 0 ? -yyn : 0);
                    x < (sizeof(yytname) / sizeof(char *)); x++)
                 if (yycheck[x + yyn] == x) {
                  strcat(msg, count == 0 ? ", expecting `" : " or `");
                  strcat(msg, yytname[x]);
                  strcat(msg, "'");
                  count++; } }
             yyerror(msg);
             free(msg); }
         else
           yyerror ("parse error; also virtual memory exceeded"); }
      else
#endif /* YYERROR_VERBOSE */
       yyerror("parse error"); }
  goto yyerrlab1;
yyerrlab1:   /* here on error raised explicitly by an action */
  if (yyerrstatus == 3) {
      /* if just tried and failed to reuse lookahead token after an error, discard it.  */
      /* return failure if at end of input */
      if (yychar == YYEOF)
       YYABORT;
#if YYDEBUG != 0
      if (yydebug)
       fprintf(stderr, "Discarding token %d (%s).\n", yychar, yytname[yychar1]);
#endif
      yychar = YYEMPTY; }
  /* Else will try to reuse lookahead token
     after shifting the error token.  */
  yyerrstatus = 3;         /* Each real token shifted decrements this */
  goto yyerrhandle;
yyerrdefault:  /* current state does not do anything special for the error token. */
#if 0
  /* This is wrong; only states that explicitly want error tokens
     should shift them.  */
  yyn = yydefact[yystate];  /* If its default is to accept any token, ok.  Otherwise pop it.*/
  if (yyn) goto yydefault;
#endif
yyerrpop:   /* pop the current state because it cannot handle the error token */
  if (yyssp == yyss) YYABORT;
  yyvsp--;
  yystate = *--yyssp;
#ifdef YYLSP_NEEDED
  yylsp--;
#endif
#if YYDEBUG != 0
  if (yydebug) {
      short *ssp1 = yyss - 1;
      fprintf (stderr, "Error: state stack now");
      while (ssp1 != yyssp)
       fprintf (stderr, " %d", *++ssp1);
      fprintf (stderr, "\n"); }
#endif
yyerrhandle:
  yyn = yypact[yystate];
  if (yyn == YYFLAG)
    goto yyerrdefault;
  yyn += YYTERROR;
  if (yyn < 0 || yyn > YYLAST || yycheck[yyn] != YYTERROR)
    goto yyerrdefault;
  yyn = yytable[yyn];
  if (yyn < 0) {
      if (yyn == YYFLAG)
       goto yyerrpop;
      yyn = -yyn;
      goto yyreduce; }
  else if (yyn == 0)
    goto yyerrpop;
  if (yyn == YYFINAL)
    YYACCEPT;
#if YYDEBUG != 0
  if (yydebug)
    fprintf(stderr, "Shifting error token, ");
#endif
  *++yyvsp = yylval;
#ifdef YYLSP_NEEDED
  *++yylsp = yylloc;
#endif
  yystate = yyn;
  goto yynewstate; }
