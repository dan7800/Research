/** fixw **/
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
/* correct mistakes in candidate names, particularly due to write-ins */ {
       int j;
       int len;
       LOGICAL eofsw;
       char date[12],time[8];
       float pcnt,total;
       FILE *fopen();
       trace = FALSE;
       blkbuf(80,report);
       welcom(date,time);
       fprintf(stderr,"\nFix Names of Candidates in the Tables\n");
       flglobal = fopen("global.tbl","r");
       flcand = fopen("candidat.tbl","r+");
       /* read global variables */
       rewind(flglobal);
       fscanf(flglobal,"%d %d %d %d %d %d %d %50c",
             &nunit, &nprec, &noffic, &ndual, &nwrite, &ncand, &ducand, electn);
       /* query for change on each candidate */
       for (j=1;j<(ncand+1);j++) {
             gtcand(j,report,0);
             report[20] = '\0';
x100:   fprintf(stderr,"\nChange %d '%s' ?\n",j,report);
             userreply(&len,&eofsw);
             if (eofsw) continue;
             if (len == 4 && (card[0] = 'D' || card[0] == 'd')) {
                  if (card[1] != 'O' && card[1] != 'o') goto x102;
                  if (card[2] != 'N' && card[2] != 'n') goto x102;
                  if (card[3] != 'E' && card[3] != 'e') goto x102;
                  goto x900;
                  };
             if (len < 1) goto x100;
x102:   card[0] = toupper(card[0]);
             if (card[0] != 'Y' && card[0] != 'N'
              && card[0] != '7' && card[0] != '0')
                  goto x100;
             if (card[0] == 'N' || card[0] == '0')
                  continue;
             /* change the name */
x110:   fprintf(stderr,"\nOld name: '%s'\nEnter new name:\n",report);
             userreply(&len,&eofsw);
             if (eofsw) goto x110;
             if (len <= 0 || card[0] == ' ') goto x110;
             smove(report,0,card,0,20);
             report[20] = '\0';
             fprintf(stderr,"\nName is now '%s'.\n",report);
             if (! confrm()) goto x110;
             /* change the name table */
             rewind(flcand);
             wtcand(j,report,0);
             rewind(flcand);
             /* loop back for next candidate */
             };
x900:  fprintf(stderr,"\n\nCandidate table files re-written.\n");
       fprintf(stderr,"\nRun CAND to review the changed name table.\n");
       fcloseall(); }
/* ===================== */
