/** cumt **/
#include <stdio.h>
#include "weltab.h"
#include "filedecl.h"
#include "candnm.h"
#include "cardrd.h"
#include "cumtvt.h"
#include "dcand.h"
#include "office.h"
#include "offnam.h"
#include "longrp.h"
#include "precin.h"
#include "precvt.h"
#include "repout.h"
#include "unitinfo.h"
main()
/* produce the cumulative report from the totals.wtb file */ {
       int j,k,ierr;
       int lines,ipage,lincr,lcand;
       int iward,iprec,isplt;
       int iunit,indxpr,ifound;
       LOGICAL lavcb;
       long int iprnum;
       char date[12],time[8];
       float pcnt,total;
       FILE *fopen();
       void chead(),showdone(),dhead();
       trace = FALSE;
       blkbuf(80,report);
       lines = 60;
       ipage = 0;
       welcom(date,time);
       fprintf(stderr,"Producing Cumulative Report...\n\n");
       fileoffices = fopen("offices.dat","r");
       flglobal = fopen("global.tbl","r");
       flunitinfo = fopen("unitinfo.tbl","r");
       floffice = fopen("offices.tbl","r");
       floffnam = fopen("offnam.tbl","r");
       fldualoff = fopen("dualoff.tbl","r");
       flcand = fopen("candidat.tbl","r");
       fldualcand = fopen("dualcand.tbl","r");
       /* read global variables */
       rewind(flglobal);
       fscanf(flglobal,"%d %d %d %d %d %d %d %50c",
             &nunit, &nprec, &noffic, &ndual, &nwrite, &ncand, &ducand, electn);
       /* if totals.wtb file does not exists, cancel */
       ierr = access("totals.wtb",0);
       if (ierr != 0) {
             fprintf(stderr,"\n*** VTOT file does not exist. Run canceled.\n");
             goto x900;
             };
       /* read in the cumulative counts */
       fltotals = fopen("totals.wtb","r");
       rewind(fltotals);
       fscanf(fltotals,"%12c %8c %50c ",cmdate,cmtime,cmelec);
       fscanf(fltotals,
             "%ld %ld %ld ",
             &cmprec,&cmpoll,&cmreg);
       for (j=1;j<(ncand+1);j++) {
             fscanf(fltotals,"%ld ",&cumt[j]);
             };
       for (j=1;j<(nprec+1);j++) {
             fscanf(fltotals,"%d %ld ",&prfini[j],&prpoll[j]);
             };
       filereport = fopen("ctotal.rpt","w");
       /* print report of each office */
       for (j=1;j<(noffic+1);j++) {
             /* get table entry for this office or proposal */
             getoff(j);
             /* report office number as status on screen */
             fprintf(stderr,"Office %d out of %d.\r",j,noffic);
             /* determine if page skip necessary */
             k = 1;
             if (offinf[DUALLOCATION] > 0) k = 2;
             lincr = 2 + (offinf[CANDCOUNT] * k);
             if (lincr <= 2) lincr = 3;
             if (lines+lincr >= 60)
                  chead(&lines,&ipage);
             lines += lincr;
             /* print heading for office */
             smove(report,0,"0",0,1);
             gtoffi(j,report,10);
             cprint(filereport);
             if (offinf[DUALLOCATION] > 0) {
                  gtdual(offinf[DUALLOCATION],report,12);
                  cprint(filereport);
                  };
             /* handle each candidate */
             lcand = offinf[CANDCOUNT];
             total = 0.0;
             if (lcand <= 0) {
                  /* no candidates */
                  smove(report,14,"( No Candidates )",0,17);
                  cprint(filereport);
                  continue;
                  };
             if (prtinf[PERCENT]) {
                  for (k=1;k<(lcand+1);k++)
                      total += cumt[offinf[CANDSTART]+k-1];
                  };
             for (k=1;k<(lcand+1);k++) {
                  gtcand(offinf[CANDSTART]+k-1,report,15,report,41);
                  cvicl(cumt[offinf[CANDSTART]+k-1],report,48,7);
                  if (prtinf[PERCENT]) {
                      smove(report,57,"(xxxx.xx %)",0,11);
                      pcnt = 0.0;
                      if (total > 0.0)
                         pcnt = (cumt[offinf[CANDSTART]+k-1] / total) * 100.0;
                      cvec(pcnt,report,58,7,2);
                      };
                  cprint(filereport);
                  if (offinf[DUALLOCATION] > 0) {
                      gtdcnd(offinf[DUALSTART]+k-1,report,17);
                      cprint(filereport);
                      };
                  /* loop back for next candidate */
                  };
             /* loop back for next office */
             };
       /* print cumulative total vote cast */
       if (lines+3 >= 60)
             chead(&lines,&ipage);
       smove(report,0,"0",0,1);
       smove(report,10,"Cumulative vote cast is:",0,24);
       cvicl(cmpoll,report,36,7);
       smove(report,44,"(xxxx.xx % of Registration)",0,27);
       pcnt = 0.0;
       if (cmreg > 0)
             pcnt = ((cmpoll * 1.0) / cmreg) * 100.0;
       cvec(pcnt,report,45,7,2);
       cprint(filereport);
       goto x900;
       /* error - canceling */
x801:  fprintf(stderr,"\n*** canceled ***\n");
       failure(999);
x900:  showdone(date,time,ipage);
       /* page skip */
       smove(report,0,"1",0,1);
       cprint(filereport);
       fprintf(stderr,"\nCumulative report complete in file ctotal.rpt.\n");
       fcloseall(); }
