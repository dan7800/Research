/** vset **/
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
main()
/* create an empty precinct vote file precid */ {
       int j,ierr;
       int iunit,indxpr,ifound;
       long iprnum;
       char date[12],time[8];
       char velect[50],telect[50];
       char fname[13];
       FILE *fopen();
       trace = FALSE;
       /* welcom(date,time); */
       setdate(date,time);
       flglobal = fopen("global.tbl","r");
       flprecid = fopen("thisprec.wtb","r");
       /* read global variables */
       rewind(flglobal);
       fscanf(flglobal,"%d %d %d %d %d %d %d %50c",
             &nunit, &nprec, &noffic, &ndual, &nwrite, &ncand, &ducand, electn);
       ifound = 0;
       /* determine precinct to set up */
       rewind(flprecid);
       fscanf(flprecid,"%d %ld %d ",&iunit,&iprnum,&indxpr);
       printf("\nPrecinct (%d,%ld,%d)\n",iunit,iprnum,indxpr);
       /* check if pfnd canceled */
       if (iunit < 0 || indxpr < 0) {
             fprintf(stderr,"*** Skipping VSET. Run canceled.\n");
             goto x890;
             };
       /* create <indxpr>.vot filename */
       smove(fname,0,"PRECxxxx.VOT",0,12);
       cvicz(indxpr,fname,4,4);
       fname[12] = '\0';
       /* if it already exists, cancel */
       ierr = access(fname,0);
       if (ierr == 0) {
             fprintf(stderr,"\n*** File '%12.12s' already exists. Run canceled.\n",fname);
             goto x900;
             };
       /* file does not exist, proceed */
       flprecvote = fopen(fname,"w");
       /* set up empty precinct record for indxpr */
       for (j=1;j<(ncand+1);j++) {
             vote[j] = -1L;
             };
       vtpoll = -1L;
       vtindx = indxpr;
       vtunit = iunit;
       vtprec = iprnum;
       setdate(vtdate,vttime);
       smove(entrer,0,"INITIAL ",0,8);
       smove(verifr,0,"INITIAL ",0,8);
       /* put status and record in <indxpr>.vot file */
       ifound = 1;
       offset = 0L;
       fseek(flprecvote,offset,0);
       fprintf(flprecvote,"%d %ld %d %d ",iunit,iprnum,indxpr,ifound);
       fprintf(flprecvote,
             ">%4d %4d %8ld %8ld %8.8s %8.8s %12.12s %8.8s ",
             vtindx,vtunit,vtprec,vtpoll,entrer,verifr,vtdate,vttime);
       for (j=1;j<(ncand+1);j++) {
             fprintf(flprecvote,"%8ld ",vote[j]);
             };
       fprintf(stderr,"Precinct %d initialized as file %12.12s.\n",indxpr,fname);
       goto x900;
       /* generate not found flag */
x890:  indxpr = 0;
       offset = 0L;
       fseek(flprecvote,offset,0);
       fprintf(flprecvote,"%d %ld %d %d ",iunit,iprnum,indxpr,ifound);
x900:  fcloseall(); }
/* ===================== */
