typedef union {
       blob_ty             *lv_line;
       blob_list_ty  *lv_list;
       stmt_ty             *lv_stmt;
       int     lv_int;
} gram_STYPE;
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
extern gram_STYPE gram_lval;