/* ===================== */
/** chead **/
void chead(lines,ipage)
/* print heading for cumulative report */
int    *lines,*ipage; {
       int page;
       float pcnt;
       page = *ipage + 1;
       *ipage = page;
       /* print heading */
       smove(report,0,"1",0,1);
       smove(report,10,"Weltab III",0,10);
       smove(report,25,"Unofficial Cumulative",0,26);
       smove(report,57,cmtime,0,8);
       smove(report,67,cmdate,0,12);
       cprint(filereport);
       smove(report,10,"Election:",0,9);
       smove(report,25,cmelec,0,50);
       cprint(filereport);
       /* print number of precincts included in totals */
       smove(report,0,"0",0,1);
       cviczl(cmprec,report,10,3);
       smove(report,14,"precincts reporting out of ",0,27);
       cvic(nprec,report,41,3);
       pcnt = (cmprec * 100.0) / (long) nprec;
       smove(report,47,"(xxxx.xx %)",0,11);
       cvec(pcnt,report,48,7,2);
       smove(report,67,"Page",0,4);
       cvic(page,report,72,4);
       cprint(filereport);
       /* set lines for remaining page */
       *lines = 5;
       return; }
/* ======================== */
/** showdone **/
void showdone(date,time,initpage)
/* produce the un/completed precincts report based on cmprec */
char   date[12],time[8];
int    initpage; {
       LOGICAL done,lavcb;
       int lastun,j,iward,iprec,iunit,isplt;
       int lines,ipage,lincr;
       long int iabsl();
       float pcnt;
       FILE *fopen();
       blkbuf(80,report);
       lines = 60;
       ipage = initpage;
       flprecinfo = fopen("precinfo.tbl","r");
       /* determine whether to print completed or uncompleted */
       done = TRUE;
       if ((cmprec > (long) (nprec / 2)) && (cmprec < nprec))
             done = FALSE;
       if (done) {
             fprintf(stderr,"\nReporting on completed precincts...\n"); }
       else {
             fprintf(stderr,"\nReporting on precincts not yet completed...\n");
             };
       /* print information on each precinct included in totals */
       for (j=1;j<(nprec+1);j++) {
             if (done && ! prfini[j]) continue;
             if (! done && prfini[j]) continue;
             lincr = 1;
             if (lines+lincr >= 60)
                  dhead(done,&lastun,&lines,&ipage,date,time);
             lines += lincr;
             /* look up precinfo record for this precinct */
             /* fprintf(stderr,"looking up indxpr %d in precinfo file.\n",j); */
             offset = (long) ((j - 1) * 80);
             fseek(flprecinfo,offset,0);
             fscanf(flprecinfo,
                  "%d %ld %d %d %d %d %d %d %d %d %d %d %d %d %d",
                  &unitno,&precno,&regist,&congr,&senate,&distct,
                  &college,&vothow,
                  &repre[0],&repre[1],&commis[0],&commis[1],&commis[2],
                  &commis[3],&commis[4]);
             if (trace) fprintf(stderr,"unitno %d.     precno %ld.\n",unitno,precno);
             if (trace) fprintf(stderr,"registration %d.\n",regist);
             /* print precinct identification */
             offset = (long) ((unitno - 1) * 40);
             fseek(flunitinfo,offset,0);
             fscanf(flunitinfo,"%2c%20c",uncode,unitnm);
             isplt = (short) (iabsl(precno) / 10000L);
             iward = (short) (iabsl(precno % 10000L) / 100L);
             iprec = (short) (iabsl(precno) % 100L);
             lavcb = FALSE;
             if (precno < 0L) lavcb = TRUE;
             /* unit */
             if (unitno != lastun) {
                  lastun = unitno;
                  smove(report,0,"0",0,1);
                  smove(report,10,unitnm,0,20);
                  if (lines+3 >= 60)
                      dhead(done,&lastun,&lines,&ipage,date,time);
                  lines += 2;
                  cprint(filereport);
                  };
             /* precinct */
             if (iward > 0) {
                  smove(report,12,"Ward ",0,5);
                  cvic(iward,report,17,2);
                  };
             if (lavcb) {
                  smove(report,21,"AVCB ",0,5); }
             else {
                  smove(report,21,"Prec ",0,5);
                  };
             cvic(iprec,report,26,2);
             if (isplt != 0) {
                  smove(report,28,"-",0,1);
                  cvicz(isplt,report,29,2);
                  };
             /* registration percent */
             if (precno >= 0L) cvic(regist,report,35,7);
             if (precno < 0L) smove(report,39,"n/a",0,3);
             cvicl(prpoll[j],report,44,7);
             pcnt = 0.0;
             if (regist > 0)
                  pcnt = ((prpoll[j] * 1.0) / regist) * 100.0;
             if (precno > 0L) cvec(pcnt,report,54,7,2);
             if (precno > 0L) smove(report,62,"%",0,1);
             /* index */
             cvicz(j,report,68,3);
             cprint(filereport);
             };
       if (done) {
             /* print cumulative totals */
             if (lines+3 >= 60)
                  dhead(done,&lastun,&lines,&ipage,date,time);
             smove(report,0,"0",0,1);
             smove(report,10,"Totals:",0,7);
             cvicl(cmreg,report,35,7);
             cvicl(cmpoll,report,44,7);
             pcnt = 0.0;
             if (cmreg > 0L)
                  pcnt = ((cmpoll * 1.0) / cmreg) * 100.0;
             cvec(pcnt,report,54,7,2);
             smove(report,62,"%",0,1);
             cprint(filereport);
             };
       return; }
