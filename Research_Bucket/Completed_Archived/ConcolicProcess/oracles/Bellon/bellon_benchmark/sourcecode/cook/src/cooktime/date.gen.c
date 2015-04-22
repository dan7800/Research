/*  A Bison parser, made from cooktime/date.y with Bison version GNU Bison version 1.22
  */
#define date_BISON 1  /* Identify Bison output.  */
#define        AGO    258
#define        COLON  259
#define        COMMA  260
#define        DAY    261
#define        DAYZONE        262
#define        ID     263
#define        JUNK   264
#define        MERIDIAN       265
#define        MONTH  266
#define        MUNIT  267
#define        NUMBER 268
#define        SLASH  269
#define        SUNIT  270
#define        UNIT   271
#define        ZONE   272
#include <ac/stdio.h>
#include <ac/stdlib.h>
#include <ac/time.h>
#include <ac/ctype.h>
#include <ac/string.h>
#include <date.h>
#include <str.h>
#include <trace.h>
#define date_STYPE int
#define daysec (24L * 60L * 60L)
#define AM 1
#define PM 2
#define DAYLIGHT 1
#define STANDARD 2
#define MAYBE    3
#define MAX_ID_LENGTH 20
static int     timeflag;
static int     zoneflag;
static int     dateflag;
static int     dayflag;
static int     relflag;
static time_t  relsec;
static time_t  relmonth;
static int     hh;
static int     mm;
static int     ss;
static int     merid;
static int     day_light_flag;
static int     dayord;
static int     dayreq;
static int     month;
static int     day;
static int     year;
static int     ourzone;
static char    *lptr;
extern date_STYPE      date_lval;
extern int     date_debug;
static int mdays[12] = {
       31, 0, 31,  30, 31, 30,  31, 31, 30,  31, 30, 31
};
#define epoch 1970
int date_parse _((void)); /* forward */
static int date_lex _((void)); /* forward */
/*
 * NAME
 *     timeconv - convert a time
 *
 * SYNOPSIS
 *     time_t timeconv(int hh, int mm, int ss, int mer);
 *
 * DESCRIPTION
 *     The timeconv function is used to convert a time
 *     specified in hours minutes and seconds, into seconds past midnight.
 *
 * ARGUMENTS
 *     hh  hours, range depends on the meridian
 *     mm  minutes, 0..59
 *     ss  seconds, 0..59
 *     mer meridian to use: AM, PM or 24
 *
 * RETURNS
 *     time_t; seconds past midnight; -1 on any error.
 */
static time_t timeconv _((int, int, int, int));
static time_t
timeconv(ahh, amm, ass, mer)
       int   ahh;
       int   amm;
       int   ass;
       int   mer; {
       time_t        result;
       /*
        * perform sanity checks on input
        */
       trace(("timeconv(ahh = %d, amm = %d, ass = %d, mer = %d)\n{\n"/*}*/,
             ahh, amm, ass, mer));
       result = -1;
       if (amm < 0 || amm > 59 || ass < 0 || ass > 59)
             goto done;
       /*
        * perform range checks depending on the meridian
        */
       switch (mer) {
       case AM:
             if (ahh < 1 || ahh > 12)
                  goto done;
             if (ahh == 12)
                  ahh = 0;
             break;
       case PM:
             if (ahh < 1 || ahh > 12)
                  goto done;
             if (ahh == 12)
                  ahh = 0;
             ahh += 12;
             break;
       case 24:
             if (ahh < 0 || ahh > 23)
                  goto done;
             break;
       default:
             goto done; }
       result = ((ahh * 60L + amm) * 60L + ass);
done:
       trace(("return %ld;\n", (long)result));
       trace((/*{*/"}\n"));
       return result; }
/*
 * NAME
 *     dateconv - convert a date
 *
 * SYNOPSIS
 *     time_t dateconv(int mm, int dd, int year, int h, int m, int s,
 *         int mer, int zone, int dayflag);
 *
 * DESCRIPTION
 *     The dateconv function may be used to convert a date after the
 *     date string has been taken apart by date_parse.
 *
 * ARGUMENTS
 *     mm  month number, in the range 1..12
 *     year        year number,  in several ranges:
 *         0..37 means 2000..2037
 *         70..99 means 1970..1999
 *         1970..2037 mean themselves.
 *     dd  day of month, in the range 1..max, where max varies for
 *         each month, as per the catchy jingle (except February,
 *         which is a monster).
 *     h   hours since midnight or meridian
 *     m   minutes past hour
 *     s   seconds past minute
 *     mer meridian, AM or PM.
 *     zone        minutes correction for the time zone.
 *     dayflag     whether to use daylight savings: STANDARD, DAYLIGHT or MAYBE.
 *
 * RETURNS
 *     time_t; the time in seconds past Jan 1 0:00:00 1970 GMT, this will
 *     always be positive or zero; -1 is returned for any error.
 *
 * CAVEAT
 *     The date functions only work between 1970 and 2037,
 *     because 0 is Jan 1 00:00:00 1970 GMT
 *     and (2^31-1) is Jan 19 03:14:07 2038 GMT
 *     hence some if the weir magic number below.
 *
 *     Because -1 is used to represent errors, times before noon Jan 1 1970
 *     in places east of GMT can't always be represented.
 */
static time_t dateconv _((int, int, int, int, int, int, int, int, int));
static time_t
dateconv(amm, dd, ayear, h, m, s, mer, zone, adayflag)
       int   amm;
       int   dd;
       int   ayear;
       int   h;
       int   m;
       int   s;
       int   mer;
       int   zone;
       int   adayflag; {
       time_t        result;
       time_t        tod;
       time_t        jdate;
       int   i;
       /*
        * make corrections for the year
        *
        * If it is 0..99, RFC822 says pick closest century.
        */
       trace(("dateconv(amm = %d, dd = %d, ayear = %d, h = %d, m = %d, \
s = %d, mer = %d, zone = %d, adayflag = %d)\n{\n"/*}*/,
             amm, dd, ayear, h, m, s, mer, zone, adayflag));
       result = -1;
       if (ayear < 0)
             ayear = -ayear;
       if (ayear < 38)
             ayear += 2000;
       else if (ayear < 100)
             ayear += 1900;
       /*
        * correct February length once we know the year
        */
       mdays[1] = 28 +
             (ayear % 4 == 0 && (ayear % 100 != 0 || ayear % 400 == 0));
       /*
        * perform some sanity checks on the input
        */
       if
       (
             ayear < epoch
       ||
             ayear >= 2038
       ||
             amm < 1
       ||
             amm > 12
       ||
             dd < 1
       ||
             dd > mdays[--amm]
       )
             goto done;
       /*
        * Determine the julian day number of the dd-mm-date_ given.
        * Turn it into seconds, and add in the time zone correction.
        */
       jdate = dd - 1;
        for (i = 0; i < amm; i++)
             jdate += mdays[i];
       for (i = epoch; i < ayear; i++)
             jdate += 365 + (i % 4 == 0);
       jdate *= daysec;
       jdate += zone * 60L;
       /*
        * Determine the time of day.
        * that is, seconds from midnight.
        * Add it into the julian date.
        */
       tod = timeconv(h, m, s, mer);
       if (tod < 0)
             goto done;
       jdate += tod;
       /*
        * Perform daylight savings correction if necessary.
        * (This assumes 1 hour daylite savings, which is probably wrong.)
        */
       if
       (
             adayflag == DAYLIGHT
       ||
             (adayflag == MAYBE && localtime(&jdate)->tm_isdst)
       )
             jdate += -1 * 60 * 60;
       /*
        * there you have it.
        */
       result = jdate;
done:
       trace(("return %ld;\n", (long)result));
       trace((/*{*/"}\n"));
       return result; }
