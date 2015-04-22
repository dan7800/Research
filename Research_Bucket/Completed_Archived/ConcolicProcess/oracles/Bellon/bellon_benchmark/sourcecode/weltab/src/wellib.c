#include <stdio.h>
#include "weltab.x"
#include "filedecl.x"
#include "candnm.x"
#include "cardrd.x"
#include "dcand.x"
#include "longrp.x"
#include "office.x"
#include "offnam.x"
#include "precin.x"
#include "precvt.x"
#include "repout.x"
#include "unitinfo.x"
/** cprint **/
void cprint(fileid)
/* print report buffer and blank it */
FILE   *fileid; {
       int trimlen;
       if (trace) fprintf(stderr,"> cprint\n");
       report[80] = '\0';
       trimlen = itrim(80,report);
       /* printf("trimlen = %d\n",trimlen); */
       report[trimlen] = '\0';
       fprintf(fileid,"%s\n",report);
       blkbuf(80,report);
       return; }
/* ==================== */
/** welcom **/
void welcom(date,time)
/* print welcome message introducing weltab, including copyright notice */
char   date[],time[]; {
       sclear();
       if (trace) fprintf(stderr,"> welcom\n");
       blkbuf(80,report);
       smove(report,0,"Weltab III (11/86)",0,18);
       setdate(date,time);
       smove(report,30,date,0,12);
       smove(report,43,time,0,8);
       cprint(stderr);
       fprintf(stderr,"(C) 1982,1986 Radius Systems Inc. All Rights Reserved.\n\n");
       return; }
/* ==================== */
/** cread **/
void cread(fileid,eof)
/* read a card into card buffer and echo it */
FILE   *fileid;
LOGICAL        *eof; {
       int c,n;
       int trimlen;
       if (trace) fprintf(stderr,"> cread\n");
       blkbuf(80,card);
       card[80] = '\0';
       *eof = FALSE;
       n = 0;
       c = getc(fileid);
       if (c == EOF || c < 0) {
             *eof = TRUE;
             fprintf(stderr,"\n *** end of file ***\n");
             return;
             };
       while (c != '\n' && n < 80 && c!= EOF && c >= 0) {
             card[n] = c;
             n++;
             c = getc(fileid);
             };
       trimlen = itrim(80,card);
       /* printf("trimlen = %d\n",trimlen); */
       card[trimlen] = '\0';
       fprintf(stderr,"%s\n",card);
       return; }
/* ==================== */
/** cnread **/
void cnread(fileid,eof)
/* read a card into card buffer without echo it */
FILE   *fileid;
LOGICAL        *eof; {
       int c,n;
       int trimlen;
       if (trace) fprintf(stderr,"> cnread\n");
       blkbuf(80,card);
       card[80] = '\0';
       *eof = FALSE;
       n = 0;
       c = getc(fileid);
       if (c == EOF || c < 0) {
             *eof = TRUE;
             return;
             };
       while (c != '\n' && n < 80 && c!= EOF && c >= 0) {
             card[n] = c;
             n++;
             c = getc(fileid);
             };
       trimlen = itrim(80,card);
       /* printf("trimlen = %d\n",trimlen); */
       card[trimlen] = '\0';
       return; }
/* ==================== */
/** wtcand **/
void wtcand(number,name,start,party,pstart)
/* write candidate name and party to offices/candidates tables */
int    number;
char   name[];
int    start;
char   party[];
int    pstart; {
       if (trace) fprintf(stderr,"> wtcand\n");
       if (number > MAXCAND || number < 1) failure(1202);
       smove(candnm,0,name,start,25);
       smove(partnm,0,party,pstart,3);
       offset = (long) ((number - 1) * 28);
       fseek(flcand,offset,0);
       fprintf(flcand,"%25.25s%3.3s",candnm,partnm);
       /* printf("%25.25s%3.3s, number = %d, offset = %ld\n", */
       /*    candnm,partnm,number,offset); */
       return; }
/* ==================== */
/** wtoffi **/
void wtoffi(number,name,start)
/* write office name to offices/candidates tables */
int    number;
char   name[];
int    start; {
       if (trace) fprintf(stderr,"> wtoffi\n");
       if (number > MAXOFFICE || number < 1) failure(1204);
       smove(offnam,0,name,start,50);
       offset = (long) ((number - 1) * 50);
       fseek(floffnam,offset,0);
       fprintf(floffnam,"%50.50s",offnam);
       return; }
/* ==================== */
/** wtdual **/
void wtdual(number,name,start)
/* write second office name to offices/candidates tables */
int    number;
char   name[];
int    start; {
       if (trace) fprintf(stderr,"> wtdual\n");
       if (number > MAXDUAL || number < 1) failure(1208);
       smove(dualnm,0,name,start,50);
       offset = (long) ((number - 1) * 50);
       fseek(fldualoff,offset,0);
       fprintf(fldualoff,"%50.50s",dualnm);
       return; }
