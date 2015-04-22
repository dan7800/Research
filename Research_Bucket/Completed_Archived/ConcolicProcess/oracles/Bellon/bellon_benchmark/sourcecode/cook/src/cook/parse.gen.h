typedef union {
       expr_ty             *lv_expr;
       expr_list_ty  lv_elist;
       stmt_ty             *lv_stmt;
       stmt_list_ty  lv_slist;
       string_ty     *lv_word;
       int     lv_number;
       expr_position_ty lv_position;
} parse_STYPE;
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
extern parse_STYPE parse_lval;
