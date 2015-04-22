typedef union {
       string_ty     *lv_string;
       long   lv_number;
       struct {
             long lhs;
             long rhs; }
                  lv_number_pair;
       struct {
             string_ty *lhs;
             string_ty *rhs; }
                  lv_string_pair;
} fingerprint_gram_STYPE;
#define        STRING 258
#define        JUNK   259
#define        NUMBER 260
#define        EQ     261
#define        LB     262
#define        RB     263
extern fingerprint_gram_STYPE fingerprint_gram_lval;
