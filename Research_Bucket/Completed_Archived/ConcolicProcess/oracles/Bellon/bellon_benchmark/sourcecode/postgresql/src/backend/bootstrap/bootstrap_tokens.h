#ifndef Y_TAB_H
# define Y_TAB_H
typedef union {
       List   *list;
       IndexElem     *ielem;
       char   *str;
       int      ival;
       Oid      oidval;
} YYSTYPE;
# define       CONST 257
# define       ID    258
# define       OPEN  259
# define       XCLOSE        260
# define       XCREATE       261
# define       INSERT_TUPLE  262
# define       STRING        263
# define       XDEFINE       264
# define       XDECLARE      265
# define       INDEX 266
# define       ON    267
# define       USING 268
# define       XBUILD        269
# define       INDICES       270
# define       UNIQUE        271
# define       COMMA 272
# define       EQUALS        273
# define       LPAREN        274
# define       RPAREN        275
# define       OBJ_ID        276
# define       XBOOTSTRAP    277
# define       XWITHOUT_OIDS 278
# define       NULLVAL       279
# define       low   280
# define       high  281
extern YYSTYPE Int_yylval;
#endif /* not Y_TAB_H */
