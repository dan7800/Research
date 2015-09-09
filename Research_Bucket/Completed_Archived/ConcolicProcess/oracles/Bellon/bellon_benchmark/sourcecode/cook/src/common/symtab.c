/*
 *     cook - file construction tool
 *     Copyright (C) 1990, 1991, 1992, 1993, 1994, 1997, 2001 Peter Miller;
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
 * MANIFEST: functions to manipulate symbol tables
 */
#include <error.h> /* for debugging */
#include <fstrcmp.h>
#include <mem.h>
#include <symtab.h>
#include <trace.h>
symtab_ty *
symtab_alloc(size)
       int     size; {
       symtab_ty     *stp;
       str_hash_ty   j;
       trace(("symtab_alloc(size = %d)\n{\n"/*}*/, size));
       stp = mem_alloc(sizeof(symtab_ty));
       stp->reap = 0;
       stp->hash_modulus = 1 << 2; /* MUST be a power of 2 */
       while (stp->hash_modulus < size)
             stp->hash_modulus <<= 1;
       stp->hash_cutover = stp->hash_modulus;
       stp->hash_split = stp->hash_modulus - stp->hash_cutover;
       stp->hash_cutover_mask = stp->hash_cutover - 1;
       stp->hash_cutover_split_mask = (stp->hash_cutover * 2) - 1;
       stp->hash_load = 0;
       stp->hash_table =
             mem_alloc(stp->hash_modulus * sizeof(symtab_row_ty *));
       for (j = 0; j < stp->hash_modulus; ++j)
             stp->hash_table[j] = 0;
       trace(("return %08lX;\n", (long)stp));
       trace((/*{*/"}\n"));
       return stp; }
void
symtab_free(stp)
       symtab_ty     *stp; {
       str_hash_ty   j;
       trace(("symtab_free(stp = %08lX)\n{\n"/*}*/, (long)stp));
       for (j = 0; j < stp->hash_modulus; ++j) {
             symtab_row_ty        **rpp;
             rpp = &stp->hash_table[j];
             while (*rpp) {
                  symtab_row_ty       *rp;
                  rp = *rpp;
                  *rpp = rp->overflow;
                  if (stp->reap)
                      stp->reap(rp->data);
                  str_free(rp->key);
                  mem_free(rp); } }
       mem_free(stp->hash_table);
       mem_free(stp);
       trace((/*{*/"}\n")); }
/*
 * NAME
 *     split - reduce symbol table load
 *
 * SYNOPSIS
 *     void split(symtab_ty);
 *
 * DESCRIPTION
 *     The split function is used to split symbols in the bucket indicated by
 *     the split point.  The symbols are split between that bucket and the one
 *     after the current end of the table.
 *
 * CAVEAT
 *     It is only sensable to do this when the symbol table load exceeds some
 *     reasonable threshold.  A threshold of 80% is suggested.
 */
static void split _((symtab_ty *));
static void
split(stp)
       symtab_ty     *stp; {
       symtab_row_ty *p;
       symtab_row_ty **ipp;
       symtab_row_ty *p2;
       str_hash_ty   index;
       /*
        * get the list to be split across buckets
        */
       trace(("split(stp = %08lX)\n{\n"/*}*/, (long)stp));
       p = stp->hash_table[stp->hash_split];
       stp->hash_table[stp->hash_split] = 0;
       /*
        * increase the modulus by one
        */
       stp->hash_modulus++;
       stp->hash_table =
             mem_change_size
             (
                  stp->hash_table,
                  stp->hash_modulus * sizeof(symtab_row_ty *)
             );
       stp->hash_table[stp->hash_modulus - 1] = 0;
       stp->hash_split = stp->hash_modulus - stp->hash_cutover;
       if (stp->hash_split >= stp->hash_cutover) {
             stp->hash_cutover = stp->hash_modulus;
             stp->hash_split = 0;
             stp->hash_cutover_mask = stp->hash_cutover - 1;
             stp->hash_cutover_split_mask = (stp->hash_cutover * 2) - 1; }
       /*
        * now redistribute the list elements
        *
        * It is important to preserve the order of the links because
        * they can be push-down stacks, and to simply add them to the
        * head of the list will reverse the order of the stack!
        */
       while (p) {
             p2 = p;
             p = p2->overflow;
             p2->overflow = 0;
             index = p2->key->str_hash & stp->hash_cutover_mask;
             if (index < stp->hash_split) {
                  index =
                      (
                         p2->key->str_hash
                      &
                         stp->hash_cutover_split_mask
                      ); }
             for
             (
                  ipp = &stp->hash_table[index];
                  *ipp;
                  ipp = &(*ipp)->overflow
             )
                  ;
             *ipp = p2; }
       trace((/*{*/"}\n")); }
