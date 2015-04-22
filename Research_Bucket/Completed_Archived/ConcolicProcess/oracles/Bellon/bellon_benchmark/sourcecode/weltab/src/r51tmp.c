/** rsum **/
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
#include "state.h"
/* LANDSCAPE in state.h controls the lines per page, which
   needs to be 56 for the Xerox 9700 printer */
#include "unitinfo.h"
       char fprecname[13],fofficename[13];
main()
/* produce the unofficial results summary */
/* this is the report given to new media at 6:00 am of all results */ {
       int cstart[4],clen[4],cbreak;
       long int prtotl[MAXPRECINCT];
       int lincr,lcand,rincr,rcand;
       int i,j,k,m,ierr,xprec,namlen,lines;
       int ioffic,ipage,icand,len1,len2,ilen;
       int iward,iprec,isplt;
       int iunit,indxpr,ifound;
       int start1,start2,start3;
       LOGICAL leftsw,rigtsw;
       LOGICAL lavcb,eofsw;
       long int iprnum;
       long int ivote;
       char date[12],time[8];
       char vtelec[51],cname[26],cparty[4];
       FILE *fopen();
       void rshead(),rsprtpag();
       trace = FALSE;
       fulltrace = FALSE;
       blkbuf(80,report);
       blkbuf(130,longrep);
       blkbuf(80,card);
       ipage = 0;
       welcom(date,time);
       fprintf(stderr,"Preparing Unofficial Results Summary...\n\n");
       flglobal = fopen("global.tbl","r");
       flunitinfo = fopen("unitinfo.tbl","r");
       flprecinfo = fopen("precinfo.tbl","r");
       floffice = fopen("offices.tbl","r");
       floffnam = fopen("offnam.tbl","r");
       fldualoff = fopen("dualoff.tbl","r");
       flcand = fopen("candidat.tbl","r");
       fldualcand = fopen("dualcand.tbl","r");
       /* read global variables */
       rewind(flglobal);
       fscanf(flglobal,"%d %d %d %d %d %d %d %50c",
             &nunit, &nprec, &noffic, &ndual, &nwrite, &ncand, &ducand, electn);
       fprintf(stderr,"%50.50s\n",electn);
       /* set up the page heading */
       blkbuf(130,headc);
       smove(headc,0,"1Weltab III",0,11);
       smove(headc,12,"Unofficial Results Summary",0,26);
       smove(headc,43,electn,0,50);
       smove(headc,93,date,0,12);
       smove(headc,106,time,0,8);
       smove(headc,117,"Page",0,4);
       smove(fprecname,0,"PRECxxxx.VOT",0,12);
       fprecname[12] = '\0';
       smove(fofficename,0,"RACExxxx.SUM",0,12);
       fofficename[12] = '\0';
       /* main loop - get next office */
x100:  noop();
       for (j=51;j<(100+1);j++) {
             getoff(j);
             /* accept all races for the unofficial summary */
             gtoffi(j,offnam,0);
             fprintf(stderr,"\nOffice %d out of %d:  %50.50s\n",j,noffic,offnam);
             cvicz(j,fofficename,4,4);
             if (ipage != 0) fclose(filereport);
             filereport = fopen(fofficename,"w");
             /* fprintf(stderr,"file %s opened. ipage %d. filereport %d.\n",
                  fofficename,ipage,filereport); */
x105:   outpos = 23;
             blkbuf(130,head1);
             blkbuf(130,head2);
             blkbuf(130,head3);
             nform = 0;
             for (i=0;i<21;i++) {
                  form[CAND][i] = 0L;
                  form[POSN][i] = 0L;
                  form[NVOTE][i] = 0L;
                  form[TOTAL][i] = 0L;
                  form[UNITTOT][i] = 0L;
                  form[WARDTOT][i] = 0L;
                  };
             for (i=0;i<(nprec+1);i++)
                  prtotl[i] = 0L;
             /* check candidates for office */
             icand = offinf[CANDCOUNT];
             /* no write-ins for the unofficial results summary */
             if (icand <= 0) {
                  smove(head1,15,
                  "*** No candidates on the ballot for this office ***",0,52);
                  rshead(j,&lines,&ipage,date,time);
                  continue;
                  };
             /* loop through candidates */
             for (k=1;k<(icand+1);k++) {
                  /* get candidate */
                  gtcand((offinf[CANDSTART]+k-1),cname,0,cparty,0);
                  namlen = itrim(25,cname);
                  if ((namlen == 3 && lscomp(cname,0,"Yes",0,3))
                   || (namlen == 2 && lscomp(cname,0,"No",0,2))) {
                      ilen = max(namlen,12);
                      if (outpos+ilen >= 130)
                         rsprtpag(j,&ipage,date,time,prtotl);
                      start2 = outpos + 7 - min(7,namlen);
                      smove(head2,start2,cname,0,namlen); }
                  else {
                      /* find name break */
x202:                  cbreak = 2;
                      scnbuf(namlen,cname,4,cstart,clen,&ierr);
/* fprintf(stderr,"namlen (itrim(25,cname) is %d\n",namlen);
   fprintf(stderr,"cname is '%25.25s', ierr %d\n",cname,ierr);
   fprintf(stderr,"cstart is %d %d %d %d. clen is %d %d %d %d.\n",
   cstart[0],cstart[1],cstart[2],cstart[3],clen[0],clen[1],clen[2],clen[3]); */
                      if (ierr >= 0) goto x215;
                      for (i=0;i<4;i++) {
                         if (clen[i] <= 0) goto x212;
                         };
                      failure(4);
x212:                  if (i <= 3) cbreak = 1;
x215:                  len2 = namlen - cstart[cbreak];
                      len1 = cstart[cbreak] - 1;
                      ilen = max(len1,len2);
                      ilen = max(ilen,12);
                      if (outpos+ilen >= 130)
                         rsprtpag(j,&ipage,date,time,prtotl);
                      start1 = outpos + 7 - min(7,len1);
                      smove(head1,start1,cname,0,len1);
                      start2 = outpos + 7 - min(7,len2);
                      smove(head2,start2,cname,cstart[cbreak],len2);
                      start3 = outpos + 7 - 3;
                      smove(head3,start3,cparty,0,3);
                      };
                  nform++;
                  if (nform > 20) failure(12);
                  form[CAND][nform] = (long) offinf[CANDSTART] + k - 1;
                  form[POSN][nform] = (long) outpos;
                  outpos += ilen;
                 };
             /* add totals column */
             if (outpos+10 >= 130)
                  rsprtpag(j,&ipage,date,time,prtotl);
             smove(head2,outpos+7-6,"Totals",0,6);
             nform++;
             if (nform > 20) failure(28);
             form[CAND][nform] = -1L;
             form[POSN][nform] = (long) outpos;
             rsprtpag(j,&ipage,date,time,prtotl);
             /* get next office */
             for (i=0;i<(nprec+1);i++)
                  prtotl[i] = 0L;
             };
x900:  fcloseall(); }
/* ===================== */
/** prtpag **/
void rsprtpag(ioffic,fpage,date,time,prtotl)
/* print page for this office */
int    *fpage;
long int prtotl[];
char   date[],time[]; {
       char buf[80];
       int lastun,lastwd,ipage;
       int lines,iw,j,k,xprec,ierr;
       int iunit,indxpr,ifound;
       long int iprnum;
       int iward,iprec,isplt;
       LOGICAL lavcb;
       void rshead();
       ipage = *fpage;
       lines = LANDSCAPE;
       for (iw=1;iw<(nform+1);iw++) {
             form[WARDTOT][iw] = 0L;
             form[UNITTOT][iw] = 0L;
             };
       /* loop through precincts */
       lastun = -1;
       lastwd = -1;
       for (j=1;j<(nprec+1);j++) {
                getprec(j,FALSE,&iward,&iprec,&isplt,&lavcb);
             if (precno < 0 & lastun != unitno) continue;
             if (! lallow(ioffic,precno,j,unitno)) continue;
             /* find <indxpr>.vot filename */
             cvicz(j,fprecname,4,4);
             /* if it does not exists, abort */
             ierr = access(fprecname,0);
             if (ierr != 0) {
                  fprintf(stderr,"*** File '%12.12s' does not exist.\n",fprecname);
                  continue;
                  };
             /* file exists, proceed */
             if (ipage != 0) fclose(flprecvote);
             flprecvote = fopen(fprecname,"r");
             /* read status and record from <indxpr>.vot file */
             rewind(flprecvote);
             fscanf(flprecvote,"%d %ld %d %d ",&iunit,&iprnum,&indxpr,&ifound);
             fscanf(flprecvote,
                  ">%d %d %ld %ld %8c %8c %12c %8c ",
                  &vtindx,&vtunit,&vtprec,&vtpoll,entrer,verifr,vtdate,vttime);
             /* eliminate precincts with no returns (unused avcb) */
             if (vtpoll < 0L) {
                  tellprec(iward,iprec,isplt,lavcb);
                  fprintf(stderr,"This precinct has no total vote cast. Skipping.\n");
                  continue;
                  };
             for (k=1;k<(ncand+1);k++) {
                  fscanf(flprecvote,"%ld ",&vote[k]);
                  };
             if (trace)
                  fprintf(stderr,"Precinct %d read from file %12.12s.\n",xprec,fprecname);
             /* print unit header if different unit */
x118:   if (lastun == vtunit) goto x150;
             if (lastun <= 0) goto x212;
             /* print ward total for prior ward, if needed */
             if (lastwd <= 0) goto x120;
             lastwd = -1;
             smove(longrep,0,"0  Ward totals:",0,15);
             for (k=1;k<(nform+1);k++) {
                  cvicl(form[WARDTOT][k],longrep,(short) form[POSN][k],7);
                  form[WARDTOT][k] = 0L;
                  };
             if (lines+2 >= LANDSCAPE)
                  rshead(ioffic,&lines,&ipage,date,time);
             lprint(filereport);
             lines += 2;
             /* print totals for prior unit */
x120:   smove(longrep,0,"0  Unit totals:",0,15);
             for (k=1;k<(nform+1);k++) {
                  cvicl(form[UNITTOT][k],longrep,(short) form[POSN][k],7);
                  form[UNITTOT][k] = 0L;
                  };
             if (lines+2 >= LANDSCAPE)
                  rshead(ioffic,&lines,&ipage,date,time);
             lprint(filereport);
             lines += 2;
             smove(longrep,0," ",0,1);
             lprint(filereport);
             smove(longrep,0,"0",0,1);
             /* print heading with unit name */
x212:   lastun = vtunit;
             smove(longrep,1,unitnm,0,20);
             if (lines+3 >= LANDSCAPE)
                  rshead(ioffic,&lines,&ipage,date,time);
             lprint(filereport);
             lines += 3;
             blkbuf(80,buf);
             fprintf(stderr,"     %20.20s          Office %d of %d.\n",
                  unitnm,ioffic,noffic);
             /* print prior ward totals, if needed */
x150:   if (lastwd <= 0) goto x152;
             if (lastwd == iward) goto x152;
             smove(longrep,0,"0  Ward totals:",0,15);
             for (iw=1;iw<(nform+1);iw++) {
                  cvicl(form[WARDTOT][iw],longrep,(short) form[POSN][iw],7);
                  form[WARDTOT][iw] = 0L;
                  };
             lastwd = -1;
             if (lines+2 > LANDSCAPE)
                  rshead(ioffic,&lines,&ipage,date,time);
             lprint(filereport);
             lines += 2;
             lprint(filereport);
             /* print ward and precinct */
x152:   if (iward > 0) {
                  smove(longrep,3,"Ward ",0,5);
                  cvic(iward,longrep,8,2);
                  };
             lastwd = iward;
             if (lavcb) {
                  smove(longrep,12,"AVCB ",0,5); }
             else {
                  smove(longrep,12,"Prec ",0,5);
                  };
             cvic(iprec,longrep,17,2);
             if (isplt != 0) {
                  smove(longrep,19,"-",0,1);
                  cvicz(isplt,longrep,20,2);
                  };
             fprintf(stderr,"       %22.22s\n",longrep);
x112:   if (nform <= 0) failure(8);
             for (k=1;k<(nform+1);k++) {
                  if (form[CAND][k] <= 0L) goto x117;
                  form[NVOTE][k] = (long) vote[form[CAND][k]];
                  form[TOTAL][k] += form[NVOTE][k];
                  form[UNITTOT][k] += form[NVOTE][k];
                  if (iward > 0)
                      form[WARDTOT][k] += form[NVOTE][k];
                  prtotl[j] += form[NVOTE][k];
                  };
             goto x159;
             /* handle totals column */
x117:   form[NVOTE][k] = prtotl[j];
             form[UNITTOT][k] += form[NVOTE][k];
             if (iward > 0)
                  form[WARDTOT][k] += form[NVOTE][k];
             form[TOTAL][k] += form[NVOTE][k];
             goto x159;
             /* put nvote for each candidate into buffer */
x159:   for (k=1;k<(nform+1);k++)
                  cvicl(form[NVOTE][k],longrep,(short) form[POSN][k],7);
             /* print line */
             if (lines+1 >= LANDSCAPE)
                  rshead(ioffic,&lines,&ipage,date,time);
             lprint(filereport);
             lines++;
             /* next precinct */
             };
       /* print ward total for last ward, if needed */
       if (lastwd > 0) {
             lastwd = -1;
             smove(longrep,0,"0  Ward totals:",0,15);
             for (k=1;k<(nform+1);k++) {
                  cvicl(form[WARDTOT][k],longrep,(short) form[POSN][k],7);
                  form[WARDTOT][k] = 0L;
                  };
             if (lines+2 >= LANDSCAPE)
                  rshead(ioffic,&lines,&ipage,date,time);
             lprint(filereport);
             lines += 2;
             };
       /* print unit total for last precinct */
x308:  smove(longrep,0,"0  Unit totals:",0,15);
       for (k=1;k<(nform+1);k++) {
             cvicl(form[UNITTOT][k],longrep,(short) form[POSN][k],7);
             form[UNITTOT][k] = 0L;
             };
       if (lines+2 >= LANDSCAPE)
             rshead(ioffic,&lines,&ipage,date,time);
       lprint(filereport);
       lines += 2;
       /* print column totals */
       smove(longrep,0," ",0,1);
       lprint(filereport);
       smove(longrep,0,"0County totals:",0,15);
       for (k=1;k<(nform+1);k++) {
             cvicl(form[TOTAL][k],longrep,(short) form[POSN][k],7);
             form[TOTAL][k] = 0L;
             };
       if (lines+3 >= LANDSCAPE)
             rshead(ioffic,&lines,&ipage,date,time);
       lprint(filereport);
       lines += 3;
       outpos = 24;
       nform = 0;
       blkbuf(130,head1);
       blkbuf(130,head2);
       blkbuf(130,head3);
       *fpage = ipage;
       return; }
/* ===================== */
/** rshead **/
void rshead(ioffic,lines,ipage,date,time)
/* print header for unofficial results summary */
int    ioffic;
int    *lines;
int    *ipage;
char   date[],time[]; {
       (*ipage)++;
       /* print heading */
       cvic(*ipage,headc,122,4);
       lhprint(filereport,headc);
       /* office line */
       blkbuf(130,head0);
       smove(head0,0,"0Office:",0,8);
       gtoffi(ioffic,head0,10);
       lhprint(filereport,head0);
       /* print candidate heading lines */
       smove(head1,0,"0",0,1);
       lhprint(filereport,head1);
       lhprint(filereport,head2);
       lhprint(filereport,head3);
       /* set lines used for new page */
       *lines = 7;
       return; }
/* ===================== */
