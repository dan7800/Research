/** cand **/
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
/* produce a report of candidate names and indices */ {
       int j,k;
       int lines,ipage,lincr,lcand;
       char date[12],time[8];
       char fname[13];
       float pcnt,total;
       FILE *fopen();
       void candhead();
       trace = FALSE;
       blkbuf(80,report);
       lines = 60;
       ipage = 0;
       welcom(date,time);
       smove(fname,0,"candidat.prt",0,12);
       fname[12] = '\0';
       filereport = fopen(fname,"w");
       flglobal = fopen("global.tbl","r");
       flcand = fopen("candidat.tbl","r");
       /* read global variables */
       offset = 0L;
       fseek(flglobal,offset,0);
       fscanf(flglobal,"%d %d %d %d %d %d %d %50c",
             &nunit, &nprec, &noffic, &ndual, &nwrite, &ncand, &ducand, electn);
       candhead(&lines,&ipage,date,time);
       smove(report,0,"0",0,1);
       smove(report,10,"Index      Candidate",0,20);
       cprint(filereport);
       smove(report,0,"0",0,1);
       /* print information on each candidate */
       for (j=1;j<(ncand+1);j++) {
             cvic(j,report,10,5);
             gtcand(j,report,16);
             cprint(filereport);
             /* loop back for next candidate */
             };
       /* page skip */
       smove(report,0,"1",0,1);
       cprint(filereport);
       fprintf(stderr,"Candidate Names report is in file %s\n",fname);
       fcloseall(); }
/* ===================== */
/** candhead **/
void candhead(lines,ipage,date,time)
/* print heading of candidate indices report */
int    *lines,*ipage;
char   date[],time[]; {
       int cprec;
       float pcnt;
       *ipage = (*ipage)++;
       /* print heading */
       smove(report,0,"1",0,1);
       smove(report,10,"Weltab III",0,10);
       smove(report,25,"Candidate Indices Report",0,24);
       smove(report,57,time,0,8);
       smove(report,67,date,0,12);
       cprint(filereport);
       smove(report,10,"Election:",0,9);
       smove(report,25,electn,0,50);
       cprint(filereport);
       smove(report,67,"Page",0,4);
       cvic(*ipage,report,72,4);
       cprint(filereport);
       /* set lines for remaining page */
       *lines = 5;
       return; }
/* ======================== */
void candtail(lines,ipage,date,time)
/* print tails of candidate indices report */
int    *lines,*ipage;
char   date[],time[]; {
       int cprec;
       float pcnt;
       *ipage = (*ipage)++;
       /* print heading */
       smove(report,0,"0",0,1);
       smove(report,10,"Weltab III",0,10);
       smove(report,25,"Candidate Report",0,24);
       smove(report,57,date,0,8);
       smove(report,67,time,0,12);
       cprint(filereport);
       smove(report,10,"Election:",0,9);
       smove(report,25,electn,0,50);
       cprint(filereport);
       smove(report,67,"Page:",0,4);
       cvic(*ipage,report,72,4);
       cprint(filereport);
       /* set lines for remaining page */
       *lines = 0;
       return; }