/* ==================== */
/** wtdcnd **/
void wtdcnd(number,name,start)
/* write second candidate name to offices/candidates tables */
int    number;
char   name[];
int    start; {
       if (trace) fprintf(stderr,"> wtdcnd\n");
       if (number > MAXDCAND || number < 1) failure(1216);
       smove(dcname,0,name,start,25);
       offset = (long) ((number - 1) * 25);
       fseek(fldualcand,offset,0);
       fprintf(fldualcand,"%25.25s",dcname);
       return; }
/* ==================== */
/** gtcand **/
void gtcand(number,name,start,party,pstart)
/* get candidate name and party */
int    number;
char   name[];
int    start;
char   party[];
int    pstart; {
       if (trace) fprintf(stderr,"> gtcand\n");
       if (number > ncand || number < 1) failure(1224);
       offset = (long) ((number - 1) * 28);
       fseek(flcand,offset,0);
       fscanf(flcand,"%25c%3c",candnm,partnm);
       smove(name,start,candnm,0,25);
       smove(party,pstart,partnm,0,3);
       return; }
/* ==================== */
/** gtoffi **/
void gtoffi(number,name,start)
/* get office name */
int    number;
char   name[];
int    start; {
       if (trace) fprintf(stderr,"> gtoffi\n");
       if (number > noffic || number < 1) failure(1232);
       offset = (long) ((number - 1) * 50);
       fseek(floffnam,offset,0);
       fscanf(floffnam,"%50c",offnam);
       smove(name,start,offnam,0,50);
       return; }
/* ==================== */
/** gtdual **/
void gtdual(number,name,start)
/* get second office name */
int    number;
char   name[];
int    start; {
       if (trace) fprintf(stderr,"> gtdual\n");
       if (number > ndual || number < 1) failure(1240);
       offset = (long) ((number - 1) * 50);
       fseek(fldualoff,offset,0);
       fscanf(fldualoff,"%50c",dualnm);
       smove(name,start,dualnm,0,50);
       return; }
/* ==================== */
/** gtdcnd **/
void gtdcnd(number,name,start)
/* get second candidate name */
int    number;
char   name[];
int    start; {
       if (trace) fprintf(stderr,"> gtdcnd\n");
       if (number > ducand || number < 1) failure(1248);
       offset = (long) ((number - 1) * 25);
       fseek(fldualcand,offset,0);
       fscanf(fldualcand,"%25c",dcname);
       smove(name,start,dcname,0,25);
       return; }
/* ==================== */
/** lprint **/
void lprint(fileid)
/* print long report buffer and blank it */
FILE   *fileid; {
       int trimlen;
       if (trace) fprintf(stderr,"> lprint\n");
       longrep[130] = '\0';
       trimlen = itrim(130,longrep);
       /* printf("trimlen = %d\n",trimlen); */
       longrep[trimlen] = '\0';
       fprintf(fileid,"%s\n",longrep);
       blkbuf(130,longrep);
       return; }
/* ==================== */
/** lhprint **/
void lhprint(fileid,buf)
/* print a long report heading buffer */
FILE   *fileid;
char   buf[]; {
       int trimlen;
       if (trace) fprintf(stderr,"> lhprint\n");
       buf[130] = '\0';
       trimlen = itrim(130,buf);
       /* printf("trimlen = %d\n",trimlen); */
       buf[trimlen] = '\0';
       fprintf(fileid,"%s\n",buf);
       return; }
/* ==================== */
/** tellprec **/
void tellprec(iward,iprec,isplt,lavcb)
int    iward;
int    iprec;
int    isplt;
LOGICAL        lavcb; {
       fprintf(stderr,
             "Precinct is: %20.20s ",unitnm);
       if (iward > 0)
             fprintf(stderr,"Ward %d, ",iward);
       if (lavcb) {
             fprintf(stderr,"AVCB %d",iprec); }
       else {
             fprintf(stderr,"Prec %d",iprec);
             };
       if (isplt != 0)
             fprintf(stderr,", Split %d",isplt);
       fprintf(stderr,".\n");
       return; }
