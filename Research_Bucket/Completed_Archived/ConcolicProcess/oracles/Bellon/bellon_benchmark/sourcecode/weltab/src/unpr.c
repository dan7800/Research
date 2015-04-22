/** unpr **/
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
#include "unitinfo.h"
main()
/* process unit and precinct information */ {
       int j,k,l;
       int lastunit;
       long int iward,iprec,iavcb,isplit;
       LOGICAL eofsw;
       char cunit[3],how[3],lastc[3];
       char date[12],time[8];
       FILE *fopen(),*fileunits,*fileprec;
       long int iabsl();
       trace = FALSE;
       blkbuf(80,report);
       welcom(date,time);
       filereport = fopen("unpr.prt","w");
       fileunits = fopen("units.dat","r");
       fileprec = fopen("precinct.dat","r");
       flunitinfo = fopen("unitinfo.tbl","w+");
       flprecinfo = fopen("precinfo.tbl","w+");
       flglobal = fopen("global.tbl","w");
       /* first process units */
       nunit = 0;
       cread(fileunits,&eofsw);
       do    {
             if (card[0] == '*') {
                  cread(fileunits,&eofsw);
                  continue;
                  };
             /* make sure that the unit code was not yet used */
             if (nunit > 1) {
                  for (j=1;j<(nunit+1);j++) {
                      offset = (long) ((j - 1) * 40);
                      fseek(flunitinfo,offset,0);
                      fscanf(flunitinfo,"%2c%20c",
                         uncode,unitnm);
                      if (lscomp(uncode,0,card,2,2)) {
                         fprintf(stderr,
                  "Code '%s' was already used for unit %d '%s'.\n",
                         uncode,j,unitnm);
                   failure(3);
                         };
                      };
                  };
             /* okay. it is unique. */
             nunit++;
             smove(uncode,0,card,2,2);
             smove(unitnm,0,card,6,20);
             offset = (long) ((nunit - 1) * 40);
             fseek(flunitinfo,offset,0);
             fprintf(flunitinfo,"%2.2s%20.20s 000 0000 0 0 0",
                  uncode,unitnm);
             cread(fileunits,&eofsw);
             }    while (! eofsw);
       /* print verification of units */
       fprintf(filereport,"\n number of units is %d.\n",nunit);
       if (nunit <= 0) failure(4);
       for (j=1;j<(nunit+1);j++) {
             offset = (long) ((j - 1) * 40);
             fseek(flunitinfo,offset,0);
             fscanf(flunitinfo,"%2c%20c",uncode,unitnm);
             smove(report,3,unitnm,0,20);
             smove(report,26,uncode,0,2);
             cprint(filereport);
             };
       /* next process precinct information */
       nprec = 0;
       smove(lastc,0,"  ",0,2);
       lastc[2] = '\0';
       lastunit = 0;
       /* bypass heading cards */
       cread(fileprec,&eofsw);
       cread(fileprec,&eofsw);
       while (fscanf(fileprec,
             "%s %ld %ld %ld %ld %d %s %d %d %d %d %d %d %d %d %d %d %d",
             cunit,&iward,&iprec,&isplit,&iavcb,&regist,
             how,&congr,&senate,&distct,&college,
             &repre[0],&repre[1],
             &commis[0],&commis[1],&commis[2],&commis[3],
             &commis[4])  != EOF) {
             /* precinct unit */
             if (strlen(cunit) <= 0) break;
             if (strlen(cunit) > 2) failure(4);
             nprec++;
             if (fulltrace)
                  printf("cunit (%s), nprec %d.\n",cunit,nprec);
             fprintf(stderr,"%d precincts processed.\r",nprec);
             /* determine if this is a new precinct */
             if (lscomp(lastc,0,cunit,0,2)) {
                  /* same unit as previous */
                  unitno = lastunit; }
             else {
                  /* precinct is for a new unit */
                  /* first rewrite the old unit */
                  if (lastunit > 0) {
                      offset = (long) ((lastunit - 1) * 40);
                      fseek(flunitinfo,offset,0);
                      fprintf(flunitinfo,"%2.2s%20.20s",
                         uncode,unitnm);
                      fprintf(flunitinfo,
                      " %3d %4d %1d %1d %1d",
                      untnprec,untstart,untwards,untavcbs,untsplit);
                      };
                  /* now record the precinct */
                  for (j=1;j<(nunit+1);j++) {
                      offset = (long) ((j - 1) * 40);
                      fseek(flunitinfo,offset,0);
                      fscanf(flunitinfo,"%2c",uncode);
                      if (lscomp(uncode,0,cunit,0,2)) goto fndunit;
                      };
                  /* error - unit not found */
                  failure(8);
                  /* found new unit */
fndunit:             unitno = j;
                  lastunit = j;
                  smove(lastc,0,cunit,0,2);
                  fscanf(flunitinfo,
                  "%20c %d %d %d %d %d",
             unitnm,&untnprec,&untstart,&untwards,&untavcbs,&untsplit);
                  if (untstart != 0) failure(18);
                  untstart = nprec;
                  };
             /* precinct identification */
             untnprec++;
             if (trace) fprintf(stderr,
             "unitno %ld, iward %ld, iprec %ld, isplit %ld, iavcb %ld\n"
                  ,unitno,iward,iprec,isplit,iavcb);
             if (iprec <= 0L && iavcb <= 0L) failure(32);
             if (iward != 0L)
                  untwards = TRUE;
             if (iprec != 0L && isplit != 0L) {
                  precno = (isplit * 10000L) + (iward * 100L) + iprec;
                  untsplit = TRUE;
                  if (iavcb != 0) failure(33);
                  };
             if (iprec != 0L && isplit == 0L && iavcb == 0L)
                  precno = (iward * 100L) + iprec;
             if (iprec == 0L && iavcb != 0L) {
                  precno = - ((iward * 100L) + iavcb);
                  untavcbs = EXISTS;
                  if (iward != 0) untavcbs = BYWARD;
                  if (isplit != 0) failure(34);
                  };
             /* type of precinct voting */
             if (how[0] == 'M') vothow = MACHINE;
             if (how[0] == 'C') vothow = PUNCHCARD;
             if (how[0] == 'P') vothow = PAPER;
             /* printf("how %s, vothow %d.\n",how,vothow); */
             /* precinct congress, senate, district court, college */
             /* printf("congress %d, senate %d, distct %d, college %d.\n",
                  congr,senate,distct,college); */
             if ((congr * senate * distct * college) <= 0
                  &&  (iavcb == 0)) failure(12);
             /* state representative */
             /* printf("state rep %d, %d.\n",repre[0],repre[1]); */
             if ((repre[0] + repre[1]) <= 0
                  && (iavcb == 0)) failure(16);
             /* county commissioner */
             /* printf("comm %d, %d, %d, %d, %d.\n",
                  commis[0],commis[1],commis[2],commis[3],commis[4]); */
             if ((commis[0] + commis[1] + commis[2] +
                  commis[3] + commis[4]) <= 0
                  && (iavcb == 0)) failure(20);
             /* registration */
             /* printf("registration %d.\n\n",regist); */
             offset = (long) ((nprec - 1) * 80);
             fseek(flprecinfo,offset,0);
             fprintf(flprecinfo,
       "%4d %8ld %6d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d %4d ",
                  unitno,precno,regist,congr,senate,distct,
                  college,vothow,
                  repre[0],repre[1],commis[0],commis[1],commis[2],
                  commis[3],commis[4]);
             fprintf(flprecinfo,
                  "0 0 0 0 0 0 0 0 0 0 ");
             };
       fprintf(stderr,"\n");
       /* rewrite last unit information */
       if (lastunit > 0) {
             offset = (long) ((lastunit - 1) * 40);
             fseek(flunitinfo,offset,0);
             fprintf(flunitinfo,"%2.2s%20.20s",
                  uncode,unitnm);
             fprintf(flunitinfo,
             " %3d %4d %1d %1d %1d",
             untnprec,untstart,untwards,untavcbs,untsplit);
             };
       /* verify precincts */
       fprintf(filereport,"\n number of precincts is %d.\n",nprec);
       if (nprec <= 0) failure(24);
       for (j=1;j<(nprec+1);j++) {
             offset = (long) ((j - 1) * 80);
             fseek(flprecinfo,offset,0);
             fscanf(flprecinfo,
                  "%d %ld %d %d %d %d %d %d %d %d %d %d %d %d %d",
                  &unitno,&precno,&regist,&congr,&senate,&distct,
                  &college,&vothow,
                  &repre[0],&repre[1],&commis[0],&commis[1],&commis[2],
                  &commis[3],&commis[4]);
             /* printf("unitno %d.     precno %ld.\n\n",unitno,precno); */
             /* printf("registration %d.\n\n",regist); */
             fprintf(stderr,"reporting on precinct %d.\r",j);
             fprintf(filereport,"\n");
             offset = (long) ((unitno - 1) * 40);
             fseek(flunitinfo,offset,0);
             fscanf(flunitinfo,"%2c%20c",uncode,unitnm);
             smove(report,2,unitnm,0,20);
             isplit = iabsl(precno) / 10000L;
             iward = iabsl(precno % 10000L) / 100L;
             iprec = (precno % 100L);
             if (untwards) {
                  smove(report,26,"ward=",0,5);
                  cvic((short) iward,report,32,2);
                  };
             if (iprec >= 0) {
                  smove(report,36,"prec=",0,5);
                  cvic((short) iprec,report,42,2);
                  };
             if (isplit != 0) {
                  smove(report,46,"split=",0,6);
                  cvic((short) isplit,report,53,2);
                  };
             if (iprec < 0) {
                  smove(report,36,"*** avcb=",0,9);
                  cvic((short) iabsl(iprec),report,46,2);
                  };
             if (vothow == MACHINE)
                  smove(report,59,"machine",0,7);
             if (vothow == PUNCHCARD)
                  smove(report,59,"punchcard",0,9);
             if (vothow == PAPER)
                  smove(report,59,"paper",0,5);
             cprint(filereport);
             fprintf(filereport,
             "     index = %d  (code %ld)        registration = %d\n",
                  j,precno,regist);
             fprintf(filereport,
             "     cong= %d  senate= %d  distct= %d  college= %d\n",
                  congr,senate,distct,college);
             fprintf(filereport,
             "     repre= %d %d  comm= %d %d %d %d %d\n",
                  repre[0],repre[1],
                  commis[0],commis[1],commis[2],
                  commis[3],commis[4]);
             };
       fprintf(stderr,"\n");
       /* verify units */
       fprintf(filereport,"\fThere are %d units:\n\n",nunit);
       if (nunit <= 0) failure(4);
       for (j=1;j<(nunit+1);j++) {
             offset = (long) ((j - 1) * 40);
             fseek(flunitinfo,offset,0);
             fscanf(flunitinfo,"%2c%20c",uncode,unitnm);
             smove(report,3,unitnm,0,20);
             smove(report,26,uncode,0,2);
             cprint(filereport);
             fscanf(flunitinfo,
                  " %d %d %d %d %d",
                  &untnprec,&untstart,&untwards,&untavcbs,&untsplit);
             fprintf(filereport,"     %d precincts beginning at %d.\n",
                  untnprec,untstart);
             fprintf(filereport,"     ");
             if (untwards)
                  fprintf(filereport,"Wards. ");
             if (untsplit)
                  fprintf(filereport,"Splits. ");
             if (untavcbs == EXISTS)
                  fprintf(filereport,"AVCBS. ");
             if (untavcbs == BYWARD)
                  fprintf(filereport,"AVCBS by Ward. ");
             fprintf(filereport,"\n\n");
             };
       /* write out global table file */
       offset = 0L;
       fseek(flglobal,offset,0);
       fprintf(flglobal,"%4d %4d %4d %4d %4d %4d %4d ",
             nunit,nprec,0,0,-1,0,0);
       fprintf(stderr,"global table file written.\n");
       fcloseall(); }
/* ===================== */