/* =====================
/** dhead **/
void dhead(done,lastun,lines,ipage,date,time)
/* print heading for un/completed precincts report */
LOGICAL        done;
int    *lastun,*lines,*ipage;
char   date[12],time[8]; {
       float pcnt;
       (*ipage)++;
       /* print heading */
       smove(report,0,"1",0,1);
       smove(report,10,"Weltab III",0,11);
       if (done)
             smove(report,25,"Precincts Completed",0,19);
       if (! done)
             smove(report,25,"Precincts Yet To Do",0,19);
       smove(report,57,time,0,8);
       smove(report,67,date,0,12);
       cprint(filereport);
       smove(report,10,"Election:",0,9);
       smove(report,25,electn,0,50);
       cprint(filereport);
       /* print number of precincts included in totals */
       smove(report,0,"0",0,1);
       cviczl(cmprec,report,10,3);
       smove(report,14,"precincts reporting out of ",0,27);
       cvic(nprec,report,41,3);
       pcnt = (cmprec * 100.0) / nprec;
       smove(report,47,"(xxxx.xx %)",0,11);
       cvec(pcnt,report,48,7,2);
       smove(report,67,"Page",0,4);
       cvic(*ipage,report,72,4);
       cprint(filereport);
       if (done) {
             smove(report,10,
       "The following precincts are included in the tabulation:",0,55); }
       else {
             smove(report,10,
       "The following precincts are not yet in the tabulation:",0,54);
             };
       smove(report,0,"0",0,1);
       cprint(filereport);
       smove(report,36,
             "Registr  At-poll    Percent    Index",0,36);
       cprint(filereport);
       /* reset for new page */
       *lines = 7;
       *lastun = -1;
       return; }
void ctail(lines,ipage)
/* print tail for cumulative report */
int    *lines,*ipage; {
       int pageno;
       pageno = *ipage + 1;
       *ipage = pageno;
       /* print heading */
       smove(report,0,"1",0,1);
       smove(report,10,"Weltab III",0,10);
       smove(report,25,"Unofficial Cumulative Result",0,26);
       smove(report,57,cmtime,0,8);
       smove(report,67,cmdate,0,12);
       cprint(filereport);
       smove(report,10,"Election:",0,9);
       smove(report,25,cmelec,0,50);
       smove(report,30,"Tail Version",0,8);
       cprint(filereport);
       /* print number of precincts included in totals */
       smove(report,0,"0",0,1);
       cviczl(cmprec,report,10,3);
       smove(report,14,"precincts reporting out of ",0,27);
       cvic(nprec,report,41,3);
       smove(report,67,"Page:",0,4);
       cvic(pageno,report,72,4);
       cprint(filereport);
       /* set lines for remaining page */
       *lines = 5;
       return; }