/* ======================= */
/** userreply **/
void userreply(len,eof)
/* read a card into card buffer and echo it */
int    *len;
LOGICAL        *eof; {
       char *result,*fgets();
       int trimlen;
             /* int j; */
       if (trace) fprintf(stderr,"> userreply\n");
       blkbuf(80,card);
       card[80] = '\0';
       *len = 0;
       *eof = FALSE;
             /* if (feof(stdin)) */
             /*   printf("On entering userreply, eof on stdin.\n"); */
             /*   if (ferror(stdin)) */
             /*   printf("On entering userreply, error on stdin.\n"); */
       rewind(stdin); /* <<-- this is needed. why? */
       result = fgets(card,80,stdin);
       if (fulltrace) fprintf(stderr,"'%s'\n",card);
             /* for (j=0;j<80;j++) */
             /* printf("%d: %d. '%c'\n",j,card[j],card[j]); */
             /* if (ferror(stdin)) */
             /* printf("After fgets, error on stdin.\n"); */
       if (feof(stdin)) {
             *eof = TRUE;
             if (trace) fprintf(stderr,"\n *** end of file ***\n");
             *len = 0;
             rewind(stdin);
             return;
             };
       trimlen = itrim(80,card);
       if (fulltrace) printf("trimlen = %d\n",trimlen);
       *len = trimlen;
       if (*len >= 81) *len = 0;
       card[*len] = '\0';
       if (fulltrace) fprintf(stderr,"'%s'\n",card);
       if (*len == 2 && lscomp(card,0,"**",0,2)) failure(99);
       return; }
/* ==================== */
/** confrm **/
LOGICAL confrm()
/* ask user for confirmation */ {
       int len;
       LOGICAL eofsw;
       if (trace) fprintf(stderr,"> confrm\n");
x100:  fprintf(stderr,"Please verify ? (Y or N):\n");
       userreply(&len,&eofsw);
       if (eofsw) goto x100;
       if (len != 1) goto x100;
       card[0] = toupper(card[0]);
       if (card[0] == 'Y') return TRUE;
       if (card[0] == 'N') return FALSE;
       if (card[0] == '7') return TRUE;
       if (card[0] == '0') return FALSE;
       goto x100; }
/* ==================== */
/** getoff **/
void getoff(office)
/* get table entry for office or proposal */
int    office; {
       offset = (long) ((office - 1) * 70);
       fseek(floffice,offset,0);
       fscanf(floffice,
             "%d %d %d %d %d %d %ld %ld %ld %2c %d %d ",
             &offinf[0], &offinf[1], &offinf[2], &offinf[3], &offinf[4],
             &offinf[5], &allow[0], &allow[1], &allow[2], allowch,
             &prtinf[0], &prtinf[1]);
       if (! trace) return;
       printf("\noffice number %d:\n",office);
       printf("offinf: %4d %4d %4d %4d %4d %4d\n",
             offinf[0], offinf[1], offinf[2], offinf[3], offinf[4], offinf[5]);
       printf("allow: %ld %ld %ld %2.2s\n",
             allow[0], allow[1], allow[2], allowch);
       printf("prtinf: %d %d\n", prtinf[0], prtinf[1]);
       return; }
/* ======================== */
/** getprec **/
void getprec(precinct,tellsw,iward,iprec,isplt,lavcb)
/* get table entry for precinct */
int    precinct;
int    tellsw;    /* whether to tell user at the terminal */
int    *iward;
int    *iprec;
int    *isplt;
LOGICAL        *lavcb; {
       long int iabsl();
       /* look up precinfo record for this precinct */
       /* printf("looking up indxpr %d in precinfo file.\n",precinct); */
       offset = (long) ((precinct - 1) * 80);
       fseek(flprecinfo,offset,0);
       fscanf(flprecinfo,
             "%d %ld %d %d %d %d %d %d %d %d %d %d %d %d %d",
             &unitno,&precno,&regist,&congr,&senate,&distct,
             &college,&vothow,
             &repre[0],&repre[1],&commis[0],&commis[1],&commis[2],
             &commis[3],&commis[4]);
       /* print precinct identification */
       offset = (long) ((unitno - 1) * 40);
       fseek(flunitinfo,offset,0);
       fscanf(flunitinfo,"%2c%20c",uncode,unitnm);
       *isplt = (short) (iabsl(precno) / 10000L);
       *iward = (short) (iabsl(precno % 10000L) / 100L);
       *iprec = (short) (iabsl(precno) % 100L);
       *lavcb = FALSE;
       if (precno < 0L) *lavcb = TRUE;
       if (tellsw || trace)
             tellprec(*iward,*iprec,*isplt,*lavcb);
       if (! trace) return;
       printf("\nprecinct number %d:\n",precinct);
       printf("unitno %d.     precno %ld.\n",unitno,precno);
       printf("registration %d.\n",regist);
       printf("congr %4d, senate %4d, distct %4d\n",
             congr,senate,distct);
       printf("college %4d, vothow %4d\n",
             college,vothow);
       printf("repre: %d %d\n",
             repre[0], repre[1]);
       printf("commis: %d %d %d %d %d\n",
             commis[0], commis[1], commis[2], commis[3], commis[4]);
       return; }
