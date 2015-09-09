/* common/config.h.  Generated automatically by configure.  */
/*
 *     cook - file construction tool
 *     Copyright (C) 1995, 1997, 2001 Peter Miller;
 *     All rights reserved.
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111, USA.
 *
 * MANIFEST: template common/config.h file
 *
 * @configure_input@
 */
#ifndef COMMON_CONFIG_H
#define COMMON_CONFIG_H
/* common/config.h.in.  Generated automatically from etc/configure.in by autoheader.  */
/* Define if on AIX 3.
   System headers sometimes define this.
   We just want to avoid a redefinition error message.  */
#ifndef _ALL_SOURCE
/* #undef _ALL_SOURCE */
#endif
/* Define to empty if the keyword does not work.  */
/* #undef const */
/* Define if you support file names longer than 14 characters.  */
#define HAVE_LONG_FILE_NAMES 1
/* Define if on MINIX.  */
/* #undef _MINIX */
/* Define if the system does not provide POSIX.1 features except
   with this defined.  */
/* #undef _POSIX_1_SOURCE */
/* Define if you need to in order for stat and other things to work.  */
/* #undef _POSIX_SOURCE */
/* Define as the return type of signal handlers (int or void).  */
#define RETSIGTYPE void
/* Define to `unsigned' if <sys/types.h> doesn't define.  */
/* #undef size_t */
/* Define if you have the ANSI C header files.  */
#define STDC_HEADERS 1
/* Define if you can safely include both <sys/time.h> and <time.h>.  */
#define TIME_WITH_SYS_TIME 1
/*
 * Define this symbol of your system has <stdarg.h> AND it works.
 */
/* #undef HAVE_STDARG_H */
/*
 * Define this symbol of your system has iswprint() AND it works.
 * (GNU libc v2 had a bug.)
 */
#define HAVE_ISWPRINT 1
/*
 * Define this symbol of your system has the wint_t type defined.
 * It is usually in <stddef.h> or <wctype.h>
 */
#define HAVE_WINT_T 1
/*
 * Set this to a suitable argument for the getpgrp function to discover
 * the process group of the current process.
 */
#define CONF_getpgrp_arg 
/*
 * Set this to be the absolute path of a Bourne shell
 * which understands functions.
 */
#define CONF_SHELL "/bin/sh"
/*
 * Set this to be the name of a BSD remote shell command (or equivalent).
 */
#define CONF_REMOTE_SHELL "rsh"
/*
 * Define to the name of the SCCS `get' command.
 * (Used only by make2cook)
 */
#define SCCS_GET "get"
/*
 * define this symbol if your system as <sys/ioctl.h> AND it
 * defines TIOCGWINSZ and struct winsize.
 */
/* #undef HAVE_winsize_SYS_IOCTL_H */
/*
 * define this symbol if your system as <termios.h> AND it
 * defines TIOCGWINSZ and struct winsize.
 */
#define HAVE_winsize_TERMIOS_H 1
/*
 * Define this symbol if your system has
 * the tm_zone field in the tm struct defined in <time.h>
 */