/*
 * NAME
 *     daylcorr
 *
 * SYNOPSIS
 *     time_t daylcorr(time_t future, time_t now);
 *
 * DESCRIPTION
 *     The daylcorr function is used to determine the difference in seconds
 *     between two times, taking daylight savings into account.
 *
 * ARGUMENTS
 *     future      - a later time
 *     now - an earlier time
 *
 * RETURNS
 *     time_t; the difference in seconds
 *
 * CAVEAT
 *     Assumes daylight savings is alays an integral number of hours.
 *     This is wrong is Saudi Arabia (time zone changes during the day),
 *     and South Australia (half hour DLS).
 */
static time_t daylcorr _((time_t, time_t));
static time_t
daylcorr(future, now)
       time_t        future;
       time_t        now; {
       int   fdayl;
       int   nowdayl;
       time_t        result;
       trace(("daylcorr(future = %ld, now = %ld)\n{\n"/*}*/,
             (long)future, (long)now));
       nowdayl = (localtime(&now)->tm_hour + 1) % 24;
       fdayl = (localtime(&future)->tm_hour + 1) % 24;
       result = ((future - now) + 60L * 60L * (nowdayl - fdayl));
       trace(("return %ld;\n", (long)result));
       trace((/*{*/"}\n"));
       return result; }
/*
 * NAME
 *     dayconv
 *
 * SYNOPSIS
 *     time_t dayconv(int ord, int day, time_t now);
 *
 * DESCRIPTION
 *     The dayconv function is used to convert a day-of-the-week into
 *     a meaningful time.
 *
 * ARGUMENTS
 *     ord - the ord'th day from now
 *     day - which day of the week
 *     now - relative to this
 *
 * RETURNS
 *     time_t; time in seconds from epoch
 */
static time_t dayconv _((int, int, time_t));
static time_t
dayconv(ord, aday, now)
       int   ord;
       int   aday;
       time_t        now; {
       time_t        tod;
       time_t        result;
       trace(("dayconv(ord = %d, aday = %d, now = %ld)\n{\n"/*}*/,
             ord, aday, (long)now));
       tod = now;
       tod += daysec * ((aday - localtime(&tod)->tm_wday + 7) % 7);
       tod += 7 * daysec * (ord <= 0 ? ord : ord - 1);
       result = daylcorr(tod, now);
       trace(("return %ld;\n", (long)result));
       trace((/*{*/"}\n"));
       return result; }
/*
 * NAME
 *     monthadd
 *
 * SYNOPSIS
 *     time_t monthadd(time_t sdate, time_t relmonth);
 *
 * DESCRIPTION
 *     The monthadd function is used to add a given number of
 *     months to a specified time.
 *
 * ARGUMENTS
 *     sdate       - add the months to this
 *     relmonth - add this many months
 *
 * RETURNS
 *     time_t; seconds since the epoch
 */
static time_t monthadd _((time_t, time_t));
static time_t
monthadd(sdate, arelmonth)
       time_t        sdate;
       time_t        arelmonth; {
       struct tm *ltime;
       int   amm;
       int   ayear;
       time_t        result;
       trace(("monthadd(sdate = %ld, arelmonth = %ld)\n{\n"/*}*/,
             (long)sdate, (long)arelmonth));
       if (arelmonth == 0)
             result = 0;
       else {
             ltime = localtime(&sdate);
             amm = 12 * (ltime->tm_year + 1900) + ltime->tm_mon + arelmonth;
             ayear = amm / 12;
             amm = amm % 12 + 1;
             result =
                  dateconv
                  (
                      amm,
                      ltime->tm_mday,
                      ayear,
                      ltime->tm_hour,
                      ltime->tm_min,
                      ltime->tm_sec,
                      24,
                      ourzone,
                      MAYBE
                  );
             if (result >= 0)
                  result = daylcorr(result, sdate); }
       trace(("return %ld;\n", (long)result));
       trace((/*{*/"}\n"));
       return result; }
/*
 * NAME
 *     date_scan
 *
 * SYNOPSIS
 *     time_t date_scan(char *s);
 *
 * DESCRIPTION
 *     The date_scan function is used to scan a string and
 *     return a number of seconds since epoch.
 *
 * ARGUMENTS
 *     s   - string to scan
 *
 * RETURNS
 *     time_t; seconds to epoch, -1 on error.
 *
 * CAVEAT
 *     it isn't psychic
 */
time_t
date_scan(p)
       char   *p; {
       time_t               now;
       struct tm     *lt;
       time_t               result;
       time_t               tod;
       /*
        * find time zone info, if not given
        */
       trace(("date_scan(p = \"%s\")\n{\n"/*}*/, p));
       lptr = p;
       /*
        * initialize things
        */
       time(&now);
        lt = localtime(&now);
       year = lt->tm_year + 1900;
       month = lt->tm_mon + 1;
       day = lt->tm_mday;
       relsec = 0;
       relmonth = 0;
       timeflag = 0;
       zoneflag = 0;
       dateflag = 0;
       dayflag = 0;
       relflag = 0;
       ourzone = 0;
       day_light_flag = MAYBE;
       hh = 0;
       mm = 0;
       ss = 0;
       merid = 24;
       /*
        * parse the string
        */
#ifdef DEBUG
       date_debug = trace_pretest_;
#endif
       trace(("date_parse()\n{\n"/*}*/));
       result = date_parse();
       trace((/*{*/"}\n"));
       if (result) {
             result = -1;
             goto done; }
       /*
        * sanity checks
        */
       result = -1;
       if (timeflag > 1 || zoneflag > 1 || dateflag > 1 || dayflag > 1)
             goto done;
       if (dateflag || timeflag || dayflag) {
             result =
                  dateconv
                  (
                      month,
                      day,
                      year,
                      hh,
                      mm,
                      ss,
                      merid,
                      ourzone,
                      day_light_flag
                  );
             if (result < 0)
                  goto done; }
       else {
             result = now;
             if (!relflag) {
                  result -=
                      (
                         (lt->tm_hour * 60L + lt->tm_min * 60)
                      +
                         lt->tm_sec
                      ); } }
       result += relsec;
       relsec = monthadd(result, relmonth);
       if (relsec < 0) {
             result = -1;
             goto done; }
       result += relsec;
       if (dayflag && !dateflag) {
             tod = dayconv(dayord, dayreq, result);
             result += tod; }
       /*
        * here for all exits
        */
done:
       trace(("return %ld;\n", (long)result));
       trace((/*{*/"}\n"));
       return result; }
/*
 * NAME
 *     date_string - build one
 *
 * SYNOPSIS
 *     char *date_string(time_t when);
 *
 * DESCRIPTION
 *     The date_string function may be used to construct a
 *     string from a given time in seconds.
 *
 *     The string will conform to the RFC822 standard,
 *     which states a definite preference for GMT dates.
 *
 * ARGUMENTS
 *     when        the time to be rendered.
 *
 * RETURNS
 *     Pointer to string containing rendered time.
 *     The contents of this string will remain undisturbed
 *     only until the next call to date_string.
 */
char *
date_string(when)
       time_t               when; {
       struct tm     *tm;
       static char   buffer[32];
       static char   *weekday_name[] = {
             "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat",
       };
       static char   *month_name[] = {
             "Jan", "Feb", "Mar", "Apr", "May", "Jun",
             "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
       };
       /*
        * break the given time down into components
        *    (RFC1036 likes GMT, remember)
        */
       trace(("date_string(when = %ld)\n{\n"/*}*/, (long)when));
       tm = gmtime(&when);
       /*
        * build the date string
        */
       sprintf
       (
             buffer,
             "%s,%3d %s %4.4d %2.2d:%2.2d:%2.2d GMT",
             weekday_name[tm->tm_wday],
             tm->tm_mday,
             month_name[tm->tm_mon],
             tm->tm_year + 1900,
             tm->tm_hour,
             tm->tm_min,
             tm->tm_sec
       );
       trace(("return \"%s\";\n", buffer));
       trace((/*{*/"}\n"));
       return buffer; }
