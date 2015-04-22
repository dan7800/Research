/** samp **/
#include <stdio.h>
#include "weltab.h"
#include "filedecl.h"
#include "candnm.h"
#include "cardrd.h"
#include "dcand.h"
#include "longrp.h"
#include "office.h"
#include "offnam.h"
#include "precin.h"
#include "precvt.h"
#include "repout.h"
#include "unitinfo.h"
main()
/* produce a sample cumulative report for verification */ {
       int j,k;
       int lines,ipage,lincr,lcand;
       char date[12],time[8];
       float pcnt,total;
       FILE *fopen();
       void shead();
       trace = FALSE;
       blkbuf(80,report);
       lines = 60;
       ipage = 0;
       welcom(date,time);
       filereport = fopen("sample.prt","w");
       fileoffices = fopen("offices.dat","r");
       flglobal = fopen("global.tbl","r");
       flunitinfo = fopen("unitinfo.tbl","r");
       flprecinfo = fopen("precinfo.tbl","r");
       floffice = fopen("offices.tbl","r");
       floffnam = fopen("offnam.tbl","r");
       fldualoff = fopen("dualoff.tbl","r");
       flcand = fopen("candidat.tbl","r");
       fldualcand = fopen("dualcand.tbl","r");
       /* read global variables */
       offset = 0L;
       fseek(flglobal,offset,0);
       fscanf(flglobal,"%d %d %d %d %d %d %d %50c",
             &nunit, &nprec, &noffic, &ndual, &nwrite, &ncand, &ducand, electn);
       /* set mock cumulative record to dummy data */
       vtpoll = 9999999L;
       for (j=1;j<(ncand+1);j++)
             vote[j] = 99999L;
       /* print report of each office */
       for (j=1;j<(noffic+1);j++) {
             /* get table entry for this office or proposal */
             offset = (long) ((j - 1) * 70);
             fseek(floffice,offset,0);
             fscanf(floffice,
                  "%d %d %d %d %d %d %ld %ld %ld %2c %d %d ",
                  &offinf[0], &offinf[1], &offinf[2], &offinf[3], &offinf[4],
                  &offinf[5], &allow[0], &allow[1], &allow[2], allowch,
                  &prtinf[0], &prtinf[1]);
             /* printf("\noffice number %d:\n",j); */
             /* printf("offinf: %4d %4d %4d %4d %4d %4d\n", */
             /* offinf[0], offinf[1], offinf[2], offinf[3], offinf[4], offinf[5]); */
             /* printf("allow: %ld %ld %ld %2.2s\n", */
             /* allow[0], allow[1], allow[2], allowch); */
             /* printf("prtinf: %d %d\n", prtinf[0], prtinf[1]); */
             /* determine if page skip necessary */
             k = 1;
             if (offinf[DUALLOCATION] > 0) k = 2;
             lincr = 2 + (offinf[CANDCOUNT] * k);
             if (lincr == 2) lincr = 3;
             if (lines+lincr >= 60)
                  shead(&lines,&ipage,date,time);
             lines += lincr;
             /* print heading for office */
             smove(report,0,"0",0,1);
             gtoffi(j,report,10);
             cprint(filereport);
             if (offinf[DUALLOCATION] > 0) {
                  gtdual(j,report,12);
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
                      total += vote[offinf[CANDSTART]+k-1];
                  };
             for (k=1;k<(lcand+1);k++) {
                  gtcand(offinf[CANDSTART]+k-1,report,15,report,41);
                  cvicl(vote[offinf[CANDSTART]+k-1],report,48,7);
                  if (prtinf[PERCENT]) {
                      smove(report,57,"(xxxx.xx %)",0,11);
                      pcnt = 0.0;
                      if (total > 0.0)
                         pcnt = (vote[offinf[CANDSTART]+k-1] / total) * 100.0;
                      cvec(pcnt,report,58,7,2);
                      };
                  cprint(filereport);
                  if (offinf[DUALLOCATION] > 0) {
                      gtdcnd(offinf[DUALSTART]+k-1,report,17);
                      cprint(filereport);
                      };
                  /* loop back for next candidate */
                  };
             /* loop back for next election */
             };
       /* print cumulative total vote cast */
       if (lines+3 >= 60)
             shead(&lines,&ipage,date,time);
       smove(report,0,"0",0,1);
       smove(report,10,"Cumulative vote cast is:",0,24);
       cvicl(vtpoll,report,36,7);
       smove(report,44,"(xxxx.xx % of registration )",0,28);
       cvec(0.0,report,45,7,2);
       cprint(filereport);
       /* page skip */
       smove(report,0,"1",0,1);
       cprint(filereport);
       fcloseall(); }
/* ===================== */
/** shead **/
void shead(lines,ipage,date,time)
/* print heading of sample cumulative report */
int    *lines,*ipage;
char   date[],time[]; {
       int cprec,pageno;
       float pcnt;
       pageno = *ipage + 1;
       *ipage = pageno;
       /* print heading */
       smove(report,0,"1",0,1);
       smove(report,10,"Weltab III",0,10);
       smove(report,25,"Sample Cumulative Report",0,24);
       smove(report,57,time,0,8);
       smove(report,67,date,0,12);
       cprint(filereport);
       smove(report,10,"Election:",0,9);
       smove(report,25,electn,0,50);
       cprint(filereport);
       smove(report,0,"0",0,1);
       cprec = 0;
       cvicz(cprec,report,10,3);
       smove(report,14,"precincts reporting out of ",0,27);
       cvic(nprec,report,41,3);
       smove(report,47,"(xxxx.xx %)",0,11);
       pcnt = (cprec * 100.0) / nprec;
       cvec(pcnt,report,48,7,2);
       smove(report,67,"Page",0,4);
       cvic(pageno,report,72,4);
       cprint(filereport);
       /* set lines for remaining page */
       *lines = 5;
       return; }
/* ======================== */
