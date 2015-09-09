/*  A Bison parser, made from cook/hashline.y with Bison version GNU Bison version 1.22
  */
#define hashline_BISON 1  /* Identify Bison output.  */
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
#define        HASH_ELIF      284
#define        HASH_ELSE      285
#define        HASH_ENDIF     286
#define        HASH_IF        287
#define        HASH_IFDEF     288
#define        HASH_IFNDEF    289
#define        HASH_INCLUDE   290
#define        HASH_INCLUDE_COOKED    291
#define        HASH_INCLUDE_COOKED2   292
#define        HASH_LINE      293
#define        HASH_PRAGMA    294
#define        HASH_JUNK      295
#include <ac/stdio.h>
#include <ac/stddef.h>
#include <ac/string.h>
#include <ac/time.h>
#include <ac/stdlib.h>
#include <cook.h>
#include <expr.h>
#include <expr/catenate.h>
#include <expr/constant.h>
#include <expr/function.h>
#include <expr/list.h>
#include <hashline.h>
#include <lex.h>
#include <mem.h>
#include <opcode/context.h>
#include <option.h>
#include <os_interface.h>
#include <sub.h>
#include <trace.h>
#include <str_list.h>
static string_list_ty done_once;
typedef struct cond cond;
struct cond {
       int   pass;
       int   state;
       cond  *next;
};
static cond    *stack;
static cond    *cond_free_list;
#ifdef DEBUG
#define hashline_DEBUG 1
#define printf trace_where(__FILE__, __LINE__), lex_trace
extern int hashline_debug;
#endif
#define hashline_error parse_error
static expr_position_ty *curpos _((void));
static expr_position_ty *
curpos() {
       static expr_position_ty pos;
       pos.pos_name = lex_cur_file();
       pos.pos_line = lex_cur_line();
       return &pos; }
/*
 * NAME
 *     open_include - open an include file
 *
 * SYNOPSIS
 *     void open_include(string_ty *filename);
 *
 * DESCRIPTION
 *     The open_include function is used to search for a given file name in
 *     the include path and lex_open it when found.
 *
 * RETURNS
 *     void
 */
static void open_include_once _((string_ty *, string_ty *));
static void
open_include_once(logical, physical)
       string_ty     *logical;
       string_ty     *physical; {
       if (!string_list_member(&done_once, physical))
             lex_open_include(logical, physical); }
void
hashline_reset() {
       string_list_destructor(&done_once); }
static void open_include _((string_ty *, int));
static void
open_include(filename, local)
       string_ty     *filename;
       int     local; {
       int     j;
       string_ty     *path;
       trace(("open_include(filename = %08lX, local = %d) entry",
             filename, local));
       trace_string(filename->str_text);
       if (filename->str_text[0] != '/') {
             if (local) {
                  string_ty   *s;
                  s = lex_cur_file();
                  if (strchr(s->str_text, '/')) {
                      s = os_dirname(s);
                      if (!s) {
                         bomb:
                         hashline_error
                         (
                          "unable to construct include file name"
                         );
                         goto ret; }
                      path = str_format("%S/%S", s, filename);
                      str_free(s); }
                  else
                      path = str_copy(filename);
                  switch (os_exists(path)) {
                  case -1:
                      str_free(path);
                      goto bomb;
                  case 1:
                      open_include_once(filename, path);
                      str_free(path);
                      goto ret; }
                  str_free(path); }
             for (j = 0; j < option.o_search_path.nstrings; ++j) {
                  path =
                      str_format
                      (
                         "%S/%S",
                         option.o_search_path.string[j],
                         filename
                      );
                  switch (os_exists(path)) {
                  case -1:
                      str_free(path);
                      goto bomb;
                  case 1:
                      open_include_once(filename, path);
                      str_free(path);
                      goto ret; }
                  str_free(path); } }
       open_include_once(filename, filename);
       ret:
       trace((/*{*/"}\n")); }
/*
 * NAME
 *     hashline - the # control line processor
 *
 * SYNOPSIS
 *     void hashline(void);
 *
 * DESCRIPTION
 *     The hashline function is used to process # control lines.
 *
 * RETURNS
 *     void
 */
void
hashline() {
       int hashline_parse _((void)); /* forward */
       trace(("hashline()\n{\n"/*}*/));
#if hashline_DEBUG
       hashline_debug = trace_pretest_;
#endif
       hashline_lex_reset();
       hashline_parse();
       trace((/*{*/"}\n")); }
/*
 * NAME
 *     cond_alloc - allocate a condition structure
 *
 * SYNOPSIS
 *     cond *cond_alloc(void);
 *
 * DESCRIPTION
 *     The cond_alloc function is used to allocate a condition structure
 *     from dynamic memory.
 *
 * RETURNS
 *     cond * - pointer to condition structure.
 *
 * CAVEAT
 *     A free list is maintained to avoid malloc overheads.
 */
static cond *cond_alloc _((void));
static cond *
cond_alloc() {
       cond   *c;
       if (cond_free_list) {
             c = cond_free_list;
             cond_free_list = c->next; }
       else
             c = (cond *)mem_alloc(sizeof(cond));
       return c; }
/*
 * NAME
 *     cond_free - free condition structure
 *
 * SYNOPSIS
 *     void cond_free(cond*);
 *
 * DESCRIPTION
 *     The cond_free function is used to indicate that a condition structure
 *     is finished with.
 *
 * RETURNS
 *     void
 *
 * CAVEAT
 *     A free list is maintained to avoid malloc overheads.
 */
static void cond_free _((cond *));
static void
cond_free(c)
       cond   *c; {
       c->next = cond_free_list;
       cond_free_list = c; }