/*
 * NAME
 *     date_error
 *
 * SYNOPSIS
 *     void date_error(char *);
 *
 * DESCRIPTION
 *     The date_error function is invoked by yacc to report
 *     errors, but we just throw it away.
 *
 * ARGUMENTS
 *     s   - error to report
 */
static void date_error _((char *));
static void
date_error(s)
       char  *s; {
       trace(("date_error(s = \"%s\")\n{\n"/*}*/, s));
       trace((/*{*/"}\n")); }
/*
 * NAME
 *     date_trace - follow parser actions
 *
 * SYNOPSIS
 *     void date_trace(char *, ...);
 *
 * DESCRIPTION
 *     The date_trace function is used to print the various shifts
 *     and reductions, etc, done by the yacc-generated parser.
 *     lines are accumulated and printed whole,
 *     so as to avoid confusing the trace output.
 *
 * ARGUMENTS
 *     as for printf
 *
 * CAVEAT
 *     only available when DEBUG is defined
 */
#ifdef DEBUG
#define date_DEBUG 1
#define printf date_trace
static void date_trace _((char *, ...));
static void
date_trace(s sva_last)
       char   *s;
       sva_last_decl {
       va_list             ap;
       static char   line[1024];
       string_ty     *buffer;
       char   *cp;
       sva_init(ap, s);
       buffer = str_vformat(s, ap);
       va_end(ap);
       strcat(line, buffer->str_text);
       str_free(buffer);
       cp = line + strlen(line) - 1;
       if (cp > line && *cp == '\n') {
             *cp = 0;
             trace_printf("%s\n", line);
             line[0] = 0; } }
#endif /* DEBUG */
#ifndef date_LTYPE
typedef
  struct date_ltype {
      int timestamp;
      int first_line;
      int first_column;
      int last_line;
      int last_column;
      char *text; }
  date_ltype;
