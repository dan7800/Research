/*
 *     cook - file construction tool
 *     Copyright (C) 1990, 1991, 1992, 1993, 1994, 1997, 1998 Peter Miller;
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
 * MANIFEST: interface definition for cook/lex.c
 */
#ifndef COOK_LEX_H
#define COOK_LEX_H
#include <main.h>
#include <str.h>
int hashline_lex _((void));
void hashline_lex_reset _((void));
int lex_cur_line _((void));
int lex_mode _((int));
int parse_lex _((void));
string_ty *lex_cur_file _((void));
void lex_lino_set _((string_ty *, string_ty *));
void lex_close _((void));
void lex_error _((struct sub_context_ty *, char *));
void parse_error _((char *));
void lex_warning _((struct sub_context_ty *, char *));
void lex_initialize _((void));
void lex_open _((string_ty *, string_ty *));
void lex_open_include _((string_ty *, string_ty *));
void lex_passing _((int));
void lex_trace _((char*, ...));
/*
 *  lex_mode() arguments
 */
#define LM_NORMAL   0
#define LM_DATA            1
#define LM_SQUOTE   2
#define LM_DQUOTE   3
#endif /* COOK_LEX_H */