/* ======================== */
/** lallow **/
LOGICAL lallow(ioffic,iprecnum,indxpr,iunit)
/* determine whether this precinct may vote for this office */
int    ioffic;
long int       iprecnum;
int    indxpr;
int    iunit; {
       int k;
       long int iabsl();
       if (trace) fprintf(stderr,"> lallow\n");
       if (trace) fprintf(stderr,"allow type is %ld.\n",allow[TYPE]);
       /* handle missing allow from absent voter count board */
       if (iprecnum < 0L && allow[TYPE] == 0L) return TRUE;
       /* handle allow all for office */
       if (trace) fprintf(stderr,"case -1, allow all\n");
       if (allow[TYPE] == ALLOWALL) return TRUE;
       /* check each possible limitation */
       if (allow[TYPE] < 1L || allow[TYPE] > 9L) failure(19);
       switch((short) allow[TYPE]) {
case ALLOWUNIT:        /* allow unit (for unit, ward or prec allow) */
case ALLOWPREC:
case ALLOWWARD:
             if (trace) fprintf(stderr,"case 2,8,9 (unit,ward,precinct)\n");
             offset = (long) ((iunit - 1) * 40);
             fseek(flunitinfo,offset,0);
             fscanf(flunitinfo,"%2c",uncode);
             if (! lscomp(allowch,0,uncode,0,2)) return FALSE;
             if (allow[TYPE] == ALLOWPREC) {
                  /* allow precinct */
                  if (trace) fprintf(stderr,"case 8, allow precinct\n");
                  if (iprecnum < 0L) return TRUE;
                  if (allow[PRECID] != iprecnum) return FALSE;
                  return TRUE;
                  };
             if (allow[TYPE] == ALLOWWARD) {
                  /* allow ward */
                  if (trace) fprintf(stderr,"case 9, allow ward\n");
                  if (iprecnum < 0L && iabsl(iprecnum/100L) == 0L) return TRUE;
                  if (allow[PRECID]/100L != iabsl(iprecnum/100L)) return FALSE;
                  return TRUE;
                  };
             if (trace) fprintf(stderr,"case 2, unit\n");
             return TRUE;
case ALLOWCOMM:        /* allow commissioner */
             if (trace) fprintf(stderr,"case 3, allow commissioner\n");
             if (trace && commis[0] <= 0) {
                  fprintf(stderr,"commis(0) is %d.\n",commis[0]);
                  pause();
                  };
             if (iprecnum < 0L && commis[0] <= 0) return TRUE;
             for (k=0;k<5;k++) {
                  if (trace) {
                      fprintf(stderr,"allow value %ld, commis(%d) %d\n",allow[VALUE],k,commis[k]);
                      pause();
                      };
                  if (allow[VALUE] == (long) commis[k]) return TRUE;
                  };
             return FALSE;
case ALLOWREPR:        /* allow representative */
             if (trace) fprintf(stderr,"case 4, allow representative\n");
             if (iprecnum < 0L && repre[0] <= 0) return TRUE;
             for (k=0;k<2;k++) {
                  if (allow[VALUE] == (long) repre[k]) return TRUE;
                  };
             return FALSE;
case ALLOWSEN: /* allow senate */
             if (trace) fprintf(stderr,"case 5, allow senate\n");
             if (iprecnum < 0L && senate <= 0) return TRUE;
             if ((long) senate != allow[VALUE]) return FALSE;
             return TRUE;
case ALLOWCONG:        /* allow congress */
             if (trace) fprintf(stderr,"case 6, allow congress\n");
             if (iprecnum < 0L && congr <= 0) return TRUE;
             if ((long) congr != allow[VALUE]) return FALSE;
             return TRUE;
case ALLOWDIST:        /* allow district court */
             if (trace) fprintf(stderr,"case 7, allow district court\n");
             if (iprecnum < 0L && distct <= 0) return TRUE;
             if ((long) distct != allow[VALUE]) return FALSE;
             return TRUE;
case ALLOWCOLL:        /* allow college */
             if (trace) fprintf(stderr,"case 1, allow college\n");
             if (iprecnum < 0L && college <= 0) return TRUE;
             if ((long) college != allow[VALUE]) return FALSE;
             return TRUE;
               };
       /* if can't tell, poll the user */
       if (trace) fprintf(stderr,"allow case unknown\n");
       return TRUE; }
/* ==================== */
/** putoff **/
void putoff(office)
/* rewrite table entry for office or proposal */
int    office; {
       offset = (long) ((office - 1) * 70);
       fseek(floffice,offset,0);
       fprintf(floffice,
             "%4d %4d %4d %4d %4d %4d %8ld %8ld %8ld %2.2s %4d %4d ",
             offinf[0], offinf[1], offinf[2], offinf[3], offinf[4],
             offinf[5], allow[0], allow[1], allow[2], allowch,
             prtinf[0], prtinf[1]);
       return; }
/* ======================== */