/*
 * NAME
 *     hash_include - process #include directive
 *
 * SYNOPSIS
 *     void hash_include(expr_list_ty *filename);
 *
 * DESCRIPTION
 *     The hash_include function is used to process #include directives.
 *
 * RETURNS
 *     void
 */
static void hash_include _((expr_list_ty *));
static void
hash_include(elp)
       expr_list_ty  *elp; {
       string_list_ty        *result;
       string_ty     *s;
       size_t               j;
       /*
        * if conditional is false, don't do
        */
       if (stack && !stack->pass)
             return;
       /*
        * turn the expressions into words
        */
       result = expr_list_evaluate(elp, 0);
       if (!result) {
             hashline_error("expression evaluation failed");
             return; }
       /*
        * include each file
        */
       for (j = 0; j < result->nstrings; ++j) {
             s = result->string[j];
             if
             (
                  s->str_length > 2
             &&
                  s->str_text[0] == '<'
             &&
                  s->str_text[s->str_length - 1] == '>'
             ) {
                  s = str_n_from_c(s->str_text + 1, s->str_length - 2);
                  open_include(s, 0);
                  str_free(s); }
             else {
                  if (s->str_length)
                      open_include(s, 1);
                  else {
                      hashline_error
                      (
                       "expression produces null file name to include"
                      ); } } }
       string_list_delete(result); }
/*
 * NAME
 *     hash_include - process #include-cooked directive
 *
 * SYNOPSIS
 *     void hash_include_cooked(expr_list_ty *filename);
 *
 * DESCRIPTION
 *     The hash_include_cooked function is used to
 *     process #include-cooked directives.
 *
 * RETURNS
 *     void
 */
static void hash_include_cooked _((expr_list_ty *, int));
static void
hash_include_cooked(elp, warn)
       expr_list_ty  *elp;
       int     warn; {
       string_list_ty        *logical;
       string_ty     *s;
       long   j;
       string_list_ty        physical;
       long   nerr;
       opcode_context_ty *ocp;
       /*
        * if conditional is false, don't do
        */
       if (stack && !stack->pass)
             return;
       /*
        * turn the expressions into words
        */
       logical = expr_list_evaluate(elp, 0);
       if (!logical) {
             hashline_error("expression evaluation failed");
             return; }
       /*
        * make sure we like the words they used
        */
       nerr = 0;
       for (j = 0; j < logical->nstrings; ++j) {
             s = logical->string[j];
             if
             (
                  s->str_length > 2
             &&
                  s->str_text[0] == '<'
             &&
                  s->str_text[s->str_length - 1] == '>'
             ) {
                  hashline_error
                  (
                         "may not use angle brackets with #include-cooked"
                  );
                  ++nerr; }
             else if (!s->str_length) {
                  hashline_error
                  (
                      "expression produces null file name to include"
                  );
                  ++nerr; } }
       if (nerr) {
             string_list_delete(logical);
             return; }
       /*
        * append to the auto-cook list
        *
        * If any of the auto-cook list are out-of-date,
        * they are recooked, and then cook starts over.
        */
       cook_auto(logical);
       /*
        * resolve the words into paths
        */
       string_list_constructor(&physical);
       ocp = opcode_context_new(0, 0);
       cook_mtime_resolve(ocp, &physical, logical, 0);
       opcode_context_delete(ocp);
       /*
        * include the resolved paths,
        * warning if they do not exist
        * (they will later, hopefully)
        */
       assert(logical->nstrings == physical.nstrings);
       for (j = 0; j < physical.nstrings; ++j) {
             s = physical.string[j];
             if (os_exists(s))
                  open_include_once(logical->string[j], s);
             else if (warn) {
                  sub_context_ty      *scp;
                  scp = sub_context_new();
                  sub_var_set(scp, "File_Name", "%S", s);
                  lex_warning
                  (
                      scp,
                      i18n("include cooked \"$filename\": file not found")
                  );
                  sub_context_delete(scp); } }
       string_list_destructor(&physical);
       string_list_delete(logical); }
/*
 * NAME
 *     hash_if - process #if directive
 *
 * SYNOPSIS
 *     void hash_if(expr_ty *);
 *
 * DESCRIPTION
 *     The hash_if function is used to process #if directives.
 *
 * RETURNS
 *     void
 */
static void hash_if _((expr_ty *));
static void
hash_if(ep)
       expr_ty             *ep; {
       cond   *c;
       trace(("hash_if(ep = %08lX)\n{\n"/*}*/, ep));
       c = cond_alloc();
       c->next = stack;
       if (stack && !stack->pass) {
             c->pass = 0;
             c->state = 1;
             lex_passing(0); }
       else {
             switch (expr_eval_condition(ep, 0)) {
             case -1:
                  hashline_error("expression evaluation failed");
                  /* fall through... */
             case 0:
                  c->pass = 0;
                  c->state = 2;
                  lex_passing(0);
                  break;
             default:
                  c->pass = 1;
                  c->state = 1;
                  lex_passing(1);
                  break; } }
       stack = c;
       trace((/*{*/"}\n")); }
/*
 * NAME
 *     hash_ifdef - process #ifdef directive
 *
 * SYNOPSIS
 *     void hash_ifdef(expr_ty*);
 *
 * DESCRIPTION
 *     The hash_ifdef function is used to process #ifdef directives.
 *
 * RETURNS
 *     void
 */