/* #undef HAVE_tm_zone */
/* The number of bytes in a int.  */
#define SIZEOF_INT 4
/* The number of bytes in a long.  */
#define SIZEOF_LONG 4
/* The number of bytes in a short.  */
#define SIZEOF_SHORT 2
/* Define if you have the gethostname function.  */
#define HAVE_GETHOSTNAME 1
/* Define if you have the getpgrp function.  */
#define HAVE_GETPGRP 1
/* Define if you have the getrusage function.  */
#define HAVE_GETRUSAGE 1
/* Define if you have the gettext function.  */
#define HAVE_GETTEXT 1
/* Define if you have the iswctype function.  */
#define HAVE_ISWCTYPE 1
/* Define if you have the mblen function.  */
#define HAVE_MBLEN 1
/* Define if you have the pathconf function.  */
#define HAVE_PATHCONF 1
/* Define if you have the regcomp function.  */
#define HAVE_REGCOMP 1
/* Define if you have the setlocale function.  */
#define HAVE_SETLOCALE 1
/* Define if you have the strcasecmp function.  */
#define HAVE_STRCASECMP 1
/* Define if you have the strerror function.  */
#define HAVE_STRERROR 1
/* Define if you have the strftime function.  */
#define HAVE_STRFTIME 1
/* Define if you have the strsignal function.  */
#define HAVE_STRSIGNAL 1
/* Define if you have the strtol function.  */
#define HAVE_STRTOL 1
/* Define if you have the tcgetpgrp function.  */
#define HAVE_TCGETPGRP 1
/* Define if you have the uname function.  */
#define HAVE_UNAME 1
/* Define if you have the wait3 function.  */
#define HAVE_WAIT3 1
/* Define if you have the wait4 function.  */
#define HAVE_WAIT4 1
/* Define if you have the wcslen function.  */
#define HAVE_WCSLEN 1
/* Define if you have the <ar.h> header file.  */
#define HAVE_AR_H 1
/* Define if you have the <dirent.h> header file.  */
#define HAVE_DIRENT_H 1
/* Define if you have the <fcntl.h> header file.  */
#define HAVE_FCNTL_H 1
/* Define if you have the <iso646.h> header file.  */
/* #undef HAVE_ISO646_H */
/* Define if you have the <libgettext.h> header file.  */
/* #undef HAVE_LIBGETTEXT_H */
/* Define if you have the <libintl.h> header file.  */
#define HAVE_LIBINTL_H 1
/* Define if you have the <limits.h> header file.  */
#define HAVE_LIMITS_H 1
/* Define if you have the <locale.h> header file.  */
#define HAVE_LOCALE_H 1
/* Define if you have the <memory.h> header file.  */
#define HAVE_MEMORY_H 1
/* Define if you have the <mntent.h> header file.  */
/* #undef HAVE_MNTENT_H */
/* Define if you have the <ndir.h> header file.  */
/* #undef HAVE_NDIR_H */
/* Define if you have the <regex.h> header file.  */
#define HAVE_REGEX_H 1
/* Define if you have the <rxposix.h> header file.  */
/* #undef HAVE_RXPOSIX_H */
/* Define if you have the <stddef.h> header file.  */
#define HAVE_STDDEF_H 1
/* Define if you have the <stdlib.h> header file.  */
#define HAVE_STDLIB_H 1
/* Define if you have the <string.h> header file.  */
#define HAVE_STRING_H 1
/* Define if you have the <sys/dir.h> header file.  */
/* #undef HAVE_SYS_DIR_H */
/* Define if you have the <sys/ndir.h> header file.  */
/* #undef HAVE_SYS_NDIR_H */
/* Define if you have the <sys/utsname.h> header file.  */
#define HAVE_SYS_UTSNAME_H 1
/* Define if you have the <unistd.h> header file.  */
#define HAVE_UNISTD_H 1
/* Define if you have the <utime.h> header file.  */
#define HAVE_UTIME_H 1
/* Define if you have the <wchar.h> header file.  */
#define HAVE_WCHAR_H 1
/* Define if you have the <wctype.h> header file.  */
#define HAVE_WCTYPE_H 1
/* Define if you have the <widec.h> header file.  */
#define HAVE_WIDEC_H 1
/* Define if you have the intl library (-lintl).  */
#define HAVE_LIBINTL 1
/* Define if you have the rx library (-lrx).  */
/* #undef HAVE_LIBRX */
/* Define if you have the socket library (-lsocket).  */
#define HAVE_LIBSOCKET 1
/* Define if you have the w library (-lw).  */
#define HAVE_LIBW 1
/* Define if you have the w32 library (-lw32).  */
/* #undef HAVE_LIBW32 */
/*
 * There is more to do, but we need to insulate it from config.status,
 * because it screws up the #undef lines.  They are all implications of
 * the above information, so there is no need for you to edit the file,
 * if you are configuring Aegis manually.
 */
#include <config.messy.h>
#endif /* COMMON_CONFIG_H */
