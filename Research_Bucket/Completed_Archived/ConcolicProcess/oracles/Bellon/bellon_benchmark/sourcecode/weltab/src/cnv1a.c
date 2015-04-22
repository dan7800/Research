/** cnv1a **/
/* produce canvass worksheet for only precinct found by pfnd */
/* mod 10-23-88 to gen. ARCHSQ.BAT to PKARC and erase the .cnv report */
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
       int canvstart,canvend,canvcount;
       LOGICAL revised;
       /* left and rights side copies of office table */
       int lfoffinf[6],rtoffinf[6];
       long int lfallow[3],rtallow[3];
       char lfallowch[2],rtallowch[2];
       LOGICAL lfprtinf[2],rtprtinf[2];
main()
/* program to produce the canvass worksheet */
/* from the database of unofficial election results  */ {
       int ppage,lk,rk,ileft,iright;
       int lincr,lcand,rincr,rcand;
       int j,k,m,ierr,xprec,len;
       int lines,ipage,ioffic;
       int iward,iprec,isplt;
       int iunit,indxpr,ifound;
       LOGICAL leftsw,rigtsw,eofsw;
       LOGICAL lavcb;
       long int iprnum,iabsl();
       long int ivote;
       char date[12],time[8];
       char vtelec[50];
       char fname[13],fcnvname[13];
       FILE *fopen(),*flleft,*flright;
       void canvw();
       trace = FALSE;
       fulltrace = FALSE;
       blkbuf(80,report);
       blkbuf(130,longrep);
       blkbuf(80,card);
       ipage = 0;
       canvcount = 0;
       welcom(date,time);
       fprintf(stderr,"Canvass Worksheet...\n");
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
       fprintf(stderr,"%50.50s\n",electn);
/* qorig:      fprintf(stderr,
       "\nIs this the Original or Revised worksheet (O or R) ?\n");
       userreply(&len,&eofsw);
       if (eofsw) goto qorig;
       if (len < 1) goto qorig;
       card[0] = toupper(card[0]);
       if (card[0] != 'O' && card[0] != 'R') goto qorig;  */
       revised = FALSE;
/*     if (card[0] == 'R') revised = TRUE;                */
       /* read precinct selected by pfnd */
       flprecid = fopen("thisprec.wtb","r");
       rewind(flprecid);
       fscanf(flprecid,"%d %ld %d ",&iunit,&iprnum,&indxpr);
       if (trace) printf("\nPrecinct (%d,%ld,%d)\n",iunit,iprnum,indxpr);
       /* check if pfnd canceled */
       if (iunit < 0 || indxpr < 0) {
             fprintf(stderr,"*** Skipping CNV1A. Run canceled.\n");
             goto done;
             };
       canvstart = indxpr;
       canvend = indxpr;
/*  qstart:    fprintf(stderr,
       "\nStart precinct (1,%d) or ALL ?\n",nprec);
       userreply(&len,&eofsw);
       if (eofsw) goto qstart;
       if (len < 1) goto qstart;
       card[0] = toupper(card[0]);
       card[1] = toupper(card[1]);
       if (lscomp(card,0,"AL",0,2)) {
             canvstart = 1;
             canvend = nprec;
             goto gocanv;
             };
       if (lscomp(card,0,"QU",0,2)) failure(99);
       if (lscomp(card,0,"**",0,2)) failure(99);
       cvci(card,0,len,&canvstart);
       if (canvstart < 1 || canvstart > nprec) goto qstart;
       if (canvstart != 1) {
             fprintf(stderr,
"\nReminder: When start is not precinct one, worksheet page number\n");
             fprintf(stderr,
"          is not printed.  Precinct page number still appears.\n");
             };   */
/* qend:       fprintf(stderr,
       "\nEnd precinct (1,%d) ?\n",nprec);
       userreply(&len,&eofsw);
       if (eofsw) goto qend;
       if (len < 1) goto qend;
       card[0] = toupper(card[0]);
       card[1] = toupper(card[1]);
       if (lscomp(card,0,"QU",0,2)) failure(99);
       if (lscomp(card,0,"**",0,2)) failure(99);
       cvci(card,0,len,&canvend);
       if (canvend < 1 || canvend > nprec) goto qend; */
gocanv:        fprintf(stderr,"\n");
       smove(fname,0,"PRECxxxx.VOT",0,12);
       fname[12] = '\0';
       smove(fcnvname,0,"PRECxxxx.CNV",0,12);
       fcnvname[12] = '\0';
       flleft = fopen("left.tmp","w+");
       flright = fopen("right.tmp","w+");
       /* get each precinct */
       for (xprec=canvstart;xprec<(canvend+1);xprec++) {
             /* find <indxpr>.vot and .cnv filename */
             cvicz(xprec,fname,4,4);
             smove(fcnvname,4,fname,4,4);
             lines = 60;
             ppage = 0;
             /* if it does not exists, abort */
             ierr = access(fname,0);
             if (ierr != 0) {
                  if (trace)
                      fprintf(stderr,"*** File '%12.12s' does not exist.\n",fname);
                  continue;
                  };
             /* file exists, proceed */
             if (ipage > 0) fclose(flprecvote);
             flprecvote = fopen(fname,"r");
/* fprintf(stderr,"file '%s' fopened as flprecvote\n",fname); */
             /* look up precinfo record for this precinct */
             /* printf("looking up indxpr %d in precinfo file.\n",indxpr); */
             offset = (long) ((xprec - 1) * 80);
             fseek(flprecinfo,offset,0);
             fscanf(flprecinfo,
                  "%d %ld %d %d %d %d %d %d %d %d %d %d %d %d %d",
                  &unitno,&precno,&regist,&congr,&senate,&distct,
                  &college,&vothow,
                  &repre[0],&repre[1],&commis[0],&commis[1],&commis[2],
                  &commis[3],&commis[4]);
             if (trace) printf("unitno %d.     precno %ld.\n",unitno,precno);
             if (trace) printf("registration %d.\n",regist);
             /* read status and record from <indxpr>.vot file */
             offset = 0L;
             fseek(flprecvote,offset,0);
             fscanf(flprecvote,"%d %ld %d %d ",&iunit,&iprnum,&indxpr,&ifound);
             /* print precinct identification */
             if (trace) {
                  fprintf(stderr,
                  "iunit %d, unitno %d, iprnum %ld, indxpr %d\n",
                      iunit,unitno,iprnum,indxpr);
                  pause();
                  };
             offset = (long) ((iunit - 1) * 40);
             fseek(flunitinfo,offset,0);
             fscanf(flunitinfo,"%2c%20c",uncode,unitnm);
             isplt = (short) (iabsl(iprnum) / 10000L);
             iward = (short) (iabsl(iprnum % 10000L) / 100L);
             iprec = (short) (iabsl(iprnum) % 100L);
             lavcb = FALSE;
             if (iprnum < 0L) lavcb = TRUE;
             if (ifound <= 1) {
                  fprintf(stderr,"\n");
                  tellprec(iward,iprec,isplt,lavcb);
                  fprintf(stderr,"This precinct has not been edited. Skipping.\n");
                  continue;
                  };
             fscanf(flprecvote,
                  ">%d %d %ld %ld %8c %8c %12c %8c ",
                  &vtindx,&vtunit,&vtprec,&vtpoll,entrer,verifr,vtdate,vttime);
             /* eliminate precincts with no returns (unused avcb) */
             if (vtpoll < 0L) {
                  fprintf(stderr,"\n");
                  tellprec(iward,iprec,isplt,lavcb);
                  fprintf(stderr,"This precinct has no total vote cast. Skipping.\n");
                  continue;
                  };
             for (j=1;j<(ncand+1);j++) {
                  fscanf(flprecvote,"%ld ",&vote[j]);
                  };
             if (trace)
                  fprintf(stderr,"Precinct %d read from file %12.12s.\n",xprec,fname);
             setdate(date,time);
             canvcount++;
             /* will do report, so open <indxpr>.cnv file */
             if (ipage > 0) fclose(filereport);
                  /* but first, write the ARCHSQ.BAT file */
                  filereport = fopen("archsq.bat","w");
                  fprintf(filereport,"echo off\n");
                  fprintf(filereport,"pkarc a %8.8s %12.12s >archsq.out\n",fname,fcnvname);
                  fprintf(filereport,"erase %12.12s \n",fcnvname);
                  fclose(filereport);
             filereport = fopen(fcnvname,"w");
             /* print precinct total vote cast */
             if (lines+2 >= 60)
                  canvw(&lines,&ipage,&ppage,xprec,iward,iprec,isplt,lavcb,date,time);
             smove(longrep,0,"0",0,1);
             smove(longrep,5,"Precinct total vote cast is:",0,28);
             cvicl(vtpoll,longrep,34,10);
             lprint(filereport);
             lines += 2;
             /* print longrep of each office */
             ioffic = 0;
x300:   ioffic++;
             if (ioffic > noffic) continue;
             /* find next two valid races */
             rewind(flleft);
             rewind(flright);
             ileft = 0;
             iright = 0;
             for (j=ioffic;j<(noffic+1);j++) {
                  /* look up table entry for this office */
                  getoff(j);
                  /* determine if this office is valid for this precinct */
                  if (! lallow(j,vtprec,xprec,vtunit))
                      continue;
                  /* determine if both right & left found */
x301:           if (ileft > 0) {
                      iright = j;
                      for (m=0;m<6;m++) rtoffinf[m] = offinf[m];
                      for (m=0;m<3;m++) rtallow[m] = allow[m];
                      rtallowch[0] = allowch[0];
                      rtallowch[1] = allowch[1];
                      for (m=0;m<2;m++) rtprtinf[m] = prtinf[m];
                      };
                  if (ileft <= 0) {
                      ileft = j;
                      for (m=0;m<6;m++) lfoffinf[m] = offinf[m];
                      for (m=0;m<3;m++) lfallow[m] = allow[m];
                      lfallowch[0] = allowch[0];
                      lfallowch[1] = allowch[1];
                      for (m=0;m<2;m++) lfprtinf[m] = prtinf[m];
                      };
                  if (iright > 0) goto x309;
                  };
             /* ran out of races */
             ioffic = noffic;
             if (ileft <= 0) continue;
             goto x310;
             /* found left and right races to print */
x309:   ioffic = iright;
             goto x310;
             /* determine if page skip necessary */
x310:   lk = 2;
                  /* if (lfoffinf[DUALSTART] > 0) lk++; */
             lincr = 2 + (lfoffinf[CANDCOUNT] * lk);
             if (lincr <= 2) lincr = 4;
                  /* if (lfoffinf[DUALSTART] > 0) lincr++; */
             /* adjust for nwrite+1 write-in spaces */
             lincr = lincr + ((nwrite + 1) * lk);
             rincr = 0;
             if (iright <= 0) goto x311;
             rk = 2;
                  /* if (rtoffinf[DUALSTART] > 0) rk++; */
             rincr = 2 + (rtoffinf[CANDCOUNT] * rk);
             if (rincr <= 2) rincr = 4;
                  /* if (rtoffinf[DUALSTART] > 0) rincr++; */
             /* adjust for nwrite+1 write-in spaces */
             rincr = rincr + ((nwrite + 1) * rk);
x311:   if (lines+max(lincr,rincr) >= 60)
                  canvw(&lines,&ipage,&ppage,xprec,iward,iprec,isplt,lavcb,date,time);
             lines += max(lincr,rincr);
             /* tell user offices being handled */
             if (ileft < iright) {
                  fprintf(stderr,"Offices %d,%d\r",ileft,iright); }
             else {
                  fprintf(stderr,"Offices %d,END\r",ileft);
                  };
             /* print heading for office */
             smove(longrep,0,"0",0,1);
             gtoffi(ileft,longrep,5);
             if (iright > 0) gtoffi(iright,longrep,65);
             lprint(filereport);
             /* handle left office */
                  /* if (lfoffinf[DUALLOCATION] > 0)
                      gtdual(lfoffinf[DUALLOCATION],report,7);
                  if (lfoffinf[DUALLOCATION] > 0)
                      cprint(flleft); */
             /* handle each candidate */
x312:   lcand = lfoffinf[CANDCOUNT];
             if (lcand > 0) goto x315;
             smove(report,10,"(No candidates on ballot)",0,25);
             cprint(flleft);
             /* account for 3 write-in spaces */
x315:   if (lfoffinf[WRITEINCOUNT] > 0)
                  lcand = lcand + (nwrite + 1);
             if (lcand <= 0) goto x360;
x325:   for (k=1;k<(lcand+1);k++) {
                  gtcand((lfoffinf[CANDSTART]+k-1),report,10,report,36);
                  ivote = vote[lfoffinf[CANDSTART]+k-1];
                  if (ivote < 0L) ivote = 0L;
                  cvicl(ivote,report,44,7);
x330:           cprint(flleft);
                      /* if (lfoffinf[DUALLOCATION] <= 0)
                         continue;
                      gtdcnd((lfoffinf[DUALSTART]+k-1),report,12);
                      cprint(flleft); */
                  /* loop back for next candidate */
                  };
             goto x400;
             /* no candidates */
x360:   cprint(flleft);
             smove(report,11,"*** Warning *** No candidates",0,29);
             cprint(flright);
             /* handle right office */
x400:   smove(report,0,"99999",0,5);
             cprint(flleft);
             if (iright <= 0) goto x455;
                  /* if (rtoffinf[DUALLOCATION] > 0)
                      gtdual(rtoffinf[DUALLOCATION],report,7);
                  if (rtoffinf[DUALLOCATION] > 0)
                      cprint(flright); */
             /* handle each candidate */
x412:   rcand = rtoffinf[CANDCOUNT];
             if (rcand > 0) goto x415;
             smove(report,10,"(No candidates on ballot)",0,25);
             cprint(flright);
             /* account for 3 write-in spaces */
x415:   if (rtoffinf[WRITEINCOUNT] > 0)
                  rcand = rcand + (nwrite + 1);
             if (rcand <= 0) goto x460;
x425:   for (k=1;k<(rcand+1);k++) {
                  gtcand((rtoffinf[CANDSTART]+k-1),report,10,report,36);
                  ivote = vote[rtoffinf[CANDSTART]+k-1];
                  if (ivote < 0L) ivote = 0L;
                  cvicl(ivote,report,44,7);
x430:           cprint(flright);
                      /* if (rtoffinf[DUALLOCATION] <= 0)
                         continue;
                      gtdcnd((rtoffinf[DUALSTART]+k-1),report,12);
                      cprint(flright); */
                  /* loop back for next candidate */
                  };
             goto x455;
             /* no candidates */
x460:   cprint(flright);
             smove(report,11,"*** Warning *** No candidates",0,29);
             cprint(flright);
x455:   smove(report,0,"99999",0,5);
             cprint(flright);
             /* merge left and right for report line */
             leftsw = TRUE;
             rigtsw = TRUE;
             rewind(flleft);
             rewind(flright);
x475:   if (leftsw)
                  cnread(flleft,&eofsw);
             if (eofsw || lscomp(card,0,"99999",0,5))
                  leftsw = FALSE;
             if (leftsw) smove(longrep,0,card,0,60);
             if (rigtsw)
                  cnread(flright,&eofsw);
             if (eofsw || lscomp(card,0,"99999",0,5))
                  rigtsw = FALSE;
             if (rigtsw) smove(longrep,60,card,0,60);
             if (! leftsw && ! rigtsw) goto x300;
             if (itrim(130,longrep) <= 0) goto x475;
             smove(longrep,0,"0",0,1);
             lprint(filereport);
             goto x475;
             /* loop back for next precinct */
             };
       goto done;
done:  fprintf(stderr,"\rCanvass worksheet complete.\n");
       fprintf(stderr,"%d precincts produced out of %d total.\n",
             canvcount,nprec);
       fcloseall(); }