static void hash_ifdef _((expr_ty *));
static void
hash_ifdef(ep)
       expr_ty             *ep; {
       expr_ty             *e1;
       expr_ty             *e2;
       string_ty     *s;
       trace(("hash_ifdef(ep = %08lX)\n{\n"/*}*/, ep));
       s = str_from_c("defined");
       e1 = expr_constant_new(s, curpos());
       str_free(s);
       e2 = expr_function_new2(e1, ep);
       expr_delete(e1);
       hash_if(e2);
       expr_delete(e2);
       trace((/*{*/"}\n")); }
/*
 * NAME
 *     hash_ifndef - process #ifndef directives
 *
 * SYNOPSIS
 *     void hash_ifndef(expr_ty *);
 *
 * DESCRIPTION
 *     The hash_ifndef function is used to process #ifndef directives.
 *
 * RETURNS
 *     void
 */
static void hash_ifndef _((expr_ty *));
static void
hash_ifndef(ep)
       expr_ty             *ep; {
       expr_ty             *e1;
       expr_ty             *e2;
       expr_ty             *e3;
       string_ty     *s;
       trace(("hash_ifndef(ep = %08lX)\n{\n"/*}*/, ep));
       s = str_from_c("defined");
       e1 = expr_constant_new(s, curpos());
       str_free(s);
       e2 = expr_function_new2(e1, ep);
       expr_delete(e1);
       s = str_from_c("not");
       e1 = expr_constant_new(s, curpos());
       e3 = expr_function_new2(e1, e2);
       expr_delete(e1);
       expr_delete(e2);
       hash_if(e3);
       expr_delete(e3);
       trace((/*{*/"}\n")); }
/*
 * NAME
 *     hash_elif - process #elif directive
 *
 * SYNOPSIS
 *     void hash_elif(expr_ty*);
 *
 * DESCRIPTION
 *     The hash_elif function is used to provess #elif directives.
 *
 * RETURNS
 *     void
 */
static void hash_elif _((expr_ty *));
static void
hash_elif(ep)
       expr_ty             *ep; {
       trace(("hash_elif(ep = %08lX)\n{\n"/*}*/, ep));
       if (!stack)
             hashline_error("#elif without matching #if");
       else {
             switch (stack->state) {
             case 1:
                  stack->pass = 0;
                  stack->state = 1;
                  lex_passing(0);
                  break;
             case 2:
                  switch (expr_eval_condition(ep, 0)) {
                  case -1:
                      hashline_error("expression evaluation failed");
                      /* fall through... */
                  case 0:
                      stack->pass = 0;
                      stack->state = 2;
                      lex_passing(0);
                      break;
                  default:
                      stack->pass = 1;
                      stack->state = 1;
                      lex_passing(1);
                      break; }
                  break;
             case 3:
                  stack->pass = 0;
                  stack->state = 3;
                  hashline_error("#elif after #else");
                  lex_passing(0);
                  break; } }
       trace((/*{*/"}\n")); }
/*
 * NAME
 *     hash_else - process #else directive
 *
 * SYNOPSIS
 *     void hash_else(void);
 *
 * DESCRIPTION
 *     The hash_else function is used to process #else directives.
 *
 * RETURNS
 *     void
 */
static void hash_else _((void));
static void
hash_else() {
       trace(("hash_else()\n{\n"/*}*/));
       if (!stack)
             hashline_error("#else without matching #if");
       else {
             switch (stack->state) {
             case 1:
                  stack->pass = 0;
                  stack->state = 3;
                  lex_passing(0);
                  break;
             case 2:
                  stack->pass = 1;
                  stack->state = 3;
                  lex_passing(1);
                  break;
             case 3:
                  stack->pass = 0;
                  stack->state = 3;
                  hashline_error("#else after #else");
                  lex_passing(0);
                  break; } }
       trace((/*{*/"}\n")); }
/*
 * NAME
 *     hash_endif - process #endif directive
 *
 * SYNOPSIS
 *     void hash_endif(void);
 *
 * DESCRIPTION
 *     The hash_endif function is used to process #endif directives.
 *
 * RETURNS
 *     void
 */
static void hash_endif _((void));
static void
hash_endif() {
       trace(("hash_endif()\n{\n"/*}*/));
       if (!stack)
             hashline_error("#endif without matching #if");
       else {
             cond *c;
             c = stack;
             stack = c->next;
             cond_free(c);
             lex_passing(stack ? stack->pass : 1); }
       trace((/*{*/"}\n")); }
/*
 * NAME
 *     hash_line - process #line directive
 *
 * SYNOPSIS
 *     void hash_line(expr_list_ty *elp);
 *
 * DESCRIPTION
 *     The hash_line function is used to process #line directives.
 *
 * RETURNS
 *     void
 */
static void hash_line _((expr_list_ty *));
static void
hash_line(elp)
       expr_list_ty  *elp; {
       string_list_ty        *result;
       trace(("hash_line(elp = %08lX)\n{\n"/*}*/, elp));
       if (stack && !stack->pass)
             goto ret;
       /*
        * evaluate the expressions
        */
       result = expr_list_evaluate(elp, 0);
       if (!result) {
             hashline_error("expression evaluation failed");
             goto ret; }
       switch (result->nstrings) {
       case 1:
             lex_lino_set(result->string[0], (string_ty *)0);
             break;
       case 2:
             lex_lino_set(result->string[0], result->string[1]);
             break;
       default:
             hashline_error("#line needs positive decimal line number");
             break; }
       string_list_delete(result);
       ret:
       trace((/*{*/"}\n")); }
/*
 * NAME
 *     hash_pragma - process #pragma directive
 *
 * SYNOPSIS
 *     void hash_pragma(expr_list_ty *elp);
 *
 * DESCRIPTION
 *     The hash_pragma function is used to process #pragma directives.
 *
 * RETURNS
 *     void
 */
