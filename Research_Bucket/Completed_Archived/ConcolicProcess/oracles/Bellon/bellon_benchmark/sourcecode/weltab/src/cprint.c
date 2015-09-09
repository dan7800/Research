/** cprint **/
#include <stdio.h>
#define LINESPERPAGE 60
main()
/* print c program indented, with each subprogram beginning a new page */ {
       int c,nlines,npages,nsubr;
       long int ncin,ncout,totalines,linesin;
       ncin = 0;
       ncout = 0;
       nsubr = 0;
       npages = 1;
       totalines = 1;
       nlines = 1;
       linesin = 1;
       fprintf(stderr,"CPRINT:\nRoutine %d\r",nsubr);
       printf("  %-5ld ",totalines);
       while( (c=getchar()) != EOF) {
             ncin++;
             if (c == '\n') {
                  /* printf("Char was linend\n"); */
LINEND:                     nlines++; linesin++; totalines++;
                  putchar(c); ncout++;
                  if (nlines > LINESPERPAGE) {
                      nlines = 1;
                   printf("\f \n");  ncout++;
                      npages++;
                      };
                  c = getchar();
                  ncin++;
                  switch(c) {
                      case '\n': {
                         /* printf("Char was 2nd linend\n"); */
                         printf("  %-5ld",totalines);
                         ncout = ncout + 7;
                         goto LINEND;
                         };
                      case EOF: {
                         /* printf("Char was EOF\n"); */
                      putchar('\n');   ncout++;
                         nlines++; totalines++;
                         goto EXIT;
                         };
                    case '/': {
                         /* printf("Char was /\n"); */
                         c = getchar();
                         if (c == '*') {
                           c = getchar();
                           if (c == '*') {
                            /* found routine break */
                            nsubr++;
                            fprintf(stderr,"Routine %d\r",nsubr);
                            if (nlines != 1) {
                                    printf("\f \n");       ncout++;
                                    npages++;
                                    nlines = 1;
                                    };
/* could pick up routine name for printing on top of later pages */
                            printf("  %-5ld /**",totalines);
                              ncout = ncout + 11;
                            break; }
                           else {
                            printf("  %-5ld /*",totalines);
                            putchar(c);
                            ncout = ncout + 11;
                              break;
                            }; }
                         else {
                           putchar('/');
                           putchar(c);
                      ncout = ncout + 2;
                           break;
                           };
                         };
                      case ' ': {
                         /* printf("Char was SP\n"); */
                         printf("  %-5ld  ",totalines);
                         ncout = ncout + 9;
                         break;
                         };
                      default: {
                         /* printf("Char was %d %c\n",c,c); */
                         printf("  %-5ld ",totalines);
                         putchar(c);
                         ncout = ncout + 9;
                         break;
                         };
                      }; }
             else {
                  /* printf("Char was %d %c\n",c,c); */
                  putchar(c); ncout++;
                  };
             };
EXIT:  if (nlines != 1) {
             printf("\f \n");     ncout++;
             };
       fprintf(stderr,"\n%d pages printed.\n",npages);
       fprintf(stderr,"%ld chars in; %ld chars out.\n",ncin,ncout);
       fprintf(stderr,"%ld lines in; %ld lines out.\n",linesin,totalines);
       fprintf(stderr,"%d routines printed.\n",nsubr+1); }
