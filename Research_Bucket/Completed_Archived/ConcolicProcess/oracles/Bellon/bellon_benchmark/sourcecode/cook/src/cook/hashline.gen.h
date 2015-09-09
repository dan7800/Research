typedef union {
       expr_ty             *lv_expr;
       expr_list_ty  lv_elist;
       string_ty     *lv_word;
} hashline_STYPE;
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
extern hashline_STYPE hashline_lval;
