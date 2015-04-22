typedef union { struct {        /* bison doesn't know struct as a token type */
  St_ptr_type stp;       /* symbol table pointer */
                         /* 2 backpatch target lists: */
  bp_list *brk;          /* #1 contains positions of break statements */
  bp_list *cont;         /* #2 contains positions of continue statements */
  Ic_ptr_type tmp;       /* instruction # temp buffer for local backpatching */
  arglist_type *arglist; /* pointer to an argument list */
} t; } YYSTYPE;
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
extern YYSTYPE yyzlval;