static void hash_pragma _((expr_list_ty *));
static void
hash_pragma(elp)
       expr_list_ty  *elp; {
       static expr_ty *once;
       trace(("hash_pragma(elp = %08lX)\n{\n"/*}*/, elp));
       if (stack && !stack->pass)
             goto ret;
       /*
        * see if it was "#pragma once"
        */
       if (!once) {
             string_ty    *s;
             s = str_from_c("once");
             once = expr_constant_new(s, curpos());
             str_free(s); }
       if
       (
             elp->el_nexprs == 1
       &&
             expr_equal(elp->el_expr[0], once)
       ) {
             string_list_append_unique(&done_once, lex_cur_file());
             goto ret; }
       /*
        * add more pragma's here
        */
       ret:
       trace((/*{*/"}\n")); }
typedef union {
       expr_ty             *lv_expr;
       expr_list_ty  lv_elist;
       string_ty     *lv_word;
} hashline_STYPE;
#ifndef hashline_LTYPE
typedef
  struct hashline_ltype {
      int timestamp;
      int first_line;
      int first_column;
      int last_line;
      int last_column;
      char *text; }
  hashline_ltype;
#define hashline_LTYPE hashline_ltype
#endif
#ifndef __cplusplus
#ifndef __STDC__
#define const
#endif
#endif
#define        hashline_FINAL         32
#define        hashline_FLAG   -32768
#define        hashline_NTBASE        41
#define hashline_TRANSLATE(x) ((unsigned)(x) <= 295 ? hashline_translate[x] : 44)
static const char hashline_translate[] = {     0,
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
    26,    27,    28,    29,    30,    31,    32,    33,    34,    35,
    36,    37,    38,    39,    40
};
#if hashline_DEBUG != 0
static const short hashline_prhs[] = {     0,
     0,     3,     6,     9,    12,    15,    18,    21,    23,    25,
    28,    31,    33,    35,    39,    43,    45
};
static const short hashline_rhs[] = {    35,
    43,     0,    36,    43,     0,    37,    43,     0,    32,    42,
     0,    33,    42,     0,    34,    42,     0,    29,    42,     0,
    30,     0,    31,     0,    39,    43,     0,    38,    43,     0,
     1,     0,    28,     0,    16,    43,    21,     0,    42,     3,
    42,     0,    42,     0,    43,    42,     0
};
#endif
#if hashline_DEBUG != 0
static const short hashline_rline[] = { 0,
   978,   983,   988,   993,   998,  1003,  1008,  1013,  1017,  1021,
  1026,  1031,  1040,  1045,  1050,  1059,  1065
};
static const char * const hashline_tname[] = {   "$","error","$illegal.","CATENATE",
"COLON","COLON2","DATA","DATAEND","ELSE","EQUALS","FAIL","FILE_BOUNDARY","FUNCTION",
"HOST_BINDING","IF","LBRACE","LBRAK","LOOP","LOOPSTOP","PLUS_EQUALS","RBRACE",
"RBRAK","RETURN","SEMICOLON","SET","SINGLE_THREAD","THEN","UNSETENV","WORD",
"HASH_ELIF","HASH_ELSE","HASH_ENDIF","HASH_IF","HASH_IFDEF","HASH_IFNDEF","HASH_INCLUDE",
"HASH_INCLUDE_COOKED","HASH_INCLUDE_COOKED2","HASH_LINE","HASH_PRAGMA","HASH_JUNK",
"hashline","expr","elist",""
};
#endif
static const short hashline_r1[] = {     0,
    41,    41,    41,    41,    41,    41,    41,    41,    41,    41,
    41,    41,    42,    42,    42,    43,    43
};
static const short hashline_r2[] = {     0,
     2,     2,     2,     2,     2,     2,     2,     1,     1,     2,
     2,     1,     1,     3,     3,     1,     2
};
static const short hashline_defact[] = {     0,
    12,     0,     8,     9,     0,     0,     0,     0,     0,     0,
     0,     0,     0,    13,     7,     4,     5,     6,    16,     1,
     2,     3,    11,    10,     0,     0,    17,    14,    15,     0,
     0,     0
};
static const short hashline_defgoto[] = {    30,
    19,    20
};
static const short hashline_pact[] = {    -1,
-32768,   -15,-32768,-32768,   -15,   -15,   -15,   -15,   -15,   -15,
   -15,   -15,   -15,-32768,     1,     1,     1,     1,     1,   -15,
   -15,   -15,   -15,   -15,   -13,   -15,     1,-32768,-32768,     9,
    10,-32768
};
static const short hashline_pgoto[] = {-32768,
     0,    30
};
#define        hashline_LAST   43
static const short hashline_table[] = {     1,
    13,    15,    13,    26,    16,    17,    18,    28,    31,    32,
     0,     0,    14,     0,    14,     0,     0,     0,     0,    27,
    27,    27,    27,    27,    27,    29,     0,     2,     3,     4,
     5,     6,     7,     8,     9,    10,    11,    12,    21,    22,
    23,    24,    25
};
static const short hashline_check[] = {     1,
    16,     2,    16,     3,     5,     6,     7,    21,     0,     0,
    -1,    -1,    28,    -1,    28,    -1,    -1,    -1,    -1,    20,
    21,    22,    23,    24,    25,    26,    -1,    29,    30,    31,
    32,    33,    34,    35,    36,    37,    38,    39,     9,    10,
    11,    12,    13
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
#define hashline_errok         (hashline_errstatus = 0)
#define hashline_clearin       (hashline_char = hashline_EMPTY)
#define hashline_EMPTY         -2
#define hashline_EOF     0
#define hashline_ACCEPT        return(0)
#define hashline_ABORT         return(1)
#define hashline_ERROR         goto hashline_errlab1
/* Like hashline_ERROR except do call hashline_error.
   This remains here temporarily to ease the
   transition to the new meaning of hashline_ERROR, for GCC.
   Once GCC version 2 has supplanted version 1, this can go.  */
#define hashline_FAIL   goto hashline_errlab
#define hashline_RECOVERING()  (!!hashline_errstatus)
#define hashline_BACKUP(token, value) \
do                                    \
  if (hashline_char == hashline_EMPTY && hashline_len == 1)          \
    { hashline_char = (token), hashline_lval = (value);                     \
      hashline_char1 = hashline_TRANSLATE (hashline_char);              \
      hashline_POPSTACK;                           \
      goto hashline_backup;                         \
    }                                    \
  else                                    \
    { hashline_error ("syntax error: cannot back up"); hashline_ERROR; }       \
while (0)
#define hashline_TERROR        1
#define hashline_ERRCODE       256
#ifndef hashline_PURE
#define hashline_LEX     hashline_lex()
#endif
#ifdef hashline_PURE
#ifdef hashline_LSP_NEEDED
#define hashline_LEX     hashline_lex(&hashline_lval, &hashline_lloc)
#else
#define hashline_LEX     hashline_lex(&hashline_lval)
#endif
#endif
/* If nonreentrant, generate the variables here */
#ifndef hashline_PURE
int    hashline_char;            /*  the lookahead symbol   */
hashline_STYPE hashline_lval;           /*  the semantic value of the   */
                      /*  lookahead symbol                  */
#ifdef hashline_LSP_NEEDED
hashline_LTYPE hashline_lloc;           /*  location data for the lookahead    */
                      /*  symbol                      */
#endif
int hashline_nerrs;         /*  number of parse errors so far       */
#endif  /* not hashline_PURE */
#if hashline_DEBUG != 0
int hashline_debug;         /*  nonzero means print parse trace      */
/* Since this is uninitialized, it does not stop multiple parsers
   from coexisting.  */
#endif
/*  hashline_INITDEPTH indicates the initial size of the parser's stacks       */
#ifndef        hashline_INITDEPTH
#define hashline_INITDEPTH 200
#endif
/*  hashline_MAXDEPTH is the maximum size the stacks can grow to
    (effective only if the built-in stack extension method is used).  */
#if hashline_MAXDEPTH == 0
#undef hashline_MAXDEPTH
#endif
#ifndef hashline_MAXDEPTH
#define hashline_MAXDEPTH 10000
#endif
/* Prevent warning if -Wstrict-prototypes.  */
#ifdef __GNUC__
int hashline_parse (void);
#endif
#if __GNUC__ > 1             /* GNU C and GNU C++ define this.  */
#define __hashline__bcopy(FROM,TO,COUNT)       __builtin_memcpy(TO,FROM,COUNT)
#else                  /* not GNU C or C++ */
#ifndef __cplusplus
/* This is the most reliable way to avoid incompatibilities
   in available built-in functions on various systems.  */
static void
__hashline__bcopy (from, to, count)
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
__hashline__bcopy (char *from, char *to, int count) {
  register char *f = from;
  register char *t = to;
  register int i = count;
  while (i-- > 0)
    *t++ = *f++; }
#endif
#endif
int
hashline_parse() {
  register int hashline_state;
  register int hashline_n;
  register short *hashline_ssp;
  register hashline_STYPE *hashline_vsp;
  int hashline_errstatus;      /*  number of tokens to shift before error messages enabled */
  int hashline_char1;   /*  lookahead token as an internal (translated) token number */
  short        hashline_ssa[hashline_INITDEPTH];      /*  the state stack           */
  hashline_STYPE hashline_vsa[hashline_INITDEPTH];     /*  the semantic value stack               */
  short *hashline_ss = hashline_ssa;     /*  refer to the stacks thru separate pointers */
  hashline_STYPE *hashline_vs = hashline_vsa;  /*  to allow hashline_overflow to reallocate them elsewhere */
#ifdef hashline_LSP_NEEDED
  hashline_LTYPE hashline_lsa[hashline_INITDEPTH];     /*  the location stack               */
  hashline_LTYPE *hashline_ls = hashline_lsa;
  hashline_LTYPE *hashline_lsp;
#define hashline_POPSTACK   (hashline_vsp--, hashline_ssp--, hashline_lsp--)
#else
#define hashline_POPSTACK   (hashline_vsp--, hashline_ssp--)
#endif
  int hashline_stacksize = hashline_INITDEPTH;
#ifdef hashline_PURE
  int hashline_char;
  hashline_STYPE hashline_lval;
  int hashline_nerrs;
#ifdef hashline_LSP_NEEDED
  hashline_LTYPE hashline_lloc;
#endif
#endif
  hashline_STYPE hashline_val;         /*  the variable used to return               */
                      /*  semantic values from the action        */
                      /*  routines                      */
  int hashline_len;
#if hashline_DEBUG != 0
  if (hashline_debug)
    fprintf(stderr, "Starting parse\n");
#endif
  hashline_state = 0;
  hashline_errstatus = 0;
  hashline_nerrs = 0;
  hashline_char = hashline_EMPTY;           /* Cause a token to be read.  */
  /* Initialize stack pointers.
     Waste one element of value and location stack
     so that they stay on the same level as the state stack.
     The wasted elements are never initialized.  */
  hashline_ssp = hashline_ss - 1;
  hashline_vsp = hashline_vs;
#ifdef hashline_LSP_NEEDED
  hashline_lsp = hashline_ls;
#endif
/* Push a new state, which is found in  hashline_state  .  */
/* In all cases, when you get here, the value and location stacks
   have just been pushed. so pushing a state here evens the stacks.  */
hashline_newstate:
  *++hashline_ssp = hashline_state;
  if (hashline_ssp >= hashline_ss + hashline_stacksize - 1) {
      /* Give user a chance to reallocate the stack */
      /* Use copies of these so that the &'s don't force the real ones into memory. */
      hashline_STYPE *hashline_vs1 = hashline_vs;
      short *hashline_ss1 = hashline_ss;
#ifdef hashline_LSP_NEEDED
      hashline_LTYPE *hashline_ls1 = hashline_ls;
#endif
      /* Get the current used size of the three stacks, in elements.  */
      int size = hashline_ssp - hashline_ss + 1;
#ifdef hashline_overflow
      /* Each stack pointer address is followed by the size of
        the data in use in that stack, in bytes.  */
      hashline_overflow("parser stack overflow",
              &hashline_ss1, size * sizeof (*hashline_ssp),
              &hashline_vs1, size * sizeof (*hashline_vsp),
#ifdef hashline_LSP_NEEDED
              &hashline_ls1, size * sizeof (*hashline_lsp),
#endif
              &hashline_stacksize);
      hashline_ss = hashline_ss1; hashline_vs = hashline_vs1;
#ifdef hashline_LSP_NEEDED
      hashline_ls = hashline_ls1;
#endif
#else /* no hashline_overflow */
      /* Extend the stack our own way.  */
      if (hashline_stacksize >= hashline_MAXDEPTH) {
         hashline_error("parser stack overflow");
         return 2; }
      hashline_stacksize *= 2;
      if (hashline_stacksize > hashline_MAXDEPTH)
       hashline_stacksize = hashline_MAXDEPTH;
      hashline_ss = (short *) alloca (hashline_stacksize * sizeof (*hashline_ssp));
      __hashline__bcopy ((char *)hashline_ss1, (char *)hashline_ss, size * sizeof (*hashline_ssp));
      hashline_vs = (hashline_STYPE *) alloca (hashline_stacksize * sizeof (*hashline_vsp));
      __hashline__bcopy ((char *)hashline_vs1, (char *)hashline_vs, size * sizeof (*hashline_vsp));
#ifdef hashline_LSP_NEEDED
      hashline_ls = (hashline_LTYPE *) alloca (hashline_stacksize * sizeof (*hashline_lsp));
      __hashline__bcopy ((char *)hashline_ls1, (char *)hashline_ls, size * sizeof (*hashline_lsp));
#endif
#endif /* no hashline_overflow */
      hashline_ssp = hashline_ss + size - 1;
      hashline_vsp = hashline_vs + size - 1;
#ifdef hashline_LSP_NEEDED
      hashline_lsp = hashline_ls + size - 1;
#endif
#if hashline_DEBUG != 0
      if (hashline_debug)
       fprintf(stderr, "Stack size increased to %d\n", hashline_stacksize);
#endif
      if (hashline_ssp >= hashline_ss + hashline_stacksize - 1)
       hashline_ABORT; }
#if hashline_DEBUG != 0
  if (hashline_debug)
    fprintf(stderr, "Entering state %d\n", hashline_state);
#endif
  goto hashline_backup;
 hashline_backup:
/* Do appropriate processing given the current state.  */
/* Read a lookahead token if we need one and don't already have one.  */
/* hashline_resume: */
  /* First try to decide what to do without reference to lookahead token.  */
  hashline_n = hashline_pact[hashline_state];
  if (hashline_n == hashline_FLAG)
    goto hashline_default;
  /* Not known => get a lookahead token if don't already have one.  */
  /* hashline_char is either hashline_EMPTY or hashline_EOF
     or a valid token in external form.  */
  if (hashline_char == hashline_EMPTY) {
#if hashline_DEBUG != 0
      if (hashline_debug)
       fprintf(stderr, "Reading a token: ");
#endif
      hashline_char = hashline_LEX; }
  /* Convert token to internal form (in hashline_char1) for indexing tables with */
  if (hashline_char <= 0)           /* This means end of input. */ {
      hashline_char1 = 0;
      hashline_char = hashline_EOF;       /* Don't call hashline_LEX any more */
#if hashline_DEBUG != 0
      if (hashline_debug)
       fprintf(stderr, "Now at end of input.\n");
#endif
    }
  else {
      hashline_char1 = hashline_TRANSLATE(hashline_char);
#if hashline_DEBUG != 0
      if (hashline_debug) {
         fprintf (stderr, "Next token is %d (%s", hashline_char, hashline_tname[hashline_char1]);
         /* Give the individual parser a way to print the precise meaning
            of a token, for further debugging info.  */
#ifdef hashline_PRINT
         hashline_PRINT (stderr, hashline_char, hashline_lval);
#endif
         fprintf (stderr, ")\n"); }
#endif
    }
  hashline_n += hashline_char1;
  if (hashline_n < 0 || hashline_n > hashline_LAST || hashline_check[hashline_n] != hashline_char1)
    goto hashline_default;
  hashline_n = hashline_table[hashline_n];
  /* hashline_n is what to do for this token type in this state.
     Negative => reduce, -hashline_n is rule number.
     Positive => shift, hashline_n is new state.
       New state is final state => don't bother to shift,
       just return success.
     0, or most negative number => error.  */
  if (hashline_n < 0) {
      if (hashline_n == hashline_FLAG)
       goto hashline_errlab;
      hashline_n = -hashline_n;
      goto hashline_reduce; }
  else if (hashline_n == 0)
    goto hashline_errlab;
  if (hashline_n == hashline_FINAL)
    hashline_ACCEPT;
  /* Shift the lookahead token.  */
#if hashline_DEBUG != 0
  if (hashline_debug)
    fprintf(stderr, "Shifting token %d (%s), ", hashline_char, hashline_tname[hashline_char1]);
#endif
  /* Discard the token being shifted unless it is eof.  */
  if (hashline_char != hashline_EOF)
    hashline_char = hashline_EMPTY;
  *++hashline_vsp = hashline_lval;
#ifdef hashline_LSP_NEEDED
  *++hashline_lsp = hashline_lloc;
#endif
  /* count tokens shifted since error; after three, turn off error status.  */
  if (hashline_errstatus) hashline_errstatus--;
  hashline_state = hashline_n;
  goto hashline_newstate;
/* Do the default action for the current state.  */
hashline_default:
  hashline_n = hashline_defact[hashline_state];
  if (hashline_n == 0)
    goto hashline_errlab;
/* Do a reduction.  hashline_n is the number of a rule to reduce with.  */
hashline_reduce:
  hashline_len = hashline_r2[hashline_n];
  hashline_val = hashline_vsp[1-hashline_len]; /* implement default value of the action */
#if hashline_DEBUG != 0
  if (hashline_debug) {
      int i;
      fprintf (stderr, "Reducing via rule %d (line %d), ",
              hashline_n, hashline_rline[hashline_n]);
      /* Print the symbols being reduced, and their result.  */
      for (i = hashline_prhs[hashline_n]; hashline_rhs[i] > 0; i++)
       fprintf (stderr, "%s ", hashline_tname[hashline_rhs[i]]);
      fprintf (stderr, " -> %s\n", hashline_tname[hashline_r1[hashline_n]]); }
#endif
  switch (hashline_n) {
case 1: {
                  hash_include(&hashline_vsp[0].lv_elist);
                  expr_list_destructor(&hashline_vsp[0].lv_elist);
             ;
    break;}
case 2: {
                  hash_include_cooked(&hashline_vsp[0].lv_elist, 1);
                  expr_list_destructor(&hashline_vsp[0].lv_elist);
             ;
    break;}
case 3: {
                  hash_include_cooked(&hashline_vsp[0].lv_elist, 0);
                  expr_list_destructor(&hashline_vsp[0].lv_elist);
             ;
    break;}
case 4: {
                  hash_if(hashline_vsp[0].lv_expr);
                  expr_delete(hashline_vsp[0].lv_expr);
             ;
    break;}
case 5: {
                  hash_ifdef(hashline_vsp[0].lv_expr);
                  expr_delete(hashline_vsp[0].lv_expr);
             ;
    break;}
case 6: {
                  hash_ifndef(hashline_vsp[0].lv_expr);
                  expr_delete(hashline_vsp[0].lv_expr);
             ;
    break;}
case 7: {
                  hash_elif(hashline_vsp[0].lv_expr);
                  expr_delete(hashline_vsp[0].lv_expr);
             ;
    break;}
case 8: {
                  hash_else();
             ;
    break;}
case 9: {
                  hash_endif();
             ;
    break;}
case 10: {
                  hash_pragma(&hashline_vsp[0].lv_elist);
                  expr_list_destructor(&hashline_vsp[0].lv_elist);
             ;
    break;}
case 11: {
                  hash_line(&hashline_vsp[0].lv_elist);
                  expr_list_destructor(&hashline_vsp[0].lv_elist);
             ;
    break;}
case 13: {
                  hashline_val.lv_expr = expr_constant_new(hashline_vsp[0].lv_word, curpos());
                  str_free(hashline_vsp[0].lv_word);
             ;
    break;}
case 14: {
                  hashline_val.lv_expr = expr_function_new(&hashline_vsp[-1].lv_elist);
                  expr_list_destructor(&hashline_vsp[-1].lv_elist);
             ;
    break;}
case 15: {
                  hashline_val.lv_expr = expr_catenate_new(hashline_vsp[-2].lv_expr, hashline_vsp[0].lv_expr);
                  expr_delete(hashline_vsp[-2].lv_expr);
                  expr_delete(hashline_vsp[0].lv_expr);
             ;
    break;}
case 16: {
                  expr_list_constructor(&hashline_val.lv_elist);
                  expr_list_append(&hashline_val.lv_elist, hashline_vsp[0].lv_expr);
                  expr_delete(hashline_vsp[0].lv_expr);
             ;
    break;}
case 17: {
                  hashline_val.lv_elist = hashline_vsp[-1].lv_elist;
                  expr_list_append(&hashline_val.lv_elist, hashline_vsp[0].lv_expr);
                  expr_delete(hashline_vsp[0].lv_expr);
             ;
    break;} }
   /* the action file gets copied in in place of this dollarsign */
  hashline_vsp -= hashline_len;
  hashline_ssp -= hashline_len;
#ifdef hashline_LSP_NEEDED
  hashline_lsp -= hashline_len;
#endif
#if hashline_DEBUG != 0
  if (hashline_debug) {
      short *ssp1 = hashline_ss - 1;
      fprintf (stderr, "state stack now");
      while (ssp1 != hashline_ssp)
       fprintf (stderr, " %d", *++ssp1);
      fprintf (stderr, "\n"); }
#endif
  *++hashline_vsp = hashline_val;
#ifdef hashline_LSP_NEEDED
  hashline_lsp++;
  if (hashline_len == 0) {
      hashline_lsp->first_line = hashline_lloc.first_line;
      hashline_lsp->first_column = hashline_lloc.first_column;
      hashline_lsp->last_line = (hashline_lsp-1)->last_line;
      hashline_lsp->last_column = (hashline_lsp-1)->last_column;
      hashline_lsp->text = 0; }
  else {
      hashline_lsp->last_line = (hashline_lsp+hashline_len-1)->last_line;
      hashline_lsp->last_column = (hashline_lsp+hashline_len-1)->last_column; }
#endif
  /* Now "shift" the result of the reduction.
     Determine what state that goes to,
     based on the state we popped back to
     and the rule number reduced by.  */
  hashline_n = hashline_r1[hashline_n];
  hashline_state = hashline_pgoto[hashline_n - hashline_NTBASE] + *hashline_ssp;
  if (hashline_state >= 0 && hashline_state <= hashline_LAST && hashline_check[hashline_state] == *hashline_ssp)
    hashline_state = hashline_table[hashline_state];
  else
    hashline_state = hashline_defgoto[hashline_n - hashline_NTBASE];
  goto hashline_newstate;
hashline_errlab:   /* here on detecting error */
  if (! hashline_errstatus)
    /* If not already recovering from an error, report this error.  */ {
      ++hashline_nerrs;
#ifdef hashline_ERROR_VERBOSE
      hashline_n = hashline_pact[hashline_state];
      if (hashline_n > hashline_FLAG && hashline_n < hashline_LAST) {
         int size = 0;
         char *msg;
         int x, count;
         count = 0;
         /* Start X at -hashline_n if nec to avoid negative indexes in hashline_check.  */
         for (x = (hashline_n < 0 ? -hashline_n : 0);
              x < (sizeof(hashline_tname) / sizeof(char *)); x++)
           if (hashline_check[x + hashline_n] == x)
             size += strlen(hashline_tname[x]) + 15, count++;
         msg = (char *) malloc(size + 15);
         if (msg != 0) {
             strcpy(msg, "parse error");
             if (count < 5) {
               count = 0;
               for (x = (hashline_n < 0 ? -hashline_n : 0);
                    x < (sizeof(hashline_tname) / sizeof(char *)); x++)
                 if (hashline_check[x + hashline_n] == x) {
                  strcat(msg, count == 0 ? ", expecting `" : " or `");
                  strcat(msg, hashline_tname[x]);
                  strcat(msg, "'");
                  count++; } }
             hashline_error(msg);
             free(msg); }
         else
           hashline_error ("parse error; also virtual memory exceeded"); }
      else
#endif /* hashline_ERROR_VERBOSE */
       hashline_error("parse error"); }
  goto hashline_errlab1;
hashline_errlab1:   /* here on error raised explicitly by an action */
  if (hashline_errstatus == 3) {
      /* if just tried and failed to reuse lookahead token after an error, discard it.  */
      /* return failure if at end of input */
      if (hashline_char == hashline_EOF)
       hashline_ABORT;
#if hashline_DEBUG != 0
      if (hashline_debug)
       fprintf(stderr, "Discarding token %d (%s).\n", hashline_char, hashline_tname[hashline_char1]);
#endif
      hashline_char = hashline_EMPTY; }
  /* Else will try to reuse lookahead token
     after shifting the error token.  */
  hashline_errstatus = 3;           /* Each real token shifted decrements this */
  goto hashline_errhandle;
hashline_errdefault:  /* current state does not do anything special for the error token. */
#if 0
  /* This is wrong; only states that explicitly want error tokens
     should shift them.  */
  hashline_n = hashline_defact[hashline_state];  /* If its default is to accept any token, ok.  Otherwise pop it.*/
  if (hashline_n) goto hashline_default;
#endif
hashline_errpop:   /* pop the current state because it cannot handle the error token */
  if (hashline_ssp == hashline_ss) hashline_ABORT;
  hashline_vsp--;
  hashline_state = *--hashline_ssp;
#ifdef hashline_LSP_NEEDED
  hashline_lsp--;
#endif
#if hashline_DEBUG != 0
  if (hashline_debug) {
      short *ssp1 = hashline_ss - 1;
      fprintf (stderr, "Error: state stack now");
      while (ssp1 != hashline_ssp)
       fprintf (stderr, " %d", *++ssp1);
      fprintf (stderr, "\n"); }
#endif
hashline_errhandle:
  hashline_n = hashline_pact[hashline_state];
  if (hashline_n == hashline_FLAG)
    goto hashline_errdefault;
  hashline_n += hashline_TERROR;
  if (hashline_n < 0 || hashline_n > hashline_LAST || hashline_check[hashline_n] != hashline_TERROR)
    goto hashline_errdefault;
  hashline_n = hashline_table[hashline_n];
  if (hashline_n < 0) {
      if (hashline_n == hashline_FLAG)
       goto hashline_errpop;
      hashline_n = -hashline_n;
      goto hashline_reduce; }
  else if (hashline_n == 0)
    goto hashline_errpop;
  if (hashline_n == hashline_FINAL)
    hashline_ACCEPT;
#if hashline_DEBUG != 0
  if (hashline_debug)
    fprintf(stderr, "Shifting error token, ");
#endif
  *++hashline_vsp = hashline_lval;
#ifdef hashline_LSP_NEEDED
  *++hashline_lsp = hashline_lloc;
#endif
  hashline_state = hashline_n;
  goto hashline_newstate; }