/*
 * NAME
 *     symtab_query - search for a variable
 *
 * SYNOPSIS
 *     int symtab_query(symtab_ty *, string_ty *key);
 *
 * DESCRIPTION
 *     The symtab_query function is used to reference a variable.
 *
 * RETURNS
 *     If the variable has been defined, the function returns a non-zero value
 *     and the value is returned through the 'value' pointer.
 *     If the variable has not been defined, it returns zero,
 *     and 'value' is unaltered.
 */
void *
symtab_query(stp, key)
       symtab_ty     *stp;
       string_ty     *key; {
       str_hash_ty   index;
       symtab_row_ty *p;
       void   *result;
       trace(("symtab_query(stp = %08lX, key = \"%s\")\n{\n"/*}*/,
             (long)stp, key->str_text));
       result = 0;
       index = key->str_hash & stp->hash_cutover_mask;
       if (index < stp->hash_split)
             index = key->str_hash & stp->hash_cutover_split_mask;
       for (p = stp->hash_table[index]; p; p = p->overflow) {
             if (str_equal(key, p->key)) {
                  result = p->data;
                  break; } }
       trace(("return %08lX;\n", (long)result));
       trace((/*{*/"}\n"));
       return result; }
static void
symtab_query_fuzzy_inner _((symtab_ty *, string_ty *, double *, string_ty **,
       void **));
static void
symtab_query_fuzzy_inner(stp, key, best_weight, best_key, best_result)
       symtab_ty     *stp;
       string_ty     *key;
       double               *best_weight;
       string_ty     **best_key;
       void   **best_result; {
       str_hash_ty   index;
       symtab_row_ty *p;
       trace(("symtab_query_fuzzy_inner(stp = %08lX, key = \"%s\")\n{\n",
             (long)stp, key->str_text));
       for (index = 0; index < stp->hash_modulus; ++index) {
             for (p = stp->hash_table[index]; p; p = p->overflow) {
                  double      w;
                  w = fstrcmp(key->str_text, p->key->str_text);
                  if (w > *best_weight) {
                      *best_key = p->key;
                      *best_weight = w;
                      *best_result = p->data; } } }
       trace(("}\n")); }
void *
symtab_query_fuzzy(stp, key, key_used)
       symtab_ty     *stp;
       string_ty     *key;
       string_ty     **key_used; {
       double               best_weight;
       void   *best_result;
       string_ty     *best_key;
       trace(("symtab_query(stp = %08lX, key = \"%s\")\n{\n",
             (long)stp, key->str_text));
       best_weight = 0.6;
       best_result = 0;
       best_key = 0;
       symtab_query_fuzzy_inner
       (
             stp,
             key,
             &best_weight,
             &best_key,
             &best_result
       );
       if (key_used && best_key)
             *key_used = best_key;
       trace(("return %08lX;\n", (long)best_result));
       trace(("}\n"));
       return best_result; }
void *
symtab_query_fuzzyN(stp_table, stp_length, key, key_used)
       symtab_ty     **stp_table;
       size_t               stp_length;
       string_ty     *key;
       string_ty     **key_used; {
       double               best_weight;
       void   *best_result;
       string_ty     *best_key;
       size_t               j;
       trace(("symtab_query_fuzzyN(key = \"%s\")\n{\n", key->str_text));
       best_weight = 0.6;
       best_result = 0;
       best_key = 0;
       for (j = 0; j < stp_length; ++j) {
             symtab_query_fuzzy_inner
             (
                  stp_table[j],
                  key,
                  &best_weight,
                  &best_key,
                  &best_result
             ); }
       if (key_used && best_key)
             *key_used = best_key;
       trace(("return %08lX;\n", (long)best_result));
       trace(("}\n"));
       return best_result; }
/*
 * NAME
 *     symtab_assign - assign a variable
 *
 * SYNOPSIS
 *     void symtab_assign(symtab_ty *, string_ty *key, void *data);
 *
 * DESCRIPTION
 *     The symtab_assign function is used to assign
 *     a value to a given variable.
 *
 * CAVEAT
 *     The name is copied, the data is not.
 */
void
symtab_assign(stp, key, data)
       symtab_ty     *stp;
       string_ty     *key;
       void   *data; {
       str_hash_ty   index;
       symtab_row_ty *p;
       trace(("symtab_assign(stp = %08lX, key = \"%s\", data = %08lX)\n\
{\n"/*}*/, (long)stp, key->str_text, (long)data));
       index = key->str_hash & stp->hash_cutover_mask;
       if (index < stp->hash_split)
             index = key->str_hash & stp->hash_cutover_split_mask;
       for (p = stp->hash_table[index]; p; p = p->overflow) {
             if (str_equal(key, p->key)) {
                  trace(("modify existing entry\n"));
                  if (stp->reap)
                      stp->reap(p->data);
                  p->data = data;
                  goto done; } }
       trace(("new entry\n"));
       p = mem_alloc(sizeof(symtab_row_ty));
       p->key = str_copy(key);
       p->overflow = stp->hash_table[index];
       p->data = data;
       stp->hash_table[index] = p;
       stp->hash_load++;
       while (stp->hash_load * 10 >= stp->hash_modulus * 8)
             split(stp);
       done:
       trace((/*{*/"}\n")); }
