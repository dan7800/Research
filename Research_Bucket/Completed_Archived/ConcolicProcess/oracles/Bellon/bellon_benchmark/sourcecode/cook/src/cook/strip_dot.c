/*
 *     cook - file construction tool
 *     Copyright (C) 1997 Peter Miller;
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
 * MANIFEST: functions to remove leading dots from pathnames
 */
#include <option.h>
#include <str_list.h>
#include <strip_dot.h>
/*
 * NAME
 *     strip_dot_inner
 *
 * SYNOPSIS
 *     void strip_dot_inner(string_ty **);
 *
 * DESCRIPTION
 *     The strip_dot_inner function is used to check the filename for a
 *     leading dot path element.  If there is one, it is removed, the
 *     original string free()ed, and the new string put in its place.
 */
static void strip_dot_inner _((string_ty **));
static void
strip_dot_inner(p)
       string_ty     **p; {
       char   *cp;
       size_t               len;
       string_ty     *tmp;
       cp = (*p)->str_text;
       len = (*p)->str_length;
       while (len >= 3 && cp[0] == '.' && cp[1] == '/') {
             cp += 2;
             len -= 2; }
       if (len == (*p)->str_length)
             return;
       tmp = str_n_from_c(cp, len);
       str_free(*p);
       *p = tmp; }
/*
 * NAME
 *     strip_dot
 *
 * SYNOPSIS
 *     void strip_dot(string_ty **);
 *
 * DESCRIPTION
 *     The strip_dot function is used to check the filename for a
 *     leading dot path element.  If there is one, it is removed, the
 *     original string free()ed, and the new string put in its place.
 *
 * CAVEAT
 *     This is only done if the STRIP_DOT option is enabled.  It is
 *     enabled by default, but the userr may choose to turn it off.
 */
void
strip_dot(spp)
       string_ty     **spp; {
       if (option_test(OPTION_STRIP_DOT))
             strip_dot_inner(spp); }
/*
 * NAME
 *     strip_dot_list
 *
 * SYNOPSIS
 *     void strip_dot_list(string_list_ty *);
 *
 * DESCRIPTION
 *     The strip_dot_list function is used to check each filename in
 *     the list for a leading dot path element.  If there is one, it is
 *     removed, the original string free()ed, and the new string put in
 *     its place.
 *
 * CAVEAT
 *     This is only done if the STRIP_DOT option is enabled.  It is
 *     enabled by default, but the userr may choose to turn it off.
 */
void
strip_dot_list(slp)
       string_list_ty        *slp; {
       size_t               j;
       if (option_test(OPTION_STRIP_DOT)) {
             for (j = 0; j < slp->nstrings; ++j)
                  strip_dot_inner(&slp->string[j]); } }