/* ===================== */
/** canvw **/
void canvw(lines,ipage,ppage,index,iward,iprec,isplt,lavcb,date,time)
/* print header for canvass worksheet */
int    *lines,*ipage,*ppage;
int    index,iward,iprec,isplt;
LOGICAL        lavcb;
char   date[12],time[8]; {
       (*ipage)++;
       (*ppage)++;
       /* print heading */
       smove(longrep,0,"1Weltab III",0,11);
       if (revised) {
             smove(longrep,12,"Revised Canvass Wksht",0,21); }
       else {
             smove(longrep,12,"Canvass Worksheet",0,17);
             };
       smove(longrep,35,electn,0,50);
       smove(longrep,86,date,0,12);
       smove(longrep,99,time,0,8);
       smove(longrep,112,"Page",0,4);
       if (canvstart == 1)
             cvic(*ipage,longrep,117,4);
       lprint(filereport);
       /* print identity of this precinct */
       smove(longrep,1,unitnm,0,20);
       if (iward > 0) {
             smove(longrep,25,"Ward ",0,5);
             cvic(iward,longrep,30,2);
             };
       if (lavcb) {
             smove(longrep,36,"AVCB ",0,5); }
       else {
             smove(longrep,36,"Prec ",0,5);
             };
       cvic(iprec,longrep,41,2);
       if (isplt != 0) {
             smove(longrep,45,"Split",0,5);
             cvicz(isplt,longrep,51,2);
             };
       smove(longrep,64,"(",0,1);
       smove(longrep,65,vtdate,0,12);
       smove(longrep,79,vttime,0,8);
       smove(longrep,87,")",0,1);
       smove(longrep,90,"(Index",0,6);
       cvic(index,longrep,96,4);
       smove(longrep,100,")",0,1);
       smove(longrep,103,"Precinct-page",0,13);
       cvic(*ppage,longrep,117,4);
       /* alert user when starting new precinct */
       if (*ppage <= 1) {
             blkbuf(80,report);
             smove(report,1,longrep,0,63);
             cprint(stderr);
             };
       lprint(filereport);
       /* set lines used for new page */
       *lines = 2;
       return; }
/* ===================== */