#define date_LTYPE date_ltype
#endif
#ifndef date_STYPE
#define date_STYPE int
#endif
#ifndef __cplusplus
#ifndef __STDC__
#define const
#endif
#endif
#define        date_FINAL         43
#define        date_FLAG           -32768
#define        date_NTBASE    18
#define date_TRANSLATE(x) ((unsigned)(x) <= 272 ? date_translate[x] : 26)
static const char date_translate[] = {     0,
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
    16,    17
};
#if date_DEBUG != 0
static const short date_prhs[] = {     0,
     0,     1,     4,     6,     8,    10,    12,    14,    16,    18,
    20,    23,    27,    32,    37,    43,    50,    57,    59,    61,
    63,    66,    69,    73,    79,    82,    87,    90,    94,    97,
   100,   103,   105,   107,   109
};
static const short date_rhs[] = {    -1,
    18,    19,     0,     1,     0,    21,     0,    22,     0,    24,
     0,    23,     0,    25,     0,    20,     0,    13,     0,    13,
    10,     0,    13,     4,    13,     0,    13,     4,    13,    10,
     0,    13,     4,    13,    13,     0,    13,     4,    13,     4,
    13,     0,    13,     4,    13,     4,    13,    10,     0,    13,
     4,    13,     4,    13,    13,     0,    17,     0,     7,     0,
     6,     0,     6,     5,     0,    13,     6,     0,    13,    14,
    13,     0,    13,    14,    13,    14,    13,     0,    11,    13,
     0,    11,    13,     5,    13,     0,    13,    11,     0,    13,
    11,    13,     0,    13,    16,     0,    13,    12,     0,    13,
    15,     0,    16,     0,    12,     0,    15,     0,    25,     3,
     0
};
#endif
#if date_DEBUG != 0
static const short date_rline[] = { 0,
   754,   755,   756,   768,   770,   772,   774,   776,   778,   782,
   798,   805,   811,   817,   826,   833,   840,   853,   858,   866,
   871,   876,   884,   889,   895,   900,   906,   911,   920,   922,
   924,   926,   928,   930,   932
};
static const char * const date_tname[] = {   "$","error","$illegal.","AGO","COLON",
"COMMA","DAY","DAYZONE","ID","JUNK","MERIDIAN","MONTH","MUNIT","NUMBER","SLASH",
"SUNIT","UNIT","ZONE","timedate","item","NumberSpecification","TimeSpecification",
"TimeZone","DayOfWeekSpecification","DateSpecification","RelativeSpecification",
""
};
#endif
static const short date_r1[] = {     0,
    18,    18,    18,    19,    19,    19,    19,    19,    19,    20,
    21,    21,    21,    21,    21,    21,    21,    22,    22,    23,
    23,    23,    24,    24,    24,    24,    24,    24,    25,    25,
    25,    25,    25,    25,    25
};
static const short date_r2[] = {     0,
     0,     2,     1,     1,     1,     1,     1,     1,     1,     1,
     2,     3,     4,     4,     5,     6,     6,     1,     1,     1,
     2,     2,     3,     5,     2,     4,     2,     3,     2,     2,
     2,     1,     1,     1,     2
};
static const short date_defact[] = {     0,
     3,     0,    20,    19,     0,    33,    10,    34,    32,    18,
     2,     9,     4,     5,     7,     6,     8,    21,    25,     0,
    22,    11,    27,    30,     0,    31,    29,    35,     0,    12,
    28,    23,    26,     0,    13,    14,     0,    15,    24,    16,
    17,     0,     0
};
static const short date_defgoto[] = {     2,
    11,    12,    13,    14,    15,    16,    17
};
static const short date_pact[] = {     0,
-32768,    14,    -2,-32768,    -9,-32768,    28,-32768,-32768,-32768,
-32768,-32768,-32768,-32768,-32768,-32768,     5,-32768,     4,    -3,
-32768,-32768,     6,-32768,     9,-32768,-32768,-32768,    10,    37,
-32768,    19,-32768,    11,-32768,-32768,    15,    -8,-32768,-32768,
-32768,    18,-32768
};
static const short date_pgoto[] = {-32768,
-32768,-32768,-32768,-32768,-32768,-32768,-32768
};
#define        date_LAST           50
static const short date_table[] = {    -1,
     1,    40,    18,    19,    41,    -1,    -1,    28,    29,    30,
    -1,    -1,    -1,    42,    -1,    -1,    -1,    43,    31,     3,
     4,    32,    33,    38,     5,     6,     7,    39,     8,     9,
    10,    20,    37,    21,     0,     0,     0,    22,    23,    24,
    34,    25,    26,    27,     0,     0,    35,     0,     0,    36
};
static const short date_check[] = {     0,
     1,    10,     5,    13,    13,     6,     7,     3,     5,    13,
    11,    12,    13,     0,    15,    16,    17,     0,    13,     6,
     7,    13,    13,    13,    11,    12,    13,    13,    15,    16,
    17,     4,    14,     6,    -1,    -1,    -1,    10,    11,    12,
     4,    14,    15,    16,    -1,    -1,    10,    -1,    -1,    13
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
#define date_errok         (date_errstatus = 0)
#define date_clearin   (date_char = date_EMPTY)
#define date_EMPTY         -2
#define date_EOF             0
#define date_ACCEPT    return(0)
#define date_ABORT     return(1)
#define date_ERROR         goto date_errlab1
/* Like date_ERROR except do call date_error.
   This remains here temporarily to ease the
   transition to the new meaning of date_ERROR, for GCC.
   Once GCC version 2 has supplanted version 1, this can go.  */
#define date_FAIL           goto date_errlab
#define date_RECOVERING()  (!!date_errstatus)
#define date_BACKUP(token, value) \
do                                    \
  if (date_char == date_EMPTY && date_len == 1)                          \
    { date_char = (token), date_lval = (value);                     \
      date_char1 = date_TRANSLATE (date_char);                      \
      date_POPSTACK;                           \
      goto date_backup;                                 \
    }                                    \
  else                                    \
    { date_error ("syntax error: cannot back up"); date_ERROR; }       \
while (0)
#define date_TERROR    1
#define date_ERRCODE   256
#ifndef date_PURE
#define date_LEX             date_lex()
#endif
#ifdef date_PURE
#ifdef date_LSP_NEEDED
#define date_LEX             date_lex(&date_lval, &date_lloc)
#else
#define date_LEX             date_lex(&date_lval)
#endif
#endif
/* If nonreentrant, generate the variables here */
#ifndef date_PURE
int    date_char;                /*  the lookahead symbol           */
date_STYPE     date_lval;           /*  the semantic value of the   */
                      /*  lookahead symbol                  */
#ifdef date_LSP_NEEDED
date_LTYPE date_lloc;           /*  location data for the lookahead    */
                      /*  symbol                      */
#endif
int date_nerrs;                     /*  number of parse errors so far       */
#endif  /* not date_PURE */
#if date_DEBUG != 0
int date_debug;                     /*  nonzero means print parse trace  */
/* Since this is uninitialized, it does not stop multiple parsers
   from coexisting.  */
#endif
/*  date_INITDEPTH indicates the initial size of the parser's stacks   */
#ifndef        date_INITDEPTH
#define date_INITDEPTH 200
#endif
/*  date_MAXDEPTH is the maximum size the stacks can grow to
    (effective only if the built-in stack extension method is used).  */
#if date_MAXDEPTH == 0
#undef date_MAXDEPTH
#endif
#ifndef date_MAXDEPTH
#define date_MAXDEPTH 10000
#endif
/* Prevent warning if -Wstrict-prototypes.  */
#ifdef __GNUC__
int date_parse (void);
#endif
#if __GNUC__ > 1             /* GNU C and GNU C++ define this.  */
#define __date__bcopy(FROM,TO,COUNT)   __builtin_memcpy(TO,FROM,COUNT)
#else                  /* not GNU C or C++ */
#ifndef __cplusplus
/* This is the most reliable way to avoid incompatibilities
   in available built-in functions on various systems.  */
static void
__date__bcopy (from, to, count)
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
__date__bcopy (char *from, char *to, int count) {
  register char *f = from;
  register char *t = to;
  register int i = count;
  while (i-- > 0)
    *t++ = *f++; }
#endif
#endif
int
date_parse() {
  register int date_state;
  register int date_n;
  register short *date_ssp;
  register date_STYPE *date_vsp;
  int date_errstatus;  /*  number of tokens to shift before error messages enabled */
  int date_char1;           /*  lookahead token as an internal (translated) token number */
  short        date_ssa[date_INITDEPTH];      /*  the state stack           */
  date_STYPE date_vsa[date_INITDEPTH]; /*  the semantic value stack       */
  short *date_ss = date_ssa;     /*  refer to the stacks thru separate pointers */
  date_STYPE *date_vs = date_vsa;      /*  to allow date_overflow to reallocate them elsewhere */
#ifdef date_LSP_NEEDED
  date_LTYPE date_lsa[date_INITDEPTH]; /*  the location stack           */
  date_LTYPE *date_ls = date_lsa;
  date_LTYPE *date_lsp;
#define date_POPSTACK   (date_vsp--, date_ssp--, date_lsp--)
#else
#define date_POPSTACK   (date_vsp--, date_ssp--)
#endif
  int date_stacksize = date_INITDEPTH;
#ifdef date_PURE
  int date_char;
  date_STYPE date_lval;
  int date_nerrs;
#ifdef date_LSP_NEEDED
  date_LTYPE date_lloc;
#endif
#endif
  date_STYPE date_val;         /*  the variable used to return               */
                      /*  semantic values from the action        */
                      /*  routines                      */
  int date_len;
#if date_DEBUG != 0
  if (date_debug)
    fprintf(stderr, "Starting parse\n");
#endif
  date_state = 0;
  date_errstatus = 0;
  date_nerrs = 0;
  date_char = date_EMPTY;           /* Cause a token to be read.  */
  /* Initialize stack pointers.
     Waste one element of value and location stack
     so that they stay on the same level as the state stack.
     The wasted elements are never initialized.  */
  date_ssp = date_ss - 1;
  date_vsp = date_vs;
#ifdef date_LSP_NEEDED
  date_lsp = date_ls;
#endif
/* Push a new state, which is found in  date_state  .  */
/* In all cases, when you get here, the value and location stacks
   have just been pushed. so pushing a state here evens the stacks.  */
date_newstate:
  *++date_ssp = date_state;
  if (date_ssp >= date_ss + date_stacksize - 1) {
      /* Give user a chance to reallocate the stack */
      /* Use copies of these so that the &'s don't force the real ones into memory. */
      date_STYPE *date_vs1 = date_vs;
      short *date_ss1 = date_ss;
#ifdef date_LSP_NEEDED
      date_LTYPE *date_ls1 = date_ls;
#endif
      /* Get the current used size of the three stacks, in elements.  */
      int size = date_ssp - date_ss + 1;
#ifdef date_overflow
      /* Each stack pointer address is followed by the size of
        the data in use in that stack, in bytes.  */
      date_overflow("parser stack overflow",
              &date_ss1, size * sizeof (*date_ssp),
              &date_vs1, size * sizeof (*date_vsp),
#ifdef date_LSP_NEEDED
              &date_ls1, size * sizeof (*date_lsp),
#endif
              &date_stacksize);
      date_ss = date_ss1; date_vs = date_vs1;
#ifdef date_LSP_NEEDED
      date_ls = date_ls1;
#endif
#else /* no date_overflow */
      /* Extend the stack our own way.  */
      if (date_stacksize >= date_MAXDEPTH) {
         date_error("parser stack overflow");
         return 2; }
      date_stacksize *= 2;
      if (date_stacksize > date_MAXDEPTH)
       date_stacksize = date_MAXDEPTH;
      date_ss = (short *) alloca (date_stacksize * sizeof (*date_ssp));
      __date__bcopy ((char *)date_ss1, (char *)date_ss, size * sizeof (*date_ssp));
      date_vs = (date_STYPE *) alloca (date_stacksize * sizeof (*date_vsp));
      __date__bcopy ((char *)date_vs1, (char *)date_vs, size * sizeof (*date_vsp));
#ifdef date_LSP_NEEDED
      date_ls = (date_LTYPE *) alloca (date_stacksize * sizeof (*date_lsp));
      __date__bcopy ((char *)date_ls1, (char *)date_ls, size * sizeof (*date_lsp));
#endif
#endif /* no date_overflow */
      date_ssp = date_ss + size - 1;
      date_vsp = date_vs + size - 1;
#ifdef date_LSP_NEEDED
      date_lsp = date_ls + size - 1;
#endif
#if date_DEBUG != 0
      if (date_debug)
       fprintf(stderr, "Stack size increased to %d\n", date_stacksize);
#endif
      if (date_ssp >= date_ss + date_stacksize - 1)
       date_ABORT; }
#if date_DEBUG != 0
  if (date_debug)
    fprintf(stderr, "Entering state %d\n", date_state);
#endif
  goto date_backup;
 date_backup:
/* Do appropriate processing given the current state.  */
/* Read a lookahead token if we need one and don't already have one.  */
/* date_resume: */
  /* First try to decide what to do without reference to lookahead token.  */
  date_n = date_pact[date_state];
  if (date_n == date_FLAG)
    goto date_default;
  /* Not known => get a lookahead token if don't already have one.  */
  /* date_char is either date_EMPTY or date_EOF
     or a valid token in external form.  */
  if (date_char == date_EMPTY) {
#if date_DEBUG != 0
      if (date_debug)
       fprintf(stderr, "Reading a token: ");
#endif
      date_char = date_LEX; }
  /* Convert token to internal form (in date_char1) for indexing tables with */
  if (date_char <= 0)   /* This means end of input. */ {
      date_char1 = 0;
      date_char = date_EOF;       /* Don't call date_LEX any more */
#if date_DEBUG != 0
      if (date_debug)
       fprintf(stderr, "Now at end of input.\n");
#endif
    }
  else {
      date_char1 = date_TRANSLATE(date_char);
#if date_DEBUG != 0
      if (date_debug) {
         fprintf (stderr, "Next token is %d (%s", date_char, date_tname[date_char1]);
         /* Give the individual parser a way to print the precise meaning
            of a token, for further debugging info.  */
#ifdef date_PRINT
         date_PRINT (stderr, date_char, date_lval);
#endif
         fprintf (stderr, ")\n"); }
#endif
    }
  date_n += date_char1;
  if (date_n < 0 || date_n > date_LAST || date_check[date_n] != date_char1)
    goto date_default;
  date_n = date_table[date_n];
  /* date_n is what to do for this token type in this state.
     Negative => reduce, -date_n is rule number.
     Positive => shift, date_n is new state.
       New state is final state => don't bother to shift,
       just return success.
     0, or most negative number => error.  */
  if (date_n < 0) {
      if (date_n == date_FLAG)
       goto date_errlab;
      date_n = -date_n;
      goto date_reduce; }
  else if (date_n == 0)
    goto date_errlab;
  if (date_n == date_FINAL)
    date_ACCEPT;
  /* Shift the lookahead token.  */
#if date_DEBUG != 0
  if (date_debug)
    fprintf(stderr, "Shifting token %d (%s), ", date_char, date_tname[date_char1]);
#endif
  /* Discard the token being shifted unless it is eof.  */
  if (date_char != date_EOF)
    date_char = date_EMPTY;
  *++date_vsp = date_lval;
#ifdef date_LSP_NEEDED
  *++date_lsp = date_lloc;
#endif
  /* count tokens shifted since error; after three, turn off error status.  */
  if (date_errstatus) date_errstatus--;
  date_state = date_n;
  goto date_newstate;
/* Do the default action for the current state.  */
date_default:
  date_n = date_defact[date_state];
  if (date_n == 0)
    goto date_errlab;
/* Do a reduction.  date_n is the number of a rule to reduce with.  */
date_reduce:
  date_len = date_r2[date_n];
  date_val = date_vsp[1-date_len]; /* implement default value of the action */
#if date_DEBUG != 0
  if (date_debug) {
      int i;
      fprintf (stderr, "Reducing via rule %d (line %d), ",
              date_n, date_rline[date_n]);
      /* Print the symbols being reduced, and their result.  */
      for (i = date_prhs[date_n]; date_rhs[i] > 0; i++)
       fprintf (stderr, "%s ", date_tname[date_rhs[i]]);
      fprintf (stderr, " -> %s\n", date_tname[date_r1[date_n]]); }
#endif
  switch (date_n) {
case 3: {
                  /*
                   * Mostly, this production is unnecessary,
                   * however it silences warnings about unused
                   * labels, etc.
                   */
                  return -1;
             ;
    break;}
case 4:
{ timeflag++; ;
    break;}
case 5:
{ zoneflag++; ;
    break;}
case 6:
{ dateflag++; ;
    break;}
case 7:
{ dayflag++; ;
    break;}
case 8:
{ relflag++; ;
    break;}
case 10: {
                  if (timeflag && dateflag && !relflag)
                      year = date_vsp[0];
                  else {
                      timeflag++;
                      hh = date_vsp[0] / 100;
                      mm = date_vsp[0] % 100;
                      ss = 0;
                      merid = 24; }
             ;
    break;}
case 11: {
                  hh = date_vsp[-1];
                  mm = 0;
                  ss = 0;
                  merid = date_vsp[0];
             ;
    break;}
case 12: {
                  hh = date_vsp[-2];
                  mm = date_vsp[0];
                  merid = 24;
             ;
    break;}
case 13: {
                  hh = date_vsp[-3];
                  mm = date_vsp[-1];
                  merid = date_vsp[0];
             ;
    break;}
case 14: {
                  hh = date_vsp[-3];
                  mm = date_vsp[-1];
                  merid = 24;
                  day_light_flag = STANDARD;
                  date_vsp[0] = -date_vsp[0];
                  ourzone = date_vsp[0] % 100 + 60 * date_vsp[0] / 100;
             ;
    break;}
case 15: {
                  hh = date_vsp[-4];
                  mm = date_vsp[-2];
                  ss = date_vsp[0];
                  merid = 24;
             ;
    break;}
case 16: {
                  hh = date_vsp[-5];
                  mm = date_vsp[-3];
                  ss = date_vsp[-1];
                  merid = date_vsp[0];
             ;
    break;}
case 17: {
                  hh = date_vsp[-5];
                  mm = date_vsp[-3];
                  ss = date_vsp[-1];
                  merid = 24;
                  day_light_flag = STANDARD;
                  date_vsp[0] = -date_vsp[0];
                  ourzone = date_vsp[0] % 100 + 60 * date_vsp[0] / 100;
             ;
    break;}
case 18: {
                  ourzone = date_vsp[0];
                  day_light_flag = STANDARD;
             ;
    break;}
case 19: {
                  ourzone = date_vsp[0];
                  day_light_flag = DAYLIGHT;
             ;
    break;}
case 20: {
                  dayord = 1;
                  dayreq = date_vsp[0];
             ;
    break;}
case 21: {
                  dayord = 1;
                  dayreq = date_vsp[-1];
             ;
    break;}
case 22: {
                  dayord = date_vsp[-1];
                  dayreq = date_vsp[0];
             ;
    break;}
case 23: {
                  month = date_vsp[-2];
                  day = date_vsp[0];
             ;
    break;}
case 24: {
                  month = date_vsp[-4];
                  day = date_vsp[-2];
                  year = date_vsp[0];
             ;
    break;}
case 25: {
                  month = date_vsp[-1];
                  day = date_vsp[0];
             ;
    break;}
case 26: {
                  month = date_vsp[-3];
                  day = date_vsp[-2];
                  year = date_vsp[0];
             ;
    break;}
case 27: {
                  month = date_vsp[0];
                  day = date_vsp[-1];
             ;
    break;}
case 28: {
                  month = date_vsp[-1];
                  day = date_vsp[-2];
                  year = date_vsp[0];
             ;
    break;}
case 29:
{ relsec +=  60L * date_vsp[-1] * date_vsp[0]; ;
    break;}
case 30:
{ relmonth += date_vsp[-1] * date_vsp[0]; ;
    break;}
case 31:
{ relsec += date_vsp[-1]; ;
    break;}
case 32:
{ relsec +=  60L * date_vsp[0]; ;
    break;}
case 33:
{ relmonth += date_vsp[0]; ;
    break;}
case 34:
{ relsec++; ;
    break;}
case 35: {
                  relsec = -relsec;
                  relmonth = -relmonth;
             ;
    break;} }
   /* the action file gets copied in in place of this dollarsign */
  date_vsp -= date_len;
  date_ssp -= date_len;
#ifdef date_LSP_NEEDED
  date_lsp -= date_len;
#endif
#if date_DEBUG != 0
  if (date_debug) {
      short *ssp1 = date_ss - 1;
      fprintf (stderr, "state stack now");
      while (ssp1 != date_ssp)
       fprintf (stderr, " %d", *++ssp1);
      fprintf (stderr, "\n"); }
#endif
  *++date_vsp = date_val;
#ifdef date_LSP_NEEDED
  date_lsp++;
  if (date_len == 0) {
      date_lsp->first_line = date_lloc.first_line;
      date_lsp->first_column = date_lloc.first_column;
      date_lsp->last_line = (date_lsp-1)->last_line;
      date_lsp->last_column = (date_lsp-1)->last_column;
      date_lsp->text = 0; }
  else {
      date_lsp->last_line = (date_lsp+date_len-1)->last_line;
      date_lsp->last_column = (date_lsp+date_len-1)->last_column; }
#endif
  /* Now "shift" the result of the reduction.
     Determine what state that goes to,
     based on the state we popped back to
     and the rule number reduced by.  */
  date_n = date_r1[date_n];
  date_state = date_pgoto[date_n - date_NTBASE] + *date_ssp;
  if (date_state >= 0 && date_state <= date_LAST && date_check[date_state] == *date_ssp)
    date_state = date_table[date_state];
  else
    date_state = date_defgoto[date_n - date_NTBASE];
  goto date_newstate;
date_errlab:   /* here on detecting error */
  if (! date_errstatus)
    /* If not already recovering from an error, report this error.  */ {
      ++date_nerrs;
#ifdef date_ERROR_VERBOSE
      date_n = date_pact[date_state];
      if (date_n > date_FLAG && date_n < date_LAST) {
         int size = 0;
         char *msg;
         int x, count;
         count = 0;
         /* Start X at -date_n if nec to avoid negative indexes in date_check.  */
         for (x = (date_n < 0 ? -date_n : 0);
              x < (sizeof(date_tname) / sizeof(char *)); x++)
           if (date_check[x + date_n] == x)
             size += strlen(date_tname[x]) + 15, count++;
         msg = (char *) malloc(size + 15);
         if (msg != 0) {
             strcpy(msg, "parse error");
             if (count < 5) {
               count = 0;
               for (x = (date_n < 0 ? -date_n : 0);
                    x < (sizeof(date_tname) / sizeof(char *)); x++)
                 if (date_check[x + date_n] == x) {
                  strcat(msg, count == 0 ? ", expecting `" : " or `");
                  strcat(msg, date_tname[x]);
                  strcat(msg, "'");
                  count++; } }
             date_error(msg);
             free(msg); }
         else
           date_error ("parse error; also virtual memory exceeded"); }
      else
#endif /* date_ERROR_VERBOSE */
       date_error("parse error"); }
  goto date_errlab1;
date_errlab1:   /* here on error raised explicitly by an action */
  if (date_errstatus == 3) {
      /* if just tried and failed to reuse lookahead token after an error, discard it.  */
      /* return failure if at end of input */
      if (date_char == date_EOF)
       date_ABORT;
#if date_DEBUG != 0
      if (date_debug)
       fprintf(stderr, "Discarding token %d (%s).\n", date_char, date_tname[date_char1]);
#endif
      date_char = date_EMPTY; }
  /* Else will try to reuse lookahead token
     after shifting the error token.  */
  date_errstatus = 3;   /* Each real token shifted decrements this */
  goto date_errhandle;
date_errdefault:  /* current state does not do anything special for the error token. */
#if 0
  /* This is wrong; only states that explicitly want error tokens
     should shift them.  */
  date_n = date_defact[date_state];  /* If its default is to accept any token, ok.  Otherwise pop it.*/
  if (date_n) goto date_default;
#endif
date_errpop:   /* pop the current state because it cannot handle the error token */
  if (date_ssp == date_ss) date_ABORT;
  date_vsp--;
  date_state = *--date_ssp;
#ifdef date_LSP_NEEDED
  date_lsp--;
#endif
#if date_DEBUG != 0
  if (date_debug) {
      short *ssp1 = date_ss - 1;
      fprintf (stderr, "Error: state stack now");
      while (ssp1 != date_ssp)
       fprintf (stderr, " %d", *++ssp1);
      fprintf (stderr, "\n"); }
#endif
date_errhandle:
  date_n = date_pact[date_state];
  if (date_n == date_FLAG)
    goto date_errdefault;
  date_n += date_TERROR;
  if (date_n < 0 || date_n > date_LAST || date_check[date_n] != date_TERROR)
    goto date_errdefault;
  date_n = date_table[date_n];
  if (date_n < 0) {
      if (date_n == date_FLAG)
       goto date_errpop;
      date_n = -date_n;
      goto date_reduce; }
  else if (date_n == 0)
    goto date_errpop;
  if (date_n == date_FINAL)
    date_ACCEPT;
#if date_DEBUG != 0
  if (date_debug)
    fprintf(stderr, "Shifting error token, ");
#endif
  *++date_vsp = date_lval;
#ifdef date_LSP_NEEDED
  *++date_lsp = date_lloc;
#endif
  date_state = date_n;
  goto date_newstate; }
/* The following need to be here to make sure Berkeley Yacc doesn't puke. */
/*
 * NAME
 *     table - list of known names
 *
 * SYNOPSIS
 *     table_t table[];
 *
 * DESCRIPTION
 *     The table is used to hold the list of known names.
 *     This includes time zone names and days of the week, etc.
 *
 * CAVEAT
 *     It is in English.
 *     It is impossible to have a full list of time zones.
 */
typedef struct table_t table_t;
struct table_t {
       char  *name;
       int   type;
       int   value;
};
#define HRMIN(a, b) ((a) * 60 + (b))
static table_t table[] = {
       { "a",               ZONE,         HRMIN(1, 0),   },
       { "a.c.s.s.t.",       DAYZONE,      -HRMIN(9, 30),       },
       { "a.c.s.t.", ZONE,     -HRMIN(9, 30),   },
       { "a.d.t.",   DAYZONE,  HRMIN(4, 0),     },
       { "a.e.s.s.t.",       DAYZONE,      -HRMIN(10, 0),       },
       { "a.e.s.t.", ZONE,     -HRMIN(10, 0),   },
       { "a.m.",     MERIDIAN,   AM,             },
       { "a.s.t.",   ZONE,         HRMIN(4, 0),       },
    { "a.w.s.t.", ZONE, -HRMIN(8, 0), }, /* (no daylight time there, I'm told */
       { "acsst",    DAYZONE, -HRMIN(9, 30), }, /* Australian Central Summer */
       { "acst",     ZONE,    -HRMIN(9, 30), }, /* Australian Central Time */
       { "adt",      DAYZONE,     HRMIN(4, 0),        },
      { "aesst", DAYZONE, -HRMIN(10, 0), }, /* Australian Eastern Summer Time */
       { "aest",     ZONE,    -HRMIN(10, 0), }, /* Australian Eastern Time */
       { "ago",      AGO,         1,         },
       { "am",             MERIDIAN,    AM,               },
       { "apr",      MONTH,             4,     },
       { "apr.",     MONTH,           4,   },
       { "april",    MONTH,         4,         },
       { "ast",      ZONE,               HRMIN(4, 0),  },  /* Atlantic */
       { "aug",      MONTH,             8,     },
       { "aug.",     MONTH,           8,   },
       { "august",   MONTH,       8,               },
       { "awst",     ZONE,     -HRMIN(8, 0), }, /* Australian Western Time */
       { "b",               ZONE,         HRMIN(2, 0),   },
       { "b.s.t.",   DAYZONE,  HRMIN(0, 0),     },
       { "bst",      DAYZONE,       HRMIN(0, 0), }, /* British Summer Time */
       { "c",               ZONE,         HRMIN(3, 0),   },
       { "c.d.t.",   DAYZONE,  HRMIN(6, 0),     },
       { "c.s.t.",   ZONE,         HRMIN(6, 0),       },
       { "cdt",      DAYZONE,     HRMIN(6, 0),        },
       { "cst",      ZONE,               HRMIN(6, 0),  }, /* Central */
       { "d",               ZONE,         HRMIN(4, 0),   },
       { "day",      UNIT,               1 * 24 * 60,  },
       { "days",     UNIT,             1 * 24 * 60, },
       { "dec",      MONTH,             12,   },
       { "dec.",     MONTH,           12,         },
       { "december", MONTH,   12,         },
       { "e",               ZONE,         HRMIN(5, 0),   },
       { "e.d.t.",   DAYZONE,  HRMIN(5, 0),     },
       { "e.e.s.t.", DAYZONE,        HRMIN(0, 0),   },
       { "e.e.t.",   ZONE,         HRMIN(0, 0),       },
       { "e.s.t.",   ZONE,         HRMIN(5, 0),       },
       { "edt",      DAYZONE,     HRMIN(5, 0),        },
       { "eest",    DAYZONE, HRMIN(0, 0), }, /* European Eastern Summer Time */
       { "eet",      ZONE,        HRMIN(0, 0), }, /* European Eastern Time */
       { "eigth",    NUMBER,       8,               },
       { "eleventh", NUMBER,         11,       },
       { "est",      ZONE,               HRMIN(5, 0),  }, /* Eastern */
       { "f",               ZONE,         HRMIN(6, 0),   },
       { "feb",      MONTH,             2,     },
       { "feb.",     MONTH,           2,   },
       { "february", MONTH,   2,           },
       { "fifth",    NUMBER,       5,               },
       { "first",    NUMBER,       1,               },
       { "fortnight",        UNIT,   14 * 24 * 60,   },
       { "fortnights",       UNIT,         14 * 24 * 60,  },
       { "fourth",   NUMBER,     4,             },
       { "fri",      DAY,         5,         },
       { "fri.",     DAY,               5,       },
       { "friday",   DAY,           5,   },
       { "g",               ZONE,         HRMIN(7, 0),   },
       { "g.m.t.",   ZONE,         HRMIN(0, 0),       },
       { "gmt",      ZONE,               HRMIN(0, 0),  },
       { "h",               ZONE,         HRMIN(8, 0),   },
       { "h.d.t.",   DAYZONE,  HRMIN(10, 0),    },
       { "h.s.t.",   ZONE,         HRMIN(10, 0),      },
       { "hdt",      DAYZONE,     HRMIN(10, 0),       },
       { "hour",     UNIT,             60,   },
       { "hours",    UNIT,           60,         },
       { "hr",             UNIT,               60,     },
       { "hrs",      UNIT,               60,     },
       { "hst",      ZONE,               HRMIN(10, 0), }, /* Hawaii */
       { "i",               ZONE,         HRMIN(9, 0),   },
       { "j.s.t.",   ZONE,         -HRMIN(9, 0), }, /* Japan Standard Time */
       { "jan",      MONTH,             1,     },
       { "jan.",     MONTH,           1,   },
       { "january",  MONTH,     1,             },
       { "jst",      ZONE,         -HRMIN(9, 0), }, /* Japan Standard Time */
       { "jul",      MONTH,             7,     },
       { "jul.",     MONTH,           7,   },
       { "july",     MONTH,           7,   },
       { "jun",      MONTH,             6,     },
       { "jun.",     MONTH,           6,   },
       { "june",     MONTH,           6,   },
       { "k",               ZONE,         HRMIN(10, 0),  },
       { "l",               ZONE,         HRMIN(11, 0),  },
       { "last",     NUMBER,         -1,               },
       { "m",               ZONE,         HRMIN(12, 0),  },
       { "m.d.t.",   DAYZONE,  HRMIN(7, 0),     },
       { "m.e.s.t.", DAYZONE,        -HRMIN(1, 0),  },
       { "m.e.t.",   ZONE,         -HRMIN(1, 0),      },
       { "m.s.t.",   ZONE,         HRMIN(7, 0),       },
       { "mar",      MONTH,             3,     },
       { "mar.",     MONTH,           3,   },
       { "march",    MONTH,         3,         },
       { "may",      MONTH,             5,     },
       { "mdt",      DAYZONE,     HRMIN(7, 0),        },
       { "mest",    DAYZONE, -HRMIN(1, 0), }, /* Middle European Summer Time */
       { "met",      ZONE,        -HRMIN(1, 0), }, /* Middle European Time */
       { "min",      UNIT,               1,       },
       { "mins",     UNIT,             1,     },
       { "minute",   UNIT,         1,         },
       { "minutes",  UNIT,       1,               },
       { "mon",      DAY,         1,         },
       { "mon.",     DAY,               1,       },
       { "monday",   DAY,           1,   },
       { "month",    MUNIT,         1,         },
       { "months",   MUNIT,       1,               },
       { "mst",      ZONE,               HRMIN(7, 0),  }, /* Mountain */
       { "n",               ZONE,         -HRMIN(1, 0),  },
       { "n.s.t.",   ZONE,         HRMIN(3, 30),      },
       { "next",     NUMBER,         2,         },
       { "ninth",    NUMBER,       9,               },
       { "nov",      MONTH,             11,   },
       { "nov.",     MONTH,           11,         },
       { "november", MONTH,   11,         },
       { "now",      UNIT,               0,       },
       { "nst",      ZONE,               HRMIN(3, 30), }, /* Newfoundland */
       { "o",               ZONE,         -HRMIN(2, 0),  },
       { "oct",      MONTH,             10,   },
       { "oct.",     MONTH,           10,         },
       { "october",  MONTH,     10,           },
       { "p",               ZONE,         -HRMIN(3, 0),  },
       { "p.d.t.",   DAYZONE,  HRMIN(8, 0),     },
       { "p.m.",     MERIDIAN,   PM,             },
       { "p.s.t.",   ZONE,         HRMIN(8, 0),       },
       { "pdt",      DAYZONE,     HRMIN(8, 0),        },
       { "pm",             MERIDIAN,    PM,               },
       { "pst",      ZONE,               HRMIN(8, 0),  }, /* Pacific */
       { "q",               ZONE,         -HRMIN(4, 0),  },
       { "r",               ZONE,         -HRMIN(5, 0),  },
       { "s",               ZONE,         -HRMIN(6, 0),  },
       { "sat",      DAY,         6,         },
       { "sat.",     DAY,               6,       },
       { "saturday", DAY,       6,               },
       { "sec",      SUNIT,             1,     },
       { "second",   SUNIT,       1,               },
       { "seconds",  SUNIT,     1,             },
       { "secs",     SUNIT,           1,   },
       { "sep",      MONTH,             9,     },
       { "sep.",     MONTH,           9,   },
       { "sept",     MONTH,           9,   },
       { "sept.",    MONTH,         9,         },
       { "september",        MONTH,         9,         },
       { "seventh",  NUMBER,   7,           },
       { "sixth",    NUMBER,       6,               },
       { "sun",      DAY,         0,         },
       { "sun.",     DAY,               0,       },
       { "sunday",   DAY,           0,   },
       { "t",               ZONE,         -HRMIN(7, 0),  },
       { "tenth",    NUMBER,       10,             },
       { "third",    NUMBER,       3,               },
       { "this",     UNIT,             0,     },
       { "thu",      DAY,         4,         },
       { "thu.",     DAY,               4,       },
       { "thur",     DAY,               4,       },
       { "thur.",    DAY,             4,     },
       { "thurs",    DAY,             4,     },
       { "thurs.",   DAY,           4,   },
       { "thursday", DAY,       4,               },
       { "today",    UNIT,           0,   },
       { "tomorrow", UNIT,     1 * 24 * 60,     },
       { "tue",      DAY,         2,         },
       { "tue.",     DAY,               2,       },
       { "tues",     DAY,               2,       },
       { "tues.",    DAY,             2,     },
       { "tuesday",  DAY,         2,         },
       { "twelfth",  NUMBER,   12,         },
       { "u",               ZONE,         -HRMIN(8, 0),  },
       { "u.t.",     ZONE,             HRMIN(0, 0), },
       { "ut",             ZONE,               HRMIN(0, 0),  },
       { "v",               ZONE,         -HRMIN(9, 0),  },
       { "w",               ZONE,         -HRMIN(10, 0), },
       { "w.e.s.t.", DAYZONE,        -HRMIN(2, 0),  },
       { "w.e.t.",   ZONE,         -HRMIN(2, 0),      },
       { "wed",      DAY,         3,         },
       { "wed.",     DAY,               3,       },
       { "wednes",   DAY,           3,   },
       { "wednes.",  DAY,         3,         },
       { "wednesday",        DAY,     3,             },
       { "week",     UNIT,             7 * 24 * 60, },
       { "weeks",    UNIT,           7 * 24 * 60,        },
       { "west",   DAYZONE, -HRMIN(2, 0), }, /* Western European Summer Time */
       { "wet",      ZONE,            -HRMIN(2, 0), }, /* Western European Time */
       { "x",               ZONE,         -HRMIN(11, 0), },
       { "y",               ZONE,         -HRMIN(12, 0), },
       { "y.d.t.",   DAYZONE,  HRMIN(9, 0),     },
       { "y.s.t.",   ZONE,         HRMIN(9, 0),       },
       { "ydt",      DAYZONE,     HRMIN(9, 0),        },
       { "year",     MUNIT,           12,         },
       { "years",    MUNIT,         12,               },
       { "yesterday",        UNIT,   -1*24*60,       },
       { "yst",      ZONE,               HRMIN(9, 0),  }, /* Yukon */
       { "z",               ZONE,         HRMIN(0, 0),   },
};
/*
 * NAME
 *     lookup - find name
 *
 * SYNOPSIS
 *     int lookup(char *id);
 *
 * DESCRIPTION
 *     The lookup function is used to find a token corresponding to
 *     a given name.
 *
 * ARGUMENTS
 *     id  - name to search for.  Assumes already downcased.
 *
 * RETURNS
 *     int; yacc token, ID if not found.
 */
static int lookup _((char *));
static int
lookup(id)
       char  *id; {
       table_t       *tp;
       int   min;
       int   max;
       int   mid;
       int   cmp;
       int   result;
       /*
        * binary chop the table
        */
       trace(("lookup(id = \"%s\")\n{\n"/*}*/, id));
       result = ID;
       min = 0;
       max = SIZEOF(table) - 1;
       while (min <= max) {
             mid = (min + max) / 2;
             tp = table + mid;
             cmp = strcmp(id, tp->name);
             if (!cmp) {
                  date_lval = tp->value;
                  result = tp->type;
                  break; }
             if (cmp < 0)
                  max = mid - 1;
             else
                  min = mid + 1; }
       trace(("return %d;\n", result));
       trace((/*{*/"}\n"));
       return result; }
/*
 * NAME
 *     date_lex - lexical analyser
 *
 * SYNOPSIS
 *     int date_lex(void);
 *
 * DESCRIPTION
 *     The date_lex function is used to scan the input string
 *     and break it into discrete tokens.
 *
 * RETURNS
 *     int; the yacc token, 0 means the-end.
 */
static int
date_lex() {
       int   sign;
       int   c;
       char  *p;
       char  idbuf[MAX_ID_LENGTH];
       int   pcnt;
       int   token;
       trace(("date_lex()\n{\n"/*}*/));
       date_lval = 0;
       for (;;) {
             /*
              * get the next input character
              */
             c = *lptr++;
             /*
              * action depends on the character
              */
             switch (c) {
             case 0:
                  token = 0;
                  lptr--;
                  break;
             case ' ':
             case '\t':
                  /*
                   * ignore white space
                   */
                  continue;
             case ':':
                  token = COLON;
                  break;
             case ',':
                  token = COMMA;
                  break;
             case '/':
                  token = SLASH;
                  break;
             case '-':
                  if (!isdigit(*lptr)) {
                      /*
                       * ignore lonely '-'s
                       */
                      continue; }
                  sign = -1;
                  c = *lptr++;
                  goto number;
             case '+':
                  if (!isdigit(*lptr)) {
                      token = c;
                      break; }
                  sign = 1;
                  c = *lptr++;
                  goto number;
             case '0': case '1': case '2': case '3': case '4':
             case '5': case '6': case '7': case '8': case '9':
                  /*
                   * numbers
                   */
                  sign = 1;
                  number:
                  for (;;) {
                      date_lval = date_lval * 10 + c - '0';
                      c = *lptr++;
                      switch (c) {
                      case '0': case '1': case '2': case '3':
                      case '4': case '5': case '6': case '7':
                      case '8': case '9':
                         continue; }
                      break; }
                  date_lval *= sign;
                  lptr--;
                  token = NUMBER;
                  break;
             case 'a': case 'b': case 'c': case 'd': case 'e':
             case 'f': case 'g': case 'h': case 'i': case 'j':
             case 'k': case 'l': case 'm': case 'n': case 'o':
             case 'p': case 'q': case 'r': case 's': case 't':
             case 'u': case 'v': case 'w': case 'x': case 'y': case 'z':
             case 'A': case 'B': case 'C': case 'D': case 'E':
             case 'F': case 'G': case 'H': case 'I': case 'J':
             case 'K': case 'L': case 'M': case 'N': case 'O':
             case 'P': case 'Q': case 'R': case 'S': case 'T':
             case 'U': case 'V': case 'W': case 'X': case 'Y': case 'Z':
                  /*
                   * name
                   */
                  p = idbuf;
                  for (;;) {
                      if (isupper(c))
                         c = tolower(c);
                      if (p < idbuf + sizeof(idbuf) - 1)
                         *p++ = c;
                      c = *lptr++;
                      switch (c) {
                      case 'a': case 'b': case 'c': case 'd':
                      case 'e': case 'f': case 'g': case 'h':
                      case 'i': case 'j': case 'k': case 'l':
                      case 'm': case 'n': case 'o': case 'p':
                      case 'q': case 'r': case 's': case 't':
                      case 'u': case 'v': case 'w': case 'x':
                      case 'y': case 'z':
                      case 'A': case 'B': case 'C': case 'D':
                      case 'E': case 'F': case 'G': case 'H':
                      case 'I': case 'J': case 'K': case 'L':
                      case 'M': case 'N': case 'O': case 'P':
                      case 'Q': case 'R': case 'S': case 'T':
                      case 'U': case 'V': case 'W': case 'X':
                      case 'Y': case 'Z':
                      case '.':
                         continue; }
                      break; }
                  *p = 0;
                  lptr--;
                  token = lookup(idbuf);
                  break;
             case '('/*)*/:
                  /*
                   * comment
                   */
                  for (pcnt = 1; pcnt > 0; ) {
                      c = *lptr++;
                      switch (c) {
                      case 0:
                         --lptr;
                         pcnt = 0;
                         break;
                      case '('/*)*/:
                         pcnt++;
                         break;
                      case /*(*/')':
                         pcnt--;
                         break; } }
                  continue;
             default:
                  /*
                   * unrecognosed
                   */
                  token = JUNK;
                  break; }
             break; }
       trace(("date_lval = %d;\n", date_lval));
       trace(("return %d;\n", token));
       trace((/*{*/"}\n"));
       return token; }