/*
 * NAME
 *     symtab_assign_push - assign a variable
 *
 * SYNOPSIS
 *     void symtab_assign_push(symtab_ty *, string_ty *key, void *data);
 *
 * DESCRIPTION
 *     The symtab_assign function is used to assign
 *     a value to a given variable.
 *     Any previous value will be obscured until this one
 *     is deleted with symtab_delete.
 *
 * CAVEAT
 *     The name is copied, the data is not.
 */
void
symtab_assign_push(stp, key, data)
       symtab_ty     *stp;
       string_ty     *key;
       void   *data; {
       str_hash_ty   index;
       symtab_row_ty *p;
       trace(("symtab_assign_push(stp = %08lX, key = \"%s\", data = %08lX)\n\
{\n"/*}*/, (long)stp, key->str_text, (long)data));
       index = key->str_hash & stp->hash_cutover_mask;
       if (index < stp->hash_split)
             index = key->str_hash & stp->hash_cutover_split_mask;
       p = mem_alloc(sizeof(symtab_row_ty));
       p->key = str_copy(key);
       p->overflow = stp->hash_table[index];
       p->data = data;
       stp->hash_table[index] = p;
       stp->hash_load++;
       while (stp->hash_load * 10 >= stp->hash_modulus * 8)
             split(stp);
       trace((/*{*/"}\n")); }
/*
 * NAME
 *     symtab_delete - delete a variable
 *
 * SYNOPSIS
 *     void symtab_delete(string_ty *name, symtab_class_ty class);
 *
 * DESCRIPTION
 *     The symtab_delete function is used to delete variables.
 *
 * CAVEAT
 *     The name is freed, the data is reaped.
 *     (By default, reap does nothing.)
 */
void
symtab_delete(stp, key)
       symtab_ty     *stp;
       string_ty     *key; {
       str_hash_ty   index;
       symtab_row_ty **pp;
       trace(("symtab_delete(stp = %08lX, key = \"%s\")\n{\n"/*}*/,
             (long)stp, key->str_text));
       index = key->str_hash & stp->hash_cutover_mask;
       if (index < stp->hash_split)
             index = key->str_hash & stp->hash_cutover_split_mask;
       pp = &stp->hash_table[index];
       for (;;) {
             symtab_row_ty        *p;
             p = *pp;
             if (!p)
                  break;
             if (str_equal(key, p->key)) {
                  if (stp->reap)
                      stp->reap(p->data);
                  str_free(p->key);
                  *pp = p->overflow;
                  mem_free(p);
                  stp->hash_load--;
                  break; }
             pp = &p->overflow; }
       trace((/*{*/"}\n")); }
/*
 * NAME
 *     symtab_dump - dump symbol table
 *
 * SYNOPSIS
 *     void symtab_dump(symtab_ty *stp, char *caption);
 *
 * DESCRIPTION
 *     The symtab_dump function is used to dump the contents of the
 *     symbol table.  The caption will be used to indicate why the
 *     symbol table was dumped.
 *
 * CAVEAT
 *     This function is only available when symbol DEBUG is defined.
 */
#ifdef DEBUG
void
symtab_dump(stp, caption)
       symtab_ty     *stp;
       char   *caption; {
       int     j;
       symtab_row_ty *p;
       error_raw("symbol table %s = {", caption);
       for (j = 0; j < stp->hash_modulus; ++j) {
             for (p = stp->hash_table[j]; p; p = p->overflow) {
                  error_raw
                  (
                      "key = \"%s\", data = %08lX",
                      p->key->str_text,
                      (long)p->data
                  ); } }
       error_raw("}"); }
#endif
void
symtab_walk(stp, func, arg)
       symtab_ty     *stp;
       void   (*func)_((symtab_ty *, string_ty *, void *, void *));
       void   *arg; {
       long   j;
       symtab_row_ty *rp;
       trace(("symtab_walk(stp = %08lX)\n{\n"/*}*/, (long)stp));
       for (j = 0; j < stp->hash_modulus; ++j)
             for (rp = stp->hash_table[j]; rp; rp = rp->overflow)
                  func(stp, rp->key, rp->data, arg);
       trace((/*{*/"}\n")); }
